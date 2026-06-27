package com.example.myweibo.data

import android.content.Context
import androidx.media3.muxer.MuxerUtil
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

/**
 * Builds spec-compliant Motion Photos via AndroidX Media3 (Google Motion Photo 1.0).
 */
object MotionPhotoMuxer {
    fun buildMotionPhoto(
        context: Context,
        jpegBytes: ByteArray,
        videoBytes: ByteArray,
        presentationTimestampUs: Long,
    ): ByteArray {
        require(jpegBytes.isNotEmpty()) { "Motion Photo 缺少静态图" }
        require(videoBytes.isNotEmpty()) { "Motion Photo 缺少视频" }
        val trimmedJpeg = trimJpegToEoi(jpegBytes)
        require(looksLikeJpeg(trimmedJpeg)) { "Motion Photo 静态图必须是 JPEG" }
        require(MotionPhotoVideoPreparer.looksLikeMp4(videoBytes)) { "Motion Photo 视频必须是 MP4" }

        val jpegFile = File.createTempFile("motion_jpeg_", ".jpg", context.cacheDir)
        val videoFile = File.createTempFile("motion_video_", ".mp4", context.cacheDir)
        val outputFile = File.createTempFile("motion_out_", ".jpg", context.cacheDir)
        return try {
            jpegFile.writeBytes(trimmedJpeg)
            videoFile.writeBytes(videoBytes)
            FileOutputStream(outputFile).channel.use { outputChannel ->
                MuxerUtil.createMotionPhotoFromJpegImageAndBmffVideo(
                    FileInputStream(jpegFile),
                    normalizePresentationTimestampUs(presentationTimestampUs),
                    FileInputStream(videoFile),
                    "video/mp4",
                    outputChannel,
                )
            }
            outputFile.readBytes().also { bytes ->
                MotionPhotoValidator.validate(bytes)?.let { reason ->
                    throw IllegalStateException("Media3 Motion Photo 校验失败：$reason")
                }
            }
        } finally {
            jpegFile.delete()
            videoFile.delete()
            outputFile.delete()
        }
    }

    private fun normalizePresentationTimestampUs(value: Long): Long =
        when {
            value > 0L -> value
            else -> 500_000L
        }

    private fun trimJpegToEoi(jpeg: ByteArray): ByteArray {
        for (index in jpeg.size - 2 downTo 0) {
            if (jpeg[index] == 0xFF.toByte() && jpeg[index + 1] == 0xD9.toByte()) {
                return jpeg.copyOfRange(0, index + 2)
            }
        }
        return jpeg
    }

    private fun looksLikeJpeg(bytes: ByteArray): Boolean =
        bytes.size >= 3 &&
            bytes[0] == 0xFF.toByte() &&
            bytes[1] == 0xD8.toByte() &&
            bytes[2] == 0xFF.toByte()
}
