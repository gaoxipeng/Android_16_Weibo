package com.example.myweibo.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.myweibo.data.TimelineKind
import com.example.myweibo.data.DockEntry
import com.example.myweibo.data.DockPreferences
import com.example.myweibo.ui.liquidglass.LiquidBottomTab
import com.example.myweibo.ui.liquidglass.LiquidBottomTabs
import com.example.myweibo.ui.liquidglass.LiquidDockSearchButton
import com.example.myweibo.ui.liquidglass.DockTimelinePickerReveal
import com.example.myweibo.ui.liquidglass.dockSelectionTween
import com.example.myweibo.ui.theme.isAppLightTheme
import com.kyant.backdrop.Backdrop
import kotlin.math.roundToInt

private val DockHeight = 56.dp
private val DockGap = 8.dp
private val DockTabMinWidth = 48.dp
private val DockCapsulePadding = 4.dp

/** Optional account/timeline data; main tabs still use the bar’s onTabChange callback. */
internal data class DockRuntime(
    val catalogue: List<DockEntry>,
    val selectedEntryId: String? = null,
    val onEntrySelected: (DockEntry) -> Unit,
    val profileAvatar: DockAvatarRequest?,
    /** Jump to the top of the selected timeline and refresh it once. */
    val onEntryDoubleClick: ((DockEntry) -> Unit)? = null,
    /** Action for tapping an already-selected timeline entry. */
    val onSelectedTimelineClick: (() -> Unit)? = null,
)

@Composable
internal fun WeiboLiquidBottomBar(
    selectedTab: MainTab,
    darkSurfaceOverride: Boolean = false,
    onTabChange: (MainTab) -> Unit,
    backdrop: Backdrop,
    frostAmount: Float = 0f,
    glassAmount: Float = 1f,
    timelineMenuExpanded: Boolean,
    onTimelineMenuExpandedChange: (Boolean) -> Unit,
    selectedTimelineKind: TimelineKind = TimelineKind.Following,
    selectedTimelineLabel: String? = null,
    timelineMenuContent: @Composable (dismiss: () -> Unit) -> Unit,
    timelineMenuWidth: Dp = 152.dp,
    timelineMenuHeight: Dp = 89.dp,
    timelineMenuGap: Dp = 4.dp,
    onBarBoundsChanged: ((Rect) -> Unit)? = null,
    modifier: Modifier = Modifier,
    dockPreferences: DockPreferences = DockPreferences(),
    unreadMessagesCount: Int = 0,
    applyNavigationBarInsets: Boolean = true,
    dockRuntime: DockRuntime? = null,
) {
    val catalogue = dockRuntime?.catalogue.orEmpty()
    val entries = remember(dockPreferences, catalogue) { dockPreferences.orderedVisibleEntries(catalogue) }
    val entryIds = remember(entries) { entries.map(DockEntry::id) }
    val homeIndex = entryIds.indexOf(DockEntry.Home.id)
    val firstTimelineIndex = entries.indexOfFirst(DockEntry::isTimelineSlot)
    val visibleTimelineIndex = dockRuntime?.selectedEntryId?.let(entryIds::indexOf)
        ?.takeIf { it >= 0 && entries[it].isTimelineShortcut() }
    val selectedIndex = when (selectedTab) {
        // Home represents unpinned routes only when it is actually visible.
        MainTab.Feed -> visibleTimelineIndex ?: homeIndex
        MainTab.Messages -> entryIds.indexOf(DockEntry.Messages.id)
        MainTab.Compose -> entryIds.indexOf(DockEntry.Compose.id)
        MainTab.Search, MainTab.Mine -> -1
    }
    val homeLabel = if (visibleTimelineIndex == null) {
        selectedTimelineLabel ?: selectedTimelineKind.label
    } else DockEntry.Home.title

    var menuAnchorEntryId by remember { mutableStateOf<String?>(null) }
    val menuAnchorIndex = menuAnchorEntryId?.let(entryIds::indexOf)?.takeIf { it >= 0 }
        ?: homeIndex.takeIf { it >= 0 }
        ?: visibleTimelineIndex?.takeIf { selectedTab == MainTab.Feed }
        ?: firstTimelineIndex
    LaunchedEffect(timelineMenuExpanded) {
        if (!timelineMenuExpanded) menuAnchorEntryId = null
    }
    LaunchedEffect(menuAnchorIndex, timelineMenuExpanded) {
        if (menuAnchorIndex < 0 && timelineMenuExpanded) onTimelineMenuExpandedChange(false)
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 16.dp, top = 4.dp, end = 16.dp, bottom = 8.dp)
            .then(if (applyNavigationBarInsets) Modifier.navigationBarsPadding() else Modifier),
        contentAlignment = Alignment.BottomStart,
    ) {
        val density = LocalDensity.current
        val dockWidth = maxWidth
        val endcapSize = DockHeight.coerceAtMost(dockWidth / 2f)
        val middleWidth = (dockWidth - endcapSize * 2).coerceAtLeast(0.dp)
        val gap = if (entries.isNotEmpty() && middleWidth > DockGap * 2) DockGap else 0.dp
        val availableCapsuleWidth = (middleWidth - gap * 2).coerceAtLeast(0.dp)
        val minimumCapsuleWidth = with(density) {
            (DockTabMinWidth.roundToPx() * entries.size + DockCapsulePadding.roundToPx() * 2).toDp()
        }
        val capsuleWidth = maxOf(minimumCapsuleWidth, availableCapsuleWidth)
        val scrollable = capsuleWidth > availableCapsuleWidth
        val capsuleScroll = rememberScrollState()
        val insetPx = with(density) { DockCapsulePadding.roundToPx() }
        val capsuleWidthPx = with(density) { capsuleWidth.roundToPx() }
        val viewportWidthPx = with(density) { availableCapsuleWidth.roundToPx() }
        val entryWidthPx = (capsuleWidthPx - 2f * insetPx) / entries.size.coerceAtLeast(1)

        // A change from the Home picker can select a shortcut that is offscreen. Reveal it
        // with the smallest movement, but don't fight swipes while the selection is unchanged.
        LaunchedEffect(entryIds, selectedIndex, viewportWidthPx, capsuleWidthPx) {
            if (!scrollable || selectedIndex < 0 || viewportWidthPx <= 0) return@LaunchedEffect
            val start = (insetPx + entryWidthPx * selectedIndex).roundToInt()
            val end = (insetPx + entryWidthPx * (selectedIndex + 1)).roundToInt()
            val current = capsuleScroll.value.coerceAtMost((capsuleWidthPx - viewportWidthPx).coerceAtLeast(0))
            val target = when {
                start < current -> start
                end > current + viewportWidthPx -> end - viewportWidthPx
                else -> current
            }
            if (target != capsuleScroll.value) capsuleScroll.scrollTo(target)
        }

        val selectedEntryId = entryIds.getOrNull(selectedIndex)
        val selectEntry: (DockEntry) -> Unit = { entry ->
            if (timelineMenuExpanded) onTimelineMenuExpandedChange(false)
            if (entry.isTimelineSlot() && entry.id == selectedEntryId) {
                dockRuntime?.onSelectedTimelineClick?.invoke()
            } else if (dockRuntime != null && entry.isTimelineShortcut()) {
                dockRuntime.onEntrySelected(entry)
            } else {
                onTabChange(entry.toMainTab())
            }
        }
        Dock(
            entries = entries,
            entryIds = entryIds,
            selectedTab = selectedTab,
            selectedIndex = selectedIndex,
            homeLabel = homeLabel,
            onEntrySelected = selectEntry,
            onEndcapSelected = { tab ->
                onTimelineMenuExpandedChange(false)
                onTabChange(tab)
            },
            hasSelectedTimelineClick = dockRuntime?.onSelectedTimelineClick != null,
            onTimelineDoubleClick = dockRuntime?.onEntryDoubleClick?.let { goToTopAndRefresh ->
                { entry ->
                    if (entry.id == selectedEntryId) {
                        if (timelineMenuExpanded) onTimelineMenuExpandedChange(false)
                        goToTopAndRefresh(entry)
                    } else {
                        // A double tap on an inactive timeline only switches there once.
                        selectEntry(entry)
                    }
                }
            },
            onTimelineLongPress = { entry ->
                menuAnchorEntryId = entry.id
                onTimelineMenuExpandedChange(true)
            },
            showLabels = dockPreferences.showLabels,
            unreadMessagesCount = unreadMessagesCount,
            profileAvatar = dockRuntime?.profileAvatar,
            endcapSize = endcapSize,
            availableCapsuleWidth = availableCapsuleWidth,
            capsuleWidth = capsuleWidth,
            capsuleScroll = capsuleScroll,
            backdrop = backdrop,
            frostAmount = frostAmount,
            glassAmount = glassAmount,
            darkSurfaceOverride = darkSurfaceOverride,
            modifier = Modifier.fillMaxWidth().onGloballyPositioned { onBarBoundsChanged?.invoke(it.boundsInRoot()) },
        )

        val menuWidth = timelineMenuWidth.coerceAtMost(dockWidth)
        val menuAnchorCenterInDock = with(density) {
            val scrollOffset = if (scrollable) capsuleScroll.value else 0
            val anchorCenterInViewport = insetPx + entryWidthPx * (menuAnchorIndex + 0.5f) - scrollOffset
            endcapSize.roundToPx() + gap.roundToPx() + anchorCenterInViewport.coerceIn(0f, viewportWidthPx.toFloat())
        }
        val timelineMenuOffsetX = with(density) {
            (menuAnchorCenterInDock - menuWidth.toPx() / 2f)
                .roundToInt()
                .coerceIn(0, (dockWidth - menuWidth).roundToPx().coerceAtLeast(0))
        }
        if (timelineMenuExpanded && menuAnchorIndex >= 0 && availableCapsuleWidth > 0.dp) {
            DockTimelinePickerReveal(
                // Don't lower the entering menu into the dock's input area.
                lift = minOf(timelineMenuGap, timelineMenuHeight, 8.dp).coerceAtLeast(0.dp),
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .offset { IntOffset(timelineMenuOffsetX, -(DockHeight + timelineMenuGap).roundToPx()) }
                    .zIndex(50f),
            ) {
                timelineMenuContent { onTimelineMenuExpandedChange(false) }
            }
        }
    }
}

@Composable
private fun Dock(
    entries: List<DockEntry>,
    entryIds: List<String>,
    selectedTab: MainTab,
    selectedIndex: Int,
    homeLabel: String,
    onEntrySelected: (DockEntry) -> Unit,
    onEndcapSelected: (MainTab) -> Unit,
    hasSelectedTimelineClick: Boolean,
    onTimelineDoubleClick: ((DockEntry) -> Unit)?,
    onTimelineLongPress: (DockEntry) -> Unit,
    showLabels: Boolean,
    unreadMessagesCount: Int,
    profileAvatar: DockAvatarRequest?,
    endcapSize: Dp,
    availableCapsuleWidth: Dp,
    capsuleWidth: Dp,
    capsuleScroll: ScrollState,
    backdrop: Backdrop,
    frostAmount: Float,
    glassAmount: Float,
    darkSurfaceOverride: Boolean,
    modifier: Modifier = Modifier,
) {
    val accentColor = MaterialTheme.colorScheme.primary
    val contentColor = if (isAppLightTheme() && !darkSurfaceOverride) Color.Black else Color(0xFFD0D0D0)
    val scrollable = capsuleWidth > availableCapsuleWidth

    Row(
        modifier = modifier.height(DockHeight).selectableGroup(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        LiquidDockSearchButton(
            selected = selectedTab == MainTab.Search,
            onClick = { onEndcapSelected(MainTab.Search) },
            backdrop = backdrop,
            frostAmount = frostAmount,
            glassAmount = glassAmount,
            forceDarkSurface = darkSurfaceOverride,
            modifier = Modifier.size(endcapSize).semantics { contentDescription = MainTab.Search.label },
        ) {
            DockEntryGlyph(DockEntry.Search, contentColor)
        }

        if (entries.isNotEmpty() && availableCapsuleWidth > 0.dp) {
            Box(
                Modifier.width(availableCapsuleWidth)
                    .testTag("dock_capsule")
                    .then(if (scrollable) Modifier.horizontalScroll(capsuleScroll) else Modifier),
            ) {
                // Cancel positional gestures if the user changes the item order while touching.
                key(entryIds) {
                    LiquidBottomTabs(
                        selectedTabIndex = { selectedIndex },
                        onTabSelected = { index -> onEntrySelected(entries[index]) },
                        backdrop = backdrop,
                        frostAmount = frostAmount,
                        glassAmount = glassAmount,
                        forceDarkSurface = darkSurfaceOverride,
                        tabsCount = entries.size,
                        isTabLongPressEnabled = { entries[it].isTimelineSlot() },
                        onTabLongPress = { onTimelineLongPress(entries[it]) },
                        onTabDoubleClick = onTimelineDoubleClick?.let { callback -> { index -> callback(entries[index]) } },
                        isTabDoubleClickEnabled = { entries[it].isTimelineSlot() },
                        height = DockHeight,
                        dragSelectionEnabled = !scrollable,
                        modifier = Modifier.width(capsuleWidth),
                    ) {
                        entries.forEachIndexed { index, entry ->
                            val isHome = entry.id == DockEntry.Home.id
                            val unread = if (entry.id == DockEntry.Messages.id) unreadMessagesCount else 0
                            LiquidBottomTab(
                                selected = index == selectedIndex,
                                dockIndex = index,
                                onClick = { onEntrySelected(entry) },
                                onLongClick = if (entry.isTimelineSlot()) ({ onTimelineLongPress(entry) }) else null,
                                onDoubleClick = onTimelineDoubleClick?.takeIf { scrollable && entry.isTimelineSlot() }
                                    ?.let { callback -> { callback(entry) } },
                                modifier = Modifier.semantics {
                                    contentDescription = entry.title
                                    if (index == selectedIndex && entry.isTimelineSlot()) {
                                        if (hasSelectedTimelineClick) {
                                            val clickLabel = if (onTimelineDoubleClick != null) "回到顶部" else "回到顶部并刷新"
                                            onClick(label = clickLabel) { onEntrySelected(entry); true }
                                        }
                                        onTimelineDoubleClick?.let { goToTopAndRefresh ->
                                            customActions = listOf(CustomAccessibilityAction("回到顶部并刷新") { goToTopAndRefresh(entry); true })
                                        }
                                    }
                                    if (isHome && homeLabel != entry.title) stateDescription = homeLabel
                                    if (unread > 0) stateDescription = "$unread 条未读消息"
                                },
                            ) {
                                val color by animateColorAsState(
                                    targetValue = if (index == selectedIndex) accentColor else contentColor,
                                    animationSpec = dockSelectionTween(),
                                    label = "dock-tab-color",
                                )
                                Box(
                                    modifier = Modifier.size(if (showLabels) 32.dp else 36.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    if (unread > 0) {
                                        BadgedBox(badge = {
                                            Badge(modifier = Modifier.clearAndSetSemantics { }) {
                                                Text(if (unread > 99) "99+" else unread.toString())
                                            }
                                        }) {
                                            DockEntryGlyph(entry, color)
                                        }
                                    } else {
                                        DockEntryGlyph(entry, color)
                                    }
                                }
                                if (showLabels) {
                                    DockLabel(
                                        label = if (isHome) homeLabel else entry.title,
                                        fallback = if (isHome) entry.title else null,
                                        color = color,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        DockAvatar(
            request = profileAvatar,
            contentColor = contentColor,
            selected = selectedTab == MainTab.Mine,
            onClick = { onEndcapSelected(MainTab.Mine) },
            backdrop = backdrop,
            frostAmount = frostAmount,
            glassAmount = glassAmount,
            modifier = Modifier.size(endcapSize),
        )
    }
}

@Composable
private fun DockLabel(label: String, fallback: String?, color: Color) {
    val style = LocalTextStyle.current.copy(
        fontSize = 10.sp,
        lineHeight = 12.sp,
        fontWeight = FontWeight.Normal,
    )
    BoxWithConstraints(Modifier.fillMaxWidth().padding(horizontal = 2.dp)) {
        val measurer = rememberTextMeasurer()
        val maxWidth = constraints.maxWidth
        val displayLabel = remember(label, fallback, measurer, style, maxWidth) {
            if (fallback != null && label != fallback && measurer.measure(
                    text = label,
                    style = style,
                    maxLines = 1,
                    softWrap = false,
                    constraints = Constraints(maxWidth = maxWidth),
                ).hasVisualOverflow
            ) fallback else label
        }
        Text(
            text = displayLabel,
            modifier = Modifier.align(Alignment.Center).clearAndSetSemantics { },
            color = color,
            style = style,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

private fun DockEntry.toMainTab(): MainTab = when (id) {
    DockEntry.Messages.id -> MainTab.Messages
    DockEntry.Compose.id -> MainTab.Compose
    else -> MainTab.Feed
}

private fun DockEntry.isTimelineShortcut(): Boolean = timelineKind != null || group != null

private fun DockEntry.isTimelineSlot(): Boolean = id == DockEntry.Home.id || isTimelineShortcut()

@Composable
internal fun DockEntryGlyph(
    entry: DockEntry,
    color: Color,
    size: Dp = 22.dp,
    contentDescription: String? = null,
) {
    val badge = entry.groupBadge
    if (badge.isNullOrBlank()) {
        Icon(
            painter = painterResource(entry.iconRes),
            contentDescription = contentDescription,
            modifier = Modifier.size(size),
            tint = color,
        )
        return
    }

    val glyphDescription = contentDescription
    Box(
        modifier = Modifier.size(size)
            .border(1.dp, color, RoundedCornerShape(6.dp))
            .clearAndSetSemantics {
                if (glyphDescription != null) this.contentDescription = glyphDescription
            }
            .padding(horizontal = 2.dp, vertical = 1.dp),
        contentAlignment = Alignment.Center,
    ) {
        BasicText(
            text = badge,
            style = LocalTextStyle.current.copy(
                color = color,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                lineHeight = TextUnit.Unspecified,
            ),
            maxLines = 1,
            softWrap = false,
            autoSize = TextAutoSize.StepBased(minFontSize = 6.sp, maxFontSize = 13.sp, stepSize = 0.5.sp),
        )
    }
}
