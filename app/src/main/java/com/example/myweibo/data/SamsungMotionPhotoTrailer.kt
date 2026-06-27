package com.example.myweibo.data

import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Samsung SEF trailer used by cross-vendor Motion Photo writers.
 * @see <a href="https://github.com/doodspav/motionphoto">doodspav/motionphoto</a>
 */
internal data class SamsungMotionPhotoTrailer(
    val data: ByteArray,
    /** Bytes from EOF to embedded MP4 start (MicroVideoOffset). */
    val negativeVideoOffset: Int,
    /** Bytes from JPEG EOI to MP4 start (Container Primary Padding). */
    val primaryPadding: Int,
) {
    companion object {
        private val EMBEDDED_VIDEO_MARKER = byteArrayOf(0x00, 0x00, 0x30, 0x0A)
        private val EMBEDDED_VIDEO_LABEL = "MotionPhoto_Data".toByteArray(Charsets.US_ASCII)
        private const val EMBEDDED_VIDEO_LABEL_LENGTH = 16
        private const val SEF_VERSION = 106

        fun create(videoBytes: ByteArray): SamsungMotionPhotoTrailer {
            require(videoBytes.isNotEmpty()) { "Motion Photo 视频为空" }
            val trailer = ArrayList<Byte>()
            val embeddedVideoTagOffset = trailer.size
            trailer.addAll(EMBEDDED_VIDEO_MARKER.toList())
            trailer.addAll(int32LE(EMBEDDED_VIDEO_LABEL_LENGTH).toList())
            trailer.addAll(EMBEDDED_VIDEO_LABEL.toList())
            val videoOffsetPositive = trailer.size
            trailer.addAll(videoBytes.toList())

            val sefHeadOffset = trailer.size
            trailer.addAll("SEFH".toByteArray(Charsets.US_ASCII).toList())
            trailer.addAll(int32LE(SEF_VERSION).toList())
            trailer.addAll(int32LE(1).toList())
            trailer.addAll(EMBEDDED_VIDEO_MARKER.toList())
            trailer.addAll(int32LE(sefHeadOffset - embeddedVideoTagOffset).toList())
            trailer.addAll(int32LE(sefHeadOffset - embeddedVideoTagOffset).toList())

            val sefDataSize = trailer.size - sefHeadOffset
            trailer.addAll(int32LE(sefDataSize).toList())
            trailer.addAll("SEFT".toByteArray(Charsets.US_ASCII).toList())

            val bytes = trailer.toByteArray()
            return SamsungMotionPhotoTrailer(
                data = bytes,
                negativeVideoOffset = bytes.size - videoOffsetPositive,
                primaryPadding = videoOffsetPositive,
            )
        }

        private fun int32LE(value: Int): ByteArray =
            ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(value).array()
    }
}
