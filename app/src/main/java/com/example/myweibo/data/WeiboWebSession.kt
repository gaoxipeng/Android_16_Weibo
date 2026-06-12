package com.example.myweibo.data

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.net.http.SslError
import android.webkit.CookieManager
import android.webkit.SslErrorHandler
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class WeiboWebSession(context: Context) {
    val webView: WebView = WebView(context)

    var currentUrl: String = ""
        private set

    var isWeiboPageReady: Boolean = false
        private set

    private val albumSubalbumCache = mutableMapOf<String, List<String>>()

    init {
        configureWebView()
        webView.loadUrl(WEIBO_HOME)
    }

    fun openLogin() {
        webView.loadUrl(WEIBO_PASSPORT_LOGIN)
    }

    fun openWeiboHome() {
        webView.loadUrl(WEIBO_HOME)
    }

    fun hasLoginCookie(): Boolean {
        CookieManager.getInstance().flush()
        val cookie = CookieManager.getInstance().getCookie(WEIBO_HOME)
            ?: CookieManager.getInstance().getCookie("https://www.weibo.com/")
            ?: ""
        return hasAuthenticatedCookie(cookie)
    }

    suspend fun loadTimeline(kind: TimelineKind, cursor: String? = null): TimelinePage {
        val raw = loadTimelineRaw(kind, cursor)
        return WeiboJsonParser.parseTimeline(raw)
    }

    suspend fun loadUserTimeline(uid: String, page: Int = 1): TimelinePage {
        val raw = loadUserTimelineRaw(uid, page)
        return WeiboJsonParser.parseUserTimeline(raw, uid, page)
    }

    suspend fun loadUserTimelineRaw(uid: String, page: Int = 1): String =
        fetchJson(
            WeiboEndpoints.USER_TIMELINE,
            linkedMapOf(
                "uid" to uid,
                "page" to page.toString(),
                "feature" to "0",
            )
        )

    suspend fun loadUserAlbumImages(uid: String, cursor: String? = null): AlbumPage {
        return when {
            cursor?.startsWith("timeline:") == true -> loadTimelineAlbumPage(uid, cursor)
            cursor?.startsWith("sub:") == true -> loadSubalbumPage(uid, cursor)
            else -> loadImageWallPage(uid, cursor)
        }
    }

    private suspend fun loadImageWallPage(uid: String, cursor: String?): AlbumPage {
        val isStart = cursor == null
        val sinceId = cursor
            ?.removePrefix("wall:")
            ?.takeIf { cursor.startsWith("wall:") }
            ?.ifBlank { "0" }
            ?: "0"

        val params = linkedMapOf(
            "uid" to uid,
            "sinceid" to sinceId,
        )
        if (isStart) {
            params["has_album"] = "true"
        }

        val raw = runCatching {
            fetchJson(WeiboEndpoints.PROFILE_IMAGE_WALL, params)
        }.getOrNull()

        if (raw == null) {
            return if (isStart) loadAlbumHtmlFallback(uid) else AlbumPage(emptyList(), null)
        }

        val wall = WeiboJsonParser.parseImageWallPage(raw)
        if (isStart) {
            if (wall.subalbumContainerIds.isNotEmpty()) {
                albumSubalbumCache[uid] = wall.subalbumContainerIds
            } else {
                albumSubalbumCache.remove(uid)
            }
        }

        if (isStart && wall.images.isEmpty() && wall.nextSinceId == null && wall.subalbumContainerIds.isEmpty()) {
            loadAlbumHtmlFallback(uid).takeIf { it.images.isNotEmpty() }?.let { return it }
        }

        val nextCursor = when {
            wall.nextSinceId != null -> "wall:${wall.nextSinceId}"
            albumSubalbumCache[uid]?.isNotEmpty() == true -> "sub:0:0"
            isStart && wall.images.isEmpty() -> "timeline:1"
            else -> null
        }
        return AlbumPage(images = wall.images, nextCursor = nextCursor)
    }

    private suspend fun loadSubalbumPage(uid: String, cursor: String): AlbumPage {
        val body = cursor.removePrefix("sub:")
        val separator = body.indexOf(':')
        if (separator <= 0) return AlbumPage(emptyList(), null)

        val index = body.substring(0, separator).toIntOrNull() ?: return AlbumPage(emptyList(), null)
        val sinceId = body.substring(separator + 1).ifBlank { "0" }
        val containerId = albumSubalbumCache[uid]?.getOrNull(index)
            ?: return loadTimelineAlbumPage(uid, "timeline:1")

        val raw = runCatching {
            fetchJson(
                WeiboEndpoints.PROFILE_ALBUM_DETAIL,
                linkedMapOf(
                    "uid" to uid,
                    "containerid" to containerId,
                    "since_id" to sinceId,
                ),
            )
        }.getOrNull() ?: return AlbumPage(emptyList(), null)

        val page = WeiboJsonParser.parseAlbumDetailPage(raw)
        val nextCursor = when {
            page.nextCursor != null -> "sub:$index:${page.nextCursor}"
            index + 1 < (albumSubalbumCache[uid]?.size ?: 0) -> "sub:${index + 1}:0"
            else -> {
                albumSubalbumCache.remove(uid)
                null
            }
        }
        return AlbumPage(images = page.images, nextCursor = nextCursor)
    }

    private suspend fun loadTimelineAlbumPage(uid: String, cursor: String): AlbumPage {
        val page = cursor.removePrefix("timeline:").toIntOrNull()?.coerceAtLeast(1) ?: 1
        val timeline = loadUserTimeline(uid, page)
        if (timeline.items.isEmpty() && timeline.nextCursor == null) {
            return AlbumPage(images = emptyList(), nextCursor = null)
        }
        val images = timeline.items.flatMap { item ->
            item.images.map { image -> image.copy(createdAt = item.createdAt ?: image.createdAt) }
        }.distinctBy { it.largeUrl }
        return AlbumPage(
            images = images,
            nextCursor = timeline.nextCursor?.let { "timeline:${page + 1}" },
        )
    }

    private suspend fun loadAlbumHtmlFallback(uid: String): AlbumPage {
        val raw = runCatching {
            nativeFetchAbsoluteJson(
                url = "https://weibo.com/u/$uid?tabtype=album",
                referer = "https://weibo.com/",
                origin = "https://weibo.com",
            )
        }.getOrNull() ?: return AlbumPage(emptyList(), nextCursor = "timeline:1")

        val parsed = WeiboJsonParser.parseAlbumPage(raw)
        return if (parsed.images.isNotEmpty()) {
            parsed.copy(nextCursor = parsed.nextCursor?.let { "wall:$it" } ?: "timeline:1")
        } else {
            AlbumPage(emptyList(), nextCursor = "timeline:1")
        }
    }

    suspend fun loadTimelineRaw(kind: TimelineKind, cursor: String? = null): String {
        val params = when (kind) {
            TimelineKind.ForYou -> linkedMapOf(
                if (cursor == null) "since_id" to "0" else "max_id" to cursor,
            )

            TimelineKind.Following -> linkedMapOf(
                "list_id" to "110001768015440",
                "refresh" to "4",
                "count" to "20",
                "fid" to "110001768015440",
                if (cursor == null) "since_id" to "0" else "max_id" to cursor,
            )
        }

        return fetchJson(WeiboEndpoints.timelinePath(kind), params)
    }

    data class CommentsPage(val items: List<CommentItem>, val nextCursor: String?)

    private fun commentRequestParams(item: FeedItem, sort: CommentSort): LinkedHashMap<String, String> =
        linkedMapOf(
            "id" to item.id,
            "uid" to item.authorId,
            "flow" to sort.flow,
            "is_reload" to "1",
            "is_show_bulletin" to "2",
            "is_mix" to "0",
            "count" to "20",
            "fetch_level" to "0",
            "locale" to "zh",
        )

    suspend fun expandLongText(item: FeedItem): FeedItem {
        val candidates = buildList {
            item.statusId.takeIf { it.isNotBlank() }?.let { add(it) }
            item.id.takeIf { it.isNotBlank() && it != item.statusId }?.let { add(it) }
        }
        if (candidates.isEmpty()) {
            throw IllegalStateException("无效的微博 ID")
        }

        var lastError: Throwable? = null
        for (id in candidates) {
            runCatching {
                val raw = fetchJson(
                    WeiboEndpoints.STATUS_LONG_TEXT,
                    linkedMapOf("id" to id),
                )
                val data = JSONObject(raw).optJSONObject("data")
                    ?: throw IllegalStateException("微博长文接口未返回内容")
                if (!WeiboJsonParser.hasLongTextPayload(data)) {
                    throw IllegalStateException("微博长文内容为空")
                }
                val merged = WeiboJsonParser.mergeLongTextIntoFeedItem(item, data)
                if (!merged.isLongText) return merged
                throw IllegalStateException("微博长文解析失败")
            }.onSuccess { return it }
                .onFailure { lastError = it }
        }
        for (id in candidates) {
            runCatching {
                val raw = fetchJson(
                    WeiboEndpoints.STATUS_DETAIL,
                    linkedMapOf(
                        "id" to id,
                        "isGetLongText" to "1",
                    ),
                )
                val detail = WeiboJsonParser.parseStatusDetail(raw)
                    ?: throw IllegalStateException("微博详情接口未返回有效内容")
                if (!detail.isLongText && detail.text.length >= item.text.length) {
                    return detail
                }
                throw IllegalStateException("微博详情长文解析失败")
            }.onSuccess { return it }
                .onFailure { lastError = it }
        }
        throw lastError ?: IllegalStateException("微博长文接口未返回内容")
    }

    suspend fun loadComments(item: FeedItem, sort: CommentSort = CommentSort.Time): CommentsPage {
        val raw = fetchJson(
            WeiboEndpoints.STATUS_COMMENTS,
            commentRequestParams(item, sort),
        )
        val items = WeiboJsonParser.parseComments(raw)
        val json = org.json.JSONObject(raw)
        val cursor = json.optJSONObject("data")?.nullableString("max_id")
            ?: json.nullableString("max_id")
        return CommentsPage(items, cursor.takeUnless { it.isNullOrBlank() || it == "0" })
    }

    suspend fun loadMoreComments(
        item: FeedItem,
        cursor: String,
        sort: CommentSort = CommentSort.Time,
    ): CommentsPage {
        val raw = fetchJson(
            WeiboEndpoints.STATUS_COMMENTS,
            commentRequestParams(item, sort) + linkedMapOf("max_id" to cursor),
        )
        val items = WeiboJsonParser.parseComments(raw)
        val json = org.json.JSONObject(raw)
        val nextCursor = json.optJSONObject("data")?.nullableString("max_id")
            ?: json.nullableString("max_id")
        return CommentsPage(items, nextCursor.takeUnless { it.isNullOrBlank() || it == "0" })
    }

    suspend fun loadUserProfile(lookup: ProfileLookup): UserProfile {
        val params = when (lookup) {
            is ProfileLookup.Uid -> linkedMapOf("uid" to lookup.uid)
            is ProfileLookup.ScreenName -> linkedMapOf(
                "screen_name" to lookup.screenName,
                "scene" to "profile",
            )
        }
        val raw = runCatching {
            fetchJson(WeiboEndpoints.PROFILE_INFO, params)
        }.getOrElse { error ->
            if (lookup is ProfileLookup.ScreenName) {
                nativeFetchAbsoluteJson(
                    url = "https://m.weibo.cn/api/container/getIndex?type=uname&value=${lookup.screenName.urlEncode()}",
                    referer = "https://m.weibo.cn/",
                    origin = "https://m.weibo.cn",
                )
            } else {
                throw error
            }
        }
        return WeiboJsonParser.parseUserProfile(raw)
    }

    suspend fun loadCurrentUserProfile(): UserProfile {
        val config = loadCurrentUserConfig()
        val uid = config.optString("uid").takeIf { it.isNotBlank() && it != "0" }
            ?: throw IllegalStateException("\u672A\u8BFB\u5230\u5F53\u524D\u767B\u5F55\u7528\u6237 UID\uFF0C\u8BF7\u5148\u5B8C\u6210\u5FAE\u535A\u767B\u5F55")
        val raw = runCatching {
            fetchJson(
                WeiboEndpoints.PROFILE_INFO,
                linkedMapOf("uid" to uid),
            )
        }.getOrElse {
            nativeFetchAbsoluteJson(
                url = "https://m.weibo.cn/api/container/getIndex?type=uid&value=${uid.urlEncode()}",
                referer = "https://m.weibo.cn/u/$uid",
                origin = "https://m.weibo.cn",
            )
        }
        return WeiboJsonParser.parseUserProfile(raw, config)
    }

    private suspend fun loadCurrentUserConfig(): JSONObject {
        runCatching { parseUserConfig(nativeFetchJson(WeiboEndpoints.CONFIG, emptyMap())) }
            .getOrNull()
            ?.let { return it }

        runCatching {
            parseUserConfig(
                nativeFetchAbsoluteJson(
                    url = "https://m.weibo.cn/api/config",
                    referer = "https://m.weibo.cn/",
                    origin = "https://m.weibo.cn",
                )
            )
        }.getOrNull()?.let { return it }

        throw IllegalStateException("\u672A\u8BFB\u5230\u5F53\u524D\u767B\u5F55\u7528\u6237 UID\uFF0C\u8BF7\u5728\u8BBE\u7F6E\u4E2D\u91CD\u65B0\u6253\u5F00\u5FAE\u535A\u9996\u9875\u540E\u518D\u8BD5")
    }

    private fun parseUserConfig(raw: String): JSONObject? {
        val root = JSONObject(raw)
        val data = root.optJSONObject("data") ?: root
        val user = data.optJSONObject("user")
            ?: data.optJSONObject("login_user")
            ?: data.optJSONObject("userInfo")
            ?: data
        val uid = data.optString("uid")
            .ifBlank { data.optString("id") }
            .ifBlank { data.optString("idstr") }
            .ifBlank { user.optString("idstr") }
            .ifBlank { user.optString("id") }
        if (uid.isBlank() || uid == "0") return null
        return JSONObject().apply {
            put("uid", uid)
            put("screen_name", data.optString("screen_name").ifBlank {
                data.optString("nick").ifBlank {
                    data.optString("name").ifBlank {
                        user.optString("screen_name").ifBlank { user.optString("name") }
                    }
                }
            })
            put("avatar", data.optString("avatar").ifBlank {
                data.optString("profile_image_url").ifBlank {
                    user.optString("avatar_hd").ifBlank {
                        user.optString("avatar_large").ifBlank { user.optString("profile_image_url") }
                    }
                }
            })
        }
    }

    suspend fun fetchJson(path: String, params: Map<String, String> = emptyMap()): String {
        ensureOnWeiboOrigin()
        waitForWeiboOrigin()
        return nativeFetchJson(path, params)
    }

    private suspend fun nativeFetchJson(path: String, params: Map<String, String>): String =
        withContext(Dispatchers.IO) {
            CookieManager.getInstance().flush()
            val cookie = CookieManager.getInstance().getCookie(WEIBO_HOME)
                ?: CookieManager.getInstance().getCookie("https://www.weibo.com/")
                ?: ""
            if (!hasAuthenticatedCookie(cookie)) {
                throw IllegalStateException("未发现微博登录 Cookie，请到账户页登录后回到微博首页")
            }

            val url = buildUrl(path, params)
            val connection = (URL(url).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 12_000
                readTimeout = 12_000
                instanceFollowRedirects = false
                setRequestProperty("User-Agent", DESKTOP_CHROME_USER_AGENT)
                setRequestProperty("Accept", "application/json, text/plain, */*")
                setRequestProperty("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8")
                setRequestProperty("Referer", WEIBO_HOME)
                setRequestProperty("Origin", "https://weibo.com")
                setRequestProperty("X-Requested-With", "XMLHttpRequest")
                setRequestProperty("Cookie", cookie)
            }

            val status = connection.responseCode
            syncResponseCookies(connection)
            val body = if (status in 200..299) {
                connection.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
            } else {
                val errorBody = connection.errorStream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }
                    .orEmpty()
                throw IllegalStateException(
                    "weibo-native-request-failed:$status @ $currentUrl ${errorBody.take(120)}"
                )
            }

            if (body.trimStart().startsWith("<")) {
                throw IllegalStateException("微博返回了 HTML 页面，可能登录未生效或被跳转 @ $currentUrl")
            }
            body
        }

    private suspend fun nativeFetchAbsoluteJson(url: String, referer: String, origin: String): String =
        withContext(Dispatchers.IO) {
            CookieManager.getInstance().flush()
            val cookie = CookieManager.getInstance().getCookie(referer)
                ?: CookieManager.getInstance().getCookie(WEIBO_HOME)
                ?: CookieManager.getInstance().getCookie("https://www.weibo.com/")
                ?: ""
            if (!hasAuthenticatedCookie(cookie)) {
                throw IllegalStateException("\u672A\u53D1\u73B0\u5FAE\u535A\u767B\u5F55 Cookie")
            }

            val connection = (URL(url).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 12_000
                readTimeout = 12_000
                instanceFollowRedirects = false
                setRequestProperty("User-Agent", DESKTOP_CHROME_USER_AGENT)
                setRequestProperty("Accept", "application/json, text/plain, */*")
                setRequestProperty("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8")
                setRequestProperty("Referer", referer)
                setRequestProperty("Origin", origin)
                setRequestProperty("X-Requested-With", "XMLHttpRequest")
                setRequestProperty("Cookie", cookie)
            }

            val status = connection.responseCode
            syncResponseCookies(connection)
            val body = if (status in 200..299) {
                connection.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
            } else {
                val errorBody = connection.errorStream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }
                    .orEmpty()
                throw IllegalStateException("weibo-absolute-request-failed:$status ${errorBody.take(120)}")
            }

            if (body.trimStart().startsWith("<")) {
                throw IllegalStateException("\u5FAE\u535A\u8FD4\u56DE HTML\uFF0C\u8BF7\u786E\u8BA4\u767B\u5F55\u72B6\u6001")
            }
            body
        }

    private fun syncResponseCookies(connection: HttpURLConnection) {
        val manager = CookieManager.getInstance()
        val requestUrl = connection.url.toString()
        connection.headerFields?.forEach { (key, values) ->
            if (key != null && key.equals("Set-Cookie", ignoreCase = true)) {
                values.forEach { cookieLine ->
                    manager.setCookie(requestUrl, cookieLine)
                    COOKIE_ORIGINS.forEach { origin ->
                        manager.setCookie(origin, cookieLine)
                    }
                }
            }
        }
        manager.flush()
    }

    private fun buildUrl(path: String, params: Map<String, String>): String {
        val query = params.entries.joinToString("&") { (key, value) ->
            "${key.urlEncode()}=${value.urlEncode()}"
        }
        return "https://weibo.com$path" + if (query.isBlank()) "" else "?$query"
    }

    private fun hasAuthenticatedCookie(cookie: String): Boolean =
        cookie.split(";")
            .map { it.trim() }
            .any { it.startsWith("SUB=") && it.length > "SUB=".length }

    private fun String.urlEncode(): String =
        URLEncoder.encode(this, Charsets.UTF_8.name())

    private fun ensureOnWeiboOrigin() {
        if (!currentUrl.startsWith("https://weibo.com") && !currentUrl.startsWith("https://www.weibo.com")) {
            webView.loadUrl(WEIBO_HOME)
        }
    }

    private suspend fun waitForWeiboOrigin() {
        repeat(40) {
            if (isWeiboPageReady) return
            delay(250)
        }
    }

    private suspend fun currentUserConfig(): JSONObject {
        ensureOnWeiboOrigin()
        waitForWeiboOrigin()
        return evaluateJson(
            """
            (function() {
              const config = window.${'$'}CONFIG || {};
              const user = config.user || {};
              return {
                uid: String(config.uid || user.idstr || user.id || ''),
                screen_name: String(config.nick || config.screen_name || user.screen_name || user.name || ''),
                avatar: String(config.avatar || user.avatar_hd || user.profile_image_url || '')
              };
            })();
            """.trimIndent()
        )
    }

    private suspend fun evaluateJson(script: String): JSONObject =
        suspendCancellableCoroutine { continuation ->
            webView.post {
                webView.evaluateJavascript(script) { result ->
                    runCatching { decodeEvaluateJavascriptResult(result) }
                        .onSuccess { continuation.resume(it) }
                        .onFailure { continuation.resumeWithException(it) }
                }
            }
        }

    private fun decodeEvaluateJavascriptResult(result: String?): JSONObject {
        val raw = result?.trim().orEmpty()
        if (raw.isBlank() || raw == "null") {
            throw IllegalStateException("empty-webview-js-result")
        }

        return if (raw.startsWith("\"")) {
            JSONObject(JSONObject("{\"value\":$raw}").getString("value"))
        } else {
            JSONObject(raw)
        }
    }

    private fun fetchScript(path: String, params: Map<String, String>): String {
        val paramsLiteral = params.entries.joinToString(",") { (key, value) ->
            "${key.jsQuote()}:${value.jsQuote()}"
        }
        return """
            (async function() {
              try {
                const params = new URLSearchParams({$paramsLiteral});
                const base = location.hostname === 'www.weibo.com' ? 'https://www.weibo.com' : 'https://weibo.com';
                const url = base + ${path.jsQuote()} + (params.toString() ? '?' + params.toString() : '');
                const response = await fetch(url, {
                  credentials: 'include',
                  referrer: base + '/',
                  headers: {
                    'accept': 'application/json, text/plain, */*',
                    'x-requested-with': 'XMLHttpRequest'
                  }
                });
                const body = await response.text();
                return { ok: response.ok, status: response.status, body: body, href: location.href };
              } catch (error) {
                return { ok: false, status: 0, error: String(error), href: location.href };
              }
            })();
        """.trimIndent()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun configureWebView() {
        CookieManager.getInstance().setAcceptCookie(true)
        CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true)

        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            databaseEnabled = true
            mediaPlaybackRequiresUserGesture = false
            loadsImagesAutomatically = true
            useWideViewPort = true
            loadWithOverviewMode = true
            cacheMode = WebSettings.LOAD_DEFAULT
            userAgentString = DESKTOP_CHROME_USER_AGENT
        }

        webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                currentUrl = url.orEmpty()
                isWeiboPageReady = false
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                currentUrl = url.orEmpty()
                isWeiboPageReady =
                    currentUrl.startsWith("https://weibo.com") ||
                        currentUrl.startsWith("https://www.weibo.com")
            }

            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                return false
            }

            override fun onReceivedSslError(view: WebView?, handler: SslErrorHandler?, error: SslError?) {
                handler?.cancel()
            }
        }
    }

    suspend fun loadEmotions(): List<WeiboEmoticon> {
        val raw = fetchJson("/ajax/statuses/config")
        return WeiboJsonParser.parseEmotions(raw)
    }

    suspend fun persistCurrentAccount(store: WeiboAccountStore): StoredWeiboAccount? {
        if (!hasLoginCookie()) return null
        val profile = loadCurrentUserProfile()
        val account = StoredWeiboAccount(
            id = profile.id,
            screenName = profile.screenName,
            avatarUrl = profile.avatarUrl,
            cookies = captureCookieSnapshot(),
        )
        store.upsertAccount(account)
        return account
    }

    suspend fun activateAccount(store: WeiboAccountStore, accountId: String) {
        val account = store.getAccount(accountId)
            ?: throw IllegalStateException("未找到账号 $accountId")
        clearAllCookies()
        restoreCookieSnapshot(account.cookies)
        store.setActiveAccountId(accountId)
        webView.loadUrl(WEIBO_HOME)
        delay(400)
    }

    suspend fun prepareAddAccount() {
        clearAllCookies()
        openLogin()
    }

    fun captureCookieSnapshot(): Map<String, String> {
        CookieManager.getInstance().flush()
        return COOKIE_ORIGINS.mapNotNull { origin ->
            CookieManager.getInstance().getCookie(origin)
                ?.takeIf { it.isNotBlank() }
                ?.let { origin to it }
        }.toMap()
    }

    fun restoreCookieSnapshot(snapshot: Map<String, String>) {
        val manager = CookieManager.getInstance()
        snapshot.forEach { (origin, cookieValue) ->
            cookieValue.split(";")
                .map { it.trim() }
                .filter { it.isNotBlank() }
                .forEach { pair -> manager.setCookie(origin, pair) }
        }
        manager.flush()
    }

    suspend fun clearAllCookies() {
        suspendCancellableCoroutine { continuation ->
            CookieManager.getInstance().removeAllCookies { success ->
                CookieManager.getInstance().flush()
                if (continuation.isActive) {
                    continuation.resume(success)
                }
            }
        }
    }

    private fun String.jsQuote(): String =
        JSONObject.quote(this)

    private fun org.json.JSONObject.nullableString(key: String): String? =
        if (has(key) && !isNull(key)) optString(key, "") else null

    companion object {
        private const val WEIBO_HOME = "https://weibo.com/"
        private const val WEIBO_PASSPORT_LOGIN = "https://passport.weibo.cn/signin/login"
        private val COOKIE_ORIGINS = listOf(
            "https://weibo.com/",
            "https://www.weibo.com/",
            "https://passport.weibo.cn/",
            "https://m.weibo.cn/",
        )
        private const val DESKTOP_CHROME_USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 " +
                "(KHTML, like Gecko) Chrome/126.0.0.0 Safari/537.36"
    }
}
