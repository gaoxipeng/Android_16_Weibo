package com.example.myweibo.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory

/**
 * Builds an Android Motion Photo from a Weibo Live Photo still image and video.
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
        val videoBytes = downloadVideoBytes(videoUrl)
        val stillBitmap = decodeStillBitmap(imageBytes)
            ?: throw IllegalStateException("Live Photo still image decode failed")
        val mirrorVideo = LivePhotoMirrorDetector.shouldMirrorVideoToMatchStill(stillBitmap, videoBytes)
        val preparedVideo = MotionPhotoVideoPreparer.prepareForMotionPhoto(
            context = context,
            sourceBytes = videoBytes,
            mirror = mirrorVideo,
        )
        if (preparedVideo.isEmpty()) {
            throw IllegalStateException("Live Photo video is empty")
        }
        val jpegBytes = compressJpeg(stillBitmap)
            ?: throw IllegalStateException("Live Photo still image decode failed")
        val presentationTimestampUs = MotionPhotoVideoPreparer.extractMotionPhotoPresentationTimestampUs(
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
        return "MVIMG_${timestamp}${uniqueSuffix}_MP.jpg"
    }

    private fun decodeStillBitmap(imageBytes: ByteArray): Bitmap? =
        BitmapExifOrientation.decodeSampledBitmap(imageBytes, maxDecodeDim = 8192)
            ?: BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)

    private fun compressJpeg(bitmap: Bitmap): ByteArray? {
        val output = java.io.ByteArrayOutputStream()
        if (!bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 95, output)) {
            return null
        }
        return output.toByteArray()
    }

    private fun downloadVideoBytes(videoUrl: String): ByteArray {
        return MediaUrlResolver.livePhotoVideoCandidates(videoUrl).firstNotNullOfOrNull { candidate ->
            runCatching { downloadVideoBytesFromUrl(candidate) }.getOrNull()
        } ?: throw IllegalStateException("Live Photo video download failed")
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
                    val startedAt = System.currentTimeMillis()
                    while (true) {
                        val read = input.read(buffer)
                        if (read <= 0) break
                        output.write(buffer, 0, read)
                        if (System.currentTimeMillis() - startedAt > VIDEO_DOWNLOAD_MAX_DURATION_MS) {
                            throw java.net.SocketTimeoutException("Live Photo video download timed out")
                        }
                        if (output.size() > 64 * 1024 * 1024) {
                            throw IllegalStateException("Live Photo video is too large")
                        }
                    }
                    output.toByteArray()
                }
            } catch (error: Exception) {
                lastError = error
                if (attempt < 1) Thread.sleep(200L)
            }
        }
        throw lastError ?: IllegalStateException("Live Photo video download failed")
    }

    private const val USER_AGENT =
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 " +
            "(KHTML, like Gecko) Chrome/126.0.0.0 Safari/537.36"
    private const val VIDEO_DOWNLOAD_MAX_DURATION_MS = 45_000L
}
