package com.example.myweibo

import com.example.myweibo.data.FeedImage
import com.example.myweibo.data.FeedItem
import com.example.myweibo.data.FeedUrlEntity
import com.example.myweibo.data.FeedTimelineGap
import com.example.myweibo.data.mergeFeedTimelinePages
import com.example.myweibo.data.mergeFeedTimelineRefresh
import com.example.myweibo.data.projectFeedTimelineRefresh
import com.example.myweibo.data.replaceFeedTimelineRefresh
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class FeedTimelineRefreshPolicyTest {
    @Test fun refreshRetainsHistoryAndHidesOnlyTheOverflowBeforeThePreviousHead() {
        val current = listOf(post(4), post(3), post(1))
        val incoming = listOf(post(7), post(6), post(5), post(4), post(2))
        val full = mergeFeedTimelineRefresh(current, incoming)
        val projection = projectFeedTimelineRefresh(full, current.first().id, 2)

        assertEquals(ids(7, 6, 5, 4, 3, 2, 1), full.map { it.id })
        assertEquals(ids(7, 6, 4, 3, 2, 1), projection.visibleItems.map { it.id })
        assertEquals(FeedTimelineGap(insertionIndex = 2, hiddenPostCount = 1), projection.gap)
    }

    @Test fun repeatedRefreshesKeepTheSameBoundaryAndDoNotExposeOrCountHiddenDuplicates() {
        val current = listOf(post(4), post(3))
        val boundary = current.first().id
        val first = mergeFeedTimelineRefresh(current, listOf(post(6), post(5), post(4)))
        assertNull(projectFeedTimelineRefresh(first, boundary, 2).gap)

        val second = mergeFeedTimelineRefresh(first, listOf(post(8), post(7), post(6)))
        val projection = projectFeedTimelineRefresh(second, boundary, 2)
        assertEquals(ids(8, 7, 4, 3), projection.visibleItems.map { it.id })
        assertEquals(FeedTimelineGap(2, 2), projection.gap)

        // Older refresh response including already hidden posts, and an empty response.
        for (incoming in listOf(listOf(post(6), post(5), post(4)), emptyList())) {
            val again = mergeFeedTimelineRefresh(second, incoming)
            assertEquals(second, again)
            assertEquals(projection, projectFeedTimelineRefresh(again, boundary, 2))
        }
        assertEquals(second, projectFeedTimelineRefresh(second, null, 2).visibleItems)
    }

    @Test fun noOverlapCountsOnlyFetchedPostsNewerThanPreviousHead() {
        val current = listOf(post(5), post(3))
        val full = mergeFeedTimelineRefresh(current, listOf(post(50), post(8), post(4), post(2)))
        val projection = projectFeedTimelineRefresh(full, current.first().id, 1)

        assertEquals(ids(50, 8, 5, 4, 3, 2), full.map { it.id })
        assertEquals(ids(50, 5, 4, 3, 2), projection.visibleItems.map { it.id })
        assertEquals(FeedTimelineGap(1, 1), projection.gap)
    }

    @Test fun overlapAnchorsUndatedPostsButDisjointUndatedPostsAreNotPresumedNew() {
        val current = listOf(post(4, dated = false), post(3, dated = false))
        val full = mergeFeedTimelineRefresh(current, listOf(post(5, dated = false), current.first()))
        val boundary = current.first().id
        assertEquals(ids(5, 4, 3), full.map { it.id })
        assertEquals(FeedTimelineGap(0, 1), projectFeedTimelineRefresh(full, boundary, 0).gap)

        val disjoint = mergeFeedTimelineRefresh(current, listOf(post(5, dated = false)))
        assertEquals(ids(4, 3, 5), disjoint.map { it.id })
        assertNull(projectFeedTimelineRefresh(disjoint, boundary, 0).gap)
    }

    @Test fun duplicatesUseIdOrNonblankStatusIdWithoutDroppingDistinctBlankStatuses() {
        val old = post(4)
        val firstNew = post(7).copy(statusId = "")
        val secondNew = post(6).copy(statusId = "")
        val thirdNew = post(5)
        val incoming = listOf(
            firstNew, firstNew.copy(text = "duplicate id"), secondNew, thirdNew,
            thirdNew.copy(id = "alias-5"), old.copy(id = "alias-4", text = "different"),
        )
        val full = mergeFeedTimelineRefresh(listOf(old, old, post(3)), incoming)

        assertEquals(listOf(firstNew, secondNew, thirdNew, old.copy(text = "different"), post(3)), full)
        assertEquals(FeedTimelineGap(1, 2), projectFeedTimelineRefresh(full, old.id, 1).gap)
    }

    @Test fun matchedPostsRefreshTheirServerPayloadWithoutChangingRowIdOrPosition() {
        val old = post(4)
        val fresh = old.copy(
            id = "server-alias",
            text = "Updated server body",
            likesCount = "14",
            commentsCount = "3",
            repostsCount = "2",
            liked = true,
        )
        val full = mergeFeedTimelineRefresh(listOf(post(5), old, post(3)), listOf(fresh))

        assertEquals(listOf(post(5), fresh.copy(id = old.id), post(3)), full)
    }

    @Test fun aTruncatedRefreshDoesNotReplaceResolvedTextOrItsResources() {
        val image = FeedImage("image", "https://example.com/thumb", "https://example.com/full")
        val url = FeedUrlEntity("https://t.cn/link", "Article", "https://example.com/article")
        val expanded = post(4).copy(
            text = "The entire server body https://t.cn/link",
            emoticons = mapOf("[smile]" to "https://example.com/smile"),
            images = listOf(image),
            inlineImageLinks = mapOf("https://t.cn/image" to listOf(image)),
            urlEntities = listOf(url),
        )
        val preview = post(4).copy(
            text = "The entire…", isLongText = true, requiresLongTextFetch = true, likesCount = "8",
        )
        val full = mergeFeedTimelineRefresh(listOf(expanded), listOf(preview))

        assertEquals(listOf(expanded.copy(likesCount = "8")), full)
        val revisedPreview = preview.copy(editCount = 1)
        assertEquals(listOf(revisedPreview), mergeFeedTimelineRefresh(full, listOf(revisedPreview)))
    }

    @Test fun refreshPreservesExpandedRetweetBodyIndependentlyOfTheOuterPost() {
        val expanded = post(3).copy(text = "Full original post")
        val current = post(4).copy(retweetedStatus = expanded)
        val preview = post(3).copy(text = "Full…", requiresLongTextFetch = true, commentsCount = "2")
        val incoming = current.copy(text = "Refreshed outer post", retweetedStatus = preview)

        val full = mergeFeedTimelineRefresh(listOf(current), listOf(incoming))
        assertEquals(listOf(incoming.copy(retweetedStatus = expanded.copy(commentsCount = "2"))), full)
    }

    @Test fun replacementKeepsOverlappingRowIdentityAndFullBodiesButNotOldOrderOrTail() {
        val quote = post(1).copy(text = "Entire quote")
        val old = post(3).copy(isPinned = true, text = "Entire post", retweetedStatus = quote)
        val fresh = old.copy(
            id = "outer-alias", isPinned = false, text = "Preview…", requiresLongTextFetch = true,
            likesCount = "2",
            retweetedStatus = quote.copy(id = "quote-alias", text = "Preview…", requiresLongTextFetch = true),
        )
        val current = listOf(old, post(2), post(4))
        assertEquals(
            listOf(post(2), old.copy(isPinned = false, likesCount = "2")),
            replaceFeedTimelineRefresh(current, listOf(post(2), fresh)),
        )
        val edited = fresh.copy(editCount = 1)
        assertEquals(
            listOf(edited.copy(id = old.id, retweetedStatus = quote)),
            replaceFeedTimelineRefresh(current, listOf(edited)),
        )
        assertEquals(listOf(fresh), replaceFeedTimelineRefresh(emptyList(), listOf(fresh)))
    }

    @Test fun initialLoadAndNoNewPostsDoNotCreateAGap() {
        val initial = mergeFeedTimelineRefresh(emptyList(), listOf(post(4), post(4), post(3)))
        assertEquals(ids(4, 3), initial.map { it.id })
        val firstLoad = projectFeedTimelineRefresh(initial, previousHeadId = null, maxVisibleNewPosts = 0)
        assertEquals(initial, firstLoad.visibleItems)
        assertNull(firstLoad.gap)

        val olderOnly = mergeFeedTimelineRefresh(initial, listOf(post(4), post(2)))
        val noNew = projectFeedTimelineRefresh(olderOnly, initial.first().id, 0)
        assertEquals(ids(4, 3, 2), noNew.visibleItems.map { it.id })
        assertNull(noNew.gap)
    }

    @Test fun paginationConsumesFullTimelineAndProjectionDoesNotCountAppendedHistory() {
        val previous = listOf(post(3), post(2))
        val full = mergeFeedTimelineRefresh(previous, listOf(post(6), post(5), post(4), post(3)))
        val (paged, appended) = mergeFeedTimelinePages(full, listOf(post(4), post(2), post(1)))
        val projection = projectFeedTimelineRefresh(paged, previous.first().id, 1)

        assertEquals(1, appended)
        assertEquals(ids(6, 5, 4, 3, 2, 1), paged.map { it.id })
        assertEquals(ids(6, 3, 2, 1), projection.visibleItems.map { it.id })
        assertEquals(FeedTimelineGap(1, 2), projection.gap)
    }

    @Test fun oldAndMovedPinsStayInPlaceAndDoNotCountTowardTheGap() {
        val pinned = post(1).copy(isPinned = true)
        val previous = listOf(pinned, post(5), post(4), post(2))
        val movedPin = post(2).copy(isPinned = true)
        val newlyFetchedOldPin = post(3).copy(isPinned = true)
        val full = mergeFeedTimelineRefresh(
            previous, listOf(movedPin, newlyFetchedOldPin, pinned, post(8), post(7), post(5)),
        )

        assertEquals(ids(1, 8, 7, 5, 4, 3, 2), full.map { it.id })
        assertEquals(previous, full.filter { row -> previous.any { it.id == row.id } })
        val projection = projectFeedTimelineRefresh(full, previous.first { !it.isPinned }.id, 1)
        assertEquals(ids(1, 8, 5, 4, 3, 2), projection.visibleItems.map { it.id })
        assertEquals(FeedTimelineGap(2, 1), projection.gap)
        assertNull(projectFeedTimelineRefresh(full, pinned.id, 0).gap)
    }

    @Test fun movedPinsAreNotOverlapAnchorsForUndatedPosts() {
        val previous = listOf(post(5, dated = false), post(4, dated = false))
        val movedHead = previous.first().copy(isPinned = true)
        val full = mergeFeedTimelineRefresh(previous, listOf(post(3, dated = false), movedHead))

        assertEquals(ids(5, 4, 3), full.map { it.id })
        assertNull(projectFeedTimelineRefresh(full, previous.first().id, 0).gap)
    }

    @Test fun oldRowsBeforeADeeperOverlapDoNotCountWhenTheRefreshOmitsTheBoundary() {
        val previous = listOf(post(8), post(5), post(2))
        val full = mergeFeedTimelineRefresh(previous, listOf(post(10), post(7), post(5)))

        assertEquals(ids(10, 8, 7, 5, 2), full.map { it.id })
        val projection = projectFeedTimelineRefresh(full, previous.first().id, 0)
        assertEquals(ids(8, 7, 5, 2), projection.visibleItems.map { it.id })
        assertEquals(FeedTimelineGap(0, 1), projection.gap)
        val boundaryRemoved = full.filterNot { it.id == previous.first().id }
        val missing = projectFeedTimelineRefresh(boundaryRemoved, previous.first().id, 0)
        assertEquals(boundaryRemoved, missing.visibleItems)
        assertNull(missing.gap)
    }

    @Test fun knownOldPostsDoNotMoveAboveTheBoundaryEvenWhenTheServerListsThemFirst() {
        val previous = listOf(post(8), post(5), post(2))
        val full = mergeFeedTimelineRefresh(previous, listOf(post(7), post(8), post(5)))

        assertEquals(ids(8, 7, 5, 2), full.map { it.id })
        assertNull(projectFeedTimelineRefresh(full, previous.first().id, 0).gap)
    }

    @Test fun changingLimitReprojectsWithoutLosingPosts() {
        val full = listOf(post(5), post(4), post(3), post(2))
        val zero = projectFeedTimelineRefresh(full, post(3).id, -1)
        assertEquals(ids(3, 2), zero.visibleItems.map { it.id })
        assertEquals(FeedTimelineGap(0, 2), zero.gap)
        val unlimited = projectFeedTimelineRefresh(full, post(3).id, Int.MAX_VALUE)
        assertEquals(full, unlimited.visibleItems)
        assertNull(unlimited.gap)
        assertNull(projectFeedTimelineRefresh(full, "missing", 0).gap)
    }

    private fun ids(vararg numbers: Int) = numbers.map { "id-$it" }

    private fun post(number: Int, dated: Boolean = true) = FeedItem(
        id = "id-$number",
        statusId = "status-$number",
        authorId = "author",
        authorName = "Author",
        authorAvatarUrl = null,
        createdAt = if (dated) (1_700_000_000L + number).toString() else null,
        source = null,
        ipLocation = null,
        text = "Post $number",
        repostsCount = "0",
        commentsCount = "0",
        likesCount = "0",
        images = emptyList(),
    )
}
