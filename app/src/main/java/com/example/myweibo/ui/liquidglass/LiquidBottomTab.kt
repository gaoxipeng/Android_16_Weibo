package com.example.myweibo.ui.liquidglass

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.IntState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

internal val LocalDockPressedTab = staticCompositionLocalOf<IntState?> { null }

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RowScope.LiquidBottomTab(
    onClick: () -> Unit,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onLongClick: (() -> Unit)? = null,
    onDoubleClick: (() -> Unit)? = null,
    dockIndex: Int = -1,
    content: @Composable ColumnScope.() -> Unit
) {
    val drivenPress = LocalDockPressedTab.current
    val localPress = remember { mutableStateOf(false) }
    val onPressedChange = remember { { pressed: Boolean -> localPress.value = pressed } }
    val motion = animateDockPress(dockIndex >= 0 && (drivenPress?.let { it.intValue == dockIndex } ?: localPress.value))
    val pressObserver = if (dockIndex >= 0 && drivenPress == null) Modifier.observeDockPress(onPressedChange) else Modifier
    val action = if (onLongClick == null && onDoubleClick == null) {
        Modifier.selectable(
            selected = selected,
            interactionSource = null,
            indication = null,
            role = Role.Tab,
            onClick = onClick,
        )
    } else {
        Modifier
            .semantics { this.selected = selected }
            .combinedClickable(
                interactionSource = null,
                indication = null,
                role = Role.Tab,
                onClick = onClick,
                onLongClick = onLongClick,
                onDoubleClick = onDoubleClick,
            )
    }
    Box(modifier.fillMaxHeight().weight(1f).clip(RoundedCornerShape(percent = 50)).then(pressObserver).then(action)) {
        Column(
            Modifier.matchParentSize().dockPressScale(motion),
            verticalArrangement = Arrangement.spacedBy(0.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
            content = content,
        )
    }
}
