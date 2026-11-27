package com.example.myweibo.data

import android.content.Context

data class ReadingPreferences(
    val foldLongPosts: Boolean = false,
    val maxLines: Int = ReadingNumber.BodyLines.defaultValue,
    val reverseReading: Boolean = false,
    val newPostLimit: Int = ReadingNumber.NewPostLimit.defaultValue,
)

class ReadingPreferencesStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences(
        "weibo_reading_prefs",
        Context.MODE_PRIVATE,
    )

    fun read(): ReadingPreferences {
        val saved = prefs.all
        return ReadingPreferences(
            foldLongPosts = saved[KEY_FOLD_LONG_POSTS] as? Boolean ?: false,
            maxLines = ReadingNumber.BodyLines.readSaved(saved[KEY_MAX_LINES]),
            reverseReading = saved[KEY_REVERSE_READING] as? Boolean ?: false,
            newPostLimit = ReadingNumber.NewPostLimit.readSaved(saved[KEY_NEW_POST_LIMIT]),
        )
    }

    /** Reject invalid values without changing any saved preferences. */
    fun write(preferences: ReadingPreferences): Boolean {
        if (preferences.maxLines !in ReadingNumber.BodyLines.range ||
            preferences.newPostLimit !in ReadingNumber.NewPostLimit.range
        ) return false

        prefs.edit()
            .putBoolean(KEY_FOLD_LONG_POSTS, preferences.foldLongPosts)
            .putInt(KEY_MAX_LINES, preferences.maxLines)
            .putBoolean(KEY_REVERSE_READING, preferences.reverseReading)
            .putInt(KEY_NEW_POST_LIMIT, preferences.newPostLimit)
            .apply()
        return true
    }

    private companion object {
        const val KEY_FOLD_LONG_POSTS = "fold_long_posts"
        const val KEY_MAX_LINES = "max_lines"
        const val KEY_REVERSE_READING = "reverse_reading"
        const val KEY_NEW_POST_LIMIT = "new_post_limit"
    }
}

internal enum class ReadingNumber(val range: IntRange, val defaultValue: Int) {
    BodyLines(3..30, 8),
    NewPostLimit(1..200, 20);

    fun parse(input: String): Int? = input.trim().toIntOrNull()?.takeIf { it in range }

    fun readSaved(value: Any?): Int = (value as? Int)?.takeIf { it in range } ?: defaultValue
}
