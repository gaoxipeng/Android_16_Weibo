package com.example.myweibo

import com.example.myweibo.data.WeiboJsonParser
import com.example.myweibo.data.WeiboPostVisibility
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class StatusVisibilityTest {
    @Test fun friendsPublishingAndModificationUseDifferentValues() {
        assertEquals(6, WeiboPostVisibility.FriendsCircle.apiValue)
        assertEquals(2, WeiboPostVisibility.FriendsCircle.modifyApiValue)
        assertEquals(WeiboPostVisibility.FriendsCircle, WeiboPostVisibility.fromApiValue(2))
        assertEquals("设为好友可见", WeiboPostVisibility.FriendsCircle.changeLabel)
        assertEquals("仅粉丝可见", WeiboPostVisibility.Followers.scopeLabel)
    }
    private fun status(visible: Any? = null): JSONObject = JSONObject()
        .put("idstr", "123456789")
        .put("text", "测试微博")
        .put("user", JSONObject().put("idstr", "42").put("screen_name", "作者"))
        .apply { if (visible != null) put("visible", visible) }

    @Test fun parsesAllSupportedObjectVisibilityValues() {
        WeiboPostVisibility.entries.forEach { visibility ->
            val raw = status(JSONObject().put("type", visibility.apiValue)).toString()
            assertEquals(visibility, WeiboJsonParser.parseStatusDetail(raw)?.visibility)
        }
    }

    @Test fun parsesScalarVisibility() {
        assertEquals(WeiboPostVisibility.Private, WeiboJsonParser.parseStatusDetail(status(1).toString())?.visibility)
    }

    @Test fun absentOrUnknownVisibilityDoesNotInventRestriction() {
        assertNull(WeiboJsonParser.parseStatusDetail(status().toString())?.visibility)
        assertNull(WeiboJsonParser.parseStatusDetail(status(JSONObject().put("type", 99)).toString())?.visibility)
    }

    @Test fun retweetKeepsIndependentVisibility() {
        val raw = status(0).put("retweeted_status", status(JSONObject().put("type", 6)).put("idstr", "987654321"))
        val parsed = WeiboJsonParser.parseStatusDetail(raw.toString())!!
        assertEquals(WeiboPostVisibility.Public, parsed.visibility)
        assertEquals(WeiboPostVisibility.FriendsCircle, parsed.retweetedStatus?.visibility)
    }
}
