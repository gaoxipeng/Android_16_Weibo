package com.example.myweibo

import com.example.myweibo.ui.isWeiboMessageInboxTitle
import com.example.myweibo.ui.isWeiboMessageInboxUrl
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MessageInboxRouteTest {
    @Test fun inboxUrlsStayOnTheTabBar() {
        assertTrue(isWeiboMessageInboxUrl("https://m.weibo.cn/message"))
        assertTrue(isWeiboMessageInboxUrl("https://m.weibo.cn/message/"))
        assertTrue(isWeiboMessageInboxUrl("https://m.weibo.cn/messages"))
        assertTrue(isWeiboMessageInboxUrl("https://m.weibo.cn/message/at"))
        assertTrue(isWeiboMessageInboxUrl("https://m.weibo.cn/message/comment"))
    }

    @Test fun directChatsHideTheTabBar() {
        assertFalse(isWeiboMessageInboxUrl("https://m.weibo.cn/message/chat?uid=123"))
        assertFalse(isWeiboMessageInboxUrl("https://m.weibo.cn/chat?uid=123"))
        assertFalse(isWeiboMessageInboxUrl("https://m.weibo.cn/message?uid=123"))
        assertFalse(isWeiboMessageInboxUrl("https://m.weibo.cn/message#/chat/123"))
    }

    @Test fun pageTitlesDistinguishInboxFromAChat() {
        assertTrue(isWeiboMessageInboxTitle("消息箱"))
        assertTrue(isWeiboMessageInboxTitle("评论"))
        assertFalse(isWeiboMessageInboxTitle("花心大挪卜"))
    }
}
