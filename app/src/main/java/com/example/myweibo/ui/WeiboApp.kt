package com.example.myweibo.ui

import android.graphics.BitmapFactory
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebView
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.changedToUpIgnoreConsumed
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.myweibo.R
import com.example.myweibo.data.AlbumPage
import com.example.myweibo.data.CommentItem
import com.example.myweibo.data.FeedImage
import com.example.myweibo.data.FeedItem
import com.example.myweibo.data.ProfileLookup
import com.example.myweibo.data.FeedMedia
import com.example.myweibo.data.MediaType
import com.example.myweibo.data.MineCacheStore
import com.example.myweibo.data.MinePostsCache
import com.example.myweibo.data.NativeUiMessage
import com.example.myweibo.data.TimelineCacheStore
import com.example.myweibo.data.TimelineKind
import com.example.myweibo.data.UserProfile
import com.example.myweibo.data.WeiboWebSession
import com.example.myweibo.data.WeiboJsonParser
import com.example.myweibo.data.formatWeiboTime
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.net.HttpURLConnection
import java.net.URL
import kotlin.math.abs
import kotlin.math.hypot

private enum class MainTab(val label: String) {
    Search("\u641C\u7D22"),
    Messages("\u6D88\u606F"),
    Feed("\u9996\u9875"),
    Mine("\u6211\u7684"),
    Compose("\u5199\u5FAE\u535A")
}

private enum class MineContentTab(val label: String) {
    Posts("\u5FAE\u535A"),
    Album("\u76F8\u518C")
}

private class VideoPlaybackCoordinator {
    var activeKey by mutableStateOf<String?>(null)
    val positions = mutableStateMapOf<String, Long>()
}

private val LocalVideoPlaybackCoordinator = staticCompositionLocalOf { VideoPlaybackCoordinator() }

private fun videoPlaybackKey(media: FeedMedia): String =
    media.streamUrl.ifBlank { media.downloadUrl.orEmpty() }.ifBlank { media.coverUrl.orEmpty() }

/*
private enum class LegacyMainTab(val label: String) {
    Feed("Feed"),
    Account("Account"),
}

*/

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeiboApp() {
    val context = LocalContext.current
    val session = remember { WeiboWebSession(context) }
    val timelineCacheStore = remember { TimelineCacheStore(context) }
    val mineCacheStore = remember { MineCacheStore(context) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val feedListState = rememberLazyListState()
    val minePostsListState = rememberLazyListState()
    val videoPlaybackCoordinator = remember { VideoPlaybackCoordinator() }

    var selectedTab by remember { mutableStateOf(MainTab.Feed) }
    val timelineKind = TimelineKind.Following
    var items by remember { mutableStateOf<List<FeedItem>>(emptyList()) }
    var nextCursor by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var selectedItem by remember { mutableStateOf<FeedItem?>(null) }
    var comments by remember { mutableStateOf<List<CommentItem>>(emptyList()) }
    var commentsLoading by remember { mutableStateOf(false) }
    var commentsCursor by remember { mutableStateOf<String?>(null) }
    var commentsHasMore by remember { mutableStateOf(true) }
    var mediaPreview by remember { mutableStateOf<FeedMedia?>(null) }
    var message by remember { mutableStateOf<NativeUiMessage?>(null) }
    var hasLoginCookie by remember { mutableStateOf(session.hasLoginCookie()) }
    var cacheLoaded by remember { mutableStateOf(false) }
    var mineProfile by remember { mutableStateOf<UserProfile?>(null) }
    var mineProfileLoading by remember { mutableStateOf(false) }
    var mineProfileError by remember { mutableStateOf<String?>(null) }
    var mineHasLoginCookie by remember { mutableStateOf(session.hasLoginCookie()) }
    var minePosts by remember { mutableStateOf<List<FeedItem>>(emptyList()) }
    var minePostsError by remember { mutableStateOf<String?>(null) }
    var minePostsPage by remember { mutableStateOf(1) }
    var minePostsHasMore by remember { mutableStateOf(true) }
    var minePostsLoadingMore by remember { mutableStateOf(false) }
    var mineAlbumImages by remember { mutableStateOf<List<FeedImage>>(emptyList()) }
    var mineAlbumNextCursor by remember { mutableStateOf<String?>(null) }
    var mineAlbumHasMore by remember { mutableStateOf(true) }
    var mineAlbumLoadingMore by remember { mutableStateOf(false) }
    var emoticonMap by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var emoticonSyncing by remember { mutableStateOf(false) }
    var visitedUserId by remember { mutableStateOf<String?>(null) }
    var visitedUserScreenName by remember { mutableStateOf("") }
    var visitedProfile by remember { mutableStateOf<UserProfile?>(null) }
    var visitedProfileLoading by remember { mutableStateOf(false) }
    var visitedPosts by remember { mutableStateOf<List<FeedItem>>(emptyList()) }
    var visitedPostsHasMore by remember { mutableStateOf(true) }
    var visitedPostsPage by remember { mutableStateOf(1) }
    var visitedPostsLoadingMore by remember { mutableStateOf(false) }
    var visitedAlbumImages by remember { mutableStateOf<List<FeedImage>>(emptyList()) }
    var visitedAlbumHasMore by remember { mutableStateOf(false) }

    fun loadVisitedUserProfile(lookup: ProfileLookup) {
        scope.launch {
            visitedProfileLoading = true
            visitedAlbumImages = emptyList()
            runCatching { session.loadUserProfile(lookup) }
                .onSuccess { profile ->
                    visitedProfile = profile
                    visitedUserId = profile.id
                    visitedUserScreenName = profile.screenName
                    runCatching { session.loadUserTimeline(profile.id) }
                        .onSuccess { page ->
                            visitedPosts = page.items
                            visitedPostsPage = 1
                            visitedPostsHasMore = page.items.isNotEmpty()
                        }
                    runCatching { session.loadUserAlbumImages(profile.id) }
                        .onSuccess { page ->
                            visitedAlbumImages = page.images
                        }
                }
                .onFailure { visitedProfile = null }
            visitedProfileLoading = false
        }
    }

    fun loadMoreVisitedPosts() {
        val uid = visitedUserId ?: return
        if (visitedPostsLoadingMore || visitedProfileLoading || !visitedPostsHasMore) return
        scope.launch {
            visitedPostsLoadingMore = true
            val next = visitedPostsPage + 1
            runCatching { session.loadUserTimeline(uid, next) }
                .onSuccess { page ->
                    visitedPosts = (visitedPosts + page.items).distinctBy { it.id }
                    visitedPostsPage = next
                    visitedPostsHasMore = page.items.isNotEmpty()
                }
            visitedPostsLoadingMore = false
        }
    }

    fun openUser(idOrScreenName: String) {
        val value = idOrScreenName.trim()
        if (value.isBlank()) return
        loadVisitedUserProfile(
            if (value.all { it.isDigit() }) {
                ProfileLookup.Uid(value)
            } else {
                ProfileLookup.ScreenName(value)
            }
        )
    }

    BackHandler(
        enabled = mediaPreview != null || selectedItem != null || visitedUserId != null || selectedTab != MainTab.Feed
    ) {
        when {
            mediaPreview != null -> mediaPreview = null
            selectedItem != null -> selectedItem = null
            visitedUserId != null -> visitedUserId = null
            selectedTab != MainTab.Feed -> selectedTab = MainTab.Feed
        }
    }

    fun showMessage(title: String, detail: String) {
        message = NativeUiMessage(title, detail)
        scope.launch { snackbarHostState.showSnackbar("$title\uFF1A$detail") }
    }

    fun refreshTimeline() {
        scope.launch {
            isLoading = true
            hasLoginCookie = session.hasLoginCookie()
            feedListState.animateScrollToItem(0)
            runCatching {
                val raw = session.loadTimelineRaw(timelineKind)
                timelineCacheStore.writeFollowingTimeline(raw)
                WeiboJsonParser.parseTimeline(raw)
            }
                .onSuccess { page ->
                    items = page.items
                    nextCursor = page.nextCursor
                    hasLoginCookie = true
                    if (page.items.isEmpty()) {
                        showMessage("\u6CA1\u6709\u8BFB\u5230\u4FE1\u606F\u6D41", "\u8BF7\u5148\u5728\u6211\u7684\u9875\u9762\u8BBE\u7F6E\u4E2D\u767B\u5F55\u5FAE\u535A\uFF0C\u6216\u7A0D\u540E\u518D\u5237\u65B0")
                    }
                }
                .onFailure { error ->
                    showMessage("同步失败", error.message ?: "请确认已登录 weibo.com")
                }
            isLoading = false
            feedListState.animateScrollToItem(0)
        }
    }

    fun refreshTimelineFromTop() {
        scope.launch {
            feedListState.animateScrollToItem(0)
            isLoading = true
            hasLoginCookie = session.hasLoginCookie()
            runCatching {
                val raw = session.loadTimelineRaw(timelineKind)
                timelineCacheStore.writeFollowingTimeline(raw)
                WeiboJsonParser.parseTimeline(raw)
            }
                .onSuccess { page ->
                    items = page.items
                    nextCursor = page.nextCursor
                    hasLoginCookie = true
                    if (page.items.isEmpty()) {
                        showMessage("\u6CA1\u6709\u8BFB\u5230\u4FE1\u606F\u6D41", "\u8BF7\u5148\u5728\u6211\u7684\u9875\u9762\u8BBE\u7F6E\u4E2D\u767B\u5F55\u5FAE\u535A\uFF0C\u6216\u7A0D\u540E\u518D\u5237\u65B0")
                    }
                }
                .onFailure { error ->
                    showMessage("同步失败", error.message ?: "请确认已登录 weibo.com")
                }
            isLoading = false
            feedListState.animateScrollToItem(0)
        }
    }

    fun loadMore() {
        val cursor = nextCursor ?: return
        scope.launch {
            isLoading = true
            runCatching { session.loadTimeline(timelineKind, cursor) }
                .onSuccess { page ->
                    items = items + page.items
                    nextCursor = page.nextCursor
                }
                .onFailure { error -> showMessage("\u52A0\u8F7D\u5931\u8D25", error.message ?: "\u65E0\u6CD5\u8BFB\u53D6\u4E0B\u4E00\u9875") }
            isLoading = false
        }
    }

    fun refreshMineProfile() {
        mineHasLoginCookie = session.hasLoginCookie()
        if (!mineHasLoginCookie) {
            mineProfileError = "\u8BF7\u5148\u5728\u8BBE\u7F6E\u4E2D\u767B\u5F55\u5FAE\u535A"
            return
        }
        scope.launch {
            mineProfileLoading = true
            runCatching { session.loadCurrentUserProfile() }
                .onSuccess { profile ->
                    mineProfile = profile
                    mineProfileError = null
                    mineHasLoginCookie = true
                    mineCacheStore.writeProfile(profile)
                    runCatching { session.loadUserTimeline(profile.id) }
                        .onSuccess { page ->
                            minePosts = page.items
                            minePostsPage = 1
                            minePostsHasMore = page.items.isNotEmpty()
                            minePostsError = null
                            mineCacheStore.writePosts(MinePostsCache(page.items, page = 1, hasMore = page.items.isNotEmpty()))
                        }
                        .onFailure {
                            minePostsError = it.message ?: "\u65E0\u6CD5\u8BFB\u53D6\u7528\u6237\u4E3B\u9875\u5FAE\u535A"
                        }
                    runCatching { session.loadUserAlbumImages(profile.id) }
                        .onSuccess { page ->
                            mineAlbumImages = page.images
                            mineAlbumNextCursor = page.nextCursor
                            mineAlbumHasMore = page.nextCursor != null
                            mineCacheStore.writeAlbum(page)
                        }
                        .onFailure {
                            // API 加载失败时直接用帖子里提取的图片兜底
                            mineAlbumImages = ownAlbumImagesFromPosts(minePosts)
                            mineCacheStore.writeAlbum(AlbumPage(images = mineAlbumImages))
                            mineAlbumHasMore = false
                        }
                }
                .onFailure {
                    mineProfileError = it.message ?: "\u65E0\u6CD5\u8BFB\u53D6\u5FAE\u535A\u7528\u6237\u8D44\u6599"
                }
            mineProfileLoading = false
        }
    }

    fun loadMoreMinePosts() {
        val uid = mineProfile?.id?.takeIf { it.isNotBlank() } ?: return
        if (minePostsLoadingMore || mineProfileLoading || !minePostsHasMore) return
        scope.launch {
            minePostsLoadingMore = true
            val nextPage = minePostsPage + 1
            runCatching { session.loadUserTimeline(uid, page = nextPage) }
                .onSuccess { page ->
                    val merged = (minePosts + page.items).distinctBy { it.id }
                    minePosts = merged
                    minePostsPage = nextPage
                    minePostsHasMore = page.items.isNotEmpty()
                    minePostsError = null
                    mineCacheStore.writePosts(
                        MinePostsCache(
                            items = merged,
                            page = nextPage,
                            hasMore = page.items.isNotEmpty(),
                        )
                    )
                }
                .onFailure { error ->
                    minePostsError = error.message ?: "\u65E0\u6CD5\u7EE7\u7EED\u8BFB\u53D6\u4E2A\u4EBA\u4E3B\u9875\u5FAE\u535A"
                }
            minePostsLoadingMore = false
        }
    }

    fun loadMoreMineAlbum() {
        val uid = mineProfile?.id?.takeIf { it.isNotBlank() } ?: return
        if (mineAlbumLoadingMore || mineProfileLoading || !mineAlbumHasMore) return
        scope.launch {
            mineAlbumLoadingMore = true
            val cursor = mineAlbumNextCursor
            runCatching { session.loadUserAlbumImages(uid, cursor) }
                .onSuccess { page ->
                    mineAlbumImages = (mineAlbumImages + page.images).distinctBy { it.largeUrl }
                    mineAlbumNextCursor = page.nextCursor
                    mineAlbumHasMore = page.nextCursor != null
                    mineCacheStore.writeAlbum(
                        AlbumPage(
                            images = mineAlbumImages,
                            nextCursor = mineAlbumNextCursor,
                        )
                    )
                }
            mineAlbumLoadingMore = false
        }
    }

    fun openDetail(item: FeedItem) {
        selectedItem = item
        comments = emptyList()
        commentsCursor = null
        commentsHasMore = true
        scope.launch {
            commentsLoading = true
            runCatching { session.loadComments(item) }
                .onSuccess { page ->
                    comments = page.items
                    commentsCursor = page.nextCursor
                    commentsHasMore = page.nextCursor != null
                }
                .onFailure { error -> showMessage("\u8BC4\u8BBA\u52A0\u8F7D\u5931\u8D25", error.message ?: "\u5FAE\u535A\u63A5\u53E3\u65E0\u54CD\u5E94") }
            commentsLoading = false
        }
    }

    fun loadMoreComments() {
        val item = selectedItem ?: return
        val cursor = commentsCursor ?: return
        if (commentsLoading || !commentsHasMore) return
        scope.launch {
            commentsLoading = true
            runCatching { session.loadMoreComments(item, cursor) }
                .onSuccess { page ->
                    comments = comments + page.items
                    commentsCursor = page.nextCursor
                    commentsHasMore = page.nextCursor != null
                }
            commentsLoading = false
        }
    }

    fun syncEmoticons() {
        if (emoticonSyncing) return
        scope.launch {
            emoticonSyncing = true
            runCatching { session.loadEmotions() }
                .onSuccess { emotions ->
                    emoticonMap = emotions.associate { it.phrase to it.url }
                    showMessage("\u8868\u60C5\u540C\u6B65\u5B8C\u6210", "\u5DF2\u540C\u6B65 ${emoticonMap.size} \u4E2A\u8868\u60C5")
                }
                .onFailure { error ->
                    showMessage("表情同步失败", error.message ?: "无法读取微博表情配置")
                }
            emoticonSyncing = false
        }
    }

    LaunchedEffect(Unit) {
        timelineCacheStore.readFollowingTimeline()?.let { page ->
            items = page.items
            nextCursor = page.nextCursor
        }
        mineCacheStore.readProfile()?.let { profile ->
            mineProfile = profile
            mineProfileError = null
        }
        mineCacheStore.readPosts()?.let { cache ->
            minePosts = cache.items
            minePostsPage = cache.page
            minePostsHasMore = cache.hasMore
            minePostsError = null
        }
        mineCacheStore.readAlbum()?.let { page ->
            mineAlbumImages = filterOutRetweetedOnlyImages(page.images, minePosts)
            mineAlbumNextCursor = page.nextCursor
            mineAlbumHasMore = page.nextCursor != null
        }
        hasLoginCookie = session.hasLoginCookie()
        mineHasLoginCookie = hasLoginCookie
        cacheLoaded = true
        if (items.isEmpty() && hasLoginCookie) {
            refreshTimeline()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            session.webView.destroy()
        }
    }

    CompositionLocalProvider(LocalVideoPlaybackCoordinator provides videoPlaybackCoordinator) {
    MyWeiboScaffold(
        selectedTab = selectedTab,
        selectedItem = selectedItem,
        isLoading = isLoading,
        snackbarHostState = snackbarHostState,
        onTabChange = { tab ->
            if (tab == MainTab.Feed && selectedTab == MainTab.Feed && selectedItem == null) {
                refreshTimelineFromTop()
            } else {
                if (tab == MainTab.Feed) {
                    hasLoginCookie = session.hasLoginCookie()
                }
                selectedTab = tab
            }
        },
        onBack = { selectedItem = null },
        onRefresh = { refreshTimeline() },
    ) { innerPadding ->
        Box(Modifier.fillMaxSize().padding(innerPadding)) {
            when {
                visitedUserId != null -> {
                    LaunchedEffect(visitedUserId) {
                        // already loaded in loadVisitedUserProfile
                    }
                    MineScreen(
                        session = session,
                        profile = visitedProfile,
                        isLoading = visitedProfileLoading,
                        loadError = null,
                        hasLoginCookie = false,
                        posts = visitedPosts,
                        postsError = null,
                        postsLoadingMore = visitedPostsLoadingMore,
                        albumImages = visitedAlbumImages,
                        albumLoadingMore = false,
                        postsHasMore = visitedPostsHasMore,
                        albumHasMore = false,
                        emoticonMap = emoticonMap,
                        emoticonCount = emoticonMap.size,
                        emoticonSyncing = emoticonSyncing,
                        onRefresh = {
                            visitedUserId?.let { loadVisitedUserProfile(ProfileLookup.Uid(it)) }
                        },
                        onLoadMorePosts = { loadMoreVisitedPosts() },
                        onLoadMoreAlbum = {},
                        onSyncEmoticons = { syncEmoticons() },
                        onItemClick = ::openDetail,
                        onMediaClick = { mediaPreview = it },
                        onUserClick = ::openUser,
                    )
                }

                selectedItem != null -> DetailScreen(
                    item = selectedItem!!,
                    comments = comments,
                    isLoadingComments = commentsLoading,
                    onBack = { selectedItem = null },
                    onRefresh = {
                        scope.launch {
                            commentsLoading = true
                            commentsCursor = null
                            runCatching { session.loadComments(selectedItem!!) }
                                .onSuccess { page ->
                                    comments = page.items
                                    commentsCursor = page.nextCursor
                                }
                            commentsLoading = false
                        }
                    },
                    onLoadMoreComments = { loadMoreComments() },
                    commentsHasMore = commentsHasMore,
                    onMediaClick = { mediaPreview = it },
                    emoticonMap = emoticonMap,
                    onRetweetClick = ::openDetail,
                    onUserClick = ::openUser,
                )

                selectedTab == MainTab.Search -> PlaceholderScreen(
                    title = "\u641C\u7D22",
                    body = "\u641C\u7D22\u9875\u5C06\u63A5\u5165\u5FAE\u535A\u641C\u7D22\u63A5\u53E3\uFF0C\u7528\u4E8E\u67E5\u627E\u7528\u6237\u3001\u8BDD\u9898\u548C\u5FAE\u535A\u5185\u5BB9\u3002"
                )

                selectedTab == MainTab.Messages -> PlaceholderScreen(
                    title = "\u6D88\u606F",
                    body = "\u6D88\u606F\u9875\u5C06\u627F\u8F7D\u8BC4\u8BBA\u3001\u70B9\u8D5E\u3001\u63D0\u53CA\u548C\u79C1\u4FE1\u5165\u53E3\u3002"
                )

                selectedTab == MainTab.Mine -> {
                    LaunchedEffect(mineProfile) {
                        if (mineProfile == null && !mineProfileLoading) {
                            refreshMineProfile()
                        }
                    }
                    MineScreen(
                        session = session,
                        profile = mineProfile,
                        isLoading = mineProfileLoading,
                        loadError = mineProfileError,
                        hasLoginCookie = mineHasLoginCookie,
                        posts = minePosts,
                        postsError = minePostsError,
                        postsLoadingMore = minePostsLoadingMore,
                        albumImages = mineAlbumImages,
                        albumLoadingMore = mineAlbumLoadingMore,
                        postsHasMore = minePostsHasMore,
                        albumHasMore = mineAlbumHasMore,
                        emoticonMap = emoticonMap,
                        emoticonCount = emoticonMap.size,
                        emoticonSyncing = emoticonSyncing,
                        postsListState = minePostsListState,
                        onRefresh = { refreshMineProfile() },
                        onLoadMorePosts = { loadMoreMinePosts() },
                        onLoadMoreAlbum = { loadMoreMineAlbum() },
                        onSyncEmoticons = { syncEmoticons() },
                        onItemClick = ::openDetail,
                        onMediaClick = { mediaPreview = it },
                        onUserClick = ::openUser,
                    )
                }

                selectedTab == MainTab.Compose -> PlaceholderScreen(
                    title = "\u5199\u5FAE\u535A",
                    body = "\u8FD9\u91CC\u540E\u7EED\u4F1A\u63A5\u5165\u5FAE\u535A\u53D1\u5E03\u63A5\u53E3\uFF0C\u652F\u6301\u539F\u751F\u7F16\u8F91\u548C\u53D1\u5E03\u3002"
                )

                else -> FollowFeedScreen(
                    session = session,
                    listState = feedListState,
                    items = items,
                    isLoading = isLoading,
                    cacheLoaded = cacheLoaded,
                    hasLoginCookie = hasLoginCookie,
                    emoticonMap = emoticonMap,
                    onRefresh = { refreshTimeline() },
                    onLoadMore = { loadMore() },
                    onOpenLoginSettings = { selectedTab = MainTab.Mine },
                    onUserClick = ::openUser,
                    onItemClick = ::openDetail,
                    onMediaClick = { mediaPreview = it },
                )
            }

            if (selectedTab != MainTab.Mine && selectedItem == null) {
                HiddenSessionWebView(session)
            }

            if (selectedItem == null) {
                FloatingBottomBar(
                    selectedTab = selectedTab,
                    onTabChange = { tab ->
                        if (tab == MainTab.Feed && selectedTab == MainTab.Feed) {
                            refreshTimelineFromTop()
                        } else {
                            if (tab == MainTab.Feed) {
                                hasLoginCookie = session.hasLoginCookie()
                            }
                            selectedTab = tab
                        }
                    },
                    modifier = Modifier.align(Alignment.BottomCenter),
                )
            }

            mediaPreview?.let { media ->
                FullscreenMediaPreview(
                    media = media,
                    onDismiss = { mediaPreview = null },
                )
            }
        }
    }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MyWeiboScaffold(
    selectedTab: MainTab,
    selectedItem: FeedItem?,
    isLoading: Boolean,
    snackbarHostState: SnackbarHostState,
    onTabChange: (MainTab) -> Unit,
    onBack: () -> Unit,
    onRefresh: () -> Unit,
    content: @Composable (PaddingValues) -> Unit,
) {
    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            if (false) {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                ),
                title = {
                    Text(
                        text = selectedItem?.authorName ?: "MyWeibo",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                navigationIcon = {
                    if (selectedItem != null) {
                        TextButton(onClick = onBack) { Text("返回") }
                    }
                },
                actions = {
                    if (false) {
                        TextButton(onClick = onRefresh, enabled = !isLoading) {
                            Text(if (isLoading) "\u540C\u6B65\u4E2D" else "\u540C\u6B65")
                        }
                    }
                }
            )
            }
        },
        bottomBar = {
            if (false && selectedItem == null) {
                FloatingBottomBar(
                    selectedTab = selectedTab,
                    onTabChange = onTabChange,
                )
            }
        },
        content = content,
    )
}

@Composable
private fun FloatingBottomBar(
    selectedTab: MainTab,
    onTabChange: (MainTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    val glassShape = RoundedCornerShape(36.dp)
    val surface = MaterialTheme.colorScheme.surface
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    var liquidActive by remember { mutableStateOf(false) }
    var gestureActive by remember { mutableStateOf(false) }
    var gesturePillOffset by remember { mutableStateOf(0.dp) }
    var highlightedTab by remember { mutableStateOf<MainTab?>(null) }

    LaunchedEffect(selectedTab) {
        liquidActive = true
        delay(260)
        liquidActive = false
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.Transparent,
            shadowElevation = 0.dp,
            shape = glassShape,
            border = BorderStroke(
                width = 0.5.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFFE6E6E6).copy(alpha = 0.70f),
                        Color(0xFFD8D8D8).copy(alpha = 0.42f),
                        Color(0xFFF2F2F2).copy(alpha = 0.58f),
                    )
                )
            ),
        ) {
            Box(
                modifier = Modifier
                    .height(72.dp)
                    .clip(glassShape),
            ) {
                // 搴曞眰锛氭瘺鐜荤拑鑳屾櫙
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .blur(28.dp)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Transparent,
                                    Color.Transparent,
                                ),
                                start = Offset(0f, 0f),
                                end = Offset(0f, Float.POSITIVE_INFINITY),
                            )
                        )
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Transparent,
                                    Color.Transparent,
                                )
                            )
                        )
                )

                // Upper layer: clear content over the glass background.
                BoxWithConstraints(
                    Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp)
                ) {
                    val tabs = MainTab.entries
                    val selectedIndex = tabs.indexOf(selectedTab).coerceAtLeast(0)
                    val itemWidth = maxWidth / tabs.size
                    val pillWidth = itemWidth + 2.dp
                    val maxWidthPx = with(density) { maxWidth.toPx() }.coerceAtLeast(1f)
                    val pillWidthPx = with(density) { pillWidth.toPx() }
                    val animatedPillOffset by animateDpAsState(
                        targetValue = if (gestureActive) {
                            gesturePillOffset
                        } else {
                            itemWidth * selectedIndex - 1.dp
                        },
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioNoBouncy,
                            stiffness = Spring.StiffnessLow,
                        ),
                        label = "selected-nav-pill",
                    )
                    val animatedPillWidth by animateDpAsState(
                        targetValue = if (gestureActive) pillWidth + 12.dp else pillWidth,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioNoBouncy,
                            stiffness = Spring.StiffnessMedium,
                        ),
                        label = "selected-nav-pill-width",
                    )
                    val animatedPillHeight by animateDpAsState(
                        targetValue = if (gestureActive) 62.dp else 58.dp,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioNoBouncy,
                            stiffness = Spring.StiffnessMedium,
                        ),
                        label = "selected-nav-pill-height",
                    )
                    val pillOffset = animatedPillOffset - (animatedPillWidth - pillWidth) / 2

                    fun indexForX(x: Float): Int {
                        val safeX = x.coerceIn(0f, maxWidthPx - 0.01f)
                        return ((safeX / maxWidthPx) * tabs.size).toInt().coerceIn(0, tabs.lastIndex)
                    }

                    fun pillOffsetForX(x: Float) = with(density) {
                        val minOffset = -1.dp.toPx()
                        val maxOffset = maxWidthPx - pillWidthPx + 1.dp.toPx()
                        (x - pillWidthPx / 2f).coerceIn(minOffset, maxOffset).toDp()
                    }

                    fun pillOffsetForIndex(index: Int) = itemWidth * index - 1.dp

                    Canvas(Modifier.fillMaxSize()) {
                        drawRoundRect(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Transparent,
                                    Color.Transparent,
                                ),
                                center = Offset(size.width * 0.5f, size.height * 0.1f),
                                radius = size.width * 0.55f,
                            ),
                            cornerRadius = CornerRadius(36.dp.toPx(), 36.dp.toPx()),
                        )
                    }

                    LiquidSelectedPill(
                        active = liquidActive || gestureActive,
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .offset(x = pillOffset)
                            .width(animatedPillWidth)
                            .height(animatedPillHeight),
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(maxWidthPx, pillWidthPx) {
                                awaitEachGesture {
                                    val down = awaitFirstDown(requireUnconsumed = false)
                                    var targetIndex = indexForX(down.position.x)
                                    gestureActive = true
                                    highlightedTab = tabs[targetIndex]
                                    gesturePillOffset = pillOffsetForX(down.position.x)
                                    down.consume()

                                    while (true) {
                                        val event = awaitPointerEvent()
                                        val change = event.changes.firstOrNull { it.id == down.id }
                                            ?: event.changes.firstOrNull()
                                            ?: break
                                        targetIndex = indexForX(change.position.x)
                                        highlightedTab = tabs[targetIndex]
                                        gesturePillOffset = pillOffsetForX(change.position.x)
                                        change.consume()
                                        if (!change.pressed) {
                                            break
                                        }
                                    }

                                    highlightedTab = null
                                    gesturePillOffset = pillOffsetForIndex(targetIndex)
                                    liquidActive = true
                                    onTabChange(tabs[targetIndex])
                                    scope.launch {
                                        delay(120)
                                        gestureActive = false
                                    }
                                    scope.launch {
                                        delay(260)
                                        liquidActive = false
                                    }
                                }
                            },
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        tabs.forEach { tab ->
                            val isTouchingSelectedTab = gestureActive && highlightedTab == selectedTab
                            FloatingNavItem(
                                tab = tab,
                                selected = selectedTab == tab && (!gestureActive || highlightedTab == null || isTouchingSelectedTab),
                                highlighted = gestureActive && highlightedTab != selectedTab && highlightedTab == tab,
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LiquidSelectedPill(
    active: Boolean,
    modifier: Modifier = Modifier,
) {
    val surface = MaterialTheme.colorScheme.surface
    val neutralGlow = MaterialTheme.colorScheme.outlineVariant
    val shape = RoundedCornerShape(26.dp)

    Surface(
        modifier = modifier,
        color = Color.Transparent,
        shape = shape,
        shadowElevation = 0.dp,
        border = BorderStroke(
            1.dp,
            Brush.linearGradient(
                colors = if (active) {
                    listOf(
                        Color.White.copy(alpha = 0.30f),
                        neutralGlow.copy(alpha = 0.18f),
                        Color(0xFFB8B8B8).copy(alpha = 0.16f),
                    )
                } else {
                    listOf(
                        Color.White.copy(alpha = 0.10f),
                        Color(0xFFBDBDBD).copy(alpha = 0.18f),
                        Color(0xFF9E9E9E).copy(alpha = 0.10f),
                    )
                }
            )
        ),
    ) {
        Box(
            Modifier
                .clip(shape)
                .blur(8.dp)
                .background(
                    Brush.linearGradient(
                        colors = if (active) {
                            listOf(
                                Color(0xFFE8E8E8).copy(alpha = 0.34f),
                                Color.White.copy(alpha = 0.16f),
                                Color(0xFFBDBDBD).copy(alpha = 0.16f),
                            )
                        } else {
                            listOf(
                                Color(0xFFCFCFCF).copy(alpha = 0.16f),
                                Color(0xFFE4E4E4).copy(alpha = 0.24f),
                                Color(0xFFB8B8B8).copy(alpha = 0.12f),
                            )
                        },
                        start = Offset(0f, 0f),
                        end = Offset(260f, 90f),
                    )
                )
        ) {
            Canvas(Modifier.fillMaxSize()) {
                if (active) {
                    drawRoundRect(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.30f),
                                Color.White.copy(alpha = 0.08f),
                                Color.Transparent,
                            ),
                            center = Offset(size.width * 0.5f, size.height * 0.18f),
                            radius = size.width * 0.88f,
                        ),
                        cornerRadius = CornerRadius(26.dp.toPx(), 26.dp.toPx()),
                    )
                }
            }
        }
    }
}

@Composable
private fun FloatingNavItem(
    tab: MainTab,
    selected: Boolean,
    highlighted: Boolean,
    modifier: Modifier = Modifier,
) {
    val primary = MaterialTheme.colorScheme.primary
    val hotPink = Color(0xFFFF4F9A)
    val iconColor = when {
        highlighted -> hotPink
        selected -> primary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Column(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(28.dp))
            .padding(top = 8.dp, bottom = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier.size(32.dp),
            contentAlignment = Alignment.Center,
        ) {
            TabIcon(tab = tab, color = iconColor)
        }
        Text(
            text = tab.label,
            fontSize = 10.sp,
            color = iconColor,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun TabIcon(tab: MainTab, color: Color) {
    Icon(
        painter = painterResource(
            when (tab) {
                MainTab.Feed -> R.drawable.ic_tab_home
                MainTab.Search -> R.drawable.ic_tab_search
                MainTab.Messages -> R.drawable.ic_tab_messages
                MainTab.Mine -> R.drawable.ic_tab_mine
                MainTab.Compose -> R.drawable.ic_tab_compose
            }
        ),
        contentDescription = tab.label,
        modifier = Modifier.size(20.dp),
        tint = color,
    )
}


@Composable
private fun FollowFeedScreen(
    session: WeiboWebSession,
    listState: LazyListState,
    items: List<FeedItem>,
    isLoading: Boolean,
    cacheLoaded: Boolean,
    hasLoginCookie: Boolean,
    emoticonMap: Map<String, String> = emptyMap(),
    onUserClick: ((String) -> Unit)? = null,
    onRefresh: () -> Unit,
    onLoadMore: () -> Unit,
    onOpenLoginSettings: () -> Unit,
    onItemClick: (FeedItem) -> Unit,
    onMediaClick: (FeedMedia) -> Unit,
) {
    val topInset = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    PullToRefreshBox(
        isRefreshing = isLoading,
        onRefresh = onRefresh,
        modifier = Modifier.fillMaxSize(),
    ) {
        val shouldLoadMore by remember {
            derivedStateOf {
                val layoutInfo = listState.layoutInfo
                val totalItems = layoutInfo.totalItemsCount
                val lastVisibleItem = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                totalItems > 0 && lastVisibleItem >= totalItems - 3
            }
        }

        LaunchedEffect(shouldLoadMore) {
            snapshotFlow { shouldLoadMore }
                .distinctUntilChanged()
                .filter { it }
                .collect { onLoadMore() }
        }

        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = topInset + 12.dp, bottom = 24.dp),
        ) {
            if (!cacheLoaded) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator()
                    }
                }
            } else if (items.isEmpty() && !isLoading) {
                item {
                    EmptyState(
                        title = if (hasLoginCookie) "\u6682\u65E0\u672C\u5730\u7F13\u5B58" else "\u9700\u8981\u767B\u5F55\u5FAE\u535A",
                        body = if (hasLoginCookie) {
                            "\u5DF2\u68C0\u6D4B\u5230\u5FAE\u535A\u767B\u5F55\u6001\u3002\u4E0B\u62C9\u6216\u70B9\u51FB\u5E95\u90E8\u9996\u9875\u6309\u94AE\uFF0C\u5373\u53EF\u540C\u6B65\u5173\u6CE8\u4FE1\u606F\u6D41\u3002"
                        } else {
                            "\u672C\u5730\u8FD8\u6CA1\u6709\u7F13\u5B58\u7684\u4FE1\u606F\u6D41\u3002\u8BF7\u5230\u6211\u7684\u9875\u9762\u7684\u8BBE\u7F6E\u4E2D\u767B\u5F55\u5FAE\u535A\u3002"
                        },
                        actionLabel = if (hasLoginCookie) "\u7ACB\u5373\u540C\u6B65" else "\u6253\u5F00\u5FAE\u535A\u767B\u5F55\u9875",
                        onAction = {
                            if (hasLoginCookie) {
                                onRefresh()
                            } else {
                                onOpenLoginSettings()
                            }
                        },
                    )
                }
            }

            items(items, key = { it.id }) { item ->
                FeedCard(
                    item = item,
                    onClick = { onItemClick(item) },
                    onMediaClick = onMediaClick,
                    emoticonMap = emoticonMap,
                    onUserClick = onUserClick,
                    onRetweetClick = onItemClick,
                )
            }

            if (isLoading && items.isNotEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(20.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }
}

@Composable
private fun FeedScreen(
    session: WeiboWebSession,
    kind: TimelineKind,
    items: List<FeedItem>,
    isLoading: Boolean,
    message: NativeUiMessage?,
    onKindChange: (TimelineKind) -> Unit,
    onRefresh: () -> Unit,
    onLoadMore: () -> Unit,
    onItemClick: (FeedItem) -> Unit,
    onMediaClick: (FeedMedia) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp),
    ) {
        item {
            FeedHeader(
                kind = kind,
                isLoading = isLoading,
                message = message,
                onKindChange = onKindChange,
                onRefresh = onRefresh,
            )
        }

        if (items.isEmpty() && !isLoading) {
            item {
                EmptyState(
                    title = "等待微博数据",
                    body = "\u5148\u5230\u6211\u7684\u9875\u9762\u8BBE\u7F6E\u4E2D\u5B8C\u6210\u767B\u5F55\uFF0C\u518D\u56DE\u5230\u9996\u9875\u5237\u65B0\u3002\u6570\u636E\u6E90\u4F7F\u7528 weibo.com/ajax/*\uFF0C\u548C example \u7684\u6D4F\u89C8\u5668\u6269\u5C55\u8DEF\u7EBF\u4E00\u81F4\u3002",
                    actionLabel = "\u6253\u5F00\u5FAE\u535A\u767B\u5F55\u9875",
                    onAction = { session.openLogin() },
                )
            }
        }

        items(items, key = { it.id }) { item ->
            FeedCard(
                item = item,
                onClick = { onItemClick(item) },
                onMediaClick = onMediaClick,
                emoticonMap = mapOf(),
            )
        }

        item {
            Box(
                modifier = Modifier.fillMaxWidth().padding(20.dp),
                contentAlignment = Alignment.Center,
            ) {
                if (isLoading) {
                    CircularProgressIndicator()
                } else if (items.isNotEmpty()) {
                    Button(onClick = onLoadMore) { Text("加载更多") }
                }
            }
        }
    }
}

@Composable
private fun FeedHeader(
    kind: TimelineKind,
    isLoading: Boolean,
    message: NativeUiMessage?,
    onKindChange: (TimelineKind) -> Unit,
    onRefresh: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "重新排版微博",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = "\u4F7F\u7528\u5FAE\u535A\u7F51\u9875\u767B\u5F55\u6001\u8BFB\u53D6\u63A5\u53E3\uFF0C\u539F\u751F Material 3 \u5448\u73B0\u4FE1\u606F\u6D41\u3001\u5A92\u4F53\u548C\u8BC4\u8BBA\u3002",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TimelineKind.entries.forEach { option ->
                FilterChip(
                    selected = kind == option,
                    onClick = { onKindChange(option) },
                    label = { Text(option.label) },
                )
            }
        }
        if (message != null) {
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                shape = RoundedCornerShape(8.dp),
            ) {
                Column(Modifier.padding(12.dp)) {
                    Text(message.title, fontWeight = FontWeight.SemiBold)
                    Text(message.detail, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        if (!isLoading) {
            TextButton(onClick = onRefresh) { Text("\u4ECE\u5FAE\u535A\u540C\u6B65\u6700\u65B0\u5185\u5BB9") }
        }
    }
}

@Composable
private fun EmoticonText(text: String, emoticonMap: Map<String, String>, style: TextStyle, onUserClick: ((String) -> Unit)? = null) {
    if (text.isBlank()) {
        Text(text = "\u65E0\u6B63\u6587", style = style)
        return
    }

    val primaryColor = MaterialTheme.colorScheme.primary
    val inlineContent = mutableMapOf<String, InlineTextContent>()
    val emojiSize = style.fontSize.times(1.4f)

    // 娣诲姞琛ㄦ儏 inline content
    emoticonMap.forEach { (phrase, url) ->
        inlineContent[phrase] = InlineTextContent(
            Placeholder(width = emojiSize, height = emojiSize, placeholderVerticalAlign = PlaceholderVerticalAlign.TextCenter)
        ) { EmojiImage(url = url) }
    }

    // Add clickable @username inline content.
    val mentionPattern = Regex("""@[\w\u4e00-\u9fa5]+""")
    val lineH = style.lineHeight.takeIf { it != TextUnit.Unspecified } ?: style.fontSize * 1.5f
    Regex("""@[\p{L}\p{N}_-]+""").findAll(text).forEach { match ->
        val token = match.value
        val screenName = token.removePrefix("@")
        val estWidth = style.fontSize * (tokenWidthEm(token) + 0.16f)
        inlineContent[token] = InlineTextContent(
            Placeholder(width = estWidth, height = lineH, placeholderVerticalAlign = PlaceholderVerticalAlign.TextCenter)
        ) {
            Text(
                text = token,
                color = primaryColor,
                fontWeight = FontWeight.Medium,
                style = style,
                softWrap = false,
                maxLines = 1,
                modifier = Modifier.clickable { onUserClick?.invoke(screenName) },
            )
        }
    }

    val annotatedString = buildAnnotatedString {
        val tokenPattern = Regex("""\[[^\[\]]+\]|@[\w\u4e00-\u9fa5]+""")
        var last = 0
        Regex("""\[[^\[\]]+\]|@[\p{L}\p{N}_-]+""").findAll(text).forEach { match ->
            if (match.range.first > last) {
                append(text.substring(last, match.range.first))
            }
            val token = match.value
            if (emoticonMap.containsKey(token) || token.startsWith("@")) {
                // Check whether this token has inline content.
                if (inlineContent.containsKey(token)) {
                    appendInlineContent(token, token)
                } else {
                    append(token)
                }
            } else {
                append(token)
            }
            last = match.range.last + 1
        }
        if (last < text.length) {
            append(text.substring(last))
        }
    }

    Text(
        text = annotatedString,
        inlineContent = inlineContent,
        style = style,
    )
}

private fun tokenWidthEm(token: String): Float =
    token.sumOf { char ->
        when {
            char == '@' -> 0.78
            char == '_' || char == '-' -> 0.46
            char.isWhitespace() -> 0.28
            char.code <= 0x007F -> 0.62
            else -> 1.0
        }
    }.toFloat()

@Composable
private fun EmojiImage(url: String) {
    var bitmap by remember(url) { mutableStateOf<android.graphics.Bitmap?>(null) }
    LaunchedEffect(url) {
        runCatching {
            withContext(Dispatchers.IO) {
                val bytes = URL(url).openConnection().apply {
                    (this as HttpURLConnection).connectTimeout = 5000
                    readTimeout = 5000
                }.inputStream.use { it.readBytes() }
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            }
        }.onSuccess { bitmap = it }
    }
    val image = bitmap
    if (image != null) {
        Image(
            bitmap = image.asImageBitmap(),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit,
        )
    }
}

@Composable
private fun FeedCard(
    item: FeedItem,
    onClick: () -> Unit,
    onMediaClick: (FeedMedia) -> Unit,
    emoticonMap: Map<String, String> = emptyMap(),
    onUserClick: ((String) -> Unit)? = null,
    onRetweetClick: ((FeedItem) -> Unit)? = null,
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
    ) {
        val resolvedEmoticonMap = item.emoticons.ifEmpty { emoticonMap }
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            AuthorRow(item, onUserClick = onUserClick)
            EmoticonText(
                text = item.text,
                emoticonMap = resolvedEmoticonMap,
                style = MaterialTheme.typography.bodyMedium,
                onUserClick = onUserClick,
            )
            MediaStrip(
                images = item.images,
                media = item.media,
                onMediaClick = onMediaClick,
            )
            item.retweetedStatus?.let {
                QuotedStatus(item = it, onMediaClick = onMediaClick, emoticonMap = resolvedEmoticonMap, onClick = onRetweetClick?.let { cb -> { cb(it) } }, onUserClick = onUserClick)
            }
            StatusActions(item, onCommentClick = onClick)
        }
    }
}

@Composable
private fun QuotedStatus(item: FeedItem, onMediaClick: (FeedMedia) -> Unit, emoticonMap: Map<String, String> = emptyMap(), onClick: (() -> Unit)? = null, onUserClick: ((String) -> Unit)? = null) {
    val resolvedMap = item.emoticons.ifEmpty { emoticonMap }
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerHighest,
        shape = RoundedCornerShape(8.dp),
        modifier = if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier,
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "@${item.authorName}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            EmoticonText(
                text = item.text,
                emoticonMap = resolvedMap,
                style = MaterialTheme.typography.bodyMedium,
                onUserClick = onUserClick,
            )
            MediaStrip(images = item.images, media = item.media, onMediaClick = onMediaClick)
        }
    }
}

@Composable
private fun AuthorRow(item: FeedItem, onUserClick: ((String) -> Unit)? = null) {
    val metadataText = listOfNotNull(
        formatWeiboTime(item.createdAt),
        item.source?.takeIf { it.isNotBlank() }?.let { "\u6765\u81EA $it" },
        item.ipLocation,
    ).joinToString(" ")

    val uid = item.authorId
    Row(verticalAlignment = Alignment.CenterVertically) {
        RemoteImage(
            url = item.authorAvatarUrl,
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .clickable(enabled = uid.isNotBlank()) { onUserClick?.invoke(uid) },
            contentScale = ContentScale.Crop,
        )
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = item.authorName,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.clickable(enabled = uid.isNotBlank()) { onUserClick?.invoke(uid) },
            )
            if (metadataText.isNotBlank()) {
                Text(
                    text = metadataText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.58f),
                )
            }
            if (false) {
            Text(
                text = listOfNotNull(formatWeiboTime(item.createdAt), item.source).joinToString(" \u00B7 "),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            }
            if (false) item.ipLocation?.let { location ->
                Text(
                    text = "IP\u5C5E\u5730 $location",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun MediaStrip(
    images: List<FeedImage>,
    media: FeedMedia?,
    onMediaClick: (FeedMedia) -> Unit,
) {
    if (images.isEmpty() && media == null) return

    var viewerOpen by remember { mutableStateOf(false) }
    var viewerIndex by remember { mutableStateOf(0) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (images.isNotEmpty()) {
            val gridColumns = when (images.size) {
                1 -> 1
                2 -> 2
                3 -> 3
                4 -> 2
                else -> 3
            }
            val rows = images.chunked(gridColumns)

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                rows.forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        row.forEach { image ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(
                                        if (images.size == 1) {
                                            val w = image.width ?: 0
                                            val h = image.height ?: 0
                                            if (w > 0 && h > 0) (w.toFloat() / h).coerceIn(0.33f, 3f) else 1f
                                        } else 1f
                                    )
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                                    .clickable {
                                        viewerIndex = images.indexOf(image)
                                        viewerOpen = true
                                    },
                            ) {
                                RemoteImage(
                                    url = image.largeUrl,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = if (images.size == 1) ContentScale.FillWidth else ContentScale.Crop,
                                )
                                if (image.isLivePhoto || image.isGif) {
                                    Surface(
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd)
                                            .padding(5.dp),
                                        shape = CircleShape,
                                        color = Color.Black.copy(alpha = 0.42f),
                                        contentColor = Color.White,
                                    ) {
                                        Icon(
                                            painter = painterResource(R.drawable.ic_live_photo),
                                            contentDescription = if (image.isGif) "GIF" else "LivePhoto",
                                            modifier = Modifier
                                                .padding(3.dp)
                                                .size(15.dp),
                                            tint = Color.White,
                                        )
                                    }
                                }
                            }
                        }
                        // Fill remaining columns with empty space
                        repeat(gridColumns - row.size) {
                            Spacer(Modifier.weight(1f))
                        }
                    }
                }
            }

            if (viewerOpen) {
                FullscreenImageViewer(
                    images = images,
                    initialIndex = viewerIndex,
                    onDismiss = { viewerOpen = false },
                )
            }
        }

        if (media != null) {
            InlineVideoPlayer(media = media, onClick = { onMediaClick(media) })
        }
    }
}

@Composable
private fun FullscreenImageViewer(
    images: List<FeedImage>,
    initialIndex: Int,
    onDismiss: () -> Unit,
) {
    val pagerState = rememberPagerState(pageCount = { images.size }, initialPage = initialIndex)
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false),
    ) {
        BackHandler { onDismiss() }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
        ) {
            HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
                ZoomableFullscreenImage(
                    image = images[page],
                    onTap = onDismiss,
                )
            }
            Text(
                text = "${pagerState.currentPage + 1} / ${images.size}",
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 48.dp),
                color = Color.White,
                style = MaterialTheme.typography.labelLarge,
            )
        }
    }
}

@Composable
private fun ZoomableFullscreenImage(image: FeedImage, onTap: () -> Unit) {
    var targetScale by remember(image.largeUrl) { mutableStateOf(1f) }
    val scale by animateFloatAsState(
        targetValue = targetScale,
        animationSpec = tween(durationMillis = 180),
        label = "fullscreen-image-scale",
    )
    var offsetX by remember(image.largeUrl) { mutableStateOf(0f) }
    var offsetY by remember(image.largeUrl) { mutableStateOf(0f) }
    var bitmap by remember(image.largeUrl) { mutableStateOf<android.graphics.Bitmap?>(null) }
    var livePlaying by remember(image.largeUrl) { mutableStateOf(image.isLivePhoto || image.isGif) }

    LaunchedEffect(image.largeUrl) {
        bitmap = withContext(Dispatchers.IO) { loadFullscreenBitmap(image) }
    }

    val loadedBitmap = bitmap
    if (loadedBitmap == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Color.White)
        }
        return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(image.largeUrl) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    var lastCentroid: Offset? = null
                    var lastDistance = 0f
                    while (true) {
                        val event = awaitPointerEvent()
                        val pressed = event.changes.filter { it.pressed }
                        if (pressed.isEmpty()) break

                        if (pressed.size >= 2) {
                            val first = pressed[0].position
                            val second = pressed[1].position
                            val centroid = Offset((first.x + second.x) / 2f, (first.y + second.y) / 2f)
                            val distance = hypot(first.x - second.x, first.y - second.y)
                            if (lastDistance > 0f) {
                                val zoom = (distance / lastDistance).coerceIn(0.72f, 1.38f)
                                targetScale = (targetScale * zoom).coerceIn(1f, 5f)
                                val previousCentroid = lastCentroid ?: centroid
                                val pan = centroid - previousCentroid
                                offsetX = if (targetScale > 1f) offsetX + pan.x else 0f
                                offsetY = if (targetScale > 1f) offsetY + pan.y else 0f
                            }
                            lastCentroid = centroid
                            lastDistance = distance
                            event.changes.forEach { it.consume() }
                        } else {
                            lastCentroid = null
                            lastDistance = 0f
                            val change = pressed.first()
                            val totalDrag = change.position - down.position
                            val delta = change.position - change.previousPosition
                            if (targetScale > 1.01f) {
                                offsetX += delta.x
                                offsetY += delta.y
                                change.consume()
                            } else if (totalDrag.y > 72f && abs(totalDrag.y) > abs(totalDrag.x) * 1.35f) {
                                change.consume()
                                onTap()
                                break
                            }
                        }
                    }
                }
            }
            .pointerInput(image.largeUrl) {
                detectTapGestures(
                    onTap = { onTap() },
                    onDoubleTap = {
                        if (targetScale > 1f) {
                            targetScale = 1f
                            offsetX = 0f
                            offsetY = 0f
                        } else {
                            targetScale = 2.6f
                        }
                    },
                    onLongPress = {
                        if (image.isLivePhoto || image.isGif) livePlaying = true
                    },
                )
            },
        contentAlignment = Alignment.Center,
    ) {
        Image(
            bitmap = loadedBitmap.asImageBitmap(),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    translationX = offsetX
                    translationY = offsetY
                },
            contentScale = ContentScale.Fit,
        )
        if ((image.isLivePhoto || image.isGif) && livePlaying) {
            LivePhotoOverlay(
                image = image,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        translationX = offsetX
                        translationY = offsetY
                    },
                onEnded = { livePlaying = false },
            )
        }
    }
}

@Composable
private fun LivePhotoOverlay(
    image: FeedImage,
    modifier: Modifier = Modifier,
    onEnded: () -> Unit,
) {
    val context = LocalContext.current
    val videoUrl = image.livePhotoVideoUrl?.takeIf { it.isNotBlank() }
        ?: if (image.isGif) image.largeUrl else ""
    val gifFallback = videoUrl == image.largeUrl
    var videoVisible by remember(videoUrl) { mutableStateOf(false) }
    val player = remember(videoUrl) {
        androidx.media3.exoplayer.ExoPlayer.Builder(context).build().apply {
            setMediaSource(buildVideoMediaSource(context, videoUrl))
            repeatMode = androidx.media3.common.Player.REPEAT_MODE_OFF
            prepare()
            playWhenReady = true
        }
    }

    DisposableEffect(player) {
        val listener = object : androidx.media3.common.Player.Listener {
            override fun onRenderedFirstFrame() {
                videoVisible = true
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == androidx.media3.common.Player.STATE_ENDED) onEnded()
            }

            override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                onEnded()
            }
        }
        player.addListener(listener)
        onDispose {
            player.removeListener(listener)
            player.release()
        }
    }

    AndroidView(
        modifier = modifier.graphicsLayer {
            alpha = if (videoVisible) 1f else 0f
        },
        factory = { ctx ->
            (android.view.LayoutInflater.from(ctx)
                .inflate(R.layout.view_live_photo_player, null, false) as androidx.media3.ui.PlayerView).apply {
                this.player = player
                useController = false
                resizeMode = androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_FIT
                setShutterBackgroundColor(android.graphics.Color.TRANSPARENT)
                setBackgroundColor(android.graphics.Color.TRANSPARENT)
            }
        },
        update = { it.player = player },
    )
}

@Composable
private fun InlineVideoPlayer(media: FeedMedia, onClick: () -> Unit = {}) {
    val videoCoordinator = LocalVideoPlaybackCoordinator.current
    val playbackKey = remember(media.streamUrl, media.downloadUrl, media.coverUrl) { videoPlaybackKey(media) }
    val inlinePlaying = videoCoordinator.activeKey == playbackKey
    var fullscreen by remember(media.streamUrl) { mutableStateOf(false) }
    var aspectRatio by remember(media.streamUrl) { mutableStateOf(16f / 9f) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(aspectRatio.coerceIn(0.56f, 1.78f))
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHighest),
    ) {
        if (inlinePlaying && media.type == MediaType.Video) {
            WeiboVideoSurface(
                media = media,
                isFullscreen = false,
                onAspectRatio = { aspectRatio = it },
                onFullscreen = { fullscreen = true },
            )
        } else {
            RemoteImage(
                url = media.coverUrl,
                modifier = Modifier
                    .fillMaxSize()
                    .clickable {
                        if (media.type == MediaType.Video) {
                            videoCoordinator.activeKey = playbackKey
                        } else {
                            onClick()
                        }
                    },
                contentScale = ContentScale.Crop,
            )
            IconButton(
                onClick = {
                    if (media.type == MediaType.Video) {
                        videoCoordinator.activeKey = playbackKey
                    } else {
                        onClick()
                    }
                },
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(52.dp),
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_video_play),
                    contentDescription = "播放",
                    modifier = Modifier.size(32.dp),
                    tint = Color.White,
                )
            }
            Text(
                text = media.title,
                modifier = Modifier.align(Alignment.BottomStart).padding(12.dp),
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.labelLarge,
            )
        }
    }

    if (fullscreen) {
        Dialog(
            onDismissRequest = { fullscreen = false },
            properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false),
        ) {
            BackHandler { fullscreen = false }
            Box(Modifier.fillMaxSize().background(Color.Black), contentAlignment = Alignment.Center) {
                WeiboVideoSurface(
                    media = media,
                    isFullscreen = true,
                    onAspectRatio = {},
                    onFullscreen = { fullscreen = false },
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}

@Composable
private fun WeiboVideoSurface(
    media: FeedMedia,
    isFullscreen: Boolean,
    onAspectRatio: (Float) -> Unit,
    onFullscreen: () -> Unit,
    modifier: Modifier = Modifier.fillMaxSize(),
) {
    val context = LocalContext.current
    val videoCoordinator = LocalVideoPlaybackCoordinator.current
    val playbackKey = remember(media.streamUrl, media.downloadUrl, media.coverUrl) { videoPlaybackKey(media) }
    val videoCandidates = remember(media.streamUrl, media.downloadUrl) {
        listOfNotNull(media.streamUrl, media.downloadUrl)
            .flatMap(::videoUrlCandidates)
            .distinct()
    }
    var videoIndex by remember(videoCandidates) { mutableStateOf(0) }
    val videoUrl = videoCandidates.getOrElse(videoIndex) { media.streamUrl }
    var playbackError by remember(videoUrl) { mutableStateOf<String?>(null) }
    var isBuffering by remember(videoUrl) { mutableStateOf(true) }
    var positionMs by remember(videoUrl) { mutableStateOf(0L) }
    var durationMs by remember(videoUrl) { mutableStateOf(0L) }
    var isPlaying by remember(videoUrl) { mutableStateOf(true) }
    var selectedSpeed by remember(videoUrl) { mutableStateOf(1f) }
    var displayedSpeed by remember(videoUrl) { mutableStateOf(1f) }

    val playerCache = remember { mutableMapOf<String, androidx.media3.exoplayer.ExoPlayer>() }
    val player = remember(videoUrl) {
        playerCache[videoUrl]?.also { it.playWhenReady = true }
            ?: androidx.media3.exoplayer.ExoPlayer.Builder(context).build().apply {
                playerCache[videoUrl] = this
                setMediaSource(buildVideoMediaSource(context, videoUrl))
                prepare()
                videoCoordinator.positions[playbackKey]?.takeIf { it > 0L }?.let { seekTo(it) }
                playWhenReady = true
            }
    }

    DisposableEffect(player) {
        val listener = object : androidx.media3.common.Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                isBuffering = playbackState == androidx.media3.common.Player.STATE_BUFFERING ||
                    playbackState == androidx.media3.common.Player.STATE_IDLE
                isPlaying = player.isPlaying
            }

            override fun onIsPlayingChanged(value: Boolean) {
                isPlaying = value
            }

            override fun onVideoSizeChanged(videoSize: androidx.media3.common.VideoSize) {
                val width = videoSize.width.takeIf { it > 0 } ?: return
                val height = videoSize.height.takeIf { it > 0 } ?: return
                onAspectRatio(width.toFloat() / height.toFloat())
            }

            override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                isBuffering = false
                if (videoIndex < videoCandidates.lastIndex) {
                    videoIndex += 1
                } else {
                    playbackError = error.message ?: "视频无法播放"
                }
            }
        }
        player.addListener(listener)
        onDispose {
            videoCoordinator.positions[playbackKey] = player.currentPosition.coerceAtLeast(0L)
            player.removeListener(listener)
            player.release()
        }
    }

    LaunchedEffect(player) {
        while (true) {
            positionMs = player.currentPosition.coerceAtLeast(0L)
            videoCoordinator.positions[playbackKey] = positionMs
            durationMs = player.duration.takeIf { it > 0 } ?: durationMs
            isPlaying = player.isPlaying
            delay(80)
        }
    }

    Box(
        modifier = modifier
            .background(Color.Black),
    ) {
        AndroidView(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(player, selectedSpeed) {
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        down.consume()
                        val upBeforeLongPress = withTimeoutOrNull(360L) { waitForUpOrCancellation() }
                        if (upBeforeLongPress == null) {
                            displayedSpeed = 2f
                            player.setPlaybackSpeed(2f)
                            val up = waitForUpOrCancellation()
                            up?.consume()
                            displayedSpeed = selectedSpeed
                            player.setPlaybackSpeed(selectedSpeed)
                        } else {
                            upBeforeLongPress.consume()
                        }
                    }
                },
            factory = { ctx ->
                androidx.media3.ui.PlayerView(ctx).apply {
                    this.player = player
                    useController = false
                    resizeMode = androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_FIT
                    setShutterBackgroundColor(android.graphics.Color.BLACK)
                }
            },
            update = { it.player = player },
        )

        if (!isFullscreen) {
            GlassTextButton(
                text = "全屏",
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(10.dp)
                    .width(54.dp)
                    .height(28.dp),
                onClick = onFullscreen,
            )
        }

        if (isBuffering && playbackError == null) {
            Box(
                modifier = Modifier.matchParentSize().background(Color.Black.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(28.dp), strokeWidth = 2.dp)
            }
        }

        playbackError?.let { error ->
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color.Black.copy(alpha = 0.64f)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = error,
                    color = Color.White,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(16.dp),
                    textAlign = TextAlign.Center,
                )
            }
        }

        VideoControls(
            isPlaying = isPlaying,
            positionMs = positionMs,
            durationMs = durationMs,
            speed = displayedSpeed,
            onPlayPause = {
                if (player.isPlaying) {
                    player.pause()
                } else {
                    if (durationMs > 0 && positionMs >= durationMs - 500) {
                        player.seekTo(0)
                    }
                    videoCoordinator.activeKey = playbackKey
                    player.play()
                }
                isPlaying = player.isPlaying
            },
            onSeek = { target ->
                positionMs = target
                player.seekTo(target)
            },
            onSpeedClick = {
                selectedSpeed = when (selectedSpeed) {
                    1f -> 1.5f
                    1.5f -> 2f
                    else -> 1f
                }
                displayedSpeed = selectedSpeed
                player.setPlaybackSpeed(selectedSpeed)
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(start = 6.dp, end = 6.dp, bottom = if (isFullscreen) 24.dp else 8.dp)
                .fillMaxWidth()
                .height(32.dp),
        )
    }
}

@Composable
private fun GlassTextButton(
    text: String,
    modifier: Modifier,
    onClick: () -> Unit,
) {
    Box(
        modifier = modifier.clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        VideoControlGlassBackground(Modifier.matchParentSize())
        Text(
            text = text,
            color = Color.White,
            style = MaterialTheme.typography.labelMedium,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun VideoControls(
    isPlaying: Boolean,
    positionMs: Long,
    durationMs: Long,
    speed: Float,
    onPlayPause: () -> Unit,
    onSeek: (Long) -> Unit,
    onSpeedClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        VideoControlGlassBackground(Modifier.matchParentSize())
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            IconButton(
                onClick = onPlayPause,
                modifier = Modifier.size(24.dp),
            ) {
                Icon(
                    painter = painterResource(if (isPlaying) R.drawable.ic_video_pause else R.drawable.ic_video_play),
                    contentDescription = if (isPlaying) "暂停" else "播放",
                    modifier = Modifier.size(17.dp),
                    tint = Color.White,
                )
            }
            Text(
                text = formatVideoTime(positionMs),
                modifier = Modifier.width(38.dp),
                color = Color.White,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
            )
            CompactVideoScrubber(
                positionMs = positionMs,
                durationMs = durationMs,
                onSeek = onSeek,
                modifier = Modifier.weight(1f).height(18.dp),
            )
            Text(
                text = "-${formatVideoTime((durationMs - positionMs).coerceAtLeast(0L))}",
                modifier = Modifier.width(42.dp),
                color = Color.White,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
            )
            TextButton(
                onClick = onSpeedClick,
                modifier = Modifier.width(30.dp),
                contentPadding = PaddingValues(0.dp),
            ) {
                Text(
                    text = speedLabel(speed),
                    color = Color.White,
                    fontSize = 13.sp,
                    maxLines = 1,
                )
            }
        }
    }
}

@Composable
private fun CompactVideoScrubber(
    positionMs: Long,
    durationMs: Long,
    onSeek: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val progress = if (durationMs > 0L) (positionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f) else 0f
    Canvas(
        modifier = modifier.pointerInput(durationMs) {
            awaitEachGesture {
                val down = awaitFirstDown(requireUnconsumed = false)
                fun seekTo(x: Float) {
                    if (durationMs <= 0L || size.width <= 0) return
                    val ratio = (x / size.width.toFloat()).coerceIn(0f, 1f)
                    onSeek((durationMs * ratio).toLong())
                }
                seekTo(down.position.x)
                while (true) {
                    val event = awaitPointerEvent()
                    val change = event.changes.firstOrNull() ?: break
                    if (change.pressed) seekTo(change.position.x)
                    if (event.changes.all { it.changedToUpIgnoreConsumed() }) break
                }
            }
        },
    ) {
        val y = size.height / 2f
        val stroke = 3.dp.toPx()
        drawLine(
            color = Color.White.copy(alpha = 0.26f),
            start = Offset(0f, y),
            end = Offset(size.width, y),
            strokeWidth = stroke,
            cap = StrokeCap.Round,
        )
        drawLine(
            color = Color(0xFFFF4F9A),
            start = Offset(0f, y),
            end = Offset(size.width * progress, y),
            strokeWidth = stroke,
            cap = StrokeCap.Round,
        )
        drawCircle(
            color = Color.White,
            radius = 5.5.dp.toPx(),
            center = Offset(size.width * progress, y),
        )
    }
}

@Composable
private fun VideoControlGlassBackground(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .blur(24.dp)
            .background(Color(0xFF3A3A3C).copy(alpha = 0.45f))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.06f),
                        Color.White.copy(alpha = 0.02f),
                    )
                )
            ),
    )
}

@Composable
private fun StatusActions(item: FeedItem, onCommentClick: (() -> Unit)? = null) {
    val actionColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.62f)
    val chipColor = actionColor
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(26.dp)
            .graphicsLayer {
                scaleX = 0.94f
                scaleY = 0.94f
            },
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AssistChip(
            onClick = {},
            modifier = Modifier.height(24.dp),
            colors = AssistChipDefaults.assistChipColors(containerColor = Color.Transparent, labelColor = actionColor),
            border = null,
            label = { Text("转发 ${item.repostsCount}", fontSize = 11.sp, color = chipColor) },
        )
        AssistChip(
            onClick = { onCommentClick?.invoke() },
            modifier = Modifier.height(24.dp),
            colors = AssistChipDefaults.assistChipColors(containerColor = Color.Transparent, labelColor = actionColor),
            border = null,
            label = { Text("评论 ${item.commentsCount}", fontSize = 11.sp, color = chipColor) },
        )
        AssistChip(
            onClick = {},
            modifier = Modifier.height(24.dp),
            colors = AssistChipDefaults.assistChipColors(containerColor = Color.Transparent, labelColor = actionColor),
            border = null,
            label = { Text("\u8D5E ${item.likesCount}", fontSize = 11.sp, color = chipColor) },
        )
    }
}

@Composable
private fun DetailScreen(
    item: FeedItem,
    comments: List<CommentItem>,
    isLoadingComments: Boolean,
    onBack: () -> Unit,
    onRefresh: () -> Unit,
    onMediaClick: (FeedMedia) -> Unit,
    emoticonMap: Map<String, String> = emptyMap(),
    onRetweetClick: ((FeedItem) -> Unit)? = null,
    onUserClick: ((String) -> Unit)? = null,
    commentsHasMore: Boolean = true,
    onLoadMoreComments: () -> Unit = {},
) {
    val topInset = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val listState = rememberLazyListState()

    LaunchedEffect(listState) {
        snapshotFlow {
            val info = listState.layoutInfo
            val last = info.visibleItemsInfo.lastOrNull()?.index ?: 0
            info.totalItemsCount > 0 && last >= info.totalItemsCount - 3
        }
            .distinctUntilChanged()
            .filter { it }
            .collect { onLoadMoreComments() }
    }

    PullToRefreshBox(
        isRefreshing = isLoadingComments,
        onRefresh = onRefresh,
        modifier = Modifier.fillMaxSize(),
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = topInset, bottom = 24.dp),
        ) {
            item {
                Column(Modifier.padding(vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    FeedCard(
                        item = item,
                        onClick = {},
                        onMediaClick = onMediaClick,
                        emoticonMap = emoticonMap,
                        onRetweetClick = onRetweetClick,
                        onUserClick = onUserClick,
                    )
                    Text(
                        text = "评论 ${item.commentsCount}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 8.dp),
                    )
                }
            }

            if (isLoadingComments && comments.isEmpty()) {
                item {
                    Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            }

            if (comments.isEmpty() && !isLoadingComments) {
                item {
                    EmptyState(title = "暂无评论", body = "")
                }
            }

            items(comments, key = { it.id }) { comment ->
                CommentRow(comment)
                HorizontalDivider()
            }
        }
    }
}

@Composable
private fun CommentRow(comment: CommentItem, depth: Int = 0) {
    val resolvedMap = comment.emoticons.ifEmpty { emptyMap() }
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = (18 + depth * 24).dp, end = 18.dp, top = 10.dp, bottom = 4.dp),
            verticalAlignment = Alignment.Top,
        ) {
            RemoteImage(
                url = comment.authorAvatarUrl,
                modifier = Modifier.size(28.dp).clip(CircleShape),
                contentScale = ContentScale.Crop,
            )
            Spacer(Modifier.width(8.dp))
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(comment.authorName, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    if (comment.replyToAuthor != null) {
                        Text(
                            text = " 回复 @${comment.replyToAuthor}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                EmoticonText(text = comment.text, emoticonMap = resolvedMap, style = MaterialTheme.typography.bodyMedium)
                Text(
                    text = listOfNotNull(
                        formatWeiboTime(comment.createdAt),
                        comment.ipLocation?.let { "来自$it" },
                        "\u8D5E ${comment.likesCount}",
                    ).joinToString("  "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.58f),
                )
            }
        }
        comment.comments.forEach { nested ->
            CommentRow(comment = nested, depth = depth + 1)
        }
    }
}

@Composable
private fun PlaceholderScreen(
    title: String,
    body: String,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = body,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Surface(
            color = MaterialTheme.colorScheme.surfaceContainerHighest,
            shape = RoundedCornerShape(8.dp),
        ) {
            Text(
                text = "\u8FD9\u662F\u9996\u7248\u5360\u4F4D\u9875\uFF0C\u5DF2\u7ECF\u63A5\u5165\u5E95\u90E8\u5BFC\u822A\u548C\u8FD4\u56DE\u6808\u3002",
                modifier = Modifier.padding(14.dp),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun MineScreen(
    session: WeiboWebSession,
    profile: UserProfile?,
    isLoading: Boolean,
    loadError: String?,
    hasLoginCookie: Boolean,
    posts: List<FeedItem>,
    postsError: String?,
    postsLoadingMore: Boolean,
    albumImages: List<FeedImage>,
    albumLoadingMore: Boolean,
    postsHasMore: Boolean = true,
    albumHasMore: Boolean = true,
    emoticonMap: Map<String, String> = emptyMap(),
    emoticonCount: Int = 0,
    emoticonSyncing: Boolean = false,
    postsListState: LazyListState = rememberLazyListState(),
    onRefresh: () -> Unit,
    onLoadMorePosts: () -> Unit,
    onLoadMoreAlbum: () -> Unit,
    onSyncEmoticons: () -> Unit,
    onItemClick: (FeedItem) -> Unit,
    onMediaClick: (FeedMedia) -> Unit,
    onUserClick: ((String) -> Unit)? = null,
) {
    var showSettings by remember { mutableStateOf(false) }
    var showAccountManagement by remember { mutableStateOf(false) }
    val pagerState = rememberPagerState(pageCount = { MineContentTab.entries.size })
    val coroutineScope = rememberCoroutineScope()
    val albumListState = rememberLazyListState()

    if (showSettings) {
        BackHandler {
            if (showAccountManagement) {
                showAccountManagement = false
            } else {
                showSettings = false
            }
        }
        if (showAccountManagement) {
            SettingsPageShell(
                title = "\u8D26\u53F7\u7BA1\u7406",
                onBack = { showAccountManagement = false },
            ) {
                AccountLoginPanel(session = session)
            }
        } else {
            SettingsScreen(
                hasLoginCookie = hasLoginCookie,
                emoticonCount = emoticonCount,
                emoticonSyncing = emoticonSyncing,
                onBack = {
                    showAccountManagement = false
                    showSettings = false
                },
                onOpenAccount = { showAccountManagement = true },
                onSyncEmoticons = onSyncEmoticons,
            )
        }
        return
    }


    val topInset = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    // Infinite scroll for Posts
    LaunchedEffect(postsListState) {
        snapshotFlow {
            val layoutInfo = postsListState.layoutInfo
            val lastVisibleIndex = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            layoutInfo.totalItemsCount > 0 && lastVisibleIndex >= layoutInfo.totalItemsCount - 2
        }
            .distinctUntilChanged()
            .filter { it }
            .collect { onLoadMorePosts() }
    }

    // Infinite scroll for Album
    LaunchedEffect(albumListState) {
        snapshotFlow {
            albumListState.layoutInfo.totalItemsCount > 0 && !albumListState.canScrollForward
        }
            .distinctUntilChanged()
            .filter { it }
            .collect { onLoadMoreAlbum() }
    }

    // Two-way sync: tab click \u2192 pager scroll
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }
            .collect { page -> }
    }

    Column(Modifier.fillMaxSize()) {
        PullToRefreshBox(
            isRefreshing = isLoading,
            onRefresh = onRefresh,
            modifier = Modifier.fillMaxWidth().weight(1f),
        ) {
            Column(Modifier.fillMaxSize()) {
                MineProfileHeader(
                    profile = profile,
                    hasLoginCookie = hasLoginCookie,
                    loadError = loadError,
                    onOpenSettings = { showSettings = true },
                )

                MineContentTabs(
                    selectedTab = MineContentTab.entries[pagerState.currentPage],
                    onTabSelected = { tab ->
                        val sameTab = tab == MineContentTab.entries[pagerState.currentPage]
                        if (sameTab) {
                            coroutineScope.launch {
                                when (tab) {
                                    MineContentTab.Posts -> postsListState.animateScrollToItem(0)
                                    MineContentTab.Album -> albumListState.animateScrollToItem(0)
                                }
                            }
                        } else {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(MineContentTab.entries.indexOf(tab))
                            }
                        }
                    },
                )

                postsError?.let { error ->
                    Text(
                        text = error,
                        modifier = Modifier.padding(horizontal = 4.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }

                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    beyondViewportPageCount = 1,
                ) { page ->
                    when (MineContentTab.entries[page]) {
                        MineContentTab.Posts -> {
                            LazyColumn(
                                state = postsListState,
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(bottom = 96.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                if (posts.isEmpty()) {
                                    item {
                                        EmptyState(
                                            title = if (hasLoginCookie) "\u6682\u672A\u8BFB\u5230\u4E3B\u9875\u5FAE\u535A" else "\u672A\u767B\u5F55",
                                            body = if (hasLoginCookie) "\u4E0B\u62C9\u5237\u65B0\u540E\u4F1A\u4ECE\u4E2A\u4EBA\u4E3B\u9875\u91CD\u65B0\u8BFB\u53D6\u3002" else "\u8BF7\u5728\u5C01\u9762\u53F3\u4E0A\u89D2\u8BBE\u7F6E\u4E2D\u767B\u5F55\u5FAE\u535A\u3002",
                                        )
                                    }
                                } else {
                                    items(posts, key = { it.id }) { post ->
                                        FeedCard(
                                            item = post,
                                            onClick = { onItemClick(post) },
                                            onMediaClick = onMediaClick,
                                            emoticonMap = emoticonMap,
                                            onUserClick = onUserClick,
                                            onRetweetClick = onItemClick,
                                        )
                                    }
                                    if (postsLoadingMore) {
                                        item { MineLoadingMoreIndicator() }
                                    }
                                    if (!postsHasMore && posts.isNotEmpty()) {
                                        item {
                                            Text(
                                                text = "\u5DF2\u7ECF\u5230\u5E95\u4E86",
                                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                                textAlign = TextAlign.Center,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        MineContentTab.Album -> {
                            LazyColumn(
                                state = albumListState,
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(bottom = 96.dp),
                            ) {
                                item {
                                    MineAlbumTimeline(
                                        posts = posts,
                                        albumImages = albumImages,
                                        onMediaClick = onMediaClick,
                                    )
                                }
                                if (albumLoadingMore) {
                                    item { MineLoadingMoreIndicator() }
                                }
                                if (!albumHasMore && albumImages.isNotEmpty()) {
                                    item {
                                        Text(
                                            text = "\u5DF2\u7ECF\u5230\u5E95\u4E86",
                                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                                            textAlign = TextAlign.Center,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsPageShell(
    title: String,
    onBack: () -> Unit,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = 16.dp,
                    top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 6.dp,
                    end = 16.dp,
                    bottom = 8.dp,
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = title,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        ) {
            content()
        }
    }
}

@Composable
private fun SettingsScreen(
    hasLoginCookie: Boolean,
    emoticonCount: Int,
    emoticonSyncing: Boolean,
    onBack: () -> Unit,
    onOpenAccount: () -> Unit,
    onSyncEmoticons: () -> Unit,
) {
    SettingsPageShell(title = "设置", onBack = onBack) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                SettingsActionCard(
                    title = "账号管理",
                    subtitle = if (hasLoginCookie) {
                        "\u7BA1\u7406\u5FAE\u535A\u7F51\u9875\u767B\u5F55\u6001\uFF0C\u6216\u91CD\u65B0\u767B\u5F55\u8D26\u53F7"
                    } else {
                        "\u767B\u5F55\u5FAE\u535A\u4EE5\u8BFB\u53D6\u4E3B\u9875\u3001\u4FE1\u606F\u6D41\u4E0E\u8BC4\u8BBA"
                    },
                    status = if (hasLoginCookie) "\u5DF2\u767B\u5F55" else "\u672A\u767B\u5F55",
                    actionLabel = "进入",
                    onClick = onOpenAccount,
                )
            }
            item {
                SettingsActionCard(
                    title = "表情同步",
                    subtitle = if (emoticonCount > 0) {
                        "\u5DF2\u540C\u6B65 $emoticonCount \u4E2A\u5FAE\u535A\u8868\u60C5\uFF0C\u53EF\u7EE7\u7EED\u66F4\u65B0"
                    } else {
                        "同步微博表情配置，提升正文与评论表情显示"
                    },
                    status = if (emoticonSyncing) {
                        "\u540C\u6B65\u4E2D"
                    } else if (emoticonCount > 0) {
                        "\u5DF2\u540C\u6B65"
                    } else {
                        "\u672A\u540C\u6B65"
                    },
                    actionLabel = if (emoticonCount > 0) "更新" else "同步",
                    loading = emoticonSyncing,
                    enabled = !emoticonSyncing,
                    onClick = onSyncEmoticons,
                )
            }
        }
    }
}

@Composable
private fun SettingsActionCard(
    title: String,
    subtitle: String,
    status: String,
    actionLabel: String,
    loading: Boolean = false,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(enabled = enabled, onClick = onClick),
        shape = RoundedCornerShape(8.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceContainerHighest,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        shape = RoundedCornerShape(8.dp),
                    ) {
                        Text(
                            text = status,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            style = MaterialTheme.typography.labelSmall,
                            maxLines = 1,
                        )
                    }
                }
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    strokeWidth = 2.dp,
                )
            } else {
                Text(
                    text = actionLabel,
                    style = MaterialTheme.typography.labelLarge,
                    color = if (enabled) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@Composable
private fun MineProfileHeader(
    profile: UserProfile?,
    hasLoginCookie: Boolean,
    loadError: String?,
    onOpenSettings: (() -> Unit)?,
) {
    ElevatedCard(shape = RoundedCornerShape(8.dp)) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(3f)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.20f),
                                Color.White.copy(alpha = 0.42f),
                                MaterialTheme.colorScheme.surfaceContainerHighest,
                            )
                        )
                    )
            ) {
                profile?.coverUrl?.let { cover ->
                    RemoteImage(
                        url = cover,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.FillWidth,
                    )
                }
                if (onOpenSettings != null) {
                    Surface(
                        modifier = Modifier.align(Alignment.TopEnd).padding(top = 32.dp, end = 10.dp),
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.62f),
                        tonalElevation = 0.dp,
                        shadowElevation = 1.dp,
                    ) {
                        IconButton(
                            onClick = onOpenSettings,
                            modifier = Modifier.size(42.dp),
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_settings),
                                contentDescription = "\u8BBE\u7F6E",
                                tint = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, top = 0.dp, end = 16.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Surface(
                        modifier = Modifier.padding(top = 0.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surface,
                        shadowElevation = 2.dp,
                    ) {
                        RemoteImage(
                            url = profile?.avatarUrl,
                            modifier = Modifier.size(84.dp).padding(4.dp).clip(CircleShape),
                            contentScale = ContentScale.Crop,
                        )
                    }
                    Spacer(Modifier.width(14.dp))
                    Column(
                        modifier = Modifier.weight(1f).padding(bottom = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(3.dp),
                    ) {
                        Text(
                            text = profile?.screenName ?: if (hasLoginCookie) "\u5FAE\u535A\u7528\u6237" else "\u672A\u767B\u5F55",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = profile?.description?.takeIf { it.isNotBlank() }
                                ?: if (hasLoginCookie) "\u4E2A\u4EBA\u7B80\u4ECB\u6682\u672A\u83B7\u53D6" else "\u8BF7\u5148\u5728\u8BBE\u7F6E\u4E2D\u767B\u5F55\u5FAE\u535A",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }

                profile?.verifiedReason?.takeIf { it.isNotBlank() }?.let { reason ->
                    Text(
                        text = reason,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                MineInlineStats(profile)
            }

            loadError?.let {
                Text(
                    text = it,
                    modifier = Modifier.padding(horizontal = 16.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}

@Composable
private fun MineInlineStats(profile: UserProfile?) {
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        MineInlineStat(value = profile?.followersCount ?: "--", label = "\u7C89\u4E1D")
        MineInlineStat(value = profile?.followingCount ?: "--", label = "\u5173\u6CE8")
        MineInlineStat(value = profile?.statusesCount ?: "--", label = "\u5FAE\u535A")
    }
}

@Composable
private fun MineInlineStat(value: String, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(value, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.width(3.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun MineContentTabs(
    selectedTab: MineContentTab,
    onTabSelected: (MineContentTab) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(start = 4.dp, top = 2.dp, bottom = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(22.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        MineContentTab.entries.forEach { tab ->
            val selected = selectedTab == tab
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(3.dp))
                    .clickable { onTabSelected(tab) }
                    .padding(horizontal = 2.dp, vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = tab.label,
                    fontSize = 14.sp,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (selected) Color(0xFFFF4F9A) else MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Box(
                    modifier = Modifier
                        .width(22.dp)
                        .height(3.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(if (selected) Color(0xFFFF4F9A) else Color.Transparent)
                )
            }
        }
    }
}

@Composable
private fun MineLoadingMoreIndicator() {
    Box(
        modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
    }
}

@Composable
private fun MineAlbumTimeline(
    posts: List<FeedItem>,
    albumImages: List<FeedImage>,
    onMediaClick: (FeedMedia) -> Unit,
) {
    var viewerImages by remember { mutableStateOf<List<FeedImage>>(emptyList()) }
    var viewerIndex by remember { mutableStateOf(0) }
    val postGroups = remember(posts) {
        posts.mapNotNull { post ->
            val imgs = post.images.distinctBy { it.largeUrl }
            if (imgs.isEmpty()) null else albumDateLabel(post.createdAt) to imgs
        }
        .groupBy({ it.first }, { it.second })
        .mapValues { entry -> entry.value.flatten().distinctBy { it.largeUrl } }
    }
    val albumGrouped = remember(albumImages) {
        val all = albumImages.distinctBy { it.largeUrl }
        if (all.isEmpty()) emptyMap()
        else all.groupBy { albumDateLabel(it.createdAt) }
            .mapValues { entry -> entry.value.distinctBy { it.largeUrl } }
    }
    val visibleGroups = if (albumGrouped.isNotEmpty()) albumGrouped else postGroups

    if (visibleGroups.isEmpty()) {
        EmptyState(
            title = "\u6682\u672A\u8BFB\u5230\u76F8\u518C",
            body = "\u4E0B\u62C9\u5237\u65B0\u540E\u4F1A\u4ECE\u4E2A\u4EBA\u4E3B\u9875\u5FAE\u535A\u4E2D\u6574\u7406\u56FE\u7247\u548C LivePhoto\u3002",
        )
        return
    }

    Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
        visibleGroups.forEach { (dateLabel, images) ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Column(
                    modifier = Modifier.width(42.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = dateLabel.first,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    if (dateLabel.second.isNotBlank()) {
                        Text(
                            text = dateLabel.second,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                BoxWithConstraints(modifier = Modifier.weight(1f)) {
                    val gap = 6.dp
                    val cellSize = (maxWidth - gap * 2) / 3
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(gap),
                        verticalArrangement = Arrangement.spacedBy(gap),
                        maxItemsInEachRow = 3,
                    ) {
                        images.forEach { image ->
                            MineAlbumTile(
                                image = image,
                                size = cellSize,
                                onClick = {
                                    viewerImages = images
                                    viewerIndex = images.indexOf(image).coerceAtLeast(0)
                                },
                            )
                        }
                    }
                }
            }
        }
    }

    if (viewerImages.isNotEmpty()) {
        FullscreenImageViewer(
            images = viewerImages,
            initialIndex = viewerIndex,
            onDismiss = { viewerImages = emptyList() },
        )
    }
}

@Composable
private fun MineAlbumTile(
    image: FeedImage,
    size: androidx.compose.ui.unit.Dp,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
    ) {
        RemoteImage(
            url = image.thumbnailUrl.ifBlank { image.largeUrl },
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )
        if (image.isLivePhoto) {
            Surface(
                modifier = Modifier.align(Alignment.BottomEnd).padding(4.dp),
                shape = RoundedCornerShape(3.dp),
                color = Color.Black.copy(alpha = 0.38f),
                contentColor = Color.White,
            ) {
                Text(
                    text = "Live",
                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp),
                    style = MaterialTheme.typography.labelSmall,
                )
            }
        }
    }
}

private fun ownAlbumImagesFromPosts(posts: List<FeedItem>): List<FeedImage> =
    posts.flatMap { post ->
        post.images.map { it.copy(createdAt = it.createdAt ?: post.createdAt) }
    }.distinctBy { it.largeUrl }

private fun filterOutRetweetedOnlyImages(images: List<FeedImage>, posts: List<FeedItem>): List<FeedImage> {
    if (images.isEmpty() || posts.isEmpty()) return images
    val ownUrls = posts.flatMap { it.images }.mapTo(mutableSetOf()) { it.largeUrl }
    val retweetedUrls = posts.flatMap { it.retweetedStatus?.images ?: emptyList() }
        .mapTo(mutableSetOf()) { it.largeUrl }
    if (retweetedUrls.isEmpty()) return images
    return images.filterNot { image -> image.largeUrl in retweetedUrls && image.largeUrl !in ownUrls }
}

private fun albumDateLabel(createdAt: String?): Pair<String, String> {
    val value = createdAt.orEmpty()
    val year = Regex("""(19|20)\d{2}""").find(value)?.value.orEmpty()

    Regex("""(19|20)\d{2}[-/](\d{1,2})[-/]\d{1,2}""").find(value)?.let { match ->
        val month = match.groupValues.getOrNull(2).orEmpty().trimStart('0').ifBlank { "0" }
        return "${month}\u6708" to year
    }

    val englishMonth = listOf(
        "Jan" to "\u0031\u6708",
        "Feb" to "\u0032\u6708",
        "Mar" to "\u0033\u6708",
        "Apr" to "\u0034\u6708",
        "May" to "\u0035\u6708",
        "Jun" to "\u0036\u6708",
        "Jul" to "\u0037\u6708",
        "Aug" to "\u0038\u6708",
        "Sep" to "\u0039\u6708",
        "Oct" to "\u0031\u0030\u6708",
        "Nov" to "\u0031\u0031\u6708",
        "Dec" to "\u0031\u0032\u6708",
    ).firstOrNull { value.contains(it.first, ignoreCase = true) }?.second
    if (englishMonth != null) return englishMonth to year

    Regex("""(\d{1,2})[-/]\d{1,2}""").find(value)?.groupValues?.getOrNull(1)?.let {
        return "${it.trimStart('0').ifBlank { "0" }}\u6708" to year
    }
    return if (year.isNotBlank()) "\u5168\u90E8" to year else "\u5168\u90E8" to ""
}

@Composable
private fun MineStatsRow(profile: UserProfile?) {
    ElevatedCard(shape = RoundedCornerShape(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            MineStatItem(value = profile?.followersCount ?: "--", label = "\u7C89\u4E1D")
            MineStatItem(value = profile?.followingCount ?: "--", label = "\u5173\u6CE8")
            MineStatItem(value = profile?.statusesCount ?: "--", label = "\u5FAE\u535A")
            MineStatItem(value = profile?.photosCount ?: "--", label = "\u76F8\u518C")
        }
    }
}

@Composable
private fun MineStatItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun MineShortcutGrid() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "\u5185\u5BB9",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            MineShortcut("\u5168\u90E8\u5FAE\u535A", "\u65F6\u95F4\u7EBF")
            MineShortcut("\u76F8\u518C", "\u56FE\u7247\u4E0E LivePhoto")
            MineShortcut("\u89C6\u9891", "\u5FAE\u535A\u89C6\u9891")
            MineShortcut("\u8D5E\u8FC7", "\u559C\u6B22\u5185\u5BB9")
            MineShortcut("\u6536\u85CF", "\u7A0D\u540E\u9605\u8BFB")
            MineShortcut("\u8BC4\u8BBA", "\u4E92\u52A8\u8BB0\u5F55")
        }
    }
}

@Composable
private fun MineShortcut(title: String, subtitle: String) {
    Surface(
        modifier = Modifier.width(156.dp),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHighest,
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun MineInfoPanel(profile: UserProfile?, hasLoginCookie: Boolean) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHighest,
    ) {
        Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("\u8D26\u6237\u4FE1\u606F", fontWeight = FontWeight.SemiBold)
            MineInfoLine("\u767B\u5F55\u72B6\u6001", if (hasLoginCookie) "\u5DF2\u767B\u5F55" else "\u672A\u767B\u5F55")
            MineInfoLine("\u5730\u533A", profile?.location?.takeIf { it.isNotBlank() } ?: "--")
        }
    }
}

@Composable
private fun MineInfoLine(label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = label,
            modifier = Modifier.width(82.dp),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodySmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun AccountLoginPanel(session: WeiboWebSession) {
    Column(Modifier.fillMaxSize()) {
        Surface(
            color = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text("\u5FAE\u535A\u7F51\u9875\u767B\u5F55", fontWeight = FontWeight.SemiBold)
                Text(
                    "\u8FD9\u91CC\u4F7F\u7528\u684C\u9762 Chrome \u6807\u8BC6\u6253\u5F00 weibo.com\u3002\u767B\u5F55\u6210\u529F\u540E\u70B9\u51FB\u56DE\u5230\u5FAE\u535A\u9996\u9875\uFF0C\u518D\u5230\u9996\u9875\u540C\u6B65\u3002",
                    style = MaterialTheme.typography.bodySmall,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { session.openWeiboHome() }) {
                        Text("\u56DE\u5230\u5FAE\u535A\u9996\u9875")
                    }
                    TextButton(onClick = { session.openLogin() }) {
                        Text("\u91CD\u65B0\u6253\u5F00")
                    }
                }
            }
        }
        AndroidView(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            factory = {
                session.webView.detachFromParent()
                session.webView
            },
        )
    }
}

@Composable
private fun AccountScreen(session: WeiboWebSession) {
    Column(Modifier.fillMaxSize()) {
        Surface(
            color = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        ) {
            Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("微博网页会话", fontWeight = FontWeight.SemiBold)
                Text(
                    "\u8FD9\u91CC\u4F7F\u7528\u684C\u9762 Chrome \u6807\u8BC6\u6253\u5F00 weibo.com\u3002\u767B\u5F55\u6210\u529F\u540E\u56DE\u5230\u5FAE\u535A\u9996\u9875\uFF0C\u518D\u5230\u9996\u9875\u5237\u65B0\u3002",
                    style = MaterialTheme.typography.bodySmall,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { session.openWeiboHome() }) {
                        Text("\u5B8C\u6210\u767B\u5F55\uFF0C\u56DE\u5230\u5FAE\u535A\u9996\u9875")
                    }
                    TextButton(onClick = { session.openLogin() }) {
                        Text("重新打开")
                    }
                }
            }
        }
        AndroidView(
            modifier = Modifier.fillMaxWidth().weight(1f),
            factory = {
                session.webView.detachFromParent()
                session.webView
            },
        )
    }
}

@Composable
private fun HiddenSessionWebView(session: WeiboWebSession) {
    AndroidView(
        modifier = Modifier.size(1.dp),
        factory = {
            session.webView.detachFromParent()
            session.webView
        },
    )
}

@Composable
private fun FullscreenMediaPreview(media: FeedMedia, onDismiss: () -> Unit) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false),
    ) {
        BackHandler { onDismiss() }
        Box(Modifier.fillMaxSize().background(Color.Black), contentAlignment = Alignment.Center) {
            WeiboVideoSurface(
                media = media,
                isFullscreen = true,
                onAspectRatio = {},
                onFullscreen = onDismiss,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Composable
private fun RemoteImage(
    url: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
) {
    var bitmap by remember(url) { mutableStateOf<android.graphics.Bitmap?>(null) }
    var failed by remember(url) { mutableStateOf(false) }

    LaunchedEffect(url) {
        bitmap = null
        failed = false
        val target = url?.takeIf { it.isNotBlank() } ?: return@LaunchedEffect
        runCatching {
            withContext(Dispatchers.IO) {
                val bytes = URL(target).openConnection().apply {
                    (this as HttpURLConnection).connectTimeout = 8000
                    readTimeout = 8000
                    setRequestProperty("User-Agent", DESKTOP_CHROME_USER_AGENT)
                    setRequestProperty("Referer", "https://weibo.com/")
                }.inputStream.use { it.readBytes() }

                val options = BitmapFactory.Options().apply {
                    inJustDecodeBounds = true
                }
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
                val maxDim = 480
                val scale = maxOf(1, (options.outWidth / maxDim).coerceAtMost(options.outHeight / maxDim))
                options.inJustDecodeBounds = false
                options.inSampleSize = Integer.highestOneBit(scale)
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
            }
        }.onSuccess { bitmap = it }
            .onFailure { failed = true }
    }

    Box(
        modifier = modifier.background(MaterialTheme.colorScheme.surfaceContainerHighest),
        contentAlignment = Alignment.Center,
    ) {
        val image = bitmap
        if (image != null) {
            Image(
                bitmap = image.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = contentScale,
            )
        } else if (failed) {
            Text(
                text = "\u56FE\u7247\u4E0D\u53EF\u7528",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun EmptyState(
    title: String,
    body: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Text(
            text = body,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (actionLabel != null && onAction != null) {
            Button(onClick = onAction) { Text(actionLabel) }
        }
    }
}

private fun WebView.detachFromParent(): WebView {
    (parent as? ViewGroup)?.removeView(this)
    return this
}

private object FullscreenBitmapCache {
    private const val MaxEntries = 24
    private val entries = object : LinkedHashMap<String, android.graphics.Bitmap>(MaxEntries, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, android.graphics.Bitmap>?): Boolean =
            size > MaxEntries
    }

    @Synchronized
    fun get(url: String): android.graphics.Bitmap? = entries[url]

    @Synchronized
    fun put(url: String, bitmap: android.graphics.Bitmap) {
        entries[url] = bitmap
    }
}

private fun loadFullscreenBitmap(image: FeedImage): android.graphics.Bitmap? {
    val candidates = (image.downloadUrls + image.largeUrl + image.thumbnailUrl)
        .filter { it.isNotBlank() }
        .distinct()
    candidates.forEach { url ->
        FullscreenBitmapCache.get(url)?.let { return it }
    }
    candidates.forEach { url ->
        runCatching {
            val bytes = URL(url).openConnection().apply {
                (this as HttpURLConnection).connectTimeout = 10_000
                readTimeout = 20_000
                setRequestProperty("User-Agent", DESKTOP_CHROME_USER_AGENT)
                setRequestProperty("Referer", "https://weibo.com/")
            }.inputStream.use { it.readBytes() }
            val opts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size, opts)
            val maxDim = 4096
            val rawSample = maxOf(opts.outWidth / maxDim, opts.outHeight / maxDim).coerceAtLeast(1)
            opts.inJustDecodeBounds = false
            opts.inSampleSize = Integer.highestOneBit(rawSample).coerceAtLeast(1)
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size, opts)
        }.getOrNull()?.let { bitmap ->
            FullscreenBitmapCache.put(url, bitmap)
            return bitmap
        }
    }
    return null
}

private fun buildVideoMediaSource(
    context: android.content.Context,
    url: String,
): androidx.media3.exoplayer.source.MediaSource {
    val mediaItem = androidx.media3.common.MediaItem.fromUri(android.net.Uri.parse(url))
    val factory = weiboDataSourceFactory(context)
    return when {
        url.startsWith("data:application/dash+xml", ignoreCase = true) ||
            url.contains(".mpd", ignoreCase = true) ->
            androidx.media3.exoplayer.dash.DashMediaSource.Factory(factory).createMediaSource(mediaItem)
        url.contains("m3u8", ignoreCase = true) ->
            androidx.media3.exoplayer.hls.HlsMediaSource.Factory(factory).createMediaSource(mediaItem)
        else ->
            androidx.media3.exoplayer.source.ProgressiveMediaSource.Factory(factory).createMediaSource(mediaItem)
    }
}

private var videoCache: androidx.media3.datasource.cache.SimpleCache? = null
private fun getVideoCache(context: android.content.Context): androidx.media3.datasource.cache.SimpleCache {
    return videoCache ?: run {
        val cacheDir = java.io.File(context.cacheDir, "weibo-video")
        cacheDir.mkdirs()
        androidx.media3.datasource.cache.SimpleCache(
            cacheDir,
            androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor(200L * 1024 * 1024),
            androidx.media3.database.StandaloneDatabaseProvider(context),
        ).also { videoCache = it }
    }
}

private fun weiboDataSourceFactory(context: android.content.Context): androidx.media3.datasource.DataSource.Factory {
    val cookie = CookieManager.getInstance().getCookie("https://weibo.com/").orEmpty()
    val headers = buildMap {
        put("Accept", "*/*")
        put("Referer", "https://weibo.com/")
        put("Origin", "https://weibo.com")
        put("User-Agent", DESKTOP_CHROME_USER_AGENT)
        if (cookie.isNotBlank()) put("Cookie", cookie)
    }
    val httpFactory = androidx.media3.datasource.DefaultHttpDataSource.Factory()
        .setUserAgent(DESKTOP_CHROME_USER_AGENT)
        .setDefaultRequestProperties(headers)
        .setAllowCrossProtocolRedirects(true)
        .setConnectTimeoutMs(12_000)
        .setReadTimeoutMs(20_000)
    val upstream = androidx.media3.datasource.DefaultDataSource.Factory(context, httpFactory)
    return androidx.media3.datasource.cache.CacheDataSource.Factory()
        .setCache(getVideoCache(context))
        .setUpstreamDataSourceFactory(upstream)
        .setFlags(androidx.media3.datasource.cache.CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
}

private fun videoUrlCandidates(url: String): List<String> {
    val trimmed = url.trim()
    if (trimmed.isBlank()) return emptyList()
    if (trimmed.startsWith("http://", ignoreCase = true)) {
        return listOf(trimmed.replaceFirst("http://", "https://", ignoreCase = true), trimmed)
    }
    return listOf(trimmed)
}

private fun formatVideoTime(ms: Long): String {
    val totalSeconds = (ms / 1000L).coerceAtLeast(0L)
    val minutes = totalSeconds / 60L
    val seconds = totalSeconds % 60L
    return "$minutes:${seconds.toString().padStart(2, '0')}"
}

private fun speedLabel(speed: Float): String =
    if (speed == speed.toInt().toFloat()) "${speed.toInt()}x" else "${speed}x"

private const val DESKTOP_CHROME_USER_AGENT =
    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 " +
        "(KHTML, like Gecko) Chrome/126.0.0.0 Safari/537.36"
