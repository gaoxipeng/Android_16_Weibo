package com.example.myweibo.data

import android.content.Context
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMetadataRetriever
import android.media.MediaMuxer
import android.net.Uri
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.effect.ScaleAndRotateTransformation
import androidx.media3.transformer.Composition
import androidx.media3.transformer.DefaultEncoderFactory
import androidx.media3.transformer.EditedMediaItem
import androidx.media3.transformer.Effects
import androidx.media3.transformer.ExportException
import androidx.media3.transformer.ExportResult
import androidx.media3.transformer.Transformer
import androidx.media3.transformer.VideoEncoderSettings
import java.io.File
import java.nio.ByteBuffer
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

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
    ): ByteArray = prepareForMotionPhoto(
        context = context,
        sourceBytes = sourceBytes,
        mirror = false,
    )

    fun prepareForMotionPhoto(
        context: Context,
        sourceBytes: ByteArray,
        mirror: Boolean,
    ): ByteArray {
        val normalized = normalizeMp4Payload(sourceBytes)
        if (!looksLikeMp4(normalized)) {
            throw IllegalStateException("Live Photo 视频格式无效")
        }
        if (!mirror) return normalized
        return mirrorVideoPixelsHorizontally(context, normalized)
    }

    private data class VideoEncodingHint(
        val mimeType: String?,
        val width: Int,
        val height: Int,
        val frameRate: Int,
        val bitrate: Int,
    )

    private fun readVideoEncodingHint(context: Context, videoBytes: ByteArray): VideoEncodingHint {
        val file = File.createTempFile("motion_hint_", ".mp4", context.cacheDir)
        val extractor = MediaExtractor()
        val initialHint = try {
            file.writeBytes(videoBytes)
            extractor.setDataSource(file.absolutePath)
            var hint: VideoEncodingHint? = null
            for (trackIndex in 0 until extractor.trackCount) {
                val format = extractor.getTrackFormat(trackIndex)
                val mime = format.getString(MediaFormat.KEY_MIME) ?: continue
                if (!mime.startsWith("video/")) continue
                val width = format.getInteger(MediaFormat.KEY_WIDTH)
                val height = format.getInteger(MediaFormat.KEY_HEIGHT)
                val bitrate = if (format.containsKey(MediaFormat.KEY_BIT_RATE)) {
                    format.getInteger(MediaFormat.KEY_BIT_RATE)
                } else {
                    0
                }
                val frameRate = if (format.containsKey(MediaFormat.KEY_FRAME_RATE)) {
                    format.getInteger(MediaFormat.KEY_FRAME_RATE)
                } else {
                    30
                }
                hint = VideoEncodingHint(
                    mimeType = mime,
                    width = width,
                    height = height,
                    frameRate = frameRate.coerceAtLeast(24),
                    bitrate = bitrate,
                )
                break
            }
            hint ?: VideoEncodingHint(MimeTypes.VIDEO_H264, 720, 1280, 30, 0)
        } catch (_: Exception) {
            VideoEncodingHint(MimeTypes.VIDEO_H264, 720, 1280, 30, 0)
        } finally {
            runCatching { extractor.release() }
            file.delete()
        }
        if (initialHint.bitrate > 0) return initialHint
        return readVideoBitrateFromMetadata(context, videoBytes) ?: initialHint
    }

    private fun readVideoBitrateFromMetadata(context: Context, videoBytes: ByteArray): VideoEncodingHint? {
        val file = File.createTempFile("motion_meta_", ".mp4", context.cacheDir)
        return try {
            file.writeBytes(videoBytes)
            MediaMetadataRetriever().use { retriever ->
                retriever.setDataSource(file.absolutePath)
                val bitrate = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE)
                    ?.toIntOrNull()
                    ?.takeIf { it > 0 }
                    ?: return null
                val width = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)
                    ?.toIntOrNull()
                    ?.takeIf { it > 0 }
                    ?: 720
                val height = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)
                    ?.toIntOrNull()
                    ?.takeIf { it > 0 }
                    ?: 1280
                val frameRate = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CAPTURE_FRAMERATE)
                    ?.toFloatOrNull()
                    ?.toInt()
                    ?.coerceAtLeast(24)
                    ?: 30
                VideoEncodingHint(
                    mimeType = MimeTypes.VIDEO_H264,
                    width = width,
                    height = height,
                    frameRate = frameRate,
                    bitrate = bitrate,
                )
            }
        } catch (_: Exception) {
            null
        } finally {
            file.delete()
        }
    }

    private fun targetMirrorEncoderBitrate(hint: VideoEncodingHint): Int {
        val pixels = hint.width.toLong() * hint.height
        val estimated = (pixels * 0.14 * hint.frameRate).toLong().toInt()
        return maxOf(hint.bitrate, estimated, 4_000_000).coerceAtMost(20_000_000)
    }

    fun extractMotionPhotoPresentationTimestampUs(
        context: Context,
        videoBytes: ByteArray,
    ): Long {
        val firstVideoUs = extractPresentationTimestampUs(context, videoBytes)
        if (firstVideoUs > 0L) return firstVideoUs
        val durationUs = extractDurationUs(context, videoBytes)
        if (durationUs > 0L) return durationUs / 2L
        return 500_000L
    }

    fun applyHorizontalMirrorDisplayMatrix(videoBytes: ByteArray): ByteArray {
        val patched = videoBytes.copyOf()
        return if (patchFirstVideoTrackMatrix(patched) && looksLikeMp4(patched)) patched else videoBytes
    }

    fun mirrorVideoPixelsHorizontally(
        context: Context,
        videoBytes: ByteArray,
    ): ByteArray {
        val appContext = context.applicationContext
        val completionLatch = CountDownLatch(1)
        val resultRef = AtomicReference<ByteArray>()
        val errorRef = AtomicReference<Exception?>()
        TransformerWorker.handler.post {
            val inputFile = File.createTempFile("motion_mirror_in_", ".mp4", appContext.cacheDir)
            val outputFile = File.createTempFile("motion_mirror_out_", ".mp4", appContext.cacheDir)
            fun cleanupTempFiles() {
                inputFile.delete()
                outputFile.delete()
            }
            try {
                inputFile.writeBytes(videoBytes)
                val encodingHint = readVideoEncodingHint(appContext, videoBytes)
                val encoderFactory = DefaultEncoderFactory.Builder(appContext)
                    .setRequestedVideoEncoderSettings(
                        VideoEncoderSettings.Builder()
                            .setBitrate(targetMirrorEncoderBitrate(encodingHint))
                            .build(),
                    )
                    .build()
                val effect = ScaleAndRotateTransformation.Builder()
                    .setScale(-1f, 1f)
                    .build()
                val editedMediaItem = EditedMediaItem.Builder(MediaItem.fromUri(Uri.fromFile(inputFile)))
                    .setEffects(Effects(emptyList(), listOf(effect)))
                    .build()
                val transformerBuilder = Transformer.Builder(appContext)
                    .setLooper(TransformerWorker.looper)
                    .setEncoderFactory(encoderFactory)
                when (encodingHint.mimeType) {
                    MimeTypes.VIDEO_H264, MimeTypes.VIDEO_H265 -> {
                        transformerBuilder.setVideoMimeType(encodingHint.mimeType)
                    }
                }
                val transformer = transformerBuilder
                    .addListener(
                        object : Transformer.Listener {
                            override fun onCompleted(composition: Composition, exportResult: ExportResult) {
                                try {
                                    val mirrored = outputFile.readBytes()
                                    if (!looksLikeMp4(mirrored) || mirrored.isEmpty()) {
                                        errorRef.set(
                                            IllegalStateException("Live Photo video mirror transform failed"),
                                        )
                                    } else {
                                        resultRef.set(mirrored)
                                    }
                                } catch (error: Exception) {
                                    errorRef.set(error)
                                } finally {
                                    cleanupTempFiles()
                                    completionLatch.countDown()
                                }
                            }

                            override fun onError(
                                composition: Composition,
                                exportResult: ExportResult,
                                exportException: ExportException,
                            ) {
                                errorRef.set(exportException)
                                cleanupTempFiles()
                                completionLatch.countDown()
                            }
                        },
                    )
                    .build()
                transformer.start(editedMediaItem, outputFile.absolutePath)
            } catch (error: Exception) {
                errorRef.set(error)
                cleanupTempFiles()
                completionLatch.countDown()
            }
        }
        if (!completionLatch.await(120, TimeUnit.SECONDS)) {
            throw IllegalStateException("Live Photo video mirror transform timed out")
        }
        errorRef.get()?.let { throw it }
        return resultRef.get()
            ?: throw IllegalStateException("Live Photo video mirror transform failed")
    }

    private object TransformerWorker {
        private val thread = HandlerThread("MotionPhotoTransformer").apply { start() }
        val looper: Looper = thread.looper
        val handler = Handler(looper)
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

    private fun readInt32(source: ByteArray, offset: Int): Int =
        ((source[offset].toInt() and 0xFF) shl 24) or
            ((source[offset + 1].toInt() and 0xFF) shl 16) or
            ((source[offset + 2].toInt() and 0xFF) shl 8) or
            (source[offset + 3].toInt() and 0xFF)

    private fun writeInt32(target: ByteArray, offset: Int, value: Int) {
        target[offset] = (value ushr 24).toByte()
        target[offset + 1] = (value ushr 16).toByte()
        target[offset + 2] = (value ushr 8).toByte()
        target[offset + 3] = value.toByte()
    }

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

    private fun extractDurationUs(context: Context, videoBytes: ByteArray): Long {
        if (videoBytes.isEmpty()) return 0L
        val inputFile = File.createTempFile("motion_dur_", ".mp4", context.cacheDir)
        return try {
            inputFile.writeBytes(videoBytes)
            MediaMetadataRetriever().use { retriever ->
                retriever.setDataSource(inputFile.absolutePath)
                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                    ?.toLongOrNull()
                    ?.takeIf { it > 0L }
                    ?.times(1_000L)
                    ?: 0L
            }
        } catch (_: Exception) {
            0L
        } finally {
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
            val embedTrackOrder = buildMotionPhotoEmbedTrackOrder(extractor)
            if (embedTrackOrder.isEmpty()) {
                throw IllegalStateException("Live Photo 视频无可用画面轨道")
            }
            muxer = MediaMuxer(outputFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
            readVideoRotationDegrees(inputFile)?.let(muxer::setOrientationHint)
            val sourceToMuxer = IntArray(extractor.trackCount) { -1 }
            embedTrackOrder.forEach { sourceIndex ->
                sourceToMuxer[sourceIndex] = muxer.addTrack(extractor.getTrackFormat(sourceIndex))
            }
            muxer.start()
            val samples = ArrayList<MuxSample>()
            val buffer = ByteBuffer.allocate(2 * 1024 * 1024)
            for (sourceIndex in embedTrackOrder) {
                extractor.selectTrack(sourceIndex)
                while (true) {
                    buffer.clear()
                    val sampleSize = extractor.readSampleData(buffer, 0)
                    if (sampleSize < 0) break
                    val sampleBuffer = ByteBuffer.allocate(sampleSize)
                    buffer.position(0)
                    buffer.limit(sampleSize)
                    sampleBuffer.put(buffer)
                    sampleBuffer.flip()
                    samples.add(
                        MuxSample(
                            muxerTrackIndex = sourceToMuxer[sourceIndex],
                            buffer = sampleBuffer,
                            info = MediaCodec.BufferInfo().apply {
                                offset = 0
                                size = sampleSize
                                presentationTimeUs = extractor.sampleTime
                                flags = extractor.sampleFlags
                            },
                        ),
                    )
                    if (!extractor.advance()) break
                }
                extractor.unselectTrack(sourceIndex)
            }
            samples.sortWith(compareBy({ it.info.presentationTimeUs }, { it.muxerTrackIndex }))
            samples.forEach { sample ->
                muxer.writeSampleData(sample.muxerTrackIndex, sample.buffer, sample.info)
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

    private fun buildMotionPhotoEmbedTrackOrder(extractor: MediaExtractor): List<Int> {
        val videoTracks = mutableListOf<Int>()
        val audioTracks = mutableListOf<Int>()
        for (trackIndex in 0 until extractor.trackCount) {
            val mime = extractor.getTrackFormat(trackIndex).getString(MediaFormat.KEY_MIME).orEmpty()
            when {
                mime.startsWith("video/") -> videoTracks.add(trackIndex)
                mime.startsWith("audio/") -> audioTracks.add(trackIndex)
            }
        }
        if (videoTracks.isEmpty()) return emptyList()
        return buildList {
            add(videoTracks.first())
            audioTracks.firstOrNull()?.let(::add)
        }
    }

    private data class MuxSample(
        val muxerTrackIndex: Int,
        val buffer: ByteBuffer,
        val info: MediaCodec.BufferInfo,
    )

    private fun readVideoRotationDegrees(file: File): Int? =
        runCatching {
            MediaMetadataRetriever().use { retriever ->
                retriever.setDataSource(file.absolutePath)
                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION)
                    ?.toIntOrNull()
                    ?.takeIf { it in setOf(90, 180, 270) }
            }
        }.getOrNull()

    private fun patchFirstVideoTrackMatrix(bytes: ByteArray): Boolean {
        val moov = findChildBox(bytes, 0, bytes.size, "moov") ?: return false
        for (trak in childBoxes(bytes, moov.contentStart, moov.contentEnd, "trak")) {
            if (!isVideoTrack(bytes, trak)) continue
            val tkhd = findChildBox(bytes, trak.contentStart, trak.contentEnd, "tkhd") ?: continue
            return patchTkhdHorizontalMirror(bytes, tkhd)
        }
        return false
    }

    private fun isVideoTrack(bytes: ByteArray, trak: Mp4Box): Boolean {
        val mdia = findChildBox(bytes, trak.contentStart, trak.contentEnd, "mdia") ?: return false
        val hdlr = findChildBox(bytes, mdia.contentStart, mdia.contentEnd, "hdlr") ?: return false
        val handlerOffset = hdlr.contentStart + 8
        if (handlerOffset + 4 > hdlr.contentEnd) return false
        return bytes.decodeAscii(handlerOffset, 4) == "vide"
    }

    private fun patchTkhdHorizontalMirror(bytes: ByteArray, tkhd: Mp4Box): Boolean {
        if (tkhd.contentStart + 4 > tkhd.contentEnd) return false
        val version = bytes[tkhd.contentStart].toInt() and 0xFF
        val matrixOffset = tkhd.contentStart + when (version) {
            1 -> 60
            else -> 40
        }
        val widthOffset = matrixOffset + 36
        if (widthOffset + 8 > tkhd.contentEnd) return false
        val widthFixed = readInt32(bytes, widthOffset)
        if (widthFixed == 0) return false

        val aOffset = matrixOffset
        val cOffset = matrixOffset + 12
        val xOffset = matrixOffset + 24
        val a = readInt32(bytes, aOffset)
        val c = readInt32(bytes, cOffset)
        val x = readInt32(bytes, xOffset)
        writeInt32(bytes, aOffset, -a)
        writeInt32(bytes, cOffset, -c)
        writeInt32(bytes, xOffset, widthFixed - x)
        return true
    }

    private fun findChildBox(
        bytes: ByteArray,
        start: Int,
        end: Int,
        type: String,
    ): Mp4Box? = childBoxes(bytes, start, end, type).firstOrNull()

    private fun childBoxes(
        bytes: ByteArray,
        start: Int,
        end: Int,
        type: String? = null,
    ): Sequence<Mp4Box> = sequence {
        var offset = start
        while (offset + 8 <= end && offset + 8 <= bytes.size) {
            val size32 = readUInt32(bytes, offset)
            val boxType = bytes.decodeAscii(offset + 4, 4)
            val headerSize: Int
            val boxSize: Long
            if (size32 == 1L) {
                if (offset + 16 > end) break
                headerSize = 16
                boxSize = readUInt64(bytes, offset + 8)
            } else {
                headerSize = 8
                boxSize = if (size32 == 0L) (end - offset).toLong() else size32
            }
            if (boxSize < headerSize || offset + boxSize > end || offset + boxSize > bytes.size) break
            val box = Mp4Box(
                start = offset,
                contentStart = offset + headerSize,
                contentEnd = (offset + boxSize).toInt(),
                type = boxType,
            )
            if (type == null || boxType == type) yield(box)
            offset = (offset + boxSize).toInt()
        }
    }

    private fun readUInt64(source: ByteArray, offset: Int): Long {
        var value = 0L
        for (index in 0 until 8) {
            value = (value shl 8) or (source[offset + index].toLong() and 0xFFL)
        }
        return value
    }

    private fun ByteArray.decodeAscii(offset: Int, length: Int): String =
        if (offset < 0 || offset + length > size) {
            ""
        } else {
            String(this, offset, length, Charsets.US_ASCII)
        }

    private data class Mp4Box(
        val start: Int,
        val contentStart: Int,
        val contentEnd: Int,
        val type: String,
    )
}
