package com.example.myweibo.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myweibo.R
import com.example.myweibo.data.TimelineKind
import com.example.myweibo.data.DOCK_MAX_CENTER_SLOTS
import com.example.myweibo.data.DockEntry
import com.example.myweibo.data.DockPreferences
import com.example.myweibo.data.dockCatalogue

/** A non-scrolling settings section; the parent owns scrolling and app-wide state. */
@Composable
fun DockSettings(
    preferences: DockPreferences,
    onPreferencesChange: (DockPreferences) -> Unit,
    modifier: Modifier = Modifier,
    catalogue: List<DockEntry> = dockCatalogue(null, emptyList()),
    showTitle: Boolean = true,
    preview: @Composable (DockPreferences, Modifier) -> Unit = { config, previewModifier ->
        DockPreview(config.orderedVisibleEntries(catalogue), config.showLabels, previewModifier)
    },
) {
    val config = remember(preferences) { preferences.normalized() }
    val renderedItems = remember(config, catalogue) { config.orderedVisibleEntries(catalogue).map(DockEntry::id) }
    val items = remember(renderedItems, config) { renderedItems.filter { it in config.visibleEntryIds } }
    val sections = remember(config, catalogue) {
        config.orderedEntries(catalogue).groupBy(DockEntry::settingsSection)
    }
    val rowOrder = remember(sections) { DockSection.entries.flatMap { sections[it].orEmpty() }.map(DockEntry::id) }
    val defaults = remember(config, catalogue) { config.restoringDefaults(catalogue) }
    val currentUpdate by rememberUpdatedState(onPreferencesChange)
    val currentConfig by rememberUpdatedState(config)
    var drag by remember(rowOrder, items) { mutableStateOf<DockDrag?>(null) }
    val rowHeights = remember { mutableMapOf<String, Int>() }
    val move: (String, Int) -> Unit = { id, index ->
        currentUpdate(currentConfig.moveEntry(id, index, catalogue))
    }

    Column(
        modifier.fillMaxWidth().testTag("dock_settings"),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (showTitle) {
            Text(
                text = stringResource(R.string.dock_title),
                modifier = Modifier.semantics { heading() },
                style = MaterialTheme.typography.titleLarge,
            )
        }
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surfaceContainer,
        ) {
            Column(Modifier.padding(vertical = 8.dp)) {
                DockHeading(R.string.dock_preview)
                preview(config, Modifier.padding(horizontal = 12.dp, vertical = 8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                        .testTag("dock_labels")
                        .toggleable(config.showLabels, role = Role.Switch) {
                            currentUpdate(currentConfig.copy(showLabels = it))
                        }
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(stringResource(R.string.dock_show_labels), Modifier.weight(1f))
                    Switch(checked = config.showLabels, onCheckedChange = null)
                }
                HorizontalDivider(Modifier.padding(vertical = 4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f)) {
                        DockHeading(R.string.dock_items)
                        Text(
                            text = stringResource(
                                R.string.dock_visible_count, renderedItems.size, DOCK_MAX_CENTER_SLOTS,
                            ),
                            modifier = Modifier.padding(horizontal = 12.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    TextButton(
                        onClick = { currentUpdate(defaults) },
                        enabled = config != defaults,
                        modifier = Modifier.testTag("dock_restore_defaults"),
                    ) {
                        Text(stringResource(R.string.dock_restore_defaults))
                    }
                }
                Text(
                    text = stringResource(R.string.dock_items_hint),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (renderedItems.size == DOCK_MAX_CENTER_SLOTS) {
                    Text(
                        text = stringResource(R.string.dock_slots_full),
                        modifier = Modifier.padding(horizontal = 12.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                DockSection.entries.forEach { section ->
                    val rows = sections[section].orEmpty()
                    if (rows.isEmpty()) return@forEach
                    DockHeading(section.labelRes)
                    val sectionRows = rows.map(DockEntry::id)
                    val visibleSectionRows = sectionRows.filter { it in items }
                    rows.forEach { entry ->
                        val id = entry.id
                        key(id) {
                            val index = items.indexOf(id)
                            val visible = index >= 0
                            val shown = id in renderedItems
                            val label = entry.displayTitle()
                            val currentMove by rememberUpdatedState(move)
                            val handle = if (id !in visibleSectionRows || visibleSectionRows.size < 2) Modifier
                            else Modifier.pointerInput(sectionRows, items, id) {
                                val from = visibleSectionRows.indexOf(id)
                                var distance = 0f
                                var target = from
                                var centers = emptyList<Float>()
                                detectDragGesturesAfterLongPress(
                                    onDragStart = {
                                        distance = 0f
                                        target = from
                                        val allCenters = dockRowCenters(sectionRows.map { rowHeights[it] ?: 0 })
                                        centers = visibleSectionRows.map { allCenters[sectionRows.indexOf(it)] }
                                        drag = DockDrag(id, id, false)
                                    },
                                    onDragEnd = {
                                        drag = null
                                        if (target != from) currentMove(id, items.indexOf(visibleSectionRows[target]))
                                    },
                                    onDragCancel = { drag = null },
                                    onDrag = { change, amount ->
                                        change.consume()
                                        distance += amount.y
                                        target = dockDragTarget(centers, from, distance)
                                        drag = DockDrag(id, visibleSectionRows[target], target < from)
                                    },
                                )
                            }
                            val dragState = drag
                            Box(Modifier.onSizeChanged { rowHeights[id] = it.height }) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = 48.dp)
                                        .then(
                                            if (dragState?.from == id) {
                                                Modifier.background(MaterialTheme.colorScheme.secondaryContainer)
                                            } else Modifier,
                                        )
                                        .padding(horizontal = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Box(Modifier.size(48.dp).then(handle), contentAlignment = Alignment.Center) {
                                        if (visibleSectionRows.size > 1 && visible) {
                                            Icon(Icons.Default.DragHandle, contentDescription = null)
                                        }
                                    }
                                    DockItemName(entry, label, Modifier.weight(1f))
                                    if (shown) {
                                        val position = renderedItems.indexOf(id) + 1
                                        Text(
                                            text = stringResource(R.string.dock_item_position, position),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                    IconButton(
                                        onClick = { move(id, index - 1) },
                                        enabled = index > 0,
                                        modifier = Modifier.size(48.dp).testTag("dock_up_$id"),
                                    ) {
                                        Icon(Icons.Default.KeyboardArrowUp, stringResource(R.string.dock_move_up, label))
                                    }
                                    IconButton(
                                        onClick = { move(id, index + 1) },
                                        enabled = visible && index < items.lastIndex,
                                        modifier = Modifier.size(48.dp).testTag("dock_down_$id"),
                                    ) {
                                        Icon(Icons.Default.KeyboardArrowDown, stringResource(R.string.dock_move_down, label))
                                    }
                                    val toggleLabel = stringResource(R.string.dock_show_item, label)
                                    val canToggle = if (shown) config.canHideEntry(id, catalogue)
                                    else config.canShowEntry(id, catalogue)
                                    val disabledReason = if (shown) stringResource(R.string.dock_last_timeline)
                                    else stringResource(R.string.dock_slots_full)
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .testTag("dock_visibility_$id")
                                            .toggleable(shown, enabled = canToggle, role = Role.Checkbox) { show ->
                                                currentUpdate(currentConfig.withEntryVisibility(id, show, catalogue))
                                            }
                                            .semantics {
                                                contentDescription = toggleLabel
                                                if (!canToggle) stateDescription = disabledReason
                                            },
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Checkbox(checked = shown, onCheckedChange = null, enabled = canToggle)
                                    }
                                }
                                if (dragState != null && dragState.to == id && dragState.from != id) {
                                    HorizontalDivider(
                                        modifier = Modifier.align(
                                            if (dragState.before) Alignment.TopCenter else Alignment.BottomCenter,
                                        ),
                                        thickness = 2.dp,
                                        color = MaterialTheme.colorScheme.primary,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DockHeading(labelRes: Int) {
    Text(
        text = stringResource(labelRes),
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp).semantics { heading() },
        style = MaterialTheme.typography.titleSmall,
    )
}

@Composable
private fun DockItemName(entry: DockEntry, label: String, modifier: Modifier) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        DockSettingsIcon(entry, Modifier.size(20.dp))
        Text(label, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun DockPreview(entries: List<DockEntry>, showLabels: Boolean, modifier: Modifier) {
    val shape = RoundedCornerShape(20.dp)
    val color = MaterialTheme.colorScheme.surfaceContainerHighest
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Surface(modifier = Modifier.width(56.dp), shape = shape, color = color) {
            PreviewItem(DockEntry.Search, showLabels, Modifier.testTag("dock_preview_search"))
        }
        Surface(
            modifier = Modifier.weight(1f).testTag("dock_preview_items"),
            shape = shape,
            color = color,
        ) {
            BoxWithConstraints {
                val stripWidth = maxOf(maxWidth, 48.dp * entries.size + 8.dp)
                Row(Modifier.horizontalScroll(rememberScrollState()).width(stripWidth).padding(horizontal = 4.dp)) {
                    entries.forEach { entry ->
                        key(entry.id) {
                            PreviewItem(entry, showLabels, Modifier.weight(1f).testTag("dock_preview_${entry.id}"))
                        }
                    }
                }
            }
        }
        Surface(modifier = Modifier.width(56.dp), shape = shape, color = color) {
            PreviewItem(DockEntry.Profile, showLabels, Modifier.testTag("dock_preview_profile"))
        }
    }
}

@Composable
private fun PreviewItem(entry: DockEntry, showLabel: Boolean, modifier: Modifier) {
    val label = entry.displayTitle()
    Column(
        modifier = modifier.heightIn(min = if (showLabel) 56.dp else 44.dp).padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterVertically),
    ) {
        DockSettingsIcon(entry, Modifier.size(20.dp), description = if (showLabel) null else label)
        if (showLabel) {
            Text(
                text = label,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun DockEntry.displayTitle(): String =
    if (id == DockEntry.Home.id) TimelineKind.Following.label else titleRes?.let { stringResource(it) } ?: title

@Composable
private fun DockSettingsIcon(entry: DockEntry, modifier: Modifier, description: String? = null) {
    val badge = entry.groupBadge
    if (badge == null) {
        Icon(painterResource(entry.iconRes), contentDescription = description, modifier = modifier)
    } else {
        DockGroupBadge(badge, modifier.then(if (description == null) Modifier else Modifier.semantics {
            contentDescription = description
        }))
    }
}

@Composable
internal fun DockGroupBadge(badge: String, modifier: Modifier = Modifier, color: Color = LocalContentColor.current) {
    Box(
        modifier = modifier.border(1.dp, color.copy(alpha = 0.62f), CircleShape).padding(horizontal = 1.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = badge,
            modifier = Modifier.clearAndSetSemantics {},
            color = color,
            fontSize = 9.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            softWrap = false,
        )
    }
}

private enum class DockSection(val labelRes: Int) {
    Common(R.string.dock_section_common),
    Timeline(R.string.dock_section_timelines),
    Groups(R.string.dock_section_groups),
}

private val DockEntry.settingsSection: DockSection
    get() = when {
        group != null -> DockSection.Groups
        isTimeline -> DockSection.Timeline
        else -> DockSection.Common
    }

private data class DockDrag(val from: String, val to: String, val before: Boolean)

private fun dockRowCenters(heights: List<Int>): List<Float> {
    var top = 0f
    return heights.map { height ->
        val center = top + height / 2f
        top += height
        center
    }
}

/** Cross a neighbouring row's center before marking an insertion on its far side. */
private fun dockDragTarget(centers: List<Float>, from: Int, distance: Float): Int {
    val position = centers[from] + distance
    var target = from
    if (distance < 0f) {
        while (target > 0 && position <= centers[target - 1]) target--
    } else if (distance > 0f) {
        while (target < centers.lastIndex && position >= centers[target + 1]) target++
    }
    return target
}
