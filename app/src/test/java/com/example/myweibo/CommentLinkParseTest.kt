package com.example.myweibo

import com.example.myweibo.data.WeiboJsonParser
import org.junit.Assert.assertEquals
import org.junit.Test

class CommentLinkParseTest {
    @Test
    fun adjacentEmoticonIsNotPartOfShortLink() {
        val comment = WeiboJsonParser.parseComments("""
            {"data":[{"idstr":"1","text_raw":"http://t.cn/AXIYwS5a[赞啊]",
            "text":"http://t.cn/AXIYwS5a<img alt='[赞啊]' src='https://example.com/like.png'>",
            "user":{"idstr":"2","screen_name":"测试"}}]}
        """).single()
        assertEquals("http://t.cn/AXIYwS5a[赞啊]", comment.text)
        assertEquals(listOf("http://t.cn/AXIYwS5a"), comment.urlEntities.map { it.shortUrl })
        assertEquals("https://example.com/like.png", comment.emoticons["[赞啊]"])
    }
}
