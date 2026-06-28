package com.example.myweibo.data

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.exifinterface.media.ExifInterface
import java.io.ByteArrayInputStream
import kotlin.math.roundToInt

/**
 * Decodes remote still images while honoring EXIF rotation.
 *
 * Some Weibo live-photo stills carry mirrored EXIF orientations even though the
 * paired video is not mirrored. Treating mirror-only tags as normal keeps the
 * still frame aligned with the motion frame while preserving camera rotation.
 */
object BitmapExifOrientation {
    fun decodeSampledBitmap(bytes: ByteArray, maxDecodeDim: Int): Bitmap? {
        if (bytes.isEmpty()) return null
        return runCatching {
            val orientation = readExifOrientation(bytes)
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
            val sampleSize = calculateSampleSize(
                width = bounds.outWidth,
                height = bounds.outHeight,
                maxDecodeDim = maxDecodeDim,
            )
            val options = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }
            val decoded = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options) ?: return@runCatching null
            val scaled = scaleBitmapToMaxDim(decoded, maxDecodeDim)
            val rotated = rotateBitmapForExif(scaled, orientation)
            if (rotated !== scaled) {
                scaled.recycle()
            }
            if (scaled !== decoded && decoded !== rotated) {
                decoded.recycle()
            }
            rotated
        }.getOrNull()
    }

    private fun readExifOrientation(bytes: ByteArray): Int =
        runCatching {
            ExifInterface(ByteArrayInputStream(bytes))
                .getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
        }.getOrDefault(ExifInterface.ORIENTATION_NORMAL)

    private fun calculateSampleSize(width: Int, height: Int, maxDecodeDim: Int): Int {
        val target = maxDecodeDim.coerceAtLeast(1)
        var sampleSize = 1
        var sampledWidth = width
        var sampledHeight = height
        while (sampledWidth / 2 >= target || sampledHeight / 2 >= target) {
            sampleSize *= 2
            sampledWidth /= 2
            sampledHeight /= 2
        }
        return sampleSize.coerceAtLeast(1)
    }

    private fun scaleBitmapToMaxDim(bitmap: Bitmap, maxDecodeDim: Int): Bitmap {
        val maxDim = maxOf(bitmap.width, bitmap.height)
        val target = maxDecodeDim.coerceAtLeast(1)
        if (maxDim <= target) return bitmap
        val scale = maxDim.toFloat() / target.toFloat()
        return Bitmap.createScaledBitmap(
            bitmap,
            (bitmap.width / scale).roundToInt().coerceAtLeast(1),
            (bitmap.height / scale).roundToInt().coerceAtLeast(1),
            true,
        )
    }

    private fun rotateBitmapForExif(bitmap: Bitmap, orientation: Int): Bitmap {
        val degrees = when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90,
            ExifInterface.ORIENTATION_TRANSPOSE -> 90f
            ExifInterface.ORIENTATION_ROTATE_180 -> 180f
            ExifInterface.ORIENTATION_ROTATE_270,
            ExifInterface.ORIENTATION_TRANSVERSE -> 270f
            else -> 0f
        }
        if (degrees == 0f) return bitmap
        return Bitmap.createBitmap(
            bitmap,
            0,
            0,
            bitmap.width,
            bitmap.height,
            Matrix().apply { postRotate(degrees) },
            true,
        )
    }
}
