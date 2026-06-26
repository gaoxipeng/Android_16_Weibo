package com.example.myweibo.data

import android.content.Context

class ThemeSettingsStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun readThemeColor(): String =
        prefs.getString(KEY_THEME_COLOR, DEFAULT_THEME_COLOR).orEmpty().ifBlank { DEFAULT_THEME_COLOR }

    fun writeThemeColor(value: String) {
        prefs.edit().putString(KEY_THEME_COLOR, value).apply()
    }

    private companion object {
        const val PREFS_NAME = "weibo_app_prefs"
        const val KEY_THEME_COLOR = "theme_color"
        const val DEFAULT_THEME_COLOR = "dusty_rose"
    }
}
