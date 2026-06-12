package com.example.myweibo.data

import android.content.Context

class PlaybackSettingsStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun readBackgroundPlaybackEnabled(): Boolean =
        prefs.getBoolean(KEY_BACKGROUND_PLAYBACK, false)

    fun writeBackgroundPlaybackEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_BACKGROUND_PLAYBACK, enabled).apply()
    }

    private companion object {
        const val PREFS_NAME = "weibo_app_prefs"
        const val KEY_BACKGROUND_PLAYBACK = "background_playback_enabled"
    }
}
