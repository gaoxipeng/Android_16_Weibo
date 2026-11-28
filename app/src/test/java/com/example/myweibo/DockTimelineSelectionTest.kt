package com.example.myweibo

import com.example.myweibo.data.TimelineGroup
import com.example.myweibo.data.TimelineKind
import com.example.myweibo.data.DockEntry
import com.example.myweibo.data.dockCatalogue
import com.example.myweibo.ui.selectedDockTimelineShortcut
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
}
