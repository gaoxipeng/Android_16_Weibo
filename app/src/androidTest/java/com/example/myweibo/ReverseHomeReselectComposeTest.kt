package com.example.myweibo

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.click
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.myweibo.data.FeedItem
import com.example.myweibo.data.ReadingPreferences
import com.example.myweibo.data.ReadingPreferencesStore
import com.example.myweibo.data.TimelineCacheStore
import com.example.myweibo.data.TimelineKind
import com.example.myweibo.data.TimelinePage
import com.example.myweibo.ui.FollowFeedScreen
import com.example.myweibo.ui.HomeTimelineReadingState
import com.example.myweibo.ui.MainTab
import com.example.myweibo.ui.WeiboLiquidBottomBar
import com.kyant.backdrop.backdrops.emptyBackdrop
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Real feed rows and liquid tab gestures with local data; no production account or network. */
@RunWith(AndroidJUnit4::class)
class ReverseHomeReselectComposeTest {
    @get:Rule val compose = createComposeRule()

    @Test fun selectedHomeTouchJumpsToOldHeadAtZeroOffsetWithStaticMarkerAtTheLimit() {
        val fixture = mount()
        fixture.publish(newCount = 2)
        fixture.scrollTo(index = 12, offset = 19)

        home().assertIsSelected().performTouchInput { click() }
        fixture.assertAtOldHead(index = 3) // Two new posts, then the static marker.

        fixture.scrollTo(index = 2)
        val marker = label(R.string.feed_reverse_browse_new_posts_marker)
        compose.onNodeWithText(marker).assertIsDisplayed().assert(!hasClickAction())
        compose.runOnIdle {
            assertEquals("以上是新微博", marker)
            assertEquals(post(24).id, fixture.timeline.reverseBrowsePreviousHeadId)
            assertEquals((26 downTo 1).map(::post), fixture.timeline.items)
            assertEquals(0, fixture.refreshes)
        }
    }

    @Test fun foldedDividerTogglesCachedRowsAndBothProjectionsJumpToTheSameOldHead() {
        val fixture = mount()
        fixture.publish(newCount = 4)
        fixture.scrollTo(index = 12, offset = 19)
        home().performClick() // Accessibility activation uses the same selected-tab behavior.
        fixture.assertAtOldHead(index = 3)

        fixture.scrollTo(index = 2)
        val folded = compose.onNodeWithText(label(R.string.feed_reverse_browse_gap, 2))
        val expanded = compose.onNodeWithText(label(R.string.feed_reverse_browse_gap_open, 2))
        compose.onNodeWithText(label(R.string.feed_reverse_browse_new_posts_marker)).assertDoesNotExist()
        folded.assertIsDisplayed().performClick()
        expanded.assertIsDisplayed()
        compose.runOnIdle {
            assertEquals((28 downTo 1).map(::post), fixture.timeline.feedProjection.visibleItems)
            assertEquals(0, fixture.refreshes)
            assertEquals(0, fixture.pages)
        }

        fixture.scrollTo(index = 12, offset = 19)
        home().performTouchInput { click() }
        fixture.assertAtOldHead(index = 5)
        fixture.scrollTo(index = 2)
        expanded.performClick()
        folded.assertIsDisplayed()
        compose.runOnIdle {
            assertEquals((28 downTo 27).map(::post) + (24 downTo 1).map(::post), fixture.timeline.feedProjection.visibleItems)
            assertEquals((28 downTo 1).map(::post), fixture.timeline.items)
            assertEquals(post(24).id, fixture.timeline.reverseBrowsePreviousHeadId)
            assertFalse(fixture.timeline.reverseBrowseGapExpanded)
            assertEquals(0, fixture.refreshes)
            assertEquals(0, fixture.pages)
        }
    }

    @Test fun switchingBackOrLongPressingDoesNotReselectAndOnlyMissingBoundaryRefreshes() {
        val fixture = mount()
        compose.onNodeWithContentDescription(MainTab.Mine.label).performTouchInput { click() }
        home().performTouchInput { click() } // HOME was not selected: only switch tabs.
        home().assertIsSelected().performTouchInput { longClick() }
        compose.runOnIdle {
            assertEquals(1, fixture.menuOpens)
            assertEquals(0, fixture.refreshes)
        }
        home().performTouchInput { click() } // Verified feed, but no protected refresh yet.
        compose.runOnIdle { assertEquals(1, fixture.refreshes) }

        fixture.publish(newCount = 0)
        fixture.scrollTo(index = 10, offset = 19)
        home().performTouchInput { click() }
        fixture.assertAtOldHead(index = 0)
        compose.onNodeWithText(label(R.string.feed_reverse_browse_new_posts_marker)).assertDoesNotExist()
        compose.runOnIdle {
            assertEquals(1, fixture.refreshes)
            fixture.timeline.resetTimelineReadingSession()
        }
        home().performClick()
        compose.runOnIdle { assertEquals(2, fixture.refreshes) }
    }

    @Test fun reverseReadingDisabledKeepsNormalHomeRefresh() {
        val fixture = mount(reverseReading = false)
        fixture.publish(newCount = 2)
        home().performTouchInput { click() }
        compose.onNodeWithText(label(R.string.feed_reverse_browse_new_posts_marker)).assertDoesNotExist()
        compose.runOnIdle { assertEquals(1, fixture.refreshes) }
    }

    private fun mount(reverseReading: Boolean = true): Fixture {
        val fixture = Fixture()
        compose.setContent {
            val context = LocalContext.current
            val listState = rememberLazyListState()
            val timeline = remember {
                HomeTimelineReadingState(
                    ReadingPreferencesStore(context), TimelineCacheStore(context), TimelineKind.Following,
                    ReadingPreferences(reverseReading = reverseReading, newPostLimit = 2),
                ).apply {
                    feedCacheLoaded = true
                    timelineVerifiedFromNetwork = true
                    items = (24 downTo 1).map(::post)
                }
            }
            var selectedTab by remember { mutableStateOf(MainTab.Feed) }
            SideEffect { fixture.timeline = timeline; fixture.listState = listState }
            MaterialTheme {
                Box(Modifier.fillMaxSize()) {
                    FollowFeedScreen(
                        listState = listState,
                        homeTimeline = timeline,
                        hasLoginCookie = true,
                        onRefresh = { fixture.refreshes++ },
                        onLoadMore = { fixture.pages++ },
                        onOpenLoginSettings = {},
                        onItemClick = { _, _ -> },
                        onCommentClick = { _, _ -> },
                        onCommentLongClick = {},
                        onRepostClick = { _, _ -> },
                        onMediaClick = { _, _ -> },
                    )
                    WeiboLiquidBottomBar(
                        selectedTab = selectedTab,
                        backdrop = emptyBackdrop(),
                        timelineMenuExpanded = false,
                        onTimelineMenuExpandedChange = { if (it) fixture.menuOpens++ },
                        timelineMenuContent = {},
                        onTabChange = { tab ->
                            if (tab == MainTab.Feed && tab == selectedTab) {
                                timeline.onHomeTabReselected(listState) { fixture.refreshes++ }
                            } else {
                                selectedTab = tab
                            }
                        },
                        modifier = Modifier.align(Alignment.BottomCenter),
                    )
                }
            }
        }
        return fixture
    }

    private fun home() = compose.onNodeWithContentDescription(MainTab.Feed.label)

    private fun label(resource: Int, vararg args: Any): String =
        InstrumentationRegistry.getInstrumentation().targetContext.getString(resource, *args)

    private inner class Fixture {
        lateinit var timeline: HomeTimelineReadingState
        lateinit var listState: LazyListState
        var refreshes = 0
        var pages = 0
        var menuOpens = 0

        fun publish(newCount: Int) = compose.runOnIdle {
            val protect = timeline.preferences.reverseReading
            timeline.publishRefresh(TimelinePage((24 + newCount downTo 24).map(::post)), listState, protect, protect)
        }

        fun scrollTo(index: Int, offset: Int = 0) = compose.runOnIdle {
            listState.requestScrollToItem(index, offset)
        }

        fun assertAtOldHead(index: Int) = compose.runOnIdle {
            assertEquals(index, listState.firstVisibleItemIndex)
            assertEquals(0, listState.firstVisibleItemScrollOffset)
            val head = listState.layoutInfo.visibleItemsInfo.first { it.index == index }
            assertEquals(post(24).id, head.key)
            assertEquals(0, head.offset)
        }
    }

    private fun post(number: Int) = FeedItem(
        id = "reverse-post-$number",
        statusId = "reverse-status-$number",
        authorId = "fixture-author",
        authorName = "Fixture author",
        authorAvatarUrl = null,
        createdAt = (1_700_000_000L + number).toString(),
        source = null,
        ipLocation = null,
        text = if (number > 24) "New post $number" else "Earlier post $number",
        repostsCount = "0",
        commentsCount = "0",
        likesCount = "0",
        images = emptyList(),
    )
}
