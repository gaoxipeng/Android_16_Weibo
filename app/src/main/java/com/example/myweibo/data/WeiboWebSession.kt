package com.example.myweibo.data

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.net.http.SslError
import android.util.Base64
import android.webkit.CookieManager
import android.webkit.SslErrorHandler
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
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
    private val appContext = context.applicationContext
    val webView: WebView = WebView(context)

    private val albumLoadMutex = Mutex()
    private val mweiboSessionMutex = Mutex()
    private var mweiboSessionReady = false
    private val viewportCoordinator = SessionWebViewportCoordinator()

    var currentUrl: String = ""
        private set

    var isWeiboPageReady: Boolean = false
        private set

    init {
        configureWebView()
        webView.loadUrl(WEIBO_HOME)
    }

    fun openLogin(forceReload: Boolean = false) {
        val activeUrl = webView.url.orEmpty().ifBlank { currentUrl }
        if (!forceReload && activeUrl.startsWith(WEIBO_PASSPORT_ORIGIN)) return
        webView.loadUrl(WEIBO_PASSPORT_LOGIN)
    }

    fun openWeiboHome() {
        webView.loadUrl(WEIBO_HOME)
    }

    fun hasLoginCookie(): Boolean {
        val cookie = CookieManager.getInstance().getCookie(WEIBO_HOME)
            ?: CookieManager.getInstance().getCookie("https://www.weibo.com/")
            ?: ""
        return hasAuthenticatedCookie(cookie)
    }

    suspend fun loadTimeline(kind: TimelineKind, cursor: String? = null): TimelinePage {
        val raw = loadTimelineRaw(kind, cursor)
        return WeiboJsonParser.parseTimeline(raw)
    }

    suspend fun loadHotSearch(): List<HotSearchItem> {
        val raw = fetchJson(
            WeiboEndpoints.SEARCH_BAND,
            mapOf("last_tab" to "hot"),
        )
        return WeiboJsonParser.parseHotSearch(raw)
    }

    suspend fun loadSearchSuggest(query: String): SearchSuggestResult {
        val raw = fetchJson(
            WeiboEndpoints.SEARCH_SIDE,
            mapOf("q" to query.trim()),
        )
        return WeiboJsonParser.parseSearchSuggest(raw)
    }

    suspend fun loadTopicSearch(topic: String, page: Int = 1, channelType: String = "1"): TimelinePage {
        ensureMweiboSession()
        val normalized = topic.removePrefix("#").removeSuffix("#").trim()
        if (normalized.isBlank()) return TimelinePage(items = emptyList())
        val containerId = "231522type=$channelType&q=#$normalized#"
        val url = Uri.Builder()
            .scheme("https")
            .authority("m.weibo.cn")
            .appendPath("api")
            .appendPath("container")
            .appendPath("getIndex")
            .appendQueryParameter("containerid", containerId)
            .appendQueryParameter("page_type", "searchall")
            .appendQueryParameter("v_p", "42")
            .appendQueryParameter("page", page.toString())
            .build()
            .toString()
        val referer = Uri.Builder()
            .scheme("https")
            .authority("m.weibo.cn")
            .appendPath("search")
            .appendQueryParameter("containerid", containerId)
            .build()
            .toString()
        val raw = nativeFetchMweiboJson(url, referer)
        return WeiboJsonParser.parseMweiboTopicTimeline(raw, page)
    }

    suspend fun loadWeiboSearch(query: String, page: Int = 1, realtime: Boolean = false): TimelinePage {
        val normalized = query.trim()
        if (normalized.isBlank()) return TimelinePage(items = emptyList())
        val builder = Uri.Builder()
            .scheme("https")
            .authority("s.weibo.com")
            .appendPath(if (realtime) "realtime" else "weibo")
            .appendQueryParameter("q", normalized)
            .appendQueryParameter("page", page.toString())
        if (realtime) {
            builder
                .appendQueryParameter("rd", "realtime")
                .appendQueryParameter("tw", "realtime")
                .appendQueryParameter("Refer", "weibo_realtime")
        } else {
            builder
                .appendQueryParameter("topic_ad", "")
                .appendQueryParameter("t", "547")
        }
        val url = builder.build().toString()
        val raw = nativeFetchAbsoluteHtml(
            url = url,
            referer = "https://s.weibo.com/",
            origin = "https://s.weibo.com",
        )
        return WeiboJsonParser.parseSWeiboSearchTimeline(raw, page)
    }

    suspend fun loadWeiboUserSearch(query: String, page: Int = 1): SearchUserPage {
        val normalized = query.trim()
        if (normalized.isBlank()) return SearchUserPage(items = emptyList())
        val url = Uri.Builder()
            .scheme("https")
            .authority("s.weibo.com")
            .appendPath("user")
            .appendQueryParameter("q", normalized)
            .appendQueryParameter("page", page.toString())
            .build()
            .toString()
        val raw = nativeFetchAbsoluteHtml(
            url = url,
            referer = "https://s.weibo.com/",
            origin = "https://s.weibo.com",
        )
        return WeiboJsonParser.parseSWeiboUserSearch(raw, page)
    }

    private suspend fun ensureMweiboSession() {
        mweiboSessionMutex.withLock {
            if (mweiboSessionReady) return
            runCatching {
                nativeFetchMweiboJson("https://m.weibo.cn/api/config")
            }
            mweiboSessionReady = true
        }
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
        if (cursor?.startsWith("waterfall:cursor:") == true) {
            return@withLock loadUserAlbumWaterfall(
                uid = uid,
                cursor = cursor,
                onPageLoaded = onPageLoaded,
                fallbackFromEmptyWall = false,
            )
        }
        if (cursor != null &&
            !cursor.startsWith("wall:cursor:") &&
            !cursor.startsWith("wall:")
        ) {
            return@withLock AlbumPage(images = emptyList(), nextCursor = null)
        }
        if (cursor == null) {
            val wallPage = runCatching {
                loadUserAlbumImageWall(uid, cursor, onPageLoaded)
            }.getOrElse { error ->
                if (error is CancellationException) throw error
                null
            }
            if (wallPage != null && wallPage.images.isNotEmpty()) {
                return@withLock wallPage
            }
            return@withLock loadUserAlbumWaterfall(
                uid = uid,
                cursor = null,
                onPageLoaded = onPageLoaded,
                fallbackFromEmptyWall = wallPage != null,
            )
        }
        loadUserAlbumImageWall(uid, cursor, onPageLoaded)
    }

    private suspend fun loadUserAlbumImageWall(
        uid: String,
        cursor: String?,
        onPageLoaded: ((List<FeedImage>) -> Unit)?,
    ): AlbumPage {
        val referer = "https://weibo.com/u/$uid?tabtype=album"
        val isStart = cursor == null
        val useCursorPagination = cursor?.startsWith("wall:cursor:") == true
        val sinceId = when {
            useCursorPagination -> null
            cursor?.startsWith("wall:") == true -> cursor.removePrefix("wall:").ifBlank { "0" }
            else -> "0"
        }
        val videoCursor = cursor?.removePrefix("wall:cursor:")?.takeIf { useCursorPagination }

        val monthContext = WeiboJsonParser.AlbumMonthContext()
        val params = linkedMapOf("uid" to uid)
        if (useCursorPagination || videoCursor != null) {
            params["cursor"] = videoCursor ?: "0"
        } else {
            params["sinceid"] = sinceId ?: "0"
            if (isStart) {
                params["has_album"] = "true"
            }
        }

        val raw = fetchJson(WeiboEndpoints.PROFILE_IMAGE_WALL, params, referer)

        val wall = WeiboJsonParser.parseImageWallPage(
            raw = raw,
            monthContext = monthContext,
        )
        val images = wall.images.distinctByAlbumKey()
        onPageLoaded?.invoke(images)

        val nextPageCursor = when {
            wall.nextCursor != null -> "wall:cursor:${wall.nextCursor}"
            wall.nextSinceId != null -> "wall:${wall.nextSinceId}"
            else -> null
        }

        return AlbumPage(
            images = images,
            nextCursor = nextPageCursor,
            fetchTrace = AlbumFetchTrace(
                source = AlbumFetchSource.ImageWall,
                imageCount = images.size,
                isFirstPage = isStart,
            ),
        )
    }

    private suspend fun loadUserAlbumWaterfall(
        uid: String,
        cursor: String?,
        onPageLoaded: ((List<FeedImage>) -> Unit)?,
        fallbackFromEmptyWall: Boolean,
    ): AlbumPage {
        val referer = "https://weibo.com/u/$uid?tabtype=album"
        val waterfallCursor = cursor
            ?.removePrefix("waterfall:cursor:")
            ?.ifBlank { "0" }
            ?: "0"
        val isFirstPage = cursor == null || waterfallCursor == "0"
        val monthContext = WeiboJsonParser.AlbumMonthContext()
        val params = linkedMapOf(
            "uid" to uid,
            "cursor" to waterfallCursor,
        )
        val raw = fetchJson(WeiboEndpoints.PROFILE_ALBUM_WATERFALL, params, referer)

        val wall = WeiboJsonParser.parseImageWallPage(
            raw = raw,
            monthContext = monthContext,
        )
        val images = wall.images.distinctByAlbumKey()
        onPageLoaded?.invoke(images)

        val nextPageCursor = wall.nextCursor?.let { "waterfall:cursor:$it" }

        return AlbumPage(
            images = images,
            nextCursor = nextPageCursor,
            fetchTrace = AlbumFetchTrace(
                source = AlbumFetchSource.Waterfall,
                imageCount = images.size,
                isFirstPage = isFirstPage,
                fallbackFromEmptyWall = fallbackFromEmptyWall,
            ),
        )
    }

    private fun List<FeedImage>.distinctByAlbumKey(): List<FeedImage> =
        distinctBy { "${it.type.orEmpty()}:${it.id}:${it.largeUrl}" }

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

            TimelineKind.FriendsCircle -> linkedMapOf(
                "list_id" to FRIENDS_CIRCLE_GID,
                "refresh" to "4",
                "fast_refresh" to "1",
                "count" to "25",
            ).apply {
                if (cursor != null) {
                    put("max_id", cursor)
                }
            }
        }

        val referer = when (kind) {
            TimelineKind.FriendsCircle -> "https://weibo.com/mygroups?gid=$FRIENDS_CIRCLE_GID"
            else -> WEIBO_HOME
        }
        return fetchJson(WeiboEndpoints.timelinePath(kind), params, referer)
    }

    data class CommentsPage(
        val items: List<CommentItem>,
        val nextCursor: String?,
        val totalCount: Int? = null,
    )

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
        val hasSameMedia = medias.isNotEmpty() || other.medias.isEmpty()
        return hasText && textIsNotShorter && hasSameImages && hasSameMedia
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
        return CommentsPage(
            items = items,
            nextCursor = cursor.takeUnless { it.isNullOrBlank() || it == "0" },
            totalCount = WeiboJsonParser.parseCommentsTotalCount(raw),
        )
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
        return CommentsPage(
            items = items,
            nextCursor = nextCursor.takeUnless { it.isNullOrBlank() || it == "0" },
            totalCount = WeiboJsonParser.parseCommentsTotalCount(raw),
        )
    }

    suspend fun loadNestedComments(
        commentId: String,
        statusAuthorUid: String,
        cursor: String? = null,
    ): CommentsPage {
        val isFirstPage = cursor.isNullOrBlank()
        val raw = fetchJson(
            WeiboEndpoints.STATUS_COMMENTS,
            linkedMapOf(
                "flow" to "1",
                "id" to commentId,
                "uid" to statusAuthorUid,
                "is_reload" to if (isFirstPage) "1" else "0",
                "is_show_bulletin" to "2",
                "is_mix" to "1",
                "fetch_level" to "1",
                "count" to "20",
                "max_id" to (cursor?.takeIf { it.isNotBlank() } ?: "0"),
                "locale" to "zh",
            ),
        )
        val items = WeiboJsonParser.parseComments(raw)
        val json = org.json.JSONObject(raw)
        val nextCursor = json.optJSONObject("data")?.nullableString("max_id")
            ?: json.nullableString("max_id")
        return CommentsPage(items, nextCursor.takeUnless { it.isNullOrBlank() || it == "0" })
    }

    suspend fun postStatusComment(
        item: FeedItem,
        text: String,
        replyToCommentId: String? = null,
        alsoRepost: Boolean = false,
        picId: String? = null,
    ) {
        val resolvedPicId = picId?.trim().orEmpty()
        val commentText = text.trim()
        require(commentText.isNotBlank() || resolvedPicId.isNotBlank()) { "评论内容不能为空" }
        val statusId = item.id.takeIf { it.isNotBlank() } ?: item.statusId
        require(statusId.isNotBlank()) { "无效的微博 ID" }
        val params = linkedMapOf(
            "id" to statusId,
            "comment" to commentText,
            "pic_id" to resolvedPicId,
            "is_repost" to if (alsoRepost) "1" else "0",
            "comment_ori" to "0",
            "is_comment" to "0",
            "fp" to "",
        )
        val endpoint = if (!replyToCommentId.isNullOrBlank()) {
            params["cid"] = replyToCommentId
            WeiboEndpoints.COMMENT_REPLY
        } else {
            WeiboEndpoints.COMMENT_CREATE
        }
        val raw = postForm(
            path = endpoint,
            params = params,
            referer = "https://weibo.com/detail/${item.statusId.takeIf { it.isNotBlank() } ?: statusId}",
        )
        WeiboJsonParser.assertMutationSuccess(raw, "评论发布失败")
    }

    suspend fun uploadCommentImage(uri: Uri): String =
        runCatching {
            nativeUploadImage(uri)
        }.recoverCatching { error ->
            val message = error.message.orEmpty()
            if (
                message.contains("image-upload-redirected-to-html") ||
                message.contains("图片上传失败:302")
            ) {
                webViewUploadImage(uri)
            } else {
                throw error
            }
        }.getOrThrow()

    suspend fun loadAllRelationUsers(
        uid: String,
        tab: FriendListTab,
        maxPages: Int = RELATION_LIST_MAX_PAGES,
    ): List<RelationUser> {
        val targetUid = uid.trim()
        require(targetUid.isNotBlank()) { "无效的用户 UID" }
        val merged = linkedMapOf<String, RelationUser>()
        var page = 1
        while (page <= maxPages) {
            val relationPage = loadFriends(targetUid, page, tab)
            if (relationPage.items.isEmpty()) break
            for (user in relationPage.items) {
                val key = user.id.ifBlank { user.name }
                if (key.isBlank()) continue
                merged.putIfAbsent(key, user)
            }
            if (!relationPage.hasNextPage) break
            page++
        }
        return merged.values.toList()
    }

    suspend fun loadMentionSuggestionBundle(uid: String): MentionCandidateBundle {
        val targetUid = uid.trim()
        require(targetUid.isNotBlank()) { "无效的用户 UID" }
        val following = loadAllRelationUsers(targetUid, FriendListTab.Following)
        val fans = loadAllRelationUsers(targetUid, FriendListTab.Fans)
        val mergedRelations = mergeRelationUsers(following, fans)
        val avatarSuggestions = mergedRelations
            .filter { it.following && it.followMe }
            .mapNotNull { it.toMentionCandidate() }
            .let { dedupeMentionCandidates(it, emptyList()) }
            .take(MUTUAL_FOLLOW_MENTION_LIMIT)
        val nameIndex = dedupeMentionCandidates(
            mergedRelations.mapNotNull { it.toMentionCandidate() },
            emptyList(),
        )
        return MentionCandidateBundle(
            avatarSuggestions = avatarSuggestions,
            nameIndex = nameIndex,
        )
    }

    suspend fun loadLikeUsers(statusId: String, page: Int = 1): LikeUsersPage {
        ensureMweiboSession()
        val id = statusId.trim()
        require(id.isNotBlank()) { "无效的微博 ID" }
        val url = Uri.Builder()
            .scheme("https")
            .authority("m.weibo.cn")
            .appendPath("api")
            .appendPath("attitudes")
            .appendPath("show")
            .appendQueryParameter("id", id)
            .appendQueryParameter("page", page.toString())
            .build()
            .toString()
        val referer = "https://m.weibo.cn/detail/$id"
        val raw = nativeFetchMweiboJson(url, referer)
        return WeiboJsonParser.parseMweiboAttitudes(raw, page)
    }

    suspend fun loadReposts(item: FeedItem, page: Int = 1): RepostsPage {
        if (page <= 1 && item.hasNoReposts()) {
            return RepostsPage(items = emptyList(), nextPage = null)
        }
        val candidates = repostTimelineStatusIdCandidates(item)
        return loadPcReposts(item, candidates, page)
    }

    private suspend fun loadPcReposts(
        item: FeedItem,
        candidates: List<String>,
        page: Int,
    ): RepostsPage {
        var lastError: Throwable? = null
        for ((index, statusId) in candidates.withIndex()) {
            val parameterSets = listOf(
                linkedMapOf(
                    "id" to statusId,
                    "page" to page.toString(),
                    "moduleID" to "feed",
                    "count" to "20",
                ),
                linkedMapOf(
                    "id" to statusId,
                    "page" to page.toString(),
                    "is_reload" to if (page <= 1) "1" else "0",
                    "count" to "20",
                ),
                linkedMapOf(
                    "id" to statusId,
                    "page" to page.toString(),
                ),
            )
            for (params in parameterSets) {
                runCatching {
                    val referer = "https://weibo.com/${item.authorId}/${repostTimelineDetailRefererId(item, statusId)}"
                    val raw = runCatching {
                        fetchJson(WeiboEndpoints.STATUS_REPOST_TIMELINE, params, referer)
                    }.recoverCatching {
                        webViewFetchJson(WeiboEndpoints.STATUS_REPOST_TIMELINE, params)
                    }.getOrThrow()
                    val parsed = WeiboJsonParser.parsePcReposts(raw, page)
                    if (page <= 1 &&
                        parsed.items.isEmpty() &&
                        !item.hasNoReposts() &&
                        index < candidates.lastIndex
                    ) {
                        throw IllegalStateException("empty-pc-reposts-for-candidate:$statusId")
                    }
                    parsed
                }.onSuccess { return it }
                    .onFailure { lastError = it }
            }
        }
        throw lastError ?: IllegalStateException("PC转发列表加载失败")
    }

    /** PC 转发接口通常优先接受 mblogid/bid，失败时再尝试数字 mid/idstr。 */
    private fun repostTimelineStatusIdCandidates(item: FeedItem): List<String> {
        val raw = listOf(item.statusId, item.id).distinct().filter { it.isNotBlank() }
        require(raw.isNotEmpty()) { "无效的微博 ID" }
        return raw
    }

    private fun repostTimelineDetailRefererId(item: FeedItem, timelineId: String): String =
        item.statusId.takeIf { it.isNotBlank() } ?: timelineId

    private fun FeedItem.hasNoReposts(): Boolean {
        val value = repostsCount.trim()
        return value == "0" || value == "--" || value.equals("null", ignoreCase = true)
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

    suspend fun loadFriends(uid: String, page: Int, tab: FriendListTab): RelationPage {
        val targetUid = uid.trim()
        require(targetUid.isNotBlank()) { "无效的用户 UID" }
        val params = linkedMapOf(
            "uid" to targetUid,
            "page" to page.toString(),
        )
        if (tab == FriendListTab.Fans) {
            params["relate"] = "fans"
            params["count"] = "20"
            params["type"] = "fans"
            params["fansSortType"] = "followTime"
        }
        val raw = fetchJson(
            path = WeiboEndpoints.FRIENDS,
            params = params,
            referer = "https://weibo.com/u/$targetUid",
        )
        return WeiboJsonParser.parseFriendsPage(raw)
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
        return enrichUserProfileWithDetail(WeiboJsonParser.parseUserProfile(raw))
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
        return enrichUserProfileWithDetail(WeiboJsonParser.parseUserProfile(raw, config))
    }

    private suspend fun enrichUserProfileWithDetail(profile: UserProfile): UserProfile {
        val uid = profile.id.takeIf { it.isNotBlank() } ?: return profile
        if (!profile.ipLocation.isNullOrBlank()) return profile
        return runCatching {
            val detailRaw = fetchJson(
                WeiboEndpoints.PROFILE_DETAIL,
                linkedMapOf("uid" to uid),
            )
            WeiboJsonParser.mergeProfileDetail(profile, detailRaw)
        }.getOrDefault(profile)
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

    suspend fun loadArticle(articleId: String): WeiboArticle {
        val showUrl = articleShowUrl(articleId)
        val detailUrl = "https://card.weibo.com/article/m/aj/detail?id=$articleId"
        val raw = fetchAbsoluteJson(detailUrl, showUrl, "https://card.weibo.com")
        return WeiboArticleParser.parseDetail(raw, articleId)
    }

    suspend fun fetchAbsoluteJson(
        url: String,
        referer: String,
        origin: String = referer,
    ): String = nativeFetchAbsoluteJson(url, referer, origin)

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
            val cookie = mergedCookieHeader(referer)
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
                instanceFollowRedirects = true
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

    private suspend fun nativeUploadImage(uri: Uri): String =
        withContext(Dispatchers.IO) {
            CookieManager.getInstance().flush()
            val cookie = mergedCookieHeader("https://picupload.weibo.com/")
            if (!hasAuthenticatedCookie(cookie)) {
                throw IllegalStateException("未发现微博登录 Cookie，请到账户页登录后重试")
            }
            val resolver = appContext.contentResolver
            val mime = resolver.getType(uri)?.takeIf { it.isNotBlank() } ?: "image/jpeg"
            val bytes = resolver.openInputStream(uri)?.use { it.readBytes() }
                ?: throw IllegalStateException("无法读取所选图片")
            val base64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
            val params = linkedMapOf(
                "mime" to mime,
                "data" to base64,
                "url" to "weibo.com",
                "markpos" to "1",
                "logo" to "1",
                "nick" to "",
                "marks" to "1",
                "app" to "miniblog",
                "s" to "rdxt",
                "pri" to "null",
                "file_source" to "1",
            )
            val body = params.entries.joinToString("&") { (key, value) ->
                "${key.urlEncode()}=${value.urlEncode()}"
            }
            val connection = (URL("https://picupload.weibo.com/interface/pic_upload.php").openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                doOutput = true
                connectTimeout = 20_000
                readTimeout = 30_000
                instanceFollowRedirects = true
                setRequestProperty("User-Agent", DESKTOP_CHROME_USER_AGENT)
                setRequestProperty("Accept", "application/json, text/plain, */*")
                setRequestProperty("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8")
                setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                setRequestProperty("Referer", WEIBO_HOME)
                setRequestProperty("Origin", "https://weibo.com")
                setRequestProperty("X-Requested-With", "XMLHttpRequest")
                setRequestProperty("Cookie", cookie)
            }
            extractCookieValue(cookie, "XSRF-TOKEN")?.let { token ->
                connection.setRequestProperty("X-XSRF-TOKEN", URLDecoder.decode(token, Charsets.UTF_8.name()))
            }
            connection.outputStream.bufferedWriter(Charsets.UTF_8).use { it.write(body) }

            val status = connection.responseCode
            syncResponseCookies(connection)
            val responseBody = if (status in 200..299) {
                connection.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
            } else {
                val errorBody = connection.errorStream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
                throw IllegalStateException("图片上传失败:$status ${errorBody.take(120)}")
            }
            if (responseBody.trimStart().startsWith("<")) {
                throw IllegalStateException("image-upload-redirected-to-html")
            }
            parseUploadPicId(responseBody)
        }

    private suspend fun webViewUploadImage(uri: Uri): String {
        val (mime, base64) = withContext(Dispatchers.IO) {
            val resolver = appContext.contentResolver
            val resolvedMime = resolver.getType(uri)?.takeIf { it.isNotBlank() } ?: "image/jpeg"
            val bytes = resolver.openInputStream(uri)?.use { it.readBytes() }
                ?: throw IllegalStateException("无法读取所选图片")
            resolvedMime to Base64.encodeToString(bytes, Base64.NO_WRAP)
        }
        ensureOnWeiboOrigin()
        waitForWeiboOrigin()
        val payload = evaluateJson(
            """
            (async function() {
              try {
                const params = new URLSearchParams();
                params.set('mime', ${mime.jsQuote()});
                params.set('data', ${base64.jsQuote()});
                params.set('url', 'weibo.com');
                params.set('markpos', '1');
                params.set('logo', '1');
                params.set('nick', '');
                params.set('marks', '1');
                params.set('app', 'miniblog');
                params.set('s', 'rdxt');
                params.set('pri', 'null');
                params.set('file_source', '1');
                const response = await fetch('https://picupload.weibo.com/interface/pic_upload.php', {
                  method: 'POST',
                  credentials: 'include',
                  mode: 'cors',
                  referrer: 'https://weibo.com/',
                  headers: {
                    'accept': 'application/json, text/plain, */*',
                    'content-type': 'application/x-www-form-urlencoded; charset=UTF-8',
                    'x-requested-with': 'XMLHttpRequest'
                  },
                  body: params.toString()
                });
                const body = await response.text();
                return { ok: response.ok, status: response.status, body: body, url: response.url };
              } catch (error) {
                return { ok: false, status: 0, body: '', error: String(error), url: location.href };
              }
            })()
            """.trimIndent(),
        )
        val body = payload.nullableString("body").orEmpty()
        if (!payload.optBoolean("ok") || body.isBlank()) {
            val status = payload.optInt("status")
            val error = payload.nullableString("error").orEmpty()
            throw IllegalStateException("图片上传失败:${status.takeUnless { it == 0 } ?: ""} ${error.ifBlank { body.take(120) }}".trim())
        }
        if (body.trimStart().startsWith("<")) {
            throw IllegalStateException("image-upload-redirected-to-html")
        }
        return parseUploadPicId(body)
    }

    private fun parseUploadPicId(raw: String): String {
        val jsonText = raw.trim()
            .removePrefix("(")
            .removeSuffix(")")
            .let { text ->
                val start = text.indexOf('{')
                val end = text.lastIndexOf('}')
                if (start >= 0 && end > start) text.substring(start, end + 1) else text
            }
        val root = runCatching { JSONObject(jsonText) }
            .getOrElse { throw IllegalStateException("图片上传返回无法解析: ${raw.take(80)}") }
        val direct = root.optStringOrNull("pic_id")
            ?: root.optStringOrNull("pid")
            ?: root.optJSONObject("data")?.optStringOrNull("pic_id")
            ?: root.optJSONObject("data")?.optStringOrNull("pid")
        if (!direct.isNullOrBlank()) return direct
        val pics = root.optJSONObject("data")?.optJSONObject("pics") ?: root.optJSONObject("pics")
        if (pics != null) {
            val keys = pics.keys()
            while (keys.hasNext()) {
                val item = pics.optJSONObject(keys.next()) ?: continue
                val pid = item.optStringOrNull("pid")
                    ?: item.optStringOrNull("pic_id")
                    ?: item.optStringOrNull("id")
                if (!pid.isNullOrBlank()) return pid
            }
        }
        val code = root.optStringOrNull("code")
        val message = root.optStringOrNull("msg") ?: root.optStringOrNull("message") ?: raw.take(80)
        throw IllegalStateException("图片上传失败${code?.let { ":$it" }.orEmpty()} $message")
    }

    private fun JSONObject.optStringOrNull(name: String): String? {
        if (!has(name) || isNull(name)) return null
        return opt(name)?.toString()?.trim()?.takeIf { it.isNotBlank() && it != "null" }
    }

    private fun extractCookieValue(cookie: String, name: String): String? =
        cookie.split(";")
            .map { it.trim() }
            .firstOrNull { it.startsWith("$name=") }
            ?.substringAfter("=")
            ?.takeIf { it.isNotBlank() }

    private suspend fun nativeFetchAbsoluteJson(url: String, referer: String, origin: String): String =
        nativeFetchAbsoluteJsonInternal(
            url = url,
            referer = referer,
            origin = origin,
            userAgent = DESKTOP_CHROME_USER_AGENT,
            cookie = mergedCookieHeader(referer),
        )

    private suspend fun nativeFetchMweiboJson(url: String, referer: String = "https://m.weibo.cn/"): String =
        nativeFetchAbsoluteJsonInternal(
            url = url,
            referer = referer,
            origin = "https://m.weibo.cn",
            userAgent = MWEIBO_USER_AGENT,
            cookie = mergedCookieHeader("https://m.weibo.cn/"),
        )

    private suspend fun nativeFetchAbsoluteHtml(url: String, referer: String, origin: String): String =
        nativeFetchAbsoluteTextInternal(
            url = url,
            referer = referer,
            origin = origin,
            accept = "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8",
            userAgent = DESKTOP_CHROME_USER_AGENT,
            cookie = mergedCookieHeader(referer),
            requestedWith = false,
        )

    private suspend fun nativeFetchAbsoluteJsonInternal(
        url: String,
        referer: String,
        origin: String,
        userAgent: String,
        cookie: String,
    ): String =
        withContext(Dispatchers.IO) {
            CookieManager.getInstance().flush()
            if (!hasAuthenticatedCookie(cookie)) {
                throw IllegalStateException("\u672A\u53D1\u73B0\u5FAE\u535A\u767B\u5F55 Cookie")
            }

            val connection = (URL(url).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 12_000
                readTimeout = 12_000
                instanceFollowRedirects = false
                setRequestProperty("User-Agent", userAgent)
                setRequestProperty("Accept", "application/json, text/plain, */*")
                setRequestProperty("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8")
                setRequestProperty("Referer", referer)
                setRequestProperty("Origin", origin)
                setRequestProperty("X-Requested-With", "XMLHttpRequest")
                setRequestProperty("Cookie", cookie)
            }
            extractCookieValue(cookie, "XSRF-TOKEN")?.let { token ->
                connection.setRequestProperty(
                    "X-XSRF-TOKEN",
                    URLDecoder.decode(token, Charsets.UTF_8.name()),
                )
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

    private suspend fun nativeFetchAbsoluteTextInternal(
        url: String,
        referer: String,
        origin: String,
        accept: String,
        userAgent: String,
        cookie: String,
        requestedWith: Boolean,
    ): String =
        withContext(Dispatchers.IO) {
            CookieManager.getInstance().flush()
            if (!hasAuthenticatedCookie(cookie)) {
                throw IllegalStateException("\u672A\u53D1\u73B0\u5FAE\u535A\u767B\u5F55 Cookie")
            }

            val connection = (URL(url).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 12_000
                readTimeout = 12_000
                instanceFollowRedirects = true
                setRequestProperty("User-Agent", userAgent)
                setRequestProperty("Accept", accept)
                setRequestProperty("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8")
                setRequestProperty("Referer", referer)
                setRequestProperty("Origin", origin)
                if (requestedWith) {
                    setRequestProperty("X-Requested-With", "XMLHttpRequest")
                }
                setRequestProperty("Cookie", cookie)
            }

            val status = connection.responseCode
            syncResponseCookies(connection)
            if (status in 200..299) {
                connection.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
            } else {
                val errorBody = connection.errorStream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }
                    .orEmpty()
                throw IllegalStateException("weibo-html-request-failed:$status ${errorBody.take(120)}")
            }
        }

    private fun mergedCookieHeader(primaryOrigin: String): String {
        val jar = linkedMapOf<String, String>()
        listOf(
            primaryOrigin,
            "https://picupload.weibo.com/",
            "https://s.weibo.com/",
            "https://m.weibo.cn/",
            WEIBO_HOME,
            "https://www.weibo.com/",
        ).forEach { origin ->
            CookieManager.getInstance().getCookie(origin)
                ?.split(";")
                ?.forEach { part ->
                    val trimmed = part.trim()
                    if (trimmed.isBlank()) return@forEach
                    jar[trimmed.substringBefore("=")] = trimmed
                }
        }
        return jar.values.joinToString("; ")
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

    @SuppressLint("SetJavaScriptEnabled")
    private fun configureWebView() {
        CookieManager.getInstance().setAcceptCookie(true)
        CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true)

        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            mediaPlaybackRequiresUserGesture = false
            loadsImagesAutomatically = true
            useWideViewPort = true
            loadWithOverviewMode = false
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
                if (currentUrl.contains("passport.weibo")) {
                    if (viewportCoordinator.shouldFitOnPageFinished(url) &&
                        viewportCoordinator.debounceAllowed()
                    ) {
                        view?.fitSessionWebViewport(scrollToTop = false)
                    }
                }
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

    suspend fun persistCurrentAccount(
        store: WeiboAccountStore,
        makeActive: Boolean = false,
    ): StoredWeiboAccount? {
        if (!hasLoginCookie()) return null
        val profile = loadCurrentUserProfile()
        val account = StoredWeiboAccount(
            id = profile.id,
            screenName = profile.screenName,
            avatarUrl = profile.avatarUrl,
            cookies = captureCookieSnapshot(),
        )
        store.upsertAccount(account, makeActive = makeActive)
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
        withContext(Dispatchers.Main) {
            webView.stopLoading()
            webView.loadUrl("about:blank")
            webView.onPause()
        }
        clearAllCookies()
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
        private const val MUTUAL_FOLLOW_MENTION_LIMIT = 40
        private const val RELATION_LIST_MAX_PAGES = 100
        private const val FRIENDS_CIRCLE_GID = "100097312739005"
        private const val WEIBO_PASSPORT_LOGIN = "https://passport.weibo.cn/signin/login"
        private const val WEIBO_PASSPORT_ORIGIN = "https://passport.weibo.cn/"
        private val COOKIE_ORIGINS = listOf(
            "https://weibo.com/",
            "https://www.weibo.com/",
            "https://s.weibo.com/",
            "https://picupload.weibo.com/",
            "https://passport.weibo.cn/",
            "https://m.weibo.cn/",
            "https://card.weibo.com/",
        )
        private const val DESKTOP_CHROME_USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 " +
                "(KHTML, like Gecko) Chrome/126.0.0.0 Safari/537.36"
        private const val MWEIBO_USER_AGENT =
            "Mozilla/5.0 (iPhone; CPU iPhone OS 16_0 like Mac OS X) AppleWebKit/605.1.15 " +
                "(KHTML, like Gecko) Mobile/15E148 Weibo (iPhone14,2__weibo__14.9.0__iphone__os16.0)"
    }
}

private class SessionWebViewportCoordinator {
    private var lastFittedUrl: String? = null
    private var lastFitAtMs: Long = 0L

    fun shouldFitOnPageFinished(url: String?): Boolean {
        val normalized = url.orEmpty()
        if (normalized == lastFittedUrl) return false
        lastFittedUrl = normalized
        return true
    }

    fun debounceAllowed(minIntervalMs: Long = 800L): Boolean {
        val now = System.currentTimeMillis()
        if (now - lastFitAtMs < minIntervalMs) return false
        lastFitAtMs = now
        return true
    }
}

private fun WebView.fitSessionWebViewport(scrollToTop: Boolean = false) {
    evaluateJavascript(
        """
        (function() {
            var meta = document.querySelector('meta[name="viewport"]');
            if (!meta) {
                meta = document.createElement('meta');
                meta.name = 'viewport';
                document.head.appendChild(meta);
            }
            var viewport = 'width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no, viewport-fit=cover';
            if (meta.content !== viewport) {
                meta.content = viewport;
            }
            var width = window.innerWidth + 'px';
            var height = window.innerHeight + 'px';
            if (document.documentElement.style.width !== width) {
                document.documentElement.style.width = width;
            }
            if (document.documentElement.style.height !== height) {
                document.documentElement.style.height = height;
            }
            if (document.body.style.width !== width) {
                document.body.style.width = width;
            }
            if (document.body.style.minHeight !== height) {
                document.body.style.minHeight = height;
            }
            ${if (scrollToTop) "window.scrollTo(0, 0);" else ""}
        })();
        """.trimIndent(),
        null,
    )
}
