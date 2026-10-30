package com.example.myweibo.data

import android.webkit.CookieManager
import java.net.HttpURLConnection
import java.net.URL

/**
 * Some iPhone Live Photo MOV files carry a reflected display matrix in their
 * video track. Media3 applies only part of that transform, which turns the
 * clip into a horizontally mirrored version of the still. MediaMetadataRetriever
 * normalizes the same file, so the existing content comparison cannot see it.
 *
 * Inspect the first chunk of the MOV and report whether the video track matrix
 * has a negative determinant (a reflection).
 */
object LivePhotoContainerMirrorDetector {
    private const val HEAD_BYTES = 512 * 1024
    private const val USER_AGENT =
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 " +
            "(KHTML, like Gecko) Chrome/126.0.0.0 Safari/537.36"

    private val cache = object : LinkedHashMap<String, Boolean>(8, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, Boolean>?): Boolean =
            size > 16
    }

    @Synchronized
    fun hasReflectedVideoTrack(videoUrl: String): Boolean? {
        if (videoUrl.isBlank()) return null
        cache[videoUrl]?.let { return it }
        val decision = runCatching { parse(readHead(videoUrl)) }.getOrNull()
        if (decision != null) cache[videoUrl] = decision
        return decision
    }

    private fun readHead(url: String): ByteArray? {
        val cookie = CookieManager.getInstance().getCookie("https://weibo.com/").orEmpty()
        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
            connectTimeout = 8_000
            readTimeout = 10_000
            instanceFollowRedirects = true
            setRequestProperty("User-Agent", USER_AGENT)
            setRequestProperty("Referer", "https://weibo.com/")
            setRequestProperty("Origin", "https://weibo.com")
            setRequestProperty("Accept", "*/*")
            setRequestProperty("Range", "bytes=0-${HEAD_BYTES - 1}")
            if (cookie.isNotBlank()) setRequestProperty("Cookie", cookie)
        }
        return runCatching {
            connection.inputStream.use { input ->
                val output = java.io.ByteArrayOutputStream()
                val buffer = ByteArray(16 * 1024)
                while (output.size() < HEAD_BYTES) {
                    val read = input.read(buffer, 0, minOf(buffer.size, HEAD_BYTES - output.size()))
                    if (read <= 0) break
                    output.write(buffer, 0, read)
                }
                output.toByteArray()
            }
        }.also { connection.disconnect() }.getOrNull()
    }

    internal fun parseForTest(bytes: ByteArray): Boolean? = parse(bytes)

    /** Uses an already downloaded payload, avoiding a second network request. */
    fun hasReflectedVideoTrack(bytes: ByteArray): Boolean? = parse(bytes)

    private fun parse(bytes: ByteArray?): Boolean? {        if (bytes == null || bytes.size < 16) return null
        var offset = 0
        while (offset + 8 <= bytes.size) {
            val size = readUInt32(bytes, offset)
            val type = readType(bytes, offset + 4)
            if (size < 8L || offset + size > bytes.size) break
            if (type == "moov") {
                return parseMoov(bytes, offset + 8, offset + size.toInt())
            }
            offset += size.toInt()
        }
        return null
    }

    private fun parseMoov(bytes: ByteArray, start: Int, end: Int): Boolean? {
        var offset = start
        while (offset + 8 <= end) {
            val size = readUInt32(bytes, offset)
            val type = readType(bytes, offset + 4)
            if (size < 8L || offset + size > end) break
            if (type == "trak") {
                val reflected = parseTrak(bytes, offset + 8, offset + size.toInt())
                if (reflected != null) return reflected
            }
            offset += size.toInt()
        }
        return null
    }

    private fun parseTrak(bytes: ByteArray, start: Int, end: Int): Boolean? {
        var offset = start
        var matrix: DoubleArray? = null
        var isVideo = false
        while (offset + 8 <= end) {
            val size = readUInt32(bytes, offset)
            val type = readType(bytes, offset + 4)
            if (size < 8L || offset + size > end) break
            val body = offset + 8
            val bodyEnd = offset + size.toInt()
            when (type) {
                "tkhd" -> matrix = parseTkhdMatrix(bytes, body, bodyEnd)
                "mdia" -> isVideo = isVideoTrack(bytes, body, bodyEnd)
            }
            offset += size.toInt()
        }
        if (!isVideo) return null
        val m = matrix ?: return null
        // 3x3 fixed-point matrix: [a b u; c d v; x y w].
        val determinant = m[0] * m[4] - m[1] * m[3]
        return determinant < -0.001
    }

    private fun isVideoTrack(bytes: ByteArray, start: Int, end: Int): Boolean {
        var offset = start
        while (offset + 8 <= end) {
            val size = readUInt32(bytes, offset)
            val type = readType(bytes, offset + 4)
            if (size < 8L || offset + size > end) break
            if (type == "hdlr") {
                val handler = readType(bytes, offset + 8 + 8)
                return handler == "vide"
            }
            offset += size.toInt()
        }
        return false
    }

    private fun parseTkhdMatrix(bytes: ByteArray, start: Int, end: Int): DoubleArray? {
        if (start + 1 > end) return null
        val version = bytes[start].toInt() and 0xff
        val matrixOffset = start + if (version == 1) 52 else 40
        if (matrixOffset + 36 > end) return null
        return DoubleArray(9) { index ->
            readInt32(bytes, matrixOffset + index * 4) / 65536.0
        }
    }

    private fun readType(bytes: ByteArray, offset: Int): String {
        if (offset + 4 > bytes.size) return ""
        return String(bytes, offset, 4, Charsets.US_ASCII)
    }

    private fun readUInt32(bytes: ByteArray, offset: Int): Long {
        if (offset + 4 > bytes.size) return 0L
        return ((bytes[offset].toLong() and 0xff) shl 24) or
            ((bytes[offset + 1].toLong() and 0xff) shl 16) or
            ((bytes[offset + 2].toLong() and 0xff) shl 8) or
            (bytes[offset + 3].toLong() and 0xff)
    }

    private fun readInt32(bytes: ByteArray, offset: Int): Int {
        if (offset + 4 > bytes.size) return 0
        return ((bytes[offset].toInt() and 0xff) shl 24) or
            ((bytes[offset + 1].toInt() and 0xff) shl 16) or
            ((bytes[offset + 2].toInt() and 0xff) shl 8) or
            (bytes[offset + 3].toInt() and 0xff)
    }
}
