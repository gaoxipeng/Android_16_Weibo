package com.example.myweibo

import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionOnScreen
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.click
import androidx.compose.ui.test.doubleClick
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.hasScrollToIndexAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipe
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.myweibo.data.FeedItem
import com.example.myweibo.data.FeedMedia
import com.example.myweibo.data.MediaType
import com.example.myweibo.data.ReadingPreferences
import com.example.myweibo.data.ReadingPreferencesStore
import com.example.myweibo.data.TimelineCacheStore
import com.example.myweibo.data.TimelineKind
import com.example.myweibo.data.TimelinePage
import com.example.myweibo.data.WeiboJsonParser
import com.example.myweibo.ui.ExplicitTimelineRefreshBaseline
import com.example.myweibo.ui.FollowFeedScreen
import com.example.myweibo.ui.HomeTimelineReadingState
import com.example.myweibo.ui.HomeRefreshTrigger
import com.example.myweibo.ui.HomeRefreshPublication
import com.example.myweibo.ui.MainTab
import com.example.myweibo.ui.WeiboLiquidBottomBar
import com.example.myweibo.ui.goToTimelineTop
import com.example.myweibo.ui.goToTimelineTopAndRefresh
import com.kyant.backdrop.backdrops.emptyBackdrop
import java.util.concurrent.atomic.AtomicInteger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Real feed rows and liquid tab gestures with local data; no production account or network. */
@RunWith(AndroidJUnit4::class)
class ReverseHomeReselectComposeTest {
    @get:Rule val compose = createComposeRule()

    @Test fun selectedHomeSingleJumpsToTopWithoutRefreshAndKeepsStaticMarker() {
        val fixture = mount()
        fixture.publish(newCount = 2)
        fixture.scrollTo(index = 12, offset = 19)

        fixture.tapSelectedHome()
        fixture.assertAtRow(index = 0, postNumber = 26)

        fixture.scrollTo(index = 2)
        val marker = label(R.string.feed_reverse_browse_new_posts_marker)
        compose.onNodeWithText(marker).assertIsDisplayed().assert(!hasClickAction())
        compose.runOnIdle {
            assertEquals("以上是新微博", marker)
            assertEquals((26 downTo 1).map(::post), fixture.timeline.items)
            assertEquals(0, fixture.refreshes)
        }
    }

    @Test fun openedFoldStaysOpenAfterSelectedSingleJumpsToTop() {
        val fixture = mount()
        fixture.publish(newCount = 4)
        fixture.scrollTo(index = 2)
        val folded = compose.onNodeWithText(label(R.string.feed_reverse_browse_gap, 2))
        compose.onNodeWithText(label(R.string.feed_reverse_browse_new_posts_marker)).assertDoesNotExist()
        folded.assertIsDisplayed().performClick()
        folded.assertDoesNotExist()
        compose.runOnIdle {
            assertTrue(fixture.timeline.reverseBrowseGapExpanded)
            assertNull(fixture.timeline.feedProjection.dividerIndex)
            assertEquals((28 downTo 1).map(::post), fixture.timeline.feedProjection.visibleItems)
            assertEquals(0, fixture.refreshes)
            assertEquals(0, fixture.pages)
        }

        fixture.scrollTo(index = 12, offset = 19)
        fixture.tapSelectedHome()
        fixture.assertAtRow(index = 0, postNumber = 28)
        fixture.scrollTo(index = 0)
        folded.assertDoesNotExist()
        compose.runOnIdle {
            assertEquals((28 downTo 1).map(::post), fixture.timeline.feedProjection.visibleItems)
            assertEquals((28 downTo 1).map(::post), fixture.timeline.items)
            assertTrue(fixture.timeline.reverseBrowseGapExpanded)
            assertNull(fixture.timeline.feedProjection.dividerIndex)
            assertEquals(0, fixture.refreshes)
            assertEquals(0, fixture.pages)
        }
    }

    @Test fun openingFourFoldedPostsKeepsVisibleOldVideoAtSameScreenY() {
        val coverPackage = InstrumentationRegistry.getInstrumentation().targetContext.packageName
        val fixture = mount(row = { number -> fourFoldedPostsFixture(number, coverPackage) })
        fixture.publish(newCount = 6) // Keep 30 and 29 visible; fold 28 through 25 before old head 24.
        val folded = compose.onNodeWithText(label(R.string.feed_reverse_browse_gap, 4))
        val oldVideo = compose.onNodeWithText("Old post 24: portrait video")
        val oldestHidden = compose.onNodeWithText("New post 25: oldest folded reply", substring = true)

        fun openFromMidScreen() {
            fixture.positionFoldUnderPartiallyVisiblePost()
            folded.assertIsDisplayed()
            oldVideo.assertIsDisplayed()
            val oldScreenY = compose.runOnIdle {
                val layout = fixture.listState.layoutInfo
                val projection = fixture.timeline.feedProjection
                assertEquals(4, projection.gap?.hiddenPostCount)
                val divider = layout.visibleItemsInfo.first { it.index == projection.dividerIndex }
                val newer = layout.visibleItemsInfo.first { it.key == post(29).id }
                val old = layout.visibleItemsInfo.first { it.key == post(24).id }
                val newerTop = newer.offset + layout.beforeContentPadding
                val dividerCenter = divider.offset + layout.beforeContentPadding + divider.size / 2
                val viewportHeight = layout.viewportSize.height
                assertEquals(newer.index, fixture.listState.firstVisibleItemIndex)
                assertTrue("Newer card must straddle the viewport top", newerTop < 0 && newerTop + newer.size > 0)
                assertTrue("Fold divider must be mid-screen", dividerCenter in viewportHeight / 3..viewportHeight * 2 / 3)
                assertEquals(divider.index + 1, old.index)
                assertEquals(divider.offset + divider.size, old.offset)
                assertTrue("Fixture must use a tall old card", old.size > divider.size * 3)
                assertTrue((25..28).none { number -> projection.visibleItems.any { it.id == post(number).id } })
                assertTrue("Feed screen origin was not measured", fixture.feedTopOnScreen.isFinite())
                fixture.feedTopOnScreen + layout.beforeContentPadding + old.offset
            }
            folded.performTouchInput { click() }
            oldVideo.assertIsDisplayed()
            compose.runOnIdle {
                assertTrue(fixture.timeline.reverseBrowseGapExpanded)
                assertNull(fixture.timeline.feedProjection.dividerIndex)
                val layout = fixture.listState.layoutInfo
                val old = layout.visibleItemsInfo.first { it.key == post(24).id }
                assertEquals(
                    "Opening the fold must not move the already-visible old video card on screen",
                    oldScreenY, fixture.feedTopOnScreen + layout.beforeContentPadding + old.offset, 0.5f,
                )
                val oldest = layout.visibleItemsInfo.first { it.key == post(25).id }
                assertEquals(old.index - 1, oldest.index)
                assertTrue("Oldest folded card must be visible immediately above old head", oldest.offset < old.offset)
                assertEquals(old.offset, oldest.offset + oldest.size)
                assertEquals((30 downTo 1).map { post(it).id }, fixture.timeline.feedProjection.visibleItems.map { it.id })
                assertEquals(0, fixture.refreshes)
                assertEquals(0, fixture.pages)
            }
            oldestHidden.assertIsDisplayed()
        }

        openFromMidScreen()
        fixture.scrollTo(index = 0)
        folded.assertDoesNotExist()
    }

    @Test fun selectedDoubleLandsOnOldTopBeforeOneRefreshAndKeepsItAfterPrepend() {
        val fixture = mount(newPostLimit = 1)
        fixture.publish(newCount = 2)
        fixture.scrollTo(index = 12, offset = 19)
        home().assertIsSelected().performTouchInput { doubleClick() }
        fixture.assertAtRow(index = 0, postNumber = 26)
        compose.runOnIdle {
            assertEquals(1, fixture.refreshes)
            // Recorded inside the request callback, before delivering any response.
            assertEquals(post(26).id to 0, fixture.homeRefreshStart)
            assertTrue(fixture.timeline.isLoading)
            assertEquals(1, fixture.timeline.feedProjection.gap?.hiddenPostCount)
            assertEquals((26 downTo 1).map(::post), fixture.timeline.items)
        }

        fixture.finishHomeRefresh(Result.success(TimelinePage((29 downTo 26).map(::post))))
        // Lazy rows are 29, divider, 26, ... (28 and 27 are hidden by the fold).
        fixture.assertAtRow(index = 2, postNumber = 26)
        compose.runOnIdle {
            assertEquals(1, fixture.refreshes)
            assertTrue(fixture.listState.canScrollBackward)
            assertEquals(2, fixture.timeline.feedProjection.gap?.hiddenPostCount)
            assertEquals(1, fixture.timeline.feedProjection.dividerIndex)
            assertEquals((29 downTo 1).map(::post), fixture.timeline.items)
            assertNull(fixture.timeline.feedRefreshHint)
        }

        val feed = compose.onNode(hasScrollToIndexAction())
        feed.performScrollToIndex(0)
        fixture.assertAtRow(index = 0, postNumber = 29)
        compose.onNodeWithText(post(29).text).assertIsDisplayed()
        feed.performScrollToIndex(1)
        compose.onNodeWithText(label(R.string.feed_reverse_browse_gap, 2)).assertIsDisplayed()
        compose.runOnIdle { assertEquals(1, fixture.refreshes) }
    }

    @Test fun selectedDoubleAtMeasuredHeadDoesNotRequestAnotherJump() {
        val fixture = mount()
        fixture.assertAtRow(index = 0, postNumber = 24)
        home().assertIsSelected().performTouchInput { doubleClick() }
        compose.runOnIdle {
            assertEquals(post(24).id to 0, fixture.homeRefreshStart)
            assertEquals(1, fixture.refreshes)
            // A real top jump suppresses layout paging; staying on the measured head does not.
            assertFalse(fixture.timeline.longPostController(fixture.listState).isLayoutPagingSuppressed)
        }
        fixture.finishHomeRefresh(Result.success(TimelinePage((25 downTo 24).map(::post))))
        fixture.assertAtRow(index = 2, postNumber = 24) // One new post and the static marker.
    }

    @Test fun homeJumpToUnmeasuredListTimesOutWithoutRefreshing() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val refreshes = AtomicInteger()
        withTimeout(2_000L) {
            withContext(Dispatchers.Main) {
                val fixture = Fixture(::post, ReadingPreferences())
                val timeline = fixture.openTimeline(context, TimelineKind.Following, listOf(post(24)))
                // A detached list cannot lay out the requested head. The child must finish
                // without a refresh before the enclosing test timeout.
                timeline.goToTimelineTopAndRefresh(LazyListState(), this) { refreshes.incrementAndGet() }
            }
        }
        assertEquals(0, refreshes.get())
    }

    @Test fun displacedSelectedFeedDoubleKeepsOldTopWhenFoldingIsDisabled() {
        val fixture = mount(reverseReading = false)
        fixture.scrollTo(index = 10, offset = 19)
        home().assertIsSelected().performTouchInput {
            click(center - Offset(width * 0.12f, 0f))
            advanceEventTime(100)
            click(center + Offset(width * 0.12f, 0f))
        }
        fixture.assertAtRow(index = 0, postNumber = 24)
        compose.runOnIdle {
            assertEquals(post(24).id to 0, fixture.homeRefreshStart)
            assertEquals(1, fixture.refreshes)
        }
        fixture.finishHomeRefresh(Result.success(TimelinePage((27 downTo 24).map(::post))))
        fixture.assertAtRow(index = 3, postNumber = 24)
        compose.runOnIdle {
            assertNull(fixture.timeline.feedProjection.dividerIndex)
            assertEquals((27 downTo 1).map(::post), fixture.timeline.feedProjection.visibleItems)
            assertEquals(1, fixture.refreshes)
        }
    }

    @Test fun selectedDoubleDoesNotOverrideScrollingWhileRefreshIsPending() {
        val fixture = mount()
        fixture.scrollTo(index = 12, offset = 19)
        home().assertIsSelected().performTouchInput { doubleClick() }
        fixture.assertAtRow(index = 0, postNumber = 24)
        fixture.scrollTo(index = 12, offset = 15)
        val readingPosition = fixture.firstVisibleRow()
        fixture.finishHomeRefresh(Result.success(TimelinePage((27 downTo 24).map(::post))))
        assertEquals(readingPosition, fixture.firstVisibleRow())
        compose.runOnIdle { assertEquals(1, fixture.refreshes) }
    }

    @Test fun confirmedZeroAtTopShowsNoNewHintAndClearsFoldWithoutRemovingHistory() {
        val fixture = mount(showResultHint = true, newPostLimit = 1)
        fixture.publish(newCount = 2)
        fixture.scrollTo(index = 12, offset = 19)
        home().assertIsSelected().performTouchInput { doubleClick() }
        fixture.finishHomeRefresh(Result.success(TimelinePage((26 downTo 24).map(::post))))
        fixture.assertAtRow(index = 0, postNumber = 26)
        compose.onNodeWithText("没有新微博了").assertIsDisplayed()
        compose.runOnIdle {
            assertEquals((26 downTo 1).map(::post), fixture.timeline.items)
            assertTrue(requireNotNull(fixture.timeline.confirmedRefresh).newPosts.isEmpty())
            assertNull(fixture.timeline.feedProjection.dividerIndex)
            assertEquals(1, fixture.refreshes)
        }
    }

    @Test fun unsuccessfulAndUnconfirmedExplicitRefreshesKeepPreviousFoldAndNeverHintNoNew() {
        val fixture = mount(newPostLimit = 1)
        fixture.publish(newCount = 2)
        val cases = listOf(
            "network failure" to Result.failure<TimelinePage>(IllegalStateException("fixture offline")),
            "empty success" to Result.success(TimelinePage(emptyList())),
            "reordered response" to Result.success(TimelinePage(listOf(post(26), post(24), post(25)))),
            "disjoint response" to Result.success(TimelinePage(listOf(post(28), post(27)))),
            "repeat disjoint response" to Result.success(TimelinePage(listOf(post(28), post(27)))),
        )
        val previousBatch = compose.runOnIdle { fixture.timeline.confirmedRefresh }
        cases.forEach { (scenario, result) ->
            fixture.scrollTo(index = 10, offset = 19)
            home().assertIsSelected().performTouchInput { doubleClick() }
            val anchoredRow = fixture.firstVisibleRow()
            fixture.finishHomeRefresh(result)
            assertEquals(scenario, anchoredRow, fixture.firstVisibleRow())
            compose.runOnIdle {
                assertNull(scenario, fixture.timeline.feedRefreshHint)
                assertEquals(scenario, previousBatch, fixture.timeline.confirmedRefresh)
                assertEquals(scenario, 1, fixture.timeline.feedProjection.gap?.hiddenPostCount)
                assertTrue(scenario, fixture.timeline.items.containsAll((26 downTo 1).map(::post)))
            }
        }
        compose.runOnIdle { assertEquals(cases.size, fixture.refreshes) }
    }

    @Test fun aResponseFromAnEarlierVisitCannotReplaceTheCurrentFold() {
        val fixture = mount(newPostLimit = 1)
        fixture.publish(newCount = 2)
        compose.runOnIdle {
            val timeline = fixture.timeline
            val previous = timeline.confirmedRefresh
            val oldVisit = requireNotNull(timeline.captureExplicitRefreshBaseline(fixture.accountId))
            timeline.leaveExplicitRefreshVisit()
            val trigger = HomeRefreshTrigger.Pull
            val publication = requireNotNull(timeline.applyRefreshPage(
                TimelinePage(listOf(post(28), post(27), post(26))), fixture.listState, trigger,
                timeline.preservesRefreshViewport(trigger), fixture.accountId, timeline.displayedOrderedRoute,
                explicitBaseline = oldVisit,
            ))
            assertNull(publication.confirmedNewCount)
            assertEquals(previous, timeline.confirmedRefresh)
            assertEquals(1, timeline.feedProjection.gap?.hiddenPostCount)
        }
    }

    @Test fun automaticRefreshAndPaginationDoNotCreateOrReviveAFold() {
        val fixture = mount(newPostLimit = 1)
        fixture.deliverRefresh(Result.success(TimelinePage((26 downTo 24).map(::post))), explicit = false)
        compose.runOnIdle { assertNull(fixture.timeline.confirmedRefresh) }
        fixture.deliverRefresh(Result.success(TimelinePage((28 downTo 26).map(::post))), explicit = true)
        compose.runOnIdle { assertEquals(1, fixture.timeline.feedProjection.gap?.hiddenPostCount) }
        fixture.deliverRefresh(Result.success(TimelinePage((28 downTo 24).map(::post))), explicit = true)
        fixture.deliverRefresh(Result.success(TimelinePage((30 downTo 24).map(::post))), explicit = false)
        compose.runOnIdle {
            assertEquals(1, fixture.page(TimelinePage(listOf(post(2), post(1), post(0)))))
            assertEquals((30 downTo 0).map(::post), fixture.timeline.items)
            assertTrue(requireNotNull(fixture.timeline.confirmedRefresh).newPosts.isEmpty())
            assertNull(fixture.timeline.feedProjection.dividerIndex)
        }
    }

    @Test fun automaticOverlapCannotMakeALaterDisjointExplicitResponseClearAFold() {
        val fixture = mount(newPostLimit = 1)
        fixture.publish(newCount = 2)
        val previous = compose.runOnIdle { fixture.timeline.confirmedRefresh }
        fixture.deliverRefresh(Result.success(TimelinePage((28 downTo 27).map(::post))), explicit = false)
        fixture.deliverRefresh(Result.success(TimelinePage((28 downTo 26).map(::post))), explicit = false)
        fixture.deliverRefresh(Result.success(TimelinePage((28 downTo 27).map(::post))), explicit = true)
        compose.runOnIdle { assertEquals(previous, fixture.timeline.confirmedRefresh) }
        fixture.deliverRefresh(Result.success(TimelinePage((29 downTo 26).map(::post))), explicit = true)
        compose.runOnIdle {
            assertEquals(listOf(post(29).id), fixture.timeline.confirmedRefresh?.newPosts?.map { it.rowId })
            assertNull(fixture.timeline.feedProjection.gap)
        }
    }

    @Test fun otherTabsAndAnInitiallyUnselectedFeedDoNotRefresh() {
        val fixture = mount(initialTab = MainTab.Mine)
        MainTab.entries.forEach { tab ->
            compose.onNodeWithContentDescription(tab.label).assertIsDisplayed()
        }
        compose.onNodeWithContentDescription(MainTab.Mine.label).performTouchInput { doubleClick() }
        home().performTouchInput { doubleClick() }
        home().assertIsSelected()
        compose.runOnIdle { assertEquals(0, fixture.refreshes) }
    }

    @Test fun nonReverseAutomaticAndPullReplaceThePagingChainButDoubleTapDoesNotRewriteIt() = runBlocking {
        val fixture = mount(reverseReading = false)
        val timeline = fixture.timeline
        val raw = """{"statuses":[{"idstr":"26","text":"new"},{"idstr":"25","text":"old"}],"max_id":"server-page-2"}"""
        val first = WeiboJsonParser.parseTimeline(raw)
        assertEquals(2, first.items.size)
        val automatic = compose.runOnIdle {
            timeline.nextCursor = "old-tail"
            val publication = requireNotNull(fixture.apply(first, HomeRefreshTrigger.Automatic))
            assertFalse(publication.keptReading)
            assertFalse(publication.protectedViewport)
            assertEquals(first.items.map { it.id }, timeline.items.map { it.id })
            assertEquals("server-page-2", timeline.nextCursor)
            publication
        }
        withContext(Dispatchers.Main) {
            timeline.cacheFollowingReplacement(automatic, fixture.accountId, raw, timeline.timelineCacheSessionGeneration) { true }
        }
        val page2 = """{"statuses":[{"idstr":"24","text":"next"}],"max_id":"server-page-3"}"""
        compose.runOnIdle {
            assertEquals(1, fixture.page(WeiboJsonParser.parseTimeline(page2)))
            assertEquals("server-page-3", timeline.nextCursor)
        }
        timeline.cacheStore.appendFollowingTimeline(fixture.accountId, page2)
        val diskBeforeDouble = requireNotNull(timeline.cacheStore.readFollowingTimeline(fixture.accountId))
        assertEquals(listOf("26", "25", "24"), diskBeforeDouble.items.map { it.id })
        assertEquals("server-page-3", diskBeforeDouble.nextCursor)
        val newer = """{"statuses":[{"idstr":"27","text":"fresher"},{"idstr":"26","text":"new"},{"idstr":"25","text":"old"}],"max_id":"fresh-head-cursor"}"""
        val next = WeiboJsonParser.parseTimeline(newer)
        val explicit = compose.runOnIdle {
            val baseline = requireNotNull(timeline.captureExplicitRefreshBaseline(fixture.accountId))
            assertTrue(WeiboJsonParser.hasCompleteExplicitTimelineResponse(newer, next))
            val publication = requireNotNull(fixture.apply(next, HomeRefreshTrigger.SelectedFeedDoubleTap, baseline))
            assertTrue(publication.keptReading)
            assertEquals(1, publication.confirmedNewCount)
            assertEquals(listOf("27", "26", "25", "24"), timeline.items.map { it.id })
            assertEquals("server-page-3", timeline.nextCursor)
            publication
        }
        withContext(Dispatchers.Main) {
            timeline.cacheFollowingReplacement(explicit, fixture.accountId, newer, timeline.timelineCacheSessionGeneration) { true }
        }
        assertEquals(diskBeforeDouble, timeline.cacheStore.readFollowingTimeline(fixture.accountId))
        compose.runOnIdle {
            val pull = requireNotNull(fixture.apply(next, HomeRefreshTrigger.Pull))
            assertFalse(pull.keptReading)
            assertEquals(next.items.map { it.id }, timeline.items.map { it.id })
            assertEquals("fresh-head-cursor", timeline.nextCursor)
            assertNull(timeline.confirmedRefresh)
        }
    }

    @Test fun specialFollowingKeepsUnsortedNextPageAndCursorOutsideConfirmedFoldBasis() {
        val fixture = mount(kind = TimelineKind.SpecialFollowing, newPostLimit = 1)
        fixture.publish(newCount = 2)
        compose.runOnIdle {
            val timeline = fixture.timeline
            val batch = requireNotNull(timeline.confirmedRefresh)
            val tail = listOf(post(-1), post(0), post(-2))
            assertEquals(3, fixture.page(TimelinePage(tail, "original-next-cursor")))
            assertEquals(tail, timeline.items.takeLast(3))
            assertEquals(tail, timeline.feedProjection.visibleItems.takeLast(3))
            assertEquals("original-next-cursor", timeline.nextCursor)
            assertEquals(batch, timeline.confirmedRefresh)
            val baseline = requireNotNull(timeline.captureExplicitRefreshBaseline(fixture.accountId))
            assertEquals((26 downTo 1).map(::post), baseline.items)
            val publication = requireNotNull(fixture.apply(
                TimelinePage((29 downTo 24).map(::post), "refresh-cursor"), HomeRefreshTrigger.Pull, baseline,
            ))
            assertEquals(3, publication.confirmedNewCount)
            assertEquals(tail, timeline.items.takeLast(3))
            assertEquals("original-next-cursor", timeline.nextCursor)
        }
    }

    @Test fun specialFollowingExplicitUnsortedFirstPageReplacesTheFoldAndPagingChain() {
        val fixture = mount(kind = TimelineKind.SpecialFollowing, newPostLimit = 1)
        fixture.publish(newCount = 2)
        compose.runOnIdle {
            assertEquals(1, fixture.timeline.feedProjection.gap?.hiddenPostCount)
            fixture.timeline.nextCursor = "stale-tail-cursor"
        }
        home().performTouchInput { doubleClick() }
        val serverOrder = listOf(post(27), post(29), post(26), post(25))
        fixture.finishHomeRefresh(Result.success(TimelinePage(serverOrder, "unordered-server-cursor")))
        compose.runOnIdle {
            val timeline = fixture.timeline
            assertEquals(1, fixture.refreshes)
            assertEquals(serverOrder, timeline.items)
            assertEquals(serverOrder, timeline.feedProjection.visibleItems)
            assertEquals("unordered-server-cursor", timeline.nextCursor)
            assertNull(timeline.feedProjection.dividerIndex)
            assertNull(timeline.confirmedRefresh)
            assertNull(timeline.captureExplicitRefreshBaseline(fixture.accountId))
            assertNull(timeline.feedRefreshHint)
        }

        // A sorted first page can establish a fresh baseline, but cannot reuse the
        // old batch or the intervening ranked snapshot to classify a fold.
        val sorted = (31 downTo 25).map(::post)
        fixture.deliverRefresh(Result.success(TimelinePage(sorted, "sorted-server-cursor")), explicit = true)
        compose.runOnIdle {
            val timeline = fixture.timeline
            assertEquals(sorted, timeline.items)
            assertEquals("sorted-server-cursor", timeline.nextCursor)
            assertNull(timeline.confirmedRefresh)
            assertEquals(sorted, requireNotNull(timeline.captureExplicitRefreshBaseline(fixture.accountId)).items)
        }
    }

    @Test fun selectedFeedLongPressDragAndAccessibilityClickDoNotRefresh() {
        val fixture = mount()
        home().performClick()
        compose.runOnIdle { assertEquals(1, fixture.topJumps.get()) }
        home().performTouchInput { longClick() }
        compose.runOnIdle { assertEquals(1, fixture.menuOpens) }
        home().performTouchInput { swipe(center, center + Offset(width.toFloat(), 0f)) }
        compose.onNodeWithContentDescription(MainTab.Messages.label).assertIsSelected()
        compose.runOnIdle {
            assertEquals(1, fixture.topJumps.get())
            assertEquals(0, fixture.refreshes)
        }
    }

    private fun mount(
        reverseReading: Boolean = true,
        kind: TimelineKind = TimelineKind.Following,
        initialTab: MainTab = MainTab.Feed,
        newPostLimit: Int = 2,
        showResultHint: Boolean = false,
        row: (Int) -> FeedItem = ::post,
    ): Fixture {
        val readingPreferences = ReadingPreferences(reverseReading = reverseReading, newPostLimit = newPostLimit)
        val fixture = Fixture(row, readingPreferences)
        compose.setContent {
            val context = LocalContext.current
            val listState = rememberLazyListState()
            val scope = rememberCoroutineScope()
            val timeline = remember { fixture.openTimeline(context, kind, (24 downTo 1).map(row)) }
            var selectedTab by remember { mutableStateOf(initialTab) }
            SideEffect { fixture.timeline = timeline; fixture.listState = listState }
            MaterialTheme {
                Box(Modifier.fillMaxSize().onGloballyPositioned { fixture.feedTopOnScreen = it.positionOnScreen().y }) {
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
                        feedUiOnTop = selectedTab == MainTab.Feed,
                    )
                    // The app-level capsule lives outside FollowFeedScreen. Mirror the real result
                    // text here for the offline recording, not the refresh decision itself.
                    if (showResultHint) timeline.feedRefreshHint?.let { Text(it, Modifier.align(Alignment.TopCenter)) }
                    WeiboLiquidBottomBar(
                        selectedTab = selectedTab,
                        backdrop = emptyBackdrop(),
                        timelineMenuExpanded = false,
                        onTimelineMenuExpandedChange = { if (it) fixture.menuOpens++ },
                        timelineMenuContent = {},
                        onTabChange = { tab ->
                            if (tab == MainTab.Feed && selectedTab == tab) {
                                fixture.topJumps.incrementAndGet()
                                timeline.goToTimelineTop(listState)
                            } else {
                                if (selectedTab == MainTab.Feed) timeline.leaveExplicitRefreshVisit()
                                selectedTab = tab
                            }
                        },
                        selectedTimelineKind = timeline.timelineKind,
                        onSelectedFeedDoubleClick = {
                            timeline.goToTimelineTopAndRefresh(listState, scope, fixture::startHomeRefresh)
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

    private inner class Fixture(
        private val row: (Int) -> FeedItem,
        private val readingPreferences: ReadingPreferences,
    ) {
        lateinit var timeline: HomeTimelineReadingState
        lateinit var listState: LazyListState
        var feedTopOnScreen = Float.NaN
        var refreshes = 0
        val topJumps = AtomicInteger()
        var pages = 0
        var menuOpens = 0
        private var pendingHomeBaseline: ExplicitTimelineRefreshBaseline? = null
        var homeRefreshStart: Pair<Any, Int>? = null
            private set
        val accountId = (System.nanoTime() and Long.MAX_VALUE).toString()

        fun openTimeline(
            context: Context,
            kind: TimelineKind,
            rows: List<FeedItem>,
            ownerId: String = accountId,
            groupId: String? = null,
        ) = HomeTimelineReadingState(
            ReadingPreferencesStore(context), TimelineCacheStore(context), kind, readingPreferences,
        ).apply {
            selectedTimelineGroupId = groupId
            feedCacheLoaded = true
            items = rows
            timelineCacheAccountId = ownerId
            acceptOrderedRows(ownerId, chronological = true)
        }

        fun startHomeRefresh() {
            homeRefreshStart = currentFirstVisibleRow()
            refreshes++
            timeline.timelineRequestGeneration++
            timeline.feedRefreshHint = null
            pendingHomeBaseline = requireNotNull(timeline.captureExplicitRefreshBaseline(accountId))
            timeline.isLoading = true
        }

        fun finishHomeRefresh(result: Result<TimelinePage>) = compose.runOnIdle {
            val baseline = requireNotNull(pendingHomeBaseline)
            pendingHomeBaseline = null
            timeline.isLoading = false
            val page = result.getOrNull() ?: return@runOnIdle
            val count = apply(page, HomeRefreshTrigger.SelectedFeedDoubleTap, baseline)?.confirmedNewCount
            if (count == 0 && listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset == 0) {
                timeline.feedRefreshHint = "没有新微博了"
            }
        }

        fun deliverRefresh(result: Result<TimelinePage>, explicit: Boolean) = compose.runOnIdle {
            timeline.timelineRequestGeneration++
            timeline.feedRefreshHint = null
            val baseline = if (explicit) timeline.captureExplicitRefreshBaseline(accountId) else null
            val page = result.getOrNull() ?: return@runOnIdle
            apply(page, if (explicit) HomeRefreshTrigger.Pull else HomeRefreshTrigger.Automatic, baseline)
        }

        fun apply(
            page: TimelinePage,
            trigger: HomeRefreshTrigger,
            baseline: ExplicitTimelineRefreshBaseline? = null,
        ): HomeRefreshPublication? = timeline.applyRefreshPage(
            page, listState, trigger, timeline.preservesRefreshViewport(trigger), accountId,
            timeline.displayedOrderedRoute, explicitBaseline = baseline,
        )

        fun page(page: TimelinePage): Int {
            val (merged, appended) = timeline.mergeNextPage(timeline.items, page.items)
            timeline.publishNextPage(page, merged, appended)
            return appended
        }

        fun publish(newCount: Int) = deliverRefresh(
            Result.success(TimelinePage((24 + newCount downTo 24).map(row))), explicit = true,
        )

        fun scrollTo(index: Int, offset: Int = 0) = compose.runOnIdle {
            listState.requestScrollToItem(index, offset)
        }

        fun positionFoldUnderPartiallyVisiblePost() {
            val dividerIndex = compose.runOnIdle { requireNotNull(timeline.feedProjection.dividerIndex) }
            scrollTo(index = dividerIndex)
            val dividerHeight = compose.runOnIdle {
                listState.layoutInfo.visibleItemsInfo.first { it.index == dividerIndex }.size
            }
            scrollTo(index = dividerIndex - 1)
            val offset = compose.runOnIdle {
                val layout = listState.layoutInfo
                val newer = layout.visibleItemsInfo.first { it.index == dividerIndex - 1 }
                // Derive the scroll amount from measured rows and viewport, not emulator density.
                val centerOffset = newer.size + layout.beforeContentPadding + dividerHeight / 2 - layout.viewportSize.height / 2
                centerOffset.coerceIn(layout.beforeContentPadding + 1, newer.size - 1)
            }
            scrollTo(index = dividerIndex - 1, offset = offset)
        }

        fun tapSelectedHome() {
            val expected = topJumps.get() + 1
            home().assertIsSelected().performTouchInput { click() }
            compose.waitUntil(2_000) { topJumps.get() == expected }
        }

        fun firstVisibleRow(): Pair<Any, Int> = compose.runOnIdle { requireNotNull(currentFirstVisibleRow()) }

        private fun currentFirstVisibleRow(): Pair<Any, Int>? {
            val row = listState.layoutInfo.visibleItemsInfo.firstOrNull { it.index == listState.firstVisibleItemIndex }
                ?: return null
            return row.key to listState.firstVisibleItemScrollOffset
        }

        fun assertAtRow(index: Int, postNumber: Int, offset: Int = 0) = compose.runOnIdle {
            assertEquals(index, listState.firstVisibleItemIndex)
            assertEquals(offset, listState.firstVisibleItemScrollOffset)
            val row = listState.layoutInfo.visibleItemsInfo.first { it.index == index }
            assertEquals(post(postNumber).id, row.key)
            assertEquals(-offset, row.offset)
        }
    }

    private fun fourFoldedPostsFixture(number: Int, coverPackage: String): FeedItem = post(number).let { row ->
        fun portraitVideo(title: String) = FeedMedia(
            type = MediaType.Video,
            title = title,
            coverUrl = "android.resource://$coverPackage/${R.drawable.ic_tab_home}",
            streamUrl = "https://example.invalid/fixture.mp4", // Preview only; never played.
            durationSeconds = 12,
            coverWidth = 360,
            coverHeight = 640,
        )
        when (number) {
            29 -> row.copy(text = "New post 29: portrait clip", medias = listOf(portraitVideo("Newer clip")))
            28 -> row.copy(text = "New post 28: one line")
            27 -> row.copy(text = "New post 27: two lines\nSecond line")
            26 -> row.copy(text = "New post 26: three lines\nSecond line\nThird line")
            25 -> row.copy(text = "New post 25: oldest folded reply\nSecond line\nThird line\nFourth line")
            24 -> row.copy(text = "Old post 24: portrait video", medias = listOf(portraitVideo("Old clip")))
            else -> row
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
