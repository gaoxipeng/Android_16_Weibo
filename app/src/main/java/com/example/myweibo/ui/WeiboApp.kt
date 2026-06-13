package com.example.myweibo.ui

import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.graphics.drawable.AnimatedImageDrawable
import android.graphics.drawable.Drawable
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebView
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOut
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationState
import androidx.compose.animation.core.DecayAnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDecay
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.exponentialDecay
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.BoxScope
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.CardDefaults
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
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.border
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials
import dev.chrisbanes.haze.rememberHazeState
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.changedToUpIgnoreConsumed
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.zIndex
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.example.myweibo.R
import com.example.myweibo.VideoPipActivity
import com.example.myweibo.data.AlbumPage
import com.example.myweibo.data.CommentItem
import com.example.myweibo.data.CommentSort
import com.example.myweibo.data.CommentSortStore
import com.example.myweibo.data.EmoticonCacheStore
import com.example.myweibo.data.ImageSaveHelper
import com.example.myweibo.data.FeedImage
import com.example.myweibo.data.EditHistoryEntry
import com.example.myweibo.data.FeedItem
import com.example.myweibo.data.ProfileLookup
import com.example.myweibo.data.FeedMedia
import com.example.myweibo.data.MediaType
import com.example.myweibo.data.MineCacheStore
import com.example.myweibo.data.PlaybackSettingsStore
import com.example.myweibo.data.MinePostsCache
import com.example.myweibo.data.NativeUiMessage
import com.example.myweibo.data.StoredWeiboAccount
import com.example.myweibo.data.TimelineCacheStore
import com.example.myweibo.data.TimelineKind
import com.example.myweibo.data.UserProfile
import com.example.myweibo.data.WeiboStatusActions
import com.example.myweibo.data.WeiboAccountStore
import com.example.myweibo.data.WeiboJsonParser
import com.example.myweibo.data.WeiboWebSession
import com.example.myweibo.data.formatWeiboTime
import com.example.myweibo.ui.theme.StatusQuotedBackground
import com.example.myweibo.ui.theme.WeiboTopicBlue
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.net.HttpURLConnection
import java.net.URL
import java.nio.ByteBuffer
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

private enum class MainTab(val label: String) {
    Feed("\u9996\u9875"),
    Messages("\u6D88\u606F"),
    Search("\u641C\u7D22"),
    Compose("\u5199\u5FAE\u535A"),
    Mine("\u6211\u7684"),
}

private enum class MineContentTab(val label: String) {
    Posts("\u5FAE\u535A"),
    Album("\u76F8\u518C")
}

private fun feedRefreshHintMessage(
    previousItems: List<FeedItem>,
    refreshedItems: List<FeedItem>,
): String {
    val previousIds = previousItems.asSequence().map { it.statusId }.toSet()
    val newCount = refreshedItems.count { it.statusId !in previousIds }
    return if (newCount == 0) "\u6682\u65E0\u65B0\u5FAE\u535A" else "\u66F4\u65B0\u4E86 $newCount \u6761\u5FAE\u535A"
}

private fun likeFailureMessage(error: Throwable): String {
    val message = error.message.orEmpty()
    return if (
        message.contains("weibo-native-post-failed:400") ||
        message.contains("\u6587\u672C\u5185\u5BB9\u76F8\u540C") ||
        message.contains("\u8BF7\u969410\u5206\u949F")
    ) {
        "\u64CD\u4F5C\u592A\u9891\u7E41\uFF0C\u8BF7\u7A0D\u540E\u518D\u8BD5"
    } else {
        message.ifBlank { "\u8BF7\u7A0D\u540E\u91CD\u8BD5" }
    }
}

private fun lerpFloat(start: Float, stop: Float, fraction: Float): Float =
    start + (stop - start) * fraction

private val HintCapsuleWhite = Color.White
private val HintCapsuleText = Color(0xFF1F1F1F)
private val FeedRefreshIndicatorColor = Color(0xFF9E9E9E)

private class VideoPlaybackCoordinator {
    var activeKey by mutableStateOf<String?>(null)
    val positions = mutableStateMapOf<String, Long>()
    private val pauseHandlers = mutableSetOf<() -> Unit>()

    fun registerPauseHandler(handler: () -> Unit) {
        pauseHandlers += handler
    }

    fun unregisterPauseHandler(handler: () -> Unit) {
        pauseHandlers -= handler
    }

    fun pauseAll() {
        pauseHandlers.forEach { it() }
    }
}

private data class VisitedProfileSnapshot(
    val userId: String?,
    val screenName: String,
    val profile: UserProfile?,
    val posts: List<FeedItem>,
    val postsPage: Int,
    val postsHasMore: Boolean,
    val albumImages: List<FeedImage>,
    val albumNextCursor: String?,
    val albumHasMore: Boolean,
    val pagerPage: Int,
    val postsScrollIndex: Int = 0,
    val postsScrollOffset: Int = 0,
    val albumScrollIndex: Int = 0,
    val albumScrollOffset: Int = 0,
)

private data class AlbumViewerState(
    val images: List<FeedImage>,
    val initialIndex: Int,
    val statusCache: Map<String, FeedItem> = emptyMap(),
    val profilePagerPage: Int? = null,
    val albumScrollIndex: Int = 0,
    val albumScrollOffset: Int = 0,
)

private data class DetailSnapshot(
    val item: FeedItem,
    val comments: List<CommentItem>,
    val commentsCursor: String?,
    val commentsHasMore: Boolean,
    val commentSort: CommentSort,
    val scrollIndex: Int = 0,
    val scrollOffset: Int = 0,
    val albumViewerState: AlbumViewerState? = null,
)

private fun FeedImage.albumStatusCacheKey(): String =
    statusId?.takeIf { it.isNotBlank() } ?: largeUrl

private val LocalVideoPlaybackCoordinator = staticCompositionLocalOf { VideoPlaybackCoordinator() }
private val LocalUiMessenger = staticCompositionLocalOf<(String, String) -> Unit> { { _, _ -> } }
private val LocalHazeState = staticCompositionLocalOf<HazeState?> { null }

private data class VideoPeekRequest(
    val media: FeedMedia,
    val anchorBounds: Rect,
    val onCancel: () -> Unit,
    val onRelease: () -> Unit,
)

private class VideoPeekController {
    var activeRequest by mutableStateOf<VideoPeekRequest?>(null)

    fun open(request: VideoPeekRequest) {
        activeRequest = request
    }

    fun cancel() {
        val request = activeRequest
        activeRequest = null
        request?.onCancel?.invoke()
    }

    fun release() {
        val request = activeRequest
        activeRequest = null
        request?.onRelease?.invoke()
    }
}

private data class ImagePeekRequest(
    val image: FeedImage,
    val allImages: List<FeedImage>,
    val imageIndex: Int,
    val anchorBounds: Rect,
    val pressOffset: Offset,
    val onDismiss: () -> Unit,
)

private class ImagePeekController {
    var activeRequest by mutableStateOf<ImagePeekRequest?>(null)

    fun open(request: ImagePeekRequest) {
        activeRequest = request
    }

    fun dismiss() {
        activeRequest?.onDismiss?.invoke()
        activeRequest = null
    }
}

private val LocalImagePeekController = staticCompositionLocalOf { ImagePeekController() }
private val LocalVideoPeekController = staticCompositionLocalOf { VideoPeekController() }

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
    val emoticonCacheStore = remember { EmoticonCacheStore(context) }
    val accountStore = remember { WeiboAccountStore(context) }
    val commentSortStore = remember { CommentSortStore(context) }
    val playbackSettingsStore = remember { PlaybackSettingsStore(context) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val feedListState = rememberLazyListState()
    val minePostsListState = rememberLazyListState()
    val mineAlbumListState = rememberLazyListState()
    val visitedPostsListState = rememberLazyListState()
    val visitedAlbumListState = rememberLazyListState()
    val detailListState = rememberLazyListState()
    val videoPlaybackCoordinator = remember { VideoPlaybackCoordinator() }
    val profileHeaderHeights = remember { mutableStateMapOf<String, Dp>() }

    var selectedTab by remember { mutableStateOf(MainTab.Feed) }
    var bottomBarExpanded by remember { mutableStateOf(true) }
    var minePagerPage by remember { mutableStateOf(0) }
    var visitedMinePagerPage by remember { mutableStateOf(0) }
    var feedRefreshHint by remember { mutableStateOf<String?>(null) }
    val timelineKind = TimelineKind.Following
    var items by remember { mutableStateOf<List<FeedItem>>(emptyList()) }
    var nextCursor by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var selectedItem by remember { mutableStateOf<FeedItem?>(null) }
    var detailBackStack by remember { mutableStateOf<List<DetailSnapshot>>(emptyList()) }
    var albumViewerState by remember { mutableStateOf<AlbumViewerState?>(null) }
    var comments by remember { mutableStateOf<List<CommentItem>>(emptyList()) }
    var commentsLoading by remember { mutableStateOf(false) }
    var commentsCursor by remember { mutableStateOf<String?>(null) }
    var commentsHasMore by remember { mutableStateOf(true) }
    var nestedCommentsLoadingIds by remember { mutableStateOf(setOf<String>()) }
    var commentSort by remember { mutableStateOf(commentSortStore.read()) }
    var backgroundPlaybackEnabled by remember { mutableStateOf(playbackSettingsStore.readBackgroundPlaybackEnabled()) }
    var mediaPreview by remember { mutableStateOf<FeedMedia?>(null) }
    var message by remember { mutableStateOf<NativeUiMessage?>(null) }
    var hasLoginCookie by remember { mutableStateOf(session.hasLoginCookie()) }
    var storedAccounts by remember { mutableStateOf(accountStore.readAccounts()) }
    var activeAccountId by remember { mutableStateOf(accountStore.readActiveAccountId()) }
    var cacheLoaded by remember { mutableStateOf(false) }
    var expandedFeedItems by remember { mutableStateOf<Map<String, FeedItem>>(emptyMap()) }
    var longTextLoadingIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    var likeLoadingIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    var editHistoryItem by remember { mutableStateOf<FeedItem?>(null) }
    var editHistoryEntries by remember { mutableStateOf<List<EditHistoryEntry>>(emptyList()) }
    var editHistoryLoading by remember { mutableStateOf(false) }
    var editHistoryError by remember { mutableStateOf<String?>(null) }
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
    var mineAlbumLoading by remember { mutableStateOf(false) }
    var mineAlbumError by remember { mutableStateOf<String?>(null) }
    var mineAlbumJob by remember { mutableStateOf<Job?>(null) }
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
    var visitedAlbumNextCursor by remember { mutableStateOf<String?>(null) }
    var visitedAlbumHasMore by remember { mutableStateOf(false) }
    var visitedAlbumLoadingMore by remember { mutableStateOf(false) }
    var visitedAlbumLoading by remember { mutableStateOf(false) }
    var visitedAlbumError by remember { mutableStateOf<String?>(null) }
    var visitedAlbumJob by remember { mutableStateOf<Job?>(null) }
    var visitedFollowLoading by remember { mutableStateOf(false) }
    var visitedProfileLoadGeneration by remember { mutableIntStateOf(0) }
    var visitedListScrollResetGeneration by remember { mutableIntStateOf(-1) }
    var visitedProfileBackStack by remember { mutableStateOf<List<VisitedProfileSnapshot>>(emptyList()) }
    var lastHomeBackPressAt by remember { mutableStateOf(0L) }
    var exitHintVisible by remember { mutableStateOf(false) }
    var exitHintToken by remember { mutableIntStateOf(0) }
    var pendingOpenAccountLogin by remember { mutableStateOf(false) }

    fun openAccountLoginManagement() {
        selectedTab = MainTab.Mine
        pendingOpenAccountLogin = true
    }

    fun showMessage(title: String, detail: String) {
        message = NativeUiMessage(title, detail)
        scope.launch { snackbarHostState.showSnackbar("$title\uFF1A$detail") }
    }

    fun isLoginStateFailure(error: Throwable): Boolean {
        val message = error.message.orEmpty()
        return message.contains("未发现微博登录 Cookie") ||
            message.contains("登录未生效") ||
            message.contains("weibo-native-request-failed:401") ||
            message.contains("weibo-native-request-failed:403")
    }

    fun reloadStoredAccounts() {
        storedAccounts = accountStore.readAccounts()
        activeAccountId = accountStore.readActiveAccountId()
    }

    suspend fun persistLoginSession() {
        runCatching { session.persistCurrentAccount(accountStore) }
            .onSuccess {
                reloadStoredAccounts()
                hasLoginCookie = session.hasLoginCookie()
                mineHasLoginCookie = hasLoginCookie
            }
    }

    fun currentVisitedProfileSnapshot(): VisitedProfileSnapshot? {
        if (visitedUserId == null && visitedProfile == null) return null
        return VisitedProfileSnapshot(
            userId = visitedUserId,
            screenName = visitedUserScreenName,
            profile = visitedProfile,
            posts = visitedPosts,
            postsPage = visitedPostsPage,
            postsHasMore = visitedPostsHasMore,
            albumImages = visitedAlbumImages,
            albumNextCursor = visitedAlbumNextCursor,
            albumHasMore = visitedAlbumHasMore,
            pagerPage = visitedMinePagerPage,
            postsScrollIndex = visitedPostsListState.firstVisibleItemIndex,
            postsScrollOffset = visitedPostsListState.firstVisibleItemScrollOffset,
            albumScrollIndex = visitedAlbumListState.firstVisibleItemIndex,
            albumScrollOffset = visitedAlbumListState.firstVisibleItemScrollOffset,
        )
    }

    fun restoreVisitedProfileSnapshot(snapshot: VisitedProfileSnapshot) {
        visitedAlbumJob?.cancel()
        visitedProfileLoadGeneration += 1
        visitedListScrollResetGeneration = visitedProfileLoadGeneration
        visitedUserId = snapshot.userId
        visitedUserScreenName = snapshot.screenName
        visitedProfile = snapshot.profile
        visitedProfileLoading = false
        visitedPosts = snapshot.posts
        visitedPostsPage = snapshot.postsPage
        visitedPostsHasMore = snapshot.postsHasMore
        visitedPostsLoadingMore = false
        visitedAlbumImages = snapshot.albumImages
        visitedAlbumNextCursor = snapshot.albumNextCursor
        visitedAlbumHasMore = snapshot.albumHasMore
        visitedAlbumLoadingMore = false
        visitedAlbumLoading = false
        visitedAlbumError = null
        visitedMinePagerPage = snapshot.pagerPage
        scope.launch {
            var attempts = 0
            while (attempts < 24 && visitedPostsListState.layoutInfo.totalItemsCount == 0) {
                delay(16)
                attempts++
            }
            runCatching {
                visitedPostsListState.scrollToItem(snapshot.postsScrollIndex, snapshot.postsScrollOffset)
            }
            attempts = 0
            while (attempts < 24 && visitedAlbumListState.layoutInfo.totalItemsCount == 0) {
                delay(16)
                attempts++
            }
            runCatching {
                visitedAlbumListState.scrollToItem(snapshot.albumScrollIndex, snapshot.albumScrollOffset)
            }
        }
    }

    fun clearVisitedProfileState() {
        visitedAlbumJob?.cancel()
        visitedProfileLoadGeneration += 1
        visitedListScrollResetGeneration = -1
        visitedProfileBackStack = emptyList()
        visitedUserId = null
        visitedUserScreenName = ""
        visitedProfile = null
        visitedProfileLoading = false
        visitedPosts = emptyList()
        visitedPostsPage = 1
        visitedPostsHasMore = true
        visitedPostsLoadingMore = false
        visitedAlbumImages = emptyList()
        visitedAlbumNextCursor = null
        visitedAlbumHasMore = false
        visitedAlbumLoadingMore = false
        visitedAlbumLoading = false
        visitedAlbumError = null
        visitedMinePagerPage = 0
    }

    fun loadVisitedUserAlbum(uid: String, loadGeneration: Int) {
        visitedAlbumJob?.cancel()
        visitedAlbumJob = scope.launch {
            visitedAlbumLoading = true
            visitedAlbumError = null
            try {
                val page = session.loadUserAlbumImages(
                    uid = uid,
                    onPageLoaded = { images ->
                        if (loadGeneration == visitedProfileLoadGeneration && uid == visitedProfile?.id) {
                            visitedAlbumImages = images
                        }
                    },
                )
                if (loadGeneration == visitedProfileLoadGeneration && uid == visitedProfile?.id) {
                    visitedAlbumImages = page.images
                    visitedAlbumNextCursor = page.nextCursor
                    visitedAlbumHasMore = page.nextCursor != null
                    visitedAlbumError = if (page.images.isEmpty() && page.nextCursor == null) {
                        "\u76F8\u518C\u6682\u65E0\u56FE\u7247"
                    } else {
                        null
                    }
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Throwable) {
                if (loadGeneration == visitedProfileLoadGeneration) {
                    visitedAlbumError = error.message ?: "\u65E0\u6CD5\u8BFB\u53D6\u76F8\u518C"
                }
            } finally {
                if (loadGeneration == visitedProfileLoadGeneration) {
                    visitedAlbumLoading = false
                }
            }
        }
    }

    fun loadVisitedUserProfile(lookup: ProfileLookup, keepContent: Boolean = false) {
        visitedAlbumJob?.cancel()
        val loadGeneration = if (keepContent) {
            visitedProfileLoadGeneration
        } else {
            visitedProfileLoadGeneration + 1
        }
        if (!keepContent) {
            visitedProfileLoadGeneration = loadGeneration
        }

        if (!keepContent) {
            visitedProfile = null
            visitedPosts = emptyList()
            visitedPostsPage = 1
            visitedPostsHasMore = true
            visitedPostsLoadingMore = false
            visitedAlbumImages = emptyList()
            visitedAlbumNextCursor = null
            visitedAlbumHasMore = false
            visitedAlbumLoadingMore = false
            visitedAlbumLoading = false
            visitedAlbumError = null
            visitedMinePagerPage = 0

            when (lookup) {
                is ProfileLookup.Uid -> {
                    visitedUserId = lookup.uid
                    visitedUserScreenName = ""
                }
                is ProfileLookup.ScreenName -> {
                    visitedUserScreenName = lookup.screenName
                    visitedUserId = lookup.screenName
                }
            }
        }

        visitedProfileLoading = true

        scope.launch {
            try {
                runCatching { session.loadUserProfile(lookup) }
                    .onSuccess { profile ->
                        if (loadGeneration != visitedProfileLoadGeneration) return@onSuccess
                        visitedProfile = profile
                        visitedUserId = profile.id
                        visitedUserScreenName = profile.screenName
                        runCatching { session.loadUserTimeline(profile.id) }
                            .onSuccess { page ->
                                if (loadGeneration != visitedProfileLoadGeneration) return@onSuccess
                                visitedPosts = page.items
                                visitedPostsPage = 1
                                visitedPostsHasMore = page.nextCursor != null
                            }
                        loadVisitedUserAlbum(profile.id, loadGeneration)
                    }
                    .onFailure {
                        if (loadGeneration != visitedProfileLoadGeneration) return@onFailure
                        if (!keepContent) {
                            visitedProfile = null
                        }
                        showMessage("主页加载失败", "无法读取该用户资料，请稍后重试")
                    }
            } finally {
                if (loadGeneration == visitedProfileLoadGeneration) {
                    visitedProfileLoading = false
                }
            }
        }
    }

    fun currentDetailSnapshot(): DetailSnapshot? {
        val item = selectedItem ?: return null
        return DetailSnapshot(
            item = item,
            comments = comments,
            commentsCursor = commentsCursor,
            commentsHasMore = commentsHasMore,
            commentSort = commentSort,
            scrollIndex = detailListState.firstVisibleItemIndex,
            scrollOffset = detailListState.firstVisibleItemScrollOffset,
            albumViewerState = albumViewerState,
        )
    }

    fun restoreDetailSnapshot(snapshot: DetailSnapshot) {
        selectedItem = snapshot.item
        comments = snapshot.comments
        commentsCursor = snapshot.commentsCursor
        commentsHasMore = snapshot.commentsHasMore
        commentSort = snapshot.commentSort
        albumViewerState = snapshot.albumViewerState
        scope.launch {
            var attempts = 0
            while (attempts < 24 && detailListState.layoutInfo.totalItemsCount == 0) {
                delay(16)
                attempts++
            }
            runCatching {
                detailListState.scrollToItem(snapshot.scrollIndex, snapshot.scrollOffset)
            }
        }
    }

    fun pushCurrentDetailSnapshot() {
        currentDetailSnapshot()?.let { snapshot ->
            detailBackStack = detailBackStack + snapshot
        }
    }

    fun closeVisitedProfile() {
        val previous = visitedProfileBackStack.lastOrNull()
        if (detailBackStack.isNotEmpty()) {
            if (previous != null) {
                visitedProfileBackStack = visitedProfileBackStack.dropLast(1)
                restoreVisitedProfileSnapshot(previous)
            } else {
                clearVisitedProfileState()
            }
            val detail = detailBackStack.last()
            detailBackStack = detailBackStack.dropLast(1)
            restoreDetailSnapshot(detail)
            return
        }
        if (previous != null) {
            visitedProfileBackStack = visitedProfileBackStack.dropLast(1)
            restoreVisitedProfileSnapshot(previous)
        } else {
            clearVisitedProfileState()
        }
    }

    fun loadMoreVisitedPosts() {
        val uid = visitedUserId?.takeIf { it.all(Char::isDigit) } ?: return
        if (visitedPostsLoadingMore || visitedProfileLoading || !visitedPostsHasMore) return
        scope.launch {
            visitedPostsLoadingMore = true
            val next = visitedPostsPage + 1
            runCatching { session.loadUserTimeline(uid, next) }
                .onSuccess { page ->
                    visitedPosts = (visitedPosts + page.items).distinctBy { it.id }
                    visitedPostsPage = next
                    visitedPostsHasMore = page.nextCursor != null
                }
            visitedPostsLoadingMore = false
        }
    }

    fun loadMoreVisitedAlbum() {
        val uid = visitedUserId?.takeIf { it.all(Char::isDigit) } ?: return
        if (visitedAlbumLoadingMore || visitedProfileLoading || !visitedAlbumHasMore) return
        scope.launch {
            visitedAlbumLoadingMore = true
            val cursor = visitedAlbumNextCursor
            runCatching { session.loadUserAlbumImages(uid, cursor) }
                .onSuccess { page ->
                    visitedAlbumImages = (visitedAlbumImages + page.images).distinctBy { it.largeUrl }
                    visitedAlbumNextCursor = page.nextCursor
                    visitedAlbumHasMore = page.nextCursor != null
                }
            visitedAlbumLoadingMore = false
        }
    }

    fun openUser(idOrScreenName: String) {
        val value = idOrScreenName.trim().removePrefix("@")
        if (value.isBlank()) return
        val isCurrentUser = if (value.all { it.isDigit() }) {
            value == visitedProfile?.id || value == visitedUserId
        } else {
            value == visitedProfile?.screenName ||
                value == visitedUserScreenName ||
                value == visitedUserId
        }
        if (isCurrentUser) return

        if (selectedItem != null) {
            pushCurrentDetailSnapshot()
            selectedItem = null
        }

        currentVisitedProfileSnapshot()?.let { snapshot ->
            visitedProfileBackStack = visitedProfileBackStack + snapshot
        }
        loadVisitedUserProfile(
            if (value.all { it.isDigit() }) {
                ProfileLookup.Uid(value)
            } else {
                ProfileLookup.ScreenName(value)
            }
        )
    }

    fun applyProfilePagerPage(page: Int) {
        if (visitedUserId != null) {
            visitedMinePagerPage = page
        } else if (selectedTab == MainTab.Mine) {
            minePagerPage = page
        }
    }

    fun restoreProfilePagerFromViewer(viewer: AlbumViewerState) {
        viewer.profilePagerPage?.let(::applyProfilePagerPage)
    }

    fun restoreAlbumScrollFromViewer(viewer: AlbumViewerState) {
        val listState = when {
            visitedUserId != null -> visitedAlbumListState
            selectedTab == MainTab.Mine -> mineAlbumListState
            else -> null
        } ?: return
        scope.launch {
            var attempts = 0
            while (attempts < 24 && listState.layoutInfo.totalItemsCount == 0) {
                delay(16)
                attempts++
            }
            runCatching {
                listState.scrollToItem(viewer.albumScrollIndex, viewer.albumScrollOffset)
            }
        }
    }

    fun dismissAlbumViewer() {
        val viewer = albumViewerState ?: return
        restoreProfilePagerFromViewer(viewer)
        albumViewerState = null
        restoreAlbumScrollFromViewer(viewer)
    }

    fun closeDetail() {
        val previous = detailBackStack.lastOrNull()
        if (previous != null) {
            detailBackStack = detailBackStack.dropLast(1)
            restoreDetailSnapshot(previous)
        } else {
            selectedItem = null
            comments = emptyList()
            commentsCursor = null
            commentsHasMore = true
            albumViewerState?.let(::restoreProfilePagerFromViewer)
        }
    }

    fun closeEditHistory() {
        editHistoryItem = null
        editHistoryEntries = emptyList()
        editHistoryError = null
        editHistoryLoading = false
    }

    fun openEditHistory(item: FeedItem) {
        if (!item.isEdited) return
        editHistoryItem = item
        editHistoryEntries = emptyList()
        editHistoryError = null
        scope.launch {
            editHistoryLoading = true
            runCatching { session.loadEditHistory(item) }
                .onSuccess { entries ->
                    editHistoryEntries = entries
                    if (entries.isEmpty()) {
                        editHistoryError = "\u6682\u65E0\u7F16\u8F91\u8BB0\u5F55"
                    }
                }
                .onFailure { error ->
                    editHistoryError = error.message ?: "\u52A0\u8F7D\u5931\u8D25"
                }
            editHistoryLoading = false
        }
    }

    BackHandler(enabled = true) {
        when {
            editHistoryItem != null -> {
                lastHomeBackPressAt = 0L
                closeEditHistory()
            }
            mediaPreview != null -> {
                lastHomeBackPressAt = 0L
                mediaPreview = null
            }
            selectedItem != null -> {
                lastHomeBackPressAt = 0L
                closeDetail()
            }
            albumViewerState != null -> {
                lastHomeBackPressAt = 0L
                dismissAlbumViewer()
            }
            visitedUserId != null -> {
                lastHomeBackPressAt = 0L
                closeVisitedProfile()
            }
            selectedTab != MainTab.Feed -> {
                lastHomeBackPressAt = 0L
                selectedTab = MainTab.Feed
            }
            else -> {
                val now = System.currentTimeMillis()
                if (now - lastHomeBackPressAt <= 2_000L) {
                    (context as? android.app.Activity)?.finish()
                } else {
                    lastHomeBackPressAt = now
                    exitHintVisible = true
                    exitHintToken += 1
                }
            }
        }
    }

    fun refreshTimeline() {
        scope.launch {
            val previousItems = items
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
                    persistLoginSession()
                    if (page.items.isEmpty()) {
                        feedRefreshHint = null
                        showMessage("\u6CA1\u6709\u8BFB\u5230\u4FE1\u606F\u6D41", "\u8BF7\u5148\u5728\u6211\u7684\u9875\u9762\u8BBE\u7F6E\u4E2D\u767B\u5F55\u5FAE\u535A\uFF0C\u6216\u7A0D\u540E\u518D\u5237\u65B0")
                    } else {
                        feedRefreshHint = feedRefreshHintMessage(previousItems, page.items)
                    }
                }
                .onFailure { error ->
                    feedRefreshHint = null
                    if (isLoginStateFailure(error)) {
                        hasLoginCookie = false
                        mineHasLoginCookie = false
                    } else {
                        hasLoginCookie = session.hasLoginCookie()
                        mineHasLoginCookie = hasLoginCookie
                    }
                    showMessage("同步失败", error.message ?: "请确认已登录 weibo.com")
                }
            isLoading = false
            feedListState.animateScrollToItem(0)
        }
    }

    fun refreshTimelineFromTop() {
        scope.launch {
            val previousItems = items
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
                    persistLoginSession()
                    if (page.items.isEmpty()) {
                        feedRefreshHint = null
                        showMessage("\u6CA1\u6709\u8BFB\u5230\u4FE1\u606F\u6D41", "\u8BF7\u5148\u5728\u6211\u7684\u9875\u9762\u8BBE\u7F6E\u4E2D\u767B\u5F55\u5FAE\u535A\uFF0C\u6216\u7A0D\u540E\u518D\u5237\u65B0")
                    } else {
                        feedRefreshHint = feedRefreshHintMessage(previousItems, page.items)
                    }
                }
                .onFailure { error ->
                    feedRefreshHint = null
                    if (isLoginStateFailure(error)) {
                        hasLoginCookie = false
                        mineHasLoginCookie = false
                    } else {
                        hasLoginCookie = session.hasLoginCookie()
                        mineHasLoginCookie = hasLoginCookie
                    }
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
                            minePostsHasMore = page.nextCursor != null
                            minePostsError = null
                            mineCacheStore.writePosts(
                                MinePostsCache(
                                    page.items,
                                    page = 1,
                                    hasMore = page.nextCursor != null,
                                )
                            )
                        }
                        .onFailure {
                            minePostsError = it.message ?: "\u65E0\u6CD5\u8BFB\u53D6\u7528\u6237\u4E3B\u9875\u5FAE\u535A"
                        }
                    mineAlbumJob?.cancel()
                    mineAlbumJob = scope.launch {
                        mineAlbumLoading = true
                        mineAlbumError = null
                        try {
                            val page = session.loadUserAlbumImages(
                                uid = profile.id,
                                onPageLoaded = { images ->
                                    mineAlbumImages = filterOutRetweetedOnlyImages(images, minePosts)
                                },
                            )
                            mineAlbumImages = filterOutRetweetedOnlyImages(page.images, minePosts)
                            mineAlbumNextCursor = page.nextCursor
                            mineAlbumHasMore = page.nextCursor != null
                            mineAlbumError = if (page.images.isEmpty() && page.nextCursor == null) {
                                "\u76F8\u518C\u6682\u65E0\u56FE\u7247"
                            } else {
                                null
                            }
                            mineCacheStore.writeAlbum(page)
                        } catch (error: CancellationException) {
                            throw error
                        } catch (error: Throwable) {
                            mineAlbumError = error.message ?: "\u65E0\u6CD5\u8BFB\u53D6\u76F8\u518C"
                        } finally {
                            mineAlbumLoading = false
                        }
                    }
                }
                .onFailure {
                    mineProfileError = it.message ?: "\u65E0\u6CD5\u8BFB\u53D6\u5FAE\u535A\u7528\u6237\u8D44\u6599"
                }
            mineProfileLoading = false
        }
    }

    fun switchStoredAccount(accountId: String) {
        scope.launch {
            runCatching { session.activateAccount(accountStore, accountId) }
                .onSuccess {
                    reloadStoredAccounts()
                    hasLoginCookie = session.hasLoginCookie()
                    mineHasLoginCookie = hasLoginCookie
                    refreshTimeline()
                    refreshMineProfile()
                }
                .onFailure { error ->
                    showMessage("切换账号失败", error.message ?: "无法恢复该账号登录态")
                }
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
                    minePostsHasMore = page.nextCursor != null
                    minePostsError = null
                    mineCacheStore.writePosts(
                        MinePostsCache(
                            items = merged,
                            page = nextPage,
                            hasMore = page.nextCursor != null,
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
                    mineAlbumError = null
                    mineCacheStore.writeAlbum(
                        AlbumPage(
                            images = mineAlbumImages,
                            nextCursor = mineAlbumNextCursor,
                        )
                    )
                }
                .onFailure { error ->
                    mineAlbumError = error.message ?: "\u65E0\u6CD5\u7EE7\u7EED\u8BFB\u53D6\u76F8\u518C"
                }
            mineAlbumLoadingMore = false
        }
    }

    fun resolveFeedItem(item: FeedItem): FeedItem {
        var resolved = expandedFeedItems[item.statusId] ?: item
        val retweeted = resolved.retweetedStatus
        if (retweeted != null) {
            expandedFeedItems[retweeted.statusId]?.let { expandedRetweet ->
                resolved = resolved.copy(retweetedStatus = expandedRetweet)
            }
        }
        return resolved
    }

    fun mergeExpandedIntoItem(item: FeedItem, expanded: FeedItem): FeedItem =
        when {
            item.statusId == expanded.statusId -> expanded
            item.retweetedStatus?.statusId == expanded.statusId -> item.copy(retweetedStatus = expanded)
            else -> item
        }

    fun applyExpandedItem(expanded: FeedItem) {
        expandedFeedItems = expandedFeedItems + (expanded.statusId to expanded)
        items = items.map { mergeExpandedIntoItem(it, expanded) }
        minePosts = minePosts.map { mergeExpandedIntoItem(it, expanded) }
        visitedPosts = visitedPosts.map { mergeExpandedIntoItem(it, expanded) }
        selectedItem = selectedItem?.let { mergeExpandedIntoItem(it, expanded) }
        detailBackStack = detailBackStack.map { snapshot ->
            snapshot.copy(item = mergeExpandedIntoItem(snapshot.item, expanded))
        }
    }

    fun toggleStatusLike(item: FeedItem) {
        val likeId = item.id.trim().takeIf { it.isNotBlank() && it != "0" && it.all(Char::isDigit) }
            ?: return
        if (likeId in likeLoadingIds) return
        likeLoadingIds = likeLoadingIds + likeId
        val wasLiked = item.liked
        val delta = if (wasLiked) -1 else 1
        val updated = item.copy(
            liked = !wasLiked,
            likesCount = WeiboJsonParser.bumpDisplayCount(item.likesCount, delta),
        )
        applyExpandedItem(updated)
        scope.launch {
            runCatching {
                if (wasLiked) {
                    session.cancelStatusLike(likeId, item.statusId)
                } else {
                    session.setStatusLike(likeId, item.statusId)
                }
            }.onFailure { error ->
                applyExpandedItem(item)
                showMessage("\u70B9\u8D5E\u64CD\u4F5C\u5931\u8D25", likeFailureMessage(error))
            }
            likeLoadingIds = likeLoadingIds - likeId
        }
    }

    fun loadLongText(item: FeedItem) {
        if (!item.isLongText || item.statusId in longTextLoadingIds) return
        scope.launch {
            longTextLoadingIds = longTextLoadingIds + item.statusId
            runCatching { session.expandLongText(item) }
                .onSuccess { expanded ->
                    if (!expanded.isLongText) {
                        applyExpandedItem(expanded)
                    } else {
                        showMessage("\u5168\u6587\u52A0\u8F7D\u5931\u8D25", "\u5FAE\u535A\u957F\u6587\u5185\u5BB9\u4E3A\u7A7A")
                    }
                }
                .onFailure { error ->
                    showMessage("\u5168\u6587\u52A0\u8F7D\u5931\u8D25", error.message ?: "\u5FAE\u535A\u63A5\u53E3\u65E0\u54CD\u5E94")
                }
            longTextLoadingIds = longTextLoadingIds - item.statusId
        }
    }

    fun reloadComments() {
        val item = selectedItem ?: return
        scope.launch {
            commentsLoading = true
            commentsCursor = null
            commentsHasMore = true
            runCatching { session.loadComments(item, commentSort) }
                .onSuccess { page ->
                    comments = page.items
                    commentsCursor = page.nextCursor
                    commentsHasMore = page.nextCursor != null
                }
                .onFailure { error ->
                    showMessage("\u8BC4\u8BBA\u52A0\u8F7D\u5931\u8D25", error.message ?: "\u5FAE\u535A\u63A5\u53E3\u65E0\u54CD\u5E94")
                }
            commentsLoading = false
        }
    }

    fun openDetailInternal(item: FeedItem) {
        pushCurrentDetailSnapshot()
        val resolved = resolveFeedItem(item)
        selectedItem = resolved
        comments = emptyList()
        commentsCursor = null
        commentsHasMore = true
        scope.launch { runCatching { detailListState.scrollToItem(0) } }
        if (resolved.isLongText) {
            loadLongText(resolved)
        }
        resolved.retweetedStatus?.takeIf { it.isLongText }?.let { loadLongText(it) }
        reloadComments()
    }

    fun openDetail(item: FeedItem) {
        openDetailInternal(item)
    }

    fun openDetailFromSource(item: FeedItem, sourceBounds: Rect?) {
        openDetailInternal(item)
    }

    fun openDetailFromAlbumViewer(item: FeedItem, viewer: AlbumViewerState) {
        restoreProfilePagerFromViewer(viewer)
        albumViewerState = viewer
        openDetail(item)
    }

    fun toggleVisitedFollow() {
        val profile = visitedProfile ?: return
        val uid = profile.id.takeIf { it.isNotBlank() } ?: return
        if (uid == mineProfile?.id) return
        val wasFollowing = profile.following
        val nextFollowing = !wasFollowing
        visitedProfile = profile.copy(following = nextFollowing)
        scope.launch {
            visitedFollowLoading = true
            runCatching {
                if (wasFollowing) {
                    session.unfollowUser(uid, profile)
                } else {
                    session.followUser(uid, profile)
                }
            }.onSuccess { updated ->
                visitedProfile = updated
            }.onFailure { error ->
                visitedProfile = profile.copy(following = wasFollowing)
                showMessage("\u5173\u6CE8\u64CD\u4F5C\u5931\u8D25", error.message ?: "\u8BF7\u7A0D\u540E\u91CD\u8BD5")
            }
            visitedFollowLoading = false
        }
    }

    fun changeCommentSort(sort: CommentSort) {
        if (sort == commentSort) return
        commentSort = sort
        commentSortStore.write(sort)
        reloadComments()
    }

    fun loadMoreComments() {
        val item = selectedItem ?: return
        val cursor = commentsCursor ?: return
        if (commentsLoading || !commentsHasMore) return
        scope.launch {
            commentsLoading = true
            runCatching { session.loadMoreComments(item, cursor, commentSort) }
                .onSuccess { page ->
                    comments = comments + page.items
                    commentsCursor = page.nextCursor
                    commentsHasMore = page.nextCursor != null
                }
            commentsLoading = false
        }
    }

    fun expandNestedComments(commentId: String) {
        val item = selectedItem ?: return
        val authorUid = item.authorId.takeIf { it.isNotBlank() } ?: return
        if (commentId in nestedCommentsLoadingIds) return
        scope.launch {
            nestedCommentsLoadingIds = nestedCommentsLoadingIds + commentId
            runCatching { session.loadNestedComments(commentId, authorUid) }
                .onSuccess { page ->
                    comments = updateCommentTree(comments, commentId) { comment ->
                        comment.copy(
                            comments = page.items,
                            moreInfoText = null,
                        )
                    }
                }
                .onFailure { error ->
                    showMessage("\u697C\u4E2D\u697C\u5C55\u5F00\u5931\u8D25", error.message ?: "\u5FAE\u535A\u63A5\u53E3\u65E0\u54CD\u5E94")
                }
            nestedCommentsLoadingIds = nestedCommentsLoadingIds - commentId
        }
    }

    fun syncEmoticons() {
        if (emoticonSyncing) return
        scope.launch {
            emoticonSyncing = true
            runCatching { session.loadEmotions() }
                .onSuccess { emotions ->
                    if (emotions.isEmpty()) {
                        showMessage("表情同步失败", "微博未返回表情配置，请确认已登录后重试")
                    } else {
                        val synced = emotions.associate { it.phrase to it.url }
                        emoticonMap = synced
                        emoticonCacheStore.write(synced)
                        showMessage("表情同步完成", "已同步 ${synced.size} 个表情")
                    }
                }
                .onFailure { error ->
                    showMessage("表情同步失败", error.message ?: "无法读取微博表情配置")
                }
            emoticonSyncing = false
        }
    }

    LaunchedEffect(Unit) {
        val savedActiveId = accountStore.readActiveAccountId()
        if (savedActiveId != null && accountStore.getAccount(savedActiveId) != null) {
            runCatching { session.activateAccount(accountStore, savedActiveId) }
        } else if (session.hasLoginCookie()) {
            persistLoginSession()
        }
        reloadStoredAccounts()

        emoticonMap = emoticonCacheStore.read()
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

    val bottomBarListState: LazyListState? = when {
        selectedItem != null -> null
        visitedUserId != null -> null
        selectedTab == MainTab.Mine -> {
            if (minePagerPage == 0) minePostsListState else mineAlbumListState
        }
        selectedTab == MainTab.Feed -> feedListState
        else -> null
    }

    LaunchedEffect(selectedTab, visitedUserId) {
        bottomBarExpanded = true
    }

    LaunchedEffect(bottomBarListState) {
        val state = bottomBarListState ?: return@LaunchedEffect
        var lastScrollTotal =
            state.firstVisibleItemIndex * 10_000 + state.firstVisibleItemScrollOffset
        snapshotFlow {
            state.firstVisibleItemIndex * 10_000 + state.firstVisibleItemScrollOffset
        }.collect { scrollTotal ->
            if (scrollTotal > lastScrollTotal + 24) {
                bottomBarExpanded = false
            }
            lastScrollTotal = scrollTotal
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, backgroundPlaybackEnabled) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP && !backgroundPlaybackEnabled) {
                videoPlaybackCoordinator.pauseAll()
                videoPlaybackCoordinator.activeKey = null
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    CompositionLocalProvider(
        LocalVideoPlaybackCoordinator provides videoPlaybackCoordinator,
        LocalUiMessenger provides { title, detail -> showMessage(title, detail) },
    ) {
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
        onBack = { closeDetail() },
        onRefresh = { refreshTimeline() },
    ) { innerPadding ->
        val hazeState = rememberHazeState()
        val imagePeekController = remember { ImagePeekController() }
        val videoPeekController = remember { VideoPeekController() }
        CompositionLocalProvider(
            LocalHazeState provides hazeState,
            LocalImagePeekController provides imagePeekController,
            LocalVideoPeekController provides videoPeekController,
        ) {
            Box(Modifier.fillMaxSize()) {
            Box(Modifier.matchParentSize().hazeSource(state = hazeState)) {
            Box(Modifier.fillMaxSize().padding(innerPadding)) {
            val detailOverlayItem = selectedItem
            val feedUiOnTop = selectedTab == MainTab.Feed &&
                visitedUserId == null &&
                detailOverlayItem == null
            val keepFeedAlive = selectedTab == MainTab.Feed &&
                (visitedUserId != null || detailOverlayItem != null)
            val visitedProfileVisible = visitedUserId != null &&
                selectedItem == null &&
                albumViewerState == null
            val feedBackdropScale by animateFloatAsState(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = 0,
                    easing = FastOutSlowInEasing,
                ),
                label = "feedBackdropScale",
            )
            val feedVisibleAlpha by animateFloatAsState(
                targetValue = when {
                    feedUiOnTop -> 1f
                    else -> 0f
                },
                animationSpec = tween(
                    durationMillis = 0,
                    easing = FastOutSlowInEasing,
                ),
                label = "feedVisibleAlpha",
            )

            Box(Modifier.fillMaxSize()) {
                if (selectedTab == MainTab.Feed && (feedUiOnTop || keepFeedAlive)) {
                    Box(
                        Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                alpha = feedVisibleAlpha
                                scaleX = feedBackdropScale
                                scaleY = feedBackdropScale
                            },
                    ) {
                        FollowFeedScreen(
                            session = session,
                            listState = feedListState,
                            items = items,
                            isLoading = isLoading,
                            cacheLoaded = cacheLoaded,
                            hasLoginCookie = hasLoginCookie,
                            emoticonMap = emoticonMap,
                            onRefresh = { refreshTimeline() },
                            onLoadMore = { loadMore() },
                            onOpenLoginSettings = ::openAccountLoginManagement,
                            onUserClick = ::openUser,
                            onItemClick = { item, bounds -> openDetailFromSource(item, bounds) },
                            onMediaClick = { mediaPreview = it },
                            resolveFeedItem = ::resolveFeedItem,
                            isLongTextLoading = { it.statusId in longTextLoadingIds },
                            onLoadLongText = ::loadLongText,
                            onToggleLike = ::toggleStatusLike,
                            onOpenEditHistory = ::openEditHistory,
                        )
                    }
                }

                if (visitedUserId != null) {
                    LaunchedEffect(visitedProfileLoadGeneration) {
                        if (visitedListScrollResetGeneration == visitedProfileLoadGeneration) return@LaunchedEffect
                        visitedListScrollResetGeneration = visitedProfileLoadGeneration
                        runCatching {
                            visitedPostsListState.scrollToItem(0)
                            visitedAlbumListState.scrollToItem(0)
                        }
                    }
                    key(visitedProfileLoadGeneration) {
                        Box(
                            Modifier
                                .fillMaxSize()
                                .graphicsLayer { alpha = if (visitedProfileVisible) 1f else 0f },
                        ) {
                            MineScreen(
                                session = session,
                                profile = visitedProfile,
                                profileHeaderHeight = profileHeaderHeights[visitedUserId.orEmpty()] ?: 0.dp,
                                onProfileHeaderHeightChange = { height ->
                                    visitedUserId?.let { profileHeaderHeights[it] = height }
                                },
                                isLoading = visitedProfileLoading,
                                loadError = null,
                                hasLoginCookie = hasLoginCookie,
                                posts = visitedPosts,
                                postsError = null,
                                postsLoadingMore = visitedPostsLoadingMore,
                                albumImages = visitedAlbumImages,
                                albumLoading = visitedAlbumLoading,
                                albumLoadingMore = visitedAlbumLoadingMore,
                                postsHasMore = visitedPostsHasMore,
                                albumHasMore = visitedAlbumHasMore,
                                emoticonMap = emoticonMap,
                                emoticonCount = emoticonMap.size,
                                emoticonSyncing = emoticonSyncing,
                                postsListState = visitedPostsListState,
                                albumListState = visitedAlbumListState,
                                albumError = visitedAlbumError,
                                initialPagerPage = visitedMinePagerPage,
                                onMinePagerPageChanged = { visitedMinePagerPage = it },
                                onRefresh = {
                                    when {
                                        visitedProfile != null ->
                                            loadVisitedUserProfile(ProfileLookup.Uid(visitedProfile!!.id), keepContent = true)
                                        visitedUserId?.all(Char::isDigit) == true ->
                                            loadVisitedUserProfile(ProfileLookup.Uid(visitedUserId!!), keepContent = true)
                                        visitedUserScreenName.isNotBlank() ->
                                            loadVisitedUserProfile(
                                                ProfileLookup.ScreenName(visitedUserScreenName),
                                                keepContent = true,
                                            )
                                    }
                                },
                                onLoadMorePosts = { loadMoreVisitedPosts() },
                                onLoadMoreAlbum = { loadMoreVisitedAlbum() },
                                onSyncEmoticons = { syncEmoticons() },
                                onItemClick = ::openDetail,
                                onOpenAlbumViewer = { albumViewerState = it },
                                onMediaClick = { mediaPreview = it },
                                onUserClick = ::openUser,
                                isLongTextLoading = { it.statusId in longTextLoadingIds },
                                onLoadLongText = ::loadLongText,
                                onToggleLike = ::toggleStatusLike,
                                onOpenEditHistory = ::openEditHistory,
                                enableSettings = false,
                                showFollowActions = hasLoginCookie &&
                                    visitedProfile?.id?.isNotBlank() == true &&
                                    visitedProfile?.id != mineProfile?.id,
                                followLoading = visitedFollowLoading,
                                onFollowClick = { toggleVisitedFollow() },
                            )
                        }
                    }
                }

                if (visitedUserId == null && selectedItem == null) {
                    when (selectedTab) {
                        MainTab.Search -> PlaceholderScreen(
                            title = "\u641C\u7D22",
                            body = "\u641C\u7D22\u9875\u5C06\u63A5\u5165\u5FAE\u535A\u641C\u7D22\u63A5\u53E3\uFF0C\u7528\u4E8E\u67E5\u627E\u7528\u6237\u3001\u8BDD\u9898\u548C\u5FAE\u535A\u5185\u5BB9\u3002"
                        )

                        MainTab.Messages -> PlaceholderScreen(
                            title = "\u6D88\u606F",
                            body = "\u6D88\u606F\u9875\u5C06\u627F\u8F7D\u8BC4\u8BBA\u3001\u70B9\u8D5E\u3001\u63D0\u53CA\u548C\u79C1\u4FE1\u5165\u53E3\u3002"
                        )

                        MainTab.Mine -> {
                            LaunchedEffect(mineProfile) {
                                if (mineProfile == null && !mineProfileLoading) {
                                    refreshMineProfile()
                                }
                            }
                            MineScreen(
                                session = session,
                                profile = mineProfile,
                                profileHeaderHeight = profileHeaderHeights["mine-self"] ?: 0.dp,
                                onProfileHeaderHeightChange = { height ->
                                    profileHeaderHeights["mine-self"] = height
                                },
                                isLoading = mineProfileLoading,
                                loadError = mineProfileError,
                                hasLoginCookie = mineHasLoginCookie,
                                posts = minePosts,
                                postsError = minePostsError,
                                postsLoadingMore = minePostsLoadingMore,
                                albumImages = mineAlbumImages,
                                albumLoading = mineAlbumLoading,
                                albumLoadingMore = mineAlbumLoadingMore,
                                postsHasMore = minePostsHasMore,
                                albumHasMore = mineAlbumHasMore,
                                emoticonMap = emoticonMap,
                                emoticonCount = emoticonMap.size,
                                emoticonSyncing = emoticonSyncing,
                                postsListState = minePostsListState,
                                albumListState = mineAlbumListState,
                                albumError = mineAlbumError,
                                initialPagerPage = minePagerPage,
                                onMinePagerPageChanged = { minePagerPage = it },
                                onRefresh = { refreshMineProfile() },
                                onLoadMorePosts = { loadMoreMinePosts() },
                                onLoadMoreAlbum = { loadMoreMineAlbum() },
                                onSyncEmoticons = { syncEmoticons() },
                                onItemClick = ::openDetail,
                                onOpenAlbumViewer = { albumViewerState = it },
                                onMediaClick = { mediaPreview = it },
                                onUserClick = ::openUser,
                                isLongTextLoading = { it.statusId in longTextLoadingIds },
                                onLoadLongText = ::loadLongText,
                                onToggleLike = ::toggleStatusLike,
                                onOpenEditHistory = ::openEditHistory,
                                storedAccounts = storedAccounts,
                                activeAccountId = activeAccountId,
                                onSwitchAccount = ::switchStoredAccount,
                                onPrepareAddAccount = { session.prepareAddAccount() },
                                onPersistLoginSession = { persistLoginSession() },
                                onReturnToFeed = {
                                    scope.launch {
                                        persistLoginSession()
                                        session.openWeiboHome()
                                        selectedTab = MainTab.Feed
                                        refreshTimeline()
                                    }
                                },
                                pendingOpenAccountLogin = pendingOpenAccountLogin,
                                onPendingOpenAccountLoginConsumed = { pendingOpenAccountLogin = false },
                                backgroundPlaybackEnabled = backgroundPlaybackEnabled,
                                onBackgroundPlaybackChange = { enabled ->
                                    backgroundPlaybackEnabled = enabled
                                    playbackSettingsStore.writeBackgroundPlaybackEnabled(enabled)
                                },
                            )
                        }

                        MainTab.Compose -> PlaceholderScreen(
                            title = "\u5199\u5FAE\u535A",
                            body = "\u8FD9\u91CC\u540E\u7EED\u4F1A\u63A5\u5165\u5FAE\u535A\u53D1\u5E03\u63A5\u53E3\uFF0C\u652F\u6301\u539F\u751F\u7F16\u8F91\u548C\u53D1\u5E03\u3002"
                        )

                        MainTab.Feed -> Unit
                    }
                }

                AnimatedContent(
                    targetState = detailOverlayItem,
                    modifier = Modifier
                        .fillMaxSize()
                        .zIndex(1f),
                    transitionSpec = {
                        EnterTransition.None togetherWith ExitTransition.None
                    },
                    contentKey = { it?.id ?: "feed-detail-none" },
                    label = "feedDetailTransition",
                ) { detailItem ->
                    if (detailItem != null) {
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            color = MaterialTheme.colorScheme.background,
                        ) {
                            DetailScreen(
                                item = detailItem,
                                comments = comments,
                                commentSort = commentSort,
                                isLoadingComments = commentsLoading,
                                isLongTextLoading = { it.statusId in longTextLoadingIds },
                                onLoadLongText = ::loadLongText,
                                onToggleLike = ::toggleStatusLike,
                                onBack = { closeDetail() },
                                onRefresh = { reloadComments() },
                                onCommentSortChange = ::changeCommentSort,
                                onLoadMoreComments = { loadMoreComments() },
                                commentsHasMore = commentsHasMore,
                                listState = detailListState,
                                onMediaClick = { mediaPreview = it },
                                emoticonMap = emoticonMap,
                                onRetweetClick = ::openDetail,
                                onUserClick = ::openUser,
                                onExpandNestedComments = ::expandNestedComments,
                                nestedCommentsLoadingIds = nestedCommentsLoadingIds,
                                onOpenEditHistory = ::openEditHistory,
                            )
                        }
                    }
                }
            }

            if (selectedTab != MainTab.Mine && selectedItem == null) {
                HiddenSessionWebView(session)
            }

            if (selectedItem == null) {
                val feedRefreshTopInset = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
                AnimatedVisibility(
                    visible = selectedTab == MainTab.Feed &&
                        visitedUserId == null &&
                        feedRefreshHint != null,
                    enter = fadeIn(tween(220)) + slideInVertically(tween(220)) { fullHeight -> -fullHeight / 2 },
                    exit = fadeOut(tween(180)) + slideOutVertically(tween(180)) { fullHeight -> -fullHeight / 2 },
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = feedRefreshTopInset + 10.dp),
                ) {
                    feedRefreshHint?.let { hint ->
                        FeedRefreshCapsuleHint(
                            message = hint,
                            onDismiss = { feedRefreshHint = null },
                        )
                    }
                }
            }

            if (selectedItem == null && visitedUserId == null) {
                FloatingBottomBar(
                    selectedTab = selectedTab,
                    expanded = bottomBarExpanded,
                    hazeState = hazeState,
                    onExpandRequest = { bottomBarExpanded = true },
                    onCollapsedTap = {
                        when (selectedTab) {
                            MainTab.Feed -> refreshTimelineFromTop()
                            MainTab.Mine -> {
                                scope.launch {
                                    if (minePagerPage == 0) {
                                        minePostsListState.animateScrollToItem(0)
                                    } else {
                                        mineAlbumListState.animateScrollToItem(0)
                                    }
                                }
                                refreshMineProfile()
                            }
                            else -> Unit
                        }
                    },
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

            ExitConfirmCapsule(
                visible = exitHintVisible,
                token = exitHintToken,
                onDismiss = { exitHintVisible = false },
                modifier = Modifier.align(Alignment.Center),
            )
            }
            }
            imagePeekController.activeRequest?.let { request ->
                ImageActionOverlay(
                    image = request.image,
                    allImages = request.allImages,
                    initialImageIndex = request.imageIndex,
                    anchorBounds = request.anchorBounds,
                    pressOffset = request.pressOffset,
                    onDismiss = { imagePeekController.dismiss() },
                )
            }
            videoPeekController.activeRequest?.let { request ->
                VideoPeekOverlay(
                    media = request.media,
                    anchorBounds = request.anchorBounds,
                    onCancel = { videoPeekController.cancel() },
                )
            }
            }
            mediaPreview?.let { media ->
                Box(Modifier.fillMaxSize().zIndex(500f)) {
                    FullscreenMediaPreview(
                        media = media,
                        onDismiss = { mediaPreview = null },
                    )
                }
            }
            albumViewerState?.takeIf { selectedItem == null }?.let { viewer ->
                val relatedPosts = when {
                    visitedUserId != null -> visitedPosts
                    selectedTab == MainTab.Mine -> minePosts
                    else -> emptyList()
                }
                Box(Modifier.fillMaxSize().zIndex(500f)) {
                    FullscreenImageViewer(
                        images = viewer.images,
                        initialIndex = viewer.initialIndex,
                        onDismiss = { dismissAlbumViewer() },
                        session = session,
                        relatedPosts = relatedPosts,
                        emoticonMap = emoticonMap,
                        statusCache = viewer.statusCache,
                        onOpenStatus = { item, index, cache ->
                            openDetailFromAlbumViewer(
                                item,
                                AlbumViewerState(
                                    images = viewer.images,
                                    initialIndex = index,
                                    statusCache = cache,
                                    profilePagerPage = viewer.profilePagerPage,
                                    albumScrollIndex = viewer.albumScrollIndex,
                                    albumScrollOffset = viewer.albumScrollOffset,
                                ),
                            )
                        },
                    )
                }
            }
            editHistoryItem?.let { item ->
                Box(Modifier.fillMaxSize().zIndex(600f)) {
                    EditHistoryScreen(
                        item = item,
                        entries = editHistoryEntries,
                        isLoading = editHistoryLoading,
                        errorMessage = editHistoryError,
                        emoticonMap = emoticonMap,
                        onDismiss = ::closeEditHistory,
                    )
                }
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
                    expanded = true,
                    hazeState = rememberHazeState(),
                    onExpandRequest = {},
                    onCollapsedTap = {},
                    onTabChange = onTabChange,
                )
            }
        },
        content = content,
    )
}

@OptIn(ExperimentalHazeMaterialsApi::class)
@Composable
private fun FloatingBottomBar(
    selectedTab: MainTab,
    onTabChange: (MainTab) -> Unit,
    expanded: Boolean,
    hazeState: HazeState,
    onExpandRequest: () -> Unit,
    onCollapsedTap: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val glassShape = RoundedCornerShape(36.dp)
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    val collapsedWidth = 72.dp
    val collapsedWidthPx = with(density) { collapsedWidth.toPx() }
    val tabs = MainTab.entries
    var liquidActive by remember { mutableStateOf(false) }
    var gestureActive by remember { mutableStateOf(false) }
    var gesturePillXPx by remember { mutableFloatStateOf(0f) }
    var highlightedIndex by remember { mutableIntStateOf(-1) }
    var collapsedLongPressDragging by remember { mutableStateOf(false) }

    LaunchedEffect(selectedTab) {
        if (!gestureActive) {
            liquidActive = true
            delay(260)
            liquidActive = false
        }
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 18.dp, top = 4.dp, end = 18.dp, bottom = 24.dp),
        contentAlignment = Alignment.BottomStart,
    ) {
        val fullBarWidth = maxWidth
        val selectedIndex = tabs.indexOf(selectedTab).coerceAtLeast(0)
        val showExpandedChrome = expanded || gestureActive
        val animatedBarWidth by animateDpAsState(
            targetValue = if (expanded) fullBarWidth else collapsedWidth,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessMediumLow,
            ),
            label = "bottom-bar-width",
        )
        val barWidth = if (gestureActive) fullBarWidth else animatedBarWidth

        BoxWithConstraints(Modifier.width(fullBarWidth).height(72.dp)) {
            val barContentPadding = 8.dp
            val contentWidth = fullBarWidth - barContentPadding * 2
            val itemWidth = contentWidth / tabs.size
            val restingPillWidth = itemWidth
            val gesturePillWidth = itemWidth + 12.dp
            val maxWidthPx = with(density) { contentWidth.toPx() }.coerceAtLeast(1f)
            val horizontalPaddingPx = with(density) { barContentPadding.toPx() }
            val restingPillWidthPx = with(density) { restingPillWidth.toPx() }
            val gesturePillWidthPx = with(density) { gesturePillWidth.toPx() }
            val expandedState by rememberUpdatedState(expanded)
            val gestureActiveState by rememberUpdatedState(gestureActive)
            val onExpandRequestState by rememberUpdatedState(onExpandRequest)
            val onTabChangeState by rememberUpdatedState(onTabChange)
            val onCollapsedTapState by rememberUpdatedState(onCollapsedTap)

            fun contentX(rawX: Float) = (rawX - horizontalPaddingPx).coerceIn(0f, maxWidthPx)

            fun indexForX(rawX: Float): Int {
                val x = contentX(rawX)
                return ((x / maxWidthPx) * tabs.size).toInt().coerceIn(0, tabs.lastIndex)
            }

            fun pillOffsetPxForX(rawX: Float, activePillWidthPx: Float = gesturePillWidthPx): Float {
                val x = contentX(rawX)
                val maxOffset = (maxWidthPx - activePillWidthPx).coerceAtLeast(0f)
                return (x - activePillWidthPx / 2f).coerceIn(0f, maxOffset)
            }

            fun pillOffsetPxForIndex(index: Int, activePillWidthPx: Float = restingPillWidthPx): Float {
                val slotStart = with(density) { (itemWidth * index).toPx() }
                val slotCenter = slotStart + with(density) { itemWidth.toPx() } / 2f
                val maxOffset = (maxWidthPx - activePillWidthPx).coerceAtLeast(0f)
                return (slotCenter - activePillWidthPx / 2f).coerceIn(0f, maxOffset)
            }

            fun updateGesturePill(rawX: Float) {
                gesturePillXPx = pillOffsetPxForX(rawX)
                highlightedIndex = indexForX(rawX)
            }

            fun resetGesture() {
                gestureActive = false
                collapsedLongPressDragging = false
                highlightedIndex = -1
                liquidActive = false
            }

            fun finishGesture(targetIndex: Int, requestExpand: Boolean = false) {
                highlightedIndex = -1
                collapsedLongPressDragging = false
                liquidActive = true
                if (requestExpand) {
                    onExpandRequestState()
                }
                onTabChangeState(tabs[targetIndex])
                scope.launch {
                    delay(120)
                    gestureActive = false
                }
                scope.launch {
                    delay(260)
                    liquidActive = false
                }
            }

            val animatedPillOffset by animateDpAsState(
                targetValue = with(density) { pillOffsetPxForIndex(selectedIndex).toDp() },
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMediumLow,
                ),
                label = "selected-nav-pill",
            )
            val pillWidthDp = if (gestureActive) gesturePillWidth else restingPillWidth
            val pillHeightDp = if (gestureActive) 62.dp else 58.dp
            val pillOffset = if (gestureActive) {
                with(density) { gesturePillXPx.toDp() }
            } else {
                animatedPillOffset
            }
            val highlightedTab = highlightedIndex.takeIf { it >= 0 }?.let { tabs[it] }

            Surface(
                modifier = Modifier.width(barWidth).fillMaxHeight(),
                color = Color.Transparent,
                shadowElevation = 0.dp,
                shape = glassShape,
                border = BorderStroke(
                    width = 0.5.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.26f),
                            Color.White.copy(alpha = 0.10f),
                            Color(0xFFD4D4D4).copy(alpha = 0.12f),
                        )
                    )
                ),
            ) {
                Box(
                    modifier = Modifier
                        .width(fullBarWidth)
                        .fillMaxHeight(),
                ) {
                    FrostedBarBackground(
                        shape = glassShape,
                        hazeState = hazeState,
                        modifier = Modifier.matchParentSize(),
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clip(glassShape)
                            .padding(horizontal = barContentPadding),
                    ) {
                    if (showExpandedChrome) {
                        LiquidSelectedPill(
                            active = liquidActive || gestureActive,
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .offset(x = pillOffset)
                                .width(pillWidthDp)
                                .height(pillHeightDp),
                        )

                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            tabs.forEach { tab ->
                                val isTouchingSelectedTab = gestureActive && highlightedTab == selectedTab
                                FloatingNavItem(
                                    tab = tab,
                                    selected = selectedTab == tab &&
                                        (!gestureActive || highlightedTab == null || isTouchingSelectedTab),
                                    highlighted = gestureActive &&
                                        highlightedTab != selectedTab &&
                                        highlightedTab == tab,
                                    modifier = Modifier.weight(1f),
                                )
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .width(collapsedWidth)
                                .fillMaxHeight(),
                            contentAlignment = Alignment.Center,
                        ) {
                            LiquidSelectedPill(
                                active = false,
                                modifier = Modifier.size(width = 58.dp, height = 58.dp),
                            )
                            FloatingNavItem(
                                tab = selectedTab,
                                selected = true,
                                highlighted = false,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .matchParentSize()
                    .pointerInput(maxWidthPx, collapsedWidthPx) {
                        detectTapGestures(
                            onTap = { offset ->
                                if (!expandedState && !gestureActiveState && offset.x <= collapsedWidthPx) {
                                    onExpandRequestState()
                                }
                            },
                            onDoubleTap = { offset ->
                                if (!expandedState && !gestureActiveState && offset.x <= collapsedWidthPx) {
                                    onCollapsedTapState()
                                }
                            },
                        )
                    }
                    .pointerInput(maxWidthPx, gesturePillWidthPx, collapsedWidthPx) {
                        detectDragGesturesAfterLongPress(
                            onDragStart = { offset ->
                                if (expandedState) return@detectDragGesturesAfterLongPress
                                if (offset.x > collapsedWidthPx) return@detectDragGesturesAfterLongPress
                                collapsedLongPressDragging = true
                                gestureActive = true
                                liquidActive = true
                                onExpandRequestState()
                                updateGesturePill(offset.x)
                            },
                            onDrag = { change, _ ->
                                if (!collapsedLongPressDragging) return@detectDragGesturesAfterLongPress
                                updateGesturePill(change.position.x)
                            },
                            onDragEnd = {
                                if (!collapsedLongPressDragging) return@detectDragGesturesAfterLongPress
                                val targetIndex = highlightedIndex
                                    .takeIf { it >= 0 }
                                    ?: selectedIndex
                                finishGesture(targetIndex, requestExpand = true)
                            },
                            onDragCancel = {
                                if (collapsedLongPressDragging) {
                                    collapsedLongPressDragging = false
                                    gestureActive = false
                                    highlightedIndex = -1
                                    liquidActive = false
                                }
                            },
                        )
                    },
            )

            if (expanded && !collapsedLongPressDragging) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .pointerInput(maxWidthPx, gesturePillWidthPx) {
                            awaitEachGesture {
                                val down = awaitFirstDown(requireUnconsumed = false)
                                var targetIndex = indexForX(down.position.x)
                                gestureActive = true
                                updateGesturePill(down.position.x)
                                down.consume()

                                while (true) {
                                    val event = awaitPointerEvent()
                                    val change = event.changes.firstOrNull { it.id == down.id }
                                        ?: event.changes.firstOrNull()
                                        ?: break
                                    targetIndex = indexForX(change.position.x)
                                    updateGesturePill(change.position.x)
                                    change.consume()
                                    if (!change.pressed) {
                                        break
                                    }
                                }

                                finishGesture(targetIndex)
                            }
                        },
                )
            }
        }
    }
}

@OptIn(ExperimentalHazeMaterialsApi::class)
@Composable
private fun FrostedBarBackground(
    shape: RoundedCornerShape,
    hazeState: HazeState,
    modifier: Modifier = Modifier,
) {
    val surface = MaterialTheme.colorScheme.surface
    Box(modifier = modifier.clip(shape)) {
        Box(
            Modifier
                .matchParentSize()
                .hazeEffect(state = hazeState, style = HazeMaterials.ultraThin()) {
                    blurRadius = 24.dp
                    noiseFactor = 0.03f
                    alpha = 0.62f
                }
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.14f),
                            Color.White.copy(alpha = 0.08f),
                            surface.copy(alpha = 0.05f),
                        ),
                    ),
                ),
        )
        Box(
            Modifier
                .matchParentSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.05f),
                            Color.Transparent,
                            Color(0xFFE4E4E4).copy(alpha = 0.03f),
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(500f, 260f),
                    ),
                ),
        )
        Canvas(Modifier.fillMaxSize()) {
            drawRoundRect(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.10f),
                        Color.White.copy(alpha = 0.02f),
                        Color.Transparent,
                    ),
                    center = Offset(size.width * 0.5f, size.height * 0.08f),
                    radius = size.width * 0.72f,
                ),
                cornerRadius = CornerRadius(size.height / 2f, size.height / 2f),
            )
        }
    }
}

@Composable
private fun OpaqueHintCapsule(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val shape = RoundedCornerShape(22.dp)
    Surface(
        modifier = modifier,
        shape = shape,
        color = HintCapsuleWhite,
        shadowElevation = 0.dp,
        border = BorderStroke(
            1.dp,
            Color(0xFFE6E6E6),
        ),
    ) {
        content()
    }
}

@Composable
private fun FeedRefreshCapsuleHint(
    message: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LaunchedEffect(message) {
        delay(2200)
        onDismiss()
    }

    OpaqueHintCapsule(modifier = modifier) {
        Text(
            text = message,
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = HintCapsuleText,
        )
    }
}

@Composable
private fun ExitConfirmCapsule(
    visible: Boolean,
    token: Int,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LaunchedEffect(token) {
        if (visible) {
            delay(1800)
            onDismiss()
        }
    }

    AnimatedVisibility(
        visible = visible,
        modifier = modifier.zIndex(80f),
        enter = fadeIn(tween(120)) + scaleIn(
            initialScale = 0.92f,
            animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        ),
        exit = fadeOut(tween(140)) + scaleOut(targetScale = 0.94f),
    ) {
        OpaqueHintCapsule {
            Text(
                text = "再按一次退出程序",
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = HintCapsuleText,
            )
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
private fun AppPullToRefreshBox(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    val state = rememberPullToRefreshState()
    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        modifier = modifier,
        state = state,
        indicator = {
            PullToRefreshDefaults.Indicator(
                modifier = Modifier.align(Alignment.TopCenter),
                isRefreshing = isRefreshing,
                state = state,
                containerColor = HintCapsuleWhite,
                color = FeedRefreshIndicatorColor,
            )
        },
        content = content,
    )
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
    onItemClick: (FeedItem, Rect?) -> Unit,
    onMediaClick: (FeedMedia) -> Unit,
    resolveFeedItem: (FeedItem) -> FeedItem = { it },
    isLongTextLoading: (FeedItem) -> Boolean = { false },
    onLoadLongText: ((FeedItem) -> Unit)? = null,
    onToggleLike: ((FeedItem) -> Unit)? = null,
    onOpenEditHistory: ((FeedItem) -> Unit)? = null,
) {
    val topInset = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    AppPullToRefreshBox(
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
                val resolved = resolveFeedItem(item)
                var cardBounds by remember(resolved.id) { mutableStateOf<Rect?>(null) }
                FeedCard(
                    item = resolved,
                    onClick = { onItemClick(resolved, cardBounds) },
                    onBoundsChange = { cardBounds = it },
                    onMediaClick = onMediaClick,
                    emoticonMap = emoticonMap,
                    onUserClick = onUserClick,
                    onRetweetClick = { retweeted -> onItemClick(retweeted, cardBounds) },
                    isLongTextLoading = isLongTextLoading,
                    onLoadLongText = onLoadLongText,
                    onToggleLike = onToggleLike,
                    onOpenEditHistory = onOpenEditHistory,
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
private fun EmoticonText(
    text: String,
    emoticonMap: Map<String, String>,
    style: TextStyle,
    modifier: Modifier = Modifier,
    onUserClick: ((String) -> Unit)? = null,
    leadingAuthorName: String? = null,
    onLeadingAuthorClick: (() -> Unit)? = null,
    trailingLabel: String? = null,
    onTrailingClick: (() -> Unit)? = null,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
) {
    if (text.isBlank() && trailingLabel == null && leadingAuthorName == null) {
        Text(text = "\u65E0\u6B63\u6587", style = style, modifier = modifier)
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

    // Reserve inline slots for emoticons that also look like @mentions.
    val lineH = style.lineHeight.takeIf { it != TextUnit.Unspecified } ?: style.fontSize * 1.5f
    emoticonMap.keys.filter { it.startsWith("@") }.forEach { token ->
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
            )
        }
    }

    val annotatedString = buildAnnotatedString {
        leadingAuthorName?.let { authorName ->
            val authorLabel = "@$authorName\uFF1A"
            val authorStyle = SpanStyle(
                color = primaryColor,
                fontWeight = FontWeight.Medium,
                textDecoration = TextDecoration.None,
            )
            if (onLeadingAuthorClick != null) {
                withLink(
                    LinkAnnotation.Clickable(
                        tag = "quoted-author:$authorName",
                        linkInteractionListener = { onLeadingAuthorClick() },
                    ),
                ) {
                    withStyle(authorStyle) {
                        append(authorLabel)
                    }
                }
            } else {
                withStyle(authorStyle) {
                    append(authorLabel)
                }
            }
        }
        var last = 0
        Regex("""\[[^\[\]]+\]|@[\p{L}\p{N}_-]+|#[^#\n]+#""").findAll(text).forEach { match ->
            if (match.range.first > last) {
                append(text.substring(last, match.range.first))
            }
            val token = match.value
            when {
                emoticonMap.containsKey(token) && inlineContent.containsKey(token) -> {
                    appendInlineContent(token, token)
                }
                token.startsWith("@") -> {
                    val screenName = token.removePrefix("@")
                    if (onUserClick != null) {
                        withLink(
                            LinkAnnotation.Clickable(
                                tag = "mention:$screenName",
                                linkInteractionListener = { onUserClick(screenName) },
                            ),
                        ) {
                            withStyle(
                                SpanStyle(
                                    color = primaryColor,
                                    fontWeight = FontWeight.Medium,
                                    textDecoration = TextDecoration.None,
                                ),
                            ) {
                                append(token)
                            }
                        }
                    } else {
                        withStyle(SpanStyle(color = primaryColor, fontWeight = FontWeight.Medium)) {
                            append(token)
                        }
                    }
                }
                token.startsWith("#") && token.endsWith("#") -> {
                    withStyle(SpanStyle(color = WeiboTopicBlue)) {
                        append(token)
                    }
                }
                else -> append(token)
            }
            last = match.range.last + 1
        }
        if (last < text.length) {
            append(text.substring(last))
        }
        trailingLabel?.let { label ->
            append('\u00A0')
            val trailingStyle = SpanStyle(
                color = if (onTrailingClick != null) primaryColor else primaryColor.copy(alpha = 0.6f),
                fontSize = style.fontSize,
                textDecoration = TextDecoration.None,
            )
            if (onTrailingClick != null) {
                withLink(
                    LinkAnnotation.Clickable(
                        tag = "read-full-text",
                        linkInteractionListener = { onTrailingClick() },
                    ),
                ) {
                    withStyle(trailingStyle) {
                        append(label)
                    }
                }
            } else {
                withStyle(trailingStyle) {
                    append(label)
                }
            }
        }
    }

    Text(
        text = annotatedString,
        inlineContent = inlineContent,
        style = style,
        modifier = modifier,
        maxLines = maxLines,
        overflow = overflow,
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
    isLongTextLoading: (FeedItem) -> Boolean = { false },
    onLoadLongText: ((FeedItem) -> Unit)? = null,
    onToggleLike: ((FeedItem) -> Unit)? = null,
    showAuthorRow: Boolean = true,
    onOpenEditHistory: ((FeedItem) -> Unit)? = null,
    onBoundsChange: (Rect) -> Unit = {},
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = 12.dp,
                end = 12.dp,
                top = if (showAuthorRow) 6.dp else 0.dp,
                bottom = 6.dp,
            )
            .onGloballyPositioned { coordinates ->
                onBoundsChange(coordinates.boundsInWindow())
            },
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = Color.White,
        ),
    ) {
        val resolvedEmoticonMap = item.emoticons.ifEmpty { emoticonMap }
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            if (showAuthorRow) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top,
                ) {
                    Box(
                        Modifier
                            .weight(1f)
                            .clickable(onClick = onClick),
                    ) {
                        AuthorRow(
                            item = item,
                            onUserClick = onUserClick,
                            avatarClickable = true,
                        )
                    }
                    FeedCardActionMenu(
                        item = item,
                        onOpenEditHistory = onOpenEditHistory,
                    )
                }
            }
            Column(
                modifier = Modifier.clickable(onClick = onClick),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                StatusTextSection(
                    item = item,
                    emoticonMap = resolvedEmoticonMap,
                    style = MaterialTheme.typography.bodyMedium,
                    onUserClick = onUserClick,
                    isLongTextLoading = isLongTextLoading(item),
                    onLoadLongText = onLoadLongText,
                )
            }
            item.retweetedStatus?.let { retweeted ->
                QuotedStatus(
                    item = retweeted,
                    onMediaClick = onMediaClick,
                    emoticonMap = resolvedEmoticonMap,
                    onClick = onRetweetClick?.let { cb -> { cb(retweeted) } },
                    onUserClick = onUserClick,
                    isLongTextLoading = isLongTextLoading(retweeted),
                    onLoadLongText = onLoadLongText,
                )
            }
            MediaStrip(
                images = item.images,
                media = item.media,
                onMediaClick = onMediaClick,
            )
            StatusActions(
                item = item,
                onCommentClick = onClick,
                onToggleLike = onToggleLike?.let { toggle -> { toggle(item) } },
            )
        }
    }
}

@Composable
private fun StatusTextSection(
    item: FeedItem,
    emoticonMap: Map<String, String>,
    style: TextStyle,
    onUserClick: ((String) -> Unit)?,
    isLongTextLoading: Boolean,
    onLoadLongText: ((FeedItem) -> Unit)?,
    leadingAuthorName: String? = null,
    onLeadingAuthorClick: (() -> Unit)? = null,
) {
    EmoticonText(
        modifier = Modifier.fillMaxWidth(),
        text = item.text,
        emoticonMap = emoticonMap,
        style = style,
        onUserClick = onUserClick,
        leadingAuthorName = leadingAuthorName,
        onLeadingAuthorClick = onLeadingAuthorClick,
        trailingLabel = if (item.isLongText && onLoadLongText != null) {
            if (isLongTextLoading) "\u52A0\u8F7D\u5168\u6587..." else "\u9605\u8BFB\u5168\u6587"
        } else {
            null
        },
        onTrailingClick = if (item.isLongText && onLoadLongText != null && !isLongTextLoading) {
            { onLoadLongText(item) }
        } else {
            null
        },
    )
}

@Composable
private fun QuotedStatus(
    item: FeedItem,
    onMediaClick: (FeedMedia) -> Unit,
    emoticonMap: Map<String, String> = emptyMap(),
    onClick: (() -> Unit)? = null,
    onUserClick: ((String) -> Unit)? = null,
    isLongTextLoading: Boolean = false,
    onLoadLongText: ((FeedItem) -> Unit)? = null,
) {
    val resolvedMap = item.emoticons.ifEmpty { emoticonMap }
    val userTarget = item.authorId.takeIf { it.isNotBlank() } ?: item.authorName
    Surface(
        color = StatusQuotedBackground,
        shape = RoundedCornerShape(8.dp),
    ) {
        Column(
            Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Column(
                modifier = if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                StatusTextSection(
                    item = item,
                    emoticonMap = resolvedMap,
                    style = MaterialTheme.typography.bodyMedium,
                    onUserClick = onUserClick,
                    isLongTextLoading = isLongTextLoading,
                    onLoadLongText = onLoadLongText,
                    leadingAuthorName = item.authorName,
                    onLeadingAuthorClick = if (userTarget.isNotBlank() && onUserClick != null) {
                        { onUserClick(userTarget) }
                    } else {
                        null
                    },
                )
            }
            MediaStrip(images = item.images, media = item.media, onMediaClick = onMediaClick)
        }
    }
}

@OptIn(ExperimentalHazeMaterialsApi::class)
@Composable
private fun CommentSortActionMenu(
    selected: CommentSort,
    onSelected: (CommentSort) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val density = LocalDensity.current
    val haptic = LocalHapticFeedback.current

    fun dismissMenu() {
        expanded = false
    }

    if (expanded) {
        BackHandler { dismissMenu() }
    }

    val metaColor = weiboMetaTextColor()
    Box {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
                        expanded = !expanded
                    },
                )
                .padding(horizontal = 4.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = selected.label,
                style = MaterialTheme.typography.bodySmall,
                color = metaColor,
            )
            SettingsExpandIndicator(
                expanded = expanded,
                modifier = Modifier.size(16.dp),
                tint = metaColor,
            )
        }

        if (expanded) {
            Popup(
                alignment = Alignment.TopEnd,
                offset = IntOffset(0, with(density) { 30.dp.roundToPx() }),
                onDismissRequest = { dismissMenu() },
                properties = PopupProperties(focusable = true),
            ) {
                ImageActionFrostedCard(modifier = Modifier.width(112.dp)) {
                    CommentSort.entries.forEachIndexed { index, sort ->
                        if (index > 0) {
                            ImageActionMenuDivider()
                        }
                        ImageActionRow(
                            label = sort.label,
                            enabled = true,
                            selected = sort == selected,
                            onClick = {
                                dismissMenu()
                                if (sort != selected) {
                                    onSelected(sort)
                                }
                            },
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalHazeMaterialsApi::class)
@Composable
private fun FeedCardActionMenu(
    item: FeedItem,
    onOpenEditHistory: ((FeedItem) -> Unit)? = null,
) {
    var expanded by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val density = LocalDensity.current
    val haptic = LocalHapticFeedback.current
    val shareUrl = remember(item.id, item.statusId, item.authorId) {
        WeiboStatusActions.weiboUrl(item)
    }

    fun dismissMenu() {
        expanded = false
    }

    if (expanded) {
        BackHandler { dismissMenu() }
    }

    Box {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(8.dp))
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
                        expanded = !expanded
                    },
                ),
            contentAlignment = Alignment.Center,
        ) {
            SettingsExpandIndicator(
                expanded = expanded,
                modifier = Modifier.size(18.dp),
                tint = weiboMetaTextColor(),
            )
        }

        if (expanded) {
            Popup(
                alignment = Alignment.TopEnd,
                offset = IntOffset(0, with(density) { 34.dp.roundToPx() }),
                onDismissRequest = { dismissMenu() },
                properties = PopupProperties(focusable = true),
            ) {
                ImageActionFrostedCard(modifier = Modifier.width(140.dp)) {
                    ImageActionRow(
                        label = "跳转到微博",
                        enabled = shareUrl != null,
                        onClick = {
                            dismissMenu()
                            WeiboStatusActions.openInWeiboApp(context, item)
                        },
                    )
                    ImageActionMenuDivider()
                    ImageActionRow(
                        label = "分享",
                        enabled = shareUrl != null,
                        onClick = {
                            dismissMenu()
                            WeiboStatusActions.shareLink(context, item)
                        },
                    )
                    if (item.isEdited) {
                        ImageActionMenuDivider()
                        ImageActionRow(
                            label = "编辑历史",
                            enabled = onOpenEditHistory != null,
                            onClick = {
                                dismissMenu()
                                onOpenEditHistory?.invoke(item)
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AuthorRow(
    item: FeedItem,
    onUserClick: ((String) -> Unit)? = null,
    avatarClickable: Boolean = false,
) {
    val metadataText = listOfNotNull(
        formatWeiboTime(item.createdAt),
        item.source?.takeIf { it.isNotBlank() }?.let { "\u6765\u81EA $it" },
        "\u5DF2\u7F16\u8F91".takeIf { item.isEdited },
        item.ipLocation,
    ).joinToString(" ")

    val uid = item.authorId
    val userTarget = uid.takeIf { it.isNotBlank() } ?: item.authorName
    Row(verticalAlignment = Alignment.CenterVertically) {
        RemoteImage(
            url = item.authorAvatarUrl,
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .clickable(
                    enabled = avatarClickable && onUserClick != null && userTarget.isNotBlank(),
                    onClick = { onUserClick?.invoke(userTarget) },
                ),
            contentScale = ContentScale.Crop,
        )
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = item.authorName,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.clickable(
                    enabled = onUserClick != null && userTarget.isNotBlank(),
                    onClick = { onUserClick?.invoke(userTarget) },
                ),
            )
            if (metadataText.isNotBlank()) {
                Text(
                    text = metadataText,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
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
private fun EditHistoryScreen(
    item: FeedItem,
    entries: List<EditHistoryEntry>,
    isLoading: Boolean,
    errorMessage: String?,
    emoticonMap: Map<String, String>,
    onDismiss: () -> Unit,
) {
    BackHandler { onDismiss() }
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false),
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.78f),
            shape = RoundedCornerShape(18.dp),
            color = Color.White,
            tonalElevation = 0.dp,
            shadowElevation = 8.dp,
        ) {
            Column(Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 22.dp, end = 10.dp, top = 14.dp, bottom = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "微博编辑记录",
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = if (item.authorName.isNotBlank()) " ${item.authorName}" else "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(42.dp),
                    ) {
                        Text(
                            text = "×",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
                if (false) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 22.dp, end = 10.dp, top = 14.dp, bottom = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("返回")
                    }
                    Text(
                        text = if (item.authorName.isNotBlank()) {
                            "${item.authorName} · 编辑历史"
                        } else {
                            "编辑历史"
                        },
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                }
                HorizontalDivider(
                    thickness = 0.5.dp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.12f),
                )
                when {
                    isLoading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center,
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                    errorMessage != null && entries.isEmpty() -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = errorMessage,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            itemsIndexed(entries) { index, entry ->
                                EditHistoryEntryCard(
                                    entry = entry,
                                    versionIndex = entries.size - index,
                                    emoticonMap = emoticonMap,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EditHistoryEntryCard(
    entry: EditHistoryEntry,
    versionIndex: Int,
    emoticonMap: Map<String, String>,
) {
    val timeLabel = formatWeiboTime(entry.editedAt ?: entry.createdAt)
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = buildString {
                    append("版本 $versionIndex")
                    if (timeLabel.isNotBlank()) {
                        append(" · ")
                        append(timeLabel)
                    }
                },
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f),
            )
            StatusTextSection(
                item = FeedItem(
                    id = entry.id,
                    statusId = entry.id,
                    authorId = "",
                    authorName = "",
                    authorAvatarUrl = null,
                    createdAt = entry.createdAt,
                    source = null,
                    ipLocation = null,
                    text = entry.text,
                    repostsCount = "0",
                    commentsCount = "0",
                    likesCount = "0",
                    images = entry.images,
                    media = null,
                ),
                emoticonMap = emoticonMap,
                style = MaterialTheme.typography.bodyMedium,
                onUserClick = null,
                isLongTextLoading = false,
                onLoadLongText = null,
            )
            if (entry.images.isNotEmpty()) {
                MediaStrip(
                    images = entry.images,
                    media = null,
                    onMediaClick = {},
                )
            }
        }
    }
}

private fun singleImageDisplayAspectRatio(width: Int, height: Int): Float {
    if (width <= 0 || height <= 0) return 1f
    val naturalAspect = width.toFloat() / height
    val heightToWidth = height.toFloat() / width
    if (heightToWidth <= 1.35f) {
        return naturalAspect.coerceIn(0.75f, 3f)
    }
    val minAspect = when {
        heightToWidth <= 2f -> 0.62f
        heightToWidth <= 3f -> 0.72f
        heightToWidth <= 5f -> 0.82f
        else -> 0.9f
    }
    return naturalAspect.coerceAtLeast(minAspect).coerceAtMost(3f)
}

@Composable
private fun FeedImageCell(
    image: FeedImage,
    allImages: List<FeedImage>,
    imageIndex: Int = 0,
    modifier: Modifier = Modifier,
    previewUrl: String? = null,
    contentScale: ContentScale = ContentScale.Crop,
    showLiveBadge: Boolean = true,
    onOpenViewer: () -> Unit,
) {
    var actionOpen by remember(image.id) { mutableStateOf(false) }
    var peekActive by remember(image.id) { mutableStateOf(false) }
    var longPressTriggered by remember(image.id) { mutableStateOf(false) }
    var anchorBounds by remember(image.id) { mutableStateOf<Rect?>(null) }
    var longPressOffset by remember(image.id) { mutableStateOf<Offset?>(null) }
    val imagePeekController = LocalImagePeekController.current
    val haptic = LocalHapticFeedback.current
    val peekScale by animateFloatAsState(
        targetValue = if (peekActive) 0.96f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium,
        ),
        label = "feed-image-peek-scale",
    )

    LaunchedEffect(longPressTriggered) {
        if (!longPressTriggered) return@LaunchedEffect
        delay(220)
        if (!longPressTriggered) return@LaunchedEffect
        val bounds = anchorBounds ?: return@LaunchedEffect
        val localPressOffset = longPressOffset ?: Offset(bounds.width / 2f, bounds.height / 2f)
        val pressOffset = Offset(bounds.left + localPressOffset.x, bounds.top + localPressOffset.y)
        actionOpen = true
        imagePeekController.open(
            ImagePeekRequest(
                image = image,
                allImages = allImages,
                imageIndex = imageIndex,
                anchorBounds = bounds,
                pressOffset = pressOffset,
                onDismiss = {
                    actionOpen = false
                    peekActive = false
                    longPressTriggered = false
                    longPressOffset = null
                },
            ),
        )
    }

    Box(
        modifier = modifier
            .zIndex(if (actionOpen || peekActive) 10f else 0f)
            .onGloballyPositioned { coordinates ->
                anchorBounds = coordinates.boundsInWindow()
            }
            .graphicsLayer {
                scaleX = peekScale
                scaleY = peekScale
                alpha = if (actionOpen) 0f else 1f
                clip = false
            }
            .clip(RoundedCornerShape(4.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
            .pointerInput(image.id) {
                detectTapGestures(
                    onTap = { onOpenViewer() },
                    onLongPress = { offset ->
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        longPressOffset = offset
                        longPressTriggered = true
                        peekActive = true
                    },
                    onPress = {
                        tryAwaitRelease()
                        if (!actionOpen) {
                            peekActive = false
                            longPressTriggered = false
                            longPressOffset = null
                        }
                    },
                )
            },
    ) {
        RemoteImage(
            url = previewUrl ?: image.largeUrl,
            modifier = Modifier.fillMaxSize(),
            contentScale = contentScale,
            animated = image.isGif,
        )
        if (showLiveBadge && (image.isLivePhoto || image.isGif)) {
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(4.dp),
                shape = CircleShape,
                color = Color.Black.copy(alpha = 0.42f),
                contentColor = Color.White,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_live_photo),
                    contentDescription = if (image.isGif) "GIF" else "LivePhoto",
                    modifier = Modifier
                        .padding(2.dp)
                        .size(11.dp),
                    tint = Color.White,
                )
            }
        }
    }

}

private val ActionMenuCornerRadius = 14.dp
private val ActionMenuRowHeight = 40.dp
private val ActionMenuPaddingHorizontal = 12.dp
private val ActionMenuPaddingVertical = 0.dp
private val ActionMenuDividerThickness = 0.5.dp
private val ActionMenuThreeRowHeight =
    ActionMenuPaddingVertical * 2 + ActionMenuRowHeight * 3 + ActionMenuDividerThickness * 2
private val ActionMenuTwoRowHeight =
    ActionMenuPaddingVertical * 2 + ActionMenuRowHeight * 2 + ActionMenuDividerThickness

@Composable
private fun actionMenuTextStyle(selected: Boolean = false): TextStyle =
    MaterialTheme.typography.bodyMedium.copy(
        fontSize = 15.sp,
        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
        platformStyle = PlatformTextStyle(includeFontPadding = false),
    )

@OptIn(ExperimentalHazeMaterialsApi::class)
@Composable
private fun ImageActionFrostedCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val hazeState = LocalHazeState.current
    val shape = RoundedCornerShape(ActionMenuCornerRadius)
    Surface(
        modifier = modifier,
        shape = shape,
        color = Color.Transparent,
        shadowElevation = 0.dp,
        tonalElevation = 0.dp,
    ) {
        Box(
            Modifier
                .clip(shape)
                .border(0.6.dp, Color.Black.copy(alpha = 0.10f), shape),
        ) {
            if (hazeState != null) {
                Box(
                    Modifier
                        .matchParentSize()
                        .hazeEffect(state = hazeState, style = HazeMaterials.ultraThin()) {
                            blurRadius = 86.dp
                            noiseFactor = 0.04f
                            alpha = 1f
                        },
                )
            }
            Box(
                Modifier
                    .matchParentSize()
                    .background(Color(0xFFF2F2F3).copy(alpha = 0.74f))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.42f),
                                Color(0xFFF1F1F2).copy(alpha = 0.28f),
                                Color(0xFFD9D9DC).copy(alpha = 0.16f),
                            ),
                        ),
                    ),
            )
            Column(
                Modifier.padding(vertical = ActionMenuPaddingVertical),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(0.dp),
                content = content,
            )
        }
    }
}

@Composable
private fun ImageActionOverlay(
    image: FeedImage,
    allImages: List<FeedImage>,
    initialImageIndex: Int,
    anchorBounds: Rect,
    pressOffset: Offset,
    onDismiss: () -> Unit,
) {
    val onMessage = LocalUiMessenger.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    var saving by remember { mutableStateOf(false) }
    val images = allImages.ifEmpty { listOf(image) }
    val safeInitialIndex = initialImageIndex.coerceIn(0, images.lastIndex)
    val pagerState = rememberPagerState(initialPage = safeInitialIndex) { images.size }
    val currentImage = images[pagerState.currentPage]
    val enterProgress = remember { Animatable(0f) }
    val dragY = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        enterProgress.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessMediumLow,
            ),
        )
    }

    fun dismissOverlay() {
        scope.launch {
            enterProgress.animateTo(0f, tween(180))
            onDismiss()
        }
    }

    val progress = enterProgress.value
    val dragFade = (dragY.value / 300f).coerceIn(0f, 0.9f)
    val scrimAlpha = (0.18f * progress) * (1f - dragFade)

    BackHandler { dismissOverlay() }
    BoxWithConstraints(
        Modifier
            .fillMaxSize()
            .zIndex(300f),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = scrimAlpha))
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = { dismissOverlay() },
                ),
        )

            val previewAspect = feedImagePreviewAspectRatio(currentImage)
            val previewWidth = minOf(maxWidth * 0.88f, 340.dp)
            val previewHeight = previewWidth / previewAspect
            val targetLeft = (maxWidth - previewWidth) / 2
            val targetTop = maxHeight * 0.18f
            val anchorLeft = with(density) { anchorBounds.left.toDp() }
            val anchorTop = with(density) { anchorBounds.top.toDp() }
            val anchorWidth = with(density) { anchorBounds.width.toDp().coerceAtLeast(1.dp) }
            val anchorHeight = with(density) { anchorBounds.height.toDp().coerceAtLeast(1.dp) }
            val previewLeft = anchorLeft + (targetLeft - anchorLeft) * progress
            val previewTop = anchorTop + (targetTop - anchorTop) * progress + with(density) { dragY.value.toDp() }
            val previewW = anchorWidth + (previewWidth - anchorWidth) * progress
            val previewH = anchorHeight + (previewHeight - anchorHeight) * progress
            val previewCorner = 4.dp + (18.dp - 4.dp) * progress
            val previewAlpha = (1f - dragFade) * progress.coerceIn(0f, 1f)
            val previewShape = RoundedCornerShape(previewCorner)
            val dragOffsetY = with(density) { dragY.value.toDp() }
            val actualPreviewTop = previewTop + dragOffsetY
            val previewBounds = Rect(
                left = with(density) { previewLeft.toPx() },
                top = with(density) { actualPreviewTop.toPx() },
                right = with(density) { (previewLeft + previewW).toPx() },
                bottom = with(density) { (actualPreviewTop + previewH).toPx() },
            )
            val menuWidth = 180.dp
            val menuHeight = ActionMenuThreeRowHeight
            val menuPlacement = calculateActionMenuOffsetFromAnchorPx(
                anchorBounds = previewBounds,
                screenWidthPx = with(density) { maxWidth.toPx() },
                screenHeightPx = with(density) { maxHeight.toPx() },
                menuWidthPx = with(density) { menuWidth.toPx() },
                menuHeightPx = with(density) { menuHeight.toPx() },
                marginPx = with(density) { 14.dp.toPx() },
                gapPx = with(density) { 10.dp.toPx() },
            )

            Column(
                modifier = Modifier
                    .offset(x = previewLeft, y = actualPreviewTop)
                    .width(previewW)
                    .graphicsLayer { alpha = previewAlpha },
            ) {
                Box(
                    modifier = Modifier
                        .size(previewW, previewH)
                        .shadow(
                            elevation = (18f * progress).dp,
                            shape = previewShape,
                            clip = false,
                        )
                        .clip(previewShape)
                        .background(Color.Black)
                        .pointerInput(Unit) {
                            detectVerticalDragGestures(
                                onVerticalDrag = { _, amount ->
                                    scope.launch {
                                        dragY.snapTo((dragY.value + amount).coerceAtLeast(0f))
                                    }
                                },
                                onDragEnd = {
                                    if (dragY.value > 140f) {
                                        dismissOverlay()
                                    } else {
                                        scope.launch {
                                            dragY.animateTo(
                                                targetValue = 0f,
                                                animationSpec = spring(stiffness = Spring.StiffnessMedium),
                                            )
                                        }
                                    }
                                },
                            )
                        },
                ) {
                    if (images.size > 1) {
                        HorizontalPager(
                            state = pagerState,
                            modifier = Modifier.fillMaxSize(),
                        ) { page ->
                            ImageActionPreviewImage(
                                image = images[page],
                                modifier = Modifier.fillMaxSize(),
                            )
                        }
                    } else {
                        ImageActionPreviewImage(
                            image = currentImage,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }

                    if (images.size > 1) {
                        Row(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 10.dp),
                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                        ) {
                            images.forEachIndexed { index, _ ->
                                Box(
                                    modifier = Modifier
                                        .size(if (index == pagerState.currentPage) 7.dp else 6.dp)
                                        .clip(CircleShape)
                                        .background(
                                            Color.White.copy(
                                                alpha = if (index == pagerState.currentPage) 0.95f else 0.42f,
                                            ),
                                        ),
                                )
                            }
                        }
                    }
                }
            }

            AnimatedVisibility(
                visible = progress > 0.55f,
                enter = fadeIn(tween(160)) + slideInVertically(tween(180)) {
                    if (menuPlacement.belowAnchor) -it / 6 else it / 6
                },
                exit = fadeOut(tween(140)) + slideOutVertically(tween(160)) {
                    if (menuPlacement.belowAnchor) -it / 6 else it / 6
                },
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset { menuPlacement.offset }
                    .width(menuWidth),
            ) {
                ImageActionFrostedCard {
                    ImageActionRow(
                        label = "保存",
                        enabled = !saving,
                        onClick = {
                            saving = true
                            scope.launch {
                                ImageSaveHelper.saveImage(context, currentImage)
                                    .onSuccess { name ->
                                        onMessage("保存成功", "已保存到相册：$name")
                                        dismissOverlay()
                                    }
                                    .onFailure { error ->
                                        onMessage("保存失败", error.message ?: "请稍后重试")
                                    }
                                saving = false
                            }
                        },
                    )
                    ImageActionMenuDivider()
                    ImageActionRow(
                        label = "保存全部",
                        enabled = !saving && images.isNotEmpty(),
                        onClick = {
                            saving = true
                            scope.launch {
                                ImageSaveHelper.saveAllImages(context, images)
                                    .onSuccess { count ->
                                        onMessage("保存成功", "已保存 $count 张图片到相册")
                                        dismissOverlay()
                                    }
                                    .onFailure { error ->
                                        onMessage("保存失败", error.message ?: "请稍后重试")
                                    }
                                saving = false
                            }
                        },
                    )
                    ImageActionMenuDivider()
                    ImageActionRow(
                        label = "分享",
                        enabled = !saving,
                        onClick = {
                            saving = true
                            scope.launch {
                                ImageSaveHelper.shareImage(context, currentImage)
                                    .onFailure { error ->
                                        onMessage("分享失败", error.message ?: "请稍后重试")
                                    }
                                saving = false
                            }
                        },
                    )
                }
            }
    }
}

private fun feedImagePreviewAspectRatio(image: FeedImage): Float {
    val width = image.width ?: 0
    val height = image.height ?: 0
    if (width > 0 && height > 0) {
        return (width.toFloat() / height.toFloat()).coerceIn(0.45f, 2.2f)
    }
    return 1f
}

@Composable
private fun VideoPeekOverlay(
    media: FeedMedia,
    anchorBounds: Rect,
    onCancel: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    var aspectRatio by remember(media.streamUrl) { mutableStateOf(16f / 9f) }
    val enterProgress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        enterProgress.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessMediumLow,
            ),
        )
    }

    fun cancelOverlay() {
        scope.launch {
            enterProgress.animateTo(0f, tween(180))
            onCancel()
        }
    }

    BackHandler { cancelOverlay() }
    BoxWithConstraints(
        Modifier
            .fillMaxSize()
            .zIndex(300f),
    ) {
        val progress = enterProgress.value
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(Color.Black.copy(alpha = 0.16f * progress))
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = { cancelOverlay() },
                ),
        )

        val previewWidth = minOf(maxWidth * 0.92f, 360.dp)
        val previewHeight = previewWidth / aspectRatio.coerceIn(0.56f, 1.78f)
        val targetLeft = (maxWidth - previewWidth) / 2
        val targetTop = maxHeight * 0.18f
        val anchorLeft = with(density) { anchorBounds.left.toDp() }
        val anchorTop = with(density) { anchorBounds.top.toDp() }
        val anchorWidth = with(density) { anchorBounds.width.toDp().coerceAtLeast(1.dp) }
        val anchorHeight = with(density) { anchorBounds.height.toDp().coerceAtLeast(1.dp) }
        val previewLeft = anchorLeft + (targetLeft - anchorLeft) * progress
        val previewTop = anchorTop + (targetTop - anchorTop) * progress
        val previewW = anchorWidth + (previewWidth - anchorWidth) * progress
        val previewH = anchorHeight + (previewHeight - anchorHeight) * progress
        val previewCorner = 4.dp + (18.dp - 4.dp) * progress

        Box(
            modifier = Modifier
                .offset(x = previewLeft, y = previewTop)
                .size(previewW, previewH)
                .graphicsLayer { alpha = progress.coerceIn(0f, 1f) }
                .shadow(
                    elevation = (18f * progress).dp,
                    shape = RoundedCornerShape(previewCorner),
                    clip = false,
                )
                .clip(RoundedCornerShape(previewCorner))
                .background(Color.Black),
        ) {
            WeiboVideoSurface(
                media = media,
                isFullscreen = false,
                controlsEnabled = false,
                resumePosition = true,
                savePositionOnDispose = true,
                onAspectRatio = { aspectRatio = it },
                onFullscreen = {},
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Composable
private fun ImageActionPreviewImage(
    image: FeedImage,
    modifier: Modifier = Modifier,
) {
    if (image.isGif) {
        AnimatedRemoteImage(
            url = image.downloadUrls.firstOrNull { it.isNotBlank() } ?: image.largeUrl,
            modifier = modifier,
            contentScale = ContentScale.Fit,
        )
    } else {
        RemoteImage(
            url = image.largeUrl.ifBlank { image.thumbnailUrl },
            fallbackUrls = image.downloadUrls.filter { it.isNotBlank() },
            modifier = modifier,
            contentScale = ContentScale.Fit,
        )
    }
}

private data class ActionMenuPlacement(
    val offset: IntOffset,
    val belowAnchor: Boolean,
)

private fun calculateActionMenuOffsetFromPointPx(
    pressOffset: Offset,
    screenWidthPx: Float,
    screenHeightPx: Float,
    menuWidthPx: Float,
    menuHeightPx: Float,
    marginPx: Float,
    gapPx: Float,
): ActionMenuPlacement {
    val maxX = (screenWidthPx - menuWidthPx - marginPx).coerceAtLeast(marginPx)
    val x = (pressOffset.x - menuWidthPx / 2f).coerceIn(marginPx, maxX)
    val showBelow = pressOffset.y + gapPx + menuHeightPx <= screenHeightPx - marginPx
    val targetY = if (showBelow) {
        pressOffset.y + gapPx
    } else {
        pressOffset.y - gapPx - menuHeightPx
    }
    val maxY = (screenHeightPx - menuHeightPx - marginPx).coerceAtLeast(marginPx)
    val y = targetY.coerceIn(marginPx, maxY)
    return ActionMenuPlacement(IntOffset(x.roundToInt(), y.roundToInt()), showBelow)
}

private fun calculateActionMenuOffsetFromAnchorPx(
    anchorBounds: Rect,
    screenWidthPx: Float,
    screenHeightPx: Float,
    menuWidthPx: Float,
    menuHeightPx: Float,
    marginPx: Float,
    gapPx: Float,
): ActionMenuPlacement {
    val maxX = (screenWidthPx - menuWidthPx - marginPx).coerceAtLeast(marginPx)
    val x = (anchorBounds.center.x - menuWidthPx / 2f).coerceIn(marginPx, maxX)
    val hasSpaceBelow = anchorBounds.bottom + gapPx + menuHeightPx <= screenHeightPx - marginPx
    val targetY = if (hasSpaceBelow) {
        anchorBounds.bottom + gapPx
    } else {
        anchorBounds.top - gapPx - menuHeightPx
    }
    val maxY = (screenHeightPx - menuHeightPx - marginPx).coerceAtLeast(marginPx)
    val y = targetY.coerceIn(marginPx, maxY)
    return ActionMenuPlacement(IntOffset(x.roundToInt(), y.roundToInt()), hasSpaceBelow)
}

@Composable
private fun ImageActionMenuDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = ActionMenuPaddingHorizontal),
        thickness = ActionMenuDividerThickness,
        color = Color.Black.copy(alpha = 0.08f),
    )
}

@Composable
private fun ImageActionRow(
    label: String,
    enabled: Boolean,
    selected: Boolean = false,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(ActionMenuRowHeight)
            .clickable(enabled = enabled, onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            modifier = Modifier
                .fillMaxHeight()
                .wrapContentHeight(Alignment.CenterVertically)
                .padding(horizontal = ActionMenuPaddingHorizontal),
            style = actionMenuTextStyle(selected = selected),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = when {
                !enabled -> Color(0x661C1C1E)
                selected -> MaterialTheme.colorScheme.primary
                else -> Color(0xFF1C1C1E)
            },
        )
    }
}

@Composable
private fun FeedImagePreviewContent(
    image: FeedImage,
    livePlaying: Boolean,
    onLiveEnded: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.background(Color.Black),
        contentAlignment = Alignment.Center,
    ) {
        if (image.isGif) {
            AnimatedRemoteImage(
                url = image.downloadUrls.firstOrNull { it.isNotBlank() } ?: image.largeUrl,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit,
            )
        } else {
            RemoteImage(
                url = image.largeUrl,
                fallbackUrls = image.downloadUrls.drop(1),
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit,
            )
        }
        if (image.isLivePhoto && livePlaying) {
            LivePhotoOverlay(
                image = image,
                modifier = Modifier.fillMaxSize(),
                onEnded = onLiveEnded,
            )
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
                            FeedImageCell(
                                image = image,
                                allImages = images,
                                imageIndex = images.indexOf(image),
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(
                                        if (images.size == 1) {
                                            singleImageDisplayAspectRatio(
                                                width = image.width ?: 0,
                                                height = image.height ?: 0,
                                            )
                                        } else {
                                            1f
                                        }
                                    ),
                                contentScale = if (images.size == 1) ContentScale.FillWidth else ContentScale.Crop,
                                onOpenViewer = {
                                    viewerIndex = images.indexOf(image)
                                    viewerOpen = true
                                },
                            )
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
            InlineVideoPlayer(
                media = media,
                onClick = { onMediaClick(media) },
                onFullscreenRequest = { onMediaClick(media) },
            )
        }
    }
}

@Composable
private fun FullscreenImageViewer(
    images: List<FeedImage>,
    initialIndex: Int,
    onDismiss: () -> Unit,
    session: WeiboWebSession? = null,
    relatedPosts: List<FeedItem> = emptyList(),
    emoticonMap: Map<String, String> = emptyMap(),
    statusCache: Map<String, FeedItem> = emptyMap(),
    onOpenStatus: ((FeedItem, Int, Map<String, FeedItem>) -> Unit)? = null,
) {
    val pagerState = rememberPagerState(pageCount = { images.size }, initialPage = initialIndex)
    val pagerFling = PagerDefaults.flingBehavior(
        state = pagerState,
        decayAnimationSpec = exponentialDecay(frictionMultiplier = 0.82f),
    )
    val scope = rememberCoroutineScope()
    var blockPagerScroll by remember { mutableStateOf(false) }
    val showStatusCaption = onOpenStatus != null && session != null
    val currentPage = pagerState.currentPage
    val currentImage = images.getOrNull(currentPage)
    var resolvedStatusCache by remember(images, statusCache) { mutableStateOf(statusCache) }
    val initialImage = images.getOrNull(initialIndex)
    var statusItem by remember(images, statusCache, initialIndex) {
        mutableStateOf(initialImage?.albumStatusCacheKey()?.let(statusCache::get))
    }
    var statusLoading by remember(images, statusCache, initialIndex) {
        mutableStateOf(
            showStatusCaption &&
                initialImage != null &&
                statusCache[initialImage.albumStatusCacheKey()] == null,
        )
    }

    LaunchedEffect(currentPage, currentImage?.albumStatusCacheKey(), relatedPosts, session, showStatusCaption) {
        val activeSession = session
        if (!showStatusCaption || currentImage == null || activeSession == null) {
            statusItem = null
            statusLoading = false
            return@LaunchedEffect
        }
        resolvedStatusCache[currentImage.albumStatusCacheKey()]?.let { cached ->
            statusItem = cached
            statusLoading = false
            return@LaunchedEffect
        }
        statusLoading = true
        statusItem = null
        val resolved = runCatching {
            resolveAlbumImageStatus(currentImage, relatedPosts, activeSession)
        }.getOrNull()
        if (resolved != null) {
            resolvedStatusCache = resolvedStatusCache + (currentImage.albumStatusCacheKey() to resolved)
        }
        statusItem = resolved
        statusLoading = false
    }

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
            HorizontalPager(
                state = pagerState,
                flingBehavior = pagerFling,
                userScrollEnabled = !blockPagerScroll,
                modifier = Modifier.fillMaxSize(),
            ) { page ->
                ZoomableFullscreenImage(
                    image = images[page],
                    allImages = images,
                    onDismiss = onDismiss,
                    hasMultipleImages = images.size > 1,
                    onBlockPagerScroll = { blockPagerScroll = it },
                    onRequestPageChange = { delta ->
                        scope.launch {
                            val next = (pagerState.currentPage + delta).coerceIn(0, images.lastIndex)
                            if (next != pagerState.currentPage) {
                                pagerState.animateScrollToPage(next)
                            }
                        }
                    },
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
            if (showStatusCaption) {
                AlbumImageStatusCaption(
                    statusItem = statusItem,
                    loading = statusLoading,
                    emoticonMap = emoticonMap,
                    onClick = {
                        statusItem?.let { item ->
                            onOpenStatus?.invoke(item, currentPage, resolvedStatusCache)
                        }
                    },
                    modifier = Modifier.align(Alignment.BottomCenter),
                )
            }
        }
    }
}

@Composable
private fun AlbumImageStatusCaption(
    statusItem: FeedItem?,
    loading: Boolean,
    emoticonMap: Map<String, String>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (!loading && statusItem == null) return

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.88f)),
                ),
            )
            .navigationBarsPadding()
            .clickable(
                enabled = statusItem != null,
                onClick = onClick,
            )
            .padding(horizontal = 16.dp, vertical = 14.dp),
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = Color.White,
                strokeWidth = 2.dp,
            )
        } else if (statusItem != null) {
            EmoticonText(
                text = statusItem.text,
                emoticonMap = statusItem.emoticons.ifEmpty { emoticonMap },
                style = MaterialTheme.typography.bodyMedium.copy(color = Color.White),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

private data class FitImageLayout(
    val fitWidthPx: Float,
    val fitHeightPx: Float,
    val maxPanX: Float,
    val maxPanY: Float,
)

private enum class FullscreenDragAxis {
    Horizontal,
    Vertical,
}

private fun computeFitImageLayout(
    containerWidthPx: Float,
    containerHeightPx: Float,
    imageAspect: Float,
    scale: Float,
): FitImageLayout {
    val containerAspect = containerWidthPx / containerHeightPx
    val fitWidthPx: Float
    val fitHeightPx: Float
    if (imageAspect > containerAspect) {
        fitWidthPx = containerWidthPx
        fitHeightPx = containerWidthPx / imageAspect
    } else {
        fitWidthPx = containerHeightPx * imageAspect
        fitHeightPx = containerHeightPx
    }
    val scaledWidth = fitWidthPx * scale
    val scaledHeight = fitHeightPx * scale
    val maxPanX = ((scaledWidth - containerWidthPx) / 2f).coerceAtLeast(0f)
    val maxPanY = ((scaledHeight - containerHeightPx) / 2f).coerceAtLeast(0f)
    return FitImageLayout(fitWidthPx, fitHeightPx, maxPanX, maxPanY)
}

private fun computeVisibleBlackBars(
    offsetY: Float,
    layout: FitImageLayout,
    scale: Float,
    containerHeightPx: Float,
): Pair<Float, Float> {
    val scaledHeight = layout.fitHeightPx * scale
    val centerY = containerHeightPx / 2f + offsetY
    val top = (centerY - scaledHeight / 2f).coerceAtLeast(0f)
    val bottom = (containerHeightPx - (centerY + scaledHeight / 2f)).coerceAtLeast(0f)
    return top to bottom
}

private suspend fun flingPanOffset(
    start: Float,
    initialVelocity: Float,
    min: Float,
    max: Float,
    decaySpec: DecayAnimationSpec<Float>,
    onUpdate: (Float) -> Unit,
) {
    if (max - min < 1f || abs(initialVelocity) < 4f) {
        onUpdate(start.coerceIn(min, max))
        return
    }
    val animationState = AnimationState(
        initialValue = start,
        initialVelocity = initialVelocity,
    )
    animationState.animateDecay(decaySpec) {
        onUpdate(value.coerceIn(min, max))
    }
    onUpdate(animationState.value.coerceIn(min, max))
}

@Composable
private fun ZoomableFullscreenImage(
    image: FeedImage,
    allImages: List<FeedImage>,
    onDismiss: () -> Unit,
    hasMultipleImages: Boolean = false,
    onBlockPagerScroll: (Boolean) -> Unit = {},
    onRequestPageChange: (Int) -> Unit = {},
) {
    var targetScale by remember(image.largeUrl) { mutableStateOf(1f) }
    val scale by animateFloatAsState(
        targetValue = targetScale,
        animationSpec = tween(durationMillis = 180),
        label = "fullscreen-image-scale",
    )
    var panOffsetX by remember(image.largeUrl) { mutableFloatStateOf(0f) }
    var panOffsetY by remember(image.largeUrl) { mutableFloatStateOf(0f) }
    var dismissTranslationY by remember(image.largeUrl) { mutableStateOf(0f) }
    var panInertiaJob by remember(image.largeUrl) { mutableStateOf<Job?>(null) }
    val dismissSnapAnim = remember(image.largeUrl) { Animatable(0f) }
    val scope = rememberCoroutineScope()
    val currentTargetScale = rememberUpdatedState(targetScale)
    var bitmap by remember(image.largeUrl) {
        mutableStateOf(FullscreenBitmapCache.get(image.largeUrl))
    }
    var livePlaying by remember(image.largeUrl) { mutableStateOf(image.isLivePhoto) }
    var actionMenuOffset by remember(image.largeUrl) { mutableStateOf<Offset?>(null) }
    var actionMenuVisible by remember(image.largeUrl) { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current

    LaunchedEffect(image.largeUrl) {
        dismissTranslationY = 0f
        dismissSnapAnim.snapTo(0f)
        panOffsetX = 0f
        panOffsetY = 0f
        onBlockPagerScroll(false)
        actionMenuOffset = null
        actionMenuVisible = false
        FullscreenBitmapCache.get(image.largeUrl)?.let { cached ->
            bitmap = cached
            return@LaunchedEffect
        }
        bitmap = withContext(Dispatchers.IO) { loadFullscreenBitmap(image) }
        bitmap?.let { FullscreenBitmapCache.put(image.largeUrl, it) }
    }

    LaunchedEffect(actionMenuVisible, actionMenuOffset) {
        if (!actionMenuVisible && actionMenuOffset != null) {
            delay(180)
            actionMenuOffset = null
        }
    }

    val loadedBitmap = bitmap
    if (loadedBitmap == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Color.White)
        }
        return
    }

    BoxWithConstraints(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        val density = LocalDensity.current
        val containerWidthPx = with(density) { maxWidth.toPx() }
        val containerHeightPx = with(density) { maxHeight.toPx() }
        val blackBarThresholdPx = containerHeightPx / 3f
        val dismissReleaseThresholdPx = containerHeightPx * 0.14f
        val pageSwitchThresholdPx = 80f

        val containerAspect = remember(maxWidth, maxHeight) {
            val width = maxWidth.value.coerceAtLeast(1f)
            val height = maxHeight.value.coerceAtLeast(1f)
            width / height
        }
        val imageAspect = remember(loadedBitmap.width, loadedBitmap.height, image.width, image.height) {
            val width = (image.width ?: loadedBitmap.width).coerceAtLeast(1)
            val height = (image.height ?: loadedBitmap.height).coerceAtLeast(1)
            width.toFloat() / height.toFloat()
        }
        val fillScreenScale = remember(containerAspect, imageAspect) {
            maxOf(imageAspect / containerAspect, containerAspect / imageAspect)
                .coerceIn(1.35f, 5f)
        }

        fun layoutFor(currentScale: Float) =
            computeFitImageLayout(containerWidthPx, containerHeightPx, imageAspect, currentScale)

        fun clampPan(x: Float, y: Float, layout: FitImageLayout): Pair<Float, Float> =
            x.coerceIn(-layout.maxPanX, layout.maxPanX) to y.coerceIn(-layout.maxPanY, layout.maxPanY)

        fun updatePagerScrollBlock(currentScale: Float, currentOffsetX: Float) {
            val layout = layoutFor(currentScale)
            val atHorizontalEdge = layout.maxPanX <= 1f ||
                abs(currentOffsetX) >= layout.maxPanX - 4f
            onBlockPagerScroll(currentScale > 1.01f && layout.maxPanX > 1f && !atHorizontalEdge)
        }

        fun isInsideDisplayedImage(point: Offset): Boolean {
            val dismissOffset = dismissTranslationY + dismissSnapAnim.value
            val currentScale = scale
            val layout = layoutFor(currentScale)
            val displayedWidth = layout.fitWidthPx * currentScale
            val displayedHeight = layout.fitHeightPx * currentScale
            val centerX = containerWidthPx / 2f + panOffsetX
            val centerY = containerHeightPx / 2f + panOffsetY + dismissOffset
            return point.x in (centerX - displayedWidth / 2f)..(centerX + displayedWidth / 2f) &&
                point.y in (centerY - displayedHeight / 2f)..(centerY + displayedHeight / 2f)
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(image.largeUrl, containerWidthPx, containerHeightPx, imageAspect) {
                    awaitEachGesture {
                        panInertiaJob?.cancel()
                        val velocityTracker = VelocityTracker()
                        val touchSlop = viewConfiguration.touchSlop
                        var panningZoomed = false
                        var dismissing = false
                        var lockedAxis: FullscreenDragAxis? = null
                        var horizontalPageDragAccum = 0f
                        var gestureScale = currentTargetScale.value
                        val down = awaitFirstDown(requireUnconsumed = false)
                        var lastCentroid: Offset? = null
                        var lastDistance = 0f
                        while (true) {
                            val event = awaitPointerEvent()
                            val pressed = event.changes.filter { it.pressed }
                            if (pressed.isEmpty()) break

                            if (pressed.size >= 2) {
                                dismissTranslationY = 0f
                                val first = pressed[0].position
                                val second = pressed[1].position
                                val centroid = Offset((first.x + second.x) / 2f, (first.y + second.y) / 2f)
                                val distance = hypot(first.x - second.x, first.y - second.y)
                                if (lastDistance > 0f) {
                                    val zoom = (distance / lastDistance).coerceIn(0.72f, 1.38f)
                                    gestureScale = (gestureScale * zoom).coerceIn(1f, 5f)
                                    targetScale = gestureScale
                                    val previousCentroid = lastCentroid ?: centroid
                                    val pan = centroid - previousCentroid
                                    if (gestureScale > 1f) {
                                        val layout = layoutFor(gestureScale)
                                        val (cx, cy) = clampPan(
                                            panOffsetX + pan.x,
                                            panOffsetY + pan.y,
                                            layout,
                                        )
                                        panOffsetX = cx
                                        panOffsetY = cy
                                        updatePagerScrollBlock(gestureScale, panOffsetX)
                                        velocityTracker.addPosition(
                                            pressed.first().uptimeMillis,
                                            centroid,
                                        )
                                    }
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
                                gestureScale = currentTargetScale.value
                                if (lockedAxis == null) {
                                    val absX = abs(totalDrag.x)
                                    val absY = abs(totalDrag.y)
                                    if (hypot(totalDrag.x, totalDrag.y) > touchSlop) {
                                        lockedAxis = when {
                                            absX > absY * 1.08f -> FullscreenDragAxis.Horizontal
                                            absY > absX * 1.18f -> FullscreenDragAxis.Vertical
                                            else -> null
                                        }
                                    }
                                }
                                val layout = layoutFor(gestureScale)
                                val (blackTop, blackBottom) = computeVisibleBlackBars(
                                    panOffsetY,
                                    layout,
                                    gestureScale,
                                    containerHeightPx,
                                )
                                val canDismissDown = blackTop >= blackBarThresholdPx && delta.y > 0
                                val canDismissUp = blackBottom >= blackBarThresholdPx && delta.y < 0
                                val canDismissVertically = lockedAxis == FullscreenDragAxis.Vertical &&
                                    (canDismissDown || canDismissUp ||
                                    (
                                        gestureScale <= 1.01f &&
                                            abs(totalDrag.y) > 48f &&
                                            abs(totalDrag.y) > abs(totalDrag.x) * 1.35f
                                        ))

                                updatePagerScrollBlock(gestureScale, panOffsetX)

                                if (gestureScale <= 1.01f && lockedAxis == FullscreenDragAxis.Horizontal) {
                                    dismissTranslationY = 0f
                                    onBlockPagerScroll(false)
                                    continue
                                }

                                if (
                                    gestureScale > 1.01f &&
                                    hasMultipleImages &&
                                    abs(delta.x) > abs(delta.y) * 0.55f
                                ) {
                                    val atLeftEdge = layout.maxPanX <= 1f || panOffsetX <= -layout.maxPanX + 4f
                                    val atRightEdge = layout.maxPanX <= 1f || panOffsetX >= layout.maxPanX - 4f
                                    val swipeToNext = atLeftEdge && delta.x < 0
                                    val swipeToPrev = atRightEdge && delta.x > 0
                                    if (swipeToNext || swipeToPrev) {
                                        horizontalPageDragAccum += delta.x
                                        if (horizontalPageDragAccum < -pageSwitchThresholdPx) {
                                            onRequestPageChange(1)
                                            horizontalPageDragAccum = 0f
                                            change.consume()
                                            break
                                        } else if (horizontalPageDragAccum > pageSwitchThresholdPx) {
                                            onRequestPageChange(-1)
                                            horizontalPageDragAccum = 0f
                                            change.consume()
                                            break
                                        }
                                        change.consume()
                                        continue
                                    }
                                }

                                if (canDismissVertically) {
                                    dismissing = true
                                    dismissTranslationY += delta.y
                                    velocityTracker.addPosition(change.uptimeMillis, change.position)
                                    change.consume()
                                    continue
                                }

                                if (gestureScale > 1.01f) {
                                    panningZoomed = true
                                    velocityTracker.addPosition(change.uptimeMillis, change.position)
                                    val (cx, cy) = clampPan(
                                        panOffsetX + delta.x,
                                        panOffsetY + delta.y,
                                        layout,
                                    )
                                    panOffsetX = cx
                                    panOffsetY = cy
                                    updatePagerScrollBlock(gestureScale, panOffsetX)
                                    change.consume()
                                } else {
                                    onBlockPagerScroll(false)
                                }
                            }
                        }

                        gestureScale = currentTargetScale.value
                        val dismissDistance = dismissTranslationY + dismissSnapAnim.value
                        if (dismissing || dismissDistance != 0f) {
                            val velocity = velocityTracker.calculateVelocity()
                            if (
                                abs(dismissDistance) > dismissReleaseThresholdPx ||
                                abs(velocity.y) > 850f
                            ) {
                                onDismiss()
                            } else {
                                scope.launch {
                                    dismissSnapAnim.snapTo(dismissTranslationY)
                                    dismissTranslationY = 0f
                                    dismissSnapAnim.animateTo(0f, tween(220))
                                }
                            }
                            return@awaitEachGesture
                        }

                        updatePagerScrollBlock(gestureScale, panOffsetX)
                        if (panningZoomed && gestureScale > 1.01f) {
                            val velocity = velocityTracker.calculateVelocity()
                            val layout = layoutFor(gestureScale)
                            val decaySpec = exponentialDecay<Float>(
                                frictionMultiplier = 0.42f,
                                absVelocityThreshold = 0.5f,
                            )
                            val startX = panOffsetX
                            val startY = panOffsetY
                            panInertiaJob = scope.launch {
                                coroutineScope {
                                    launch {
                                        flingPanOffset(
                                            start = startX,
                                            initialVelocity = velocity.x,
                                            min = -layout.maxPanX,
                                            max = layout.maxPanX,
                                            decaySpec = decaySpec,
                                        ) { panOffsetX = it }
                                    }
                                    launch {
                                        flingPanOffset(
                                            start = startY,
                                            initialVelocity = velocity.y,
                                            min = -layout.maxPanY,
                                            max = layout.maxPanY,
                                            decaySpec = decaySpec,
                                        ) { panOffsetY = it }
                                    }
                                }
                                updatePagerScrollBlock(gestureScale, panOffsetX)
                            }
                        }
                    }
                }
                .pointerInput(image.largeUrl, fillScreenScale) {
                    detectTapGestures(
                        onTap = {
                            if (actionMenuVisible) {
                                actionMenuVisible = false
                            } else {
                                onDismiss()
                            }
                        },
                        onDoubleTap = {
                            actionMenuVisible = false
                            panInertiaJob?.cancel()
                            dismissTranslationY = 0f
                            scope.launch { dismissSnapAnim.snapTo(0f) }
                            onBlockPagerScroll(false)
                            if (targetScale > 1f) {
                                targetScale = 1f
                                panOffsetX = 0f
                                panOffsetY = 0f
                            } else {
                                targetScale = fillScreenScale
                            }
                        },
                        onLongPress = { offset ->
                            if (image.isLivePhoto && isInsideDisplayedImage(offset)) {
                                livePlaying = true
                            } else {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                actionMenuOffset = offset
                                actionMenuVisible = true
                            }
                        },
                    )
                },
            contentAlignment = Alignment.Center,
        ) {
            val dismissOffsetY = dismissTranslationY + dismissSnapAnim.value
            val dismissAlpha = (1f - abs(dismissOffsetY) / containerHeightPx * 0.75f).coerceIn(0.35f, 1f)
            val imageModifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    translationX = panOffsetX
                    translationY = panOffsetY + dismissOffsetY
                    alpha = dismissAlpha
                }
            if (image.isGif) {
                AnimatedRemoteImage(
                    url = image.downloadUrls.firstOrNull { it.isNotBlank() } ?: image.largeUrl,
                    modifier = imageModifier,
                    contentScale = ContentScale.Fit,
                )
            } else {
                Image(
                    bitmap = loadedBitmap.asImageBitmap(),
                    contentDescription = null,
                    modifier = imageModifier,
                    contentScale = ContentScale.Fit,
                )
            }
            if (image.isLivePhoto && livePlaying) {
                LivePhotoOverlay(
                    image = image,
                    modifier = imageModifier,
                    onEnded = { livePlaying = false },
                )
            }

            actionMenuOffset?.let { offset ->
                FullscreenImageActionMenu(
                    image = image,
                    allImages = allImages,
                    pressOffset = offset,
                    visible = actionMenuVisible,
                    screenWidthPx = containerWidthPx,
                    screenHeightPx = containerHeightPx,
                    onDismiss = { actionMenuVisible = false },
                )
            }
        }
    }
}

@Composable
private fun BoxScope.FullscreenImageActionMenu(
    image: FeedImage,
    allImages: List<FeedImage>,
    pressOffset: Offset,
    visible: Boolean,
    screenWidthPx: Float,
    screenHeightPx: Float,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val onMessage = LocalUiMessenger.current
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    var saving by remember { mutableStateOf(false) }
    val images = allImages.ifEmpty { listOf(image) }
    val menuWidth = 180.dp
    val estimatedMenuHeight = ActionMenuThreeRowHeight
    val margin = 14.dp
    val gap = 10.dp
    val menuPlacement = calculateActionMenuOffsetFromPointPx(
        pressOffset = pressOffset,
        screenWidthPx = screenWidthPx,
        screenHeightPx = screenHeightPx,
        menuWidthPx = with(density) { menuWidth.toPx() },
        menuHeightPx = with(density) { estimatedMenuHeight.toPx() },
        marginPx = with(density) { margin.toPx() },
        gapPx = with(density) { gap.toPx() },
    )

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(150)) + slideIn(tween(170)) {
            IntOffset(0, if (menuPlacement.belowAnchor) -it.height / 7 else it.height / 7)
        },
        exit = fadeOut(tween(130)) + slideOut(tween(150)) {
            IntOffset(0, if (menuPlacement.belowAnchor) -it.height / 7 else it.height / 7)
        },
        modifier = Modifier
            .align(Alignment.TopStart)
            .offset { menuPlacement.offset }
            .width(menuWidth)
            .zIndex(20f),
    ) {
        ImageActionFrostedCard {
            ImageActionRow(
                label = "保存",
                enabled = !saving,
                onClick = {
                    saving = true
                    scope.launch {
                        ImageSaveHelper.saveImage(context, image)
                            .onSuccess { name ->
                                onMessage("保存成功", "已保存到相册：$name")
                                onDismiss()
                            }
                            .onFailure { error ->
                                onMessage("保存失败", error.message ?: "请稍后重试")
                            }
                        saving = false
                    }
                },
            )
            ImageActionMenuDivider()
            ImageActionRow(
                label = "保存全部",
                enabled = !saving && images.isNotEmpty(),
                onClick = {
                    saving = true
                    scope.launch {
                        ImageSaveHelper.saveAllImages(context, images)
                            .onSuccess { count ->
                                onMessage("保存成功", "已保存 $count 张图片到相册")
                                onDismiss()
                            }
                            .onFailure { error ->
                                onMessage("保存失败", error.message ?: "请稍后重试")
                            }
                        saving = false
                    }
                },
            )
            ImageActionMenuDivider()
            ImageActionRow(
                label = "分享",
                enabled = !saving,
                onClick = {
                    saving = true
                    scope.launch {
                        ImageSaveHelper.shareImage(context, image)
                            .onSuccess { onDismiss() }
                            .onFailure { error ->
                                onMessage("分享失败", error.message ?: "请稍后重试")
                            }
                        saving = false
                    }
                },
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
    val videoCoordinator = LocalVideoPlaybackCoordinator.current
    val videoUrl = image.livePhotoVideoUrl?.takeIf { it.isNotBlank() } ?: return
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
        val pauseHandler = { player.pause() }
        videoCoordinator.registerPauseHandler(pauseHandler)
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
            videoCoordinator.unregisterPauseHandler(pauseHandler)
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
private fun VideoPlayHintBadge(
    durationSeconds: Int?,
    modifier: Modifier = Modifier,
) {
    val hintText = buildString {
        append("点击播放")
        if (durationSeconds != null && durationSeconds > 0) {
            append(' ')
            append(formatVideoTime(durationSeconds * 1000L))
        }
    }
    Text(
        text = hintText,
        modifier = modifier,
        color = Color.White,
        fontSize = 13.sp,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

@Composable
private fun InlineVideoPlayer(
    media: FeedMedia,
    onClick: () -> Unit = {},
    onFullscreenRequest: () -> Unit = onClick,
) {
    val videoCoordinator = LocalVideoPlaybackCoordinator.current
    val videoPeekController = LocalVideoPeekController.current
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    val playbackKey = remember(media.streamUrl, media.downloadUrl, media.coverUrl) { videoPlaybackKey(media) }
    val inlinePlaying = videoCoordinator.activeKey == playbackKey
    var aspectRatio by remember(media.streamUrl) { mutableStateOf(16f / 9f) }
    var actionOpen by remember(media.streamUrl) { mutableStateOf(false) }
    var peekActive by remember(media.streamUrl) { mutableStateOf(false) }
    var anchorBounds by remember(media.streamUrl) { mutableStateOf<Rect?>(null) }
    val peekScale by animateFloatAsState(
        targetValue = if (peekActive) 0.96f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium,
        ),
        label = "feed-video-peek-scale",
    )

    fun resetPeekState() {
        actionOpen = false
        peekActive = false
    }

    fun openVideoPeek() {
        val bounds = anchorBounds ?: return
        actionOpen = true
        peekActive = true
        videoCoordinator.pauseAll()
        videoCoordinator.activeKey = null
        videoPeekController.open(
            VideoPeekRequest(
                media = media,
                anchorBounds = bounds,
                onCancel = { resetPeekState() },
                onRelease = {
                    resetPeekState()
                    scope.launch {
                        delay(32)
                        videoCoordinator.activeKey = null
                        onFullscreenRequest()
                    }
                },
            ),
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(aspectRatio.coerceIn(0.56f, 1.78f))
            .onGloballyPositioned { coordinates ->
                anchorBounds = coordinates.boundsInWindow()
            }
            .graphicsLayer {
                scaleX = peekScale
                scaleY = peekScale
                alpha = if (actionOpen) 0f else 1f
            }
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
            .pointerInput(media.streamUrl, media.type) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    var lastPosition = down.position
                    var cancelledByMoveBeforeLongPress = false
                    var releasedBeforeLongPress = false
                    val longPressed = withTimeoutOrNull(viewConfiguration.longPressTimeoutMillis) {
                        while (true) {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull { it.id == down.id }
                                ?: event.changes.firstOrNull()
                                ?: return@withTimeoutOrNull false
                            lastPosition = change.position
                            if (!change.pressed) {
                                releasedBeforeLongPress = true
                                return@withTimeoutOrNull false
                            }
                            val preLongPressMove = lastPosition - down.position
                            if (hypot(preLongPressMove.x, preLongPressMove.y) > viewConfiguration.touchSlop) {
                                cancelledByMoveBeforeLongPress = true
                                return@withTimeoutOrNull false
                            }
                        }
                    } == null && !cancelledByMoveBeforeLongPress && !releasedBeforeLongPress

                    if (!longPressed) {
                        if (cancelledByMoveBeforeLongPress) {
                            return@awaitEachGesture
                        }
                        if (media.type == MediaType.Video) {
                            videoCoordinator.activeKey = playbackKey
                        } else {
                            onClick()
                        }
                        return@awaitEachGesture
                    }

                    if (media.type != MediaType.Video) {
                        onClick()
                        return@awaitEachGesture
                    }

                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    down.consume()
                    openVideoPeek()

                    val dragCancelThreshold = 82f
                    var cancelledByDrag = false
                    while (true) {
                        val event = awaitPointerEvent(PointerEventPass.Initial)
                        val change = event.changes.firstOrNull { it.id == down.id }
                            ?: event.changes.firstOrNull()
                            ?: break
                        change.consume()
                        if (!change.pressed) {
                            break
                        }
                        lastPosition = change.position
                        val totalDrag = lastPosition - down.position
                        if (
                            totalDrag.y > dragCancelThreshold &&
                            abs(totalDrag.y) > abs(totalDrag.x) * 1.15f
                        ) {
                            cancelledByDrag = true
                            videoPeekController.cancel()
                            while (true) {
                                val consumeEvent = awaitPointerEvent(PointerEventPass.Initial)
                                consumeEvent.changes.forEach { it.consume() }
                                if (consumeEvent.changes.all { !it.pressed }) break
                            }
                            break
                        }
                    }

                    if (cancelledByDrag) {
                        resetPeekState()
                    } else {
                        videoPeekController.release()
                    }
                }
            },
    ) {
        if (inlinePlaying && media.type == MediaType.Video) {
            WeiboVideoSurface(
                media = media,
                isFullscreen = false,
                onAspectRatio = { aspectRatio = it },
                onFullscreen = {
                    videoCoordinator.activeKey = null
                    onFullscreenRequest()
                },
                onEnterPictureInPicture = {
                    videoCoordinator.activeKey = null
                },
            )
        } else {
            RemoteImage(
                url = media.coverUrl,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_video_play),
                    contentDescription = "播放",
                    modifier = Modifier.size(60.dp),
                    tint = Color.White,
                )
            }
            if (media.type == MediaType.Video) {
                VideoPlayHintBadge(
                    durationSeconds = media.durationSeconds,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 6.dp, end = 10.dp),
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
}

@Composable
private fun WeiboVideoSurface(
    media: FeedMedia,
    isFullscreen: Boolean,
    onAspectRatio: (Float) -> Unit,
    onFullscreen: () -> Unit,
    onEnterPictureInPicture: (() -> Unit)? = null,
    modifier: Modifier = Modifier.fillMaxSize(),
    controlsEnabled: Boolean = true,
    resumePosition: Boolean = true,
    savePositionOnDispose: Boolean = true,
) {
    val context = LocalContext.current
    val onMessage = LocalUiMessenger.current
    val scope = rememberCoroutineScope()
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
    var controlsVisible by remember(videoUrl) { mutableStateOf(true) }
    var controlsHideSignal by remember(videoUrl) { mutableIntStateOf(0) }
    var downloading by remember(videoUrl) { mutableStateOf(false) }
    var aspectRatio by remember(videoUrl) { mutableFloatStateOf(16f / 9f) }
    val fullscreenTopInset = if (isFullscreen) {
        WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    } else {
        0.dp
    }
    val trackViewportPause = !isFullscreen && controlsEnabled
    val screenHeightPx = remember(context) {
        context.resources.displayMetrics.heightPixels.toFloat()
    }
    var isViewportVisible by remember(videoUrl) { mutableStateOf(true) }

    fun showControls() {
        controlsVisible = true
        controlsHideSignal++
    }

    fun toggleControls() {
        if (controlsVisible) {
            controlsVisible = false
        } else {
            showControls()
        }
    }

    LaunchedEffect(isPlaying, isBuffering, playbackError, controlsHideSignal) {
        if (!isPlaying || isBuffering || playbackError != null) {
            controlsVisible = true
            return@LaunchedEffect
        }
        delay(3_000)
        controlsVisible = false
    }

    val playerCache = remember { mutableMapOf<String, androidx.media3.exoplayer.ExoPlayer>() }
    val player = remember(videoUrl) {
        playerCache[videoUrl]?.also { it.playWhenReady = true }
            ?: androidx.media3.exoplayer.ExoPlayer.Builder(context).build().apply {
                playerCache[videoUrl] = this
                setMediaSource(buildVideoMediaSource(context, videoUrl))
                prepare()
                if (resumePosition) {
                    videoCoordinator.positions[playbackKey]?.takeIf { it > 0L }?.let { seekTo(it) }
                }
                playWhenReady = true
            }
    }

    fun enterPictureInPicture() {
        showControls()
        val currentPosition = player.currentPosition.coerceAtLeast(0L)
        videoCoordinator.positions[playbackKey] = currentPosition
        player.pause()
        VideoPipActivity.start(
            context = context,
            media = media,
            positionMs = currentPosition,
            speed = selectedSpeed,
            aspectRatio = aspectRatio,
        )
        onEnterPictureInPicture?.invoke()
    }

    DisposableEffect(player) {
        val pauseHandler = {
            val currentPosition = player.currentPosition.coerceAtLeast(0L)
            if (currentPosition > 0L || videoCoordinator.positions[playbackKey] == null) {
                videoCoordinator.positions[playbackKey] = currentPosition
            }
            player.pause()
        }
        videoCoordinator.registerPauseHandler(pauseHandler)
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
                val ratio = width.toFloat() / height.toFloat()
                aspectRatio = ratio
                onAspectRatio(ratio)
            }

            override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                isBuffering = false
                if (videoIndex < videoCandidates.lastIndex) {
                    playbackError = null
                    videoIndex += 1
                } else {
                    playbackError = error.message ?: "视频无法播放"
                }
            }
        }
        player.addListener(listener)
        onDispose {
            videoCoordinator.unregisterPauseHandler(pauseHandler)
            if (savePositionOnDispose) {
                val currentPosition = player.currentPosition.coerceAtLeast(0L)
                if (currentPosition > 0L || videoCoordinator.positions[playbackKey] == null) {
                    videoCoordinator.positions[playbackKey] = currentPosition
                }
            }
            player.pause()
            if (trackViewportPause && videoCoordinator.activeKey == playbackKey) {
                videoCoordinator.activeKey = null
            }
            player.removeListener(listener)
            player.release()
        }
    }

    LaunchedEffect(isViewportVisible, trackViewportPause, player) {
        if (!trackViewportPause || isViewportVisible) return@LaunchedEffect
        val currentPosition = player.currentPosition.coerceAtLeast(0L)
        if (currentPosition > 0L || videoCoordinator.positions[playbackKey] == null) {
            videoCoordinator.positions[playbackKey] = currentPosition
        }
        player.pause()
        player.playWhenReady = false
        isPlaying = false
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
            .background(Color.Black)
            .then(
                if (trackViewportPause) {
                    Modifier.onGloballyPositioned { coordinates ->
                        val bounds = coordinates.boundsInWindow()
                        val visibleTop = max(bounds.top, 0f)
                        val visibleBottom = min(bounds.bottom, screenHeightPx)
                        val visibleHeight = (visibleBottom - visibleTop).coerceAtLeast(0f)
                        val fraction = visibleHeight / bounds.height.coerceAtLeast(1f)
                        isViewportVisible = fraction >= 0.35f
                    }
                } else {
                    Modifier
                },
            ),
    ) {
        AndroidView(
            modifier = Modifier
                .fillMaxSize()
                .then(
                    if (controlsEnabled) {
                        Modifier.pointerInput(player, selectedSpeed, isFullscreen, onFullscreen) {
                            detectTapGestures(
                                onTap = { toggleControls() },
                                onDoubleTap = {
                                    if (!isFullscreen) {
                                        onFullscreen()
                                    }
                                },
                                onPress = {
                                    val releasedEarly = withTimeoutOrNull(360L) { tryAwaitRelease() }
                                    if (releasedEarly == null) {
                                        showControls()
                                        displayedSpeed = 2f
                                        player.setPlaybackSpeed(2f)
                                        tryAwaitRelease()
                                        displayedSpeed = selectedSpeed
                                        player.setPlaybackSpeed(selectedSpeed)
                                        showControls()
                                    }
                                },
                            )
                        }
                    } else {
                        Modifier
                    },
                ),
            factory = { ctx ->
                (android.view.LayoutInflater.from(ctx)
                    .inflate(R.layout.view_video_player, null, false) as androidx.media3.ui.PlayerView).apply {
                    this.player = player
                    useController = false
                    resizeMode = androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_FIT
                    setShutterBackgroundColor(android.graphics.Color.BLACK)
                }
            },
            update = {
                it.resizeMode = androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_FIT
                it.player = player
                it.requestLayout()
            },
        )

        AnimatedVisibility(
            visible = controlsEnabled && controlsVisible && !isFullscreen,
            enter = fadeIn(tween(200)) + slideInVertically(tween(220)) { -it },
            exit = fadeOut(tween(180)) + slideOutVertically(tween(200)) { -it },
            modifier = Modifier.align(Alignment.TopStart),
        ) {
            GlassTextButton(
                text = "全屏",
                modifier = Modifier
                    .padding(10.dp)
                    .width(54.dp)
                    .height(28.dp),
                onClick = {
                    showControls()
                    onFullscreen()
                },
            )
        }

        AnimatedVisibility(
            visible = controlsEnabled && controlsVisible,
            enter = fadeIn(tween(200)) + slideInVertically(tween(220)) { -it },
            exit = fadeOut(tween(180)) + slideOutVertically(tween(200)) { -it },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .zIndex(if (isFullscreen) 20f else 0f),
        ) {
            GlassTextButton(
                text = "画中画",
                modifier = Modifier
                    .padding(
                        end = 10.dp,
                        top = if (isFullscreen) fullscreenTopInset + 10.dp else 10.dp,
                    )
                    .width(66.dp)
                    .height(28.dp),
                onClick = { enterPictureInPicture() },
            )
        }

        AnimatedVisibility(
            visible = controlsEnabled && controlsVisible && isFullscreen,
            enter = fadeIn(tween(200)) + slideInVertically(tween(220)) { -it },
            exit = fadeOut(tween(180)) + slideOutVertically(tween(200)) { -it },
            modifier = Modifier
                .align(Alignment.TopStart)
                .zIndex(20f),
        ) {
            GlassTextButton(
                text = if (downloading) "下载中" else "下载",
                modifier = Modifier
                    .padding(start = 10.dp, top = fullscreenTopInset + 10.dp)
                    .width(if (downloading) 66.dp else 54.dp)
                    .height(28.dp),
                enabled = !downloading,
                onClick = {
                    downloading = true
                    Toast.makeText(context, "开始下载视频", Toast.LENGTH_SHORT).show()
                    scope.launch {
                        ImageSaveHelper.saveVideo(context, media)
                            .onSuccess { name ->
                                Toast.makeText(context, "下载完成：$name", Toast.LENGTH_LONG).show()
                                onMessage("下载完成", "已保存到相册：$name")
                            }
                            .onFailure { error ->
                                val message = error.message ?: "请稍后重试"
                                Toast.makeText(context, "下载失败：$message", Toast.LENGTH_LONG).show()
                                onMessage("下载失败", message)
                            }
                        downloading = false
                    }
                },
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

        AnimatedVisibility(
            visible = controlsEnabled && controlsVisible,
            enter = fadeIn(tween(200)) + slideInVertically(tween(220)) { it },
            exit = fadeOut(tween(180)) + slideOutVertically(tween(200)) { it },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(start = 6.dp, end = 6.dp, bottom = if (isFullscreen) 24.dp else 8.dp)
                .fillMaxWidth(),
        ) {
            VideoControls(
                isPlaying = isPlaying,
                positionMs = positionMs,
                durationMs = durationMs,
                speed = displayedSpeed,
                onPlayPause = {
                    showControls()
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
                    showControls()
                    positionMs = target
                    player.seekTo(target)
                },
                onSpeedClick = {
                    showControls()
                    selectedSpeed = when (selectedSpeed) {
                        1f -> 1.5f
                        1.5f -> 2f
                        else -> 1f
                    }
                    displayedSpeed = selectedSpeed
                    player.setPlaybackSpeed(selectedSpeed)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(32.dp),
            )
        }
    }
}

@Composable
private fun GlassTextButton(
    text: String,
    modifier: Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    Box(
        modifier = modifier.clickable(enabled = enabled, onClick = onClick),
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
                text = formatVideoTime((durationMs - positionMs).coerceAtLeast(0L)),
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

private val StatusLikeColor = Color(0xFFE94326)

@Composable
private fun StatusActions(
    item: FeedItem,
    onCommentClick: (() -> Unit)? = null,
    onToggleLike: (() -> Unit)? = null,
) {
    val actionColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.62f)
    val chipColor = actionColor
    val haptic = LocalHapticFeedback.current
    val likeLabelColor = if (item.liked) StatusLikeColor else chipColor
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
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            contentAlignment = Alignment.Center,
        ) {
            AssistChip(
                onClick = {},
                modifier = Modifier.height(24.dp),
                colors = AssistChipDefaults.assistChipColors(containerColor = Color.Transparent, labelColor = actionColor),
                border = null,
                label = { Text("转发 ${item.repostsCount}", fontSize = 11.sp, color = chipColor) },
            )
        }
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            contentAlignment = Alignment.Center,
        ) {
            AssistChip(
                onClick = { onCommentClick?.invoke() },
                modifier = Modifier.height(24.dp),
                colors = AssistChipDefaults.assistChipColors(containerColor = Color.Transparent, labelColor = actionColor),
                border = null,
                label = { Text("评论 ${item.commentsCount}", fontSize = 11.sp, color = chipColor) },
            )
        }
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .height(24.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .combinedClickable(
                        onClick = {},
                        onLongClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onToggleLike?.invoke()
                        },
                    )
                    .padding(horizontal = 8.dp),
                contentAlignment = Alignment.Center,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                    Text("\u8D5E", fontSize = 11.sp, color = likeLabelColor)
                    Text(item.likesCount, fontSize = 11.sp, color = likeLabelColor)
                    AnimatedVisibility(
                        visible = item.liked,
                        enter = fadeIn(tween(120)) + scaleIn(
                            initialScale = 0.55f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessMediumLow,
                            ),
                        ),
                        exit = fadeOut(tween(90)) + scaleOut(targetScale = 0.75f),
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_status_like),
                            contentDescription = "\u8D5E",
                            modifier = Modifier.size(15.dp),
                            tint = Color.Unspecified,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailScreen(
    item: FeedItem,
    comments: List<CommentItem>,
    commentSort: CommentSort,
    isLoadingComments: Boolean,
    onBack: () -> Unit,
    onRefresh: () -> Unit,
    onCommentSortChange: (CommentSort) -> Unit,
    onMediaClick: (FeedMedia) -> Unit,
    emoticonMap: Map<String, String> = emptyMap(),
    onRetweetClick: ((FeedItem) -> Unit)? = null,
    onUserClick: ((String) -> Unit)? = null,
    commentsHasMore: Boolean = true,
    listState: LazyListState = rememberLazyListState(),
    onLoadMoreComments: () -> Unit = {},
    isLongTextLoading: (FeedItem) -> Boolean = { false },
    onLoadLongText: ((FeedItem) -> Unit)? = null,
    onToggleLike: ((FeedItem) -> Unit)? = null,
    onExpandNestedComments: ((String) -> Unit)? = null,
    nestedCommentsLoadingIds: Set<String> = emptySet(),
    onOpenEditHistory: ((FeedItem) -> Unit)? = null,
) {
    val topInset = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

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

    AppPullToRefreshBox(
        isRefreshing = isLoadingComments,
        onRefresh = onRefresh,
        modifier = Modifier.fillMaxSize(),
    ) {
        Column(Modifier.fillMaxSize()) {
            DetailStickyAuthorHeader(
                item = item,
                onUserClick = onUserClick,
                onOpenEditHistory = onOpenEditHistory,
                modifier = Modifier.padding(top = topInset),
            )
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 24.dp),
            ) {
                item {
                    Column(Modifier.padding(bottom = 12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        FeedCard(
                            item = item,
                            onClick = {},
                            onMediaClick = onMediaClick,
                            emoticonMap = emoticonMap,
                            onRetweetClick = onRetweetClick,
                            onUserClick = onUserClick,
                            isLongTextLoading = isLongTextLoading,
                            onLoadLongText = onLoadLongText,
                            onToggleLike = onToggleLike,
                            showAuthorRow = false,
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = CommentRowOuterStart, end = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(
                                text = "评论 ${item.commentsCount}",
                                fontSize = CommentAuthorFontSize,
                                fontWeight = FontWeight.SemiBold,
                            )
                            CommentSortActionMenu(
                                selected = commentSort,
                                onSelected = onCommentSortChange,
                            )
                        }
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
                    Text(
                        text = "暂无评论",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        fontSize = CommentAuthorFontSize,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }
            }

            itemsIndexed(comments, key = { _, comment -> comment.id }) { index, comment ->
                CommentRow(
                    comment = comment,
                    onUserClick = onUserClick,
                    onExpandNestedComments = onExpandNestedComments,
                    nestedCommentsLoadingIds = nestedCommentsLoadingIds,
                )
                if (index < comments.lastIndex) {
                    CommentDivider()
                }
            }
            }
        }
    }
}

@Composable
private fun DetailStickyAuthorHeader(
    item: FeedItem,
    onUserClick: ((String) -> Unit)?,
    onOpenEditHistory: ((FeedItem) -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Color.White,
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 26.dp)
                    .padding(top = 12.dp, bottom = 10.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    AuthorRow(
                        item = item,
                        onUserClick = onUserClick,
                        avatarClickable = true,
                    )
                }
                FeedCardActionMenu(
                    item = item,
                    onOpenEditHistory = onOpenEditHistory,
                )
            }
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 26.dp),
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.10f),
            )
        }
    }
}

private val CommentRowOuterStart = 18.dp
private val CommentAuthorFontSize = 13.sp
private val CommentAvatarSize = 28.dp
private val CommentAvatarTextGap = 8.dp

private fun commentTextContainsReplyTo(text: String, replyToAuthor: String?): Boolean {
    val trimmed = text.trimStart()
    if (trimmed.startsWith("回复") && trimmed.contains("@")) return true
    if (replyToAuthor.isNullOrBlank()) return false
    return Regex("回复\\s*@${Regex.escape(replyToAuthor)}\\s*[:：]?").containsMatchIn(trimmed)
}

private fun updateCommentTree(
    comments: List<CommentItem>,
    commentId: String,
    transform: (CommentItem) -> CommentItem,
): List<CommentItem> = comments.map { comment ->
    when {
        comment.id == commentId -> transform(comment)
        comment.comments.isNotEmpty() ->
            comment.copy(comments = updateCommentTree(comment.comments, commentId, transform))
        else -> comment
    }
}

@Composable
private fun CommentDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(
            start = CommentRowOuterStart,
            end = 24.dp,
        ),
        thickness = 0.5.dp,
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.10f),
    )
}

@Composable
private fun CommentRow(
    comment: CommentItem,
    depth: Int = 0,
    onUserClick: ((String) -> Unit)? = null,
    onExpandNestedComments: ((String) -> Unit)? = null,
    nestedCommentsLoadingIds: Set<String> = emptySet(),
) {
    val resolvedMap = comment.emoticons.ifEmpty { emptyMap() }
    val authorTarget = comment.authorId.takeIf { it.isNotBlank() } ?: comment.authorName
    val verticalPadding = if (depth == 0) 4.dp else 2.dp
    val rowStart = CommentRowOuterStart + (depth * 24).dp
    val showReplyHeader = depth == 0 &&
        comment.replyToAuthor != null &&
        !commentTextContainsReplyTo(comment.text, comment.replyToAuthor)
    val nestedContentStart = rowStart + CommentAvatarSize + CommentAvatarTextGap
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = rowStart, end = 18.dp, top = verticalPadding, bottom = verticalPadding),
            verticalAlignment = Alignment.Top,
        ) {
            RemoteImage(
                url = comment.authorAvatarUrl,
                modifier = Modifier
                    .size(CommentAvatarSize)
                    .clip(CircleShape)
                    .clickable(
                        enabled = onUserClick != null && authorTarget.isNotBlank(),
                        onClick = { onUserClick?.invoke(authorTarget) },
                    ),
                contentScale = ContentScale.Crop,
            )
            Spacer(Modifier.width(CommentAvatarTextGap))
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = comment.authorName,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = CommentAuthorFontSize,
                        modifier = Modifier.clickable(
                            enabled = onUserClick != null && authorTarget.isNotBlank(),
                            onClick = { onUserClick?.invoke(authorTarget) },
                        ),
                    )
                    if (showReplyHeader) {
                        Text(
                            text = " 回复 ",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        val replyTarget = comment.replyToAuthorId?.takeIf { it.isNotBlank() }
                            ?: comment.replyToAuthor
                        Text(
                            text = "@${comment.replyToAuthor}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.clickable(
                                enabled = onUserClick != null && !replyTarget.isNullOrBlank(),
                                onClick = { replyTarget?.let { onUserClick?.invoke(it) } },
                            ),
                        )
                    }
                }
                EmoticonText(
                    text = comment.text,
                    emoticonMap = resolvedMap,
                    style = MaterialTheme.typography.bodyMedium,
                    onUserClick = onUserClick,
                )
                if (comment.images.isNotEmpty()) {
                    CommentImageStrip(images = comment.images)
                }
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
            CommentRow(
                comment = nested,
                depth = depth + 1,
                onUserClick = onUserClick,
                onExpandNestedComments = onExpandNestedComments,
                nestedCommentsLoadingIds = nestedCommentsLoadingIds,
            )
        }
        comment.moreInfoText?.let { moreText ->
            val isExpanding = comment.id in nestedCommentsLoadingIds
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = nestedContentStart, end = 18.dp, top = 2.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                if (isExpanding) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(12.dp),
                        strokeWidth = 1.5.dp,
                    )
                }
                Text(
                    text = if (isExpanding) "加载中..." else moreText,
                    fontSize = CommentAuthorFontSize,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable(
                        enabled = !isExpanding && onExpandNestedComments != null,
                        onClick = { onExpandNestedComments?.invoke(comment.id) },
                    ),
                )
            }
        }
    }
}

@Composable
private fun CommentImageStrip(images: List<FeedImage>) {
    if (images.isEmpty()) return

    var viewerOpen by remember { mutableStateOf(false) }
    var viewerIndex by remember { mutableStateOf(0) }

    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.padding(top = 4.dp),
    ) {
        images.forEachIndexed { index, image ->
            FeedImageCell(
                image = image,
                allImages = images,
                imageIndex = index,
                modifier = Modifier.size(72.dp),
                previewUrl = image.thumbnailUrl,
                contentScale = ContentScale.Crop,
                showLiveBadge = true,
                onOpenViewer = {
                    viewerIndex = index
                    viewerOpen = true
                },
            )
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
    profileHeaderHeight: Dp,
    onProfileHeaderHeightChange: (Dp) -> Unit,
    isLoading: Boolean,
    loadError: String?,
    hasLoginCookie: Boolean,
    posts: List<FeedItem>,
    postsError: String?,
    postsLoadingMore: Boolean,
    albumImages: List<FeedImage>,
    albumLoading: Boolean = false,
    albumLoadingMore: Boolean,
    postsHasMore: Boolean = true,
    albumHasMore: Boolean = true,
    emoticonMap: Map<String, String> = emptyMap(),
    emoticonCount: Int = 0,
    emoticonSyncing: Boolean = false,
    postsListState: LazyListState = rememberLazyListState(),
    albumListState: LazyListState = rememberLazyListState(),
    albumError: String? = null,
    initialPagerPage: Int = 0,
    onMinePagerPageChanged: (Int) -> Unit = {},
    onRefresh: () -> Unit,
    onLoadMorePosts: () -> Unit,
    onLoadMoreAlbum: () -> Unit,
    onSyncEmoticons: () -> Unit,
    onItemClick: (FeedItem) -> Unit,
    onOpenAlbumViewer: (AlbumViewerState) -> Unit,
    onMediaClick: (FeedMedia) -> Unit,
    onUserClick: ((String) -> Unit)? = null,
    isLongTextLoading: (FeedItem) -> Boolean = { false },
    onLoadLongText: ((FeedItem) -> Unit)? = null,
    onToggleLike: ((FeedItem) -> Unit)? = null,
    onOpenEditHistory: ((FeedItem) -> Unit)? = null,
    enableSettings: Boolean = true,
    storedAccounts: List<StoredWeiboAccount> = emptyList(),
    activeAccountId: String? = null,
    onSwitchAccount: (String) -> Unit = {},
    onPrepareAddAccount: suspend () -> Unit = {},
    onPersistLoginSession: suspend () -> Unit = {},
    onReturnToFeed: () -> Unit = {},
    pendingOpenAccountLogin: Boolean = false,
    onPendingOpenAccountLoginConsumed: () -> Unit = {},
    backgroundPlaybackEnabled: Boolean = false,
    onBackgroundPlaybackChange: (Boolean) -> Unit = {},
    showFollowActions: Boolean = false,
    followLoading: Boolean = false,
    onFollowClick: () -> Unit = {},
) {
    var showSettings by remember { mutableStateOf(false) }
    var showAccountManagement by remember { mutableStateOf(false) }
    val pagerState = rememberPagerState(
        initialPage = initialPagerPage.coerceIn(0, MineContentTab.entries.lastIndex),
        pageCount = { MineContentTab.entries.size },
    )
    val coroutineScope = rememberCoroutineScope()
    val captureAlbumViewerOpen: (AlbumViewerState) -> Unit = { state ->
        onOpenAlbumViewer(
            state.copy(
                profilePagerPage = pagerState.currentPage,
                albumScrollIndex = albumListState.firstVisibleItemIndex,
                albumScrollOffset = albumListState.firstVisibleItemScrollOffset,
            ),
        )
    }

    LaunchedEffect(initialPagerPage) {
        val target = initialPagerPage.coerceIn(0, MineContentTab.entries.lastIndex)
        if (pagerState.currentPage != target) {
            pagerState.scrollToPage(target)
        }
    }

    LaunchedEffect(pendingOpenAccountLogin) {
        if (!pendingOpenAccountLogin) return@LaunchedEffect
        onPendingOpenAccountLoginConsumed()
        coroutineScope.launch {
            onPrepareAddAccount()
            showSettings = true
            showAccountManagement = true
        }
    }

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }
            .collect { page -> onMinePagerPageChanged(page) }
    }

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
                onBack = {
                    coroutineScope.launch {
                        if (session.hasLoginCookie()) {
                            onPersistLoginSession()
                        }
                    }
                    showAccountManagement = false
                },
            ) {
                AccountLoginPanel(
                    session = session,
                    onReturnToFeed = {
                        coroutineScope.launch {
                            onPersistLoginSession()
                        }
                        showAccountManagement = false
                        showSettings = false
                        onReturnToFeed()
                    },
                )
            }
        } else {
            SettingsScreen(
                hasLoginCookie = hasLoginCookie,
                accounts = storedAccounts,
                activeAccountId = activeAccountId,
                emoticonMap = emoticonMap,
                emoticonSyncing = emoticonSyncing,
                backgroundPlaybackEnabled = backgroundPlaybackEnabled,
                onBackgroundPlaybackChange = onBackgroundPlaybackChange,
                onBack = {
                    showAccountManagement = false
                    showSettings = false
                },
                onSwitchAccount = onSwitchAccount,
                onAddAccount = {
                    coroutineScope.launch {
                        onPrepareAddAccount()
                        showAccountManagement = true
                    }
                },
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

    val mineTabScrollPosition by remember {
        derivedStateOf {
            pagerState.currentPage + pagerState.currentPageOffsetFraction
        }
    }

    val density = LocalDensity.current
    val collapseThresholdPx = remember(density) { with(density) { 72.dp.roundToPx() } }
    val compactBarContentHeight = 48.dp
    val activeListState = when (pagerState.currentPage) {
        MineContentTab.Posts.ordinal -> postsListState
        else -> albumListState
    }
    val collapseProgress by remember(activeListState, collapseThresholdPx) {
        derivedStateOf {
            when {
                activeListState.firstVisibleItemIndex > 0 -> 1f
                else -> (activeListState.firstVisibleItemScrollOffset.toFloat() / collapseThresholdPx)
                    .coerceIn(0f, 1f)
            }
        }
    }
    val animatedCollapseAnim = remember { Animatable(0f) }
    var collapseAnimationReady by remember { mutableStateOf(false) }
    LaunchedEffect(collapseProgress) {
        if (!collapseAnimationReady) {
            animatedCollapseAnim.snapTo(collapseProgress)
            collapseAnimationReady = true
        } else {
            animatedCollapseAnim.animateTo(
                collapseProgress,
                animationSpec = tween(durationMillis = 220),
            )
        }
    }
    val animatedCollapse = animatedCollapseAnim.value
    val compactBarHeight = (topInset + compactBarContentHeight) * animatedCollapse
    val profileHeaderSlotHeight = when {
        profileHeaderHeight > 0.dp ->
            (profileHeaderHeight * (1f - animatedCollapse)).coerceAtLeast(0.dp)
        animatedCollapse >= 1f -> 0.dp
        else -> Dp.Unspecified
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        AppPullToRefreshBox(
            isRefreshing = isLoading,
            onRefresh = onRefresh,
            modifier = Modifier.fillMaxWidth().weight(1f),
        ) {
            Column(Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(compactBarHeight)
                        .graphicsLayer { clip = true },
                ) {
                    if (animatedCollapse > 0.01f) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(topInset + compactBarContentHeight)
                                .background(Color.White)
                                .padding(
                                    top = topInset,
                                    start = 16.dp,
                                    end = if (showFollowActions) 12.dp else 4.dp,
                                )
                                .graphicsLayer { alpha = animatedCollapse },
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            RemoteImage(
                                url = profile?.avatarUrl,
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop,
                            )
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.Center,
                            ) {
                                Text(
                                    text = profile?.screenName
                                        ?: if (hasLoginCookie) "\u5FAE\u535A\u7528\u6237" else "\u672A\u767B\u5F55",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                            if (showFollowActions && profile?.id?.isNotBlank() == true) {
                                ProfileFollowCapsuleButton(
                                    following = profile.following,
                                    loading = followLoading,
                                    onClick = onFollowClick,
                                )
                            } else if (enableSettings) {
                                IconButton(
                                    onClick = { showSettings = true },
                                    modifier = Modifier.size(40.dp),
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
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(
                            if (profileHeaderSlotHeight != Dp.Unspecified) {
                                Modifier.height(profileHeaderSlotHeight)
                            } else {
                                Modifier
                            },
                        )
                        .clipToBounds(),
                    contentAlignment = Alignment.TopStart,
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight(unbounded = true)
                            .onGloballyPositioned { coordinates ->
                                if (coordinates.size.height <= 0) return@onGloballyPositioned
                                val measured = with(density) {
                                    coordinates.size.height.toDp()
                                }
                                if (measured != profileHeaderHeight) {
                                    onProfileHeaderHeightChange(measured)
                                }
                            },
                    ) {
                        MineProfileHeader(
                            profile = profile,
                            hasLoginCookie = hasLoginCookie,
                            loadError = loadError,
                            onOpenSettings = if (enableSettings) {
                                { showSettings = true }
                            } else {
                                null
                            },
                            showFollowActions = showFollowActions,
                            followLoading = followLoading,
                            onFollowClick = onFollowClick,
                        )
                    }
                }

                MineContentTabs(
                    scrollPosition = mineTabScrollPosition,
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
                                            isLongTextLoading = isLongTextLoading,
                                            onLoadLongText = onLoadLongText,
                                            onToggleLike = onToggleLike,
                                            onOpenEditHistory = onOpenEditHistory,
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
                                        albumImages = albumImages,
                                        albumError = albumError,
                                        albumLoading = albumLoading || albumLoadingMore || isLoading,
                                        onOpenAlbumViewer = captureAlbumViewerOpen,
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
    accounts: List<StoredWeiboAccount>,
    activeAccountId: String?,
    emoticonMap: Map<String, String>,
    emoticonSyncing: Boolean,
    backgroundPlaybackEnabled: Boolean,
    onBackgroundPlaybackChange: (Boolean) -> Unit,
    onBack: () -> Unit,
    onSwitchAccount: (String) -> Unit,
    onAddAccount: () -> Unit,
    onSyncEmoticons: () -> Unit,
) {
    var accountExpanded by remember { mutableStateOf(false) }
    var emoticonExpanded by remember { mutableStateOf(false) }
    val emoticonCount = emoticonMap.size

    SettingsPageShell(title = "设置", onBack = onBack) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                SettingsAccountCard(
                    expanded = accountExpanded,
                    onExpandedChange = { accountExpanded = it },
                    accounts = accounts,
                    activeAccountId = activeAccountId,
                    hasLoginCookie = hasLoginCookie,
                    onSwitchAccount = onSwitchAccount,
                    onAddAccount = onAddAccount,
                )
            }
            item {
                SettingsEmoticonCard(
                    expanded = emoticonExpanded,
                    onExpandedChange = { emoticonExpanded = it },
                    emoticonMap = emoticonMap,
                    emoticonSyncing = emoticonSyncing,
                    onSyncEmoticons = onSyncEmoticons,
                )
            }
            item {
                SettingsPlaybackCard(
                    backgroundPlaybackEnabled = backgroundPlaybackEnabled,
                    onBackgroundPlaybackChange = onBackgroundPlaybackChange,
                )
            }
        }
    }
}

@Composable
private fun SettingsPlaybackCard(
    backgroundPlaybackEnabled: Boolean,
    onBackgroundPlaybackChange: (Boolean) -> Unit,
) {
    ElevatedCard(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = Color.White,
        ),
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
                Text(
                    text = "后台播放声音",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = if (backgroundPlaybackEnabled) {
                        "返回桌面时继续播放视频声音"
                    } else {
                        "返回桌面时自动暂停视频"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Switch(
                checked = backgroundPlaybackEnabled,
                onCheckedChange = onBackgroundPlaybackChange,
            )
        }
    }
}

@Composable
private fun weiboMetaTextColor(): Color =
    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.58f)

@Composable
private fun SettingsExpandIndicator(
    expanded: Boolean,
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.onSurfaceVariant,
) {
    val rotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = tween(200),
        label = "settings_expand_chevron",
    )
    Icon(
        painter = painterResource(R.drawable.ic_chevron_down),
        contentDescription = null,
        modifier = modifier
            .size(20.dp)
            .graphicsLayer { rotationZ = rotation },
        tint = tint,
    )
}

@Composable
private fun SettingsAccountCard(
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    accounts: List<StoredWeiboAccount>,
    activeAccountId: String?,
    hasLoginCookie: Boolean,
    onSwitchAccount: (String) -> Unit,
    onAddAccount: () -> Unit,
) {
    val activeAccount = accounts.firstOrNull { it.id == activeAccountId }
    val subtitle = when {
        activeAccount != null -> "${activeAccount.screenName}（UID ${activeAccount.id}）"
        accounts.isNotEmpty() -> "已保存 ${accounts.size} 个账号，点击展开切换"
        hasLoginCookie -> "已登录，点击展开管理账号"
        else -> "登录微博以读取主页、信息流与评论"
    }
    val status = when {
        accounts.isNotEmpty() -> "${accounts.size} 个账号"
        hasLoginCookie -> "已登录"
        else -> "未登录"
    }

    ElevatedCard(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = Color.White,
        ),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onExpandedChange(!expanded) }
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
                            text = "账号管理",
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
                SettingsExpandIndicator(expanded = expanded)
            }

            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, bottom = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    if (accounts.isEmpty()) {
                        Text(
                            text = "暂无已保存账号",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 4.dp),
                        )
                    } else {
                        accounts.forEach { account ->
                            val isActive = account.id == activeAccountId
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (isActive) {
                                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                                        } else {
                                            StatusQuotedBackground
                                        }
                                    )
                                    .clickable(enabled = !isActive) { onSwitchAccount(account.id) }
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                RemoteImage(
                                    url = account.avatarUrl,
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop,
                                )
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(2.dp),
                                ) {
                                    Text(
                                        text = account.screenName.ifBlank { "微博用户" },
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Medium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                    Text(
                                        text = "UID ${account.id}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                                if (isActive) {
                                    Text(
                                        text = "当前",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.SemiBold,
                                    )
                                }
                            }
                        }
                    }
                    TextButton(
                        onClick = onAddAccount,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("添加账号")
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsEmoticonCard(
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    emoticonMap: Map<String, String>,
    emoticonSyncing: Boolean,
    onSyncEmoticons: () -> Unit,
) {
    val emoticonCount = emoticonMap.size
    val sortedEmoticons = remember(emoticonMap) {
        emoticonMap.entries.sortedBy { it.key }
    }
    val subtitle = if (emoticonCount > 0) {
        "本地已缓存 $emoticonCount 个微博表情，点击展开查看"
    } else {
        "本地暂无表情缓存，同步后可提升正文与评论表情显示"
    }
    val status = when {
        emoticonSyncing -> "同步中"
        emoticonCount > 0 -> "本地 $emoticonCount 个"
        else -> "未同步"
    }

    ElevatedCard(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = Color.White,
        ),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onExpandedChange(!expanded) }
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
                            text = "表情同步",
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
                if (emoticonSyncing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        strokeWidth = 2.dp,
                    )
                } else {
                    SettingsExpandIndicator(expanded = expanded)
                }
            }

            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, bottom = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    if (sortedEmoticons.isEmpty()) {
                        Text(
                            text = "暂无本地表情",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 4.dp),
                        )
                    } else {
                        BoxWithConstraints(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 360.dp)
                                .verticalScroll(rememberScrollState()),
                        ) {
                            val gap = 6.dp
                            val cellWidth = (maxWidth - gap * 6) / 7
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(gap),
                                verticalArrangement = Arrangement.spacedBy(gap),
                                maxItemsInEachRow = 7,
                            ) {
                                sortedEmoticons.forEach { (phrase, url) ->
                                    SettingsEmoticonTile(
                                        phrase = phrase,
                                        url = url,
                                        width = cellWidth,
                                    )
                                }
                            }
                        }
                    }
                    TextButton(
                        onClick = onSyncEmoticons,
                        enabled = !emoticonSyncing,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        if (emoticonSyncing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                            )
                            Spacer(Modifier.width(8.dp))
                        }
                        Text(if (emoticonCount > 0) "更新表情" else "同步表情")
                    }
                }
            }
        }
    }
}

private fun emoticonDisplayLabel(phrase: String): String {
    if (phrase.length >= 2 && phrase.startsWith("[") && phrase.endsWith("]")) {
        return phrase.substring(1, phrase.length - 1)
    }
    return phrase
}

@Composable
private fun SettingsEmoticonTile(
    phrase: String,
    url: String,
    width: Dp,
) {
    Column(
        modifier = Modifier.width(width),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        Box(
            modifier = Modifier.size(width),
            contentAlignment = Alignment.Center,
        ) {
            EmojiImage(url = url)
        }
        Text(
            text = emoticonDisplayLabel(phrase),
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 9.sp,
                lineHeight = 10.sp,
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
        )
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
        colors = CardDefaults.elevatedCardColors(
            containerColor = Color.White,
        ),
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
private fun ProfileCoverBanner(
    coverUrls: List<String>,
    modifier: Modifier = Modifier,
    onOpenSettings: (() -> Unit)? = null,
) {
    val coverImages = remember(coverUrls) { WeiboJsonParser.profileCoverImages(coverUrls) }
    var viewerOpen by remember { mutableStateOf(false) }
    var viewerIndex by remember { mutableStateOf(0) }
    val pagerState = rememberPagerState(pageCount = { coverImages.size.coerceAtLeast(1) })

    Box(modifier) {
        if (coverImages.isNotEmpty()) {
            if (coverImages.size == 1) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable {
                            viewerIndex = 0
                            viewerOpen = true
                        },
                ) {
                    val image = coverImages[0]
                    RemoteImage(
                        url = image.largeUrl,
                        fallbackUrls = image.downloadUrls.drop(1),
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                    )
                }
            } else {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize(),
                    beyondViewportPageCount = 1,
                ) { page ->
                    val image = coverImages[page]
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable {
                                viewerIndex = page
                                viewerOpen = true
                            },
                    ) {
                        RemoteImage(
                            url = image.largeUrl,
                            fallbackUrls = image.downloadUrls.drop(1),
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                        )
                    }
                }
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    coverImages.indices.forEach { index ->
                        val selected = pagerState.currentPage == index
                        Box(
                            modifier = Modifier
                                .size(if (selected) 7.dp else 6.dp)
                                .clip(CircleShape)
                                .background(
                                    if (selected) Color.White.copy(alpha = 0.95f)
                                    else Color.White.copy(alpha = 0.45f),
                                ),
                        )
                    }
                }
            }
        }

        if (onOpenSettings != null) {
            IconButton(
                onClick = onOpenSettings,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 32.dp, end = 10.dp)
                    .size(40.dp),
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_settings),
                    contentDescription = "\u8BBE\u7F6E",
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }

    if (viewerOpen && coverImages.isNotEmpty()) {
        FullscreenImageViewer(
            images = coverImages,
            initialIndex = viewerIndex.coerceIn(0, coverImages.lastIndex),
            onDismiss = { viewerOpen = false },
        )
    }
}

@Composable
private fun MineProfileHeader(
    profile: UserProfile?,
    hasLoginCookie: Boolean,
    loadError: String?,
    onOpenSettings: (() -> Unit)?,
    showFollowActions: Boolean = false,
    followLoading: Boolean = false,
    onFollowClick: () -> Unit = {},
) {
    ElevatedCard(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = Color.White,
        ),
    ) {
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
                                StatusQuotedBackground,
                            )
                        )
                    )
            ) {
                ProfileCoverBanner(
                    coverUrls = profile?.coverUrls.orEmpty(),
                    modifier = Modifier.fillMaxSize(),
                    onOpenSettings = onOpenSettings,
                )
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

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    MineInlineStats(
                        profile = profile,
                        modifier = Modifier.weight(1f),
                    )
                    if (showFollowActions && profile?.id?.isNotBlank() == true) {
                        ProfileFollowCapsuleButton(
                            following = profile.following,
                            loading = followLoading,
                            onClick = onFollowClick,
                        )
                    }
                }
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

private val WeiboFollowOrange = Color(0xFFFF8200)

@Composable
private fun ProfileFollowCapsuleButton(
    following: Boolean,
    loading: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        enabled = !loading,
        shape = RoundedCornerShape(999.dp),
        color = if (following) Color.White else WeiboFollowOrange,
        border = if (following) BorderStroke(1.dp, Color(0xFFE0E0E0)) else null,
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center,
        ) {
            if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(14.dp),
                    color = if (following) WeiboFollowOrange else Color.White,
                    strokeWidth = 2.dp,
                )
            } else {
                Text(
                    text = if (following) "\u5DF2\u5173\u6CE8" else "+\u5173\u6CE8",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium,
                    color = if (following) Color(0xFF636363) else Color.White,
                )
            }
        }
    }
}

@Composable
private fun MineInlineStats(
    profile: UserProfile?,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
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
    scrollPosition: Float,
    onTabSelected: (MineContentTab) -> Unit,
) {
    val tabs = MineContentTab.entries
    val accent = Color(0xFFFF4F9A)
    val density = LocalDensity.current
    val indicatorWidth = 22.dp
    val tabCentersPx = remember { FloatArray(tabs.size) { Float.NaN } }
    var layoutReady by remember { mutableStateOf(false) }
    val highlightedIndex = scrollPosition
        .roundToInt()
        .coerceIn(0, tabs.lastIndex)

    val indicatorCenterPx = if (!layoutReady || tabs.size == 1) {
        tabCentersPx.firstOrNull { !it.isNaN() } ?: 0f
    } else {
        val position = scrollPosition.coerceIn(0f, tabs.lastIndex.toFloat())
        val left = position.toInt()
        val right = (left + 1).coerceAtMost(tabs.lastIndex)
        val fraction = position - left
        tabCentersPx[left] + (tabCentersPx[right] - tabCentersPx[left]) * fraction
    }
    val indicatorOffset = with(density) { indicatorCenterPx.toDp() - indicatorWidth / 2 }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 12.dp, top = 2.dp, bottom = 2.dp),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(22.dp),
            verticalAlignment = Alignment.Top,
        ) {
            tabs.forEachIndexed { index, tab ->
                val selected = index == highlightedIndex
                Column(
                    modifier = Modifier
                        .onGloballyPositioned { coords ->
                            tabCentersPx[index] = coords.positionInParent().x + coords.size.width / 2f
                            if (!layoutReady && tabCentersPx.all { !it.isNaN() }) {
                                layoutReady = true
                            }
                        }
                        .clip(RoundedCornerShape(3.dp))
                        .clickable { onTabSelected(tab) }
                        .padding(horizontal = 2.dp, vertical = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = tab.label,
                        fontSize = 14.sp,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (selected) accent else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(3.dp))
                }
            }
        }

        if (layoutReady) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .offset(x = indicatorOffset)
                    .width(indicatorWidth)
                    .height(3.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(accent),
            )
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
    albumImages: List<FeedImage>,
    albumError: String? = null,
    albumLoading: Boolean = false,
    onOpenAlbumViewer: (AlbumViewerState) -> Unit,
    onMediaClick: (FeedMedia) -> Unit,
) {
    val albumGrouped = remember(albumImages) {
        albumImages
            .distinctBy { it.largeUrl }
            .mapNotNull { image ->
                albumMonthLabel(image.createdAt)?.let { label -> label to image }
            }
            .groupBy({ it.first }, { it.second })
            .mapValues { entry -> entry.value.distinctBy { it.largeUrl } }
            .entries
            .sortedWith(
                compareByDescending<Map.Entry<Pair<String, String>, List<FeedImage>>> {
                    albumGroupSortKey(it.key)
                },
            )
            .associate { it.key to it.value }
    }

    if (albumGrouped.isEmpty()) {
        EmptyState(
            title = "\u6682\u672A\u8BFB\u5230\u76F8\u518C",
            body = when {
                albumLoading -> "\u6B63\u5728\u52A0\u8F7D\u76F8\u518C\u2026"
                !albumError.isNullOrBlank() -> albumError
                else -> "\u4E0B\u62C9\u5237\u65B0\u540E\u4F1A\u4ECE\u76F8\u518C\u63A5\u53E3\u52A0\u8F7D\u56FE\u7247\u3002"
            },
        )
        return
    }

    Column(
        modifier = Modifier.padding(horizontal = 12.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        albumGrouped.forEach { (dateLabel, images) ->
            MineAlbumMonthSection(
                dateLabel = dateLabel,
                images = images,
                onImageClick = { groupImages, index ->
                    onOpenAlbumViewer(AlbumViewerState(groupImages, index))
                },
            )
        }
    }
}

private suspend fun resolveAlbumImageStatus(
    image: FeedImage,
    posts: List<FeedItem>,
    session: WeiboWebSession,
): FeedItem? {
    findPostForAlbumImage(posts, image)?.let { return it }
    val statusId = image.statusId?.takeIf { it.isNotBlank() } ?: return null
    return session.loadStatusDetail(statusId)
}

private fun findPostForAlbumImage(posts: List<FeedItem>, image: FeedImage): FeedItem? {
    val statusId = image.statusId?.takeIf { it.isNotBlank() }
    if (statusId != null) {
        posts.firstOrNull { post ->
            post.id == statusId || post.statusId == statusId
        }?.let { return it }
    }
    val imageKeys = albumImageMatchKeys(image)
    if (imageKeys.isEmpty()) return null
    return posts.firstOrNull { post ->
        postImageMatchKeys(post).any { it in imageKeys }
    }
}

private fun postImageMatchKeys(post: FeedItem): Set<String> {
    val keys = mutableSetOf<String>()
    post.images.forEach { keys += albumImageMatchKeys(it) }
    post.retweetedStatus?.images?.forEach { keys += albumImageMatchKeys(it) }
    return keys
}

private fun albumImageMatchKeys(image: FeedImage): Set<String> {
    val keys = mutableSetOf<String>()
    sinaimgPid(image.largeUrl)?.let(keys::add)
    sinaimgPid(image.thumbnailUrl)?.let(keys::add)
    image.downloadUrls.forEach { url -> sinaimgPid(url)?.let(keys::add) }
    if (image.id.isNotBlank() && !image.id.startsWith("http")) {
        keys += image.id.substringBeforeLast('.')
    }
    return keys
}

private fun sinaimgPid(url: String): String? =
    Regex("""/([^/?#]+)\.(?:jpg|jpeg|png|gif|webp)""", RegexOption.IGNORE_CASE)
        .find(url)
        ?.groupValues
        ?.getOrNull(1)

@Composable
private fun MineAlbumMonthSection(
    dateLabel: Pair<String, String>,
    images: List<FeedImage>,
    onImageClick: (List<FeedImage>, Int) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top,
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
        BoxWithConstraints(
            modifier = Modifier
                .weight(1f)
                .wrapContentHeight(),
        ) {
            val gap = 6.dp
            val cellSize = (maxWidth - gap * 2) / 3
            Column(verticalArrangement = Arrangement.spacedBy(gap)) {
                var imageIndex = 0
                images.chunked(3).forEach { rowImages ->
                    Row(horizontalArrangement = Arrangement.spacedBy(gap)) {
                        rowImages.forEach { image ->
                            val currentIndex = imageIndex++
                            MineAlbumTile(
                                image = image,
                                size = cellSize,
                                onClick = { onImageClick(images, currentIndex) },
                            )
                        }
                    }
                }
            }
        }
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
            animated = image.isGif,
        )
        if (image.isLivePhoto || image.isGif) {
            Surface(
                modifier = Modifier.align(Alignment.BottomEnd).padding(4.dp),
                shape = CircleShape,
                color = Color.Black.copy(alpha = 0.42f),
                contentColor = Color.White,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_live_photo),
                    contentDescription = if (image.isGif) "GIF" else "LivePhoto",
                    modifier = Modifier
                        .padding(2.dp)
                        .size(11.dp),
                    tint = Color.White,
                )
            }
        }
    }
}

private fun filterOutRetweetedOnlyImages(images: List<FeedImage>, posts: List<FeedItem>): List<FeedImage> {
    if (images.isEmpty() || posts.isEmpty()) return images
    val ownUrls = posts.flatMap { it.images }.mapTo(mutableSetOf()) { it.largeUrl }
    val retweetedUrls = posts.flatMap { it.retweetedStatus?.images ?: emptyList() }
        .mapTo(mutableSetOf()) { it.largeUrl }
    if (retweetedUrls.isEmpty()) return images
    return images.filterNot { image -> image.largeUrl in retweetedUrls && image.largeUrl !in ownUrls }
}

private fun albumMonthLabel(createdAt: String?): Pair<String, String>? {
    val label = albumDateLabel(createdAt)
    return label.takeUnless { it.first == "\u5168\u90E8" }
}

private fun albumGroupSortKey(label: Pair<String, String>): Long {
    val (monthLabel, yearLabel) = label
    val year = yearLabel.toIntOrNull() ?: 0
    val month = monthLabel.removeSuffix("\u6708").toIntOrNull() ?: 0
    return year * 100L + month
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
    ElevatedCard(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = Color.White,
        ),
    ) {
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
private fun AccountLoginPanel(
    session: WeiboWebSession,
    onReturnToFeed: () -> Unit,
) {
    LaunchedEffect(Unit) {
        session.openLogin()
    }

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
                Text("\u5FAE\u535A\u767B\u5F55", fontWeight = FontWeight.SemiBold)
                Text(
                    "\u8FD9\u91CC\u4F7F\u7528\u684C\u9762 Chrome \u6807\u8BC6\u6253\u5F00 passport.weibo.cn\u3002\u767B\u5F55\u6210\u529F\u540E\u70B9\u51FB\u56DE\u5230\u5FAE\u535A\u9996\u9875\uFF0C\u5C06\u8DF3\u8F6C\u5230\u9996\u9875\u5E76\u540C\u6B65\u6570\u636E\u3002",
                    style = MaterialTheme.typography.bodySmall,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = onReturnToFeed) {
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
                    "\u8FD9\u91CC\u4F7F\u7528\u684C\u9762 Chrome \u6807\u8BC6\u6253\u5F00 passport.weibo.cn\u3002\u767B\u5F55\u6210\u529F\u540E\u56DE\u5230\u5FAE\u535A\u9996\u9875\uFF0C\u518D\u5230\u9996\u9875\u5237\u65B0\u3002",
                    style = MaterialTheme.typography.bodySmall,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = { session.openWeiboHome() }) {
                        Text("\u5B8C\u6210\u767B\u5F55\uFF0C\u56DE\u5230\u5FAE\u535A\u9996\u9875")
                    }
                    TextButton(onClick = { session.openLogin() }) {
                        Text("\u91CD\u65B0\u6253\u5F00")
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
    val context = LocalContext.current
    val videoCoordinator = LocalVideoPlaybackCoordinator.current
    val activity = context as? android.app.Activity

    DisposableEffect(media) {
        videoCoordinator.activeKey = null
        onDispose { }
    }

    DisposableEffect(activity) {
        if (activity == null) return@DisposableEffect onDispose {}
        val window = activity.window
        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        insetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        insetsController.hide(WindowInsetsCompat.Type.systemBars())
        onDispose {
            insetsController.show(WindowInsetsCompat.Type.systemBars())
            WindowCompat.setDecorFitsSystemWindows(window, true)
        }
    }

    BackHandler(onBack = onDismiss)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center,
    ) {
        WeiboVideoSurface(
            media = media,
            isFullscreen = true,
            onAspectRatio = {},
            onFullscreen = onDismiss,
            onEnterPictureInPicture = onDismiss,
            modifier = Modifier.fillMaxSize(),
        )
    }
}

@Composable
private fun RemoteImage(
    url: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    animated: Boolean = false,
    fallbackUrls: List<String> = emptyList(),
) {
    if (animated) {
        AnimatedRemoteImage(
            url = url,
            modifier = modifier,
            contentScale = contentScale,
        )
        return
    }

    val candidates = remember(url, fallbackUrls) {
        (listOfNotNull(url?.takeIf { it.isNotBlank() }) + fallbackUrls).distinct()
    }
    var candidateIndex by remember(candidates) { mutableStateOf(0) }
    val currentUrl = candidates.getOrNull(candidateIndex)
    var bitmap by remember(currentUrl) { mutableStateOf<android.graphics.Bitmap?>(null) }
    var failed by remember(currentUrl) { mutableStateOf(false) }

    LaunchedEffect(currentUrl, candidateIndex) {
        bitmap = null
        failed = false
        val target = currentUrl ?: return@LaunchedEffect
        runCatching {
            withContext(Dispatchers.IO) {
                val bytes = fetchRemoteBytes(target, connectTimeoutMs = 8000, readTimeoutMs = 8000)

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
            .onFailure {
                if (candidateIndex < candidates.lastIndex) {
                    candidateIndex += 1
                } else {
                    failed = true
                }
            }
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
private fun AnimatedRemoteImage(
    url: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
) {
    var drawable by remember(url) { mutableStateOf<Drawable?>(null) }
    var failed by remember(url) { mutableStateOf(false) }

    LaunchedEffect(url) {
        drawable = null
        failed = false
        val target = url?.takeIf { it.isNotBlank() } ?: return@LaunchedEffect
        runCatching {
            withContext(Dispatchers.IO) {
                loadRemoteAnimatedDrawable(target)
            }
        }.onSuccess { drawable = it }
            .onFailure { failed = true }
    }

    Box(
        modifier = modifier.background(MaterialTheme.colorScheme.surfaceContainerHighest),
        contentAlignment = Alignment.Center,
    ) {
        val image = drawable
        if (image != null) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { context ->
                    ImageView(context).apply {
                        adjustViewBounds = false
                        scaleType = imageViewScaleType(contentScale)
                        setImageDrawable(image)
                        (drawable as? AnimatedImageDrawable)?.start()
                    }
                },
                update = { view ->
                    view.scaleType = imageViewScaleType(contentScale)
                    if (view.drawable !== image) {
                        view.setImageDrawable(image)
                    }
                    (image as? AnimatedImageDrawable)?.start()
                },
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

private fun imageViewScaleType(contentScale: ContentScale): ImageView.ScaleType =
    when (contentScale) {
        ContentScale.Crop -> ImageView.ScaleType.CENTER_CROP
        ContentScale.FillBounds -> ImageView.ScaleType.FIT_XY
        else -> ImageView.ScaleType.FIT_CENTER
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

private object RemoteBytesCache {
    private const val MaxEntries = 36
    private const val MaxBytesPerEntry = 12 * 1024 * 1024
    private val entries = object : LinkedHashMap<String, ByteArray>(MaxEntries, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, ByteArray>?): Boolean =
            size > MaxEntries
    }

    @Synchronized
    fun get(url: String): ByteArray? = entries[url]

    @Synchronized
    fun put(url: String, bytes: ByteArray) {
        if (bytes.size <= MaxBytesPerEntry) {
            entries[url] = bytes
        }
    }
}

private fun fetchRemoteBytes(
    url: String,
    connectTimeoutMs: Int,
    readTimeoutMs: Int,
): ByteArray {
    RemoteBytesCache.get(url)?.let { return it }
    val bytes = URL(url).openConnection().apply {
        (this as HttpURLConnection).connectTimeout = connectTimeoutMs
        readTimeout = readTimeoutMs
        setRequestProperty("User-Agent", DESKTOP_CHROME_USER_AGENT)
        setRequestProperty("Referer", "https://weibo.com/")
    }.inputStream.use { it.readBytes() }
    RemoteBytesCache.put(url, bytes)
    return bytes
}

private fun loadRemoteAnimatedDrawable(url: String): Drawable {
    val bytes = fetchRemoteBytes(url, connectTimeoutMs = 10_000, readTimeoutMs = 20_000)
    val source = ImageDecoder.createSource(ByteBuffer.wrap(bytes))
    return ImageDecoder.decodeDrawable(source)
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
            val bytes = fetchRemoteBytes(url, connectTimeoutMs = 10_000, readTimeoutMs = 20_000)
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
