package com.example.myweibo

import com.example.myweibo.data.LivePhotoContainerMirrorDetector
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LivePhotoContainerMirrorTest {
    @Test
    fun detectsReflectedVideoTrack() {
        val bytes = movie(reflected = true)
        assertTrue(LivePhotoContainerMirrorDetector.parseForTest(bytes) == true)
    }

    @Test
    fun ignoresNormalVideoTrack() {
        val bytes = movie(reflected = false)
        assertFalse(LivePhotoContainerMirrorDetector.parseForTest(bytes) == true)
    }

    private fun movie(reflected: Boolean): ByteArray {
        val tkhdPayload = ByteArray(76)
        val matrixOffset = 40
        val matrix = longArrayOf(
            65536, 0, 0,
            0, if (reflected) -65536 else 65536, 0,
            0, 1440L * 65536, 65536,
        )
        matrix.forEachIndexed { index, value ->
            putInt(tkhdPayload, matrixOffset + index * 4, value.toInt())
        }

        val hdlrPayload = ByteArray(12)
        "vide".toByteArray(Charsets.US_ASCII).copyInto(hdlrPayload, 8)

        val mdia = box("mdia", box("hdlr", hdlrPayload))
        val trak = box("trak", box("tkhd", tkhdPayload) + mdia)
        return box("moov", trak)
    }

    private fun box(type: String, payload: ByteArray): ByteArray {
        val output = ByteArrayOutputStream()
        val size = ByteBuffer.allocate(4).putInt(payload.size + 8).array()
        output.write(size)
        output.write(type.toByteArray(Charsets.US_ASCII))
        output.write(payload)
        return output.toByteArray()
    }

    private fun putInt(target: ByteArray, offset: Int, value: Int) {
        target[offset] = (value ushr 24).toByte()
        target[offset + 1] = (value ushr 16).toByte()
        target[offset + 2] = (value ushr 8).toByte()
        target[offset + 3] = value.toByte()
    }
}
