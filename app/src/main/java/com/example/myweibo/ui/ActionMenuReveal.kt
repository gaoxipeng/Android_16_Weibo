package com.example.myweibo.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import kotlinx.coroutines.launch

internal fun computeActionMenuOriginInMenu(
    anchorInRoot: Offset,
    menuOffset: IntOffset,
    menuWidthPx: Float,
    menuHeightPx: Float,
): Offset = Offset(
    x = (anchorInRoot.x - menuOffset.x).coerceIn(0f, menuWidthPx.coerceAtLeast(1f)),
    y = (anchorInRoot.y - menuOffset.y).coerceIn(0f, menuHeightPx.coerceAtLeast(1f)),
)

@Composable
internal fun ActionMenuReveal(
    visible: Boolean,
    menuWidth: Dp,
    menuHeight: Dp,
    originInMenu: Offset,
    modifier: Modifier = Modifier,
    onExitComplete: () -> Unit = {},
    content: @Composable () -> Unit,
) {
    val density = LocalDensity.current
    val menuWidthPx = with(density) { menuWidth.toPx() }
    val menuHeightPx = with(density) { menuHeight.toPx() }
    val transformOrigin = remember(menuWidthPx, menuHeightPx, originInMenu) {
        TransformOrigin(
            pivotFractionX = if (menuWidthPx > 0f) (originInMenu.x / menuWidthPx).coerceIn(0f, 1f) else 0.5f,
            pivotFractionY = if (menuHeightPx > 0f) (originInMenu.y / menuHeightPx).coerceIn(0f, 1f) else 0.5f,
        )
    }
    val scale = remember { Animatable(ActionMenuRevealHiddenScale) }
    val alpha = remember { Animatable(0f) }
    var rendered by remember { mutableStateOf(false) }

    LaunchedEffect(visible) {
        if (visible) {
            rendered = true
            alpha.snapTo(0f)
            scale.snapTo(ActionMenuRevealHiddenScale)
            launch { alpha.animateTo(1f, tween(durationMillis = 140)) }
            scale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessMedium,
                ),
            )
        } else if (rendered) {
            launch { alpha.animateTo(0f, tween(durationMillis = 100)) }
            scale.animateTo(
                targetValue = ActionMenuRevealHiddenScale,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMediumLow,
                ),
            )
            rendered = false
            onExitComplete()
        }
    }

    if (rendered) {
        Box(
            modifier
                .graphicsLayer {
                    scaleX = scale.value
                    scaleY = scale.value
                    this.alpha = alpha.value
                    this.transformOrigin = transformOrigin
                },
        ) {
            content()
        }
    }
}

private const val ActionMenuRevealHiddenScale = 0.02f
