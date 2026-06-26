package com.example.myweibo.data

import android.content.Context

enum class FeedThumbnailQuality(
    val storageValue: String,
    val label: String,
    val description: String,
    val maxDecodeDim: Int,
    private val preferLargeUrl: Boolean,
    private val feedSinaimgVariant: String? = null,
) {
    Low("low", "省流", "中等缩略图 bmiddle，约 440px", 640, preferLargeUrl = false, feedSinaimgVariant = "bmiddle"),
    Medium("medium", "标准", "中图 mw690，约 690px", 960, preferLargeUrl = true, feedSinaimgVariant = "mw690"),
    High("high", "高清", "大图预览 mw1024", 1280, preferLargeUrl = true, feedSinaimgVariant = "mw1024"),
    ;

    companion object {
        fun fromStorage(value: String?): FeedThumbnailQuality =
            entries.firstOrNull { it.storageValue == value } ?: Medium
    }

    fun displayUrl(image: FeedImage): String {
        val preferred = if (preferLargeUrl) {
            image.largeUrl.ifBlank { image.thumbnailUrl }
        } else {
            image.thumbnailUrl.ifBlank { image.largeUrl }
        }
        return preferred.withSinaimgVariant(feedSinaimgVariant)
    }

    fun fallbackUrl(image: FeedImage): String? {
        val primary = displayUrl(image)
        val fallback = if (preferLargeUrl) image.thumbnailUrl else image.largeUrl
        return fallback
            .withSinaimgVariant(feedSinaimgVariant)
            .takeIf { it.isNotBlank() && it != primary }
    }
}

private fun String.withSinaimgVariant(variant: String?): String {
    if (variant.isNullOrBlank()) return this
    if (!contains("sinaimg.cn", ignoreCase = true)) return this
    return replace(
        Regex("""/(?:large|mw2000|woriginal|original|bmiddle|orj360|orj480|mw690|mw1024|thumbnail|thumb(?:180|300|150)?|small|wap360)/""", RegexOption.IGNORE_CASE),
        "/$variant/",
    )
}

class ImageSettingsStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun readThumbnailQuality(): FeedThumbnailQuality =
        FeedThumbnailQuality.fromStorage(prefs.getString(KEY_FEED_THUMBNAIL_QUALITY, null))

    fun writeThumbnailQuality(quality: FeedThumbnailQuality) {
        prefs.edit().putString(KEY_FEED_THUMBNAIL_QUALITY, quality.storageValue).apply()
    }

    private companion object {
        const val PREFS_NAME = "weibo_app_prefs"
        const val KEY_FEED_THUMBNAIL_QUALITY = "feed_thumbnail_quality"
    }
}
