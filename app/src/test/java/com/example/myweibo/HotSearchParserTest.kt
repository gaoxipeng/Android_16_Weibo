package com.example.myweibo

import com.example.myweibo.data.WeiboJsonParser
import org.junit.Assert.*
import org.junit.Test

class HotSearchParserTest {
    @Test fun personalBoardUsesRealtimeAndSnakeCaseBadge() {
        val result = WeiboJsonParser.parseHotSearch("""{"ok":1,"data":{"realtime":[
            {"word":"#我的话题#","icon_desc":"热","num":123},
            {"word":"广告","is_ad":1},
            {"word":"  "},
            {"word":"第二条","small_icon_desc":"新"}
        ]}}""")
        assertEquals(listOf("#我的话题#", "第二条"), result.map { it.word })
        assertEquals(listOf("热", "新"), result.map { it.label })
        assertEquals(123L, result.first().heat)
    }

    @Test fun standardBoardStillUsesExistingBadgeFields() {
        val result = WeiboJsonParser.parseHotSearch("""{"data":{"realtime":[
            {"word":"一","labelName":"爆"},{"word":"二","iconDesc":"沸"}
        ]}}""")
        assertEquals(listOf("爆", "沸"), result.map { it.label })
        assertTrue(WeiboJsonParser.parseHotSearch("""{"ok":1,"data":{"realtime":[]}}""").isEmpty())
    }
}
