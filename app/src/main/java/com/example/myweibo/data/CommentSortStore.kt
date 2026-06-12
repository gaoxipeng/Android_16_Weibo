package com.example.myweibo.data

import android.content.Context

class CommentSortStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun read(): CommentSort = CommentSort.fromFlow(prefs.getString(KEY_SORT_FLOW, null))

    fun write(sort: CommentSort) {
        prefs.edit().putString(KEY_SORT_FLOW, sort.flow).apply()
    }

    private companion object {
        const val PREFS_NAME = "weibo_app_prefs"
        const val KEY_SORT_FLOW = "comment_sort_flow"
    }
}
