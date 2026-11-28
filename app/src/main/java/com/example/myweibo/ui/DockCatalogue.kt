package com.example.myweibo.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import com.example.myweibo.data.TimelineGroup
import com.example.myweibo.data.TimelineKind
import com.example.myweibo.data.DockEntry
import com.example.myweibo.data.DockPreferences
import com.example.myweibo.data.dockCatalogue
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive

internal val LocalDockCatalogue = compositionLocalOf { dockCatalogue(null, emptyList()) }

/** Binds a group fetch to the verified session, not just the selected account ID. */
internal data class DockGroupRequest(val ownerId: String, val sessionGeneration: Int) {
    suspend fun loadIfCurrent(
        currentRequest: () -> DockGroupRequest?,
        load: suspend () -> List<TimelineGroup>,
    ): List<TimelineGroup>? {
        if (this != currentRequest()) return null
        val groups = load()
        currentCoroutineContext().ensureActive()
        return groups.takeIf { this == currentRequest() }
    }

    fun selectGroupIfCurrent(
        currentRequest: () -> DockGroupRequest?,
        groups: List<TimelineGroup>,
        groupId: String,
        switchGroup: (TimelineGroup) -> Unit,
    ) {
        if (this != currentRequest()) return
        // Repeated menu selections are requests to reload, not merely to show Home.
        groups.firstOrNull { it.id == groupId }?.let(switchGroup)
    }
}

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
