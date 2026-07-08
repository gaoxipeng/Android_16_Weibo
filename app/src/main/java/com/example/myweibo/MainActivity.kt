package com.example.myweibo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.myweibo.ui.WeiboApp
import java.net.CookieHandler
import java.net.CookieManager
import java.net.URI

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        CookieHandler.setDefault(object : CookieHandler() {
            private val fallback = CookieManager()
            override fun get(uri: URI, requestHeaders: Map<String, List<String>>?): Map<String, List<String>> {
                val map = HashMap(fallback.get(uri, requestHeaders))
                val cookieStr = android.webkit.CookieManager.getInstance().getCookie(uri.toString())
                if (!cookieStr.isNullOrBlank()) {
                    val cookies = map.getOrDefault("Cookie", mutableListOf())
                    cookies.addAll(cookieStr.split(";").map { it.trim() })
                    map["Cookie"] = cookies
                }
                return map
            }
            override fun put(uri: URI, responseHeaders: Map<String, List<String>>?) {
                fallback.put(uri, responseHeaders)
                val webkit = android.webkit.CookieManager.getInstance()
                responseHeaders?.forEach { (key, values) ->
                    if (key.equals("Set-Cookie", ignoreCase = true) ||
                        key.equals("Set-Cookie2", ignoreCase = true)
                    ) {
                        values.forEach { cookieLine ->
                            webkit.setCookie(uri.toString(), cookieLine)
                        }
                    }
                }
                webkit.flush()
            }
        })

        enableEdgeToEdge()
        setContent {
            WeiboApp()
        }
    }
}
