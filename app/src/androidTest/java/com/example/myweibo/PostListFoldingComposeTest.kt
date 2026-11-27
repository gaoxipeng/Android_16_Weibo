package com.example.myweibo

import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.myweibo.data.FeedItem
import com.example.myweibo.data.ReadingPreferences
import com.example.myweibo.data.ReadingPreferencesStore
import com.example.myweibo.data.TimelineCacheStore
import com.example.myweibo.data.TimelineKind
import com.example.myweibo.ui.FeedCard
import com.example.myweibo.ui.FloatingLongPostCollapseTag
import com.example.myweibo.ui.HomeTimelineReadingState
import com.example.myweibo.ui.LocalHomeTimelineReadingState
import com.example.myweibo.ui.LongPostToggleTag
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Exercise the actual shared list card without Home's list controller or network data. */
@RunWith(AndroidJUnit4::class)
class PostListFoldingComposeTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun listBodyAndQuoteHaveIndependentInlineControls() {
        val post = fixturePost("outer").copy(retweetedStatus = fixturePost("quote"))
        var openedDetails = 0
        setFixture { _ ->
            LazyColumn {
                item(key = post.id) {
                    FeedCard(
                        item = post,
                        onClick = { openedDetails++ },
                        onRetweetCardClick = { _, _, _ -> openedDetails++ },
                        onMediaClick = { _, _ -> },
                        showAuthorRow = false,
                        showActionMenu = false,
                        showStatusActions = false,
                    )
                }
            }
        }

        val toggles = compose.onAllNodesWithTag(LongPostToggleTag)
        toggles.assertCountEquals(2)
        assertBody("outer", lines = 3, folded = true)
        assertBody("quote", lines = 3, folded = true)
        assertLabel(toggles[0], R.string.long_post_expand)
        assertLabel(toggles[1], R.string.long_post_expand)

        toggles[0].performClick()
        assertBody("outer", lines = 6, folded = false)
        assertBody("quote", lines = 3, folded = true)
        assertLabel(toggles[0], R.string.long_post_collapse)
        toggles[0].performClick()
        assertBody("outer", lines = 3, folded = true)

        toggles[1].performClick()
        assertBody("outer", lines = 3, folded = true)
        assertBody("quote", lines = 6, folded = false)
        assertLabel(toggles[1], R.string.long_post_collapse)
        toggles[1].performClick()
        assertBody("quote", lines = 3, folded = true)
        compose.onNodeWithTag(FloatingLongPostCollapseTag).assertDoesNotExist()
        compose.runOnIdle { assertEquals(0, openedDetails) }
    }

    @Test
    fun keyedListRowKeepsItsExpansionAndRespondsToLivePreferenceChanges() {
        val post = fixturePost("list")
        lateinit var owner: HomeTimelineReadingState
        setFixture { reading ->
            SideEffect { owner = reading }
            LazyColumn(Modifier.fillMaxSize().testTag("post-list")) {
                item(key = post.id) { Card(post) }
                items(8) { Box(Modifier.height(300.dp)) }
            }
        }

        val list = compose.onNodeWithTag("post-list")
        val toggle = compose.onNodeWithTag(LongPostToggleTag)
        toggle.performClick()
        assertBody("list", lines = 6, folded = false)
        list.performScrollToIndex(8)
        toggle.assertDoesNotExist()
        list.performScrollToIndex(0)
        assertBody("list", lines = 6, folded = false)
        assertLabel(toggle, R.string.long_post_collapse)

        compose.runOnIdle { owner.updatePreferences { it.copy(maxLines = 4) } }
        assertBody("list", lines = 4, folded = true)
        assertLabel(toggle, R.string.long_post_expand)
        compose.runOnIdle { owner.updatePreferences { it.copy(foldLongPosts = false) } }
        assertBody("list", lines = 6, folded = false)
        toggle.assertDoesNotExist()
    }

    @Test
    fun sharedDetailCardKeepsBothBodiesFullWithFoldingEnabled() {
        val post = fixturePost("detail").copy(retweetedStatus = fixturePost("detail-quote"))
        setFixture { _ ->
            LazyColumn {
                item { Card(post, sharedDetailCard = true) }
            }
        }
        assertBody("detail", lines = 6, folded = false)
        assertBody("detail-quote", lines = 6, folded = false)
        compose.onNodeWithTag(LongPostToggleTag).assertDoesNotExist()
        compose.onNodeWithTag(FloatingLongPostCollapseTag).assertDoesNotExist()
    }

    private fun setFixture(content: @Composable (HomeTimelineReadingState) -> Unit) {
        compose.setContent {
            val context = LocalContext.current
            val state = remember(context) {
                // The real observable settings owner, but don't touch the user's settings file.
                val isolated = object : ContextWrapper(context.applicationContext) {
                    override fun getApplicationContext(): Context = this
                    override fun getSharedPreferences(name: String, mode: Int) =
                        super.getSharedPreferences("post_list_test_$name", mode)
                }
                HomeTimelineReadingState(
                    ReadingPreferencesStore(isolated), TimelineCacheStore(context), TimelineKind.Following,
                    initialPreferences = ReadingPreferences(foldLongPosts = true, maxLines = 3),
                )
            }
            CompositionLocalProvider(
                LocalHomeTimelineReadingState provides state,
                LocalDensity provides Density(LocalDensity.current.density, 1f),
            ) {
                MaterialTheme { content(state) }
            }
        }
    }

    @Composable
    private fun Card(post: FeedItem, sharedDetailCard: Boolean = false) {
        FeedCard(
            item = post,
            onClick = {},
            onMediaClick = { _, _ -> },
            showAuthorRow = false,
            showActionMenu = false,
            showStatusActions = false,
            insetRounded = sharedDetailCard,
            sharedDetailCard = sharedDetailCard,
        )
    }

    private fun assertBody(which: String, lines: Int, folded: Boolean) {
        val layouts = mutableListOf<TextLayoutResult>()
        compose.onNodeWithText("$which:", substring = true, useUnmergedTree = true)
            .performSemanticsAction(SemanticsActions.GetTextLayoutResult) { it(layouts) }
        val layout = layouts.single()
        assertEquals(lines, layout.lineCount)
        assertEquals(folded, layout.didOverflowHeight)
        assertEquals(folded, layout.isLineEllipsized(lines - 1))
        assertEquals(if (folded) TextOverflow.Ellipsis else TextOverflow.Clip, layout.layoutInput.overflow)
    }

    private fun assertLabel(node: SemanticsNodeInteraction, label: Int) {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        node.assertIsDisplayed().assertTextEquals(context.getString(label))
    }

    private fun fixturePost(id: String) = FeedItem(
        id = id,
        statusId = id,
        authorId = "author",
        authorName = "Q",
        authorAvatarUrl = null,
        createdAt = "1700000000",
        source = null,
        ipLocation = null,
        text = "$id: one\ntwo\nthree\nfour\nfive\nsix",
        repostsCount = "0",
        commentsCount = "0",
        likesCount = "0",
        images = emptyList(),
    )
}
