package com.example.myweibo

import com.example.myweibo.data.WeiboJsonParser
import org.junit.Assert.assertEquals
import org.junit.Test

class CommentLinkParseTest {
    @Test
    fun commentRegionDoesNotUseSelfReportedProfileLocation() {
        val comments = WeiboJsonParser.parseComments("""
            {"data":[
              {"idstr":"1","text":"评论","source":"来自黑龙江",
               "user":{"idstr":"2","screen_name":"测试","location":"其他"}},
              {"idstr":"3","text":"评论","user":{"idstr":"4","screen_name":"测试","location":"北京"}},
              {"idstr":"5","text":"评论","region_name":"其他","source":"来自广东",
               "user":{"idstr":"6","screen_name":"测试"}}
            ]}
        """)
        assertEquals("黑龙江", comments[0].ipLocation)
        assertEquals(null, comments[1].ipLocation)
        assertEquals("广东", comments[2].ipLocation)
    }

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
