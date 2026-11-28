package com.example.myweibo.ui.liquidglass

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.AbsoluteAlignment
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastCoerceIn
import androidx.compose.ui.util.fastRoundToInt
import com.example.myweibo.data.bottomBarBlurSamplingPadding
import com.example.myweibo.data.bottomBarGlassBlurDp
import com.example.myweibo.data.bottomBarGlassRefractionAmountDp
import com.example.myweibo.data.bottomBarGlassRefractionHeightDp
import com.example.myweibo.data.bottomBarGlassSurfaceAlpha
import com.example.myweibo.data.normalizeBottomBarGlassAmount
import com.example.myweibo.ui.theme.isAppLightTheme
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.highlight.Highlight
import com.kyant.backdrop.shadow.Shadow
import kotlinx.coroutines.withTimeoutOrNull

fun Modifier.drawBottomBarGlassBackdrop(
    backdrop: Backdrop,
    frostAmount: Float,
    glassAmount: Float,
    isLightTheme: Boolean,
): Modifier {
    val surfaceColor = (if (isLightTheme) Color.White else Color(0xFF121212))
        .copy(alpha = bottomBarGlassSurfaceAlpha(normalizeBottomBarGlassAmount(frostAmount)))
    return drawBottomBarGlassBackdrop(backdrop, frostAmount, glassAmount, surfaceColor)
}

/** Same lens, blur, capsule, highlight and shadow; smaller controls can supply a denser surface. */
internal fun Modifier.drawBottomBarGlassBackdrop(
    backdrop: Backdrop,
    frostAmount: Float,
    glassAmount: Float,
    surfaceColor: Color,
): Modifier {
    val frost = normalizeBottomBarGlassAmount(frostAmount)
    val glass = normalizeBottomBarGlassAmount(glassAmount)
    return drawBackdrop(
        backdrop = backdrop,
        shape = { RoundedCornerShape(percent = 50) },
        effects = {
            val radius = bottomBarGlassBlurDp(frost).dp.toPx()
            val lensHeight = bottomBarGlassRefractionHeightDp(glass).dp.toPx()
            if (radius > 0f) {
                blur(radius, TileMode.Clamp)
                padding = bottomBarBlurSamplingPadding(radius, lensHeight)
            }
            if (glass > 0f) lens(
                lensHeight,
                bottomBarGlassRefractionAmountDp(glass).dp.toPx(),
            )
        },
        highlight = { Highlight.Default },
        shadow = { Shadow.Default },
        onDrawSurface = { drawRect(surfaceColor) },
    )
}

private data class PendingDockTabTap(val index: Int, val upTimeMillis: Long)

@Composable
fun LiquidBottomTabs(
    selectedTabIndex: () -> Int,
    onTabSelected: (index: Int) -> Unit,
    backdrop: Backdrop,
    tabsCount: Int,
    frostAmount: Float = 0f,
    glassAmount: Float = 1f,
    forceDarkSurface: Boolean = false,
    modifier: Modifier = Modifier,
    gestureController: LiquidBottomTabsGestureController =
        rememberLiquidBottomTabsGestureController(),
    feedTabIndex: Int = 0,
    onTabLongPress: (index: Int) -> Unit = {},
    height: Dp = 64.dp,
    dragSelectionEnabled: Boolean = true,
    isTabLongPressEnabled: ((index: Int) -> Boolean)? = null,
    onTabDoubleClick: ((index: Int) -> Unit)? = null,
    isTabDoubleClickEnabled: (index: Int) -> Boolean = { true },
    content: @Composable RowScope.() -> Unit,
) {
    require(tabsCount > 0)
    val isLightTheme = isAppLightTheme()
    val innerHeight = height - 8.dp
    val indicatorColor = (if (isLightTheme && !forceDarkSurface) Color.Black else Color.White)
        .copy(alpha = 0.14f)

    BoxWithConstraints(
        modifier = modifier,
        contentAlignment = Alignment.CenterStart,
    ) {
        val density = LocalDensity.current
        val horizontalPaddingPx = with(density) { 4.dp.roundToPx() }
        val tabWidth = ((constraints.maxWidth - 2f * horizontalPaddingPx) / tabsCount)
            .coerceAtLeast(1f)
        val isLtr = LocalLayoutDirection.current == LayoutDirection.Ltr
        // An external or hidden destination leaves the capsule unselected.
        val selectedIndex = selectedTabIndex().takeIf { it in 0 until tabsCount } ?: -1
        val currentTabWidth = rememberUpdatedState(tabWidth)
        val currentIsLtr = rememberUpdatedState(isLtr)
        val currentSelectedIndex = rememberUpdatedState(selectedIndex)
        val currentHorizontalPadding = rememberUpdatedState(horizontalPaddingPx)
        val currentIsTabLongPressEnabled = rememberUpdatedState(isTabLongPressEnabled ?: { index: Int -> index == feedTabIndex })
        val currentMaxWidth = rememberUpdatedState(constraints.maxWidth)
        val currentMaxHeight = rememberUpdatedState(constraints.maxHeight)
        val currentOnTabSelected = rememberUpdatedState(onTabSelected)
        val currentOnTabLongPress = rememberUpdatedState(onTabLongPress)
        val currentOnTabDoubleClick = rememberUpdatedState(onTabDoubleClick)
        val currentIsTabDoubleClickEnabled = rememberUpdatedState(isTabDoubleClickEnabled)
        fun valueAt(position: Offset): Float {
            val visualValue = ((position.x - currentHorizontalPadding.value) / currentTabWidth.value - 0.5f)
                .fastCoerceIn(0f, (tabsCount - 1).toFloat())
            return if (currentIsLtr.value) visualValue else (tabsCount - 1).toFloat() - visualValue
        }

        fun indexAt(position: Offset): Int =
            valueAt(position).fastRoundToInt().fastCoerceIn(0, tabsCount - 1)

        val pressedTab = remember(tabsCount) { mutableIntStateOf(-1) }
        var pressedIndex by pressedTab
        var previewPosition by remember(tabsCount) { mutableStateOf<Float?>(null) }
        var previewing by remember(tabsCount) { mutableStateOf(false) }
        val indicatorPosition = remember(tabsCount) { Animatable(selectedIndex.coerceAtLeast(0).toFloat()) }
        val indicatorAlpha by animateFloatAsState(
            targetValue = if (selectedIndex >= 0 || previewing) 1f else 0f,
            animationSpec = dockSelectionTween(),
            label = "dock-indicator-alpha",
        )
        LaunchedEffect(selectedIndex, previewing, indicatorPosition) {
            if (!previewing) {
                previewPosition?.let { indicatorPosition.snapTo(it) }
                previewPosition = null
                if (selectedIndex >= 0) {
                    if (indicatorAlpha == 0f) indicatorPosition.snapTo(selectedIndex.toFloat())
                    else indicatorPosition.animateTo(selectedIndex.toFloat(), dockSelectionTween())
                }
            }
        }
        var controlledGesturePosition by remember { mutableStateOf(Offset.Zero) }

        fun beginControlledGesture(position: Offset, showLongPressMenu: Boolean): Boolean {
            controlledGesturePosition = position
            val index = indexAt(position)
            val openedMenu = showLongPressMenu && currentIsTabLongPressEnabled.value(index)
            if (openedMenu) currentOnTabLongPress.value(index)
            previewing = true
            previewPosition = valueAt(position)
            return openedMenu
        }

        fun dragControlledGesture(position: Offset) {
            controlledGesturePosition = position
            previewPosition = valueAt(position)
        }

        fun finishControlledGesture() {
            val targetIndex = indexAt(controlledGesturePosition)
            previewing = false
            if (targetIndex != currentSelectedIndex.value) {
                currentOnTabSelected.value(targetIndex)
            }
        }

        fun selectTappedTab(index: Int) {
            // A tap also activates an already-selected tab, unlike a drag settling on it.
            currentOnTabSelected.value(index)
        }

        fun cancelControlledGesture() {
            previewing = false
        }

        DisposableEffect(gestureController, pressedTab) {
            gestureController.impl = object : LiquidBottomTabsGestureController.GestureImpl {
                fun localPosition(positionFraction: Offset): Offset = Offset(
                    x = positionFraction.x.coerceIn(0f, 1f) * currentMaxWidth.value,
                    y = positionFraction.y.coerceIn(0f, 1f) * currentMaxHeight.value,
                )

                override fun begin(positionFraction: Offset) {
                    val position = localPosition(positionFraction)
                    pressedIndex = indexAt(position)
                    beginControlledGesture(position, showLongPressMenu = true)
                }

                override fun drag(positionFraction: Offset, dragAmount: Offset) {
                    dragControlledGesture(localPosition(positionFraction))
                }

                override fun end() {
                    pressedIndex = -1
                    finishControlledGesture()
                }

                override fun cancel() {
                    pressedIndex = -1
                    cancelControlledGesture()
                }
            }
            onDispose {
                gestureController.impl = null
            }
        }

        Box(
            Modifier
                .drawBottomBarGlassBackdrop(backdrop, frostAmount, glassAmount, isLightTheme)
                .height(height)
                .fillMaxWidth(),
        )
        // Selection uses one lightweight layer on the existing glass, not a second
        // composited copy of all tabs or a refractive animation on every down event.
        Box(Modifier.fillMaxWidth().height(innerHeight).padding(horizontal = 4.dp)) {
            Box(
                Modifier
                    .align(AbsoluteAlignment.CenterLeft)
                    .fillMaxWidth(1f / tabsCount)
                    .height(innerHeight)
                    .graphicsLayer {
                        val position = previewPosition ?: indicatorPosition.value
                        val physical = if (isLtr) position else tabsCount - 1f - position
                        translationX = physical * tabWidth
                        alpha = indicatorAlpha
                    }
                    .background(indicatorColor, RoundedCornerShape(percent = 50)),
            )
        }
        CompositionLocalProvider(LocalDockPressedTab provides if (dragSelectionEnabled) pressedTab else null) {
            Row(
                Modifier.height(innerHeight).fillMaxWidth().padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                content = content,
            )
        }

        // Let the scroll container own horizontal drags when the tabs overflow.
        if (dragSelectionEnabled) {
            Box(
                Modifier
                    .fillMaxSize()
                    .pointerInput(
                        tabsCount,
                        feedTabIndex,
                        isLtr,
                        tabWidth,
                    ) {
                        var pendingTap: PendingDockTabTap? = null
                        try {
                            awaitEachGesture {
                                // Use the pointer-event timeout (same as long press) rather than a
                                // separately scheduled coroutine that can race the next gesture.
                                var previousTap = pendingTap
                                pendingTap = null
                                val nextDown = if (previousTap != null) {
                                    withTimeoutOrNull(viewConfiguration.doubleTapTimeoutMillis) {
                                        awaitFirstDown(requireUnconsumed = false)
                                    }
                                } else null
                                if (nextDown == null && previousTap != null) {
                                    selectTappedTab(previousTap.index)
                                    previousTap = null
                                }
                                val down = nextDown ?: awaitFirstDown(requireUnconsumed = false)
                                down.consume()
                                val targetIndex = indexAt(down.position)
                                pressedIndex = targetIndex
                                val canDoubleTap = down.type == PointerType.Touch &&
                                    currentOnTabDoubleClick.value != null && currentIsTabDoubleClickEnabled.value(targetIndex)
                                val isSecondTap = previousTap != null && canDoubleTap && previousTap.index == targetIndex &&
                                    down.uptimeMillis - previousTap.upTimeMillis in
                                    viewConfiguration.doubleTapMinTimeMillis..viewConfiguration.doubleTapTimeoutMillis
                                if (previousTap != null && !isSecondTap) selectTappedTab(previousTap.index)
                                var latestPosition = down.position
                                var releasedAt: Long? = null
                                var movedBeforeLongPress = false
                                val completedBeforeTimeout = withTimeoutOrNull(
                                    viewConfiguration.longPressTimeoutMillis,
                                ) {
                                    while (true) {
                                        val event = awaitPointerEvent()
                                        val change = event.changes.firstOrNull { it.id == down.id }
                                            ?: return@withTimeoutOrNull true
                                        if (change.isConsumed || event.changes.any { it.id != down.id && it.pressed }) {
                                            return@withTimeoutOrNull true
                                        }
                                        latestPosition = change.position
                                        if (!change.pressed) {
                                            releasedAt = change.uptimeMillis
                                            change.consume()
                                            return@withTimeoutOrNull true
                                        }
                                        if ((change.position - down.position).getDistance() >
                                            viewConfiguration.touchSlop
                                        ) {
                                            movedBeforeLongPress = true
                                            return@withTimeoutOrNull true
                                        }
                                        change.consume()
                                    }
                                } != null

                                if (completedBeforeTimeout) {
                                    val upTime = releasedAt
                                    if (upTime != null) {
                                        pressedIndex = -1
                                        when {
                                            isSecondTap -> {
                                                cancelControlledGesture()
                                                currentOnTabDoubleClick.value?.invoke(targetIndex)
                                            }
                                            canDoubleTap -> {
                                                cancelControlledGesture()
                                                pendingTap = PendingDockTabTap(targetIndex, upTime)
                                            }
                                            else -> selectTappedTab(targetIndex)
                                        }
                                        return@awaitEachGesture
                                    }
                                    if (!movedBeforeLongPress) {
                                        pressedIndex = -1
                                        if (isSecondTap) selectTappedTab(targetIndex)
                                        return@awaitEachGesture
                                    }
                                }

                                if (isSecondTap) selectTappedTab(targetIndex)
                                val openedMenu = beginControlledGesture(
                                    position = latestPosition,
                                    showLongPressMenu = !completedBeforeTimeout,
                                )
                                val longPressPosition = latestPosition
                                var draggedAfterLongPress = false
                                while (true) {
                                    val event = awaitPointerEvent()
                                    val change = event.changes.firstOrNull { it.id == down.id }
                                        ?: event.changes.firstOrNull()
                                        ?: break
                                    latestPosition = change.position
                                    change.consume()
                                    if (!change.pressed) break

                                    draggedAfterLongPress = draggedAfterLongPress ||
                                        (latestPosition - longPressPosition).getDistance() > viewConfiguration.touchSlop
                                    dragControlledGesture(latestPosition)
                                }

                                pressedIndex = -1
                                // Holding an unselected tab opens its menu without selecting the tab on release.
                                if (openedMenu && !draggedAfterLongPress) {
                                    cancelControlledGesture()
                                } else {
                                    finishControlledGesture()
                                }
                            }
                        } finally {
                            // A layout/key change can cancel pointer input mid-gesture.
                            pressedIndex = -1
                            previewing = false
                        }
                    },
            )
        }
    }
}
