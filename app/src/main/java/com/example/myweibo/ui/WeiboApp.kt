@file:Suppress("UnsafeOptInUsageError")

package com.example.myweibo.ui

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.content.ComponentCallbacks2
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.graphics.drawable.AnimatedImageDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.ColorDrawable
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.webkit.CookieManager
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOut
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationState
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.DecayAnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animate
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
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.gestures.AnchoredDraggableDefaults
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.ScrollScope
import androidx.compose.foundation.gestures.animateTo
import androidx.compose.foundation.gestures.anchoredDraggable
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
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imeAnimationTarget
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.gestures.animateScrollBy
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.rounded.Settings
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
import androidx.compose.material3.RadioButton
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
import androidx.compose.runtime.withFrameMillis
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.rotate
import androidx.compose.foundation.border
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.AwaitPointerEventScope
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.changedToUpIgnoreConsumed
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.zIndex
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.platform.ViewConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.lerp as lerpDp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
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
import com.example.myweibo.data.MentionSuggestionCacheStore
import com.example.myweibo.data.SearchHistoryStore
import com.example.myweibo.data.SearchSettingsStore
import com.example.myweibo.data.SearchSuggestResult
import com.example.myweibo.data.SearchUserItem
import com.example.myweibo.data.MentionCandidate
import com.example.myweibo.data.extractActiveMentionQuery
import com.example.myweibo.data.filterMentionCandidatesByQuery
import com.example.myweibo.data.mentionCandidateKey
import com.example.myweibo.data.ImageSaveHelper
import com.example.myweibo.data.LivePhotoMirrorCorrectionStore
import com.example.myweibo.data.LivePhotoMirrorDetector
import com.example.myweibo.data.ImageUrlResolver
import com.example.myweibo.data.BitmapExifOrientation
import com.example.myweibo.data.buildSaveMetadata
import com.example.myweibo.data.resolveForSave
import com.example.myweibo.data.FriendListTab
import com.example.myweibo.data.HotSearchItem
import com.example.myweibo.data.RelationUser
import com.example.myweibo.data.toRelationUser
import com.example.myweibo.data.toUserProfile
import com.example.myweibo.data.FeedImage
import com.example.myweibo.data.isSavableToAlbum
import com.example.myweibo.data.toAlbumFeedMedia
import com.example.myweibo.data.FeedItem
import com.example.myweibo.data.FeedUrlEntity
import com.example.myweibo.data.LikeUsersPage
import com.example.myweibo.data.ProfileLookup
import com.example.myweibo.data.FeedMedia
import com.example.myweibo.data.MediaUrlResolver
import com.example.myweibo.data.MediaType
import com.example.myweibo.data.MineCacheStore
import com.example.myweibo.data.FeedThumbnailQuality
import com.example.myweibo.data.FeedFontSize
import com.example.myweibo.data.FeedLineSpacing
import com.example.myweibo.data.ImageSettingsStore
import com.example.myweibo.data.PlaybackSettingsStore
import com.example.myweibo.data.TypographySettingsStore
import com.example.myweibo.data.MinePostsCache
import com.example.myweibo.data.NativeUiMessage
import com.example.myweibo.data.StoredWeiboAccount
import com.example.myweibo.data.AppearanceMode
import com.example.myweibo.data.ThemeSettingsStore
import com.example.myweibo.data.TimelineCacheStore
import com.example.myweibo.data.TimelineKind
import com.example.myweibo.data.UserProfile
import com.example.myweibo.data.WeiboStatusActions
import com.example.myweibo.data.WeiboAccountStore
import com.example.myweibo.data.WeiboJsonParser
import com.example.myweibo.data.WeiboPostVisibility
import com.example.myweibo.data.WeiboWebSession
import com.example.myweibo.data.WeiboArticle
import com.example.myweibo.data.WeiboLinkResolver
import com.example.myweibo.data.resolveArticleId
import com.example.myweibo.data.formatWeiboTime
import com.example.myweibo.data.parseWeiboCreatedAtMillis
import com.example.myweibo.data.collectEmoticons
import com.example.myweibo.data.collectAllEmoticons
import com.example.myweibo.data.collectAllCommentEmoticons
import com.example.myweibo.data.mergeFeedTimelinePages
import com.example.myweibo.data.sortFeedTimelineItems
import com.example.myweibo.ui.theme.MyWeiboTheme
import com.example.myweibo.ui.theme.isAppLightTheme
import com.example.myweibo.ui.theme.WeiboTopicBlue
import com.example.myweibo.ui.liquidglass.LocalHazeState
import com.example.myweibo.ui.liquidglass.LocalLiquidMenuBackdrop
import com.example.myweibo.ui.liquidglass.SurfaceLiquidCapsule
import com.example.myweibo.ui.liquidglass.SurfaceLiquidMenuCard
import com.example.myweibo.ui.liquidglass.TransparentLiquidCapsule
import com.example.myweibo.ui.liquidglass.TransparentLiquidIconButton
import com.example.myweibo.ui.liquidglass.TransparentLiquidTextButton
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import androidx.compose.foundation.MutatePriority
import androidx.compose.foundation.gestures.stopScroll
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.withContext
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.coroutines.yield
import okhttp3.ConnectionPool
import okhttp3.Dispatcher
import okhttp3.OkHttpClient
import okhttp3.Request
import java.nio.ByteBuffer
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

private enum class SearchMode(val label: String) {
    Weibo("微博"),
    User("用户"),
}

private enum class SearchWeiboSort(val label: String) {
    Comprehensive("综合"),
    Realtime("实时"),
}

private fun storedSearchMode(value: String): SearchMode =
    when (value) {
        SearchSettingsStore.MODE_USER -> SearchMode.User
        else -> SearchMode.Weibo
    }

private fun SearchMode.storageValue(): String =
    when (this) {
        SearchMode.Weibo -> SearchSettingsStore.MODE_WEIBO
        SearchMode.User -> SearchSettingsStore.MODE_USER
    }

private fun storedSearchWeiboSort(value: String): SearchWeiboSort =
    when (value) {
        SearchSettingsStore.SORT_REALTIME -> SearchWeiboSort.Realtime
        else -> SearchWeiboSort.Comprehensive
    }

private fun SearchWeiboSort.storageValue(): String =
    when (this) {
        SearchWeiboSort.Comprehensive -> SearchSettingsStore.SORT_COMPREHENSIVE
        SearchWeiboSort.Realtime -> SearchSettingsStore.SORT_REALTIME
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

private const val ListScrollToTopDurationMillis = 320
private const val BottomBarCollapseScrollDelta = 12
private const val ListLoadMoreItemsFromBottom = 3

private inline fun <T> runCatchingPreservingCancellation(block: () -> T): Result<T> =
    try {
        Result.success(block())
    } catch (error: CancellationException) {
        throw error
    } catch (error: Throwable) {
        Result.failure(error)
    }

private const val MediaLongPressTimeoutMillis = 500L
private const val MediaPeekCompressScale = 0.968f
private const val MediaPeekPopPeakScale = 1.06f
private const val MediaPeekHoldCompressPhase = 0.82f
private const val MediaPeekCompressHapticProgress = 0.68f
private const val MediaPeekPopPhaseEnd = 0.36f
private const val MediaPeekPopHoldFraction = 0.14f
private const val MediaPeekFingerFollowStrength = 0.88f

private val MediaPeekCompressEasing = CubicBezierEasing(0.32f, 0f, 0.67f, 1f)
private val MediaPeekPopUpEasing = CubicBezierEasing(0.34f, 1.48f, 0.64f, 1f)
private val MediaPeekLayoutEasing = CubicBezierEasing(0.22f, 1f, 0.36f, 1f)
private val MediaPeekEnterEasing = CubicBezierEasing(0.22f, 1f, 0.36f, 1f)

private val MediaPeekEnterAnimationSpec = tween<Float>(
    durationMillis = 480,
    easing = MediaPeekEnterEasing,
)
private val MediaPeekDockAnimationSpec = tween<Float>(
    durationMillis = 380,
    easing = MediaPeekEnterEasing,
)
private val MediaPeekDismissAnimationSpec = tween<Float>(
    durationMillis = 220,
    easing = FastOutSlowInEasing,
)

private enum class MediaPeekHapticStage {
    Compress,
}

private class MediaPeekHaptics(
    private val composeHaptic: HapticFeedback,
    private val view: View,
) {
    private val vibrator = view.context.getSystemService(Vibrator::class.java)

    fun perform(stage: MediaPeekHapticStage) {
        if (stage != MediaPeekHapticStage.Compress) return
        try {
            composeHaptic.performHapticFeedback(HapticFeedbackType.LongPress)
            vibratePredefined(VibrationEffect.EFFECT_TICK)
        } catch (_: Exception) {
            try {
                composeHaptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            } catch (_: Exception) {
                // Ignore unsupported haptic effects on this device.
            }
        }
    }

    private fun vibratePredefined(effectId: Int) {
        val vibrator = vibrator ?: return
        if (!vibrator.hasVibrator()) return
        try {
            vibrator.vibrate(VibrationEffect.createPredefined(effectId))
        } catch (_: Exception) {
            // Predefined effect not supported on this device.
        }
    }
}

@Composable
private fun rememberMediaPeekHaptics(): MediaPeekHaptics {
    val composeHaptic = LocalHapticFeedback.current
    val view = LocalView.current
    return remember(composeHaptic, view) { MediaPeekHaptics(composeHaptic, view) }
}

private data class MediaPeekGraphicsMotion(
    val translationX: Float,
    val translationY: Float,
    val scaleX: Float,
    val scaleY: Float,
    val alpha: Float,
    val cornerRadius: Dp,
    val visualBounds: Rect,
    val fingerFollow: Float,
)

private fun Offset.toWindowPosition(anchorBounds: Rect): Offset =
    Offset(anchorBounds.left + x, anchorBounds.top + y)

private fun Rect.toOverlayLocal(overlayOriginInWindow: Offset): Rect =
    Rect(
        left = left - overlayOriginInWindow.x,
        top = top - overlayOriginInWindow.y,
        right = right - overlayOriginInWindow.x,
        bottom = bottom - overlayOriginInWindow.y,
    )

private fun isMediaAnchorVisibleOnScreen(
    anchorBounds: Rect,
    screenWidthPx: Float,
    screenHeightPx: Float,
    minVisiblePx: Float = 8f,
): Boolean {
    if (anchorBounds.width <= 0f || anchorBounds.height <= 0f) return false
    val visibleWidth = minOf(anchorBounds.right, screenWidthPx) - maxOf(anchorBounds.left, 0f)
    val visibleHeight = minOf(anchorBounds.bottom, screenHeightPx) - maxOf(anchorBounds.top, 0f)
    return visibleWidth >= minVisiblePx && visibleHeight >= minVisiblePx
}

private fun computeVideoPeekTopExitTargetBounds(
    sourceBounds: Rect,
    screenWidthPx: Float,
    statusBarTopPx: Float,
    aspectRatio: Float,
): Rect {
    val safeAspect = aspectRatio.coerceIn(0.25f, 4f)
    val targetWidth = sourceBounds.width.coerceAtMost(screenWidthPx * 0.72f) * 0.5f
    val targetHeight = targetWidth / safeAspect
    val centerX = screenWidthPx / 2f
    val centerY = maxOf(statusBarTopPx * 0.25f, targetHeight * 0.15f)
    return Rect(
        left = centerX - targetWidth / 2f,
        top = centerY - targetHeight / 2f,
        right = centerX + targetWidth / 2f,
        bottom = centerY + targetHeight / 2f,
    )
}

private fun resolveVideoPeekDismissTargetBounds(
    anchorInWindow: Rect,
    sourceBoundsInOverlay: Rect,
    screenWidthPx: Float,
    screenHeightPx: Float,
    statusBarTopPx: Float,
    aspectRatio: Float,
    overlayOriginInWindow: Offset,
    forceTopExit: Boolean = false,
): Pair<Rect, Boolean> {
    val anchorInOverlay = anchorInWindow.toOverlayLocal(overlayOriginInWindow)
    val useTopExit = forceTopExit ||
        !isMediaAnchorVisibleOnScreen(anchorInWindow, screenWidthPx, screenHeightPx)
    return if (useTopExit) {
        computeVideoPeekTopExitTargetBounds(
            sourceBounds = sourceBoundsInOverlay,
            screenWidthPx = screenWidthPx,
            statusBarTopPx = statusBarTopPx,
            aspectRatio = aspectRatio,
        ) to true
    } else {
        (anchorInOverlay.takeIf { it.width > 0f && it.height > 0f } ?: sourceBoundsInOverlay) to false
    }
}

private fun resolveVideoAnchorBounds(
    anchorCoordinates: LayoutCoordinates?,
    fallback: Rect?,
): Rect =
    anchorCoordinates?.takeIf { it.isAttached }?.boundsInWindow()
        ?: fallback
        ?: Rect.Zero

private fun mediaPeekHoldScale(holdProgress: Float): Float {
    val t = holdProgress.coerceIn(0f, 1f)
    if (t <= 0f) return 1f
    if (t >= 1f) return MediaPeekPopPeakScale
    return if (t < MediaPeekHoldCompressPhase) {
        val phase = t / MediaPeekHoldCompressPhase
        lerp(1f, MediaPeekCompressScale, MediaPeekCompressEasing.transform(phase))
    } else {
        val phase = (t - MediaPeekHoldCompressPhase) / (1f - MediaPeekHoldCompressPhase)
        lerp(MediaPeekCompressScale, MediaPeekPopPeakScale, MediaPeekPopUpEasing.transform(phase))
    }
}

private fun mediaPeekLayoutStartProgress(): Float =
    MediaPeekPopPhaseEnd + MediaPeekPopHoldFraction

private fun mediaPeekEnterScale(rawProgress: Float): Float {
    val t = rawProgress.coerceIn(0f, 1f)
    val popEnd = MediaPeekPopPhaseEnd
    val holdEnd = mediaPeekLayoutStartProgress().coerceAtMost(0.92f)
    return when {
        t <= popEnd -> {
            val phase = t / popEnd
            lerp(1f, MediaPeekPopPeakScale, MediaPeekPopUpEasing.transform(phase))
        }
        t <= holdEnd -> MediaPeekPopPeakScale
        else -> {
            val phase = (t - holdEnd) / (1f - holdEnd)
            lerp(MediaPeekPopPeakScale, 1f, MediaPeekLayoutEasing.transform(phase))
        }
    }
}

private fun mediaPeekLayoutProgress(rawProgress: Float): Float {
    val t = rawProgress.coerceIn(0f, 1f)
    val layoutStart = mediaPeekLayoutStartProgress()
    if (t <= layoutStart) return 0f
    val phase = ((t - layoutStart) / (1f - layoutStart)).coerceIn(0f, 1f)
    return MediaPeekLayoutEasing.transform(phase)
}

private fun computeMediaPeekGraphicsMotion(
    anchorBounds: Rect,
    layoutOriginLeftPx: Float,
    layoutOriginTopPx: Float,
    layoutWidthPx: Float,
    layoutHeightPx: Float,
    layoutProgress: Float,
    enterScale: Float,
    fingerFollowAlpha: Float,
    expandProgress: Float,
    expandWidthPx: Float,
    expandHeightPx: Float,
    expandCenterX: Float = expandWidthPx / 2f,
    expandCenterY: Float = expandHeightPx / 2f,
    dockProgress: Float = 0f,
    dockLeftPx: Float = layoutOriginLeftPx,
    dockTopPx: Float = layoutOriginTopPx,
    dockWidthPx: Float = layoutWidthPx,
    dockHeightPx: Float = layoutHeightPx,
): MediaPeekGraphicsMotion {
    val anchorCenterX = anchorBounds.center.x
    val anchorCenterY = anchorBounds.center.y
    val anchorWidth = anchorBounds.width.coerceAtLeast(1f)
    val anchorHeight = anchorBounds.height.coerceAtLeast(1f)

    val holdCenterX = layoutOriginLeftPx + layoutWidthPx / 2f
    val holdCenterY = layoutOriginTopPx + layoutHeightPx / 2f
    val dockCenterX = dockLeftPx + dockWidthPx / 2f
    val dockCenterY = dockTopPx + dockHeightPx / 2f

    val morphCenterX = lerp(anchorCenterX, holdCenterX, layoutProgress)
    val morphCenterY = lerp(anchorCenterY, holdCenterY, layoutProgress)
    val morphWidth = lerp(anchorWidth, layoutWidthPx, layoutProgress)
    val morphHeight = lerp(anchorHeight, layoutHeightPx, layoutProgress)

    val settledCenterX = lerp(morphCenterX, dockCenterX, dockProgress)
    val settledCenterY = lerp(morphCenterY, dockCenterY, dockProgress)
    val settledWidth = lerp(morphWidth, dockWidthPx, dockProgress)
    val settledHeight = lerp(morphHeight, dockHeightPx, dockProgress)

    val visualCenterX = lerp(settledCenterX, expandCenterX, expandProgress)
    val visualCenterY = lerp(settledCenterY, expandCenterY, expandProgress)
    val visualWidth = lerp(settledWidth, expandWidthPx, expandProgress)
    val visualHeight = lerp(settledHeight, expandHeightPx, expandProgress)

    val fingerFollow = fingerFollowAlpha * (0.28f + 0.72f * layoutProgress) * MediaPeekFingerFollowStrength
    val translationX = visualCenterX - holdCenterX
    val translationY = visualCenterY - holdCenterY

    val alpha = 1f
    val settledCornerRadius = 8.dp + 10.dp * layoutProgress
    val dockedCornerRadius = settledCornerRadius + (12.dp - settledCornerRadius) * dockProgress
    val cornerRadius = dockedCornerRadius * (1f - expandProgress)
    val visualBounds = Rect(
        left = visualCenterX - visualWidth / 2f,
        top = visualCenterY - visualHeight / 2f,
        right = visualCenterX + visualWidth / 2f,
        bottom = visualCenterY + visualHeight / 2f,
    )
    val uniformScale = computeMorphCoverScale(
        morphWidthPx = visualWidth,
        morphHeightPx = visualHeight,
        contentWidthPx = layoutWidthPx,
        contentHeightPx = layoutHeightPx,
    ) * enterScale
    return MediaPeekGraphicsMotion(
        translationX = translationX,
        translationY = translationY,
        scaleX = uniformScale,
        scaleY = uniformScale,
        alpha = alpha,
        cornerRadius = cornerRadius,
        visualBounds = visualBounds,
        fingerFollow = fingerFollow,
    )
}

private fun Modifier.mediaPeekMotionLayer(
    motion: MediaPeekGraphicsMotion,
    fingerDragOffset: () -> Offset,
    shadowElevation: Dp,
): Modifier {
    val cornerShape = RoundedCornerShape(motion.cornerRadius)
    return this
        .then(
            if (shadowElevation > 0.dp) {
                Modifier.shadow(shadowElevation, cornerShape, clip = false)
            } else {
                Modifier
            },
        )
        .graphicsLayer {
            val dragOffset = fingerDragOffset()
            translationX = motion.translationX + dragOffset.x * motion.fingerFollow
            translationY = motion.translationY + dragOffset.y * motion.fingerFollow
            scaleX = motion.scaleX
            scaleY = motion.scaleY
            alpha = motion.alpha.coerceIn(0f, 1f)
            transformOrigin = TransformOrigin(0.5f, 0.5f)
        }
        .clip(cornerShape)
}

private enum class MediaLongPressResult {
    LongPress,
    Tap,
    Cancelled,
}

private suspend fun AwaitPointerEventScope.awaitMediaLongPress(
    down: PointerInputChange,
    viewConfiguration: ViewConfiguration,
    onHoldProgress: (Float) -> Unit,
    onHaptic: ((MediaPeekHapticStage) -> Unit)? = null,
): MediaLongPressResult {
    var cancelledByMoveBeforeLongPress = false
    var releasedBeforeLongPress = false
    var compressHapticFired = false
    val longPressTimeout = viewConfiguration.longPressTimeoutMillis
    val longPressed = withTimeoutOrNull(longPressTimeout) {
        while (true) {
            val event = awaitPointerEvent(PointerEventPass.Initial)
            val change = event.changes.firstOrNull { it.id == down.id }
                ?: event.changes.firstOrNull()
                ?: return@withTimeoutOrNull false
            val elapsed = change.uptimeMillis - down.uptimeMillis
            val linearProgress = (elapsed.toFloat() / longPressTimeout).coerceIn(0f, 1f)
            onHoldProgress(linearProgress)
            if (!change.pressed) {
                releasedBeforeLongPress = true
                return@withTimeoutOrNull false
            }
            val preLongPressMove = change.position - down.position
            val holdMovement = hypot(preLongPressMove.x, preLongPressMove.y)
            if (holdMovement > viewConfiguration.touchSlop) {
                cancelledByMoveBeforeLongPress = true
                return@withTimeoutOrNull false
            }
            if (
                !compressHapticFired &&
                linearProgress >= MediaPeekCompressHapticProgress &&
                holdMovement <= viewConfiguration.touchSlop * 0.72f
            ) {
                compressHapticFired = true
                onHaptic?.invoke(MediaPeekHapticStage.Compress)
            }
        }
    } == null && !cancelledByMoveBeforeLongPress && !releasedBeforeLongPress
    if (longPressed) {
        onHoldProgress(1f)
        if (!compressHapticFired) {
            onHaptic?.invoke(MediaPeekHapticStage.Compress)
        }
    } else {
        onHoldProgress(0f)
    }
    return when {
        longPressed -> MediaLongPressResult.LongPress
        cancelledByMoveBeforeLongPress -> MediaLongPressResult.Cancelled
        else -> MediaLongPressResult.Tap
    }
}

private data class VideoPeekDragResult(
    val cancelledByDrag: Boolean,
    val floatByDrag: Boolean,
)

private suspend fun AwaitPointerEventScope.awaitVideoPeekDragGesture(
    down: PointerInputChange,
    pressWindowOffset: Offset,
    bounds: Rect,
    videoPeekController: VideoPeekController,
): VideoPeekDragResult {
    val dragGestureThreshold = 82f
    var lastPosition = down.position
    var cancelledByDrag = false
    var floatByDrag = false
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
        videoPeekController.updateFingerDragOffset(
            change.position.toWindowPosition(bounds) - pressWindowOffset,
        )
        val totalDrag = lastPosition - down.position
        val verticalDominant = abs(totalDrag.y) > abs(totalDrag.x) * 1.15f
        if (totalDrag.y > dragGestureThreshold && verticalDominant) {
            cancelledByDrag = true
            videoPeekController.cancel()
            while (true) {
                val consumeEvent = awaitPointerEvent(PointerEventPass.Initial)
                consumeEvent.changes.forEach { it.consume() }
                if (consumeEvent.changes.all { !it.pressed }) break
            }
            break
        }
        if (totalDrag.y < -dragGestureThreshold && verticalDominant) {
            floatByDrag = true
            videoPeekController.release()
            while (true) {
                val consumeEvent = awaitPointerEvent(PointerEventPass.Initial)
                consumeEvent.changes.forEach { it.consume() }
                if (consumeEvent.changes.all { !it.pressed }) break
            }
            break
        }
    }
    videoPeekController.resetFingerDragOffset()
    return VideoPeekDragResult(cancelledByDrag, floatByDrag)
}

@Composable
private fun MediaLongPressConfiguration(content: @Composable () -> Unit) {
    val base = LocalViewConfiguration.current
    val mediaConfig = remember(base) {
        object : ViewConfiguration by base {
            override val longPressTimeoutMillis: Long = MediaLongPressTimeoutMillis
        }
    }
    CompositionLocalProvider(LocalViewConfiguration provides mediaConfig, content = content)
}

private fun LazyListState.scrollDistanceToTop(): Int {
    if (firstVisibleItemIndex == 0) {
        return firstVisibleItemScrollOffset
    }

    layoutInfo.visibleItemsInfo
        .firstOrNull { it.index == firstVisibleItemIndex }
        ?.let { firstVisible ->
            return (-firstVisible.offset + firstVisibleItemScrollOffset).coerceAtLeast(0)
        }

    val averageItemSize = layoutInfo.visibleItemsInfo
        .asSequence()
        .map { it.size }
        .average()
        .takeIf { it > 0 }
        ?: 400.0
    return (firstVisibleItemScrollOffset + firstVisibleItemIndex * averageItemSize).toInt()
}

private suspend fun LazyListState.animateScrollToTopFixed(
    durationMillis: Int = ListScrollToTopDurationMillis,
) {
    if (layoutInfo.totalItemsCount == 0) return
    if (firstVisibleItemIndex == 0 && firstVisibleItemScrollOffset == 0) return

    val scrollDistance = scrollDistanceToTop()
    if (scrollDistance <= 0) {
        scrollToItem(0, 0)
        return
    }

    runCatching {
        animateScrollBy(
            value = -scrollDistance.toFloat(),
            animationSpec = tween(durationMillis),
        )
    }

    if (firstVisibleItemIndex != 0 || firstVisibleItemScrollOffset != 0) {
        scrollToItem(0, 0)
    }
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

private fun commentFailureMessage(error: Throwable): String {
    val message = error.message.orEmpty()
    return when {
        error is TimeoutCancellationException ->
            "图片上传或评论发送超时，请稍后重试"
        message.contains("未发现微博登录 Cookie") ->
            "未检测到微博登录状态，请先在账户页登录"
        message.contains("image-upload-redirected-to-html") ||
            message.contains("weibo-native-post-failed:302") ||
            message.contains("图片上传失败:302") ||
            message.contains("微博返回了 HTML") ->
            "登录状态已失效或被微博重定向，请重新登录后再试"
        message.contains("无法读取所选图片") ->
            "无法读取所选图片，请重新选择"
        message.contains("图片") && message.contains("失败") ->
            message
        message.contains("评论") ->
            message
        else -> message.ifBlank { "评论发送失败，请稍后重试" }
    }
}


private val HintCapsuleWhite = Color.White
private val HintCapsuleText = Color(0xFF1F1F1F)
private val HintCapsuleProgressBg = Color(0xFF007AFF)
private val HintCapsuleProgressText = Color.White
private val HintCapsuleSuccessBg = Color(0xFF34C759)
private val HintCapsuleSuccessText = Color.White
private val HintCapsulePlaceholder = Color(0xFFAAAAAA)
private val HintCapsuleBorderColor = Color(0xFFE6E6E6)
private val SettingsBottomBarInset = 96.dp
// 64dp 胶囊高度 + 24dp 底边距 + 12dp 展开动画溢出
private val LiquidBottomBarReserve = 100.dp
private val LiquidBottomBarContentGap = 8.dp
private val SearchBarBottomGap = 20.dp
private val SearchBarCompanionGap = 8.dp
// 与 WeiboLiquidBottomBar 一致：64dp 栏高 + 24dp 底边距
private val SearchBottomBarClearance = 64.dp + 24.dp
private val SearchSuggestionPanelMaxHeight = 176.dp
private val FeedRefreshIndicatorColor = Color(0xFF9E9E9E)
private val FeedCardContentHorizontalPadding = 12.dp
private val FeedCardSectionSpacing = 10.dp
private val FeedCardItemSpacing = 8.dp
private const val WeiboListFlingFriction = 0.45f
private const val WeiboListFlingStopVelocity = 0.75f

/**
 * A slightly shorter, more controlled decay than Compose's platform default. It preserves the
 * release velocity, but removes the long low-speed tail that makes content feel as if it is
 * drifting after the finger has stopped.
 */
@Composable
private fun rememberWeiboListFlingBehavior(): FlingBehavior {
    val decay = remember {
        exponentialDecay<Float>(
            frictionMultiplier = WeiboListFlingFriction,
            absVelocityThreshold = WeiboListFlingStopVelocity,
        )
    }
    return remember(decay) {
        object : FlingBehavior {
            override suspend fun ScrollScope.performFling(initialVelocity: Float): Float {
                if (abs(initialVelocity) < WeiboListFlingStopVelocity) return initialVelocity
                var lastValue = 0f
                val animation = AnimationState(
                    initialValue = 0f,
                    initialVelocity = initialVelocity,
                )
                animation.animateDecay(decay) {
                    val delta = value - lastValue
                    val consumed = scrollBy(delta)
                    lastValue = value
                    if (abs(delta - consumed) > 0.5f) cancelAnimation()
                }
                return animation.velocity
            }
        }
    }
}

private class LayoutAnchorHolder {
    var coordinates: LayoutCoordinates? = null

    fun boundsInWindow(): Rect? =
        coordinates?.takeIf { it.isAttached }?.boundsInWindow()

    fun boundsInRoot(): Rect? =
        coordinates?.takeIf { it.isAttached }?.boundsInRoot()
}

private data class DetailVideoViewport(
    val headerBottomPx: Float = 0f,
    val viewportBottomPx: Float = 0f,
)

/** 只在当前详情页作用域内共享边界，避免多个详情页/导航动画互相污染坐标。 */
private val LocalDetailVideoViewport = staticCompositionLocalOf { DetailVideoViewport() }

private const val SingleImageMaxHeightToWidth = 1.2f
private const val SingleImageMaxWidthFraction = 0.7f
private const val VideoMaxHeightToWidth = 1f
private val VideoControlCapsuleShape = RoundedCornerShape(percent = 50)
private val VideoProgressLineWidth = 2.5.dp
private val VideoControlBarHeight = 32.dp
private val VideoControlBarBottomFullscreen = 26.dp
private val VideoControlBarBottomInline = 8.dp
private val VideoFullscreenTopControlInset = 18.dp
private val VideoFullscreenTopControlRowSpacing = 8.dp
private val VideoFullscreenHorizontalControlInset = 16.dp
private const val VideoMaxWidthFraction = 1f
private const val AlbumGridMaxDecodeDim = 320
private const val FullscreenDefaultMaxDecodeDim = 4096
private const val FullscreenLongImageMaxDecodeDim = 8192
private const val FullscreenLongImageOriginalMaxPixels = 32_000_000
private const val FullscreenDefaultMaxZoomScale = 8f
private const val FullscreenDynamicMaxZoomScale = 80f
private const val FullscreenFillZoomHeadroom = 1.15f
private const val FullscreenPixelPerfectZoomHeadroom = 2f
private const val AlbumBitmapCacheMaxBytes = 96 * 1024 * 1024
private const val AlbumGridMaxReadBytes = 768 * 1024
private const val AlbumGridPrefetchConcurrency = 4
private const val AlbumGridPrefetchBatchSize = 24
private const val FeedImageLoadConcurrency = 4
private const val FeedBitmapCacheMaxBytes = 32 * 1024 * 1024
private const val FullscreenBitmapCacheMaxBytes = 160 * 1024 * 1024

private data class MorandiThemeColor(
    val storageValue: String,
    val label: String,
    val primary: Color,
    val primaryContainer: Color,
    val secondary: Color,
)

private val MorandiThemeColors = listOf(
    // 中性色
    MorandiThemeColor("classic_black", "黑色", Color(0xFF202124), Color(0xFFE6E6E6), Color(0xFF4A4A4A)),
    MorandiThemeColor("graphite", "石墨", Color(0xFF4E5965), Color(0xFFDDE2E7), Color(0xFF66717C)),
    // 红粉
    MorandiThemeColor("wine_red", "酒红", Color(0xFF9A4E5B), Color(0xFFEBD7DB), Color(0xFFA56B73)),
    MorandiThemeColor("coral", "珊瑚", Color(0xFFC8665D), Color(0xFFF0D8D5), Color(0xFFB27771)),
    MorandiThemeColor("ruby_red", "宝石红", Color(0xFFE03A56), Color(0xFFFCE3E8), Color(0xFFD24B63)),
    MorandiThemeColor("rose_red", "玫红", Color(0xFFFF3B6F), Color(0xFFFFE3EC), Color(0xFFE03568)),
    // 橙黄
    MorandiThemeColor("clay", "陶土", Color(0xFFC08A72), Color(0xFFEEDDD5), Color(0xFFA98B7C)),
    MorandiThemeColor("amber", "琥珀", Color(0xFFB07A39), Color(0xFFEADDCB), Color(0xFFA4865A)),
    MorandiThemeColor("vivid_orange", "活力橙", Color(0xFFFF6D00), Color(0xFFFFE8D6), Color(0xFFE86200)),
    // 绿色
    MorandiThemeColor("sage", "鼠尾草", Color(0xFF86A08A), Color(0xFFDCE8DD), Color(0xFF8A9B82)),
    MorandiThemeColor("emerald", "翠绿", Color(0xFF00A86B), Color(0xFFD4F5E6), Color(0xFF1A9B65)),
    MorandiThemeColor("vivid_green", "草绿", Color(0xFF34C759), Color(0xFFE5F9E8), Color(0xFF2DB350)),
    // 青色
    MorandiThemeColor("smoky_teal", "烟青", Color(0xFF789E9B), Color(0xFFD7E5E3), Color(0xFF7F9996)),
    MorandiThemeColor("cyan", "天青", Color(0xFF00BCD4), Color(0xFFD6F5F9), Color(0xFF00A8BA)),
    // 蓝色
    MorandiThemeColor("mist_blue", "雾蓝", Color(0xFF7F9CAF), Color(0xFFD8E4EA), Color(0xFF879BA7)),
    MorandiThemeColor("ocean_blue", "海蓝", Color(0xFF4E7FA8), Color(0xFFD6E5F1), Color(0xFF668AA8)),
    MorandiThemeColor("electric_blue", "电光蓝", Color(0xFF2B7CFF), Color(0xFFDFECFF), Color(0xFF4A90E8)),
    MorandiThemeColor("cobalt", "钴蓝", Color(0xFF0057FF), Color(0xFFDDE8FF), Color(0xFF3366E6)),
    // 紫色
    MorandiThemeColor("iris", "鸢尾", Color(0xFF7367B2), Color(0xFFE0DDF2), Color(0xFF8279AF)),
    MorandiThemeColor("violet", "紫罗兰", Color(0xFF8B5CF6), Color(0xFFECE8FE), Color(0xFF7C4FE8)),
)

private fun morandiThemeColorFromStorage(value: String?): MorandiThemeColor =
    MorandiThemeColors.firstOrNull { it.storageValue == value } ?: MorandiThemeColors.first()

private val FeedImageLoadSemaphore = Semaphore(FeedImageLoadConcurrency)

private const val RemoteBytesCacheMaxTotal = 32 * 1024 * 1024
private const val RemoteBytesMaxCachedEntry = 8 * 1024 * 1024
private const val RemoteBytesAnimatedMaxRead = 64 * 1024 * 1024
private const val RemoteDiskBytesCacheMaxTotal = 256 * 1024 * 1024
private const val NavTransitionDurationMs = 280
// ComponentCallbacks2.TRIM_MEMORY_* 在较新 SDK 中已标记 deprecated，数值仍稳定可用。
private const val TrimMemoryRunningLow = 10
private const val TrimMemoryRunningCritical = 15
private const val TrimMemoryComplete = 80
private const val ImageAcceptHeader = "image/avif,image/webp,image/apng,image/svg+xml,image/*,*/*;q=0.8"

private fun navStackEnterTransition() =
    slideInHorizontally(
        animationSpec = tween(NavTransitionDurationMs, easing = FastOutSlowInEasing),
        initialOffsetX = { fullWidth -> fullWidth },
    )

private fun navStackExitTransition() =
    slideOutHorizontally(
        animationSpec = tween(NavTransitionDurationMs, easing = FastOutSlowInEasing),
        targetOffsetX = { fullWidth -> fullWidth },
    )

@Composable
private fun <T> NavAnimatedOverlay(
    target: T?,
    modifier: Modifier = Modifier,
    stackTop: Boolean = false,
    layerBaseZIndex: Float = 0f,
    animationKey: Any? = null,
    visible: Boolean? = null,
    stackAnimated: Boolean = true,
    navKind: NavOverlayKind? = null,
    pendingEnterKind: NavOverlayKind? = null,
    pendingExitKind: NavOverlayKind? = null,
    onClearPendingEnter: () -> Unit = {},
    onClearPendingExit: () -> Unit = {},
    enter: EnterTransition = navStackEnterTransition(),
    exit: ExitTransition = navStackExitTransition(),
    exitHoldMillis: Long = NavTransitionDurationMs.toLong(),
    onHidden: () -> Unit = {},
    content: @Composable (T) -> Unit,
) {
    val navTransitions = LocalNavTransitionCoordinator.current
    var displayed by remember { mutableStateOf<T?>(null) }
    if (target != null) {
        displayed = target
    }
    val overlayVisible = visible ?: (target != null)
    // 栈内仍挂载（target != null）但被上层盖住时，保持组合树不销毁，避免 LazyList 滚动位置丢失。
    val avVisible = target != null || overlayVisible
    val transitionIdentity = animationKey ?: target
    val enteredKeys = remember { mutableStateMapOf<Any, Boolean>() }
    val wantsEnterAnimation = stackAnimated &&
        overlayVisible &&
        transitionIdentity != null &&
        enteredKeys[transitionIdentity] != true &&
        (navKind == null || pendingEnterKind == null || navOverlayKindsMatch(pendingEnterKind, navKind))
    val shouldAnimateExit = stackAnimated &&
        !overlayVisible &&
        target == null &&
        (navKind == null || navOverlayKindsMatch(pendingExitKind, navKind))
    var skipAnimationToken by remember { mutableIntStateOf(0) }
    // 进入/退出动画在 identity 或 pendingExit 生命周期内锁定，避免中途 effectiveEnter/Exit 变 None 导致动画被掐断。
    val effectiveEnter = remember(transitionIdentity, skipAnimationToken, pendingEnterKind) {
        if (wantsEnterAnimation) enter else EnterTransition.None
    }
    val effectiveExit = remember(transitionIdentity, skipAnimationToken, pendingExitKind) {
        if (shouldAnimateExit) exit else ExitTransition.None
    }
    val effectiveExitHoldMillis = if (shouldAnimateExit) exitHoldMillis else 0L
    val transitionState = remember(skipAnimationToken, transitionIdentity) {
        MutableTransitionState(false)
    }
    SideEffect {
        transitionState.targetState = avVisible
    }
    LaunchedEffect(avVisible, transitionIdentity) {
        if (!avVisible && transitionIdentity != null) {
            enteredKeys.remove(transitionIdentity)
        }
    }
    LaunchedEffect(
        transitionState.isIdle,
        transitionState.currentState,
        overlayVisible,
        transitionIdentity,
        pendingEnterKind,
    ) {
        if (!overlayVisible || transitionIdentity == null) return@LaunchedEffect
        if (!transitionState.isIdle || !transitionState.currentState) return@LaunchedEffect
        enteredKeys[transitionIdentity] = true
        if (navKind != null && navOverlayKindsMatch(pendingEnterKind, navKind)) {
            onClearPendingEnter()
        }
    }
    val isAnimating = !transitionState.isIdle
    val isExiting = isAnimating && !transitionState.targetState
    val zIndex = when {
        isExiting -> NavOverlayExitZIndex
        stackTop -> NavOverlayTopZIndex
        else -> layerBaseZIndex
    }

    DisposableEffect(Unit) {
        val unregister = navTransitions.registerSkipHandler { skipAnimationToken++ }
        onDispose { unregister() }
    }
    DisposableEffect(isAnimating) {
        if (isAnimating) {
            navTransitions.onTransitionStart()
            onDispose { navTransitions.onTransitionEnd() }
        } else {
            onDispose {}
        }
    }

    AnimatedVisibility(
        visibleState = transitionState,
        modifier = modifier.zIndex(zIndex),
        enter = effectiveEnter,
        exit = effectiveExit,
        label = "nav-stack-overlay",
    ) {
        displayed?.let { item -> content(item) }
    }
    if (!avVisible && displayed != null) {
        LaunchedEffect(displayed, transitionState.isIdle, target) {
            if (!transitionState.isIdle || transitionState.targetState) return@LaunchedEffect
            if (effectiveExitHoldMillis > 0L) {
                delay(effectiveExitHoldMillis)
            }
            displayed = null
            if (navKind != null && navOverlayKindsMatch(pendingExitKind, navKind)) {
                onClearPendingExit()
            }
            onHidden()
        }
    }
}

private class VideoPlaybackCoordinator {
    var activeKey by mutableStateOf<String?>(null)
    var fullscreenKey by mutableStateOf<String?>(null)
    val positions = mutableStateMapOf<String, Long>()
    var pendingFullscreenHandoffKey by mutableStateOf<String?>(null)
    var pendingPeekHandoffKey by mutableStateOf<String?>(null)
    var peekPlaybackKey by mutableStateOf<String?>(null)
    var detailOverlayOpen by mutableStateOf(false)
    var detailHandoffActive by mutableStateOf(false)
    var proactiveStashSignalKey by mutableStateOf<String?>(null)
    var autoScrollFloatingKey by mutableStateOf<String?>(null)
    private val sharedPlayersByMediaKey = mutableMapOf<String, androidx.media3.exoplayer.ExoPlayer>()
    private val handoffPlayers = mutableMapOf<String, androidx.media3.exoplayer.ExoPlayer>()
    private val surfaceBoundPlayers = java.util.Collections.newSetFromMap(
        java.util.IdentityHashMap<androidx.media3.exoplayer.ExoPlayer, Boolean>(),
    )
    private val transitionFrames = mutableMapOf<String, Bitmap>()
    private val inlinePauseHandlers = mutableMapOf<String, () -> Unit>()
    private val fullscreenPauseHandlers = mutableMapOf<String, () -> Unit>()
    private val peekPauseHandlers = mutableMapOf<String, () -> Unit>()
    private val peekRestartFromBeginningKeys = mutableSetOf<String>()
    private val inlineHandoffResumeKeys = mutableSetOf<String>()

    private fun mediaAliasKey(mediaPart: String) = "*|$mediaPart"

    private fun mediaPartFromPlaybackKey(playbackKey: String): String =
        playbackKey.substringAfter('|', "")

    fun rememberPosition(playbackKey: String, positionMs: Long) {
        if (positionMs > 0L || positions[playbackKey] == null) {
            positions[playbackKey] = positionMs
        }
        val mediaPart = mediaPartFromPlaybackKey(playbackKey)
        if (mediaPart.isNotBlank()) {
            val alias = mediaAliasKey(mediaPart)
            if (positionMs > 0L || positions[alias] == null) {
                positions[alias] = positionMs
            }
        }
    }

    fun resolvePosition(playbackKey: String): Long? {
        positions[playbackKey]?.let { return it }
        val mediaPart = mediaPartFromPlaybackKey(playbackKey)
        if (mediaPart.isBlank()) return null
        return positions[mediaAliasKey(mediaPart)]
    }

    fun clearPosition(playbackKey: String) {
        positions[playbackKey] = 0L
        val mediaPart = mediaPartFromPlaybackKey(playbackKey)
        if (mediaPart.isNotBlank()) {
            positions[mediaAliasKey(mediaPart)] = 0L
        }
    }

    private fun clearHandoffFlagsForKey(playbackKey: String) {
        if (pendingFullscreenHandoffKey == playbackKey) {
            pendingFullscreenHandoffKey = null
        }
        if (pendingPeekHandoffKey == playbackKey) {
            pendingPeekHandoffKey = null
        }
    }

    private fun findHandoffKeyByMediaPart(mediaPart: String, except: String? = null): String? =
        handoffPlayers.keys.firstOrNull { key ->
            key != except && mediaPartFromPlaybackKey(key) == mediaPart
        }

    fun schedulePeekRestartIfAtEnd(playbackKey: String, durationMs: Long?) {
        val saved = resolvePosition(playbackKey) ?: return
        val duration = durationMs?.takeIf { it > VideoEndRestartThresholdMs } ?: return
        if (saved >= duration - VideoEndRestartThresholdMs) {
            clearPosition(playbackKey)
            peekRestartFromBeginningKeys += playbackKey
        }
    }

    fun isPeekRestartFromBeginning(playbackKey: String): Boolean =
        playbackKey in peekRestartFromBeginningKeys

    fun consumePeekRestartFromBeginning(playbackKey: String): Boolean =
        peekRestartFromBeginningKeys.remove(playbackKey)

    fun beginFullscreenHandoff(playbackKey: String) {
        pendingFullscreenHandoffKey = playbackKey
    }

    fun storeTransitionFrame(playbackKey: String, bitmap: Bitmap) {
        transitionFrames.put(playbackKey, bitmap)?.takeIf { it !== bitmap }?.recycle()
    }

    fun consumeTransitionFrame(playbackKey: String): Bitmap? =
        transitionFrames.remove(playbackKey)

    fun requestProactivePeekStash(playbackKey: String) {
        proactiveStashSignalKey = playbackKey
    }

    fun isProactiveStashRequested(playbackKey: String): Boolean =
        proactiveStashSignalKey?.let { matchesPlaybackKey(it, playbackKey) } == true

    fun clearProactiveStashRequest(playbackKey: String) {
        if (isProactiveStashRequested(playbackKey)) {
            proactiveStashSignalKey = null
        }
    }

    fun hasStashedHandoff(playbackKey: String): Boolean {
        if (handoffPlayers.containsKey(playbackKey)) return true
        val mediaPart = mediaPartFromPlaybackKey(playbackKey)
        if (mediaPart.isBlank()) return false
        return findHandoffKeyByMediaPart(mediaPart, except = playbackKey) != null
    }

    fun cancelFullscreenHandoff(playbackKey: String) {
        if (pendingFullscreenHandoffKey == playbackKey) {
            pendingFullscreenHandoffKey = null
        }
        handoffPlayers.remove(playbackKey)?.release()
    }

    fun beginPeekHandoff(playbackKey: String) {
        pendingPeekHandoffKey = playbackKey
        inlineHandoffResumeKeys += playbackKey
    }

    fun beginDetailHandoff(playbackKey: String) {
        pendingPeekHandoffKey = playbackKey
        inlineHandoffResumeKeys += playbackKey
        detailHandoffActive = true
        activeKey = playbackKey
    }

    fun isDetailHandoffTarget(playbackKey: String): Boolean =
        detailHandoffActive &&
            pendingPeekHandoffKey?.let { matchesPlaybackKey(it, playbackKey) } == true

    fun finishDetailHandoff(playbackKey: String) {
        if (detailHandoffActive &&
            pendingPeekHandoffKey?.let { matchesPlaybackKey(it, playbackKey) } == true
        ) {
            detailHandoffActive = false
        }
    }

    fun completeDetailPlaybackHandoff(playbackKey: String) {
        if (pendingPeekHandoffKey?.let { matchesPlaybackKey(it, playbackKey) } == true) {
            pendingPeekHandoffKey = null
        }
        clearInlineHandoffResume(playbackKey)
        detailHandoffActive = false
    }

    fun resumeDetailHandoffPlayback(
        playbackKey: String,
        player: androidx.media3.exoplayer.ExoPlayer,
    ): Boolean {
        if (!shouldResumeInlineHandoff(playbackKey)) return false
        player.playWhenReady = true
        player.play()
        return true
    }

    fun isTransferringToDetail(playbackKey: String): Boolean =
        detailHandoffActive &&
            (
                pendingPeekHandoffKey?.let { matchesPlaybackKey(it, playbackKey) } == true ||
                    activeKey?.let { matchesPlaybackKey(it, playbackKey) } == true
                )

    fun registerSharedPlayer(playbackKey: String, player: androidx.media3.exoplayer.ExoPlayer) {
        val mediaPart = mediaPartFromPlaybackKey(playbackKey)
        if (mediaPart.isNotBlank()) {
            sharedPlayersByMediaKey[mediaPart] = player
        }
    }

    fun releaseSharedPlayer(playbackKey: String, player: androidx.media3.exoplayer.ExoPlayer) {
        val mediaPart = mediaPartFromPlaybackKey(playbackKey)
        if (mediaPart.isNotBlank()) {
            sharedPlayersByMediaKey.remove(mediaPart, player)
        }
    }

    fun adoptSharedPlayer(playbackKey: String): androidx.media3.exoplayer.ExoPlayer? {
        consumeHandoffPlayer(playbackKey)?.let { player ->
            registerSharedPlayer(playbackKey, player)
            return player
        }
        val mediaPart = mediaPartFromPlaybackKey(playbackKey)
        if (mediaPart.isBlank()) return null
        val shared = sharedPlayersByMediaKey.remove(mediaPart) ?: return null
        // 从 shared 接管时也清掉 pending，避免 peek 一直认为尚在交接而卡住。
        if (pendingPeekHandoffKey?.let { matchesPlaybackKey(it, playbackKey) } == true) {
            pendingPeekHandoffKey = null
        }
        return shared
    }

    fun playbackKeysShareMedia(first: String, second: String): Boolean {
        val firstMedia = mediaPartFromPlaybackKey(first)
        val secondMedia = mediaPartFromPlaybackKey(second)
        return firstMedia.isNotBlank() && firstMedia == secondMedia
    }

    fun matchesPlaybackKey(candidate: String, reference: String): Boolean =
        candidate == reference || playbackKeysShareMedia(candidate, reference)

    fun isPlaybackKeyActive(playbackKey: String): Boolean =
        activeKey?.let { matchesPlaybackKey(it, playbackKey) } == true

    fun isPlaybackKeyHandoffPending(playbackKey: String): Boolean =
        pendingPeekHandoffKey?.let { matchesPlaybackKey(it, playbackKey) } == true

    fun shouldResumeInlineHandoff(playbackKey: String): Boolean =
        inlineHandoffResumeKeys.any { matchesPlaybackKey(it, playbackKey) }

    fun clearInlineHandoffResume(playbackKey: String) {
        inlineHandoffResumeKeys.removeAll { matchesPlaybackKey(it, playbackKey) }
    }

    fun markAutoScrollFloating(playbackKey: String) {
        autoScrollFloatingKey = playbackKey
    }

    fun isAutoScrollFloating(playbackKey: String): Boolean =
        autoScrollFloatingKey?.let { matchesPlaybackKey(it, playbackKey) } == true

    fun clearAutoScrollFloating(playbackKey: String) {
        if (isAutoScrollFloating(playbackKey)) {
            autoScrollFloatingKey = null
        }
    }

    fun shouldSuppressInlineForDetailOverlay(playbackKey: String): Boolean =
        detailOverlayOpen &&
            (
                isDetailHandoffTarget(playbackKey) ||
                    isPlaybackKeyActive(playbackKey) ||
                    isPlaybackKeyHandoffPending(playbackKey)
                )

    fun isFullscreenHandoffPending(playbackKey: String): Boolean =
        pendingFullscreenHandoffKey?.let { matchesPlaybackKey(it, playbackKey) } == true

    fun cancelPeekHandoff(playbackKey: String) {
        if (pendingPeekHandoffKey?.let { matchesPlaybackKey(it, playbackKey) } == true) {
            pendingPeekHandoffKey = null
        }
        handoffPlayers.remove(playbackKey)?.release()
        val mediaPart = mediaPartFromPlaybackKey(playbackKey)
        if (mediaPart.isNotBlank()) {
            findHandoffKeyByMediaPart(mediaPart, except = playbackKey)?.let { matched ->
                handoffPlayers.remove(matched)?.release()
            }
        }
    }

    fun markPlayerSurfaceBound(player: androidx.media3.exoplayer.ExoPlayer) {
        surfaceBoundPlayers.add(player)
    }

    fun markPlayerSurfaceUnbound(player: androidx.media3.exoplayer.ExoPlayer) {
        surfaceBoundPlayers.remove(player)
    }

    fun isPlayerSurfaceBound(player: androidx.media3.exoplayer.ExoPlayer): Boolean =
        surfaceBoundPlayers.contains(player)

    fun stashHandoffPlayer(playbackKey: String, player: androidx.media3.exoplayer.ExoPlayer) {
        val currentPosition = player.currentPosition.coerceAtLeast(0L)
        rememberPosition(playbackKey, currentPosition)
        // 仅当详情页内联已绑定并接管播放时，禁止 clearVideoSurface（列表 dispose 勿拆详情画面）。
        // 自动滚到浮窗等 peek/全屏交接必须交出实例，否则浮窗黑屏无声。
        val keepDetailBoundSurface = isPlayerSurfaceBound(player) &&
            detailOverlayOpen &&
            detailHandoffActive &&
            pendingPeekHandoffKey == null &&
            pendingFullscreenHandoffKey == null
        if (keepDetailBoundSurface) {
            registerSharedPlayer(playbackKey, player)
            return
        }
        // 清空 Surface 时务必暂停；新 PlayerView 挂载后再 resume。
        player.playWhenReady = false
        player.pause()
        player.clearVideoSurface()
        markPlayerSurfaceUnbound(player)
        handoffPlayers[playbackKey] = player
        registerSharedPlayer(playbackKey, player)
    }

    fun isAnotherInlinePlaybackActive(playbackKey: String): Boolean {
        val current = activeKey ?: return false
        return !matchesPlaybackKey(current, playbackKey)
    }

    fun consumeHandoffPlayer(playbackKey: String): androidx.media3.exoplayer.ExoPlayer? {
        handoffPlayers.remove(playbackKey)?.let { player ->
            clearHandoffFlagsForKey(playbackKey)
            clearProactiveStashRequest(playbackKey)
            return player
        }
        val mediaPart = mediaPartFromPlaybackKey(playbackKey)
        if (mediaPart.isBlank()) return null
        val matchedKey = findHandoffKeyByMediaPart(mediaPart, except = playbackKey) ?: return null
        val player = handoffPlayers.remove(matchedKey) ?: return null
        clearHandoffFlagsForKey(matchedKey)
        clearProactiveStashRequest(playbackKey)
        if (pendingPeekHandoffKey?.let { matchesPlaybackKey(it, playbackKey) } == true) {
            pendingPeekHandoffKey = null
        }
        return player
    }

    fun registerPauseHandler(playbackKey: String, isFullscreen: Boolean, handler: () -> Unit) {
        if (isFullscreen) {
            fullscreenPauseHandlers[playbackKey] = handler
        } else {
            inlinePauseHandlers[playbackKey] = handler
        }
    }

    fun unregisterPauseHandler(playbackKey: String, isFullscreen: Boolean) {
        if (isFullscreen) {
            fullscreenPauseHandlers.remove(playbackKey)
        } else {
            inlinePauseHandlers.remove(playbackKey)
        }
    }

    fun registerPeekPauseHandler(playbackKey: String, handler: () -> Unit) {
        peekPauseHandlers[playbackKey] = handler
    }

    fun unregisterPeekPauseHandler(playbackKey: String) {
        peekPauseHandlers.remove(playbackKey)
    }

    fun pausePeek(exceptKey: String? = null) {
        peekPauseHandlers.forEach { (key, handler) ->
            if (key != exceptKey) handler()
        }
    }

    fun pauseAll() {
        inlinePauseHandlers.values.forEach { it() }
        fullscreenPauseHandlers.values.forEach { it() }
        peekPauseHandlers.values.forEach { it() }
    }

    fun pauseInlineOnly(exceptKey: String? = null) {
        inlinePauseHandlers.forEach { (key, handler) ->
            if (exceptKey != null && matchesPlaybackKey(key, exceptKey)) return@forEach
            handler()
        }
    }

    fun requestInlinePlayback(playbackKey: String) {
        pausePeek()
        pauseInlineOnly(exceptKey = playbackKey)
        activeKey = playbackKey
    }

    fun claimPeekPlayback(playbackKey: String) {
        pauseInlineOnly()
        fullscreenPauseHandlers.values.forEach { it() }
        pausePeek(exceptKey = playbackKey)
        activeKey = null
        peekPlaybackKey = playbackKey
    }

    /**
     * 滚动浮窗交接专用：先占权、不暂停内联。
     * 内联会主动 stash 并卸掉 pause handler，之后由浮窗 Surface 恢复播放。
     */
    fun claimPeekPlaybackForScrollFloat(playbackKey: String) {
        fullscreenPauseHandlers.values.forEach { it() }
        pausePeek(exceptKey = playbackKey)
        activeKey = null
        peekPlaybackKey = playbackKey
    }

    fun releasePeekPlayback(playbackKey: String) {
        if (peekPlaybackKey == playbackKey) {
            peekPlaybackKey = null
        }
    }

    fun claimFullscreenPlayback(playbackKey: String) {
        pauseInlineOnly()
        pausePeek()
        activeKey = null
        peekPlaybackKey = null
        fullscreenKey = playbackKey
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

private enum class DetailContentSection {
    Comments,
    Reposts,
}

private data class LikeUsersOverlayState(
    val item: FeedItem,
    val anchorBounds: Rect,
    val likeId: String,
    val knownTotal: Int? = null,
)

private data class CommentComposeTarget(
    val status: FeedItem,
    val replyTo: CommentItem? = null,
) {
    val isReply: Boolean
        get() = replyTo != null

    val title: String
        get() = if (isReply) "回复评论" else "发布评论"

    val placeholder: String
        get() = replyTo?.authorName?.let { "回复 @$it" } ?: "写评论..."
}

private const val DETAIL_SECTION_HEADER_INDEX = 1

private data class DetailSnapshot(
    val item: FeedItem,
    val comments: List<CommentItem>,
    val commentsCursor: String?,
    val commentsHasMore: Boolean,
    val commentSort: CommentSort,
    val contentSection: DetailContentSection = DetailContentSection.Comments,
    val reposts: List<CommentItem> = emptyList(),
    val repostsNextPage: Int? = null,
    val repostsHasMore: Boolean = true,
    val scrollIndex: Int = 0,
    val scrollOffset: Int = 0,
    val albumViewerState: AlbumViewerState? = null,
    val instanceKey: Int = 0,
)

private data class ArticleOverlayState(
    val entity: FeedUrlEntity,
    val article: WeiboArticle? = null,
    val loading: Boolean = false,
    val error: String? = null,
)

private data class FollowListTabCache(
    val users: List<RelationUser>,
    val page: Int,
    val hasMore: Boolean,
    val errorMsg: String?,
)

private fun followListTabCacheKey(instanceKey: Int, uid: String, tab: FriendListTab): String =
    "$instanceKey|$uid|${tab.name}"

private data class FollowListOverlayState(
    val uid: String,
    val screenName: String,
    val avatarUrl: String?,
    val description: String?,
    val tab: FriendListTab,
    val instanceKey: Int = 0,
)

private data class ScrollRestore(
    val index: Int = 0,
    val offset: Int = 0,
)

private data class NavRestoreState(
    val selectedTab: MainTab = MainTab.Feed,
    val feedScroll: ScrollRestore = ScrollRestore(),
    val minePostsScroll: ScrollRestore = ScrollRestore(),
    val mineAlbumScroll: ScrollRestore = ScrollRestore(),
    val minePagerPage: Int = 0,
    val searchScroll: ScrollRestore = ScrollRestore(),
    val visitedProfile: VisitedProfileSnapshot? = null,
    val detail: DetailSnapshot? = null,
    val followListOverlay: FollowListOverlayState? = null,
    val followListFollowingScroll: ScrollRestore = ScrollRestore(),
    val followListFansScroll: ScrollRestore = ScrollRestore(),
    val albumViewerState: AlbumViewerState? = null,
    val articleOverlay: ArticleOverlayState? = null,
    val mediaPreview: MediaPreviewRequest? = null,
)

private sealed interface NavOverlayKind {
    data class Detail(val itemId: String) : NavOverlayKind
    data class VisitedProfile(val uid: String) : NavOverlayKind
    data class FollowList(val uid: String) : NavOverlayKind
    data object AlbumViewer : NavOverlayKind
    data object MediaPreview : NavOverlayKind
    data object Article : NavOverlayKind
    data object TabSwitch : NavOverlayKind
}

private enum class OverlayTop {
    None,
    Detail,
    VisitedProfile,
    FollowList,
    AlbumViewer,
    MediaPreview,
    Article,
}

private fun overlayTopKind(stack: List<NavOverlayKind>): OverlayTop = when (stack.lastOrNull()) {
    is NavOverlayKind.Detail -> OverlayTop.Detail
    is NavOverlayKind.VisitedProfile -> OverlayTop.VisitedProfile
    is NavOverlayKind.FollowList -> OverlayTop.FollowList
    NavOverlayKind.AlbumViewer -> OverlayTop.AlbumViewer
    NavOverlayKind.MediaPreview -> OverlayTop.MediaPreview
    NavOverlayKind.Article -> OverlayTop.Article
    NavOverlayKind.TabSwitch, null -> OverlayTop.None
}

private fun navOverlayKindsMatch(a: NavOverlayKind?, b: NavOverlayKind?): Boolean {
    if (a == null || b == null) return false
    return when {
        a is NavOverlayKind.Detail && b is NavOverlayKind.Detail -> true
        a is NavOverlayKind.VisitedProfile && b is NavOverlayKind.VisitedProfile -> true
        a is NavOverlayKind.FollowList && b is NavOverlayKind.FollowList -> true
        else -> a == b
    }
}

private const val NavOverlayTopZIndex = 590f
private const val NavOverlayExitZIndex = 595f

private data class MediaPreviewRequest(
    val media: FeedMedia,
    val playbackOwnerId: String,
)

private fun FeedImage.albumStatusCacheKey(): String =
    statusId?.takeIf { it.isNotBlank() } ?: largeUrl

private val LocalVideoPlaybackCoordinator = staticCompositionLocalOf { VideoPlaybackCoordinator() }
private val LocalDetailInlineVideoPlayback = staticCompositionLocalOf { false }
private val LocalUiMessenger = staticCompositionLocalOf<(String, String) -> Unit> { { _, _ -> } }
private val LocalImageSaveHint = staticCompositionLocalOf { ImageSaveHintController() }
private val LocalTopicClickHandler = staticCompositionLocalOf<((String) -> Unit)?> { null }

private enum class CapsuleHintTone {
    Neutral,
    Success,
    Progress,
}

private data class AppCapsuleHintState(
    val message: String,
    val tone: CapsuleHintTone = CapsuleHintTone.Neutral,
    val autoDismissMillis: Long? = 2200L,
    val progress: Float? = null,
)

private class ImageSaveHintController {
    var activeHint by mutableStateOf<AppCapsuleHintState?>(null)

    fun showProgress(current: Int, total: Int) {
        activeHint = AppCapsuleHintState(
            message = "正在保存 $current/$total",
            tone = CapsuleHintTone.Progress,
            autoDismissMillis = null,
            progress = if (total > 0) current.toFloat() / total.toFloat() else null,
        )
    }

    fun showBatchSaveProgress(progress: ImageSaveHelper.SaveAllProgress, itemLabel: String = "张") {
        val message = if (progress.total > 1) {
            "正在保存第 ${progress.activeIndex}/${progress.total} $itemLabel"
        } else {
            "正在保存"
        }
        activeHint = AppCapsuleHintState(
            message = message,
            tone = CapsuleHintTone.Progress,
            autoDismissMillis = null,
            progress = if (progress.total > 0) {
                progress.completed.toFloat() / progress.total.toFloat()
            } else {
                null
            },
        )
    }

    fun showVideoProgress(progress: Float?) {
        activeHint = AppCapsuleHintState(
            message = "正在保存视频",
            tone = CapsuleHintTone.Progress,
            autoDismissMillis = null,
            progress = progress,
        )
    }

    fun showSuccess(message: String) {
        activeHint = AppCapsuleHintState(
            message = message,
            tone = CapsuleHintTone.Success,
            autoDismissMillis = 2400L,
        )
    }

    fun showFailure(message: String) {
        activeHint = AppCapsuleHintState(
            message = message,
            tone = CapsuleHintTone.Neutral,
            autoDismissMillis = 2800L,
        )
    }

    fun clear() {
        activeHint = null
    }

    suspend fun saveOne(
        context: Context,
        image: FeedImage,
        status: FeedItem? = null,
    ): Boolean {
        showProgress(1, 1)
        return ImageSaveHelper.saveImage(
            context = context,
            image = image,
            metadata = image.buildSaveMetadata(status),
            status = status,
        )
            .onSuccess {
                showSuccess("已保存到相册")
            }
            .onFailure { error ->
                showFailure(error.message ?: "保存失败")
            }
            .isSuccess
    }

    suspend fun saveVideo(
        context: Context,
        media: FeedMedia,
    ): Boolean {
        showVideoProgress(null)
        return ImageSaveHelper.saveVideo(context, media) { fraction ->
            showVideoProgress(fraction)
        }
            .onSuccess {
                showSuccess("已保存到相册")
            }
            .onFailure { error ->
                showFailure(error.message ?: "保存失败")
            }
            .isSuccess
    }

    suspend fun saveAll(
        context: Context,
        images: List<FeedImage>,
        status: FeedItem? = null,
        includeVideos: Boolean = false,
    ): ImageSaveHelper.SaveAllImagesResult {
        val videos = if (includeVideos) {
            status?.medias?.filter { it.isSavableToAlbum() }.orEmpty()
        } else {
            emptyList()
        }
        val total = images.size + videos.size
        if (total == 0) {
            showFailure("没有可保存的内容")
            return ImageSaveHelper.SaveAllImagesResult(
                saved = 0,
                total = 0,
                errors = listOf("没有可保存的内容"),
            )
        }
        var saved = 0
        val errors = mutableListOf<String>()
        if (images.isNotEmpty()) {
            val imageResult = ImageSaveHelper.saveAllImages(context, images, status = status) { progress ->
                withContext(Dispatchers.Main.immediate) {
                    showBatchSaveProgress(
                        progress = progress.copy(total = total),
                        itemLabel = if (videos.isNotEmpty()) "项" else "张",
                    )
                }
            }
            saved += imageResult.saved
            errors += imageResult.errors
        }
        for ((index, media) in videos.withIndex()) {
            val position = images.size + index + 1
            withContext(Dispatchers.Main.immediate) {
                showBatchSaveProgress(
                    progress = ImageSaveHelper.SaveAllProgress(
                        completed = saved,
                        total = total,
                        activeIndex = position,
                    ),
                    itemLabel = "项",
                )
            }
            val videoSaved = ImageSaveHelper.saveVideo(context, media) { fraction ->
                showVideoProgress(
                    if (total > 0) {
                        ((position - 1) + fraction.coerceIn(0f, 1f)) / total.toFloat()
                    } else {
                        fraction
                    },
                )
            }
                .onFailure { error ->
                    errors += error.message?.takeIf { it.isNotBlank() } ?: "视频保存失败"
                }
                .isSuccess
            if (videoSaved) {
                saved += 1
                withContext(Dispatchers.Main.immediate) {
                    showBatchSaveProgress(
                        progress = ImageSaveHelper.SaveAllProgress(
                            completed = saved,
                            total = total,
                            activeIndex = position,
                        ),
                        itemLabel = "项",
                    )
                }
            }
        }
        val usesMixedMedia = videos.isNotEmpty()
        when {
            saved == total && usesMixedMedia ->
                showSuccess("已保存 ${saved} 项到相册")
            saved == total ->
                showSuccess("已保存 ${saved} 张图片到相册")
            saved > 0 && usesMixedMedia ->
                showSuccess("已保存 ${saved}/${total} 项")
            saved > 0 ->
                showSuccess("已保存 ${saved}/${total} 张图片")
            else ->
                showFailure(errors.firstOrNull()?.takeIf { it.isNotBlank() } ?: "保存失败")
        }
        return ImageSaveHelper.SaveAllImagesResult(saved = saved, total = total, errors = errors)
    }
}

private enum class VideoPeekDismissReason {
    Cancel,
    Release,
    PlaybackEnded,
    EnterFullscreen,
}

private data class VideoPeekRequest(
    val media: FeedMedia,
    val anchorBounds: Rect,
    val pressOffset: Offset,
    val playbackOwnerId: String,
    val resolveAnchorBounds: () -> Rect,
    val expandFromAnchor: Boolean = false,
    val fromFullscreen: Boolean = false,
    val dockImmediately: Boolean = false,
    val onCancel: () -> Unit,
    val onRelease: () -> Unit,
    val onPlaybackEnded: () -> Unit,
    val onOpenFullscreenBehind: () -> Unit,
    val onEnterFullscreenHandoffComplete: () -> Unit = {},
)

private class VideoPeekController {
    var activeRequest by mutableStateOf<VideoPeekRequest?>(null)
    var pendingDismiss by mutableStateOf<VideoPeekDismissReason?>(null)
    var isFloating by mutableStateOf(false)
    var isFullscreenMode by mutableStateOf(false)
    /** 自动滚回卡片等场景跳过浮窗回缩动画，避免黑块下落。 */
    var snapDismiss by mutableStateOf(false)
        private set
    var fingerDragOffset by mutableStateOf(Offset.Zero)
        private set

    fun updateFingerDragOffset(offset: Offset) {
        fingerDragOffset = offset
    }

    fun resetFingerDragOffset() {
        fingerDragOffset = Offset.Zero
    }

    fun open(request: VideoPeekRequest) {
        pendingDismiss = null
        snapDismiss = false
        isFloating = false
        isFullscreenMode = false
        fingerDragOffset = Offset.Zero
        activeRequest = request
    }

    fun openFloating(request: VideoPeekRequest) {
        pendingDismiss = null
        snapDismiss = false
        isFloating = true
        isFullscreenMode = false
        fingerDragOffset = Offset.Zero
        activeRequest = request
    }

    fun cancel(snap: Boolean = false) {
        if (activeRequest != null && pendingDismiss == null) {
            snapDismiss = snap
            pendingDismiss = VideoPeekDismissReason.Cancel
        }
    }

    fun release() {
        if (activeRequest != null && pendingDismiss == null) {
            isFloating = true
        }
    }

    fun exitFullscreenToFloating() {
        if (activeRequest != null && pendingDismiss == null && isFullscreenMode) {
            isFullscreenMode = false
            isFloating = true
        }
    }

    fun dismissForPlaybackEnded() {
        if (activeRequest != null && pendingDismiss == null) {
            pendingDismiss = VideoPeekDismissReason.PlaybackEnded
        }
    }

    fun enterFullscreen() {
        if (activeRequest != null && pendingDismiss == null && !isFullscreenMode) {
            pendingDismiss = VideoPeekDismissReason.EnterFullscreen
        }
    }

    fun completeDismiss() {
        val request = activeRequest
        val reason = pendingDismiss
        activeRequest = null
        pendingDismiss = null
        snapDismiss = false
        isFloating = false
        isFullscreenMode = false
        fingerDragOffset = Offset.Zero
        when (reason) {
            VideoPeekDismissReason.Cancel -> request?.onCancel?.invoke()
            VideoPeekDismissReason.Release -> request?.onRelease?.invoke()
            VideoPeekDismissReason.PlaybackEnded -> request?.onPlaybackEnded?.invoke()
            VideoPeekDismissReason.EnterFullscreen -> Unit
            null -> Unit
        }
    }

    fun completeFullscreenExpand() {
        if (pendingDismiss != VideoPeekDismissReason.EnterFullscreen) return
        pendingDismiss = null
        isFloating = false
        isFullscreenMode = true
        activeRequest?.onEnterFullscreenHandoffComplete?.invoke()
    }
}

private data class FeedCardActionMenuRequest(
    val item: FeedItem,
    val anchorBoundsInRoot: Rect,
    val backHandlerEnabled: Boolean,
)

private class FeedCardActionMenuController {
    var activeRequest by mutableStateOf<FeedCardActionMenuRequest?>(null)
    var menuRevealVisible by mutableStateOf(false)

    fun open(item: FeedItem, anchorBoundsInRoot: Rect, backHandlerEnabled: Boolean) {
        activeRequest = FeedCardActionMenuRequest(item, anchorBoundsInRoot, backHandlerEnabled)
        menuRevealVisible = true
    }

    fun dismiss() {
        menuRevealVisible = false
        activeRequest = null
    }
}

private val LocalFeedCardActionMenuController = staticCompositionLocalOf { FeedCardActionMenuController() }

private class NavTransitionCoordinator {
    var activeTransitions by mutableIntStateOf(0)
    val isTransitioning: Boolean
        get() = activeTransitions > 0
    private val skipHandlers = mutableListOf<() -> Unit>()

    fun onTransitionStart() {
        activeTransitions++
    }

    fun onTransitionEnd() {
        activeTransitions = (activeTransitions - 1).coerceAtLeast(0)
    }

    fun registerSkipHandler(handler: () -> Unit): () -> Unit {
        skipHandlers.add(handler)
        return { skipHandlers.remove(handler) }
    }

    fun skipAllTransitions() {
        skipHandlers.toList().forEach { it.invoke() }
    }
}

private val LocalNavTransitionCoordinator = staticCompositionLocalOf { NavTransitionCoordinator() }

private class FeedListScrollCoordinator {
    var isListScrolling by mutableStateOf(false)
    var stopScrollAction: (suspend () -> Unit)? = null

    suspend fun interruptIfScrolling(): Boolean {
        if (!isListScrolling) return false
        stopScrollAction?.invoke()
        return true
    }
}

private val LocalFeedListScrollCoordinator = staticCompositionLocalOf { FeedListScrollCoordinator() }

private val LocalFeedBodyTextStyle = staticCompositionLocalOf { TextStyle.Default }

private const val EmoticonBitmapMaxDecodeDim = 96
private const val FeedEmoticonTextCacheMaxEntries = 600
private const val UrlLinkIconInlineContentKey = "url-link-icon"

private data class FeedEmoticonTextCacheKey(
    val text: String,
    val leadingAuthorName: String?,
    val trailingLabel: String?,
    val trailingLocation: String?,
    val inlineImageTokens: List<String>,
    val urlEntityTokens: List<String>,
    val emoticonTokens: List<String>,
    val linkScopeKey: String?,
    val linkFlags: Int,
    val fontSizeBits: ULong,
    val lineHeightBits: ULong,
    val primaryColorValue: ULong,
)

private object FeedEmoticonTextCache {
    private val cache = object : LinkedHashMap<FeedEmoticonTextCacheKey, AnnotatedString>(
        FeedEmoticonTextCacheMaxEntries,
        0.75f,
        true,
    ) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<FeedEmoticonTextCacheKey, AnnotatedString>?): Boolean =
            size > FeedEmoticonTextCacheMaxEntries
    }

    @Synchronized
    fun get(key: FeedEmoticonTextCacheKey): AnnotatedString? = cache[key]

    @Synchronized
    fun put(key: FeedEmoticonTextCacheKey, value: AnnotatedString) {
        cache[key] = value
    }

    fun clear() {
        synchronized(this) { cache.clear() }
    }
}

private object FeedEmoticonLinkDispatcher {
    var onUserClick: ((String) -> Unit)? = null
    var onTopicClick: ((String) -> Unit)? = null
    var onUrlEntityClick: ((FeedUrlEntity) -> Unit)? = null

    private val trailingClicks = java.util.concurrent.ConcurrentHashMap<String, () -> Unit>()
    private val locationClicks = java.util.concurrent.ConcurrentHashMap<String, () -> Unit>()
    private val leadingAuthorClicks = java.util.concurrent.ConcurrentHashMap<String, () -> Unit>()
    private val inlineImagesByToken = java.util.concurrent.ConcurrentHashMap<String, List<FeedImage>>()
    private val urlEntitiesByToken = java.util.concurrent.ConcurrentHashMap<String, FeedUrlEntity>()

    private val inlineImageCallbacks = java.util.concurrent.ConcurrentHashMap<String, () -> Unit>()

    private fun scopedKey(scopeKey: String, token: String): String = "$scopeKey|$token"

    fun registerTrailing(scopeKey: String, action: () -> Unit) {
        trailingClicks[scopeKey] = action
    }

    fun unregisterTrailing(scopeKey: String) {
        trailingClicks.remove(scopeKey)
    }

    fun trailingClick(scopeKey: String) {
        trailingClicks[scopeKey]?.invoke()
    }

    fun registerLocation(scopeKey: String, action: () -> Unit) {
        locationClicks[scopeKey] = action
    }

    fun unregisterLocation(scopeKey: String) {
        locationClicks.remove(scopeKey)
    }

    fun locationClick(scopeKey: String) {
        locationClicks[scopeKey]?.invoke()
    }

    fun registerLeadingAuthor(authorName: String, action: () -> Unit) {
        leadingAuthorClicks[authorName] = action
    }

    fun unregisterLeadingAuthor(authorName: String) {
        leadingAuthorClicks.remove(authorName)
    }

    fun leadingAuthorClick(authorName: String) {
        leadingAuthorClicks[authorName]?.invoke()
    }

    fun hasLeadingAuthorClick(authorName: String): Boolean =
        leadingAuthorClicks.containsKey(authorName)

    fun hasTrailingClick(scopeKey: String): Boolean =
        trailingClicks.containsKey(scopeKey)

    fun hasInlineImageClick(scopeKey: String, token: String): Boolean =
        inlineImageCallbacks.containsKey(scopedKey(scopeKey, token))

    fun hasUrlEntityClick(scopeKey: String, token: String): Boolean =
        urlEntitiesByToken.containsKey(scopedKey(scopeKey, token))

    fun unregisterScope(scopeKey: String) {
        trailingClicks.remove(scopeKey)
        locationClicks.remove(scopeKey)
        val prefix = "$scopeKey|"
        inlineImagesByToken.keys.removeAll { it.startsWith(prefix) }
        inlineImageCallbacks.keys.removeAll { it.startsWith(prefix) }
        urlEntitiesByToken.keys.removeAll { it.startsWith(prefix) }
    }

    fun registerInlineImage(
        scopeKey: String,
        token: String,
        images: List<FeedImage>,
        action: () -> Unit,
    ) {
        val key = scopedKey(scopeKey, token)
        inlineImagesByToken[key] = images
        inlineImageCallbacks[key] = action
    }

    fun inlineImageClick(scopeKey: String, token: String) {
        inlineImageCallbacks[scopedKey(scopeKey, token)]?.invoke()
    }

    fun registerUrlEntities(scopeKey: String, entities: Map<String, FeedUrlEntity>) {
        entities.forEach { (token, entity) ->
            urlEntitiesByToken[scopedKey(scopeKey, token)] = entity
        }
    }

    fun urlEntityClick(scopeKey: String, token: String) {
        urlEntitiesByToken[scopedKey(scopeKey, token)]?.let { entity ->
            onUrlEntityClick?.invoke(entity)
        }
    }
}

private fun feedEmoticonGlobalLinkFlags(): Int {
    var flags = 0
    if (FeedEmoticonLinkDispatcher.onUserClick != null) flags = flags or 1
    if (FeedEmoticonLinkDispatcher.onTopicClick != null) flags = flags or 2
    if (FeedEmoticonLinkDispatcher.onUrlEntityClick != null) flags = flags or 4
    return flags
}

private fun buildFeedEmoticonTextCacheKey(
    text: String,
    leadingAuthorName: String?,
    trailingLabel: String?,
    trailingLocation: String?,
    inlineImageLinks: Map<String, List<FeedImage>>,
    urlEntities: Map<String, FeedUrlEntity>,
    emoticonTokens: List<String>,
    linkScopeKey: String?,
    hasTrailingLink: Boolean,
    hasLeadingLink: Boolean,
    hasScopedInlineImageLinks: Boolean,
    hasScopedUrlLinks: Boolean,
    hasLocationLink: Boolean,
    style: TextStyle,
    primaryColor: Color,
): FeedEmoticonTextCacheKey {
    var linkFlags = feedEmoticonGlobalLinkFlags()
    if (hasTrailingLink) linkFlags = linkFlags or 8
    if (hasLeadingLink) linkFlags = linkFlags or 16
    if (hasScopedInlineImageLinks) linkFlags = linkFlags or 32
    if (hasScopedUrlLinks) linkFlags = linkFlags or 64
    if (hasLocationLink) linkFlags = linkFlags or 128
    return FeedEmoticonTextCacheKey(
        text = text,
        leadingAuthorName = leadingAuthorName,
        trailingLabel = trailingLabel,
        trailingLocation = trailingLocation,
        inlineImageTokens = inlineImageLinks.keys.sorted(),
        urlEntityTokens = urlEntities.keys.sorted(),
        emoticonTokens = emoticonTokens,
        linkScopeKey = linkScopeKey,
        linkFlags = linkFlags,
        fontSizeBits = textUnitCacheBits(style.fontSize),
        lineHeightBits = textUnitCacheBits(style.lineHeight),
        primaryColorValue = primaryColor.value.toULong(),
    )
}

private fun textUnitCacheBits(unit: TextUnit): ULong =
    if (unit == TextUnit.Unspecified) 0uL else unit.value.toRawBits().toULong()

private fun cancelFeedListScrollOnTap(
    coordinator: FeedListScrollCoordinator,
    scope: CoroutineScope,
    pointerChange: androidx.compose.ui.input.pointer.PointerInputChange,
): Boolean {
    if (!coordinator.isListScrolling) return false
    pointerChange.consume()
    scope.launch {
        coordinator.interruptIfScrolling()
    }
    return true
}

@Composable
private fun NavTransitionTouchShield(
    coordinator: NavTransitionCoordinator,
    modifier: Modifier = Modifier,
) {
    if (!coordinator.isTransitioning) return
    Box(
        modifier
            .fillMaxSize()
            .pointerInput(coordinator.isTransitioning) {
                awaitEachGesture {
                    awaitFirstDown(requireUnconsumed = false).consume()
                    coordinator.skipAllTransitions()
                }
            },
    )
}

private class SearchBarOverlayController {
    var active by mutableStateOf(false)
    var queryInput by mutableStateOf(TextFieldValue(""))
    var mode by mutableStateOf(SearchMode.Weibo)
    var onModeChange: (SearchMode) -> Unit = {}
    var onQueryInputChange: (TextFieldValue) -> Unit = { queryInput = it }
    var onSearch: () -> Unit = {}
    var onClear: () -> Unit = {}
    var suggestions by mutableStateOf(SearchSuggestResult())
    var suggestionsVisible by mutableStateOf(false)
    var suggestionsLoading by mutableStateOf(false)
    var onSuggestionClick: (String, SearchMode) -> Unit = { _, _ -> }
    var onSuggestionUserClick: (SearchUserItem) -> Unit = {}
    var bottomPadding by mutableStateOf(SearchBottomBarClearance + SearchBarBottomGap)
}

private enum class ImagePeekDismissReason {
    Cancel,
    Release,
    EnterFullscreen,
}

private data class ImagePeekRequest(
    val image: FeedImage,
    val allImages: List<FeedImage>,
    val imageIndex: Int,
    val anchorBounds: Rect,
    val pressOffset: Offset,
    val resolveAnchorBounds: () -> Rect,
    val statusItem: FeedItem? = null,
    val onCancel: () -> Unit,
    val onRelease: () -> Unit,
    val onOpenFullscreenBehind: (Int) -> Unit,
    val onEnterFullscreenHandoffComplete: () -> Unit,
)

private class ImagePeekController {
    var activeRequest by mutableStateOf<ImagePeekRequest?>(null)
    var pendingDismiss by mutableStateOf<ImagePeekDismissReason?>(null)
    var isFloating by mutableStateOf(false)
    var fingerDragOffset by mutableStateOf(Offset.Zero)
        private set

    fun updateFingerDragOffset(offset: Offset) {
        fingerDragOffset = offset
    }

    fun resetFingerDragOffset() {
        fingerDragOffset = Offset.Zero
    }

    private var horizontalDragHandler: ((Float) -> Unit)? = null

    fun bindHorizontalDragHandler(handler: ((Float) -> Unit)?) {
        horizontalDragHandler = handler
    }

    fun dispatchHorizontalDrag(deltaX: Float) {
        if (deltaX != 0f) {
            horizontalDragHandler?.invoke(deltaX)
        }
    }

    fun open(request: ImagePeekRequest) {
        pendingDismiss = null
        isFloating = false
        fingerDragOffset = Offset.Zero
        activeRequest = request
    }

    fun cancel() {
        if (activeRequest != null && pendingDismiss == null) {
            pendingDismiss = ImagePeekDismissReason.Cancel
        }
    }

    fun release() {
        if (activeRequest != null && pendingDismiss == null) {
            isFloating = true
        }
    }

    fun enterFullscreen() {
        if (activeRequest != null && pendingDismiss == null) {
            pendingDismiss = ImagePeekDismissReason.EnterFullscreen
        }
    }

    fun completeDismiss() {
        val request = activeRequest
        val reason = pendingDismiss
        activeRequest = null
        pendingDismiss = null
        isFloating = false
        fingerDragOffset = Offset.Zero
        horizontalDragHandler = null
        when (reason) {
            ImagePeekDismissReason.Cancel -> request?.onCancel?.invoke()
            ImagePeekDismissReason.Release -> request?.onRelease?.invoke()
            ImagePeekDismissReason.EnterFullscreen -> Unit
            null -> Unit
        }
    }

    fun completeEnterFullscreenHandoff() {
        val request = activeRequest
        activeRequest = null
        pendingDismiss = null
        isFloating = false
        fingerDragOffset = Offset.Zero
        horizontalDragHandler = null
        request?.onEnterFullscreenHandoffComplete?.invoke()
    }

    fun finishFullscreenHandoff() {
        if (pendingDismiss != ImagePeekDismissReason.EnterFullscreen) return
        completeEnterFullscreenHandoff()
    }
}

private val LocalImagePeekController = staticCompositionLocalOf { ImagePeekController() }
private val LocalVideoPeekController = staticCompositionLocalOf { VideoPeekController() }
private val LocalFeedThumbnailQuality = staticCompositionLocalOf { FeedThumbnailQuality.Medium }
private val LocalFeedImageUpgradeNotifier = staticCompositionLocalOf { FeedImageUpgradeNotifier() }
private val LocalFeedEmoticonInlineContent = staticCompositionLocalOf { emptyMap<String, InlineTextContent>() }

private data class FeedTypographyMetrics(
    val body: TextStyle,
    val emojiSize: TextUnit,
    val placeholderLineHeight: TextUnit,
)

private val LocalFeedTypographyMetrics = staticCompositionLocalOf {
    val size = 15.sp
    FeedTypographyMetrics(
        body = TextStyle(
            fontSize = size,
            lineHeight = size * FeedLineSpacing.Normal.lineHeightMultiplier,
            platformStyle = PlatformTextStyle(includeFontPadding = false),
        ),
        emojiSize = size * 1.4f,
        placeholderLineHeight = size * FeedLineSpacing.Normal.lineHeightMultiplier,
    )
}

@Composable
private fun rememberFeedTypographyMetrics(
    fontSize: FeedFontSize,
    lineSpacing: FeedLineSpacing,
): FeedTypographyMetrics {
    val bodyMedium = MaterialTheme.typography.bodyMedium
    val fontScale = LocalDensity.current.fontScale
    return remember(fontSize, lineSpacing, fontScale, bodyMedium) {
        val size = (fontSize.sizeSp / fontScale).sp
        val body = bodyMedium.copy(
            fontSize = size,
            lineHeight = size * lineSpacing.lineHeightMultiplier,
            platformStyle = PlatformTextStyle(includeFontPadding = false),
        )
        val placeholderLineHeight = body.lineHeight.takeIf { it != TextUnit.Unspecified }
            ?: body.fontSize * 1.5f
        FeedTypographyMetrics(
            body = body,
            emojiSize = body.fontSize * 1.4f,
            placeholderLineHeight = placeholderLineHeight,
        )
    }
}

private fun emojiInlineContent(
    url: String,
    size: TextUnit,
): InlineTextContent =
    InlineTextContent(
        Placeholder(
            width = size * 0.85f,
            height = size,
            placeholderVerticalAlign = PlaceholderVerticalAlign.TextCenter,
        ),
    ) {
        EmojiImage(url = url)
    }

private fun urlLinkIconInlineContent(
    size: TextUnit,
    color: Color,
): InlineTextContent =
    InlineTextContent(
        Placeholder(
            width = size,
            height = size,
            placeholderVerticalAlign = PlaceholderVerticalAlign.TextCenter,
        ),
    ) {
        Icon(
            imageVector = Icons.Outlined.Link,
            contentDescription = null,
            tint = color,
            modifier = Modifier
                .fillMaxSize()
                .padding(1.dp)
                .rotate(135f),
        )
    }

private fun mentionInlineContent(
    token: String,
    body: TextStyle,
    lineHeight: TextUnit,
    color: Color,
): InlineTextContent {
    val estWidth = body.fontSize * (tokenWidthEm(token) + 0.16f)
    return InlineTextContent(
        Placeholder(
            width = estWidth,
            height = lineHeight,
            placeholderVerticalAlign = PlaceholderVerticalAlign.TextCenter,
        ),
    ) {
        Text(
            text = token,
            color = color,
            fontWeight = FontWeight.Medium,
            style = body,
            softWrap = false,
            maxLines = 1,
        )
    }
}

@Composable
private fun rememberFeedEmoticonInlineContent(
    emoticonMap: Map<String, String>,
    metrics: FeedTypographyMetrics,
): Map<String, InlineTextContent> {
    val primaryColor = MaterialTheme.colorScheme.primary
    val body = metrics.body
    return remember(emoticonMap, metrics.emojiSize, metrics.placeholderLineHeight, primaryColor) {
        buildMap {
            emoticonMap.forEach { (phrase, url) ->
                put(phrase, emojiInlineContent(url = url, size = metrics.emojiSize))
            }
            emoticonMap.keys.filter { it.startsWith("@") }.forEach { token ->
                put(
                    token,
                    mentionInlineContent(
                        token = token,
                        body = body,
                        lineHeight = metrics.placeholderLineHeight,
                        color = primaryColor,
                    ),
                )
            }
        }
    }
}

@Composable
private fun rememberMissingEmoticonInlineContent(
    tokens: List<String>,
    emoticonMap: Map<String, String>,
    metrics: FeedTypographyMetrics,
): Map<String, InlineTextContent> {
    val primaryColor = MaterialTheme.colorScheme.primary
    val body = metrics.body
    return remember(tokens, emoticonMap, metrics.emojiSize, metrics.placeholderLineHeight, primaryColor) {
        if (tokens.isEmpty()) {
            emptyMap()
        } else {
            buildMap {
                tokens.forEach { token ->
                    val url = emoticonMap[token] ?: return@forEach
                    put(
                        token,
                        if (token.startsWith("@")) {
                            mentionInlineContent(
                                token = token,
                                body = body,
                                lineHeight = metrics.placeholderLineHeight,
                                color = primaryColor,
                            )
                        } else {
                            emojiInlineContent(url = url, size = metrics.emojiSize)
                        },
                    )
                }
            }
        }
    }
}

private fun textNeedsRichRendering(
    text: String,
    leadingAuthorName: String?,
    trailingLabel: String?,
    trailingLocation: String?,
    inlineImageLinks: Map<String, List<FeedImage>>,
    urlEntities: Map<String, FeedUrlEntity>,
): Boolean {
    if (leadingAuthorName != null || trailingLabel != null || trailingLocation != null) return true
    if (text.isEmpty()) return false
    if (text.indexOf('@') >= 0 || text.indexOf('#') >= 0 || text.indexOf('[') >= 0) return true
    return inlineImageLinks.keys.any(text::contains) ||
        urlEntities.keys.any(text::contains)
}

private class FeedImageUpgradeNotifier {
    var revision by mutableIntStateOf(0)
        private set

    fun notifyCacheUpdated() {
        revision++
    }
}

private fun feedImageUrlCandidates(image: FeedImage): List<String> =
    ImageUrlResolver.feedBitmapCandidates(image)

private fun feedImageCacheCandidates(
    image: FeedImage,
    quality: FeedThumbnailQuality,
): List<String> =
    ImageUrlResolver.feedDisplayCandidates(image, quality)

private fun fullscreenImageUrlCandidates(image: FeedImage): List<String> =
    ImageUrlResolver.fullscreenCandidates(image)

private fun Bitmap.isFullscreenQuality(image: FeedImage): Boolean {
    val maxDim = maxOf(width, height)
    val expectedWidth = image.width ?: 0
    val expectedHeight = image.height ?: 0
    val decodeDim = fullscreenDecodeDim(image)
    if (expectedWidth > 0 && expectedHeight > 0) {
        val expectedMax = maxOf(expectedWidth, expectedHeight)
        val targetMax = min((expectedMax * 0.85f).roundToInt(), decodeDim)
        return maxDim >= targetMax
    }
    return maxDim >= (decodeDim * 0.75f).roundToInt()
}

private fun fullscreenDecodeDim(image: FeedImage): Int {
    val width = image.width ?: 0
    val height = image.height ?: 0
    if (width <= 0 || height <= 0) return FullscreenDefaultMaxDecodeDim
    val longSide = maxOf(width, height)
    val shortSide = min(width, height).coerceAtLeast(1)
    return if (longSide.toFloat() / shortSide.toFloat() >= 3f) {
        val pixelCount = width.toLong() * height.toLong()
        if (pixelCount <= FullscreenLongImageOriginalMaxPixels) {
            longSide
        } else {
            FullscreenLongImageMaxDecodeDim
        }
    } else {
        FullscreenDefaultMaxDecodeDim
    }
}

private fun albumGridUrlCandidates(image: FeedImage): List<String> =
    ImageUrlResolver.albumGridCandidates(image)

private suspend fun loadAlbumGridBitmap(
    image: FeedImage,
    maxDecodeDim: Int = AlbumGridMaxDecodeDim,
): Bitmap? {
    AlbumThumbnailBitmapCache.getForImage(image, maxDecodeDim)?.takeIfDrawable()?.let { return it }
    for (url in albumGridUrlCandidates(image)) {
        decodeCachedRemoteBitmap(url, maxDecodeDim)?.let { bitmap ->
            AlbumThumbnailBitmapCache.putForImage(image, maxDecodeDim, bitmap)
            return bitmap
        }
        val bitmap = runCatching {
            withContext(Dispatchers.IO) {
                val bytes = fetchRemoteBytes(
                    url = url,
                    connectTimeoutMs = 5000,
                    readTimeoutMs = 5000,
                    maxReadBytes = AlbumGridMaxReadBytes,
                )
                decodeBitmapFromBytes(bytes, maxDecodeDim)
            }
        }.getOrNull()
        if (bitmap != null) {
            AlbumThumbnailBitmapCache.putForImage(image, maxDecodeDim, bitmap)
            return bitmap
        }
    }
    return null
}

private suspend fun prefetchAlbumGridThumbnails(images: List<FeedImage>) {
    val pending = images.filter { image ->
        AlbumThumbnailBitmapCache.getForImage(image, AlbumGridMaxDecodeDim) == null
    }
    if (pending.isEmpty()) return
    val semaphore = Semaphore(AlbumGridPrefetchConcurrency)
    pending.take(AlbumGridPrefetchBatchSize).chunked(12).forEach { batch ->
        coroutineScope {
            batch.map { image ->
                async(Dispatchers.IO) {
                    semaphore.withPermit {
                        loadAlbumGridBitmap(image)
                    }
                }
            }.awaitAll()
        }
        yield()
    }
}

private fun remoteReadLimitForDecodeDim(maxDecodeDim: Int): Int = when {
    maxDecodeDim <= AlbumGridMaxDecodeDim -> 4 * 1024 * 1024
    maxDecodeDim <= 960 -> 4 * 1024 * 1024
    maxDecodeDim <= FullscreenDefaultMaxDecodeDim -> 16 * 1024 * 1024
    else -> 48 * 1024 * 1024
}

private fun decodeBitmapFromBytes(bytes: ByteArray, maxDecodeDim: Int): Bitmap? =
    BitmapExifOrientation.decodeSampledBitmap(bytes, maxDecodeDim)

private fun Bitmap?.takeIfDrawable(): Bitmap? = this?.takeIf { !it.isRecycled }

private fun trimImageCaches(keepFraction: Float) {
    val fraction = keepFraction.coerceIn(0f, 1f)
    AlbumThumbnailBitmapCache.trimToFraction(min(fraction + 0.25f, 1f))
    FeedBitmapCache.trimToFraction(fraction)
    FullscreenBitmapCache.trimToFraction(fraction)
    RemoteBytesCache.trimToFraction(fraction)
}

private fun decodeCachedRemoteBitmap(url: String, maxDecodeDim: Int): Bitmap? {
    val bytes = RemoteBytesCache.get(url) ?: return null
    return decodeBitmapFromBytes(bytes, maxDecodeDim)
}

private suspend fun loadRemoteBitmap(
    url: String,
    maxDecodeDim: Int,
    connectTimeoutMs: Int = 8000,
    readTimeoutMs: Int = 8000,
): Bitmap? {
    decodeCachedRemoteBitmap(url, maxDecodeDim)?.let { return it }
    val bytes = fetchRemoteBytes(
        url = url,
        connectTimeoutMs = connectTimeoutMs,
        readTimeoutMs = readTimeoutMs,
        maxReadBytes = remoteReadLimitForDecodeDim(maxDecodeDim),
    )
    return decodeBitmapFromBytes(bytes, maxDecodeDim)
}

@Composable
private fun rememberLivePhotoMirrorCorrection(
    image: FeedImage,
    stillBitmap: Bitmap?,
): LivePhotoMirrorCorrection {
    val key = remember(image.id, image.largeUrl, image.livePhotoVideoUrl) {
        LivePhotoMirrorCorrectionStore.correctionKey(image)
    }
    var correction by remember(key) {
        mutableStateOf(
            key?.let { cacheKey ->
                LivePhotoMirrorCorrectionStore.getMirrorVideo(cacheKey)?.let { mirrorVideo ->
                    if (mirrorVideo) LivePhotoMirrorCorrection.MirrorVideo else LivePhotoMirrorCorrection.None
                }
            } ?: LivePhotoMirrorCorrection.None,
        )
    }

    LaunchedEffect(key, stillBitmap) {
        val targetKey = key ?: return@LaunchedEffect
        LivePhotoMirrorCorrectionStore.getMirrorVideo(targetKey)?.let { mirrorVideo ->
            correction = if (mirrorVideo) LivePhotoMirrorCorrection.MirrorVideo else LivePhotoMirrorCorrection.None
            return@LaunchedEffect
        }
        val detected = withContext(Dispatchers.IO) {
            detectLivePhotoMirrorCorrection(image, stillBitmap)
        }
        correction = detected
    }
    return correction
}

private suspend fun detectLivePhotoMirrorCorrection(
    image: FeedImage,
    stillBitmap: Bitmap?,
): LivePhotoMirrorCorrection {
    if (!image.isLivePhoto) return LivePhotoMirrorCorrection.None
    val still = stillBitmap?.takeIfDrawable() ?: loadLivePhotoStillForMirrorDetection(image) ?: return LivePhotoMirrorCorrection.None
    val videoUrl = image.livePhotoVideoUrl.orEmpty()
    val frame = LivePhotoMirrorDetector.loadReferenceFrameFromUrl(videoUrl)
        ?: return LivePhotoMirrorCorrection.None
    val mirrorVideo = LivePhotoMirrorDetector.shouldMirrorVideoToMatchStill(still, frame)
    LivePhotoMirrorCorrectionStore.putMirrorVideo(image, mirrorVideo)
    return if (mirrorVideo) {
        LivePhotoMirrorCorrection.MirrorVideo
    } else {
        LivePhotoMirrorCorrection.None
    }
}

private suspend fun loadLivePhotoStillForMirrorDetection(image: FeedImage): Bitmap? {
    resolveFullscreenPreviewBitmap(image)?.takeIfDrawable()?.let { return it }
    val candidates = ImageUrlResolver.livePhotoStillCandidates(image)
    for (url in candidates) {
        runCatching {
            loadRemoteBitmap(
                url = url,
                maxDecodeDim = 640,
                connectTimeoutMs = 5_000,
                readTimeoutMs = 8_000,
            )
        }.getOrNull()?.takeIfDrawable()?.let { return it }
    }
    return null
}

private fun videoMediaKey(media: FeedMedia): String =
    media.resolvedPlaybackUrl()
        ?: media.streamUrl.ifBlank { media.replayUrl.orEmpty() }
        .ifBlank { media.downloadUrl.orEmpty() }
        .ifBlank { media.coverUrl.orEmpty() }

private fun videoPlaybackKey(media: FeedMedia, ownerId: String): String =
    "$ownerId|${videoMediaKey(media)}"

private fun isMediaInlineExpanded(
    media: FeedMedia,
    playbackOwnerId: String,
    videoCoordinator: VideoPlaybackCoordinator,
    isDetailInlinePlayback: Boolean,
): Boolean {
    val playbackKey = videoPlaybackKey(media, playbackOwnerId)
    val suppressedByDetailOverlay = !isDetailInlinePlayback &&
        videoCoordinator.shouldSuppressInlineForDetailOverlay(playbackKey)
    return !suppressedByDetailOverlay &&
        (
            videoCoordinator.isPlaybackKeyActive(playbackKey) ||
                videoCoordinator.isPlaybackKeyHandoffPending(playbackKey) ||
                (isDetailInlinePlayback && videoCoordinator.isDetailHandoffTarget(playbackKey))
            ) &&
        videoCoordinator.fullscreenKey != playbackKey &&
        videoCoordinator.peekPlaybackKey != playbackKey
}

private fun feedItemPlaybackOwnerId(item: FeedItem): String =
    item.statusId.ifBlank { item.id }

private fun feedItemInlinePlaybackKeys(item: FeedItem): Set<String> {
    val keys = mutableSetOf<String>()
    fun register(medias: List<FeedMedia>, ownerId: String) {
        medias.forEach { media ->
            keys += videoPlaybackKey(media, ownerId)
        }
    }
    register(item.medias, feedItemPlaybackOwnerId(item))
    item.retweetedStatus?.let { retweeted ->
        val retweetOwnerId = retweeted.statusId.ifBlank { feedItemPlaybackOwnerId(item) }
        register(retweeted.medias, retweetOwnerId)
    }
    return keys
}

private fun videoDurationMs(media: FeedMedia): Long? =
    media.durationSeconds?.takeIf { it > 0 }?.times(1000L)

private const val VideoEndRestartThresholdMs = 500L
private const val VideoPeekFloatingPlaybackSpeed = 2f
private const val VideoPeekDockAspectRatio = 16f / 9f
private enum class ForcedVideoOrientation {
    None,
    Landscape,
    Portrait,
}

private fun profileAvatarFeedImage(avatarUrl: String?): FeedImage? {
    val url = avatarUrl?.takeIf { it.isNotBlank() } ?: return null
    return FeedImage(
        id = url,
        thumbnailUrl = url,
        largeUrl = url,
        downloadUrls = listOf(url),
    )
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun WeiboApp() {
    val context = LocalContext.current
    val session = remember { WeiboWebSession(context) }
    val timelineCacheStore = remember { TimelineCacheStore(context) }
    val mineCacheStore = remember { MineCacheStore(context) }
    val emoticonCacheStore = remember { EmoticonCacheStore(context) }
    val mentionSuggestionCacheStore = remember { MentionSuggestionCacheStore(context) }
    val accountStore = remember { WeiboAccountStore(context) }
    val commentSortStore = remember { CommentSortStore(context) }
    val searchSettingsStore = remember { SearchSettingsStore(context) }
    val searchHistoryStore = remember { SearchHistoryStore(context) }
    val playbackSettingsStore = remember { PlaybackSettingsStore(context) }
    val imageSettingsStore = remember { ImageSettingsStore(context) }
    val themeSettingsStore = remember { ThemeSettingsStore(context) }
    val typographySettingsStore = remember { TypographySettingsStore(context) }
    LaunchedEffect(context) {
        withContext(Dispatchers.IO) {
            RemoteDiskBytesCache.configure(java.io.File(context.cacheDir, "remote-image-bytes"))
        }
    }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val initialFollowingScroll = remember { timelineCacheStore.readFollowingScroll() }
    val feedListState = rememberLazyListState(
        initialFirstVisibleItemIndex = initialFollowingScroll.first,
        initialFirstVisibleItemScrollOffset = initialFollowingScroll.second,
    )
    val minePostsListState = rememberLazyListState()
    val mineAlbumListState = rememberLazyListState()
    val visitedPostsListState = rememberLazyListState()
    val visitedAlbumListState = rememberLazyListState()
    val followListFollowingListState = rememberLazyListState()
    val followListFansListState = rememberLazyListState()
    val searchListState = rememberLazyListState()
    val videoPlaybackCoordinator = remember { VideoPlaybackCoordinator() }
    val feedCardActionMenuController = remember { FeedCardActionMenuController() }
    val navTransitionCoordinator = remember { NavTransitionCoordinator() }
    val feedListScrollCoordinator = remember { FeedListScrollCoordinator() }
    val videoPeekController = remember { VideoPeekController() }
    val profileHeaderHeights = remember { mutableStateMapOf<String, Dp>() }

    var selectedTab by remember { mutableStateOf(MainTab.Feed) }
    var bottomBarExpanded by remember { mutableStateOf(true) }
    var bottomBarAwaitingOutsideDismiss by remember { mutableStateOf(false) }
    var minePagerPage by remember { mutableStateOf(0) }
    var visitedMinePagerPage by remember { mutableStateOf(0) }
    var feedRefreshHint by remember { mutableStateOf<String?>(null) }
    var operationCapsuleHint by remember { mutableStateOf<String?>(null) }
    var timelineKind by remember { mutableStateOf(TimelineKind.Following) }
    var items by remember { mutableStateOf<List<FeedItem>>(emptyList()) }
    var nextCursor by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var feedLoadingMore by remember { mutableStateOf(false) }
    var timelineRefreshJob by remember { mutableStateOf<Job?>(null) }
    var timelineLoadMoreJob by remember { mutableStateOf<Job?>(null) }
    var timelineRequestGeneration by remember { mutableIntStateOf(0) }
    var selectedItem by remember { mutableStateOf<FeedItem?>(null) }

    LaunchedEffect(selectedItem?.actionMenuKey()) {
        if (selectedItem != null) {
            feedCardActionMenuController.dismiss()
        }
    }

    SideEffect {
        videoPlaybackCoordinator.detailOverlayOpen = selectedItem != null
    }

    var navStack by remember { mutableStateOf<List<NavRestoreState>>(emptyList()) }
    var navOverlayStack by remember { mutableStateOf<List<NavOverlayKind>>(emptyList()) }
    var navEnterPendingKind by remember { mutableStateOf<NavOverlayKind?>(null) }
    var navExitPendingKind by remember { mutableStateOf<NavOverlayKind?>(null) }
    var pendingDeferredNavRestore by remember { mutableStateOf<NavRestoreState?>(null) }
    var deferredNavRestoreGeneration by remember { mutableIntStateOf(0) }
    var lastDetailScroll by remember { mutableStateOf(ScrollRestore()) }
    var detailScrollPending by remember { mutableStateOf<Pair<String, ScrollRestore>?>(null) }
    var detailScrollRestoreToken by remember { mutableIntStateOf(0) }
    var activeDetailInstanceKey by remember { mutableIntStateOf(0) }
    var albumOverlayInstanceKey by remember { mutableIntStateOf(0) }
    var mediaOverlayInstanceKey by remember { mutableIntStateOf(0) }
    var articleOverlayInstanceKey by remember { mutableIntStateOf(0) }
    var albumViewerState by remember { mutableStateOf<AlbumViewerState?>(null) }
    var comments by remember { mutableStateOf<List<CommentItem>>(emptyList()) }
    var commentsLoading by remember { mutableStateOf(false) }
    var commentsLoadingMore by remember { mutableStateOf(false) }
    var commentsRequestJob by remember { mutableStateOf<Job?>(null) }
    var commentsCursor by remember { mutableStateOf<String?>(null) }
    var commentsHasMore by remember { mutableStateOf(true) }
    var commentComposeTarget by remember { mutableStateOf<CommentComposeTarget?>(null) }
    var commentSubmitting by remember { mutableStateOf(false) }
    var composeSubmitting by remember { mutableStateOf(false) }
    var composeSessionKey by remember { mutableIntStateOf(0) }
    var messagesSessionKey by remember { mutableIntStateOf(0) }
    var commentSubmitJob by remember { mutableStateOf<Job?>(null) }
    var nestedCommentsLoadingIds by remember { mutableStateOf(setOf<String>()) }
    var detailContentSection by remember { mutableStateOf(DetailContentSection.Comments) }
    var reposts by remember { mutableStateOf<List<CommentItem>>(emptyList()) }
    var repostsLoading by remember { mutableStateOf(false) }
    var repostsLoadingMore by remember { mutableStateOf(false) }
    var repostsRequestJob by remember { mutableStateOf<Job?>(null) }
    var repostsNextPage by remember { mutableStateOf<Int?>(null) }
    var repostsHasMore by remember { mutableStateOf(true) }
    var detailRequestGeneration by remember { mutableIntStateOf(0) }
    var commentSort by remember { mutableStateOf(commentSortStore.read()) }
    var backgroundPlaybackEnabled by remember { mutableStateOf(playbackSettingsStore.readBackgroundPlaybackEnabled()) }
    var feedThumbnailQuality by remember { mutableStateOf(imageSettingsStore.readThumbnailQuality()) }
    var feedLineSpacing by remember { mutableStateOf(typographySettingsStore.readLineSpacing()) }
    var feedFontSize by remember { mutableStateOf(typographySettingsStore.readFontSize()) }
    var selectedThemeColor by remember {
        mutableStateOf(morandiThemeColorFromStorage(themeSettingsStore.readThemeColor()))
    }
    var appearanceMode by remember {
        mutableStateOf(themeSettingsStore.readAppearanceMode())
    }
    var mediaPreview by remember { mutableStateOf<MediaPreviewRequest?>(null) }
    var searchPendingQuery by remember { mutableStateOf<String?>(null) }
    var searchPendingMode by remember { mutableStateOf<SearchMode?>(null) }
    var searchMode by remember { mutableStateOf(storedSearchMode(searchSettingsStore.readMode())) }
    var searchWeiboSort by remember { mutableStateOf(storedSearchWeiboSort(searchSettingsStore.readWeiboSort())) }
    var message by remember { mutableStateOf<NativeUiMessage?>(null) }
    var hasLoginCookie by remember { mutableStateOf(session.hasLoginCookie()) }
    var storedAccounts by remember { mutableStateOf(accountStore.readAccounts()) }
    var activeAccountId by remember { mutableStateOf(accountStore.readActiveAccountId()) }
    var cacheLoaded by remember { mutableStateOf(false) }
    var feedScrollRestoreApplied by remember { mutableStateOf(false) }
    var expandedFeedItems by remember { mutableStateOf<Map<String, FeedItem>>(emptyMap()) }
    var longTextLoadingIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    var likeLoadingIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    var likeUsersOverlay by remember { mutableStateOf<LikeUsersOverlayState?>(null) }
    var likeUsers by remember { mutableStateOf<List<MentionCandidate>>(emptyList()) }
    var likeUsersLoading by remember { mutableStateOf(false) }
    var likeUsersLoadingMore by remember { mutableStateOf(false) }
    var likeUsersNextPage by remember { mutableStateOf<Int?>(null) }
    var likeUsersError by remember { mutableStateOf<String?>(null) }
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
    var recentCommentEmoticons by remember { mutableStateOf<List<String>>(emptyList()) }
    var emoticonSyncing by remember { mutableStateOf(false) }
    var mentionAvatarSuggestions by remember { mutableStateOf<List<MentionCandidate>>(emptyList()) }
    var mentionNameIndex by remember { mutableStateOf<List<MentionCandidate>>(emptyList()) }
    var mentionSuggestionsUid by remember { mutableStateOf<String?>(null) }
    var mentionSuggestionsRefreshedUid by remember { mutableStateOf<String?>(null) }
    var mentionSuggestionsLoading by remember { mutableStateOf(false) }
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
    var lastHomeBackPressAt by remember { mutableStateOf(0L) }
    var exitHintVisible by remember { mutableStateOf(false) }
    var exitHintToken by remember { mutableIntStateOf(0) }
    var pendingOpenAccountLogin by remember { mutableStateOf(false) }
    var articleOverlay by remember { mutableStateOf<ArticleOverlayState?>(null) }
    var followListOverlay by remember { mutableStateOf<FollowListOverlayState?>(null) }
    var followListOverlayInstanceKey by remember { mutableIntStateOf(0) }
    val followListTabCaches = remember { mutableStateMapOf<String, FollowListTabCache>() }

    fun scrollLazyListState(listState: LazyListState, scroll: ScrollRestore) {
        scope.launch {
            var attempts = 0
            while (attempts < 24 && listState.layoutInfo.totalItemsCount == 0) {
                delay(16)
                attempts++
            }
            runCatching {
                listState.scrollToItem(scroll.index, scroll.offset)
            }
        }
    }

    fun loadArticleIntoOverlay(entity: FeedUrlEntity) {
        val articleId = resolveArticleId(entity.url) ?: resolveArticleId(entity.shortUrl)
        articleOverlay = ArticleOverlayState(entity = entity, loading = articleId != null)
        if (articleId == null) return
        scope.launch {
            runCatching { session.loadArticle(articleId) }
                .onSuccess { article ->
                    articleOverlay = ArticleOverlayState(
                        entity = entity,
                        article = article,
                        loading = false,
                    )
                }
                .onFailure { error ->
                    articleOverlay = ArticleOverlayState(
                        entity = entity,
                        loading = false,
                        error = error.message ?: "文章加载失败",
                    )
                }
        }
    }

    fun openAccountLoginManagement() {
        selectedTab = MainTab.Mine
        pendingOpenAccountLogin = true
    }

    fun showMessage(title: String, detail: String) {
        message = NativeUiMessage(title, detail)
        scope.launch { snackbarHostState.showSnackbar("$title\uFF1A$detail") }
    }

    fun absorbDiscoveredEmoticons(discovered: Map<String, String>) {
        if (discovered.isEmpty()) return
        scope.launch {
            val merged = emoticonCacheStore.merge(discovered, overwriteExisting = false)
            if (merged != emoticonMap) {
                emoticonMap = merged
            }
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
                        val merged = emoticonCacheStore.merge(synced, overwriteExisting = true)
                        emoticonMap = merged
                        recentCommentEmoticons = emoticonCacheStore.readRecent().filter { it in merged }
                        operationCapsuleHint = "已同步 ${synced.size} 个表情"
                    }
                }
                .onFailure { error ->
                    showMessage("表情同步失败", error.message ?: "无法读取微博表情配置")
                }
            emoticonSyncing = false
        }
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

    suspend fun persistLoginSession(makeActive: Boolean = false) {
        runCatching { session.persistCurrentAccount(accountStore, makeActive = makeActive) }
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

    fun restoreVisitedProfileScroll(snapshot: VisitedProfileSnapshot) {
        scope.launch {
            var attempts = 0
            while (attempts < 24 && visitedPostsListState.layoutInfo.totalItemsCount == 0) {
                delay(16)
                attempts++
            }
            if (visitedPostsListState.firstVisibleItemIndex != snapshot.postsScrollIndex ||
                visitedPostsListState.firstVisibleItemScrollOffset != snapshot.postsScrollOffset
            ) {
                runCatching {
                    visitedPostsListState.scrollToItem(snapshot.postsScrollIndex, snapshot.postsScrollOffset)
                }
            }
            attempts = 0
            while (attempts < 24 && visitedAlbumListState.layoutInfo.totalItemsCount == 0) {
                delay(16)
                attempts++
            }
            if (visitedAlbumListState.firstVisibleItemIndex != snapshot.albumScrollIndex ||
                visitedAlbumListState.firstVisibleItemScrollOffset != snapshot.albumScrollOffset
            ) {
                runCatching {
                    visitedAlbumListState.scrollToItem(snapshot.albumScrollIndex, snapshot.albumScrollOffset)
                }
            }
        }
    }

    fun restoreVisitedProfileSnapshot(
        snapshot: VisitedProfileSnapshot,
        incrementGeneration: Boolean = true,
    ) {
        visitedAlbumJob?.cancel()
        if (incrementGeneration) {
            visitedProfileLoadGeneration += 1
            visitedListScrollResetGeneration = visitedProfileLoadGeneration
        } else {
            visitedListScrollResetGeneration = visitedProfileLoadGeneration
        }
        val sameProfileContext = !incrementGeneration &&
            snapshot.userId == visitedUserId &&
            snapshot.profile == visitedProfile &&
            snapshot.posts === visitedPosts &&
            snapshot.albumImages === visitedAlbumImages
        if (sameProfileContext) {
            visitedMinePagerPage = snapshot.pagerPage
            restoreVisitedProfileScroll(snapshot)
            return
        }
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
        restoreVisitedProfileScroll(snapshot)
    }

    fun clearVisitedProfileState() {
        visitedAlbumJob?.cancel()
        visitedListScrollResetGeneration = -1
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

    fun loadVisitedUserAlbum(uid: String, loadGeneration: Int, force: Boolean = false) {
        if (!force && visitedAlbumImages.isNotEmpty()) return
        if (visitedAlbumLoading || visitedAlbumLoadingMore) return
        visitedAlbumJob?.cancel()
        visitedAlbumJob = scope.launch {
            visitedAlbumLoading = true
            visitedAlbumError = null
            if (force) {
                visitedAlbumImages = emptyList()
                visitedAlbumNextCursor = null
                visitedAlbumHasMore = true
            }
            try {
                val page = session.loadUserAlbumImages(uid = uid)
                if (loadGeneration == visitedProfileLoadGeneration && uid == visitedProfile?.id) {
                    val lookup = buildAlbumPostLookup(visitedPosts)
                    visitedAlbumImages = enrichAlbumImagesFromPosts(page.images, lookup)
                    visitedAlbumNextCursor = page.nextCursor
                    visitedAlbumHasMore = page.nextCursor != null
                    visitedAlbumError = if (page.images.isEmpty() && page.nextCursor == null) {
                        "\u76F8\u518C\u6682\u65E0\u5185\u5BB9"
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

    fun ensureVisitedAlbumLoaded() {
        val uid = visitedProfile?.id?.takeIf { it.isNotBlank() } ?: return
        loadVisitedUserAlbum(uid, visitedProfileLoadGeneration)
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
                        if (visitedMinePagerPage == MineContentTab.Album.ordinal) {
                            loadVisitedUserAlbum(profile.id, loadGeneration, force = true)
                        }
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
            contentSection = detailContentSection,
            reposts = reposts,
            repostsNextPage = repostsNextPage,
            repostsHasMore = repostsHasMore,
            scrollIndex = lastDetailScroll.index,
            scrollOffset = lastDetailScroll.offset,
            albumViewerState = albumViewerState,
            instanceKey = activeDetailInstanceKey,
        )
    }

    fun restoreDetailSnapshot(snapshot: DetailSnapshot) {
        commentsRequestJob?.cancel()
        repostsRequestJob?.cancel()
        detailRequestGeneration += 1
        commentsLoading = false
        commentsLoadingMore = false
        repostsLoading = false
        repostsLoadingMore = false
        nestedCommentsLoadingIds = emptySet()
        selectedItem = snapshot.item
        comments = snapshot.comments
        commentsCursor = snapshot.commentsCursor
        commentsHasMore = snapshot.commentsHasMore
        commentSort = snapshot.commentSort
        detailContentSection = snapshot.contentSection
        reposts = snapshot.reposts
        repostsNextPage = snapshot.repostsNextPage
        repostsHasMore = snapshot.repostsHasMore
        albumViewerState = snapshot.albumViewerState
        activeDetailInstanceKey = snapshot.instanceKey
        lastDetailScroll = ScrollRestore(snapshot.scrollIndex, snapshot.scrollOffset)
        detailScrollPending = snapshot.item.id to ScrollRestore(snapshot.scrollIndex, snapshot.scrollOffset)
        detailScrollRestoreToken += 1
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
                    val lookup = buildAlbumPostLookup(visitedPosts)
                    val enrichedPage = enrichAlbumImagesFromPosts(page.images, lookup)
                    visitedAlbumImages = (visitedAlbumImages + enrichedPage).distinctBy { it.largeUrl }
                    visitedAlbumNextCursor = page.nextCursor
                    visitedAlbumHasMore = page.nextCursor != null
                }
            visitedAlbumLoadingMore = false
        }
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
            if (listState.firstVisibleItemIndex == viewer.albumScrollIndex &&
                listState.firstVisibleItemScrollOffset == viewer.albumScrollOffset
            ) {
                return@launch
            }
            runCatching {
                listState.scrollToItem(viewer.albumScrollIndex, viewer.albumScrollOffset)
            }
        }
    }

    fun captureNavRestoreState(): NavRestoreState = NavRestoreState(
        selectedTab = selectedTab,
        feedScroll = ScrollRestore(
            feedListState.firstVisibleItemIndex,
            feedListState.firstVisibleItemScrollOffset,
        ),
        minePostsScroll = ScrollRestore(
            minePostsListState.firstVisibleItemIndex,
            minePostsListState.firstVisibleItemScrollOffset,
        ),
        mineAlbumScroll = ScrollRestore(
            mineAlbumListState.firstVisibleItemIndex,
            mineAlbumListState.firstVisibleItemScrollOffset,
        ),
        minePagerPage = minePagerPage,
        searchScroll = ScrollRestore(
            searchListState.firstVisibleItemIndex,
            searchListState.firstVisibleItemScrollOffset,
        ),
        visitedProfile = currentVisitedProfileSnapshot(),
        detail = currentDetailSnapshot(),
        followListOverlay = followListOverlay,
        followListFollowingScroll = ScrollRestore(
            followListFollowingListState.firstVisibleItemIndex,
            followListFollowingListState.firstVisibleItemScrollOffset,
        ),
        followListFansScroll = ScrollRestore(
            followListFansListState.firstVisibleItemIndex,
            followListFansListState.firstVisibleItemScrollOffset,
        ),
        albumViewerState = albumViewerState,
        articleOverlay = articleOverlay,
        mediaPreview = mediaPreview,
    )

    fun applyDeferredNavRestore(state: NavRestoreState) {
        mediaPreview = null
        articleOverlay = null
        albumViewerState = null

        if (state.visitedProfile != null) {
            restoreVisitedProfileSnapshot(state.visitedProfile, incrementGeneration = false)
        } else {
            clearVisitedProfileState()
        }

        scrollLazyListState(feedListState, state.feedScroll)
        scrollLazyListState(minePostsListState, state.minePostsScroll)
        scrollLazyListState(mineAlbumListState, state.mineAlbumScroll)
        scrollLazyListState(searchListState, state.searchScroll)

        if (state.followListOverlay != null) {
            scrollLazyListState(followListFollowingListState, state.followListFollowingScroll)
            scrollLazyListState(followListFansListState, state.followListFansScroll)
        }

        if (state.albumViewerState != null) {
            if (state.detail != null) {
                restoreDetailSnapshot(state.detail)
            } else {
                selectedItem = null
                comments = emptyList()
                commentsCursor = null
                commentsHasMore = true
            }
            albumViewerState = state.albumViewerState
            restoreProfilePagerFromViewer(state.albumViewerState)
            restoreAlbumScrollFromViewer(state.albumViewerState)
        } else if (state.detail != null) {
            restoreDetailSnapshot(state.detail)
        } else {
            selectedItem = null
            comments = emptyList()
            commentsCursor = null
            commentsHasMore = true
        }

        articleOverlay = state.articleOverlay
        mediaPreview = state.mediaPreview
        followListOverlay = state.followListOverlay
    }

    fun flushDeferredNavRestore() {
        val state = pendingDeferredNavRestore ?: return
        pendingDeferredNavRestore = null
        applyDeferredNavRestore(state)
    }

    fun scheduleDeferredNavRestore(state: NavRestoreState) {
        pendingDeferredNavRestore = state
        val generation = deferredNavRestoreGeneration + 1
        deferredNavRestoreGeneration = generation
        scope.launch {
            delay(NavTransitionDurationMs.toLong() + 32L)
            if (deferredNavRestoreGeneration == generation && pendingDeferredNavRestore === state) {
                flushDeferredNavRestore()
            }
        }
    }

    fun clearNavExitPendingKind() {
        navExitPendingKind = null
        flushDeferredNavRestore()
    }

    fun applyNavRestoreState(state: NavRestoreState) {
        selectedTab = state.selectedTab
        minePagerPage = state.minePagerPage

        if (navExitPendingKind != null) {
            scheduleDeferredNavRestore(state)
        } else {
            pendingDeferredNavRestore = null
            followListOverlay = state.followListOverlay
            applyDeferredNavRestore(state)
        }
    }

    fun pushNavigation(kind: NavOverlayKind, navigate: () -> Unit) {
        navStack = navStack + captureNavRestoreState()
        navOverlayStack = navOverlayStack + kind
        navEnterPendingKind = kind
        navExitPendingKind = null
        navigate()
    }

    fun popNavigation(): Boolean {
        if (navStack.isEmpty()) return false
        navExitPendingKind = navOverlayStack.lastOrNull()
        navEnterPendingKind = null
        val previous = navStack.last()
        navStack = navStack.dropLast(1)
        navOverlayStack = navOverlayStack.dropLast(1)
        applyNavRestoreState(previous)
        return true
    }

    fun navigateBack() {
        popNavigation()
    }

    fun closeArticleOverlay() {
        navigateBack()
    }

    fun closeFollowList() {
        navigateBack()
    }

    fun dismissFollowListForTabSwitch() {
        if (followListOverlay == null) return
        followListOverlay = null
        navExitPendingKind = null
        if (navStack.isNotEmpty()) {
            navStack = navStack.dropLast(1)
            navOverlayStack = navOverlayStack.dropLast(1)
        }
    }

    fun openFollowList(
        uid: String,
        screenName: String,
        avatarUrl: String?,
        description: String?,
        tab: FriendListTab,
    ) {
        pushNavigation(NavOverlayKind.FollowList(uid)) {
            followListOverlayInstanceKey += 1
            followListOverlay = FollowListOverlayState(
                uid = uid,
                screenName = screenName,
                avatarUrl = avatarUrl,
                description = description,
                tab = tab,
                instanceKey = followListOverlayInstanceKey,
            )
        }
    }

    fun pushMediaPreview(media: FeedMedia, playbackOwnerId: String) {
        val nextKey = videoPlaybackKey(media, playbackOwnerId)
        if (mediaPreview?.let { videoPlaybackKey(it.media, it.playbackOwnerId) == nextKey } == true) {
            return
        }
        videoPlaybackCoordinator.claimFullscreenPlayback(nextKey)
        pushNavigation(NavOverlayKind.MediaPreview) {
            mediaOverlayInstanceKey = navStack.size
            mediaPreview = MediaPreviewRequest(media, playbackOwnerId)
        }
    }

    fun openFloatingPlaybackFromFullscreen(media: FeedMedia, playbackOwnerId: String) {
        val playbackKey = videoPlaybackKey(media, playbackOwnerId)
        val displayMetrics = context.resources.displayMetrics
        val screenWidthPx = displayMetrics.widthPixels.toFloat()
        val screenHeightPx = displayMetrics.heightPixels.toFloat()
        val anchorBounds = Rect(
            left = 0f,
            top = 0f,
            right = screenWidthPx,
            bottom = screenHeightPx,
        )
        videoPlaybackCoordinator.beginPeekHandoff(playbackKey)
        videoPlaybackCoordinator.claimPeekPlayback(playbackKey)
        navigateBack()
        videoPeekController.openFloating(
            VideoPeekRequest(
                media = media,
                anchorBounds = anchorBounds,
                pressOffset = Offset(screenWidthPx / 2f, screenHeightPx / 2f),
                playbackOwnerId = playbackOwnerId,
                resolveAnchorBounds = { anchorBounds },
                fromFullscreen = true,
                onCancel = {
                    videoPlaybackCoordinator.cancelPeekHandoff(playbackKey)
                },
                onRelease = {},
                onPlaybackEnded = {
                    videoPeekController.dismissForPlaybackEnded()
                },
                onOpenFullscreenBehind = {
                    videoPlaybackCoordinator.beginFullscreenHandoff(playbackKey)
                    pushMediaPreview(media, playbackOwnerId)
                },
                onEnterFullscreenHandoffComplete = {},
            ),
        )
    }

    fun pushAlbumViewer(viewer: AlbumViewerState) {
        pushNavigation(NavOverlayKind.AlbumViewer) {
            albumOverlayInstanceKey = navStack.size
            albumViewerState = viewer
        }
    }

    fun dismissAlbumViewer() {
        if (albumViewerState == null) return
        if (navOverlayStack.lastOrNull() == NavOverlayKind.AlbumViewer) {
            popNavigation()
            return
        }
        val viewer = albumViewerState ?: return
        albumViewerState = null
        restoreProfilePagerFromViewer(viewer)
        restoreAlbumScrollFromViewer(viewer)
    }

    fun closeVisitedProfile() {
        navigateBack()
    }

    fun openSearchTopic(topic: String) {
        val bareTopic = topic.removePrefix("#").removeSuffix("#").trim()
        val normalized = if (bareTopic.isBlank()) "" else "#$bareTopic#"
        if (normalized.isBlank()) return
        if (selectedTab == MainTab.Search && visitedUserId == null && selectedItem == null) {
            searchPendingQuery = normalized
            searchPendingMode = SearchMode.Weibo
            bottomBarExpanded = true
            return
        }
        pushNavigation(NavOverlayKind.TabSwitch) {
            selectedItem = null
            comments = emptyList()
            commentsCursor = null
            commentsHasMore = true
            if (visitedUserId != null) {
                clearVisitedProfileState()
            }
            searchPendingQuery = normalized
            searchPendingMode = SearchMode.Weibo
            selectedTab = MainTab.Search
            bottomBarExpanded = true
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

        pushNavigation(NavOverlayKind.VisitedProfile(value)) {
            if (selectedItem == null) {
                comments = emptyList()
                commentsCursor = null
                commentsHasMore = true
            }
            loadVisitedUserProfile(
                if (value.all { it.isDigit() }) {
                    ProfileLookup.Uid(value)
                } else {
                    ProfileLookup.ScreenName(value)
                },
            )
        }
    }

    fun openUserFromFollowList(userId: String) {
        val value = userId.trim().removePrefix("@")
        if (value.isBlank()) return
        val isSameUser = if (value.all { it.isDigit() }) {
            value == visitedProfile?.id || value == visitedUserId
        } else {
            value == visitedProfile?.screenName ||
                value == visitedUserScreenName ||
                value == visitedUserId
        }
        if (isSameUser) {
            closeFollowList()
            return
        }
        pushNavigation(NavOverlayKind.VisitedProfile(value)) {
            if (selectedItem == null) {
                comments = emptyList()
                commentsCursor = null
                commentsHasMore = true
            }
            loadVisitedUserProfile(
                if (value.all { it.isDigit() }) {
                    ProfileLookup.Uid(value)
                } else {
                    ProfileLookup.ScreenName(value)
                },
            )
        }
    }

    fun closeDetail() {
        val floatingActive = videoPeekController.activeRequest != null &&
            videoPeekController.isFloating &&
            videoPeekController.pendingDismiss == null
        val autoScrollFloating = floatingActive &&
            videoPlaybackCoordinator.activeKey == null &&
            videoPlaybackCoordinator.peekPlaybackKey != null &&
            videoPlaybackCoordinator.isAutoScrollFloating(
                videoPlaybackCoordinator.peekPlaybackKey.orEmpty(),
            )
        // 详情内联播放、自动滚动浮窗：返回首页时必须停掉，否则会出现只闻其声、点首页才恢复画面。
        // 仅保留用户从全屏主动浮窗的播放。
        val keepUserFloating = floatingActive &&
            !autoScrollFloating &&
            videoPeekController.activeRequest?.fromFullscreen == true
        if (!keepUserFloating) {
            if (floatingActive) {
                videoPlaybackCoordinator.peekPlaybackKey?.let { key ->
                    videoPlaybackCoordinator.clearAutoScrollFloating(key)
                    videoPlaybackCoordinator.cancelPeekHandoff(key)
                    videoPlaybackCoordinator.clearInlineHandoffResume(key)
                }
                videoPeekController.cancel()
            }
            videoPlaybackCoordinator.activeKey?.let { key ->
                videoPlaybackCoordinator.cancelPeekHandoff(key)
                videoPlaybackCoordinator.clearInlineHandoffResume(key)
                videoPlaybackCoordinator.clearAutoScrollFloating(key)
                videoPlaybackCoordinator.finishDetailHandoff(key)
                videoPlaybackCoordinator.completeDetailPlaybackHandoff(key)
            }
            videoPlaybackCoordinator.pauseInlineOnly()
            videoPlaybackCoordinator.pausePeek()
            videoPlaybackCoordinator.activeKey = null
            videoPlaybackCoordinator.peekPlaybackKey = null
        }
        popNavigation()
    }

    fun prepareInlineVideoHandoffForDetail(item: FeedItem, hostItem: FeedItem? = null) {
        if (videoPlaybackCoordinator.peekPlaybackKey != null) return
        val activeKey = videoPlaybackCoordinator.activeKey ?: return
        val candidateKeys = buildSet {
            addAll(feedItemInlinePlaybackKeys(item))
            hostItem?.let { addAll(feedItemInlinePlaybackKeys(it)) }
        }
        val shouldHandoff = candidateKeys.any { candidate ->
            videoPlaybackCoordinator.matchesPlaybackKey(activeKey, candidate)
        }
        if (shouldHandoff) {
            videoPlaybackCoordinator.beginDetailHandoff(activeKey)
        } else {
            videoPlaybackCoordinator.pauseInlineOnly()
            videoPlaybackCoordinator.activeKey = null
        }
    }

    fun handleRootBackPress() {
        val now = System.currentTimeMillis()
        if (now - lastHomeBackPressAt <= 2_000L) {
            (context as? android.app.Activity)?.finish()
        } else {
            lastHomeBackPressAt = now
            exitHintVisible = true
            exitHintToken += 1
        }
    }

    BackHandler(enabled = true) {
        if (albumViewerState != null) {
            dismissAlbumViewer()
            lastHomeBackPressAt = 0L
            return@BackHandler
        }
        if (popNavigation()) {
            lastHomeBackPressAt = 0L
            return@BackHandler
        }
        when {
            selectedTab == MainTab.Messages -> Unit
            selectedTab == MainTab.Compose -> {
                lastHomeBackPressAt = 0L
                selectedTab = MainTab.Feed
            }
            selectedTab != MainTab.Feed -> {
                lastHomeBackPressAt = 0L
                selectedTab = MainTab.Feed
            }
            else -> handleRootBackPress()
        }
    }

    fun reconcileFeedLikeState(freshItems: List<FeedItem>) {
        if (freshItems.isEmpty()) return
        val freshLookup = buildMap<String, FeedItem> {
            fun register(item: FeedItem) {
                if (item.statusId.isNotBlank()) put(item.statusId, item)
                if (item.id.isNotBlank()) put(item.id, item)
            }
            freshItems.forEach { item ->
                register(item)
                item.retweetedStatus?.let(::register)
            }
        }
        expandedFeedItems = expandedFeedItems.mapValues { (_, expanded) ->
            if (expanded.statusId in likeLoadingIds) return@mapValues expanded
            val fresh = freshLookup[expanded.statusId] ?: freshLookup[expanded.id] ?: return@mapValues expanded
            val mergedLikesCount = mergeAuthoritativeLikeCount(expanded.likesCount, fresh.likesCount)
            val mergedCommentsCount = mergeAuthoritativeLikeCount(expanded.commentsCount, fresh.commentsCount)
            val mergedRepostsCount = mergeAuthoritativeLikeCount(expanded.repostsCount, fresh.repostsCount)
            var merged = expanded.copy(
                liked = fresh.liked,
                likesCount = mergedLikesCount,
                commentsCount = mergedCommentsCount,
                repostsCount = mergedRepostsCount,
            )
            val freshRetweet = fresh.retweetedStatus
            val itemRetweet = expanded.retweetedStatus
            if (freshRetweet != null && itemRetweet != null) {
                merged = merged.copy(
                    retweetedStatus = itemRetweet.copy(
                        liked = freshRetweet.liked,
                        likesCount = mergeAuthoritativeLikeCount(
                            itemRetweet.likesCount,
                            freshRetweet.likesCount,
                        ),
                        commentsCount = mergeAuthoritativeLikeCount(
                            itemRetweet.commentsCount,
                            freshRetweet.commentsCount,
                        ),
                        repostsCount = mergeAuthoritativeLikeCount(
                            itemRetweet.repostsCount,
                            freshRetweet.repostsCount,
                        ),
                    ),
                )
            }
            merged
        }
    }

    fun refreshTimeline() {
        timelineRefreshJob?.cancel()
        timelineLoadMoreJob?.cancel()
        timelineRequestGeneration += 1
        val requestGeneration = timelineRequestGeneration
        val requestedKind = timelineKind
        timelineRefreshJob = scope.launch {
            val previousItems = items
            isLoading = true
            feedLoadingMore = false
            hasLoginCookie = session.hasLoginCookie()
            if (feedListState.layoutInfo.totalItemsCount > 0) {
                feedListState.animateScrollToTopFixed()
            }
            runCatchingPreservingCancellation {
                val raw = session.loadTimelineRaw(requestedKind)
                if (requestedKind == TimelineKind.Following) {
                    timelineCacheStore.writeFollowingTimeline(raw)
                }
                WeiboJsonParser.parseTimeline(raw)
            }
                .onSuccess { page ->
                    if (requestGeneration != timelineRequestGeneration || requestedKind != timelineKind) {
                        return@onSuccess
                    }
                    items = sortFeedTimelineItems(page.items)
                    reconcileFeedLikeState(page.items)
                    nextCursor = page.nextCursor
                    absorbDiscoveredEmoticons(page.items.collectAllEmoticons())
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
                    if (requestGeneration != timelineRequestGeneration || requestedKind != timelineKind) {
                        return@onFailure
                    }
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
            if (requestGeneration == timelineRequestGeneration && requestedKind == timelineKind) {
                isLoading = false
                timelineRefreshJob = null
                if (feedListState.layoutInfo.totalItemsCount > 0) {
                    feedListState.animateScrollToTopFixed()
                }
            }
        }
    }

    fun refreshTimelineFromTop() {
        refreshTimeline()
        return
        scope.launch {
            val previousItems = items
            feedListState.animateScrollToTopFixed()
            isLoading = true
            hasLoginCookie = session.hasLoginCookie()
            runCatching {
                val raw = session.loadTimelineRaw(timelineKind)
                if (timelineKind == TimelineKind.Following) {
                    timelineCacheStore.writeFollowingTimeline(raw)
                }
                WeiboJsonParser.parseTimeline(raw)
            }
                .onSuccess { page ->
                    items = sortFeedTimelineItems(page.items)
                    reconcileFeedLikeState(page.items)
                    nextCursor = page.nextCursor
                    absorbDiscoveredEmoticons(page.items.collectAllEmoticons())
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
            feedListState.animateScrollToTopFixed()
        }
    }

    fun switchTimelineKind(kind: TimelineKind) {
        if (kind == timelineKind) return
        timelineRefreshJob?.cancel()
        timelineLoadMoreJob?.cancel()
        timelineRequestGeneration += 1
        timelineKind = kind
        items = emptyList()
        nextCursor = null
        feedRefreshHint = null
        refreshTimeline()
    }

    fun loadMore() {
        val cursor = nextCursor ?: return
        if (feedLoadingMore || isLoading) return
        val requestGeneration = timelineRequestGeneration
        val requestedKind = timelineKind
        timelineLoadMoreJob = scope.launch {
            feedLoadingMore = true
            runCatchingPreservingCancellation { session.loadTimeline(requestedKind, cursor) }
                .onSuccess { page ->
                    if (requestGeneration != timelineRequestGeneration || requestedKind != timelineKind) {
                        return@onSuccess
                    }
                    val (merged, appended) = mergeFeedTimelinePages(items, page.items)
                    items = merged
                    reconcileFeedLikeState(page.items)
                    absorbDiscoveredEmoticons(page.items.collectAllEmoticons())
                    nextCursor = when {
                        page.items.isEmpty() -> null
                        appended == 0 -> null
                        else -> page.nextCursor ?: page.items.lastOrNull()?.id
                    }
                }
                .onFailure { error ->
                    if (requestGeneration == timelineRequestGeneration && requestedKind == timelineKind) {
                        showMessage("\u52A0\u8F7D\u5931\u8D25", error.message ?: "\u65E0\u6CD5\u8BFB\u53D6\u4E0B\u4E00\u9875")
                    }
                }
            if (requestGeneration == timelineRequestGeneration && requestedKind == timelineKind) {
                feedLoadingMore = false
                timelineLoadMoreJob = null
            }
        }
    }

    fun loadMineAlbumFirstPage(force: Boolean = false) {
        val uid = mineProfile?.id?.takeIf { it.isNotBlank() } ?: return
        if (!force && mineAlbumImages.isNotEmpty()) return
        if (mineAlbumLoading || mineAlbumLoadingMore) return
        mineAlbumJob?.cancel()
        mineAlbumJob = scope.launch {
            mineAlbumLoading = true
            mineAlbumError = null
            if (force) {
                mineAlbumImages = emptyList()
                mineAlbumNextCursor = null
                mineAlbumHasMore = true
            }
            try {
                val page = session.loadUserAlbumImages(uid = uid)
                val lookup = buildAlbumPostLookup(minePosts)
                mineAlbumImages = filterOutRetweetedOnlyImages(
                    enrichAlbumImagesFromPosts(page.images, lookup),
                    minePosts,
                )
                mineAlbumNextCursor = page.nextCursor
                mineAlbumHasMore = page.nextCursor != null
                mineAlbumError = if (page.images.isEmpty() && page.nextCursor == null) {
                    "\u76F8\u518C\u6682\u65E0\u5185\u5BB9"
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
                    if (minePagerPage == MineContentTab.Album.ordinal) {
                        loadMineAlbumFirstPage(force = true)
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

    fun deleteStoredAccount(accountId: String) {
        scope.launch {
            val deletingActive = activeAccountId == accountId
            accountStore.removeAccount(accountId)
            reloadStoredAccounts()
            val nextActiveId = activeAccountId
            if (deletingActive) {
                if (nextActiveId != null && accountStore.getAccount(nextActiveId) != null) {
                    runCatching { session.activateAccount(accountStore, nextActiveId) }
                        .onSuccess {
                            hasLoginCookie = session.hasLoginCookie()
                            mineHasLoginCookie = hasLoginCookie
                            refreshTimeline()
                            refreshMineProfile()
                        }
                        .onFailure { error ->
                            showMessage("账号删除成功", "但切换到下一个账号失败：${error.message ?: "登录态不可用"}")
                        }
                } else {
                    session.clearAllCookies()
                    hasLoginCookie = false
                    mineHasLoginCookie = false
                    items = emptyList()
                    nextCursor = null
                    mineProfile = null
                    minePosts = emptyList()
                    mineAlbumImages = emptyList()
                }
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
                    val lookup = buildAlbumPostLookup(minePosts)
                    val enrichedPage = enrichAlbumImagesFromPosts(page.images, lookup)
                    mineAlbumImages = filterOutRetweetedOnlyImages(
                        (mineAlbumImages + enrichedPage).distinctBy { it.largeUrl },
                        minePosts,
                    )
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
        val base = expandedFeedItems[item.statusId] ?: item
        val resolvedRetweet = base.retweetedStatus?.let { resolveFeedItem(it) }
        return when {
            base === item && resolvedRetweet === base.retweetedStatus -> item
            resolvedRetweet === base.retweetedStatus -> base
            else -> base.copy(retweetedStatus = resolvedRetweet)
        }
    }

    fun mergeExpandedIntoItem(item: FeedItem, expanded: FeedItem): FeedItem =
        when {
            item.statusId == expanded.statusId -> expanded
            item.retweetedStatus != null -> {
                val mergedRetweet = mergeExpandedIntoItem(item.retweetedStatus, expanded)
                if (mergedRetweet !== item.retweetedStatus) {
                    item.copy(retweetedStatus = mergedRetweet)
                } else {
                    item
                }
            }
            else -> item
        }

    fun applyExpandedItem(expanded: FeedItem) {
        expandedFeedItems = expandedFeedItems + (expanded.statusId to expanded)
        absorbDiscoveredEmoticons(expanded.collectEmoticons())
        items = items.map { mergeExpandedIntoItem(it, expanded) }
        minePosts = minePosts.map { mergeExpandedIntoItem(it, expanded) }
        visitedPosts = visitedPosts.map { mergeExpandedIntoItem(it, expanded) }
        selectedItem = selectedItem?.let { mergeExpandedIntoItem(it, expanded) }
        navStack = navStack.map { state ->
            state.copy(
                detail = state.detail?.copy(item = mergeExpandedIntoItem(state.detail.item, expanded)),
                visitedProfile = state.visitedProfile?.copy(
                    posts = state.visitedProfile.posts.map { mergeExpandedIntoItem(it, expanded) },
                ),
            )
        }
        likeUsersOverlay = likeUsersOverlay?.let { overlay ->
            val merged = mergeExpandedIntoItem(overlay.item, expanded)
            if (merged !== overlay.item) overlay.copy(item = merged) else overlay
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

    fun syncItemDisplayCount(
        item: FeedItem,
        totalCount: Int?,
        loadedCount: Int,
        currentCount: (FeedItem) -> String,
        updateCount: (FeedItem, String) -> FeedItem,
    ) {
        val resolved = resolveFeedItem(item)
        val current = parseApproxDisplayCount(currentCount(resolved)) ?: 0
        val authoritativeCount = listOfNotNull(
            totalCount?.takeIf { it > 0 },
            loadedCount.takeIf { it > 0 },
        ).maxOrNull() ?: return
        if (authoritativeCount <= current) return
        applyExpandedItem(
            updateCount(resolved, WeiboJsonParser.formatDisplayCount(authoritativeCount)),
        )
    }

    fun syncItemLikeCountFromLikeUsers(item: FeedItem, page: LikeUsersPage, loadedCount: Int) {
        syncItemDisplayCount(
            item = item,
            totalCount = page.totalCount,
            loadedCount = loadedCount,
            currentCount = { resolveFeedItem(item).likesCount },
            updateCount = { resolved, formatted ->
                resolved.copy(likesCount = formatted)
            },
        )
    }

    fun syncItemCommentsCount(item: FeedItem, totalCount: Int?, loadedCount: Int) {
        syncItemDisplayCount(
            item = item,
            totalCount = totalCount,
            loadedCount = loadedCount,
            currentCount = { resolveFeedItem(item).commentsCount },
            updateCount = { resolved, formatted ->
                resolved.copy(commentsCount = formatted)
            },
        )
    }

    fun syncItemRepostsCount(item: FeedItem, totalCount: Int?, loadedCount: Int) {
        syncItemDisplayCount(
            item = item,
            totalCount = totalCount,
            loadedCount = loadedCount,
            currentCount = { resolveFeedItem(item).repostsCount },
            updateCount = { resolved, formatted ->
                resolved.copy(repostsCount = formatted)
            },
        )
    }

    fun applyLikeUsersLoadResult(
        item: FeedItem,
        page: LikeUsersPage,
        mergedUsers: List<MentionCandidate>,
        currentPage: Int,
        previousKnownTotal: Int? = null,
    ): Int? {
        syncItemLikeCountFromLikeUsers(item, page, mergedUsers.size)
        val resolved = resolveFeedItem(item)
        val knownTotal = listOfNotNull(
            previousKnownTotal,
            page.totalCount?.takeIf { it > 0 },
            parseApproxDisplayCount(resolved.likesCount),
        ).maxOrNull()
        likeUsersOverlay = likeUsersOverlay?.copy(knownTotal = knownTotal)
        return resolveLikeUsersNextPage(
            page = page,
            currentPage = currentPage,
            loadedCount = mergedUsers.size,
            knownTotal = knownTotal,
        )
    }

    fun openLikeUsers(item: FeedItem, anchorBounds: Rect) {
        val resolved = resolveFeedItem(item)
        if (resolved.hasNoLikes()) {
            operationCapsuleHint = "\u6682\u65e0\u70b9\u8d5e"
            return
        }
        val likeId = resolved.id.trim().takeIf { it.isNotBlank() && it != "0" && it.all(Char::isDigit) }
            ?: resolved.statusId.trim().takeIf { it.isNotBlank() && it.all(Char::isDigit) }
            ?: run {
                showMessage("无法查看点赞", "微博 ID 无效")
                return
            }
        likeUsersOverlay = LikeUsersOverlayState(
            item = resolved,
            anchorBounds = anchorBounds,
            likeId = likeId,
        )
        likeUsers = emptyList()
        likeUsersError = null
        likeUsersNextPage = null
        likeUsersLoadingMore = false
        scope.launch {
            likeUsersLoading = true
            runCatching { session.loadLikeUsers(likeId) }
                .onSuccess { page ->
                    likeUsers = page.users
                    likeUsersNextPage = applyLikeUsersLoadResult(
                        item = resolved,
                        page = page,
                        mergedUsers = page.users,
                        currentPage = 1,
                    )
                }
                .onFailure { error ->
                    likeUsersError = error.message ?: "加载失败"
                }
            likeUsersLoading = false
        }
    }

    fun loadMoreLikeUsers() {
        val overlay = likeUsersOverlay ?: return
        val nextPage = likeUsersNextPage ?: return
        if (likeUsersLoadingMore || likeUsersLoading) return
        val item = resolveFeedItem(overlay.item)
        scope.launch {
            likeUsersLoadingMore = true
            runCatching { session.loadLikeUsers(overlay.likeId, nextPage) }
                .onSuccess { page ->
                    if (page.users.isEmpty()) {
                        likeUsersNextPage = null
                        return@onSuccess
                    }
                    val merged = (likeUsers + page.users).distinctBy { mentionCandidateKey(it) }
                    if (merged.size == likeUsers.size) {
                        likeUsersNextPage = null
                        return@onSuccess
                    }
                    likeUsers = merged
                    likeUsersNextPage = applyLikeUsersLoadResult(
                        item = item,
                        page = page,
                        mergedUsers = merged,
                        currentPage = nextPage,
                        previousKnownTotal = overlay.knownTotal,
                    )
                }
                .onFailure { error ->
                    showMessage("加载失败", error.message ?: "无法继续读取点赞列表")
                }
            likeUsersLoadingMore = false
        }
    }

    fun closeLikeUsers() {
        likeUsersOverlay = null
        likeUsers = emptyList()
        likeUsersError = null
        likeUsersNextPage = null
        likeUsersLoadingMore = false
    }

    fun loadLongText(item: FeedItem) {
        if (!item.requiresLongTextFetch || item.statusId in longTextLoadingIds) return
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
        commentsRequestJob?.cancel()
        val requestGeneration = detailRequestGeneration
        val requestedItemId = item.id
        val requestedSort = commentSort
        commentsRequestJob = scope.launch {
            commentsLoading = true
            commentsLoadingMore = false
            commentsCursor = null
            commentsHasMore = true
            runCatchingPreservingCancellation { session.loadComments(item, requestedSort) }
                .onSuccess { page ->
                    if (
                        requestGeneration != detailRequestGeneration ||
                        selectedItem?.id != requestedItemId ||
                        commentSort != requestedSort
                    ) {
                        return@onSuccess
                    }
                    comments = page.items
                    absorbDiscoveredEmoticons(page.items.collectAllCommentEmoticons())
                    commentsCursor = page.nextCursor
                    commentsHasMore = page.nextCursor != null
                    syncItemCommentsCount(item, page.totalCount, page.items.size)
                }
                .onFailure { error ->
                    if (
                        requestGeneration != detailRequestGeneration ||
                        selectedItem?.id != requestedItemId ||
                        commentSort != requestedSort
                    ) {
                        return@onFailure
                    }
                    showMessage("\u8BC4\u8BBA\u52A0\u8F7D\u5931\u8D25", error.message ?: "\u5FAE\u535A\u63A5\u53E3\u65E0\u54CD\u5E94")
                }
            if (
                requestGeneration == detailRequestGeneration &&
                selectedItem?.id == requestedItemId &&
                commentSort == requestedSort
            ) {
                commentsLoading = false
                commentsRequestJob = null
            }
        }
    }

    fun openCommentComposer(status: FeedItem, replyTo: CommentItem? = null) {
        detailContentSection = DetailContentSection.Comments
        commentComposeTarget = CommentComposeTarget(resolveFeedItem(status), replyTo)
    }

    fun submitComment(target: CommentComposeTarget, text: String, photoUris: List<Uri>, alsoRepost: Boolean) {
        if (commentSubmitting) return
        val job = scope.launch {
            commentSubmitting = true
            try {
                val timeoutMs = if (photoUris.isNotEmpty()) 90_000L else 45_000L
                withTimeout(timeoutMs) {
                    val picId = photoUris.firstOrNull()?.let { uri ->
                        session.uploadCommentImage(uri)
                    }
                    session.postStatusComment(
                        item = target.status,
                        text = text,
                        replyToCommentId = target.replyTo?.id,
                        alsoRepost = alsoRepost,
                        picId = picId,
                    )
                }
                commentComposeTarget = null
                operationCapsuleHint = if (target.isReply) {
                    "已回复 @${target.replyTo?.authorName}"
                } else {
                    "评论已发布"
                }
                val updated = target.status.copy(
                    commentsCount = WeiboJsonParser.bumpDisplayCount(target.status.commentsCount, 1),
                ).let { status ->
                    if (alsoRepost) {
                        status.copy(
                            repostsCount = WeiboJsonParser.bumpDisplayCount(status.repostsCount, 1),
                        )
                    } else {
                        status
                    }
                }
                applyExpandedItem(updated)
                selectedItem = selectedItem?.let { mergeExpandedIntoItem(it, updated) }
                reloadComments()
            } catch (error: CancellationException) {
                if (error is TimeoutCancellationException) {
                    operationCapsuleHint = commentFailureMessage(error)
                } else {
                    throw error
                }
            } catch (error: Throwable) {
                operationCapsuleHint = commentFailureMessage(error)
            } finally {
                commentSubmitting = false
                commentSubmitJob = null
            }
        }
        commentSubmitJob = job
    }

    fun submitComposeWeibo(text: String, visibility: WeiboPostVisibility, photoUris: List<Uri>) {
        if (composeSubmitting) return
        scope.launch {
            composeSubmitting = true
            try {
                val timeoutMs = if (photoUris.isNotEmpty()) 120_000L else 60_000L
                withTimeout(timeoutMs) {
                    session.postStatus(
                        content = text,
                        visibility = visibility,
                        photoUris = photoUris,
                    )
                }
                operationCapsuleHint = "微博已发布"
                composeSessionKey += 1
                selectedTab = MainTab.Feed
                refreshTimelineFromTop()
            } catch (error: CancellationException) {
                if (error is TimeoutCancellationException) {
                    operationCapsuleHint = "发布超时，请稍后重试"
                } else {
                    throw error
                }
            } catch (error: Throwable) {
                operationCapsuleHint = error.message ?: "微博发布失败"
            } finally {
                composeSubmitting = false
            }
        }
    }

    fun reloadReposts() {
        val item = selectedItem ?: return
        repostsRequestJob?.cancel()
        val requestGeneration = detailRequestGeneration
        val requestedItemId = item.id
        repostsRequestJob = scope.launch {
            repostsLoading = true
            repostsLoadingMore = false
            repostsNextPage = null
            repostsHasMore = true
            runCatchingPreservingCancellation { session.loadReposts(item, page = 1) }
                .onSuccess { page ->
                    if (
                        requestGeneration != detailRequestGeneration ||
                        selectedItem?.id != requestedItemId
                    ) {
                        return@onSuccess
                    }
                    reposts = page.items
                    repostsNextPage = page.nextPage
                    repostsHasMore = page.nextPage != null
                    syncItemRepostsCount(item, page.totalCount, page.items.size)
                }
                .onFailure { error ->
                    if (
                        requestGeneration != detailRequestGeneration ||
                        selectedItem?.id != requestedItemId
                    ) {
                        return@onFailure
                    }
                    showMessage("\u8F6C\u53D1\u52A0\u8F7D\u5931\u8D25", error.message ?: "\u5FAE\u535A\u63A5\u53E3\u65E0\u54CD\u5E94")
                }
            if (
                requestGeneration == detailRequestGeneration &&
                selectedItem?.id == requestedItemId
            ) {
                repostsLoading = false
                repostsRequestJob = null
            }
        }
    }

    fun selectDetailContentSection(section: DetailContentSection) {
        if (detailContentSection == section) return
        detailContentSection = section
        if (section == DetailContentSection.Reposts && reposts.isEmpty() && !repostsLoading) {
            reloadReposts()
        }
    }

    fun reloadDetailContent() {
        when (detailContentSection) {
            DetailContentSection.Comments -> reloadComments()
            DetailContentSection.Reposts -> reloadReposts()
        }
    }

    fun resetDetailContentState(initialSection: DetailContentSection = DetailContentSection.Comments) {
        commentsRequestJob?.cancel()
        repostsRequestJob?.cancel()
        detailRequestGeneration += 1
        commentsLoading = false
        commentsLoadingMore = false
        repostsLoading = false
        repostsLoadingMore = false
        nestedCommentsLoadingIds = emptySet()
        comments = emptyList()
        commentsCursor = null
        commentsHasMore = true
        detailContentSection = initialSection
        reposts = emptyList()
        repostsNextPage = null
        repostsHasMore = true
    }

    fun openDetailPrepared(
        item: FeedItem,
        initialSection: DetailContentSection,
        scrollToContentSection: Boolean,
    ) {
        val resolved = resolveFeedItem(item)
        prepareInlineVideoHandoffForDetail(resolved)
        feedCardActionMenuController.dismiss()
        activeDetailInstanceKey += 1
        val scroll = if (scrollToContentSection) {
            ScrollRestore(index = DETAIL_SECTION_HEADER_INDEX)
        } else {
            ScrollRestore()
        }
        lastDetailScroll = scroll
        detailScrollPending = resolved.id to scroll
        selectedItem = resolved
        resetDetailContentState(initialSection)
        if (resolved.requiresLongTextFetch) {
            loadLongText(resolved)
        }
        resolved.retweetedStatus?.takeIf { it.requiresLongTextFetch }?.let { loadLongText(it) }
        when (initialSection) {
            DetailContentSection.Comments -> reloadComments()
            DetailContentSection.Reposts -> reloadReposts()
        }
    }

    fun openDetailInternal(item: FeedItem, hostItem: FeedItem? = null) {
        prepareInlineVideoHandoffForDetail(
            resolveFeedItem(item),
            hostItem?.let(::resolveFeedItem),
        )
        pushNavigation(NavOverlayKind.Detail(resolveFeedItem(item).id)) {
            openDetailPrepared(
                item = item,
                initialSection = DetailContentSection.Comments,
                scrollToContentSection = false,
            )
        }
    }

    fun openUrlEntity(entity: FeedUrlEntity) {
        val articleId = resolveArticleId(entity.url) ?: resolveArticleId(entity.shortUrl)
        if (articleId != null) {
            pushNavigation(NavOverlayKind.Article) {
                articleOverlayInstanceKey = navStack.size
                loadArticleIntoOverlay(entity)
            }
            return
        }
        val statusCandidates = WeiboLinkResolver.statusIdCandidates(entity.url, entity.shortUrl)
        if (statusCandidates.isNotEmpty()) {
            scope.launch {
                val item = statusCandidates.firstNotNullOfOrNull { candidate ->
                    runCatching { session.loadStatusDetail(candidate) }.getOrNull()
                }
                if (item != null) {
                    openDetailInternal(item)
                } else {
                    pushNavigation(NavOverlayKind.Article) {
                articleOverlayInstanceKey = navStack.size
                loadArticleIntoOverlay(entity)
            }
                }
            }
            return
        }
        pushNavigation(NavOverlayKind.Article) { loadArticleIntoOverlay(entity) }
    }

    fun openDetailToSection(item: FeedItem, section: DetailContentSection) {
        prepareInlineVideoHandoffForDetail(resolveFeedItem(item))
        pushNavigation(NavOverlayKind.Detail(resolveFeedItem(item).id)) {
            openDetailPrepared(
                item = item,
                initialSection = section,
                scrollToContentSection = true,
            )
        }
    }

    fun openDetail(item: FeedItem, hostItem: FeedItem? = null) {
        openDetailInternal(item, hostItem)
    }

    fun openDetailFromSource(item: FeedItem, sourceBounds: Rect?, hostItem: FeedItem? = null) {
        openDetailInternal(item, hostItem)
    }

    fun openDetailFromAlbumViewer(item: FeedItem, viewer: AlbumViewerState) {
        val resolved = resolveFeedItem(item)
        prepareInlineVideoHandoffForDetail(resolved)
        feedCardActionMenuController.dismiss()
        pushNavigation(NavOverlayKind.Detail(resolved.id)) {
            restoreProfilePagerFromViewer(viewer)
            albumViewerState = null
            activeDetailInstanceKey += 1
            lastDetailScroll = ScrollRestore()
            detailScrollPending = resolved.id to ScrollRestore()
            selectedItem = resolved
            resetDetailContentState()
            if (resolved.requiresLongTextFetch) {
                loadLongText(resolved)
            }
            resolved.retweetedStatus?.takeIf { it.requiresLongTextFetch }?.let { loadLongText(it) }
            reloadComments()
        }
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
        val requestGeneration = detailRequestGeneration
        val requestedItemId = item.id
        val requestedSort = commentSort
        commentsRequestJob = scope.launch {
            commentsLoading = true
            commentsLoadingMore = true
            runCatchingPreservingCancellation { session.loadMoreComments(item, cursor, requestedSort) }
                .onSuccess { page ->
                    if (
                        requestGeneration != detailRequestGeneration ||
                        selectedItem?.id != requestedItemId ||
                        commentSort != requestedSort
                    ) {
                        return@onSuccess
                    }
                    comments = comments + page.items
                    absorbDiscoveredEmoticons(page.items.collectAllCommentEmoticons())
                    commentsCursor = page.nextCursor
                    commentsHasMore = page.nextCursor != null
                    syncItemCommentsCount(item, page.totalCount, comments.size)
                }
                .onFailure { error ->
                    if (
                        requestGeneration != detailRequestGeneration ||
                        selectedItem?.id != requestedItemId ||
                        commentSort != requestedSort
                    ) {
                        return@onFailure
                    }
                    showMessage("评论加载失败", error.message ?: "微博接口无响应")
                }
            if (
                requestGeneration == detailRequestGeneration &&
                selectedItem?.id == requestedItemId &&
                commentSort == requestedSort
            ) {
                commentsLoading = false
                commentsLoadingMore = false
                commentsRequestJob = null
            }
        }
    }

    fun loadMoreReposts() {
        val item = selectedItem ?: return
        val page = repostsNextPage ?: return
        if (repostsLoading || !repostsHasMore) return
        val requestGeneration = detailRequestGeneration
        val requestedItemId = item.id
        repostsRequestJob = scope.launch {
            repostsLoading = true
            repostsLoadingMore = true
            runCatchingPreservingCancellation { session.loadReposts(item, page) }
                .onSuccess { result ->
                    if (
                        requestGeneration != detailRequestGeneration ||
                        selectedItem?.id != requestedItemId
                    ) {
                        return@onSuccess
                    }
                    reposts = mergeCommentItems(reposts, result.items)
                    repostsNextPage = result.nextPage
                    repostsHasMore = result.nextPage != null
                    syncItemRepostsCount(item, result.totalCount, reposts.size)
                }
                .onFailure { error ->
                    if (
                        requestGeneration != detailRequestGeneration ||
                        selectedItem?.id != requestedItemId
                    ) {
                        return@onFailure
                    }
                    showMessage("转发加载失败", error.message ?: "微博接口无响应")
                }
            if (
                requestGeneration == detailRequestGeneration &&
                selectedItem?.id == requestedItemId
            ) {
                repostsLoading = false
                repostsLoadingMore = false
                repostsRequestJob = null
            }
        }
    }

    fun loadNestedCommentsPage(commentId: String) {
        val item = selectedItem ?: return
        val authorUid = item.authorId.takeIf { it.isNotBlank() } ?: return
        val requestGeneration = detailRequestGeneration
        val requestedItemId = item.id
        if (commentId in nestedCommentsLoadingIds) return
        val parent = findCommentInTree(comments, commentId) ?: return
        val append = parent.moreInfoText == null && parent.nestedNextCursor != null
        if (!append && parent.moreInfoText == null) return
        val cursor = if (append) parent.nestedNextCursor else null
        scope.launch {
            nestedCommentsLoadingIds = nestedCommentsLoadingIds + commentId
            runCatchingPreservingCancellation {
                session.loadNestedComments(commentId, authorUid, cursor)
            }
                .onSuccess { page ->
                    if (
                        requestGeneration != detailRequestGeneration ||
                        selectedItem?.id != requestedItemId
                    ) {
                        return@onSuccess
                    }
                    comments = updateCommentTree(comments, commentId) { comment ->
                        comment.copy(
                            comments = if (append) {
                                mergeCommentItems(comment.comments, page.items)
                            } else {
                                sortNestedCommentsByTime(page.items)
                            },
                            moreInfoText = null,
                            nestedNextCursor = page.nextCursor,
                        )
                    }
                }
                .onFailure { error ->
                    if (
                        requestGeneration != detailRequestGeneration ||
                        selectedItem?.id != requestedItemId
                    ) {
                        return@onFailure
                    }
                    val title = if (append) "\u697C\u4E2D\u697C\u52A0\u8F7D\u5931\u8D25" else "\u697C\u4E2D\u697C\u5C55\u5F00\u5931\u8D25"
                    showMessage(title, error.message ?: "\u5FAE\u535A\u63A5\u53E3\u65E0\u54CD\u5E94")
                }
            if (
                requestGeneration == detailRequestGeneration &&
                selectedItem?.id == requestedItemId
            ) {
                nestedCommentsLoadingIds = nestedCommentsLoadingIds - commentId
            }
        }
    }

    suspend fun resolveMentionUid(): String? =
        mineProfile?.id?.takeIf { it.isNotBlank() }
            ?: activeAccountId?.takeIf { it.isNotBlank() }
            ?: runCatching { session.loadCurrentUserProfile().id }.getOrNull()?.takeIf { it.isNotBlank() }

    suspend fun applyMentionSuggestionCache(uid: String) {
        mentionSuggestionCacheStore.read(uid)?.let { cached ->
            mentionAvatarSuggestions = cached.avatarSuggestions
            mentionNameIndex = cached.nameIndex
        }
    }

    suspend fun refreshMentionSuggestions(showLoadingIfEmpty: Boolean) {
        if (!hasLoginCookie) return
        val uid = resolveMentionUid() ?: return
        applyMentionSuggestionCache(uid)
        val shouldShowLoading = showLoadingIfEmpty &&
            mentionAvatarSuggestions.isEmpty() &&
            mentionNameIndex.isEmpty()
        if (shouldShowLoading) {
            mentionSuggestionsLoading = true
        }
        runCatching { session.loadMentionSuggestionBundle(uid) }
            .onSuccess { bundle ->
                mentionAvatarSuggestions = bundle.avatarSuggestions
                mentionNameIndex = bundle.nameIndex
                mentionSuggestionCacheStore.write(uid, bundle)
            }
        mentionSuggestionsLoading = false
        mentionSuggestionsUid = uid
        mentionSuggestionsRefreshedUid = uid
    }

    LaunchedEffect(Unit) {
        val savedActiveId = accountStore.readActiveAccountId()
        if (savedActiveId != null && accountStore.getAccount(savedActiveId) != null) {
            runCatching { session.activateAccount(accountStore, savedActiveId) }
        } else if (session.hasLoginCookie()) {
            persistLoginSession(makeActive = true)
        }
        reloadStoredAccounts()

        emoticonMap = emoticonCacheStore.read()
        recentCommentEmoticons = emoticonCacheStore.readRecent().filter { it in emoticonMap }
        timelineCacheStore.readFollowingTimeline()?.let { page ->
            items = sortFeedTimelineItems(page.items)
            nextCursor = page.nextCursor
            absorbDiscoveredEmoticons(page.items.collectAllEmoticons())
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
            val lookup = buildAlbumPostLookup(minePosts)
            mineAlbumImages = filterOutRetweetedOnlyImages(
                enrichAlbumImagesFromPosts(page.images, lookup),
                minePosts,
            )
            mineAlbumNextCursor = page.nextCursor?.takeIf {
                it.startsWith("wall:") || it.startsWith("waterfall:cursor:")
            }
            mineAlbumHasMore = mineAlbumNextCursor != null
        }
        activeAccountId?.takeIf { it.isNotBlank() }?.let { uid ->
            applyMentionSuggestionCache(uid)
            mentionSuggestionsUid = uid
        }
        hasLoginCookie = session.hasLoginCookie()
        mineHasLoginCookie = hasLoginCookie
        cacheLoaded = true
        feedScrollRestoreApplied = true
        if (items.isEmpty() && hasLoginCookie) {
            refreshTimeline()
        }
    }

    LaunchedEffect(feedListState, timelineKind) {
        snapshotFlow {
            feedListState.isScrollInProgress
        }
            .distinctUntilChanged()
            .collect { scrolling ->
                if (!scrolling &&
                    timelineKind == TimelineKind.Following &&
                    cacheLoaded &&
                    feedScrollRestoreApplied
                ) {
                    timelineCacheStore.writeFollowingScroll(
                        feedListState.firstVisibleItemIndex,
                        feedListState.firstVisibleItemScrollOffset,
                    )
                }
            }
    }

    LaunchedEffect(activeAccountId, mineProfile?.id, hasLoginCookie) {
        if (!hasLoginCookie) {
            mentionAvatarSuggestions = emptyList()
            mentionNameIndex = emptyList()
            mentionSuggestionsUid = null
            mentionSuggestionsRefreshedUid = null
            return@LaunchedEffect
        }
        val uid = resolveMentionUid() ?: return@LaunchedEffect
        if (uid == mentionSuggestionsRefreshedUid) return@LaunchedEffect
        refreshMentionSuggestions(showLoadingIfEmpty = false)
    }

    LaunchedEffect(commentComposeTarget) {
        if (commentComposeTarget == null || !hasLoginCookie) return@LaunchedEffect
        refreshMentionSuggestions(showLoadingIfEmpty = true)
    }

    LaunchedEffect(minePosts) {
        if (mineAlbumImages.isEmpty() || minePosts.isEmpty()) return@LaunchedEffect
        val enriched = filterOutRetweetedOnlyImages(
            enrichAlbumImagesFromPosts(mineAlbumImages, buildAlbumPostLookup(minePosts)),
            minePosts,
        )
        if (enriched != mineAlbumImages) {
            mineAlbumImages = enriched
        }
    }

    LaunchedEffect(visitedPosts) {
        if (visitedAlbumImages.isEmpty() || visitedPosts.isEmpty()) return@LaunchedEffect
        val enriched = filterOutRetweetedOnlyImages(
            enrichAlbumImagesFromPosts(visitedAlbumImages, buildAlbumPostLookup(visitedPosts)),
            visitedPosts,
        )
        if (enriched != visitedAlbumImages) {
            visitedAlbumImages = enriched
        }
    }

    DisposableEffect(Unit) {
        val trimCallback = object : ComponentCallbacks2 {
            override fun onConfigurationChanged(newConfig: Configuration) = Unit

            override fun onLowMemory() {
                trimImageCaches(0f)
            }

            override fun onTrimMemory(level: Int) {
                when (level) {
                    TrimMemoryRunningLow,
                    TrimMemoryRunningCritical,
                    TrimMemoryComplete,
                    -> trimImageCaches(0.35f)
                    ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN -> trimImageCaches(0.6f)
                }
            }
        }
        context.registerComponentCallbacks(trimCallback)
        onDispose {
            context.unregisterComponentCallbacks(trimCallback)
            session.webView.destroy()
        }
    }

    val bottomBarListState: LazyListState? = when {
        selectedItem != null -> null
        visitedUserId != null -> {
            if (visitedMinePagerPage == MineContentTab.Posts.ordinal) {
                visitedPostsListState
            } else {
                visitedAlbumListState
            }
        }
        selectedTab == MainTab.Mine -> {
            if (minePagerPage == MineContentTab.Posts.ordinal) {
                minePostsListState
            } else {
                mineAlbumListState
            }
        }
        selectedTab == MainTab.Feed -> feedListState
        else -> null
    }

    LaunchedEffect(selectedTab, visitedUserId) {
        if (selectedTab != MainTab.Messages && selectedTab != MainTab.Compose) {
            bottomBarExpanded = true
        }
        bottomBarAwaitingOutsideDismiss = false
    }

    LaunchedEffect(bottomBarListState) {
        val state = bottomBarListState ?: run {
            feedListScrollCoordinator.isListScrolling = false
            feedListScrollCoordinator.stopScrollAction = null
            return@LaunchedEffect
        }
        feedListScrollCoordinator.stopScrollAction = { state.stopScroll(MutatePriority.UserInput) }
        var lastScrollTotal =
            state.firstVisibleItemIndex * 10_000 + state.firstVisibleItemScrollOffset
        try {
            coroutineScope {
                launch {
                    snapshotFlow { state.isScrollInProgress }
                        .distinctUntilChanged()
                        .collect { scrolling ->
                            feedListScrollCoordinator.isListScrolling = scrolling
                        }
                }
                snapshotFlow {
                    state.firstVisibleItemIndex * 10_000 + state.firstVisibleItemScrollOffset
                }.collect { scrollTotal ->
                    if (scrollTotal > lastScrollTotal + BottomBarCollapseScrollDelta) {
                        bottomBarExpanded = false
                        bottomBarAwaitingOutsideDismiss = false
                    }
                    lastScrollTotal = scrollTotal
                }
            }
        } finally {
            feedListScrollCoordinator.stopScrollAction = null
            feedListScrollCoordinator.isListScrolling = false
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, backgroundPlaybackEnabled, timelineKind, cacheLoaded, feedScrollRestoreApplied) {
        fun persistFeedScrollNow() {
            if (timelineKind == TimelineKind.Following && cacheLoaded && feedScrollRestoreApplied) {
                timelineCacheStore.writeFollowingScroll(
                    feedListState.firstVisibleItemIndex,
                    feedListState.firstVisibleItemScrollOffset,
                )
            }
        }
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) {
                persistFeedScrollNow()
                if (!backgroundPlaybackEnabled) {
                    videoPlaybackCoordinator.pauseAll()
                    videoPlaybackCoordinator.activeKey = null
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            persistFeedScrollNow()
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(
        selectedTab,
        visitedUserId,
        visitedPosts,
        selectedItem,
        mediaPreview,
        articleOverlay,
        followListOverlay,
        albumViewerState,
        videoPeekController.activeRequest,
        videoPeekController.isFloating,
        videoPeekController.isFullscreenMode,
        videoPeekController.pendingDismiss,
        videoPlaybackCoordinator.pendingPeekHandoffKey,
        videoPlaybackCoordinator.activeKey,
        videoPlaybackCoordinator.peekPlaybackKey,
    ) {
        val homeFeedOnTop = selectedTab == MainTab.Feed &&
            visitedUserId == null &&
            selectedItem == null &&
            mediaPreview == null &&
            articleOverlay == null &&
            followListOverlay == null &&
            albumViewerState == null
        val keepFloatingPeekPlayback = videoPeekController.activeRequest != null &&
            videoPeekController.isFloating &&
            !videoPeekController.isFullscreenMode &&
            videoPeekController.pendingDismiss == null
        val profileInlinePlaybackActive = visitedUserId != null &&
            selectedItem == null &&
            albumViewerState == null &&
            mediaPreview == null &&
            articleOverlay == null &&
            followListOverlay == null
        val searchInlinePlaybackActive = selectedTab == MainTab.Search &&
            visitedUserId == null &&
            selectedItem == null &&
            followListOverlay == null &&
            articleOverlay == null &&
            mediaPreview == null &&
            albumViewerState == null
        val mineInlinePlaybackActive = selectedTab == MainTab.Mine &&
            visitedUserId == null &&
            selectedItem == null &&
            followListOverlay == null &&
            articleOverlay == null &&
            mediaPreview == null &&
            albumViewerState == null
        if (!homeFeedOnTop) {
            when {
                mediaPreview != null || keepFloatingPeekPlayback -> {
                    // 浮窗已接管：切勿 pauseInlineOnly。内联 pause handler 可能仍挂着同一
                    // ExoPlayer，会把浮窗刚恢复的播放再次 pause → 黑屏无声。
                    videoPlaybackCoordinator.activeKey = null
                }
                selectedItem != null -> {
                    // 详情页冷启动点播会写入 activeKey；本 effect 以 activeKey 为 key，
                    // 若非 handoff 时一律清成 null，会立刻拆掉 PlayerView，出现有声无画。
                    val preserveKey = if (videoPlaybackCoordinator.detailHandoffActive) {
                        videoPlaybackCoordinator.pendingPeekHandoffKey
                            ?: videoPlaybackCoordinator.activeKey
                    } else {
                        videoPlaybackCoordinator.activeKey
                    }
                    videoPlaybackCoordinator.pauseInlineOnly(exceptKey = preserveKey)
                    if (videoPlaybackCoordinator.activeKey != preserveKey) {
                        videoPlaybackCoordinator.activeKey = preserveKey
                    }
                }
                profileInlinePlaybackActive -> {
                    val profilePlaybackKeys = visitedPosts
                        .map(::resolveFeedItem)
                        .flatMap(::feedItemInlinePlaybackKeys)
                        .toSet()
                    fun keyBelongsToProfile(key: String): Boolean =
                        profilePlaybackKeys.any { candidate ->
                            videoPlaybackCoordinator.matchesPlaybackKey(candidate, key)
                        }
                    val peekKey = videoPlaybackCoordinator.peekPlaybackKey
                    val peekActiveOnProfile = peekKey != null &&
                        keyBelongsToProfile(peekKey) &&
                        videoPeekController.activeRequest != null &&
                        videoPeekController.pendingDismiss == null
                    if (peekActiveOnProfile) {
                        videoPlaybackCoordinator.pauseInlineOnly(exceptKey = peekKey)
                        return@LaunchedEffect
                    }
                    val currentKey = videoPlaybackCoordinator.activeKey
                    val preserveKey = currentKey?.takeIf { it in profilePlaybackKeys }
                    videoPlaybackCoordinator.pauseInlineOnly(exceptKey = preserveKey)
                    videoPlaybackCoordinator.activeKey = preserveKey
                    if (preserveKey == null && peekKey == null) {
                        currentKey?.let(videoPlaybackCoordinator::cancelPeekHandoff)
                        if (videoPeekController.activeRequest != null) {
                            videoPeekController.cancel()
                        }
                    }
                }
                searchInlinePlaybackActive ||
                    mineInlinePlaybackActive -> {
                    val preserveKey = videoPlaybackCoordinator.activeKey
                    videoPlaybackCoordinator.pauseInlineOnly(exceptKey = preserveKey)
                    videoPlaybackCoordinator.activeKey = preserveKey
                }
                else -> {
                    videoPlaybackCoordinator.pauseAll()
                    videoPlaybackCoordinator.activeKey = null
                }
            }
        }
    }

    val systemInDarkTheme = isSystemInDarkTheme()
    val darkTheme = when (appearanceMode) {
        AppearanceMode.Light -> false
        AppearanceMode.Dark -> true
        AppearanceMode.System -> systemInDarkTheme
    }

    MyWeiboTheme(darkTheme = darkTheme) {
    val themedColorScheme = MaterialTheme.colorScheme.copy(
        primary = selectedThemeColor.primary,
        primaryContainer = selectedThemeColor.primaryContainer,
        secondary = selectedThemeColor.secondary,
        tertiary = selectedThemeColor.primary,
        inversePrimary = selectedThemeColor.primary,
    )

    MaterialTheme(
        colorScheme = themedColorScheme,
        typography = MaterialTheme.typography,
    ) {
    CompositionLocalProvider(
        LocalVideoPlaybackCoordinator provides videoPlaybackCoordinator,
        LocalUiMessenger provides { title, detail -> showMessage(title, detail) },
        LocalTopicClickHandler provides ::openSearchTopic,
    ) {
    MediaLongPressConfiguration {
    MyWeiboScaffold(
        snackbarHostState = snackbarHostState,
    ) { innerPadding ->
        val hazeState = rememberHazeState()
        val bottomBarBackdrop = rememberLayerBackdrop()
        val searchBarOverlay = remember { SearchBarOverlayController() }
        var timelineMenuExpanded by remember { mutableStateOf(false) }
        val imagePeekController = remember { ImagePeekController() }
        val feedImageUpgradeNotifier = remember { FeedImageUpgradeNotifier() }
        val imageSaveHintController = remember { ImageSaveHintController() }
        val feedTypographyMetrics = rememberFeedTypographyMetrics(feedFontSize, feedLineSpacing)
        val feedEmoticonInlineContent = rememberFeedEmoticonInlineContent(emoticonMap, feedTypographyMetrics)
        LaunchedEffect(feedFontSize, feedLineSpacing) {
            FeedEmoticonTextCache.clear()
        }
        LaunchedEffect(emoticonMap) {
            val urls = emoticonMap.values.distinct()
            if (urls.isEmpty()) return@LaunchedEffect
            withContext(Dispatchers.IO) {
                urls.take(300).forEach { url ->
                    if (FeedBitmapCache.get(url) != null) return@forEach
                    decodeCachedRemoteBitmap(url, EmoticonBitmapMaxDecodeDim) ?: run {
                        runCatching {
                            val bytes = FeedImageLoadSemaphore.withPermit {
                                fetchRemoteBytes(
                                    url = url,
                                    connectTimeoutMs = 5000,
                                    readTimeoutMs = 5000,
                                    maxReadBytes = 2 * 1024 * 1024,
                                )
                            }
                            decodeBitmapFromBytes(bytes, EmoticonBitmapMaxDecodeDim)?.also { bitmap ->
                                FeedBitmapCache.put(url, bitmap)
                            }
                        }.getOrNull()
                    }
                }
            }
        }
        val topicClickHandler = LocalTopicClickHandler.current
        SideEffect {
            FeedEmoticonLinkDispatcher.onTopicClick = topicClickHandler
        }
        CompositionLocalProvider(
            LocalHazeState provides hazeState,
            LocalLiquidMenuBackdrop provides bottomBarBackdrop,
            LocalFeedCardActionMenuController provides feedCardActionMenuController,
            LocalNavTransitionCoordinator provides navTransitionCoordinator,
            LocalFeedListScrollCoordinator provides feedListScrollCoordinator,
            LocalImagePeekController provides imagePeekController,
            LocalImageSaveHint provides imageSaveHintController,
            LocalVideoPeekController provides videoPeekController,
            LocalFeedThumbnailQuality provides feedThumbnailQuality,
            LocalFeedBodyTextStyle provides feedTypographyMetrics.body,
            LocalFeedTypographyMetrics provides feedTypographyMetrics,
            LocalFeedEmoticonInlineContent provides feedEmoticonInlineContent,
            LocalFeedImageUpgradeNotifier provides feedImageUpgradeNotifier,
        ) {
            Box(Modifier.fillMaxSize()) {
            val detailOverlayItem = selectedItem?.let(::resolveFeedItem)
            val overlayTop = overlayTopKind(navOverlayStack)
            val detailLayerActive = navOverlayStack.any { it is NavOverlayKind.Detail } && detailOverlayItem != null
            val detailOverlayVisible = overlayTop == OverlayTop.Detail && detailLayerActive
            val detailNavKind = detailOverlayItem?.id?.let { NavOverlayKind.Detail(itemId = it) }
            val profileLayerActive = navOverlayStack.any { it is NavOverlayKind.VisitedProfile } && visitedUserId != null
            val visitedProfileVisible = overlayTop == OverlayTop.VisitedProfile && profileLayerActive
            val followListLayerActive = navOverlayStack.any { it is NavOverlayKind.FollowList } &&
                followListOverlay != null
            val followListUiOnTop = overlayTop == OverlayTop.FollowList && followListLayerActive
            val followListNavKind = followListOverlay?.uid?.let { NavOverlayKind.FollowList(it) }
            val followListExiting = navOverlayKindsMatch(navExitPendingKind, followListNavKind)
            val albumLayerActive = navOverlayStack.any { it == NavOverlayKind.AlbumViewer } && albumViewerState != null
            val albumViewerVisible = overlayTop == OverlayTop.AlbumViewer && albumLayerActive
            val mediaLayerActive = navOverlayStack.any { it == NavOverlayKind.MediaPreview } && mediaPreview != null
            val mediaPreviewVisible = overlayTop == OverlayTop.MediaPreview && mediaLayerActive
            val articleLayerActive = navOverlayStack.any { it == NavOverlayKind.Article } && articleOverlay != null
            val articleOverlayVisible = overlayTop == OverlayTop.Article && articleLayerActive
            Box(Modifier.matchParentSize().hazeSource(state = hazeState)) {
            Box(Modifier.fillMaxSize().padding(innerPadding)) {
            val mainContentClear = visitedUserId == null && selectedItem == null
            val messagesWebVisible = selectedTab == MainTab.Messages && mainContentClear
            val composeWebVisible = selectedTab == MainTab.Compose && mainContentClear
            val composeDisplayName = mineProfile?.screenName?.takeIf { it.isNotBlank() }
                ?: storedAccounts.firstOrNull { it.id == activeAccountId }?.screenName?.takeIf { it.isNotBlank() }
                ?: "微博用户"
            val composeAvatarUrl = mineProfile?.avatarUrl
                ?: storedAccounts.firstOrNull { it.id == activeAccountId }?.avatarUrl
            LaunchedEffect(composeWebVisible, activeAccountId, hasLoginCookie) {
                if (!composeWebVisible || !hasLoginCookie) return@LaunchedEffect
                runCatching {
                    refreshMentionSuggestions(showLoadingIfEmpty = true)
                    if (mineProfile == null && !mineProfileLoading) {
                        refreshMineProfile()
                    }
                }
            }
            var previousComposeAccountId by remember { mutableStateOf(activeAccountId) }
            LaunchedEffect(activeAccountId) {
                if (activeAccountId == previousComposeAccountId) return@LaunchedEffect
                previousComposeAccountId = activeAccountId
                composeSessionKey += 1
            }
            var messagesWebAuthSnapshot by remember {
                mutableStateOf<Triple<Boolean, String?, Boolean>?>(null)
            }
            LaunchedEffect(hasLoginCookie, activeAccountId, cacheLoaded) {
                val snapshot = Triple(hasLoginCookie, activeAccountId, cacheLoaded)
                if (snapshot == messagesWebAuthSnapshot) return@LaunchedEffect
                messagesWebAuthSnapshot = snapshot
                if (cacheLoaded && hasLoginCookie) {
                    messagesSessionKey += 1
                }
            }
            val webTabVisible = messagesWebVisible || composeWebVisible

            Box(
                Modifier
                    .then(if (messagesWebVisible) Modifier.fillMaxSize() else Modifier.size(0.dp))
                    .graphicsLayer {
                        alpha = if (messagesWebVisible) 1f else 0f
                        clip = true
                        if (messagesWebVisible) {
                            // 隔离 WebView 硬件合成层，避免干扰底部玻璃胶囊采样
                            compositingStrategy = CompositingStrategy.Offscreen
                        }
                    }
                    .zIndex(if (messagesWebVisible) 2f else -10f)
                    .blockHiddenTouches(messagesWebVisible),
            ) {
                if (!hasLoginCookie) {
                    WebTabLoginPlaceholder()
                } else {
                    key(messagesSessionKey) {
                        MessagesScreen(
                            onRootBack = ::handleRootBackPress,
                            active = messagesWebVisible,
                        )
                    }
                }
            }
            Box(
                Modifier
                    .then(if (composeWebVisible) Modifier.fillMaxSize() else Modifier.size(0.dp))
                    .graphicsLayer {
                        alpha = if (composeWebVisible) 1f else 0f
                        clip = true
                    }
                    .zIndex(if (composeWebVisible) 85f else -10f)
                    .blockHiddenTouches(composeWebVisible),
            ) {
                key(composeSessionKey) {
                    ComposeWeiboScreen(
                        active = composeWebVisible,
                        loggedIn = hasLoginCookie,
                        screenName = composeDisplayName,
                        avatarUrl = composeAvatarUrl,
                        submitting = composeSubmitting,
                        emoticonMap = emoticonMap,
                        recentEmoticons = recentCommentEmoticons,
                        mentionAvatarSuggestions = mentionAvatarSuggestions,
                        mentionNameIndex = mentionNameIndex,
                        mentionSuggestionsLoading = mentionSuggestionsLoading,
                        onEmoticonUsed = { phrase ->
                            recentCommentEmoticons = emoticonCacheStore
                                .touchRecent(phrase)
                                .filter { it in emoticonMap }
                        },
                        onSubmit = ::submitComposeWeibo,
                    )
                }
            }

            Box(
                Modifier
                    .fillMaxSize()
                    .zIndex(1f)
                    .layerBackdrop(bottomBarBackdrop),
            ) {
                Box(
                    Modifier
                        .matchParentSize()
                        .background(MaterialTheme.colorScheme.background),
                )
                if (webTabVisible && cacheLoaded) {
                    Box(
                        Modifier
                            .matchParentSize()
                            .background(Color.White),
                    )
                }
            val feedUiOnTop = selectedTab == MainTab.Feed &&
                visitedUserId == null &&
                detailOverlayItem == null
            val keepFeedAlive = selectedTab == MainTab.Feed && (
                (visitedUserId != null && detailOverlayItem == null) ||
                detailOverlayItem != null
            )
            val feedLayerVisible = selectedTab == MainTab.Feed && (feedUiOnTop || keepFeedAlive)
            val feedVisibleAlpha = if (feedUiOnTop || keepFeedAlive) 1f else 0f
            val searchUiOnTop = selectedTab == MainTab.Search &&
                visitedUserId == null &&
                detailOverlayItem == null &&
                followListOverlay == null &&
                articleOverlay == null &&
                mediaPreview == null &&
                albumViewerState == null
            val keepSearchAlive = selectedTab == MainTab.Search &&
                (visitedUserId != null ||
                    detailOverlayItem != null ||
                    followListOverlay != null ||
                    articleOverlay != null ||
                    mediaPreview != null ||
                    albumViewerState != null)
            val mineUiOnTop = selectedTab == MainTab.Mine &&
                visitedUserId == null &&
                detailOverlayItem == null &&
                followListOverlay == null &&
                articleOverlay == null &&
                mediaPreview == null &&
                albumViewerState == null
            val keepMineAlive = selectedTab == MainTab.Mine &&
                visitedUserId == null &&
                (detailOverlayItem != null ||
                    followListOverlay != null ||
                    articleOverlay != null ||
                    mediaPreview != null ||
                    albumViewerState != null)
            Box(Modifier.fillMaxSize()) {
                if (feedLayerVisible) {
                    Box(
                        Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background)
                            .graphicsLayer { alpha = feedVisibleAlpha }
                            .blockHiddenTouches(feedUiOnTop || composeWebVisible),
                    ) {
                        FollowFeedScreen(
                            session = session,
                            listState = feedListState,
                            items = items,
                            isLoading = isLoading,
                            cacheLoaded = cacheLoaded,
                            hasLoginCookie = hasLoginCookie,
                            emoticonMap = emoticonMap,
                            feedUiOnTop = feedUiOnTop,
                            onRefresh = { refreshTimeline() },
                            onLoadMore = { loadMore() },
                            onOpenLoginSettings = ::openAccountLoginManagement,
                            onUserClick = ::openUser,
                            onItemClick = { item, bounds -> openDetailFromSource(item, bounds) },
                            onRetweetClick = { retweeted, host ->
                                openDetailFromSource(retweeted, null, host)
                            },
                            onCommentClick = { item -> openDetailToSection(item, DetailContentSection.Comments) },
                            onCommentLongClick = { item -> openCommentComposer(item) },
                            onRepostClick = { item -> openDetailToSection(item, DetailContentSection.Reposts) },
                            onMediaClick = ::pushMediaPreview,
                            resolveFeedItem = ::resolveFeedItem,
                            isLongTextLoading = { it.statusId in longTextLoadingIds },
                            onLoadLongText = ::loadLongText,
                            onToggleLike = ::toggleStatusLike,
                            onLikeClick = ::openLikeUsers,
                            onUrlEntityClick = ::openUrlEntity,
                        )
                    }
                }

                NavAnimatedOverlay(
                    target = followListOverlay.takeIf { followListLayerActive },
                    modifier = Modifier
                        .fillMaxSize()
                        .blockHiddenTouches(followListUiOnTop || followListExiting),
                    stackTop = followListUiOnTop,
                    layerBaseZIndex = 550f,
                    visible = followListUiOnTop,
                    animationKey = followListOverlay?.instanceKey,
                    navKind = followListNavKind,
                    pendingEnterKind = navEnterPendingKind,
                    pendingExitKind = navExitPendingKind,
                    onClearPendingEnter = { navEnterPendingKind = null },
                    onClearPendingExit = ::clearNavExitPendingKind,
                ) { overlay ->
                        key(overlay.uid, overlay.instanceKey) {
                            FollowFansListScreen(
                                session = session,
                                overlay = overlay,
                                followingListState = followListFollowingListState,
                                fansListState = followListFansListState,
                                tabCaches = followListTabCaches,
                                backHandlerEnabled = followListUiOnTop,
                                onBack = ::closeFollowList,
                                onTabChange = { tab ->
                                    followListOverlay = overlay.copy(tab = tab)
                                },
                                onUserClick = ::openUserFromFollowList,
                                showMessage = ::showMessage,
                                mineProfileId = mineProfile?.id,
                            )
                        }
                }

                NavAnimatedOverlay(
                    target = MainTab.Search.takeIf { selectedTab == MainTab.Search && (searchUiOnTop || keepSearchAlive) },
                    modifier = Modifier.fillMaxSize(),
                    stackTop = searchUiOnTop,
                    visible = searchUiOnTop,
                    stackAnimated = false,
                ) {
                    SearchScreen(
                            session = session,
                            searchBarOverlay = searchBarOverlay,
                            searchHistoryStore = searchHistoryStore,
                            searchBarVisible = searchUiOnTop,
                            hasLoginCookie = hasLoginCookie,
                            pendingQuery = searchPendingQuery,
                            onPendingQueryConsumed = { searchPendingQuery = null },
                            pendingSearchMode = searchPendingMode,
                            onPendingSearchModeConsumed = { searchPendingMode = null },
                            emoticonMap = emoticonMap,
                            listState = searchListState,
                            resultsBackEnabled = searchUiOnTop,
                            onResultsNavigateBack = if (navStack.isNotEmpty()) ::navigateBack else null,
                            onItemClick = { item, _ -> openDetailFromSource(item, null) },
                            onMediaClick = ::pushMediaPreview,
                            onUserClick = ::openUser,
                            resolveFeedItem = ::resolveFeedItem,
                            isLongTextLoading = { it.statusId in longTextLoadingIds },
                            onLoadLongText = ::loadLongText,
                            onToggleLike = ::toggleStatusLike,
                            onLikeClick = ::openLikeUsers,
                            onUrlEntityClick = ::openUrlEntity,
                            onOpenLoginSettings = ::openAccountLoginManagement,
                            mineProfileId = mineProfile?.id,
                            searchMode = searchMode,
                            onSearchModeChange = { mode ->
                                searchMode = mode
                                searchSettingsStore.writeMode(mode.storageValue())
                            },
                            weiboSort = searchWeiboSort,
                            onWeiboSortChange = { sort ->
                                searchWeiboSort = sort
                                searchSettingsStore.writeWeiboSort(sort.storageValue())
                            },
                        )
                }

                val visitedProfileNavTarget = visitedUserId.takeIf { profileLayerActive }
                if (visitedProfileNavTarget != null && visitedProfileVisible) {
                    LaunchedEffect(visitedProfileLoadGeneration) {
                        if (visitedListScrollResetGeneration == visitedProfileLoadGeneration) return@LaunchedEffect
                        visitedListScrollResetGeneration = visitedProfileLoadGeneration
                        runCatching {
                            visitedPostsListState.scrollToItem(0)
                            visitedAlbumListState.scrollToItem(0)
                        }
                    }
                }
                NavAnimatedOverlay(
                    target = visitedProfileNavTarget,
                    modifier = Modifier
                        .fillMaxSize()
                        .blockHiddenTouches(visitedProfileVisible),
                    stackTop = visitedProfileVisible,
                    layerBaseZIndex = 540f,
                    visible = visitedProfileVisible,
                    animationKey = visitedProfileLoadGeneration,
                    navKind = visitedProfileNavTarget?.let { NavOverlayKind.VisitedProfile(it) },
                    pendingEnterKind = navEnterPendingKind,
                    pendingExitKind = navExitPendingKind,
                    onClearPendingEnter = { navEnterPendingKind = null },
                    onClearPendingExit = ::clearNavExitPendingKind,
                ) { uid ->
                    key(uid) {
                        Box(Modifier.fillMaxSize()) {
                            BackHandler(
                                enabled = visitedProfileVisible,
                                onBack = ::closeVisitedProfile,
                            )
                            VisitedUserProfileContent(
                                    session = session,
                                    profile = visitedProfile,
                                    profileHeaderHeight = profileHeaderHeights[uid] ?: 0.dp,
                                    onProfileHeaderHeightChange = { height ->
                                        profileHeaderHeights[uid] = height
                                    },
                                    isLoading = visitedProfileLoading,
                                    hasLoginCookie = hasLoginCookie,
                                    posts = visitedPosts,
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
                                    onAlbumTabSelected = ::ensureVisitedAlbumLoaded,
                                    onRefresh = {
                                        when {
                                            visitedProfile != null ->
                                                loadVisitedUserProfile(ProfileLookup.Uid(visitedProfile!!.id), keepContent = true)
                                            uid.all(Char::isDigit) ->
                                                loadVisitedUserProfile(ProfileLookup.Uid(uid), keepContent = true)
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
                                    onRetweetClick = { retweeted, host -> openDetail(retweeted, host) },
                                    onCommentLongClick = ::openCommentComposer,
                                    onOpenAlbumViewer = ::pushAlbumViewer,
                                    onMediaClick = ::pushMediaPreview,
                                    onUserClick = ::openUser,
                                    isLongTextLoading = { it.statusId in longTextLoadingIds },
                                    onLoadLongText = ::loadLongText,
                                    onToggleLike = ::toggleStatusLike,
                                    onLikeClick = ::openLikeUsers,
                                    onUrlEntityClick = ::openUrlEntity,
                                    onOpenFollowList = ::openFollowList,
                                    showFollowActions = hasLoginCookie &&
                                        visitedProfile?.id?.isNotBlank() == true &&
                                        visitedProfile?.id != mineProfile?.id,
                                    followLoading = visitedFollowLoading,
                                    onFollowClick = { toggleVisitedFollow() },
                                )
                        }
                    }
                }

                NavAnimatedOverlay(
                    target = MainTab.Mine.takeIf {
                        visitedUserId == null && selectedTab == MainTab.Mine && (mineUiOnTop || keepMineAlive)
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .blockHiddenTouches(mineUiOnTop),
                    stackTop = mineUiOnTop,
                    visible = mineUiOnTop,
                    stackAnimated = false,
                ) {
                    LaunchedEffect(mineProfile, mineProfileLoading) {
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
                                onAlbumTabSelected = { loadMineAlbumFirstPage() },
                                onRefresh = { refreshMineProfile() },
                                onLoadMorePosts = { loadMoreMinePosts() },
                                onLoadMoreAlbum = { loadMoreMineAlbum() },
                                onSyncEmoticons = { syncEmoticons() },
                                onItemClick = ::openDetail,
                                onRetweetClick = { retweeted, host -> openDetail(retweeted, host) },
                                onCommentLongClick = ::openCommentComposer,
                                onOpenAlbumViewer = ::pushAlbumViewer,
                                onMediaClick = ::pushMediaPreview,
                                onUserClick = ::openUser,
                                isLongTextLoading = { it.statusId in longTextLoadingIds },
                                onLoadLongText = ::loadLongText,
                                onToggleLike = ::toggleStatusLike,
                                onLikeClick = ::openLikeUsers,
                                onUrlEntityClick = ::openUrlEntity,
                                onOpenFollowList = { uid, screenName, avatarUrl, description, tab ->
                                    openFollowList(uid, screenName, avatarUrl, description, tab)
                                },
                                storedAccounts = storedAccounts,
                                activeAccountId = activeAccountId,
                                onSwitchAccount = ::switchStoredAccount,
                                onDeleteAccount = ::deleteStoredAccount,
                                onPrepareAddAccount = { session.prepareAddAccount() },
                                onPersistLoginSession = { persistLoginSession(makeActive = true) },
                                onReturnToFeed = {
                                    selectedTab = MainTab.Feed
                                    scope.launch {
                                        session.resumeAfterAccountLogin()
                                        persistLoginSession(makeActive = true)
                                        reloadStoredAccounts()
                                        session.openWeiboHome()
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
                                feedThumbnailQuality = feedThumbnailQuality,
                                onFeedThumbnailQualityChange = { quality ->
                                    feedThumbnailQuality = quality
                                    imageSettingsStore.writeThumbnailQuality(quality)
                                },
                                feedLineSpacing = feedLineSpacing,
                                onFeedLineSpacingChange = { spacing ->
                                    feedLineSpacing = spacing
                                    typographySettingsStore.writeLineSpacing(spacing)
                                },
                                feedFontSize = feedFontSize,
                                onFeedFontSizeChange = { size ->
                                    feedFontSize = size
                                    typographySettingsStore.writeFontSize(size)
                                },
                                feedPreviewItemProvider = {
                                    items.firstOrNull()?.let(::resolveFeedItem)
                                },
                                selectedThemeColor = selectedThemeColor,
                                onThemeColorChange = { color ->
                                    selectedThemeColor = color
                                    themeSettingsStore.writeThemeColor(color.storageValue)
                                },
                                appearanceMode = appearanceMode,
                                onAppearanceModeChange = { mode ->
                                    appearanceMode = mode
                                    themeSettingsStore.writeAppearanceMode(mode)
                                },
                            )
                }

                NavAnimatedOverlay(
                    target = detailOverlayItem.takeIf { detailLayerActive },
                    modifier = Modifier
                        .fillMaxSize()
                        .blockHiddenTouches(detailOverlayVisible),
                    stackTop = detailOverlayVisible,
                    layerBaseZIndex = 570f,
                    visible = detailOverlayVisible,
                    animationKey = activeDetailInstanceKey,
                    navKind = detailNavKind,
                    pendingEnterKind = navEnterPendingKind,
                    pendingExitKind = navExitPendingKind,
                    onClearPendingEnter = { navEnterPendingKind = null },
                    onClearPendingExit = ::clearNavExitPendingKind,
                ) { detailItem ->
                    key(detailItem.id, activeDetailInstanceKey) {
                        val detailListState = rememberLazyListState()
                        LaunchedEffect(detailListState) {
                            snapshotFlow {
                                ScrollRestore(
                                    detailListState.firstVisibleItemIndex,
                                    detailListState.firstVisibleItemScrollOffset,
                                )
                            }.collect { scroll ->
                                lastDetailScroll = scroll
                            }
                        }
                        LaunchedEffect(
                            detailItem.id,
                            activeDetailInstanceKey,
                            detailScrollRestoreToken,
                        ) {
                            val pending = detailScrollPending ?: return@LaunchedEffect
                            if (pending.first != detailItem.id) return@LaunchedEffect
                            val target = pending.second
                            var attempts = 0
                            while (attempts < 48) {
                                val total = detailListState.layoutInfo.totalItemsCount
                                if (total > 0 && target.index < total) break
                                delay(16)
                                attempts++
                            }
                            runCatching {
                                detailListState.scrollToItem(target.index, target.offset)
                            }
                            detailScrollPending = null
                        }
                        BackHandler(
                            enabled = detailOverlayVisible,
                            onBack = { closeDetail() },
                        )
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            color = MaterialTheme.colorScheme.background,
                        ) {
                            CompositionLocalProvider(LocalDetailInlineVideoPlayback provides true) {
                            DetailScreen(
                                item = detailItem,
                                contentSection = detailContentSection,
                                comments = comments,
                                reposts = reposts,
                                commentSort = commentSort,
                                isLoadingComments = commentsLoading,
                                isLoadingReposts = repostsLoading,
                                isLoadingMoreComments = commentsLoadingMore,
                                isLoadingMoreReposts = repostsLoadingMore,
                                isLongTextLoading = { it.statusId in longTextLoadingIds },
                                onLoadLongText = ::loadLongText,
                                onToggleLike = ::toggleStatusLike,
                                onLikeClick = ::openLikeUsers,
                                onBack = { closeDetail() },
                                onRefresh = { reloadDetailContent() },
                                onCommentSortChange = ::changeCommentSort,
                                onLoadMoreComments = { loadMoreComments() },
                                onLoadMoreReposts = { loadMoreReposts() },
                                commentsHasMore = commentsHasMore,
                                repostsHasMore = repostsHasMore,
                                onSelectContentSection = ::selectDetailContentSection,
                                listState = detailListState,
                                onMediaClick = ::pushMediaPreview,
                                emoticonMap = emoticonMap,
                                onRetweetClick = { retweeted, host -> openDetail(retweeted, host) },
                                onUserClick = ::openUser,
                                onComposeComment = ::openCommentComposer,
                                onExpandNestedComments = ::loadNestedCommentsPage,
                                nestedCommentsLoadingIds = nestedCommentsLoadingIds,
                                onUrlEntityClick = ::openUrlEntity,
                            )
                            }
                        }
                    }
                }

                commentComposeTarget?.let { target ->
                    val activeAvatarUrl = storedAccounts
                        .firstOrNull { it.id == activeAccountId }
                        ?.avatarUrl
                    CommentComposerDialog(
                        target = target,
                        avatarUrl = mineProfile?.avatarUrl ?: activeAvatarUrl,
                        submitting = commentSubmitting,
                        emoticonMap = emoticonMap,
                        recentEmoticons = recentCommentEmoticons,
                        mentionAvatarSuggestions = mentionAvatarSuggestions,
                        mentionNameIndex = mentionNameIndex,
                        mentionSuggestionsLoading = mentionSuggestionsLoading,
                        onEmoticonUsed = { phrase ->
                            recentCommentEmoticons = emoticonCacheStore.touchRecent(phrase).filter { it in emoticonMap }
                        },
                        onDismiss = {
                            if (commentSubmitting) {
                                commentSubmitJob?.cancel()
                                commentSubmitJob = null
                                commentSubmitting = false
                            }
                            commentComposeTarget = null
                        },
                        onSubmit = { text, photoUris, alsoRepost -> submitComment(target, text, photoUris, alsoRepost) },
                    )
                }

                likeUsersOverlay?.let { overlay ->
                    val overlayItem = resolveFeedItem(overlay.item)
                    Box(Modifier.fillMaxSize().zIndex(580f)) {
                        LikeUsersOverlay(
                            item = overlayItem,
                            anchorBounds = overlay.anchorBounds,
                            loading = likeUsersLoading,
                            loadingMore = likeUsersLoadingMore,
                            hasMore = likeUsersNextPage != null,
                            users = likeUsers,
                            error = likeUsersError,
                            onDismiss = ::closeLikeUsers,
                            onLoadMore = ::loadMoreLikeUsers,
                            onToggleLike = { toggleStatusLike(overlayItem) },
                            onUserClick = { uid ->
                                closeLikeUsers()
                                openUser(uid)
                            },
                        )
                    }
                }
            }

            HiddenSessionWebView(session)
            }

            if (searchBarOverlay.active) {
                val suggestionReserve = if (searchBarOverlay.suggestionsVisible) {
                    SearchSuggestionPanelMaxHeight + SearchBarCompanionGap
                } else {
                    0.dp
                }
                val searchFieldHeight = 44.dp
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .height(searchBarOverlay.bottomPadding + searchFieldHeight + suggestionReserve)
                        .zIndex(84f),
                )
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .zIndex(85f)
                        .padding(
                            start = 18.dp,
                            end = 18.dp,
                            bottom = searchBarOverlay.bottomPadding,
                        ),
                ) {
                    SearchSuggestionPanel(
                        suggestions = searchBarOverlay.suggestions,
                        visible = searchBarOverlay.suggestionsVisible,
                        loading = searchBarOverlay.suggestionsLoading,
                        onSuggestionClick = searchBarOverlay.onSuggestionClick,
                        onUserClick = searchBarOverlay.onSuggestionUserClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = SearchBarCompanionGap),
                    )
                    SearchCapsuleField(
                        value = searchBarOverlay.queryInput,
                        onValueChange = searchBarOverlay.onQueryInputChange,
                        mode = searchBarOverlay.mode,
                        onModeChange = searchBarOverlay.onModeChange,
                        onSearch = searchBarOverlay.onSearch,
                        onClear = searchBarOverlay.onClear,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            val feedCapsuleHint = if (
                selectedTab == MainTab.Feed &&
                selectedItem == null &&
                visitedUserId == null &&
                feedRefreshHint != null
            ) {
                feedRefreshHint
            } else {
                null
            }
            val saveHintState = imageSaveHintController.activeHint
            val capsuleHintState = saveHintState?.let { null }
                ?: operationCapsuleHint?.let {
                    AppCapsuleHintState(message = it)
                }
                ?: feedCapsuleHint?.let {
                    AppCapsuleHintState(message = it)
                }
            val feedRefreshTopInset = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
            AnimatedVisibility(
                visible = capsuleHintState != null,
                enter = fadeIn(tween(220)) + slideInVertically(tween(220)) { fullHeight -> -fullHeight / 2 },
                exit = fadeOut(tween(180)) + slideOutVertically(tween(180)) { fullHeight -> -fullHeight / 2 },
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = feedRefreshTopInset + 10.dp)
                    .zIndex(90f),
            ) {
                capsuleHintState?.let { hint ->
                    FeedRefreshCapsuleHint(
                        message = hint.message,
                        tone = hint.tone,
                        autoDismissMillis = hint.autoDismissMillis,
                        onDismiss = {
                            when {
                                operationCapsuleHint != null -> operationCapsuleHint = null
                                else -> feedRefreshHint = null
                            }
                        },
                    )
                }
            }

            val detailExiting = navExitPendingKind is NavOverlayKind.Detail
            val bottomBarVisible = visitedUserId == null &&
                (selectedItem == null || detailExiting)
            if (selectedItem == null && visitedUserId == null) {
                if (timelineMenuExpanded) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .zIndex(40f)
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() },
                                onClick = { timelineMenuExpanded = false },
                            ),
                    )
                }
                if (bottomBarExpanded && bottomBarAwaitingOutsideDismiss) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .zIndex(40f)
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() },
                                onClick = {
                                    bottomBarExpanded = false
                                    bottomBarAwaitingOutsideDismiss = false
                                },
                            ),
                    )
                }
            }
            val timelineMenuLabels = remember {
                listOf(TimelineKind.Following.label, TimelineKind.FriendsCircle.label)
            }
            val timelineMenuWidth = rememberActionMenuWidth(timelineMenuLabels)
            if (bottomBarVisible) {
                WeiboLiquidBottomBar(
                    selectedTab = selectedTab,
                    expanded = bottomBarExpanded,
                    backdrop = bottomBarBackdrop,
                    timelineMenuExpanded = timelineMenuExpanded,
                    onTimelineMenuExpandedChange = { timelineMenuExpanded = it },
                    onExpandRequest = {
                        if (!bottomBarExpanded) {
                            bottomBarAwaitingOutsideDismiss = true
                        }
                        bottomBarExpanded = true
                    },
                    onCollapsedTap = {
                        when (selectedTab) {
                            MainTab.Feed -> refreshTimelineFromTop()
                            MainTab.Mine -> {
                                scope.launch {
                                    if (minePagerPage == 0) {
                                        minePostsListState.animateScrollToTopFixed()
                                    } else {
                                        mineAlbumListState.animateScrollToTopFixed()
                                    }
                                }
                                refreshMineProfile()
                            }
                            else -> Unit
                        }
                    },
                    feedTabLabel = timelineKind.label,
                    selectedTimelineKind = timelineKind,
                    onTimelineKindChange = { kind ->
                        dismissFollowListForTabSwitch()
                        selectedTab = MainTab.Feed
                        switchTimelineKind(kind)
                    },
                    onTabChange = { tab ->
                        if (tab != selectedTab) {
                            dismissFollowListForTabSwitch()
                            if (tab == MainTab.Feed && visitedUserId != null) {
                                clearVisitedProfileState()
                            }
                        }
                        if (tab == MainTab.Feed && selectedTab == MainTab.Feed) {
                            refreshTimelineFromTop()
                        } else {
                            if (tab == MainTab.Feed) {
                                hasLoginCookie = session.hasLoginCookie()
                            }
                            selectedTab = tab
                        }
                    },
                    timelineMenuContent = { dismiss ->
                        ImageActionFrostedCard(
                            modifier = Modifier.width(timelineMenuWidth),
                            menuHeight = ActionMenuTwoRowHeight,
                        ) {
                            ImageActionRow(
                                label = TimelineKind.Following.label,
                                enabled = true,
                                selected = timelineKind == TimelineKind.Following,
                                onClick = {
                                    dismiss()
                                    dismissFollowListForTabSwitch()
                                    selectedTab = MainTab.Feed
                                    switchTimelineKind(TimelineKind.Following)
                                },
                            )
                            ImageActionRow(
                                label = TimelineKind.FriendsCircle.label,
                                enabled = true,
                                selected = timelineKind == TimelineKind.FriendsCircle,
                                onClick = {
                                    dismiss()
                                    dismissFollowListForTabSwitch()
                                    selectedTab = MainTab.Feed
                                    switchTimelineKind(TimelineKind.FriendsCircle)
                                },
                            )
                        }
                    },
                    timelineMenuWidth = timelineMenuWidth,
                    timelineMenuHeight = ActionMenuTwoRowHeight,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .zIndex(91f),
                )
            }

            ExitConfirmCapsule(
                visible = exitHintVisible,
                token = exitHintToken,
                onDismiss = { exitHintVisible = false },
                modifier = Modifier.align(Alignment.Center),
            )
            FeedCardActionMenuOverlay(
                controller = feedCardActionMenuController,
                backdrop = bottomBarBackdrop,
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
                    resolveAnchorBounds = request.resolveAnchorBounds,
                    statusItem = request.statusItem,
                    isFloating = imagePeekController.isFloating,
                    dismissReason = imagePeekController.pendingDismiss,
                    onRequestCancel = { imagePeekController.cancel() },
                    onDismissComplete = { imagePeekController.completeDismiss() },
                    onOpenFullscreenBehind = { request.onOpenFullscreenBehind(it) },
                    onEnterFullscreenHandoffComplete = { imagePeekController.completeEnterFullscreenHandoff() },
                )
            }
            }
            val overlayTop = overlayTopKind(navOverlayStack)
            val detailOverlayItem = selectedItem?.let(::resolveFeedItem)
            val detailLayerActive = navOverlayStack.any { it is NavOverlayKind.Detail } && detailOverlayItem != null
            val albumLayerActive = navOverlayStack.any { it == NavOverlayKind.AlbumViewer } && albumViewerState != null
            val albumViewerVisible = overlayTop == OverlayTop.AlbumViewer && albumLayerActive
            val mediaLayerActive = navOverlayStack.any { it == NavOverlayKind.MediaPreview } && mediaPreview != null
            val mediaPreviewVisible = overlayTop == OverlayTop.MediaPreview && mediaLayerActive
            val articleLayerActive = navOverlayStack.any { it == NavOverlayKind.Article } && articleOverlay != null
            val articleOverlayVisible = overlayTop == OverlayTop.Article && articleLayerActive
            NavAnimatedOverlay(
                target = mediaPreview.takeIf { mediaLayerActive },
                modifier = Modifier
                    .fillMaxSize()
                    .blockHiddenTouches(mediaPreviewVisible)
                    .background(Color.Black),
                stackTop = mediaPreviewVisible,
                layerBaseZIndex = 500f,
                visible = mediaPreviewVisible,
                animationKey = mediaOverlayInstanceKey,
                navKind = NavOverlayKind.MediaPreview,
                pendingEnterKind = navEnterPendingKind,
                pendingExitKind = navExitPendingKind,
                onClearPendingEnter = { navEnterPendingKind = null },
                onClearPendingExit = ::clearNavExitPendingKind,
            ) { request ->
                FullscreenMediaPreview(
                    media = request.media,
                    playbackOwnerId = request.playbackOwnerId,
                    onDismiss = { navigateBack() },
                    onEnterFloatingPlayback = {
                        openFloatingPlaybackFromFullscreen(request.media, request.playbackOwnerId)
                    },
                )
            }
            videoPeekController.activeRequest?.let { request ->
                VideoPeekOverlay(
                    modifier = Modifier.zIndex(
                        when {
                            // 须高于详情层（约 590），否则详情内滚动浮窗会被挡住 → 只听声音看不见画面
                            videoPeekController.isFullscreenMode -> 600f
                            videoPeekController.isFloating -> 605f
                            else -> 565f
                        },
                    ),
                    media = request.media,
                    playbackOwnerId = request.playbackOwnerId,
                    anchorBounds = request.anchorBounds,
                    pressOffset = request.pressOffset,
                    resolveAnchorBounds = request.resolveAnchorBounds,
                    expandFromAnchor = request.expandFromAnchor,
                    fromFullscreen = request.fromFullscreen,
                    dockImmediately = request.dockImmediately,
                    isFloating = videoPeekController.isFloating,
                    isFullscreenMode = videoPeekController.isFullscreenMode,
                    dismissReason = videoPeekController.pendingDismiss,
                    onRequestCancel = { videoPeekController.cancel() },
                    onDismissComplete = { videoPeekController.completeDismiss() },
                    onPlaybackEnded = { videoPeekController.dismissForPlaybackEnded() },
                    onEnterFullscreenHandoffComplete = { request.onEnterFullscreenHandoffComplete() },
                )
            }
            NavAnimatedOverlay(
                target = albumViewerState.takeIf { albumLayerActive },
                modifier = Modifier
                    .fillMaxSize()
                    .blockHiddenTouches(albumViewerVisible),
                stackTop = albumViewerVisible,
                layerBaseZIndex = 560f,
                visible = albumViewerVisible,
                animationKey = albumOverlayInstanceKey,
                navKind = NavOverlayKind.AlbumViewer,
                pendingEnterKind = navEnterPendingKind,
                pendingExitKind = navExitPendingKind,
                onClearPendingEnter = { navEnterPendingKind = null },
                onClearPendingExit = ::clearNavExitPendingKind,
            ) { viewer ->
                val relatedPosts = when {
                    visitedUserId != null -> visitedPosts
                    selectedTab == MainTab.Mine -> minePosts
                    else -> emptyList()
                }
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
            NavAnimatedOverlay(
                target = articleOverlay.takeIf { articleLayerActive },
                modifier = Modifier
                    .fillMaxSize()
                    .blockHiddenTouches(articleOverlayVisible),
                stackTop = articleOverlayVisible,
                layerBaseZIndex = 600f,
                visible = articleOverlayVisible,
                animationKey = articleOverlayInstanceKey,
                navKind = NavOverlayKind.Article,
                pendingEnterKind = navEnterPendingKind,
                pendingExitKind = navExitPendingKind,
                onClearPendingEnter = { navEnterPendingKind = null },
                onClearPendingExit = ::clearNavExitPendingKind,
            ) { overlay ->
                ArticleReaderOverlay(
                    state = overlay,
                    onBack = ::closeArticleOverlay,
                    onRetry = { loadArticleIntoOverlay(overlay.entity) },
                )
            }
            NavTransitionTouchShield(
                coordinator = navTransitionCoordinator,
                modifier = Modifier.zIndex(595f),
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(650f),
            ) {
                ImageSaveHintOverlay(
                    modifier = Modifier.align(Alignment.TopCenter),
                )
            }
        }
    }
    }
    }
    }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MyWeiboScaffold(
    snackbarHostState: SnackbarHostState,
    content: @Composable (PaddingValues) -> Unit,
) {
    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        content = content,
    )
}

@Composable
private fun OpaqueHintCapsule(
    modifier: Modifier = Modifier,
    tone: CapsuleHintTone = CapsuleHintTone.Neutral,
    content: @Composable () -> Unit,
) {
    val cornerRadius = 22.dp
    val tint = when (tone) {
        CapsuleHintTone.Success -> HintCapsuleSuccessBg
        CapsuleHintTone.Progress -> HintCapsuleProgressBg
        CapsuleHintTone.Neutral -> Color.Unspecified
    }
    SurfaceLiquidCapsule(
        modifier = modifier,
        cornerRadius = cornerRadius,
        useMenuGlassStyle = tone == CapsuleHintTone.Neutral,
        tint = tint,
    ) {
        Box(
            modifier = Modifier.wrapContentSize(),
            contentAlignment = Alignment.Center,
            content = { content() },
        )
    }
}

@Composable
private fun ImageSaveHintOverlay(
    modifier: Modifier = Modifier,
    zIndex: Float = 1000f,
) {
    val saveHintController = LocalImageSaveHint.current
    val hint = saveHintController.activeHint
    val topInset = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    AnimatedVisibility(
        visible = hint != null,
        enter = fadeIn(tween(220)) + slideInVertically(tween(220)) { fullHeight -> -fullHeight / 2 },
        exit = fadeOut(tween(180)) + slideOutVertically(tween(180)) { fullHeight -> -fullHeight / 2 },
        modifier = modifier
            .zIndex(zIndex)
            .padding(top = topInset + 10.dp),
    ) {
        hint?.let {
            FeedRefreshCapsuleHint(
                message = it.message,
                tone = it.tone,
                autoDismissMillis = it.autoDismissMillis,
                progress = it.progress,
                onDismiss = { saveHintController.clear() },
            )
        }
    }
}

@Composable
private fun FeedRefreshCapsuleHint(
    message: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    tone: CapsuleHintTone = CapsuleHintTone.Neutral,
    autoDismissMillis: Long? = 2200L,
    progress: Float? = null,
) {
    LaunchedEffect(message, autoDismissMillis) {
        autoDismissMillis?.let { millis ->
            delay(millis)
            onDismiss()
        }
    }

    val textColor = when (tone) {
        CapsuleHintTone.Success -> HintCapsuleSuccessText
        CapsuleHintTone.Progress -> HintCapsuleProgressText
        CapsuleHintTone.Neutral -> hintCapsuleTextColor()
    }
    OpaqueHintCapsule(modifier = modifier, tone = tone) {
        Column(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = textColor,
            )
            if (tone == CapsuleHintTone.Progress) {
                Spacer(modifier = Modifier.height(8.dp))
                val trackColor = HintCapsuleProgressText.copy(alpha = 0.18f)
                HintProgressBar(
                    progress = progress,
                    modifier = Modifier
                        .width(132.dp)
                        .height(3.dp),
                    color = HintCapsuleProgressText,
                    trackColor = trackColor,
                )
            }
        }
    }
}

@Composable
private fun HintProgressBar(
    progress: Float?,
    modifier: Modifier = Modifier,
    color: Color,
    trackColor: Color,
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress?.coerceIn(0f, 1f) ?: 0.35f,
        animationSpec = tween(durationMillis = 220),
        label = "hint-progress",
    )
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(999.dp))
            .background(trackColor),
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(animatedProgress.coerceIn(0.08f, 1f))
                .clip(RoundedCornerShape(999.dp))
                .background(color),
        )
    }
}

@Composable
private fun AppLoadingIndicator(
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    strokeWidth: Dp = 4.dp,
) {
    CircularProgressIndicator(
        modifier = modifier.size(size),
        color = FeedRefreshIndicatorColor,
        strokeWidth = strokeWidth,
    )
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
                color = hintCapsuleTextColor(),
            )
        }
    }
}

@Composable
private fun Modifier.consumeTouchEvents(): Modifier = clickable(
    indication = null,
    interactionSource = remember { MutableInteractionSource() },
    onClick = {},
)

private fun Modifier.blockHiddenTouches(visible: Boolean): Modifier =
    if (visible) {
        this
    } else {
        then(
            Modifier.pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        event.changes.forEach { it.consume() }
                    }
                }
            },
        )
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
                containerColor = MaterialTheme.colorScheme.surface,
                color = FeedRefreshIndicatorColor,
            )
        },
        content = content,
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
    onRetweetClick: (FeedItem, FeedItem) -> Unit = { item, _ -> onItemClick(item, null) },
    onCommentClick: (FeedItem) -> Unit,
    onCommentLongClick: (FeedItem) -> Unit,
    onRepostClick: (FeedItem) -> Unit,
    onMediaClick: (FeedMedia, String) -> Unit,
    resolveFeedItem: (FeedItem) -> FeedItem = { it },
    isLongTextLoading: (FeedItem) -> Boolean = { false },
    onLoadLongText: ((FeedItem) -> Unit)? = null,
    onToggleLike: ((FeedItem) -> Unit)? = null,
    onLikeClick: ((FeedItem, Rect) -> Unit)? = null,
    onUrlEntityClick: ((FeedUrlEntity) -> Unit)? = null,
    feedUiOnTop: Boolean = true,
) {
    val topInset = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    SideEffect {
        FeedEmoticonLinkDispatcher.onUserClick = onUserClick
        FeedEmoticonLinkDispatcher.onUrlEntityClick = onUrlEntityClick
    }

    if (!cacheLoaded) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator()
        }
        return
    }

    val feedContent: @Composable BoxScope.() -> Unit = {
        LaunchedEffect(listState) {
            snapshotFlow {
                val layoutInfo = listState.layoutInfo
                val totalItems = layoutInfo.totalItemsCount
                val lastVisibleItem = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                totalItems > 0 && lastVisibleItem >= totalItems - ListLoadMoreItemsFromBottom
            }
                .distinctUntilChanged()
                .filter { it }
                .collect { onLoadMore() }
        }

        LazyColumn(
            state = listState,
            flingBehavior = rememberWeiboListFlingBehavior(),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = topInset + 12.dp, bottom = 24.dp),
        ) {
            if (isLoading && items.isEmpty()) {
                item(key = "feed-loading") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator()
                    }
                }
            } else if (items.isEmpty()) {
                item(key = "feed-empty") {
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

            items(items, key = { it.id }, contentType = { "feed_card" }) { item ->
                val resolved = resolveFeedItem(item)
                FeedCard(
                    item = resolved,
                    onClick = { onItemClick(resolved, null) },
                    onMediaClick = onMediaClick,
                    emoticonMap = emoticonMap,
                    onUserClick = onUserClick,
                    onRetweetClick = { retweeted, host -> onRetweetClick(retweeted, host) },
                    isLongTextLoading = isLongTextLoading,
                    onLoadLongText = onLoadLongText,
                    onToggleLike = onToggleLike,
                    onLikeClick = onLikeClick,
                    onUrlEntityClick = onUrlEntityClick,
                    onCommentClick = { onCommentClick(resolved) },
                    onCommentLongClick = { onCommentLongClick(resolved) },
                    onRepostClick = { onRepostClick(resolved) },
                    menuBackEnabled = feedUiOnTop,
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

    AppPullToRefreshBox(
        isRefreshing = isLoading,
        onRefresh = onRefresh,
        modifier = Modifier.fillMaxSize(),
        content = feedContent,
    )
}

private fun resolveEmoticonMap(
    global: Map<String, String>,
    item: Map<String, String>,
): Map<String, String> = if (item.isEmpty()) global else global + item

private fun buildStatusTokenRegex(
    inlineImageLinks: Map<String, List<FeedImage>>,
    urlEntities: Map<String, FeedUrlEntity> = emptyMap(),
): Regex {
    val urlPattern = (inlineImageLinks.keys + urlEntities.keys)
        .sortedByDescending { it.length }
        .joinToString("|") { Regex.escape(it) }
    val base = """#[^#\n]+#|@[\p{L}\p{N}_\-.·\u00B7\u30FB]+|\[[^\[\]]+\]"""
    val pattern = if (urlPattern.isNotEmpty()) "$base|$urlPattern" else base
    return Regex(pattern)
}

@Composable
private fun feedBodyTextStyle(): TextStyle = LocalFeedBodyTextStyle.current

@Composable
private fun EmoticonText(
    text: String,
    emoticonMap: Map<String, String>,
    style: TextStyle,
    modifier: Modifier = Modifier,
    contentKey: String = text,
    onUserClick: ((String) -> Unit)? = null,
    leadingAuthorName: String? = null,
    onLeadingAuthorClick: (() -> Unit)? = null,
    trailingLabel: String? = null,
    trailingLocation: String? = null,
    onTrailingClick: (() -> Unit)? = null,
    onLocationClick: (() -> Unit)? = null,
    inlineImageLinks: Map<String, List<FeedImage>> = emptyMap(),
    onInlineImageClick: ((List<FeedImage>) -> Unit)? = null,
    urlEntities: Map<String, FeedUrlEntity> = emptyMap(),
    onUrlEntityClick: ((FeedUrlEntity) -> Unit)? = null,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
) {
    if (text.isBlank() && trailingLabel == null && leadingAuthorName == null && trailingLocation == null) {
        Text(text = "\u65E0\u6B63\u6587", style = style, modifier = modifier)
        return
    }

    if (!textNeedsRichRendering(text, leadingAuthorName, trailingLabel, trailingLocation, inlineImageLinks, urlEntities)) {
        Text(
            text = text,
            style = style,
            modifier = modifier,
            maxLines = maxLines,
            overflow = overflow,
        )
        return
    }

    val primaryColor = MaterialTheme.colorScheme.primary
    val sharedInlineContent = LocalFeedEmoticonInlineContent.current
    val feedTypographyMetrics = LocalFeedTypographyMetrics.current
    val linkScopeKey = remember(contentKey, leadingAuthorName, trailingLabel, trailingLocation) {
        buildString {
            append(contentKey)
            leadingAuthorName?.let { append("|la:$it") }
            trailingLabel?.let { append("|tr:$it") }
            trailingLocation?.let { append("|loc:$it") }
        }
    }
    val trailingClickState = rememberUpdatedState(onTrailingClick)
    val locationClickState = rememberUpdatedState(onLocationClick)
    val leadingAuthorClickState = rememberUpdatedState(onLeadingAuthorClick)
    val inlineImageClickState = rememberUpdatedState(onInlineImageClick)
    SideEffect {
        if (trailingClickState.value != null) {
            FeedEmoticonLinkDispatcher.registerTrailing(linkScopeKey) {
                trailingClickState.value?.invoke()
            }
        } else {
            FeedEmoticonLinkDispatcher.unregisterTrailing(linkScopeKey)
        }
        if (locationClickState.value != null) {
            FeedEmoticonLinkDispatcher.registerLocation(linkScopeKey) {
                locationClickState.value?.invoke()
            }
        } else {
            FeedEmoticonLinkDispatcher.unregisterLocation(linkScopeKey)
        }
        leadingAuthorName?.let { authorName ->
            if (leadingAuthorClickState.value != null) {
                FeedEmoticonLinkDispatcher.registerLeadingAuthor(authorName) {
                    leadingAuthorClickState.value?.invoke()
                }
            } else {
                FeedEmoticonLinkDispatcher.unregisterLeadingAuthor(authorName)
            }
        }
        inlineImageLinks.forEach { (token, images) ->
            if (inlineImageClickState.value != null) {
                FeedEmoticonLinkDispatcher.registerInlineImage(linkScopeKey, token, images) {
                    inlineImageClickState.value?.invoke(images)
                }
            }
        }
        if (urlEntities.isNotEmpty()) {
            FeedEmoticonLinkDispatcher.registerUrlEntities(linkScopeKey, urlEntities)
        }
    }
    DisposableEffect(linkScopeKey) {
        onDispose {
            FeedEmoticonLinkDispatcher.unregisterScope(linkScopeKey)
            leadingAuthorName?.let { FeedEmoticonLinkDispatcher.unregisterLeadingAuthor(it) }
        }
    }
    val tokenRegex = remember(inlineImageLinks, urlEntities) {
        buildStatusTokenRegex(inlineImageLinks, urlEntities)
    }
    val emoticonTokensInText: List<String> = remember(text, emoticonMap, tokenRegex) {
        tokenRegex.findAll(text)
            .map { it.value }
            .filter { emoticonMap.containsKey(it) }
            .distinct()
            .sorted()
            .toList()
    }
    val missingEmoticonTokens = remember(text, emoticonMap, sharedInlineContent, tokenRegex) {
        tokenRegex.findAll(text)
            .map { it.value }
            .filter { token -> emoticonMap.containsKey(token) && !sharedInlineContent.containsKey(token) }
            .distinct()
            .toList()
    }
    val missingInlineContent = rememberMissingEmoticonInlineContent(
        tokens = missingEmoticonTokens,
        emoticonMap = emoticonMap,
        metrics = feedTypographyMetrics,
    )
    val inlineContent = remember(sharedInlineContent, missingInlineContent, style.fontSize, primaryColor) {
        val withEmoticons = if (missingInlineContent.isEmpty()) {
            sharedInlineContent
        } else {
            sharedInlineContent + missingInlineContent
        }
        withEmoticons + (UrlLinkIconInlineContentKey to urlLinkIconInlineContent(style.fontSize, primaryColor))
    }
    val hasTrailingLink = trailingLabel != null && onTrailingClick != null
    val hasLocationLink = trailingLocation != null && onLocationClick != null
    val hasLeadingLink = leadingAuthorName != null && onLeadingAuthorClick != null
    val hasScopedInlineImageLinks = inlineImageLinks.isNotEmpty() && onInlineImageClick != null
    val hasScopedUrlLinks = urlEntities.isNotEmpty() && onUrlEntityClick != null
    val annotatedString = remember(
        text,
        leadingAuthorName,
        trailingLabel,
        trailingLocation,
        inlineImageLinks,
        urlEntities,
        emoticonTokensInText,
        linkScopeKey,
        hasTrailingLink,
        hasLocationLink,
        hasLeadingLink,
        hasScopedInlineImageLinks,
        hasScopedUrlLinks,
        tokenRegex,
        primaryColor,
        style.fontSize,
        style.lineHeight,
    ) {
        val cacheKey = buildFeedEmoticonTextCacheKey(
            text = text,
            leadingAuthorName = leadingAuthorName,
            trailingLabel = trailingLabel,
            trailingLocation = trailingLocation,
            inlineImageLinks = inlineImageLinks,
            urlEntities = urlEntities,
            emoticonTokens = emoticonTokensInText,
            linkScopeKey = linkScopeKey,
            hasTrailingLink = hasTrailingLink,
            hasLeadingLink = hasLeadingLink,
            hasScopedInlineImageLinks = hasScopedInlineImageLinks,
            hasScopedUrlLinks = hasScopedUrlLinks,
            hasLocationLink = hasLocationLink,
            style = style,
            primaryColor = primaryColor,
        )
        FeedEmoticonTextCache.get(cacheKey) ?: buildEmoticonAnnotatedString(
            text = text,
            emoticonMap = emoticonMap,
            inlineImageLinks = inlineImageLinks,
            urlEntities = urlEntities,
            tokenRegex = tokenRegex,
            primaryColor = primaryColor,
            leadingAuthorName = leadingAuthorName,
            trailingLabel = trailingLabel,
            trailingLocation = trailingLocation,
            linkScopeKey = linkScopeKey,
            hasLeadingLink = hasLeadingLink,
            hasTrailingLink = hasTrailingLink,
            hasLocationLink = hasLocationLink,
            hasScopedInlineImageLinks = hasScopedInlineImageLinks,
            hasScopedUrlLinks = hasScopedUrlLinks,
            style = style,
        ).also { FeedEmoticonTextCache.put(cacheKey, it) }
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

private fun buildEmoticonAnnotatedString(
    text: String,
    emoticonMap: Map<String, String>,
    inlineImageLinks: Map<String, List<FeedImage>>,
    urlEntities: Map<String, FeedUrlEntity>,
    tokenRegex: Regex,
    primaryColor: Color,
    leadingAuthorName: String?,
    trailingLabel: String?,
    trailingLocation: String?,
    linkScopeKey: String?,
    hasLeadingLink: Boolean,
    hasTrailingLink: Boolean,
    hasLocationLink: Boolean,
    hasScopedInlineImageLinks: Boolean,
    hasScopedUrlLinks: Boolean,
    style: TextStyle,
): AnnotatedString = buildAnnotatedString {
        leadingAuthorName?.let { authorName ->
            val authorLabel = "@$authorName\uFF1A"
            val authorStyle = SpanStyle(
                color = primaryColor,
                fontWeight = FontWeight.Medium,
                textDecoration = TextDecoration.None,
            )
            if (hasLeadingLink) {
                withLink(
                    LinkAnnotation.Clickable(
                        tag = "quoted-author:$authorName",
                        linkInteractionListener = { FeedEmoticonLinkDispatcher.leadingAuthorClick(authorName) },
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
        tokenRegex.findAll(text).forEach { match ->
            if (match.range.first > last) {
                append(text.substring(last, match.range.first))
            }
            val token = match.value
            when {
                inlineImageLinks.containsKey(token) &&
                    linkScopeKey != null &&
                    hasScopedInlineImageLinks -> {
                    val linkStyle = SpanStyle(
                        color = primaryColor,
                        fontWeight = FontWeight.Medium,
                        textDecoration = TextDecoration.None,
                    )
                    withLink(
                        LinkAnnotation.Clickable(
                            tag = "view-image:$token",
                            linkInteractionListener = {
                                FeedEmoticonLinkDispatcher.inlineImageClick(linkScopeKey, token)
                            },
                        ),
                    ) {
                        withStyle(linkStyle) {
                            append("\u67E5\u770B\u56FE\u7247")
                        }
                    }
                }
                inlineImageLinks.containsKey(token) -> {
                    withStyle(SpanStyle(color = primaryColor, fontWeight = FontWeight.Medium)) {
                        append("\u67E5\u770B\u56FE\u7247")
                    }
                }
                urlEntities.containsKey(token) &&
                    linkScopeKey != null &&
                    hasScopedUrlLinks -> {
                    val entity = urlEntities.getValue(token)
                    val linkStyle = SpanStyle(
                        color = primaryColor,
                        fontWeight = FontWeight.Medium,
                        textDecoration = TextDecoration.None,
                    )
                    withLink(
                        LinkAnnotation.Clickable(
                            tag = "url-entity:${entity.shortUrl}",
                            linkInteractionListener = {
                                FeedEmoticonLinkDispatcher.urlEntityClick(linkScopeKey, token)
                            },
                        ),
                    ) {
                        withStyle(linkStyle) {
                            appendInlineContent(UrlLinkIconInlineContentKey, "链接")
                            append(entity.title)
                        }
                    }
                }
                urlEntities.containsKey(token) -> {
                    val entity = urlEntities.getValue(token)
                    withStyle(SpanStyle(color = primaryColor, fontWeight = FontWeight.Medium)) {
                        appendInlineContent(UrlLinkIconInlineContentKey, "链接")
                        append(entity.title)
                    }
                }
                emoticonMap.containsKey(token) -> {
                    appendInlineContent(token, token)
                }
                token.startsWith("@") -> {
                    val screenName = token.removePrefix("@")
                    if (FeedEmoticonLinkDispatcher.onUserClick != null) {
                        withLink(
                            LinkAnnotation.Clickable(
                                tag = "mention:$screenName",
                                linkInteractionListener = {
                                    FeedEmoticonLinkDispatcher.onUserClick?.invoke(screenName)
                                },
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
                    val topic = token.removePrefix("#").removeSuffix("#")
                    if (FeedEmoticonLinkDispatcher.onTopicClick != null) {
                        withLink(
                            LinkAnnotation.Clickable(
                                tag = "topic:$topic",
                                linkInteractionListener = {
                                    FeedEmoticonLinkDispatcher.onTopicClick?.invoke(topic)
                                },
                            ),
                        ) {
                            withStyle(
                                SpanStyle(
                                    color = WeiboTopicBlue,
                                    fontWeight = FontWeight.Medium,
                                    textDecoration = TextDecoration.None,
                                ),
                            ) {
                                append(token)
                            }
                        }
                    } else {
                        withStyle(SpanStyle(color = WeiboTopicBlue)) {
                            append(token)
                        }
                    }
                }
                else -> append(token)
            }
            last = match.range.last + 1
        }
        if (last < text.length) {
            append(text.substring(last))
        }
        trailingLocation?.takeIf { it.isNotBlank() }?.let { location ->
            append('\u2009')
            val locationStyle = SpanStyle(
                color = primaryColor,
                fontWeight = FontWeight.Bold,
                textDecoration = TextDecoration.None,
            )
            if (linkScopeKey != null && hasLocationLink) {
                withLink(
                    LinkAnnotation.Clickable(
                        tag = "location:$linkScopeKey",
                        linkInteractionListener = {
                            FeedEmoticonLinkDispatcher.locationClick(linkScopeKey)
                        },
                    ),
                ) {
                    withStyle(locationStyle) {
                        appendInlineContent(UrlLinkIconInlineContentKey, "链接")
                        append('\u2009')
                        append(location.trim())
                    }
                }
            } else {
                withStyle(locationStyle) {
                    appendInlineContent(UrlLinkIconInlineContentKey, "链接")
                    append('\u2009')
                    append(location.trim())
                }
            }
        }
        trailingLabel?.let { label ->
            append('\u00A0')
            val trailingStyle = SpanStyle(
                color = if (linkScopeKey != null) primaryColor else primaryColor.copy(alpha = 0.6f),
                fontSize = style.fontSize,
                textDecoration = TextDecoration.None,
            )
            if (linkScopeKey != null && hasTrailingLink) {
                withLink(
                    LinkAnnotation.Clickable(
                        tag = "read-full-text:$linkScopeKey",
                        linkInteractionListener = { FeedEmoticonLinkDispatcher.trailingClick(linkScopeKey) },
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
    var bitmap by remember(url) {
        mutableStateOf(FeedBitmapCache.get(url)?.takeIfDrawable())
    }
    LaunchedEffect(url) {
        if (bitmap != null) return@LaunchedEffect
        FeedBitmapCache.get(url)?.takeIfDrawable()?.let {
            bitmap = it
            return@LaunchedEffect
        }
        runCatching {
            withContext(Dispatchers.IO) {
                decodeCachedRemoteBitmap(url, EmoticonBitmapMaxDecodeDim) ?: run {
                    val bytes = FeedImageLoadSemaphore.withPermit {
                        fetchRemoteBytes(
                            url = url,
                            connectTimeoutMs = 5000,
                            readTimeoutMs = 5000,
                            maxReadBytes = 2 * 1024 * 1024,
                        )
                    }
                    decodeBitmapFromBytes(bytes, EmoticonBitmapMaxDecodeDim)?.also { decoded ->
                        FeedBitmapCache.put(url, decoded)
                    }
                }
            }
        }.onSuccess { decoded ->
            if (decoded != null) {
                bitmap = decoded
            }
        }
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
    onMediaClick: (FeedMedia, String) -> Unit,
    emoticonMap: Map<String, String> = emptyMap(),
    onUserClick: ((String) -> Unit)? = null,
    onRetweetClick: ((FeedItem, FeedItem) -> Unit)? = null,
    isLongTextLoading: (FeedItem) -> Boolean = { false },
    onLoadLongText: ((FeedItem) -> Unit)? = null,
    onToggleLike: ((FeedItem) -> Unit)? = null,
    onLikeClick: ((FeedItem, Rect) -> Unit)? = null,
    onUrlEntityClick: ((FeedUrlEntity) -> Unit)? = null,
    autoFloatingOnScrollAway: Boolean = false,
    onCommentClick: (() -> Unit)? = null,
    onCommentLongClick: (() -> Unit)? = null,
    onRepostClick: (() -> Unit)? = null,
    showAuthorRow: Boolean = true,
    menuBackEnabled: Boolean = true,
    insetRounded: Boolean = false,
) {
    val displayItem = remember(item) { WeiboJsonParser.sanitizeRepostVideoLinks(item) }
    val resolvedEmoticonMap = remember(emoticonMap, displayItem.emoticons, displayItem.retweetedStatus?.emoticons) {
        resolveEmoticonMap(emoticonMap, displayItem.collectEmoticons())
    }
    val urlEntityMap = remember(displayItem.urlEntities) {
        displayItem.urlEntities.associateBy { entity -> entity.shortUrl }
    }
    var inlineImagePreview by remember(displayItem.statusId) { mutableStateOf<List<FeedImage>?>(null) }
    val cardContainerColor = MaterialTheme.colorScheme.surface
    val contentVerticalPadding = when {
        insetRounded -> 14.dp
        showAuthorRow -> 10.dp
        else -> 0.dp
    }
    val cardBody: @Composable () -> Unit = {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    top = contentVerticalPadding,
                    bottom = if (insetRounded) 14.dp else 10.dp,
                ),
            verticalArrangement = Arrangement.spacedBy(FeedCardSectionSpacing),
        ) {
            if (showAuthorRow) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = FeedCardContentHorizontalPadding),
                    verticalAlignment = Alignment.Top,
                ) {
                    Box(
                        Modifier
                            .weight(1f)
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() },
                                onClick = onClick,
                            ),
                    ) {
                        AuthorRow(
                            item = item,
                            onUserClick = onUserClick,
                            avatarClickable = true,
                        )
                    }
                    if (menuBackEnabled) {
                        FeedCardActionMenu(
                            item = item,
                            backHandlerEnabled = menuBackEnabled,
                        )
                    }
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = FeedCardContentHorizontalPadding)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                        onClick = onClick,
                    ),
                verticalArrangement = Arrangement.spacedBy(FeedCardSectionSpacing),
            ) {
                StatusTextSection(
                    item = displayItem,
                    emoticonMap = resolvedEmoticonMap,
                    style = feedBodyTextStyle(),
                    onUserClick = onUserClick,
                    isLongTextLoading = isLongTextLoading(item),
                    onLoadLongText = onLoadLongText,
                    inlineImageLinks = displayItem.inlineImageLinks,
                    onInlineImageClick = { inlineImagePreview = it },
                    urlEntities = urlEntityMap,
                    onUrlEntityClick = onUrlEntityClick,
                )
            }
            displayItem.retweetedStatus?.let { retweeted ->
                QuotedStatus(
                    modifier = Modifier.padding(horizontal = FeedCardContentHorizontalPadding),
                    item = retweeted,
                    playbackOwnerId = retweeted.statusId.ifBlank { feedItemPlaybackOwnerId(item) },
                    onMediaClick = onMediaClick,
                    emoticonMap = emoticonMap,
                    onClick = onRetweetClick?.let { cb -> { cb(retweeted, item) } },
                    onUserClick = onUserClick,
                    isLongTextLoading = isLongTextLoading(retweeted),
                    onLoadLongText = onLoadLongText,
                    onUrlEntityClick = onUrlEntityClick,
                    autoFloatingOnScrollAway = autoFloatingOnScrollAway,
                )
            }
            MediaStrip(
                modifier = Modifier.padding(horizontal = FeedCardContentHorizontalPadding),
                images = displayItem.images,
                medias = displayItem.medias,
                playbackOwnerId = feedItemPlaybackOwnerId(item),
                onMediaClick = onMediaClick,
                onDetailClick = if (showAuthorRow) onClick else null,
                imageOwner = item,
                autoFloatingOnScrollAway = autoFloatingOnScrollAway,
            )
            StatusActions(
                modifier = Modifier.padding(horizontal = FeedCardContentHorizontalPadding),
                item = item,
                onRepostClick = onRepostClick ?: onClick,
                onCommentClick = onCommentClick ?: onClick,
                onCommentLongClick = onCommentLongClick,
                onLikeClick = onLikeClick?.let { open -> { bounds -> open(item, bounds) } },
                onToggleLike = onToggleLike?.let { toggle -> { toggle(item) } },
            )
        }
    }
    Box(
        modifier = Modifier.fillMaxWidth(),
    ) {
        if (insetRounded) {
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = FeedCardContentHorizontalPadding),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = cardContainerColor,
                ),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 0.dp),
            ) {
                cardBody()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = FeedCardItemSpacing)
                    .background(cardContainerColor),
            ) {
                cardBody()
            }
        }
        inlineImagePreview?.let { images ->
            FullscreenImageViewer(
                images = images,
                initialIndex = 0,
                onDismiss = { inlineImagePreview = null },
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
    inlineImageLinks: Map<String, List<FeedImage>> = emptyMap(),
    onInlineImageClick: ((List<FeedImage>) -> Unit)? = null,
    urlEntities: Map<String, FeedUrlEntity> = emptyMap(),
    onUrlEntityClick: ((FeedUrlEntity) -> Unit)? = null,
) {
    val context = LocalContext.current
    val locationOpenUrl = remember(item.locationName, item.locationUrl) {
        resolveFeedLocationOpenUrl(item.locationName, item.locationUrl)
    }
    EmoticonText(
        modifier = Modifier.fillMaxWidth(),
        text = item.text,
        contentKey = item.statusId.ifBlank { item.id },
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
        trailingLocation = item.locationName,
        onTrailingClick = if (item.isLongText && onLoadLongText != null && !isLongTextLoading) {
            { onLoadLongText(item) }
        } else {
            null
        },
        onLocationClick = locationOpenUrl?.let { url ->
            {
                WeiboStatusActions.openExternalUrl(
                    context = context,
                    rawUrl = url,
                    failureMessage = "无法打开地理位置",
                )
            }
        },
        inlineImageLinks = inlineImageLinks,
        onInlineImageClick = onInlineImageClick,
        urlEntities = urlEntities,
        onUrlEntityClick = onUrlEntityClick,
    )
}

private fun resolveFeedLocationOpenUrl(locationName: String?, locationUrl: String?): String? {
    locationUrl?.trim()?.takeIf { it.isNotBlank() }?.let { return it }
    val name = locationName?.trim()?.takeIf { it.isNotBlank() } ?: return null
    return "https://s.weibo.com/weibo?q=" + java.net.URLEncoder.encode(name, Charsets.UTF_8.name())
}

@Composable
private fun QuotedStatus(
    item: FeedItem,
    playbackOwnerId: String,
    onMediaClick: (FeedMedia, String) -> Unit,
    emoticonMap: Map<String, String> = emptyMap(),
    onClick: (() -> Unit)? = null,
    onUserClick: ((String) -> Unit)? = null,
    isLongTextLoading: Boolean = false,
    onLoadLongText: ((FeedItem) -> Unit)? = null,
    onUrlEntityClick: ((FeedUrlEntity) -> Unit)? = null,
    autoFloatingOnScrollAway: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val resolvedMap = resolveEmoticonMap(emoticonMap, item.collectEmoticons())
    val userTarget = item.authorId.takeIf { it.isNotBlank() } ?: item.authorName
    val quotedShape = RoundedCornerShape(8.dp)
    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape = quotedShape,
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.background,
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(FeedCardSectionSpacing),
        ) {
            Column(
                modifier = if (onClick != null) {
                    Modifier.clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                        onClick = onClick,
                    )
                } else {
                    Modifier
                },
                verticalArrangement = Arrangement.spacedBy(FeedCardSectionSpacing),
            ) {
                StatusTextSection(
                    item = item,
                    emoticonMap = resolvedMap,
                    style = feedBodyTextStyle(),
                    onUserClick = onUserClick,
                    isLongTextLoading = isLongTextLoading,
                    onLoadLongText = onLoadLongText,
                    leadingAuthorName = item.authorName,
                    onLeadingAuthorClick = if (userTarget.isNotBlank() && onUserClick != null) {
                        { onUserClick(userTarget) }
                    } else {
                        null
                    },
                    inlineImageLinks = item.inlineImageLinks,
                    onInlineImageClick = null,
                    urlEntities = item.urlEntities.associateBy { entity -> entity.shortUrl },
                    onUrlEntityClick = onUrlEntityClick,
                )
            }
            MediaStrip(
                images = item.images,
                medias = item.medias,
                playbackOwnerId = playbackOwnerId,
                onMediaClick = onMediaClick,
                onDetailClick = onClick,
                imageOwner = item,
                autoFloatingOnScrollAway = autoFloatingOnScrollAway,
            )
        }
    }
}

@Composable
private fun CommentSortLinesIcon(
    modifier: Modifier = Modifier,
    tint: Color,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        repeat(3) {
            Box(
                Modifier
                    .width(12.dp)
                    .height(1.5.dp)
                    .background(tint, RoundedCornerShape(1.dp)),
            )
        }
    }
}

@Composable
private fun CommentSortToggle(
    selected: CommentSort,
    onSelected: (CommentSort) -> Unit,
) {
    val metaColor = weiboMetaTextColor()
    val nextSort = if (selected == CommentSort.Time) CommentSort.Hot else CommentSort.Time

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = { onSelected(nextSort) },
            )
            .padding(horizontal = 4.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        CommentSortLinesIcon(tint = metaColor)
        Text(
            text = selected.label,
            style = MaterialTheme.typography.bodySmall,
            color = metaColor,
        )
    }
}

private fun FeedItem.actionMenuKey(): String = statusId.ifBlank { id }

@Composable
private fun FeedCardActionMenu(
    item: FeedItem,
    backHandlerEnabled: Boolean = true,
) {
    val controller = LocalFeedCardActionMenuController.current
    val menuKey = item.actionMenuKey()
    val anchorHolder = remember(menuKey) { LayoutAnchorHolder() }
    val isExpanded = controller.activeRequest?.item?.actionMenuKey() == menuKey

    LaunchedEffect(backHandlerEnabled, isExpanded) {
        if (!backHandlerEnabled && isExpanded) {
            controller.dismiss()
        }
    }

    Box(
        modifier = Modifier.size(32.dp),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(8.dp))
                .onGloballyPositioned { coordinates ->
                    anchorHolder.coordinates = coordinates
                }
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = {
                        if (isExpanded) {
                            controller.dismiss()
                        } else {
                            val bounds = anchorHolder.boundsInRoot() ?: return@clickable
                            controller.open(item, bounds, backHandlerEnabled)
                        }
                    },
                ),
            contentAlignment = Alignment.Center,
        ) {
            SettingsExpandIndicator(
                modifier = Modifier.size(18.dp),
                tint = weiboMetaTextColor(),
            )
        }
    }
}

@Composable
private fun FeedCardActionMenuOverlay(
    controller: FeedCardActionMenuController,
    backdrop: Backdrop,
) {
    val activeRequest = controller.activeRequest
    var displayedRequest by remember { mutableStateOf<FeedCardActionMenuRequest?>(null) }
    val menuVisible = controller.menuRevealVisible

    LaunchedEffect(activeRequest) {
        if (activeRequest != null) {
            displayedRequest = activeRequest
        }
    }

    val request = displayedRequest ?: return
    val context = LocalContext.current
    val density = LocalDensity.current
    val shareUrl = remember(request.item.id) {
        WeiboStatusActions.weiboUrl(request.item)
    }

    fun dismissMenu() {
        controller.dismiss()
    }

    LaunchedEffect(request.backHandlerEnabled) {
        if (!request.backHandlerEnabled) {
            dismissMenu()
        }
    }

    BackHandler(enabled = request.backHandlerEnabled && menuVisible) {
        dismissMenu()
    }

    val menuLabels = remember { listOf("\u8df3\u8f6c\u5230\u5fae\u535a", "\u5206\u4eab") }
    val menuHeight = ActionMenuTwoRowHeight
    val gapFromButton = 6.dp
    val screenMargin = 14.dp
    val anchor = request.anchorBoundsInRoot

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .zIndex(590f),
    ) {
        val screenWidthPx = with(density) { maxWidth.toPx() }
        val screenHeightPx = with(density) { maxHeight.toPx() }
        val menuWidth = rememberActionMenuWidth(menuLabels, maxWidth - screenMargin * 2)
        val menuWidthPx = with(density) { menuWidth.toPx() }
        val menuHeightPx = with(density) { menuHeight.toPx() }
        val menuPlacement = calculateFeedCardActionMenuOffsetPx(
            anchorBounds = anchor,
            screenWidthPx = screenWidthPx,
            screenHeightPx = screenHeightPx,
            menuWidthPx = menuWidthPx,
            menuHeightPx = menuHeightPx,
            marginPx = with(density) { screenMargin.toPx() },
            gapPx = with(density) { gapFromButton.toPx() },
        )
        val originInMenu = computeActionMenuOriginInMenu(
            anchorInRoot = anchor.center,
            menuOffset = menuPlacement.offset,
            menuWidthPx = menuWidthPx,
            menuHeightPx = menuHeightPx,
        )

        if (menuVisible) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                        onClick = { dismissMenu() },
                    ),
            )
        }
        ActionMenuReveal(
            visible = menuVisible,
            menuWidth = menuWidth,
            menuHeight = menuHeight,
            originInMenu = originInMenu,
            onExitComplete = { displayedRequest = null },
            modifier = Modifier
                .offset { menuPlacement.offset }
                .width(menuWidth)
                .height(menuHeight),
        ) {
            ImageActionFrostedCard(
                modifier = Modifier.fillMaxSize(),
                backdrop = backdrop,
            ) {
                ImageActionRow(
                    label = "跳转到微博",
                    enabled = shareUrl != null,
                    onClick = {
                        dismissMenu()
                        WeiboStatusActions.openInWeiboApp(context, request.item)
                    },
                )
                ImageActionRow(
                    label = "分享",
                    enabled = shareUrl != null,
                    onClick = {
                        dismissMenu()
                        WeiboStatusActions.shareLink(context, request.item)
                    },
                )
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

private data class SingleImageFeedLayout(
    val aspectRatio: Float,
    val widthFraction: Float,
)

private fun singleImageFeedLayout(
    width: Int,
    height: Int,
    maxWidthFraction: Float = SingleImageMaxWidthFraction,
    maxHeightToWidth: Float = SingleImageMaxHeightToWidth,
): SingleImageFeedLayout {
    val naturalAspect = if (width > 0 && height > 0) {
        width.toFloat() / height
    } else {
        1f
    }
    val aspectRatio = naturalAspect.coerceIn(0.05f, 3f)
    val maxHeightFraction = maxWidthFraction * maxHeightToWidth
    val heightAtMaxWidth = maxWidthFraction / aspectRatio
    val widthFraction = if (heightAtMaxWidth <= maxHeightFraction) {
        maxWidthFraction
    } else {
        maxHeightFraction * aspectRatio
    }
    return SingleImageFeedLayout(
        aspectRatio = aspectRatio,
        widthFraction = widthFraction,
    )
}

private fun feedVideoFeedLayout(
    media: FeedMedia,
    width: Int = media.coverWidth ?: 0,
    height: Int = media.coverHeight ?: 0,
): SingleImageFeedLayout {
    val resolvedWidth = width.takeIf { it > 0 } ?: when (media.videoOrientation?.lowercase()) {
        "vertical", "portrait" -> 9
        else -> 16
    }
    val resolvedHeight = height.takeIf { it > 0 } ?: when (media.videoOrientation?.lowercase()) {
        "vertical", "portrait" -> 16
        else -> 9
    }
    return singleImageFeedLayout(
        width = resolvedWidth,
        height = resolvedHeight,
        maxWidthFraction = VideoMaxWidthFraction,
        maxHeightToWidth = VideoMaxHeightToWidth,
    )
}

private fun feedVideoDisplayAspectRatio(media: FeedMedia): Float =
    feedVideoFeedLayout(media).aspectRatio

private fun isPortraitFeedVideo(aspectRatio: Float, media: FeedMedia): Boolean {
    when (media.videoOrientation?.lowercase()) {
        "vertical", "portrait" -> return true
        "horizontal", "landscape" -> return false
    }
    val coverWidth = media.coverWidth ?: 0
    val coverHeight = media.coverHeight ?: 0
    if (coverWidth > 0 && coverHeight > 0 && coverWidth != coverHeight) {
        return coverHeight > coverWidth
    }
    if (aspectRatio > 0f && abs(aspectRatio - 16f / 9f) > 0.02f) {
        return aspectRatio < 1f
    }
    return feedVideoDisplayAspectRatio(media) < 1f
}

@Composable
private fun FeedImageThumbnailContent(
    image: FeedImage,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    previewUrl: String? = null,
    maxDecodeDimOverride: Int? = null,
) {
    val thumbnailQuality = LocalFeedThumbnailQuality.current
    val upgradeRevision = LocalFeedImageUpgradeNotifier.current.revision
    val feedCacheCandidates = remember(image, thumbnailQuality) {
        feedImageCacheCandidates(image, thumbnailQuality)
    }
    val cachedFeedBitmap = remember(image.id, image.largeUrl, thumbnailQuality, upgradeRevision) {
        FeedBitmapCache.get(feedCacheCandidates)?.takeIfDrawable()
    }
    val displayUrl = remember(image, thumbnailQuality) { thumbnailQuality.displayUrl(image) }
    val fallbackUrl = remember(image, thumbnailQuality) { thumbnailQuality.fallbackUrl(image) }
    val decodeDim = maxDecodeDimOverride ?: thumbnailQuality.maxDecodeDim

    if (maxDecodeDimOverride == null && cachedFeedBitmap != null) {
        val imageBitmap = remember(cachedFeedBitmap) { cachedFeedBitmap.asImageBitmap() }
        Image(
            bitmap = imageBitmap,
            contentDescription = null,
            modifier = modifier,
            contentScale = contentScale,
        )
    } else if (maxDecodeDimOverride != null) {
        AlbumGridRemoteImage(
            image = image,
            maxDecodeDim = maxDecodeDimOverride,
            previewUrl = previewUrl,
            modifier = modifier,
            contentScale = contentScale,
        )
    } else {
        RemoteImage(
            url = previewUrl ?: displayUrl,
            fallbackUrls = if (previewUrl != null) {
                emptyList()
            } else {
                listOfNotNull(fallbackUrl)
            },
            cacheLookupUrls = feedCacheCandidates,
            maxDecodeDim = decodeDim,
            modifier = modifier,
            contentScale = contentScale,
            animated = image.isGif,
            limitConcurrency = true,
        )
    }
}

private enum class LivePhotoMirrorCorrection {
    None,
    MirrorVideo,
}

@Composable
private fun FeedImageCell(
    image: FeedImage,
    allImages: List<FeedImage>,
    imageIndex: Int = 0,
    imageOwner: FeedItem? = null,
    modifier: Modifier = Modifier,
    previewUrl: String? = null,
    contentScale: ContentScale = ContentScale.Crop,
    showLiveBadge: Boolean = true,
    cornerRadius: Dp = 4.dp,
    maxDecodeDimOverride: Int? = null,
    onOpenViewer: (Int, Rect?, (() -> Unit)?, (() -> Unit)?) -> Unit,
    onAnchorBoundsChanged: (Rect) -> Unit = {},
) {
    var actionOpen by remember(image.id) { mutableStateOf(false) }
    var peekActive by remember(image.id) { mutableStateOf(false) }
    var pressHoldProgress by remember(image.id) { mutableFloatStateOf(0f) }
    val anchorHolder = remember(image.id) { LayoutAnchorHolder() }
    val imagePeekController = LocalImagePeekController.current
    val feedListScrollCoordinator = LocalFeedListScrollCoordinator.current
    val gestureScope = rememberCoroutineScope()
    val mediaHaptics = rememberMediaPeekHaptics()
    val holdScale = mediaPeekHoldScale(if (actionOpen) 0f else pressHoldProgress)

    fun resetPeekState() {
        actionOpen = false
        peekActive = false
        pressHoldProgress = 0f
        imagePeekController.resetFingerDragOffset()
    }

    fun openImagePeek(pressWindowOffset: Offset) {
        val bounds = anchorHolder.boundsInWindow() ?: return
        actionOpen = true
        peekActive = true
        pressHoldProgress = 1f
        imagePeekController.open(
            ImagePeekRequest(
                image = image,
                allImages = allImages,
                imageIndex = imageIndex,
                anchorBounds = bounds,
                pressOffset = pressWindowOffset,
                statusItem = imageOwner,
                resolveAnchorBounds = {
                    anchorHolder.boundsInWindow() ?: bounds
                },
                onCancel = { resetPeekState() },
                onRelease = {},
                onOpenFullscreenBehind = { index ->
                    onOpenViewer(index, null, {
                        resetPeekState()
                        imagePeekController.finishFullscreenHandoff()
                    }, null)
                },
                onEnterFullscreenHandoffComplete = { peekActive = false },
            ),
        )
    }

    Box(
        modifier = modifier
            .zIndex(if (actionOpen || peekActive) 10f else 0f)
            .onGloballyPositioned { coordinates ->
                anchorHolder.coordinates = coordinates
            }
            .graphicsLayer {
                scaleX = holdScale
                scaleY = holdScale
                alpha = if (actionOpen) 0f else 1f
            }
            .clip(RoundedCornerShape(cornerRadius))
            .background(
                if (actionOpen) Color.Black else MaterialTheme.colorScheme.surfaceContainerHighest,
            )
            .pointerInput(image.id) {
                awaitEachGesture {
                    val bounds = anchorHolder.boundsInWindow() ?: return@awaitEachGesture
                    val down = awaitFirstDown(requireUnconsumed = false)
                    if (cancelFeedListScrollOnTap(feedListScrollCoordinator, gestureScope, down)) {
                        return@awaitEachGesture
                    }
                    var lastPosition = down.position
                    var pressResult = MediaLongPressResult.Tap
                    pressResult = awaitMediaLongPress(
                        down = down,
                        viewConfiguration = viewConfiguration,
                        onHoldProgress = { pressHoldProgress = it },
                        onHaptic = mediaHaptics::perform,
                    )

                    when (pressResult) {
                        MediaLongPressResult.Tap -> {
                            val currentBounds = anchorHolder.boundsInWindow() ?: bounds
                            onAnchorBoundsChanged(currentBounds)
                            onOpenViewer(imageIndex, currentBounds, null, null)
                        }
                        MediaLongPressResult.Cancelled -> Unit
                        MediaLongPressResult.LongPress -> Unit
                    }
                    if (pressResult != MediaLongPressResult.LongPress) {
                        return@awaitEachGesture
                    }

                    val pressWindowOffset = down.position.toWindowPosition(bounds)
                    down.consume()
                    openImagePeek(pressWindowOffset)
                    imagePeekController.updateFingerDragOffset(Offset.Zero)

                    val dragGestureThreshold = 82f
                    var cancelledByDrag = false
                    var fullscreenByDrag = false
                    val canSwipeImages = allImages.size > 1
                    while (true) {
                        val event = awaitPointerEvent(PointerEventPass.Initial)
                        val change = event.changes.firstOrNull { it.id == down.id }
                            ?: event.changes.firstOrNull()
                            ?: break
                        if (!change.pressed) {
                            break
                        }
                        lastPosition = change.position
                        val totalDrag = lastPosition - down.position
                        val horizontalDominant = abs(totalDrag.x) > abs(totalDrag.y) * 1.15f
                        val verticalDominant = abs(totalDrag.y) > abs(totalDrag.x) * 1.15f
                        if (canSwipeImages && horizontalDominant) {
                            val deltaX = change.position.x - change.previousPosition.x
                            imagePeekController.dispatchHorizontalDrag(deltaX)
                            change.consume()
                            continue
                        }
                        change.consume()
                        imagePeekController.updateFingerDragOffset(
                            change.position.toWindowPosition(bounds) - pressWindowOffset,
                        )
                        if (
                            totalDrag.y > dragGestureThreshold &&
                            verticalDominant
                        ) {
                            cancelledByDrag = true
                            imagePeekController.cancel()
                            while (true) {
                                val consumeEvent = awaitPointerEvent(PointerEventPass.Initial)
                                consumeEvent.changes.forEach { it.consume() }
                                if (consumeEvent.changes.all { !it.pressed }) break
                            }
                            break
                        }
                        if (
                            totalDrag.y < -dragGestureThreshold &&
                            verticalDominant
                        ) {
                            fullscreenByDrag = true
                            imagePeekController.enterFullscreen()
                            while (true) {
                                val consumeEvent = awaitPointerEvent(PointerEventPass.Initial)
                                consumeEvent.changes.forEach { it.consume() }
                                if (consumeEvent.changes.all { !it.pressed }) break
                            }
                            break
                        }
                    }

                    imagePeekController.resetFingerDragOffset()
                    if (!cancelledByDrag && !fullscreenByDrag) {
                        peekActive = false
                        imagePeekController.release()
                    }
                }
            },
    ) {
        FeedImageThumbnailContent(
            image = image,
            previewUrl = previewUrl,
            modifier = Modifier.fillMaxSize(),
            contentScale = contentScale,
            maxDecodeDimOverride = maxDecodeDimOverride,
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

private val ActionMenuMaxWidth = 220.dp
private val ActionMenuCornerRadius = 22.dp
private val ActionMenuBlurRadius = 16.dp
private val ActionMenuCardInset = 5.dp
private val ActionMenuItemGap = 3.dp
private val ActionMenuCapsuleHeight = 38.dp
private val ActionMenuCapsulePaddingHorizontal = 14.dp
private val ActionMenuTwoRowHeight =
    ActionMenuCardInset * 2 + ActionMenuCapsuleHeight * 2 + ActionMenuItemGap
private val ActionMenuThreeRowHeight =
    ActionMenuCardInset * 2 + ActionMenuCapsuleHeight * 3 + ActionMenuItemGap * 2

@Composable
private fun actionMenuTextStyle(selected: Boolean = false): TextStyle =
    MaterialTheme.typography.bodyMedium.copy(
        fontSize = 15.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.15.sp,
        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
        platformStyle = PlatformTextStyle(includeFontPadding = false),
    )

@Composable
private fun rememberActionMenuWidth(
    labels: List<String>,
    maxWidth: Dp = ActionMenuMaxWidth,
): Dp {
    val textMeasurer = rememberTextMeasurer()
    val style = actionMenuTextStyle()
    val density = LocalDensity.current
    val horizontalPadding = ActionMenuCapsulePaddingHorizontal * 2 + ActionMenuCardInset * 2

    return remember(labels, maxWidth, style, density) {
        val maxTextWidthPx = labels.maxOfOrNull { label ->
            textMeasurer.measure(
                text = label,
                style = style,
                maxLines = 1,
                overflow = TextOverflow.Clip,
            ).size.width
        } ?: 0
        val widthDp = with(density) {
            (maxTextWidthPx.toFloat() + horizontalPadding.toPx()).toDp()
        }
        minOf(widthDp, maxWidth)
    }
}

@Composable
private fun ImageActionFrostedCard(
    modifier: Modifier = Modifier,
    backdrop: Backdrop? = null,
    menuHeight: Dp? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    SurfaceLiquidMenuCard(
        modifier = modifier.then(
            if (menuHeight != null) Modifier.height(menuHeight) else Modifier,
        ),
        backdrop = backdrop ?: LocalLiquidMenuBackdrop.current,
        cornerRadius = ActionMenuCornerRadius,
        blurRadius = ActionMenuBlurRadius,
        surfaceColor = actionMenuSurfaceColor(),
        contentPadding = PaddingValues(ActionMenuCardInset),
        content = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(ActionMenuItemGap),
            ) {
                content()
            }
        },
    )
}

@Composable
private fun ImageActionOverlay(
    image: FeedImage,
    allImages: List<FeedImage>,
    initialImageIndex: Int,
    anchorBounds: Rect,
    pressOffset: Offset,
    resolveAnchorBounds: () -> Rect,
    statusItem: FeedItem? = null,
    isFloating: Boolean,
    dismissReason: ImagePeekDismissReason?,
    onRequestCancel: () -> Unit,
    onDismissComplete: () -> Unit,
    onOpenFullscreenBehind: (Int) -> Unit,
    onEnterFullscreenHandoffComplete: () -> Unit,
) {
    val onMessage = LocalUiMessenger.current
    val saveHint = LocalImageSaveHint.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    val imagePeekController = LocalImagePeekController.current
    var saving by remember { mutableStateOf(false) }
    val images = allImages.ifEmpty { listOf(image) }
    val resolvedImages = remember(images, statusItem) {
        images.map { it.resolveForSave(statusItem, images) }
    }
    val showSaveAll = images.size > 1
    val safeInitialIndex = initialImageIndex.coerceIn(0, images.lastIndex)
    val pagerState = rememberPagerState(initialPage = safeInitialIndex) { images.size }
    val pagerFlingBehavior = PagerDefaults.flingBehavior(
        state = pagerState,
        snapAnimationSpec = tween(
            durationMillis = 280,
            easing = MediaPeekEnterEasing,
        ),
    )
    val currentImage = resolvedImages[pagerState.currentPage]
    val enterProgress = remember { Animatable(0f) }
    val fullscreenExpandProgress = remember { Animatable(0f) }
    var fullscreenOpened by remember { mutableStateOf(false) }
    var actionMenuVisible by remember { mutableStateOf(false) }
    var closingAnchorBounds by remember(anchorBounds) { mutableStateOf(anchorBounds) }
    var peekDismissStartBounds by remember { mutableStateOf<Rect?>(null) }
    var peekDismissTargetBounds by remember { mutableStateOf<Rect?>(null) }
    var overlayWindowOrigin by remember { mutableStateOf(Offset.Zero) }
    val peekDismissMorphProgress = remember { Animatable(1f) }

    fun requestDismiss(handoffBounds: Rect? = null) {
        if (isFloating && dismissReason == null) {
            closingAnchorBounds = resolveAnchorBounds()
            peekDismissStartBounds = handoffBounds
            peekDismissTargetBounds = closingAnchorBounds
                .takeIf { it.width > 0f && it.height > 0f }
                ?.toOverlayLocal(overlayWindowOrigin)
        }
        onRequestCancel()
    }

    LaunchedEffect(Unit) {
        actionMenuVisible = true
        val layoutStart = mediaPeekLayoutStartProgress()
        enterProgress.snapTo(layoutStart)
        enterProgress.animateTo(
            targetValue = 1f,
            animationSpec = MediaPeekEnterAnimationSpec,
        )
    }

    LaunchedEffect(isFloating) {
        if (isFloating && enterProgress.value < 1f) {
            val resumeFrom = enterProgress.value.coerceAtLeast(mediaPeekLayoutStartProgress())
            if (enterProgress.value < resumeFrom) {
                enterProgress.snapTo(resumeFrom)
            }
            enterProgress.animateTo(1f, MediaPeekEnterAnimationSpec)
        }
    }

    DisposableEffect(pagerState, scope) {
        if (images.size > 1) {
            imagePeekController.bindHorizontalDragHandler { deltaX ->
                scope.launch {
                    pagerState.scroll {
                        scrollBy(-deltaX)
                    }
                }
            }
        }
        onDispose {
            imagePeekController.bindHorizontalDragHandler(null)
        }
    }

    LaunchedEffect(dismissReason) {
        when (dismissReason) {
            null -> return@LaunchedEffect
            ImagePeekDismissReason.EnterFullscreen -> {
                actionMenuVisible = false
                if (enterProgress.value < 1f) {
                    enterProgress.snapTo(1f)
                }
                fullscreenExpandProgress.snapTo(0f)
                fullscreenExpandProgress.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(
                        durationMillis = NavTransitionDurationMs,
                        easing = FastOutSlowInEasing,
                    ),
                )
                if (!fullscreenOpened) {
                    fullscreenOpened = true
                    onOpenFullscreenBehind(pagerState.currentPage)
                }
                withFrameMillis { }
                onEnterFullscreenHandoffComplete()
            }
            else -> {
                actionMenuVisible = false
                fullscreenExpandProgress.snapTo(0f)
                closingAnchorBounds = resolveAnchorBounds()
                if (isFloating) {
                    peekDismissTargetBounds = closingAnchorBounds
                        .takeIf { it.width > 0f && it.height > 0f }
                        ?.toOverlayLocal(overlayWindowOrigin)
                    peekDismissMorphProgress.snapTo(1f)
                    peekDismissMorphProgress.animateTo(
                        targetValue = 0f,
                        animationSpec = spring(dampingRatio = 0.9f, stiffness = 520f),
                    )
                } else {
                    enterProgress.animateTo(
                        targetValue = 0f,
                        animationSpec = MediaPeekDismissAnimationSpec,
                    )
                }
                onDismissComplete()
            }
        }
    }

    BackHandler(enabled = dismissReason != ImagePeekDismissReason.EnterFullscreen) {
        requestDismiss()
    }
    val expandProgressForBackdrop = fullscreenExpandProgress.value.coerceIn(0f, 1f)
    val expandingToFullscreen = expandProgressForBackdrop > 0f ||
        fullscreenOpened ||
        dismissReason == ImagePeekDismissReason.EnterFullscreen
    BoxWithConstraints(
        Modifier
            .fillMaxSize()
            .onGloballyPositioned { coordinates ->
                val windowBounds = coordinates.boundsInWindow()
                overlayWindowOrigin = Offset(windowBounds.left, windowBounds.top)
            }
            .then(
                if (expandingToFullscreen) {
                    Modifier.background(Color.Black)
                } else {
                    Modifier
                },
            )
            .pointerInput(isFloating, dismissReason, images.size) {
                if (!isFloating || dismissReason != null) return@pointerInput
                val dragGestureThreshold = 82f
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    var totalDrag = Offset.Zero
                    var handled = false
                    while (true) {
                        val event = awaitPointerEvent(PointerEventPass.Initial)
                        val change = event.changes.firstOrNull { it.id == down.id }
                            ?: event.changes.firstOrNull()
                            ?: break
                        if (!change.pressed) {
                            if (!handled) {
                                val verticalDominant = abs(totalDrag.y) > abs(totalDrag.x) * 1.15f
                                if (totalDrag.y > dragGestureThreshold && verticalDominant) {
                                    requestDismiss()
                                } else if (totalDrag.y < -dragGestureThreshold && verticalDominant) {
                                    imagePeekController.enterFullscreen()
                                }
                            }
                            break
                        }
                        val delta = change.position - change.previousPosition
                        totalDrag += delta
                        val verticalDominant = abs(totalDrag.y) > abs(totalDrag.x) * 1.15f
                        if (!verticalDominant) continue
                        if (totalDrag.y > dragGestureThreshold) {
                            change.consume()
                            requestDismiss()
                            handled = true
                            while (true) {
                                val consumeEvent = awaitPointerEvent(PointerEventPass.Initial)
                                consumeEvent.changes.forEach { it.consume() }
                                if (consumeEvent.changes.all { !it.pressed }) break
                            }
                            break
                        }
                        if (totalDrag.y < -dragGestureThreshold) {
                            change.consume()
                            imagePeekController.enterFullscreen()
                            handled = true
                            while (true) {
                                val consumeEvent = awaitPointerEvent(PointerEventPass.Initial)
                                consumeEvent.changes.forEach { it.consume() }
                                if (consumeEvent.changes.all { !it.pressed }) break
                            }
                            break
                        }
                    }
                }
            }
            .zIndex(
                if (fullscreenExpandProgress.value > 0f || dismissReason == ImagePeekDismissReason.EnterFullscreen) {
                    550f
                } else {
                    300f
                },
            ),
    ) {
        val rawProgress = enterProgress.value.coerceIn(0f, 1f)
        val layoutProgress = mediaPeekLayoutProgress(rawProgress)
        val enterScale = mediaPeekEnterScale(rawProgress)
        val fingerFollowAlpha = if (isFloating) 0f else (1f - expandProgressForBackdrop).coerceIn(0f, 1f)
        val expandProgress = expandProgressForBackdrop
        val useFloatingDismissMorph = isFloating &&
            dismissReason != null &&
            dismissReason != ImagePeekDismissReason.EnterFullscreen
        val scrimAlpha = if (expandingToFullscreen) {
            1f
        } else if (useFloatingDismissMorph) {
            0.42f * peekDismissMorphProgress.value.coerceIn(0f, 1f)
        } else {
            0.42f * layoutProgress
        }
        val previewTapModifier = if (isFloating && dismissReason == null) {
            Modifier.pointerInput(pagerState.currentPage) {
                detectTapGestures(
                    onTap = { imagePeekController.enterFullscreen() },
                )
            }
        } else {
            Modifier
        }

            val previewAspect = feedImagePreviewAspectRatio(currentImage)
            val previewWidth = minOf(maxWidth * 0.88f, 340.dp)
            val previewHeight = previewWidth / previewAspect
            val targetLeft = (maxWidth - previewWidth) / 2
            val targetTop = maxHeight * 0.18f
            val targetLeftPx = with(density) { targetLeft.toPx() }
            val targetTopPx = with(density) { targetTop.toPx() }
            val targetWidthPx = with(density) { previewWidth.toPx() }
            val targetHeightPx = with(density) { previewHeight.toPx() }
            val maxWidthPx = with(density) { maxWidth.toPx() }
            val maxHeightPx = with(density) { maxHeight.toPx() }
            val safeImageAspect = previewAspect.coerceIn(0.25f, 4f)

            if (useFloatingDismissMorph) {
                val transition = peekDismissMorphProgress.value.coerceIn(0f, 1f)
                val floatingBounds = Rect(
                    left = targetLeftPx,
                    top = targetTopPx,
                    right = targetLeftPx + targetWidthPx,
                    bottom = targetTopPx + targetHeightPx,
                )
                val morphSourceBounds = peekDismissStartBounds ?: floatingBounds
                val morphTargetBounds = peekDismissTargetBounds
                    ?: closingAnchorBounds.toOverlayLocal(overlayWindowOrigin)
                val morphWidth = lerp(morphTargetBounds.width, morphSourceBounds.width, transition)
                val morphHeight = lerp(morphTargetBounds.height, morphSourceBounds.height, transition)
                val morphCenterX = lerp(morphTargetBounds.center.x, morphSourceBounds.center.x, transition)
                val morphCenterY = lerp(morphTargetBounds.center.y, morphSourceBounds.center.y, transition)
                val uniformScale = computeMorphCoverScale(
                    morphWidthPx = morphWidth,
                    morphHeightPx = morphHeight,
                    contentWidthPx = targetWidthPx,
                    contentHeightPx = targetHeightPx,
                )
                val morphLeft = morphCenterX - morphWidth / 2f
                val morphTop = morphCenterY - morphHeight / 2f
                val morphRight = morphCenterX + morphWidth / 2f
                val morphBottom = morphCenterY + morphHeight / 2f
                val morphCornerRadiusPx = with(density) {
                    lerpDp(ThumbnailMorphCornerRadius, 18.dp, transition).toPx()
                }
                MorphRevealScrim(
                    morphLeft = morphLeft,
                    morphTop = morphTop,
                    morphRight = morphRight,
                    morphBottom = morphBottom,
                    cornerRadiusPx = morphCornerRadiusPx,
                    scrimColor = Color.Black.copy(alpha = scrimAlpha),
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .morphRevealClip(
                            morphLeft = morphLeft,
                            morphTop = morphTop,
                            morphRight = morphRight,
                            morphBottom = morphBottom,
                            cornerRadiusPx = morphCornerRadiusPx,
                        )
                        .graphicsLayer {
                            translationX = morphCenterX - maxWidthPx / 2f
                            translationY = morphCenterY - maxHeightPx / 2f
                            scaleX = uniformScale
                            scaleY = uniformScale
                            transformOrigin = TransformOrigin.Center
                        },
                ) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(previewWidth, previewHeight)
                            .background(Color.Black),
                    ) {
                        if (images.size > 1) {
                            HorizontalPager(
                                state = pagerState,
                                modifier = Modifier.fillMaxSize(),
                                userScrollEnabled = false,
                                beyondViewportPageCount = 1,
                                flingBehavior = pagerFlingBehavior,
                            ) { page ->
                                ImageActionPreviewImage(
                                    image = images[page],
                                    isActive = page == pagerState.currentPage,
                                    imageOwner = statusItem,
                                    modifier = Modifier.fillMaxSize(),
                                )
                            }
                        } else {
                            ImageActionPreviewImage(
                                image = currentImage,
                                imageOwner = statusItem,
                                modifier = Modifier.fillMaxSize(),
                            )
                        }
                    }
                }
            } else {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(if (expandingToFullscreen) Color.Black else Color.Black.copy(alpha = scrimAlpha))
                .clickable(
                    enabled = dismissReason != ImagePeekDismissReason.EnterFullscreen,
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = { requestDismiss() },
                ),
        )

            val screenAspect = maxWidthPx / maxHeightPx.coerceAtLeast(1f)
            val fullscreenImageWidthPx: Float
            val fullscreenImageHeightPx: Float
            if (screenAspect > safeImageAspect) {
                fullscreenImageHeightPx = maxHeightPx
                fullscreenImageWidthPx = fullscreenImageHeightPx * safeImageAspect
            } else {
                fullscreenImageWidthPx = maxWidthPx
                fullscreenImageHeightPx = fullscreenImageWidthPx / safeImageAspect
            }
            val motion = computeMediaPeekGraphicsMotion(
                anchorBounds = closingAnchorBounds,
                layoutOriginLeftPx = targetLeftPx,
                layoutOriginTopPx = targetTopPx,
                layoutWidthPx = targetWidthPx,
                layoutHeightPx = targetHeightPx,
                layoutProgress = layoutProgress,
                enterScale = enterScale,
                fingerFollowAlpha = fingerFollowAlpha,
                expandProgress = expandProgress,
                expandWidthPx = fullscreenImageWidthPx,
                expandHeightPx = fullscreenImageHeightPx,
                expandCenterX = maxWidthPx / 2f,
                expandCenterY = maxHeightPx / 2f,
            )
            val menuLabels = buildList {
                add("\u4fdd\u5b58")
                if (showSaveAll) add("\u4fdd\u5b58\u5168\u90e8")
                add("\u5206\u4eab")
            }
            val menuHeight = if (showSaveAll) ActionMenuThreeRowHeight else ActionMenuTwoRowHeight
            val menuWidth = rememberActionMenuWidth(menuLabels, maxWidth - 28.dp)
            val menuPlacement = calculateActionMenuOffsetFromAnchorPx(
                anchorBounds = motion.visualBounds,
                screenWidthPx = maxWidthPx,
                screenHeightPx = maxHeightPx,
                menuWidthPx = with(density) { menuWidth.toPx() },
                menuHeightPx = with(density) { menuHeight.toPx() },
                marginPx = with(density) { 14.dp.toPx() },
                gapPx = with(density) { 10.dp.toPx() },
            )

            val menuWidthPx = with(density) { menuWidth.toPx() }
            val menuHeightPx = with(density) { menuHeight.toPx() }
            val menuRevealVisible = actionMenuVisible && expandProgress == 0f && !fullscreenOpened
            val originInMenu = computeActionMenuOriginInMenu(
                anchorInRoot = pressOffset,
                menuOffset = menuPlacement.offset,
                menuWidthPx = menuWidthPx,
                menuHeightPx = menuHeightPx,
            )
            Box(
                modifier = Modifier
                    .offset(x = targetLeft, y = targetTop)
                    .size(previewWidth, previewHeight)
                    .mediaPeekMotionLayer(
                        motion = motion,
                        fingerDragOffset = { imagePeekController.fingerDragOffset },
                        shadowElevation = if (layoutProgress > 0.28f && expandProgress == 0f) {
                            16.dp
                        } else {
                            0.dp
                        },
                    )
                    .background(Color.Black),
            ) {
                if (images.size > 1) {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize(),
                        userScrollEnabled = dismissReason == null,
                        beyondViewportPageCount = 1,
                        flingBehavior = pagerFlingBehavior,
                    ) { page ->
                        ImageActionPreviewImage(
                            image = images[page],
                            isActive = page == pagerState.currentPage,
                            imageOwner = statusItem,
                            modifier = Modifier
                                .fillMaxSize()
                                .then(previewTapModifier),
                        )
                    }
                } else {
                    ImageActionPreviewImage(
                        image = currentImage,
                        imageOwner = statusItem,
                        modifier = Modifier
                            .fillMaxSize()
                            .then(previewTapModifier),
                    )
                }

                if (images.size > 1) {
                    ImagePeekPageIndicator(
                        pageCount = images.size,
                        pagerState = pagerState,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 10.dp),
                    )
                }
            }

            ActionMenuReveal(
                visible = menuRevealVisible,
                menuWidth = menuWidth,
                menuHeight = menuHeight,
                originInMenu = originInMenu,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset { menuPlacement.offset }
                    .width(menuWidth)
                    .height(menuHeight),
            ) {
                ImageActionFrostedCard(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    ImageActionRow(
                        label = "保存",
                        enabled = !saving,
                        onClick = {
                            saving = true
                            scope.launch {
                                if (saveHint.saveOne(context, currentImage, statusItem)) {
                                    requestDismiss()
                                }
                                saving = false
                            }
                        },
                    )
                    if (showSaveAll) {
                        ImageActionRow(
                            label = "保存全部",
                            enabled = !saving,
                            onClick = {
                                saving = true
                                scope.launch {
                                    val result = saveHint.saveAll(context, resolvedImages, statusItem)
                                    if (result.saved > 0) {
                                        requestDismiss()
                                    }
                                    saving = false
                                }
                            },
                        )
                    }
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
            ImageSaveHintOverlay(
                modifier = Modifier.align(Alignment.TopCenter),
            )
            }
    }
}

@Composable
private fun ImagePeekPageIndicator(
    pageCount: Int,
    pagerState: PagerState,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        repeat(pageCount) { index ->
            val offset = (pagerState.currentPage - index) + pagerState.currentPageOffsetFraction
            val proximity = (1f - abs(offset).coerceIn(0f, 1f))
            Box(
                modifier = Modifier
                    .size(lerpDp(6.dp, 7.dp, proximity))
                    .clip(CircleShape)
                    .background(
                        Color.White.copy(alpha = lerp(0.42f, 0.95f, proximity)),
                    ),
            )
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
    playbackOwnerId: String,
    anchorBounds: Rect,
    pressOffset: Offset,
    resolveAnchorBounds: () -> Rect,
    expandFromAnchor: Boolean = false,
    fromFullscreen: Boolean = false,
    dockImmediately: Boolean = false,
    isFloating: Boolean,
    isFullscreenMode: Boolean,
    dismissReason: VideoPeekDismissReason?,
    onRequestCancel: () -> Unit,
    onDismissComplete: () -> Unit,
    onPlaybackEnded: () -> Unit,
    onEnterFullscreenHandoffComplete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val videoPeekController = LocalVideoPeekController.current
    val playbackKey = remember(media.streamUrl, media.downloadUrl, media.coverUrl, playbackOwnerId) {
        videoPlaybackKey(media, playbackOwnerId)
    }
    val statusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    var aspectRatio by remember(media.streamUrl) { mutableStateOf(16f / 9f) }
    val enterProgress = remember(fromFullscreen, dockImmediately) {
        Animatable(
            when {
                fromFullscreen -> 0f
                dockImmediately -> 1f
                else -> 0f
            },
        )
    }
    val dockProgress = remember(dockImmediately) {
        Animatable(if (dockImmediately) 1f else 0f)
    }
    val fullscreenExpandProgress = remember { Animatable(if (isFullscreenMode) 1f else 0f) }
    var closingAnchorBounds by remember(anchorBounds) { mutableStateOf(anchorBounds) }
    var peekDismissStartBounds by remember { mutableStateOf<Rect?>(null) }
    var peekDismissTargetBounds by remember { mutableStateOf<Rect?>(null) }
    var peekDismissUseTopExit by remember { mutableStateOf(false) }
    var overlayWindowOrigin by remember { mutableStateOf(Offset.Zero) }
    val peekDismissMorphProgress = remember { Animatable(1f) }
    val isDocked = isFloating || dockProgress.value > 0f || isFullscreenMode

    fun requestDismiss() {
        onRequestCancel()
    }

    ImmersiveVideoChromeEffect(enabled = isFullscreenMode)

    LaunchedEffect(expandFromAnchor, fromFullscreen, dockImmediately) {
        when {
            expandFromAnchor || fromFullscreen -> enterProgress.snapTo(0f)
            dockImmediately -> enterProgress.snapTo(1f)
            else -> {
                enterProgress.snapTo(0f)
                enterProgress.animateTo(
                    targetValue = 1f,
                    animationSpec = MediaPeekEnterAnimationSpec,
                )
            }
        }
    }

    LaunchedEffect(isFloating, dockImmediately, fromFullscreen, isFullscreenMode) {
        if (isFullscreenMode) {
            enterProgress.snapTo(1f)
            dockProgress.snapTo(1f)
            fullscreenExpandProgress.snapTo(1f)
            return@LaunchedEffect
        }
        if (isFloating) {
            if (enterProgress.value < 1f) {
                enterProgress.animateTo(1f, MediaPeekEnterAnimationSpec)
            }
            if (fullscreenExpandProgress.value > 0f) {
                fullscreenExpandProgress.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(
                        durationMillis = NavTransitionDurationMs,
                        easing = FastOutSlowInEasing,
                    ),
                )
            }
            if (dockImmediately) {
                dockProgress.snapTo(1f)
            } else {
                dockProgress.animateTo(
                    targetValue = 1f,
                    animationSpec = MediaPeekDockAnimationSpec,
                )
            }
        } else if (dismissReason == null) {
            dockProgress.snapTo(0f)
        }
    }

    LaunchedEffect(dismissReason) {
        when (dismissReason) {
            null -> return@LaunchedEffect
            VideoPeekDismissReason.EnterFullscreen -> {
                if (!expandFromAnchor && enterProgress.value < 1f) {
                    enterProgress.snapTo(1f)
                }
                if (isFloating && dockProgress.value < 1f) {
                    dockProgress.snapTo(1f)
                }
                fullscreenExpandProgress.snapTo(0f)
                fullscreenExpandProgress.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(
                        durationMillis = NavTransitionDurationMs,
                        easing = FastOutSlowInEasing,
                    ),
                )
                videoPeekController.completeFullscreenExpand()
            }
            else -> {
                fullscreenExpandProgress.snapTo(0f)
                videoPeekController.resetFingerDragOffset()
                if (videoPeekController.snapDismiss) {
                    onDismissComplete()
                    return@LaunchedEffect
                }
                if (isFloating) {
                    closingAnchorBounds = resolveAnchorBounds()
                    val statusBarTopPx = with(density) { statusBarTop.toPx() }
                    val screenWidthPx = with(density) { configuration.screenWidthDp.dp.toPx() }
                    val screenHeightPx = with(density) { configuration.screenHeightDp.dp.toPx() }
                    val dockLeftPx = 0f
                    val dockTopPx = statusBarTopPx + with(density) { 8.dp.toPx() }
                    val dockWidthPx = screenWidthPx
                    val dockHeightPx = dockWidthPx / VideoPeekDockAspectRatio
                    val sourceBounds = Rect(
                        left = dockLeftPx,
                        top = dockTopPx,
                        right = dockLeftPx + dockWidthPx,
                        bottom = dockTopPx + dockHeightPx,
                    )
                    val (targetBounds, useTopExit) = resolveVideoPeekDismissTargetBounds(
                        anchorInWindow = closingAnchorBounds,
                        sourceBoundsInOverlay = sourceBounds,
                        screenWidthPx = screenWidthPx,
                        screenHeightPx = screenHeightPx,
                        statusBarTopPx = statusBarTopPx,
                        aspectRatio = aspectRatio,
                        overlayOriginInWindow = overlayWindowOrigin,
                        forceTopExit = fromFullscreen,
                    )
                    peekDismissStartBounds = sourceBounds
                    peekDismissTargetBounds = targetBounds
                    peekDismissUseTopExit = useTopExit
                    peekDismissMorphProgress.snapTo(1f)
                    peekDismissMorphProgress.animateTo(
                        targetValue = 0f,
                        animationSpec = spring(dampingRatio = 0.9f, stiffness = 520f),
                    )
                } else {
                    if (dockProgress.value > 0f) {
                        dockProgress.animateTo(0f, MediaPeekDismissAnimationSpec)
                    }
                    enterProgress.animateTo(
                        targetValue = 0f,
                        animationSpec = MediaPeekDismissAnimationSpec,
                    )
                }
                onDismissComplete()
            }
        }
    }

    BackHandler(enabled = isFullscreenMode || (dismissReason != VideoPeekDismissReason.EnterFullscreen && isDocked)) {
        requestDismiss()
    }

    val expandProgressForBackdrop = fullscreenExpandProgress.value.coerceIn(0f, 1f)
    val expandingToFullscreen = isFullscreenMode ||
        expandProgressForBackdrop > 0f ||
        dismissReason == VideoPeekDismissReason.EnterFullscreen

    BoxWithConstraints(
        modifier = modifier
            .onGloballyPositioned { coordinates ->
                val windowBounds = coordinates.boundsInWindow()
                overlayWindowOrigin = Offset(windowBounds.left, windowBounds.top)
            }
            .then(
            if (expandingToFullscreen || !isDocked) {
                Modifier.fillMaxSize()
            } else {
                Modifier.fillMaxWidth()
            },
        ),
    ) {
        val rawProgress = enterProgress.value.coerceIn(0f, 1f)
        val layoutProgress = mediaPeekLayoutProgress(rawProgress)
        val enterScale = mediaPeekEnterScale(rawProgress)
        val fingerFollowAlpha = if (isFloating || isDocked) 0f else (1f - expandProgressForBackdrop).coerceIn(0f, 1f)
        val expandProgress = expandProgressForBackdrop
        val dockedProgress = dockProgress.value.coerceIn(0f, 1f)
        val useFloatingDismissMorph = isFloating &&
            dismissReason != null &&
            dismissReason != VideoPeekDismissReason.EnterFullscreen
        val effectiveMaxHeight = if (maxHeight == Dp.Infinity) screenHeight else maxHeight
        val safeVideoAspect = aspectRatio.coerceIn(0.25f, 4f)

        val previewWidth = minOf(maxWidth - 32.dp, maxWidth * 0.94f)
        val previewHeight = previewWidth / safeVideoAspect
        val holdLeft = (maxWidth - previewWidth) / 2
        val holdTop = effectiveMaxHeight * 0.18f
        val dockWidth = maxWidth
        val dockHeight = dockWidth / VideoPeekDockAspectRatio
        val dockLeft = 0.dp
        val dockTop = statusBarTop + 8.dp

        val holdLeftPx = with(density) { holdLeft.toPx() }
        val holdTopPx = with(density) { holdTop.toPx() }
        val holdWidthPx = with(density) { previewWidth.toPx() }
        val holdHeightPx = with(density) { previewHeight.toPx() }
        val dockLeftPx = with(density) { dockLeft.toPx() }
        val dockTopPx = with(density) { dockTop.toPx() }
        val dockWidthPx = with(density) { dockWidth.toPx() }
        val dockHeightPx = with(density) { dockHeight.toPx() }
        val maxWidthPx = with(density) { maxWidth.toPx() }
        val maxHeightPx = with(density) { effectiveMaxHeight.toPx() }
        val screenAspect = maxWidthPx / maxHeightPx.coerceAtLeast(1f)
        val fullscreenVideoWidthPx: Float
        val fullscreenVideoHeightPx: Float
        if (screenAspect > safeVideoAspect) {
            fullscreenVideoHeightPx = maxHeightPx
            fullscreenVideoWidthPx = fullscreenVideoHeightPx * safeVideoAspect
        } else {
            fullscreenVideoWidthPx = maxWidthPx
            fullscreenVideoHeightPx = fullscreenVideoWidthPx / safeVideoAspect
        }
        val motion = computeMediaPeekGraphicsMotion(
            anchorBounds = anchorBounds,
            layoutOriginLeftPx = holdLeftPx,
            layoutOriginTopPx = holdTopPx,
            layoutWidthPx = holdWidthPx,
            layoutHeightPx = holdHeightPx,
            layoutProgress = layoutProgress,
            enterScale = enterScale,
            fingerFollowAlpha = fingerFollowAlpha,
            expandProgress = expandProgress,
            expandWidthPx = fullscreenVideoWidthPx,
            expandHeightPx = fullscreenVideoHeightPx,
            expandCenterX = maxWidthPx / 2f,
            expandCenterY = maxHeightPx / 2f,
            dockProgress = dockedProgress,
            dockLeftPx = dockLeftPx,
            dockTopPx = dockTopPx,
            dockWidthPx = dockWidthPx,
            dockHeightPx = dockHeightPx,
        )
        val useDockedColumnLayout = isDocked && !expandingToFullscreen && !useFloatingDismissMorph
        val peekDismissEnabled = isDocked && dismissReason == null && !useFloatingDismissMorph && !isFullscreenMode

        val videoCardModifier = Modifier
            .size(previewWidth, previewHeight)
            .mediaPeekMotionLayer(
                motion = motion,
                fingerDragOffset = { videoPeekController.fingerDragOffset },
                shadowElevation = if (layoutProgress > 0.28f && expandProgress == 0f) {
                    if (isDocked) 8.dp else 16.dp
                } else {
                    0.dp
                },
            )
            .background(Color.Black)
            .then(
                if (!isDocked && dismissReason == null) {
                    Modifier.clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                        onClick = { videoPeekController.enterFullscreen() },
                    )
                } else {
                    Modifier
                },
            )

        @Composable
        fun VideoPeekCard(modifier: Modifier) {
            Box(modifier = modifier) {
                WeiboVideoSurface(
                    media = media,
                    playbackOwnerId = playbackOwnerId,
                    isFullscreen = isFullscreenMode,
                    controlsEnabled = isDocked || isFullscreenMode,
                    initialControlsVisible = false,
                    trackViewportPauseOverride = false,
                    isPeekPlayback = !isFullscreenMode,
                    seamlessOverlayPlayback = !expandFromAnchor,
                    resumePosition = true,
                    savePositionOnDispose = true,
                    playbackSpeedOverride = when {
                        isFullscreenMode -> null
                        expandFromAnchor -> null
                        isDocked -> null
                        else -> VideoPeekFloatingPlaybackSpeed
                    },
                    onPlaybackEnded = onPlaybackEnded,
                    onAspectRatio = { width, height ->
                        aspectRatio = width.toFloat() / height.toFloat()
                    },
                    onFullscreen = { videoPeekController.enterFullscreen() },
                    onEnterFloatingPlayback = if (isFullscreenMode) {
                        { videoPeekController.exitFullscreenToFloating() }
                    } else {
                        null
                    },
                    showFullscreenButton = false,
                    showPictureInPictureButton = false,
                    onPeekVerticalDismiss = if (peekDismissEnabled) {
                        { requestDismiss() }
                    } else {
                        null
                    },
                    onPeekFingerDragOffset = if (peekDismissEnabled) {
                        { offset -> videoPeekController.updateFingerDragOffset(offset) }
                    } else {
                        null
                    },
                    onPeekFingerDragReset = if (peekDismissEnabled) {
                        { videoPeekController.resetFingerDragOffset() }
                    } else {
                        null
                    },
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }

        if (useFloatingDismissMorph) {
            val transition = peekDismissMorphProgress.value.coerceIn(0f, 1f)
            val morphSourceBounds = peekDismissStartBounds ?: Rect(
                left = dockLeftPx,
                top = dockTopPx,
                right = dockLeftPx + dockWidthPx,
                bottom = dockTopPx + dockHeightPx,
            )
            val morphTargetBounds = peekDismissTargetBounds
                ?: closingAnchorBounds.toOverlayLocal(overlayWindowOrigin)
            val morphWidth = lerp(morphTargetBounds.width, morphSourceBounds.width, transition)
            val morphHeight = lerp(morphTargetBounds.height, morphSourceBounds.height, transition)
            val morphCenterX = lerp(morphTargetBounds.center.x, morphSourceBounds.center.x, transition)
            val morphCenterY = lerp(morphTargetBounds.center.y, morphSourceBounds.center.y, transition)
            val morphLeft = morphCenterX - morphWidth / 2f
            val morphTop = morphCenterY - morphHeight / 2f
            val morphRight = morphCenterX + morphWidth / 2f
            val morphBottom = morphCenterY + morphHeight / 2f
            val morphCornerRadiusPx = with(density) {
                lerpDp(12.dp, ThumbnailMorphCornerRadius, transition).toPx()
            }
            MorphRevealScrim(
                morphLeft = morphLeft,
                morphTop = morphTop,
                morphRight = morphRight,
                morphBottom = morphBottom,
                cornerRadiusPx = morphCornerRadiusPx,
                scrimColor = Color.Transparent,
            )
        } else if (expandingToFullscreen) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black),
            )
        } else if (!isDocked) {
            val scrimAlpha = 0.42f * layoutProgress
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = scrimAlpha))
                    .clickable(
                        enabled = dismissReason == null,
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                        onClick = { onRequestCancel() },
                    )
                    .pointerInput(dismissReason) {
                        if (dismissReason != null) return@pointerInput
                        val dragGestureThreshold = 82f
                        awaitEachGesture {
                            val down = awaitFirstDown(requireUnconsumed = false)
                            var totalDrag = Offset.Zero
                            var handled = false
                            while (true) {
                                val event = awaitPointerEvent(PointerEventPass.Initial)
                                val change = event.changes.firstOrNull { it.id == down.id }
                                    ?: event.changes.firstOrNull()
                                    ?: break
                                if (!change.pressed) {
                                    if (!handled) {
                                        val verticalDominant = abs(totalDrag.y) > abs(totalDrag.x) * 1.15f
                                        if (totalDrag.y > dragGestureThreshold && verticalDominant) {
                                            onRequestCancel()
                                        } else if (totalDrag.y < -dragGestureThreshold && verticalDominant) {
                                            videoPeekController.release()
                                        } else {
                                            videoPeekController.enterFullscreen()
                                        }
                                    }
                                    break
                                }
                                totalDrag += change.position - change.previousPosition
                                val verticalDominant = abs(totalDrag.y) > abs(totalDrag.x) * 1.15f
                                if (totalDrag.y > dragGestureThreshold && verticalDominant) {
                                    onRequestCancel()
                                    handled = true
                                    while (true) {
                                        val consumeEvent = awaitPointerEvent(PointerEventPass.Initial)
                                        consumeEvent.changes.forEach { it.consume() }
                                        if (consumeEvent.changes.all { !it.pressed }) break
                                    }
                                    break
                                }
                                if (totalDrag.y < -dragGestureThreshold && verticalDominant) {
                                    videoPeekController.release()
                                    handled = true
                                    while (true) {
                                        val consumeEvent = awaitPointerEvent(PointerEventPass.Initial)
                                        consumeEvent.changes.forEach { it.consume() }
                                        if (consumeEvent.changes.all { !it.pressed }) break
                                    }
                                    break
                                }
                            }
                        }
                    },
            )
        }

        val floatingDismissMorphModifier = if (useFloatingDismissMorph) {
            val transition = peekDismissMorphProgress.value.coerceIn(0f, 1f)
            val morphSourceBounds = peekDismissStartBounds ?: Rect(
                left = dockLeftPx,
                top = dockTopPx,
                right = dockLeftPx + dockWidthPx,
                bottom = dockTopPx + dockHeightPx,
            )
            val morphTargetBounds = peekDismissTargetBounds
                ?: closingAnchorBounds.toOverlayLocal(overlayWindowOrigin)
            val morphWidth = lerp(morphTargetBounds.width, morphSourceBounds.width, transition)
            val morphHeight = lerp(morphTargetBounds.height, morphSourceBounds.height, transition)
            val morphCenterX = lerp(morphTargetBounds.center.x, morphSourceBounds.center.x, transition)
            val morphCenterY = lerp(morphTargetBounds.center.y, morphSourceBounds.center.y, transition)
            val morphLeft = morphCenterX - morphWidth / 2f
            val morphTop = morphCenterY - morphHeight / 2f
            val morphRight = morphCenterX + morphWidth / 2f
            val morphBottom = morphCenterY + morphHeight / 2f
            val morphCornerRadiusPx = with(density) {
                lerpDp(12.dp, ThumbnailMorphCornerRadius, transition).toPx()
            }
            val uniformScale = computeMorphCoverScale(
                morphWidthPx = morphWidth,
                morphHeightPx = morphHeight,
                contentWidthPx = dockWidthPx,
                contentHeightPx = dockHeightPx,
            )
            val morphAlpha = if (peekDismissUseTopExit) transition else 1f
            Modifier
                .fillMaxSize()
                .morphRevealClip(
                    morphLeft = morphLeft,
                    morphTop = morphTop,
                    morphRight = morphRight,
                    morphBottom = morphBottom,
                    cornerRadiusPx = morphCornerRadiusPx,
                )
                .graphicsLayer {
                    translationX = morphCenterX - maxWidthPx / 2f
                    translationY = morphCenterY - maxHeightPx / 2f
                    scaleX = uniformScale
                    scaleY = uniformScale
                    alpha = morphAlpha
                    transformOrigin = TransformOrigin.Center
                }
        } else {
            Modifier
        }

        Box(
            modifier = if (useFloatingDismissMorph) {
                Modifier.fillMaxSize()
            } else if (useDockedColumnLayout) {
                Modifier
                    .fillMaxWidth()
                    .height(dockTop + dockHeight)
            } else {
                Modifier.fillMaxSize()
            },
        ) {
            Box(
                modifier = floatingDismissMorphModifier.then(
                    if (useFloatingDismissMorph) {
                        Modifier.fillMaxSize()
                    } else {
                        Modifier
                    },
                ),
            ) {
                VideoPeekCard(
                    modifier = when {
                        isFullscreenMode -> Modifier
                            .fillMaxSize()
                            .background(Color.Black)
                        useFloatingDismissMorph -> Modifier
                            .align(Alignment.Center)
                            .size(dockWidth, dockHeight)
                            .graphicsLayer {
                                shape = RoundedCornerShape(12.dp)
                                clip = true
                                shadowElevation = 8.dp.toPx()
                            }
                            .background(Color.Black)
                        useDockedColumnLayout -> Modifier
                            .offset(x = dockLeft, y = dockTop)
                            .size(dockWidth, dockHeight)
                            .graphicsLayer {
                                shape = RoundedCornerShape(12.dp)
                                clip = true
                                shadowElevation = 8.dp.toPx()
                            }
                            .background(Color.Black)
                        else -> Modifier
                            .offset(x = holdLeft, y = holdTop)
                            .then(videoCardModifier)
                    },
                )
            }
        }
    }
}

private fun enrichViewerFeedImage(image: FeedImage, owner: FeedItem?): FeedImage {
    if (image.isLivePhoto) return image
    val candidates = buildList {
        owner?.images?.let(::addAll)
        owner?.retweetedStatus?.images?.let(::addAll)
    }
    val match = candidates.firstOrNull { candidate ->
        candidate.isLivePhoto && imagesReferToSamePhoto(image, candidate)
    } ?: return image
    return image.copy(
        livePhotoVideoUrl = match.livePhotoVideoUrl,
        type = "livephoto",
        width = image.width ?: match.width,
        height = image.height ?: match.height,
        downloadUrls = (image.downloadUrls + match.downloadUrls).distinct(),
    )
}

private fun resolveFeedImageForViewer(
    image: FeedImage,
    imageOwner: FeedItem? = null,
    relatedPosts: List<FeedItem> = emptyList(),
): FeedImage {
    val owner = imageOwner ?: findPostForAlbumImage(relatedPosts, image)
    var resolved = enrichViewerFeedImage(image, owner)
    if (!resolved.isLivePhoto && relatedPosts.isNotEmpty()) {
        resolved = enrichAlbumImagesFromPosts(
            images = listOf(resolved),
            lookup = buildAlbumPostLookup(relatedPosts),
        ).first()
    }
    return resolved
}

private fun imagesReferToSamePhoto(a: FeedImage, b: FeedImage): Boolean {
    if (a.largeUrl == b.largeUrl || a.id == b.id) return true
    val aKeys = albumImageMatchKeys(a)
    val bKeys = albumImageMatchKeys(b)
    return aKeys.isNotEmpty() && aKeys.any { it in bKeys }
}

@Composable
private fun ImageActionPreviewImage(
    image: FeedImage,
    modifier: Modifier = Modifier,
    isActive: Boolean = true,
    imageOwner: FeedItem? = null,
    relatedPosts: List<FeedItem> = emptyList(),
) {
    val resolvedImage = remember(image, imageOwner, relatedPosts) {
        resolveFeedImageForViewer(image, imageOwner, relatedPosts)
    }
    var livePlaying by remember(resolvedImage.largeUrl) { mutableStateOf(false) }
    LaunchedEffect(isActive, resolvedImage.isLivePhoto, resolvedImage.largeUrl) {
        livePlaying = isActive && resolvedImage.isLivePhoto
    }
    FeedImagePreviewContent(
        image = resolvedImage,
        livePlaying = livePlaying,
        onLiveEnded = { livePlaying = false },
        isActive = isActive,
        isFullscreen = false,
        modifier = modifier,
    )
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

private fun calculateFeedCardActionMenuOffsetPx(
    anchorBounds: Rect,
    screenWidthPx: Float,
    screenHeightPx: Float,
    menuWidthPx: Float,
    menuHeightPx: Float,
    marginPx: Float,
    gapPx: Float,
): ActionMenuPlacement {
    val maxX = (screenWidthPx - menuWidthPx - marginPx).coerceAtLeast(marginPx)
    val x = (anchorBounds.right - menuWidthPx).coerceIn(marginPx, maxX)
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
private fun ImageActionRow(
    label: String,
    enabled: Boolean,
    selected: Boolean = false,
    onClick: () -> Unit,
) {
    val capsuleShape = RoundedCornerShape(percent = 50)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(ActionMenuCapsuleHeight)
            .clip(capsuleShape)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = ActionMenuCapsulePaddingHorizontal),
        contentAlignment = Alignment.CenterStart,
    ) {
        Text(
            text = label,
            style = actionMenuTextStyle(selected = selected),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = actionMenuItemTextColor(enabled = enabled, selected = selected),
        )
    }
}

@Composable
private fun FeedImagePreviewContent(
    image: FeedImage,
    livePlaying: Boolean,
    onLiveEnded: () -> Unit,
    modifier: Modifier = Modifier,
    isActive: Boolean = true,
    isFullscreen: Boolean = false,
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
                placeholderBackground = Color.Black,
            )
        } else {
            RemoteImage(
                url = image.largeUrl,
                fallbackUrls = image.downloadUrls.drop(1),
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit,
                placeholderBackground = Color.Black,
            )
        }
        if (image.isLivePhoto && livePlaying) {
            LivePhotoOverlay(
                image = image,
                modifier = Modifier.fillMaxSize(),
                isActive = isActive,
                isInFullscreen = isFullscreen,
                onEnded = onLiveEnded,
            )
        }
    }
}

@Composable
private fun MediaStrip(
    images: List<FeedImage>,
    medias: List<FeedMedia> = emptyList(),
    playbackOwnerId: String,
    onMediaClick: (FeedMedia, String) -> Unit,
    modifier: Modifier = Modifier,
    onDetailClick: (() -> Unit)? = null,
    imageOwner: FeedItem? = null,
    autoFloatingOnScrollAway: Boolean = false,
) {
    if (images.isEmpty() && medias.isEmpty()) return

    var viewerOpen by remember { mutableStateOf(false) }
    var viewerIndex by remember { mutableStateOf(0) }
    var thumbnailBoundsByIndex by remember { mutableStateOf<Map<Int, Rect>>(emptyMap()) }
    var viewerSourceBoundsByIndex by remember { mutableStateOf<Map<Int, Rect>>(emptyMap()) }
    var viewerAnimateOpenFromSource by remember { mutableStateOf(true) }
    var viewerDismissHook by remember { mutableStateOf<(() -> Unit)?>(null) }
    var viewerCloseHandoff by remember { mutableStateOf<(() -> Unit)?>(null) }

    fun openImageViewer(
        index: Int,
        sourceBounds: Rect?,
        onClosed: (() -> Unit)? = null,
        onCloseStart: (() -> Unit)? = null,
    ) {
        viewerIndex = index
        if (sourceBounds != null) {
            thumbnailBoundsByIndex = thumbnailBoundsByIndex + (index to sourceBounds)
        }
        viewerSourceBoundsByIndex = thumbnailBoundsByIndex
        viewerAnimateOpenFromSource = sourceBounds != null
        viewerDismissHook = onClosed
        viewerCloseHandoff = onCloseStart
        viewerOpen = true
    }

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
                if (images.size == 1) {
                    val image = images.first()
                    val layout = singleImageFeedLayout(
                        width = image.width ?: 0,
                        height = image.height ?: 0,
                    )
                    Box(modifier = Modifier.fillMaxWidth()) {
                        FeedImageCell(
                            image = image,
                            allImages = images,
                            imageIndex = 0,
                            imageOwner = imageOwner,
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .fillMaxWidth(layout.widthFraction)
                                .aspectRatio(layout.aspectRatio),
                            contentScale = ContentScale.Fit,
                            onOpenViewer = { index, bounds, onClosed, _ ->
                                openImageViewer(index, bounds, onClosed)
                            },
                            onAnchorBoundsChanged = { bounds ->
                                thumbnailBoundsByIndex = thumbnailBoundsByIndex + (0 to bounds)
                            },
                        )
                        if (onDetailClick != null) {
                            Box(
                                modifier = Modifier.matchParentSize(),
                                contentAlignment = Alignment.TopEnd,
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(1f - layout.widthFraction)
                                        .fillMaxHeight()
                                        .clickable(
                                            indication = null,
                                            interactionSource = remember { MutableInteractionSource() },
                                            onClick = onDetailClick,
                                        ),
                                )
                            }
                        }
                    }
                } else {
                    rows.forEachIndexed { rowIndex, row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            row.forEachIndexed { indexInRow, image ->
                                val cellIndex = rowIndex * gridColumns + indexInRow
                                FeedImageCell(
                                    image = image,
                                    allImages = images,
                                    imageIndex = cellIndex,
                                    imageOwner = imageOwner,
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f),
                                    contentScale = ContentScale.Crop,
                                    onOpenViewer = { index, bounds, onClosed, _ ->
                                        openImageViewer(index, bounds, onClosed)
                                    },
                                    onAnchorBoundsChanged = { bounds ->
                                        thumbnailBoundsByIndex = thumbnailBoundsByIndex + (cellIndex to bounds)
                                    },
                                )
                            }
                            repeat(gridColumns - row.size) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .then(
                                            if (onDetailClick != null) {
                                                Modifier.clickable(
                                                    indication = null,
                                                    interactionSource = remember { MutableInteractionSource() },
                                                    onClick = onDetailClick,
                                                )
                                            } else {
                                                Modifier
                                            },
                                        ),
                                )
                            }
                        }
                    }
                }
            }

            if (viewerOpen) {
                FullscreenImageViewer(
                    images = images,
                    initialIndex = viewerIndex,
                    sourceBoundsByIndex = viewerSourceBoundsByIndex,
                    animateOpenFromSource = viewerAnimateOpenFromSource,
                    onCloseStart = viewerCloseHandoff,
                    imageOwner = imageOwner,
                    onDismiss = {
                        viewerOpen = false
                        viewerSourceBoundsByIndex = emptyMap()
                        viewerAnimateOpenFromSource = true
                        viewerCloseHandoff = null
                        viewerDismissHook?.invoke()
                        viewerDismissHook = null
                    },
                )
            }
        }

        medias.takeIf { it.isNotEmpty() }?.let { videoMedias ->
            val videoCoordinator = LocalVideoPlaybackCoordinator.current
            val isDetailInlinePlayback = LocalDetailInlineVideoPlayback.current
            val activeInlineMedia = videoMedias.firstOrNull { media ->
                isMediaInlineExpanded(
                    media = media,
                    playbackOwnerId = playbackOwnerId,
                    videoCoordinator = videoCoordinator,
                    isDetailInlinePlayback = isDetailInlinePlayback,
                )
            }
            val gridMedias = if (activeInlineMedia != null) {
                videoMedias.filter { it != activeInlineMedia }
            } else {
                videoMedias
            }
            val gridColumns = when (gridMedias.size) {
                1 -> 1
                2 -> 2
                3 -> 3
                4 -> 2
                else -> 3
            }
            val rows = gridMedias.chunked(gridColumns)

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                if (videoMedias.size == 1) {
                    InlineVideoPlayer(
                        media = videoMedias.first(),
                        playbackOwnerId = playbackOwnerId,
                        onClick = { onMediaClick(videoMedias.first(), playbackOwnerId) },
                        onFullscreenRequest = { onMediaClick(videoMedias.first(), playbackOwnerId) },
                        onDetailClick = onDetailClick,
                        autoFloatingOnScrollAway = autoFloatingOnScrollAway,
                    )
                } else {
                    activeInlineMedia?.let { media ->
                        InlineVideoPlayer(
                            media = media,
                            playbackOwnerId = playbackOwnerId,
                            onClick = { onMediaClick(media, playbackOwnerId) },
                            onFullscreenRequest = { onMediaClick(media, playbackOwnerId) },
                            onDetailClick = onDetailClick,
                            autoFloatingOnScrollAway = autoFloatingOnScrollAway,
                        )
                    }
                    if (gridMedias.isNotEmpty()) {
                        rows.forEachIndexed { _, row ->
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                row.forEach { media ->
                                    InlineVideoPlayer(
                                        media = media,
                                        playbackOwnerId = playbackOwnerId,
                                        onClick = { onMediaClick(media, playbackOwnerId) },
                                        onFullscreenRequest = { onMediaClick(media, playbackOwnerId) },
                                        autoFloatingOnScrollAway = autoFloatingOnScrollAway,
                                        modifier = Modifier
                                            .weight(1f)
                                            .aspectRatio(1f),
                                        gridCell = true,
                                    )
                                }
                                repeat(gridColumns - row.size) {
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .aspectRatio(1f)
                                            .then(
                                                if (onDetailClick != null) {
                                                    Modifier.clickable(
                                                        indication = null,
                                                        interactionSource = remember { MutableInteractionSource() },
                                                        onClick = onDetailClick,
                                                    )
                                                } else {
                                                    Modifier
                                                },
                                            ),
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

private val ThumbnailMorphCornerRadius = 4.dp

@Composable
private fun MorphRevealScrim(
    morphLeft: Float,
    morphTop: Float,
    morphRight: Float,
    morphBottom: Float,
    cornerRadiusPx: Float,
    scrimColor: Color = Color.Black,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val path = Path().apply {
            fillType = PathFillType.EvenOdd
            addRect(Rect(0f, 0f, size.width, size.height))
            addRoundRect(
                RoundRect(
                    left = morphLeft,
                    top = morphTop,
                    right = morphRight,
                    bottom = morphBottom,
                    cornerRadius = CornerRadius(cornerRadiusPx, cornerRadiusPx),
                ),
            )
        }
        drawPath(path, scrimColor)
    }
}

private fun Modifier.morphRevealClip(
    morphLeft: Float,
    morphTop: Float,
    morphRight: Float,
    morphBottom: Float,
    cornerRadiusPx: Float,
): Modifier = drawWithContent {
    val path = Path().apply {
        addRoundRect(
            RoundRect(
                left = morphLeft,
                top = morphTop,
                right = morphRight,
                bottom = morphBottom,
                cornerRadius = CornerRadius(cornerRadiusPx, cornerRadiusPx),
            ),
        )
    }
    drawContext.canvas.save()
    drawContext.canvas.clipPath(path)
    drawContent()
    drawContext.canvas.restore()
}

@Composable
private fun FullscreenImageViewer(
    images: List<FeedImage>,
    initialIndex: Int,
    onDismiss: () -> Unit,
    sourceBoundsByIndex: Map<Int, Rect> = emptyMap(),
    animateOpenFromSource: Boolean = true,
    onCloseStart: (() -> Unit)? = null,
    imageOwner: FeedItem? = null,
    session: WeiboWebSession? = null,
    relatedPosts: List<FeedItem> = emptyList(),
    emoticonMap: Map<String, String> = emptyMap(),
    statusCache: Map<String, FeedItem> = emptyMap(),
    onOpenStatus: ((FeedItem, Int, Map<String, FeedItem>) -> Unit)? = null,
) {
    val scope = rememberCoroutineScope()
    val viewerImages = remember(images, relatedPosts) {
        if (relatedPosts.isEmpty()) {
            images
        } else {
            enrichAlbumImagesFromPosts(images, buildAlbumPostLookup(relatedPosts))
        }
    }
    val pagerState = rememberPagerState(pageCount = { viewerImages.size }, initialPage = initialIndex)
    val transitionProgress = remember(initialIndex) {
        Animatable(if (animateOpenFromSource && sourceBoundsByIndex[initialIndex] != null) 0f else 1f)
    }
    var transitionClosing by remember { mutableStateOf(false) }
    var dragDismissProgress by remember { mutableFloatStateOf(0f) }
    var closeStartBounds by remember { mutableStateOf<Rect?>(null) }
    var dismissBoundsProviders by remember { mutableStateOf<Map<Int, () -> Rect>>(emptyMap()) }
    fun dismissViewer(startBounds: Rect? = null) {
        if (!transitionClosing) {
            val closeBounds = sourceBoundsByIndex[pagerState.currentPage]
            if (closeBounds != null) {
                closeStartBounds = startBounds ?: dismissBoundsProviders[pagerState.currentPage]?.invoke()
                transitionClosing = true
                onCloseStart?.invoke()
                scope.launch {
                    transitionProgress.animateTo(
                        targetValue = 0f,
                        animationSpec = spring(dampingRatio = 0.9f, stiffness = 520f),
                    )
                    onDismiss()
                }
            } else {
                onCloseStart?.invoke()
                onDismiss()
            }
        }
    }
    val pagerFling = PagerDefaults.flingBehavior(
        state = pagerState,
        decayAnimationSpec = exponentialDecay(frictionMultiplier = 0.82f),
    )
    var blockPagerScroll by remember { mutableStateOf(false) }
    val showStatusCaption = onOpenStatus != null && session != null
    val currentPage = pagerState.currentPage
    val currentImage = viewerImages.getOrNull(currentPage)
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

    LaunchedEffect(initialIndex, animateOpenFromSource) {
        if (animateOpenFromSource && sourceBoundsByIndex[initialIndex] != null) {
            transitionProgress.snapTo(0f)
            transitionProgress.animateTo(
                targetValue = 1f,
                animationSpec = spring(dampingRatio = 0.9f, stiffness = 520f),
            )
        }
    }

    LaunchedEffect(currentPage, currentImage?.albumStatusCacheKey(), relatedPosts, session, showStatusCaption) {
        if (!showStatusCaption || currentImage == null) {
            statusItem = null
            statusLoading = false
            return@LaunchedEffect
        }
        val activeSession = session ?: return@LaunchedEffect
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
        onDismissRequest = { dismissViewer() },
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false),
    ) {
        val dialogWindow = (LocalView.current.parent as? DialogWindowProvider)?.window
        SideEffect {
            dialogWindow?.apply {
                setWindowAnimations(0)
                setBackgroundDrawable(ColorDrawable(android.graphics.Color.TRANSPARENT))
                setDimAmount(0f)
                clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            }
        }
        BackHandler { dismissViewer() }
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize(),
        ) {
            val density = LocalDensity.current
            val containerWidthPx = with(density) { maxWidth.toPx() }.coerceAtLeast(1f)
            val containerHeightPx = with(density) { maxHeight.toPx() }.coerceAtLeast(1f)
            val morphPageIndex = if (transitionClosing) pagerState.currentPage else initialIndex
            val morphSourceBounds = when {
                transitionClosing -> closeStartBounds ?: sourceBoundsByIndex[pagerState.currentPage]
                pagerState.currentPage == initialIndex -> sourceBoundsByIndex[initialIndex]
                else -> null
            }
            val morphTargetBounds = if (transitionClosing && closeStartBounds != null) {
                sourceBoundsByIndex[pagerState.currentPage]
            } else {
                null
            }
            val morphImage = viewerImages.getOrNull(morphPageIndex)
            val imageAspect = morphImage?.let {
                val width = (it.width ?: 1).coerceAtLeast(1)
                val height = (it.height ?: 1).coerceAtLeast(1)
                width.toFloat() / height.toFloat()
            } ?: 1f
            val fitLayout = computeFitImageLayout(
                containerWidthPx = containerWidthPx,
                containerHeightPx = containerHeightPx,
                imageAspect = imageAspect,
                scale = 1f,
            )
            val transition = transitionProgress.value.coerceIn(0f, 1f)
            val backdropAlpha = (1f - dragDismissProgress).coerceIn(0f, 1f)
            val morphWidth: Float
            val morphHeight: Float
            val morphCenterX: Float
            val morphCenterY: Float
            val uniformScale: Float
            if (morphSourceBounds != null) {
                val targetBounds = morphTargetBounds
                if (targetBounds != null) {
                    morphWidth = lerp(targetBounds.width, morphSourceBounds.width, transition)
                    morphHeight = lerp(targetBounds.height, morphSourceBounds.height, transition)
                    morphCenterX = lerp(targetBounds.center.x, morphSourceBounds.center.x, transition)
                    morphCenterY = lerp(targetBounds.center.y, morphSourceBounds.center.y, transition)
                } else {
                    morphWidth = lerp(morphSourceBounds.width, fitLayout.fitWidthPx, transition)
                    morphHeight = lerp(morphSourceBounds.height, fitLayout.fitHeightPx, transition)
                    morphCenterX = lerp(morphSourceBounds.center.x, containerWidthPx / 2f, transition)
                    morphCenterY = lerp(morphSourceBounds.center.y, containerHeightPx / 2f, transition)
                }
                uniformScale = maxOf(
                    morphWidth / fitLayout.fitWidthPx.coerceAtLeast(1f),
                    morphHeight / fitLayout.fitHeightPx.coerceAtLeast(1f),
                ).coerceIn(0.05f, 4f)
            } else {
                morphWidth = fitLayout.fitWidthPx
                morphHeight = fitLayout.fitHeightPx
                morphCenterX = containerWidthPx / 2f
                morphCenterY = containerHeightPx / 2f
                uniformScale = 1f
            }
            val activeMorph = morphSourceBounds != null && (transitionClosing || transition < 0.999f)
            val effectiveBackdropAlpha = when {
                activeMorph && transitionClosing -> (transition * backdropAlpha).coerceIn(0f, 1f)
                sourceBoundsByIndex.isEmpty() -> (transition * backdropAlpha).coerceIn(0f, 1f)
                else -> backdropAlpha
            }
            val morphLeft = morphCenterX - morphWidth / 2f
            val morphTop = morphCenterY - morphHeight / 2f
            val morphRight = morphCenterX + morphWidth / 2f
            val morphBottom = morphCenterY + morphHeight / 2f
            val morphCornerRadiusPx = with(density) {
                ThumbnailMorphCornerRadius.toPx() * (1f - transition).coerceIn(0f, 1f)
            }
            when {
                activeMorph && transitionClosing -> {
                    MorphRevealScrim(
                        morphLeft = morphLeft,
                        morphTop = morphTop,
                        morphRight = morphRight,
                        morphBottom = morphBottom,
                        cornerRadiusPx = morphCornerRadiusPx,
                        scrimColor = Color.Black.copy(alpha = effectiveBackdropAlpha),
                    )
                }
                activeMorph -> {
                    MorphRevealScrim(
                        morphLeft = morphLeft,
                        morphTop = morphTop,
                        morphRight = morphRight,
                        morphBottom = morphBottom,
                        cornerRadiusPx = morphCornerRadiusPx,
                        scrimColor = Color.Black.copy(alpha = backdropAlpha),
                    )
                }
                else -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = effectiveBackdropAlpha)),
                    )
                }
            }
            HorizontalPager(
                state = pagerState,
                flingBehavior = pagerFling,
                userScrollEnabled = !blockPagerScroll,
                modifier = Modifier
                    .fillMaxSize()
                    .then(
                        if (activeMorph) {
                            Modifier.morphRevealClip(
                                morphLeft = morphLeft,
                                morphTop = morphTop,
                                morphRight = morphRight,
                                morphBottom = morphBottom,
                                cornerRadiusPx = morphCornerRadiusPx,
                            )
                        } else {
                            Modifier
                        },
                    )
                    .graphicsLayer {
                        if (activeMorph) {
                            translationX = morphCenterX - containerWidthPx / 2f
                            translationY = morphCenterY - containerHeightPx / 2f
                            scaleX = uniformScale
                            scaleY = uniformScale
                            alpha = 1f
                            transformOrigin = TransformOrigin.Center
                        }
                    },
            ) { page ->
                val pageImage = viewerImages[page]
                ZoomableFullscreenImage(
                    image = pageImage,
                    allImages = viewerImages,
                    isActive = page == pagerState.currentPage,
                    imageOwner = imageOwner ?: findPostForAlbumImage(relatedPosts, pageImage),
                    relatedPosts = relatedPosts,
                    onDismiss = { dismissViewer() },
                    onDismissFromBounds = { bounds -> dismissViewer(bounds) },
                    onDismissBoundsProvider = { provider ->
                        dismissBoundsProviders = dismissBoundsProviders + (page to provider)
                    },
                    hasMultipleImages = viewerImages.size > 1,
                    onBlockPagerScroll = { blockPagerScroll = it },
                    onDragDismissProgress = { dragDismissProgress = it },
                    onRequestPageChange = { delta ->
                        scope.launch {
                            val next = (pagerState.currentPage + delta).coerceIn(0, viewerImages.lastIndex)
                            if (next != pagerState.currentPage) {
                                pagerState.animateScrollToPage(next)
                            }
                        }
                    },
                )
            }
            Text(
                text = "${pagerState.currentPage + 1} / ${viewerImages.size}",
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 48.dp)
                    .graphicsLayer {
                        alpha = if (transitionClosing) 0f else transition.coerceIn(0f, 1f)
                    },
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
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .graphicsLayer {
                            alpha = if (transitionClosing) 0f else transition.coerceIn(0f, 1f)
                        },
                )
            }
            ImageSaveHintOverlay(
                modifier = Modifier.align(Alignment.TopCenter),
            )
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
                emoticonMap = resolveEmoticonMap(emoticonMap, statusItem.collectEmoticons()),
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

private fun computeMorphCoverScale(
    morphWidthPx: Float,
    morphHeightPx: Float,
    contentWidthPx: Float,
    contentHeightPx: Float,
): Float = maxOf(
    morphWidthPx / contentWidthPx.coerceAtLeast(1f),
    morphHeightPx / contentHeightPx.coerceAtLeast(1f),
).coerceIn(0.05f, 4f)

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

private fun computeFullscreenMaxZoomScale(
    bitmapWidth: Int,
    bitmapHeight: Int,
    containerWidthPx: Float,
    containerHeightPx: Float,
    imageAspect: Float,
    rawFillScreenScale: Float,
    sourceWidth: Int = 0,
    sourceHeight: Int = 0,
): Float {
    val fitLayout = computeFitImageLayout(
        containerWidthPx = containerWidthPx,
        containerHeightPx = containerHeightPx,
        imageAspect = imageAspect,
        scale = 1f,
    )
    val targetWidth = maxOf(bitmapWidth, sourceWidth.coerceAtLeast(0))
    val targetHeight = maxOf(bitmapHeight, sourceHeight.coerceAtLeast(0))
    val pixelPerfectScale = maxOf(
        targetWidth / fitLayout.fitWidthPx.coerceAtLeast(1f),
        targetHeight / fitLayout.fitHeightPx.coerceAtLeast(1f),
    )
    return maxOf(
        FullscreenDefaultMaxZoomScale,
        rawFillScreenScale * FullscreenFillZoomHeadroom,
        pixelPerfectScale * FullscreenPixelPerfectZoomHeadroom,
    ).coerceAtMost(FullscreenDynamicMaxZoomScale)
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
    onDismissFromBounds: (Rect) -> Unit = { onDismiss() },
    onDismissBoundsProvider: (((() -> Rect) -> Unit))? = null,
    hasMultipleImages: Boolean = false,
    isActive: Boolean = true,
    imageOwner: FeedItem? = null,
    relatedPosts: List<FeedItem> = emptyList(),
    onBlockPagerScroll: (Boolean) -> Unit = {},
    onDragDismissProgress: (Float) -> Unit = {},
    onRequestPageChange: (Int) -> Unit = {},
) {
    val context = LocalContext.current
    val resolvedImage = remember(image, imageOwner, relatedPosts) {
        resolveFeedImageForViewer(image, imageOwner, relatedPosts)
    }
    var scale by remember(resolvedImage.largeUrl) { mutableFloatStateOf(1f) }
    var panOffsetX by remember(resolvedImage.largeUrl) { mutableFloatStateOf(0f) }
    var panOffsetY by remember(resolvedImage.largeUrl) { mutableFloatStateOf(0f) }
    var dismissTranslationY by remember(resolvedImage.largeUrl) { mutableStateOf(0f) }
    var panInertiaJob by remember(resolvedImage.largeUrl) { mutableStateOf<Job?>(null) }
    var zoomAnimationJob by remember(resolvedImage.largeUrl) { mutableStateOf<Job?>(null) }
    val dismissSnapAnim = remember(resolvedImage.largeUrl) { Animatable(0f) }
    val scope = rememberCoroutineScope()
    var fullscreenBitmap by remember(resolvedImage.largeUrl) {
        mutableStateOf(
            FullscreenBitmapCache.get(fullscreenImageUrlCandidates(resolvedImage))
                ?.takeIfDrawable()
                ?.takeIf { it.isFullscreenQuality(resolvedImage) },
        )
    }
    var previewBitmap by remember(resolvedImage.largeUrl) {
        mutableStateOf(resolveFullscreenPreviewBitmap(resolvedImage))
    }
    var fullscreenLoading by remember(resolvedImage.largeUrl) { mutableStateOf(false) }
    var livePlaying by remember(resolvedImage.largeUrl) { mutableStateOf(false) }
    var actionMenuOffset by remember(resolvedImage.largeUrl) { mutableStateOf<Offset?>(null) }
    var actionMenuVisible by remember(resolvedImage.largeUrl) { mutableStateOf(false) }

    LaunchedEffect(isActive, resolvedImage.isLivePhoto, resolvedImage.largeUrl) {
        livePlaying = isActive && resolvedImage.isLivePhoto
    }

    LaunchedEffect(resolvedImage.largeUrl) {
        dismissTranslationY = 0f
        dismissSnapAnim.snapTo(0f)
        zoomAnimationJob?.cancel()
        zoomAnimationJob = null
        panOffsetX = 0f
        panOffsetY = 0f
        scale = 1f
        onBlockPagerScroll(false)
        onDragDismissProgress(0f)
        actionMenuOffset = null
        actionMenuVisible = false
        val fullscreenCandidates = fullscreenImageUrlCandidates(resolvedImage)
        FullscreenBitmapCache.get(fullscreenCandidates)?.takeIfDrawable()?.let { cached ->
            if (cached.isFullscreenQuality(resolvedImage)) {
                fullscreenBitmap = cached
                return@LaunchedEffect
            }
        }
        if (previewBitmap == null) {
            previewBitmap = withContext(Dispatchers.IO) { loadFullscreenPreviewBitmap(resolvedImage) }
        }
        if (fullscreenBitmap?.isFullscreenQuality(resolvedImage) == true) {
            return@LaunchedEffect
        }
        fullscreenLoading = true
        val loaded = withContext(Dispatchers.IO) { loadFullscreenBitmap(resolvedImage) }
        fullscreenLoading = false
        loaded?.let { bitmap ->
            fullscreenBitmap = bitmap
            fullscreenCandidates.firstOrNull()?.let { url ->
                FullscreenBitmapCache.put(url, bitmap)
            } ?: FullscreenBitmapCache.putForImage(resolvedImage, bitmap)
        }
    }

    val displayBitmap = fullscreenBitmap?.takeIf { it.isFullscreenQuality(resolvedImage) }
        ?: previewBitmap
        ?: fullscreenBitmap
    val showFullscreenLoading = fullscreenLoading &&
        displayBitmap != null &&
        fullscreenBitmap?.isFullscreenQuality(resolvedImage) != true

    LaunchedEffect(actionMenuVisible, actionMenuOffset) {
        if (!actionMenuVisible && actionMenuOffset != null) {
            delay(180)
            actionMenuOffset = null
        }
    }

    val loadedBitmap = displayBitmap
    if (loadedBitmap == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Color.White)
        }
        return
    }

    MediaLongPressConfiguration {
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
        val imageAspect = remember(loadedBitmap.width, loadedBitmap.height) {
            loadedBitmap.width.toFloat() / loadedBitmap.height.coerceAtLeast(1).toFloat()
        }
        val rawFillScreenScale = remember(containerAspect, imageAspect) {
            maxOf(imageAspect / containerAspect, containerAspect / imageAspect)
                .coerceAtLeast(1f)
        }
        val maxZoomScale = remember(
            loadedBitmap.width,
            loadedBitmap.height,
            resolvedImage.width,
            resolvedImage.height,
            containerWidthPx,
            containerHeightPx,
            imageAspect,
            rawFillScreenScale,
        ) {
            computeFullscreenMaxZoomScale(
                bitmapWidth = loadedBitmap.width,
                bitmapHeight = loadedBitmap.height,
                containerWidthPx = containerWidthPx,
                containerHeightPx = containerHeightPx,
                imageAspect = imageAspect,
                rawFillScreenScale = rawFillScreenScale,
                sourceWidth = resolvedImage.width ?: 0,
                sourceHeight = resolvedImage.height ?: 0,
            )
        }
        val fillScreenScale = remember(rawFillScreenScale, maxZoomScale) {
            rawFillScreenScale.coerceIn(1.35f, maxZoomScale)
        }
        val latestScaleState = rememberUpdatedState(scale)
        val latestPanOffsetXState = rememberUpdatedState(panOffsetX)
        val latestPanOffsetYState = rememberUpdatedState(panOffsetY)
        val latestActionMenuVisibleState = rememberUpdatedState(actionMenuVisible)

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

        fun readDisplayedBounds(): Rect {
            val currentScale = latestScaleState.value
            val layout = layoutFor(currentScale)
            val dismissOffset = dismissTranslationY + dismissSnapAnim.value
            val displayedWidth = layout.fitWidthPx * currentScale
            val displayedHeight = layout.fitHeightPx * currentScale
            val centerX = containerWidthPx / 2f + latestPanOffsetXState.value
            val centerY = containerHeightPx / 2f + latestPanOffsetYState.value + dismissOffset
            return Rect(
                left = centerX - displayedWidth / 2f,
                top = centerY - displayedHeight / 2f,
                right = centerX + displayedWidth / 2f,
                bottom = centerY + displayedHeight / 2f,
            )
        }

        fun dismissFromCurrentBounds() {
            val handoffBounds = readDisplayedBounds()
            dismissTranslationY = 0f
            panOffsetX = 0f
            panOffsetY = 0f
            scale = 1f
            scope.launch { dismissSnapAnim.snapTo(0f) }
            onDismissFromBounds(handoffBounds)
        }

        DisposableEffect(onDismissBoundsProvider) {
            onDismissBoundsProvider?.invoke(::readDisplayedBounds)
            onDispose { }
        }

        val pageMenuBackdrop = rememberLayerBackdrop()
        Box(Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .layerBackdrop(pageMenuBackdrop),
            ) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(Color.Black),
                )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(image.largeUrl, containerWidthPx, containerHeightPx, imageAspect, maxZoomScale) {
                    awaitEachGesture {
                        var velocityTracker = VelocityTracker()
                        val touchSlop = viewConfiguration.touchSlop
                        var panningZoomed = false
                        var pinching = false
                        var postPinchPanOrigin: Offset? = null
                        var dismissing = false
                        var lockedAxis: FullscreenDragAxis? = null
                        var horizontalPageDragAccum = 0f
                        val boxCenterX = containerWidthPx / 2f
                        val boxCenterY = containerHeightPx / 2f
                        val down = awaitFirstDown(requireUnconsumed = false)
                        panInertiaJob?.cancel()
                        zoomAnimationJob?.cancel()
                        zoomAnimationJob = null
                        var gestureScale = latestScaleState.value
                        var gesturePanOffsetX = latestPanOffsetXState.value
                        var gesturePanOffsetY = latestPanOffsetYState.value
                        var lastDistance = 0f
                        while (true) {
                            val event = awaitPointerEvent()
                            val pressed = event.changes.filter { it.pressed }
                            if (pressed.isEmpty()) break

                            if (pressed.size >= 2) {
                                if (!pinching) {
                                    pinching = true
                                    postPinchPanOrigin = null
                                    panningZoomed = false
                                    velocityTracker = VelocityTracker()
                                }
                                dismissTranslationY = 0f
                                panInertiaJob?.cancel()
                                val first = pressed[0].position
                                val second = pressed[1].position
                                val centroid = Offset((first.x + second.x) / 2f, (first.y + second.y) / 2f)
                                val distance = hypot(first.x - second.x, first.y - second.y)
                                if (lastDistance > 0f) {
                                    val oldScale = gestureScale.coerceAtLeast(0.01f)
                                    val newScale = (oldScale * (distance / lastDistance)).coerceIn(1f, maxZoomScale)
                                    gesturePanOffsetX = centroid.x - boxCenterX -
                                        (centroid.x - boxCenterX - gesturePanOffsetX) * (newScale / oldScale)
                                    gesturePanOffsetY = centroid.y - boxCenterY -
                                        (centroid.y - boxCenterY - gesturePanOffsetY) * (newScale / oldScale)
                                    val layout = layoutFor(newScale)
                                    val (cx, cy) = clampPan(gesturePanOffsetX, gesturePanOffsetY, layout)
                                    gesturePanOffsetX = cx
                                    gesturePanOffsetY = cy
                                    panOffsetX = gesturePanOffsetX
                                    panOffsetY = gesturePanOffsetY
                                    scale = newScale
                                    gestureScale = newScale
                                    updatePagerScrollBlock(newScale, gesturePanOffsetX)
                                }
                                lastDistance = distance
                                event.changes.forEach { it.consume() }
                            } else {
                                lastDistance = 0f
                                if (pinching) {
                                    pinching = false
                                    postPinchPanOrigin = pressed.first().position
                                    velocityTracker = VelocityTracker()
                                    lockedAxis = null
                                    event.changes.forEach { it.consume() }
                                    continue
                                }
                                val change = pressed.first()
                                val panOrigin = postPinchPanOrigin ?: down.position
                                val totalDrag = change.position - panOrigin
                                val delta = change.position - change.previousPosition
                                gestureScale = latestScaleState.value
                                gesturePanOffsetX = latestPanOffsetXState.value
                                gesturePanOffsetY = latestPanOffsetYState.value
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
                                    gesturePanOffsetY,
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

                                updatePagerScrollBlock(gestureScale, gesturePanOffsetX)

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
                                    val atLeftEdge = layout.maxPanX <= 1f || gesturePanOffsetX <= -layout.maxPanX + 4f
                                    val atRightEdge = layout.maxPanX <= 1f || gesturePanOffsetX >= layout.maxPanX - 4f
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
                                    val dragProgress = (abs(dismissTranslationY) / (containerHeightPx * 0.48f))
                                        .coerceIn(0f, 1f)
                                    onDragDismissProgress(dragProgress)
                                    velocityTracker.addPosition(change.uptimeMillis, change.position)
                                    change.consume()
                                    continue
                                }

                                if (gestureScale > 1.01f) {
                                    if (!panningZoomed && hypot(totalDrag.x, totalDrag.y) > touchSlop) {
                                        panningZoomed = true
                                    }
                                    if (panningZoomed) {
                                        velocityTracker.addPosition(change.uptimeMillis, change.position)
                                        val (cx, cy) = clampPan(
                                            gesturePanOffsetX + delta.x,
                                            gesturePanOffsetY + delta.y,
                                            layout,
                                        )
                                        gesturePanOffsetX = cx
                                        gesturePanOffsetY = cy
                                        panOffsetX = gesturePanOffsetX
                                        panOffsetY = gesturePanOffsetY
                                        updatePagerScrollBlock(gestureScale, gesturePanOffsetX)
                                        change.consume()
                                    }
                                } else {
                                    onBlockPagerScroll(false)
                                }
                            }
                        }

                        gestureScale = latestScaleState.value
                        val dismissDistance = dismissTranslationY + dismissSnapAnim.value
                        if (dismissing || dismissDistance != 0f) {
                            val velocity = velocityTracker.calculateVelocity()
                            if (
                                abs(dismissDistance) > dismissReleaseThresholdPx ||
                                abs(velocity.y) > 850f
                            ) {
                                val handoffScale = latestScaleState.value
                                val handoffLayout = layoutFor(handoffScale)
                                val handoffProgress = (abs(dismissDistance) / (containerHeightPx * 0.48f))
                                    .coerceIn(0f, 1f)
                                val handoffDragScale =
                                    1f - 0.16f * FastOutSlowInEasing.transform(handoffProgress)
                                val handoffWidth = handoffLayout.fitWidthPx * handoffScale * handoffDragScale
                                val handoffHeight = handoffLayout.fitHeightPx * handoffScale * handoffDragScale
                                val handoffCenterX = containerWidthPx / 2f + latestPanOffsetXState.value
                                val handoffCenterY = containerHeightPx / 2f + latestPanOffsetYState.value +
                                    dismissDistance
                                val handoffBounds = Rect(
                                    left = handoffCenterX - handoffWidth / 2f,
                                    top = handoffCenterY - handoffHeight / 2f,
                                    right = handoffCenterX + handoffWidth / 2f,
                                    bottom = handoffCenterY + handoffHeight / 2f,
                                )
                                dismissTranslationY = 0f
                                panOffsetX = 0f
                                panOffsetY = 0f
                                scale = 1f
                                onDismissFromBounds(handoffBounds)
                            } else {
                                scope.launch {
                                    dismissSnapAnim.snapTo(dismissTranslationY)
                                    dismissTranslationY = 0f
                                    dismissSnapAnim.animateTo(0f, tween(220))
                                    onDragDismissProgress(0f)
                                }
                            }
                            return@awaitEachGesture
                        }

                        gestureScale = latestScaleState.value
                        gesturePanOffsetX = latestPanOffsetXState.value
                        gesturePanOffsetY = latestPanOffsetYState.value
                        updatePagerScrollBlock(gestureScale, gesturePanOffsetX)
                        if (panningZoomed && gestureScale > 1.01f) {
                            val velocity = velocityTracker.calculateVelocity()
                            val layout = layoutFor(gestureScale)
                            val decaySpec = exponentialDecay<Float>(
                                frictionMultiplier = 0.42f,
                                absVelocityThreshold = 0.5f,
                            )
                            val startX = gesturePanOffsetX
                            val startY = gesturePanOffsetY
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
                                updatePagerScrollBlock(gestureScale, latestPanOffsetXState.value)
                            }
                        }
                    }
                }
                .pointerInput(image.largeUrl, fillScreenScale) {
                    detectTapGestures(
                        onTap = {
                            if (latestActionMenuVisibleState.value) {
                                actionMenuVisible = false
                            } else {
                                dismissFromCurrentBounds()
                            }
                        },
                        onDoubleTap = {
                            actionMenuVisible = false
                            panInertiaJob?.cancel()
                            zoomAnimationJob?.cancel()
                            dismissTranslationY = 0f
                            scope.launch { dismissSnapAnim.snapTo(0f) }
                            onBlockPagerScroll(false)
                            val currentScale = latestScaleState.value
                            if (currentScale > 1f) {
                                zoomAnimationJob = scope.launch {
                                    animate(
                                        initialValue = currentScale,
                                        targetValue = 1f,
                                        animationSpec = tween(180),
                                    ) { value, _ -> scale = value }
                                    zoomAnimationJob = null
                                }
                                panOffsetX = 0f
                                panOffsetY = 0f
                            } else {
                                zoomAnimationJob = scope.launch {
                                    animate(
                                        initialValue = currentScale,
                                        targetValue = fillScreenScale,
                                        animationSpec = tween(180),
                                    ) { value, _ -> scale = value }
                                    zoomAnimationJob = null
                                }
                            }
                        },
                        onLongPress = { offset ->
                            if (resolvedImage.isLivePhoto && isInsideDisplayedImage(offset)) {
                                livePlaying = true
                            } else {
                                actionMenuOffset = offset
                                actionMenuVisible = true
                            }
                        },
                    )
                },
            contentAlignment = Alignment.Center,
        ) {
            val dismissOffsetY = dismissTranslationY + dismissSnapAnim.value
            val dismissProgress = (abs(dismissOffsetY) / (containerHeightPx * 0.48f)).coerceIn(0f, 1f)
            val dragScale = 1f - 0.16f * FastOutSlowInEasing.transform(dismissProgress)
            val dismissAlpha = (1f - dismissProgress * 0.18f).coerceIn(0.82f, 1f)
            val currentScale = scale
            val imageLayout = layoutFor(currentScale)
            val displayedWidth = imageLayout.fitWidthPx * currentScale
            val displayedHeight = imageLayout.fitHeightPx * currentScale
            val imageCenterX = containerWidthPx / 2f + panOffsetX
            val imageCenterY = containerHeightPx / 2f + panOffsetY + dismissOffsetY
            val iconSizePx = with(density) { 16.dp.toPx() }
            val iconMarginPx = with(density) { 8.dp.toPx() }
            val imageLeft = imageCenterX - displayedWidth / 2f
            val imageTop = imageCenterY - displayedHeight / 2f
            val iconOffsetX = (imageLeft + displayedWidth - iconSizePx - iconMarginPx)
                .coerceIn(iconMarginPx, containerWidthPx - iconSizePx - iconMarginPx)
                .roundToInt()
            val iconOffsetY = (imageTop + displayedHeight - iconSizePx - iconMarginPx)
                .coerceIn(iconMarginPx, containerHeightPx - iconSizePx - iconMarginPx)
                .roundToInt()
            val imageModifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = currentScale * dragScale
                    scaleY = currentScale * dragScale
                    translationX = panOffsetX
                    translationY = panOffsetY + dismissOffsetY
                    alpha = dismissAlpha
                }
            Box(modifier = imageModifier) {
                if (resolvedImage.isGif) {
                    AnimatedRemoteImage(
                        url = resolvedImage.downloadUrls.firstOrNull { it.isNotBlank() }
                            ?: resolvedImage.largeUrl,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit,
                    )
                } else {
                    Image(
                        bitmap = loadedBitmap.asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit,
                    )
                }
                if (resolvedImage.isLivePhoto) {
                    LivePhotoOverlay(
                        image = resolvedImage,
                        stillBitmap = loadedBitmap,
                        modifier = Modifier.fillMaxSize(),
                        isActive = isActive && livePlaying,
                        isInFullscreen = true,
                        onEnded = { livePlaying = false },
                    )
                }
            }
            if (resolvedImage.isLivePhoto && !livePlaying) {
                Icon(
                    painter = painterResource(R.drawable.ic_live_photo),
                    contentDescription = "Live Photo",
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .offset {
                            IntOffset(iconOffsetX, iconOffsetY)
                        }
                        .zIndex(8f)
                        .size(16.dp)
                        .clickable(
                            indication = null,
                            interactionSource = remember(resolvedImage.largeUrl) {
                                MutableInteractionSource()
                            },
                            onClick = { livePlaying = true },
                        ),
                    tint = Color.White.copy(alpha = 0.92f),
                )
            }
            if (showFullscreenLoading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 56.dp)
                        .size(22.dp),
                    color = Color.White.copy(alpha = 0.85f),
                    strokeWidth = 2.dp,
                )
            }
        }
        }
        }

        actionMenuOffset?.let { offset ->
            FullscreenImageActionMenu(
                image = resolvedImage,
                allImages = remember(allImages, imageOwner, relatedPosts) {
                    allImages.map { pageImage ->
                        resolveFeedImageForViewer(pageImage, imageOwner, relatedPosts)
                    }
                },
                pressOffset = offset,
                visible = actionMenuVisible,
                screenWidthPx = containerWidthPx,
                screenHeightPx = containerHeightPx,
                backdrop = pageMenuBackdrop,
                statusItem = imageOwner,
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
    backdrop: Backdrop? = null,
    statusItem: FeedItem? = null,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val onMessage = LocalUiMessenger.current
    val saveHint = LocalImageSaveHint.current
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    var saving by remember { mutableStateOf(false) }
    val images = allImages.ifEmpty { listOf(image) }
    val showSaveAll = images.size > 1
    val estimatedMenuHeight = if (showSaveAll) ActionMenuThreeRowHeight else ActionMenuTwoRowHeight
    val margin = 14.dp
    val gap = 10.dp
    val menuLabels = buildList {
        add("\u4fdd\u5b58")
        if (showSaveAll) add("\u4fdd\u5b58\u5168\u90e8")
        add("\u5206\u4eab")
    }
    val maxMenuWidth = with(density) { screenWidthPx.toDp() } - margin * 2
    val menuWidth = rememberActionMenuWidth(menuLabels, maxMenuWidth)
    val menuWidthPx = with(density) { menuWidth.toPx() }
    val menuHeightPx = with(density) { estimatedMenuHeight.toPx() }
    val menuPlacement = calculateActionMenuOffsetFromPointPx(
        pressOffset = pressOffset,
        screenWidthPx = screenWidthPx,
        screenHeightPx = screenHeightPx,
        menuWidthPx = menuWidthPx,
        menuHeightPx = menuHeightPx,
        marginPx = with(density) { margin.toPx() },
        gapPx = with(density) { gap.toPx() },
    )
    val originInMenu = computeActionMenuOriginInMenu(
        anchorInRoot = pressOffset,
        menuOffset = menuPlacement.offset,
        menuWidthPx = menuWidthPx,
        menuHeightPx = menuHeightPx,
    )
    ActionMenuReveal(
        visible = visible,
        menuWidth = menuWidth,
        menuHeight = estimatedMenuHeight,
        originInMenu = originInMenu,
        modifier = Modifier
            .align(Alignment.TopStart)
            .offset { menuPlacement.offset }
            .width(menuWidth)
            .height(estimatedMenuHeight)
            .zIndex(20f),
    ) {
        ImageActionFrostedCard(
            modifier = Modifier.fillMaxSize(),
            backdrop = backdrop,
        ) {
            ImageActionRow(
                label = "保存",
                enabled = !saving,
                onClick = {
                    saving = true
                    scope.launch {
                        if (saveHint.saveOne(context, image, statusItem)) {
                            onDismiss()
                        }
                        saving = false
                    }
                },
            )
            if (showSaveAll) {
                ImageActionRow(
                    label = "保存全部",
                    enabled = !saving,
                    onClick = {
                        saving = true
                        scope.launch {
                            val result = saveHint.saveAll(context, images, statusItem)
                            if (result.saved > 0) {
                                onDismiss()
                            }
                            saving = false
                        }
                    },
                )
            }
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
    stillBitmap: Bitmap? = null,
    modifier: Modifier = Modifier,
    isActive: Boolean = true,
    isInFullscreen: Boolean = false,
    onEnded: () -> Unit,
) {
    val context = LocalContext.current
    val videoCoordinator = LocalVideoPlaybackCoordinator.current
    val videoUrl = image.livePhotoVideoUrl?.takeIf { it.isNotBlank() } ?: return
    val playbackKey = remember(image.id, image.largeUrl, videoUrl) {
        "${image.id}:$videoUrl"
    }
    val mirrorMode = rememberLivePhotoMirrorCorrection(image, stillBitmap)
    var videoVisible by remember(playbackKey) { mutableStateOf(false) }
    val player = remember(playbackKey) {
        androidx.media3.exoplayer.ExoPlayer.Builder(context).build().apply {
            setMediaSource(buildVideoMediaSource(context, videoUrl))
            repeatMode = androidx.media3.common.Player.REPEAT_MODE_OFF
            volume = 0f
            playWhenReady = false
            prepare()
        }
    }

    LaunchedEffect(isActive, playbackKey) {
        if (isActive) {
            videoCoordinator.pausePeek()
            if (player.playbackState == androidx.media3.common.Player.STATE_ENDED) {
                player.seekTo(0)
            }
            player.volume = 1f
            player.playWhenReady = true
        } else {
            player.playWhenReady = false
            player.pause()
            player.volume = 0f
            videoVisible = false
        }
    }

    DisposableEffect(player, playbackKey, isInFullscreen) {
        val pauseHandler = { player.pause() }
        videoCoordinator.registerPauseHandler(playbackKey, isFullscreen = isInFullscreen, pauseHandler)
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
            videoCoordinator.unregisterPauseHandler(playbackKey, isFullscreen = isInFullscreen)
            player.removeListener(listener)
            player.release()
        }
    }

    AndroidView(
        modifier = modifier.graphicsLayer {
            scaleX = if (mirrorMode == LivePhotoMirrorCorrection.MirrorVideo) -1f else 1f
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
        update = { playerView ->
            playerView.player = player
        },
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

/** 下拉恢复：视频上界需回到阈值下方的滞回距离（避免贴边抖动）。 */
private val CardAutoFloatReturnHysteresis = 6.dp

@Composable
private fun InlineVideoPlayer(
    media: FeedMedia,
    playbackOwnerId: String,
    onClick: () -> Unit = {},
    onFullscreenRequest: () -> Unit = onClick,
    onDetailClick: (() -> Unit)? = null,
    autoFloatingOnScrollAway: Boolean = false,
    modifier: Modifier = Modifier,
    gridCell: Boolean = false,
) {
    val videoCoordinator = LocalVideoPlaybackCoordinator.current
    val videoPeekController = LocalVideoPeekController.current
    val haptic = LocalHapticFeedback.current
    val playbackKey = remember(media.streamUrl, media.downloadUrl, media.coverUrl, playbackOwnerId) {
        videoPlaybackKey(media, playbackOwnerId)
    }
    val isDetailInlinePlayback = LocalDetailInlineVideoPlayback.current
    val suppressedByDetailOverlay = !isDetailInlinePlayback &&
        videoCoordinator.shouldSuppressInlineForDetailOverlay(playbackKey)
    val inlinePlaying = !suppressedByDetailOverlay &&
        (
            videoCoordinator.isPlaybackKeyActive(playbackKey) ||
                videoCoordinator.isPlaybackKeyHandoffPending(playbackKey) ||
                (isDetailInlinePlayback && videoCoordinator.isDetailHandoffTarget(playbackKey))
            ) &&
        videoCoordinator.fullscreenKey != playbackKey &&
        videoCoordinator.peekPlaybackKey != playbackKey
    var measuredVideoWidth by remember(media.streamUrl) {
        mutableIntStateOf(media.coverWidth ?: 0)
    }
    var measuredVideoHeight by remember(media.streamUrl) {
        mutableIntStateOf(media.coverHeight ?: 0)
    }
    val cardLayout = remember(
        media.streamUrl,
        media.videoOrientation,
        media.coverWidth,
        media.coverHeight,
        measuredVideoWidth,
        measuredVideoHeight,
        gridCell,
    ) {
        if (gridCell) {
            SingleImageFeedLayout(aspectRatio = 1f, widthFraction = 1f)
        } else {
            feedVideoFeedLayout(
                media = media,
                width = measuredVideoWidth,
                height = measuredVideoHeight,
            )
        }
    }
    var actionOpen by remember(media.streamUrl) { mutableStateOf(false) }
    var peekActive by remember(media.streamUrl) { mutableStateOf(false) }
    var pressHoldProgress by remember(media.streamUrl) { mutableFloatStateOf(0f) }
    val anchorHolder = remember(media.streamUrl) { LayoutAnchorHolder() }
    var lastTapUptimeMs by remember(media.streamUrl) { mutableStateOf(0L) }
    var videoBoundsInWindow by remember(media.streamUrl) { mutableStateOf<Rect?>(null) }
    val isAutoScrollFloating = videoCoordinator.isAutoScrollFloating(playbackKey)
    val inlineHandoffPending = videoCoordinator.isPlaybackKeyHandoffPending(playbackKey)
    val suppressPausedCover = actionOpen || peekActive || isAutoScrollFloating || inlineHandoffPending
    // 滚动进浮窗：claimPeek 后仍先保留内联 Surface，主动 stash 后再拆，避免交接竞态黑屏无声。
    val keepInlineSurfaceForPeekHandoff = (isAutoScrollFloating || peekActive) &&
        inlineHandoffPending &&
        !videoCoordinator.hasStashedHandoff(playbackKey)
    val showInlineVideoSurface = media.isStreamPlayable() &&
        (inlinePlaying || keepInlineSurfaceForPeekHandoff)
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val detailVideoViewport = LocalDetailVideoViewport.current
    val returnHysteresisPx = with(density) { CardAutoFloatReturnHysteresis.toPx() }
    val feedListScrollCoordinator = LocalFeedListScrollCoordinator.current
    val gestureScope = rememberCoroutineScope()
    val mediaHaptics = rememberMediaPeekHaptics()
    val holdScale = mediaPeekHoldScale(if (actionOpen) 0f else pressHoldProgress)

    fun resetPeekState() {
        actionOpen = false
        peekActive = false
        pressHoldProgress = 0f
        videoPeekController.resetFingerDragOffset()
    }

    fun openVideoPeek(pressWindowOffset: Offset) {
        val bounds = anchorHolder.boundsInWindow() ?: return
        val wasPlayingBeforePeek = videoCoordinator.activeKey == playbackKey &&
            videoCoordinator.fullscreenKey != playbackKey
        actionOpen = true
        peekActive = true
        pressHoldProgress = 0f
        videoCoordinator.schedulePeekRestartIfAtEnd(playbackKey, videoDurationMs(media))
        videoCoordinator.claimPeekPlayback(playbackKey)
        videoPeekController.open(
            VideoPeekRequest(
                media = media,
                anchorBounds = bounds,
                pressOffset = pressWindowOffset,
                playbackOwnerId = playbackOwnerId,
                resolveAnchorBounds = {
                    resolveVideoAnchorBounds(anchorHolder.coordinates, anchorHolder.boundsInWindow())
                },
                onCancel = {
                    videoCoordinator.cancelFullscreenHandoff(playbackKey)
                    resetPeekState()
                    if (wasPlayingBeforePeek && media.isStreamPlayable()) {
                        videoCoordinator.requestInlinePlayback(playbackKey)
                    }
                },
                onRelease = {},
                onPlaybackEnded = { resetPeekState() },
                onOpenFullscreenBehind = {},
                onEnterFullscreenHandoffComplete = {
                    resetPeekState()
                },
            ),
        )
    }

    fun openInlineFloatingPlayback(pressWindowOffset: Offset, fromAutoScroll: Boolean = false) {
        val bounds = anchorHolder.boundsInWindow() ?: return
        if (fromAutoScroll) {
            videoCoordinator.markAutoScrollFloating(playbackKey)
        }
        actionOpen = true
        peekActive = true
        pressHoldProgress = 0f
        videoCoordinator.beginPeekHandoff(playbackKey)
        if (fromAutoScroll) {
            videoCoordinator.claimPeekPlaybackForScrollFloat(playbackKey)
        } else {
            videoCoordinator.claimPeekPlayback(playbackKey)
        }
        videoPeekController.openFloating(
            VideoPeekRequest(
                media = media,
                anchorBounds = bounds,
                pressOffset = pressWindowOffset,
                playbackOwnerId = playbackOwnerId,
                resolveAnchorBounds = {
                    resolveVideoAnchorBounds(anchorHolder.coordinates, anchorHolder.boundsInWindow())
                },
                // 从当前画面连续缩放到停靠位；播放器实例在两个 Surface 之间交接。
                dockImmediately = false,
                onCancel = {
                    val returningToInline = videoCoordinator.pendingPeekHandoffKey?.let { pending ->
                        videoCoordinator.matchesPlaybackKey(pending, playbackKey)
                    } == true
                    if (!returningToInline) {
                        videoCoordinator.clearAutoScrollFloating(playbackKey)
                        videoCoordinator.cancelPeekHandoff(playbackKey)
                        videoCoordinator.clearInlineHandoffResume(playbackKey)
                    }
                    resetPeekState()
                    if (returningToInline && media.isStreamPlayable()) {
                        videoCoordinator.clearAutoScrollFloating(playbackKey)
                        videoCoordinator.releasePeekPlayback(playbackKey)
                        videoCoordinator.requestInlinePlayback(playbackKey)
                    }
                },
                onRelease = {},
                onPlaybackEnded = {
                    videoCoordinator.clearAutoScrollFloating(playbackKey)
                    resetPeekState()
                },
                onOpenFullscreenBehind = {},
                onEnterFullscreenHandoffComplete = {
                    resetPeekState()
                },
            ),
        )
    }

    fun returnToInlineFromScrollFloating() {
        if (!videoCoordinator.isAutoScrollFloating(playbackKey)) return
        if (videoCoordinator.peekPlaybackKey != playbackKey || !videoPeekController.isFloating) return
        if (videoPeekController.pendingDismiss != null) return
        videoCoordinator.clearAutoScrollFloating(playbackKey)
        videoCoordinator.beginPeekHandoff(playbackKey)
        videoPeekController.cancel()
    }

    LaunchedEffect(
        autoFloatingOnScrollAway,
        isDetailInlinePlayback,
        inlinePlaying,
        videoCoordinator.peekPlaybackKey,
        actionOpen,
        peekActive,
        isAutoScrollFloating,
        videoBoundsInWindow,
        detailVideoViewport.headerBottomPx,
    ) {
        if (!autoFloatingOnScrollAway || !isDetailInlinePlayback) return@LaunchedEffect
        val bounds = videoBoundsInWindow ?: return@LaunchedEffect
        val threshold = detailVideoViewport.headerBottomPx
        if (threshold <= 0f) return@LaunchedEffect

        if (isAutoScrollFloating) {
            if (bounds.top > threshold + returnHysteresisPx) {
                returnToInlineFromScrollFloating()
            }
        } else {
            if (!inlinePlaying || !media.isStreamPlayable()) return@LaunchedEffect
            if (videoCoordinator.peekPlaybackKey == playbackKey) return@LaunchedEffect
            if (actionOpen || peekActive) return@LaunchedEffect
            // 视频画面上边界接触头像/发布时间卡片底边的这一帧即开始交接。
            if (bounds.top > threshold) return@LaunchedEffect
            val center = Offset(
                bounds.left + bounds.width / 2f,
                bounds.top + bounds.height / 2f,
            )
            openInlineFloatingPlayback(center, fromAutoScroll = true)
        }
    }

    fun openInlineFullscreenTransition(pressWindowOffset: Offset) {
        val bounds = anchorHolder.boundsInWindow()
        if (bounds == null || !media.isStreamPlayable()) {
            videoCoordinator.activeKey = null
            onFullscreenRequest()
            return
        }
        actionOpen = true
        peekActive = true
        pressHoldProgress = 0f
        videoCoordinator.schedulePeekRestartIfAtEnd(playbackKey, videoDurationMs(media))
        videoCoordinator.beginFullscreenHandoff(playbackKey)
        videoCoordinator.claimPeekPlayback(playbackKey)
        videoPeekController.open(
            VideoPeekRequest(
                media = media,
                anchorBounds = bounds,
                pressOffset = pressWindowOffset,
                playbackOwnerId = playbackOwnerId,
                resolveAnchorBounds = {
                    resolveVideoAnchorBounds(anchorHolder.coordinates, anchorHolder.boundsInWindow())
                },
                expandFromAnchor = true,
                onCancel = {
                    videoCoordinator.cancelFullscreenHandoff(playbackKey)
                    resetPeekState()
                    videoCoordinator.requestInlinePlayback(playbackKey)
                },
                onRelease = {},
                onPlaybackEnded = { resetPeekState() },
                onOpenFullscreenBehind = {},
                onEnterFullscreenHandoffComplete = {
                    resetPeekState()
                },
            ),
        )
        videoPeekController.enterFullscreen()
    }

    val cornerRadius = if (gridCell) 4.dp else 8.dp
    val playButtonSize = if (gridCell) 44.dp else 65.dp
    val playIconSize = if (gridCell) 36.dp else 60.dp

    Box(modifier = if (gridCell) modifier else Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .then(
                    if (gridCell) {
                        Modifier.fillMaxSize()
                    } else {
                        Modifier
                            .align(Alignment.TopStart)
                            .fillMaxWidth(cardLayout.widthFraction)
                            .aspectRatio(cardLayout.aspectRatio)
                    },
                )
                .zIndex(if (actionOpen || peekActive) 10f else 0f)
                .onGloballyPositioned { coordinates ->
                    anchorHolder.coordinates = coordinates
                    if (autoFloatingOnScrollAway || isAutoScrollFloating) {
                        val nextBounds = coordinates.boundsInWindow()
                        if (videoBoundsInWindow != nextBounds) videoBoundsInWindow = nextBounds
                    }
                }
                .graphicsLayer {
                    scaleX = holdScale
                    scaleY = holdScale
                    alpha = if (actionOpen) 0f else 1f
                }
                .clip(RoundedCornerShape(cornerRadius))
                .background(Color.Black)
                .pointerInput(media.streamUrl, media.type) {
                awaitEachGesture {
                    val bounds = anchorHolder.boundsInWindow() ?: return@awaitEachGesture
                    val down = awaitFirstDown(requireUnconsumed = false)
                    if (cancelFeedListScrollOnTap(feedListScrollCoordinator, gestureScope, down)) {
                        return@awaitEachGesture
                    }
                    var pressResult = MediaLongPressResult.Tap
                    pressResult = awaitMediaLongPress(
                        down = down,
                        viewConfiguration = viewConfiguration,
                        onHoldProgress = { pressHoldProgress = it },
                        onHaptic = mediaHaptics::perform,
                    )

                    if (pressResult != MediaLongPressResult.LongPress) {
                        if (pressResult == MediaLongPressResult.Tap) {
                            val tapUptime = down.uptimeMillis
                            val pressWindowOffset = down.position.toWindowPosition(bounds)
                            if (gridCell && media.isStreamPlayable() && !inlinePlaying) {
                                videoCoordinator.requestInlinePlayback(playbackKey)
                                return@awaitEachGesture
                            }
                            val isDoubleTap = lastTapUptimeMs > 0L &&
                                tapUptime - lastTapUptimeMs <= viewConfiguration.doubleTapTimeoutMillis
                            if (isDoubleTap) {
                                lastTapUptimeMs = 0L
                                if (media.isStreamPlayable()) {
                                    if (!inlinePlaying) {
                                        videoCoordinator.requestInlinePlayback(playbackKey)
                                    }
                                } else {
                                    onClick()
                                }
                            } else {
                                lastTapUptimeMs = tapUptime
                            }
                        }
                        return@awaitEachGesture
                    }

                    if (!media.isStreamPlayable()) {
                        onClick()
                        return@awaitEachGesture
                    }

                    val pressWindowOffset = down.position.toWindowPosition(bounds)
                    down.consume()
                    openVideoPeek(pressWindowOffset)
                    videoPeekController.updateFingerDragOffset(Offset.Zero)

                    val dragResult = awaitVideoPeekDragGesture(
                        down = down,
                        pressWindowOffset = pressWindowOffset,
                        bounds = bounds,
                        videoPeekController = videoPeekController,
                    )
                    if (!dragResult.cancelledByDrag && !dragResult.floatByDrag) {
                        videoPeekController.enterFullscreen()
                    }
                }
            },
    ) {
        if (showInlineVideoSurface) {
            WeiboVideoSurface(
                media = media,
                playbackOwnerId = playbackOwnerId,
                isFullscreen = false,
                videoResizeMode = if (gridCell) {
                    androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                } else {
                    androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_FIT
                },
                onAspectRatio = { width, height ->
                    if (!gridCell && width > 0 && height > 0) {
                        measuredVideoWidth = width
                        measuredVideoHeight = height
                    }
                },
                onFullscreen = {
                    val bounds = anchorHolder.boundsInWindow()
                    val center = bounds?.let {
                        Offset(it.left + it.width / 2f, it.top + it.height / 2f)
                    } ?: Offset.Zero
                    openInlineFullscreenTransition(center)
                },
                onEnterFloatingPlayback = {
                    val bounds = anchorHolder.boundsInWindow()
                    val center = bounds?.let {
                        Offset(it.left + it.width / 2f, it.top + it.height / 2f)
                    } ?: Offset.Zero
                    openInlineFloatingPlayback(center, fromAutoScroll = true)
                },
                autoEnterFloatingOnViewportHidden = false,
                trackViewportPauseOverride = if (autoFloatingOnScrollAway) false else null,
                showPictureInPictureButton = false,
            )
        } else if (!suppressPausedCover) {
            val coverPlayBackdrop = rememberLayerBackdrop()
            Box(
                Modifier
                    .fillMaxSize()
                    .layerBackdrop(coverPlayBackdrop),
            ) {
                RemoteImage(
                    url = media.coverUrl,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
            }
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                TransparentLiquidIconButton(
                    onClick = {
                        if (media.isStreamPlayable()) {
                            videoCoordinator.requestInlinePlayback(playbackKey)
                        } else {
                            onClick()
                        }
                    },
                    backdrop = coverPlayBackdrop,
                    modifier = Modifier.size(playButtonSize),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_video_play),
                        contentDescription = "播放",
                        modifier = Modifier.size(playIconSize),
                        tint = Color.White,
                    )
                }
            }
            if (media.liveBadgeLabel() != null) {
                Text(
                    text = media.liveBadgeLabel().orEmpty(),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 6.dp, end = 10.dp),
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            } else if (media.type == MediaType.Video) {
                VideoPlayHintBadge(
                    durationSeconds = media.durationSeconds,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 6.dp, end = 10.dp),
                )
            }
            if (!gridCell) {
                Text(
                    text = media.title,
                    modifier = Modifier.align(Alignment.BottomStart).padding(12.dp),
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        } else {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color.Black),
            )
        }
        }
        if (onDetailClick != null && !gridCell) {
            Box(
                modifier = Modifier.matchParentSize(),
                contentAlignment = Alignment.TopEnd,
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(1f - cardLayout.widthFraction)
                        .fillMaxHeight()
                        .clickable(
                            indication = null,
                            interactionSource = remember(media.streamUrl) { MutableInteractionSource() },
                            onClick = onDetailClick,
                        ),
                )
            }
        }
    }
}

@Composable
private fun WeiboVideoSurface(
    media: FeedMedia,
    playbackOwnerId: String,
    isFullscreen: Boolean,
    onAspectRatio: (width: Int, height: Int) -> Unit,
    onFullscreen: () -> Unit,
    onEnterPictureInPicture: (() -> Unit)? = null,
    onEnterFloatingPlayback: (() -> Unit)? = null,
    modifier: Modifier = Modifier.fillMaxSize(),
    controlsEnabled: Boolean = true,
    resumePosition: Boolean = true,
    savePositionOnDispose: Boolean = true,
    playbackSpeedOverride: Float? = null,
    onPlaybackEnded: (() -> Unit)? = null,
    videoResizeMode: Int = androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_FIT,
    initialControlsVisible: Boolean = true,
    trackViewportPauseOverride: Boolean? = null,
    autoEnterFloatingOnViewportHidden: Boolean = false,
    showFullscreenButton: Boolean = true,
    showPictureInPictureButton: Boolean = true,
    isPeekPlayback: Boolean = false,
    seamlessOverlayPlayback: Boolean = false,
    onPeekVerticalDismiss: (() -> Unit)? = null,
    onPeekFingerDragOffset: ((Offset) -> Unit)? = null,
    onPeekFingerDragReset: (() -> Unit)? = null,
) {
    val context = LocalContext.current
    val saveHint = LocalImageSaveHint.current
    val scope = rememberCoroutineScope()
    val videoCoordinator = LocalVideoPlaybackCoordinator.current
    val isDetailInlinePlayback = LocalDetailInlineVideoPlayback.current
    val isInlineFeedOrDetail = !isPeekPlayback && !isFullscreen && !seamlessOverlayPlayback
    val isLiveBroadcast = media.isLiveBroadcast()
    val isLiveReplay = media.isLiveReplay()
    val hideProgressControls = isLiveBroadcast
    val playbackUrl = media.resolvedPlaybackUrl().orEmpty()
    val isPlaybackUnavailable = media.type == MediaType.Live && !media.isLivePlayable()
    val effectiveResumePosition = resumePosition && !isLiveBroadcast
    val effectiveSavePosition = savePositionOnDispose && !isLiveBroadcast
    val playbackKey = remember(
        playbackOwnerId,
        media.streamUrl,
        media.replayUrl,
        media.liveStatus,
        media.downloadUrl,
        media.coverUrl,
    ) {
        videoPlaybackKey(media, playbackOwnerId)
    }
    val videoCandidates = remember(media.streamUrl, media.replayUrl, media.liveStatus, media.downloadUrl, media.type) {
        when {
            isLiveReplay -> listOfNotNull(
                media.replayUrl?.takeIf { it.isNotBlank() },
                media.streamUrl.takeIf { it.isNotBlank() },
            )
            isLiveBroadcast -> listOfNotNull(media.streamUrl.takeIf { it.isNotBlank() })
            media.type == MediaType.Live -> emptyList()
            else -> listOfNotNull(media.streamUrl, media.downloadUrl)
        }.flatMap(::videoUrlCandidates).distinct()
    }
    val fullscreenSafePadding = WindowInsets.safeDrawing.asPaddingValues()
    val isDevicePortrait = LocalConfiguration.current.orientation == Configuration.ORIENTATION_PORTRAIT
    val fullscreenTopInset = if (isFullscreen && !isDevicePortrait) {
        fullscreenSafePadding.calculateTopPadding()
    } else {
        0.dp
    }
    val fullscreenBottomInset = if (isFullscreen && !isDevicePortrait) {
        fullscreenSafePadding.calculateBottomPadding()
    } else {
        0.dp
    }

    if (isPlaybackUnavailable || playbackUrl.isBlank()) {
        val videoControlBackdrop = rememberLayerBackdrop()
        Box(
            modifier = modifier.background(Color.Black),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                Modifier
                    .fillMaxSize()
                    .layerBackdrop(videoControlBackdrop),
            ) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(Color.Black),
                )
                RemoteImage(
                    url = media.coverUrl,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.35f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = media.liveBadgeLabel() ?: "直播暂不可播放",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
            if (controlsEnabled && !isFullscreen) {
                GlassTextButton(
                    text = "全屏",
                    backdrop = videoControlBackdrop,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(10.dp)
                        .width(54.dp)
                        .height(28.dp),
                    onClick = onFullscreen,
                )
            }
        }
        return
    }

    var videoIndex by remember(videoCandidates) { mutableStateOf(0) }
    val videoUrl = videoCandidates.getOrElse(videoIndex) { playbackUrl }
    var playbackError by remember(videoUrl) { mutableStateOf<String?>(null) }
    var isBuffering by remember(videoUrl) { mutableStateOf(true) }
    var positionMs by remember(videoUrl) { mutableStateOf(0L) }
    var durationMs by remember(videoUrl) { mutableStateOf(0L) }
    var isPlaying by remember(videoUrl) { mutableStateOf(true) }
    var selectedSpeed by remember(videoUrl) { mutableStateOf(1f) }
    var displayedSpeed by remember(videoUrl) { mutableStateOf(1f) }
    var controlsVisible by remember(videoUrl, initialControlsVisible) {
        mutableStateOf(initialControlsVisible)
    }
    var controlsHideSignal by remember(videoUrl) { mutableIntStateOf(0) }
    var isScrubbing by remember(videoUrl) { mutableStateOf(false) }
    var isProgressBarScrubbing by remember(videoUrl) { mutableStateOf(false) }
    var downloading by remember(videoUrl) { mutableStateOf(false) }
    var aspectRatio by remember(videoUrl) {
        mutableFloatStateOf(feedVideoDisplayAspectRatio(media))
    }
    var forcedOrientation by remember(videoUrl) { mutableStateOf(ForcedVideoOrientation.None) }
    val isPortraitVideo = isPortraitFeedVideo(aspectRatio, media)
    val isLandscapeVideo = !isPortraitVideo
    val effectiveResizeMode = videoResizeMode
    LaunchedEffect(isFullscreen, isLandscapeVideo) {
        forcedOrientation = if (isFullscreen && isLandscapeVideo) {
            ForcedVideoOrientation.Landscape
        } else {
            ForcedVideoOrientation.None
        }
    }
    val showLandscapeToggle = false
    val showPortraitToggle = false
    val fullscreenPrimaryControlTop = if (isDevicePortrait) {
        18.dp
    } else {
        fullscreenTopInset + VideoFullscreenTopControlInset
    }
    val fullscreenFloatingButtonTop = fullscreenPrimaryControlTop
    val fullscreenSecondaryButtonEnd = if (isFullscreen && onEnterFloatingPlayback != null) {
        VideoFullscreenHorizontalControlInset + 62.dp
    } else {
        VideoFullscreenHorizontalControlInset
    }
    FullscreenForcedOrientationEffect(
        orientation = forcedOrientation,
        enabled = isFullscreen,
    )
    val trackViewportPause = trackViewportPauseOverride ?: (
        !isFullscreen &&
            controlsEnabled &&
            !videoCoordinator.shouldResumeInlineHandoff(playbackKey) &&
            !(isDetailInlinePlayback && videoCoordinator.detailHandoffActive)
        )
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

    LaunchedEffect(isPlaying, isBuffering, playbackError, controlsHideSignal, isProgressBarScrubbing) {
        if (!isPlaying || isBuffering || playbackError != null) {
            controlsVisible = true
            return@LaunchedEffect
        }
        if (isProgressBarScrubbing) {
            controlsVisible = true
            return@LaunchedEffect
        }
        delay(2_000)
        controlsVisible = false
    }

    val playerCache = remember { mutableMapOf<String, androidx.media3.exoplayer.ExoPlayer>() }
    var resumeAfterSurfaceAttach by remember(playbackKey, videoUrl) { mutableStateOf(false) }

    fun configureExistingPlayer(
        target: androidx.media3.exoplayer.ExoPlayer,
        deferPlaybackUntilSurface: Boolean = false,
    ) {
        if (videoCoordinator.consumePeekRestartFromBeginning(playbackKey)) {
            target.seekTo(0)
            videoCoordinator.clearPosition(playbackKey)
        } else if (target.playbackState == androidx.media3.common.Player.STATE_ENDED &&
            playbackSpeedOverride != null
        ) {
            target.seekTo(0)
            videoCoordinator.clearPosition(playbackKey)
        } else if (effectiveResumePosition) {
            val resumeAt = videoCoordinator.resolvePosition(playbackKey)?.takeIf { it > 0L }
            if (resumeAt != null && target.currentPosition.coerceAtLeast(0L) < 500L) {
                target.seekTo(resumeAt)
            }
        }
        if (playbackSpeedOverride != null) {
            target.setPlaybackSpeed(playbackSpeedOverride)
        } else {
            target.setPlaybackSpeed(1f)
        }
        if (deferPlaybackUntilSurface) {
            resumeAfterSurfaceAttach = true
            target.playWhenReady = false
            target.pause()
        } else {
            target.playWhenReady = true
            target.play()
        }
    }

    fun createFreshPlayer(
        deferPlaybackUntilSurface: Boolean = false,
    ): androidx.media3.exoplayer.ExoPlayer =
        androidx.media3.exoplayer.ExoPlayer.Builder(context).build().apply {
            playerCache[videoUrl] = this
            setMediaSource(
                buildVideoMediaSource(
                    context = context,
                    url = videoUrl,
                    useCache = !isLiveBroadcast && !(isLiveReplay && videoUrl.contains("m3u8", ignoreCase = true)),
                    omitOrigin = isLiveBroadcast || videoUrl.contains("m3u8", ignoreCase = true),
                ),
            )
            prepare()
            if (videoCoordinator.consumePeekRestartFromBeginning(playbackKey)) {
                seekTo(0)
                videoCoordinator.clearPosition(playbackKey)
            } else if (effectiveResumePosition) {
                videoCoordinator.resolvePosition(playbackKey)?.takeIf { it > 0L }?.let { seekTo(it) }
            }
            playbackSpeedOverride?.let { setPlaybackSpeed(it) }
            if (deferPlaybackUntilSurface) {
                resumeAfterSurfaceAttach = true
                playWhenReady = false
            } else {
                playWhenReady = true
            }
        }

    var inlinePlayer by remember(playbackKey, videoUrl) {
        mutableStateOf<androidx.media3.exoplayer.ExoPlayer?>(null)
    }
    var inlineWaitingForHandoff by remember(playbackKey) { mutableStateOf(false) }

    if (isInlineFeedOrDetail) {
        LaunchedEffect(
            playbackKey,
            videoUrl,
            isDetailInlinePlayback,
            videoCoordinator.pendingPeekHandoffKey,
            videoCoordinator.detailHandoffActive,
            videoCoordinator.activeKey,
        ) {
            if (inlinePlayer != null) {
                if (videoCoordinator.isPlaybackKeyActive(playbackKey) &&
                    !inlinePlayer!!.isPlaying &&
                    inlinePlayer!!.playbackState != androidx.media3.common.Player.STATE_BUFFERING &&
                    !resumeAfterSurfaceAttach
                ) {
                    configureExistingPlayer(
                        inlinePlayer!!,
                        deferPlaybackUntilSurface = isDetailInlinePlayback,
                    )
                }
                return@LaunchedEffect
            }

            suspend fun claimSharedPlayer(): androidx.media3.exoplayer.ExoPlayer? =
                videoCoordinator.adoptSharedPlayer(playbackKey)

            claimSharedPlayer()?.let { adopted ->
                configureExistingPlayer(
                    adopted,
                    deferPlaybackUntilSurface = isDetailInlinePlayback,
                )
                inlinePlayer = adopted
                inlineWaitingForHandoff = false
                if (videoCoordinator.isPlaybackKeyActive(playbackKey)) {
                    videoCoordinator.requestInlinePlayback(playbackKey)
                }
                return@LaunchedEffect
            }

            val awaitingInlineHandoff = videoCoordinator.isPlaybackKeyHandoffPending(playbackKey) ||
                (
                    isDetailInlinePlayback &&
                        videoCoordinator.isDetailHandoffTarget(playbackKey)
                    )
            if (awaitingInlineHandoff) {
                inlineWaitingForHandoff = true
                val deadline = android.os.SystemClock.uptimeMillis() + 2_000L
                while (android.os.SystemClock.uptimeMillis() < deadline) {
                    claimSharedPlayer()?.let { adopted ->
                        configureExistingPlayer(
                            adopted,
                            deferPlaybackUntilSurface = isDetailInlinePlayback,
                        )
                        inlinePlayer = adopted
                        inlineWaitingForHandoff = false
                        if (videoCoordinator.isPlaybackKeyActive(playbackKey)) {
                            videoCoordinator.requestInlinePlayback(playbackKey)
                        }
                        return@LaunchedEffect
                    }
                    delay(16)
                }
                inlineWaitingForHandoff = false
                return@LaunchedEffect
            }

            val deferUntilSurface = isDetailInlinePlayback
            val cached = playerCache[videoUrl]?.takeUnless {
                it.playbackState == androidx.media3.common.Player.STATE_IDLE &&
                    it.playerError != null
            }
            val resolved = cached
                ?: createFreshPlayer(deferPlaybackUntilSurface = deferUntilSurface)
                    .also { playerCache[videoUrl] = it }
            configureExistingPlayer(
                resolved,
                deferPlaybackUntilSurface = deferUntilSurface,
            )
            inlinePlayer = resolved
            videoCoordinator.registerSharedPlayer(playbackKey, resolved)
            if (videoCoordinator.isPlaybackKeyActive(playbackKey)) {
                videoCoordinator.requestInlinePlayback(playbackKey)
            }
        }
    }

    val awaitingFullscreenHandoff = !seamlessOverlayPlayback &&
        videoCoordinator.isFullscreenHandoffPending(playbackKey) &&
        (isFullscreen || isPeekPlayback)
    val awaitingPeekHandoffSource = !isInlineFeedOrDetail &&
        !seamlessOverlayPlayback &&
        !isFullscreen && !isPeekPlayback &&
        videoCoordinator.isPlaybackKeyHandoffPending(playbackKey)
    val awaitingPeekHandoffConsume = isPeekPlayback &&
        videoCoordinator.isPlaybackKeyHandoffPending(playbackKey)

    fun resolveOverlayPlayer(): androidx.media3.exoplayer.ExoPlayer? {
        // Overlay 交接只能领取 source 已明确 stash 的实例。若直接 adopt shared，
        // 会与 source 的主动 stash 竞态：浮窗刚挂上 Surface 又被 source 拆掉并暂停。
        val adopted = if (awaitingFullscreenHandoff || awaitingPeekHandoffConsume) {
            videoCoordinator.consumeHandoffPlayer(playbackKey)
        } else {
            videoCoordinator.adoptSharedPlayer(playbackKey)
        }
        adopted?.let { handoff ->
            configureExistingPlayer(handoff, deferPlaybackUntilSurface = true)
            return handoff
        }
        playerCache[videoUrl]?.let { cached ->
            configureExistingPlayer(cached)
            return cached
        }
        return createFreshPlayer()
    }

    var handoffPlayerResolved by remember(playbackKey, videoUrl) { mutableStateOf(false) }
    val immediatePlayer = if (isInlineFeedOrDetail) {
        null
    } else {
        remember(
            playbackKey,
            videoUrl,
            awaitingFullscreenHandoff,
            awaitingPeekHandoffSource,
            handoffPlayerResolved,
        ) {
            if (
                handoffPlayerResolved ||
                awaitingFullscreenHandoff ||
                awaitingPeekHandoffSource
            ) {
                null
            } else if (awaitingPeekHandoffConsume) {
                null
            } else {
                resolveOverlayPlayer()
            }
        }
    }
    var deferredPlayer by remember(playbackKey, videoUrl) {
        mutableStateOf<androidx.media3.exoplayer.ExoPlayer?>(null)
    }

    if (!isInlineFeedOrDetail) {
        LaunchedEffect(
            awaitingFullscreenHandoff,
            awaitingPeekHandoffSource,
            awaitingPeekHandoffConsume,
            playbackKey,
            videoUrl,
        ) {
            if (!awaitingFullscreenHandoff && !awaitingPeekHandoffSource && !awaitingPeekHandoffConsume) {
                return@LaunchedEffect
            }
            val deadline = android.os.SystemClock.uptimeMillis() + 1_200L
            while (android.os.SystemClock.uptimeMillis() < deadline) {
                videoCoordinator.consumeHandoffPlayer(playbackKey)?.let { handoff ->
                    configureExistingPlayer(handoff, deferPlaybackUntilSurface = true)
                    deferredPlayer = handoff
                    handoffPlayerResolved = true
                    if (!isPeekPlayback && !isFullscreen) {
                        videoCoordinator.requestInlinePlayback(playbackKey)
                    }
                    return@LaunchedEffect
                }
                if (
                    !videoCoordinator.isFullscreenHandoffPending(playbackKey) &&
                    !videoCoordinator.isPlaybackKeyHandoffPending(playbackKey)
                ) {
                    break
                }
                delay(16)
            }
            if (videoCoordinator.isPlaybackKeyHandoffPending(playbackKey) ||
                videoCoordinator.hasStashedHandoff(playbackKey)
            ) {
                videoCoordinator.consumeHandoffPlayer(playbackKey)?.let { handoff ->
                    configureExistingPlayer(handoff, deferPlaybackUntilSurface = true)
                    deferredPlayer = handoff
                    handoffPlayerResolved = true
                    if (!isPeekPlayback && !isFullscreen) {
                        videoCoordinator.requestInlinePlayback(playbackKey)
                    }
                    return@LaunchedEffect
                }
            }
            val resolved = resolveOverlayPlayer()
            deferredPlayer = resolved
            handoffPlayerResolved = true
            if (!isPeekPlayback && !isFullscreen) {
                videoCoordinator.requestInlinePlayback(playbackKey)
            }
        }
    }

    fun resumeInlineHandoffPlaybackIfNeeded(target: androidx.media3.exoplayer.ExoPlayer) {
        if (!videoCoordinator.shouldResumeInlineHandoff(playbackKey)) return
        if (!videoCoordinator.isPlaybackKeyActive(playbackKey)) return
        if (resumeAfterSurfaceAttach) return
        configureExistingPlayer(target)
        if (!isDetailInlinePlayback) {
            videoCoordinator.clearInlineHandoffResume(playbackKey)
        }
    }

    LaunchedEffect(videoCoordinator.detailOverlayOpen) {
        if (!isInlineFeedOrDetail) return@LaunchedEffect
        if (!videoCoordinator.detailOverlayOpen && inlinePlayer != null) {
            // no-op: keep inline player alive for return navigation
        }
    }

    val player = when {
        isInlineFeedOrDetail -> inlinePlayer
        else -> immediatePlayer ?: deferredPlayer
    }
    val playerViewHolder = remember { object { var view: androidx.media3.ui.PlayerView? = null } }
    var stashedForHandoff by remember(playbackKey, videoUrl) { mutableStateOf(false) }
    val stashedForHandoffState = rememberUpdatedState(stashedForHandoff)
    var transitionFrame by remember(playbackKey) {
        mutableStateOf(videoCoordinator.consumeTransitionFrame(playbackKey))
    }

    if (!isInlineFeedOrDetail) {
        LaunchedEffect(
            player,
            playbackKey,
            isFullscreen,
            isPeekPlayback,
            videoCoordinator.proactiveStashSignalKey,
            resumeAfterSurfaceAttach,
        ) {
            val target = player ?: return@LaunchedEffect
            if (isFullscreen || isPeekPlayback || stashedForHandoff || resumeAfterSurfaceAttach) {
                return@LaunchedEffect
            }
            if (!videoCoordinator.isProactiveStashRequested(playbackKey)) return@LaunchedEffect
            videoCoordinator.clearProactiveStashRequest(playbackKey)
            playerViewHolder.view?.player = null
            videoCoordinator.stashHandoffPlayer(playbackKey, target)
            stashedForHandoff = true
        }
    } else {
        // 详情内联 → 浮窗：在 Surface 仍挂载时主动交出，避免 claimPeek 后立刻拆掉造成交接失败。
        LaunchedEffect(
            player,
            playbackKey,
            videoCoordinator.pendingPeekHandoffKey,
            videoCoordinator.detailHandoffActive,
        ) {
            val target = player ?: return@LaunchedEffect
            if (stashedForHandoff) return@LaunchedEffect
            if (!videoCoordinator.isPlaybackKeyHandoffPending(playbackKey)) return@LaunchedEffect
            // 首页进详情的交接不要从详情内联 stash。
            if (isDetailInlinePlayback && videoCoordinator.detailHandoffActive) return@LaunchedEffect
            val textureView = playerViewHolder.view?.videoSurfaceView as? android.view.TextureView
            textureView?.bitmap?.takeIf { it.width > 0 && it.height > 0 }?.let { bitmap ->
                videoCoordinator.storeTransitionFrame(playbackKey, bitmap)
            }
            videoCoordinator.unregisterPauseHandler(playbackKey, isFullscreen = false)
            playerViewHolder.view?.player = null
            videoCoordinator.markPlayerSurfaceUnbound(target)
            videoCoordinator.stashHandoffPlayer(playbackKey, target)
            stashedForHandoff = true
        }
    }

    fun attachPlayerToView(view: androidx.media3.ui.PlayerView) {
        val target = player ?: return
        playerViewHolder.view = view
        val needsForceRebind = resumeAfterSurfaceAttach && view.player === target
        if (view.player !== target || needsForceRebind) {
            if (needsForceRebind) {
                view.player = null
            }
            view.player = target
        }
        videoCoordinator.markPlayerSurfaceBound(target)
        val pendingDetailHandoffResume = isInlineFeedOrDetail &&
            isDetailInlinePlayback &&
            videoCoordinator.shouldResumeInlineHandoff(playbackKey)
        val shouldResumeOnAttach = resumeAfterSurfaceAttach || pendingDetailHandoffResume
        if (!shouldResumeOnAttach) return
        fun resumePlayback() {
            // 浮窗本身就是本次交接的最终所有者。旧 Surface dispose/重组可能短暂清掉
            // peekPlaybackKey，不能因此拒绝恢复，否则会永久停在暂停态且播放按钮无效。
            if (isPeekPlayback && videoCoordinator.peekPlaybackKey?.let {
                    videoCoordinator.matchesPlaybackKey(it, playbackKey)
                } != true
            ) {
                videoCoordinator.claimPeekPlayback(playbackKey)
            }
            // view.post 可能在 activeKey 已清空 / Surface 已拆卸后执行，避免无画面仍出声音
            val stillOwned = when {
                isPeekPlayback -> true
                isFullscreen -> videoCoordinator.fullscreenKey?.let {
                    videoCoordinator.matchesPlaybackKey(it, playbackKey)
                } == true
                else -> videoCoordinator.isPlaybackKeyActive(playbackKey) ||
                    videoCoordinator.isPlaybackKeyHandoffPending(playbackKey) ||
                    (isDetailInlinePlayback && videoCoordinator.isDetailHandoffTarget(playbackKey))
            }
            if (!stillOwned) {
                // 尚未 claim peek 时已挂上 Surface：用 handoff pending 也允许恢复，避免永久卡住。
                val peekHandoffPending = isPeekPlayback &&
                    videoCoordinator.isPlaybackKeyHandoffPending(playbackKey)
                if (!peekHandoffPending) return
            }
            if (playerViewHolder.view !== view) return
            if (view.player !== target) {
                view.player = target
            }
            videoCoordinator.markPlayerSurfaceBound(target)
            target.playWhenReady = true
            target.play()
            resumeAfterSurfaceAttach = false
            if (isDetailInlinePlayback && videoCoordinator.shouldResumeInlineHandoff(playbackKey)) {
                videoCoordinator.completeDetailPlaybackHandoff(playbackKey)
            }
        }
        if (view.isAttachedToWindow) {
            view.post { resumePlayback() }
        } else {
            view.addOnAttachStateChangeListener(
                object : android.view.View.OnAttachStateChangeListener {
                    override fun onViewAttachedToWindow(v: android.view.View) {
                        view.removeOnAttachStateChangeListener(this)
                        view.post { resumePlayback() }
                    }

                    override fun onViewDetachedFromWindow(v: android.view.View) = Unit
                },
            )
        }
    }

    LaunchedEffect(
        player,
        isDetailInlinePlayback,
        isViewportVisible,
        resumeAfterSurfaceAttach,
        videoCoordinator.pendingPeekHandoffKey,
    ) {
        if (!isInlineFeedOrDetail || !isDetailInlinePlayback || player == null) return@LaunchedEffect
        if (!videoCoordinator.shouldResumeInlineHandoff(playbackKey)) return@LaunchedEffect
        if (!isViewportVisible || resumeAfterSurfaceAttach) return@LaunchedEffect
        player.playWhenReady = true
        player.play()
        playerViewHolder.view?.post {
            player.playWhenReady = true
            player.play()
            videoCoordinator.completeDetailPlaybackHandoff(playbackKey)
        }
    }

    if (player == null || (!isInlineFeedOrDetail && stashedForHandoff)) {
        Box(
            modifier = modifier.background(Color.Black),
            contentAlignment = Alignment.Center,
        ) {
            val pendingHandoffPlaceholder = inlineWaitingForHandoff ||
                awaitingFullscreenHandoff ||
                awaitingPeekHandoffSource ||
                awaitingPeekHandoffConsume ||
                videoCoordinator.isPlaybackKeyHandoffPending(playbackKey)
            val placeholderFrame = transitionFrame
            if (placeholderFrame != null && !placeholderFrame.isRecycled) {
                Image(
                    bitmap = placeholderFrame.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit,
                )
            } else if (!pendingHandoffPlaceholder && !media.coverUrl.isNullOrBlank()) {
                RemoteImage(
                    url = media.coverUrl.orEmpty(),
                    modifier = Modifier.fillMaxSize(),
                    contentScale = if (awaitingFullscreenHandoff) {
                        ContentScale.Fit
                    } else {
                        ContentScale.Crop
                    },
                )
            }
        }
        return
    }
    var videoFrameReady by remember(player) {
        mutableStateOf(
            player.playbackState == androidx.media3.common.Player.STATE_READY &&
                player.videoSize.width > 0 &&
                (player.isPlaying || player.currentPosition > 0L),
        )
    }
    LaunchedEffect(videoFrameReady) {
        if (!videoFrameReady || transitionFrame == null) return@LaunchedEffect
        delay(140)
        transitionFrame?.recycle()
        transitionFrame = null
    }

    fun openFullscreenWithCurrentFrame() {
        val textureView = playerViewHolder.view?.videoSurfaceView as? android.view.TextureView
        textureView?.bitmap?.takeIf { it.width > 0 && it.height > 0 }?.let { bitmap ->
            videoCoordinator.storeTransitionFrame(playbackKey, bitmap)
        }
        onFullscreen()
    }

    LaunchedEffect(player, playbackSpeedOverride) {
        if (playbackSpeedOverride == null) return@LaunchedEffect
        if (player.playbackState == androidx.media3.common.Player.STATE_ENDED) {
            player.seekTo(0)
            videoCoordinator.clearPosition(playbackKey)
        }
    }

    LaunchedEffect(player) {
        isBuffering = player.playbackState == androidx.media3.common.Player.STATE_BUFFERING
        isPlaying = player.isPlaying
    }

    LaunchedEffect(playbackSpeedOverride, selectedSpeed, player) {
        player.setPlaybackSpeed(playbackSpeedOverride ?: selectedSpeed)
    }

    fun enterPictureInPicture() {
        showControls()
        val currentPosition = player.currentPosition.coerceAtLeast(0L)
        videoCoordinator.rememberPosition(playbackKey, currentPosition)
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

    fun captureHandoffTransitionFrame() {
        val textureView = playerViewHolder.view?.videoSurfaceView as? android.view.TextureView
        textureView?.bitmap?.takeIf { it.width > 0 && it.height > 0 }?.let { bitmap ->
            videoCoordinator.storeTransitionFrame(playbackKey, bitmap)
        }
    }

    DisposableEffect(player) {
        val pauseHandler = {
            val currentPosition = player.currentPosition.coerceAtLeast(0L)
            videoCoordinator.rememberPosition(playbackKey, currentPosition)
            player.pause()
        }
        if (isPeekPlayback) {
            videoCoordinator.registerPeekPauseHandler(playbackKey, pauseHandler)
        } else {
            videoCoordinator.registerPauseHandler(playbackKey, isFullscreen, pauseHandler)
        }
        val listener = object : androidx.media3.common.Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                isBuffering = playbackState == androidx.media3.common.Player.STATE_BUFFERING
                isPlaying = player.isPlaying
                if (playbackState == androidx.media3.common.Player.STATE_ENDED) {
                    onPlaybackEnded?.invoke()
                }
            }

            override fun onIsPlayingChanged(value: Boolean) {
                isPlaying = value
                if (!value) {
                    isScrubbing = false
                }
            }

            override fun onVideoSizeChanged(videoSize: androidx.media3.common.VideoSize) {
                val width = videoSize.width.takeIf { it > 0 } ?: return
                val height = videoSize.height.takeIf { it > 0 } ?: return
                aspectRatio = width.toFloat() / height.toFloat()
                onAspectRatio(width, height)
            }

            override fun onRenderedFirstFrame() {
                videoFrameReady = true
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
            if (isPeekPlayback) {
                videoCoordinator.unregisterPeekPauseHandler(playbackKey)
                videoCoordinator.releasePeekPlayback(playbackKey)
            } else {
                videoCoordinator.unregisterPauseHandler(playbackKey, isFullscreen)
            }
            if (effectiveSavePosition) {
                if (videoCoordinator.isPeekRestartFromBeginning(playbackKey)) {
                    videoCoordinator.clearPosition(playbackKey)
                } else {
                    val currentPosition = player.currentPosition.coerceAtLeast(0L)
                    videoCoordinator.rememberPosition(playbackKey, currentPosition)
                }
            }
            player.removeListener(listener)
            val handoffToFullscreen = !seamlessOverlayPlayback && !isFullscreen &&
                videoCoordinator.isFullscreenHandoffPending(playbackKey)
            val handoffToPeek = !seamlessOverlayPlayback && !isPeekPlayback &&
                videoCoordinator.isPlaybackKeyHandoffPending(playbackKey)
            val handoffFromPeek = isPeekPlayback && !isFullscreen &&
                videoCoordinator.isPlaybackKeyHandoffPending(playbackKey)
            val transferringToDetail = !isDetailInlinePlayback &&
                videoCoordinator.isTransferringToDetail(playbackKey)
            val switchingToAnotherInline = isInlineFeedOrDetail &&
                !isPeekPlayback &&
                !isFullscreen &&
                !seamlessOverlayPlayback &&
                videoCoordinator.isAnotherInlinePlaybackActive(playbackKey)
            val alreadyStashedForHandoff = stashedForHandoffState.value
            videoCoordinator.registerSharedPlayer(playbackKey, player)
            if (handoffToFullscreen || handoffToPeek || handoffFromPeek || transferringToDetail) {
                if (!videoCoordinator.hasStashedHandoff(playbackKey)) {
                    captureHandoffTransitionFrame()
                    playerViewHolder.view?.player = null
                    videoCoordinator.stashHandoffPlayer(playbackKey, player)
                }
            } else if (switchingToAnotherInline) {
                if (!videoCoordinator.hasStashedHandoff(playbackKey)) {
                    playerViewHolder.view?.player = null
                    videoCoordinator.stashHandoffPlayer(playbackKey, player)
                }
            } else if (!alreadyStashedForHandoff) {
                val adoptedByDetail = videoCoordinator.detailOverlayOpen &&
                    !isDetailInlinePlayback &&
                    videoCoordinator.isPlaybackKeyActive(playbackKey)
                if (adoptedByDetail) {
                    playerViewHolder.view?.player = null
                } else {
                    videoCoordinator.releaseSharedPlayer(playbackKey, player)
                    player.pause()
                    if (trackViewportPause && videoCoordinator.isPlaybackKeyActive(playbackKey)) {
                        videoCoordinator.activeKey = null
                    }
                    player.release()
                }
            }
        }
    }

    LaunchedEffect(isViewportVisible, trackViewportPause, player, autoEnterFloatingOnViewportHidden, resumeAfterSurfaceAttach) {
        if (videoCoordinator.shouldResumeInlineHandoff(playbackKey)) {
            if (
                videoCoordinator.isPlaybackKeyActive(playbackKey) &&
                isViewportVisible &&
                !resumeAfterSurfaceAttach
            ) {
                resumeInlineHandoffPlaybackIfNeeded(player)
                if (isDetailInlinePlayback) {
                    videoCoordinator.completeDetailPlaybackHandoff(playbackKey)
                }
            }
            return@LaunchedEffect
        }
        if (!trackViewportPause || isViewportVisible) return@LaunchedEffect
        if (
            autoEnterFloatingOnViewportHidden &&
            onEnterFloatingPlayback != null &&
            !isPeekPlayback &&
            !isFullscreen &&
            videoCoordinator.peekPlaybackKey != playbackKey &&
            (player.isPlaying || player.playWhenReady)
        ) {
            onEnterFloatingPlayback()
            return@LaunchedEffect
        }
        val currentPosition = player.currentPosition.coerceAtLeast(0L)
        videoCoordinator.rememberPosition(playbackKey, currentPosition)
        player.pause()
        player.playWhenReady = false
        isPlaying = false
    }

    fun ensurePlaybackOwnership() {
        if (isFullscreen) return
        if (isPeekPlayback) {
            if (videoCoordinator.peekPlaybackKey != playbackKey) {
                videoCoordinator.claimPeekPlayback(playbackKey)
            }
        } else if (videoCoordinator.activeKey != playbackKey) {
            videoCoordinator.requestInlinePlayback(playbackKey)
        }
    }

    fun togglePlayPause() {
        showControls()
        if (player.isPlaying) {
            player.pause()
        } else {
            if (durationMs > 0 && positionMs >= durationMs - 500) {
                player.seekTo(0)
            }
            ensurePlaybackOwnership()
            // 用户点击播放即是明确的恢复意图；Surface 已由当前 PlayerView 持有时，
            // 不再让交接阶段的 defer 标记阻止播放。
            if (playerViewHolder.view?.player === player) {
                resumeAfterSurfaceAttach = false
                videoCoordinator.markPlayerSurfaceBound(player)
            }
            player.playWhenReady = true
            player.play()
        }
        isPlaying = player.isPlaying
    }

    val positionState by rememberUpdatedState(positionMs)
    val durationState by rememberUpdatedState(durationMs)
    val onSeekState = rememberUpdatedState<(Long) -> Unit> { target ->
        showControls()
        positionMs = target
        player.seekTo(target)
    }
    val togglePlayPauseState = rememberUpdatedState { togglePlayPause() }
    val toggleControlsState = rememberUpdatedState { toggleControls() }
    val gestureScopeState = rememberUpdatedState(scope)
    val peekDismissState = rememberUpdatedState(onPeekVerticalDismiss)
    val peekFingerDragState = rememberUpdatedState(onPeekFingerDragOffset)
    val peekFingerDragResetState = rememberUpdatedState(onPeekFingerDragReset)

    LaunchedEffect(player, videoFrameReady, playbackKey, videoCoordinator.activeKey, resumeAfterSurfaceAttach, isViewportVisible) {
        if (!videoFrameReady || resumeAfterSurfaceAttach) return@LaunchedEffect
        if (isDetailInlinePlayback &&
            videoCoordinator.shouldResumeInlineHandoff(playbackKey) &&
            !isViewportVisible
        ) {
            return@LaunchedEffect
        }
        resumeInlineHandoffPlaybackIfNeeded(player)
    }

    LaunchedEffect(player, isDetailInlinePlayback, playbackKey) {
        if (!isDetailInlinePlayback) return@LaunchedEffect
        // 详情页首次开播后若仍无画面首帧，强制重绑 Surface（常见于 handoff/清 surface 竞态）。
        delay(180)
        if (videoFrameReady) return@LaunchedEffect
        if (!player.playWhenReady && !player.isPlaying && !resumeAfterSurfaceAttach) return@LaunchedEffect
        playerViewHolder.view?.let { view ->
            view.player = null
            view.player = player
            videoCoordinator.markPlayerSurfaceBound(player)
            val stillOwned = videoCoordinator.isPlaybackKeyActive(playbackKey) ||
                resumeAfterSurfaceAttach ||
                videoCoordinator.isPlaybackKeyHandoffPending(playbackKey)
            if (stillOwned) {
                player.playWhenReady = true
                player.play()
                resumeAfterSurfaceAttach = false
            }
        }
    }

    LaunchedEffect(player, isScrubbing) {
        while (true) {
            if (!isScrubbing) {
                positionMs = player.currentPosition.coerceAtLeast(0L)
                videoCoordinator.rememberPosition(playbackKey, positionMs)
                durationMs = player.duration.takeIf { it > 0 } ?: durationMs
                isPlaying = player.isPlaying
            }
            if (player.isPlaying && !isScrubbing) {
                withFrameMillis { }
            } else {
                delay(if (isScrubbing) 500 else 200)
            }
        }
    }

    Box(
        modifier = modifier
            .background(Color.Black)
            .clipToBounds()
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
        val videoControlBackdrop = rememberLayerBackdrop()
        Box(
            Modifier
                .fillMaxSize()
                .layerBackdrop(videoControlBackdrop),
        ) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color.Black),
            )
            Box(
                Modifier
                    .fillMaxSize()
                    .then(
                    if (controlsEnabled && !hideProgressControls) {
                        Modifier.pointerInput(
                            durationState,
                            hideProgressControls,
                            onPeekVerticalDismiss,
                            isFullscreen,
                            isPeekPlayback,
                        ) {
                        val peekDismissActive = onPeekVerticalDismiss != null
                        val peekDismissThreshold = if (peekDismissActive) 48f else 82f
                        val peekDismissReleaseThreshold = if (peekDismissActive) 28f else peekDismissThreshold
                        val verticalDominanceRatio = if (peekDismissActive) 0.85f else 1.15f
                        val horizontalDominanceRatio = 1.15f
                        val allowSurfaceScrub = (isFullscreen || isPeekPlayback) && !peekDismissActive
                        var lastTapUptimeMs = 0L
                        var pendingSingleTapJob: Job? = null
                        awaitEachGesture {
                            val down = awaitFirstDown(requireUnconsumed = false)
                            val slop = viewConfiguration.touchSlop
                            val doubleTapTimeout = viewConfiguration.doubleTapTimeoutMillis.toLong()
                            val anchorX = down.position.x
                            val startY = down.position.y
                            val anchorPosition = positionState
                            val width = size.width.toFloat()
                            var dragging = false
                            var peekDismissed = false
                            var lastSeekPosition = anchorPosition
                            while (true) {
                                val event = awaitPointerEvent()
                                val change = event.changes.firstOrNull { it.id == down.id }
                                    ?: event.changes.firstOrNull()
                                    ?: break
                                val totalDrag = change.position - down.position
                                val dx = abs(totalDrag.x)
                                val dy = abs(totalDrag.y)
                                val duration = durationState
                                val peekDismiss = peekDismissState.value
                                val verticalDominant = dy > dx * verticalDominanceRatio
                                if (peekDismiss != null && change.pressed) {
                                    peekFingerDragState.value?.invoke(totalDrag)
                                    if (dy > peekDismissThreshold && verticalDominant) {
                                        change.consume()
                                        peekDismissed = true
                                        pendingSingleTapJob?.cancel()
                                        peekDismiss.invoke()
                                        while (true) {
                                            val consumeEvent = awaitPointerEvent()
                                            consumeEvent.changes.forEach { it.consume() }
                                            if (consumeEvent.changes.all { !it.pressed }) break
                                        }
                                        break
                                    }
                                }
                                if (
                                    !peekDismissActive &&
                                    !dragging &&
                                    dy > slop &&
                                    dy > dx * horizontalDominanceRatio
                                ) {
                                    // 纵向滑动交给外层列表，避免误触进度拖动。
                                    break
                                }
                                if (
                                    allowSurfaceScrub &&
                                    !dragging &&
                                    dx > slop &&
                                    dx > dy * horizontalDominanceRatio
                                ) {
                                    dragging = true
                                    isScrubbing = true
                                    pendingSingleTapJob?.cancel()
                                    showControls()
                                    controlsHideSignal++
                                }
                                if (dragging && duration > 0L && width > 0f && change.pressed) {
                                    change.consume()
                                    val deltaMs = ((change.position.x - anchorX) / width * duration).toLong()
                                    val newPosition = (anchorPosition + deltaMs).coerceIn(0L, duration)
                                    if (newPosition != lastSeekPosition) {
                                        lastSeekPosition = newPosition
                                        onSeekState.value(newPosition)
                                    }
                                }
                                if (event.changes.all { it.changedToUpIgnoreConsumed() }) {
                                    if (!dragging && !peekDismissed && peekDismiss != null) {
                                        if (dy > peekDismissReleaseThreshold && verticalDominant) {
                                            peekDismiss.invoke()
                                            peekDismissed = true
                                        }
                                    }
                                    if (!dragging && !peekDismissed) {
                                        val upUptime = change.uptimeMillis
                                        pendingSingleTapJob?.cancel()
                                        if (
                                            lastTapUptimeMs > 0L &&
                                            upUptime - lastTapUptimeMs <= doubleTapTimeout
                                        ) {
                                            lastTapUptimeMs = 0L
                                            togglePlayPauseState.value()
                                        } else {
                                            lastTapUptimeMs = upUptime
                                            pendingSingleTapJob = gestureScopeState.value.launch {
                                                delay(doubleTapTimeout)
                                                if (lastTapUptimeMs == upUptime) {
                                                    lastTapUptimeMs = 0L
                                                    toggleControlsState.value()
                                                }
                                            }
                                        }
                                    }
                                    break
                                }
                            }
                            if (peekDismissed) {
                                peekFingerDragResetState.value?.invoke()
                            } else if (peekDismissState.value != null) {
                                peekFingerDragResetState.value?.invoke()
                            }
                            if (dragging) {
                                isScrubbing = false
                            }
                        }
                    }
                } else if (controlsEnabled) {
                    Modifier.pointerInput(Unit) {
                        detectTapGestures(onTap = { toggleControls() })
                    }
                } else {
                    Modifier
                },
                ),
        ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            val safeVideoAspect = aspectRatio.coerceIn(0.25f, 4f)
            val viewportAspect = if (maxHeight > 0.dp) maxWidth / maxHeight else safeVideoAspect
            val fullscreenVideoModifier = if (!isFullscreen) {
                Modifier.fillMaxSize()
            } else if (viewportAspect > safeVideoAspect) {
                Modifier
                    .fillMaxHeight()
                    .aspectRatio(safeVideoAspect)
            } else {
                Modifier
                    .fillMaxWidth()
                    .aspectRatio(safeVideoAspect)
            }
            AndroidView(
            modifier = fullscreenVideoModifier,
            factory = { ctx ->
                (android.view.LayoutInflater.from(ctx)
                    .inflate(R.layout.view_video_player, null, false) as androidx.media3.ui.PlayerView).apply {
                    attachPlayerToView(this)
                    useController = false
                    resizeMode = effectiveResizeMode
                    keepScreenOn = isPlaying
                    setKeepContentOnPlayerReset(true)
                    setShutterBackgroundColor(android.graphics.Color.BLACK)
                }
            },
            update = { view ->
                attachPlayerToView(view)
                view.resizeMode = effectiveResizeMode
                view.keepScreenOn = isPlaying
                view.requestLayout()
            },
            onRelease = { view ->
                view.keepScreenOn = false
                val releasedPlayer = view.player as? androidx.media3.exoplayer.ExoPlayer
                view.player = null
                releasedPlayer?.let(videoCoordinator::markPlayerSurfaceUnbound)
                if (playerViewHolder.view === view) {
                    playerViewHolder.view = null
                }
            },
            )
        }
        }
        }

        AnimatedVisibility(
            visible = controlsEnabled && controlsVisible && !isFullscreen && !isPeekPlayback && showFullscreenButton,
            enter = fadeIn(tween(200)) + slideInVertically(tween(220)) { -it },
            exit = fadeOut(tween(180)) + slideOutVertically(tween(200)) { -it },
            modifier = Modifier.align(Alignment.TopStart),
        ) {
            GlassTextButton(
                text = "全屏",
                backdrop = videoControlBackdrop,
                modifier = Modifier
                    .padding(10.dp)
                    .width(54.dp)
                    .height(28.dp),
                onClick = {
                    showControls()
                    openFullscreenWithCurrentFrame()
                },
            )
        }

        AnimatedVisibility(
            visible = !videoFrameReady &&
                playbackError == null &&
                !(seamlessOverlayPlayback && isFullscreen) &&
                (
                    transitionFrame != null ||
                        (
                            !inlineWaitingForHandoff &&
                                !awaitingPeekHandoffSource &&
                                !awaitingPeekHandoffConsume &&
                                !videoCoordinator.isPlaybackKeyHandoffPending(playbackKey) &&
                                !media.coverUrl.isNullOrBlank()
                            )
                    ),
            enter = EnterTransition.None,
            exit = fadeOut(tween(120)),
            modifier = Modifier.matchParentSize(),
        ) {
            val frame = transitionFrame
            if (frame != null && !frame.isRecycled) {
                Image(
                    bitmap = frame.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black),
                    contentScale = ContentScale.Fit,
                )
            } else {
                RemoteImage(
                    url = media.coverUrl.orEmpty(),
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black),
                    contentScale = ContentScale.Fit,
                )
            }
        }

        AnimatedVisibility(
            visible = controlsEnabled && controlsVisible && isFullscreen && showLandscapeToggle,
            enter = fadeIn(tween(200)) + slideInVertically(tween(220)) { -it },
            exit = fadeOut(tween(180)) + slideOutVertically(tween(200)) { -it },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .zIndex(21f),
        ) {
            GlassTextButton(
                text = "横屏",
                backdrop = videoControlBackdrop,
                modifier = Modifier
                    .padding(
                        end = fullscreenSecondaryButtonEnd,
                        top = fullscreenPrimaryControlTop,
                    )
                    .width(54.dp)
                    .height(28.dp),
                onClick = {
                    showControls()
                    forcedOrientation = ForcedVideoOrientation.Landscape
                },
            )
        }

        AnimatedVisibility(
            visible = controlsEnabled && controlsVisible && isFullscreen && showPortraitToggle,
            enter = fadeIn(tween(200)) + slideInVertically(tween(220)) { -it },
            exit = fadeOut(tween(180)) + slideOutVertically(tween(200)) { -it },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .zIndex(21f),
        ) {
            GlassTextButton(
                text = "竖屏",
                backdrop = videoControlBackdrop,
                modifier = Modifier
                    .padding(
                        end = fullscreenSecondaryButtonEnd,
                        top = fullscreenPrimaryControlTop,
                    )
                    .width(54.dp)
                    .height(28.dp),
                onClick = {
                    showControls()
                    forcedOrientation = ForcedVideoOrientation.Portrait
                },
            )
        }

        AnimatedVisibility(
            visible = controlsEnabled && controlsVisible && !isPeekPlayback && (
                onEnterFloatingPlayback != null ||
                (showPictureInPictureButton && onEnterPictureInPicture != null)
                ),
            enter = fadeIn(tween(200)) + slideInVertically(tween(220)) { -it },
            exit = fadeOut(tween(180)) + slideOutVertically(tween(200)) { -it },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .zIndex(if (isFullscreen) 20f else 0f),
        ) {
            GlassTextButton(
                text = if (onEnterFloatingPlayback != null) "浮窗" else "画中画",
                backdrop = videoControlBackdrop,
                modifier = Modifier
                    .padding(
                        end = if (isFullscreen) VideoFullscreenHorizontalControlInset else 10.dp,
                        top = if (isFullscreen) fullscreenFloatingButtonTop else 10.dp,
                    )
                    .width(if (onEnterFloatingPlayback != null) 54.dp else 66.dp)
                    .height(28.dp),
                onClick = {
                    showControls()
                    if (onEnterFloatingPlayback != null) {
                        onEnterFloatingPlayback()
                    } else {
                        enterPictureInPicture()
                    }
                },
            )
        }

        AnimatedVisibility(
            visible = controlsEnabled && controlsVisible && isFullscreen && !isLiveBroadcast,
            enter = fadeIn(tween(200)) + slideInVertically(tween(220)) { -it },
            exit = fadeOut(tween(180)) + slideOutVertically(tween(200)) { -it },
            modifier = Modifier
                .align(Alignment.TopStart)
                .zIndex(20f),
        ) {
            GlassTextButton(
                text = if (downloading) "保存中" else "保存",
                backdrop = videoControlBackdrop,
                modifier = Modifier
                    .padding(
                        start = VideoFullscreenHorizontalControlInset,
                        top = fullscreenPrimaryControlTop,
                    )
                    .width(if (downloading) 66.dp else 54.dp)
                    .height(28.dp),
                enabled = !downloading,
                onClick = {
                    downloading = true
                    scope.launch {
                        saveHint.saveVideo(context, media)
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
            visible = controlsEnabled && controlsVisible && !isFullscreen && isPeekPlayback,
            enter = fadeIn(tween(200)) + slideInVertically(tween(220)) { -it },
            exit = fadeOut(tween(180)) + slideOutVertically(tween(200)) { -it },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .zIndex(10f),
        ) {
            GlassTextButton(
                text = "全屏",
                backdrop = videoControlBackdrop,
                modifier = Modifier
                    .padding(top = 8.dp, end = 8.dp)
                    .width(54.dp)
                    .height(28.dp),
                onClick = {
                    showControls()
                    openFullscreenWithCurrentFrame()
                },
            )
        }

        AnimatedVisibility(
            visible = controlsEnabled && controlsVisible && !hideProgressControls,
            enter = fadeIn(tween(200)) + slideInVertically(tween(220)) { it },
            exit = fadeOut(tween(180)) + slideOutVertically(tween(200)) { it },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(
                    start = if (isFullscreen) VideoFullscreenHorizontalControlInset else 6.dp,
                    end = if (isFullscreen) VideoFullscreenHorizontalControlInset else 6.dp,
                    bottom = if (isFullscreen) {
                        if (isDevicePortrait) {
                            49.dp
                        } else {
                            fullscreenBottomInset + VideoControlBarBottomFullscreen
                        }
                    } else {
                        VideoControlBarBottomInline
                    },
                )
                .fillMaxWidth(),
        ) {
            VideoControls(
                isPlaying = isPlaying,
                positionMs = positionMs,
                durationMs = durationMs,
                speed = displayedSpeed,
                backdrop = videoControlBackdrop,
                onPlayPause = {
                    showControls()
                    if (player.isPlaying) {
                        player.pause()
                    } else {
                        if (durationMs > 0 && positionMs >= durationMs - 500) {
                            player.seekTo(0)
                        }
                        ensurePlaybackOwnership()
                        player.playWhenReady = true
                        player.play()
                    }
                    isPlaying = player.isPlaying
                },
                onSeek = { target ->
                    showControls()
                    positionMs = target
                    player.seekTo(target)
                },
                onScrubbingChanged = { scrubbing ->
                    isProgressBarScrubbing = scrubbing
                    if (!scrubbing) {
                        showControls()
                    }
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
                    .height(VideoControlBarHeight),
            )
        }
    }
}

@Composable
private fun GlassTextButton(
    text: String,
    backdrop: Backdrop,
    modifier: Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    TransparentLiquidTextButton(
        text = text,
        onClick = onClick,
        backdrop = backdrop,
        modifier = modifier,
        enabled = enabled,
        textColor = Color.White,
    )
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
    backdrop: Backdrop,
    modifier: Modifier = Modifier,
    onScrubbingChanged: (Boolean) -> Unit = {},
) {
    val progress = if (durationMs > 0L) {
        (positionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)
    } else {
        0f
    }
    var isScrubbing by remember { mutableStateOf(false) }
    val positionState by rememberUpdatedState(positionMs)
    val durationState by rememberUpdatedState(durationMs)
    val onSeekState by rememberUpdatedState(onSeek)
    val onScrubbingChangedState by rememberUpdatedState(onScrubbingChanged)

    TransparentLiquidCapsule(
        modifier = modifier
            .pointerInput(durationState) {
                val horizontalDominanceRatio = 1.15f
                awaitEachGesture {
                    var scrubbing = false
                    try {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        down.consume()
                        val slop = viewConfiguration.touchSlop
                        val duration = durationState
                        val width = size.width.toFloat()
                        if (duration <= 0L || width <= 0f) return@awaitEachGesture
                        val anchorPosition = positionState
                        val anchorX = down.position.x
                        val anchorY = down.position.y
                        var lastSeekPosition = anchorPosition
                        while (true) {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull() ?: break
                            val dx = abs(change.position.x - anchorX)
                            val dy = abs(change.position.y - anchorY)
                            if (!scrubbing) {
                                if (dy > slop && dy > dx * horizontalDominanceRatio) {
                                    break
                                }
                                if (dx > slop && dx > dy * horizontalDominanceRatio) {
                                    scrubbing = true
                                    isScrubbing = true
                                    onScrubbingChangedState(true)
                                }
                            }
                            if (scrubbing && change.pressed) {
                                change.consume()
                                val deltaMs = ((change.position.x - anchorX) / width * duration).toLong()
                                val newPosition = (anchorPosition + deltaMs).coerceIn(0L, duration)
                                if (newPosition != lastSeekPosition) {
                                    lastSeekPosition = newPosition
                                    onSeekState(newPosition)
                                }
                            }
                            if (event.changes.all { it.changedToUpIgnoreConsumed() }) break
                        }
                    } finally {
                        if (isScrubbing) {
                            isScrubbing = false
                            onScrubbingChangedState(false)
                        }
                    }
                }
            },
        backdrop = backdrop,
        pill = true,
        surfaceColor = Color.White.copy(alpha = 0.14f),
    ) {
        VideoControlCapsuleProgressBackground(
            progress = progress,
            animate = isPlaying && !isScrubbing,
            modifier = Modifier.matchParentSize(),
        )
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = formatVideoTime(positionMs),
                modifier = Modifier
                    .widthIn(min = 36.dp)
                    .align(Alignment.CenterVertically),
                color = Color.White,
                style = videoControlTextStyle(12),
                maxLines = 1,
            )
            IconButton(
                onClick = onPlayPause,
                modifier = Modifier
                    .size(25.dp)
                    .align(Alignment.CenterVertically),
            ) {
                Icon(
                    painter = painterResource(if (isPlaying) R.drawable.ic_video_pause else R.drawable.ic_video_play),
                    contentDescription = if (isPlaying) "暂停" else "播放",
                    modifier = Modifier.size(20.dp),
                    tint = Color.White,
                )
            }
            Spacer(Modifier.weight(1f))
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .widthIn(min = 30.dp)
                    .align(Alignment.CenterVertically)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                        onClick = onSpeedClick,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = speedLabel(speed),
                    color = Color.White,
                    style = videoControlTextStyle(13),
                    maxLines = 1,
                )
            }
            Text(
                text = formatVideoTime((durationMs - positionMs).coerceAtLeast(0L)),
                modifier = Modifier
                    .widthIn(min = 36.dp)
                    .align(Alignment.CenterVertically),
                color = Color.White.copy(alpha = 0.82f),
                style = videoControlTextStyle(12),
                maxLines = 1,
                textAlign = TextAlign.End,
            )
        }
    }
}

@Composable
private fun VideoControlCapsuleProgressBackground(
    progress: Float,
    animate: Boolean,
    modifier: Modifier = Modifier,
) {
    val targetProgress = progress.coerceIn(0f, 1f)
    val displayedProgress by animateFloatAsState(
        targetValue = targetProgress,
        animationSpec = if (animate) {
            tween(durationMillis = 120, easing = LinearEasing)
        } else {
            snap()
        },
        label = "video-control-progress",
    )
    BoxWithConstraints(
        modifier = modifier.clip(VideoControlCapsuleShape),
    ) {
        if (displayedProgress > 0f) {
            val lineOffset = (maxWidth * displayedProgress - VideoProgressLineWidth)
                .coerceIn(0.dp, maxWidth - VideoProgressLineWidth)
            Box(
                modifier = Modifier
                    .offset(x = lineOffset)
                    .width(VideoProgressLineWidth)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(percent = 50))
                    .background(MaterialTheme.colorScheme.primary),
            )
        }
    }
}

private val StatusLikeColor = Color(0xFFE94326)

@Composable
private fun StatusActions(
    item: FeedItem,
    modifier: Modifier = Modifier,
    onRepostClick: (() -> Unit)? = null,
    onCommentClick: (() -> Unit)? = null,
    onCommentLongClick: (() -> Unit)? = null,
    onLikeClick: ((Rect) -> Unit)? = null,
    onToggleLike: (() -> Unit)? = null,
) {
    val actionColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.62f)
    val chipColor = actionColor
    val haptic = LocalHapticFeedback.current
    val likeLabelColor = if (item.liked) StatusLikeColor else chipColor
    Row(
        modifier = modifier
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
                onClick = { onRepostClick?.invoke() },
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
            Box(
                modifier = Modifier
                    .height(24.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .combinedClickable(
                        onClick = { onCommentClick?.invoke() },
                        onLongClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onCommentLongClick?.invoke()
                        },
                    )
                    .padding(horizontal = 8.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text("评论 ${item.commentsCount}", fontSize = 11.sp, color = chipColor)
            }
        }
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            contentAlignment = Alignment.Center,
        ) {
            var likeAnchorBounds by remember(item.id) { mutableStateOf<Rect?>(null) }
            Box(
                modifier = Modifier
                    .height(24.dp)
                    .onGloballyPositioned { coordinates ->
                        likeAnchorBounds = coordinates.boundsInWindow()
                    }
                    .clip(RoundedCornerShape(12.dp))
                    .combinedClickable(
                        onClick = {
                            likeAnchorBounds?.let { bounds ->
                                onLikeClick?.invoke(bounds)
                            }
                        },
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
    contentSection: DetailContentSection,
    comments: List<CommentItem>,
    reposts: List<CommentItem>,
    commentSort: CommentSort,
    isLoadingComments: Boolean,
    isLoadingReposts: Boolean,
    isLoadingMoreComments: Boolean = false,
    isLoadingMoreReposts: Boolean = false,
    onBack: () -> Unit,
    onRefresh: () -> Unit,
    onCommentSortChange: (CommentSort) -> Unit,
    onSelectContentSection: (DetailContentSection) -> Unit,
    onMediaClick: (FeedMedia, String) -> Unit,
    emoticonMap: Map<String, String> = emptyMap(),
    onRetweetClick: ((FeedItem, FeedItem) -> Unit)? = null,
    onUserClick: ((String) -> Unit)? = null,
    commentsHasMore: Boolean = true,
    repostsHasMore: Boolean = true,
    listState: LazyListState = rememberLazyListState(),
    onLoadMoreComments: () -> Unit = {},
    onLoadMoreReposts: () -> Unit = {},
    isLongTextLoading: (FeedItem) -> Boolean = { false },
    onLoadLongText: ((FeedItem) -> Unit)? = null,
    onToggleLike: ((FeedItem) -> Unit)? = null,
    onLikeClick: ((FeedItem, Rect) -> Unit)? = null,
    onComposeComment: ((FeedItem, CommentItem?) -> Unit)? = null,
    onUrlEntityClick: ((FeedUrlEntity) -> Unit)? = null,
    onExpandNestedComments: ((String) -> Unit)? = null,
    nestedCommentsLoadingIds: Set<String> = emptySet(),
) {
    val showingReposts = contentSection == DetailContentSection.Reposts
    val sectionItems = if (showingReposts) reposts else comments
    val isLoadingSection = if (showingReposts) isLoadingReposts else isLoadingComments
    val isLoadingMoreSection = if (showingReposts) isLoadingMoreReposts else isLoadingMoreComments
    val sectionHasMore = if (showingReposts) repostsHasMore else commentsHasMore
    val onLoadMoreSection = if (showingReposts) onLoadMoreReposts else onLoadMoreComments
    var pullRefreshing by remember { mutableStateOf(false) }
    var detailVideoViewport by remember { mutableStateOf(DetailVideoViewport()) }
    LaunchedEffect(isLoadingSection) {
        if (!isLoadingSection) {
            pullRefreshing = false
        }
    }

    LaunchedEffect(
        listState,
        contentSection,
        sectionItems.size,
        isLoadingSection,
        sectionHasMore,
    ) {
        snapshotFlow {
            val info = listState.layoutInfo
            val last = info.visibleItemsInfo.lastOrNull()?.index ?: -1
            info.totalItemsCount to last
        }
            .filter { (total, last) -> total > 0 && last >= total - 3 }
            .collect {
                if (sectionHasMore && !isLoadingSection) {
                    onLoadMoreSection()
                }
            }
    }

    Box(Modifier.fillMaxSize()) {
    AppPullToRefreshBox(
        isRefreshing = pullRefreshing,
        onRefresh = {
            pullRefreshing = true
            onRefresh()
        },
        modifier = Modifier.fillMaxSize(),
    ) {
        Column(Modifier.fillMaxSize()) {
            DetailStickyAuthorHeader(
                item = item,
                onUserClick = onUserClick,
            )
            LazyColumn(
                state = listState,
                flingBehavior = rememberWeiboListFlingBehavior(),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .onGloballyPositioned { coordinates ->
                        val bounds = coordinates.boundsInWindow()
                        val next = DetailVideoViewport(
                            headerBottomPx = bounds.top,
                            viewportBottomPx = bounds.bottom,
                        )
                        if (detailVideoViewport != next) detailVideoViewport = next
                    },
                contentPadding = PaddingValues(bottom = 24.dp),
            ) {
                item(key = "detail-feed") {
                    CompositionLocalProvider(LocalDetailVideoViewport provides detailVideoViewport) {
                        Box(Modifier.padding(top = 8.dp)) {
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
                                onLikeClick = onLikeClick,
                                onUrlEntityClick = onUrlEntityClick,
                                onCommentClick = { onSelectContentSection(DetailContentSection.Comments) },
                                onCommentLongClick = { onComposeComment?.invoke(item, null) },
                                onRepostClick = { onSelectContentSection(DetailContentSection.Reposts) },
                                showAuthorRow = false,
                                insetRounded = true,
                                autoFloatingOnScrollAway = true,
                            )
                        }
                    }
                }
                item(key = "detail-section-header") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = CommentRowOuterStart, end = 8.dp, top = 12.dp, bottom = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = if (showingReposts) {
                                "转发 ${item.repostsCount}"
                            } else {
                                "评论 ${item.commentsCount}"
                            },
                            fontSize = CommentAuthorFontSize,
                            fontWeight = FontWeight.SemiBold,
                        )
                        if (!showingReposts) {
                            CommentSortToggle(
                                selected = commentSort,
                                onSelected = onCommentSortChange,
                            )
                        }
                    }
                }

                if (sectionItems.isEmpty() && isLoadingSection) {
                    item {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            AppLoadingIndicator(size = 22.dp, strokeWidth = 2.dp)
                        }
                    }
                }

                if (sectionItems.isEmpty() && !isLoadingSection) {
                    item {
                        Text(
                            text = if (showingReposts) "暂无转发" else "暂无评论",
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

                itemsIndexed(
                    sectionItems,
                    key = { _, entry -> entry.id },
                    contentType = { _, _ -> "comment_row" },
                ) { index, entry ->
                    CommentRow(
                        comment = entry,
                        onUserClick = onUserClick,
                        onExpandNestedComments = if (showingReposts) null else onExpandNestedComments,
                        nestedCommentsLoadingIds = nestedCommentsLoadingIds,
                        onReplyClick = if (showingReposts) null else { comment -> onComposeComment?.invoke(item, comment) },
                    )
                    if (index < sectionItems.lastIndex) {
                        CommentDivider()
                    }
                }

                if (isLoadingSection && sectionItems.isNotEmpty()) {
                    item {
                        Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                            AppLoadingIndicator()
                        }
                    }
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
    modifier: Modifier = Modifier,
) {
    val topInset = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    Surface(
        modifier = modifier
            .fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 26.dp)
                    .padding(top = topInset + 12.dp, bottom = 10.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Box(modifier = Modifier.weight(1f).consumeTouchEvents()) {
                    AuthorRow(
                        item = item,
                        onUserClick = onUserClick,
                        avatarClickable = true,
                    )
                }
                Box(Modifier.zIndex(1f)) {
                    FeedCardActionMenu(item = item)
                }
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

private fun findCommentInTree(comments: List<CommentItem>, commentId: String): CommentItem? {
    for (comment in comments) {
        if (comment.id == commentId) return comment
        findCommentInTree(comment.comments, commentId)?.let { return it }
    }
    return null
}

private fun mergeCommentItems(
    existing: List<CommentItem>,
    appended: List<CommentItem>,
): List<CommentItem> {
    if (appended.isEmpty()) return existing
    val seen = existing.map { it.id }.toMutableSet()
    return sortNestedCommentsByTime(existing + appended.filter { it.id !in seen })
}

private fun sortNestedCommentsByTime(comments: List<CommentItem>): List<CommentItem> =
    comments
        .sortedBy { parseWeiboCreatedAtMillis(it.createdAt) ?: Long.MAX_VALUE }
        .map { comment ->
            if (comment.comments.isEmpty()) comment
            else comment.copy(comments = sortNestedCommentsByTime(comment.comments))
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
private fun CommentComposerDialog(
    target: CommentComposeTarget,
    avatarUrl: String?,
    submitting: Boolean,
    emoticonMap: Map<String, String>,
    recentEmoticons: List<String>,
    mentionAvatarSuggestions: List<MentionCandidate>,
    mentionNameIndex: List<MentionCandidate>,
    mentionSuggestionsLoading: Boolean,
    onEmoticonUsed: (String) -> Unit,
    onDismiss: () -> Unit,
    onSubmit: (String, List<Uri>, Boolean) -> Unit,
) {
    var text by remember(target) { mutableStateOf(TextFieldValue("")) }
    var alsoRepost by remember(target) { mutableStateOf(false) }
    var emoticonPanelVisible by remember(target) { mutableStateOf(false) }
    var selectedPhotoUris by remember(target) { mutableStateOf<List<Uri>>(emptyList()) }
    val focusRequester = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val canSubmit = (text.text.trim().isNotEmpty() || selectedPhotoUris.isNotEmpty()) && !submitting
    val sendColor = Color(0xFFFFB36B)
    val cardColor = if (isLightAppearance()) Color(0xFFFFFBFF) else MaterialTheme.colorScheme.surface
    val inputColor = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.72f)
    val inputTextStyle = MaterialTheme.typography.bodyMedium.copy(
        color = MaterialTheme.colorScheme.onSurface,
        lineHeight = 22.sp,
    )
    val recentEntries = remember(emoticonMap, recentEmoticons) {
        recentEmoticons.mapNotNull { phrase -> emoticonMap[phrase]?.let { phrase to it } }
    }
    val allEntries = remember(emoticonMap) {
        emoticonMap.entries.sortedBy { it.key }.map { it.key to it.value }
    }
    val photoPickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetMultipleContents(),
    ) { uris ->
        selectedPhotoUris = uris.take(9)
    }
    val mentionCursor = min(text.selection.start, text.selection.end)
    val activeMentionQuery = extractActiveMentionQuery(text.text, mentionCursor)
    val displayedMentionSuggestions = remember(
        text.text,
        text.selection.end,
        mentionAvatarSuggestions,
        mentionNameIndex,
    ) {
        when (val query = activeMentionQuery) {
            null -> emptyList()
            "" -> mentionAvatarSuggestions
            else -> filterMentionCandidatesByQuery(mentionNameIndex, query)
        }
    }
    val mentionPanelExpanded = activeMentionQuery != null

    fun insertAtCursor(value: String, requestKeyboard: Boolean = false) {
        val selection = text.selection
        val start = min(selection.start, selection.end).coerceIn(0, text.text.length)
        val end = max(selection.start, selection.end).coerceIn(0, text.text.length)
        val nextText = (text.text.substring(0, start) + value + text.text.substring(end)).take(600)
        val nextCursor = (start + value.length).coerceAtMost(nextText.length)
        text = TextFieldValue(
            text = nextText,
            selection = TextRange(nextCursor),
        )
        if (requestKeyboard) {
            emoticonPanelVisible = false
            runCatching { focusRequester.requestFocus() }
            keyboard?.show()
        }
    }

    fun appendEmoticon(phrase: String) {
        insertAtCursor(phrase)
        onEmoticonUsed(phrase)
    }

    fun insertMention() {
        insertAtCursor("@", requestKeyboard = true)
    }

    fun insertMentionUser(user: MentionCandidate) {
        val cursor = min(text.selection.start, text.selection.end).coerceIn(0, text.text.length)
        val before = text.text.substring(0, cursor)
        val after = text.text.substring(cursor)
        val atIndex = before.lastIndexOf('@')
        val mention = "@${user.name} "
        val nextText = if (atIndex >= 0) {
            before.substring(0, atIndex) + mention + after
        } else if (cursor > 0 && text.text.getOrNull(cursor - 1) == '@') {
            before + "${user.name} " + after
        } else {
            before + mention + after
        }.take(600)
        val nextCursor = if (atIndex >= 0) {
            (atIndex + mention.length).coerceAtMost(nextText.length)
        } else {
            (cursor + mention.length).coerceAtMost(nextText.length)
        }
        text = TextFieldValue(
            text = nextText,
            selection = TextRange(nextCursor),
        )
        runCatching { focusRequester.requestFocus() }
        keyboard?.show()
    }

    fun openEmoticons() {
        emoticonPanelVisible = !emoticonPanelVisible
        if (emoticonPanelVisible) {
            keyboard?.hide()
            focusManager.clearFocus()
        }
    }

    LaunchedEffect(target) {
        delay(120)
        runCatching { focusRequester.requestFocus() }
        keyboard?.show()
    }

    Dialog(
        onDismissRequest = {
            focusManager.clearFocus()
            keyboard?.hide()
            onDismiss()
        },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false,
        ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.18f))
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = {
                        focusManager.clearFocus()
                        keyboard?.hide()
                        onDismiss()
                    },
                ),
            contentAlignment = Alignment.BottomCenter,
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .imePadding()
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                        onClick = {},
                    ),
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                color = cardColor,
                tonalElevation = 1.dp,
                shadowElevation = 8.dp,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    target.replyTo?.let { reply ->
                        Text(
                            text = "@${reply.authorName}: ${reply.text.trim().replace(Regex("\\s+"), " ").take(46)}",
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(inputColor.copy(alpha = 0.48f))
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    AnimatedVisibility(visible = mentionPanelExpanded && displayedMentionSuggestions.isNotEmpty()) {
                        MentionSuggestionPanel(
                            users = displayedMentionSuggestions,
                            vertical = !activeMentionQuery.isNullOrBlank(),
                            onSelect = ::insertMentionUser,
                        )
                    }
                    if (mentionSuggestionsLoading && mentionPanelExpanded && displayedMentionSuggestions.isEmpty()) {
                        Text(
                            text = "正在加载 @ 联系人…",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(9.dp),
                    ) {
                        RemoteImage(
                            url = avatarUrl,
                            modifier = Modifier
                                .padding(top = 3.dp)
                                .size(34.dp)
                                .clip(CircleShape)
                                .clickable(
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() },
                                    onClick = { openEmoticons() },
                                ),
                            contentScale = ContentScale.Crop,
                            maxDecodeDim = 96,
                        )
                        BasicTextField(
                            value = text,
                            onValueChange = { value ->
                                val next = if (value.text.length <= 600) {
                                    value
                                } else {
                                    value.copy(
                                        text = value.text.take(600),
                                        selection = TextRange(600),
                                    )
                                }
                                text = next
                            },
                            enabled = !submitting,
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 58.dp, max = 138.dp)
                                .focusRequester(focusRequester),
                            textStyle = inputTextStyle,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                            keyboardActions = KeyboardActions(
                                onSend = {
                                    if (canSubmit) {
                                        onSubmit(text.text, selectedPhotoUris, alsoRepost)
                                    }
                                },
                            ),
                            decorationBox = { innerTextField ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = 58.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(inputColor)
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                ) {
                                    if (text.text.isEmpty()) {
                                        Text(
                                            text = target.placeholder,
                                            style = inputTextStyle.copy(
                                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.58f),
                                            ),
                                        )
                                    }
                                    innerTextField()
                                }
                            },
                        )
                    }
                    if (selectedPhotoUris.isNotEmpty()) {
                        LazyRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 44.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            items(selectedPhotoUris, key = { it.toString() }) { uri ->
                                LocalUriThumbnail(
                                    uri = uri,
                                    modifier = Modifier
                                        .size(54.dp)
                                        .clip(RoundedCornerShape(8.dp)),
                                )
                            }
                        }
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 44.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        CommentComposerIconButton(
                            iconRes = R.drawable.ic_comment_emoji,
                            onClick = { openEmoticons() },
                            contentDescription = "表情",
                        )
                        Spacer(Modifier.width(6.dp))
                        CommentComposerTextButton(
                            text = "@",
                            onClick = { insertMention() },
                            contentDescription = "@",
                        )
                        Spacer(Modifier.width(6.dp))
                        CommentComposerIconButton(
                            iconRes = R.drawable.ic_comment_photo,
                            onClick = {
                                emoticonPanelVisible = false
                                photoPickerLauncher.launch("image/*")
                            },
                            contentDescription = "照片",
                        )
                        Spacer(Modifier.width(10.dp))
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(999.dp))
                                .clickable(
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() },
                                    onClick = { alsoRepost = !alsoRepost },
                                )
                                .padding(horizontal = 4.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(14.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (alsoRepost) sendColor else Color.Transparent,
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = if (alsoRepost) sendColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.55f),
                                        shape = CircleShape,
                                    ),
                                contentAlignment = Alignment.Center,
                            ) {
                                if (alsoRepost) {
                                    Text(
                                        text = "✓",
                                        fontSize = 9.sp,
                                        lineHeight = 9.sp,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                    )
                                }
                            }
                            Text(
                                text = "同时转发",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Spacer(Modifier.weight(1f))
                        Surface(
                            modifier = Modifier
                                .height(32.dp)
                                .widthIn(min = 66.dp)
                                .clip(RoundedCornerShape(999.dp))
                                .clickable(
                                    enabled = canSubmit,
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() },
                                    onClick = {
                                        if (canSubmit) {
                                            onSubmit(text.text, selectedPhotoUris, alsoRepost)
                                        }
                                    },
                                ),
                            shape = RoundedCornerShape(999.dp),
                            color = if (canSubmit) sendColor else Color(0xFFFFD9B5).copy(alpha = 0.58f),
                        ) {
                            Box(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                if (submitting) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(15.dp),
                                        strokeWidth = 2.dp,
                                        color = Color.White,
                                    )
                                } else {
                                    Text(
                                        text = "发送",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.White,
                                    )
                                }
                            }
                        }
                    }
                    AnimatedVisibility(visible = emoticonPanelVisible) {
                        CommentEmoticonPanel(
                            recentEntries = recentEntries,
                            allEntries = allEntries,
                            onSelect = ::appendEmoticon,
                        )
                    }
                }
            }
        }
    }
}

private const val ComposeWeiboMaxTextLength = 2000
private const val ComposeWeiboMaxPhotos = 18

@Composable
private fun isLightAppearance(): Boolean = isAppLightTheme()

@Composable
private fun hintCapsuleBorderColor(): Color =
    if (isLightAppearance()) HintCapsuleBorderColor else MaterialTheme.colorScheme.outline

@Composable
private fun hintCapsuleTextColor(): Color =
    if (isLightAppearance()) HintCapsuleText else MaterialTheme.colorScheme.onSurface

@Composable
private fun actionMenuSurfaceColor(): Color =
    if (isLightAppearance()) {
        Color(0x8AF0F0F0)
    } else {
        MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.54f)
    }

@Composable
private fun actionMenuItemTextColor(enabled: Boolean, selected: Boolean): Color = when {
    !enabled -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
    selected -> MaterialTheme.colorScheme.primary
    else -> MaterialTheme.colorScheme.onSurface
}

@Composable
private fun visibilityMenuSubtitleColor(): Color =
    if (isLightAppearance()) {
        VisibilityMenuSubtitleColor
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

@Composable
private fun hintCapsulePlaceholderColor(): Color =
    if (isLightAppearance()) HintCapsulePlaceholder else MaterialTheme.colorScheme.onSurfaceVariant

@Composable
private fun searchHistoryChipBackground(): Color =
    if (isLightAppearance()) {
        Color(0xFFECECEC)
    } else {
        MaterialTheme.colorScheme.surfaceContainerHigh
    }

@Composable
private fun searchHistoryDeleteBadgeBackground(): Color =
    if (isLightAppearance()) {
        Color(0xFF999999)
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f)
    }

@Composable
private fun composeWeiboToolbarMutedColor(): Color =
    if (isLightAppearance()) {
        Color(0xFF999999)
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ComposeWeiboScreen(
    active: Boolean,
    loggedIn: Boolean,
    screenName: String,
    avatarUrl: String?,
    submitting: Boolean,
    emoticonMap: Map<String, String>,
    recentEmoticons: List<String>,
    mentionAvatarSuggestions: List<MentionCandidate>,
    mentionNameIndex: List<MentionCandidate>,
    mentionSuggestionsLoading: Boolean,
    onEmoticonUsed: (String) -> Unit,
    onSubmit: (String, WeiboPostVisibility, List<Uri>) -> Unit,
) {
    if (!loggedIn) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .statusBarsPadding(),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "请先在设置中登录微博",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        return
    }

    var text by remember { mutableStateOf(TextFieldValue("")) }
    var visibility by remember { mutableStateOf(WeiboPostVisibility.Public) }
    var visibilityMenuVisible by remember { mutableStateOf(false) }
    var visibilityMenuAnchor by remember { mutableStateOf<Rect?>(null) }
    var displayedVisibilityAnchor by remember { mutableStateOf<Rect?>(null) }
    var visibilityChipBounds by remember { mutableStateOf(Rect.Zero) }
    var emoticonPanelVisible by remember { mutableStateOf(false) }
    var selectedPhotoUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    val focusRequester = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val accentColor = MaterialTheme.colorScheme.primary
    val inputTextStyle = feedBodyTextStyle().copy(
        color = MaterialTheme.colorScheme.onSurface,
        lineHeight = 24.sp,
    )
    val recentEntries = remember(emoticonMap, recentEmoticons) {
        recentEmoticons.mapNotNull { phrase -> emoticonMap[phrase]?.let { phrase to it } }
    }
    val allEntries = remember(emoticonMap) {
        emoticonMap.entries.sortedBy { it.key }.map { it.key to it.value }
    }
    val photoPickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetMultipleContents(),
    ) { uris ->
        if (uris.isEmpty()) return@rememberLauncherForActivityResult
        val merged = (selectedPhotoUris + uris).distinctBy { it.toString() }
        selectedPhotoUris = merged.take(ComposeWeiboMaxPhotos)
    }
    val mentionCursor = min(text.selection.start, text.selection.end)
    val activeMentionQuery = extractActiveMentionQuery(text.text, mentionCursor)
    val displayedMentionSuggestions = remember(
        text.text,
        text.selection.end,
        mentionAvatarSuggestions,
        mentionNameIndex,
    ) {
        when (val query = activeMentionQuery) {
            null -> emptyList()
            "" -> mentionAvatarSuggestions
            else -> filterMentionCandidatesByQuery(mentionNameIndex, query)
        }
    }
    val mentionPanelExpanded = activeMentionQuery != null
    val canSubmit = (text.text.trim().isNotEmpty() || selectedPhotoUris.isNotEmpty()) && !submitting

    fun insertAtCursor(value: String, requestKeyboard: Boolean = false) {
        val selection = text.selection
        val start = min(selection.start, selection.end).coerceIn(0, text.text.length)
        val end = max(selection.start, selection.end).coerceIn(0, text.text.length)
        val nextText = (text.text.substring(0, start) + value + text.text.substring(end))
            .take(ComposeWeiboMaxTextLength)
        val nextCursor = (start + value.length).coerceAtMost(nextText.length)
        text = TextFieldValue(
            text = nextText,
            selection = TextRange(nextCursor),
        )
        if (requestKeyboard) {
            emoticonPanelVisible = false
            runCatching { focusRequester.requestFocus() }
            keyboard?.show()
        }
    }

    fun appendEmoticon(phrase: String) {
        insertAtCursor(phrase)
        onEmoticonUsed(phrase)
    }

    fun insertMention() {
        insertAtCursor("@", requestKeyboard = true)
    }

    fun insertMentionUser(user: MentionCandidate) {
        val cursor = min(text.selection.start, text.selection.end).coerceIn(0, text.text.length)
        val before = text.text.substring(0, cursor)
        val after = text.text.substring(cursor)
        val atIndex = before.lastIndexOf('@')
        val mention = "@${user.name} "
        val nextText = if (atIndex >= 0) {
            before.substring(0, atIndex) + mention + after
        } else if (cursor > 0 && text.text.getOrNull(cursor - 1) == '@') {
            before + "${user.name} " + after
        } else {
            before + mention + after
        }.take(ComposeWeiboMaxTextLength)
        val nextCursor = if (atIndex >= 0) {
            (atIndex + mention.length).coerceAtMost(nextText.length)
        } else {
            (cursor + mention.length).coerceAtMost(nextText.length)
        }
        text = TextFieldValue(
            text = nextText,
            selection = TextRange(nextCursor),
        )
        runCatching { focusRequester.requestFocus() }
        keyboard?.show()
    }

    fun openEmoticons() {
        emoticonPanelVisible = !emoticonPanelVisible
        if (emoticonPanelVisible) {
            keyboard?.hide()
            focusManager.clearFocus()
        }
    }

    LaunchedEffect(visibilityMenuAnchor) {
        if (visibilityMenuAnchor != null) {
            displayedVisibilityAnchor = visibilityMenuAnchor
        }
    }

    var hasAutoFocused by remember { mutableStateOf(false) }
    LaunchedEffect(active) {
        if (!active || hasAutoFocused) return@LaunchedEffect
        hasAutoFocused = true
        delay(150)
        runCatching { focusRequester.requestFocus() }
            .onSuccess { keyboard?.show() }
    }

    val toolbarMutedColor = composeWeiboToolbarMutedColor()
    val bottomBarGap = LiquidBottomBarContentGap
    val bottomBarReserve = LiquidBottomBarReserve + bottomBarGap
    val imeBottom = WindowInsets.ime.asPaddingValues().calculateBottomPadding()
    val imeTargetBottom = WindowInsets.imeAnimationTarget.asPaddingValues().calculateBottomPadding()
    val imeInsetForLayout = maxOf(imeBottom, imeTargetBottom)
    val bottomBarClearanceTarget = maxOf(bottomBarReserve, imeInsetForLayout + bottomBarGap)
    val bottomBarClearance by animateDpAsState(
        targetValue = bottomBarClearanceTarget,
        animationSpec = tween(
            durationMillis = SearchImeFollowDurationMillis,
            easing = FastOutSlowInEasing,
        ),
        label = "compose-weibo-bottom-clearance",
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .consumeTouchEvents()
            .statusBarsPadding()
            .padding(bottom = bottomBarClearance),
    ) {
        Column(Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    RemoteImage(
                        url = avatarUrl,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop,
                        maxDecodeDim = 96,
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                        Text(
                            text = screenName,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                            lineHeight = 18.sp,
                            color = accentColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .onGloballyPositioned { coordinates ->
                                    visibilityChipBounds = coordinates.boundsInRoot()
                                }
                                .clickable(
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() },
                                    onClick = {
                                        visibilityMenuAnchor = visibilityChipBounds
                                        visibilityMenuVisible = true
                                    },
                                )
                                .padding(top = 1.dp, bottom = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp),
                        ) {
                            Text(
                                text = visibility.label,
                                fontSize = 13.sp,
                                lineHeight = 15.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Icon(
                                painter = painterResource(R.drawable.ic_chevron_down),
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(
                            enabled = canSubmit,
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() },
                            onClick = {
                                if (canSubmit) {
                                    onSubmit(text.text, visibility, selectedPhotoUris)
                                }
                            },
                        )
                        .padding(horizontal = 6.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    if (submitting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = accentColor,
                        )
                    } else {
                        Text(
                            text = "发送",
                            fontSize = 16.sp,
                            lineHeight = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (canSubmit) {
                                accentColor
                            } else {
                                accentColor.copy(alpha = 0.35f)
                            },
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                AnimatedVisibility(visible = mentionPanelExpanded && displayedMentionSuggestions.isNotEmpty()) {
                    MentionSuggestionPanel(
                        users = displayedMentionSuggestions,
                        vertical = !activeMentionQuery.isNullOrBlank(),
                        onSelect = { insertMentionUser(it) },
                    )
                }
                if (mentionSuggestionsLoading && mentionPanelExpanded && displayedMentionSuggestions.isEmpty()) {
                    Text(
                        text = "正在加载 @ 联系人…",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                ) {
                    BasicTextField(
                        value = text,
                        onValueChange = { value ->
                            val next = if (value.text.length <= ComposeWeiboMaxTextLength) {
                                value
                            } else {
                                value.copy(
                                    text = value.text.take(ComposeWeiboMaxTextLength),
                                    selection = TextRange(ComposeWeiboMaxTextLength),
                                )
                            }
                            text = next
                        },
                        enabled = !submitting,
                        modifier = Modifier
                            .fillMaxSize()
                            .focusRequester(focusRequester),
                        textStyle = inputTextStyle,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Default),
                        decorationBox = { innerTextField ->
                            Box(Modifier.fillMaxSize()) {
                                if (text.text.isEmpty()) {
                                    Text(
                                        text = "分享新鲜事儿...",
                                        style = inputTextStyle.copy(
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f),
                                        ),
                                    )
                                }
                                innerTextField()
                            }
                        },
                    )
                }

                if (selectedPhotoUris.isNotEmpty()) {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        selectedPhotoUris.forEach { uri ->
                            Box {
                                LocalUriThumbnail(
                                    uri = uri,
                                    modifier = Modifier
                                        .size(72.dp)
                                        .clip(RoundedCornerShape(8.dp)),
                                )
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .offset(x = 4.dp, y = (-4).dp)
                                        .size(18.dp)
                                        .clip(CircleShape)
                                        .background(Color.Black.copy(alpha = 0.55f))
                                        .clickable {
                                            selectedPhotoUris = selectedPhotoUris.filterNot { it == uri }
                                        },
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        text = "×",
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        lineHeight = 12.sp,
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End,
            ) {
                Text(
                    text = text.text.length.toString(),
                    fontSize = 20.sp,
                    lineHeight = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = toolbarMutedColor,
                )
                Spacer(Modifier.width(14.dp))
                CommentComposerTextButton(
                    text = "@",
                    onClick = { insertMention() },
                    contentDescription = "@",
                    color = toolbarMutedColor,
                )
                Spacer(Modifier.width(6.dp))
                CommentComposerIconButton(
                    iconRes = R.drawable.ic_comment_photo,
                    onClick = {
                        emoticonPanelVisible = false
                        photoPickerLauncher.launch("image/*")
                    },
                    contentDescription = "照片",
                    tint = toolbarMutedColor,
                )
                Spacer(Modifier.width(6.dp))
                CommentComposerIconButton(
                    iconRes = R.drawable.ic_comment_emoji,
                    onClick = { openEmoticons() },
                    contentDescription = "表情",
                    tint = toolbarMutedColor,
                )
            }

            AnimatedVisibility(visible = emoticonPanelVisible) {
                CommentEmoticonPanel(
                    recentEntries = recentEntries,
                    allEntries = allEntries,
                    onSelect = { appendEmoticon(it) },
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }
        }

        displayedVisibilityAnchor?.let { anchor ->
            ComposeVisibilityPickerOverlay(
                visible = visibilityMenuVisible,
                selected = visibility,
                anchorBounds = anchor,
                onDismiss = { visibilityMenuVisible = false },
                onExitComplete = { visibilityMenuAnchor = null },
                onSelected = { option ->
                    visibility = option
                    visibilityMenuVisible = false
                },
            )
        }
    }
}

@Composable
private fun ComposeVisibilityPickerOverlay(
    visible: Boolean,
    selected: WeiboPostVisibility,
    anchorBounds: Rect,
    onDismiss: () -> Unit,
    onExitComplete: () -> Unit,
    onSelected: (WeiboPostVisibility) -> Unit,
) {
    val backdrop = LocalLiquidMenuBackdrop.current
    val density = LocalDensity.current
    val options = WeiboPostVisibility.entries
    val menuHeight = visibilityMenuHeight(options)
    val gapFromAnchor = 4.dp
    val screenMargin = 14.dp

    BackHandler(enabled = visible) { onDismiss() }

    var overlayOriginInRoot by remember { mutableStateOf(Offset.Zero) }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .onGloballyPositioned { coordinates ->
                overlayOriginInRoot = coordinates.localToRoot(Offset.Zero)
            }
            .zIndex(20f),
    ) {
        val screenWidthPx = with(density) { maxWidth.toPx() }
        val screenHeightPx = with(density) { maxHeight.toPx() }
        val localAnchorBounds = remember(anchorBounds, overlayOriginInRoot) {
            Rect(
                left = anchorBounds.left - overlayOriginInRoot.x,
                top = anchorBounds.top - overlayOriginInRoot.y,
                right = anchorBounds.right - overlayOriginInRoot.x,
                bottom = anchorBounds.bottom - overlayOriginInRoot.y,
            )
        }
        val menuWidth = rememberVisibilityMenuWidth(options, maxWidth - screenMargin * 2)
        val menuWidthPx = with(density) { menuWidth.toPx() }
        val menuHeightPx = with(density) { menuHeight.toPx() }
        val menuPlacement = calculateVisibilityMenuOffsetPx(
            anchorBounds = localAnchorBounds,
            screenWidthPx = screenWidthPx,
            screenHeightPx = screenHeightPx,
            menuWidthPx = menuWidthPx,
            menuHeightPx = menuHeightPx,
            marginPx = with(density) { screenMargin.toPx() },
            gapPx = with(density) { gapFromAnchor.toPx() },
        )
        val originInMenu = computeActionMenuOriginInMenu(
            anchorInRoot = Offset(
                localAnchorBounds.left,
                if (menuPlacement.belowAnchor) localAnchorBounds.bottom else localAnchorBounds.top,
            ),
            menuOffset = menuPlacement.offset,
            menuWidthPx = menuWidthPx,
            menuHeightPx = menuHeightPx,
        )

        if (visible) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                        onClick = onDismiss,
                    ),
            )
        }
        ActionMenuReveal(
            visible = visible,
            menuWidth = menuWidth,
            menuHeight = menuHeight,
            originInMenu = originInMenu,
            onExitComplete = onExitComplete,
            modifier = Modifier
                .offset { menuPlacement.offset }
                .width(menuWidth)
                .height(menuHeight),
        ) {
            ImageActionFrostedCard(
                modifier = Modifier.fillMaxSize(),
                backdrop = backdrop,
                menuHeight = menuHeight,
            ) {
                options.forEach { option ->
                    VisibilityMenuRow(
                        label = option.label,
                        subtitle = option.subtitle,
                        selected = selected == option,
                        onClick = { onSelected(option) },
                    )
                }
            }
        }
    }
}

private val VisibilityMenuRowTallHeight = 56.dp
private val VisibilityMenuWidthExtra = 12.dp
private val VisibilityMenuSubtitleColor = Color(0xFF999999)

@Composable
private fun VisibilityMenuRow(
    label: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val titleStyle = actionMenuTextStyle(selected = selected)
    val titleColor = if (selected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurface
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(VisibilityMenuRowTallHeight)
            .clip(RoundedCornerShape(percent = 50))
            .clickable(onClick = onClick)
            .padding(horizontal = ActionMenuCapsulePaddingHorizontal),
        contentAlignment = Alignment.CenterStart,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
            Text(
                text = label,
                style = titleStyle,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = titleColor,
            )
            if (subtitle.isNotBlank()) {
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    lineHeight = 13.sp,
                    color = visibilityMenuSubtitleColor(),
                    maxLines = 2,
                    overflow = TextOverflow.Clip,
                )
            }
        }
    }
}

@Composable
private fun rememberVisibilityMenuWidth(
    options: List<WeiboPostVisibility>,
    maxWidth: Dp,
): Dp {
    val textMeasurer = rememberTextMeasurer()
    val titleStyle = actionMenuTextStyle()
    val subtitleStyle = MaterialTheme.typography.bodySmall.copy(
        fontSize = 11.sp,
        lineHeight = 13.sp,
    )
    val density = LocalDensity.current
    val horizontalPadding = ActionMenuCapsulePaddingHorizontal * 2 + ActionMenuCardInset * 2

    return remember(options, maxWidth, titleStyle, subtitleStyle, density) {
        val maxTextWidthPx = options.maxOf { option ->
            val titleWidth = textMeasurer.measure(
                text = option.label,
                style = titleStyle,
                maxLines = 1,
                overflow = TextOverflow.Clip,
            ).size.width
            val subtitleWidth = if (option.subtitle.isBlank()) {
                0
            } else {
                textMeasurer.measure(
                    text = option.subtitle,
                    style = subtitleStyle,
                    maxLines = 1,
                    overflow = TextOverflow.Clip,
                ).size.width
            }
            maxOf(titleWidth, subtitleWidth)
        }
        val widthDp = with(density) {
            (maxTextWidthPx.toFloat() + horizontalPadding.toPx()).toDp() + VisibilityMenuWidthExtra
        }
        minOf(widthDp, maxWidth)
    }
}

private fun visibilityMenuHeight(options: List<WeiboPostVisibility>): Dp {
    val gaps = (options.size - 1).coerceAtLeast(0)
    val rowsHeight = VisibilityMenuRowTallHeight * options.size
    return ActionMenuCardInset * 2 + rowsHeight + ActionMenuItemGap * gaps
}

private fun calculateVisibilityMenuOffsetPx(
    anchorBounds: Rect,
    screenWidthPx: Float,
    screenHeightPx: Float,
    menuWidthPx: Float,
    menuHeightPx: Float,
    marginPx: Float,
    gapPx: Float,
): ActionMenuPlacement {
    val maxX = (screenWidthPx - menuWidthPx - marginPx).coerceAtLeast(marginPx)
    val x = anchorBounds.left.coerceIn(marginPx, maxX)
    val hasSpaceAbove = anchorBounds.top - gapPx - menuHeightPx >= marginPx
    val hasSpaceBelow = anchorBounds.bottom + gapPx + menuHeightPx <= screenHeightPx - marginPx
    val (targetY, belowAnchor) = when {
        hasSpaceAbove -> anchorBounds.top - gapPx - menuHeightPx to false
        hasSpaceBelow -> anchorBounds.bottom + gapPx to true
        else -> anchorBounds.top - gapPx - menuHeightPx to false
    }
    val maxY = (screenHeightPx - menuHeightPx - marginPx).coerceAtLeast(marginPx)
    val y = targetY.coerceIn(marginPx, maxY)
    return ActionMenuPlacement(IntOffset(x.roundToInt(), y.roundToInt()), belowAnchor)
}

private fun FeedItem.hasNoLikes(): Boolean {
    val value = likesCount.trim()
    return value == "0" || value == "--" || value.equals("null", ignoreCase = true) || value.isEmpty()
}

private const val LikeUsersPageSize = 20

private fun parseApproxDisplayCount(value: String): Int? {
    val trimmed = value.trim()
    if (trimmed.isEmpty() || trimmed == "--") return null
    Regex("""^([\d.]+)万$""").find(trimmed)?.let { match ->
        val num = match.groupValues[1].toDoubleOrNull() ?: return null
        return (num * 10_000).toInt().coerceAtLeast(0)
    }
    return trimmed.toIntOrNull()
}

private fun mergeAuthoritativeLikeCount(existing: String, incoming: String): String {
    val existingCount = parseApproxDisplayCount(existing)
    val incomingCount = parseApproxDisplayCount(incoming)
    return when {
        existingCount != null && incomingCount != null && existingCount > incomingCount ->
            WeiboJsonParser.formatDisplayCount(existingCount)
        else -> incoming
    }
}

private fun resolveLikeUsersNextPage(
    page: LikeUsersPage,
    currentPage: Int,
    loadedCount: Int,
    knownTotal: Int?,
): Int? {
    if (page.users.isEmpty()) return null

    if (page.users.size >= LikeUsersPageSize) {
        return page.nextPage ?: (currentPage + 1)
    }

    page.nextPage?.let { return it }

    if (knownTotal != null && loadedCount < knownTotal) {
        return currentPage + 1
    }

    return null
}

private object LikeUsersLayout {
    val TileWidthDefault = 58.dp
    val TileMinWidth = 44.dp
    val TileGap = 8.dp
    val CardHorizontalPadding = 12.dp
    val MaxColumns = 5
    val CardMinWidth = 88.dp
}

private data class LikeUsersCardLayout(
    val cardWidth: Dp,
    val tileWidth: Dp,
    val columns: Int,
)

private fun computeLikeUsersCardLayout(
    userCount: Int,
    maxScreenWidth: Dp,
    screenMargin: Dp = 12.dp,
): LikeUsersCardLayout {
    val gap = LikeUsersLayout.TileGap
    val horizontalPadding = LikeUsersLayout.CardHorizontalPadding * 2
    val maxCardWidth = (maxScreenWidth - screenMargin * 2)
        .coerceAtLeast(LikeUsersLayout.CardMinWidth)
    val maxContentWidth = (maxCardWidth - horizontalPadding).coerceAtLeast(1.dp)
    val preferredColumns = minOf(maxOf(userCount, 1), LikeUsersLayout.MaxColumns)

    var columns = preferredColumns
    while (columns > 1) {
        val candidateTileWidth = (maxContentWidth - gap * (columns - 1)) / columns
        if (candidateTileWidth >= LikeUsersLayout.TileMinWidth) break
        columns--
    }

    val tileWidth = ((maxContentWidth - gap * (columns - 1)) / columns)
        .coerceAtMost(LikeUsersLayout.TileWidthDefault)
    val contentWidth = tileWidth * columns + gap * (columns - 1)
    val cardWidth = (contentWidth + horizontalPadding)
        .coerceIn(LikeUsersLayout.CardMinWidth, maxCardWidth)
    return LikeUsersCardLayout(
        cardWidth = cardWidth,
        tileWidth = tileWidth,
        columns = columns,
    )
}

@Composable
private fun LikeToggleCapsule(
    item: FeedItem,
    onClick: () -> Unit,
) {
    val chipColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.62f)
    val likeLabelColor = if (item.liked) StatusLikeColor else chipColor
    Box(
        modifier = Modifier
            .height(28.dp)
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, hintCapsuleBorderColor(), RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = onClick,
            )
            .padding(horizontal = 10.dp),
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

@Composable
private fun LikeUsersOverlay(
    item: FeedItem,
    anchorBounds: Rect,
    loading: Boolean,
    loadingMore: Boolean,
    hasMore: Boolean,
    users: List<MentionCandidate>,
    error: String?,
    onDismiss: () -> Unit,
    onLoadMore: () -> Unit,
    onToggleLike: () -> Unit,
    onUserClick: (String) -> Unit,
) {
    BackHandler { onDismiss() }
    val showEmptyCapsule = users.isEmpty() && (item.hasNoLikes() || !loading)
    val emptyMessage = error ?: "暂无点赞"

    if (showEmptyCapsule) {
        LaunchedEffect(emptyMessage) {
            delay(1800)
            onDismiss()
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .zIndex(580f)
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = onDismiss,
                ),
            contentAlignment = Alignment.Center,
        ) {
            AnimatedVisibility(
                visible = true,
                enter = fadeIn(tween(120)) + scaleIn(
                    initialScale = 0.92f,
                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                ),
                exit = fadeOut(tween(140)) + scaleOut(targetScale = 0.94f),
            ) {
                OpaqueHintCapsule {
                    Text(
                        text = emptyMessage,
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = hintCapsuleTextColor(),
                    )
                }
            }
        }
        return
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .zIndex(580f)
            .background(Color.Black.copy(alpha = 0.18f))
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = onDismiss,
            ),
    ) {
        val density = LocalDensity.current
        val margin = 12.dp
        val gap = 8.dp
        val layoutUserCount = when {
            users.size >= LikeUsersLayout.MaxColumns -> LikeUsersLayout.MaxColumns
            users.isNotEmpty() -> users.size
            loading -> LikeUsersLayout.MaxColumns
            else -> 1
        }
        val cardLayout = computeLikeUsersCardLayout(
            userCount = layoutUserCount,
            maxScreenWidth = maxWidth,
            screenMargin = margin,
        )
        val cardWidth = cardLayout.cardWidth
        val cardMaxHeight = 240.dp
        val placement = calculateActionMenuOffsetFromAnchorPx(
            anchorBounds = anchorBounds,
            screenWidthPx = with(density) { maxWidth.toPx() },
            screenHeightPx = with(density) { maxHeight.toPx() },
            menuWidthPx = with(density) { cardWidth.toPx() },
            menuHeightPx = with(density) { cardMaxHeight.toPx() },
            marginPx = with(density) { margin.toPx() },
            gapPx = with(density) { gap.toPx() },
        )
        val cardShape = RoundedCornerShape(16.dp)
        Surface(
            modifier = Modifier
                .offset { placement.offset }
                .width(cardWidth)
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = {},
                ),
            shape = cardShape,
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 6.dp,
            border = BorderStroke(1.dp, hintCapsuleBorderColor()),
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    LikeToggleCapsule(
                        item = item,
                        onClick = onToggleLike,
                    )
                }
                if (loading && users.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(88.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                    }
                } else {
                    LikeUsersGrid(
                        users = users,
                        columns = cardLayout.columns,
                        tileWidth = cardLayout.tileWidth,
                        hasMore = hasMore,
                        loadingMore = loadingMore,
                        onLoadMore = onLoadMore,
                        onUserClick = onUserClick,
                    )
                }
            }
        }
    }
}

@Composable
private fun LikeUsersGrid(
    users: List<MentionCandidate>,
    columns: Int,
    tileWidth: Dp,
    hasMore: Boolean,
    loadingMore: Boolean,
    onLoadMore: () -> Unit,
    onUserClick: (String) -> Unit,
) {
    val scrollState = rememberScrollState()
    val shouldLoadMore by remember {
        derivedStateOf {
            hasMore &&
                !loadingMore &&
                scrollState.maxValue > 0 &&
                scrollState.value >= scrollState.maxValue - 48
        }
    }
    LaunchedEffect(shouldLoadMore) {
        snapshotFlow { shouldLoadMore }
            .distinctUntilChanged()
            .filter { it }
            .collect { onLoadMore() }
    }
    LaunchedEffect(users.size, hasMore, loadingMore, scrollState.maxValue) {
        if (!hasMore || loadingMore || users.isEmpty()) return@LaunchedEffect
        if (scrollState.maxValue == 0) {
            onLoadMore()
            return@LaunchedEffect
        }
        if (scrollState.value >= scrollState.maxValue - 48) {
            onLoadMore()
        }
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 210.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(LikeUsersLayout.TileGap),
    ) {
        users.chunked(columns).forEachIndexed { rowIndex, rowUsers ->
            key(rowIndex) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(LikeUsersLayout.TileGap),
                ) {
                    rowUsers.forEach { user ->
                        key(mentionCandidateKey(user)) {
                            MentionSuggestionAvatarTile(
                                user = user,
                                tileWidth = tileWidth,
                                onSelect = { onUserClick(user.id) },
                            )
                        }
                    }
                }
            }
        }
        if (loadingMore) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(32.dp),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
            }
        }
    }
}

@Composable
private fun MentionSuggestionPanel(
    users: List<MentionCandidate>,
    vertical: Boolean = false,
    onSelect: (MentionCandidate) -> Unit,
) {
    val panelModifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(14.dp))
        .background(MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.56f))
        .padding(horizontal = 8.dp, vertical = 8.dp)

    if (vertical) {
        LazyColumn(
            flingBehavior = rememberWeiboListFlingBehavior(),
            modifier = panelModifier.heightIn(max = 196.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            items(users, key = { mentionCandidateKey(it) }) { user ->
                MentionSuggestionRow(user = user, onSelect = onSelect)
            }
        }
    } else {
        LazyRow(
            modifier = panelModifier,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(users, key = { mentionCandidateKey(it) }) { user ->
                MentionSuggestionAvatarTile(user = user, onSelect = onSelect)
            }
        }
    }
}

@Composable
private fun MentionSuggestionAvatarTile(
    user: MentionCandidate,
    onSelect: (MentionCandidate) -> Unit,
    tileWidth: Dp = 58.dp,
) {
    Column(
        modifier = Modifier
            .width(tileWidth)
            .clip(RoundedCornerShape(12.dp))
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = { onSelect(user) },
            )
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        RemoteImage(
            url = user.avatarUrl,
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop,
            maxDecodeDim = 96,
        )
        Text(
            text = user.name,
            fontSize = 11.sp,
            lineHeight = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun MentionSuggestionRow(
    user: MentionCandidate,
    onSelect: (MentionCandidate) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = { onSelect(user) })
            .padding(horizontal = 6.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        RemoteImage(
            url = user.avatarUrl,
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop,
            maxDecodeDim = 96,
        )
        Text(
            text = user.name,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun LocalUriThumbnail(
    uri: Uri,
    modifier: Modifier = Modifier,
) {
    AndroidView(
        modifier = modifier.background(MaterialTheme.colorScheme.surfaceContainerHighest),
        factory = { context ->
            ImageView(context).apply {
                scaleType = ImageView.ScaleType.CENTER_CROP
                setImageURI(uri)
            }
        },
        update = { imageView ->
            imageView.setImageURI(uri)
        },
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CommentEmoticonPanel(
    recentEntries: List<Pair<String, String>>,
    allEntries: List<Pair<String, String>>,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val displayedRecentEntries = recentEntries.take(14)
    val recentPhrases = displayedRecentEntries.map { it.first }.toSet()
    val remainingEntries = allEntries.filter { it.first !in recentPhrases }
    Column(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(max = 260.dp)
            .verticalScroll(rememberScrollState())
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.62f))
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (allEntries.isEmpty()) {
            Text(
                text = "请先到设置中同步微博表情",
                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp,
            )
            return@Column
        }
        if (displayedRecentEntries.isNotEmpty()) {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.Start),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                displayedRecentEntries.forEach { (phrase, url) ->
                    CommentEmoticonTile(
                        phrase = phrase,
                        url = url,
                        onSelect = onSelect,
                    )
                }
            }
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.16f),
            )
        }
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.Start),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            remainingEntries.forEach { (phrase, url) ->
                CommentEmoticonTile(
                    phrase = phrase,
                    url = url,
                    onSelect = onSelect,
                )
            }
        }
    }
}

@Composable
private fun CommentEmoticonTile(
    phrase: String,
    url: String,
    onSelect: (String) -> Unit,
) {
    Column(
        modifier = Modifier
            .width(42.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = { onSelect(phrase) },
            )
            .padding(vertical = 3.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Box(
            modifier = Modifier.size(28.dp),
            contentAlignment = Alignment.Center,
        ) {
            EmojiImage(url = url)
        }
        Text(
            text = emoticonDisplayLabel(phrase),
            fontSize = 8.sp,
            lineHeight = 9.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun CommentComposerTextButton(
    text: String,
    contentDescription: String,
    onClick: () -> Unit,
    color: Color = MaterialTheme.colorScheme.onSurfaceVariant,
) {
    Box(
        modifier = Modifier
            .size(30.dp)
            .clip(CircleShape)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            fontSize = 20.sp,
            lineHeight = 20.sp,
            fontWeight = FontWeight.SemiBold,
            color = color,
        )
    }
}

@Composable
private fun CommentComposerIconButton(
    iconRes: Int,
    contentDescription: String,
    onClick: () -> Unit,
    tint: Color = MaterialTheme.colorScheme.onSurfaceVariant,
) {
    Box(
        modifier = Modifier
            .size(30.dp)
            .clip(CircleShape)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = contentDescription,
            modifier = Modifier.size(20.dp),
            tint = tint,
        )
    }
}

@Composable
private fun CommentRow(
    comment: CommentItem,
    depth: Int = 0,
    onUserClick: ((String) -> Unit)? = null,
    onExpandNestedComments: ((String) -> Unit)? = null,
    nestedCommentsLoadingIds: Set<String> = emptySet(),
    onReplyClick: ((CommentItem) -> Unit)? = null,
) {
    val resolvedMap = comment.emoticons.ifEmpty { emptyMap() }
    val authorTarget = comment.authorId.takeIf { it.isNotBlank() } ?: comment.authorName
    val verticalPadding = if (depth == 0) 4.dp else 2.dp
    val rowStart = CommentRowOuterStart + (depth * 24).dp
    val showReplyHeader = depth == 0 &&
        comment.replyToAuthor != null &&
        !commentTextContainsReplyTo(comment.text, comment.replyToAuthor)
    val nestedContentStart = rowStart + CommentAvatarSize + CommentAvatarTextGap
    var requestCommentImageOpenIndex by remember(comment.id) { mutableStateOf<Int?>(null) }
    val pictureCommentText = comment.text.trim() == "图片评论" && comment.images.isNotEmpty()
    val haptic = LocalHapticFeedback.current
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
            Column(
                modifier = Modifier
                    .weight(1f)
                    .then(
                        if (onReplyClick != null) {
                            Modifier.combinedClickable(
                                onClick = {},
                                onLongClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onReplyClick(comment)
                                },
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() },
                            )
                        } else {
                            Modifier
                        },
                    ),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
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
                    style = feedBodyTextStyle(),
                    onUserClick = onUserClick,
                    modifier = if (pictureCommentText && comment.images.isNotEmpty()) {
                        Modifier.clickable { requestCommentImageOpenIndex = 0 }
                    } else {
                        Modifier
                    },
                )
                if (comment.images.isNotEmpty()) {
                    CommentImageStrip(
                        images = comment.images,
                        requestOpenIndex = requestCommentImageOpenIndex,
                        onRequestOpenConsumed = { requestCommentImageOpenIndex = null },
                    )
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
        sortNestedCommentsByTime(comment.comments).forEach { nested ->
            CommentRow(
                comment = nested,
                depth = depth + 1,
                onUserClick = onUserClick,
                onExpandNestedComments = onExpandNestedComments,
                nestedCommentsLoadingIds = nestedCommentsLoadingIds,
                onReplyClick = onReplyClick,
            )
        }
        val nestedActionText = comment.moreInfoText
            ?: comment.nestedNextCursor?.let { "\u52A0\u8F7D\u66F4\u591A" }
        nestedActionText?.let { actionText ->
            val isLoadingNested = comment.id in nestedCommentsLoadingIds
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = nestedContentStart, end = 18.dp, top = 2.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                if (isLoadingNested) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(12.dp),
                        strokeWidth = 1.5.dp,
                    )
                }
                Text(
                    text = if (isLoadingNested) "\u52A0\u8F7D\u4E2D..." else actionText,
                    fontSize = CommentAuthorFontSize,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable(
                        enabled = !isLoadingNested && onExpandNestedComments != null,
                        onClick = { onExpandNestedComments?.invoke(comment.id) },
                    ),
                )
            }
        }
    }
}

@Composable
private fun CommentImageStrip(
    images: List<FeedImage>,
    requestOpenIndex: Int? = null,
    onRequestOpenConsumed: () -> Unit = {},
) {
    if (images.isEmpty()) return

    var viewerOpen by remember { mutableStateOf(false) }
    var viewerIndex by remember { mutableStateOf(0) }
    var thumbnailBoundsByIndex by remember { mutableStateOf<Map<Int, Rect>>(emptyMap()) }
    var viewerSourceBoundsByIndex by remember { mutableStateOf<Map<Int, Rect>>(emptyMap()) }
    var viewerAnimateOpenFromSource by remember { mutableStateOf(true) }
    var viewerDismissHook by remember { mutableStateOf<(() -> Unit)?>(null) }

    fun openImageViewer(
        index: Int,
        sourceBounds: Rect?,
        onClosed: (() -> Unit)? = null,
    ) {
        viewerIndex = index
        if (sourceBounds != null) {
            thumbnailBoundsByIndex = thumbnailBoundsByIndex + (index to sourceBounds)
        }
        viewerSourceBoundsByIndex = thumbnailBoundsByIndex
        viewerAnimateOpenFromSource = sourceBounds != null
        viewerDismissHook = onClosed
        viewerOpen = true
    }

    LaunchedEffect(requestOpenIndex) {
        requestOpenIndex?.let { index ->
            openImageViewer(index.coerceIn(0, images.lastIndex), null)
            onRequestOpenConsumed()
        }
    }

    Column {
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
                    onOpenViewer = { cellIndex, bounds, onClosed, _ ->
                        openImageViewer(cellIndex, bounds, onClosed)
                    },
                    onAnchorBoundsChanged = { bounds ->
                        thumbnailBoundsByIndex = thumbnailBoundsByIndex + (index to bounds)
                    },
                )
            }
        }

        if (viewerOpen) {
            FullscreenImageViewer(
                images = images,
                initialIndex = viewerIndex,
                sourceBoundsByIndex = viewerSourceBoundsByIndex,
                animateOpenFromSource = viewerAnimateOpenFromSource,
                onDismiss = {
                    viewerOpen = false
                    viewerSourceBoundsByIndex = emptyMap()
                    viewerAnimateOpenFromSource = true
                    viewerDismissHook?.invoke()
                    viewerDismissHook = null
                },
            )
        }
    }
}

@Composable
private fun SearchModeToggle(
    selected: SearchMode,
    onSelected: (SearchMode) -> Unit,
) {
    val metaColor = weiboMetaTextColor()
    val nextMode = if (selected == SearchMode.Weibo) SearchMode.User else SearchMode.Weibo

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = { onSelected(nextMode) },
            )
            .padding(horizontal = 4.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        CommentSortLinesIcon(tint = metaColor)
        Text(
            text = selected.label,
            style = MaterialTheme.typography.bodySmall,
            color = metaColor,
        )
    }
}

@Composable
private fun SearchWeiboSortToggle(
    selected: SearchWeiboSort,
    onSelected: (SearchWeiboSort) -> Unit,
) {
    val metaColor = weiboMetaTextColor()
    val nextSort = if (selected == SearchWeiboSort.Comprehensive) {
        SearchWeiboSort.Realtime
    } else {
        SearchWeiboSort.Comprehensive
    }

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = { onSelected(nextSort) },
            )
            .padding(horizontal = 4.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        CommentSortLinesIcon(tint = metaColor)
        Text(
            text = selected.label,
            style = MaterialTheme.typography.bodySmall,
            color = metaColor,
        )
    }
}

@Composable
private fun SearchCapsuleField(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    mode: SearchMode,
    onModeChange: (SearchMode) -> Unit,
    onSearch: () -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "搜索微博、话题和用户",
) {
    val focusRequester = remember { FocusRequester() }
    val fieldTextStyle = MaterialTheme.typography.bodyMedium.copy(
        color = MaterialTheme.colorScheme.onSurface,
        fontSize = 15.sp,
        lineHeight = 20.sp,
    )
    val placeholderStyle = fieldTextStyle.copy(color = hintCapsulePlaceholderColor())
    SurfaceLiquidCapsule(
        modifier = modifier,
        pill = true,
        useMenuGlassStyle = true,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .padding(start = 16.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SearchModeToggle(
                selected = mode,
                onSelected = onModeChange,
            )
            Spacer(Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
            ) {
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                        .focusRequester(focusRequester),
                    textStyle = fieldTextStyle,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { onSearch() }),
                    decorationBox = { innerTextField ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight(),
                            contentAlignment = Alignment.CenterStart,
                        ) {
                            if (value.text.isEmpty()) {
                                Text(
                                    text = placeholder,
                                    style = placeholderStyle,
                                    maxLines = 1,
                                )
                            }
                            innerTextField()
                        }
                    },
                )
            }
            if (value.text.isNotEmpty()) {
                TextButton(
                    onClick = onClear,
                    contentPadding = PaddingValues(horizontal = 8.dp),
                ) {
                    Text(
                        text = "清除",
                        style = fieldTextStyle.copy(color = HintCapsulePlaceholder),
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchSuggestionPanel(
    suggestions: SearchSuggestResult,
    visible: Boolean,
    loading: Boolean,
    onSuggestionClick: (String, SearchMode) -> Unit,
    onUserClick: (SearchUserItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    val querySuggestions = suggestions.hotQueries.distinct().take(5)
    val userSuggestions = suggestions.users.take(3)
    AnimatedVisibility(
        visible = visible && (loading || querySuggestions.isNotEmpty() || userSuggestions.isNotEmpty()),
        enter = fadeIn(tween(120)) + slideInVertically(tween(140)) { it / 4 },
        exit = fadeOut(tween(100)),
        modifier = modifier,
    ) {
        SurfaceLiquidCapsule(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 14.dp,
            useMenuGlassStyle = true,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
            ) {
                if (loading && querySuggestions.isEmpty() && userSuggestions.isEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(42.dp)
                            .padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        Text(
                            text = "正在获取联想词",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                querySuggestions.forEach { query ->
                    SearchSuggestionTextRow(
                        text = query,
                        onClick = { onSuggestionClick(query, SearchMode.Weibo) },
                    )
                }
                userSuggestions.forEach { user ->
                    SearchSuggestionUserRow(
                        user = user,
                        onClick = { onUserClick(user) },
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchSuggestionTextRow(
    text: String,
    onClick: () -> Unit,
) {
    Text(
        text = text,
        modifier = Modifier
            .fillMaxWidth()
            .height(38.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp)
            .wrapContentHeight(Alignment.CenterVertically),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurface,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

@Composable
private fun SearchSuggestionUserRow(
    user: SearchUserItem,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(46.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        RemoteImage(
            url = user.avatarUrl,
            modifier = Modifier
                .size(30.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop,
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = user.screenName.ifBlank { user.name },
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = user.description.ifBlank { user.followersCount },
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Text(
            text = "用户",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

private val SearchHistoryChipRadius = 8.dp
private val SearchHistoryChipMaxWidth = 168.dp
private val SearchHistoryTitleToChipsGap = 2.dp

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SearchHistoryChipFlow(
    queries: List<String>,
    maxLines: Int,
    deleteHistoryQuery: String?,
    onDeleteHistoryQueryChange: (String?) -> Unit,
    onHistoryClick: (String) -> Unit,
    onHistoryDelete: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        maxLines = maxLines,
    ) {
        queries.forEach { query ->
            key(query) {
                SearchHistoryChip(
                    query = query,
                    showDelete = deleteHistoryQuery == query,
                    onClick = {
                        onDeleteHistoryQueryChange(null)
                        onHistoryClick(query)
                    },
                    onLongClick = { onDeleteHistoryQueryChange(query) },
                    onDelete = {
                        onDeleteHistoryQueryChange(null)
                        onHistoryDelete(query)
                    },
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SearchHistoryChipsSection(
    searchHistory: List<String>,
    searchHistoryExpanded: Boolean,
    deleteHistoryQuery: String?,
    onDeleteHistoryQueryChange: (String?) -> Unit,
    onHistoryClick: (String) -> Unit,
    onHistoryDelete: (String) -> Unit,
    onHistoryExpandToggle: () -> Unit,
) {
    val maxRows = SearchHistoryStore.DISPLAY_MAX_ROWS

    SubcomposeLayout(Modifier.fillMaxWidth()) { constraints ->
        val fullHeight = subcompose("measure-full") {
            SearchHistoryChipFlow(
                queries = searchHistory,
                maxLines = Int.MAX_VALUE,
                deleteHistoryQuery = deleteHistoryQuery,
                onDeleteHistoryQueryChange = onDeleteHistoryQueryChange,
                onHistoryClick = onHistoryClick,
                onHistoryDelete = onHistoryDelete,
            )
        }.first().measure(constraints).height

        val cappedHeight = subcompose("measure-capped") {
            SearchHistoryChipFlow(
                queries = searchHistory,
                maxLines = maxRows,
                deleteHistoryQuery = deleteHistoryQuery,
                onDeleteHistoryQueryChange = onDeleteHistoryQueryChange,
                onHistoryClick = onHistoryClick,
                onHistoryDelete = onHistoryDelete,
            )
        }.first().measure(constraints).height

        val exceeds = fullHeight > cappedHeight

        subcompose("sync-collapse") {
            LaunchedEffect(exceeds, searchHistoryExpanded) {
                if (!exceeds && searchHistoryExpanded) {
                    onHistoryExpandToggle()
                }
            }
        }.forEach { it.measure(constraints.copy(minWidth = 0, maxWidth = 0, minHeight = 0, maxHeight = 0)) }

        val visiblePlaceable = subcompose("visible") {
            Column(modifier = Modifier.fillMaxWidth()) {
                SearchHistoryChipFlow(
                    queries = searchHistory,
                    maxLines = if (searchHistoryExpanded) Int.MAX_VALUE else maxRows,
                    deleteHistoryQuery = deleteHistoryQuery,
                    onDeleteHistoryQueryChange = onDeleteHistoryQueryChange,
                    onHistoryClick = onHistoryClick,
                    onHistoryDelete = onHistoryDelete,
                )
                if (exceeds) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = if (searchHistoryExpanded) "收起" else "展开更多",
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .padding(end = 12.dp)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = onHistoryExpandToggle,
                                )
                                .padding(vertical = 2.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }
        }.first().measure(constraints)

        layout(visiblePlaceable.width, visiblePlaceable.height) {
            visiblePlaceable.place(0, 0)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SearchHistoryChip(
    query: String,
    showDelete: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onDelete: () -> Unit,
) {
    val chipShape = RoundedCornerShape(SearchHistoryChipRadius)
    val chipBackground = searchHistoryChipBackground()
    val deleteBadgeBackground = searchHistoryDeleteBadgeBackground()
    Box(
        modifier = Modifier
            .widthIn(max = SearchHistoryChipMaxWidth)
            .clip(chipShape)
            .background(chipBackground)
            .combinedClickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
                onLongClick = onLongClick,
            ),
    ) {
        Text(
            text = query,
            modifier = Modifier.padding(
                start = 12.dp,
                end = if (showDelete) 26.dp else 12.dp,
                top = 8.dp,
                bottom = 8.dp,
            ),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (showDelete) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 4.dp, end = 4.dp)
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(deleteBadgeBackground)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onDelete,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "×",
                    color = MaterialTheme.colorScheme.surface,
                    fontSize = 12.sp,
                    lineHeight = 12.sp,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

private data class HotSearchBadgeColors(
    val container: Color,
    val content: Color,
)

private fun hotSearchBadgeColors(label: String): HotSearchBadgeColors {
    val normalized = label.trim()
    val content = when {
        "爆" in normalized -> Color(0xFFFF3852)
        "沸" in normalized -> Color(0xFFFF6A00)
        "热" in normalized -> Color(0xFFFF8A00)
        "新" in normalized -> Color(0xFFFF4081)
        "荐" in normalized -> Color(0xFF2F80ED)
        "榜" in normalized -> Color(0xFFFF8A00)
        else -> Color(0xFFFF8A00)
    }
    return HotSearchBadgeColors(
        container = content.copy(alpha = 0.12f),
        content = content,
    )
}

@Composable
private fun HotSearchRow(
    rank: Int,
    item: HotSearchItem,
    onClick: () -> Unit,
) {
    val rankColor = when (rank) {
        1 -> Color(0xFFFF6B00)
        2 -> Color(0xFFFFA726)
        3 -> Color(0xFF6CB4EE)
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    val displayWord = item.word.removePrefix("#").removeSuffix("#")
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = rank.toString(),
            modifier = Modifier.width(22.dp),
            color = rankColor,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
        )
        Text(
            text = displayWord,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (item.label.isNotBlank()) {
            val badgeColors = hotSearchBadgeColors(item.label)
            Surface(
                color = badgeColors.container,
                shape = RoundedCornerShape(6.dp),
            ) {
                Text(
                    text = item.label,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = badgeColors.content,
                    maxLines = 1,
                )
            }
        }
    }
}

private const val SearchImeFollowDurationMillis = 120

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SearchScreen(
    session: WeiboWebSession,
    searchBarOverlay: SearchBarOverlayController,
    searchHistoryStore: SearchHistoryStore,
    searchBarVisible: Boolean,
    hasLoginCookie: Boolean,
    pendingQuery: String?,
    onPendingQueryConsumed: () -> Unit,
    pendingSearchMode: SearchMode?,
    onPendingSearchModeConsumed: () -> Unit,
    emoticonMap: Map<String, String>,
    listState: LazyListState,
    resultsBackEnabled: Boolean,
    onResultsNavigateBack: (() -> Unit)?,
    onItemClick: (FeedItem, Rect?) -> Unit,
    onMediaClick: (FeedMedia, String) -> Unit,
    onUserClick: (String) -> Unit,
    resolveFeedItem: (FeedItem) -> FeedItem,
    isLongTextLoading: (FeedItem) -> Boolean,
    onLoadLongText: (FeedItem) -> Unit,
    onToggleLike: (FeedItem) -> Unit,
    onLikeClick: (FeedItem, Rect) -> Unit,
    onUrlEntityClick: (FeedUrlEntity) -> Unit,
    onOpenLoginSettings: () -> Unit,
    mineProfileId: String?,
    searchMode: SearchMode,
    onSearchModeChange: (SearchMode) -> Unit,
    weiboSort: SearchWeiboSort,
    onWeiboSortChange: (SearchWeiboSort) -> Unit,
) {
    val scope = rememberCoroutineScope()
    val showMessage = LocalUiMessenger.current
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val topInset = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    var activeQuery by remember { mutableStateOf<String?>(null) }
    var activeSearchMode by remember { mutableStateOf(searchMode) }
    var hotSearchItems by remember { mutableStateOf<List<HotSearchItem>>(emptyList()) }
    var hotSearchLoading by remember { mutableStateOf(false) }
    var hotSearchError by remember { mutableStateOf<String?>(null) }
    var resultItems by remember { mutableStateOf<List<FeedItem>>(emptyList()) }
    var userResultItems by remember { mutableStateOf<List<RelationUser>>(emptyList()) }
    var resultLoading by remember { mutableStateOf(false) }
    var resultLoadingMore by remember { mutableStateOf(false) }
    var resultError by remember { mutableStateOf<String?>(null) }
    var resultNextPage by remember { mutableStateOf<String?>(null) }
    var searchGeneration by remember { mutableIntStateOf(0) }
    var initialResultsReady by remember { mutableStateOf(false) }
    var searchPullRefreshing by remember { mutableStateOf(false) }
    var searchHistory by remember { mutableStateOf(searchHistoryStore.read()) }
    var searchHistoryExpanded by remember { mutableStateOf(false) }
    var deleteHistoryQuery by remember { mutableStateOf<String?>(null) }
    var suggestResult by remember { mutableStateOf(SearchSuggestResult()) }
    var suggestForQuery by remember { mutableStateOf("") }
    var suggestLoading by remember { mutableStateOf(false) }
    var suggestLoadingForQuery by remember { mutableStateOf("") }
    var suggestRequestGeneration by remember { mutableIntStateOf(0) }
    var searchDraft by remember { mutableStateOf(searchBarOverlay.queryInput) }
    val latestSearchDraft by rememberUpdatedState(searchDraft)
    val searchDraftText = searchDraft.text

    LaunchedEffect(searchHistory, searchHistoryExpanded) {
        if (deleteHistoryQuery != null && deleteHistoryQuery !in searchHistory) {
            deleteHistoryQuery = null
        }
    }

    fun publishSuggestionOverlay(term: String = searchDraft.text.trim()) {
        searchBarOverlay.suggestions = if (suggestForQuery == term) {
            suggestResult
        } else {
            SearchSuggestResult()
        }
        searchBarOverlay.suggestionsLoading = suggestLoading &&
            suggestLoadingForQuery == term &&
            term.isNotBlank()
        searchBarOverlay.suggestionsVisible = searchBarVisible &&
            term.isNotBlank() &&
            activeQuery != term
    }

    fun resetSearchResults(clearActiveQuery: Boolean) {
        if (clearActiveQuery) {
            activeQuery = null
        }
        searchGeneration++
        resultItems = emptyList()
        userResultItems = emptyList()
        resultError = null
        resultNextPage = null
        resultLoading = false
        resultLoadingMore = false
        initialResultsReady = false
    }

    fun updateSearchDraft(value: TextFieldValue) {
        val oldTerm = searchDraft.text.trim()
        val newTerm = value.text.trim()
        searchDraft = value
        searchBarOverlay.queryInput = value
        if (newTerm != oldTerm) {
            suggestRequestGeneration++
            suggestLoading = false
            suggestLoadingForQuery = ""
            suggestResult = SearchSuggestResult()
            suggestForQuery = ""
            publishSuggestionOverlay(newTerm)
        }
        if (activeQuery != null && value.text.trim() != activeQuery) {
            resetSearchResults(clearActiveQuery = true)
        }
    }

    fun clearSearchResults() {
        searchDraft = TextFieldValue("")
        searchBarOverlay.queryInput = TextFieldValue("")
        activeQuery = null
        activeSearchMode = searchMode
        resetSearchResults(clearActiveQuery = true)
        scope.launch { runCatching { listState.scrollToItem(0) } }
    }

    BackHandler(enabled = activeQuery != null && resultsBackEnabled) {
        focusManager.clearFocus(force = true)
        keyboardController?.hide()
        if (onResultsNavigateBack != null) {
            onResultsNavigateBack()
        } else {
            clearSearchResults()
        }
    }

    fun normalizeTopic(raw: String): String =
        raw.trim()

    fun submitQuery(raw: String, modeOverride: SearchMode? = null) {
        val normalized = normalizeTopic(raw)
        if (normalized.isBlank()) return
        searchHistory = searchHistoryStore.touch(normalized)
        searchDraft = TextFieldValue(normalized, TextRange(normalized.length))
        searchBarOverlay.queryInput = searchDraft
        resetSearchResults(clearActiveQuery = false)
        activeSearchMode = modeOverride ?: searchMode
        activeQuery = normalized
    }

    fun changeWeiboSort(sort: SearchWeiboSort) {
        if (sort == weiboSort) return
        onWeiboSortChange(sort)
        if (activeSearchMode == SearchMode.Weibo && activeQuery != null) {
            resultItems = emptyList()
            resultError = null
            resultNextPage = null
            resultLoading = false
            resultLoadingMore = false
            initialResultsReady = false
            searchGeneration++
        }
    }

    suspend fun reloadHotSearch() {
        if (!hasLoginCookie) return
        hotSearchLoading = true
        hotSearchError = null
        runCatching { session.loadHotSearch() }
            .onSuccess { hotSearchItems = it }
            .onFailure { hotSearchError = it.message ?: "热搜加载失败" }
        hotSearchLoading = false
    }

    suspend fun loadTopicResults(reset: Boolean, generation: Int) {
        val query = activeQuery ?: return
        if (!hasLoginCookie) return
        if (generation != searchGeneration) return
        if (reset) {
            resultLoading = true
            resultError = null
            resultNextPage = null
            resultItems = emptyList()
            userResultItems = emptyList()
            initialResultsReady = false
            runCatching { listState.scrollToItem(0) }
        } else {
            val nextPage = resultNextPage?.toIntOrNull() ?: return
            if (resultLoading || resultLoadingMore || !initialResultsReady) return
            resultLoadingMore = true
        }
        try {
            val page = if (reset) 1 else resultNextPage?.toIntOrNull() ?: return
            if (activeSearchMode == SearchMode.User) {
                val pageResult = session.loadWeiboUserSearch(query, page)
                if (generation != searchGeneration) return
                if (reset) {
                    userResultItems = pageResult.items.map { it.toRelationUser() }
                    resultNextPage = pageResult.nextCursor
                } else {
                    val merged = (userResultItems + pageResult.items.map { it.toRelationUser() })
                        .distinctBy { it.id.ifBlank { it.screenName } }
                    if (merged.size == userResultItems.size) {
                        resultNextPage = null
                    } else {
                        userResultItems = merged
                        resultNextPage = pageResult.nextCursor
                    }
                }
            } else {
                val pageResult = session.loadWeiboSearch(
                    query = query,
                    page = page,
                    realtime = weiboSort == SearchWeiboSort.Realtime,
                )
                if (generation != searchGeneration) return
                if (reset) {
                    resultItems = pageResult.items
                    resultNextPage = pageResult.nextCursor
                } else {
                    val merged = (resultItems + pageResult.items).distinctBy { it.id }
                    if (merged.size == resultItems.size) {
                        resultNextPage = null
                    } else {
                        resultItems = merged
                        resultNextPage = pageResult.nextCursor
                    }
                }
            }
            resultError = null
            initialResultsReady = true
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            if (generation != searchGeneration) return
            if (reset) {
                resultError = error.message ?: "搜索失败"
                resultItems = emptyList()
                userResultItems = emptyList()
                resultNextPage = null
                initialResultsReady = true
            } else {
                resultNextPage = null
            }
        } finally {
            if (generation == searchGeneration) {
                if (reset) {
                    resultLoading = false
                } else {
                    resultLoadingMore = false
                }
            }
        }
    }

    LaunchedEffect(hasLoginCookie) {
        if (hasLoginCookie) reloadHotSearch()
    }

    LaunchedEffect(pendingQuery, pendingSearchMode) {
        val pending = pendingQuery?.let(::normalizeTopic)?.takeIf { it.isNotBlank() } ?: return@LaunchedEffect
        submitQuery(pending, modeOverride = pendingSearchMode)
        onPendingQueryConsumed()
        onPendingSearchModeConsumed()
    }

    LaunchedEffect(activeQuery, searchGeneration, hasLoginCookie, activeSearchMode, weiboSort) {
        val query = activeQuery ?: return@LaunchedEffect
        if (!hasLoginCookie) return@LaunchedEffect
        loadTopicResults(reset = true, generation = searchGeneration)
    }

    LaunchedEffect(
        searchDraftText,
        activeQuery,
        searchBarVisible,
        hasLoginCookie,
    ) {
        val requestGeneration = ++suggestRequestGeneration
        if (!searchBarVisible || !hasLoginCookie || activeQuery != null) {
            suggestLoading = false
            suggestLoadingForQuery = ""
            suggestResult = SearchSuggestResult()
            suggestForQuery = ""
            publishSuggestionOverlay("")
            return@LaunchedEffect
        }
        val term = searchDraftText.trim()
        if (term.isBlank()) {
            suggestLoading = false
            suggestLoadingForQuery = ""
            suggestResult = SearchSuggestResult()
            suggestForQuery = ""
            publishSuggestionOverlay("")
            return@LaunchedEffect
        }
        if (term != suggestForQuery) {
            suggestResult = SearchSuggestResult()
        }
        suggestLoading = true
        suggestLoadingForQuery = term
        publishSuggestionOverlay(term)
        try {
            delay(80)
            if (requestGeneration != suggestRequestGeneration) return@LaunchedEffect
            val latestTerm = latestSearchDraft.text.trim()
            if (latestTerm.isBlank() || latestTerm != term) return@LaunchedEffect
            val result = withTimeoutOrNull(5_000) {
                runCatching { session.loadSearchSuggest(term) }
                    .getOrElse { SearchSuggestResult() }
            } ?: SearchSuggestResult()
            if (requestGeneration != suggestRequestGeneration) return@LaunchedEffect
            if (latestSearchDraft.text.trim() != term) return@LaunchedEffect
            suggestResult = result
            suggestForQuery = term
            suggestLoading = false
            suggestLoadingForQuery = ""
            publishSuggestionOverlay(term)
        } finally {
            if (requestGeneration == suggestRequestGeneration) {
                suggestLoading = false
                suggestLoadingForQuery = ""
                publishSuggestionOverlay(searchDraftText.trim())
            }
        }
    }

    LaunchedEffect(initialResultsReady, activeQuery, searchGeneration) {
        if (activeQuery != null && initialResultsReady) {
            focusManager.clearFocus(force = true)
            keyboardController?.hide()
        }
    }

    LaunchedEffect(listState, activeQuery, searchBarVisible, searchDraftText) {
        if (!searchBarVisible || activeQuery != null || searchDraftText.trim().isNotBlank()) {
            return@LaunchedEffect
        }
        var lastScrollTotal =
            listState.firstVisibleItemIndex * 10_000 + listState.firstVisibleItemScrollOffset
        snapshotFlow {
            listState.firstVisibleItemIndex * 10_000 + listState.firstVisibleItemScrollOffset
        }.collect { scrollTotal ->
            if (scrollTotal > lastScrollTotal + 8) {
                focusManager.clearFocus(force = true)
                keyboardController?.hide()
            }
            lastScrollTotal = scrollTotal
        }
    }

    val searchGenerationState by rememberUpdatedState(searchGeneration)
    val initialResultsReadyState by rememberUpdatedState(initialResultsReady)

    LaunchedEffect(listState, activeQuery, searchGeneration, activeSearchMode, weiboSort) {
        if (activeQuery == null) return@LaunchedEffect
        snapshotFlow {
            if (!initialResultsReadyState) return@snapshotFlow null
            val info = listState.layoutInfo
            val last = info.visibleItemsInfo.lastOrNull()?.index ?: 0
            val total = info.totalItemsCount
            if (total <= 0 || last < total - 3) {
                null
            } else {
                last to total
            }
        }
            .distinctUntilChanged()
            .filterNotNull()
            .collect {
                loadTopicResults(reset = false, generation = searchGenerationState)
            }
    }

    val isRefreshing = when {
        activeQuery == null -> hotSearchLoading
        else -> searchPullRefreshing
    }
    var searchHeaderHeight by remember { mutableStateOf(0.dp) }
    val density = LocalDensity.current
    val searchBarGap = SearchBarBottomGap
    val searchCompanionGap = SearchBarCompanionGap
    val searchFieldHeight = 44.dp
    val searchBarBottom = SearchBottomBarClearance + searchBarGap
    val imeBottom = WindowInsets.ime.asPaddingValues().calculateBottomPadding()
    val imeTargetBottom = WindowInsets.imeAnimationTarget.asPaddingValues().calculateBottomPadding()
    val imeInsetForLayout = maxOf(imeBottom, imeTargetBottom)
    val searchFieldBottomTarget = maxOf(searchBarBottom, imeInsetForLayout + searchCompanionGap)
    val searchFieldBottom by animateDpAsState(
        targetValue = searchFieldBottomTarget,
        animationSpec = tween(
            durationMillis = SearchImeFollowDurationMillis,
            easing = FastOutSlowInEasing,
        ),
        label = "search-field-bottom",
    )
    val suggestionPanelInset = if (
        searchBarVisible &&
        searchDraftText.trim().isNotBlank() &&
        activeQuery != searchDraftText.trim()
    ) {
        SearchSuggestionPanelMaxHeight + SearchBarCompanionGap
    } else {
        0.dp
    }
    val listBottomInset = searchFieldBottom + searchFieldHeight + searchCompanionGap + suggestionPanelInset

    SideEffect {
        searchBarOverlay.queryInput = searchDraft
        searchBarOverlay.active = searchBarVisible
        searchBarOverlay.mode = searchMode
        searchBarOverlay.onModeChange = { mode ->
            if (mode != searchMode) {
                onSearchModeChange(mode)
                resultItems = emptyList()
                userResultItems = emptyList()
                resultError = null
                resultNextPage = null
                initialResultsReady = false
                activeQuery?.let {
                    activeSearchMode = mode
                    searchGeneration++
                }
            }
        }
        searchBarOverlay.onQueryInputChange = { value -> updateSearchDraft(value) }
        searchBarOverlay.onSearch = { submitQuery(latestSearchDraft.text) }
        searchBarOverlay.onClear = ::clearSearchResults
        val currentSuggestTerm = searchDraftText.trim()
        searchBarOverlay.suggestions = if (suggestForQuery == currentSuggestTerm) {
            suggestResult
        } else {
            SearchSuggestResult()
        }
        searchBarOverlay.suggestionsLoading = suggestLoading &&
            suggestLoadingForQuery == currentSuggestTerm &&
            currentSuggestTerm.isNotBlank()
        searchBarOverlay.suggestionsVisible = searchBarVisible &&
            currentSuggestTerm.isNotBlank() &&
            activeQuery != currentSuggestTerm
        searchBarOverlay.onSuggestionClick = { query, mode ->
            onSearchModeChange(mode)
            activeSearchMode = mode
            submitQuery(query, modeOverride = mode)
        }
        searchBarOverlay.onSuggestionUserClick = { user ->
            focusManager.clearFocus(force = true)
            keyboardController?.hide()
            onUserClick(user.id)
        }
        searchBarOverlay.bottomPadding = searchFieldBottom
    }
    DisposableEffect(Unit) {
        onDispose {
            searchBarOverlay.active = false
            searchBarOverlay.onQueryInputChange = { searchBarOverlay.queryInput = it }
            searchBarOverlay.onSuggestionUserClick = {}
            searchBarOverlay.suggestionsVisible = false
            searchBarOverlay.suggestions = SearchSuggestResult()
            searchBarOverlay.suggestionsLoading = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        if (!hasLoginCookie) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = listBottomInset),
                contentAlignment = Alignment.Center,
            ) {
                EmptyState(
                    title = "需要登录微博",
                    body = "登录后即可查看热搜与搜索结果。",
                    actionLabel = "打开登录",
                    onAction = onOpenLoginSettings,
                )
            }
            return@Box
        }

        AppPullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = {
                    scope.launch {
                        if (activeQuery == null) {
                            reloadHotSearch()
                        } else {
                            searchPullRefreshing = true
                            try {
                                loadTopicResults(reset = true, generation = searchGeneration)
                            } finally {
                                searchPullRefreshing = false
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxSize(),
            ) {
                Box(Modifier.fillMaxSize()) {
                    LazyColumn(
                        state = listState,
                        flingBehavior = rememberWeiboListFlingBehavior(),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            top = when {
                                activeQuery != null -> searchHeaderHeight + 8.dp
                                else -> topInset + 8.dp
                            },
                            start = if (activeQuery == null) 16.dp else 0.dp,
                            end = if (activeQuery == null) 16.dp else 0.dp,
                            bottom = listBottomInset,
                        ),
                    ) {
                if (activeQuery == null) {
                    if (searchHistory.isNotEmpty()) {
                        item(key = "history-chip-section") {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(
                                        text = "搜索历史",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold,
                                    )
                                    TextButton(
                                        onClick = {
                                            deleteHistoryQuery = null
                                            searchHistory = searchHistoryStore.clear()
                                            searchHistoryExpanded = false
                                        },
                                        contentPadding = PaddingValues(vertical = 4.dp),
                                    ) {
                                        Text(
                                            text = "清除",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                }
                                Spacer(Modifier.height(SearchHistoryTitleToChipsGap))
                                SearchHistoryChipsSection(
                                    searchHistory = searchHistory,
                                    searchHistoryExpanded = searchHistoryExpanded,
                                    deleteHistoryQuery = deleteHistoryQuery,
                                    onDeleteHistoryQueryChange = { deleteHistoryQuery = it },
                                    onHistoryClick = { submitQuery(it) },
                                    onHistoryDelete = { query ->
                                        searchHistory = searchHistoryStore.remove(query)
                                        deleteHistoryQuery = null
                                    },
                                    onHistoryExpandToggle = { searchHistoryExpanded = !searchHistoryExpanded },
                                )
                            }
                        }
                        item(key = "history-chip-spacer") {
                            Spacer(Modifier.height(16.dp))
                        }
                    }
                    item {
                        Text(
                            text = "热搜",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(bottom = 8.dp),
                        )
                    }
                    when {
                        hotSearchError != null && hotSearchItems.isEmpty() -> {
                            item {
                                Column(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    Text(
                                        text = hotSearchError.orEmpty(),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                    Text(
                                        text = "下拉刷新重试",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        }
                        hotSearchItems.isEmpty() && !hotSearchLoading -> {
                            item {
                                Text(
                                    text = "暂无热搜",
                                    modifier = Modifier.padding(vertical = 16.dp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                        else -> {
                            itemsIndexed(
                                hotSearchItems,
                                key = { _, item -> item.word },
                                contentType = { _, _ -> "hot_search" },
                            ) { index, item ->
                                HotSearchRow(
                                    rank = index + 1,
                                    item = item,
                                    onClick = { submitQuery(item.word) },
                                )
                                if (index < hotSearchItems.lastIndex) {
                                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
                                }
                            }
                        }
                    }
                } else {
                    when {
                        resultLoading &&
                            (if (activeSearchMode == SearchMode.User) userResultItems.isEmpty() else resultItems.isEmpty()) -> {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 48.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    CircularProgressIndicator()
                                }
                            }
                        }
                        resultError != null &&
                            (if (activeSearchMode == SearchMode.User) userResultItems.isEmpty() else resultItems.isEmpty()) -> {
                            item {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    Text(
                                        text = resultError.orEmpty(),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                    Text(
                                        text = "下拉刷新重试",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        }
                        (if (activeSearchMode == SearchMode.User) userResultItems.isEmpty() else resultItems.isEmpty()) &&
                            !resultLoading -> {
                            item {
                                Text(
                                    text = "暂无相关内容",
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                        activeSearchMode == SearchMode.User -> {
                            items(
                                userResultItems,
                                key = { it.id.ifBlank { it.screenName } },
                                contentType = { "search_user" },
                            ) { user ->
                                RelationUserRow(
                                    user = user,
                                    showFollowButton = !mineProfileId.isNullOrBlank() && user.id != mineProfileId,
                                    onClick = { onUserClick(user.id.ifBlank { user.screenName }) },
                                    session = session,
                                    onFollowChanged = { updated ->
                                        userResultItems = userResultItems.map {
                                            if (it.id == updated.id) updated else it
                                        }
                                    },
                                    showMessage = showMessage,
                                )
                            }
                        }
                        else -> {
                            items(resultItems, key = { it.id }, contentType = { "feed_card" }) { item ->
                                val resolved = resolveFeedItem(item)
                                FeedCard(
                                    item = resolved,
                                    onClick = { onItemClick(resolved, null) },
                                    onMediaClick = onMediaClick,
                                    emoticonMap = emoticonMap,
                                    onUserClick = onUserClick,
                                    isLongTextLoading = isLongTextLoading,
                                    onLoadLongText = onLoadLongText,
                                    onToggleLike = onToggleLike,
                                    onLikeClick = onLikeClick,
                                    onUrlEntityClick = onUrlEntityClick,
                                    menuBackEnabled = resultsBackEnabled,
                                )
                            }
                        }
                    }
                    if (resultLoadingMore) {
                        item { MineLoadingMoreIndicator() }
                    }
                }
            }
            if (activeQuery != null && searchBarVisible) {
                Column(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.background)
                        .onGloballyPositioned { coordinates ->
                            searchHeaderHeight = with(density) { coordinates.size.height.toDp() }
                        }
                        .padding(
                            top = topInset + 12.dp,
                            start = 16.dp,
                            end = 16.dp,
                            bottom = 12.dp,
                        ),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = activeQuery.orEmpty(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = WeiboTopicBlue,
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "${activeSearchMode.label}搜索结果",
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        if (activeSearchMode == SearchMode.Weibo) {
                            SearchWeiboSortToggle(
                                selected = weiboSort,
                                onSelected = ::changeWeiboSort,
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
private fun MobileWeiboWebScreen(
    pageUrl: String,
    onRootBack: () -> Unit,
    scrollToTopOnPageFinished: (String?) -> Boolean = { true },
    userAgent: String? = null,
    active: Boolean = true,
) {
    val context = LocalContext.current
    val topInset = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val bottomBarGap = LiquidBottomBarContentGap
    val imeBottom = WindowInsets.ime.asPaddingValues().calculateBottomPadding()
    val bottomInset = if (imeBottom > 0.dp) {
        imeBottom + bottomBarGap
    } else {
        LiquidBottomBarReserve + bottomBarGap
    }
    val mobileUserAgent =
        "Mozilla/5.0 (iPhone; CPU iPhone OS 16_0 like Mac OS X) AppleWebKit/605.1.15 " +
            "(KHTML, like Gecko) Mobile/15E148 Weibo (iPhone14,2__weibo__14.9.0__iphone__os16.0)"
    val fileUploadBridge = remember { MobileWebFileUploadBridge() }
    val viewportCoordinator = remember(pageUrl) { MobileWebViewportCoordinator() }

    val fileChooserLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        val callback = fileUploadBridge.callback
        fileUploadBridge.callback = null
        callback?.onReceiveValue(
            WebChromeClient.FileChooserParams.parseResult(result.resultCode, result.data),
        )
    }

    val webView = remember(pageUrl) {
        WebView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
            )
            setLayerType(View.LAYER_TYPE_NONE, null)
            importantForAutofill = View.IMPORTANT_FOR_AUTOFILL_NO_EXCLUDE_DESCENDANTS
            CookieManager.getInstance().setAcceptCookie(true)
            CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)
            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                loadsImagesAutomatically = true
                useWideViewPort = true
                loadWithOverviewMode = false
                mediaPlaybackRequiresUserGesture = false
                mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
                userAgentString = userAgent ?: mobileUserAgent
            }
            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    if (!viewportCoordinator.shouldFitOnPageFinished(url)) return
                    view?.fitMobileWebViewport(scrollToTop = scrollToTopOnPageFinished(url))
                }
            }
            loadUrl(pageUrl)
        }
    }

    DisposableEffect(pageUrl) {
        onDispose {
            fileUploadBridge.cancelPendingUpload()
            webView.webChromeClient = null
            webView.stopLoading()
            webView.destroy()
        }
    }

    DisposableEffect(webView) {
        webView.webChromeClient = object : WebChromeClient() {
            override fun onShowFileChooser(
                webView: WebView?,
                filePathCallback: ValueCallback<Array<Uri>>?,
                fileChooserParams: FileChooserParams?,
            ): Boolean = fileUploadBridge.handleShowFileChooser(
                filePathCallback = filePathCallback,
                fileChooserParams = fileChooserParams,
                launchChooser = fileChooserLauncher::launch,
            )
        }
        onDispose {
            webView.webChromeClient = null
        }
    }

    LaunchedEffect(active) {
        if (active) {
            webView.visibility = View.VISIBLE
            webView.onResume()
        } else {
            webView.onPause()
            webView.visibility = View.GONE
        }
    }

    val imeVisible = WindowInsets.ime.getBottom(LocalDensity.current) > 0
    LaunchedEffect(imeVisible, active) {
        if (!active) return@LaunchedEffect
        if (!viewportCoordinator.shouldFitOnImeChange(imeVisible)) return@LaunchedEffect
        delay(120)
        if (viewportCoordinator.debounceAllowed()) {
            webView.fitMobileWebViewport(scrollToTop = false)
        }
    }

    BackHandler(enabled = active) {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            onRootBack()
        }
    }

    Box(
        Modifier
            .fillMaxSize()
            .padding(top = topInset, bottom = bottomInset),
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { webView },
            update = { view ->
                view.visibility = if (active) View.VISIBLE else View.GONE
            },
        )
    }
}

@Composable
private fun WebTabLoginPlaceholder() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "请先在设置中登录微博",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun MessagesScreen(
    onRootBack: () -> Unit,
    active: Boolean = true,
) {
    MobileWeiboWebScreen(
        pageUrl = "https://m.weibo.cn/message",
        onRootBack = onRootBack,
        scrollToTopOnPageFinished = { url -> url?.contains("/message", ignoreCase = true) != true },
        active = active,
    )
}

private class MobileWebFileUploadBridge {
    var callback: ValueCallback<Array<Uri>>? = null

    fun cancelPendingUpload() {
        callback?.onReceiveValue(null)
        callback = null
    }

    fun handleShowFileChooser(
        filePathCallback: ValueCallback<Array<Uri>>?,
        fileChooserParams: WebChromeClient.FileChooserParams?,
        launchChooser: (Intent) -> Unit,
    ): Boolean {
        cancelPendingUpload()
        callback = filePathCallback
        val intent = fileChooserParams?.createIntent() ?: run {
            cancelPendingUpload()
            return false
        }
        return try {
            launchChooser(intent)
            true
        } catch (_: ActivityNotFoundException) {
            cancelPendingUpload()
            false
        }
    }
}

private class MobileWebViewportCoordinator {
    private var lastFittedUrl: String? = null
    private var lastImeVisible: Boolean? = null
    private var lastFitAtMs: Long = 0L

    fun shouldFitOnPageFinished(url: String?): Boolean {
        val normalized = url.orEmpty()
        if (normalized == lastFittedUrl) return false
        lastFittedUrl = normalized
        return true
    }

    fun shouldFitOnImeChange(imeVisible: Boolean): Boolean {
        if (imeVisible == lastImeVisible) return false
        lastImeVisible = imeVisible
        return true
    }

    fun debounceAllowed(minIntervalMs: Long = 400L): Boolean {
        val now = System.currentTimeMillis()
        if (now - lastFitAtMs < minIntervalMs) return false
        lastFitAtMs = now
        return true
    }
}

private fun WebView.fitMobileWebViewport(scrollToTop: Boolean = true) {
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
            var height = window.innerHeight + 'px';
            if (document.documentElement.style.height !== height) {
                document.documentElement.style.height = height;
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
    onAlbumTabSelected: () -> Unit = {},
    onRefresh: () -> Unit,
    onLoadMorePosts: () -> Unit,
    onLoadMoreAlbum: () -> Unit,
    onSyncEmoticons: () -> Unit,
    onItemClick: (FeedItem) -> Unit,
    onRetweetClick: (FeedItem, FeedItem) -> Unit = { item, _ -> onItemClick(item) },
    onCommentLongClick: (FeedItem) -> Unit = {},
    onOpenAlbumViewer: (AlbumViewerState) -> Unit,
    onMediaClick: (FeedMedia, String) -> Unit,
    onUserClick: ((String) -> Unit)? = null,
    isLongTextLoading: (FeedItem) -> Boolean = { false },
    onLoadLongText: ((FeedItem) -> Unit)? = null,
    onToggleLike: ((FeedItem) -> Unit)? = null,
    onLikeClick: ((FeedItem, Rect) -> Unit)? = null,
    onUrlEntityClick: ((FeedUrlEntity) -> Unit)? = null,
    enableSettings: Boolean = true,
    storedAccounts: List<StoredWeiboAccount> = emptyList(),
    activeAccountId: String? = null,
    onSwitchAccount: (String) -> Unit = {},
    onDeleteAccount: (String) -> Unit = {},
    onPrepareAddAccount: suspend () -> Unit = {},
    onPersistLoginSession: suspend () -> Unit = {},
    onReturnToFeed: () -> Unit = {},
    pendingOpenAccountLogin: Boolean = false,
    onPendingOpenAccountLoginConsumed: () -> Unit = {},
    backgroundPlaybackEnabled: Boolean = false,
    onBackgroundPlaybackChange: (Boolean) -> Unit = {},
    feedThumbnailQuality: FeedThumbnailQuality = FeedThumbnailQuality.Medium,
    onFeedThumbnailQualityChange: (FeedThumbnailQuality) -> Unit = {},
    feedLineSpacing: FeedLineSpacing = FeedLineSpacing.Compact,
    onFeedLineSpacingChange: (FeedLineSpacing) -> Unit = {},
    feedFontSize: FeedFontSize = FeedFontSize.Medium,
    onFeedFontSizeChange: (FeedFontSize) -> Unit = {},
    feedPreviewItemProvider: () -> FeedItem? = { null },
    selectedThemeColor: MorandiThemeColor = MorandiThemeColors.first(),
    onThemeColorChange: (MorandiThemeColor) -> Unit = {},
    appearanceMode: AppearanceMode = AppearanceMode.System,
    onAppearanceModeChange: (AppearanceMode) -> Unit = {},
    showFollowActions: Boolean = false,
    followLoading: Boolean = false,
    onFollowClick: () -> Unit = {},
    onOpenFollowList: ((String, String, String?, String?, FriendListTab) -> Unit)? = null,
    showPullRefreshIndicator: Boolean = true,
) {
    var showSettings by remember { mutableStateOf(false) }
    var showAccountManagement by remember { mutableStateOf(false) }
    var avatarViewerOpen by remember { mutableStateOf(false) }
    val avatarImage = remember(profile?.avatarUrl) { profileAvatarFeedImage(profile?.avatarUrl) }
    val openAvatarViewer = {
        if (avatarImage != null) avatarViewerOpen = true
    }
    val openFollowListForProfile: ((FriendListTab) -> Unit)? = onOpenFollowList?.let { callback ->
        { tab ->
            profile?.id?.takeIf { it.isNotBlank() }?.let { uid ->
                callback(uid, profile.screenName, profile.avatarUrl, profile.description, tab)
            }
        }
    }
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
            .distinctUntilChanged()
            .collect { page ->
                onMinePagerPageChanged(page)
                if (MineContentTab.entries[page] == MineContentTab.Album) {
                    onAlbumTabSelected()
                }
            }
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
                    autoReturnToFeedOnLogin = storedAccounts.isEmpty(),
                    onLoginSuccess = {
                        coroutineScope.launch {
                            delay(150)
                            val isFirstLogin = storedAccounts.isEmpty()
                            if (isFirstLogin) {
                                showAccountManagement = false
                                showSettings = false
                            }
                            onReturnToFeed()
                            onSyncEmoticons()
                        }
                    },
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
                feedThumbnailQuality = feedThumbnailQuality,
                onFeedThumbnailQualityChange = onFeedThumbnailQualityChange,
                feedLineSpacing = feedLineSpacing,
                onFeedLineSpacingChange = onFeedLineSpacingChange,
                feedFontSize = feedFontSize,
                onFeedFontSizeChange = onFeedFontSizeChange,
                feedPreviewItem = feedPreviewItemProvider(),
                selectedThemeColor = selectedThemeColor,
                onThemeColorChange = onThemeColorChange,
                appearanceMode = appearanceMode,
                onAppearanceModeChange = onAppearanceModeChange,
                onBack = {
                    showAccountManagement = false
                    showSettings = false
                },
                onSwitchAccount = onSwitchAccount,
                onDeleteAccount = onDeleteAccount,
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
            layoutInfo.totalItemsCount > 0 &&
                lastVisibleIndex >= layoutInfo.totalItemsCount - ListLoadMoreItemsFromBottom
        }
            .distinctUntilChanged()
            .filter { it }
            .collect { onLoadMorePosts() }
    }

    // Infinite scroll for Album
    LaunchedEffect(albumListState) {
        snapshotFlow {
            val layoutInfo = albumListState.layoutInfo
            val lastVisibleIndex = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            layoutInfo.totalItemsCount > 0 &&
                lastVisibleIndex >= layoutInfo.totalItemsCount - ListLoadMoreItemsFromBottom
        }
            .distinctUntilChanged()
            .filter { it }
            .collect { onLoadMoreAlbum() }
    }

    LaunchedEffect(albumImages) {
        if (albumImages.isNotEmpty()) {
            prefetchAlbumGridThumbnails(albumImages)
        }
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
    val tabsOverlapOffset = ProfileHeaderCardCoverOverlap * (1f - animatedCollapse)
    val videoPeekController = LocalVideoPeekController.current
    val blockMinePagerScroll = videoPeekController.activeRequest != null &&
        videoPeekController.pendingDismiss == null
    val isPullRefreshing = showPullRefreshIndicator && isLoading && posts.isNotEmpty()

    Box(Modifier.fillMaxSize()) {
    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        AppPullToRefreshBox(
            isRefreshing = isPullRefreshing,
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
                                .background(MaterialTheme.colorScheme.surface)
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
                                    .clip(CircleShape)
                                    .clickable(
                                        enabled = avatarImage != null,
                                        onClick = openAvatarViewer,
                                    ),
                                contentScale = ContentScale.Crop,
                            )
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .consumeTouchEvents(),
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
                                    followMe = profile.followMe,
                                    loading = followLoading,
                                    onClick = onFollowClick,
                                )
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
                    key(profile?.id, profile?.description) {
                        MineProfileHeader(
                            profile = profile,
                            hasLoginCookie = hasLoginCookie,
                            loadError = loadError,
                            onOpenSettings = null,
                            onAvatarClick = openAvatarViewer,
                            showFollowActions = showFollowActions,
                            followLoading = followLoading,
                            onFollowClick = onFollowClick,
                            onOpenFollowList = openFollowListForProfile,
                        )
                    }
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .offset(y = -tabsOverlapOffset),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surface),
                    ) {
                        MineContentTabs(
                            scrollPosition = mineTabScrollPosition,
                            onTabSelected = { tab ->
                                val sameTab = tab == MineContentTab.entries[pagerState.currentPage]
                                if (sameTab) {
                                    coroutineScope.launch {
                                        when (tab) {
                                            MineContentTab.Posts -> postsListState.animateScrollToTopFixed()
                                            MineContentTab.Album -> albumListState.animateScrollToTopFixed()
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
                    }

                    HorizontalPager(
                        state = pagerState,
                        userScrollEnabled = !blockMinePagerScroll,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .background(MaterialTheme.colorScheme.background),
                        beyondViewportPageCount = 0,
                    ) { page ->
                    when (MineContentTab.entries[page]) {
                        MineContentTab.Posts -> {
                            LazyColumn(
                                state = postsListState,
                                flingBehavior = rememberWeiboListFlingBehavior(),
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(bottom = 96.dp),
                            ) {
                                if (posts.isEmpty()) {
                                    item {
                                        if (hasLoginCookie && isLoading) {
                                            MineLoadingMoreIndicator()
                                        } else if (!hasLoginCookie) {
                                            EmptyState(
                                                title = "\u672A\u767B\u5F55",
                                                body = "\u8BF7\u5728\u5C01\u9762\u53F3\u4E0A\u89D2\u8BBE\u7F6E\u4E2D\u767B\u5F55\u5FAE\u535A\u3002",
                                            )
                                        }
                                    }
                                } else {
                                    items(posts, key = { it.id }, contentType = { "feed_card" }) { post ->
                                        FeedCard(
                                            item = post,
                                            onClick = { onItemClick(post) },
                                            onMediaClick = onMediaClick,
                                            emoticonMap = emoticonMap,
                                            onUserClick = onUserClick,
                                            onRetweetClick = onRetweetClick,
                                            onCommentLongClick = { onCommentLongClick(post) },
                                            isLongTextLoading = isLongTextLoading,
                                            onLoadLongText = onLoadLongText,
                                            onToggleLike = onToggleLike,
                                            onLikeClick = onLikeClick,
                                            onUrlEntityClick = onUrlEntityClick,
                                        )
                                    }
                                    if (postsLoadingMore || (isLoading && !showPullRefreshIndicator)) {
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
                            val albumRows = remember(albumImages) {
                                buildAlbumGridRows(groupAlbumImagesByMonth(albumImages))
                            }
                            val stickyMonthLabel by remember(albumRows, albumListState) {
                                derivedStateOf {
                                    resolveStickyAlbumMonthLabel(albumListState, albumRows)
                                }
                            }
                            Box(modifier = Modifier.fillMaxSize()) {
                                LazyColumn(
                                    state = albumListState,
                                    flingBehavior = rememberWeiboListFlingBehavior(),
                                    modifier = Modifier.fillMaxSize(),
                                    contentPadding = PaddingValues(bottom = 96.dp),
                                ) {
                                    if (albumRows.isEmpty()) {
                                        item(key = "album-empty") {
                                            EmptyState(
                                                title = "\u6682\u672A\u8BFB\u5230\u76F8\u518C",
                                                body = when {
                                                    albumLoading || albumLoadingMore || isLoading ->
                                                        "\u6B63\u5728\u52A0\u8F7D\u76F8\u518C\u2026"
                                                    !albumError.isNullOrBlank() -> albumError
                                                    else ->
                                                        "\u4E0B\u62C9\u5237\u65B0\u540E\u4F1A\u4ECE\u76F8\u518C\u63A5\u53E3\u52A0\u8F7D\u56FE\u7247\u4E0E\u89C6\u9891\u3002"
                                                },
                                            )
                                        }
                                    } else {
                                        items(
                                            items = albumRows,
                                            key = { it.key },
                                            contentType = { "album_row" },
                                        ) { row ->
                                            MineAlbumGridRow(
                                                dateLabel = row.dateLabel?.takeIf { stickyMonthLabel != row.monthKey },
                                                monthImages = row.monthImages,
                                                rowImages = row.rowImages,
                                                rowStartIndex = row.rowStartIndex,
                                                relatedPosts = posts,
                                                modifier = Modifier
                                                    .padding(horizontal = 12.dp)
                                                    .padding(bottom = 6.dp),
                                                onImageClick = { groupImages, index ->
                                                    captureAlbumViewerOpen(
                                                        AlbumViewerState(groupImages, index),
                                                    )
                                                },
                                                onVideoClick = onMediaClick,
                                            )
                                        }
                                    }
                                    if (albumLoadingMore) {
                                        item(key = "album-loading-more") { MineLoadingMoreIndicator() }
                                    }
                                    if (!albumHasMore && albumImages.isNotEmpty()) {
                                        item(key = "album-end") {
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
                                stickyMonthLabel?.let { monthKey ->
                                    AlbumMonthStickyHeader(
                                        dateLabel = monthKey,
                                        modifier = Modifier.align(Alignment.TopStart),
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

    if (enableSettings) {
        IconButton(
            onClick = { showSettings = true },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 32.dp, end = 10.dp)
                .size(40.dp)
                .zIndex(4f),
        ) {
            Icon(
                imageVector = Icons.Rounded.Settings,
                contentDescription = "\u8BBE\u7F6E",
                tint = MaterialTheme.colorScheme.onSurface,
            )
        }
    }

    if (avatarViewerOpen && avatarImage != null) {
        FullscreenImageViewer(
            images = listOf(avatarImage),
            initialIndex = 0,
            onDismiss = { avatarViewerOpen = false },
        )
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
    feedThumbnailQuality: FeedThumbnailQuality,
    onFeedThumbnailQualityChange: (FeedThumbnailQuality) -> Unit,
    feedLineSpacing: FeedLineSpacing,
    onFeedLineSpacingChange: (FeedLineSpacing) -> Unit,
    feedFontSize: FeedFontSize,
    onFeedFontSizeChange: (FeedFontSize) -> Unit,
    feedPreviewItem: FeedItem?,
    selectedThemeColor: MorandiThemeColor,
    onThemeColorChange: (MorandiThemeColor) -> Unit,
    appearanceMode: AppearanceMode,
    onAppearanceModeChange: (AppearanceMode) -> Unit,
    onBack: () -> Unit,
    onSwitchAccount: (String) -> Unit,
    onDeleteAccount: (String) -> Unit,
    onAddAccount: () -> Unit,
    onSyncEmoticons: () -> Unit,
) {
    var accountExpanded by remember { mutableStateOf(false) }
    var emoticonExpanded by remember { mutableStateOf(false) }
    var imageExpanded by remember { mutableStateOf(false) }
    var typographyExpanded by remember { mutableStateOf(false) }
    var themeExpanded by remember { mutableStateOf(false) }
    var appearanceExpanded by remember { mutableStateOf(false) }
    var showHelp by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val versionName = remember {
        runCatching {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName
        }.getOrNull().orEmpty().ifBlank { "1.2" }
    }

    BackHandler {
        if (showHelp) {
            showHelp = false
        } else {
            onBack()
        }
    }

    SettingsPageShell(title = if (showHelp) "使用说明" else "设置", onBack = onBack) {
        if (showHelp) {
            SettingsHelpContent()
        } else {
            LazyColumn(
                flingBehavior = rememberWeiboListFlingBehavior(),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    top = 12.dp,
                    end = 16.dp,
                    bottom = SettingsBottomBarInset,
                ),
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
                        onDeleteAccount = onDeleteAccount,
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
                    SettingsImageCard(
                        expanded = imageExpanded,
                        onExpandedChange = { imageExpanded = it },
                        quality = feedThumbnailQuality,
                        onQualityChange = onFeedThumbnailQualityChange,
                    )
                }
                item {
                    SettingsTypographyCard(
                        expanded = typographyExpanded,
                        onExpandedChange = { typographyExpanded = it },
                        lineSpacing = feedLineSpacing,
                        onLineSpacingChange = onFeedLineSpacingChange,
                        fontSize = feedFontSize,
                        onFontSizeChange = onFeedFontSizeChange,
                        previewItem = feedPreviewItem,
                        emoticonMap = emoticonMap,
                    )
                }
                item {
                    SettingsAppearanceCard(
                        expanded = appearanceExpanded,
                        onExpandedChange = { appearanceExpanded = it },
                        mode = appearanceMode,
                        onModeChange = onAppearanceModeChange,
                    )
                }
                item {
                    SettingsThemeColorCard(
                        expanded = themeExpanded,
                        onExpandedChange = { themeExpanded = it },
                        selected = selectedThemeColor,
                        onSelected = onThemeColorChange,
                    )
                }
                item {
                    SettingsPlaybackCard(
                        backgroundPlaybackEnabled = backgroundPlaybackEnabled,
                        onBackgroundPlaybackChange = onBackgroundPlaybackChange,
                    )
                }
                item {
                    SettingsHelpEntryCard(onOpen = { showHelp = true })
                }
                item {
                    SettingsAboutCard(versionName = versionName)
                }
            }
        }
    }
}

private data class HelpSection(
    val title: String,
    val items: List<String>,
)

private val appHelpSections = listOf(
    HelpSection(
        title = "开始使用",
        items = listOf(
            "本应用通过微博网页登录态读取数据，首次使用请到「我的 → 设置 → 账号管理」完成登录。",
            "首次登录成功后会自动返回首页并同步数据，无需手动点击「回到微博首页」。",
            "登录后可浏览首页信息流、搜索、个人主页，以及原生写微博、查看消息等功能。",
            "若首页为空，请确认已登录，并在首页下拉刷新或再次点击底部「首页」同步内容。",
        ),
    ),
    HelpSection(
        title = "底部导航",
        items = listOf(
            "底部共有五个入口：首页、消息、搜索、写微博、我的。",
            "向下滚动列表时，底部栏会自动收起到左侧小胶囊；单击小胶囊可展开。",
            "展开后点击空白区域可再次收起；点击其他 Tab 时选中块会滑向目标位置。",
            "底部栏文字大小固定，不受系统字体缩放影响。",
            "在小胶囊上长按并左右拖动，可快速切换 Tab；松手后停留在目标页。",
            "双击小胶囊：在首页刷新信息流；在「我的」回到顶部并刷新资料。",
            "再次点击当前选中的「首页」，也会从顶部刷新关注流。",
            "长按底部「首页」按钮，可在「最新微博」与「朋友圈」之间切换。",
            "写微博页为全屏编辑界面，不显示底部五个按钮；按返回键回到首页。",
            "在「消息」页按系统返回键，优先网页内后退；无法后退时留在当前页。",
            "在其他 Tab 按返回键会先回到首页；在首页连按两次返回键退出应用。",
        ),
    ),
    HelpSection(
        title = "首页信息流",
        items = listOf(
            "在首页下拉可刷新关注流；滚动到底部会自动加载更多。",
            "列表使用系统默认滑动手感；惯性滚动中点击图片或视频，会先停止滚动，避免误触打开媒体。",
            "点击微博卡片进入详情；点击头像或 @昵称 进入用户主页。",
            "点击评论图标进入详情评论区；点击转发图标进入详情转发区。",
            "长按评论图标可快速打开评论输入框；长按「赞」可快速点赞或取消赞。",
            "点击正文中的 #话题# 会临时进入搜索页并搜索该话题，返回后回到首页。",
            "长微博可点「阅读全文」展开；正文里的「查看图片」链接可直接看图。",
            "点击卡片中的链接卡片，会在应用内打开文章阅读页。",
            "点击卡片右上角菜单，可跳转官方微博或分享链接。",
            "刷新完成后，顶部会短暂显示本次更新条数提示。",
        ),
    ),
    HelpSection(
        title = "图片与视频",
        items = listOf(
            "点击信息流中的图片进入全屏查看；多张图片可左右滑动切换。",
            "长按图片会弹出预览菜单，可保存、保存全部或分享；松手后可进入全屏。",
            "全屏看图支持双指缩放、拖动；在边缘时可左右切换图片。",
            "向下拖动图片可关闭全屏；长按全屏图片同样可保存或分享。",
            "Live Photo 会在全屏时自动播放动态效果；保存时可写入系统相册为动态照片。",
            "视频：点击中心播放按钮可在卡片内播放；双击视频卡片打开底部浮窗播放。",
            "长按视频会弹出大预览，上滑可进入浮窗，下滑可关闭；松手也可进入全屏。",
            "浮窗播放时切换 Tab、进入详情或用户主页，视频会继续播放；返回可回到原页面。",
            "从首页进入微博详情时，若视频正在播放，会自动续播并保持当前进度。",
            "详情页内播放视频后向上滑动，卡片滚出屏幕时会自动进入浮窗继续播放。",
            "详情页浮窗播放时向下滑动，待原视频卡片完全回到屏幕后，会自动缩回卡片内播放。",
            "用户主页微博列表中的视频卡片，长按、浮窗、全屏等手势与首页一致。",
            "竖屏与横屏视频浮窗使用相同窗口大小；浮窗右上角可点「全屏」。",
            "全屏播放竖屏视频时，右上角可点「横屏」横屏观看；横屏后可点「竖屏」切回。",
            "全屏视频支持「浮窗」退回小窗，也支持画中画；底部胶囊进度条可拖动快进。",
            "浮窗模式下优先使用上下滑关闭，整屏横向拖动不会误触快进。",
            "可在设置中控制切到后台后是否继续播放声音。",
            "从「我的」或用户主页进入相册，可按日期浏览并查看原图，部分图片可关联到原微博。",
        ),
    ),
    HelpSection(
        title = "微博详情与评论",
        items = listOf(
            "详情页下拉可刷新微博与评论；评论列表滚动到底会自动加载更多。",
            "详情页内视频支持滚动离开卡片后自动浮窗，滑回卡片后自动回到内联播放。",
            "点击评论排序按钮，可在「按时间」与「按热度」之间切换。",
            "支持楼中楼评论展开；长按评论行可回复该评论。",
            "点击评论中的图片缩略图，会以与信息流一致的过渡动画进入全屏；收起时回到原缩略图位置。",
            "长按评论图片可弹出保存、保存全部或分享菜单；松手后可进入全屏。",
            "长按底部评论按钮或详情页评论入口，可打开评论输入弹窗。",
            "评论弹窗支持文字、表情、图片；回复时可勾选「同时转发」。",
            "评论中的图片、链接、话题与首页规则一致。",
            "详情页同样支持点赞、查看媒体、跳转用户主页等操作。",
        ),
    ),
    HelpSection(
        title = "评论 @ 提及",
        items = listOf(
            "在评论弹窗点击 @ 或输入 @ 后，会显示互相关注用户头像条（最多 40 人）。",
            "@ 后继续输入昵称片段，会从全部关注与粉丝中匹配候选（最多 40 条）。",
            "点击候选即可补全 @昵称；匹配结果按前缀优先排序。",
            "关注/粉丝列表在首次打开评论弹窗时后台加载，数量较多时可能需要等待片刻。",
        ),
    ),
    HelpSection(
        title = "搜索",
        items = listOf(
            "搜索框左侧按钮用于切换「微博 / 用户」搜索模式，点击即可循环切换。",
            "搜索微博时，结果页可切换「综合 / 实时」排序。",
            "未登录时无法加载热搜与搜索结果，请先到设置中登录。",
            "输入关键词时，搜索框上方会显示毛玻璃联想词面板，可快速选择热搜词或相关用户。",
            "输入关键词后搜索；在结果页按返回键可清空结果并回到搜索首页。",
            "从首页或用户主页点击 #话题# 进入搜索时，会临时使用「搜微博」模式，不影响搜索页原有模式设置。",
            "搜索结果中的微博、用户交互方式与首页、个人主页一致。",
        ),
    ),
    HelpSection(
        title = "写微博与消息",
        items = listOf(
            "「写微博」为原生编辑界面：顶部显示头像、昵称与「发送」，可点选公开范围。",
            "公开范围支持：公开、好友圈、粉丝、仅自己可见；点「公开 ▼」在上方弹出选项。",
            "支持文字、@ 提及、表情与相册图片，最多可选 18 张；@ 候选与评论弹窗规则一致。",
            "离开发微博页再回来，已输入的文字、图片与可见范围会保留；发布成功或切换账号后清空。",
            "未登录时进入「消息」会提示先登录；登录成功后会自动加载消息页，无需手动刷新。",
            "「消息」使用移动版微博网页（m.weibo.cn/message），登录态与首页共用。",
        ),
    ),
    HelpSection(
        title = "我的与用户主页",
        items = listOf(
            "「我的」顶部封面、头像均可点击放大查看；封面右上角齿轮进入设置。",
            "个人页可在「微博 / 相册」两个标签间切换；相册按月份分组展示。",
            "点击粉丝、关注等统计项，可打开关注 / 粉丝列表并查看其他用户。",
            "在粉丝/关注列表页点击底部其他 Tab，会正常切换页面并关闭列表。",
            "访问他人主页时，可关注/取关；互相关注时按钮显示「互相关注」。",
            "用户主页微博列表中的视频，支持与首页相同的长按预览、浮窗与全屏操作。",
            "在用户主页点击微博中的 #话题#，会进入该话题搜索；返回一次回到该用户主页。",
            "列表上滑时，顶部封面与资料区会平滑收起；封面支持多图轮播，点击可全屏查看。",
            "顶部为视频封面时，上滑收起速度与图片封面一致，不会出现突然滑过远的情况。",
        ),
    ),
    HelpSection(
        title = "设置项说明",
        items = listOf(
            "账号管理：登录、添加账号、切换已保存账号；切换后会重新加载对应账号数据。",
            "表情同步：从微博拉取表情配置到本地，改善正文与评论中的表情显示。",
            "浏览信息流时，正文里出现的表情也会自动收录到本地，同步时不会删除这些表情。",
            "图片清晰度：省流 / 标准 / 高清三档，影响信息流缩略图加载规格；全屏仍会尽量加载高清图。",
            "行距与字号：五档行距、五档字号，作用于首页微博正文、引用区与评论区。",
            "深色模式：可在浅色、深色与跟随系统之间切换。",
            "主题颜色：更换应用强调色，影响底部导航、按钮与高亮文字等界面元素。",
            "后台播放声音：关闭后，应用切到后台时会暂停视频；浮窗播放时切换页面不会暂停。",
        ),
    ),
)

@Composable
private fun SettingsPlainCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val shape = RoundedCornerShape(8.dp)
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        shape = shape,
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 0.dp,
        tonalElevation = 0.dp,
        content = content,
    )
}

@Composable
private fun SettingsHelpEntryCard(onOpen: () -> Unit) {
    SettingsPlainCard(onClick = onOpen) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            Text(
                text = "使用说明",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = "了解底部导航、手势操作与各页面功能",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun SettingsAboutCard(versionName: String) {
    SettingsPlainCard {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Image(
                painter = painterResource(R.drawable.ic_launcher_app),
                contentDescription = null,
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(14.dp)),
                contentScale = ContentScale.Crop,
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                Text(
                    text = "关于",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = "MyWeibo · 版本 $versionName",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun SettingsHelpContent() {
    LazyColumn(
        flingBehavior = rememberWeiboListFlingBehavior(),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp,
            top = 4.dp,
            end = 16.dp,
            bottom = SettingsBottomBarInset,
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text(
                text = "本页汇总了当前版本的主要交互方式。部分功能依赖微博网页接口，若官方调整页面，个别入口可能失效。",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        appHelpSections.forEach { section ->
            item {
                SettingsHelpSectionCard(section = section)
            }
        }
    }
}

@Composable
private fun SettingsHelpSectionCard(section: HelpSection) {
    SettingsPlainCard {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = section.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            section.items.forEach { item ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = item,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

private enum class SettingsAccountSwipeAnchor { Closed, Open }

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SettingsAccountRow(
    account: StoredWeiboAccount,
    isActive: Boolean,
    onSwitchAccount: (String) -> Unit,
    onDeleteAccount: (String) -> Unit,
) {
    val density = LocalDensity.current
    val deleteActionWidth = 72.dp
    val deleteActionWidthPx = remember(density) { with(density) { deleteActionWidth.toPx() } }
    val rowShape = RoundedCornerShape(8.dp)
    val rowBackground = if (isActive) {
        lerp(MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.primaryContainer, 0.35f)
    } else {
        MaterialTheme.colorScheme.surface
    }
    val dragState = remember(account.id, deleteActionWidthPx) {
        AnchoredDraggableState(
            initialValue = SettingsAccountSwipeAnchor.Closed,
            anchors = DraggableAnchors {
                SettingsAccountSwipeAnchor.Closed at 0f
                SettingsAccountSwipeAnchor.Open at -deleteActionWidthPx
            },
        )
    }
    val flingBehavior = AnchoredDraggableDefaults.flingBehavior(
        state = dragState,
        positionalThreshold = { distance -> distance * 0.35f },
        animationSpec = spring(
            dampingRatio = 0.82f,
            stiffness = Spring.StiffnessMedium,
        ),
    )
    val scope = rememberCoroutineScope()
    val swipeOffsetPx = dragState.requireOffset()
    val isRevealed = dragState.currentValue == SettingsAccountSwipeAnchor.Open
    val canSwitchAccount = !isActive && !isRevealed && swipeOffsetPx == 0f

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Max)
            .clip(rowShape),
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .width(deleteActionWidth)
                .fillMaxHeight()
                .background(Color(0xFFE35D5B))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {
                        onDeleteAccount(account.id)
                        scope.launch { dragState.animateTo(SettingsAccountSwipeAnchor.Closed) }
                    },
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "删除",
                style = MaterialTheme.typography.labelLarge,
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    translationX = swipeOffsetPx
                    clip = true
                    shape = rowShape
                }
                .anchoredDraggable(
                    state = dragState,
                    orientation = Orientation.Horizontal,
                    flingBehavior = flingBehavior,
                )
                .background(rowBackground, rowShape)
                .clickable(
                    enabled = canSwitchAccount,
                    onClick = { onSwitchAccount(account.id) },
                )
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
            Text(
                text = account.screenName.ifBlank { "微博用户" },
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
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

@Composable
private fun SettingsImageCard(
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    quality: FeedThumbnailQuality,
    onQualityChange: (FeedThumbnailQuality) -> Unit,
) {
    val subtitle = when (quality) {
        FeedThumbnailQuality.Low -> "当前为省流模式，优先加载 bmiddle 缩略图"
        FeedThumbnailQuality.Medium -> "当前为标准模式，优先加载 mw690 中图"
        FeedThumbnailQuality.High -> "当前为高清模式，优先加载 mw1024 预览图"
    }

    SettingsPlainCard {
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
                            text = "图片清晰度",
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
                                text = quality.label,
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
                SettingsExpandIndicator(expanded = expanded, rotateOnExpand = true)
            }

            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, bottom = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    FeedThumbnailQuality.entries.forEach { option ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onQualityChange(option) }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            RadioButton(
                                selected = quality == option,
                                onClick = { onQualityChange(option) },
                            )
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = option.label,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (quality == option) FontWeight.SemiBold else FontWeight.Normal,
                                )
                                Text(
                                    text = option.description,
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

@Composable
private fun SettingsLineSpacingIcon(
    lineHeightMultiplier: Float,
    tint: Color,
    modifier: Modifier = Modifier,
) {
    val gap = when {
        lineHeightMultiplier <= 1.2f -> 1.5.dp
        lineHeightMultiplier <= 1.4f -> 2.5.dp
        lineHeightMultiplier <= 1.6f -> 3.5.dp
        lineHeightMultiplier <= 1.8f -> 5.dp
        else -> 6.5.dp
    }
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(gap),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        repeat(3) {
            Box(
                Modifier
                    .width(14.dp)
                    .height(1.5.dp)
                    .background(tint, RoundedCornerShape(1.dp)),
            )
        }
    }
}

@Composable
private fun SettingsFontSizeIcon(
    sizeSp: Int,
    tint: Color,
    modifier: Modifier = Modifier,
) {
    Text(
        text = "A",
        modifier = modifier,
        color = tint,
        fontSize = (sizeSp * 0.9f).sp,
        fontWeight = FontWeight.SemiBold,
        textAlign = TextAlign.Center,
    )
}

@Composable
private fun <T> SettingsSegmentedControl(
    selected: T,
    options: List<T>,
    onSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
    segmentContent: @Composable (option: T, selected: Boolean) -> Unit,
) {
    val shape = RoundedCornerShape(12.dp)
    val borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f)
    Row(
        modifier = modifier
            .height(44.dp)
            .clip(shape)
            .border(1.dp, borderColor, shape)
            .background(MaterialTheme.colorScheme.surface),
    ) {
        options.forEachIndexed { index, option ->
            val isSelected = selected == option
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                        onClick = { onSelected(option) },
                    ),
                contentAlignment = Alignment.Center,
            ) {
                segmentContent(option, isSelected)
            }
            if (index < options.lastIndex) {
                Box(
                    Modifier
                        .fillMaxHeight()
                        .width(0.5.dp)
                        .background(borderColor),
                )
            }
        }
    }
}

@Composable
private fun SettingsTypographyCard(
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    lineSpacing: FeedLineSpacing,
    onLineSpacingChange: (FeedLineSpacing) -> Unit,
    fontSize: FeedFontSize,
    onFontSizeChange: (FeedFontSize) -> Unit,
    previewItem: FeedItem?,
    emoticonMap: Map<String, String>,
) {
    val subtitle = "行距 ${lineSpacing.label} · 字号 ${fontSize.label}"
    val selectedTint = MaterialTheme.colorScheme.primary
    val unselectedTint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
    val previewTextStyle = rememberFeedTypographyMetrics(fontSize, lineSpacing).body

    SettingsPlainCard {
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
                            text = "行距与字号",
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
                                text = fontSize.label,
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
                SettingsExpandIndicator(expanded = expanded, rotateOnExpand = true)
            }

            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, bottom = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "行距",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        SettingsSegmentedControl(
                            selected = lineSpacing,
                            options = FeedLineSpacing.entries,
                            onSelected = onLineSpacingChange,
                            modifier = Modifier.fillMaxWidth(),
                        ) { option, isSelected ->
                            SettingsLineSpacingIcon(
                                lineHeightMultiplier = option.lineHeightMultiplier,
                                tint = if (isSelected) selectedTint else unselectedTint,
                            )
                        }
                        Text(
                            text = "字号",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        SettingsSegmentedControl(
                            selected = fontSize,
                            options = FeedFontSize.entries,
                            onSelected = onFontSizeChange,
                            modifier = Modifier.fillMaxWidth(),
                        ) { option, isSelected ->
                            SettingsFontSizeIcon(
                                sizeSp = option.sizeSp,
                                tint = if (isSelected) selectedTint else unselectedTint,
                            )
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "预览",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerLowest,
                            border = BorderStroke(
                                0.5.dp,
                                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f),
                            ),
                        ) {
                            if (previewItem != null) {
                                SettingsTypographyPreview(
                                    item = previewItem,
                                    emoticonMap = emoticonMap,
                                    textStyle = previewTextStyle,
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 28.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        text = "暂无微博，请先在首页刷新",
                                        style = MaterialTheme.typography.bodyMedium,
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

@Composable
private fun SettingsTypographyPreview(
    item: FeedItem,
    emoticonMap: Map<String, String>,
    textStyle: TextStyle,
) {
    val resolvedEmoticonMap = remember(emoticonMap, item.emoticons) {
        resolveEmoticonMap(emoticonMap, item.emoticons)
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            RemoteImage(
                url = item.authorAvatarUrl,
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop,
            )
            Column(Modifier.weight(1f)) {
                Text(
                    text = item.authorName.ifBlank { "Weibo user" },
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                item.createdAt?.takeIf { it.isNotBlank() }?.let { createdAt ->
                    Text(
                        text = formatWeiboTime(createdAt),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                    )
                }
            }
        }
        EmoticonText(
            text = item.text,
            emoticonMap = resolvedEmoticonMap,
            style = textStyle,
            maxLines = 4,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun SettingsThemeColorGrid(
    options: List<MorandiThemeColor>,
    selected: MorandiThemeColor,
    onSelected: (MorandiThemeColor) -> Unit,
    cellWidth: Dp,
    columns: Int,
    gap: Dp,
) {
    Column(verticalArrangement = Arrangement.spacedBy(gap)) {
        options.chunked(columns).forEach { rowOptions ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(gap),
            ) {
                rowOptions.forEach { option ->
                    val isSelected = selected.storageValue == option.storageValue
                    val textColor = readableTextColor(option.primary)
                    Box(
                        modifier = Modifier
                            .width(cellWidth)
                            .height(30.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .border(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) {
                                    textColor.copy(alpha = 0.92f)
                                } else {
                                    Color.White.copy(alpha = 0.55f)
                                },
                                shape = RoundedCornerShape(999.dp),
                            )
                            .background(option.primary)
                            .clickable { onSelected(option) },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = option.label,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = textColor,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
                repeat(columns - rowOptions.size) {
                    Spacer(Modifier.width(cellWidth))
                }
            }
        }
    }
}

@Composable
private fun SettingsAppearanceCard(
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    mode: AppearanceMode,
    onModeChange: (AppearanceMode) -> Unit,
) {
    SettingsPlainCard {
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
                            text = "深色模式",
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
                                text = mode.label,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                style = MaterialTheme.typography.labelSmall,
                                maxLines = 1,
                            )
                        }
                    }
                    Text(
                        text = mode.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                SettingsExpandIndicator(expanded = expanded, rotateOnExpand = true)
            }

            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, bottom = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    AppearanceMode.entries.forEach { option ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onModeChange(option) }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            RadioButton(
                                selected = mode == option,
                                onClick = { onModeChange(option) },
                            )
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = option.label,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (mode == option) FontWeight.SemiBold else FontWeight.Normal,
                                )
                                Text(
                                    text = option.description,
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

@Composable
private fun SettingsThemeColorCard(
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    selected: MorandiThemeColor,
    onSelected: (MorandiThemeColor) -> Unit,
) {
    SettingsPlainCard {
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
                            text = "主题颜色",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Surface(
                            color = selected.primaryContainer.copy(alpha = 0.55f),
                            contentColor = selected.primary,
                            shape = RoundedCornerShape(8.dp),
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp),
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(selected.primary),
                                )
                                Text(
                                    text = selected.label,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                )
                            }
                        }
                    }
                    Text(
                        text = "更换应用强调色",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                SettingsExpandIndicator(expanded = expanded, rotateOnExpand = true)
            }

            AnimatedVisibility(visible = expanded) {
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 8.dp, end = 8.dp, bottom = 12.dp),
                ) {
                    val gap = 4.dp
                    val columns = 4
                    val cellWidth = (maxWidth - gap * (columns - 1)) / columns
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        SettingsThemeColorGrid(
                            options = MorandiThemeColors,
                            selected = selected,
                            onSelected = onSelected,
                            cellWidth = cellWidth,
                            columns = columns,
                            gap = gap,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsPlaybackCard(
    backgroundPlaybackEnabled: Boolean,
    onBackgroundPlaybackChange: (Boolean) -> Unit,
) {
    SettingsPlainCard {
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
    expanded: Boolean = false,
    modifier: Modifier = Modifier.size(20.dp),
    tint: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    rotateOnExpand: Boolean = false,
) {
    val rotation by animateFloatAsState(
        targetValue = if (rotateOnExpand && expanded) 180f else 0f,
        animationSpec = tween(200),
        label = "settings_expand_chevron",
    )
    Icon(
        painter = painterResource(R.drawable.ic_chevron_down),
        contentDescription = null,
        modifier = modifier.graphicsLayer { rotationZ = rotation },
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
    onDeleteAccount: (String) -> Unit,
    onAddAccount: () -> Unit,
) {
    val activeAccount = accounts.firstOrNull { it.id == activeAccountId }
    val subtitle = when {
        activeAccount != null -> activeAccount.screenName.ifBlank { "微博用户" }
        accounts.isNotEmpty() -> "已保存 ${accounts.size} 个账号，点击展开切换"
        hasLoginCookie -> "已登录，点击展开管理账号"
        else -> "登录微博以读取主页、信息流与评论"
    }
    val status = when {
        accounts.isNotEmpty() -> "${accounts.size} 个账号"
        hasLoginCookie -> "已登录"
        else -> "未登录"
    }

    SettingsPlainCard {
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
                SettingsExpandIndicator(expanded = expanded, rotateOnExpand = true)
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
                            SettingsAccountRow(
                                account = account,
                                isActive = isActive,
                                onSwitchAccount = onSwitchAccount,
                                onDeleteAccount = onDeleteAccount,
                            )
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

private fun readableTextColor(background: Color): Color {
    val luminance = 0.299f * background.red + 0.587f * background.green + 0.114f * background.blue
    return if (luminance < 0.58f) Color.White else Color(0xFF202124)
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

    SettingsPlainCard {
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
                    SettingsExpandIndicator(expanded = expanded, rotateOnExpand = true)
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
                    imageVector = Icons.Rounded.Settings,
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

private val ProfileHeaderCardRadius = 8.dp
private val ProfileHeaderAvatarImageSize = 100.dp
private val ProfileHeaderAvatarInset = 4.dp
private val ProfileHeaderAvatarFrameSize = ProfileHeaderAvatarImageSize + ProfileHeaderAvatarInset * 2
private val ProfileHeaderCoverAspect = 2.55f
private val ProfileHeaderCardCoverOverlap = 22.dp
private val ProfileHeaderStatsSpacing = 8.dp
private val ProfileHeaderNameLineHeight = 28.dp
private val ProfileHeaderNameTopLift = 12.dp

@Composable
private fun MineProfileHeader(
    profile: UserProfile?,
    hasLoginCookie: Boolean,
    loadError: String?,
    onOpenSettings: (() -> Unit)?,
    onAvatarClick: (() -> Unit)? = null,
    showFollowActions: Boolean = false,
    followLoading: Boolean = false,
    onFollowClick: () -> Unit = {},
    onOpenFollowList: ((FriendListTab) -> Unit)? = null,
) {
    val avatarExposeAboveCard = ProfileHeaderAvatarFrameSize / 3f

    Column(modifier = Modifier.fillMaxWidth()) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(unbounded = true),
        ) {
            val coverHeight = maxWidth / ProfileHeaderCoverAspect

            Column(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(coverHeight)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.20f),
                                    MaterialTheme.colorScheme.surface.copy(alpha = 0.42f),
                                    MaterialTheme.colorScheme.surfaceContainerLow,
                                ),
                            ),
                        ),
                ) {
                    ProfileCoverBanner(
                        coverUrls = profile?.coverUrls.orEmpty(),
                        modifier = Modifier.fillMaxSize(),
                        onOpenSettings = onOpenSettings,
                    )
                }

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(y = -ProfileHeaderCardCoverOverlap)
                        .zIndex(1f),
                    shape = RoundedCornerShape(ProfileHeaderCardRadius),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 0.dp,
                    tonalElevation = 0.dp,
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                start = 16.dp,
                                end = 16.dp,
                                bottom = ProfileHeaderStatsSpacing,
                            ),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Top,
                        ) {
                            Column(
                                modifier = Modifier.width(ProfileHeaderAvatarFrameSize),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                // 头像约 1/3 在卡片外，占位只需卡片内露出的 2/3，避免撑高整行
                                Spacer(Modifier.height(ProfileHeaderAvatarFrameSize - avatarExposeAboveCard))
                                val verifiedReason = profile?.verifiedReason?.takeIf { it.isNotBlank() }
                                val ipLocation = profile?.ipLocation?.takeIf { it.isNotBlank() }
                                if (verifiedReason != null || ipLocation != null) {
                                    Spacer(Modifier.height(6.dp))
                                }
                                verifiedReason?.let { reason ->
                                    Text(
                                        text = reason,
                                        modifier = Modifier.fillMaxWidth(),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center,
                                    )
                                }
                                ipLocation?.let { location ->
                                    if (verifiedReason != null) {
                                        Spacer(Modifier.height(4.dp))
                                    }
                                    Text(
                                        text = "IP\u5C5E\u5730\uFF1A$location",
                                        modifier = Modifier.fillMaxWidth(),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center,
                                    )
                                }
                            }
                            Spacer(Modifier.width(14.dp))
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(
                                        top = (
                                            ProfileHeaderAvatarFrameSize -
                                                avatarExposeAboveCard -
                                                ProfileHeaderNameLineHeight
                                            ) / 2 - ProfileHeaderNameTopLift,
                                    ),
                                verticalArrangement = Arrangement.spacedBy(2.dp),
                            ) {
                                Text(
                                    text = profile?.screenName ?: if (hasLoginCookie) "\u5FAE\u535A\u7528\u6237" else "\u672A\u767B\u5F55",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                val profileDescription = profile?.description?.takeIf { it.isNotBlank() }
                                    ?: if (!hasLoginCookie) "\u8BF7\u5148\u5728\u8BBE\u7F6E\u4E2D\u767B\u5F55\u5FAE\u535A" else null
                                if (profileDescription != null) {
                                    Text(
                                        text = profileDescription,
                                        modifier = Modifier.fillMaxWidth(),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(ProfileHeaderStatsSpacing))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            MineInlineStats(
                                profile = profile,
                                modifier = Modifier.weight(1f),
                                onOpenFollowList = onOpenFollowList,
                            )
                            if (showFollowActions && profile?.id?.isNotBlank() == true) {
                                ProfileFollowCapsuleButton(
                                    following = profile.following,
                                    followMe = profile.followMe,
                                    loading = followLoading,
                                    onClick = onFollowClick,
                                )
                            }
                        }
                    }
                }
            }

            Surface(
                modifier = Modifier
                    .padding(start = 16.dp)
                    .size(ProfileHeaderAvatarFrameSize)
                    .offset(
                        y = coverHeight - ProfileHeaderCardCoverOverlap - avatarExposeAboveCard,
                    )
                    .zIndex(2f),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 2.dp,
            ) {
                RemoteImage(
                    url = profile?.avatarUrl,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(ProfileHeaderAvatarInset)
                        .clip(CircleShape)
                        .clickable(
                            enabled = onAvatarClick != null &&
                                !profile?.avatarUrl.isNullOrBlank(),
                            onClick = { onAvatarClick?.invoke() },
                        ),
                    contentScale = ContentScale.Crop,
                )
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

private val WeiboFollowOrange = Color(0xFFFF8200)

@Composable
private fun VisitedUserProfileContent(
    session: WeiboWebSession,
    profile: UserProfile?,
    profileHeaderHeight: Dp,
    onProfileHeaderHeightChange: (Dp) -> Unit,
    isLoading: Boolean,
    hasLoginCookie: Boolean,
    posts: List<FeedItem>,
    postsLoadingMore: Boolean,
    albumImages: List<FeedImage>,
    albumLoading: Boolean,
    albumLoadingMore: Boolean,
    postsHasMore: Boolean,
    albumHasMore: Boolean,
    emoticonMap: Map<String, String>,
    emoticonCount: Int,
    emoticonSyncing: Boolean,
    postsListState: LazyListState,
    albumListState: LazyListState,
    albumError: String?,
    initialPagerPage: Int,
    onMinePagerPageChanged: (Int) -> Unit,
    onAlbumTabSelected: () -> Unit,
    onRefresh: () -> Unit,
    onLoadMorePosts: () -> Unit,
    onLoadMoreAlbum: () -> Unit,
    onSyncEmoticons: () -> Unit,
    onItemClick: (FeedItem) -> Unit,
    onRetweetClick: (FeedItem, FeedItem) -> Unit = { item, _ -> onItemClick(item) },
    onCommentLongClick: (FeedItem) -> Unit,
    onOpenAlbumViewer: (AlbumViewerState) -> Unit,
    onMediaClick: (FeedMedia, String) -> Unit,
    onUserClick: (String) -> Unit,
    isLongTextLoading: (FeedItem) -> Boolean,
    onLoadLongText: (FeedItem) -> Unit,
    onToggleLike: (FeedItem) -> Unit,
    onLikeClick: (FeedItem, Rect) -> Unit,
    onUrlEntityClick: (FeedUrlEntity) -> Unit,
    onOpenFollowList: (String, String, String?, String?, FriendListTab) -> Unit,
    showFollowActions: Boolean,
    followLoading: Boolean,
    onFollowClick: () -> Unit,
) {
    MineScreen(
        session = session,
        profile = profile,
        profileHeaderHeight = profileHeaderHeight,
        onProfileHeaderHeightChange = onProfileHeaderHeightChange,
        isLoading = isLoading,
        loadError = null,
        hasLoginCookie = hasLoginCookie,
        posts = posts,
        postsError = null,
        postsLoadingMore = postsLoadingMore,
        albumImages = albumImages,
        albumLoading = albumLoading,
        albumLoadingMore = albumLoadingMore,
        postsHasMore = postsHasMore,
        albumHasMore = albumHasMore,
        emoticonMap = emoticonMap,
        emoticonCount = emoticonCount,
        emoticonSyncing = emoticonSyncing,
        postsListState = postsListState,
        albumListState = albumListState,
        albumError = albumError,
        initialPagerPage = initialPagerPage,
        onMinePagerPageChanged = onMinePagerPageChanged,
        onAlbumTabSelected = onAlbumTabSelected,
        onRefresh = onRefresh,
        onLoadMorePosts = onLoadMorePosts,
        onLoadMoreAlbum = onLoadMoreAlbum,
        onSyncEmoticons = onSyncEmoticons,
        onItemClick = onItemClick,
        onRetweetClick = onRetweetClick,
        onCommentLongClick = onCommentLongClick,
        onOpenAlbumViewer = onOpenAlbumViewer,
        onMediaClick = onMediaClick,
        onUserClick = onUserClick,
        isLongTextLoading = isLongTextLoading,
        onLoadLongText = onLoadLongText,
        onToggleLike = onToggleLike,
        onLikeClick = onLikeClick,
        onUrlEntityClick = onUrlEntityClick,
        onOpenFollowList = onOpenFollowList,
        enableSettings = false,
        showPullRefreshIndicator = false,
        showFollowActions = showFollowActions,
        followLoading = followLoading,
        onFollowClick = onFollowClick,
    )
}

@Composable
private fun FollowFansListScreen(
    session: WeiboWebSession,
    overlay: FollowListOverlayState,
    followingListState: LazyListState,
    fansListState: LazyListState,
    tabCaches: MutableMap<String, FollowListTabCache>,
    backHandlerEnabled: Boolean = true,
    onBack: () -> Unit,
    onTabChange: (FriendListTab) -> Unit,
    onUserClick: (String) -> Unit,
    showMessage: (String, String) -> Unit,
    mineProfileId: String?,
) {
    val topInset = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val tabs = FriendListTab.entries
    val pagerState = rememberPagerState(
        initialPage = tabs.indexOf(overlay.tab).coerceAtLeast(0),
        pageCount = { tabs.size },
    )
    val coroutineScope = rememberCoroutineScope()

    BackHandler(enabled = backHandlerEnabled, onBack = onBack)

    LaunchedEffect(overlay.tab) {
        val target = tabs.indexOf(overlay.tab).coerceAtLeast(0)
        if (pagerState.currentPage != target) {
            pagerState.scrollToPage(target)
        }
    }

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }
            .distinctUntilChanged()
            .collect { page ->
                val tab = tabs[page]
                if (tab != overlay.tab) onTabChange(tab)
            }
    }

    val tabScrollPosition by remember {
        derivedStateOf { pagerState.currentPage + pagerState.currentPageOffsetFraction }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(
                        top = topInset + 12.dp,
                        bottom = 12.dp,
                        start = 16.dp,
                        end = 16.dp,
                    ),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                RemoteImage(
                    url = overlay.avatarUrl,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop,
                )
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .consumeTouchEvents(),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = overlay.screenName.ifBlank { "微博用户" },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = overlay.description?.takeIf { it.isNotBlank() } ?: "暂无简介",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            FollowListContentTabs(
                scrollPosition = tabScrollPosition,
                onTabSelected = { tab ->
                    val sameTab = tab == tabs[pagerState.currentPage]
                    if (sameTab) {
                        coroutineScope.launch {
                            when (tab) {
FriendListTab.Following -> followingListState.animateScrollToTopFixed()
                FriendListTab.Fans -> fansListState.animateScrollToTopFixed()
                            }
                        }
                    } else {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(tabs.indexOf(tab))
                        }
                    }
                },
            )
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxWidth().weight(1f),
                beyondViewportPageCount = 1,
            ) { page ->
                FollowListTabPage(
                    session = session,
                    uid = overlay.uid,
                    tab = tabs[page],
                    instanceKey = overlay.instanceKey,
                    tabCaches = tabCaches,
                    listState = when (tabs[page]) {
                        FriendListTab.Following -> followingListState
                        FriendListTab.Fans -> fansListState
                    },
                    onUserClick = onUserClick,
                    showMessage = showMessage,
                    mineProfileId = mineProfileId,
                )
            }
        }
    }
}

@Composable
private fun FollowListTabPage(
    session: WeiboWebSession,
    uid: String,
    tab: FriendListTab,
    instanceKey: Int,
    tabCaches: MutableMap<String, FollowListTabCache>,
    listState: LazyListState,
    onUserClick: (String) -> Unit,
    showMessage: (String, String) -> Unit,
    mineProfileId: String?,
) {
    val scope = rememberCoroutineScope()
    val cacheKey = followListTabCacheKey(instanceKey, uid, tab)
    var users by remember(instanceKey, uid, tab) { mutableStateOf<List<RelationUser>>(emptyList()) }
    var page by remember(instanceKey, uid, tab) { mutableIntStateOf(1) }
    var hasMore by remember(instanceKey, uid, tab) { mutableStateOf(true) }
    var loading by remember(instanceKey, uid, tab) { mutableStateOf(true) }
    var refreshing by remember(instanceKey, uid, tab) { mutableStateOf(false) }
    var loadingMore by remember(instanceKey, uid, tab) { mutableStateOf(false) }
    var errorMsg by remember(instanceKey, uid, tab) { mutableStateOf<String?>(null) }
    val loadMutex = remember(instanceKey, uid, tab) { Mutex() }

    fun persistCache() {
        tabCaches[cacheKey] = FollowListTabCache(
            users = users,
            page = page,
            hasMore = hasMore,
            errorMsg = errorMsg,
        )
    }

    suspend fun loadFirstPage() {
        runCatching {
            session.loadFriends(uid, 1, tab)
        }.onSuccess { result ->
            errorMsg = result.errorMsg
            users = result.items
            hasMore = result.hasNextPage && result.items.isNotEmpty()
            page = 1
            persistCache()
        }.onFailure { error ->
            if (error is CancellationException) throw error
            users = emptyList()
            errorMsg = error.message ?: "加载失败"
            hasMore = false
            persistCache()
        }
    }

    fun refreshList() {
        scope.launch {
            refreshing = true
            loadMutex.withLock {
                loadingMore = false
                errorMsg = null
                loadFirstPage()
            }
            runCatching { listState.animateScrollToTopFixed() }
            refreshing = false
        }
    }

    LaunchedEffect(instanceKey, uid, tab) {
        tabCaches[cacheKey]?.let { cache ->
            users = cache.users
            page = cache.page
            hasMore = cache.hasMore
            errorMsg = cache.errorMsg
            loading = false
            return@LaunchedEffect
        }
        loading = true
        loadingMore = false
        users = emptyList()
        page = 1
        hasMore = true
        errorMsg = null
        loadFirstPage()
        loading = false
    }

    LaunchedEffect(listState, uid, tab) {
        snapshotFlow {
            val info = listState.layoutInfo
            val last = info.visibleItemsInfo.lastOrNull()?.index ?: 0
            info.totalItemsCount > 0 && last >= info.totalItemsCount - 3
        }
            .distinctUntilChanged()
            .filter { it }
            .collect {
                loadMutex.withLock {
                    if (loading || loadingMore || refreshing || !hasMore || errorMsg != null) return@withLock
                    val nextPage = page + 1
                    loadingMore = true
                    try {
                        val result = session.loadFriends(uid, nextPage, tab)
                        errorMsg = result.errorMsg
                        val beforeSize = users.size
                        users = (users + result.items).distinctBy { it.id }
                        hasMore = result.hasNextPage &&
                            result.items.isNotEmpty() &&
                            users.size > beforeSize
                        page = nextPage
                        persistCache()
                    } catch (_: CancellationException) {
                        // Tab switched or composition left while loading.
                    } catch (error: Exception) {
                        showMessage("加载失败", error.message ?: "请稍后重试")
                    } finally {
                        loadingMore = false
                    }
                }
            }
    }

    AppPullToRefreshBox(
        isRefreshing = refreshing,
        onRefresh = ::refreshList,
        modifier = Modifier.fillMaxSize(),
    ) {
        LazyColumn(
            state = listState,
            flingBehavior = rememberWeiboListFlingBehavior(),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 24.dp),
        ) {
            when {
                loading && users.isEmpty() -> {
                    item {
                        Box(
                            Modifier.fillMaxWidth().padding(48.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }
                users.isEmpty() -> {
                    item {
                        Box(
                            Modifier.fillMaxWidth().padding(24.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = errorMsg ?: if (tab == FriendListTab.Fans) {
                                    "还没有粉丝"
                                } else {
                                    "还没有关注"
                                },
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
                else -> {
                    items(users, key = { it.id }, contentType = { "relation_user" }) { user ->
                        RelationUserRow(
                            user = user,
                            showFollowButton = !mineProfileId.isNullOrBlank() && user.id != mineProfileId,
                            onClick = { onUserClick(user.id) },
                            session = session,
                            onFollowChanged = { updated ->
                                users = users.map { if (it.id == updated.id) updated else it }
                            },
                            showMessage = showMessage,
                        )
                    }
                    if (loadingMore) {
                        item {
                            MineLoadingMoreIndicator()
                        }
                    }
                    if (!hasMore && users.isNotEmpty()) {
                        item {
                            Text(
                                text = "已经到底了",
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

@Composable
private fun FollowListContentTabs(
    scrollPosition: Float,
    onTabSelected: (FriendListTab) -> Unit,
) {
    val tabs = FriendListTab.entries
    val accent = MaterialTheme.colorScheme.primary
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
                val isSelected = index == highlightedIndex
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
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (isSelected) accent else MaterialTheme.colorScheme.onSurfaceVariant,
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
private fun RelationUserRow(
    user: RelationUser,
    showFollowButton: Boolean,
    onClick: () -> Unit,
    session: WeiboWebSession,
    onFollowChanged: (RelationUser) -> Unit,
    showMessage: (String, String) -> Unit,
) {
    var following by remember(user.id) { mutableStateOf(user.following) }
    var followMe by remember(user.id) { mutableStateOf(user.followMe) }
    var followLoading by remember(user.id) { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(user.following, user.followMe) {
        following = user.following
        followMe = user.followMe
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.Top,
        ) {
            RemoteImage(
                url = user.avatarLarge?.takeIf { it.isNotBlank() } ?: user.avatarUrl,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop,
            )
            Spacer(Modifier.width(12.dp))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = user.name,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                user.description?.takeIf { it.isNotBlank() }?.let { description ->
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "${user.followersCountStr?.takeIf { it.isNotBlank() } ?: user.followersCount} 粉丝",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    user.location?.takeIf { it.isNotBlank() }?.let { location ->
                        Text(
                            text = location,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
            if (showFollowButton) {
                Spacer(Modifier.width(8.dp))
                ProfileFollowCapsuleButton(
                    following = following,
                    followMe = followMe,
                    loading = followLoading,
                    onClick = {
                        scope.launch {
                            followLoading = true
                            val wasFollowing = following
                            val profile = user.copy(following = following, followMe = followMe).toUserProfile()
                            runCatching {
                                if (wasFollowing) {
                                    session.unfollowUser(user.id, profile)
                                } else {
                                    session.followUser(user.id, profile)
                                }
                            }.onSuccess { updatedProfile ->
                                following = updatedProfile.following
                                followMe = updatedProfile.followMe
                                onFollowChanged(
                                    user.copy(
                                        following = updatedProfile.following,
                                        followMe = updatedProfile.followMe,
                                    ),
                                )
                            }.onFailure { error ->
                                showMessage("关注操作失败", error.message ?: "请稍后重试")
                            }
                            followLoading = false
                        }
                    },
                )
            }
        }
        HorizontalDivider(
            modifier = Modifier.padding(start = 76.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.08f),
        )
    }
}

@Composable
private fun ProfileFollowCapsuleButton(
    following: Boolean,
    followMe: Boolean = false,
    loading: Boolean,
    onClick: () -> Unit,
) {
    val mutual = following && followMe
    val label = when {
        !following -> "+\u5173\u6CE8"
        mutual -> "\u4E92\u76F8\u5173\u6CE8"
        else -> "\u5DF2\u5173\u6CE8"
    }
    Surface(
        onClick = onClick,
        enabled = !loading,
        shape = RoundedCornerShape(999.dp),
        color = if (following) MaterialTheme.colorScheme.surface else WeiboFollowOrange,
        border = if (following) {
            BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        } else {
            null
        },
    ) {
        Box(
            modifier = Modifier.padding(
                horizontal = if (mutual) 10.dp else 14.dp,
                vertical = 6.dp,
            ),
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
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    fontSize = if (mutual) 12.sp else MaterialTheme.typography.labelMedium.fontSize,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    color = if (following) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        Color.White
                    },
                )
            }
        }
    }
}

@Composable
private fun MineInlineStats(
    profile: UserProfile?,
    modifier: Modifier = Modifier,
    onOpenFollowList: ((FriendListTab) -> Unit)? = null,
) {
    val canOpenList = onOpenFollowList != null && !profile?.id.isNullOrBlank()
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        MineInlineStat(
            value = profile?.followersCount ?: "--",
            label = "\u7C89\u4E1D",
            onClick = if (canOpenList) {
                { onOpenFollowList(FriendListTab.Fans) }
            } else {
                null
            },
        )
        MineInlineStat(
            value = profile?.followingCount ?: "--",
            label = "\u5173\u6CE8",
            onClick = if (canOpenList) {
                { onOpenFollowList(FriendListTab.Following) }
            } else {
                null
            },
        )
        MineInlineStat(value = profile?.statusesCount ?: "--", label = "\u5FAE\u535A")
    }
}

@Composable
private fun MineInlineStat(
    value: String,
    label: String,
    onClick: (() -> Unit)? = null,
) {
    Row(
        modifier = if (onClick != null) {
            Modifier.clickable(onClick = onClick)
        } else {
            Modifier
        },
        verticalAlignment = Alignment.CenterVertically,
    ) {
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
    val accent = MaterialTheme.colorScheme.primary
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
            .padding(start = 12.dp, bottom = 4.dp),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(22.dp),
            verticalAlignment = Alignment.Bottom,
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
                        .padding(horizontal = 2.dp, vertical = 2.dp),
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

private data class AlbumGridRow(
    val key: String,
    val monthKey: Pair<String, String>,
    val dateLabel: Pair<String, String>?,
    val monthImages: List<FeedImage>,
    val rowImages: List<FeedImage>,
    val rowStartIndex: Int,
)

private fun resolveStickyAlbumMonthLabel(
    listState: LazyListState,
    albumRows: List<AlbumGridRow>,
): Pair<String, String>? {
    if (albumRows.isEmpty()) return null
    val topRowIndex = listState.layoutInfo.visibleItemsInfo
        .mapNotNull { info -> info.index.takeIf { it in albumRows.indices } }
        .minOrNull() ?: return null
    val monthKey = albumRows[topRowIndex].monthKey
    val firstRowIndex = albumRows.indexOfFirst { row ->
        row.monthKey == monthKey && row.rowStartIndex == 0
    }
    if (firstRowIndex < 0) return null
    val inlineLabelVisible = listState.firstVisibleItemIndex == firstRowIndex &&
        listState.firstVisibleItemScrollOffset == 0
    return if (inlineLabelVisible) null else monthKey
}

private data class AlbumPostLookup(
    val postByStatusId: Map<String, FeedItem>,
    val postByImageKey: Map<String, FeedItem>,
    val imageByKey: Map<String, FeedImage>,
)

private fun buildAlbumPostLookup(posts: List<FeedItem>): AlbumPostLookup {
    if (posts.isEmpty()) {
        return AlbumPostLookup(emptyMap(), emptyMap(), emptyMap())
    }
    val postByStatusId = mutableMapOf<String, FeedItem>()
    val postByImageKey = mutableMapOf<String, FeedItem>()
    val imageByKey = mutableMapOf<String, FeedImage>()
    posts.forEach { post ->
        listOfNotNull(post.id, post.statusId)
            .filter { it.isNotBlank() }
            .forEach { id -> postByStatusId.putIfAbsent(id, post) }
        postImageMatchKeys(post).forEach { key ->
            postByImageKey.putIfAbsent(key, post)
        }
        (post.images + (post.retweetedStatus?.images ?: emptyList())).forEach { image ->
            albumImageMatchKeys(image).forEach { key ->
                imageByKey.putIfAbsent(key, image)
            }
        }
    }
    return AlbumPostLookup(postByStatusId, postByImageKey, imageByKey)
}

private fun findPostForAlbumImage(lookup: AlbumPostLookup, image: FeedImage): FeedItem? {
    val statusId = image.statusId?.takeIf { it.isNotBlank() }
    if (statusId != null) {
        lookup.postByStatusId[statusId]?.let { return it }
    }
    val imageKeys = albumImageMatchKeys(image)
    if (imageKeys.isEmpty()) return null
    return imageKeys.firstNotNullOfOrNull { lookup.postByImageKey[it] }
}

private fun buildAlbumGridRows(
    grouped: List<Pair<Pair<String, String>, List<FeedImage>>>,
): List<AlbumGridRow> = buildList {
    grouped.forEach { (dateLabel, monthImages) ->
        monthImages.chunked(3).forEachIndexed { rowIndex, rowImages ->
            val rowStartIndex = rowIndex * 3
            val first = rowImages.firstOrNull()
            add(
                AlbumGridRow(
                    key = "album-${dateLabel.first}-${dateLabel.second}-$rowStartIndex-${first?.id.orEmpty()}-${first?.largeUrl.orEmpty()}",
                    monthKey = dateLabel,
                    dateLabel = dateLabel.takeIf { rowIndex == 0 },
                    monthImages = monthImages,
                    rowImages = rowImages,
                    rowStartIndex = rowStartIndex,
                ),
            )
        }
    }
}

private fun groupAlbumImagesByMonth(
    albumImages: List<FeedImage>,
): List<Pair<Pair<String, String>, List<FeedImage>>> {
    return albumImages
        .distinctBy { "${it.type.orEmpty()}:${it.id}:${it.largeUrl}" }
        .mapNotNull { image ->
            albumMonthLabelForImage(image)?.let { label -> label to image }
        }
        .groupBy({ it.first }, { it.second })
        .mapValues { entry -> entry.value.distinctBy { it.largeUrl } }
        .entries
        .sortedWith(
            compareByDescending<Map.Entry<Pair<String, String>, List<FeedImage>>> {
                albumGroupSortKey(it.key)
            },
        )
        .map { it.key to it.value }
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

private fun enrichAlbumImagesFromPosts(
    images: List<FeedImage>,
    lookup: AlbumPostLookup,
): List<FeedImage> {
    if (images.isEmpty()) return images
    if (lookup.imageByKey.isEmpty() &&
        lookup.postByStatusId.isEmpty() &&
        lookup.postByImageKey.isEmpty()
    ) {
        return images
    }
    return images.map { image ->
        val post = findPostForAlbumImage(lookup, image)
        val match = albumImageMatchKeys(image)
            .firstNotNullOfOrNull { key -> lookup.imageByKey[key] }
        val mergedLiveVideo = image.livePhotoVideoUrl ?: match?.livePhotoVideoUrl
        val mergedStream = image.videoStreamUrl
            ?: match?.videoStreamUrl
            ?: post?.media?.takeIf { it.type == MediaType.Video && it.isStreamPlayable() }?.streamUrl
        if (match == null && mergedStream == null && mergedLiveVideo == null) {
            return@map image
        }
        image.copy(
            livePhotoVideoUrl = mergedLiveVideo,
            videoStreamUrl = mergedStream,
            type = when {
                mergedStream != null -> "video"
                !mergedLiveVideo.isNullOrBlank() -> "livephoto"
                match?.type == "gif" -> "gif"
                image.type.isNullOrBlank() -> match?.type
                else -> image.type
            },
            width = image.width ?: match?.width,
            height = image.height ?: match?.height,
            downloadUrls = (image.downloadUrls + (match?.downloadUrls ?: emptyList())).distinct(),
        )
    }
}

private fun sinaimgPid(url: String): String? =
    Regex("""/([^/?#]+)\.(?:jpg|jpeg|png|gif|webp)""", RegexOption.IGNORE_CASE)
        .find(url)
        ?.groupValues
        ?.getOrNull(1)

@Composable
private fun AlbumMonthStickyHeader(
    dateLabel: Pair<String, String>,
    modifier: Modifier = Modifier,
) {
    AlbumMonthLabelColumn(
        dateLabel = dateLabel,
        modifier = modifier
            .padding(start = 12.dp, top = 6.dp)
            .background(MaterialTheme.colorScheme.background)
            .padding(bottom = 2.dp),
    )
}

@Composable
private fun AlbumMonthLabelColumn(
    dateLabel: Pair<String, String>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.width(42.dp),
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
}

@Composable
private fun MineAlbumGridRow(
    dateLabel: Pair<String, String>?,
    monthImages: List<FeedImage>,
    rowImages: List<FeedImage>,
    rowStartIndex: Int,
    relatedPosts: List<FeedItem>,
    onImageClick: (List<FeedImage>, Int) -> Unit,
    onVideoClick: (FeedMedia, String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top,
    ) {
        if (dateLabel != null) {
            AlbumMonthLabelColumn(dateLabel = dateLabel)
        } else {
            Spacer(modifier = Modifier.width(42.dp))
        }
        BoxWithConstraints(
            modifier = Modifier
                .weight(1f)
                .wrapContentHeight(),
        ) {
            val gap = 6.dp
            val cellSize = (maxWidth - gap * 2) / 3
            Row(horizontalArrangement = Arrangement.spacedBy(gap)) {
                rowImages.forEachIndexed { columnIndex, image ->
                    val imageIndex = rowStartIndex + columnIndex
                    MineAlbumTile(
                        image = image,
                        size = cellSize,
                        allImages = monthImages,
                        imageIndex = imageIndex,
                        relatedPosts = relatedPosts,
                        onOpenViewer = { index, _, _, _ -> onImageClick(monthImages, index) },
                        onVideoClick = onVideoClick,
                    )
                }
            }
        }
    }
}

@Composable
private fun MineAlbumTile(
    image: FeedImage,
    size: androidx.compose.ui.unit.Dp,
    allImages: List<FeedImage>,
    imageIndex: Int,
    relatedPosts: List<FeedItem>,
    onOpenViewer: (Int, Rect?, (() -> Unit)?, (() -> Unit)?) -> Unit,
    onVideoClick: (FeedMedia, String) -> Unit,
) {
    val imageOwner = remember(image.id, image.largeUrl, relatedPosts) {
        findPostForAlbumImage(relatedPosts, image)
    }
    val albumMedia = remember(image.id, image.largeUrl, image.videoStreamUrl) {
        image.toAlbumFeedMedia()
    }
    val playbackOwnerId = image.statusId?.takeIf { it.isNotBlank() } ?: image.id
    if (albumMedia != null && image.isAlbumVideo) {
        AlbumVideoTile(
            image = image,
            media = albumMedia,
            size = size,
            playbackOwnerId = playbackOwnerId,
            onVideoClick = { onVideoClick(albumMedia, playbackOwnerId) },
        )
    } else {
        FeedImageCell(
            image = image,
            allImages = allImages,
            imageIndex = imageIndex,
            imageOwner = imageOwner,
            modifier = Modifier.size(size),
            cornerRadius = 8.dp,
            maxDecodeDimOverride = AlbumGridMaxDecodeDim,
            onOpenViewer = onOpenViewer,
        )
    }
}

@Composable
private fun AlbumVideoTile(
    image: FeedImage,
    media: FeedMedia,
    size: androidx.compose.ui.unit.Dp,
    playbackOwnerId: String,
    onVideoClick: () -> Unit,
) {
    val videoCoordinator = LocalVideoPlaybackCoordinator.current
    val videoPeekController = LocalVideoPeekController.current
    val feedListScrollCoordinator = LocalFeedListScrollCoordinator.current
    val gestureScope = rememberCoroutineScope()
    val mediaHaptics = rememberMediaPeekHaptics()
    val playbackKey = remember(media.streamUrl, media.downloadUrl, media.coverUrl, playbackOwnerId) {
        videoPlaybackKey(media, playbackOwnerId)
    }
    var actionOpen by remember(media.streamUrl) { mutableStateOf(false) }
    var peekActive by remember(media.streamUrl) { mutableStateOf(false) }
    var pressHoldProgress by remember(media.streamUrl) { mutableFloatStateOf(0f) }
    val anchorHolder = remember(media.streamUrl) { LayoutAnchorHolder() }
    val holdScale = mediaPeekHoldScale(if (actionOpen) 0f else pressHoldProgress)

    fun resetPeekState() {
        actionOpen = false
        peekActive = false
        pressHoldProgress = 0f
        videoPeekController.resetFingerDragOffset()
    }

    fun openVideoPeek(pressWindowOffset: Offset) {
        val bounds = anchorHolder.boundsInWindow() ?: return
        actionOpen = true
        peekActive = true
        pressHoldProgress = 0f
        videoCoordinator.schedulePeekRestartIfAtEnd(playbackKey, videoDurationMs(media))
        videoCoordinator.claimPeekPlayback(playbackKey)
        videoPeekController.open(
            VideoPeekRequest(
                media = media,
                anchorBounds = bounds,
                pressOffset = pressWindowOffset,
                playbackOwnerId = playbackOwnerId,
                resolveAnchorBounds = {
                    resolveVideoAnchorBounds(anchorHolder.coordinates, anchorHolder.boundsInWindow())
                },
                onCancel = { resetPeekState() },
                onRelease = {},
                onPlaybackEnded = { resetPeekState() },
                onOpenFullscreenBehind = {},
                onEnterFullscreenHandoffComplete = {},
            ),
        )
    }

    Box(
        modifier = Modifier
            .size(size)
            .zIndex(if (actionOpen || peekActive) 10f else 0f)
            .onGloballyPositioned { coordinates ->
                anchorHolder.coordinates = coordinates
            }
            .graphicsLayer {
                scaleX = holdScale
                scaleY = holdScale
                alpha = if (actionOpen) 0f else 1f
            }
            .clip(RoundedCornerShape(8.dp))
            .pointerInput(media.streamUrl) {
                awaitEachGesture {
                    val bounds = anchorHolder.boundsInWindow() ?: return@awaitEachGesture
                    val down = awaitFirstDown(requireUnconsumed = false)
                    if (cancelFeedListScrollOnTap(feedListScrollCoordinator, gestureScope, down)) {
                        return@awaitEachGesture
                    }
                    var pressResult = MediaLongPressResult.Tap
                    pressResult = awaitMediaLongPress(
                        down = down,
                        viewConfiguration = viewConfiguration,
                        onHoldProgress = { pressHoldProgress = it },
                        onHaptic = mediaHaptics::perform,
                    )

                    when (pressResult) {
                        MediaLongPressResult.Tap -> onVideoClick()
                        MediaLongPressResult.Cancelled -> Unit
                        MediaLongPressResult.LongPress -> Unit
                    }
                    if (pressResult != MediaLongPressResult.LongPress) {
                        return@awaitEachGesture
                    }

                    if (!media.isStreamPlayable()) {
                        onVideoClick()
                        return@awaitEachGesture
                    }

                    val pressWindowOffset = down.position.toWindowPosition(bounds)
                    down.consume()
                    openVideoPeek(pressWindowOffset)
                    videoPeekController.updateFingerDragOffset(Offset.Zero)

                    val dragResult = awaitVideoPeekDragGesture(
                        down = down,
                        pressWindowOffset = pressWindowOffset,
                        bounds = bounds,
                        videoPeekController = videoPeekController,
                    )
                    if (dragResult.cancelledByDrag) {
                        resetPeekState()
                    } else if (!dragResult.floatByDrag) {
                        videoPeekController.enterFullscreen()
                    }
                }
            },
    ) {
        FeedImageThumbnailContent(
            image = image,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            maxDecodeDimOverride = AlbumGridMaxDecodeDim,
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_video_play),
                contentDescription = "视频",
                modifier = Modifier.size(28.dp),
                tint = Color.White.copy(alpha = 0.92f),
            )
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

private fun albumMonthLabelForImage(image: FeedImage): Pair<String, String>? {
    albumMonthLabel(image.createdAt)?.let { return it }
    return image.statusId?.let(::albumMonthLabelFromMid)
}

private fun albumMonthLabelFromMid(mid: String): Pair<String, String>? {
    val id = mid.trim().toLongOrNull() ?: return null
    val seconds = (id shr 22) + 515_483_463L
    if (seconds < 1_000_000_000L || seconds > 4_102_444_800L) return null
    val date = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).apply {
        timeZone = java.util.TimeZone.getTimeZone("Asia/Shanghai")
    }.format(java.util.Date(seconds * 1000))
    return albumMonthLabel(date)
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
private fun AccountLoginPanel(
    session: WeiboWebSession,
    autoReturnToFeedOnLogin: Boolean = false,
    onLoginSuccess: () -> Unit = {},
    onReturnToFeed: () -> Unit,
) {
    var loginReloadKey by remember { mutableIntStateOf(0) }

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
                    if (autoReturnToFeedOnLogin) {
                        "\u4F7F\u7528\u4E0E\u6D88\u606F\u3001\u5199\u5FAE\u535A\u76F8\u540C\u7684\u72EC\u7ACB\u7F51\u9875\u5BB9\u5668\u6253\u5F00 passport.weibo.cn\u3002\u767B\u5F55\u6210\u529F\u540E\u5C06\u81EA\u52A8\u8FD4\u56DE\u5FAE\u535A\u9996\u9875\u540C\u6B65\u6570\u636E\u3002"
                    } else {
                        "\u4F7F\u7528\u4E0E\u6D88\u606F\u3001\u5199\u5FAE\u535A\u76F8\u540C\u7684\u72EC\u7ACB\u7F51\u9875\u5BB9\u5668\u6253\u5F00 passport.weibo.cn\u3002\u767B\u5F55\u6210\u529F\u540E\u70B9\u51FB\u56DE\u5230\u5FAE\u535A\u9996\u9875\u540C\u6B65\u6570\u636E\u3002"
                    },
                    style = MaterialTheme.typography.bodySmall,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = onReturnToFeed) {
                        Text("\u56DE\u5230\u5FAE\u535A\u9996\u9875")
                    }
                    TextButton(onClick = { loginReloadKey += 1 }) {
                        Text("\u91CD\u65B0\u6253\u5F00")
                    }
                }
            }
        }
        AccountLoginWebView(
            session = session,
            reloadKey = loginReloadKey,
            onLoginSuccess = onLoginSuccess,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        )
    }
}

@Composable
private fun HiddenSessionWebView(session: WeiboWebSession) {
    val webView = session.webView
    DisposableEffect(webView) {
        webView.onResume()
        onDispose {
            webView.onPause()
        }
    }
    AndroidView(
        modifier = Modifier.size(1.dp),
        factory = {
            webView.detachFromParent()
            webView
        },
    )
}

@Composable
private fun AccountLoginWebView(
    session: WeiboWebSession,
    reloadKey: Int,
    onLoginSuccess: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val onLoginSuccessState = rememberUpdatedState(onLoginSuccess)
    val loginSuccessNotified = remember(reloadKey) { java.util.concurrent.atomic.AtomicBoolean(false) }
    val loginUrl = "https://passport.weibo.cn/signin/login"
    val loginUserAgent =
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 " +
            "(KHTML, like Gecko) Chrome/126.0.0.0 Safari/537.36"
    val webView = remember(reloadKey) {
        WebView(context.applicationContext).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
            )
            setLayerType(View.LAYER_TYPE_HARDWARE, null)
            importantForAutofill = View.IMPORTANT_FOR_AUTOFILL_NO_EXCLUDE_DESCENDANTS
            setBackgroundColor(android.graphics.Color.WHITE)
            CookieManager.getInstance().setAcceptCookie(true)
            CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)
            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                loadsImagesAutomatically = true
                useWideViewPort = true
                loadWithOverviewMode = true
                mediaPlaybackRequiresUserGesture = false
                mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
                cacheMode = WebSettings.LOAD_DEFAULT
                userAgentString = loginUserAgent
            }
            webViewClient = object : WebViewClient() {
                private fun notifyLoginSuccessIfNeeded(host: WebView?) {
                    if (session.hasLoginCookie() && loginSuccessNotified.compareAndSet(false, true)) {
                        (host ?: this@apply).post { onLoginSuccessState.value() }
                    }
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    notifyLoginSuccessIfNeeded(view)
                }

                override fun onReceivedError(
                    view: WebView?,
                    errorCode: Int,
                    description: String?,
                    failingUrl: String?,
                ) {
                    if (failingUrl == loginUrl) {
                        view?.loadUrl(loginUrl)
                    }
                }
            }
            loadUrl(loginUrl)
        }
    }

    DisposableEffect(webView) {
        webView.onResume()
        onDispose {
            webView.onPause()
            webView.stopLoading()
            webView.webViewClient = WebViewClient()
            webView.post {
                runCatching { webView.destroy() }
            }
        }
    }

    AndroidView(
        modifier = modifier,
        factory = { webView },
        update = { view ->
            view.visibility = View.VISIBLE
            if (view.url == "about:blank") {
                view.loadUrl(loginUrl)
            }
        },
    )
}

private fun wrapArticleHtml(content: String): String = """
    <!DOCTYPE html>
    <html>
    <head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0">
    <style>
    body { font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif; padding: 16px; margin: 0; line-height: 1.7; color: #333; word-break: break-word; }
    img { max-width: 100%; height: auto; }
    a { color: #ff8200; }
    </style>
    </head>
    <body>$content</body>
    </html>
""".trimIndent()

@Composable
private fun ArticleReaderOverlay(
    state: ArticleOverlayState,
    onBack: () -> Unit,
    onRetry: () -> Unit,
) {
    val topInset = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val title = state.article?.title ?: state.entity.title
    val htmlContent = state.article?.htmlContent
    val fallbackUrl = state.entity.url.takeIf {
        state.article == null && !state.loading && state.error == null
    }

    BackHandler(onBack = onBack)

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = topInset)
                    .padding(horizontal = 8.dp, vertical = 8.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                    ) {
                        Text(
                            text = title,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            fontWeight = FontWeight.SemiBold,
                        )
                        state.article?.authorName?.let { author ->
                            Text(
                                text = author,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
            }
            HorizontalDivider()
            Box(Modifier.fillMaxSize()) {
                when {
                    state.loading -> {
                        CircularProgressIndicator(Modifier.align(Alignment.Center))
                    }
                    state.error != null -> {
                        Column(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Text(
                                text = state.error,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            TextButton(onClick = onRetry) { Text("重试") }
                        }
                    }
                    htmlContent != null -> {
                        key(state.article?.id) {
                            AndroidView(
                                modifier = Modifier.fillMaxSize(),
                                factory = { context ->
                                    WebView(context).apply {
                                        importantForAutofill = View.IMPORTANT_FOR_AUTOFILL_NO_EXCLUDE_DESCENDANTS
                                        settings.javaScriptEnabled = false
                                        settings.domStorageEnabled = false
                                        settings.loadsImagesAutomatically = true
                                        settings.mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
                                    }
                                },
                                update = { webView ->
                                    val baseUrl = state.article?.showUrl ?: "https://card.weibo.com/"
                                    webView.loadDataWithBaseURL(
                                        baseUrl,
                                        wrapArticleHtml(htmlContent),
                                        "text/html",
                                        "UTF-8",
                                        null,
                                    )
                                },
                            )
                        }
                    }
                    fallbackUrl != null -> {
                        AndroidView(
                            modifier = Modifier.fillMaxSize(),
                            factory = { context ->
                                WebView(context).apply {
                                    importantForAutofill = View.IMPORTANT_FOR_AUTOFILL_NO_EXCLUDE_DESCENDANTS
                                    settings.javaScriptEnabled = true
                                    settings.domStorageEnabled = true
                                    settings.loadsImagesAutomatically = true
                                }
                            },
                            update = { webView ->
                                if (webView.url != fallbackUrl) {
                                    webView.loadUrl(fallbackUrl)
                                }
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FullscreenForcedOrientationEffect(
    orientation: ForcedVideoOrientation,
    enabled: Boolean,
) {
    val context = LocalContext.current
    val activity = context as? android.app.Activity
    DisposableEffect(enabled, orientation, activity) {
        if (!enabled || activity == null || orientation == ForcedVideoOrientation.None) {
            return@DisposableEffect onDispose {}
        }
        activity.requestedOrientation = when (orientation) {
            ForcedVideoOrientation.Landscape -> ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
            ForcedVideoOrientation.Portrait -> ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
            ForcedVideoOrientation.None -> ActivityInfo.SCREEN_ORIENTATION_USER
        }
        onDispose {
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_USER
        }
    }
}

@Composable
private fun ImmersiveVideoChromeEffect(enabled: Boolean) {
    val context = LocalContext.current
    val activity = context as? android.app.Activity
    val orientation = LocalConfiguration.current.orientation
    DisposableEffect(enabled, activity) {
        if (!enabled || activity == null) return@DisposableEffect onDispose {}
        val window = activity.window
        val previousCutoutMode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode
        } else {
            null
        }
        onDispose {
            val insetsController = WindowCompat.getInsetsController(window, window.decorView)
            insetsController.show(WindowInsetsCompat.Type.systemBars())
            // MainActivity is edge-to-edge for its entire lifetime. Restoring decor fitting here
            // leaves a stale navigation-bar-sized strip after a fullscreen orientation change.
            WindowCompat.setDecorFitsSystemWindows(window, false)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && previousCutoutMode != null) {
                val attributes = window.attributes
                attributes.layoutInDisplayCutoutMode = previousCutoutMode
                window.attributes = attributes
            }
        }
    }

    SideEffect {
        if (!enabled || activity == null) return@SideEffect
        val window = activity.window
        WindowCompat.setDecorFitsSystemWindows(window, false)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val attributes = window.attributes
            attributes.layoutInDisplayCutoutMode =
                android.view.WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            window.attributes = attributes
        }
        WindowCompat.getInsetsController(window, window.decorView).apply {
            systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            hide(WindowInsetsCompat.Type.systemBars())
        }
        // Reading orientation makes this side effect run again after an in-place config change.
        @Suppress("UNUSED_EXPRESSION")
        orientation
    }
}

@Composable
private fun FullscreenMediaPreview(
    media: FeedMedia,
    playbackOwnerId: String,
    onDismiss: () -> Unit,
    onEnterFloatingPlayback: () -> Unit,
) {
    val videoCoordinator = LocalVideoPlaybackCoordinator.current

    ImmersiveVideoChromeEffect(enabled = true)

    DisposableEffect(media, playbackOwnerId) {
        val key = videoPlaybackKey(media, playbackOwnerId)
        videoCoordinator.claimFullscreenPlayback(key)
        onDispose {
            if (videoCoordinator.fullscreenKey == key) {
                videoCoordinator.fullscreenKey = null
            }
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
            playbackOwnerId = playbackOwnerId,
            isFullscreen = true,
            onAspectRatio = { _: Int, _: Int -> },
            onFullscreen = onDismiss,
            onEnterFloatingPlayback = onEnterFloatingPlayback,
            modifier = Modifier.fillMaxSize(),
        )
    }
}

@Composable
private fun AlbumGridRemoteImage(
    image: FeedImage,
    maxDecodeDim: Int,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    previewUrl: String? = null,
) {
    val cacheKey = remember(image.id, image.largeUrl, maxDecodeDim) {
        AlbumThumbnailBitmapCache.cacheKey(image, maxDecodeDim)
    }
    var bitmap by remember(cacheKey) {
        mutableStateOf(AlbumThumbnailBitmapCache.getForImage(image, maxDecodeDim)?.takeIfDrawable())
    }
    var failed by remember(cacheKey) { mutableStateOf(false) }

    SideEffect {
        AlbumThumbnailBitmapCache.getForImage(image, maxDecodeDim)?.takeIfDrawable()?.let { cached ->
            if (bitmap !== cached) {
                bitmap = cached
                failed = false
            }
        }
    }

    LaunchedEffect(cacheKey, previewUrl) {
        AlbumThumbnailBitmapCache.getForImage(image, maxDecodeDim)?.takeIfDrawable()?.let {
            bitmap = it
            failed = false
            return@LaunchedEffect
        }
        if (bitmap?.takeIfDrawable() != null) return@LaunchedEffect
        val loaded = if (previewUrl != null) {
            runCatching {
                withContext(Dispatchers.IO) {
                    FeedImageLoadSemaphore.withPermit {
                        loadRemoteBitmap(
                            url = previewUrl,
                            maxDecodeDim = maxDecodeDim,
                            connectTimeoutMs = 5000,
                            readTimeoutMs = 5000,
                        )
                    }
                }
            }.getOrNull()
        } else {
            loadAlbumGridBitmap(image, maxDecodeDim)
        }
        if (loaded != null) {
            bitmap = loaded
            failed = false
            if (previewUrl == null) {
                AlbumThumbnailBitmapCache.putForImage(image, maxDecodeDim, loaded)
            }
            return@LaunchedEffect
        }
        if (bitmap?.takeIfDrawable() == null) {
            failed = previewUrl != null || albumGridUrlCandidates(image).isNotEmpty()
        }
    }

    val imageBitmap = remember(bitmap) { bitmap?.takeIfDrawable()?.asImageBitmap() }

    Box(
        modifier = modifier.background(MaterialTheme.colorScheme.surfaceContainerHighest),
        contentAlignment = Alignment.Center,
    ) {
        if (imageBitmap != null) {
            Image(
                bitmap = imageBitmap,
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
private fun RemoteImage(
    url: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    animated: Boolean = false,
    fallbackUrls: List<String> = emptyList(),
    cacheLookupUrls: List<String> = emptyList(),
    maxDecodeDim: Int = 480,
    placeholderBackground: Color = MaterialTheme.colorScheme.surfaceContainerHighest,
    limitConcurrency: Boolean = true,
) {
    if (animated) {
        AnimatedRemoteImage(
            url = url,
            modifier = modifier,
            contentScale = contentScale,
            placeholderBackground = placeholderBackground,
            limitConcurrency = limitConcurrency,
        )
        return
    }

    val candidates = remember(url, fallbackUrls) {
        (listOfNotNull(url?.takeIf { it.isNotBlank() }) + fallbackUrls).distinct()
    }
    val cacheCandidates = remember(candidates, cacheLookupUrls) {
        (cacheLookupUrls + candidates).distinct()
    }
    val loadKey = remember(cacheCandidates, maxDecodeDim) {
        cacheCandidates.joinToString("|") + "@$maxDecodeDim"
    }
    val upgradeRevision = LocalFeedImageUpgradeNotifier.current.revision
    var bitmap by remember(loadKey) {
        mutableStateOf(FeedBitmapCache.get(cacheCandidates)?.takeIfDrawable())
    }
    var failed by remember(loadKey) { mutableStateOf(false) }

    SideEffect {
        FeedBitmapCache.get(cacheCandidates)?.takeIfDrawable()?.let { cached ->
            if (bitmap !== cached) {
                bitmap = cached
                failed = false
            }
        }
    }

    LaunchedEffect(loadKey, upgradeRevision) {
        FeedBitmapCache.get(cacheCandidates)?.takeIfDrawable()?.let {
            bitmap = it
            failed = false
            return@LaunchedEffect
        }
        if (bitmap?.takeIfDrawable() != null) return@LaunchedEffect
        for (candidateUrl in candidates) {
            val loaded = runCatching {
                withContext(Dispatchers.IO) {
                    if (limitConcurrency) {
                        FeedImageLoadSemaphore.withPermit {
                            loadRemoteBitmap(
                                url = candidateUrl,
                                maxDecodeDim = maxDecodeDim,
                            )
                        }
                    } else {
                        loadRemoteBitmap(
                            url = candidateUrl,
                            maxDecodeDim = maxDecodeDim,
                        )
                    }
                }
            }.getOrNull()
            if (loaded != null) {
                bitmap = loaded
                failed = false
                FeedBitmapCache.put(candidateUrl, loaded)
                return@LaunchedEffect
            }
        }
        if (bitmap?.takeIfDrawable() == null) {
            failed = candidates.isNotEmpty()
        }
    }

    Box(
        modifier = modifier.background(placeholderBackground),
        contentAlignment = Alignment.Center,
    ) {
        val imageBitmap = remember(bitmap) { bitmap?.takeIfDrawable()?.asImageBitmap() }
        if (imageBitmap != null) {
            Image(
                bitmap = imageBitmap,
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
    placeholderBackground: Color = MaterialTheme.colorScheme.surfaceContainerHighest,
    limitConcurrency: Boolean = true,
) {
    var drawable by remember(url) { mutableStateOf<Drawable?>(null) }
    var failed by remember(url) { mutableStateOf(false) }
    var failureText by remember(url) { mutableStateOf("\u56FE\u7247\u4E0D\u53EF\u7528") }

    LaunchedEffect(url) {
        drawable = null
        failed = false
        failureText = "\u56FE\u7247\u4E0D\u53EF\u7528"
        val target = url?.takeIf { it.isNotBlank() } ?: return@LaunchedEffect
        runCatching {
            withContext(Dispatchers.IO) {
                if (limitConcurrency) {
                    FeedImageLoadSemaphore.withPermit {
                        loadRemoteAnimatedDrawable(target)
                    }
                } else {
                    loadRemoteAnimatedDrawable(target)
                }
            }
        }.onSuccess { drawable = it }
            .onFailure { error ->
                failureText = if (error.message.orEmpty().contains("exceeds")) {
                    "GIF\u8FC7\u5927"
                } else {
                    "GIF\u52A0\u8F7D\u5931\u8D25"
                }
                failed = true
            }
    }

    Box(
        modifier = modifier.background(placeholderBackground),
        contentAlignment = Alignment.Center,
    ) {
        val image = drawable
        if (image != null) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { context ->
                    ImageView(context).apply {
                        setBackgroundColor(android.graphics.Color.BLACK)
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
                text = failureText,
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

private class BitmapByteCache(private val maxBytes: Int) {
    private var currentBytes = 0
    private val entries = object : LinkedHashMap<String, Bitmap>(16, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, Bitmap>?): Boolean {
            if (eldest == null || currentBytes <= maxBytes) return false
            currentBytes = (currentBytes - eldest.value.allocationByteCount).coerceAtLeast(0)
            return true
        }
    }

    @Synchronized
    fun get(key: String): Bitmap? {
        val bitmap = entries[key] ?: return null
        if (bitmap.isRecycled) {
            entries.remove(key)
            return null
        }
        return bitmap
    }

    @Synchronized
    fun get(keys: List<String>): Bitmap? {
        keys.forEach { key ->
            get(key)?.let { return it }
        }
        return null
    }

    @Synchronized
    fun put(key: String, bitmap: Bitmap) {
        entries.remove(key)?.let { old ->
            currentBytes = (currentBytes - old.allocationByteCount).coerceAtLeast(0)
        }
        entries[key] = bitmap
        currentBytes += bitmap.allocationByteCount
        while (currentBytes > maxBytes && entries.isNotEmpty()) {
            val eldest = entries.entries.firstOrNull() ?: break
            entries.remove(eldest.key)
            currentBytes = (currentBytes - eldest.value.allocationByteCount).coerceAtLeast(0)
        }
    }

    @Synchronized
    fun trimToFraction(keepFraction: Float) {
        val target = (maxBytes * keepFraction.coerceIn(0f, 1f)).toInt()
        while (currentBytes > target && entries.isNotEmpty()) {
            val eldest = entries.entries.firstOrNull() ?: break
            entries.remove(eldest.key)
            currentBytes = (currentBytes - eldest.value.allocationByteCount).coerceAtLeast(0)
        }
    }
}

private object AlbumThumbnailBitmapCache {
    private val cache = BitmapByteCache(AlbumBitmapCacheMaxBytes)

    fun cacheKey(image: FeedImage, maxDecodeDim: Int): String {
        val stableId = when {
            image.id.isNotBlank() && !image.id.startsWith("http") -> image.id
            image.largeUrl.isNotBlank() -> image.largeUrl
            else -> image.thumbnailUrl
        }
        return "$stableId@$maxDecodeDim"
    }

    fun getForImage(image: FeedImage, maxDecodeDim: Int): Bitmap? =
        cache.get(cacheKey(image, maxDecodeDim))

    fun putForImage(image: FeedImage, maxDecodeDim: Int, bitmap: Bitmap) {
        cache.put(cacheKey(image, maxDecodeDim), bitmap)
    }

    fun trimToFraction(keepFraction: Float) {
        cache.trimToFraction(keepFraction)
    }
}

private object FeedBitmapCache {
    private val cache = BitmapByteCache(FeedBitmapCacheMaxBytes)

    fun get(url: String): Bitmap? = cache.get(url)

    fun get(urls: List<String>): Bitmap? = cache.get(urls)

    fun getForImage(image: FeedImage): Bitmap? =
        get(feedImageUrlCandidates(image))

    fun put(url: String, bitmap: Bitmap) {
        cache.put(url, bitmap)
    }

    fun putForImage(image: FeedImage, bitmap: Bitmap) {
        feedImageUrlCandidates(image).firstOrNull()?.let { put(it, bitmap) }
    }

    fun trimToFraction(keepFraction: Float) {
        cache.trimToFraction(keepFraction)
    }
}

private object FullscreenBitmapCache {
    private val cache = BitmapByteCache(FullscreenBitmapCacheMaxBytes)

    fun get(url: String): Bitmap? = cache.get(url)

    fun get(urls: List<String>): Bitmap? = cache.get(urls)

    fun put(url: String, bitmap: Bitmap) {
        cache.put(url, bitmap)
    }

    fun putForImage(image: FeedImage, bitmap: Bitmap) {
        fullscreenImageUrlCandidates(image).firstOrNull()?.let { put(it, bitmap) }
    }

    fun trimToFraction(keepFraction: Float) {
        cache.trimToFraction(keepFraction)
    }
}

private object RemoteBytesCache {
    private const val MaxTotalBytes = RemoteBytesCacheMaxTotal
    private const val MaxBytesPerEntry = RemoteBytesMaxCachedEntry
    private var currentBytes = 0
    private val entries = object : LinkedHashMap<String, ByteArray>(16, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, ByteArray>?): Boolean {
            if (eldest == null || currentBytes <= MaxTotalBytes) return false
            currentBytes -= eldest.value.size
            return true
        }
    }

    @Synchronized
    fun get(url: String): ByteArray? = entries[url]

    @Synchronized
    fun put(url: String, bytes: ByteArray) {
        if (bytes.size > MaxBytesPerEntry) return
        entries.remove(url)?.let { removed ->
            currentBytes -= removed.size
        }
        entries[url] = bytes
        currentBytes += bytes.size
        while (currentBytes > MaxTotalBytes && entries.isNotEmpty()) {
            val eldest = entries.entries.firstOrNull() ?: break
            entries.remove(eldest.key)
            currentBytes -= eldest.value.size
        }
    }

    @Synchronized
    fun trimToFraction(keepFraction: Float) {
        val target = (MaxTotalBytes * keepFraction.coerceIn(0f, 1f)).toInt()
        while (currentBytes > target && entries.isNotEmpty()) {
            val eldest = entries.entries.firstOrNull() ?: break
            entries.remove(eldest.key)
            currentBytes -= eldest.value.size
        }
    }
}

private object RemoteDiskBytesCache {
    private var directory: java.io.File? = null

    @Synchronized
    fun configure(cacheDir: java.io.File) {
        directory = cacheDir.also { it.mkdirs() }
        trimLocked()
    }

    @Synchronized
    fun get(url: String, maxBytes: Int): ByteArray? {
        val dir = directory ?: return null
        val file = java.io.File(dir, cacheFileName(url))
        if (!file.isFile || file.length() <= 0L || file.length() > maxBytes) return null
        return runCatching {
            file.setLastModified(System.currentTimeMillis())
            file.readBytes()
        }.getOrNull()
    }

    @Synchronized
    fun put(url: String, bytes: ByteArray) {
        if (bytes.isEmpty() || bytes.size > RemoteBytesAnimatedMaxRead) return
        val dir = directory ?: return
        dir.mkdirs()
        val file = java.io.File(dir, cacheFileName(url))
        val tempFile = java.io.File(dir, "${file.name}.tmp")
        runCatching {
            tempFile.writeBytes(bytes)
            if (file.exists()) file.delete()
            if (!tempFile.renameTo(file)) {
                tempFile.delete()
                return
            }
            file.setLastModified(System.currentTimeMillis())
            trimLocked()
        }
    }

    @Synchronized
    fun trim() {
        trimLocked()
    }

    private fun trimLocked() {
        val dir = directory ?: return
        val files = dir.listFiles()?.filter { it.isFile } ?: return
        var totalBytes = files.sumOf { it.length() }
        if (totalBytes <= RemoteDiskBytesCacheMaxTotal) return
        files.sortedBy { it.lastModified() }.forEach { file ->
            if (totalBytes <= RemoteDiskBytesCacheMaxTotal) return
            val size = file.length()
            if (file.delete()) totalBytes -= size
        }
    }

    private fun cacheFileName(url: String): String {
        val digest = java.security.MessageDigest.getInstance("SHA-256")
            .digest(url.toByteArray(Charsets.UTF_8))
        return digest.joinToString(separator = "") { byte -> "%02x".format(byte) }
    }
}

private fun readRemoteBytesLimited(
    input: java.io.InputStream,
    maxBytes: Int,
): ByteArray {
    val buffer = java.io.ByteArrayOutputStream()
    val chunk = ByteArray(8192)
    var total = 0
    while (true) {
        val read = input.read(chunk)
        if (read <= 0) break
        total += read
        if (total > maxBytes) {
            throw java.io.IOException("Remote response exceeds $maxBytes bytes")
        }
        buffer.write(chunk, 0, read)
    }
    return buffer.toByteArray()
}

private fun fetchRemoteBytes(
    url: String,
    connectTimeoutMs: Int,
    readTimeoutMs: Int,
    maxReadBytes: Int = RemoteBytesMaxCachedEntry,
): ByteArray {
    RemoteBytesCache.get(url)?.takeIf { it.size <= maxReadBytes }?.let { return it }
    RemoteDiskBytesCache.get(url, maxReadBytes)?.let { bytes ->
        RemoteBytesCache.put(url, bytes)
        return bytes
    }
    val request = weiboImageRequest(url)
    val call = WeiboImageHttpClient.newCall(request).apply {
        timeout().timeout((connectTimeoutMs + readTimeoutMs).toLong(), TimeUnit.MILLISECONDS)
    }
    val bytes = call.execute().use { response ->
        if (!response.isSuccessful) {
            response.body?.byteStream()?.use { readRemoteBytesLimited(it, 64 * 1024) }
            throw java.io.IOException("Image request failed with HTTP ${response.code}")
        }
        val body = response.body ?: throw java.io.IOException("Image response has no body")
        body.byteStream().use { readRemoteBytesLimited(it, maxReadBytes) }
    }
    RemoteBytesCache.put(url, bytes)
    RemoteDiskBytesCache.put(url, bytes)
    return bytes
}

private val WeiboImageHttpClient: OkHttpClient by lazy {
    val dispatcher = Dispatcher().apply {
        maxRequests = 12
        maxRequestsPerHost = 4
    }
    OkHttpClient.Builder()
        .dispatcher(dispatcher)
        .connectionPool(ConnectionPool(8, 5, TimeUnit.MINUTES))
        .followRedirects(true)
        .followSslRedirects(true)
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()
}

private fun weiboImageRequest(url: String): Request {
    val builder = Request.Builder()
        .url(url)
        .header("Accept", ImageAcceptHeader)
        .header("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8")
        .header("User-Agent", DESKTOP_CHROME_USER_AGENT)
        .header("Referer", "https://weibo.com/")
    CookieManager.getInstance().getCookie("https://weibo.com/")
        ?.takeIf { it.isNotBlank() }
        ?.let { builder.header("Cookie", it) }
    return builder.build()
}

private fun loadRemoteAnimatedDrawable(url: String): Drawable {
    val bytes = fetchRemoteBytes(
        url = url,
        connectTimeoutMs = 10_000,
        readTimeoutMs = 20_000,
        maxReadBytes = RemoteBytesAnimatedMaxRead,
    )
    val source = ImageDecoder.createSource(ByteBuffer.wrap(bytes))
    return ImageDecoder.decodeDrawable(source)
}

private fun resolveFullscreenPreviewBitmap(image: FeedImage): Bitmap? {
    FullscreenBitmapCache.get(fullscreenImageUrlCandidates(image))
        ?.takeIfDrawable()
        ?.let { return it }
    feedImageUrlCandidates(image).forEach { url ->
        FeedBitmapCache.get(url)?.takeIfDrawable()?.let { return it }
    }
    AlbumThumbnailBitmapCache.getForImage(image, AlbumGridMaxDecodeDim)?.takeIfDrawable()?.let { return it }
    return null
}

private suspend fun loadFullscreenPreviewBitmap(image: FeedImage): Bitmap? {
    resolveFullscreenPreviewBitmap(image)?.let { return it }
    loadAlbumGridBitmap(image)?.let { return it }
    val previewUrls = listOfNotNull(
        image.thumbnailUrl.takeIf { it.isNotBlank() },
        image.largeUrl.takeIf { it.isNotBlank() },
    ).distinct()
    for (url in previewUrls) {
        runCatching {
            loadRemoteBitmap(
                url = url,
                maxDecodeDim = 960,
                connectTimeoutMs = 5_000,
                readTimeoutMs = 8_000,
            )
        }.getOrNull()?.let { bitmap ->
            return bitmap
        }
    }
    return null
}

private fun loadFullscreenBitmap(image: FeedImage): android.graphics.Bitmap? {
    val candidates = fullscreenImageUrlCandidates(image)
    val decodeDim = fullscreenDecodeDim(image)
    candidates.forEach { url ->
        FullscreenBitmapCache.get(url)?.takeIf { it.isFullscreenQuality(image) }?.let { return it }
    }
    candidates.forEach { url ->
        runCatching {
            val bytes = fetchRemoteBytes(
                url = url,
                connectTimeoutMs = 10_000,
                readTimeoutMs = 20_000,
                maxReadBytes = remoteReadLimitForDecodeDim(decodeDim),
            )
            decodeBitmapFromBytes(bytes, decodeDim)
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
    useCache: Boolean = true,
    omitOrigin: Boolean = false,
): androidx.media3.exoplayer.source.MediaSource {
    val mediaItem = androidx.media3.common.MediaItem.fromUri(android.net.Uri.parse(url))
    val factory = weiboDataSourceFactory(context, useCache = useCache, omitOrigin = omitOrigin)
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

private fun weiboDataSourceFactory(
    context: android.content.Context,
    useCache: Boolean = true,
    omitOrigin: Boolean = false,
): androidx.media3.datasource.DataSource.Factory {
    val cookie = CookieManager.getInstance().getCookie("https://weibo.com/").orEmpty()
    val headers = buildMap {
        put("Accept", "*/*")
        put("Referer", "https://weibo.com/")
        if (!omitOrigin) {
            put("Origin", "https://weibo.com")
        }
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
    if (!useCache) return upstream
    return androidx.media3.datasource.cache.CacheDataSource.Factory()
        .setCache(getVideoCache(context))
        .setUpstreamDataSourceFactory(upstream)
        .setFlags(androidx.media3.datasource.cache.CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
}

private fun videoUrlCandidates(url: String): List<String> {
    return MediaUrlResolver.playbackUrlCandidates(url)
}

private fun formatVideoTime(ms: Long): String {
    val totalSeconds = (ms / 1000L).coerceAtLeast(0L)
    val minutes = totalSeconds / 60L
    val seconds = totalSeconds % 60L
    return "$minutes:${seconds.toString().padStart(2, '0')}"
}

private fun speedLabel(speed: Float): String =
    if (speed == speed.toInt().toFloat()) "${speed.toInt()}×" else "${speed}×"

@Composable
private fun videoControlFixedSp(size: Int): TextUnit {
    val fontScale = LocalDensity.current.fontScale
    return (size / fontScale).sp
}

@Composable
private fun videoControlTextStyle(sizeSp: Int): TextStyle = TextStyle(
    fontSize = videoControlFixedSp(sizeSp),
    lineHeight = videoControlFixedSp(sizeSp),
    fontWeight = FontWeight.Bold,
    platformStyle = PlatformTextStyle(includeFontPadding = false),
)

private const val DESKTOP_CHROME_USER_AGENT =
    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 " +
        "(KHTML, like Gecko) Chrome/126.0.0.0 Safari/537.36"
