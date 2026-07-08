package com.example.myweibo.ui.liquidglass

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.spring
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import com.example.myweibo.ui.theme.isAppLightTheme
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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
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
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.math.abs
import kotlin.math.sign

@Composable
fun LiquidBottomTabs(
    selectedTabIndex: () -> Int,
    onTabSelected: (index: Int) -> Unit,
    backdrop: Backdrop,
    tabsCount: Int,
    modifier: Modifier = Modifier,
    gestureController: LiquidBottomTabsGestureController =
        rememberLiquidBottomTabsGestureController(),
    feedTabIndex: Int = 0,
    onTabLongPress: (index: Int) -> Unit = {},
    content: @Composable RowScope.() -> Unit,
) {
    val isLightTheme = isAppLightTheme()
    val surfaceColor = liquidSurfaceColor(isLightTheme)
    val tabsBackdrop = rememberLayerBackdrop()

    BoxWithConstraints(
        modifier = modifier.graphicsLayer { clip = false },
        contentAlignment = Alignment.CenterStart,
    ) {
        val density = LocalDensity.current
        val horizontalPaddingPx = with(density) { 4.dp.toPx() }
        val tabWidth = with(density) {
            (constraints.maxWidth.toFloat() - 8.dp.toPx()) / tabsCount
        }
        val offsetAnimation = remember { Animatable(0f) }
        val panelOffset by remember(density, constraints.maxWidth) {
            derivedStateOf {
                val fraction = (offsetAnimation.value / constraints.maxWidth)
                    .fastCoerceIn(-1f, 1f)
                with(density) {
                    4.dp.toPx() * fraction.sign * EaseOut.transform(abs(fraction))
                }
            }
        }

        val isLtr = LocalLayoutDirection.current == LayoutDirection.Ltr
        val animationScope = rememberCoroutineScope()
        val selectedIndex = selectedTabIndex().fastCoerceIn(0, tabsCount - 1)
        val currentTabWidth = rememberUpdatedState(tabWidth)
        val currentIsLtr = rememberUpdatedState(isLtr)
        val currentSelectedIndex = rememberUpdatedState(selectedIndex)
        val currentFeedTabIndex = rememberUpdatedState(feedTabIndex)
        val currentMaxWidth = rememberUpdatedState(constraints.maxWidth)
        val currentMaxHeight = rememberUpdatedState(constraints.maxHeight)
        val currentOnTabSelected = rememberUpdatedState(onTabSelected)
        val currentOnTabLongPress = rememberUpdatedState(onTabLongPress)
        fun valueAt(position: Offset): Float {
            val visualValue = (
                (position.x - horizontalPaddingPx) / currentTabWidth.value - 0.5f
                )
                .fastCoerceIn(0f, (tabsCount - 1).toFloat())
            return if (currentIsLtr.value) {
                visualValue
            } else {
                (tabsCount - 1).toFloat() - visualValue
            }
        }

        fun indexAt(position: Offset): Int =
            valueAt(position).fastRoundToInt().fastCoerceIn(0, tabsCount - 1)

        val dampedDragAnimation = remember(animationScope, tabsCount) {
            DampedDragAnimation(
                animationScope = animationScope,
                initialValue = selectedIndex.toFloat(),
                valueRange = 0f..(tabsCount - 1).toFloat(),
                visibilityThreshold = 0.001f,
                initialScale = 1f,
                pressedScale = 78f / 56f,
                onDragStarted = {},
                onDragStopped = {
                    val targetIndex = targetValue.fastRoundToInt()
                        .fastCoerceIn(0, tabsCount - 1)
                    animateToValue(targetIndex.toFloat())
                    onTabSelected(targetIndex)
                    animationScope.launch {
                        offsetAnimation.animateTo(0f, spring(1f, 300f, 0.5f))
                    }
                },
                onDrag = { _, dragAmount, _ ->
                    updateValue(
                        (
                            targetValue +
                                dragAmount.x / currentTabWidth.value *
                                if (currentIsLtr.value) 1f else -1f
                            )
                            .fastCoerceIn(0f, (tabsCount - 1).toFloat()),
                    )
                    animationScope.launch {
                        offsetAnimation.snapTo(offsetAnimation.value + dragAmount.x)
                    }
                },
            )
        }
        var controlledGesturePosition by remember { mutableStateOf(Offset.Zero) }

        fun beginControlledGesture(position: Offset, showLongPressMenu: Boolean) {
            controlledGesturePosition = position
            if (showLongPressMenu && indexAt(position) == currentFeedTabIndex.value) {
                currentOnTabLongPress.value(currentFeedTabIndex.value)
            }
            dampedDragAnimation.updateValue(valueAt(position))
        }

        fun dragControlledGesture(position: Offset, dragAmount: Offset) {
            controlledGesturePosition = position
            dampedDragAnimation.updateValue(valueAt(position))
            animationScope.launch {
                offsetAnimation.snapTo(offsetAnimation.value + dragAmount.x)
            }
        }

        fun finishControlledGesture() {
            val targetIndex = indexAt(controlledGesturePosition)
            dampedDragAnimation.animateToValue(targetIndex.toFloat())
            if (targetIndex != currentSelectedIndex.value) {
                currentOnTabSelected.value(targetIndex)
            }
            animationScope.launch {
                offsetAnimation.animateTo(0f, spring(1f, 300f, 0.5f))
            }
        }

        fun cancelControlledGesture() {
            dampedDragAnimation.animateToValue(currentSelectedIndex.value.toFloat())
            animationScope.launch {
                offsetAnimation.animateTo(0f, spring(1f, 300f, 0.5f))
            }
        }

        DisposableEffect(
            gestureController,
            dampedDragAnimation,
        ) {
            gestureController.impl = object : LiquidBottomTabsGestureController.GestureImpl {
                fun localPosition(positionFraction: Offset): Offset =
                    Offset(
                        x = positionFraction.x.coerceIn(0f, 1f) * currentMaxWidth.value,
                        y = positionFraction.y.coerceIn(0f, 1f) * currentMaxHeight.value,
                    )

                override fun begin(positionFraction: Offset) {
                    val position = localPosition(positionFraction)
                    dampedDragAnimation.press()
                    beginControlledGesture(
                        position = position,
                        showLongPressMenu = true,
                    )
                }

                override fun drag(positionFraction: Offset, dragAmount: Offset) {
                    dragControlledGesture(
                        position = localPosition(positionFraction),
                        dragAmount = dragAmount,
                    )
                }

                override fun end() {
                    finishControlledGesture()
                }

                override fun cancel() {
                    cancelControlledGesture()
                }
            }
            onDispose {
                gestureController.impl = null
            }
        }

        LaunchedEffect(selectedIndex) {
            if (dampedDragAnimation.targetValue.fastRoundToInt() != selectedIndex) {
                dampedDragAnimation.animateToValue(selectedIndex.toFloat())
            }
        }

        var animatedIndicatorValue by remember(dampedDragAnimation) {
            mutableFloatStateOf(dampedDragAnimation.value)
        }
        var animatedPressProgress by remember(dampedDragAnimation) {
            mutableFloatStateOf(dampedDragAnimation.pressProgress)
        }
        var animatedScaleX by remember(dampedDragAnimation) {
            mutableFloatStateOf(dampedDragAnimation.scaleX)
        }
        var animatedScaleY by remember(dampedDragAnimation) {
            mutableFloatStateOf(dampedDragAnimation.scaleY)
        }
        var animatedVelocity by remember(dampedDragAnimation) {
            mutableFloatStateOf(dampedDragAnimation.velocity)
        }
        LaunchedEffect(dampedDragAnimation) {
            while (true) {
                withFrameMillis {
                    animatedIndicatorValue = dampedDragAnimation.value
                    animatedPressProgress = dampedDragAnimation.pressProgress
                    animatedScaleX = dampedDragAnimation.scaleX
                    animatedScaleY = dampedDragAnimation.scaleY
                    animatedVelocity = dampedDragAnimation.velocity
                }
            }
        }

        val indicatorIndex = dampedDragAnimation.targetValue.fastRoundToInt()
            .fastCoerceIn(0, tabsCount - 1)
        val interactiveHighlight = remember(animationScope, tabWidth) {
            InteractiveHighlight(
                animationScope = animationScope,
                position = { size, _ ->
                    Offset(
                        if (isLtr) (animatedIndicatorValue + 0.5f) * tabWidth + panelOffset
                        else size.width - (animatedIndicatorValue + 0.5f) * tabWidth + panelOffset,
                        size.height / 2f,
                    )
                },
            )
        }

        val barShape = RoundedCornerShape(percent = 50)
        val barBorderColor = liquidMenuBorderColor(isLightTheme)
        val glassMotionTarget by remember(density) {
            derivedStateOf {
                val offsetThreshold = with(density) { 6.dp.toPx() }
                val scaleMotion = (
                    (abs(animatedScaleX - 1f) + abs(animatedScaleY - 1f)) / 0.28f
                    ).coerceIn(0f, 1f)
                maxOf(
                    animatedPressProgress,
                    scaleMotion,
                    (abs(animatedVelocity) / 8f).coerceIn(0f, 1f),
                    (abs(offsetAnimation.value) / offsetThreshold).coerceIn(0f, 1f),
                )
            }
        }
        val glassMotionProgress by animateFloatAsState(
            targetValue = glassMotionTarget,
            animationSpec = spring(dampingRatio = 0.92f, stiffness = 900f),
            label = "liquid-tab-glass-motion",
        )

        CompositionLocalProvider(
            LocalLiquidBottomTabGlassMotionProgress provides glassMotionProgress,
            LocalLiquidBottomTabPressProgress provides animatedPressProgress,
        ) {
        CompositionLocalProvider(LocalLiquidBottomTabIndicatorIndex provides indicatorIndex) {
            Row(
                Modifier
                    .graphicsLayer {
                        clip = false
                        translationX = panelOffset
                    }
                    .drawBackdrop(
                        backdrop = backdrop,
                        shape = { barShape },
                        effects = {
                            liquidMenuGlassEffects()
                        },
                        highlight = null,
                        shadow = null,
                        layerBlock = {
                            val scale = lerp(
                                1f,
                                1f + 16.dp.toPx() / size.width,
                                animatedPressProgress,
                            )
                            scaleX = scale
                            scaleY = scale
                        },
                        onDrawSurface = {
                            drawRect(surfaceColor)
                        },
                    )
                    .border(LiquidMenuBorderWidth, barBorderColor, barShape)
                    .then(interactiveHighlight.modifier)
                    .height(64.dp)
                    .fillMaxWidth()
                    .padding(4.dp),
                verticalAlignment = Alignment.CenterVertically,
                content = content,
            )
        }

        CompositionLocalProvider(
            LocalLiquidBottomTabScale provides {
                lerp(1f, 1.2f, animatedPressProgress)
            },
            LocalLiquidBottomTabIndicatorIndex provides indicatorIndex,
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
                            val progress = animatedPressProgress
                            vibrancy()
                            blur(LiquidMenuGlassBlurRadius.toPx())
                            lens(
                                BottomBarTabIndicatorLensRefraction.toPx() * progress.coerceAtLeast(0.01f),
                                LiquidMenuGlassBlurRadius.toPx() * progress.coerceAtLeast(0.01f),
                            )
                        },
                        highlight = {
                            Highlight.Default.copy(alpha = animatedPressProgress)
                        },
                        onDrawSurface = { drawRect(surfaceColor) },
                    )
                    .then(interactiveHighlight.modifier)
                    .height(56.dp)
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                content = content,
            )
        }

        Box(
            Modifier
                .padding(horizontal = 4.dp)
                .graphicsLayer {
                    clip = false
                    translationX =
                        if (isLtr) animatedIndicatorValue * tabWidth + panelOffset
                        else size.width - (animatedIndicatorValue + 1f) * tabWidth + panelOffset
                }
                .drawBackdrop(
                    backdrop = rememberCombinedBackdrop(backdrop, tabsBackdrop),
                    shape = { RoundedCornerShape(percent = 50) },
                    effects = {
                        lens(
                            10.dp.toPx() * animatedPressProgress,
                            14.dp.toPx() * animatedPressProgress,
                            chromaticAberration = true,
                        )
                    },
                    highlight = {
                        Highlight.Default.copy(alpha = animatedPressProgress)
                    },
                    shadow = { Shadow(alpha = animatedPressProgress) },
                    innerShadow = {
                        InnerShadow(
                            radius = 8.dp * animatedPressProgress,
                            alpha = animatedPressProgress,
                        )
                    },
                    layerBlock = {
                        scaleX = animatedScaleX
                        scaleY = animatedScaleY
                        val velocity = animatedVelocity / 10f
                        scaleX /= 1f - (velocity * 0.75f).fastCoerceIn(-0.2f, 0.2f)
                        scaleY *= 1f - (velocity * 0.25f).fastCoerceIn(-0.2f, 0.2f)
                    },
                    onDrawSurface = {
                        val progress = animatedPressProgress
                        drawRect(
                            if (isLightTheme) Color.Black.copy(alpha = 0.1f)
                            else Color.White.copy(alpha = 0.1f),
                            alpha = 1f - progress,
                        )
                        drawRect(Color.Black.copy(alpha = 0.03f * progress))
                    },
                )
                .height(56.dp)
                .fillMaxWidth(1f / tabsCount),
        )

        Box(
            Modifier
                .fillMaxSize()
                .pointerInput(
                    tabsCount,
                    feedTabIndex,
                    isLtr,
                    tabWidth,
                    onTabSelected,
                    onTabLongPress,
                ) {
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        down.consume()
                        dampedDragAnimation.press()
                        var latestPosition = down.position
                        var releasedBeforeLongPress = false
                        var movedBeforeLongPress = false
                        val completedBeforeTimeout = withTimeoutOrNull(
                            viewConfiguration.longPressTimeoutMillis,
                        ) {
                            while (true) {
                                val event = awaitPointerEvent()
                                val change = event.changes.firstOrNull { it.id == down.id }
                                    ?: event.changes.firstOrNull()
                                    ?: run {
                                        return@withTimeoutOrNull true
                                    }
                                latestPosition = change.position
                                if (!change.pressed) {
                                    releasedBeforeLongPress = true
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
                            if (releasedBeforeLongPress) {
                                val targetIndex = indexAt(down.position)
                                dampedDragAnimation.animateToValue(targetIndex.toFloat())
                                onTabSelected(targetIndex)
                                animationScope.launch {
                                    offsetAnimation.animateTo(0f, spring(1f, 300f, 0.5f))
                                }
                                return@awaitEachGesture
                            }
                            if (!movedBeforeLongPress) {
                                dampedDragAnimation.release()
                                return@awaitEachGesture
                            }
                        }

                        beginControlledGesture(
                            position = latestPosition,
                            showLongPressMenu = !completedBeforeTimeout,
                        )
                        var previousPosition = latestPosition
                        while (true) {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull { it.id == down.id }
                                ?: event.changes.firstOrNull()
                                ?: break
                            latestPosition = change.position
                            change.consume()
                            if (!change.pressed) break

                            val dragAmount = latestPosition - previousPosition
                            previousPosition = latestPosition
                            dragControlledGesture(latestPosition, dragAmount)
                        }

                        finishControlledGesture()
                    }
                },
        )
        }
    }
}
