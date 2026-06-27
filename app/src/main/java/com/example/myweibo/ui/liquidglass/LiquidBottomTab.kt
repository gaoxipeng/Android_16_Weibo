package com.example.myweibo.ui.liquidglass

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.util.lerp
import kotlin.math.max
import kotlin.math.min

internal fun liquidBottomTabIndicatorOverlap(
    indicatorValue: Float,
    tabIndex: Int,
    indicatorScaleX: Float = 1f,
): Float {
    val capsuleCenter = indicatorValue + 0.5f
    val halfWidth = 0.5f * indicatorScaleX.coerceAtLeast(1f)
    val capsuleStart = capsuleCenter - halfWidth
    val capsuleEnd = capsuleCenter + halfWidth
    val tabStart = tabIndex.toFloat()
    val tabEnd = tabIndex + 1f
    val overlapStart = max(capsuleStart, tabStart)
    val overlapEnd = min(capsuleEnd, tabEnd)
    return (overlapEnd - overlapStart).coerceAtLeast(0f)
}

internal fun liquidBottomTabVisualScale(
    pressProgress: Float,
    indicatorScaleX: Float,
): Float =
    1f + (indicatorScaleX.coerceAtLeast(1f) - 1f) * pressProgress.coerceIn(0f, 1f)

internal fun liquidBottomTabMainRowAlpha(
    pressProgress: Float,
    indicatorValue: Float,
    tabIndex: Int,
    indicatorScaleX: Float = 1f,
): Float {
    if (pressProgress <= 0.01f) return 1f
    val visualScale = liquidBottomTabVisualScale(pressProgress, indicatorScaleX)
    val overlap = liquidBottomTabIndicatorOverlap(
        indicatorValue = indicatorValue,
        tabIndex = tabIndex,
        indicatorScaleX = visualScale,
    ).coerceIn(0f, 1f)
    if (overlap <= 0.02f) return 1f
    val concealed = overlap * pressProgress.coerceIn(0f, 1f)
    return if (overlap >= 0.9f) {
        // 正下方 Tab：与透镜放大进度同步，盖住后再淡出
        when {
            concealed < 0.72f -> 1f
            concealed >= 0.98f -> 0f
            else -> 1f - (concealed - 0.72f) / 0.26f
        }
    } else if (overlap >= 0.25f) {
        // 放大后胶囊边缘扫到的邻位 Tab（消息/写微博旁的首页、我的），提前隐藏防重影
        when {
            concealed <= 0.08f -> 1f
            concealed >= 0.55f -> 0f
            else -> 1f - (concealed - 0.08f) / 0.47f
        }
    } else {
        1f
    }
}

internal fun liquidBottomTabBackdropRowAlpha(
    pressProgress: Float,
    indicatorValue: Float,
    tabIndex: Int,
    indicatorScaleX: Float = 1f,
): Float {
    if (pressProgress <= 0.01f) return 1f
    val visualScale = liquidBottomTabVisualScale(pressProgress, indicatorScaleX)
    val overlap = liquidBottomTabIndicatorOverlap(
        indicatorValue = indicatorValue,
        tabIndex = tabIndex,
        indicatorScaleX = visualScale,
    )
    val concealed = overlap * pressProgress.coerceIn(0f, 1f)
    // 仅正下方 Tab 写入采样层，避免邻位主题色被透镜放大
    return if (overlap >= 0.9f && concealed > 0.5f) 1f else 0f
}

internal val LocalLiquidBottomTabScale =
    staticCompositionLocalOf { { 1f } }

internal val LocalLiquidBottomTabBackdropRow =
    staticCompositionLocalOf { false }

internal val LocalLiquidBottomTabIndicatorIndex =
    staticCompositionLocalOf { -1 }

internal val LocalLiquidBottomTabIndicatorValue =
    staticCompositionLocalOf { 0f }

internal val LocalLiquidBottomTabIndicatorScaleX =
    staticCompositionLocalOf { 1f }

internal val LocalLiquidBottomTabGlassMotionProgress =
    staticCompositionLocalOf { 0f }

internal val LocalLiquidBottomTabPressProgress =
    staticCompositionLocalOf { 0f }

internal val LocalLiquidBottomTabTabIndex =
    staticCompositionLocalOf { 0 }

@Composable
fun RowScope.LiquidBottomTab(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val scale = LocalLiquidBottomTabScale.current
    val isBackdropRow = LocalLiquidBottomTabBackdropRow.current
    val pressProgress = LocalLiquidBottomTabPressProgress.current
    val indicatorValue = LocalLiquidBottomTabIndicatorValue.current
    val indicatorScaleX = LocalLiquidBottomTabIndicatorScaleX.current
    val tabIndex = LocalLiquidBottomTabTabIndex.current
    Column(
        modifier
            .clip(RoundedCornerShape(percent = 50))
            .clickable(
                interactionSource = null,
                indication = null,
                role = Role.Tab,
                onClick = onClick
            )
            .fillMaxHeight()
            .weight(1f)
            .graphicsLayer {
                val resolvedScale = if (isBackdropRow) {
                    val visualScale = liquidBottomTabVisualScale(pressProgress, indicatorScaleX)
                    val overlap = liquidBottomTabIndicatorOverlap(
                        indicatorValue = indicatorValue,
                        tabIndex = tabIndex,
                        indicatorScaleX = visualScale,
                    )
                    lerp(1f, 1.2f, pressProgress * overlap)
                } else {
                    scale()
                }
                scaleX = resolvedScale
                scaleY = resolvedScale
            },
        verticalArrangement = Arrangement.spacedBy(2f.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
        content = content
    )
}
