package com.example.myweibo.data

import android.content.Context

enum class FeedThumbnailQuality(
    val storageValue: String,
    val label: String,
    val description: String,
    val maxDecodeDim: Int,
    private val preferLargeUrl: Boolean,
) {
    Low("low", "省流", "bmiddle，约 440px", 480, preferLargeUrl = false),
    Medium("medium", "标准", "large，约 690px", 960, preferLargeUrl = true),
    High("high", "高清", "最高可用规格", 2048, preferLargeUrl = true),
    ;

    companion object {
        fun fromStorage(value: String?): FeedThumbnailQuality =
            entries.firstOrNull { it.storageValue == value } ?: Medium
    }

    fun displayUrl(image: FeedImage): String =
        if (preferLargeUrl) {
            image.largeUrl.ifBlank { image.thumbnailUrl }
        } else {
            image.thumbnailUrl.ifBlank { image.largeUrl }
        }

    fun fallbackUrl(image: FeedImage): String? {
        val primary = displayUrl(image)
        val fallback = if (preferLargeUrl) image.thumbnailUrl else image.largeUrl
        return fallback.takeIf { it.isNotBlank() && it != primary }
    }
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
