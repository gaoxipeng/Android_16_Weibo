package com.example.myweibo.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaDataSource
import android.media.MediaMetadataRetriever

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
        var preparedVideo = MotionPhotoVideoPreparer.prepare(
            context = context,
            sourceBytes = videoBytes,
        )
        if (preparedVideo.isEmpty()) {
            throw IllegalStateException("Live Photo video is empty")
        }
        val stillBitmap = decodeStillBitmap(imageBytes)
            ?: throw IllegalStateException("Live Photo still image decode failed")
        if (shouldMirrorVideoToMatchStill(stillBitmap, preparedVideo)) {
            preparedVideo = MotionPhotoVideoPreparer.mirrorVideoPixelsHorizontally(context, preparedVideo)
        }
        val jpegBytes = compressJpeg(stillBitmap)
            ?: throw IllegalStateException("Live Photo still image decode failed")
        val presentationTimestampUs = MotionPhotoVideoPreparer.extractPresentationTimestampUs(
            context,
            preparedVideo,
        )
        val motionBytes = runCatching {
            MotionPhotoWriter.buildMotionPhoto(
                jpegBytes = jpegBytes,
                videoBytes = preparedVideo,
                presentationTimestampUs = presentationTimestampUs,
            )
        }.getOrElse {
            MotionPhotoMuxer.buildMotionPhoto(
                context = context,
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

    private fun shouldMirrorVideoToMatchStill(still: Bitmap, videoBytes: ByteArray): Boolean {
        val referenceFrame = loadLivePhotoReferenceFrame(videoBytes) ?: return false
        val normalError = normalizedFrameError(still, referenceFrame, mirrorStill = false)
        val mirroredError = normalizedFrameError(still, referenceFrame, mirrorStill = true)
        return mirroredError < normalError * 0.9f
    }

    private fun loadLivePhotoReferenceFrame(videoBytes: ByteArray): Bitmap? =
        runCatching {
            MediaMetadataRetriever().use { retriever ->
                retriever.setDataSource(ByteArrayMediaDataSource(videoBytes))
                livePhotoReferenceFrameTimesUs(retriever)
                    .firstNotNullOfOrNull { timeUs ->
                        retriever.getFrameAtTime(timeUs, MediaMetadataRetriever.OPTION_CLOSEST)
                            ?.takeIf(::hasUsableFrameSignal)
                    }
                    ?: retriever.frameAtTime?.takeIf(::hasUsableFrameSignal)
            }
        }.getOrNull()

    private fun livePhotoReferenceFrameTimesUs(retriever: MediaMetadataRetriever): List<Long> {
        val durationMs = retriever
            .extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            ?.toLongOrNull()
            ?.takeIf { it > 0L }
        if (durationMs == null) {
            return listOf(500_000L, 250_000L, 750_000L, 100_000L, 0L)
        }
        val durationUs = durationMs * 1000L
        return listOf(
            durationUs / 2L,
            (durationUs * 45L) / 100L,
            (durationUs * 55L) / 100L,
            (durationUs * 35L) / 100L,
            (durationUs * 65L) / 100L,
            minOf(100_000L, durationUs),
            0L,
        ).distinct()
    }

    private fun hasUsableFrameSignal(bitmap: Bitmap): Boolean {
        val sampleCount = 16
        var minLuma = 255
        var maxLuma = 0
        for (yIndex in 0 until sampleCount) {
            val y = normalizedSampleCoordinate(yIndex, sampleCount, bitmap.height)
            for (xIndex in 0 until sampleCount) {
                val x = normalizedSampleCoordinate(xIndex, sampleCount, bitmap.width)
                val luma = bitmap.getPixel(x, y).pixelLuma()
                minLuma = minOf(minLuma, luma)
                maxLuma = maxOf(maxLuma, luma)
            }
        }
        return maxLuma - minLuma > 12
    }

    private fun normalizedFrameError(
        still: Bitmap,
        frame: Bitmap,
        mirrorStill: Boolean,
    ): Float {
        val sampleCount = 40
        var error = 0L
        for (yIndex in 0 until sampleCount) {
            val stillY = normalizedSampleCoordinate(yIndex, sampleCount, still.height)
            val frameY = normalizedSampleCoordinate(yIndex, sampleCount, frame.height)
            for (xIndex in 0 until sampleCount) {
                val stillX = normalizedSampleCoordinate(
                    if (mirrorStill) sampleCount - 1 - xIndex else xIndex,
                    sampleCount,
                    still.width,
                )
                val frameX = normalizedSampleCoordinate(xIndex, sampleCount, frame.width)
                error += colorDistance(still.getPixel(stillX, stillY), frame.getPixel(frameX, frameY))
            }
        }
        return error.toFloat() / (sampleCount * sampleCount)
    }

    private fun normalizedSampleCoordinate(index: Int, sampleCount: Int, size: Int): Int {
        if (size <= 1 || sampleCount <= 1) return 0
        return ((index.toFloat() / (sampleCount - 1).toFloat()) * (size - 1)).toInt()
            .coerceIn(0, size - 1)
    }

    private fun colorDistance(a: Int, b: Int): Int {
        val dr = ((a shr 16) and 0xff) - ((b shr 16) and 0xff)
        val dg = ((a shr 8) and 0xff) - ((b shr 8) and 0xff)
        val db = (a and 0xff) - (b and 0xff)
        return dr * dr + dg * dg + db * db
    }

    private fun Int.pixelLuma(): Int {
        val r = (this shr 16) and 0xff
        val g = (this shr 8) and 0xff
        val b = this and 0xff
        return (r * 30 + g * 59 + b * 11) / 100
    }

    private class ByteArrayMediaDataSource(private val bytes: ByteArray) : MediaDataSource() {
        override fun getSize(): Long = bytes.size.toLong()

        override fun readAt(position: Long, buffer: ByteArray, offset: Int, size: Int): Int {
            if (position < 0L || position >= bytes.size) return -1
            val length = minOf(size, bytes.size - position.toInt())
            System.arraycopy(bytes, position.toInt(), buffer, offset, length)
            return length
        }

        override fun close() = Unit
    }

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
                    while (true) {
                        val read = input.read(buffer)
                        if (read <= 0) break
                        output.write(buffer, 0, read)
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
}
