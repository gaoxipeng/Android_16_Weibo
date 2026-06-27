package com.example.myweibo.ui

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.myweibo.R
import com.example.myweibo.data.TimelineKind
import com.example.myweibo.ui.liquidglass.LiquidBottomTab
import com.example.myweibo.ui.liquidglass.LiquidBottomTabs
import com.example.myweibo.ui.liquidglass.rememberLiquidBottomTabsGestureController
import com.example.myweibo.ui.liquidglass.SurfaceLiquidIconButton
import com.kyant.backdrop.Backdrop
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
internal fun WeiboLiquidBottomBar(
    selectedTab: MainTab,
    onTabChange: (MainTab) -> Unit,
    expanded: Boolean,
    backdrop: Backdrop,
    onExpandRequest: () -> Unit,
    onCollapsedTap: () -> Unit,
    timelineMenuExpanded: Boolean,
    onTimelineMenuExpandedChange: (Boolean) -> Unit,
    feedTabLabel: String = MainTab.Feed.label,
    selectedTimelineKind: TimelineKind = TimelineKind.Following,
    onTimelineKindChange: (TimelineKind) -> Unit = {},
    timelineMenuContent: @Composable (dismiss: () -> Unit) -> Unit,
    timelineMenuWidth: Dp = 152.dp,
    timelineMenuHeight: Dp = 89.dp,
    timelineMenuGap: Dp = 4.dp,
    modifier: Modifier = Modifier,
) {
    val tabContentColor = MaterialTheme.colorScheme.primary
    val density = LocalDensity.current
    val collapsedSize = 64.dp
    val barHeight = 64.dp
    val animationOverflow = 12.dp
    val tabs = MainTab.entries
    val selectedIndex = tabs.indexOf(selectedTab).coerceAtLeast(0)
    val feedIndex = tabs.indexOf(MainTab.Feed).coerceAtLeast(0)
    val tabsGestureController = rememberLiquidBottomTabsGestureController()
    val collapsedGestureScope = rememberCoroutineScope()
    var collapsedGestureActive by remember { mutableStateOf(false) }
    var tabsMounted by remember { mutableStateOf(expanded) }
    if (expanded) {
        tabsMounted = true
    }

    val barMorphDurationMillis = 220
    // 中间段更快，首尾平滑，避免 width 布局动画带来的掉帧
    val barMorphEasing = CubicBezierEasing(0.38f, 0.02f, 0.18f, 1f)
    val morphTween = tween<Float>(
        durationMillis = barMorphDurationMillis,
        easing = barMorphEasing,
    )

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 18.dp, top = 4.dp, end = 18.dp, bottom = 24.dp)
            .navigationBarsPadding(),
        contentAlignment = Alignment.BottomStart,
    ) {
        val fullBarWidth = maxWidth
        val collapsedWidthFraction = (collapsedSize / fullBarWidth).coerceIn(0f, 1f)
        val transition = updateTransition(targetState = expanded, label = "bottom-bar-expansion")
        val isMorphing = transition.isRunning

        val morphProgress by transition.animateFloat(
            transitionSpec = { morphTween },
            label = "bar-morph-progress",
        ) { isExpanded ->
            if (isExpanded) 1f else 0f
        }

        val collapsedAlpha = (1f - morphProgress).coerceIn(0f, 1f)
        val revealAlpha = morphProgress.coerceIn(0f, 1f)
        val widthScale = collapsedWidthFraction + (1f - collapsedWidthFraction) * revealAlpha
        val bounceOvershoot = (morphProgress - 1f).coerceAtLeast(0f)
        val morphT = 1f - abs(revealAlpha - 0.5f) * 2f
        val morphScaleX = 1f + morphT * 0.06f + bounceOvershoot * 0.1f
        val morphScaleY = 1f - morphT * 0.02f + bounceOvershoot * 0.06f
        val collapsedOnTop = revealAlpha < 0.5f
        val expandedState = rememberUpdatedState(expanded)
        val collapsedInteractiveState = rememberUpdatedState(
            collapsedOnTop && collapsedAlpha > 0.5f,
        )

        Box(Modifier.width(fullBarWidth).height(barHeight + animationOverflow)) {
            if (tabsMounted && (revealAlpha > 0.01f || isMorphing)) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .width(fullBarWidth)
                        .height(barHeight)
                        .zIndex(if (collapsedOnTop) 0f else 2f)
                        .graphicsLayer {
                            clip = false
                            alpha = revealAlpha
                            scaleX = widthScale * morphScaleX
                            scaleY = morphScaleY
                            transformOrigin = TransformOrigin(0f, 1f)
                        },
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                scaleX = 0.92f + revealAlpha * 0.08f + bounceOvershoot * 0.03f
                                scaleY = 0.92f + revealAlpha * 0.08f + bounceOvershoot * 0.03f
                            },
                    ) {
                        LiquidBottomTabs(
                            selectedTabIndex = { selectedIndex },
                            onTabSelected = { index ->
                                if (index != feedIndex) {
                                    onTimelineMenuExpandedChange(false)
                                }
                                onTabChange(tabs[index])
                            },
                            backdrop = backdrop,
                            tabsCount = tabs.size,
                            gestureController = tabsGestureController,
                            feedTabIndex = feedIndex,
                            onTabLongPress = { onTimelineMenuExpandedChange(true) },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            tabs.forEachIndexed { index, tab ->
                                LiquidBottomTab(
                                    onClick = { onTabChange(tab) },
                                ) {
                                    Box(
                                        modifier = Modifier.size(28.dp),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        WeiboTabIcon(tab = tab, color = tabContentColor)
                                    }
                                    Text(
                                        text = if (tab == MainTab.Feed) feedTabLabel else tab.label,
                                        fontSize = 12.sp,
                                        color = tabContentColor,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (collapsedAlpha > 0.01f || isMorphing) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .size(collapsedSize)
                        .zIndex(if (collapsedOnTop) 2f else 0f)
                        .graphicsLayer {
                            alpha = collapsedAlpha
                            scaleX = 0.9f + collapsedAlpha * 0.1f
                            scaleY = 0.9f + collapsedAlpha * 0.1f
                            transformOrigin = TransformOrigin(0f, 1f)
                        },
                ) {
                    SurfaceLiquidIconButton(
                        onClick = onExpandRequest,
                        onDoubleClick = onCollapsedTap,
                        backdrop = backdrop,
                        isInteractive = collapsedOnTop && collapsedAlpha > 0.5f,
                        inputEnabled = false,
                        useMenuGlassStyle = true,
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        WeiboTabIcon(
                            tab = selectedTab,
                            color = tabContentColor,
                            size = 24.dp,
                        )
                    }
                }
            }

            if (!expanded || collapsedGestureActive) {
                Box(
                    Modifier
                        .align(Alignment.BottomStart)
                        .size(collapsedSize)
                        .zIndex(3f)
                        .pointerInput(tabsGestureController, collapsedSize) {
                        var pendingSingleTap: Job? = null
                        var lastTapUptime = 0L
                        awaitEachGesture {
                            val down = awaitFirstDown(
                                requireUnconsumed = false,
                                pass = PointerEventPass.Initial,
                            )
                            if (expandedState.value || !collapsedInteractiveState.value) {
                                return@awaitEachGesture
                            }

                            down.consume()
                            var latestPosition = down.position
                            var releasedBeforeLongPress = false
                            var cancelledBeforeLongPress = false
                            var releaseUptime = down.uptimeMillis
                            val completedBeforeTimeout = withTimeoutOrNull(
                                viewConfiguration.longPressTimeoutMillis,
                            ) {
                                while (true) {
                                    val event = awaitPointerEvent(PointerEventPass.Initial)
                                    val change = event.changes.firstOrNull { it.id == down.id }
                                        ?: event.changes.firstOrNull()
                                        ?: run {
                                            cancelledBeforeLongPress = true
                                            return@withTimeoutOrNull true
                                        }
                                    latestPosition = change.position
                                    if (!change.pressed) {
                                        releasedBeforeLongPress = true
                                        releaseUptime = change.uptimeMillis
                                        change.consume()
                                        return@withTimeoutOrNull true
                                    }
                                    if ((change.position - down.position).getDistance() >
                                        viewConfiguration.touchSlop
                                    ) {
                                        cancelledBeforeLongPress = true
                                        return@withTimeoutOrNull true
                                    }
                                    change.consume()
                                }
                            } != null

                            if (completedBeforeTimeout) {
                                if (releasedBeforeLongPress && !cancelledBeforeLongPress) {
                                    val isDoubleTap = lastTapUptime > 0L &&
                                        releaseUptime - lastTapUptime <=
                                        viewConfiguration.doubleTapTimeoutMillis
                                    if (isDoubleTap) {
                                        pendingSingleTap?.cancel()
                                        pendingSingleTap = null
                                        lastTapUptime = 0L
                                        onCollapsedTap()
                                    } else {
                                        lastTapUptime = releaseUptime
                                        pendingSingleTap?.cancel()
                                        pendingSingleTap = collapsedGestureScope.launch {
                                            delay(viewConfiguration.doubleTapTimeoutMillis)
                                            if (lastTapUptime == releaseUptime) {
                                                lastTapUptime = 0L
                                                onExpandRequest()
                                            }
                                        }
                                    }
                                }
                                return@awaitEachGesture
                            }

                            pendingSingleTap?.cancel()
                            pendingSingleTap = null
                            lastTapUptime = 0L
                            collapsedGestureActive = true
                            onExpandRequest()
                            val fullWidthPx = size.width.toFloat().coerceAtLeast(1f)
                            val fullHeightPx = size.height.toFloat().coerceAtLeast(1f)
                            fun normalized(position: androidx.compose.ui.geometry.Offset) =
                                androidx.compose.ui.geometry.Offset(
                                    x = position.x / fullWidthPx,
                                    y = position.y / fullHeightPx,
                                )
                            tabsGestureController.beginAt(normalized(latestPosition))
                            var previousPosition = latestPosition
                            while (true) {
                                val event = awaitPointerEvent(PointerEventPass.Initial)
                                val change = event.changes.firstOrNull { it.id == down.id }
                                    ?: event.changes.firstOrNull()
                                    ?: run {
                                        tabsGestureController.cancel()
                                        collapsedGestureActive = false
                                        break
                                    }
                                latestPosition = change.position
                                change.consume()
                                if (!change.pressed) {
                                    tabsGestureController.end()
                                    collapsedGestureActive = false
                                    break
                                }
                                val dragAmount = latestPosition - previousPosition
                                previousPosition = latestPosition
                                tabsGestureController.dragTo(
                                    normalized(latestPosition),
                                    dragAmount,
                                )
                            }
                        }
                        },
                )
            }
        }

        val timelineMenuOffsetX = with(density) {
            val fullWidthPx = fullBarWidth.toPx()
            val menuWidthPx = timelineMenuWidth.toPx()
            val barPaddingPx = 18.dp.toPx()
            val feedCenterPx = barPaddingPx + (fullBarWidth.toPx() * (feedIndex + 0.5f) / tabs.size)
            (feedCenterPx - menuWidthPx / 2f)
                .coerceIn(0f, (fullWidthPx - menuWidthPx).coerceAtLeast(0f))
                .roundToInt()
        }
        val menuLift = barHeight + animationOverflow + timelineMenuGap
        val timelineMenuOriginInMenu = with(density) {
            Offset(timelineMenuWidth.toPx() / 2f, timelineMenuHeight.toPx())
        }
        ActionMenuReveal(
            visible = timelineMenuExpanded,
            menuWidth = timelineMenuWidth,
            menuHeight = timelineMenuHeight,
            originInMenu = timelineMenuOriginInMenu,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset {
                    IntOffset(
                        timelineMenuOffsetX,
                        -with(density) { menuLift.roundToPx() },
                    )
                }
                .zIndex(50f)
                .graphicsLayer { clip = false },
        ) {
            timelineMenuContent { onTimelineMenuExpandedChange(false) }
        }
    }
}

@Composable
private fun WeiboTabIcon(
    tab: MainTab,
    color: Color,
    size: Dp = 20.dp,
) {
    Icon(
        painter = painterResource(
            when (tab) {
                MainTab.Feed -> R.drawable.ic_tab_home
                MainTab.Search -> R.drawable.ic_tab_search
                MainTab.Messages -> R.drawable.ic_tab_messages
                MainTab.Mine -> R.drawable.ic_tab_mine
                MainTab.Compose -> R.drawable.ic_tab_compose
            },
        ),
        contentDescription = tab.label,
        modifier = Modifier.size(size),
        tint = color,
    )
}
