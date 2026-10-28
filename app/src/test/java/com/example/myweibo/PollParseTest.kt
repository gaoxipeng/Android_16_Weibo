package com.example.myweibo

import com.example.myweibo.data.WeiboJsonParser
import org.junit.Assert.*
import org.junit.Test

class PollParseTest {
    @Test fun parsesH5CurrentAccountStateWithoutExecutingScripts() {
        val bootstrap = com.example.myweibo.data.parseVotePageBootstrap("""<script>window.__DATA__={"sk":"test-only","vote_info":{"id":"poll_1","title":"问题","status":1,"parted":1,"part_num":"7","maximum":1,"option_list":[{"id":"a","title":"甲","selected":1,"part_num":"5"},{"id":"b","title":"乙","selected":0,"part_num":"2"}]}};</script>""")
        val poll = com.example.myweibo.data.pollFromVoteInfo(bootstrap.getJSONObject("vote_info"))
        assertEquals("poll_1", poll.id)
        assertTrue(poll.hasVoted)
        assertFalse(poll.isEnded)
        assertEquals(7L, poll.participantCount)
        assertEquals("a", poll.options.first().id)
        assertTrue(poll.options.first().selected)
        assertEquals(listOf(5L, 2L), poll.options.map { it.count })
    }
    @Test(expected = IllegalStateException::class)
    fun loginPageCannotBeUsedForSubmittingVotes() {
        com.example.myweibo.data.parseVotePageBootstrap("<html>请登录</html>")
    }
    @Test fun preservesFormattedServerVoteTotals() {
        val poll = com.example.myweibo.data.pollFromVoteInfo(org.json.JSONObject(
            """{"id":"poll","parted":1,"option_list":[{"id":"a","title":"甲","selected":1,"part_num":"1.2万"},{"id":"b","title":"乙","part_num":"0"}]}""",
        ))
        assertEquals("1.2万", poll.options.first().countText)
        assertNull(poll.options.first().count)
        assertEquals(0L, poll.options.last().count)
    }
    @Test fun parsesObservedHudongVoteCard() {
        val item = WeiboJsonParser.parseTimeline("""{"statuses":[{"idstr":"5359092282688362","text_raw":"古文问答","page_info":{"object_type":"hudongvote","page_title":"出自下列哪篇作品","card_info":{"card_type":122,"vote_object":{"content":"出自下列哪篇作品","part_info":1,"expire_date":1793679777,"target_url":"https://vote.weibo.com/h5/index/index?vote_id=2026_10583312_-_8a3bb2","vote_list":[{"content":"《滕王阁序》","selected":1,"part_num":"1"},{"content":"《醉翁亭记》","selected":0,"part_num":"0"},{"content":"《赤壁赋》","part_num":"0"},{"content":"《岳阳楼记》","part_num":"0"}]}}}}]}""").items.single()
        val poll = requireNotNull(item.poll)
        assertEquals(4, poll.options.size)
        assertEquals("《滕王阁序》", poll.options.first().text)
        assertTrue(poll.options.first().selected)
        assertEquals(1L, poll.options.first().count)
        assertEquals(0L, poll.options.last().count)
        assertEquals(1L, poll.participantCount)
        assertEquals("1793679777", poll.endTime)
    }
    @Test fun parsesExplicitPollWithoutInventingHiddenCounts() {
        val item = WeiboJsonParser.parseTimeline("""{"statuses":[{"idstr":"123","text_raw":"投票","vote_info":{"title":"选一个","total":1,"options":[{"text":"甲","count":0},{"text":"乙"}],"end_time":"1800000000"}}]}""").items.single()
        assertEquals("选一个", item.poll?.title)
        assertEquals(0L, item.poll?.options?.first()?.count)
        assertNull(item.poll?.options?.last()?.count)
        assertEquals(1L, item.poll?.participantCount)
    }
    @Test fun recognizesVotePageAndNestedRetweet() {
        val item = WeiboJsonParser.parseTimeline("""{"statuses":[{"idstr":"123","text_raw":"转发","retweeted_status":{"idstr":"456","text_raw":"问题","page_info":{"object_type":"vote","page_title":"测试投票","page_url":"https://vote.weibo.com/poll/123"}}}]}""").items.single()
        assertNull(item.poll)
        assertEquals("测试投票", item.retweetedStatus?.poll?.title)
        assertTrue(item.retweetedStatus?.poll?.options?.isEmpty() == true)
    }
    @Test fun ordinaryPostMentioningPollIsNotAPoll() {
        assertNull(WeiboJsonParser.parseTimeline("""{"statuses":[{"idstr":"123","text_raw":"谈谈投票"}]}""").items.single().poll)
    }
}
