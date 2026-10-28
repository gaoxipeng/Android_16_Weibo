package com.example.myweibo

import com.example.myweibo.data.WeiboJsonParser
import org.junit.Assert.*
import org.junit.Test

class UserPostSearchTest {
    @Test fun scopedSearchPreservesPlayableVideoAndCanExpandBody() {
        val item = WeiboJsonParser.parseUserSearchTimeline(
            """{"ok":1,"data":{"list":[{"idstr":"12345","text_raw":"正文预览","isLongText":true,"page_info":{"object_type":"video","type":"video","page_title":"视频","media_info":{"mp4_hd_url":"https://example.com/video.mp4","duration":30}}}]}}""", "42", 1,
        ).items.single()
        assertTrue(item.requiresLongTextFetch)
        assertEquals("https://example.com/video.mp4", item.medias.single().streamUrl)
        val full = WeiboJsonParser.mergeLongTextIntoFeedItem(item,
            org.json.JSONObject().put("longTextContent", "这是搜索结果中的完整微博正文"))
        assertEquals("这是搜索结果中的完整微博正文", full.text)
        assertFalse(full.requiresLongTextFetch)
        assertEquals(item.medias, full.medias)
    }

    @Test fun searchPromptUsesOnlyDeclaredGender() {
        val female = WeiboJsonParser.parseUserProfile("""{"data":{"user":{"idstr":"42","gender":"f"}}}""")
        assertEquals("搜索她的微博", female.postSearchPlaceholder)
        assertEquals("搜索他的微博", female.copy(gender = "m").postSearchPlaceholder)
        assertEquals("搜索TA的微博", female.copy(gender = null).postSearchPlaceholder)
    }
    @Test fun acceptsScopedResultsWithoutRepeatedOwner() {
        val result = WeiboJsonParser.parseUserSearchTimeline(
            """{"ok":1,"data":{"list":[{"idstr":"12345","text_raw":"测试关键词"}]}}""", "42", 1,
        )
        assertEquals(1, result.items.size)
        assertEquals("42", result.items.single().authorId)
    }

    @Test(expected = IllegalStateException::class)
    fun failureIsNotPresentedAsNoResults() {
        WeiboJsonParser.parseUserSearchTimeline("""{"ok":0,"message":"需要登录"}""", "42", 1)
    }

    @Test fun acceptsEmptySearch() {
        assertTrue(WeiboJsonParser.parseUserSearchTimeline("""{"ok":1,"data":{"list":[]}}""", "42", 1).items.isEmpty())
    }
}
