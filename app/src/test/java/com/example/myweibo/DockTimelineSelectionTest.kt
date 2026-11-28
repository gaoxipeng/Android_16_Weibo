package com.example.myweibo

import com.example.myweibo.data.TimelineGroup
import com.example.myweibo.data.TimelineKind
import com.example.myweibo.data.DockEntry
import com.example.myweibo.data.dockCatalogue
import com.example.myweibo.ui.DockGroupRequest
import com.example.myweibo.ui.selectedDockTimelineShortcut
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DockTimelineSelectionTest {
    private val group = TimelineGroup(id = "test-group", name = "同事")

    @Test fun builtInTimelineSelectsItsOwnShortcut() {
        val entries = dockCatalogue("123", listOf(group))
        for (kind in TimelineKind.entries) {
            assertEquals(DockEntry.forTimeline(kind).id, selectedDockTimelineShortcut(entries, kind, null))
        }
    }

    @Test fun customGroupDoesNotHighlightTheBuiltInSpecialFollowingShortcut() {
        val entries = dockCatalogue("123", listOf(group))
        assertEquals(
            DockEntry.forGroup("123", group).id,
            selectedDockTimelineShortcut(entries, TimelineKind.SpecialFollowing, group.id),
        )
        assertNull(selectedDockTimelineShortcut(dockCatalogue(null, emptyList()), TimelineKind.SpecialFollowing, group.id))
    }

    @Test fun inFlightGroupsCannotPublishAfterRevocationOrSameAccountReverification() = runBlocking {
        val original = DockGroupRequest("123", 0)
        var current: DockGroupRequest? = original
        val duringSwitch = CompletableDeferred<List<TimelineGroup>>()
        val afterSwitchBack = CompletableDeferred<List<TimelineGroup>>()
        val oldFetch = async(start = CoroutineStart.UNDISPATCHED) {
            original.loadIfCurrent({ current }) { duringSwitch.await() }
        }
        val earlierFetch = async(start = CoroutineStart.UNDISPATCHED) {
            original.loadIfCurrent({ current }) { afterSwitchBack.await() }
        }

        current = null // Revoked before a cookie change, while the active ID can still be 123.
        val otherAccountGroup = TimelineGroup("other-group", "新账号分组")
        duringSwitch.complete(listOf(otherAccountGroup))
        assertNull(oldFetch.await())
        assertNull(original.loadIfCurrent({ current }) { error("revoked requests must not start") })

        val otherAccount = DockGroupRequest("456", 1)
        current = otherAccount
        assertEquals(listOf(otherAccountGroup), otherAccount.loadIfCurrent({ current }) { listOf(otherAccountGroup) })

        current = DockGroupRequest("123", 2)
        afterSwitchBack.complete(listOf(otherAccountGroup))
        assertNull(earlierFetch.await())
    }

    @Test fun selectingSameCustomGroupAgainDispatchesASecondAttempt() {
        val request = DockGroupRequest("123", 0)
        val attempts = mutableListOf<TimelineGroup>()
        // The initial selection does not have to produce timeline content before retrying.
        repeat(2) {
            request.selectGroupIfCurrent({ request }, listOf(group), group.id) { attempts.add(it) }
        }
        request.selectGroupIfCurrent({ request.copy(sessionGeneration = 1) }, listOf(group), group.id) {
            error("a stale menu must not select a group from a different session")
        }
        assertEquals(listOf(group, group), attempts)
    }
}
