package com.example.myweibo.data

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

object ImageSaveHelper {
    private const val USER_AGENT =
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 " +
            "(KHTML, like Gecko) Chrome/126.0.0.0 Safari/537.36"

    suspend fun loadBytes(image: FeedImage): ByteArray? = withContext(Dispatchers.IO) {
        imageUrlCandidates(image).firstNotNullOfOrNull { url ->
            runCatching { downloadBytes(url) }.getOrNull()
        }
    }

    suspend fun probeSizeBytes(image: FeedImage): Long? = withContext(Dispatchers.IO) {
        val url = imageUrlCandidates(image).firstOrNull() ?: return@withContext null
        runCatching {
            val connection = (URL(url).openConnection() as HttpURLConnection).apply {
                requestMethod = "HEAD"
                connectTimeout = 8_000
                readTimeout = 8_000
                setRequestProperty("User-Agent", USER_AGENT)
                setRequestProperty("Referer", "https://weibo.com/")
            }
            connection.connect()
            val length = connection.contentLengthLong
            connection.disconnect()
            length.takeIf { it > 0 }
        }.getOrNull() ?: loadBytes(image)?.size?.toLong()
    }

    suspend fun saveImage(context: Context, image: FeedImage): Result<String> = withContext(Dispatchers.IO) {
        val bytes = loadBytes(image) ?: return@withContext Result.failure(IllegalStateException("图片下载失败"))
        val isGif = image.isGif || looksLikeGif(bytes)
        val mime = if (isGif) "image/gif" else "image/jpeg"
        val ext = if (isGif) "gif" else "jpg"
        val displayName = "weibo_${sanitize(image.id)}_${System.currentTimeMillis()}.$ext"
        val uri = insertMedia(
            context = context,
            displayName = displayName,
            mimeType = mime,
            relativePath = "${Environment.DIRECTORY_PICTURES}/MyWeibo",
        ) ?: return@withContext Result.failure(IllegalStateException("无法写入相册"))
        val writeResult = runCatching {
            context.contentResolver.openOutputStream(uri)?.use { output ->
                if (isGif) {
                    output.write(bytes)
                } else {
                    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                        ?: throw IllegalStateException("图片解码失败")
                    if (!bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 95, output)) {
                        throw IllegalStateException("图片保存失败")
                    }
                }
            } ?: throw IllegalStateException("无法打开输出流")
            finalizeMedia(context, uri)
        }
        if (writeResult.isFailure) {
            context.contentResolver.delete(uri, null, null)
            return@withContext Result.failure(
                writeResult.exceptionOrNull() ?: IllegalStateException("图片保存失败"),
            )
        }

        if (image.isLivePhoto) {
            image.livePhotoVideoUrl?.takeIf { it.isNotBlank() }?.let { videoUrl ->
                runCatching { saveLivePhotoVideo(context, videoUrl, image.id) }
            }
        }
        Result.success(displayName)
    }

    suspend fun shareImage(context: Context, image: FeedImage): Result<Unit> = withContext(Dispatchers.IO) {
        val bytes = loadBytes(image) ?: return@withContext Result.failure(IllegalStateException("图片下载失败"))
        val isGif = image.isGif || looksLikeGif(bytes)
        val ext = if (isGif) "gif" else "jpg"
        val mime = if (isGif) "image/gif" else "image/jpeg"
        val file = File(context.cacheDir, "share_${sanitize(image.id)}_${System.currentTimeMillis()}.$ext")
        file.writeBytes(bytes)
        withContext(Dispatchers.Main) {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file,
            )
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = mime
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "分享图片"))
        }
        Result.success(Unit)
    }

    suspend fun saveAllImages(context: Context, images: List<FeedImage>): Result<Int> = withContext(Dispatchers.IO) {
        if (images.isEmpty()) {
            return@withContext Result.failure(IllegalStateException("没有可保存的图片"))
        }
        var saved = 0
        val errors = mutableListOf<String>()
        images.forEach { image ->
            saveImage(context, image)
                .onSuccess { saved += 1 }
                .onFailure { errors += it.message.orEmpty() }
        }
        when {
            saved == images.size -> Result.success(saved)
            saved > 0 -> Result.success(saved)
            else -> Result.failure(IllegalStateException(errors.firstOrNull() ?: "保存失败"))
        }
    }

    fun formatInfo(image: FeedImage, sizeBytes: Long?): List<String> = buildList {
        add("图片 ID：${image.id}")
        val width = image.width
        val height = image.height
        if (width != null && height != null && width > 0 && height > 0) {
            add("尺寸：${width} × ${height}")
        }
        add(
            "类型：" + when {
                image.isLivePhoto -> "LivePhoto"
                image.isGif -> "GIF"
                else -> "图片"
            },
        )
        image.createdAt?.takeIf { it.isNotBlank() }?.let { add("发布时间：$it") }
        sizeBytes?.let { add("大小：${formatFileSize(it)}") }
        imageUrlCandidates(image).firstOrNull()?.let { add("链接：$it") }
        image.livePhotoVideoUrl?.takeIf { it.isNotBlank() }?.let { add("LivePhoto 视频：$it") }
    }

    private fun imageUrlCandidates(image: FeedImage): List<String> =
        (image.downloadUrls + image.largeUrl + image.thumbnailUrl)
            .filter { it.isNotBlank() }
            .distinct()

    private fun downloadBytes(url: String): ByteArray {
        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
            connectTimeout = 12_000
            readTimeout = 20_000
            setRequestProperty("User-Agent", USER_AGENT)
            setRequestProperty("Referer", "https://weibo.com/")
        }
        return connection.inputStream.use { it.readBytes() }
    }

    private fun saveLivePhotoVideo(context: Context, videoUrl: String, imageId: String): String {
        val bytes = downloadBytes(videoUrl)
        val displayName = "weibo_${sanitize(imageId)}_${System.currentTimeMillis()}_live.mp4"
        val uri = insertMedia(
            context = context,
            displayName = displayName,
            mimeType = "video/mp4",
            relativePath = "${Environment.DIRECTORY_MOVIES}/MyWeibo",
        ) ?: throw IllegalStateException("无法写入视频")
        context.contentResolver.openOutputStream(uri)?.use { it.write(bytes) }
            ?: throw IllegalStateException("无法打开视频输出流")
        finalizeMedia(context, uri)
        return displayName
    }

    private fun insertMedia(
        context: Context,
        displayName: String,
        mimeType: String,
        relativePath: String,
    ): Uri? {
        val collection = when {
            mimeType.startsWith("video/") -> MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            mimeType.startsWith("image/") -> MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            else -> MediaStore.Downloads.EXTERNAL_CONTENT_URI
        }
        return context.contentResolver.insert(
            collection,
            ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, displayName)
                put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                put(MediaStore.MediaColumns.RELATIVE_PATH, relativePath)
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            },
        )
    }

    private fun finalizeMedia(context: Context, uri: android.net.Uri) {
        context.contentResolver.update(
            uri,
            ContentValues().apply { put(MediaStore.MediaColumns.IS_PENDING, 0) },
            null,
            null,
        )
    }

    private fun sanitize(value: String): String =
        value.replace(Regex("""[^\w.-]"""), "_").ifBlank { "image" }

    private fun looksLikeGif(bytes: ByteArray): Boolean =
        bytes.size >= 6 &&
            bytes[0] == 'G'.code.toByte() &&
            bytes[1] == 'I'.code.toByte() &&
            bytes[2] == 'F'.code.toByte()

    private fun formatFileSize(bytes: Long): String = when {
        bytes >= 1024 * 1024 -> String.format("%.1f MB", bytes / (1024.0 * 1024.0))
        bytes >= 1024 -> String.format("%.1f KB", bytes / 1024.0)
        else -> "$bytes B"
    }
}
