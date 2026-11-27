package com.example.myweibo

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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
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
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.myweibo.data.FeedImage
import com.example.myweibo.data.FeedItem
import com.example.myweibo.data.mergeFeedTimelineRefresh
import com.example.myweibo.data.projectFeedTimelineRefresh
import com.example.myweibo.ui.FeedCard
import com.example.myweibo.ui.FloatingLongPostCollapseTag
import com.example.myweibo.ui.HomeLongPost
import com.example.myweibo.ui.HomeLongPostController
import com.example.myweibo.ui.HomeLongPostGlass
import com.example.myweibo.ui.HomeLongPostOverlay
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

/** Compose fixtures only: no service login, network, or full production refresh flow. */
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
    }
}
