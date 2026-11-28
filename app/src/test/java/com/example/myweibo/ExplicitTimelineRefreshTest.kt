package com.example.myweibo

import com.example.myweibo.data.FeedItem
import com.example.myweibo.data.TimelineKind
import com.example.myweibo.data.TimelineRefreshPostIdentity
import com.example.myweibo.data.TimelineRefreshRoute
import com.example.myweibo.data.ConfirmedTimelineRefresh
import com.example.myweibo.data.classifyExplicitTimelineRefresh
import com.example.myweibo.data.countNewTimelinePosts
import com.example.myweibo.data.hasChronologicalResponse
import com.example.myweibo.data.mergeFeedTimelineRefresh
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExplicitTimelineRefreshTest {
    @Test fun refreshVerificationSeparatesAccountsGroupsAndBuiltInSpecialFollowing() {
        fun route(account: String, kind: TimelineKind, group: String? = null) =
            requireNotNull(TimelineRefreshRoute.of(account, kind, group))
        assertNotEquals(route("123", TimelineKind.Following), route("456", TimelineKind.Following))
        assertNotEquals(route("123", TimelineKind.SpecialFollowing), route("123", TimelineKind.SpecialFollowing, "9"))
        assertNotEquals(route("123", TimelineKind.SpecialFollowing, "9"), route("123", TimelineKind.SpecialFollowing, "10"))
        assertNull(TimelineRefreshRoute.of(null, TimelineKind.Following, null))
        assertNull(TimelineRefreshRoute.of("123", TimelineKind.Hot, null))
    }

    @Test fun standaloneSpecialNeedsARealChronologicalResponse() {
        val pin = post(90).copy(isPinned = true)
        assertFalse(hasChronologicalResponse(listOf(pin)))
        assertFalse(hasChronologicalResponse(listOf(post(2), post(3))))
        assertFalse(hasChronologicalResponse(listOf(post(2).copy(createdAt = null))))
        assertTrue(hasChronologicalResponse(listOf(post(3), pin, post(2))))
    }

    @Test fun successfulNoNewIsDifferentFromEmptyAndPinChangesOrBackfillsAreNotNew() {
        val previous = listOf(post(4), post(2))
        val reorderedPin = post(9).copy(isPinned = true)
        assertNull(countNewTimelinePosts(previous, emptyList(), ordered = true))
        assertNull(countNewTimelinePosts(emptyList(), previous, ordered = true))
        assertNull(countNewTimelinePosts(previous, listOf(reorderedPin), ordered = true))
        assertEquals(0, countNewTimelinePosts(previous, listOf(reorderedPin, post(4), post(3)), ordered = true))
        val prefix = post(6)
        assertEquals(1, countNewTimelinePosts(previous, listOf(prefix, prefix.copy(id = "alias"), post(4)), ordered = true))
    }

    @Test fun tiedOrUndatedDisjointPostsAreInconclusiveUntilThereIsOrderingEvidence() {
        val head = post(4)
        val previous = listOf(head, post(2))
        val tied = post(5).copy(createdAt = head.createdAt)
        val undated = post(6).copy(createdAt = null)
        assertNull(countNewTimelinePosts(previous, listOf(tied), ordered = true))
        assertNull(countNewTimelinePosts(previous, listOf(undated), ordered = true))
        assertEquals(1, countNewTimelinePosts(previous, listOf(tied, head), ordered = true))
        assertEquals(0, countNewTimelinePosts(previous, listOf(head, undated), ordered = true))
    }

    @Test fun explicitRefreshUsesThePreRequestHead() {
        // Newest first: old B/A/R, then fetch new D/C followed by B. A/B are not in the new batch.
        val previous = listOf(post(6), post(5), post(4), post(2))
        val oldHeadAlias = post(6).copy(id = "response-alias-for-B")
        val newHead = post(8)
        val pin = post(90).copy(isPinned = true)
        val response = listOf(pin, newHead, newHead.copy(id = "response-alias-for-D"), post(7), oldHeadAlias, post(5))
        assertEquals(
            ConfirmedTimelineRefresh(baseline = anchor(6), newPosts = listOf(anchor(8), anchor(7))),
            classifyExplicitTimelineRefresh(previous, response),
        )
        assertEquals(
            ConfirmedTimelineRefresh(baseline = anchor(6), newPosts = emptyList()),
            classifyExplicitTimelineRefresh(previous, listOf(pin, oldHeadAlias, post(5), post(3), post(2))),
        )
    }

    @Test fun incomingIdentityConflictsAreRejectedBeforeRowOrStatusDeduplication() {
        val head = post(6)
        val previous = listOf(head, post(5))
        val fresh = post(7)
        val alias = fresh.copy(id = "fresh-row-alias")
        val mergedConflict = alias.copy(statusId = post(8).statusId)
        assertNull(classifyExplicitTimelineRefresh(previous, listOf(fresh, fresh.copy(statusId = post(8).statusId), head)))
        assertNull(classifyExplicitTimelineRefresh(previous, listOf(fresh, alias, mergedConflict, head)))
        assertEquals(
            ConfirmedTimelineRefresh(baseline = anchor(6), newPosts = listOf(anchor(7))),
            classifyExplicitTimelineRefresh(previous, listOf(fresh, alias, head)),
        )
    }

    @Test fun sameSecondPrefixIsNewOnlyBeforeAnExactOverlappingHead() {
        val head = post(6)
        val previous = listOf(head, post(5))
        val oldestNew = post(7).copy(createdAt = head.createdAt)
        val newest = post(8).copy(createdAt = head.createdAt)
        val headAlias = head.copy(id = "known-head-alias")
        val page = listOf(newest, oldestNew, headAlias)
        val batch = classifyExplicitTimelineRefresh(previous, page)
        assertEquals(ConfirmedTimelineRefresh(baseline = anchor(6), newPosts = listOf(anchor(8), anchor(7))), batch)
        assertEquals(listOf(newest.id, oldestNew.id, head.id, post(5).id), mergeFeedTimelineRefresh(previous, page).map { it.id })
        assertNull(classifyExplicitTimelineRefresh(previous, page.dropLast(1)))
        assertEquals(
            ConfirmedTimelineRefresh(baseline = anchor(6), newPosts = emptyList()),
            classifyExplicitTimelineRefresh(previous, listOf(headAlias, oldestNew)),
        )
    }

    @Test fun outOfOrderDisjointAndOlderPagesCannotConfirmAnExplicitBatch() {
        val previous = listOf(post(6), post(5), post(4), post(2))
        val cases = mapOf(
            "empty" to emptyList(),
            "pins only" to listOf(post(90).copy(isPinned = true)),
            "newer but disjoint" to listOf(post(8), post(7)),
            "deeper overlap without previous head" to listOf(post(8), post(5)),
            "reordered known rows" to listOf(post(6), post(4), post(5)),
            "unfamiliar newer row out of order" to listOf(post(6), post(7), post(5)),
            "older pagination page" to listOf(post(4), post(2), post(1)),
        )
        cases.forEach { (scenario, page) ->
            assertNull(scenario, classifyExplicitTimelineRefresh(previous, page))
        }
    }

    @Test fun incompleteSeamCannotConfirmZeroAfterDisjointRowsWereMerged() {
        val previous = listOf(post(8), post(7), post(6), post(5), post(4))
        val unresolvedSeam = listOf(anchor(7) to anchor(6))
        assertNull(classifyExplicitTimelineRefresh(previous, previous.take(2), requiredSeams = unresolvedSeam))
        assertEquals(
            ConfirmedTimelineRefresh(baseline = anchor(8), newPosts = emptyList()),
            classifyExplicitTimelineRefresh(previous, previous.take(3), requiredSeams = unresolvedSeam),
        )
    }

    private fun anchor(number: Int) = requireNotNull(TimelineRefreshPostIdentity.from(post(number)))

    private fun post(number: Int) = FeedItem(
        id = "row-$number", statusId = "status-$number", authorId = "author", authorName = "Author",
        authorAvatarUrl = null, createdAt = (1_700_000_000L + number).toString(), source = null,
        ipLocation = null, text = "Post $number", repostsCount = "0", commentsCount = "0", likesCount = "0",
        images = emptyList(),
    )
}
