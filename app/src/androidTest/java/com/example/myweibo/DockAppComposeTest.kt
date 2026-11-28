package com.example.myweibo

import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.click
import androidx.compose.ui.test.hasScrollToIndexAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTouchInput
import androidx.test.espresso.Espresso.pressBack
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.myweibo.data.TimelineKind
import com.example.myweibo.data.DockPreferences
import com.example.myweibo.data.DockPreferencesStore
import com.example.myweibo.ui.MainTab
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExternalResource
import org.junit.rules.RuleChain
import org.junit.runner.RunWith

/** Run on a fresh, signed-out, offline app fixture: production startup opens a WebView URL. */
@RunWith(AndroidJUnit4::class)
class DockAppComposeTest {
    private val compose = createAndroidComposeRule<MainActivity>()

    // Reset only the dock's preferences, before MainActivity reads them; restore after it closes.
    @get:Rule
    val rules: RuleChain = RuleChain.outerRule(object : ExternalResource() {
        private lateinit var store: DockPreferencesStore
        private lateinit var original: DockPreferences

        override fun before() {
            store = DockPreferencesStore(InstrumentationRegistry.getInstrumentation().targetContext)
            original = store.read()
            store.write(DockPreferences())
        }

        override fun after() = store.write(original)
    }).around(compose)

    @Test fun searchFromHomeBackReturnsToHome() {
        assertSignedOutHome()
        dock(MainTab.Search).assertIsDisplayed().performTouchInput { click() }
        dock(MainTab.Search).assertIsSelected()
        val searchPage = compose.onNodeWithText("登录后即可查看热搜与搜索结果。")
        searchPage.assertIsDisplayed()

        pressBack()

        assertSignedOutHome()
        searchPage.assertDoesNotExist()
    }

    @Test fun searchLoginThenExplicitHomeDoesNotBackToMine() {
        assertSignedOutHome()
        dock(MainTab.Mine).performClick()
        dock(MainTab.Search).performClick()
        compose.onNodeWithText("打开登录").performClick()
        val accountManagement = hasText("账号管理")
        compose.waitUntil(timeoutMillis = 8_000) {
            compose.onAllNodes(accountManagement).fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNode(accountManagement).assertIsDisplayed()

        pressBack() // Account management dialog
        pressBack() // Settings
        dock(MainTab.Mine).assertIsSelected()
        dock(MainTab.Feed).performClick()
        assertSignedOutHome()

        pressBack()

        assertSignedOutHome()
        dock(MainTab.Mine).assertIsDisplayed()
    }

    @Test fun mineSettingsDockChangesSurviveRecreateAndMovedHomeStillLongPresses() {
        assertSignedOutHome()
        dock(MainTab.Mine).performClick()
        compose.onNodeWithContentDescription("设置").assertIsDisplayed().performClick()
        compose.onNodeWithText("设置").assertIsDisplayed()
        compose.onNode(hasScrollToIndexAction()).performScrollToNode(hasText("底栏"))
        compose.onNodeWithText("底栏").assertIsDisplayed()

        val messages = "dock_visibility_MESSAGES"
        settingsControl(messages).assertIsOn().performClick().assertIsOff()
        settingsControl("dock_down_HOME").performClick()
        pressBack()
        assertConfiguredDock()

        compose.activityRule.scenario.recreate()

        assertSignedOutHome()
        assertConfiguredDock()
        val timelineMenuItem = compose.onNodeWithText(TimelineKind.SpecialFollowing.label)
        timelineMenuItem.assertDoesNotExist()
        dock(MainTab.Feed).performTouchInput { longClick() }
        timelineMenuItem.assertIsDisplayed()
        dock(MainTab.Feed).assertIsSelected()
    }

    private fun settingsControl(tag: String): SemanticsNodeInteraction {
        compose.onNode(hasScrollToIndexAction()).performScrollToNode(hasTestTag(tag))
        return compose.onNodeWithTag(tag).assertIsDisplayed()
    }

    private fun assertSignedOutHome() {
        dock(MainTab.Feed).assertIsDisplayed().assertIsSelected()
        val title = hasText("本地还没有缓存的信息流。", substring = true)
        compose.waitUntil(timeoutMillis = 8_000) {
            compose.onAllNodes(title).fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNode(title).assertIsDisplayed()
    }

    private fun assertConfiguredDock() {
        dock(MainTab.Messages).assertDoesNotExist()
        dock(MainTab.Search).assertIsDisplayed()
        dock(MainTab.Mine).assertIsDisplayed()
        val homeX = dock(MainTab.Feed).fetchSemanticsNode().boundsInRoot.center.x
        val composeX = dock(MainTab.Compose).fetchSemanticsNode().boundsInRoot.center.x
        assertTrue("Home should have moved after Compose", homeX > composeX)
    }

    private fun dock(tab: MainTab) = compose.onNodeWithContentDescription(tab.label)
}
