package com.example.myweibo.data

import android.content.Context
import android.content.Intent
import android.net.Uri
import java.util.Locale
import java.net.URI

internal fun isWeiboShoppingCard(rawUrl: String): Boolean {
    val uri = runCatching { URI(rawUrl) }.getOrNull() ?: return false
    if (uri.scheme?.lowercase(Locale.ROOT) !in setOf("http", "https")) return false
    val host = uri.host.orEmpty().lowercase(Locale.ROOT)
    return (host == "apps.weibo.com" && uri.path.orEmpty().startsWith("/linkcard/2017845002:shop_sc_weibo_")) ||
        (host == "shop.sc.weibo.com" && uri.path.orEmpty().startsWith("/h5/goods/"))
}

/** These are Weibo authenticated shopping cards, not a JD/Taobao product URL. */
internal fun weiboShoppingDestination(rawUrl: String): String? {
    if (!isWeiboShoppingCard(rawUrl)) return null
    val uri = URI(rawUrl)
    if (uri.host.equals("apps.weibo.com", ignoreCase = true)) {
        val id = uri.path.substringAfter("/linkcard/2017845002:shop_sc_weibo_")
        if (!id.matches(Regex("[0-9]+"))) return null
        return "https://shop.sc.weibo.com/h5/goods/index?iid=$id"
    }
    return rawUrl.replaceFirst(Regex("^http:", RegexOption.IGNORE_CASE), "https:")
}

internal fun openWeiboShoppingCard(context: Context, rawUrl: String): Boolean {
    val secureUrl = weiboShoppingDestination(rawUrl) ?: return false
    val route = Uri.parse("sinaweibo://browser").buildUpon()
        .appendQueryParameter("url", secureUrl).build()
    return runCatching {
        context.startActivity(Intent(Intent.ACTION_VIEW, route).apply {
            setPackage("com.sina.weibo")
            addCategory(Intent.CATEGORY_BROWSABLE)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
        true
    }.getOrDefault(false)
}

/** Exact domain boundaries prevent lookalike hosts from being routed to shopping apps. */
internal fun shoppingPackage(scheme: String?, host: String?): String? {
    val normalizedScheme = scheme.orEmpty().lowercase(Locale.ROOT)
    val normalizedHost = host.orEmpty().lowercase(Locale.ROOT)
    fun domain(value: String) = normalizedHost == value || normalizedHost.endsWith(".$value")
    return when {
        normalizedScheme == "taobao" -> "com.taobao.taobao"
        normalizedScheme == "tmall" -> "com.tmall.wireless"
        normalizedScheme in setOf("openapp.jdmobile", "jdmobile") -> "com.jingdong.app.mall"
        normalizedScheme !in setOf("http", "https") -> null
        domain("taobao.com") || domain("tb.cn") -> "com.taobao.taobao"
        domain("tmall.com") -> "com.tmall.wireless"
        domain("jd.com") || domain("3.cn") || domain("jd.hk") -> "com.jingdong.app.mall"
        else -> null
    }
}

/** Rebuild an ACTION_VIEW intent; never trust components, extras or grants in an intent URL. */
internal fun openShoppingApp(context: Context, rawUrl: String): Boolean {
    val uri = runCatching {
        if (rawUrl.startsWith("intent:", ignoreCase = true)) {
            Intent.parseUri(rawUrl, Intent.URI_INTENT_SCHEME).data
        } else Uri.parse(rawUrl)
    }.getOrNull() ?: return false
    val target = shoppingPackage(uri.scheme, uri.host) ?: return false
    val intent = Intent(Intent.ACTION_VIEW, uri).apply {
        setPackage(target)
        addCategory(Intent.CATEGORY_BROWSABLE)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    return runCatching { context.startActivity(intent); true }.getOrDefault(false)
}

internal fun shoppingWebFallback(rawUrl: String): String? {
    if (!rawUrl.startsWith("intent:", ignoreCase = true)) return null
    val parsed = runCatching { Intent.parseUri(rawUrl, Intent.URI_INTENT_SCHEME) }.getOrNull() ?: return null
    val url = parsed.getStringExtra("browser_fallback_url") ?: return null
    return url.takeIf { Uri.parse(it).scheme?.lowercase(Locale.ROOT) in setOf("http", "https") }
}
