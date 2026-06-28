@file:Suppress("UnsafeOptInUsageError")

package com.example.myweibo

import android.app.PictureInPictureParams
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.util.Rational
import android.view.ViewGroup
import android.webkit.CookieManager
import androidx.activity.ComponentActivity
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.dash.DashMediaSource
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.example.myweibo.data.FeedMedia
import com.example.myweibo.data.MediaUrlResolver

class VideoPipActivity : ComponentActivity() {
    private var player: ExoPlayer? = null
    private var playerView: PlayerView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val streamUrl = intent.getStringExtra(EXTRA_STREAM_URL).orEmpty()
        val downloadUrl = intent.getStringExtra(EXTRA_DOWNLOAD_URL)
        val replayUrl = intent.getStringExtra(EXTRA_REPLAY_URL)
        val liveStatus = intent.getIntExtra(EXTRA_LIVE_STATUS, Int.MIN_VALUE).takeIf { it != Int.MIN_VALUE }
        val dashXml = intent.getStringExtra(EXTRA_DASH_XML)
        val aspectRatio = intent.getFloatExtra(EXTRA_ASPECT_RATIO, 16f / 9f)
        val startPositionMs = intent.getLongExtra(EXTRA_POSITION_MS, 0L)
        val speed = intent.getFloatExtra(EXTRA_SPEED, 1f)
        val candidates = MediaUrlResolver.pipPlaybackCandidates(
            streamUrl = streamUrl,
            downloadUrl = downloadUrl,
            replayUrl = replayUrl,
            liveStatus = liveStatus,
            dashDataUri = dashXml?.let(::dashDataUri),
        )

        playerView = PlayerView(this).apply {
            useController = false
            resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
            keepScreenOn = true
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
            )
            setShutterBackgroundColor(android.graphics.Color.BLACK)
        }
        setContentView(playerView)

        var sourceIndex = 0
        fun prepare(index: Int) {
            val url = candidates.getOrNull(index) ?: return
            val exoPlayer = player ?: ExoPlayer.Builder(this).build().also {
                player = it
                playerView?.player = it
            }
            exoPlayer.setMediaSource(buildMediaSource(url))
            exoPlayer.prepare()
            if (startPositionMs > 0 && liveStatus != 1) exoPlayer.seekTo(startPositionMs)
            exoPlayer.setPlaybackSpeed(speed)
            exoPlayer.playWhenReady = true
        }

        player = ExoPlayer.Builder(this).build().also { exoPlayer ->
            playerView?.player = exoPlayer
            exoPlayer.addListener(object : Player.Listener {
                override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                    if (sourceIndex < candidates.lastIndex) {
                        sourceIndex += 1
                        prepare(sourceIndex)
                    }
                }
            })
        }
        prepare(sourceIndex)

        playerView?.post {
            enterPictureInPictureMode(pictureInPictureParams(aspectRatio))
        }
    }

    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean,
        newConfig: Configuration,
    ) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        if (!isInPictureInPictureMode) {
            stopAndReleasePlayer()
            finishAndRemoveTask()
        }
    }

    override fun onStop() {
        if (!isInPictureInPictureMode) {
            stopAndReleasePlayer()
            if (!isFinishing) {
                finishAndRemoveTask()
            }
        }
        super.onStop()
    }

    override fun onDestroy() {
        stopAndReleasePlayer()
        playerView = null
        super.onDestroy()
    }

    private fun stopAndReleasePlayer() {
        playerView?.keepScreenOn = false
        playerView?.player = null
        player?.let { exoPlayer ->
            exoPlayer.playWhenReady = false
            exoPlayer.stop()
            exoPlayer.clearMediaItems()
            exoPlayer.release()
        }
        player = null
    }

    private fun buildMediaSource(url: String) =
        when {
            url.isDashSource() -> DashMediaSource.Factory(dataSourceFactory()).createMediaSource(MediaItem.fromUri(url))
            url.contains("m3u8", ignoreCase = true) -> HlsMediaSource.Factory(dataSourceFactory()).createMediaSource(MediaItem.fromUri(url))
            else -> ProgressiveMediaSource.Factory(dataSourceFactory()).createMediaSource(MediaItem.fromUri(Uri.parse(url)))
        }

    private fun dataSourceFactory(): DefaultDataSource.Factory {
        val cookie = CookieManager.getInstance().getCookie("https://weibo.com/").orEmpty()
        val headers = buildMap {
            put("Accept", "*/*")
            put("Referer", "https://weibo.com/")
            put("Origin", "https://weibo.com")
            put("User-Agent", DESKTOP_CHROME_USER_AGENT)
            if (cookie.isNotBlank()) put("Cookie", cookie)
        }
        val httpFactory = DefaultHttpDataSource.Factory()
            .setUserAgent(DESKTOP_CHROME_USER_AGENT)
            .setDefaultRequestProperties(headers)
            .setAllowCrossProtocolRedirects(true)
            .setConnectTimeoutMs(12_000)
            .setReadTimeoutMs(20_000)
        return DefaultDataSource.Factory(this, httpFactory)
    }

    companion object {
        const val EXTRA_STREAM_URL = "stream_url"
        const val EXTRA_DOWNLOAD_URL = "download_url"
        const val EXTRA_REPLAY_URL = "replay_url"
        const val EXTRA_LIVE_STATUS = "live_status"
        const val EXTRA_DASH_XML = "dash_xml"
        const val EXTRA_ASPECT_RATIO = "aspect_ratio"
        const val EXTRA_POSITION_MS = "position_ms"
        const val EXTRA_SPEED = "speed"

        private const val DESKTOP_CHROME_USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 " +
                "(KHTML, like Gecko) Chrome/126.0.0.0 Safari/537.36"

        fun start(
            context: Context,
            media: FeedMedia,
            positionMs: Long,
            speed: Float,
            aspectRatio: Float,
        ) {
            val playbackUrl = media.resolvedPlaybackUrl() ?: media.streamUrl
            context.startActivity(
                Intent(context, VideoPipActivity::class.java).apply {
                    putExtra(EXTRA_STREAM_URL, playbackUrl)
                    media.downloadUrl?.let { putExtra(EXTRA_DOWNLOAD_URL, it) }
                    media.replayUrl?.let { putExtra(EXTRA_REPLAY_URL, it) }
                    media.liveStatus?.let { putExtra(EXTRA_LIVE_STATUS, it) }
                    putExtra(EXTRA_ASPECT_RATIO, aspectRatio)
                    putExtra(EXTRA_POSITION_MS, positionMs)
                    putExtra(EXTRA_SPEED, speed)
                },
            )
        }

        fun pictureInPictureParams(aspectRatio: Float): PictureInPictureParams {
            val safeRatio = aspectRatio.coerceIn(0.42f, 2.39f)
            return PictureInPictureParams.Builder()
                .setAspectRatio(Rational((safeRatio * 1000).toInt().coerceAtLeast(1), 1000))
                .build()
        }

        private fun dashDataUri(xml: String): String {
            val encoded = Base64.encodeToString(xml.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)
            return "data:application/dash+xml;base64,$encoded"
        }

        private fun String.isDashSource(): Boolean =
            startsWith("data:application/dash+xml", ignoreCase = true) ||
                contains(".mpd", ignoreCase = true)
    }
}
