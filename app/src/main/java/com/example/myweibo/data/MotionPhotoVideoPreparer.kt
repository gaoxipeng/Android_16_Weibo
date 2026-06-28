package com.example.myweibo.data

import android.content.Context
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMuxer
import java.io.File
import java.nio.ByteBuffer

object MotionPhotoVideoPreparer {
    fun looksLikeMp4(bytes: ByteArray): Boolean {
        if (bytes.size < 12) return false
        val scanLimit = minOf(bytes.size - 4, 64)
        for (index in 0..scanLimit) {
            if (bytes[index] == 'f'.code.toByte() &&
                bytes[index + 1] == 't'.code.toByte() &&
                bytes[index + 2] == 'y'.code.toByte() &&
                bytes[index + 3] == 'p'.code.toByte()
            ) {
                return true
            }
        }
        return false
    }

    fun prepare(
        context: Context,
        sourceBytes: ByteArray,
    ): ByteArray {
        val normalized = normalizeMp4Payload(sourceBytes)
        if (!looksLikeMp4(normalized)) {
            throw IllegalStateException("Live Photo 视频格式无效")
        }
        return runCatching {
            remuxMp4(context, normalized)
        }.getOrElse { normalized }
    }

    private fun normalizeMp4Payload(bytes: ByteArray): ByteArray {
        val ftypIndex = findFtypOffset(bytes) ?: return bytes
        if (ftypIndex < 4) return bytes
        val boxStart = ftypIndex - 4
        if (boxStart == 0) return bytes
        val boxSize = readUInt32(bytes, boxStart)
        return if (boxSize >= 8L && boxStart + boxSize <= bytes.size) {
            bytes.copyOfRange(boxStart, bytes.size)
        } else {
            bytes.copyOfRange(boxStart, bytes.size)
        }
    }

    private fun readUInt32(source: ByteArray, offset: Int): Long =
        ((source[offset].toLong() and 0xFFL) shl 24) or
            ((source[offset + 1].toLong() and 0xFFL) shl 16) or
            ((source[offset + 2].toLong() and 0xFFL) shl 8) or
            (source[offset + 3].toLong() and 0xFFL)

    private fun findFtypOffset(bytes: ByteArray): Int? {
        val scanLimit = minOf(bytes.size - 4, 64)
        for (index in 0..scanLimit) {
            if (bytes[index] == 'f'.code.toByte() &&
                bytes[index + 1] == 't'.code.toByte() &&
                bytes[index + 2] == 'y'.code.toByte() &&
                bytes[index + 3] == 'p'.code.toByte()
            ) {
                return index
            }
        }
        return null
    }

    fun extractPresentationTimestampUs(context: Context, videoBytes: ByteArray): Long {
        if (videoBytes.isEmpty()) return 0L
        val inputFile = File.createTempFile("motion_ts_", ".mp4", context.cacheDir)
        val extractor = MediaExtractor()
        return try {
            inputFile.writeBytes(videoBytes)
            extractor.setDataSource(inputFile.absolutePath)
            var earliestVideoUs = Long.MAX_VALUE
            for (trackIndex in 0 until extractor.trackCount) {
                val mime = extractor.getTrackFormat(trackIndex)
                    .getString(MediaFormat.KEY_MIME)
                    ?.takeIf { it.startsWith("video/") }
                    ?: continue
                if (mime.isBlank()) continue
                extractor.selectTrack(trackIndex)
                val sampleTime = extractor.sampleTime
                if (sampleTime >= 0L) {
                    earliestVideoUs = minOf(earliestVideoUs, sampleTime)
                }
                extractor.unselectTrack(trackIndex)
            }
            if (earliestVideoUs == Long.MAX_VALUE) 0L else earliestVideoUs
        } catch (_: Exception) {
            0L
        } finally {
            runCatching { extractor.release() }
            inputFile.delete()
        }
    }

    private fun remuxMp4(context: Context, inputBytes: ByteArray): ByteArray {
        val inputFile = File.createTempFile("motion_in_", ".mp4", context.cacheDir)
        val outputFile = File.createTempFile("motion_out_", ".mp4", context.cacheDir)
        val extractor = MediaExtractor()
        var muxer: MediaMuxer? = null
        try {
            inputFile.writeBytes(inputBytes)
            extractor.setDataSource(inputFile.absolutePath)
            if (extractor.trackCount <= 0) {
                throw IllegalStateException("Live Photo 视频无可用轨道")
            }
            muxer = MediaMuxer(outputFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
            val trackIndexMap = IntArray(extractor.trackCount)
            for (trackIndex in 0 until extractor.trackCount) {
                trackIndexMap[trackIndex] = muxer.addTrack(extractor.getTrackFormat(trackIndex))
            }
            muxer.start()
            val buffer = ByteBuffer.allocate(2 * 1024 * 1024)
            val bufferInfo = MediaCodec.BufferInfo()
            for (trackIndex in 0 until extractor.trackCount) {
                extractor.selectTrack(trackIndex)
                extractor.seekTo(0, MediaExtractor.SEEK_TO_CLOSEST_SYNC)
                while (true) {
                    buffer.clear()
                    val sampleSize = extractor.readSampleData(buffer, 0)
                    if (sampleSize < 0) break
                    bufferInfo.offset = 0
                    bufferInfo.size = sampleSize
                    bufferInfo.presentationTimeUs = extractor.sampleTime
                    bufferInfo.flags = extractor.sampleFlags
                    muxer.writeSampleData(trackIndexMap[trackIndex], buffer, bufferInfo)
                    if (!extractor.advance()) break
                }
                extractor.unselectTrack(trackIndex)
            }
            muxer.stop()
            val remuxed = outputFile.readBytes()
            if (!looksLikeMp4(remuxed) || remuxed.isEmpty()) {
                throw IllegalStateException("Live Photo 视频重封装失败")
            }
            return remuxed
        } finally {
            runCatching { muxer?.release() }
            runCatching { extractor.release() }
            inputFile.delete()
            outputFile.delete()
        }
    }
}
