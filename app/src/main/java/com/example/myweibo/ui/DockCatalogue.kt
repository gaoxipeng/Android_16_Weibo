package com.example.myweibo.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import com.example.myweibo.data.TimelineKind
import com.example.myweibo.data.DockEntry
import com.example.myweibo.data.DockPreferences
import com.example.myweibo.data.dockCatalogue

internal val LocalDockCatalogue = compositionLocalOf { dockCatalogue(null, emptyList()) }

internal fun selectedDockTimelineShortcut(
    catalogue: List<DockEntry>,
    kind: TimelineKind,
    groupId: String?,
): String? = catalogue.firstOrNull { entry ->
    if (groupId != null) entry.group?.id == groupId else entry.group == null && entry.timelineKind == kind
}?.id

// Only react to the action that removes the active shortcut. Selecting an
// unpinned timeline through Home's menu or reordering shortcuts isn't a removal.
@Composable
internal fun DockVisibilityEffect(
    preferences: DockPreferences,
    catalogue: List<DockEntry>,
    selectedEntryId: String?,
    onFallback: (DockEntry) -> Unit,
) {
    val currentPreferences = rememberUpdatedState(preferences)
    val currentCatalogue = rememberUpdatedState(catalogue)
    val currentSelection = rememberUpdatedState(selectedEntryId)
    val currentFallback = rememberUpdatedState(onFallback)
    LaunchedEffect(Unit) {
        var previous = currentPreferences.value.visibleEntryIds
        snapshotFlow { currentPreferences.value.visibleEntryIds }.collect { next ->
            val active = currentSelection.value
            val wasRemoved = active != null && active in previous && active !in next
            previous = next
            if (wasRemoved) {
                currentPreferences.value.orderedVisibleEntries(currentCatalogue.value)
                    .firstOrNull { it.id == DockEntry.Home.id || it.timelineKind != null || it.group != null }
                    ?.let { currentFallback.value(it) }
            }
        }
    }
}
