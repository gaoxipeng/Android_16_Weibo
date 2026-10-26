package com.example.myweibo.data

import java.io.File
import java.nio.file.Files

enum class AppCacheCategory(val label: String) {
    Images("图片缓存"), Videos("视频缓存"), Web("网页缓存"),
    Temporary("分享与临时文件"), Other("其他缓存"),
}

data class AppCacheSizes(val bytes: Map<AppCacheCategory, Long>) {
    val total: Long get() = bytes.values.sum()
    operator fun get(category: AppCacheCategory): Long = bytes[category] ?: 0L
}

data class AppCacheClearResult(val freedBytes: Long, val remainingBytes: Long, val failedFiles: Int)

/** Only cacheDir is scanned. Never traverse symlinks or touch filesDir/account storage. */
object AppCacheFiles {
    fun category(relativePath: String): AppCacheCategory {
        val first = relativePath.replace('\\', '/').substringBefore('/').lowercase()
        return when {
            first == "remote-image-bytes" -> AppCacheCategory.Images
            first == "weibo-video" -> AppCacheCategory.Videos
            first.contains("webview") || first.startsWith("org.chromium") -> AppCacheCategory.Web
            first.startsWith("share_") || first.startsWith("motion_") -> AppCacheCategory.Temporary
            else -> AppCacheCategory.Other
        }
    }

    private fun files(root: File): List<File> {
        val base = root.canonicalFile
        if (!base.isDirectory) return emptyList()
        val basePath = base.toPath()
        return base.walkTopDown().onEnter {
            !Files.isSymbolicLink(it.toPath()) && it.canonicalFile.toPath().startsWith(basePath)
        }.filter {
            it.isFile && !Files.isSymbolicLink(it.toPath()) && it.canonicalFile.toPath().startsWith(basePath)
        }.toList()
    }

    fun measure(root: File): AppCacheSizes {
        val base = root.canonicalFile
        val sizes = AppCacheCategory.entries.associateWith { 0L }.toMutableMap()
        files(base).forEach { file ->
            val kind = category(file.relativeTo(base).path)
            sizes[kind] = sizes.getValue(kind) + file.length().coerceAtLeast(0L)
        }
        return AppCacheSizes(sizes)
    }

    fun clearFiles(root: File, categories: Set<AppCacheCategory>, now: Long = System.currentTimeMillis()): Int {
        val base = root.canonicalFile
        var failures = 0
        files(base).forEach { file ->
            val kind = category(file.relativeTo(base).path)
            if (kind !in categories) return@forEach
            // A receiver may still be reading a just-shared file; image writers
            // atomically rename pending files when finished.
            if ((file.name.endsWith(".pending") || kind == AppCacheCategory.Temporary) &&
                now - file.lastModified() < 5 * 60_000L) return@forEach
            if (!file.canonicalFile.toPath().startsWith(base.toPath())) return@forEach
            if (!file.delete() && file.exists()) failures++
        }
        return failures
    }
}
