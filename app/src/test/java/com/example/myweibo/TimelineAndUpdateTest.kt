package com.example.myweibo

import com.example.myweibo.data.findTimelineChannel
import com.example.myweibo.data.webRecommendationChannel
import com.example.myweibo.data.isNewerRelease
import com.example.myweibo.data.WeiboJsonParser
import org.junit.Assert.*
import org.junit.Test

class TimelineAndUpdateTest {
    @Test fun recommendationEntryDoesNotRequireNamedCatalogueItem() {
        assertEquals("102803", webRecommendationChannel().groupId)
        assertEquals("102803", webRecommendationChannel().containerId)
    }

    @Test fun namedCatalogueLookupRemainsExact() {
        val raw = """{"groups":[{"title":"热门","gid":"102803"},{"title":"推荐","gid":"personal","containerid":"personal-feed"}]}"""
        assertEquals("personal", findTimelineChannel(raw, "推荐")?.groupId)
        assertEquals("personal-feed", findTimelineChannel(raw, "推荐")?.containerId)
        assertNull(findTimelineChannel("""{"groups":[{"title":"热门","gid":"102803"}]}""", "推荐"))
    }
    @Test fun releaseVersionsAreComparedNumerically() {
        assertTrue(isNewerRelease("20261022", "v20261022.1"))
        assertTrue(isNewerRelease("20261021", "v20261022"))
        assertFalse(isNewerRelease("20261021", "v20261021"))
        assertFalse(isNewerRelease("20261021", "v20261019"))
        assertTrue(isNewerRelease("1.9", "v1.10"))
        assertFalse(isNewerRelease("1.0", "v1.0.0"))
        assertFalse(isNewerRelease("1.0", "v2.0-beta"))
    }

    @Test fun cityChannelComesFromServerInsteadOfHardCodedCity() {
        val result = findTimelineChannel("""{"groups":[{"group":[{"title":"同城","gid":"city-group","containerid":"city-container"}]}]}""", "同城")
        assertEquals("city-group", result?.groupId)
        assertEquals("city-container", result?.containerId)
        assertNull(findTimelineChannel("""{"groups":[{"title":"热门","gid":"102803"}]}""", "同城"))
    }

    @Test fun recommendationWrappersKeepPaginationCursor() {
        val page = WeiboJsonParser.parseTimeline("""{"statuses":[{"mblog":{"idstr":"123","text":"推荐正文","user":{"idstr":"42","screen_name":"用户"}}}],"max_id":"next-page"}""")
        assertEquals("123", page.items.single().statusId)
        assertEquals("next-page", page.nextCursor)
    }
}
