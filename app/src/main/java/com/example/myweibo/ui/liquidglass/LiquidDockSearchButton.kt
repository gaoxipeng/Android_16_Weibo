package com.example.myweibo.ui.liquidglass

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.example.myweibo.ui.drawDockEndcapSurface
import com.example.myweibo.ui.theme.isAppLightTheme
import com.kyant.backdrop.Backdrop

/** A separate glass surface and input target; never shares the capsule's drag gestures. */
@Composable
internal fun LiquidDockSearchButton(
    selected: Boolean,
    onClick: () -> Unit,
    backdrop: Backdrop,
    frostAmount: Float,
    glassAmount: Float,
    forceDarkSurface: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    val isLightTheme = isAppLightTheme()
    val interactionSource = remember { MutableInteractionSource() }
    val interactionPressed by interactionSource.collectIsPressedAsState()
    val touchPressed = remember { mutableStateOf(false) }
    val onPressedChange = remember { { pressed: Boolean -> touchPressed.value = pressed } }
    val pressProgress = animateDockPress(touchPressed.value || interactionPressed)
    val selectionAlpha by animateFloatAsState(
        targetValue = if (selected) 0.14f else 0f,
        animationSpec = dockSelectionTween(),
        label = "dock-search-selection",
    )
    val selectionColor = if (isLightTheme && !forceDarkSurface) Color.Black else Color.White

    Box(
        modifier.observeDockPress(onPressedChange).selectable(
            selected = selected,
            interactionSource = interactionSource,
            indication = null,
            role = Role.Tab,
            onClick = onClick,
        ),
        contentAlignment = Alignment.Center,
    ) {
        Box(Modifier.matchParentSize().dockPressScale(pressProgress), contentAlignment = Alignment.Center) {
            Box(
                Modifier.matchParentSize()
                    .drawDockEndcapSurface(backdrop, frostAmount, glassAmount, isLightTheme),
            )
            Box(
                Modifier.matchParentSize().padding(4.dp).drawWithContent {
                    val corner = CornerRadius(size.minDimension / 2f)
                    drawRoundRect(selectionColor.copy(alpha = selectionAlpha), cornerRadius = corner)
                    drawRoundRect(Color.White.copy(alpha = 0.12f * pressProgress.value), cornerRadius = corner)
                    drawContent()
                },
            )
            content()
        }
    }
}
