package com.example.myweibo

import com.example.myweibo.data.WeiboJsonParser
import org.junit.Assert.assertEquals
import org.junit.Test

class AlbumPhotoParseTest {
    @Test
    fun imageWallKeepsPhotoAndDirectVideoTogether() {
        val result = WeiboJsonParser.parseImageWallPage("""
            {"ok":1,"data":{"list":[
              {"pid":"photo","pic":"https://wx1.sinaimg.cn/large/photo.jpg"},
              {"video_detail_vo":{"type":"video","mid":"1234567890123456",
               "pic":"https://wx1.sinaimg.cn/large/video.jpg",
               "media_info":{"stream_url":"https://example.com/video.mp4"}}}
            ]}}
        """)
        assertEquals(2, result.images.size)
        assertEquals("video", result.images.last().type)
        assertEquals("https://example.com/video.mp4", result.images.last().videoStreamUrl)
    }

    @Test
    fun wrappedAlbumPhotoWithObjectUrlIsRetained() {
        val result = WeiboJsonParser.parseImageWallPage("""
            {"ok":1,"data":{"list":[{"photo_detail_vo":{
              "pid":"abc","mid":"1234567890123456",
              "large":{"url":"https://wx1.sinaimg.cn/large/abc.jpg"}
            }}]}}
        """)
        assertEquals(1, result.images.size)
        assertEquals("https://wx1.sinaimg.cn/large/abc.jpg", result.images.single().largeUrl)
    }
}
