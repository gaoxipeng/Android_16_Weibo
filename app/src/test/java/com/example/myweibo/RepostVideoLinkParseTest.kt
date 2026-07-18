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

    @Test
    fun longTextWithMixMediaShowsReadFullButton() {
        val preview = "甲".repeat(200)
        val raw = """
            {
              "statuses": [
                {
                  "idstr": "5720474518001",
                  "mblogid": "R8mcLDMhx",
                  "isLongText": true,
                  "created_at": "Tue Jul 14 08:00:00 +0800 2026",
                  "text_raw": "$preview",
                  "text": "$preview",
                  "pic_num": 12,
                  "pic_ids": [],
                  "reposts_count": 1,
                  "comments_count": 1,
                  "attitudes_count": 1,
                  "user": { "idstr": "5720474518", "screen_name": "测试用户" },
                  "mix_media_info": {
                    "items": [
                      {
                        "type": "video",
                        "data": {
                          "media_info": {
                            "mp4_hd_url": "https://example.com/v1.mp4",
                            "duration": 12
                          }
                        }
                      },
                      {
                        "type": "video",
                        "data": {
                          "media_info": {
                            "mp4_hd_url": "https://example.com/v2.mp4",
                            "duration": 20
                          }
                        }
                      }
                    ]
                  }
                }
              ]
            }
        """.trimIndent()

        val item = WeiboJsonParser.parseTimeline(raw).items.first()
        assertFalse(item.isLongText)
        assertTrue(item.requiresLongTextFetch)
    }

    @Test
    fun shortVideoCaptionDoesNotShowReadFullButton() {
        val raw = """
            {
              "statuses": [
                {
                  "idstr": "9001",
                  "mblogid": "shortVid",
                  "isLongText": true,
                  "created_at": "Tue Jul 14 08:00:00 +0800 2026",
                  "text_raw": "今日播报",
                  "text": "今日播报",
                  "reposts_count": 1,
                  "comments_count": 1,
                  "attitudes_count": 1,
                  "user": { "idstr": "1", "screen_name": "媒体" },
                  "page_info": {
                    "type": "video",
                    "object_type": "video",
                    "page_title": "媒体的微博视频",
                    "media_info": {
                      "mp4_hd_url": "https://example.com/a.mp4",
                      "duration": 60
                    }
                  }
                }
              ]
            }
        """.trimIndent()

        val item = WeiboJsonParser.parseTimeline(raw).items.first()
        assertFalse(item.isLongText)
        assertTrue(item.requiresLongTextFetch)
    }

    @Test
    fun longTextMergeKeepsVideoProgressTimestamp() {
        val seekUrl = "http://t.cn/seek1029"
        val previewItem = com.example.myweibo.data.FeedItem(
            id = "2955878834",
            statusId = "R8yArhqxy",
            authorId = "2955878834",
            authorName = "影石刘靖康",
            authorAvatarUrl = null,
            createdAt = null,
            source = null,
            ipLocation = null,
            text = "视频里的 $seekUrl @影石Insta360",
            isLongText = true,
            requiresLongTextFetch = true,
            repostsCount = "0",
            commentsCount = "0",
            likesCount = "0",
            images = emptyList(),
            medias = listOf(
                com.example.myweibo.data.FeedMedia(
                    type = com.example.myweibo.data.MediaType.Video,
                    title = "影石刘靖康的微博视频",
                    coverUrl = null,
                    streamUrl = "https://example.com/v.mp4",
                ),
            ),
            urlEntities = listOf(
                com.example.myweibo.data.FeedUrlEntity(
                    shortUrl = seekUrl,
                    title = "10:29",
                    url = "https://video.weibo.com/show?fid=1034:1&t=629",
                ),
            ),
        )
        val longTextPayload = JSONObject(
            """
            {
              "longTextContent": "视频里的<a data-url=\"$seekUrl\" href=\"https://video.weibo.com/show?fid=1034:1&t=629\">▷10:29</a> @影石Insta360 完整正文",
              "longTextContent_raw": "视频里的完整正文被挤到后面 $seekUrl",
              "url_struct": [
                {
                  "short_url": "$seekUrl",
                  "url_title": "10:29",
                  "url_type": 39,
                  "object_type": "video",
                  "h5_target_url": "https://video.weibo.com/show?fid=1034:1&t=629"
                }
              ]
            }
            """.trimIndent(),
        )

        val merged = WeiboJsonParser.mergeLongTextIntoFeedItem(previewItem, longTextPayload)
        val seekLinks = merged.urlEntities.filter { it.title == "10:29" }
        assertEquals(1, seekLinks.size)
        assertTrue(merged.text.contains(seekUrl))
        assertTrue(merged.text.indexOf(seekUrl) < merged.text.indexOf("@影石Insta360"))
    }

    @Test
    fun htmlDisplayKeepsTopicAnchorText() {
        val topic = "#杭州工之报一个不人知用享刺温人#"
        val raw = """
            {
              "statuses": [
                {
                  "idstr": "topic1",
                  "mblogid": "topicBlog",
                  "created_at": "Tue Jul 14 09:00:00 +0800 2026",
                  "text_raw": "【$topic】正文",
                  "text": "【<a href=\"https://s.weibo.com/weibo?q=%23hangzhou%23\" data-hide=\"\">$topic</a>】正文",
                  "reposts_count": 0,
                  "comments_count": 0,
                  "attitudes_count": 0,
                  "user": { "idstr": "1", "screen_name": "三联生活周刊" }
                }
              ]
            }
        """.trimIndent()

        val item = WeiboJsonParser.parseTimeline(raw).items.first()
        assertTrue(item.text.contains(topic))
        assertFalse(item.text.contains("s.weibo.com/weibo"))
        assertFalse(item.text.contains("%23"))
    }

    @Test
    fun htmlDisplayKeepsEmoticonPhrases() {
        val raw = """
            {
              "statuses": [
                {
                  "idstr": "emo1",
                  "mblogid": "emoBlog",
                  "created_at": "Tue Jul 14 09:00:00 +0800 2026",
                  "text_raw": "你好[微笑]",
                  "text": "你好<img alt=\"[微笑]\" src=\"https://face.t.sinajs.cn/t4/appstyle/expression/ext/normal/e3/2018new_weixiao_org.png\" style=\"width:1em; height:1em;\" />世界",
                  "reposts_count": 0,
                  "comments_count": 0,
                  "attitudes_count": 0,
                  "user": { "idstr": "1", "screen_name": "测试" }
                }
              ]
            }
        """.trimIndent()

        val item = WeiboJsonParser.parseTimeline(raw).items.first()
        assertTrue(item.text.contains("[微笑]"))
        assertTrue(item.emoticons.containsKey("[微笑]"))
    }

    @Test
    fun prefersRawWhenHtmlDropsVoteShortLink() {
        val voteUrl = "http://t.cn/voteLink"
        val raw = """
            {
              "statuses": [
                {
                  "idstr": "vote1",
                  "mblogid": "voteBlog",
                  "created_at": "Tue Jul 14 09:00:00 +0800 2026",
                  "text_raw": "来投票 $voteUrl",
                  "text": "来投票 <a href=\"https://vote.weibo.com/h5/index/index?vote_id=1\">微博投票</a>",
                  "reposts_count": 0,
                  "comments_count": 0,
                  "attitudes_count": 0,
                  "user": { "idstr": "1", "screen_name": "测试" },
                  "url_struct": [
                    {
                      "short_url": "$voteUrl",
                      "url_title": "微博投票",
                      "url_type": 39,
                      "object_type": "webpage",
                      "h5_target_url": "https://vote.weibo.com/h5/index/index?vote_id=1"
                    }
                  ]
                }
              ]
            }
        """.trimIndent()

        val item = WeiboJsonParser.parseTimeline(raw).items.first()
        assertTrue(item.text.contains(voteUrl))
        assertTrue(item.urlEntities.any { it.title == "微博投票" && it.shortUrl == voteUrl })
    }

    @Test
    fun prefersRawWhenHtmlDropsChaohuaTopic() {
        val raw = """
            {
              "statuses": [
                {
                  "idstr": "chao1",
                  "mblogid": "R8tYvdCoc",
                  "created_at": "Tue Jul 14 09:00:00 +0800 2026",
                  "text_raw": "周末出片 #摄影[超话]# 多图",
                  "text": "周末出片 <a href=\"https://weibo.com/p/100808abcdef\">摄影</a> 多图 http://t.cn/pic1",
                  "pic_num": 6,
                  "pic_ids": ["a","b","c","d","e","f"],
                  "reposts_count": 0,
                  "comments_count": 0,
                  "attitudes_count": 0,
                  "user": { "idstr": "5720474518", "screen_name": "测试" },
                  "url_struct": [
                    {
                      "short_url": "http://t.cn/pic1",
                      "url_title": "查看图片",
                      "url_type": 39,
                      "pic_ids": ["a"],
                      "pic_infos": { "a": { "largest": { "url": "https://example.com/a.jpg" } } }
                    }
                  ]
                }
              ]
            }
        """.trimIndent()

        val item = WeiboJsonParser.parseTimeline(raw).items.first()
        assertTrue(item.text.contains("#摄影[超话]#"))
    }

    @Test
    fun stripsTrailingExpandMarkerFromLongTextPreview() {
        val preview = "而长时间佩戴更是极易 ...展开"
        val raw = """
            {
              "statuses": [
                {
                  "idstr": "expand1",
                  "mblogid": "expandBlog",
                  "isLongText": true,
                  "created_at": "Tue Jul 14 09:00:00 +0800 2026",
                  "text_raw": "$preview",
                  "text": "$preview",
                  "reposts_count": 0,
                  "comments_count": 0,
                  "attitudes_count": 0,
                  "user": { "idstr": "1", "screen_name": "我爱音频网" }
                }
              ]
            }
        """.trimIndent()

        val item = WeiboJsonParser.parseTimeline(raw).items.first()
        assertFalse(item.isLongText)
        assertFalse(item.text.contains("展开"))
        assertFalse(item.text.trimEnd().endsWith("..."))
        assertTrue(item.text.contains("极易"))
    }

    @Test
    fun longTextWithMultipleImagesShowsReadFullButton() {
        val preview = "甲".repeat(200)
        val raw = """
            {
              "statuses": [
                {
                  "idstr": "multiPicLong1",
                  "mblogid": "multiPicLong",
                  "isLongText": true,
                  "created_at": "Tue Jul 14 10:00:00 +0800 2026",
                  "text_raw": "$preview",
                  "text": "$preview",
                  "pic_num": 6,
                  "pic_ids": ["p1","p2","p3","p4","p5","p6"],
                  "pic_infos": {
                    "p1": { "largest": { "url": "https://example.com/1.jpg" } },
                    "p2": { "largest": { "url": "https://example.com/2.jpg" } },
                    "p3": { "largest": { "url": "https://example.com/3.jpg" } },
                    "p4": { "largest": { "url": "https://example.com/4.jpg" } },
                    "p5": { "largest": { "url": "https://example.com/5.jpg" } },
                    "p6": { "largest": { "url": "https://example.com/6.jpg" } }
                  },
                  "reposts_count": 0,
                  "comments_count": 0,
                  "attitudes_count": 0,
                  "user": { "idstr": "1", "screen_name": "测试" }
                }
              ]
            }
        """.trimIndent()

        val item = WeiboJsonParser.parseTimeline(raw).items.first()
        assertFalse(item.isLongText)
        assertTrue(item.requiresLongTextFetch)
    }

    @Test
    fun manyImagesWithShortCaptionDoesNotShowReadFullButton() {
        val raw = """
            {
              "statuses": [
                {
                  "idstr": "manyPicShort1",
                  "mblogid": "manyPicShort",
                  "isLongText": true,
                  "created_at": "Tue Jul 14 10:00:00 +0800 2026",
                  "text_raw": "九图配文",
                  "text": "九图配文",
                  "pic_num": 12,
                  "pic_ids": ["a","b","c","d","e","f","g","h","i","j","k","l"],
                  "reposts_count": 0,
                  "comments_count": 0,
                  "attitudes_count": 0,
                  "user": { "idstr": "1", "screen_name": "测试" }
                }
              ]
            }
        """.trimIndent()

        val item = WeiboJsonParser.parseTimeline(raw).items.first()
        assertFalse(item.isLongText)
        assertTrue(item.requiresLongTextFetch)
    }

    @Test
    fun plainTextDecodesQuoteEntities() {
        val raw = """
            {
              "statuses": [
                {
                  "idstr": "quote1",
                  "mblogid": "quoteBlog",
                  "created_at": "Tue Jul 14 10:00:00 +0800 2026",
                  "text_raw": "第一份通报：&quot;舆论场一定要弘扬就事论事&quot;",
                  "text": "第一份通报：&quot;舆论场一定要弘扬就事论事&quot;",
                  "reposts_count": 0,
                  "comments_count": 0,
                  "attitudes_count": 0,
                  "user": { "idstr": "1", "screen_name": "测试" }
                }
              ]
            }
        """.trimIndent()

        val item = WeiboJsonParser.parseTimeline(raw).items.first()
        assertTrue(item.text.contains("\"舆论场一定要弘扬就事论事\""))
        assertFalse(item.text.contains("&quot;"))
    }

    @Test
    fun shortCaptionWithSeveralImagesDoesNotShowReadFullButton() {
        val raw = """
            {
              "statuses": [{
                "idstr": "5720474518",
                "mblogid": "R8O0yaoJq",
                "isLongText": true,
                "text_raw": "短配文 #摄影[超话]#",
                "text": "短配文 #摄影[超话]#",
                "pic_num": 6,
                "pic_ids": ["p1", "p2", "p3", "p4", "p5", "p6"],
                "reposts_count": 0,
                "comments_count": 0,
                "attitudes_count": 0,
                "user": { "idstr": "5720474518", "screen_name": "测试用户" }
              }]
            }
        """.trimIndent()

        val item = WeiboJsonParser.parseTimeline(raw).items.first()
        assertFalse(item.isLongText)
        assertTrue(item.requiresLongTextFetch)
    }

    @Test
    fun shortSuperTopicVideoCaptionDoesNotShowReadFullButton() {
        val token = "http://t.cn/A6video1"
        val raw = """
            {
              "statuses": [{
                "idstr": "5720474518",
                "mblogid": "R8O0yaoJq",
                "isLongText": true,
                "text_raw": "短配文 #摄影[超话]# $token",
                "text": "短配文 #摄影[超话]# $token",
                "url_struct": [{
                  "short_url": "$token",
                  "url_title": "测试用户的微博视频",
                  "long_url": "https://video.weibo.com/show?fid=1034:test"
                }],
                "page_info": {
                  "type": "video",
                  "page_title": "测试用户的微博视频",
                  "media_info": { "mp4_hd_url": "https://example.com/video.mp4" }
                },
                "reposts_count": 0,
                "comments_count": 0,
                "attitudes_count": 0,
                "user": { "idstr": "5720474518", "screen_name": "测试用户" }
              }]
            }
        """.trimIndent()

        val item = WeiboJsonParser.parseTimeline(raw).items.first()
        assertFalse(item.isLongText)
        assertTrue(item.requiresLongTextFetch)
    }

    @Test
    fun longTextContainingOnlySuperTopicAndVideoSupplementDoesNotExpand() {
        val preview = "真实正文".repeat(45)
        val raw = """
            {
              "statuses": [{
                "idstr": "supplement-only-1",
                "mblogid": "R8TQ7irJZ",
                "isLongText": true,
                "text_raw": "$preview",
                "text": "$preview",
                "reposts_count": 0,
                "comments_count": 0,
                "attitudes_count": 0,
                "user": { "idstr": "1682207150", "screen_name": "测试用户" }
              }]
            }
        """.trimIndent()
        val item = WeiboJsonParser.parseTimeline(raw).items.first()
        assertFalse(item.isLongText)

        val payload = JSONObject(
            """{
              "longTextContent_raw": "$preview #摄影[超话]# 某用户的微博视频 http://t.cn/A6abc123 大家都在搜",
              "longTextContent": "$preview #摄影[超话]# 某用户的微博视频 http://t.cn/A6abc123 大家都在搜"
            }""",
        )
        val merged = WeiboJsonParser.mergeLongTextIntoFeedItem(item, payload)

        assertFalse(merged.isLongText)
        assertFalse(merged.requiresLongTextFetch)
        assertEquals(item.text, merged.text)
    }

    @Test
    fun pinnedStatusIsParsedFromIsTopAndTitle() {
        val raw = """
            {
              "statuses": [
                {
                  "idstr": "pin1",
                  "mblogid": "pinnedBlog",
                  "isTop": 1,
                  "title": { "text": "置顶" },
                  "created_at": "Tue Jul 14 10:00:00 +0800 2026",
                  "text_raw": "置顶内容",
                  "text": "置顶内容",
                  "reposts_count": 0,
                  "comments_count": 0,
                  "attitudes_count": 0,
                  "user": { "idstr": "1", "screen_name": "测试用户" }
                },
                {
                  "idstr": "normal1",
                  "mblogid": "normalBlog",
                  "created_at": "Tue Jul 14 09:00:00 +0800 2026",
                  "text_raw": "普通微博",
                  "text": "普通微博",
                  "reposts_count": 0,
                  "comments_count": 0,
                  "attitudes_count": 0,
                  "user": { "idstr": "1", "screen_name": "测试用户" }
                }
              ]
            }
        """.trimIndent()

        val items = WeiboJsonParser.parseTimeline(raw).items
        assertTrue(items[0].isPinned)
        assertFalse(items[1].isPinned)
    }

    @Test
    fun repostDoesNotAdoptOriginalPlaceEntity() {
        val placeToken = "http://t.cn/placeBeijing"
        val place = "北京"
        val placeEntity = """
            {
              "short_url": "$placeToken",
              "url_title": "$place",
              "object_type": "place",
              "object_id": "B2094654D06FA4FB4099",
              "h5_target_url": "https://weibo.com/p/100101B2094654D06FA4FB4099"
            }
        """.trimIndent()
        val raw = """
            {
              "statuses": [{
                "idstr": "outer-place",
                "mblogid": "outerPlace",
                "text_raw": "转发正文//@原作者:原文",
                "text": "转发正文",
                "reposts_count": 0,
                "comments_count": 0,
                "attitudes_count": 0,
                "user": { "idstr": "1", "screen_name": "转发者" },
                "url_struct": [$placeEntity],
                "retweeted_status": {
                  "idstr": "inner-place",
                  "mblogid": "innerPlace",
                  "text_raw": "原文 $placeToken",
                  "text": "原文 $placeToken",
                  "reposts_count": 0,
                  "comments_count": 0,
                  "attitudes_count": 0,
                  "user": { "idstr": "2", "screen_name": "原作者" },
                  "url_struct": [$placeEntity]
                }
              }]
            }
        """.trimIndent()

        val outer = WeiboJsonParser.parseTimeline(raw).items.single()
        val original = requireNotNull(outer.retweetedStatus)
        assertTrue(outer.locationName.isNullOrBlank())
        assertEquals(place, original.locationName)
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
