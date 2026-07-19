package com.example.myweibo

import com.example.myweibo.data.FeedItem
import com.example.myweibo.data.FeedMedia
import com.example.myweibo.data.MediaType
import com.example.myweibo.data.MediaUrlResolver
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MediaUrlResolverTest {
    @Test
    fun parseUrlDeadlineMs_readsExpiresSeconds() {
        val url = "https://f.video.weibocdn.com/video.mp4?Expires=1700000000&ssig=abc"
        assertEquals(1_700_000_000_000L, MediaUrlResolver.parseUrlDeadlineMs(url))
    }

    @Test
    fun isPlaybackUrlLikelyExpired_respectsSkew() {
        val deadlineSec = 1_700_000_000L
        val url = "https://f.video.weibocdn.com/video.mp4?Expires=$deadlineSec"
        assertTrue(MediaUrlResolver.isPlaybackUrlLikelyExpired(url, nowMs = deadlineSec * 1000L))
        assertTrue(
            MediaUrlResolver.isPlaybackUrlLikelyExpired(
                url,
                nowMs = deadlineSec * 1000L - 30_000L,
            ),
        )
        assertFalse(
            MediaUrlResolver.isPlaybackUrlLikelyExpired(
                url,
                nowMs = deadlineSec * 1000L - 120_000L,
            ),
        )
    }

    @Test
    fun pickRefreshedMedia_prefersMatchingCover() {
        val previous = FeedMedia(
            type = MediaType.Video,
            title = "旧标题",
            coverUrl = "https://cover/a.jpg",
            streamUrl = "https://cdn/old.mp4?Expires=1",
        )
        val detail = FeedItem(
            id = "1",
            statusId = "sid",
            authorId = "u",
            authorName = "n",
            authorAvatarUrl = null,
            text = "t",
            createdAt = "",
            source = null,
            ipLocation = null,
            repostsCount = "0",
            commentsCount = "0",
            likesCount = "0",
            images = emptyList(),
            medias = listOf(
                FeedMedia(
                    type = MediaType.Video,
                    title = "其他",
                    coverUrl = "https://cover/b.jpg",
                    streamUrl = "https://cdn/other.mp4?Expires=9",
                ),
                FeedMedia(
                    type = MediaType.Video,
                    title = "新标题",
                    coverUrl = "https://cover/a.jpg",
                    streamUrl = "https://cdn/new.mp4?Expires=9",
                ),
            ),
        )
        val picked = MediaUrlResolver.pickRefreshedMedia(previous, detail)
        assertEquals("https://cdn/new.mp4?Expires=9", picked?.streamUrl)
    }

    @Test
    fun pickRefreshedMedia_returnsNullWhenNoPlayableMedia() {
        val previous = FeedMedia(
            type = MediaType.Video,
            title = "视频",
            coverUrl = null,
            streamUrl = "https://cdn/old.mp4",
        )
        val detail = FeedItem(
            id = "1",
            statusId = "sid",
            authorId = "u",
            authorName = "n",
            authorAvatarUrl = null,
            text = "t",
            createdAt = "",
            source = null,
            ipLocation = null,
            repostsCount = "0",
            commentsCount = "0",
            likesCount = "0",
            images = emptyList(),
            medias = emptyList(),
        )
        assertNull(MediaUrlResolver.pickRefreshedMedia(previous, detail))
    }
}
