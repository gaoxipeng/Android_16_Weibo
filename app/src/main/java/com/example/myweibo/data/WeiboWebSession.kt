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
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLDecoder
import java.net.URLEncoder
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class WeiboWebSession(context: Context) {
    val webView: WebView = WebView(context)

    private val albumLoadMutex = Mutex()

    var currentUrl: String = ""
        private set

    var isWeiboPageReady: Boolean = false
        private set

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

    suspend fun loadUserAlbumImages(
        uid: String,
        cursor: String? = null,
        onPageLoaded: ((List<FeedImage>) -> Unit)? = null,
    ): AlbumPage = albumLoadMutex.withLock {
        val referer = "https://weibo.com/u/$uid?tabtype=album"
        val isStart = cursor == null
        var sinceId = cursor
            ?.removePrefix("wall:")
            ?.takeIf { cursor.startsWith("wall:") }
            ?.ifBlank { "0" }
            ?: "0"

        val fetchAll = cursor == null
        val accumulated = mutableListOf<FeedImage>()
        val monthContext = WeiboJsonParser.AlbumMonthContext()
        var nextSinceId: String? = null
        var pageIndex = 0
        var emptyStreak = 0
        val maxPages = if (fetchAll) MAX_ALBUM_FETCH_PAGES else MAX_ALBUM_EMPTY_PAGES
        while (pageIndex < maxPages) {
            val params = linkedMapOf(
                "uid" to uid,
                "sinceid" to sinceId,
            )
            if (isStart && pageIndex == 0) {
                params["has_album"] = "true"
            }

            val raw = runCatching {
                fetchJson(WeiboEndpoints.PROFILE_IMAGE_WALL, params, referer)
            }.recoverCatching {
                ensureOnUserAlbumPage(uid)
                webViewFetchJson(WeiboEndpoints.PROFILE_IMAGE_WALL, params)
            }.getOrElse { throw it }

            val wall = WeiboJsonParser.parseImageWallPage(
                raw = raw,
                monthContext = monthContext,
            )
            accumulated += wall.images
            nextSinceId = wall.nextSinceId
            onPageLoaded?.invoke(accumulated.distinctBy { it.largeUrl })
            if (nextSinceId == null) {
                break
            }
            if (!fetchAll && wall.images.isNotEmpty()) {
                break
            }
            if (wall.images.isEmpty()) {
                emptyStreak++
                if (emptyStreak > MAX_ALBUM_EMPTY_PAGES) {
                    break
                }
            } else {
                emptyStreak = 0
            }
            sinceId = nextSinceId!!
            pageIndex++
            if (fetchAll) {
                delay(200)
            }
        }

        AlbumPage(
            images = accumulated.distinctBy { it.largeUrl },
            nextCursor = nextSinceId?.let { "wall:$it" },
        )
    }

    private suspend fun ensureOnUserAlbumPage(uid: String) {
        val albumUrl = "https://weibo.com/u/$uid?tabtype=album"
        if (!currentUrl.contains("/u/$uid")) {
            suspendCancellableCoroutine { continuation ->
                webView.post {
                    webView.loadUrl(albumUrl)
                    continuation.resume(Unit)
                }
            }
            delay(900)
            waitForWeiboOrigin()
        }
    }

    private suspend fun webViewFetchJson(path: String, params: Map<String, String>): String {
        ensureOnWeiboOrigin()
        waitForWeiboOrigin()
        val payload = evaluateJson(fetchScript(path, params))
        val httpOk = payload.optBoolean("ok")
        val status = payload.optInt("status")
        if (!httpOk && status != 0) {
            val error = payload.nullableString("error").orEmpty()
            throw IllegalStateException("weibo-webview-fetch-failed:$status $error")
        }
        if (status == 0 && !payload.has("body")) {
            val error = payload.nullableString("error").orEmpty()
            throw IllegalStateException("weibo-webview-fetch-failed:0 $error")
        }
        val body = payload.getString("body")
        if (body.trimStart().startsWith("<")) {
            throw IllegalStateException("\u5FAE\u535A\u8FD4\u56DE HTML\uFF0C\u8BF7\u786E\u8BA4\u767B\u5F55\u72B6\u6001")
        }
        return body
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
                if (detail.hasAtLeastSameVisibleContentAs(item)) {
                    return detail.copy(isLongText = false)
                }
                throw IllegalStateException("微博详情长文解析失败")
            }.onSuccess { return it }
                .onFailure { lastError = it }
        }
        throw lastError ?: IllegalStateException("微博长文接口未返回内容")
    }

    suspend fun loadStatusDetail(statusId: String): FeedItem? {
        val id = statusId.trim()
        if (id.isBlank()) return null
        val raw = fetchJson(
            WeiboEndpoints.STATUS_DETAIL,
            linkedMapOf(
                "id" to id,
                "isGetLongText" to "1",
            ),
        )
        return WeiboJsonParser.parseStatusDetail(raw)
    }

    private fun FeedItem.hasAtLeastSameVisibleContentAs(other: FeedItem): Boolean {
        val hasText = text.trim().isNotBlank()
        val textIsNotShorter = text.trim().length >= other.text.trim().length
        val hasSameImages = images.size >= other.images.size
        val hasSameMedia = media != null || other.media == null
        return hasText && textIsNotShorter && hasSameImages && hasSameMedia
    }

    suspend fun loadEditHistory(item: FeedItem): List<EditHistoryEntry> {
        val referer = statusLikeReferer(item.statusId, item.id)
        var lastError: Throwable? = null
        var sawEmptySuccess = false
        for ((path, params) in editHistoryRequests(item)) {
            runCatching {
                val raw = fetchJson(path, params, referer)
                WeiboJsonParser.parseEditHistoryCompat(raw)
            }.onSuccess { entries ->
                if (entries.isNotEmpty()) {
                    return entries
                }
                sawEmptySuccess = true
            }.onFailure { error ->
                lastError = error
            }
        }
        runCatching { loadEditHistoryFromMobilePage(item) }
            .onSuccess { entries ->
                if (entries.isNotEmpty()) return entries
            }
            .onFailure { error ->
                lastError = error
            }
        runCatching { loadEditHistoryFromWebDialog(item) }
            .onSuccess { entries ->
                if (entries.isNotEmpty()) return entries
            }
            .onFailure { error ->
                lastError = error
            }
        if (sawEmptySuccess) return emptyList()
        if (lastError == null) return emptyList()
        val message = lastError?.message.orEmpty()
        if (message.contains("未发现微博登录") || message.contains("Cookie")) {
            throw IllegalStateException(lastError?.message ?: "未发现微博登录 Cookie")
        }
        throw IllegalStateException("该微博没有公开编辑历史，或微博网页接口暂不可用")
    }

    private suspend fun loadEditHistoryFromMobilePage(item: FeedItem): List<EditHistoryEntry> {
        val numericId = item.id.trim().takeIf { it.isNotBlank() && it != "0" && it.all(Char::isDigit) }
            ?: return emptyList()
        val url = "https://m.weibo.cn/p/231440_-_$numericId?title=%E7%BC%96%E8%BE%91%E8%AE%B0%E5%BD%95"
        loadWebViewUrl(url)
        delay(1600)
        val payload = evaluateJson(mobileEditHistoryPageScriptV2(item.authorName))
        if (!payload.optBoolean("ok")) {
            throw IllegalStateException(payload.nullableString("error") ?: "移动端编辑记录页未返回内容")
        }
        return parseEditHistoryDialogPayload(payload, item)
    }

    private suspend fun loadEditHistoryFromWebDialog(item: FeedItem): List<EditHistoryEntry> {
        val statusKey = item.statusId.trim().takeIf { it.isNotBlank() }
            ?: item.id.trim().takeIf { it.isNotBlank() && it != "0" }
            ?: return emptyList()
        val uid = item.authorId.trim()
        val detailUrl = if (uid.isNotBlank()) {
            "https://weibo.com/$uid/$statusKey"
        } else {
            "https://weibo.com/detail/$statusKey"
        }
        loadWebViewUrl(detailUrl)
        delay(1300)
        waitForWeiboOrigin()
        val payload = evaluateJson(editHistoryDialogScript(statusKey, item.id))
        if (!payload.optBoolean("ok")) {
            throw IllegalStateException(payload.nullableString("error") ?: "编辑历史弹窗未打开")
        }
        return parseEditHistoryDialogPayload(payload, item)
    }

    private suspend fun loadWebViewUrl(url: String) {
        suspendCancellableCoroutine { continuation ->
            webView.post {
                webView.loadUrl(url)
                continuation.resume(Unit)
            }
        }
    }

    private fun parseEditHistoryDialogPayload(payload: JSONObject, item: FeedItem): List<EditHistoryEntry> {
        val entries = payload.optJSONArray("entries") ?: JSONArray()
        return buildList {
            for (index in 0 until entries.length()) {
                val entry = entries.optJSONObject(index) ?: continue
                val text = entry.nullableString("text")
                    ?.replace(Regex("""\n{3,}"""), "\n\n")
                    ?.trim()
                    .orEmpty()
                if (text.isBlank() || looksLikeEditHistoryNavigation(text)) continue
                val images = buildList {
                    val array = entry.optJSONArray("images") ?: JSONArray()
                    for (imageIndex in 0 until array.length()) {
                        val url = normalizeWebImageUrl(array.optString(imageIndex))
                        if (url.isBlank()) continue
                        add(
                            FeedImage(
                                id = "edit-${item.id}-$index-$imageIndex",
                                thumbnailUrl = url,
                                largeUrl = url,
                                downloadUrls = listOf(url),
                                statusId = item.statusId,
                            )
                        )
                    }
                }.distinctBy { it.largeUrl }
                add(
                    EditHistoryEntry(
                        id = "edit-${item.id}-$index",
                        text = text,
                        createdAt = item.createdAt,
                        editedAt = entry.nullableString("time"),
                        version = entries.length() - index,
                        images = images,
                    )
                )
            }
        }.ifEmpty {
            val text = payload.nullableString("text")
                ?.replace(Regex("""\n{3,}"""), "\n\n")
                ?.trim()
                .orEmpty()
            if (text.isBlank() || looksLikeEditHistoryNavigation(text)) emptyList() else listOf(
                EditHistoryEntry(
                    id = "edit-${item.id}-dialog",
                    text = text,
                    createdAt = item.createdAt,
                    editedAt = null,
                    version = 1,
                    images = emptyList(),
                )
            )
        }
    }

    private fun looksLikeEditHistoryNavigation(text: String): Boolean {
        val compact = text.replace(Regex("""\s+"""), "")
        if (compact.length > 120) return false
        val navWords = listOf("首页", "内容声明", "表情", "图片", "视频", "话题", "头条文章", "更多", "公开", "发送")
        return navWords.count { compact.contains(it) } >= 6
    }

    private fun normalizeWebImageUrl(raw: String): String {
        val value = raw.trim()
        if (value.isBlank() || value.startsWith("data:")) return ""
        return when {
            value.startsWith("//") -> "https:$value"
            value.startsWith("/") -> "https://weibo.com$value"
            else -> value
        }
    }

    private fun editHistoryRequests(item: FeedItem): List<Pair<String, Map<String, String>>> {
        val numericId = item.id.trim().takeIf { it.isNotBlank() && it != "0" }
        val mblogId = item.statusId.trim().takeIf { it.isNotBlank() && it != numericId }
        val params = buildList {
            numericId?.let { id ->
                add(linkedMapOf("id" to id))
                add(linkedMapOf("mid" to id))
            }
            mblogId?.let { id ->
                add(linkedMapOf("mblogid" to id))
                add(linkedMapOf("mblogId" to id))
            }
        }.distinctBy { it.entries.joinToString("&") { entry -> "${entry.key}=${entry.value}" } }
        return WeiboEndpoints.STATUS_EDIT_HISTORY_CANDIDATES.flatMap { path ->
            params.map { path to it }
        }
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

    suspend fun loadNestedComments(commentId: String, statusAuthorUid: String): CommentsPage {
        val raw = fetchJson(
            WeiboEndpoints.STATUS_COMMENTS,
            linkedMapOf(
                "flow" to "1",
                "id" to commentId,
                "uid" to statusAuthorUid,
                "is_reload" to "1",
                "is_show_bulletin" to "2",
                "is_mix" to "1",
                "fetch_level" to "1",
                "count" to "20",
                "max_id" to "0",
                "locale" to "zh",
            ),
        )
        val items = WeiboJsonParser.parseComments(raw)
        val json = org.json.JSONObject(raw)
        val nextCursor = json.optJSONObject("data")?.nullableString("max_id")
            ?: json.nullableString("max_id")
        return CommentsPage(items, nextCursor.takeUnless { it.isNullOrBlank() || it == "0" })
    }

    suspend fun followUser(uid: String, current: UserProfile): UserProfile {
        val targetUid = uid.trim()
        require(targetUid.isNotBlank()) { "无效的用户 UID" }
        val raw = postForm(
            path = WeiboEndpoints.FOLLOW_CREATE,
            params = linkedMapOf(
                "friend_uid" to targetUid,
                "page" to "profile",
                "lpage" to "profile",
            ),
            referer = "https://weibo.com/u/$targetUid",
        )
        return WeiboJsonParser.mergeFollowMutationProfile(
            raw = raw,
            existing = current,
            expectedFollowing = true,
        )
    }

    suspend fun unfollowUser(uid: String, current: UserProfile): UserProfile {
        val targetUid = uid.trim()
        require(targetUid.isNotBlank()) { "无效的用户 UID" }
        val raw = postForm(
            path = WeiboEndpoints.FOLLOW_DESTROY,
            params = linkedMapOf("uid" to targetUid),
            referer = "https://weibo.com/u/$targetUid",
        )
        return WeiboJsonParser.mergeFollowMutationProfile(
            raw = raw,
            existing = current,
            expectedFollowing = false,
        )
    }

    suspend fun setStatusLike(likeId: String, mblogId: String? = null) {
        val id = likeId.trim()
        require(id.isNotBlank() && id != "0" && id.all(Char::isDigit)) { "无效的微博 ID" }
        val raw = postForm(
            path = WeiboEndpoints.SET_LIKE,
            params = linkedMapOf("id" to id),
            referer = statusLikeReferer(mblogId, id),
        )
        WeiboJsonParser.assertMutationSuccess(raw, "\u70B9\u8D5E\u5931\u8D25")
    }

    suspend fun cancelStatusLike(likeId: String, mblogId: String? = null) {
        val id = likeId.trim()
        require(id.isNotBlank() && id != "0" && id.all(Char::isDigit)) { "无效的微博 ID" }
        val raw = postForm(
            path = WeiboEndpoints.CANCEL_LIKE,
            params = linkedMapOf("id" to id),
            referer = statusLikeReferer(mblogId, id),
        )
        WeiboJsonParser.assertMutationSuccess(raw, "\u53D6\u6D88\u70B9\u8D5E\u5931\u8D25")
    }

    private fun statusLikeReferer(mblogId: String?, likeId: String): String {
        val detailKey = mblogId?.trim()?.takeIf { it.isNotBlank() } ?: likeId
        return "https://weibo.com/detail/$detailKey"
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

    suspend fun fetchJson(
        path: String,
        params: Map<String, String> = emptyMap(),
        referer: String = WEIBO_HOME,
    ): String {
        ensureOnWeiboOrigin()
        waitForWeiboOrigin()
        return nativeFetchJson(path, params, referer)
    }

    suspend fun postForm(
        path: String,
        params: Map<String, String>,
        referer: String = WEIBO_HOME,
    ): String {
        ensureOnWeiboOrigin()
        waitForWeiboOrigin()
        return nativePostForm(path, params, referer)
    }

    private suspend fun nativeFetchJson(
        path: String,
        params: Map<String, String>,
        referer: String = WEIBO_HOME,
    ): String =
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
                setRequestProperty("Referer", referer)
                setRequestProperty("Origin", "https://weibo.com")
                setRequestProperty("X-Requested-With", "XMLHttpRequest")
                setRequestProperty("Cookie", cookie)
            }
            extractCookieValue(cookie, "XSRF-TOKEN")?.let { token ->
                connection.setRequestProperty("X-XSRF-TOKEN", URLDecoder.decode(token, Charsets.UTF_8.name()))
            }

            val status = connection.responseCode
            syncResponseCookies(connection)
            val body = if (status in 200..299) {
                connection.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
            } else {
                val errorBody = connection.errorStream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }
                    .orEmpty()
                throw IllegalStateException(
                    "weibo-native-request-failed:$status @ $url ${errorBody.take(120)}"
                )
            }

            if (body.trimStart().startsWith("<")) {
                throw IllegalStateException("微博返回了 HTML 页面，可能登录未生效或被跳转 @ $currentUrl")
            }
            body
        }

    private suspend fun nativePostForm(
        path: String,
        params: Map<String, String>,
        referer: String = WEIBO_HOME,
    ): String =
        withContext(Dispatchers.IO) {
            CookieManager.getInstance().flush()
            val cookie = CookieManager.getInstance().getCookie(WEIBO_HOME)
                ?: CookieManager.getInstance().getCookie("https://www.weibo.com/")
                ?: ""
            if (!hasAuthenticatedCookie(cookie)) {
                throw IllegalStateException("未发现微博登录 Cookie，请到账户页登录后回到微博首页")
            }

            val body = params.entries.joinToString("&") { (key, value) ->
                "${key.urlEncode()}=${value.urlEncode()}"
            }
            val connection = (URL("https://weibo.com$path").openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                doOutput = true
                connectTimeout = 12_000
                readTimeout = 12_000
                instanceFollowRedirects = false
                setRequestProperty("User-Agent", DESKTOP_CHROME_USER_AGENT)
                setRequestProperty("Accept", "application/json, text/plain, */*")
                setRequestProperty("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8")
                setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                setRequestProperty("Referer", referer)
                setRequestProperty("Origin", "https://weibo.com")
                setRequestProperty("X-Requested-With", "XMLHttpRequest")
                setRequestProperty("Cookie", cookie)
            }
            extractCookieValue(cookie, "XSRF-TOKEN")?.let { token ->
                connection.setRequestProperty("X-XSRF-TOKEN", URLDecoder.decode(token, Charsets.UTF_8.name()))
            }

            connection.outputStream.bufferedWriter(Charsets.UTF_8).use { writer ->
                writer.write(body)
            }

            val status = connection.responseCode
            syncResponseCookies(connection)
            val responseBody = if (status in 200..299) {
                connection.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
            } else {
                val errorBody = connection.errorStream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }
                    .orEmpty()
                throw IllegalStateException(
                    "weibo-native-post-failed:$status ${errorBody.take(120)}"
                )
            }

            if (responseBody.trimStart().startsWith("<")) {
                throw IllegalStateException("微博返回了 HTML 页面，可能登录未生效或被跳转")
            }
            responseBody
        }

    private fun extractCookieValue(cookie: String, name: String): String? =
        cookie.split(";")
            .map { it.trim() }
            .firstOrNull { it.startsWith("$name=") }
            ?.substringAfter("=")
            ?.takeIf { it.isNotBlank() }

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
            })()
        """.trimIndent()
    }

    private fun mobileEditHistoryPageScript(): String =
        """
            (function() {
              const visible = el => {
                if (!el) return false;
                const rect = el.getBoundingClientRect();
                const style = getComputedStyle(el);
                return rect.width > 0 && rect.height > 0 && style.visibility !== 'hidden' && style.display !== 'none';
              };
              const textOf = el => (el && (el.innerText || el.textContent || '') || '').trim();
              const root = document.querySelector('#app') || document.querySelector('.m-container-max') || document.body;
              const bodyText = textOf(root)
                .replace(/^编辑记录\s*/,'')
                .replace(/返回\s*/g, '')
                .trim();
              if (!bodyText || bodyText.includes('你访问的地址不存在')) {
                return { ok: false, error: bodyText || 'mobile-edit-history-empty', href: location.href };
              }
              const candidates = Array.from(root.querySelectorAll('.card, .card9, article, [class*="card"], [class*="m-text-box"], [class*="weibo"]'))
                .filter(visible)
                .map(el => {
                  const text = textOf(el).replace(/^编辑记录\s*/,'').trim();
                  const images = Array.from(el.querySelectorAll('img'))
                    .map(img => img.currentSrc || img.src || img.getAttribute('src') || '')
                    .filter(src => src && !src.includes('avatar') && !src.includes('face') && !src.includes('sinaimg.cn/orj360'));
                  return { text, images };
                })
                .filter(item => item.text.length > 30 && !item.text.includes('编辑记录你访问的地址不存在'));
              const distinct = [];
              const seen = new Set();
              for (const item of candidates) {
                const key = item.text.slice(0, 120);
                if (!seen.has(key)) {
                  seen.add(key);
                  distinct.push(item);
                }
              }
              return {
                ok: true,
                href: location.href,
                text: bodyText,
                entries: distinct.length ? distinct : [{ text: bodyText, images: Array.from(root.querySelectorAll('img')).map(img => img.currentSrc || img.src || '').filter(Boolean) }]
              };
            })()
        """.trimIndent()

    private fun mobileEditHistoryPageScriptV2(authorName: String): String =
        """
            (function() {
              const authorName = ${authorName.jsQuote()};
              const editTitle = '\u7f16\u8f91\u8bb0\u5f55';
              const missingText = '\u4f60\u8bbf\u95ee\u7684\u5730\u5740\u4e0d\u5b58\u5728';
              const navWords = ['\u9996\u9875','\u5185\u5bb9\u58f0\u660e','\u8868\u60c5','\u56fe\u7247','\u89c6\u9891','\u8bdd\u9898','\u5934\u6761\u6587\u7ae0','\u66f4\u591a','\u516c\u5f00','\u53d1\u9001'];
              const visible = el => {
                if (!el) return false;
                const rect = el.getBoundingClientRect();
                const style = getComputedStyle(el);
                return rect.width > 0 && rect.height > 0 && style.visibility !== 'hidden' && style.display !== 'none';
              };
              const textOf = el => (el && (el.innerText || el.textContent || '') || '').trim();
              const normalize = text => String(text || '')
                .replace(new RegExp('^' + editTitle + '\\s*'), '')
                .replace(/\u8fd4\u56de\s*/g, '')
                .replace(/\n{3,}/g, '\n\n')
                .trim();
              const root = document.querySelector('#app') || document.querySelector('.m-container-max') || document.body;
              const bodyText = normalize(textOf(root));
              const compact = bodyText.replace(/\s+/g, '');
              const navCompact = navWords.join('');
              const onlyNav = compact === navCompact || (compact.length < 120 && navWords.every(word => compact.includes(word)));
              if (!bodyText || bodyText.includes(missingText) || onlyNav) {
                return { ok: false, error: bodyText || 'mobile-edit-history-empty', href: location.href };
              }
              const selector = [
                '.card', '.card9', 'article',
                '[class*="card"]', '[class*="m-text-box"]', '[class*="weibo"]',
                '[class*="status"]', '[class*="feed"]'
              ].join(',');
              const scoreEntry = item => {
                let score = 0;
                if (authorName && item.text.includes(authorName)) score += 5;
                if (item.text.includes('\u5df2\u7f16\u8f91')) score += 4;
                if (item.text.includes('\u6765\u81ea')) score += 2;
                if (item.images.length > 0) score += 2;
                score += Math.min(item.text.length / 500, 2);
                return score;
              };
              const candidates = Array.from(root.querySelectorAll(selector))
                .filter(visible)
                .map(el => {
                  const text = normalize(textOf(el));
                  const images = Array.from(el.querySelectorAll('img'))
                    .map(img => img.currentSrc || img.src || img.getAttribute('src') || '')
                    .filter(src => src && !src.includes('avatar') && !src.includes('face') && !src.includes('sinaimg.cn/orj360'));
                  return { text, images };
                })
                .filter(item => {
                  const compactText = item.text.replace(/\s+/g, '');
                  const itemOnlyNav = compactText === navCompact || (compactText.length < 120 && navWords.every(word => compactText.includes(word)));
                  if (item.text.length <= 30 || item.text.includes(missingText) || itemOnlyNav) return false;
                  return scoreEntry(item) >= 3;
                })
                .sort((a, b) => scoreEntry(b) - scoreEntry(a));
              const distinct = [];
              const seen = new Set();
              for (const item of candidates) {
                const key = item.text.slice(0, 160);
                if (!seen.has(key)) {
                  seen.add(key);
                  distinct.push(item);
                }
              }
              return { ok: distinct.length > 0, href: location.href, text: bodyText, entries: distinct, error: distinct.length ? '' : 'mobile-edit-history-content-not-found' };
            })()
        """.trimIndent()

    private fun editHistoryDialogScript(statusKey: String, numericId: String): String =
        """
            (async function() {
              const statusKey = ${statusKey.jsQuote()};
              const numericId = ${numericId.jsQuote()};
              const sleep = ms => new Promise(resolve => setTimeout(resolve, ms));
              const visible = el => {
                if (!el) return false;
                const rect = el.getBoundingClientRect();
                const style = getComputedStyle(el);
                return rect.width > 0 && rect.height > 0 && style.visibility !== 'hidden' && style.display !== 'none';
              };
              const textOf = el => (el && (el.innerText || el.textContent || '') || '').trim();
              const clickable = el => el && (el.closest('button,[role="button"],a,[tabindex],.woo-box-flex,.woo-pop-item') || el);
              const clickNode = el => {
                const target = clickable(el);
                if (!target) return false;
                target.dispatchEvent(new MouseEvent('mouseover', { bubbles: true, cancelable: true, view: window }));
                target.dispatchEvent(new MouseEvent('mousedown', { bubbles: true, cancelable: true, view: window }));
                target.dispatchEvent(new MouseEvent('mouseup', { bubbles: true, cancelable: true, view: window }));
                target.click();
                return true;
              };
              const findStatusRoot = () => {
                const keys = [statusKey, numericId].filter(Boolean);
                const candidates = Array.from(document.querySelectorAll('article, [mid], [data-mid], [data-id], [class*="Feed"], [class*="card"], [class*="vue-recycle-scroller__item-view"]'))
                  .filter(visible);
                for (const key of keys) {
                  const hit = candidates.find(el => (el.outerHTML || '').includes(key));
                  if (hit) return hit;
                }
                return candidates.find(el => textOf(el).includes('已编辑')) || document.body;
              };
              const findMenuItem = label => {
                const nodes = Array.from(document.querySelectorAll('div,span,button,a,li'))
                  .filter(visible)
                  .filter(el => textOf(el).includes(label));
                return nodes[0] || null;
              };
              const findDialog = () => {
                const nodes = Array.from(document.querySelectorAll('[role="dialog"], .woo-modal-main, .woo-dialog-main, .woo-panel-main, body > div'))
                  .filter(visible)
                  .filter(el => textOf(el).includes('编辑记录') || textOf(el).includes('微博编辑记录'));
                return nodes.sort((a, b) => textOf(b).length - textOf(a).length)[0] || null;
              };

              let dialog = findDialog();
              if (!dialog) {
                const root = findStatusRoot();
                const beforeMenus = Array.from(document.querySelectorAll('[role="menu"], .woo-pop-wrap, .woo-popover, .woo-menu')).length;
                const controls = Array.from(root.querySelectorAll('button,[role="button"],a,svg,i,span,div'))
                  .filter(visible);
                const moreCandidates = controls.filter(el => {
                  const text = textOf(el);
                  const aria = String(el.getAttribute('aria-label') || el.getAttribute('title') || '');
                  return text === '...' || text === '•••' || text === '更多' || text === '展开' ||
                    aria.includes('更多') || aria.includes('操作') || aria.includes('更多菜单') ||
                    (el.tagName.toLowerCase() === 'svg' && (el.outerHTML || '').length < 2000);
                });
                for (const node of moreCandidates.reverse()) {
                  clickNode(node);
                  await sleep(260);
                  const item = findMenuItem('查看编辑记录');
                  if (item) {
                    clickNode(item);
                    await sleep(900);
                    dialog = findDialog();
                    if (dialog) break;
                  }
                  const afterMenus = Array.from(document.querySelectorAll('[role="menu"], .woo-pop-wrap, .woo-popover, .woo-menu')).length;
                  if (afterMenus > beforeMenus) {
                    const edit = findMenuItem('查看编辑记录');
                    if (edit) {
                      clickNode(edit);
                      await sleep(900);
                      dialog = findDialog();
                      if (dialog) break;
                    }
                  }
                }
              }

              if (!dialog) {
                return { ok: false, error: 'edit-history-dialog-not-found', href: location.href };
              }

              const dialogText = textOf(dialog);
              const contentRoot = Array.from(dialog.querySelectorAll('div'))
                .filter(visible)
                .filter(el => textOf(el).length > 80)
                .sort((a, b) => textOf(b).length - textOf(a).length)[0] || dialog;
              const blocks = Array.from(contentRoot.children)
                .filter(visible)
                .map(el => {
                  const text = textOf(el);
                  const images = Array.from(el.querySelectorAll('img'))
                    .map(img => img.currentSrc || img.src || img.getAttribute('src') || '')
                    .filter(src => src && !src.includes('face') && !src.includes('avatar') && !src.includes('orj360'));
                  return { text, images };
                })
                .filter(item => item.text.length > 30);
              const entries = blocks.length > 0 ? blocks : [{ text: dialogText, images: Array.from(dialog.querySelectorAll('img')).map(img => img.currentSrc || img.src || '').filter(Boolean) }];
              return { ok: true, href: location.href, text: dialogText, entries };
            })()
        """.trimIndent()

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
        private const val MAX_ALBUM_EMPTY_PAGES = 12
        private const val MAX_ALBUM_FETCH_PAGES = 80
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
