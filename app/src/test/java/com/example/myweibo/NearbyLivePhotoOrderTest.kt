package com.example.myweibo

import com.example.myweibo.data.WeiboJsonParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NearbyLivePhotoOrderTest {
    @Test
    fun mixedMediaPresentationOrderWinsOverPicIdsAndKeepsEachLiveVideoPaired() {
        val raw = """
            {
              "statuses": [{
                "idstr": "status-1",
                "text_raw": "两张 Live Photo",
                "pic_ids": ["photo-2", "photo-1"],
                "pic_infos": {
                  "photo-1": {
                    "type": "livephoto",
                    "video": "https://video.example/photo-1.mp4",
                    "bmiddle": {"url": "https://img.example/photo-1.jpg"},
                    "largest": {"url": "https://img.example/photo-1.jpg"}
                  },
                  "photo-2": {
                    "type": "livephoto",
                    "video": "https://video.example/photo-2.mp4",
                    "bmiddle": {"url": "https://img.example/photo-2.jpg"},
                    "largest": {"url": "https://img.example/photo-2.jpg"}
                  }
                },
                "mix_media_info": {"items": [
                  {"type": "pic", "data": {
                    "pic_id": "photo-1", "type": "livephoto",
                    "video": "https://video.example/photo-1.mp4",
                    "bmiddle": {"url": "https://img.example/photo-1.jpg"},
                    "largest": {"url": "https://img.example/photo-1.jpg"}
                  }},
                  {"type": "pic", "data": {
                    "pic_id": "photo-2", "type": "livephoto",
                    "video": "https://video.example/photo-2.mp4",
                    "bmiddle": {"url": "https://img.example/photo-2.jpg"},
                    "largest": {"url": "https://img.example/photo-2.jpg"}
                  }}
                ]},
                "user": {"idstr": "u1", "screen_name": "测试用户"}
              }]
            }
        """.trimIndent()

        val images = WeiboJsonParser.parseTimeline(raw).items.single().images

        assertEquals(listOf("photo-1", "photo-2"), images.map { it.id })
        assertEquals(
            listOf("https://video.example/photo-1.mp4", "https://video.example/photo-2.mp4"),
            images.map { it.livePhotoVideoUrl },
        )
        assertTrue(images.all { it.isLivePhoto })
    }
}
