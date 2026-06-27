package com.example.myweibo.data

import android.content.Context
import android.graphics.BitmapFactory

/**
 * 将微博 Live Photo（静图 + 短视频）组装为 Android Motion Photo 1.0 单文件。
 *
 * 文件结构（小米 / Google MVIMG 通用格式）：
 * ```
 * [Primary JPEG + XMP APP1] + [MP4 直接拼接在文件末尾]
 * ```
 */
object LivePhotoMotionPhotoAssembler {
    data class AssembledMotionPhoto(
        val bytes: ByteArray,
        val videoLength: Int,
        val displayName: String,
    )

    fun assemble(
        context: Context,
        image: FeedImage,
        imageBytes: ByteArray,
        videoUrl: String,
        uniqueSuffix: String = "",
    ): Result<AssembledMotionPhoto> = runCatching {
        val jpegBytes = ensureJpegBytes(imageBytes)
            ?: throw IllegalStateException("Live Photo 静态图解码失败")
        val videoBytes = downloadVideoBytes(videoUrl)
        val preparedVideo = MotionPhotoVideoPreparer.prepare(context, videoBytes)
        if (preparedVideo.isEmpty()) {
            throw IllegalStateException("Live Photo 视频为空")
        }
        val presentationTimestampUs = MotionPhotoVideoPreparer.extractPresentationTimestampUs(
            context,
            preparedVideo,
        )
        val motionBytes = runCatching {
            MotionPhotoMuxer.buildMotionPhoto(
                context = context,
                jpegBytes = jpegBytes,
                videoBytes = preparedVideo,
                presentationTimestampUs = presentationTimestampUs,
            )
        }.getOrElse {
            MotionPhotoWriter.buildMotionPhoto(
                jpegBytes = jpegBytes,
                videoBytes = preparedVideo,
                presentationTimestampUs = presentationTimestampUs,
            )
        }
        val videoLength = MotionPhotoValidator.embeddedVideoLength(motionBytes) ?: preparedVideo.size
        AssembledMotionPhoto(
            bytes = motionBytes,
            videoLength = videoLength,
            displayName = buildDisplayName(image.id, uniqueSuffix),
        )
    }

    fun buildDisplayName(imageId: String, uniqueSuffix: String = ""): String {
        val timestamp = java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.US)
            .format(java.util.Date())
        // Gallery apps (Google / Xiaomi / Samsung) often require the MV prefix.
        return "MVIMG_${timestamp}${uniqueSuffix}_MP.jpg"
    }

    private fun ensureJpegBytes(bytes: ByteArray): ByteArray? {
        if (looksLikeJpeg(bytes)) return bytes
        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size) ?: return null
        val output = java.io.ByteArrayOutputStream()
        if (!bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 95, output)) {
            return null
        }
        return output.toByteArray()
    }

    private fun looksLikeJpeg(bytes: ByteArray): Boolean =
        bytes.size >= 3 &&
            bytes[0] == 0xFF.toByte() &&
            bytes[1] == 0xD8.toByte() &&
            bytes[2] == 0xFF.toByte()

    private fun downloadVideoBytes(videoUrl: String): ByteArray {
        val candidates = videoUrl.trim().let { url ->
            if (url.startsWith("http://", ignoreCase = true)) {
                listOf(url.replaceFirst("http://", "https://", ignoreCase = true), url)
            } else {
                listOf(url)
            }
        }
        return candidates.firstNotNullOfOrNull { candidate ->
            runCatching { downloadVideoBytesFromUrl(candidate) }.getOrNull()
        } ?: throw IllegalStateException("Live Photo 视频下载失败")
    }

    private fun downloadVideoBytesFromUrl(url: String): ByteArray {
        var lastError: Exception? = null
        repeat(2) { attempt ->
            try {
                val connection = (java.net.URL(url).openConnection() as java.net.HttpURLConnection).apply {
                    connectTimeout = 8_000
                    readTimeout = 12_000
                    setRequestProperty("User-Agent", USER_AGENT)
                    setRequestProperty("Referer", "https://weibo.com/")
                    setRequestProperty("Origin", "https://weibo.com")
                    android.webkit.CookieManager.getInstance()
                        .getCookie("https://weibo.com/")
                        ?.takeIf { it.isNotBlank() }
                        ?.let { setRequestProperty("Cookie", it) }
                }
                return connection.inputStream.use { input ->
                    val buffer = ByteArray(8192)
                    val output = java.io.ByteArrayOutputStream()
                    while (true) {
                        val read = input.read(buffer)
                        if (read <= 0) break
                        output.write(buffer, 0, read)
                        if (output.size() > 64 * 1024 * 1024) {
                            throw IllegalStateException("Live Photo 视频体积过大")
                        }
                    }
                    output.toByteArray()
                }
            } catch (error: Exception) {
                lastError = error
                if (attempt < 1) Thread.sleep(200L)
            }
        }
        throw lastError ?: IllegalStateException("Live Photo 视频下载失败")
    }

    private const val USER_AGENT =
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 " +
            "(KHTML, like Gecko) Chrome/126.0.0.0 Safari/537.36"
}
