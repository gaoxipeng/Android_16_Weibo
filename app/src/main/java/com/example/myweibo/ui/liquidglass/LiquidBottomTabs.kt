package com.example.myweibo.ui.liquidglass

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastCoerceIn
import androidx.compose.ui.util.fastRoundToInt
import androidx.compose.ui.util.lerp
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberCombinedBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import com.kyant.backdrop.highlight.Highlight
import com.kyant.backdrop.shadow.InnerShadow
import com.kyant.backdrop.shadow.Shadow
import com.example.myweibo.ui.theme.TabAccentDark
import com.example.myweibo.ui.theme.TabAccentLight
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.sign

@Composable
fun LiquidBottomTabs(
    selectedTabIndex: () -> Int,
    onTabSelected: (index: Int) -> Unit,
    backdrop: Backdrop,
    tabsCount: Int,
    modifier: Modifier = Modifier,
    gestureController: LiquidBottomTabsGestureController = rememberLiquidBottomTabsGestureController(),
    feedTabIndex: Int = 0,
    onTabLongPress: (index: Int) -> Unit = {},
    content: @Composable RowScope.() -> Unit
) {
    val isLightTheme = !isSystemInDarkTheme()
    val accentColor =
        if (isLightTheme) TabAccentLight
        else TabAccentDark
    val surfaceColor = liquidSurfaceColor(isLightTheme)

    val tabsBackdrop = rememberLayerBackdrop()

    BoxWithConstraints(
        modifier.graphicsLayer { clip = false },
        contentAlignment = Alignment.CenterStart
    ) {
        val density = LocalDensity.current
        val tabWidth = with(density) {
            (constraints.maxWidth.toFloat() - 8f.dp.toPx()) / tabsCount
        }

        val offsetAnimation = remember { Animatable(0f) }
        val panelOffset by remember(density, constraints.maxWidth) {
            derivedStateOf {
                val fraction = (offsetAnimation.value / constraints.maxWidth).fastCoerceIn(-1f, 1f)
                with(density) {
                    4f.dp.toPx() * fraction.sign * EaseOut.transform(abs(fraction))
                }
            }
        }

        val isLtr = LocalLayoutDirection.current == LayoutDirection.Ltr
        val animationScope = rememberCoroutineScope()
        val selectedIndex = selectedTabIndex().fastCoerceIn(0, tabsCount - 1)
        var currentIndex by remember { mutableIntStateOf(selectedIndex) }
        var gestureTargetIndex by remember { mutableIntStateOf(selectedIndex) }
        var isUserGesturing by remember { mutableStateOf(false) }
        var lastGesturePosition by remember { mutableStateOf(Offset.Zero) }
        var gestureStartPosition by remember { mutableStateOf(Offset.Zero) }
        var gestureStartValue by remember { mutableFloatStateOf(selectedIndex.toFloat()) }
        val barWidthPx = constraints.maxWidth.toFloat()

        fun nearestTabIndex(position: Offset): Int {
            val slotWidth = barWidthPx / tabsCount
            var bestIndex = 0
            var bestDistance = Float.MAX_VALUE
            for (i in 0 until tabsCount) {
                val centerX = slotWidth * (i + 0.5f)
                val distance = abs(position.x - centerX)
                if (distance < bestDistance) {
                    bestDistance = distance
                    bestIndex = i
                }
            }
            return if (isLtr) bestIndex else tabsCount - 1 - bestIndex
        }

        fun commitTabSelection(index: Int) {
            onTabSelected(index)
        }

        val dampedDragAnimation = remember(animationScope, tabsCount) {
            DampedDragAnimation(
                animationScope = animationScope,
                initialValue = selectedIndex.toFloat(),
                valueRange = 0f..(tabsCount - 1).toFloat(),
                visibilityThreshold = 0.001f,
                initialScale = 1f,
                pressedScale = 78f / 56f,
                onDragStarted = { position ->
                    isUserGesturing = true
                    lastGesturePosition = position
                    gestureStartPosition = position
                    gestureStartValue = value
                    gestureTargetIndex = nearestTabIndex(position)
                },
                onDragStopped = {
                    val clampedIndex = nearestTabIndex(lastGesturePosition)
                        .fastCoerceIn(0, tabsCount - 1)
                    currentIndex = clampedIndex
                    gestureTargetIndex = clampedIndex
                    updateValue(clampedIndex.toFloat())
                    commitTabSelection(clampedIndex)
                    isUserGesturing = false
                    animationScope.launch {
                        offsetAnimation.animateTo(0f, spring(1f, 300f, 0.5f))
                    }
                },
                onDrag = { _, dragAmount, position ->
                    lastGesturePosition = position
                    val direction = if (isLtr) 1f else -1f
                    val newValue = (
                        gestureStartValue +
                            (position.x - gestureStartPosition.x) / tabWidth * direction
                        )
                        .fastCoerceIn(0f, (tabsCount - 1).toFloat())
                    gestureTargetIndex = newValue.fastRoundToInt().fastCoerceIn(0, tabsCount - 1)
                    snapToValue(newValue)
                    animationScope.launch {
                        offsetAnimation.snapTo(offsetAnimation.value + dragAmount.x)
                    }
                }
            )
        }

        LaunchedEffect(dampedDragAnimation) {
            dampedDragAnimation.snapToValue(selectedIndex.toFloat())
        }

        LaunchedEffect(selectedIndex, isUserGesturing) {
            if (isUserGesturing) return@LaunchedEffect
            currentIndex = selectedIndex
            gestureTargetIndex = selectedIndex
            dampedDragAnimation.updateValue(selectedIndex.toFloat())
        }

        DisposableEffect(gestureController, dampedDragAnimation) {
            gestureController.impl = object : LiquidBottomTabsGestureController.GestureImpl {
                override fun begin(position: Offset) {
                    isUserGesturing = true
                    lastGesturePosition = position
                    gestureStartPosition = position
                    gestureStartValue = dampedDragAnimation.value
                    gestureTargetIndex = nearestTabIndex(position)
                    dampedDragAnimation.press()
                }

                override fun drag(position: Offset, dragAmount: Offset) {
                    lastGesturePosition = position
                    val direction = if (isLtr) 1f else -1f
                    val newValue = (
                        gestureStartValue +
                            (position.x - gestureStartPosition.x) / tabWidth * direction
                        )
                        .fastCoerceIn(0f, (tabsCount - 1).toFloat())
                    gestureTargetIndex = newValue.fastRoundToInt().fastCoerceIn(0, tabsCount - 1)
                    dampedDragAnimation.snapToValue(newValue)
                    animationScope.launch {
                        offsetAnimation.snapTo(offsetAnimation.value + dragAmount.x)
                    }
                }

                override fun end() {
                    val clampedIndex = nearestTabIndex(lastGesturePosition)
                        .fastCoerceIn(0, tabsCount - 1)
                    currentIndex = clampedIndex
                    gestureTargetIndex = clampedIndex
                    dampedDragAnimation.updateValue(clampedIndex.toFloat())
                    commitTabSelection(clampedIndex)
                    dampedDragAnimation.release()
                    isUserGesturing = false
                    animationScope.launch {
                        offsetAnimation.animateTo(0f, spring(1f, 300f, 0.5f))
                    }
                }

                override fun cancel() {
                    isUserGesturing = false
                    dampedDragAnimation.release()
                    animationScope.launch {
                        offsetAnimation.animateTo(0f, spring(1f, 300f, 0.5f))
                    }
                }
            }
            onDispose {
                gestureController.impl = null
            }
        }

        var indicatorValue by remember(dampedDragAnimation) {
            mutableFloatStateOf(dampedDragAnimation.value)
        }
        var indicatorPressProgress by remember(dampedDragAnimation) {
            mutableFloatStateOf(dampedDragAnimation.pressProgress)
        }
        var indicatorScaleX by remember(dampedDragAnimation) {
            mutableFloatStateOf(dampedDragAnimation.scaleX)
        }
        var indicatorScaleY by remember(dampedDragAnimation) {
            mutableFloatStateOf(dampedDragAnimation.scaleY)
        }

        LaunchedEffect(dampedDragAnimation) {
            while (true) {
                withFrameMillis {
                    indicatorValue = dampedDragAnimation.value
                    indicatorPressProgress = dampedDragAnimation.pressProgress
                    indicatorScaleX = dampedDragAnimation.scaleX
                    indicatorScaleY = dampedDragAnimation.scaleY
                }
            }
        }

        val interactiveHighlight = remember(animationScope, tabWidth) {
            InteractiveHighlight(
                animationScope = animationScope,
                position = { size, offset ->
                    Offset(
                        if (isLtr) (indicatorValue + 0.5f) * tabWidth + panelOffset
                        else size.width - (indicatorValue + 0.5f) * tabWidth + panelOffset,
                        size.height / 2f
                    )
                }
            )
        }

        CompositionLocalProvider(
            LocalLiquidBottomTabBackdropRow provides false,
            LocalLiquidBottomTabIndicatorIndex provides gestureTargetIndex,
        ) {
        Row(
            Modifier
                .graphicsLayer {
                    clip = false
                    translationX = panelOffset
                }
                .drawBackdrop(
                    backdrop = backdrop,
                    shape = { RoundedCornerShape(percent = 50) },
                    effects = {
                        vibrancy()
                        blur(2f.dp.toPx())
                        lens(12f.dp.toPx(), 24f.dp.toPx())
                    },
                    layerBlock = {
                        val progress = indicatorPressProgress
                        val scale = lerp(1f, 1f + 16f.dp.toPx() / size.width, progress)
                        scaleX = scale
                        scaleY = scale
                    },
                    onDrawSurface = { drawRect(surfaceColor) }
                )
                .then(interactiveHighlight.modifier)
                .height(64f.dp)
                .fillMaxWidth()
                .padding(4f.dp),
            verticalAlignment = Alignment.CenterVertically,
            content = content
        )
        }

        CompositionLocalProvider(
            LocalLiquidBottomTabScale provides {
                lerp(1f, 1.2f, indicatorPressProgress)
            },
            LocalLiquidBottomTabBackdropRow provides true,
        ) {
            Row(
                Modifier
                    .clearAndSetSemantics {}
                    .alpha(0f)
                    .layerBackdrop(tabsBackdrop)
                    .graphicsLayer {
                        translationX = panelOffset
                    }
                    .drawBackdrop(
                        backdrop = backdrop,
                        shape = { RoundedCornerShape(percent = 50) },
                        effects = {
                            val progress = indicatorPressProgress
                            vibrancy()
                            blur(2f.dp.toPx())
                            lens(
                                12f.dp.toPx() * progress.coerceAtLeast(0.01f),
                                24f.dp.toPx() * progress.coerceAtLeast(0.01f),
                            )
                        },
                        highlight = {
                            Highlight.Default.copy(alpha = indicatorPressProgress)
                        },
                        onDrawSurface = { drawRect(surfaceColor) }
                    )
                    .then(interactiveHighlight.modifier)
                    .height(56f.dp)
                    .fillMaxWidth()
                    .padding(horizontal = 4f.dp)
                    .graphicsLayer(colorFilter = ColorFilter.tint(accentColor)),
                verticalAlignment = Alignment.CenterVertically,
                content = content
            )
        }

        Box(
            Modifier
                .padding(horizontal = 4f.dp)
                .graphicsLayer {
                    clip = false
                    translationX =
                        if (isLtr) indicatorValue * tabWidth + panelOffset
                        else size.width - (indicatorValue + 1f) * tabWidth + panelOffset
                }
                .drawBackdrop(
                    backdrop = rememberCombinedBackdrop(backdrop, tabsBackdrop),
                    shape = { RoundedCornerShape(percent = 50) },
                    effects = {
                        val progress = indicatorPressProgress
                        lens(
                            10f.dp.toPx() * progress,
                            14f.dp.toPx() * progress,
                            chromaticAberration = true
                        )
                    },
                    highlight = {
                        Highlight.Default.copy(alpha = indicatorPressProgress)
                    },
                    shadow = {
                        Shadow(alpha = indicatorPressProgress)
                    },
                    innerShadow = {
                        InnerShadow(
                            radius = 8f.dp * indicatorPressProgress,
                            alpha = indicatorPressProgress
                        )
                    },
                    layerBlock = {
                        scaleX = indicatorScaleX
                        scaleY = indicatorScaleY
                        val velocity = dampedDragAnimation.velocity / 10f
                        scaleX /= 1f - (velocity * 0.75f).fastCoerceIn(-0.2f, 0.2f)
                        scaleY *= 1f - (velocity * 0.25f).fastCoerceIn(-0.2f, 0.2f)
                    },
                    onDrawSurface = {
                        val progress = indicatorPressProgress
                        drawRect(
                            if (isLightTheme) Color.Black.copy(0.1f)
                            else Color.White.copy(0.1f),
                            alpha = 1f - progress
                        )
                        drawRect(Color.Black.copy(alpha = 0.03f * progress))
                    }
                )
                .height(56f.dp)
                .fillMaxWidth(1f / tabsCount)
        )

        Box(
            Modifier
                .matchParentSize()
                .pointerInput(feedTabIndex, barWidthPx, tabsCount, isLtr) {
                    detectTapGestures(
                        onTap = { offset ->
                            val index = nearestTabIndex(offset)
                                .fastCoerceIn(0, tabsCount - 1)
                            currentIndex = index
                            gestureTargetIndex = index
                            dampedDragAnimation.updateValue(index.toFloat())
                            commitTabSelection(index)
                        },
                        onLongPress = { offset ->
                            val slotWidth = barWidthPx / tabsCount
                            var bestIndex = 0
                            var bestDistance = Float.MAX_VALUE
                            for (i in 0 until tabsCount) {
                                val centerX = slotWidth * (i + 0.5f)
                                val distance = kotlin.math.abs(offset.x - centerX)
                                if (distance < bestDistance) {
                                    bestDistance = distance
                                    bestIndex = i
                                }
                            }
                            val index = if (isLtr) bestIndex else tabsCount - 1 - bestIndex
                            if (index == feedTabIndex) {
                                onTabLongPress(index)
                            }
                        },
                    )
                }
                .then(dampedDragAnimation.modifier),
        )
    }
}
