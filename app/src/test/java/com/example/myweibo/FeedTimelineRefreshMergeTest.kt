package com.example.myweibo

import com.example.myweibo.data.FeedItem
import com.example.myweibo.data.mergeFeedTimelineRefresh
import org.junit.Assert.assertEquals
import org.junit.Test

class FeedTimelineRefreshMergeTest {
    @Test fun refreshKeepsPreviousHeadAndOlderHistoryWhilePrependingNewPosts() {
        val previous = listOf(post(3), post(2), post(1))
        val merged = mergeFeedTimelineRefresh(previous, listOf(post(5), post(4), post(3)))
        assertEquals(listOf("5", "4", "3", "2", "1"), merged.map { it.id })
        assertEquals(2, merged.indexOfFirst { it.id == previous.first().id })
    }

    @Test fun nonOverlappingRefreshStillRetainsTheReadingBoundary() {
        val merged = mergeFeedTimelineRefresh(listOf(post(3), post(2)), listOf(post(7), post(6)))
        assertEquals(listOf("7", "6", "3", "2"), merged.map { it.id })
    }

    @Test fun refreshPreservesAlreadyExpandedText() {
        val full = post(3).copy(text = "Full text", isLongText = false, requiresLongTextFetch = false)
        val preview = post(3).copy(text = "Preview", isLongText = true, requiresLongTextFetch = true)
        assertEquals("Full text", mergeFeedTimelineRefresh(listOf(full), listOf(preview)).single().text)
    }

    private fun post(number: Int) = FeedItem(
        id = number.toString(),
        statusId = number.toString(),
        authorId = "author",
        authorName = "Author",
        authorAvatarUrl = null,
        createdAt = (1_700_000_000L + number).toString(),
        source = null,
        ipLocation = null,
        text = "Post $number",
        repostsCount = "0",
        commentsCount = "0",
        likesCount = "0",
        images = emptyList(),
    )
}
