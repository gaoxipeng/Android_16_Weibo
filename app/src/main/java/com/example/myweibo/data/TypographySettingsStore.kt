package com.example.myweibo.data

import android.content.Context

enum class FeedLineSpacing(
    val storageValue: String,
    val label: String,
    val lineHeightMultiplier: Float,
) {
    Tighter("tighter", "很紧", 1.15f),
    Compact("compact", "紧凑", 1.35f),
    Normal("normal", "标准", 1.55f),
    Relaxed("relaxed", "宽松", 1.75f),
    Looser("looser", "很宽", 2.0f),
    ;

    companion object {
        fun fromStorage(value: String?): FeedLineSpacing =
            entries.firstOrNull { it.storageValue == value } ?: Compact
    }
}

enum class FeedFontSize(
    val storageValue: String,
    val label: String,
    val sizeSp: Int,
) {
    Small("small", "较小", 13),
    MediumSmall("medium_small", "偏小", 14),
    Medium("medium", "标准", 15),
    MediumLarge("medium_large", "偏大", 16),
    Large("large", "大号", 18),
    ;

    companion object {
        fun fromStorage(value: String?): FeedFontSize =
            entries.firstOrNull { it.storageValue == value } ?: Medium
    }
}

class TypographySettingsStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun readLineSpacing(): FeedLineSpacing =
        FeedLineSpacing.fromStorage(prefs.getString(KEY_FEED_LINE_SPACING, null))

    fun writeLineSpacing(spacing: FeedLineSpacing) {
        prefs.edit().putString(KEY_FEED_LINE_SPACING, spacing.storageValue).apply()
    }

    fun readFontSize(): FeedFontSize =
        FeedFontSize.fromStorage(prefs.getString(KEY_FEED_FONT_SIZE, null))

    fun writeFontSize(size: FeedFontSize) {
        prefs.edit().putString(KEY_FEED_FONT_SIZE, size.storageValue).apply()
    }

    private companion object {
        const val PREFS_NAME = "weibo_app_prefs"
        const val KEY_FEED_LINE_SPACING = "feed_line_spacing"
        const val KEY_FEED_FONT_SIZE = "feed_font_size"
    }
}
