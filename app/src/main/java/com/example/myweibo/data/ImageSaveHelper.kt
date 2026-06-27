package com.example.myweibo.data

import android.content.ContentValues
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Environment
import android.os.ParcelFileDescriptor
import android.provider.MediaStore
import android.webkit.CookieManager
import androidx.core.content.FileProvider
import androidx.exifinterface.media.ExifInterface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import android.system.Os

object ImageSaveHelper {
    private val mainHandler = Handler(Looper.getMainLooper())
    private const val USER_AGENT =
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 " +
            "(KHTML, like Gecko) Chrome/126.0.0.0 Safari/537.36"
    private const val PER_IMAGE_SAVE_TIMEOUT_MS = 35_000L
    private const val LIVE_PHOTO_SAVE_TIMEOUT_MS = 55_000L
    private const val PER_DOWNLOAD_CONNECT_TIMEOUT_MS = 8_000
    private const val PER_DOWNLOAD_READ_TIMEOUT_MS = 12_000

    data class SaveAllImagesResult(
        val saved: Int,
        val total: Int,
        val errors: List<String>,
    ) {
        val failed: Int get() = (total - saved).coerceAtLeast(0)
        val isComplete: Boolean get() = saved == total && total > 0
    }

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

    suspend fun saveImage(
        context: Context,
        image: FeedImage,
        uniqueSuffix: String = "",
        metadata: ImageSaveMetadata? = null,
        includeLivePhotoVideo: Boolean = true,
        status: FeedItem? = null,
        relatedImages: List<FeedImage> = emptyList(),
    ): Result<String> = withContext(Dispatchers.IO) {
        val resolvedImage = image.resolveForSave(status, relatedImages)
        val timeoutMs = if (
            includeLivePhotoVideo &&
            !resolvedImage.isGif &&
            !resolvedImage.livePhotoVideoUrl.isNullOrBlank()
        ) {
            LIVE_PHOTO_SAVE_TIMEOUT_MS
        } else {
            PER_IMAGE_SAVE_TIMEOUT_MS
        }
        runCatching {
            withTimeout(timeoutMs) {
                saveImageInternal(
                    context = context,
                    image = resolvedImage,
                    uniqueSuffix = uniqueSuffix,
                    metadata = metadata ?: resolvedImage.buildSaveMetadata(status),
                    includeLivePhotoVideo = includeLivePhotoVideo,
                )
            }
        }.getOrElse { error ->
            when (error) {
                is TimeoutCancellationException ->
                    Result.failure(IllegalStateException("保存超时，请稍后重试"))
                else -> Result.failure(error)
            }
        }
    }

    private suspend fun saveImageInternal(
        context: Context,
        image: FeedImage,
        uniqueSuffix: String,
        metadata: ImageSaveMetadata?,
        includeLivePhotoVideo: Boolean,
    ): Result<String> {
        val bytes = loadBytes(image) ?: return Result.failure(IllegalStateException("图片下载失败"))
        val resolvedMetadata = metadata ?: image.buildSaveMetadata()
        val liveVideoUrl = image.livePhotoVideoUrl?.takeIf { it.isNotBlank() }
        if (includeLivePhotoVideo && !image.isGif && liveVideoUrl != null) {
            liveVideoUrl.let { videoUrl ->
                return saveLivePhotoMotionPhoto(
                    context = context,
                    image = image,
                    imageBytes = bytes,
                    videoUrl = videoUrl,
                    uniqueSuffix = uniqueSuffix,
                    metadata = resolvedMetadata,
                )
            }
        }
        return saveStaticImage(
            context = context,
            image = image,
            bytes = bytes,
            uniqueSuffix = uniqueSuffix,
            metadata = resolvedMetadata,
        )
    }

    private fun saveStaticImage(
        context: Context,
        image: FeedImage,
        bytes: ByteArray,
        uniqueSuffix: String,
        metadata: ImageSaveMetadata,
    ): Result<String> {
        val isGif = image.isGif || looksLikeGif(bytes)
        val mime = if (isGif) "image/gif" else "image/jpeg"
        val ext = if (isGif) "gif" else "jpg"
        val displayName = "weibo_${sanitize(image.id)}_${System.nanoTime()}${uniqueSuffix}.$ext"
        val uri = insertMedia(
            context = context,
            displayName = displayName,
            mimeType = mime,
            relativePath = "${Environment.DIRECTORY_PICTURES}/MyWeibo",
        ) ?: return Result.failure(IllegalStateException("无法写入相册"))
        val writeResult = runCatching {
            context.contentResolver.openOutputStream(uri)?.use { output ->
                when {
                    isGif -> output.write(bytes)
                    looksLikeJpeg(bytes) -> output.write(bytes)
                    else -> {
                        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                            ?: throw IllegalStateException("图片解码失败")
                        if (!bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 95, output)) {
                            throw IllegalStateException("图片保存失败")
                        }
                    }
                }
            } ?: throw IllegalStateException("无法打开输出流")
            if (!isGif) {
                applySaveMetadata(context, uri, bytes, metadata)
            }
            finalizeMedia(context, uri, metadata)
        }
        if (writeResult.isFailure) {
            context.contentResolver.delete(uri, null, null)
            return Result.failure(
                writeResult.exceptionOrNull() ?: IllegalStateException("图片保存失败"),
            )
        }
        return Result.success(displayName)
    }

    private fun saveLivePhotoMotionPhoto(
        context: Context,
        image: FeedImage,
        imageBytes: ByteArray,
        videoUrl: String,
        uniqueSuffix: String,
        metadata: ImageSaveMetadata,
    ): Result<String> {
        val assembled = LivePhotoMotionPhotoAssembler.assemble(
            context = context,
            image = image,
            imageBytes = imageBytes,
            videoUrl = videoUrl,
            uniqueSuffix = uniqueSuffix,
        ).getOrElse { error ->
            return Result.failure(
                error as? Exception ?: IllegalStateException("Live Photo 合成失败"),
            )
        }
        val uri = insertMedia(
            context = context,
            displayName = assembled.displayName,
            mimeType = "image/jpeg",
            relativePath = "${Environment.DIRECTORY_DCIM}/Camera",
        ) ?: return Result.failure(IllegalStateException("无法写入相册"))
        val xmpSnippet = MotionPhotoWriter.extractMediaStoreXmpSnippet(assembled.bytes)
            ?.let(::ensureMediaStoreXmpSnippet)
        val writeResult = runCatching {
            context.contentResolver.openFileDescriptor(uri, "rw")?.use { descriptor ->
                FileOutputStream(descriptor.fileDescriptor).use { output ->
                    output.write(assembled.bytes)
                    output.flush()
                }
                runCatching { Os.fsync(descriptor.fileDescriptor) }
            } ?: context.contentResolver.openOutputStream(uri)?.use { output ->
                output.write(assembled.bytes)
                output.flush()
            } ?: throw IllegalStateException("无法打开输出流")
            verifySavedMotionPhoto(context, uri, assembled.bytes)
            finalizeMedia(context, uri, metadata, xmpSnippet = xmpSnippet)
            verifySavedMotionPhoto(context, uri, assembled.bytes)
        }
        if (writeResult.isFailure) {
            context.contentResolver.delete(uri, null, null)
            return Result.failure(
                writeResult.exceptionOrNull() ?: IllegalStateException("Live Photo 保存失败"),
            )
        }
        return Result.success(assembled.displayName)
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

    suspend fun saveAllImages(
        context: Context,
        images: List<FeedImage>,
        status: FeedItem? = null,
        onProgress: (suspend (Int, Int) -> Unit)? = null,
    ): SaveAllImagesResult = withContext(Dispatchers.IO) {
        if (images.isEmpty()) {
            return@withContext SaveAllImagesResult(saved = 0, total = 0, errors = listOf("没有可保存的图片"))
        }
        var saved = 0
        val errors = mutableListOf<String>()
        images.forEachIndexed { index, image ->
            onProgress?.invoke(index + 1, images.size)
            if (index > 0) {
                delay(80)
            }
            val resolvedImage = image.resolveForSave(status, images)
            val timeoutMs = if (
                !resolvedImage.livePhotoVideoUrl.isNullOrBlank() && !resolvedImage.isGif
            ) {
                LIVE_PHOTO_SAVE_TIMEOUT_MS + 2_000L
            } else {
                PER_IMAGE_SAVE_TIMEOUT_MS + 2_000L
            }
            val result = runCatching {
                withTimeout(timeoutMs) {
                    saveImageInternal(
                        context = context,
                        image = resolvedImage,
                        uniqueSuffix = "_$index",
                        metadata = resolvedImage.buildSaveMetadata(status),
                        includeLivePhotoVideo = !resolvedImage.livePhotoVideoUrl.isNullOrBlank() &&
                            !resolvedImage.isGif,
                    )
                }
            }.getOrElse { error ->
                when (error) {
                    is TimeoutCancellationException ->
                        Result.failure(IllegalStateException("第 ${index + 1} 张保存超时"))
                    else -> Result.failure(error)
                }
            }
            result
                .onSuccess { saved += 1 }
                .onFailure { error ->
                    errors += error.message?.takeIf { it.isNotBlank() } ?: "第 ${index + 1} 张保存失败"
                }
        }
        SaveAllImagesResult(saved = saved, total = images.size, errors = errors)
    }

    suspend fun saveVideo(
        context: Context,
        media: FeedMedia,
        onProgress: ((Float) -> Unit)? = null,
    ): Result<String> = withContext(Dispatchers.IO) {
        val candidates = listOfNotNull(media.downloadUrl, media.streamUrl)
            .flatMap(::videoDownloadCandidates)
            .filter { it.isNotBlank() && !it.contains(".m3u8", ignoreCase = true) }
            .distinct()
        if (candidates.isEmpty()) {
            return@withContext Result.failure(IllegalStateException("没有可下载的视频地址"))
        }
        fun emitProgress(fraction: Float) {
            onProgress?.let { callback ->
                mainHandler.post { callback(fraction) }
            }
        }
        emitProgress(0.02f)
        val bytes = candidates.firstNotNullOfOrNull { url ->
            runCatching {
                downloadBytes(
                    url = url,
                    maxBytes = 256 * 1024 * 1024,
                    onProgress = { downloaded, total ->
                        val fraction = if (total > 0L) {
                            0.05f + downloaded.toFloat() / total.toFloat() * 0.8f
                        } else {
                            null
                        }
                        fraction?.let(::emitProgress)
                    },
                )
            }.getOrNull()
        }
            ?: return@withContext Result.failure(IllegalStateException("没有可下载的视频地址"))
        emitProgress(0.9f)
        val displayName = "weibo_video_${sanitize(media.title)}_${System.currentTimeMillis()}.mp4"
        val uri = insertMedia(
            context = context,
            displayName = displayName,
            mimeType = "video/mp4",
            relativePath = "${Environment.DIRECTORY_MOVIES}/MyWeibo",
        ) ?: return@withContext Result.failure(IllegalStateException("无法写入视频"))
        val writeResult = runCatching {
            context.contentResolver.openOutputStream(uri)?.use { it.write(bytes) }
                ?: throw IllegalStateException("无法打开视频输出流")
            finalizeMedia(context, uri)
        }
        if (writeResult.isFailure) {
            context.contentResolver.delete(uri, null, null)
            return@withContext Result.failure(
                writeResult.exceptionOrNull() ?: IllegalStateException("视频保存失败"),
            )
        }
        emitProgress(1f)
        Result.success(displayName)
    }

    private fun videoDownloadCandidates(url: String): List<String> {
        val trimmed = url.trim()
        if (trimmed.isBlank()) return emptyList()
        if (trimmed.startsWith("http://", ignoreCase = true)) {
            return listOf(trimmed.replaceFirst("http://", "https://", ignoreCase = true), trimmed)
        }
        return listOf(trimmed)
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
                image.isLivePhoto -> "Live Photo（Motion Photo）"
                image.isGif -> "GIF"
                else -> "图片"
            },
        )
        image.shootTime?.takeIf { it.isNotBlank() }?.let { add("拍摄时间：$it") }
        image.createdAt?.takeIf { it.isNotBlank() }?.let { add("发布时间：$it") }
        listOfNotNull(image.cameraMake, image.cameraModel)
            .filter { it.isNotBlank() }
            .distinct()
            .joinToString(" ")
            .takeIf { it.isNotBlank() }
            ?.let { add("设备：$it") }
        sizeBytes?.let { add("大小：${formatFileSize(it)}") }
        imageUrlCandidates(image).firstOrNull()?.let { add("链接：$it") }
        image.livePhotoVideoUrl?.takeIf { it.isNotBlank() }?.let { add("LivePhoto 视频：$it") }
    }

    private fun imageUrlCandidates(image: FeedImage): List<String> =
        (listOfNotNull(image.largeUrl) + image.downloadUrls + listOfNotNull(image.thumbnailUrl))
            .filter { it.isNotBlank() }
            .distinct()

    private fun verifySavedMotionPhoto(
        context: Context,
        uri: Uri,
        expectedBytes: ByteArray,
    ) {
        val savedBytes = context.contentResolver.openInputStream(uri)?.use { input ->
            input.readBytes()
        } ?: throw IllegalStateException("无法读取已保存文件")
        if (savedBytes.size != expectedBytes.size) {
            throw IllegalStateException(
                "保存后文件体积不一致（${savedBytes.size} / ${expectedBytes.size}），" +
                    "相册可能裁掉了视频数据，请确认系统为 HyperOS 且相册版本较新",
            )
        }
        MotionPhotoValidator.validate(savedBytes)?.let { reason ->
            throw IllegalStateException("保存后 Motion Photo 校验失败：$reason")
        }
    }

    /** HyperOS gallery indexes MediaStore.XMP with `%MicroVideo%` / `%MotionPhoto%`. */
    private fun ensureMediaStoreXmpSnippet(xmp: String): String =
        when {
            xmp.contains("MicroVideo", ignoreCase = true) ||
                xmp.contains("MotionPhoto", ignoreCase = true) -> xmp
            else -> xmp.replace(
                "<rdf:Description",
                """<rdf:Description GCamera:MicroVideo="1" GCamera:MotionPhoto="1"""",
            )
        }

    private fun downloadVideoBytes(url: String): ByteArray =
        downloadBytes(
            url = url,
            maxBytes = 64 * 1024 * 1024,
            includeWeiboCookie = true,
        )

    private fun downloadBytes(
        url: String,
        maxBytes: Int = 32 * 1024 * 1024,
        includeWeiboCookie: Boolean = false,
        onProgress: ((downloaded: Long, total: Long) -> Unit)? = null,
    ): ByteArray {
        var lastError: Exception? = null
        repeat(2) { attempt ->
            try {
                val connection = (URL(url).openConnection() as HttpURLConnection).apply {
                    connectTimeout = PER_DOWNLOAD_CONNECT_TIMEOUT_MS
                    readTimeout = PER_DOWNLOAD_READ_TIMEOUT_MS
                    applyWeiboRequestHeaders(includeWeiboCookie)
                }
                val totalBytes = connection.contentLengthLong.takeIf { it > 0L } ?: -1L
                return connection.inputStream.use { input ->
                    val buffer = ByteArray(8192)
                    val output = java.io.ByteArrayOutputStream()
                    var downloaded = 0L
                    while (true) {
                        val read = input.read(buffer)
                        if (read <= 0) break
                        output.write(buffer, 0, read)
                        downloaded += read
                        onProgress?.invoke(downloaded, totalBytes)
                        if (output.size() > maxBytes) {
                            throw IllegalStateException("文件体积过大")
                        }
                    }
                    output.toByteArray()
                }
            } catch (error: Exception) {
                lastError = error
                if (attempt < 1) {
                    Thread.sleep(200L)
                }
            }
        }
        throw lastError ?: IllegalStateException("下载失败")
    }

    private fun HttpURLConnection.applyWeiboRequestHeaders(includeCookie: Boolean) {
        setRequestProperty("User-Agent", USER_AGENT)
        setRequestProperty("Referer", "https://weibo.com/")
        if (!includeCookie) return
        setRequestProperty("Origin", "https://weibo.com")
        CookieManager.getInstance().getCookie("https://weibo.com/")?.takeIf { it.isNotBlank() }?.let {
            setRequestProperty("Cookie", it)
        }
    }

    private fun downloadBytes(url: String): ByteArray =
        downloadBytes(url = url, maxBytes = 32 * 1024 * 1024)

    private fun applySaveMetadata(
        context: Context,
        uri: Uri,
        sourceBytes: ByteArray,
        metadata: ImageSaveMetadata,
    ) {
        val sourceExif = runCatching {
            ExifInterface(ByteArrayInputStream(sourceBytes))
        }.getOrNull()
        context.contentResolver.openFileDescriptor(uri, "rw")?.use { descriptor ->
            writeExifMetadata(descriptor, sourceExif, metadata)
        }
    }

    private fun writeExifMetadata(
        descriptor: ParcelFileDescriptor,
        sourceExif: ExifInterface?,
        metadata: ImageSaveMetadata,
    ) {
        val exif = ExifInterface(descriptor.fileDescriptor)
        sourceExif?.getAttribute(ExifInterface.TAG_ORIENTATION)?.takeIf { it.isNotBlank() }?.let {
            exif.setAttribute(ExifInterface.TAG_ORIENTATION, it)
        }
        val make = metadata.cameraMake?.takeIf { it.isNotBlank() }
            ?: sourceExif?.getAttribute(ExifInterface.TAG_MAKE)?.takeIf { it.isNotBlank() }
        val model = metadata.cameraModel?.takeIf { it.isNotBlank() }
            ?: sourceExif?.getAttribute(ExifInterface.TAG_MODEL)?.takeIf { it.isNotBlank() }
        make?.let { exif.setAttribute(ExifInterface.TAG_MAKE, it) }
        model?.let { exif.setAttribute(ExifInterface.TAG_MODEL, it) }
        val captureTime = formatExifDateTime(metadata.shootTime)
            ?: formatExifDateTime(metadata.publishTime)
            ?: sourceExif?.getAttribute(ExifInterface.TAG_DATETIME_ORIGINAL)?.takeIf { it.isNotBlank() }
            ?: sourceExif?.getAttribute(ExifInterface.TAG_DATETIME)?.takeIf { it.isNotBlank() }
        captureTime?.let {
            exif.setAttribute(ExifInterface.TAG_DATETIME_ORIGINAL, it)
            exif.setAttribute(ExifInterface.TAG_DATETIME_DIGITIZED, it)
            exif.setAttribute(ExifInterface.TAG_DATETIME, it)
        }
        buildImageSaveDescription(metadata)?.let { description ->
            exif.setAttribute(ExifInterface.TAG_IMAGE_DESCRIPTION, description)
            exif.setAttribute(ExifInterface.TAG_USER_COMMENT, description)
        }
        exif.setAttribute(ExifInterface.TAG_SOFTWARE, "MyWeibo")
        exif.saveAttributes()
    }

    private fun buildImageSaveDescription(metadata: ImageSaveMetadata): String? {
        val parts = buildList {
            metadata.authorName?.takeIf { it.isNotBlank() }?.let { add("@$it") }
            metadata.publishSource?.takeIf { it.isNotBlank() }?.let { add("来自 $it") }
            metadata.publishTime?.takeIf { it.isNotBlank() }?.let { add("发布 $it") }
            metadata.shootTime
                ?.takeIf { it.isNotBlank() && it != metadata.publishTime }
                ?.let { add("拍摄 $it") }
            metadata.statusId?.takeIf { it.isNotBlank() }?.let { add("微博 $it") }
        }
        return parts.takeIf { it.isNotEmpty() }?.joinToString(" · ")
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

    private fun finalizeMedia(
        context: Context,
        uri: Uri,
        metadata: ImageSaveMetadata? = null,
        xmpSnippet: String? = null,
    ) {
        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.IS_PENDING, 0)
            metadata?.let { meta ->
                parseWeiboCreatedAtMillis(meta.shootTime ?: meta.publishTime)?.let { takenAt ->
                    put(MediaStore.MediaColumns.DATE_TAKEN, takenAt)
                }
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !xmpSnippet.isNullOrBlank()) {
                runCatching { put(MediaStore.Images.Media.XMP, xmpSnippet) }
            }
        }
        context.contentResolver.update(uri, values, null, null)
    }

    private fun sanitize(value: String): String =
        value.replace(Regex("""[^\w.-]"""), "_").ifBlank { "image" }

    private fun looksLikeGif(bytes: ByteArray): Boolean =
        bytes.size >= 6 &&
            bytes[0] == 'G'.code.toByte() &&
            bytes[1] == 'I'.code.toByte() &&
            bytes[2] == 'F'.code.toByte()

    private fun looksLikeJpeg(bytes: ByteArray): Boolean =
        bytes.size >= 3 &&
            bytes[0] == 0xFF.toByte() &&
            bytes[1] == 0xD8.toByte() &&
            bytes[2] == 0xFF.toByte()

    private fun formatFileSize(bytes: Long): String = when {
        bytes >= 1024 * 1024 -> String.format("%.1f MB", bytes / (1024.0 * 1024.0))
        bytes >= 1024 -> String.format("%.1f KB", bytes / 1024.0)
        else -> "$bytes B"
    }
}
