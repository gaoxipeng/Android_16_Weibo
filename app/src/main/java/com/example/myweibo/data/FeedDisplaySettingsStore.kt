package com.example.myweibo.data

import android.content.Context

class FeedDisplaySettingsStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun readHideBottomBarOnScroll(): Boolean =
        prefs.getBoolean(KEY_HIDE_BOTTOM_BAR_ON_SCROLL, true)

    fun writeHideBottomBarOnScroll(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_HIDE_BOTTOM_BAR_ON_SCROLL, enabled).apply()
    }

    // Keep the existing key for frost so users retain their previous slider value.
    fun readBottomBarFrostAmount(): Float =
        normalizeBottomBarGlassAmount(prefs.getFloat(KEY_BOTTOM_BAR_GLASS_AMOUNT, 0f))

    fun writeBottomBarFrostAmount(value: Float) {
        prefs.edit().putFloat(KEY_BOTTOM_BAR_GLASS_AMOUNT, normalizeBottomBarGlassAmount(value)).apply()
    }

    fun readBottomBarGlassAmount(): Float =
        normalizeBottomBarGlassAmount(prefs.getFloat(KEY_BOTTOM_BAR_REFRACTION_AMOUNT, 1f))

    fun writeBottomBarGlassAmount(value: Float) {
        prefs.edit().putFloat(KEY_BOTTOM_BAR_REFRACTION_AMOUNT, normalizeBottomBarGlassAmount(value)).apply()
    }

    fun readShowCommentLikeCount(): Boolean = prefs.getBoolean("show_comment_like_count", true)

    fun readTimelineKind(): TimelineKind = TimelineKind.fromStorage(prefs.getString("home_timeline_kind", null))

    fun writeTimelineKind(kind: TimelineKind) {
        prefs.edit().putString("home_timeline_kind", kind.name).apply()
    }

    fun writeShowCommentLikeCount(enabled: Boolean) {
        prefs.edit().putBoolean("show_comment_like_count", enabled).apply()
    }

    private companion object {
        const val PREFS_NAME = "weibo_app_prefs"
        const val KEY_HIDE_BOTTOM_BAR_ON_SCROLL = "feed_hide_bottom_bar_on_scroll"
        const val KEY_BOTTOM_BAR_GLASS_AMOUNT = "bottom_bar_glass_amount"
        const val KEY_BOTTOM_BAR_REFRACTION_AMOUNT = "bottom_bar_refraction_amount"
    }
}
