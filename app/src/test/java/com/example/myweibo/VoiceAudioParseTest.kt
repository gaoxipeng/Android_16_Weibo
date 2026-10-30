package com.example.myweibo

import com.example.myweibo.data.WeiboJsonParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class VoiceAudioParseTest {
    @Test
    fun parsesBlogAudio() {
        val raw = """
            {"statuses":[{
              "idstr":"5350604832510638",
              "mblogid":"Rp4MO7QuF",
              "text":"voice",
              "user":{"idstr":"1783497251","screen_name":"test"},
              "blog_audio":{
                "media_id":"5359766017343496",
                "media_url":"https://podcast.video.weibocdn.com/ul/example?a=1",
                "transcript":"转写文本",
                "media_format":"aac",
                "duration":11700
              }
            }]}
        """.trimIndent()
        val item = WeiboJsonParser.parseTimeline(raw).items.single()
        val audio = item.audio
        assertNotNull(audio)
        assertEquals(11700L, audio!!.durationMs)
        assertEquals("转写文本", audio.transcript)
        assertEquals("aac", audio.format)
    }
}
