package com.example.myweibo

import com.example.myweibo.data.UserVerification
import com.example.myweibo.data.WeiboJsonParser
import org.junit.Assert.*
import org.junit.Test

class VerificationTest {
    @Test fun cctvNewsUsesCurrentWebRedOrganizationBadge() {
        val profile = WeiboJsonParser.parseUserProfile(
            """{"data":{"user":{"id":2656274875,"screen_name":"央视新闻","verified":true,"verified_type":3,"verified_type_ext":53,"icon_list":[{"type":"vip","data":{"mbtype":12,"vvip":1}}]}}}""",
        )
        assertEquals(UserVerification.Red, profile.verification)
    }

    @Test fun usesVerificationRatherThanMembership() {
        fun profile(fields: String) = WeiboJsonParser.parseUserProfile("""{"data":{"user":{$fields}}}""")
        assertEquals(UserVerification.Gold, profile(""""verified":true,"verified_type":0,"verified_type_ext":1""").verification)
        assertEquals(UserVerification.Orange, profile(""""verified":true,"verified_type":0,"verified_type_ext":2""").verification)
        assertEquals(UserVerification.Yellow, profile(""""verified":true,"verified_type":0,"verified_type_ext":0""").verification)
        assertEquals(UserVerification.Red, profile(""""verified":true,"verified_type":3,"verified_type_ext":53""").verification)
        assertEquals(UserVerification.Grey, profile(""""verified":true,"verified_type":3,"verified_type_ext":-1""").verification)
        assertNull(profile(""""verified":false,"verified_type":0,"mbtype":12""").verification)
        assertNull(profile(""""verified_type":220""").verification)
    }
    @Test fun preservesNestedAuthorsAndCommentVerification() {
        val item = WeiboJsonParser.parseTimeline("""{"statuses":[{"idstr":"1","text_raw":"转发","user":{"verified_type":0},"retweeted_status":{"idstr":"2","text_raw":"原文","user":{"verified_type":3}}}]}""").items.single()
        assertEquals(UserVerification.Yellow, item.verification)
        assertEquals(UserVerification.Blue, item.retweetedStatus?.verification)
        val comment = WeiboJsonParser.parseComments("""{"data":[{"idstr":"3","text":"评论","user":{"verified_type":3}}]}""").single()
        assertEquals(UserVerification.Blue, comment.verification)
    }

    @Test fun matchesCurrentWebOrganizationMapping() {
        for (type in 1..7) {
            for (extension in listOf(-1, 0, 1, 2, 53)) {
                val profile = WeiboJsonParser.parseUserProfile(
                    """{"data":{"user":{"verified":true,"verified_type":$type,"verified_type_ext":$extension}}}""",
                )
                val expected = when {
                    extension == -1 -> UserVerification.Grey
                    type == 3 && extension == 53 -> UserVerification.Red
                    else -> UserVerification.Blue
                }
                assertEquals("type=$type ext=$extension", expected, profile.verification)
            }
        }
    }
}
