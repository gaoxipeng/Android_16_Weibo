package com.example.myweibo

import android.app.PictureInPictureParams
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

class VideoPipActivity : ComponentActivity() {
    private var player: ExoPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val streamUrl = intent.getStringExtra(EXTRA_STREAM_URL).orEmpty()
        val downloadUrl = intent.getStringExtra(EXTRA_DOWNLOAD_URL)
        val dashXml = intent.getStringExtra(EXTRA_DASH_XML)
        val aspectRatio = intent.getFloatExtra(EXTRA_ASPECT_RATIO, 16f / 9f)
        val startPositionMs = intent.getLongExtra(EXTRA_POSITION_MS, 0L)
        val speed = intent.getFloatExtra(EXTRA_SPEED, 1f)
        val candidates = listOfNotNull(dashXml?.let(::dashDataUri), streamUrl, downloadUrl)
            .flatMap(::videoUrlCandidates)
            .distinct()

        val playerView = PlayerView(this).apply {
            useController = false
            resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
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
                playerView.player = it
            }
            exoPlayer.setMediaSource(buildMediaSource(url))
            exoPlayer.prepare()
            if (startPositionMs > 0) exoPlayer.seekTo(startPositionMs)
            exoPlayer.setPlaybackSpeed(speed)
            exoPlayer.playWhenReady = true
        }

        player = ExoPlayer.Builder(this).build().also { exoPlayer ->
            playerView.player = exoPlayer
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

        playerView.post {
            enterPictureInPictureMode(pictureInPictureParams(aspectRatio))
        }
    }

    override fun onStop() {
        super.onStop()
        if (!isInPictureInPictureMode) finish()
    }

    override fun onDestroy() {
        player?.release()
        player = null
        super.onDestroy()
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
        const val EXTRA_DASH_XML = "dash_xml"
        const val EXTRA_ASPECT_RATIO = "aspect_ratio"
        const val EXTRA_POSITION_MS = "position_ms"
        const val EXTRA_SPEED = "speed"

        private const val DESKTOP_CHROME_USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 " +
                "(KHTML, like Gecko) Chrome/126.0.0.0 Safari/537.36"

        fun pictureInPictureParams(aspectRatio: Float): PictureInPictureParams {
            val safeRatio = aspectRatio.coerceIn(0.42f, 2.39f)
            return PictureInPictureParams.Builder()
                .setAspectRatio(Rational((safeRatio * 1000).toInt().coerceAtLeast(1), 1000))
                .build()
        }

        private fun videoUrlCandidates(url: String): List<String> {
            val trimmed = url.trim()
            if (trimmed.isBlank()) return emptyList()
            if (trimmed.startsWith("data:", ignoreCase = true)) return listOf(trimmed)
            if (!trimmed.startsWith("http://", ignoreCase = true)) return listOf(trimmed)
            return listOf(trimmed.replaceFirst("http://", "https://", ignoreCase = true), trimmed)
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
