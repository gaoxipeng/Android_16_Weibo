package com.example.myweibo.data

import android.graphics.BitmapFactory
import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets

/**
 * Writes Motion Photo files compatible with Xiaomi Gallery / Google Photos.
 *
 * Xiaomi (and most Android MVIMG) expect:
 * ```
 * [Primary JPEG + XMP APP1] + [MP4 appended at EOF]
 * ```
 * with `GCamera:MicroVideoOffset` equal to the MP4 byte length.
 *
 * File name should start with `MV` (e.g. `MVIMG_*_MP.jpg`).
 */
object MotionPhotoWriter {
    private const val XMP_HEADER = "http://ns.adobe.com/xap/1.0\u0000"
    private const val EXIF_HEADER = "Exif\u0000\u0000"
    private const val MARKER_SOI = 0xD8
    private const val MARKER_SOS = 0xDA
    private const val MARKER_EOI = 0xD9
    private const val MARKER_APP1 = 0xE1

    fun buildMotionPhoto(
        jpegBytes: ByteArray,
        videoBytes: ByteArray,
        presentationTimestampUs: Long = 0L,
    ): ByteArray {
        require(jpegBytes.isNotEmpty()) { "Motion Photo 缺少静态图" }
        require(videoBytes.isNotEmpty()) { "Motion Photo 缺少视频" }
        require(looksLikeJpeg(jpegBytes)) { "Motion Photo 静态图必须是 JPEG" }
        val normalizedVideo = normalizeMp4Payload(videoBytes)
        require(MotionPhotoVideoPreparer.looksLikeMp4(normalizedVideo)) { "Motion Photo 视频必须是 MP4" }

        val trimmedJpeg = trimJpegToEoi(jpegBytes)
        val timestamp = normalizePresentationTimestampUs(presentationTimestampUs)
        val xmp = buildMotionPhotoXmp(
            videoLength = normalizedVideo.size,
            presentationTimestampUs = timestamp,
        )
        val jpegWithXmp = insertOrReplaceXmpApp1Segment(trimmedJpeg, xmp)
        require(isDecodableJpeg(jpegWithXmp)) {
            "Motion Photo 静态图合成后无法解码"
        }
        val motionBytes = jpegWithXmp + normalizedVideo
        MotionPhotoValidator.validate(motionBytes, expectedVideoLength = normalizedVideo.size)?.let { reason ->
            throw IllegalStateException("Motion Photo 校验失败：$reason")
        }
        return motionBytes
    }

    /** Flat XMP snippet for MediaStore indexing on some OEM galleries (e.g. Xiaomi). */
    fun extractMediaStoreXmpSnippet(motionBytes: ByteArray): String? =
        MotionPhotoValidator.extractXmpXml(motionBytes)

    private fun normalizePresentationTimestampUs(value: Long): Long =
        when {
            value > 0L -> value
            else -> 500_000L
        }

    private fun trimJpegToEoi(jpeg: ByteArray): ByteArray {
        for (index in jpeg.size - 2 downTo 0) {
            if (jpeg[index] == 0xFF.toByte() && jpeg[index + 1] == MARKER_EOI.toByte()) {
                return jpeg.copyOfRange(0, index + 2)
            }
        }
        return jpeg
    }

    private fun isDecodableJpeg(jpeg: ByteArray): Boolean =
        BitmapFactory.decodeByteArray(jpeg, 0, jpeg.size) != null

    /**
     * MicroVideoOffset = video byte length when MP4 is appended directly after JPEG EOI.
     * HyperOS gallery also indexes MediaStore.XMP for `%MicroVideo%` / `%MotionPhoto%`.
     */
    private fun buildMotionPhotoXmp(
        videoLength: Int,
        presentationTimestampUs: Long,
    ): String {
        val timestamp = presentationTimestampUs.toString()
        val microVideoOffset = videoLength.toString()
        val motionVideoLength = videoLength.toString()
        return buildString {
            append("<x:xmpmeta xmlns:x=\"adobe:ns:meta/\" x:xmptk=\"Adobe XMP Core 5.1.0-jc003\">")
            append("<rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\">")
            append("<rdf:Description rdf:about=\"\"")
            append(" xmlns:Camera=\"http://ns.google.com/photos/1.0/camera/\"")
            append(" xmlns:GCamera=\"http://ns.google.com/photos/1.0/camera/\"")
            append(" xmlns:Container=\"http://ns.google.com/photos/1.0/container/\"")
            append(" xmlns:Item=\"http://ns.google.com/photos/1.0/container/item/\"")
            append(" Camera:MotionPhoto=\"1\"")
            append(" Camera:MotionPhotoVersion=\"1\"")
            append(" Camera:MotionPhotoPresentationTimestampUs=\"$timestamp\"")
            append(" GCamera:MotionPhoto=\"1\"")
            append(" GCamera:MotionPhotoVersion=\"1\"")
            append(" GCamera:MotionPhotoPresentationTimestampUs=\"$timestamp\"")
            append(" GCamera:MicroVideo=\"1\"")
            append(" GCamera:MicroVideoVersion=\"1\"")
            append(" GCamera:MicroVideoOffset=\"$microVideoOffset\"")
            append(" GCamera:MicroVideoPresentationTimestampUs=\"$timestamp\">")
            append("<Container:Directory>")
            append("<rdf:Seq>")
            append("<rdf:li rdf:parseType=\"Resource\">")
            append("<Container:Item")
            append(" Item:Mime=\"image/jpeg\"")
            append(" Item:Semantic=\"Primary\"")
            append(" Item:Length=\"0\"")
            append(" Item:Padding=\"0\"/>")
            append("</rdf:li>")
            append("<rdf:li rdf:parseType=\"Resource\">")
            append("<Container:Item")
            append(" Item:Mime=\"video/mp4\"")
            append(" Item:Semantic=\"MotionPhoto\"")
            append(" Item:Length=\"$motionVideoLength\"")
            append(" Item:Padding=\"0\"/>")
            append("</rdf:li>")
            append("</rdf:Seq>")
            append("</Container:Directory>")
            append("</rdf:Description>")
            append("</rdf:RDF>")
            append("</x:xmpmeta>")
        }
    }

    private fun normalizeMp4Payload(bytes: ByteArray): ByteArray {
        val ftypIndex = findAscii(bytes, "ftyp".toByteArray(StandardCharsets.US_ASCII), 0, 256)
        if (ftypIndex == null || ftypIndex < 4) return bytes
        val boxStart = ftypIndex - 4
        if (boxStart == 0) return bytes
        val boxSize = readUInt32(bytes, boxStart)
        return if (boxSize >= 8L && boxStart + boxSize <= bytes.size) {
            bytes.copyOfRange(boxStart, bytes.size)
        } else {
            bytes
        }
    }

    private fun buildXmpApp1Segment(xmpMeta: String): ByteArray {
        val headerBytes = XMP_HEADER.toByteArray(StandardCharsets.UTF_8)
        var payload = headerBytes + xmpMeta.toByteArray(StandardCharsets.UTF_8)
        if (payload.size % 2 != 0) {
            payload = payload + byteArrayOf(0x00)
        }
        val segmentLength = payload.size + 2
        return ByteArray(4 + payload.size).also { segment ->
            segment[0] = 0xFF.toByte()
            segment[1] = MARKER_APP1.toByte()
            segment[2] = (segmentLength shr 8).toByte()
            segment[3] = (segmentLength and 0xFF).toByte()
            System.arraycopy(payload, 0, segment, 4, payload.size)
        }
    }

    private fun insertOrReplaceXmpApp1Segment(jpeg: ByteArray, xmpMeta: String): ByteArray {
        require(jpeg.size >= 2 && jpeg[0] == 0xFF.toByte() && jpeg[1] == MARKER_SOI.toByte()) {
            "Invalid JPEG"
        }
        val sections = parseJpegSections(jpeg) ?: throw IllegalStateException("JPEG 解析失败")
        val newSegment = buildXmpApp1Segment(xmpMeta)
        val xmpHeaderBytes = XMP_HEADER.toByteArray(StandardCharsets.UTF_8)
        val exifHeaderBytes = EXIF_HEADER.toByteArray(StandardCharsets.UTF_8)

        val existingXmpIndex = sections.indexOfFirst { section ->
            section.marker == MARKER_APP1 &&
                section.data.size >= 4 + xmpHeaderBytes.size &&
                segmentStartsWith(section.data, 4, xmpHeaderBytes)
        }
        if (existingXmpIndex >= 0) {
            sections[existingXmpIndex] = JpegSection(MARKER_APP1, newSegment)
        } else {
            val insertIndex = findXmpInsertIndex(sections, exifHeaderBytes)
            sections.add(insertIndex, JpegSection(MARKER_APP1, newSegment))
        }
        return writeJpegSections(sections)
    }

    private fun findXmpInsertIndex(sections: List<JpegSection>, exifHeaderBytes: ByteArray): Int {
        val exifIndex = sections.indexOfFirst { section ->
            section.marker == MARKER_APP1 &&
                section.data.size >= 4 + exifHeaderBytes.size &&
                segmentStartsWith(section.data, 4, exifHeaderBytes)
        }
        if (exifIndex >= 0) return exifIndex + 1
        val firstApp1Index = sections.indexOfFirst { it.marker == MARKER_APP1 }
        if (firstApp1Index >= 0) return firstApp1Index + 1
        return 0
    }

    private fun parseJpegSections(jpeg: ByteArray): MutableList<JpegSection>? {
        if (jpeg.size < 2 || jpeg[0] != 0xFF.toByte() || jpeg[1] != MARKER_SOI.toByte()) return null
        val sections = ArrayList<JpegSection>()
        var offset = 2
        while (offset + 1 < jpeg.size) {
            if (jpeg[offset] != 0xFF.toByte()) return null
            var markerIndex = offset + 1
            var marker = jpeg[markerIndex].toInt() and 0xFF
            while (marker == 0xFF) {
                markerIndex++
                if (markerIndex >= jpeg.size) return null
                marker = jpeg[markerIndex].toInt() and 0xFF
            }
            if (marker == MARKER_SOS) {
                sections.add(JpegSection(marker = marker, data = jpeg.copyOfRange(offset, jpeg.size)))
                return sections
            }
            if (marker == MARKER_EOI) {
                sections.add(JpegSection(marker = marker, data = byteArrayOf(0xFF.toByte(), MARKER_EOI.toByte())))
                return sections
            }
            if (offset + 3 >= jpeg.size) return null
            val length = readUInt16(jpeg, offset + 2)
            val end = offset + 2 + length
            if (length < 2 || end > jpeg.size) return null
            sections.add(JpegSection(marker = marker, data = jpeg.copyOfRange(offset, end)))
            offset = end
        }
        return sections
    }

    private fun writeJpegSections(sections: List<JpegSection>): ByteArray {
        val output = ByteArrayOutputStream()
        output.write(0xFF)
        output.write(MARKER_SOI)
        sections.forEach { section ->
            output.write(section.data)
        }
        return output.toByteArray()
    }

    private fun segmentStartsWith(source: ByteArray, start: Int, prefix: ByteArray): Boolean {
        if (start < 0 || start + prefix.size > source.size) return false
        return prefix.indices.all { source[start + it] == prefix[it] }
    }

    private fun findAscii(source: ByteArray, needle: ByteArray, start: Int, maxEnd: Int): Int? {
        val end = minOf(source.size - needle.size, maxEnd)
        if (needle.isEmpty() || start > end) return null
        for (index in start..end) {
            if (needle.indices.all { source[index + it] == needle[it] }) {
                return index
            }
        }
        return null
    }

    private fun readUInt16(source: ByteArray, offset: Int): Int =
        ((source[offset].toInt() and 0xFF) shl 8) or (source[offset + 1].toInt() and 0xFF)

    private fun readUInt32(source: ByteArray, offset: Int): Long =
        ((source[offset].toLong() and 0xFFL) shl 24) or
            ((source[offset + 1].toLong() and 0xFFL) shl 16) or
            ((source[offset + 2].toLong() and 0xFFL) shl 8) or
            (source[offset + 3].toLong() and 0xFFL)

    private fun looksLikeJpeg(bytes: ByteArray): Boolean =
        bytes.size >= 3 &&
            bytes[0] == 0xFF.toByte() &&
            bytes[1] == MARKER_SOI.toByte() &&
            bytes[2] == 0xFF.toByte()

    private data class JpegSection(
        val marker: Int,
        val data: ByteArray,
    )
}
