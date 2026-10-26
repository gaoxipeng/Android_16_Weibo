package com.example.myweibo

import com.example.myweibo.data.WeiboJsonParser
import org.junit.Assert.*
import org.junit.Test

class EditHistoryTest {
    private val original = requireNotNull(WeiboJsonParser.parseStatusDetail(
        """{"idstr":"123","mblogid":"abc","text_raw":"当前正文","user":{"idstr":"42","screen_name":"作者","avatar_hd":"https://example.com/avatar.jpg"}}""",
    ))

    @Test fun repeatedRevisionIdsAreNotDeduplicatedAndMissingAuthorsAreInherited() {
        val page = WeiboJsonParser.parseEditHistory("""{"ok":1,"total":3,"statuses":[
            {"id":-1,"text_raw":"最新版本","created_at":"今天"},
            {"id":-1,"text_raw":"原始内容","created_at":"昨天"}] }""", original)
        assertEquals(3, page.total)
        assertEquals(listOf("最新版本", "原始内容"), page.items.map { it.text })
        assertTrue(page.items.all { it.authorName == "作者" && it.authorId == "42" })
        assertTrue(page.items.all { it.id == "123" && it.statusId == "abc" })
    }

    @Test fun nestedResponseAndEmptyHistoryAreSupported() {
        val page = WeiboJsonParser.parseEditHistory("""{"ok":1,"data":{"total":0,"statuses":[]}}""", original)
        assertEquals(0, page.total)
        assertTrue(page.items.isEmpty())
    }

    @Test(expected = IllegalStateException::class)
    fun serverErrorIsNotPresentedAsAnEmptyHistory() {
        WeiboJsonParser.parseEditHistory("""{"ok":0,"message":"请登录"}""", original)
    }
}
