package com.example.myweibo.ui

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.example.myweibo.R
import com.example.myweibo.data.TimelineKind
import com.example.myweibo.ui.liquidglass.LiquidBottomTab
import com.example.myweibo.ui.liquidglass.LiquidBottomTabs
import com.example.myweibo.ui.liquidglass.rememberLiquidBottomTabsGestureController
import com.kyant.backdrop.Backdrop
import kotlin.math.roundToInt

@Composable
internal fun WeiboLiquidBottomBar(
    selectedTab: MainTab,
    onTabChange: (MainTab) -> Unit,
    backdrop: Backdrop,
    timelineMenuExpanded: Boolean,
    onTimelineMenuExpandedChange: (Boolean) -> Unit,
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
    val barHeight = 64.dp
    val tabs = MainTab.entries
    val selectedIndex = tabs.indexOf(selectedTab).coerceAtLeast(0)
    val feedIndex = tabs.indexOf(MainTab.Feed).coerceAtLeast(0)
    val tabsGestureController = rememberLiquidBottomTabsGestureController()

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 18.dp, top = 4.dp, end = 18.dp, bottom = 16.dp)
            .navigationBarsPadding(),
        contentAlignment = Alignment.BottomStart,
    ) {
        val fullBarWidth = maxWidth

        Box(modifier = Modifier.width(fullBarWidth).height(barHeight)) {
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
                            modifier = Modifier.size(36.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            WeiboTabIcon(tab = tab, color = tabContentColor, size = 22.dp)
                        }
                    }
                }
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
        val menuLift = barHeight + timelineMenuGap
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
