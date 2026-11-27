package com.example.myweibo

import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.click
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.myweibo.data.FeedImage
import com.example.myweibo.data.FeedItem
import com.example.myweibo.data.ReadingPreferences
import com.example.myweibo.data.ReadingPreferencesStore
import com.example.myweibo.data.TimelineCacheStore
import com.example.myweibo.data.TimelineKind
import com.example.myweibo.data.TimelinePage
import com.example.myweibo.data.mergeFeedTimelineRefresh
import com.example.myweibo.data.projectFeedTimelineRefresh
import com.example.myweibo.ui.FeedCard
import com.example.myweibo.ui.FollowFeedScreen
import com.example.myweibo.ui.FloatingLongPostCollapseTag
import com.example.myweibo.ui.HomeFeedLifecycleEffect
import com.example.myweibo.ui.HomeLongPost
import com.example.myweibo.ui.HomeLongPostController
import com.example.myweibo.ui.HomeLongPostGlass
import com.example.myweibo.ui.HomeLongPostOverlay
import com.example.myweibo.ui.HomeTimelineReadingState
import com.example.myweibo.ui.LongPostBody
import com.example.myweibo.ui.LongPostCollapseGlassTag
import com.example.myweibo.ui.LongPostTextSectionTag
import com.example.myweibo.ui.LongPostToggleTag
import com.example.myweibo.ui.QuotedLongPostCardTag
import com.example.myweibo.ui.StatusTextSection
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Native Compose tests with local payloads; no service login or network. */
@RunWith(AndroidJUnit4::class)
class ReadingControlsComposeTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun bodiesThroughOneLinePastLimitRenderFullyWithoutAToggle() {
        setFixture {
            Column {
                FixtureBody(contentKey = "short", text = "one", tag = "short-body")
                FixtureBody(contentKey = "equal", text = "one\ntwo", tag = "equal-body")
                FixtureBody(contentKey = "one-over", text = "one\ntwo\nthree", tag = "one-over-body")
            }
        }

        compose.onNodeWithTag("short-body").assertLayout(1, false)
        compose.onNodeWithTag("equal-body").assertLayout(2, false)
        compose.onNodeWithTag("one-over-body").assertLayout(3, false)
        toggle().assertDoesNotExist()
    }

    @Test
    fun bodyTwoLinesPastLimitFoldsAndTogglesOnTheSameTextNode() {
        setFixture { FixtureBody(contentKey = "post-a", text = MultilineBody) }

        val body = compose.onNodeWithTag(BodyTag)
        body.assertLayout(2, true, TextOverflow.Ellipsis)
        val bodyNodeId = body.fetchSemanticsNode().id
        assertToggleLabel(R.string.long_post_expand)
        toggle().performClick()

        body.assertLayout(4, false, TextOverflow.Clip)
        assertEquals(bodyNodeId, body.fetchSemanticsNode().id)
        assertToggleLabel(R.string.long_post_collapse)
        toggle().performClick()

        body.assertLayout(2, true, TextOverflow.Ellipsis)
        assertEquals(bodyNodeId, body.fetchSemanticsNode().id)
        assertToggleLabel(R.string.long_post_expand)
    }

    @Test
    fun changingWidthRecalculatesWhetherBodyCanFold() {
        val width = mutableStateOf(240.dp)
        setFixture {
            FixtureBody(
                contentKey = "wrapping-post",
                text = "aaaa aaaa aaaa aaaa\nbbbb bbbb bbbb",
                width = width.value,
            )
        }

        val body = compose.onNodeWithTag(BodyTag)
        body.assertLayout(2, false)
        toggle().assertDoesNotExist()

        // At this width only the longer paragraph wraps, so the third line stays visible.
        compose.runOnIdle { width.value = 155.dp }
        body.assertLayout(3, false)
        toggle().assertDoesNotExist()

        // Narrow enough for both paragraphs to wrap: four natural lines, two collapsed lines.
        compose.runOnIdle { width.value = 100.dp }
        body.assertLayout(2, true, TextOverflow.Ellipsis)
        assertToggleLabel(R.string.long_post_expand)

        compose.runOnIdle { width.value = 155.dp }
        body.assertLayout(3, false)
        toggle().assertDoesNotExist()
    }

    @Test
    fun replacingContentKeyDoesNotCarryExpansionIntoTheNewPost() {
        val postKey = mutableStateOf("post-a")
        // No outer key(): the production body must reset its own state in this same slot.
        setFixture { FixtureBody(contentKey = postKey.value, text = MultilineBody) }

        assertToggleLabel(R.string.long_post_expand)
        toggle().performClick()
        val body = compose.onNodeWithTag(BodyTag)
        body.assertLayout(4, false, TextOverflow.Clip)
        assertToggleLabel(R.string.long_post_collapse)

        compose.runOnIdle { postKey.value = "post-b" }
        body.assertLayout(2, true, TextOverflow.Ellipsis)
        assertToggleLabel(R.string.long_post_expand)
    }

    @Test
    fun statusTextSectionExpandsAndCollapsesFixturePost() {
        val post = fixturePost(1).copy(text = MultilineBody)
        setFixture {
            StatusTextSection(
                item = post,
                emoticonMap = emptyMap(),
                style = MaterialTheme.typography.bodyMedium,
                onUserClick = null,
                isLongTextLoading = false,
                onLoadLongText = null,
                longPostLineLimit = 2,
            )
        }

        val body = compose.onNodeWithText(MultilineBody)
        body.assertLayout(2, true, TextOverflow.Ellipsis)
        assertToggleLabel(R.string.long_post_expand)
        toggle().performClick()
        body.assertLayout(4, false, TextOverflow.Clip)
        assertToggleLabel(R.string.long_post_collapse)
        toggle().performClick()
        body.assertLayout(2, true, TextOverflow.Ellipsis)
        assertToggleLabel(R.string.long_post_expand)
    }

    @Test
    fun homePostExpandsInlineAndCollapsesFromFloatingControl() {
        val proseLines = 96
        val text = (0 until proseLines).joinToString("\n") { "Ordinary paragraph $it" }
        val viewportHeight = 320.dp
        val post = fixturePost(1).copy(text = text, images = listOf(fixtureImage("ordinary")))
        val anchorIndex = 1
        val anchorOffset = with(compose.density) { 12.dp.roundToPx() }
        val mediaOffsetFromEnd = with(compose.density) { 110.dp.roundToPx() }
        val fixtureWidth = mutableStateOf<Dp?>(null)
        lateinit var listState: LazyListState
        lateinit var controller: HomeLongPostController
        setFixture {
            val state = rememberLazyListState()
            val owner = remember(state) { HomeLongPostController(state) }
            SideEffect { listState = state; controller = owner }
            LaunchedEffect(owner) { owner.watchPagingSuppression() }
            val width = fixtureWidth.value?.let { Modifier.width(it) } ?: Modifier.fillMaxWidth()
            Box(width.height(viewportHeight)) {
                LazyColumn(Modifier.fillMaxSize(), state = state) {
                    item(key = "before") { FixtureRow(fixturePost(0)) }
                    item(key = post.id) { FixtureFeedCard(post, owner, showActions = true) }
                    items((2..10).map(::fixturePost), key = { it.id }) { FixtureRow(it) }
                }
                HomeLongPostOverlay(owner, { Float.POSITIVE_INFINITY }, 0f)
            }
        }

        val body = cardText("Ordinary paragraph 0")
        val section = textSection(quoted = false)
        val floating = compose.onNodeWithTag(FloatingLongPostCollapseTag)
        val glass = compose.onNodeWithTag(LongPostCollapseGlassTag, useUnmergedTree = true)
        fun rowHeight() = compose.runOnIdle { listState.layoutInfo.visibleItemsInfo.single { it.key == post.id }.size }
        compose.runOnIdle { listState.requestScrollToItem(anchorIndex, anchorOffset) }
        body.assertLayout(2, true, TextOverflow.Ellipsis)
        val foldedHeight = rowHeight()
        val expandBounds = cardToggle(quoted = false).fetchSemanticsNode().boundsInRoot
        assertRightAligned(expandBounds, section)
        floating.assertDoesNotExist()

        compose.mainClock.autoAdvance = false
        cardToggle(quoted = false).performSemanticsAction(SemanticsActions.OnClick) { click -> assertTrue(click()) }
        compose.mainClock.advanceTimeBy(112)
        val expandingHeight = rowHeight()
        assertToggleLabel(R.string.long_post_collapse, floating)
        floating.assertInside(section)
        compose.mainClock.advanceTimeBy(240)
        val expandedHeight = rowHeight()
        assertTrue("Expected a measured intermediate height while expanding", expandingHeight in (foldedHeight + 1) until expandedHeight)
        compose.mainClock.autoAdvance = true
        body.assertLayout(proseLines, false, TextOverflow.Clip)
        toggle().assertDoesNotExist()

        // The row also contains a GIF and actions; its midpoint can put the
        // end-of-prose slot onscreen. Start with a full viewport of prose left.
        val proseHeight = body.textLayout().size.height
        val midpoint = proseHeight / 2
        assertTrue(proseHeight - midpoint > with(compose.density) { viewportHeight.roundToPx() })
        compose.runOnIdle {
            controller.collapseGlass = HomeLongPostGlass()
            listState.requestScrollToItem(anchorIndex, midpoint)
        }
        assertToggleLabel(R.string.long_post_collapse, floating)
        floating.assertInside(section)
        glass.assertInside(floating)
        val collapseBounds = floating.fetchSemanticsNode().boundsInRoot
        assertRightAligned(collapseBounds, section)
        assertEquals(expandBounds.right, collapseBounds.right, 1f)
        assertEquals(expandBounds.height, collapseBounds.height, 1f)
        compose.runOnIdle { controller.collapseGlass = HomeLongPostGlass(frost = 0f, refraction = 0f) }
        glass.assertDoesNotExist()
        assertToggleLabel(R.string.long_post_collapse, floating)
        assertEquals(collapseBounds, floating.fetchSemanticsNode().boundsInRoot)
        compose.runOnIdle { controller.collapseGlass = HomeLongPostGlass() }

        // The same lazy item and actual FeedCard are still visible, but only its media
        // and actions remain; none of those controls can inherit the prose toggle.
        compose.runOnIdle { listState.requestScrollToItem(anchorIndex, expandedHeight - mediaOffsetFromEnd) }
        compose.onNodeWithContentDescription("GIF").assertIsDisplayed()
        cardText("评论 0").assertIsDisplayed()
        cardText("转发 0").assertIsDisplayed()
        compose.onNodeWithText("赞", useUnmergedTree = true).assertIsDisplayed()
        floating.assertDoesNotExist()

        compose.runOnIdle { listState.requestScrollToItem(anchorIndex, midpoint) }
        // Let the scroll-triggered geometry and overlay settle before freezing frames.
        assertToggleLabel(R.string.long_post_collapse, floating)
        floating.assertInside(section)
        compose.mainClock.autoAdvance = false
        floating.performSemanticsAction(SemanticsActions.OnClick) { click -> assertTrue(click()) }
        compose.runOnIdle { assertAnchor(listState, anchorIndex, post.id, midpoint) }
        compose.mainClock.advanceTimeBy(112)
        val collapsingHeight = rowHeight()
        assertTrue("Expected a measured intermediate height while collapsing", collapsingHeight in (foldedHeight + 1) until expandedHeight)
        assertToggleLabel(R.string.long_post_collapse, floating)
        floating.assertInside(section)
        compose.runOnIdle {
            val offset = listState.firstVisibleItemScrollOffset
            assertTrue("Expected the offset to relock gradually, got $offset", offset in (anchorOffset + 1) until midpoint)
            assertAnchor(listState, anchorIndex, post.id, offset)
            assertTrue(controller.isLayoutPagingSuppressed)
        }
        compose.mainClock.advanceTimeBy(240)
        compose.mainClock.autoAdvance = true
        body.assertLayout(2, true, TextOverflow.Ellipsis)
        assertEquals(foldedHeight, rowHeight())
        floating.assertDoesNotExist()
        assertToggleLabel(R.string.long_post_expand)
        compose.runOnIdle {
            assertAnchor(listState, anchorIndex, post.id, anchorOffset)
            assertTrue(controller.isLayoutPagingSuppressed)
        }

        // A 50 dp prose section still fits a target even when the full label and
        // chevron need more room. The visual label truncates, not its semantics.
        compose.runOnIdle { fixtureWidth.value = 74.dp }
        cardToggle(quoted = false).performClick()
        assertToggleLabel(R.string.long_post_collapse, floating)
        val narrowButton = floating.fetchSemanticsNode().boundsInRoot
        assertRightAligned(narrowButton, section)
        assertEquals(section.fetchSemanticsNode().boundsInRoot.width, narrowButton.width, 1f)
    }

    @Test
    fun homeQuoteUsesConfiguredFoldAndNPlusOneException() {
        val throughException = fixturePost(11).copy(authorName = "Q", text = "Exception one\ntwo\nthree")
        val beyondException = fixturePost(12).copy(authorName = "Q", text = "Fold one\ntwo\nthree\nfour")
        setFixture {
            val listState = rememberLazyListState()
            val controller = remember(listState) { HomeLongPostController(listState) }
            LazyColumn(state = listState) {
                item(key = "post-1") {
                    FixtureFeedCard(fixturePost(1).copy(retweetedStatus = throughException), controller)
                }
                item(key = "post-2") {
                    FixtureFeedCard(fixturePost(2).copy(retweetedStatus = beyondException), controller)
                }
            }
        }

        cardText("Exception one").assertLayout(3, false)
        cardText("Fold one").assertLayout(2, true, TextOverflow.Ellipsis)
        compose.onAllNodesWithTag(LongPostToggleTag).assertCountEquals(1)
        assertQuoteExpandToggle()
    }

    @Test
    fun homeQuoteCollapsesMidReadInsideItsCardAndRelocksIndependently() {
        val outerText = (0 until 8).joinToString("\n") { "Outer $it" }
        val quoteText = (0 until 100).joinToString("\n") { "Original $it" }
        val quote = fixturePost(11).copy(authorName = "Q", text = quoteText, images = listOf(fixtureImage("quote")))
        val post = fixturePost(1).copy(text = outerText, retweetedStatus = quote)
        val next = fixturePost(2).copy(text = "Next card")
        val anchorIndex = 1
        val anchorOffset = with(compose.density) { 14.dp.roundToPx() }
        val mediaOffsetFromEnd = with(compose.density) { 110.dp.roundToPx() }
        val navHeightPx = with(compose.density) { 52.dp.toPx() }
        val topInsetPx = with(compose.density) { 18.dp.toPx() }
        lateinit var listState: LazyListState
        lateinit var owner: HomeLongPostController
        setFixture {
            val state = rememberLazyListState()
            val controller = remember(state) { HomeLongPostController(state) }
            val navTop = remember { mutableStateOf(Float.POSITIVE_INFINITY) }
            SideEffect { listState = state; owner = controller }
            Box(Modifier.fillMaxWidth().height(360.dp).onGloballyPositioned {
                navTop.value = it.boundsInRoot().bottom - navHeightPx
            }) {
                LazyColumn(Modifier.fillMaxSize().testTag(TimelineTag), state = state) {
                    item(key = "before") { FixtureRow(fixturePost(0)) }
                    item(key = post.id) { FixtureFeedCard(post, controller) }
                    item(key = next.id) { FixtureFeedCard(next, controller) }
                    items((3..14).map(::fixturePost), key = { it.id }) { FixtureRow(it) }
                }
                HomeLongPostOverlay(controller, { navTop.value }, topInsetPx)
            }
        }

        val outerBody = cardText("Outer 0")
        val quoteBody = cardText("Original 0")
        val quoteCard = compose.onNodeWithTag(QuotedLongPostCardTag, useUnmergedTree = true)
        val outerSection = textSection(quoted = false)
        val quoteSection = textSection(quoted = true)
        val floating = compose.onNodeWithTag(FloatingLongPostCollapseTag)
        compose.runOnIdle { listState.requestScrollToItem(anchorIndex, anchorOffset) }
        outerBody.assertLayout(2, true, TextOverflow.Ellipsis)
        quoteBody.assertLayout(2, true, TextOverflow.Ellipsis)

        cardToggle(quoted = false).performClick()
        outerBody.assertLayout(8, false, TextOverflow.Clip)
        quoteBody.assertLayout(2, true, TextOverflow.Ellipsis)
        val originalQuoteTop = quoteCard.fetchSemanticsNode().boundsInRoot.top
        val quoteExpand = cardToggle(quoted = true).fetchSemanticsNode().boundsInRoot
        assertRightAligned(quoteExpand, quoteSection)
        cardToggle(quoted = true).performClick()
        outerBody.assertLayout(8, false, TextOverflow.Clip)
        quoteBody.assertLayout(100, false, TextOverflow.Clip)
        val twoButtons = compose.onAllNodesWithTag(FloatingLongPostCollapseTag).assertCountEquals(2)
            .fetchSemanticsNodes().map { it.boundsInRoot }.sortedBy { it.top }
        val outerTextBounds = outerSection.fetchSemanticsNode().boundsInRoot
        val quoteTextBounds = quoteSection.fetchSemanticsNode().boundsInRoot
        assertTrue(twoButtons[0].isInside(outerTextBounds) && twoButtons[0].bottom <= originalQuoteTop)
        assertTrue(twoButtons[1].isInside(quoteTextBounds))
        assertRightAligned(twoButtons[1], quoteSection)
        assertEquals(quoteExpand.right, twoButtons[1].right, 1f)
        assertEquals(quoteExpand.height, twoButtons[1].height, 1f)
        val expandedHeight = compose.runOnIdle {
            listState.layoutInfo.visibleItemsInfo.single { it.key == post.id }.size
        }
        val timelineTop = compose.onNodeWithTag(TimelineTag).fetchSemanticsNode().boundsInRoot.top
        val quoteEndOffset = (quoteTextBounds.top - timelineTop + anchorOffset + quoteBody.textLayout().size.height).roundToInt() +
            with(compose.density) { 48.dp.roundToPx() }

        // The quote button remains usable in the middle of prose long past the outer body.
        val midpoint = expandedHeight / 2
        compose.runOnIdle {
            owner.collapseGlass = HomeLongPostGlass()
            listState.requestScrollToItem(anchorIndex, midpoint)
        }
        assertToggleLabel(R.string.long_post_collapse, floating)
        floating.assertInside(quoteSection)
        sectionNode(LongPostCollapseGlassTag, quoted = true).assertInside(floating)
        val readableBottom = compose.onNodeWithTag(TimelineTag).fetchSemanticsNode().boundsInRoot.bottom - navHeightPx
        assertTrue(floating.fetchSemanticsNode().boundsInRoot.bottom <= readableBottom)
        compose.runOnIdle { assertAnchor(listState, anchorIndex, post.id, midpoint) }

        // A narrow remaining sliver is not enough for a separate 48 dp target.
        val sliver = with(compose.density) { 24.dp.roundToPx() }
        compose.runOnIdle { listState.requestScrollToItem(anchorIndex, quoteEndOffset - sliver) }
        floating.assertDoesNotExist()

        // Stop with the GIF and next card visible but the quote's prose offscreen.
        val nextText = cardText("Next card")
        compose.runOnIdle { listState.requestScrollToItem(anchorIndex, expandedHeight - mediaOffsetFromEnd) }
        compose.onNodeWithContentDescription("GIF").assertIsDisplayed()
        nextText.assertIsDisplayed()
        floating.assertDoesNotExist()
        compose.runOnIdle { listState.requestScrollToItem(anchorIndex + 1, mediaOffsetFromEnd) }
        floating.assertDoesNotExist()

        // Leaving the item did not fold it. Collapse from its midpoint and relock to the
        // exact pre-open lazy offset and quote position, without folding the outer prose.
        compose.runOnIdle { listState.requestScrollToItem(anchorIndex, midpoint) }
        floating.assertIsDisplayed().performClick()
        quoteBody.assertLayout(2, true, TextOverflow.Ellipsis)
        outerBody.assertLayout(8, false, TextOverflow.Clip)
        assertQuoteExpandToggle()
        compose.runOnIdle { assertAnchor(listState, anchorIndex, post.id, anchorOffset) }
        assertEquals(originalQuoteTop, quoteCard.fetchSemanticsNode().boundsInRoot.top, 0.5f)
        floating.assertIsDisplayed().performClick()
        outerBody.assertLayout(2, true, TextOverflow.Ellipsis)
        floating.assertDoesNotExist()
    }

    @Test
    fun homeQuoteRestoresBelowTopAnchorAfterLiveWidthChange() {
        val quoteText = (0 until 60).joinToString("\n") { "Resize quote $it" }
        val post = fixturePost(1).copy(retweetedStatus = fixturePost(11).copy(authorName = "Q", text = quoteText))
        val width = mutableStateOf(240.dp)
        lateinit var listState: LazyListState
        setFixture {
            val state = rememberLazyListState()
            val controller = remember(state) { HomeLongPostController(state) }
            SideEffect { listState = state }
            Box(Modifier.width(width.value).height(280.dp)) {
                LazyColumn(Modifier.fillMaxSize(), state = state) {
                    item(key = "before") { FixtureRow(fixturePost(0)) }
                    item(key = post.id) { FixtureFeedCard(post, controller) }
                    items((2..10).map(::fixturePost), key = { it.id }) { FixtureRow(it) }
                }
                HomeLongPostOverlay(controller, { Float.POSITIVE_INFINITY }, 0f)
            }
        }

        val quoteBody = cardText("Resize quote 0")
        val quoteCard = compose.onNodeWithTag(QuotedLongPostCardTag, useUnmergedTree = true)
        quoteBody.assertLayout(2, true, TextOverflow.Ellipsis)
        fun itemOffset() = listState.layoutInfo.visibleItemsInfo.single { it.key == post.id }.offset
        val rowTopBefore = compose.runOnIdle { itemOffset() }
        val widthBefore = compose.runOnIdle { listState.layoutInfo.viewportSize.width }
        val quoteTopBefore = quoteCard.fetchSemanticsNode().boundsInRoot.top
        assertTrue(rowTopBefore > 0)
        cardToggle(quoted = true).performClick()
        compose.runOnIdle {
            val row = listState.layoutInfo.visibleItemsInfo.single { it.key == post.id }
            listState.requestScrollToItem(row.index, row.size / 2)
            width.value = 200.dp
        }
        compose.runOnIdle { assertTrue(listState.layoutInfo.viewportSize.width < widthBefore) }
        compose.onNodeWithTag(FloatingLongPostCollapseTag).assertIsDisplayed().performClick()

        quoteBody.assertLayout(2, true, TextOverflow.Ellipsis)
        compose.runOnIdle { assertEquals(rowTopBefore, itemOffset()) }
        assertEquals(quoteTopBefore, quoteCard.fetchSemanticsNode().boundsInRoot.top, 0.5f)
    }

    @Test
    fun keyedTimelineKeepsPartialScrollAnchorAndShowsGapAtTopAfterRefresh() {
        val previous = (12 downTo 1).map(::fixturePost)
        val projection = mutableStateOf(projectFeedTimelineRefresh(previous, null, 2))
        val anchorIndex = 4
        val anchorId = previous[anchorIndex].id
        val partialOffset = with(compose.density) { 20.dp.roundToPx() }
        lateinit var listState: LazyListState
        setFixture {
            val state = rememberLazyListState()
            SideEffect { listState = state }
            val snapshot = projection.value
            val gap = snapshot.gap
            val gapIndex = gap?.insertionIndex ?: snapshot.visibleItems.size
            LazyColumn(
                modifier = Modifier.fillMaxWidth().height(280.dp).testTag(TimelineTag),
                state = state,
            ) {
                items(snapshot.visibleItems.take(gapIndex), key = { it.id }) { FixtureRow(it) }
                if (gap != null) {
                    item(key = GapTag) {
                        Text("${gap.hiddenPostCount} hidden posts", Modifier.height(48.dp).testTag(GapTag))
                    }
                }
                items(snapshot.visibleItems.drop(gapIndex), key = { it.id }) { FixtureRow(it) }
            }
        }

        compose.onNodeWithTag(GapTag).assertDoesNotExist()
        compose.runOnIdle { listState.requestScrollToItem(anchorIndex, partialOffset) }
        compose.runOnIdle { assertAnchor(listState, anchorIndex, anchorId, partialOffset) }

        compose.runOnIdle {
            val incoming = (16 downTo 12).map(::fixturePost)
            val merged = mergeFeedTimelineRefresh(previous, incoming)
            projection.value = projectFeedTimelineRefresh(merged, previous.first().id, 2)
        }
        // Do not issue another scroll: the two new visible rows plus the gap move the index,
        // so an unchanged index would mean Compose did not preserve the key-based anchor.
        compose.runOnIdle { assertAnchor(listState, anchorIndex + 3, anchorId, partialOffset) }

        compose.onNodeWithTag(TimelineTag).performScrollToIndex(0)
        compose.onNodeWithTag(GapTag).assertIsDisplayed().assertTextEquals("2 hidden posts")
        compose.runOnIdle {
            assertEquals(
                listOf("post-16", "post-15", GapTag, previous.first().id),
                listState.layoutInfo.visibleItemsInfo.take(4).map { it.key },
            )
        }
    }

    @Test
    fun recommendationReplacementNeverFlashesExpandedPostOrFoldedQuote() {
        assertHomeRefreshFrames(TimelineKind.Hot, expandQuote = false, keepReading = false)
    }

    @Test
    fun followingReplacementNeverFlashesFoldedPostOrExpandedQuote() {
        assertHomeRefreshFrames(TimelineKind.Following, expandQuote = true, keepReading = false)
    }

    @Test
    fun protectedFollowingMergeKeepsFoldMeasurementsWhenStatusAliasesChange() {
        assertHomeRefreshFrames(TimelineKind.Following, expandQuote = true, keepReading = true)
    }

    @Test
    fun latestReselectionKeepsFoldedBodyToggleAndMediaInTheFirstRefreshLayouts() {
        val owner = compose.runOnUiThread {
            FixtureLifecycleOwner().apply { lifecycle.currentState = Lifecycle.State.RESUMED }
        }
        val loading = mutableStateOf(false)
        val post = fixturePost(1).copy(text = MultilineBody, images = listOf(fixtureImage("reselection")))
        // Keep a partial anchor without scrolling the expand target out of view.
        val anchorOffset = with(compose.density) { 8.dp.roundToPx() }
        var committedLoading = false
        var starts = 0
        lateinit var listState: LazyListState
        setFixture {
            val isLoading = loading.value
            val state = rememberLazyListState(1, anchorOffset)
            var resumeGeneration by remember { mutableIntStateOf(0) }
            SideEffect { listState = state; committedLoading = isLoading }
            HomeFeedLifecycleEffect(
                lifecycleOwner = owner,
                onStart = { starts++; resumeGeneration++ },
                onStop = {},
                onDispose = {},
            )
            Column {
                // Model tapping an already-selected latest tab: the request enters
                // loading without navigating away. The test supplies its completion.
                TextButton(onClick = { loading.value = true }, modifier = Modifier.testTag("reselect-latest")) {
                    Text("Latest")
                }
                // Like WeiboApp, the scroll state outlives the keyed feed subtree.
                key(resumeGeneration) {
                    val controller = remember(state) { HomeLongPostController(state) }
                    LazyColumn(Modifier.fillMaxSize(), state = state) {
                        item(key = "before") { FixtureRow(fixturePost(0)) }
                        item(key = post.id) { FixtureFeedCard(post, controller) }
                        items((2..10).map(::fixturePost), key = { it.id }) { FixtureRow(it) }
                    }
                }
            }
        }

        val body = cardText(MultilineBody)
        val expand = cardToggle(quoted = false)
        val expandLabel = InstrumentationRegistry.getInstrumentation().targetContext.getString(R.string.long_post_expand)
        fun assertExpandLabel() = expand.assertIsDisplayed().assert(hasAnyDescendant(hasText(expandLabel)))
        val section = textSection(quoted = false)
        val media = compose.onNodeWithContentDescription("GIF", useUnmergedTree = true)
        body.assertLayout(2, true, TextOverflow.Ellipsis)
        assertExpandLabel()
        val bodyId = body.fetchSemanticsNode().id
        val toggleBounds = expand.fetchSemanticsNode().boundsInRoot
        val mediaBounds = media.assertIsDisplayed().fetchSemanticsNode().boundsInRoot
        assertRightAligned(toggleBounds, section)
        val rowHeight = compose.runOnIdle {
            assertAnchor(listState, 1, post.id, anchorOffset)
            listState.layoutInfo.visibleItemsInfo.first { it.key == post.id }.size
        }
        val startsBeforeRefresh = compose.runOnIdle { starts }

        fun assertFirstLayouts(expectedLoading: Boolean) {
            // First commit applies loading; a second frame also catches an erroneous
            // generation change queued from a re-attached observer in that commit.
            var checkedCommit = false
            repeat(4) {
                compose.mainClock.advanceTimeByFrame()
                val isRefreshCommit = compose.runOnIdle { committedLoading == expectedLoading }
                body.assertLayout(2, true, TextOverflow.Ellipsis)
                assertExpandLabel()
                assertEquals(toggleBounds, expand.fetchSemanticsNode().boundsInRoot)
                assertEquals(mediaBounds, media.fetchSemanticsNode().boundsInRoot)
                assertEquals(bodyId, body.fetchSemanticsNode().id)
                compose.runOnIdle {
                    assertAnchor(listState, 1, post.id, anchorOffset)
                    assertEquals(rowHeight, listState.layoutInfo.visibleItemsInfo.first { it.key == post.id }.size)
                }
                if (isRefreshCommit && checkedCommit) {
                    assertEquals(startsBeforeRefresh, compose.runOnIdle { starts })
                    return
                }
                checkedCommit = isRefreshCommit
            }
            error("Loading=$expectedLoading did not commit in time")
        }

        compose.mainClock.autoAdvance = false
        try {
            compose.onNodeWithTag("reselect-latest").performClick()
            assertFirstLayouts(expectedLoading = true)
            compose.runOnIdle { loading.value = false }
            assertFirstLayouts(expectedLoading = false)
        } finally {
            compose.mainClock.autoAdvance = true
        }
    }

    @Test
    fun homeDetailReturnTabReturnAndResumeKeepFirstFoldedLayoutAndScrollAnchor() {
        val post = fixturePost(1).copy(text = NavigationBody, images = listOf(fixtureImage("navigation")))
        val posts = listOf(fixturePost(0), post) + (2..30).map(::fixturePost)
        val anchorOffset = with(compose.density) { 8.dp.roundToPx() }
        val route = mutableStateOf(HomeFixturePage.Home)
        val resumeGeneration = mutableIntStateOf(0)
        lateinit var listState: LazyListState
        lateinit var timeline: HomeTimelineReadingState
        setFixture {
            val page = route.value
            val state = rememberLazyListState(1, anchorOffset)
            val reading = rememberFixtureHomeTimeline(posts)
            SideEffect { listState = state; timeline = reading }
            Box(Modifier.fillMaxSize()) {
                // Same lifetimes as WeiboApp: a detail covers the retained feed, a tab
                // can dispose it, and a real app resume re-keys it without moving the list.
                if (page != HomeFixturePage.OtherTab) {
                    key(resumeGeneration.intValue) {
                        FixtureHomeTimeline(
                            state, reading,
                            feedUiOnTop = page == HomeFixturePage.Home,
                            onItemClick = { item, _ ->
                                assertEquals(post.id, item.id)
                                route.value = HomeFixturePage.Detail
                            },
                        )
                    }
                }
                if (page == HomeFixturePage.Detail) {
                    TextButton(onClick = { route.value = HomeFixturePage.Home }, modifier = Modifier.testTag("fixture-back")) {
                        Text("Back")
                    }
                }
            }
        }

        val body = cardText(NavigationBody)
        val expand = cardToggle(quoted = false)
        val expandLabel = InstrumentationRegistry.getInstrumentation().targetContext.getString(R.string.long_post_expand)
        val media = compose.onNodeWithContentDescription("GIF", useUnmergedTree = true)
        val toggleBounds = expand.assertIsDisplayed().fetchSemanticsNode().boundsInRoot
        val mediaBounds = media.assertIsDisplayed().fetchSemanticsNode().boundsInRoot
        val originalBodyId = body.fetchSemanticsNode().id
        val rowHeight = compose.runOnIdle { listState.layoutInfo.visibleItemsInfo.first { it.key == post.id }.size }

        fun assertHomeLayout() {
            body.assertLayout(3, true, TextOverflow.Ellipsis)
            expand.assertIsDisplayed().assert(hasAnyDescendant(hasText(expandLabel)))
            assertEquals(toggleBounds, expand.fetchSemanticsNode().boundsInRoot)
            assertEquals(mediaBounds, media.assertIsDisplayed().fetchSemanticsNode().boundsInRoot)
            compose.runOnIdle {
                assertAnchor(listState, 1, post.id, anchorOffset)
                assertEquals(rowHeight, listState.layoutInfo.visibleItemsInfo.first { it.key == post.id }.size)
            }
        }

        assertHomeLayout()
        body.performTouchInput { click(center) }
        val back = compose.onNodeWithTag("fixture-back").assertIsDisplayed()
        compose.mainClock.autoAdvance = false
        try {
            back.performClick()
            compose.mainClock.advanceTimeByFrame()
            back.assertDoesNotExist()
            assertHomeLayout()
            assertEquals(originalBodyId, body.fetchSemanticsNode().id)

            compose.runOnIdle { route.value = HomeFixturePage.OtherTab }
            compose.mainClock.advanceTimeByFrame()
            body.assertDoesNotExist()
            compose.runOnIdle { route.value = HomeFixturePage.Home }
            compose.mainClock.advanceTimeByFrame()
            assertHomeLayout()
            val remountedBodyId = body.fetchSemanticsNode().id
            assertTrue("Switching tabs must discard the old Text", remountedBodyId != originalBodyId)

            compose.runOnIdle { resumeGeneration.intValue++ }
            compose.mainClock.advanceTimeByFrame()
            assertHomeLayout()
            assertTrue("Activity resume must exercise a new Text", body.fetchSemanticsNode().id != remountedBodyId)
        } finally {
            compose.mainClock.autoAdvance = true
        }

        expand.performClick()
        body.assertLayout(5, false, TextOverflow.Clip)
        val expandedHeight = compose.runOnIdle { listState.layoutInfo.visibleItemsInfo.first { it.key == post.id }.size }
        val collapse = sectionNode(FloatingLongPostCollapseTag, quoted = false)
        val collapseBounds = collapse.assertIsDisplayed().fetchSemanticsNode().boundsInRoot
        body.performTouchInput { click(center) }
        back.assertIsDisplayed()
        compose.mainClock.autoAdvance = false
        try {
            back.performClick()
            compose.mainClock.advanceTimeByFrame()
            back.assertDoesNotExist()
            body.assertLayout(5, false, TextOverflow.Clip)
            assertEquals(collapseBounds, collapse.assertIsDisplayed().fetchSemanticsNode().boundsInRoot)

            compose.runOnIdle { route.value = HomeFixturePage.OtherTab }
            compose.mainClock.advanceTimeByFrame()
            body.assertDoesNotExist()
            compose.runOnIdle { route.value = HomeFixturePage.Home }
            compose.mainClock.advanceTimeByFrame()
            body.assertLayout(5, false, TextOverflow.Clip)
            compose.runOnIdle {
                assertAnchor(listState, 1, post.id, anchorOffset)
                assertEquals(expandedHeight, listState.layoutInfo.visibleItemsInfo.first { it.key == post.id }.size)
            }
            compose.runOnIdle { resumeGeneration.intValue++ }
            compose.mainClock.advanceTimeByFrame()
            body.assertLayout(5, false, TextOverflow.Clip)

            // Account/timeline switches reset the reader, even if the next feed has the same row ID.
            compose.runOnIdle {
                timeline.resetTimelineReadingSession()
                timeline.items = posts
            }
            compose.mainClock.advanceTimeByFrame()
            assertHomeLayout()
        } finally {
            compose.mainClock.autoAdvance = true
        }
    }

    @Test
    fun recycledAndReplacedHomeRowsUseCurrentTextAndLineLimitInTheirFirstLayout() {
        val first = fixturePost(1).copy(text = NavigationBody)
        val replacement = first.copy(text = "Replacement one\ntwo\nthree\nfour\nfive")
        lateinit var state: LazyListState
        lateinit var timeline: HomeTimelineReadingState
        setFixture {
            val list = rememberLazyListState()
            val reading = rememberFixtureHomeTimeline(listOf(first) + (2..40).map(::fixturePost))
            SideEffect { state = list; timeline = reading }
            FixtureHomeTimeline(list, reading)
        }

        val firstBody = cardText(NavigationBody)
        val replacementBody = cardText("Replacement one")
        firstBody.assertLayout(3, true, TextOverflow.Ellipsis)
        // Move far beyond the original row so lazy reuse, not just feed visibility, is exercised.
        compose.runOnIdle { state.requestScrollToItem(39) }
        firstBody.assertDoesNotExist()
        compose.mainClock.autoAdvance = false
        try {
            compose.runOnIdle { state.requestScrollToItem(0) }
            compose.mainClock.advanceTimeByFrame()
            firstBody.assertLayout(3, true, TextOverflow.Ellipsis)
            cardToggle(quoted = false).assertIsDisplayed()

            compose.runOnIdle { timeline.items = listOf(replacement) + timeline.items.drop(1) }
            compose.mainClock.advanceTimeByFrame()
            firstBody.assertDoesNotExist()
            replacementBody.assertLayout(3, true, TextOverflow.Ellipsis)
            cardToggle(quoted = false).assertIsDisplayed()

            // Change the actual Home setting: an N+1 body is fully visible on its first layout.
            compose.runOnIdle { timeline.updatePreferences { it.copy(maxLines = 4) } }
            compose.mainClock.advanceTimeByFrame()
            replacementBody.assertLayout(5, false, TextOverflow.Clip)
            cardToggle(quoted = false).assertDoesNotExist()

            compose.runOnIdle { timeline.updatePreferences { it.copy(foldLongPosts = false) } }
            compose.mainClock.advanceTimeByFrame()
            replacementBody.assertLayout(5, false, TextOverflow.Clip)
            cardToggle(quoted = false).assertDoesNotExist()
        } finally {
            compose.mainClock.autoAdvance = true
        }
    }

    @Test
    fun homeRichQuoteUsesCurrentWidthAndInlinePlaceholdersInTheFirstLayout() {
        val width = mutableStateOf(150.dp)
        val quote = fixturePost(20).copy(authorName = "Q", text = "Quote [probe]\nWWWWWWWWWWWWWWWWWW\nthird\nlast")
        val post = fixturePost(1).copy(text = "Host", retweetedStatus = quote)
        val posts = listOf(post) + (2..10).map(::fixturePost)
        val emoticons = mapOf("[probe]" to "fixture-invalid-url")
        setFixture {
            Box(Modifier.width(width.value)) {
                FixtureHomeTimeline(rememberLazyListState(), rememberFixtureHomeTimeline(posts), emoticonMap = emoticons)
            }
        }

        val quoteBody = cardText("Quote [probe]")
        quoteBody.assertLayout(3, true, TextOverflow.Ellipsis)
        assertTrue(quoteBody.textLayout().layoutInput.placeholders.isNotEmpty())
        cardToggle(quoted = true).assertIsDisplayed()
        compose.mainClock.autoAdvance = false
        try {
            compose.runOnIdle { width.value = 340.dp }
            compose.mainClock.advanceTimeByFrame()
            quoteBody.assertLayout(4, false, TextOverflow.Clip)
            cardToggle(quoted = true).assertDoesNotExist()

            compose.runOnIdle { width.value = 150.dp }
            compose.mainClock.advanceTimeByFrame()
            quoteBody.assertLayout(3, true, TextOverflow.Ellipsis)
            cardToggle(quoted = true).assertIsDisplayed()
        } finally {
            compose.mainClock.autoAdvance = true
        }
    }

    @Test
    fun homeFeedLifecycleIgnoresCatchUpAndUsesCurrentCallbacksOnRealTransitions() {
        val owner = compose.runOnUiThread {
            FixtureLifecycleOwner().apply { lifecycle.currentState = Lifecycle.State.STARTED }
        }
        val callbackVersion = mutableIntStateOf(1)
        val mounted = mutableStateOf(true)
        val events = mutableListOf<String>()
        setFixture {
            if (mounted.value) {
                val version = callbackVersion.intValue
                HomeFeedLifecycleEffect(
                    lifecycleOwner = owner,
                    onStart = { events += "start:$version" },
                    onStop = { events += "stop:$version" },
                    onDispose = { events += "dispose:$version" },
                )
            }
        }
        compose.runOnIdle {
            assertTrue("Attaching while STARTED must not simulate a resume", events.isEmpty())
            owner.lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
            callbackVersion.intValue = 2
        }
        compose.runOnIdle {
            assertTrue("A recomposition while RESUMED must not replace the observer", events.isEmpty())
            owner.lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
            owner.lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
            owner.lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_START)
            owner.lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
            assertEquals(listOf("stop:2", "start:2"), events)
            mounted.value = false
        }
        compose.runOnIdle {
            owner.lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
            owner.lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
            assertEquals(listOf("stop:2", "start:2", "dispose:2"), events)
        }
    }

    private class FixtureLifecycleOwner : LifecycleOwner {
        override val lifecycle = LifecycleRegistry(this)
    }

    private fun assertHomeRefreshFrames(kind: TimelineKind, expandQuote: Boolean, keepReading: Boolean) {
        val bodyText = (1..6).joinToString("\n") { "Ordinary line $it" }
        val quoteText = (1..6).joinToString("\n") { "Quoted line $it" }
        // Start with an omitted mblogid for the outer status (the parser falls back to id).
        val quote = fixturePost(20).copy(authorName = "Q", text = quoteText)
        val post = fixturePost(10).let { it.copy(statusId = it.id, text = bodyText, retweetedStatus = quote) }
        val tail = (9 downTo 1).map(::fixturePost)
        lateinit var timeline: HomeTimelineReadingState
        lateinit var listState: LazyListState
        setFixture {
            val context = LocalContext.current
            val state = rememberLazyListState()
            val reading = remember {
                HomeTimelineReadingState(
                    ReadingPreferencesStore(context), TimelineCacheStore(context), kind,
                    ReadingPreferences(foldLongPosts = true, maxLines = 3, reverseReading = keepReading),
                ).apply {
                    feedCacheLoaded = true
                    timelineVerifiedFromNetwork = true
                    items = listOf(post) + tail
                }
            }
            SideEffect { listState = state; timeline = reading }
            FollowFeedScreen(
                listState = state,
                homeTimeline = reading,
                hasLoginCookie = true,
                onRefresh = {},
                onLoadMore = {},
                onOpenLoginSettings = {},
                onItemClick = { _, _ -> },
                onCommentClick = { _, _ -> },
                onCommentLongClick = {},
                onRepostClick = { _, _ -> },
                onMediaClick = { _, _ -> },
            )
        }

        val expandedBody = cardText(if (expandQuote) "Quoted line 1" else "Ordinary line 1")
        val foldedBody = cardText(if (expandQuote) "Ordinary line 1" else "Quoted line 1")
        val expand = cardToggle(quoted = !expandQuote)
        val collapse = sectionNode(FloatingLongPostCollapseTag, quoted = expandQuote)
        cardToggle(quoted = expandQuote).performClick()
        expandedBody.assertLayout(6, false, TextOverflow.Clip)
        foldedBody.assertLayout(3, true, TextOverflow.Ellipsis)
        expand.assertIsDisplayed()
        collapse.assertIsDisplayed()
        val expandedNodeId = expandedBody.fetchSemanticsNode().id
        val foldedNodeId = foldedBody.fetchSemanticsNode().id
        val expandBounds = expand.fetchSemanticsNode().boundsInRoot
        val collapseBounds = collapse.fetchSemanticsNode().boundsInRoot
        val rowHeight = compose.runOnIdle { listState.layoutInfo.visibleItemsInfo.first { it.key == post.id }.size }
        val rowOffset = compose.runOnIdle { listState.layoutInfo.visibleItemsInfo.first { it.key == post.id }.offset }

        fun assertNextFrame() {
            compose.mainClock.advanceTimeByFrame()
            compose.runOnIdle {
                val row = checkNotNull(listState.layoutInfo.visibleItemsInfo.firstOrNull { it.key == post.id }) {
                    "Visible lazy row ${post.id} disappeared during refresh"
                }
                assertEquals(rowHeight, row.size)
                assertEquals(rowOffset, row.offset)
            }
            expandedBody.assertLayout(6, false, TextOverflow.Clip)
            foldedBody.assertLayout(3, true, TextOverflow.Ellipsis)
            assertEquals(expandedNodeId, expandedBody.fetchSemanticsNode().id)
            assertEquals(foldedNodeId, foldedBody.fetchSemanticsNode().id)
            expand.assertIsDisplayed()
            collapse.assertIsDisplayed()
            assertEquals(expandBounds, expand.fetchSemanticsNode().boundsInRoot)
            assertEquals(collapseBounds, collapse.fetchSemanticsNode().boundsInRoot)
        }

        // A refresh starts before its response. Check both that phase and the *first*
        // layouts after publishing, without draining Compose's clock to steady state.
        compose.mainClock.autoAdvance = false
        try {
            compose.runOnIdle { timeline.isLoading = true }
            repeat(2) { assertNextFrame() }
            val refreshed = post.copy(
                statusId = "canonical-id",
                authorName = "Refreshed author",
                text = "Ordinary line 1…",
                requiresLongTextFetch = true,
                retweetedStatus = quote.copy(id = "original-alias", text = "Quoted line 1…", requiresLongTextFetch = true),
            )
            compose.runOnIdle {
                timeline.publishRefresh(TimelinePage(listOf(refreshed) + tail), listState, keepReading, keepReading)
            }
            repeat(4) { assertNextFrame() }
            // Confirm the response rendered, not merely that the old page stayed visible.
            compose.onNodeWithText("Refreshed author", useUnmergedTree = true).assertIsDisplayed()
            compose.runOnIdle { timeline.isLoading = false }
            repeat(2) { assertNextFrame() }
        } finally {
            compose.mainClock.autoAdvance = true
        }
    }

    private enum class HomeFixturePage { Home, Detail, OtherTab }

    @Composable
    private fun rememberFixtureHomeTimeline(posts: List<FeedItem>): HomeTimelineReadingState {
        val context = LocalContext.current
        return remember(context) {
            val isolatedPreferences = object : ContextWrapper(context) {
                override fun getApplicationContext(): Context = this
                override fun getSharedPreferences(name: String, mode: Int) =
                    baseContext.getSharedPreferences("reading_controls_test_$name", mode)
            }
            HomeTimelineReadingState(
                ReadingPreferencesStore(isolatedPreferences), TimelineCacheStore(context), TimelineKind.Following,
                ReadingPreferences(foldLongPosts = true, maxLines = 3),
            ).apply {
                feedCacheLoaded = true
                timelineVerifiedFromNetwork = true
                items = posts
            }
        }
    }

    @Composable
    private fun FixtureHomeTimeline(
        listState: LazyListState,
        timeline: HomeTimelineReadingState,
        emoticonMap: Map<String, String> = emptyMap(),
        feedUiOnTop: Boolean = true,
        onItemClick: (FeedItem, Rect?) -> Unit = { _, _ -> },
    ) {
        FollowFeedScreen(
            listState = listState,
            homeTimeline = timeline,
            hasLoginCookie = true,
            emoticonMap = emoticonMap,
            feedUiOnTop = feedUiOnTop,
            onRefresh = {},
            onLoadMore = {},
            onOpenLoginSettings = {},
            onItemClick = onItemClick,
            onCommentClick = { _, _ -> },
            onCommentLongClick = {},
            onRepostClick = { _, _ -> },
            onMediaClick = { _, _ -> },
        )
    }

    private fun setFixture(content: @Composable () -> Unit) {
        compose.setContent {
            // Keep the width fixture independent of the device's accessibility font scale.
            CompositionLocalProvider(LocalDensity provides Density(LocalDensity.current.density, 1f)) {
                MaterialTheme(content = content)
            }
        }
    }

    @Composable
    private fun FixtureBody(
        contentKey: String,
        text: String,
        width: Dp = 240.dp,
        tag: String = BodyTag,
    ) {
        Column(Modifier.width(width)) {
            LongPostBody(contentKey = contentKey, lineLimit = 2) {
                maxLines, overflow, onTextLayout ->
                Text(
                    text = text,
                    modifier = Modifier.fillMaxWidth().testTag(tag),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 16.sp,
                    maxLines = maxLines,
                    overflow = overflow,
                    onTextLayout = { onTextLayout?.invoke(it) },
                )
            }
        }
    }

    private fun SemanticsNodeInteraction.assertLayout(
        lines: Int,
        truncated: Boolean,
        overflow: TextOverflow? = null,
    ) {
        val layout = textLayout()
        assertEquals(lines, layout.lineCount)
        assertEquals(truncated, layout.didOverflowHeight)
        if (overflow != null) assertEquals(overflow, layout.layoutInput.overflow)
        assertEquals(truncated && overflow == TextOverflow.Ellipsis, layout.isLineEllipsized(lines - 1))
    }

    private fun SemanticsNodeInteraction.textLayout(): TextLayoutResult {
        val results = mutableListOf<TextLayoutResult>()
        performSemanticsAction(SemanticsActions.GetTextLayoutResult) { getLayout -> getLayout(results) }
        return results.single()
    }

    private fun toggle() = compose.onNodeWithTag(LongPostToggleTag)

    @Composable
    private fun FixtureFeedCard(post: FeedItem, controller: HomeLongPostController, showActions: Boolean = false) {
        FeedCard(
            item = post,
            onClick = {},
            onMediaClick = { _, _ -> },
            onRetweetCardClick = { _, _, _ -> },
            showAuthorRow = false,
            showActionMenu = false,
            showStatusActions = showActions,
            longPostLineLimit = 2,
            homeLongPost = HomeLongPost(controller, post.id),
        )
    }

    private fun cardText(text: String) = compose.onNodeWithText(text, substring = true, useUnmergedTree = true)

    private fun cardToggle(quoted: Boolean) = sectionNode(LongPostToggleTag, quoted)

    private fun textSection(quoted: Boolean) = sectionNode(LongPostTextSectionTag, quoted)

    private fun sectionNode(tag: String, quoted: Boolean): SemanticsNodeInteraction {
        val inQuote = hasAnyAncestor(hasTestTag(QuotedLongPostCardTag))
        return compose.onNode(hasTestTag(tag) and (if (quoted) inQuote else !inQuote), useUnmergedTree = true)
    }

    private fun assertRightAligned(button: Rect, owner: SemanticsNodeInteraction) {
        val bounds = owner.fetchSemanticsNode().boundsInRoot
        val minTarget = with(compose.density) { 48.dp.roundToPx() }.toFloat()
        assertEquals(bounds.right, button.right, 1f)
        assertTrue(button.width >= minTarget && button.height >= minTarget)
    }

    private fun assertQuoteExpandToggle() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        cardToggle(quoted = true).assertIsDisplayed()
            .assert(hasAnyDescendant(hasText(context.getString(R.string.long_post_expand))))
    }

    private fun SemanticsNodeInteraction.assertInside(owner: SemanticsNodeInteraction) {
        assertIsDisplayed()
        val button = fetchSemanticsNode().boundsInRoot
        val section = owner.fetchSemanticsNode().boundsInRoot
        assertTrue("Collapse bounds $button must stay inside its text section $section", button.isInside(section))
    }

    private fun Rect.isInside(container: Rect): Boolean =
        left >= container.left && right <= container.right && top >= container.top && bottom <= container.bottom

    private fun assertToggleLabel(label: Int, node: SemanticsNodeInteraction = toggle()) {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        node.assertIsDisplayed().assertTextEquals(context.getString(label))
    }

    private fun assertAnchor(state: LazyListState, index: Int, id: String, offset: Int) {
        assertEquals(index, state.firstVisibleItemIndex)
        assertEquals(offset, state.firstVisibleItemScrollOffset)
        val item = state.layoutInfo.visibleItemsInfo.first { it.index == index }
        assertEquals(id, item.key)
        assertEquals(-offset, item.offset)
    }

    @Composable
    private fun FixtureRow(item: FeedItem) {
        Text(item.text, Modifier.fillMaxWidth().height(64.dp))
    }

    private fun fixtureImage(id: String) = FeedImage(
        id = id, thumbnailUrl = "", largeUrl = "", width = 480, height = 800, type = "gif",
    )

    private fun fixturePost(number: Int) = FeedItem(
        id = "post-$number",
        statusId = "status-$number",
        authorId = "fixture-author",
        authorName = "Fixture author",
        authorAvatarUrl = null,
        createdAt = (1_700_000_000L + number).toString(),
        source = null,
        ipLocation = null,
        text = "Post $number",
        repostsCount = "0",
        commentsCount = "0",
        likesCount = "0",
        images = emptyList(),
    )

    private companion object {
        const val BodyTag = "fixture-body"
        const val TimelineTag = "fixture-timeline"
        const val GapTag = "fixture-refresh-gap"
        const val MultilineBody = "one\ntwo\nthree\nfour"
        const val NavigationBody = "one\ntwo\nthree\nfour\nfive"
    }
}
