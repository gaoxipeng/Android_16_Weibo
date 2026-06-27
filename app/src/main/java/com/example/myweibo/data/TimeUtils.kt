package com.example.myweibo.data

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

private const val WEIBO_TZ = "GMT+0800"
private val WEIBO_DATE_PARSER = SimpleDateFormat("EEE MMM dd HH:mm:ss Z yyyy", Locale.US).apply {
    timeZone = TimeZone.getTimeZone(WEIBO_TZ)
}
private val TIME_FORMATTER = SimpleDateFormat("HH:mm", Locale.getDefault()).apply {
    timeZone = TimeZone.getTimeZone(WEIBO_TZ)
}
private val DATE_TIME_FORMATTER = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault()).apply {
    timeZone = TimeZone.getTimeZone(WEIBO_TZ)
}
private val YEAR_DATE_TIME_FORMATTER = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).apply {
    timeZone = TimeZone.getTimeZone(WEIBO_TZ)
}

fun formatWeiboTime(rawCreatedAt: String?): String {
    if (rawCreatedAt == null) return ""

    val date = runCatching { WEIBO_DATE_PARSER.parse(rawCreatedAt.trim()) }.getOrNull()
        ?: return rawCreatedAt

    val now = Calendar.getInstance(TimeZone.getTimeZone(WEIBO_TZ))
    val created = Calendar.getInstance(TimeZone.getTimeZone(WEIBO_TZ)).apply { time = date }

    val diffMs = now.timeInMillis - created.timeInMillis
    val diffMinutes = diffMs / (1000 * 60)

    if (diffMinutes < 0) {
        return TIME_FORMATTER.format(date)
    }

    if (diffMinutes < 60) {
        return if (diffMinutes <= 0) "刚刚" else "${diffMinutes}分钟前"
    }

    val today = Calendar.getInstance(TimeZone.getTimeZone(WEIBO_TZ)).apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    val yesterday = today.clone() as Calendar
    yesterday.add(Calendar.DAY_OF_MONTH, -1)

    return when {
        !created.before(today) -> TIME_FORMATTER.format(date)
        !created.before(yesterday) -> "昨天 ${TIME_FORMATTER.format(date)}"
        created.get(Calendar.YEAR) == now.get(Calendar.YEAR) -> DATE_TIME_FORMATTER.format(date)
        else -> YEAR_DATE_TIME_FORMATTER.format(date)
    }
}

fun parseWeiboCreatedAtMillis(rawCreatedAt: String?): Long? {
    if (rawCreatedAt == null) return null
    return runCatching { WEIBO_DATE_PARSER.parse(rawCreatedAt.trim())?.time }.getOrNull()
        ?: parseFlexibleTimestampMillis(rawCreatedAt)
}

private val FLEXIBLE_TIMESTAMP_FORMATS = listOf(
    SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()),
    SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()),
    SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault()),
    SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault()),
    SimpleDateFormat("yyyy:MM:dd HH:mm:ss", Locale.US),
)

private fun parseFlexibleTimestampMillis(raw: String): Long? {
    val trimmed = raw.trim()
    trimmed.toLongOrNull()?.let { value ->
        return if (value < 1_000_000_000_000L) value * 1000L else value
    }
    FLEXIBLE_TIMESTAMP_FORMATS.forEach { formatter ->
        runCatching { formatter.parse(trimmed)?.time }?.getOrNull()?.let { return it }
    }
    return null
}

private val EXIF_DATE_FORMATTER = SimpleDateFormat("yyyy:MM:dd HH:mm:ss", Locale.US)

fun formatExifDateTime(rawCreatedAt: String?): String? {
    val millis = parseWeiboCreatedAtMillis(rawCreatedAt) ?: return null
    return EXIF_DATE_FORMATTER.format(Date(millis))
}
