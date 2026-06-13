package com.example.myweibo.data

import android.content.Context

class SearchSettingsStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun readMode(): String = prefs.getString(KEY_MODE, MODE_WEIBO) ?: MODE_WEIBO

    fun writeMode(mode: String) {
        prefs.edit().putString(KEY_MODE, mode).apply()
    }

    fun readWeiboSort(): String = prefs.getString(KEY_WEIBO_SORT, SORT_COMPREHENSIVE) ?: SORT_COMPREHENSIVE

    fun writeWeiboSort(sort: String) {
        prefs.edit().putString(KEY_WEIBO_SORT, sort).apply()
    }

    companion object {
        const val PREFS_NAME = "weibo_app_prefs"
        const val KEY_MODE = "search_mode"
        const val KEY_WEIBO_SORT = "search_weibo_sort"
        const val MODE_WEIBO = "weibo"
        const val MODE_USER = "user"
        const val SORT_COMPREHENSIVE = "comprehensive"
        const val SORT_REALTIME = "realtime"
    }
}
