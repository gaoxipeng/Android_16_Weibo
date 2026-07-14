package com.example.myweibo

import com.example.myweibo.data.WeiboJsonParser
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RepostVideoLinkParseTest {

    @Test
    fun repostOfVideoKeepsSingleLinkOnOriginalOnly() {
        val videoUrlA = "http://t.cn/videoA"
        val videoUrlB = "http://t.cn/videoB"
        val title = "三吼君的微博视频"
        val raw = """
            {
              "statuses": [
                {
                  "idstr": "1001",
                  "mblogid": "outer1",
                  "created_at": "Tue Jul 14 05:00:00 +0800 2026",
                  "text_raw": "转发理由//@幼儿园园长熊熊:懂了//@三吼君:原文 #非正经观战世界杯# $videoUrlA",
                  "text": "转发理由",
                  "reposts_count": 1,
                  "comments_count": 1,
                  "attitudes_count": 1,
                  "user": { "idstr": "1", "screen_name": "大猪蹄子研究所" },
                  "url_struct": [
                    {
                      "short_url": "$videoUrlA",
                      "url_title": "$title",
                      "url_type": 39,
                      "object_type": "video",
                      "object_id": "1034:4654123456789012",
                      "h5_target_url": "https://video.weibo.com/show?fid=1034:1"
                    }
                  ],
                  "analysis_extra": "mblog_rt_mid:2002",
                  "page_info": {
                    "type": "video",
                    "object_type": "video",
                    "page_title": "$title",
                    "page_url": "https://video.weibo.com/show?fid=1034:1",
                    "media_info": {
                      "author_mid": "2002",
                      "video_title": "$title",
                      "mp4_hd_url": "https://example.com/a.mp4",
                      "duration": 135
                    }
                  },
                  "retweeted_status": {
                    "idstr": "2002",
                    "mblogid": "inner1",
                    "created_at": "Tue Jul 14 04:00:00 +0800 2026",
                    "text_raw": "原博正文 #非正经观战世界杯# $videoUrlA $videoUrlB",
                    "text": "原博正文",
                    "reposts_count": 1,
                    "comments_count": 1,
                    "attitudes_count": 1,
                    "user": { "idstr": "2", "screen_name": "三吼君" },
                    "url_struct": [
                      {
                        "short_url": "$videoUrlA",
                        "url_title": "$title",
                        "url_type": 39,
                        "object_type": "video",
                        "h5_target_url": "https://video.weibo.com/show?fid=1034:1"
                      },
                      {
                        "short_url": "$videoUrlB",
                        "url_title": "$title",
                        "url_type": 39,
                        "object_type": "video",
                        "h5_target_url": "https://video.weibo.com/show?fid=1034:1"
                      }
                    ],
                    "page_info": {
                      "type": "video",
                      "object_type": "video",
                      "page_title": "$title",
                      "page_url": "https://video.weibo.com/show?fid=1034:1",
                      "media_info": {
                        "video_title": "$title",
                        "mp4_hd_url": "https://example.com/a.mp4",
                        "duration": 135
                      }
                    }
                  }
                }
              ]
            }
        """.trimIndent()

        val page = WeiboJsonParser.parseTimeline(raw)
        assertEquals(1, page.items.size)
        val outer = page.items.first()
        val original = requireNotNull(outer.retweetedStatus)

        assertTrue(outer.urlEntities.none { it.title.contains("微博视频") })
        assertFalse(outer.text.contains(videoUrlA))
        assertFalse(outer.text.contains(title))
        assertTrue(outer.locationName.isNullOrBlank())
        assertTrue(original.locationName.isNullOrBlank())

        val originalVideoLinks = original.urlEntities.filter { it.title.contains("微博视频") }
        assertEquals(1, originalVideoLinks.size)
        assertEquals(1, countOccurrences(original.text, originalVideoLinks.first().shortUrl))
        assertTrue(original.medias.isNotEmpty() || outer.medias.isNotEmpty())
    }

    @Test
    fun videoUrlStructIsNotParsedAsLocation() {
        val title = "三吼君的微博视频"
        val raw = """
            {
              "statuses": [
                {
                  "idstr": "3001",
                  "mblogid": "videoLoc",
                  "created_at": "Tue Jul 14 05:00:00 +0800 2026",
                  "text_raw": "正文 http://t.cn/videoLoc",
                  "text": "正文",
                  "reposts_count": 0,
                  "comments_count": 0,
                  "attitudes_count": 0,
                  "user": { "idstr": "3", "screen_name": "测试" },
                  "url_struct": [
                    {
                      "short_url": "http://t.cn/videoLoc",
                      "url_title": "$title",
                      "url_type": 39,
                      "object_type": "video",
                      "object_id": "1034:4654123456789012",
                      "h5_target_url": "https://video.weibo.com/show?fid=1034:4654123456789012"
                    }
                  ],
                  "page_info": {
                    "type": "video",
                    "object_type": "video",
                    "object_id": "1034:4654123456789012",
                    "page_title": "$title",
                    "page_url": "https://video.weibo.com/show?fid=1034:4654123456789012",
                    "media_info": {
                      "video_title": "$title",
                      "mp4_hd_url": "https://example.com/a.mp4",
                      "duration": 135
                    }
                  }
                }
              ]
            }
        """.trimIndent()

        val item = WeiboJsonParser.parseTimeline(raw).items.first()
        assertTrue(item.locationName.isNullOrBlank())
        assertTrue(item.locationUrl.isNullOrBlank())
    }

    @Test
    fun sanitizeCollapsesDuplicateVideoTokens() {
        val title = "三吼君的微博视频"
        val token = "http://t.cn/dup"
        val item = WeiboJsonParser.sanitizeRepostVideoLinks(
            com.example.myweibo.data.FeedItem(
                id = "1",
                statusId = "s1",
                authorId = "a",
                authorName = "三吼君",
                authorAvatarUrl = null,
                createdAt = null,
                source = null,
                ipLocation = null,
                text = "正文 $token $token",
                repostsCount = "0",
                commentsCount = "0",
                likesCount = "0",
                images = emptyList(),
                medias = emptyList(),
                urlEntities = listOf(
                    com.example.myweibo.data.FeedUrlEntity(
                        shortUrl = token,
                        title = title,
                        url = "https://video.weibo.com/show?fid=1",
                    ),
                ),
            ),
        )
        assertEquals(1, item.urlEntities.size)
        assertEquals(1, countOccurrences(item.text, token))
    }

    private fun countOccurrences(text: String, token: String): Int {
        if (token.isBlank()) return 0
        var count = 0
        var index = 0
        while (true) {
            val found = text.indexOf(token, index)
            if (found < 0) return count
            count += 1
            index = found + token.length
        }
    }
}
