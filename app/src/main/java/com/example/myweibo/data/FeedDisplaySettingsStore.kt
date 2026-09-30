package com.example.myweibo.data

import android.content.Context

class FeedDisplaySettingsStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun readHideBottomBarOnScroll(): Boolean =
        prefs.getBoolean(KEY_HIDE_BOTTOM_BAR_ON_SCROLL, true)

    fun writeHideBottomBarOnScroll(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_HIDE_BOTTOM_BAR_ON_SCROLL, enabled).apply()
    }

    private companion object {
        const val PREFS_NAME = "weibo_app_prefs"
        const val KEY_HIDE_BOTTOM_BAR_ON_SCROLL = "feed_hide_bottom_bar_on_scroll"
    }
}
