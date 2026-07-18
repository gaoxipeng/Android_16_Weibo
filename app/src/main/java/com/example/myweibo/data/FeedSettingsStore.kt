package com.example.myweibo.data

import android.content.Context

enum class FeedBrowseMode(
    val storageValue: String,
    val label: String,
    val description: String,
) {
    Timeline(
        storageValue = "timeline",
        label = "连续信息流",
        description = "像官方微博一样连续上下浏览",
    ),
    Immersive(
        storageValue = "immersive",
        label = "单条浏览",
        description = "右侧切换微博，左侧滚动正文与评论",
    ),
}

class FeedSettingsStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun readBrowseMode(): FeedBrowseMode {
        val stored = prefs.getString(KEY_BROWSE_MODE, FeedBrowseMode.Timeline.storageValue)
        return FeedBrowseMode.entries.firstOrNull { it.storageValue == stored } ?: FeedBrowseMode.Timeline
    }

    fun writeBrowseMode(mode: FeedBrowseMode) {
        prefs.edit().putString(KEY_BROWSE_MODE, mode.storageValue).apply()
    }

    fun readImmersiveLastStatusId(): String? =
        prefs.getString(KEY_IMMERSIVE_LAST_STATUS_ID, null)?.takeIf { it.isNotBlank() }

    fun writeImmersiveLastStatusId(statusId: String) {
        if (statusId.isBlank()) return
        prefs.edit().putString(KEY_IMMERSIVE_LAST_STATUS_ID, statusId).apply()
    }

    private companion object {
        const val PREFS_NAME = "weibo_app_prefs"
        const val KEY_BROWSE_MODE = "feed_browse_mode"
        const val KEY_IMMERSIVE_LAST_STATUS_ID = "immersive_last_status_id"
    }
}
