package com.example.myweibo.data

import android.graphics.Bitmap
import android.media.MediaDataSource
import android.media.MediaMetadataRetriever
import android.webkit.CookieManager
import java.io.File
/**
 * Compares a Live Photo still frame with a reference video frame to decide whether
 * the video needs a horizontal mirror to match the cover image.
 */
object LivePhotoMirrorDetector {
    private const val MIRROR_CONFIDENCE_RATIO = 0.78f
    private const val MIN_MIRROR_ERROR_IMPROVEMENT = 900f
    private const val USER_AGENT =
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 " +
            "(KHTML, like Gecko) Chrome/126.0.0.0 Safari/537.36"

    fun shouldMirrorVideoToMatchStill(
        still: Bitmap,
        videoBytes: ByteArray,
        videoUrl: String,
    ): Boolean {
        val referenceFrame = loadReferenceFrame(videoBytes, videoUrl) ?: return false
        return shouldMirrorVideoToMatchStill(still, referenceFrame)
    }

    fun shouldMirrorVideoToMatchStill(still: Bitmap, videoBytes: ByteArray): Boolean {
        val referenceFrame = loadReferenceFrameFromBytes(videoBytes) ?: return false
        return shouldMirrorVideoToMatchStill(still, referenceFrame)
    }
    fun shouldMirrorVideoToMatchStill(still: Bitmap, referenceFrame: Bitmap): Boolean {
        val normalError = normalizedFrameError(still, referenceFrame, mirrorStill = false)
        val mirroredError = normalizedFrameError(still, referenceFrame, mirrorStill = true)
        return mirroredError < normalError * MIRROR_CONFIDENCE_RATIO &&
            normalError - mirroredError > MIN_MIRROR_ERROR_IMPROVEMENT
    }

    fun loadReferenceFrame(
        videoBytes: ByteArray,
        videoUrl: String,
    ): Bitmap? {
        if (videoUrl.isNotBlank()) {
            loadReferenceFrameFromUrl(videoUrl)?.let { return it }
        }
        return loadReferenceFrameFromBytes(videoBytes)
    }

    fun loadReferenceFrameFromBytes(videoBytes: ByteArray): Bitmap? =
        runCatching {
            MediaMetadataRetriever().use { retriever ->
                retriever.setDataSource(ByteArrayMediaDataSource(videoBytes))
                extractReferenceFrame(retriever)
            }
        }.getOrNull()

    fun loadReferenceFrameFromUrl(videoUrl: String): Bitmap? {
        val headers = livePhotoFrameHeaders()
        for (candidate in MediaUrlResolver.livePhotoVideoCandidates(videoUrl)) {
            val frame = runCatching {
                MediaMetadataRetriever().use { retriever ->
                    retriever.setDataSource(candidate, headers)
                    extractReferenceFrame(retriever)
                }
            }.getOrNull()
            if (frame != null) return frame
        }
        return null
    }

    fun loadReferenceFrameFromFile(file: File): Bitmap? =
        runCatching {
            MediaMetadataRetriever().use { retriever ->
                retriever.setDataSource(file.absolutePath)
                extractReferenceFrame(retriever)
            }
        }.getOrNull()

    private fun extractReferenceFrame(retriever: MediaMetadataRetriever): Bitmap? =
        livePhotoReferenceFrameTimesUs(retriever)
            .firstNotNullOfOrNull { timeUs ->
                retriever.getFrameAtTime(timeUs, MediaMetadataRetriever.OPTION_CLOSEST)
                    ?.takeIf(::hasUsableFrameSignal)
            }
            ?: retriever.frameAtTime?.takeIf(::hasUsableFrameSignal)

    private fun livePhotoFrameHeaders(): Map<String, String> {
        val cookie = CookieManager.getInstance().getCookie("https://weibo.com/").orEmpty()
        return buildMap {
            put("User-Agent", USER_AGENT)
            put("Referer", "https://weibo.com/")
            if (cookie.isNotBlank()) put("Cookie", cookie)
        }
    }

    fun loadReferenceFrame(videoBytes: ByteArray): Bitmap? = loadReferenceFrameFromBytes(videoBytes)
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
}
