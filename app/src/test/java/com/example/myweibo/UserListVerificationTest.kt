package com.example.myweibo

import com.example.myweibo.data.*
import org.junit.Assert.*
import org.junit.Test

class UserListVerificationTest {
    @Test fun relationsCarryBadgeIntoProfileAndMentions() {
        val user = WeiboJsonParser.parseFriendsPage(
            """{"users":[{"idstr":"1","screen_name":"央视新闻","verified":true,"verified_type":3,"verified_type_ext":53}]}""",
        ).items.single()
        assertEquals(UserVerification.Red, user.verification)
        assertEquals(UserVerification.Red, user.toUserProfile().verification)
        assertEquals(UserVerification.Red, user.toMentionCandidate()!!.verification)
    }

    @Test fun suggestionsCarryBadgeIntoUserSearchResults() {
        val user = WeiboJsonParser.parseSearchSuggest(
            """{"data":{"users":[{"idstr":"2","screen_name":"作者","verified":true,"verified_type":0,"verified_type_ext":2}]}}""",
        ).users.single()
        assertEquals(UserVerification.Orange, user.verification)
        assertEquals(UserVerification.Orange, user.toRelationUser().verification)
        assertEquals(UserVerification.Orange, user.toRelationUser().toUserProfile().verification)
    }

    @Test fun likeUserListUsesOfficialBadgeMapping() {
        val user = WeiboJsonParser.parseMweiboAttitudes(
            """{"ok":1,"data":{"data":[{"user":{"id":"3","screen_name":"机构","verified":true,"verified_type":2}}]}}""",
        ).users.single()
        assertEquals(UserVerification.Blue, user.verification)
    }

    @Test fun searchMarkupUsesBadgeClassesNotMembershipOrPlainText() {
        assertEquals(UserVerification.Gold, parseHtmlUserVerification("""<span class="woo-icon-wrap woo-icon--vgold"></span>"""))
        assertEquals(UserVerification.Blue, parseHtmlUserVerification("""<i class="icon_approve_co"></i>"""))
        assertNull(parseHtmlUserVerification("""<i class="woo-icon--vip"></i><p>woo-icon--vgold</p>"""))
    }
}
