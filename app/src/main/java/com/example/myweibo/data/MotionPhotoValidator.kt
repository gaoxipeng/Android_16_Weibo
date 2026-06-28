package com.example.myweibo.data

object MotionPhotoValidator {
    private val XMP_HEADERS = listOf(
        "http://ns.adobe.com/xap/1.0/\u0000",
        "http://ns.adobe.com/xap/1.0\u0000",
    )

    fun embeddedVideoLength(bytes: ByteArray): Int? = readEmbeddedVideoLength(bytes)

    fun validate(bytes: ByteArray, expectedVideoLength: Int = 0): String? {
        if (!looksLikeJpeg(bytes)) return "不是有效的 JPEG 文件"
        if (!containsMotionPhotoXmp(bytes)) return "缺少 Motion Photo XMP 元数据"
        val videoLength = readEmbeddedVideoLength(bytes)
            ?: return "无法解析嵌入视频长度"
        if (videoLength <= 0 || videoLength > bytes.size) {
            return "嵌入视频长度无效：$videoLength"
        }
        val videoStart = bytes.size - videoLength
        if (videoStart < 4) return "嵌入视频位置无效"
        if (!MotionPhotoVideoPreparer.looksLikeMp4(bytes.copyOfRange(videoStart, bytes.size))) {
            return "文件末尾未找到有效 MP4 视频"
        }
        if (expectedVideoLength > 0 && videoLength < expectedVideoLength) {
            return "嵌入视频体积异常（$videoLength < $expectedVideoLength）"
        }
        return null
    }

    fun extractXmpXml(jpeg: ByteArray): String? = extractXmpXmlInternal(jpeg)

    private fun looksLikeJpeg(bytes: ByteArray): Boolean =
        bytes.size >= 3 &&
            bytes[0] == 0xFF.toByte() &&
            bytes[1] == 0xD8.toByte() &&
            bytes[2] == 0xFF.toByte()

    private fun containsMotionPhotoXmp(bytes: ByteArray): Boolean {
        val xmp = extractXmpXmlInternal(bytes) ?: return false
        val hasMotionFlag = xmp.contains("GCamera:MotionPhoto=\"1\"") ||
            xmp.contains("Camera:MotionPhoto=\"1\"") ||
            xmp.contains("GCamera:MicroVideo=\"1\"") ||
            xmp.contains("MiCamera:")
        val hasVideoRef = xmp.contains("Item:Semantic=\"MotionPhoto\"") ||
            xmp.contains("GCamera:MicroVideoOffset=")
        return hasMotionFlag && hasVideoRef
    }

    private fun readEmbeddedVideoLength(bytes: ByteArray): Int? {
        val xmp = extractXmpXmlInternal(bytes) ?: return null
        val microOffset = readTagInt(xmp, "GCamera:MicroVideoOffset")
        val containerLength = readContainerMotionPhotoLength(xmp)
        return when {
            microOffset != null && containerLength != null &&
                microOffset != containerLength &&
                containerLength > 0 -> containerLength
            containerLength != null && containerLength > 0 -> containerLength
            microOffset != null && microOffset > 0 -> microOffset
            else -> null
        }
    }

    private fun readContainerMotionPhotoLength(xmp: String): Int? {
        val motionPhotoBlock = Regex(
            """Item:Semantic="MotionPhoto"[^>]*Item:Length="(\d+)"""",
        ).find(xmp)
        if (motionPhotoBlock != null) {
            return motionPhotoBlock.groupValues[1].toIntOrNull()
        }
        return Regex(
            """Item:Length="(\d+)"[^>]*Item:Semantic="MotionPhoto"""",
        ).find(xmp)?.groupValues?.get(1)?.toIntOrNull()
    }

    private fun readTagInt(xmp: String, tag: String): Int? =
        Regex("""$tag="(\d+)"""").find(xmp)?.groupValues?.get(1)?.toIntOrNull()

    private fun extractXmpXmlInternal(jpeg: ByteArray): String? {
        var offset = 2
        while (offset + 4 < jpeg.size) {
            if (jpeg[offset] != 0xFF.toByte()) return null
            val marker = jpeg[offset + 1].toInt() and 0xFF
            if (marker == 0xDA || marker == 0xD9) return null
            val length = ((jpeg[offset + 2].toInt() and 0xFF) shl 8) or (jpeg[offset + 3].toInt() and 0xFF)
            val end = offset + 2 + length
            if (length < 2 || end > jpeg.size) return null
            if (marker == 0xE1 && end - offset - 4 > 0) {
                val segmentStart = offset + 4
                val header = XMP_HEADERS.firstOrNull { candidate ->
                    val headerBytes = candidate.toByteArray(Charsets.UTF_8)
                    segmentStart + headerBytes.size <= end &&
                        headerBytes.indices.all { jpeg[segmentStart + it] == headerBytes[it] }
                }
                if (header != null) {
                    val headerSize = header.toByteArray(Charsets.UTF_8).size
                    val xmpStart = segmentStart + headerSize
                    val raw = String(jpeg, xmpStart, end - xmpStart, Charsets.UTF_8)
                    val endTag = "</x:xmpmeta>"
                    val endIndex = raw.indexOf(endTag)
                    return if (endIndex >= 0) {
                        raw.substring(0, endIndex + endTag.length)
                    } else {
                        raw.trimEnd('\u0000', ' ')
                    }
                }
            }
            offset = end
        }
        return null
    }
}
