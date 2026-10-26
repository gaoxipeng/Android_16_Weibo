package com.example.myweibo

import com.example.myweibo.data.WeiboJsonParser
import org.junit.Assert.*
import org.junit.Test

class SpecialFollowProfileTest {
    @Test fun parsesSpecialFollowingWithoutConfusingNormalFollowing() {
        val profile = WeiboJsonParser.parseUserProfile("""{"data":{"user":{"idstr":"42","screen_name":"测试用户","following":true,"special_follow":1}}}""")
        assertTrue(profile.following)
        assertTrue(profile.specialFollowing)
    }

    @Test fun absentSpecialFollowingIsNotAssumedFromFollowing() {
        val profile = WeiboJsonParser.parseUserProfile("""{"data":{"user":{"idstr":"42","screen_name":"测试用户","following":true}}}""")
        assertTrue(profile.following)
        assertFalse(profile.specialFollowing)
    }
}
