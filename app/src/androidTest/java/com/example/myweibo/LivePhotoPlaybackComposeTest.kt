package com.example.myweibo

import android.graphics.Bitmap
import android.net.Uri
import android.view.View
import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.media3.common.Player
import androidx.media3.ui.PlayerView
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.myweibo.data.FeedImage
import com.example.myweibo.ui.ZoomableFullscreenImage
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.nio.file.Files
import java.util.concurrent.atomic.AtomicBoolean

@RunWith(AndroidJUnit4::class)
class LivePhotoPlaybackComposeTest {
    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun tappingLiveAfterTerminalFailureRendersAFrameOnTheSamePlayer() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val tempDir = Files.createTempDirectory(instrumentation.targetContext.cacheDir.toPath(), "live-photo-").toFile()
        try {
            val clip = File(tempDir, "motion.mp4")
            val staged = File(tempDir, "staged.mp4")
            // This is a generated AVC test pattern, not a clip from Weibo.
            instrumentation.context.assets.open("live_photo_retry_fixture.mp4").use { asset ->
                staged.outputStream().use { asset.copyTo(it) }
            }
            val still = Bitmap.createBitmap(96, 96, Bitmap.Config.ARGB_8888)
            val image = FeedImage(
                id = tempDir.name,
                thumbnailUrl = "",
                largeUrl = "",
                livePhotoVideoUrl = Uri.fromFile(clip).toString(),
                width = still.width,
                height = still.height,
            )
            val pageActive = mutableStateOf(true)
            compose.setContent {
                ZoomableFullscreenImage(
                    image = image,
                    allImages = listOf(image),
                    transitionPreviewBitmap = still,
                    resolvedForViewer = true,
                    isActive = pageActive.value,
                    onDismiss = {},
                )
            }

            // The clip does not exist yet. Observe the real Media3 terminal error before restoring it.
            compose.waitUntil(timeoutMillis = 15_000) {
                compose.runOnIdle { currentPlayer()?.playerError != null }
            }
            val liveBadge = compose.onNodeWithContentDescription("Live Photo")
            liveBadge.assertIsDisplayed()
            val renderedFrame = AtomicBoolean(false)
            val failedPlayer = compose.runOnIdle {
                checkNotNull(currentPlayer()).also { player ->
                    player.addListener(object : Player.Listener {
                        override fun onRenderedFirstFrame() {
                            renderedFrame.set(true)
                        }
                    })
                }
            }

            assertTrue(staged.renameTo(clip))
            // Reactivating a still-composed page should not implicitly retry the failed player.
            compose.runOnIdle { pageActive.value = false }
            compose.waitForIdle()
            compose.runOnIdle { pageActive.value = true }
            liveBadge.assertIsDisplayed()
            compose.runOnIdle {
                assertSame(failedPlayer, currentPlayer())
                assertNotNull(failedPlayer.playerError)
            }

            liveBadge.performClick()
            compose.waitUntil(timeoutMillis = 15_000) { renderedFrame.get() }
            compose.runOnIdle { assertSame(failedPlayer, currentPlayer()) }
        } finally {
            tempDir.deleteRecursively()
        }
    }

    private fun currentPlayer(): Player? = compose.activity.window.decorView.findPlayerView()?.player

    private fun View.findPlayerView(): PlayerView? {
        if (this is PlayerView) return this
        if (this is ViewGroup) {
            for (index in 0 until childCount) {
                getChildAt(index).findPlayerView()?.let { return it }
            }
        }
        return null
    }
}
