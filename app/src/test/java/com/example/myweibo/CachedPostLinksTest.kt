package com.example.myweibo

import com.example.myweibo.data.*
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import java.io.File
import java.nio.file.Files

class CachedPostLinksTest {
    @Test fun cachePreservesPictureAndWebLinksIncludingReposts() = runBlocking {
        val dir = Files.createTempDirectory("weibo-link-cache").toFile()
        try {
            val base = requireNotNull(WeiboJsonParser.parseStatusDetail(
                """{"idstr":"123","text":"图片评论 http://t.cn/photo","user":{"idstr":"42","screen_name":"作者"}}""",
            ))
            val image = FeedImage("pic", "https://example.com/s.jpg", "https://example.com/l.jpg")
            val item = base.copy(
                inlineImageLinks = mapOf("http://t.cn/photo" to listOf(image)),
                urlEntities = listOf(FeedUrlEntity("http://t.cn/page", "网页", "https://example.com", "sinaweibo://browser")),
            )
            val store = MineCacheStore(dir)
            store.writePosts("42", MinePostsCache(listOf(item.copy(retweetedStatus = item))))
            val cached = requireNotNull(store.readPosts("42"))
            assertFalse(cached.needsLinkRefresh)
            assertEquals(item.inlineImageLinks, cached.items.single().inlineImageLinks)
            assertEquals(item.urlEntities, cached.items.single().urlEntities)
            assertEquals(item.inlineImageLinks, cached.items.single().retweetedStatus!!.inlineImageLinks)
            assertEquals(item.urlEntities, cached.items.single().retweetedStatus!!.urlEntities)
        } finally { dir.deleteRecursively() }
    }

    @Test fun legacyCacheRequestsMetadataRefreshWithoutDiscardingPosts() = runBlocking {
        val dir = Files.createTempDirectory("weibo-legacy-cache").toFile()
        try {
            File(dir, "42_posts.json").writeText("""{"items":[{"id":"123","text":"图片评论 http://t.cn/photo"}]}""")
            val cached = requireNotNull(MineCacheStore(dir).readPosts("42"))
            assertTrue(cached.needsLinkRefresh)
            assertEquals("123", cached.items.single().id)
        } finally { dir.deleteRecursively() }
    }

    @Test fun bareLinksExcludeBracketsEmojiAndChineseText() {
        val urls = completeBareUrlEntities("分享：【http://t.cn/AXOIEQJ】 [https://example.com/a] http://t.cn/ok[赞啊]中文", emptyMap())
        assertEquals(setOf("http://t.cn/AXOIEQJ", "https://example.com/a", "http://t.cn/ok"), urls.keys)
    }

    @Test fun structuredAndPictureLinksAreNotOverwritten() {
        val link = FeedUrlEntity("http://t.cn/page", "微博正文", "https://weibo.com/42/abc")
        val result = completeBareUrlEntities("http://t.cn/page http://t.cn/photo", mapOf(link.shortUrl to link), setOf("http://t.cn/photo"))
        assertEquals(mapOf(link.shortUrl to link), result)
    }
}
