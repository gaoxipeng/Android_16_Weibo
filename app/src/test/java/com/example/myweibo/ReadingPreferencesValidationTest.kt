package com.example.myweibo

import com.example.myweibo.data.ReadingNumber
import com.example.myweibo.data.ReadingPreferences
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test

class ReadingPreferencesValidationTest {
    @Test fun defaultsAreOffWithSafeLimits() {
        val defaults = ReadingPreferences()
        assertFalse(defaults.foldLongPosts)
        assertFalse(defaults.reverseReading)
        assertEquals(8, defaults.maxLines)
        assertEquals(20, defaults.newPostLimit)
    }

    @Test fun dialogInputAcceptsInclusiveRangesWithoutClamping() {
        val lines = ReadingNumber.BodyLines
        assertEquals(3, lines.parse("3"))
        assertEquals(30, lines.parse("30"))
        assertNull(lines.parse("2"))
        assertNull(lines.parse("31"))

        val posts = ReadingNumber.NewPostLimit
        assertEquals(1, posts.parse("1"))
        assertEquals(200, posts.parse(" 200 "))
        listOf("", "0", "201", "2.5", "abc", "99999999999999").forEach {
            assertNull(posts.parse(it))
        }
    }

    @Test fun missingMalformedOrOutOfRangeStoredValuesFallBackToDefaults() {
        val lines = ReadingNumber.BodyLines
        listOf(null, "8", 8L, 2, 31).forEach { assertEquals(8, lines.readSaved(it)) }
        assertEquals(30, lines.readSaved(30))
        val posts = ReadingNumber.NewPostLimit
        assertEquals(20, posts.readSaved(201))
        assertEquals(1, posts.readSaved(1))
    }
}
