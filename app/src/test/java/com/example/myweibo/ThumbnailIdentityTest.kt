package com.example.myweibo

import com.example.myweibo.data.FeedImage
import com.example.myweibo.data.FeedThumbnailQuality
import com.example.myweibo.data.ImageUrlResolver
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ThumbnailIdentityTest {
    private val feed = FeedImage(
        id = "photo123",
        thumbnailUrl = "https://wx1.sinaimg.cn/orj360/photo123.jpg",
        largeUrl = "https://wx1.sinaimg.cn/large/photo123.jpg",
    )

    @Test fun feedAndDetailShareKeyAcrossCdnSizeAndUrlIds() {
        val detail = feed.copy(
            id = "https://wx4.sinaimg.cn/mw2000/photo123.jpg?token=changed",
            thumbnailUrl = "https://wx4.sinaimg.cn/mw690/photo123.jpg",
            largeUrl = "https://wx4.sinaimg.cn/mw2000/photo123.jpg?token=changed",
        )
        assertEquals(
            ImageUrlResolver.thumbnailCacheKey(feed, FeedThumbnailQuality.Medium),
            ImageUrlResolver.thumbnailCacheKey(detail, FeedThumbnailQuality.Medium),
        )
    }

    @Test fun differentPhotosAndQualityStaySeparate() {
        assertNotEquals(
            ImageUrlResolver.thumbnailCacheKey(feed, FeedThumbnailQuality.Low),
            ImageUrlResolver.thumbnailCacheKey(feed, FeedThumbnailQuality.Medium),
        )
        assertNotEquals(
            ImageUrlResolver.thumbnailIdentity(feed),
            ImageUrlResolver.thumbnailIdentity(feed.copy(largeUrl = "https://wx1.sinaimg.cn/large/other.jpg")),
        )
    }

    @Test fun videoCoverSurvivesCdnVariantChanges() {
        assertEquals(
            ImageUrlResolver.videoCoverCacheKey("https://wx1.sinaimg.cn/orj480/cover.jpg"),
            ImageUrlResolver.videoCoverCacheKey("https://wx2.sinaimg.cn/large/cover.jpg?token=new"),
        )
        assertNull(ImageUrlResolver.videoCoverCacheKey(null))
    }

    @Test fun nonSinaUrlsDoNotCollideByFilename() {
        assertNotEquals(
            ImageUrlResolver.videoCoverCacheKey("https://example.com/a/cover.jpg"),
            ImageUrlResolver.videoCoverCacheKey("https://example.com/b/cover.jpg"),
        )
    }
}
