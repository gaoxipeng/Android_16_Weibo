@file:Suppress("UnsafeOptInUsageError")

package com.example.myweibo.ui

import android.content.ActivityNotFoundException
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
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationState
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
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
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
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.border
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import androidx.compose.ui.draw.clipToBounds
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
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.StrokeCap
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
import com.example.myweibo.data.FriendListTab
import com.example.myweibo.data.HotSearchItem
import com.example.myweibo.data.RelationUser
import com.example.myweibo.data.toRelationUser
import com.example.myweibo.data.toUserProfile
import com.example.myweibo.data.FeedImage
import com.example.myweibo.data.toAlbumFeedMedia
import com.example.myweibo.data.FeedItem
import com.example.myweibo.data.FeedUrlEntity
import com.example.myweibo.data.LikeUsersPage
import com.example.myweibo.data.ProfileLookup
import com.example.myweibo.data.FeedMedia
import com.example.myweibo.data.MediaType
import com.example.myweibo.data.MineCacheStore
import com.example.myweibo.data.FeedThumbnailQuality
import com.example.myweibo.data.ImageSettingsStore
import com.example.myweibo.data.PlaybackSettingsStore
import com.example.myweibo.data.MinePostsCache
import com.example.myweibo.data.NativeUiMessage
import com.example.myweibo.data.StoredWeiboAccount
import com.example.myweibo.data.ThemeSettingsStore
import com.example.myweibo.data.TimelineCacheStore
import com.example.myweibo.data.TimelineKind
import com.example.myweibo.data.UserProfile
import com.example.myweibo.data.WeiboStatusActions
import com.example.myweibo.data.WeiboAccountStore
import com.example.myweibo.data.WeiboJsonParser
import com.example.myweibo.data.WeiboWebSession
import com.example.myweibo.data.WeiboArticle
import com.example.myweibo.data.resolveArticleId
import com.example.myweibo.data.formatWeiboTime
import com.example.myweibo.data.collectEmoticons
import com.example.myweibo.data.collectAllEmoticons
import com.example.myweibo.data.collectAllCommentEmoticons
import com.example.myweibo.data.mergeFeedTimelinePages
import com.example.myweibo.data.sortFeedTimelineItems
import com.example.myweibo.ui.theme.StatusQuotedBackground
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
import kotlinx.coroutines.CancellationException
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
import java.net.HttpURLConnection
import java.net.URL
import java.nio.ByteBuffer
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
    return MediaPeekGraphicsMotion(
        translationX = translationX,
        translationY = translationY,
        scaleX = (visualWidth / layoutWidthPx.coerceAtLeast(1f)) * enterScale,
        scaleY = (visualHeight / layoutHeightPx.coerceAtLeast(1f)) * enterScale,
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
    val longPressed = withTimeoutOrNull(MediaLongPressTimeoutMillis) {
        while (true) {
            val event = awaitPointerEvent()
            val change = event.changes.firstOrNull { it.id == down.id }
                ?: event.changes.firstOrNull()
                ?: return@withTimeoutOrNull false
            val elapsed = change.uptimeMillis - down.uptimeMillis
            val linearProgress = (elapsed.toFloat() / MediaLongPressTimeoutMillis).coerceIn(0f, 1f)
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
            "\u56FE\u7247\u4E0A\u4F20\u6216\u8BC4\u8BBA\u53D1\u9001\u8D85\u65F6\uFF0C\u8BF7\u7A0D\u540E\u91CD\u8BD5"
        message.contains("image-upload-redirected-to-html") ||
            message.contains("weibo-native-post-failed:302") ||
            message.contains("\u56FE\u7247\u4E0A\u4F20\u5931\u8D25:302") ->
            "\u56FE\u7247\u4E0A\u4F20\u88AB\u5FAE\u535A\u91CD\u5B9A\u5411\uFF0C\u8BF7\u91CD\u65B0\u767B\u5F55\u540E\u518D\u8BD5"
        else -> message.ifBlank { "\u8BF7\u7A0D\u540E\u91CD\u8BD5" }
    }
}

private fun lerpFloat(start: Float, stop: Float, fraction: Float): Float =
    start + (stop - start) * fraction

private val HintCapsuleWhite = Color.White
private val HintCapsuleText = Color(0xFF1F1F1F)
private val HintCapsulePlaceholder = Color(0xFFAAAAAA)
private val HintCapsuleBorderColor = Color(0xFFE6E6E6)
private val SettingsBottomBarInset = 96.dp
private val SearchBarBottomGap = 20.dp
private val FeedRefreshIndicatorColor = Color(0xFF9E9E9E)
private val FeedCardContentHorizontalPadding = 12.dp
private val FeedCardSectionSpacing = 10.dp
private val FeedCardItemSpacing = 8.dp
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
private const val AlbumBitmapCacheMaxBytes = 96 * 1024 * 1024
private const val AlbumGridMaxReadBytes = 768 * 1024
private const val AlbumGridPrefetchConcurrency = 8
private const val AlbumGridPrefetchBatchSize = 48
private const val FeedImageLoadConcurrency = 4
private const val FeedBitmapCacheMaxBytes = 32 * 1024 * 1024
private const val FullscreenBitmapCacheMaxBytes = 64 * 1024 * 1024

private data class MorandiThemeColor(
    val storageValue: String,
    val label: String,
    val primary: Color,
    val primaryContainer: Color,
    val secondary: Color,
)

private val MorandiThemeColors = listOf(
    MorandiThemeColor("dusty_rose", "灰粉", Color(0xFFC78395), Color(0xFFF1D9DF), Color(0xFFB58C98)),
    MorandiThemeColor("mist_blue", "雾蓝", Color(0xFF7F9CAF), Color(0xFFD8E4EA), Color(0xFF879BA7)),
    MorandiThemeColor("sage", "鼠尾草", Color(0xFF86A08A), Color(0xFFDCE8DD), Color(0xFF8A9B82)),
    MorandiThemeColor("clay", "陶土", Color(0xFFC08A72), Color(0xFFEEDDD5), Color(0xFFA98B7C)),
    MorandiThemeColor("lavender_gray", "薰衣草灰", Color(0xFF9A8EAD), Color(0xFFE3DDEB), Color(0xFF958CA3)),
    MorandiThemeColor("warm_sand", "暖沙", Color(0xFFA99778), Color(0xFFE8DFCF), Color(0xFF9F927F)),
    MorandiThemeColor("smoky_teal", "烟青", Color(0xFF789E9B), Color(0xFFD7E5E3), Color(0xFF7F9996)),
    MorandiThemeColor("mauve", "豆沙紫", Color(0xFFA77F94), Color(0xFFE8D9E1), Color(0xFF9B8792)),
    MorandiThemeColor("olive_gray", "橄榄灰", Color(0xFF969873), Color(0xFFE4E5D3), Color(0xFF8F9278)),
    MorandiThemeColor("slate", "石板灰", Color(0xFF7F8C98), Color(0xFFD9E0E5), Color(0xFF858D96)),
    MorandiThemeColor("classic_black", "黑色", Color(0xFF202124), Color(0xFFE6E6E6), Color(0xFF4A4A4A)),
    MorandiThemeColor("graphite", "石墨", Color(0xFF4E5965), Color(0xFFDDE2E7), Color(0xFF66717C)),
    MorandiThemeColor("ocean_blue", "海蓝", Color(0xFF4E7FA8), Color(0xFFD6E5F1), Color(0xFF668AA8)),
    MorandiThemeColor("clear_green", "青绿", Color(0xFF4F9278), Color(0xFFD8EADF), Color(0xFF6E9B82)),
    MorandiThemeColor("wine_red", "酒红", Color(0xFF9A4E5B), Color(0xFFEBD7DB), Color(0xFFA56B73)),
    MorandiThemeColor("iris", "鸢尾", Color(0xFF7367B2), Color(0xFFE0DDF2), Color(0xFF8279AF)),
    MorandiThemeColor("amber", "琥珀", Color(0xFFB07A39), Color(0xFFEADDCB), Color(0xFFA4865A)),
    MorandiThemeColor("coral", "珊瑚", Color(0xFFC8665D), Color(0xFFF0D8D5), Color(0xFFB27771)),
)

private fun morandiThemeColorFromStorage(value: String?): MorandiThemeColor =
    MorandiThemeColors.firstOrNull { it.storageValue == value } ?: MorandiThemeColors.first()

private val FeedImageLoadSemaphore = Semaphore(FeedImageLoadConcurrency)

private const val RemoteBytesCacheMaxTotal = 32 * 1024 * 1024
private const val RemoteBytesMaxCachedEntry = 8 * 1024 * 1024
private const val RemoteBytesAnimatedMaxRead = 12 * 1024 * 1024
private const val RemoteDiskBytesCacheMaxTotal = 192 * 1024 * 1024
private const val NavTransitionDurationMs = 280
// ComponentCallbacks2.TRIM_MEMORY_* 在较新 SDK 中已标记 deprecated，数值仍稳定可用。
private const val TrimMemoryRunningLow = 10
private const val TrimMemoryRunningCritical = 15
private const val TrimMemoryComplete = 80

private fun navStackEnterTransition() =
    slideInHorizontally(
        animationSpec = tween(NavTransitionDurationMs, easing = FastOutSlowInEasing),
        initialOffsetX = { fullWidth -> fullWidth },
    ) + fadeIn(tween(NavTransitionDurationMs))

private fun navStackExitTransition() =
    slideOutHorizontally(
        animationSpec = tween(NavTransitionDurationMs, easing = FastOutSlowInEasing),
        targetOffsetX = { fullWidth -> fullWidth },
    ) + fadeOut(tween(NavTransitionDurationMs))

@Composable
private fun <T> NavAnimatedOverlay(
    target: T?,
    modifier: Modifier = Modifier,
    enter: EnterTransition = navStackEnterTransition(),
    exit: ExitTransition = navStackExitTransition(),
    exitHoldMillis: Long = NavTransitionDurationMs.toLong(),
    content: @Composable (T) -> Unit,
) {
    var displayed by remember { mutableStateOf<T?>(null) }
    if (target != null) {
        displayed = target
    }
    val visible = target != null
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = enter,
        exit = exit,
        label = "nav-stack-overlay",
    ) {
        displayed?.let { item -> content(item) }
    }
    if (!visible && displayed != null) {
        LaunchedEffect(displayed) {
            if (exitHoldMillis > 0L) {
                delay(exitHoldMillis)
            }
            displayed = null
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
    var proactiveStashSignalKey by mutableStateOf<String?>(null)
    private val handoffPlayers = mutableMapOf<String, androidx.media3.exoplayer.ExoPlayer>()
    private val transitionFrames = mutableMapOf<String, Bitmap>()
    private val inlinePauseHandlers = mutableSetOf<() -> Unit>()
    private val fullscreenPauseHandlers = mutableSetOf<() -> Unit>()
    private val peekPauseHandlers = mutableSetOf<() -> Unit>()
    private val peekRestartFromBeginningKeys = mutableSetOf<String>()

    fun schedulePeekRestartIfAtEnd(playbackKey: String, durationMs: Long?) {
        val saved = positions[playbackKey] ?: return
        val duration = durationMs?.takeIf { it > VideoEndRestartThresholdMs } ?: return
        if (saved >= duration - VideoEndRestartThresholdMs) {
            positions[playbackKey] = 0L
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

    fun hasStashedHandoff(playbackKey: String): Boolean =
        handoffPlayers.containsKey(playbackKey)

    fun cancelFullscreenHandoff(playbackKey: String) {
        if (pendingFullscreenHandoffKey == playbackKey) {
            pendingFullscreenHandoffKey = null
        }
        handoffPlayers.remove(playbackKey)?.release()
    }

    fun beginPeekHandoff(playbackKey: String) {
        pendingPeekHandoffKey = playbackKey
    }

    fun cancelPeekHandoff(playbackKey: String) {
        if (pendingPeekHandoffKey == playbackKey) {
            pendingPeekHandoffKey = null
        }
        handoffPlayers.remove(playbackKey)?.release()
    }

    fun stashHandoffPlayer(playbackKey: String, player: androidx.media3.exoplayer.ExoPlayer) {
        val currentPosition = player.currentPosition.coerceAtLeast(0L)
        if (currentPosition > 0L || positions[playbackKey] == null) {
            positions[playbackKey] = currentPosition
        }
        player.playWhenReady = false
        player.pause()
        player.clearVideoSurface()
        handoffPlayers[playbackKey] = player
    }

    fun consumeHandoffPlayer(playbackKey: String): androidx.media3.exoplayer.ExoPlayer? {
        val player = handoffPlayers.remove(playbackKey) ?: return null
        if (pendingFullscreenHandoffKey == playbackKey) {
            pendingFullscreenHandoffKey = null
        }
        if (pendingPeekHandoffKey == playbackKey) {
            pendingPeekHandoffKey = null
        }
        return player
    }

    fun registerPauseHandler(isFullscreen: Boolean, handler: () -> Unit) {
        if (isFullscreen) {
            fullscreenPauseHandlers += handler
        } else {
            inlinePauseHandlers += handler
        }
    }

    fun unregisterPauseHandler(isFullscreen: Boolean, handler: () -> Unit) {
        if (isFullscreen) {
            fullscreenPauseHandlers -= handler
        } else {
            inlinePauseHandlers -= handler
        }
    }

    fun registerPeekPauseHandler(handler: () -> Unit) {
        peekPauseHandlers += handler
    }

    fun unregisterPeekPauseHandler(handler: () -> Unit) {
        peekPauseHandlers -= handler
    }

    fun pausePeek() {
        peekPauseHandlers.forEach { it() }
    }

    fun pauseAll() {
        inlinePauseHandlers.forEach { it() }
        fullscreenPauseHandlers.forEach { it() }
        peekPauseHandlers.forEach { it() }
    }

    fun pauseInlineOnly() {
        inlinePauseHandlers.forEach { it() }
    }

    fun requestInlinePlayback(playbackKey: String) {
        pausePeek()
        pauseInlineOnly()
        activeKey = playbackKey
    }

    fun claimPeekPlayback(playbackKey: String) {
        pauseInlineOnly()
        fullscreenPauseHandlers.forEach { it() }
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

private data class MediaPreviewRequest(
    val media: FeedMedia,
    val playbackOwnerId: String,
)

private fun FeedImage.albumStatusCacheKey(): String =
    statusId?.takeIf { it.isNotBlank() } ?: largeUrl

private val LocalVideoPlaybackCoordinator = staticCompositionLocalOf { VideoPlaybackCoordinator() }
private val LocalUiMessenger = staticCompositionLocalOf<(String, String) -> Unit> { { _, _ -> } }
private val LocalTopicClickHandler = staticCompositionLocalOf<((String) -> Unit)?> { null }

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
        isFloating = false
        isFullscreenMode = false
        fingerDragOffset = Offset.Zero
        activeRequest = request
    }

    fun openFloating(request: VideoPeekRequest) {
        pendingDismiss = null
        isFloating = true
        isFullscreenMode = false
        fingerDragOffset = Offset.Zero
        activeRequest = request
    }

    fun cancel() {
        if (activeRequest != null && pendingDismiss == null) {
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

    fun open(item: FeedItem, anchorBoundsInRoot: Rect, backHandlerEnabled: Boolean) {
        activeRequest = FeedCardActionMenuRequest(item, anchorBoundsInRoot, backHandlerEnabled)
    }

    fun dismiss() {
        activeRequest = null
    }
}

private val LocalFeedCardActionMenuController = staticCompositionLocalOf { FeedCardActionMenuController() }

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
    var bottomPadding by mutableStateOf(96.dp)
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

private class FeedImageUpgradeNotifier {
    var revision by mutableIntStateOf(0)
        private set

    fun notifyCacheUpdated() {
        revision++
    }
}

private fun feedImageUrlCandidates(image: FeedImage): List<String> =
    (listOf(image.thumbnailUrl, downgradeSinaimgForFeed(image.largeUrl, "mw690")) +
        image.downloadUrls.map { downgradeSinaimgForFeed(it, "mw690") })
        .filter { it.isNotBlank() }
        .distinct()

private fun feedImageCacheCandidates(
    image: FeedImage,
    quality: FeedThumbnailQuality,
): List<String> =
    listOfNotNull(
        quality.displayUrl(image).takeIf { it.isNotBlank() },
        quality.fallbackUrl(image),
    ).distinct()

private fun downgradeSinaimgForFeed(url: String, variant: String): String {
    if (url.isBlank()) return url
    if (!url.contains("sinaimg.cn", ignoreCase = true)) return url
    return url.replace(
        Regex("""/(?:large|mw2000|woriginal|original|bmiddle|orj360|orj480|mw690|mw1024|thumbnail|thumb(?:180|300|150)?|small|wap360)/""", RegexOption.IGNORE_CASE),
        "/$variant/",
    )
}

private val LowQualityImageUrlPattern =
    Regex("""/(?:orj360|orj480|bmiddle|thumbnail|thumb(?:180|300|150)?|small|wap360)/""", RegexOption.IGNORE_CASE)

private fun fullscreenImageUrlCandidates(image: FeedImage): List<String> {
    val highQuality = (image.downloadUrls + listOf(image.largeUrl))
        .filter { it.isNotBlank() }
        .distinct()
        .filterNot { LowQualityImageUrlPattern.containsMatchIn(it) }
    if (highQuality.isNotEmpty()) return highQuality
    return listOfNotNull(
        image.largeUrl.takeIf { it.isNotBlank() },
        image.thumbnailUrl.takeIf { it.isNotBlank() },
    ).distinct()
}

private fun Bitmap.isFullscreenQuality(image: FeedImage): Boolean {
    val maxDim = maxOf(width, height)
    if (maxDim >= 960) return true
    val expectedWidth = image.width ?: 0
    val expectedHeight = image.height ?: 0
    if (expectedWidth > 0 && expectedHeight > 0) {
        val expectedMax = maxOf(expectedWidth, expectedHeight)
        if (maxDim >= (expectedMax * 0.85f).roundToInt()) return true
    }
    return maxDim >= 640
}

private fun downgradeSinaimgForAlbumGrid(url: String): String {
    if (!url.contains("sinaimg.cn", ignoreCase = true)) return url
    return url.replace(
        Regex("""/(?:large|mw2000|woriginal|original|bmiddle)/""", RegexOption.IGNORE_CASE),
        "/orj360/",
    )
}

private fun albumGridUrlCandidates(image: FeedImage): List<String> = buildList {
    image.thumbnailUrl.takeIf { it.isNotBlank() }
        ?.let(::downgradeSinaimgForAlbumGrid)
        ?.let(::add)
    image.largeUrl.takeIf { it.isNotBlank() }
        ?.let(::downgradeSinaimgForAlbumGrid)
        ?.let(::add)
    image.downloadUrls.forEach { url ->
        if (url.isNotBlank()) {
            add(downgradeSinaimgForAlbumGrid(url))
        }
    }
}.distinct()

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
    else -> 12 * 1024 * 1024
}

private fun decodeBitmapFromBytes(bytes: ByteArray, maxDecodeDim: Int): Bitmap? {
    if (bytes.isEmpty()) return null
    decodeSampledBitmap(bytes, maxDecodeDim)?.let { return it }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        return runCatching {
            val source = ImageDecoder.createSource(bytes)
            ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
                decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                val width = info.size.width
                val height = info.size.height
                if (width > 0 && height > 0) {
                    val dim = maxDecodeDim.coerceAtLeast(1)
                    val scale = maxOf(1, (width / dim).coerceAtMost(height / dim))
                    val sample = Integer.highestOneBit(scale)
                    decoder.setTargetSize(
                        (width / sample).coerceAtLeast(1),
                        (height / sample).coerceAtLeast(1),
                    )
                }
            }
        }.getOrNull()
    }
    return null
}

private fun decodeSampledBitmap(bytes: ByteArray, maxDecodeDim: Int): Bitmap? {
    val options = BitmapFactory.Options().apply {
        inJustDecodeBounds = true
    }
    BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
    if (options.outWidth <= 0 || options.outHeight <= 0) return null
    val dim = maxDecodeDim.coerceAtLeast(1)
    val scale = maxOf(1, (options.outWidth / dim).coerceAtMost(options.outHeight / dim))
    options.inJustDecodeBounds = false
    options.inSampleSize = Integer.highestOneBit(scale)
    return BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
}

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

private fun videoMediaKey(media: FeedMedia): String =
    media.resolvedPlaybackUrl()
        ?: media.streamUrl.ifBlank { media.replayUrl.orEmpty() }
        .ifBlank { media.downloadUrl.orEmpty() }
        .ifBlank { media.coverUrl.orEmpty() }

private fun videoPlaybackKey(media: FeedMedia, ownerId: String): String =
    "$ownerId|${videoMediaKey(media)}"

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

@OptIn(ExperimentalMaterial3Api::class)
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
    LaunchedEffect(context) {
        withContext(Dispatchers.IO) {
            RemoteDiskBytesCache.configure(java.io.File(context.cacheDir, "remote-image-bytes"))
        }
    }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val feedListState = rememberLazyListState()
    val minePostsListState = rememberLazyListState()
    val mineAlbumListState = rememberLazyListState()
    val visitedPostsListState = rememberLazyListState()
    val visitedAlbumListState = rememberLazyListState()
    val followListFollowingListState = rememberLazyListState()
    val followListFansListState = rememberLazyListState()
    val searchListState = rememberLazyListState()
    val videoPlaybackCoordinator = remember { VideoPlaybackCoordinator() }
    val videoPeekController = remember { VideoPeekController() }
    val profileHeaderHeights = remember { mutableStateMapOf<String, Dp>() }

    var selectedTab by remember { mutableStateOf(MainTab.Feed) }
    var bottomBarExpanded by remember { mutableStateOf(true) }
    var bottomBarAwaitingOutsideDismiss by remember { mutableStateOf(false) }
    var minePagerPage by remember { mutableStateOf(0) }
    var visitedMinePagerPage by remember { mutableStateOf(0) }
    var feedRefreshHint by remember { mutableStateOf<String?>(null) }
    var operationCapsuleHint by remember { mutableStateOf<String?>(null) }
    var albumFetchCapsuleHint by remember { mutableStateOf<String?>(null) }
    var timelineKind by remember { mutableStateOf(TimelineKind.Following) }
    var items by remember { mutableStateOf<List<FeedItem>>(emptyList()) }
    var nextCursor by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var feedLoadingMore by remember { mutableStateOf(false) }
    var timelineRefreshJob by remember { mutableStateOf<Job?>(null) }
    var timelineLoadMoreJob by remember { mutableStateOf<Job?>(null) }
    var timelineRequestGeneration by remember { mutableIntStateOf(0) }
    var selectedItem by remember { mutableStateOf<FeedItem?>(null) }
    var navStack by remember { mutableStateOf<List<NavRestoreState>>(emptyList()) }
    var lastDetailScroll by remember { mutableStateOf(ScrollRestore()) }
    var detailScrollPending by remember { mutableStateOf<Pair<String, ScrollRestore>?>(null) }
    var detailScrollRestoreToken by remember { mutableIntStateOf(0) }
    var activeDetailInstanceKey by remember { mutableIntStateOf(0) }
    var albumViewerState by remember { mutableStateOf<AlbumViewerState?>(null) }
    var comments by remember { mutableStateOf<List<CommentItem>>(emptyList()) }
    var commentsLoading by remember { mutableStateOf(false) }
    var commentsLoadingMore by remember { mutableStateOf(false) }
    var commentsRequestJob by remember { mutableStateOf<Job?>(null) }
    var commentsCursor by remember { mutableStateOf<String?>(null) }
    var commentsHasMore by remember { mutableStateOf(true) }
    var commentComposeTarget by remember { mutableStateOf<CommentComposeTarget?>(null) }
    var commentSubmitting by remember { mutableStateOf(false) }
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
    var selectedThemeColor by remember {
        mutableStateOf(morandiThemeColorFromStorage(themeSettingsStore.readThemeColor()))
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

    fun showAlbumFetchTrace(@Suppress("UNUSED_PARAMETER") page: AlbumPage) = Unit

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
                        showMessage("表情同步完成", "已同步 ${synced.size} 个表情")
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
        visitedProfileLoadGeneration += 1
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
                    showAlbumFetchTrace(page)
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
                    showAlbumFetchTrace(page)
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

    fun applyNavRestoreState(state: NavRestoreState) {
        selectedTab = state.selectedTab
        minePagerPage = state.minePagerPage
        selectedItem = null
        comments = emptyList()
        commentsCursor = null
        commentsHasMore = true
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

        followListOverlay = state.followListOverlay
        if (state.followListOverlay != null) {
            scrollLazyListState(followListFollowingListState, state.followListFollowingScroll)
            scrollLazyListState(followListFansListState, state.followListFansScroll)
        }

        if (state.detail != null) {
            restoreDetailSnapshot(state.detail)
        } else if (state.albumViewerState != null) {
            albumViewerState = state.albumViewerState
            restoreProfilePagerFromViewer(state.albumViewerState)
            restoreAlbumScrollFromViewer(state.albumViewerState)
        }

        articleOverlay = state.articleOverlay
        mediaPreview = state.mediaPreview
    }

    fun pushNavigation(navigate: () -> Unit) {
        navStack = navStack + captureNavRestoreState()
        navigate()
    }

    fun popNavigation(): Boolean {
        if (navStack.isEmpty()) return false
        val previous = navStack.last()
        navStack = navStack.dropLast(1)
        applyNavRestoreState(previous)
        return true
    }

    fun navigateBack() {
        popNavigation()
    }

    fun openUrlEntity(entity: FeedUrlEntity) {
        pushNavigation { loadArticleIntoOverlay(entity) }
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
        if (navStack.isNotEmpty()) {
            navStack = navStack.dropLast(1)
        }
    }

    fun openFollowList(
        uid: String,
        screenName: String,
        avatarUrl: String?,
        description: String?,
        tab: FriendListTab,
    ) {
        pushNavigation {
            val instanceKey = navStack.size
            followListOverlay = FollowListOverlayState(
                uid = uid,
                screenName = screenName,
                avatarUrl = avatarUrl,
                description = description,
                tab = tab,
                instanceKey = instanceKey,
            )
        }
    }

    fun pushMediaPreview(media: FeedMedia, playbackOwnerId: String) {
        val nextKey = videoPlaybackKey(media, playbackOwnerId)
        if (mediaPreview?.let { videoPlaybackKey(it.media, it.playbackOwnerId) == nextKey } == true) {
            return
        }
        videoPlaybackCoordinator.claimFullscreenPlayback(nextKey)
        pushNavigation { mediaPreview = MediaPreviewRequest(media, playbackOwnerId) }
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
        albumViewerState = viewer
    }

    fun dismissAlbumViewer() {
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
        pushNavigation {
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

        pushNavigation {
            selectedItem = null
            comments = emptyList()
            commentsCursor = null
            commentsHasMore = true
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
        pushNavigation {
            selectedItem = null
            comments = emptyList()
            commentsCursor = null
            commentsHasMore = true
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
        navigateBack()
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
            selectedTab == MainTab.Messages || selectedTab == MainTab.Compose -> Unit
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
            feedListState.animateScrollToTopFixed()
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
                feedListState.animateScrollToTopFixed()
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
                showAlbumFetchTrace(page)
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
                    showAlbumFetchTrace(page)
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
        return base.copy(
            retweetedStatus = base.retweetedStatus?.let { resolveFeedItem(it) },
        )
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
                runCatching {
                    withTimeout(45_000) {
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
                }.onSuccess {
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
                }.onFailure { error ->
                    if (error is CancellationException && error !is TimeoutCancellationException) throw error
                    operationCapsuleHint = commentFailureMessage(error)
                }
            } finally {
                commentSubmitting = false
                commentSubmitJob = null
            }
        }
        commentSubmitJob = job
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
        activeDetailInstanceKey = navStack.size
        val scroll = if (scrollToContentSection) {
            ScrollRestore(index = DETAIL_SECTION_HEADER_INDEX)
        } else {
            ScrollRestore()
        }
        lastDetailScroll = scroll
        detailScrollPending = resolved.id to scroll
        selectedItem = resolved
        resetDetailContentState(initialSection)
        if (resolved.isLongText) {
            loadLongText(resolved)
        }
        resolved.retweetedStatus?.takeIf { it.isLongText }?.let { loadLongText(it) }
        when (initialSection) {
            DetailContentSection.Comments -> reloadComments()
            DetailContentSection.Reposts -> reloadReposts()
        }
    }

    fun openDetailInternal(item: FeedItem) {
        pushNavigation {
            openDetailPrepared(
                item = item,
                initialSection = DetailContentSection.Comments,
                scrollToContentSection = false,
            )
        }
    }

    fun openDetailToSection(item: FeedItem, section: DetailContentSection) {
        pushNavigation {
            openDetailPrepared(
                item = item,
                initialSection = section,
                scrollToContentSection = true,
            )
        }
    }

    fun openDetail(item: FeedItem) {
        openDetailInternal(item)
    }

    fun openDetailFromSource(item: FeedItem, sourceBounds: Rect?) {
        openDetailInternal(item)
    }

    fun openDetailFromAlbumViewer(item: FeedItem, viewer: AlbumViewerState) {
        pushNavigation {
            restoreProfilePagerFromViewer(viewer)
            albumViewerState = viewer
            val resolved = resolveFeedItem(item)
            activeDetailInstanceKey = navStack.size
            lastDetailScroll = ScrollRestore()
            detailScrollPending = resolved.id to ScrollRestore()
            selectedItem = resolved
            resetDetailContentState()
            if (resolved.isLongText) {
                loadLongText(resolved)
            }
            resolved.retweetedStatus?.takeIf { it.isLongText }?.let { loadLongText(it) }
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
                                page.items
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
        if (items.isEmpty() && hasLoginCookie) {
            refreshTimeline()
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
        visitedUserId != null -> null
        selectedTab == MainTab.Mine -> {
            if (minePagerPage == 0) minePostsListState else mineAlbumListState
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
        val state = bottomBarListState ?: return@LaunchedEffect
        var lastScrollTotal =
            state.firstVisibleItemIndex * 10_000 + state.firstVisibleItemScrollOffset
        snapshotFlow {
            state.firstVisibleItemIndex * 10_000 + state.firstVisibleItemScrollOffset
        }.collect { scrollTotal ->
            if (scrollTotal > lastScrollTotal + 12) {
                bottomBarExpanded = false
                bottomBarAwaitingOutsideDismiss = false
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

    LaunchedEffect(
        selectedTab,
        visitedUserId,
        selectedItem,
        mediaPreview,
        articleOverlay,
        followListOverlay,
        albumViewerState,
        videoPeekController.activeRequest,
        videoPeekController.isFloating,
        videoPeekController.isFullscreenMode,
        videoPeekController.pendingDismiss,
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
        if (!homeFeedOnTop) {
            if (mediaPreview != null || keepFloatingPeekPlayback) {
                videoPlaybackCoordinator.pauseInlineOnly()
            } else {
                videoPlaybackCoordinator.pauseAll()
            }
            videoPlaybackCoordinator.activeKey = null
        }
    }

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
    MyWeiboScaffold(
        snackbarHostState = snackbarHostState,
    ) { innerPadding ->
        val hazeState = rememberHazeState()
        val bottomBarBackdrop = rememberLayerBackdrop()
        val feedCardActionMenuController = remember { FeedCardActionMenuController() }
        val searchBarOverlay = remember { SearchBarOverlayController() }
        var timelineMenuExpanded by remember { mutableStateOf(false) }
        val imagePeekController = remember { ImagePeekController() }
        val feedImageUpgradeNotifier = remember { FeedImageUpgradeNotifier() }
        CompositionLocalProvider(
            LocalHazeState provides hazeState,
            LocalLiquidMenuBackdrop provides bottomBarBackdrop,
            LocalFeedCardActionMenuController provides feedCardActionMenuController,
            LocalImagePeekController provides imagePeekController,
            LocalVideoPeekController provides videoPeekController,
            LocalFeedThumbnailQuality provides feedThumbnailQuality,
            LocalFeedImageUpgradeNotifier provides feedImageUpgradeNotifier,
        ) {
            Box(Modifier.fillMaxSize()) {
            Box(Modifier.matchParentSize().hazeSource(state = hazeState)) {
            Box(Modifier.fillMaxSize().padding(innerPadding)) {
            val mainContentClear = visitedUserId == null && selectedItem == null
            val messagesWebVisible = selectedTab == MainTab.Messages && mainContentClear
            val composeWebVisible = selectedTab == MainTab.Compose && mainContentClear
            val webTabBackdropExcluded = messagesWebVisible || composeWebVisible
            var messagesWebMounted by remember { mutableStateOf(false) }
            var composeWebMounted by remember { mutableStateOf(false) }
            if (messagesWebVisible) messagesWebMounted = true
            if (composeWebVisible) composeWebMounted = true

            if (messagesWebMounted) {
                Box(
                    Modifier
                        .then(if (messagesWebVisible) Modifier.fillMaxSize() else Modifier.size(0.dp))
                        .graphicsLayer {
                            alpha = if (messagesWebVisible) 1f else 0f
                            clip = true
                        }
                        .zIndex(if (messagesWebVisible) 2f else -10f)
                        .blockHiddenTouches(messagesWebVisible),
                ) {
                    MessagesScreen(
                        onRootBack = ::handleRootBackPress,
                        active = messagesWebVisible,
                    )
                }
            }
            if (composeWebMounted) {
                Box(
                    Modifier
                        .then(if (composeWebVisible) Modifier.fillMaxSize() else Modifier.size(0.dp))
                        .graphicsLayer {
                            alpha = if (composeWebVisible) 1f else 0f
                            clip = true
                        }
                        .zIndex(if (composeWebVisible) 2f else -10f)
                        .blockHiddenTouches(composeWebVisible),
                ) {
                    MobileWeiboWebScreen(
                        pageUrl = "https://m.weibo.cn/compose/",
                        onRootBack = ::handleRootBackPress,
                        active = composeWebVisible,
                    )
                }
            }

            Box(
                Modifier
                    .fillMaxSize()
                    .zIndex(1f)
                    .then(
                        if (!webTabBackdropExcluded) {
                            Modifier.layerBackdrop(bottomBarBackdrop)
                        } else {
                            Modifier
                        },
                    ),
            ) {
            val detailOverlayItem = selectedItem?.let(::resolveFeedItem)
            val feedUiOnTop = selectedTab == MainTab.Feed &&
                visitedUserId == null &&
                detailOverlayItem == null
            val keepFeedAlive = selectedTab == MainTab.Feed &&
                visitedUserId != null &&
                detailOverlayItem == null
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
            val visitedProfileVisible = visitedUserId != null &&
                selectedItem == null &&
                albumViewerState == null
            val followListBelongsToCurrentContext = followListOverlay?.let { overlay ->
                when {
                    visitedUserId != null -> visitedUserId == overlay.uid
                    else -> selectedTab == MainTab.Mine && overlay.uid == mineProfile?.id
                }
            } ?: false
            val followListUiOnTop = followListOverlay != null &&
                selectedItem == null &&
                articleOverlay == null &&
                mediaPreview == null &&
                albumViewerState == null &&
                followListBelongsToCurrentContext
            val feedVisibleAlpha = if (feedUiOnTop) 1f else 0f

            Box(Modifier.fillMaxSize()) {
                if (selectedTab == MainTab.Feed && (feedUiOnTop || keepFeedAlive)) {
                    Box(
                        Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background)
                            .graphicsLayer { alpha = feedVisibleAlpha }
                            .blockHiddenTouches(feedUiOnTop),
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
                    target = followListOverlay,
                    modifier = Modifier
                        .fillMaxSize()
                        .zIndex(if (followListUiOnTop) 550f else 400f),
                ) { overlay ->
                    Box(
                        Modifier
                            .fillMaxSize()
                            .graphicsLayer { alpha = if (followListUiOnTop) 1f else 0f }
                            .blockHiddenTouches(followListUiOnTop),
                    ) {
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
                }

                if (selectedTab == MainTab.Search && (searchUiOnTop || keepSearchAlive)) {
                    Box(
                        Modifier
                            .fillMaxSize()
                            .graphicsLayer { alpha = if (searchUiOnTop) 1f else 0f }
                            .blockHiddenTouches(searchUiOnTop),
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
                }

                val visitedProfileNavTarget = visitedUserId
                if (visitedProfileNavTarget != null) {
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
                    modifier = Modifier.fillMaxSize().zIndex(540f),
                ) { uid ->
                    key(uid, visitedProfileLoadGeneration) {
                        Box(
                            Modifier
                                .fillMaxSize()
                                .graphicsLayer {
                                    alpha = if (visitedProfileVisible) 1f else 0f
                                }
                                .blockHiddenTouches(visitedProfileVisible),
                        ) {
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

                if (visitedUserId == null && selectedItem == null) {
                    when (selectedTab) {
                        MainTab.Search -> Unit
                        MainTab.Messages, MainTab.Compose -> Unit

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
                                onAlbumTabSelected = { loadMineAlbumFirstPage() },
                                onRefresh = { refreshMineProfile() },
                                onLoadMorePosts = { loadMoreMinePosts() },
                                onLoadMoreAlbum = { loadMoreMineAlbum() },
                                onSyncEmoticons = { syncEmoticons() },
                                onItemClick = ::openDetail,
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
                                feedThumbnailQuality = feedThumbnailQuality,
                                onFeedThumbnailQualityChange = { quality ->
                                    feedThumbnailQuality = quality
                                    imageSettingsStore.writeThumbnailQuality(quality)
                                },
                                selectedThemeColor = selectedThemeColor,
                                onThemeColorChange = { color ->
                                    selectedThemeColor = color
                                    themeSettingsStore.writeThemeColor(color.storageValue)
                                },
                            )
                        }

                        MainTab.Feed -> Unit
                    }
                }

                NavAnimatedOverlay(
                    target = detailOverlayItem,
                    modifier = Modifier
                        .fillMaxSize()
                        .zIndex(570f),
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
                        BackHandler(onBack = { navigateBack() })
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            color = MaterialTheme.colorScheme.background,
                        ) {
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
                                onBack = { navigateBack() },
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
                                onRetweetClick = ::openDetail,
                                onUserClick = ::openUser,
                                onComposeComment = ::openCommentComposer,
                                onExpandNestedComments = ::loadNestedCommentsPage,
                                nestedCommentsLoadingIds = nestedCommentsLoadingIds,
                                onUrlEntityClick = ::openUrlEntity,
                            )
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

            if (
                selectedTab != MainTab.Mine &&
                selectedTab != MainTab.Messages &&
                selectedTab != MainTab.Compose &&
                selectedItem == null
            ) {
                HiddenSessionWebView(session)
            }
            }

            if (searchBarOverlay.active) {
                val suggestionReserve = if (searchBarOverlay.suggestionsVisible) 176.dp else 0.dp
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .height(searchBarOverlay.bottomPadding + 52.dp + suggestionReserve)
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
                            .padding(bottom = 8.dp),
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

            val capsuleHint = operationCapsuleHint
                ?: albumFetchCapsuleHint
                ?: if (
                    selectedTab == MainTab.Feed &&
                    selectedItem == null &&
                    visitedUserId == null &&
                    feedRefreshHint != null
                ) {
                    feedRefreshHint
                } else {
                    null
                }
            val feedRefreshTopInset = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
            AnimatedVisibility(
                visible = capsuleHint != null,
                enter = fadeIn(tween(220)) + slideInVertically(tween(220)) { fullHeight -> -fullHeight / 2 },
                exit = fadeOut(tween(180)) + slideOutVertically(tween(180)) { fullHeight -> -fullHeight / 2 },
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = feedRefreshTopInset + 10.dp)
                    .zIndex(90f),
            ) {
                capsuleHint?.let { hint ->
                    FeedRefreshCapsuleHint(
                        message = hint,
                        autoDismissMillis = if (albumFetchCapsuleHint != null) 3200L else 2200L,
                        onDismiss = {
                            when {
                                operationCapsuleHint != null -> operationCapsuleHint = null
                                albumFetchCapsuleHint != null -> albumFetchCapsuleHint = null
                                else -> feedRefreshHint = null
                            }
                        },
                    )
                }
            }

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
                        ImageActionFrostedCard(modifier = Modifier.width(136.dp)) {
                            listOf(TimelineKind.Following, TimelineKind.FriendsCircle).forEachIndexed { index, kind ->
                                if (index > 0) {
                                    ImageActionMenuDivider()
                                }
                                ImageActionRow(
                                    label = kind.label,
                                    enabled = true,
                                    selected = kind == timelineKind,
                                    onClick = {
                                        dismiss()
                                        dismissFollowListForTabSwitch()
                                        selectedTab = MainTab.Feed
                                        switchTimelineKind(kind)
                                    },
                                )
                            }
                        }
                    },
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
                    isFloating = imagePeekController.isFloating,
                    dismissReason = imagePeekController.pendingDismiss,
                    onRequestCancel = { imagePeekController.cancel() },
                    onDismissComplete = { imagePeekController.completeDismiss() },
                    onOpenFullscreenBehind = { request.onOpenFullscreenBehind(it) },
                    onEnterFullscreenHandoffComplete = { imagePeekController.completeEnterFullscreenHandoff() },
                )
            }
            }
            if (mediaPreview != null) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .zIndex(498f)
                        .background(Color.Black),
                )
            }
            NavAnimatedOverlay(
                target = mediaPreview,
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(500f)
                    .background(Color.Black),
                enter = EnterTransition.None,
                exit = ExitTransition.None,
                exitHoldMillis = 0L,
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
                            videoPeekController.isFullscreenMode -> 600f
                            videoPeekController.isFloating -> 575f
                            else -> 565f
                        },
                    ),
                    media = request.media,
                    playbackOwnerId = request.playbackOwnerId,
                    anchorBounds = request.anchorBounds,
                    pressOffset = request.pressOffset,
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
            NavAnimatedOverlay(
                target = articleOverlay,
                modifier = Modifier.fillMaxSize().zIndex(600f),
            ) { overlay ->
                ArticleReaderOverlay(
                    state = overlay,
                    onBack = ::closeArticleOverlay,
                    onRetry = { loadArticleIntoOverlay(overlay.entity) },
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
    content: @Composable () -> Unit,
) {
    SurfaceLiquidCapsule(modifier = modifier, cornerRadius = 22.dp) {
        content()
    }
}

@Composable
private fun FeedRefreshCapsuleHint(
    message: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    autoDismissMillis: Long = 2200L,
) {
    LaunchedEffect(message) {
        delay(autoDismissMillis)
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
                color = HintCapsuleText,
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
                containerColor = HintCapsuleWhite,
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
    onMediaClick: (FeedMedia, String) -> Unit,
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
    val base = """\[[^\[\]]+\]|@[\p{L}\p{N}_\-.·\u00B7\u30FB]+|#[^#\n]+#"""
    val pattern = if (urlPattern.isNotEmpty()) "$base|$urlPattern" else base
    return Regex(pattern)
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
    inlineImageLinks: Map<String, List<FeedImage>> = emptyMap(),
    onInlineImageClick: ((List<FeedImage>) -> Unit)? = null,
    urlEntities: Map<String, FeedUrlEntity> = emptyMap(),
    onUrlEntityClick: ((FeedUrlEntity) -> Unit)? = null,
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
        val tokenRegex = buildStatusTokenRegex(inlineImageLinks, urlEntities)
        tokenRegex.findAll(text).forEach { match ->
            if (match.range.first > last) {
                append(text.substring(last, match.range.first))
            }
            val token = match.value
            when {
                inlineImageLinks.containsKey(token) && onInlineImageClick != null -> {
                    val linkStyle = SpanStyle(
                        color = primaryColor,
                        fontWeight = FontWeight.Medium,
                        textDecoration = TextDecoration.None,
                    )
                    withLink(
                        LinkAnnotation.Clickable(
                            tag = "view-image:$token",
                            linkInteractionListener = { onInlineImageClick(inlineImageLinks[token].orEmpty()) },
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
                urlEntities.containsKey(token) && onUrlEntityClick != null -> {
                    val entity = urlEntities.getValue(token)
                    val linkStyle = SpanStyle(
                        color = primaryColor,
                        fontWeight = FontWeight.Medium,
                        textDecoration = TextDecoration.None,
                    )
                    withLink(
                        LinkAnnotation.Clickable(
                            tag = "url-entity:${entity.shortUrl}",
                            linkInteractionListener = { onUrlEntityClick(entity) },
                        ),
                    ) {
                        withStyle(linkStyle) {
                            append(entity.title)
                        }
                    }
                }
                urlEntities.containsKey(token) -> {
                    val entity = urlEntities.getValue(token)
                    withStyle(SpanStyle(color = primaryColor, fontWeight = FontWeight.Medium)) {
                        append(entity.title)
                    }
                }
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
                    val topic = token.removePrefix("#").removeSuffix("#")
                    val onTopicClick = LocalTopicClickHandler.current
                    if (onTopicClick != null) {
                        withLink(
                            LinkAnnotation.Clickable(
                                tag = "topic:$topic",
                                linkInteractionListener = { onTopicClick(topic) },
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
    onMediaClick: (FeedMedia, String) -> Unit,
    emoticonMap: Map<String, String> = emptyMap(),
    onUserClick: ((String) -> Unit)? = null,
    onRetweetClick: ((FeedItem) -> Unit)? = null,
    isLongTextLoading: (FeedItem) -> Boolean = { false },
    onLoadLongText: ((FeedItem) -> Unit)? = null,
    onToggleLike: ((FeedItem) -> Unit)? = null,
    onLikeClick: ((FeedItem, Rect) -> Unit)? = null,
    onUrlEntityClick: ((FeedUrlEntity) -> Unit)? = null,
    onCommentClick: (() -> Unit)? = null,
    onCommentLongClick: (() -> Unit)? = null,
    onRepostClick: (() -> Unit)? = null,
    showAuthorRow: Boolean = true,
    menuBackEnabled: Boolean = true,
    insetRounded: Boolean = false,
    onBoundsChange: (Rect) -> Unit = {},
) {
    val resolvedEmoticonMap = resolveEmoticonMap(emoticonMap, item.collectEmoticons())
    var inlineImagePreview by remember(item.statusId) { mutableStateOf<List<FeedImage>?>(null) }
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
                    FeedCardActionMenu(
                        item = item,
                        backHandlerEnabled = menuBackEnabled,
                    )
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
                    item = item,
                    emoticonMap = resolvedEmoticonMap,
                    style = MaterialTheme.typography.bodyMedium,
                    onUserClick = onUserClick,
                    isLongTextLoading = isLongTextLoading(item),
                    onLoadLongText = onLoadLongText,
                    inlineImageLinks = item.inlineImageLinks,
                    onInlineImageClick = { inlineImagePreview = it },
                    urlEntities = item.urlEntities.associateBy { entity -> entity.shortUrl },
                    onUrlEntityClick = onUrlEntityClick,
                )
            }
            item.retweetedStatus?.let { retweeted ->
                QuotedStatus(
                    modifier = Modifier.padding(horizontal = FeedCardContentHorizontalPadding),
                    item = retweeted,
                    playbackOwnerId = item.statusId,
                    onMediaClick = onMediaClick,
                    emoticonMap = emoticonMap,
                    onClick = onRetweetClick?.let { cb -> { cb(retweeted) } },
                    onUserClick = onUserClick,
                    isLongTextLoading = isLongTextLoading(retweeted),
                    onLoadLongText = onLoadLongText,
                    onUrlEntityClick = onUrlEntityClick,
                )
            }
            MediaStrip(
                modifier = Modifier.padding(horizontal = FeedCardContentHorizontalPadding),
                images = item.images,
                medias = item.medias,
                playbackOwnerId = item.statusId,
                onMediaClick = onMediaClick,
                onDetailClick = if (showAuthorRow) onClick else null,
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
        modifier = Modifier
            .fillMaxWidth()
            .onGloballyPositioned { coordinates ->
                onBoundsChange(coordinates.boundsInWindow())
            },
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
        inlineImageLinks = inlineImageLinks,
        onInlineImageClick = onInlineImageClick,
        urlEntities = urlEntities,
        onUrlEntityClick = onUrlEntityClick,
    )
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
    modifier: Modifier = Modifier,
) {
    val resolvedMap = resolveEmoticonMap(emoticonMap, item.collectEmoticons())
    val userTarget = item.authorId.takeIf { it.isNotBlank() } ?: item.authorName
    val quotedShape = RoundedCornerShape(8.dp)
    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant, quotedShape),
        shape = quotedShape,
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.58f),
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

@Composable
private fun FeedCardActionMenu(
    item: FeedItem,
    backHandlerEnabled: Boolean = true,
) {
    val controller = LocalFeedCardActionMenuController.current
    var anchorBounds by remember(item.id) { mutableStateOf<Rect?>(null) }
    val isExpanded = controller.activeRequest?.item?.id == item.id

    LaunchedEffect(backHandlerEnabled, isExpanded) {
        if (!backHandlerEnabled && isExpanded) {
            controller.dismiss()
        }
    }

    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(RoundedCornerShape(8.dp))
            .onGloballyPositioned { coordinates ->
                anchorBounds = coordinates.boundsInRoot()
            }
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = {
                    if (isExpanded) {
                        controller.dismiss()
                    } else {
                        val bounds = anchorBounds ?: return@clickable
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

@Composable
private fun FeedCardActionMenuOverlay(
    controller: FeedCardActionMenuController,
    backdrop: Backdrop,
) {
    val request = controller.activeRequest ?: return
    val context = LocalContext.current
    val density = LocalDensity.current
    val shareUrl = remember(request.item.id) {
        WeiboStatusActions.weiboUrl(request.item)
    }

    LaunchedEffect(request.backHandlerEnabled) {
        if (!request.backHandlerEnabled) {
            controller.dismiss()
        }
    }

    BackHandler(enabled = request.backHandlerEnabled) {
        controller.dismiss()
    }

    val menuWidth = 140.dp
    val gapFromButton = 6.dp
    val screenMargin = 14.dp
    val anchor = request.anchorBoundsInRoot

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .zIndex(45f),
    ) {
        val screenWidthPx = with(density) { maxWidth.toPx() }
        val screenHeightPx = with(density) { maxHeight.toPx() }
        val menuPlacement = calculateFeedCardActionMenuOffsetPx(
            anchorBounds = anchor,
            screenWidthPx = screenWidthPx,
            screenHeightPx = screenHeightPx,
            menuWidthPx = with(density) { menuWidth.toPx() },
            menuHeightPx = with(density) { ActionMenuTwoRowHeight.toPx() },
            marginPx = with(density) { screenMargin.toPx() },
            gapPx = with(density) { gapFromButton.toPx() },
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = { controller.dismiss() },
                ),
        )
        ImageActionFrostedCard(
            modifier = Modifier
                .offset { menuPlacement.offset }
                .width(menuWidth),
            backdrop = backdrop,
        ) {
            ImageActionRow(
                label = "跳转到微博",
                enabled = shareUrl != null,
                onClick = {
                    controller.dismiss()
                    WeiboStatusActions.openInWeiboApp(context, request.item)
                },
            )
            ImageActionMenuDivider()
            ImageActionRow(
                label = "分享",
                enabled = shareUrl != null,
                onClick = {
                    controller.dismiss()
                    WeiboStatusActions.shareLink(context, request.item)
                },
            )
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

private fun singleImageDisplayAspectRatio(
    width: Int,
    height: Int,
    maxHeightToWidth: Float = SingleImageMaxHeightToWidth,
): Float {
    if (width <= 0 || height <= 0) return 1f
    val naturalAspect = width.toFloat() / height
    val minAspectFromHeightCap = 1f / maxHeightToWidth
    return naturalAspect.coerceIn(minAspectFromHeightCap, 3f)
}

private fun feedVideoDisplayAspectRatio(media: FeedMedia): Float {
    val width = media.coverWidth ?: when (media.videoOrientation) {
        "vertical" -> 9
        else -> 16
    }
    val height = media.coverHeight ?: when (media.videoOrientation) {
        "vertical" -> 16
        else -> 9
    }
    return singleImageDisplayAspectRatio(
        width = width,
        height = height,
        maxHeightToWidth = VideoMaxHeightToWidth,
    )
}

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

@Composable
private fun FeedImageCell(
    image: FeedImage,
    allImages: List<FeedImage>,
    imageIndex: Int = 0,
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
    var anchorBounds by remember(image.id) { mutableStateOf<Rect?>(null) }
    var anchorCoordinates by remember(image.id) { mutableStateOf<LayoutCoordinates?>(null) }
    val imagePeekController = LocalImagePeekController.current
    val mediaHaptics = rememberMediaPeekHaptics()
    val holdScale = mediaPeekHoldScale(if (actionOpen) 0f else pressHoldProgress)

    fun resetPeekState() {
        actionOpen = false
        peekActive = false
        pressHoldProgress = 0f
        imagePeekController.resetFingerDragOffset()
    }

    fun openImagePeek(pressWindowOffset: Offset) {
        val bounds = anchorCoordinates?.takeIf { it.isAttached }?.boundsInWindow()
            ?: anchorBounds
            ?: return
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
                anchorCoordinates = coordinates
                val bounds = coordinates.boundsInWindow()
                anchorBounds = bounds
                onAnchorBoundsChanged(bounds)
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
                    val bounds = anchorCoordinates?.takeIf { it.isAttached }?.boundsInWindow()
                        ?: anchorBounds
                        ?: return@awaitEachGesture
                    val down = awaitFirstDown(requireUnconsumed = false)
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
                            val currentBounds = anchorCoordinates?.takeIf { it.isAttached }?.boundsInWindow()
                                ?: bounds
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

@Composable
private fun ImageActionFrostedCard(
    modifier: Modifier = Modifier,
    backdrop: Backdrop? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    SurfaceLiquidMenuCard(
        modifier = modifier,
        backdrop = backdrop ?: LocalLiquidMenuBackdrop.current,
        cornerRadius = ActionMenuCornerRadius,
        contentPadding = PaddingValues(vertical = ActionMenuPaddingVertical),
        content = content,
    )
}

@Composable
private fun ImageActionOverlay(
    image: FeedImage,
    allImages: List<FeedImage>,
    initialImageIndex: Int,
    anchorBounds: Rect,
    pressOffset: Offset,
    isFloating: Boolean,
    dismissReason: ImagePeekDismissReason?,
    onRequestCancel: () -> Unit,
    onDismissComplete: () -> Unit,
    onOpenFullscreenBehind: (Int) -> Unit,
    onEnterFullscreenHandoffComplete: () -> Unit,
) {
    val onMessage = LocalUiMessenger.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    val imagePeekController = LocalImagePeekController.current
    var saving by remember { mutableStateOf(false) }
    val images = allImages.ifEmpty { listOf(image) }
    val safeInitialIndex = initialImageIndex.coerceIn(0, images.lastIndex)
    val pagerState = rememberPagerState(initialPage = safeInitialIndex) { images.size }
    val pagerFlingBehavior = PagerDefaults.flingBehavior(
        state = pagerState,
        snapAnimationSpec = tween(
            durationMillis = 280,
            easing = MediaPeekEnterEasing,
        ),
    )
    val currentImage = images[pagerState.currentPage]
    val enterProgress = remember { Animatable(0f) }
    val fullscreenExpandProgress = remember { Animatable(0f) }
    var fullscreenOpened by remember { mutableStateOf(false) }
    var actionMenuVisible by remember { mutableStateOf(false) }

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
                enterProgress.animateTo(
                    targetValue = 0f,
                    animationSpec = MediaPeekDismissAnimationSpec,
                )
                onDismissComplete()
            }
        }
    }

    BackHandler(enabled = dismissReason != ImagePeekDismissReason.EnterFullscreen) {
        onRequestCancel()
    }
    val expandProgressForBackdrop = fullscreenExpandProgress.value.coerceIn(0f, 1f)
    val expandingToFullscreen = expandProgressForBackdrop > 0f ||
        fullscreenOpened ||
        dismissReason == ImagePeekDismissReason.EnterFullscreen
    BoxWithConstraints(
        Modifier
            .fillMaxSize()
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
                                    onRequestCancel()
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
                            onRequestCancel()
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
        val menuRevealProgress = if (actionMenuVisible && expandProgress == 0f && !fullscreenOpened) {
            val layoutStart = mediaPeekLayoutStartProgress()
            ((rawProgress - layoutStart) / (1f - layoutStart)).coerceIn(0f, 1f)
        } else {
            0f
        }
        val scrimAlpha = if (expandingToFullscreen) {
            1f
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(if (expandingToFullscreen) Color.Black else Color.Black.copy(alpha = scrimAlpha))
                .clickable(
                    enabled = dismissReason != ImagePeekDismissReason.EnterFullscreen,
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = { onRequestCancel() },
                ),
        )

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
                anchorBounds = anchorBounds,
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
            val menuWidth = 180.dp
            val menuHeight = ActionMenuThreeRowHeight
            val menuPlacement = calculateActionMenuOffsetFromAnchorPx(
                anchorBounds = motion.visualBounds,
                screenWidthPx = maxWidthPx,
                screenHeightPx = maxHeightPx,
                menuWidthPx = with(density) { menuWidth.toPx() },
                menuHeightPx = with(density) { menuHeight.toPx() },
                marginPx = with(density) { 14.dp.toPx() },
                gapPx = with(density) { 10.dp.toPx() },
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
                            modifier = Modifier
                                .fillMaxSize()
                                .then(previewTapModifier),
                        )
                    }
                } else {
                    ImageActionPreviewImage(
                        image = currentImage,
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

            if (actionMenuVisible && expandProgress == 0f && !fullscreenOpened) {
                ImageActionFrostedCard(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .offset { menuPlacement.offset }
                        .width(menuWidth)
                        .graphicsLayer {
                            alpha = menuRevealProgress
                            scaleX = lerp(0.92f, 1f, menuRevealProgress)
                            scaleY = lerp(0.92f, 1f, menuRevealProgress)
                            val slideDistance = with(density) { 14.dp.toPx() }
                            translationY = if (menuPlacement.belowAnchor) {
                                lerp(-slideDistance, 0f, menuRevealProgress)
                            } else {
                                lerp(slideDistance, 0f, menuRevealProgress)
                            }
                        },
                ) {
                    ImageActionRow(
                        label = "保存",
                        enabled = !saving,
                        onClick = {
                            saving = true
                            scope.launch {
                                ImageSaveHelper.saveImage(context, currentImage)
                                    .onSuccess { name ->
                                        onMessage("保存成功", "已保存到相册：$name")
                                        onRequestCancel()
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
                                        onRequestCancel()
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
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
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
    val isDocked = isFloating || dockProgress.value > 0f || isFullscreenMode

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
                if (dockProgress.value > 0f) {
                    dockProgress.animateTo(0f, MediaPeekDismissAnimationSpec)
                }
                enterProgress.animateTo(
                    targetValue = 0f,
                    animationSpec = MediaPeekDismissAnimationSpec,
                )
                onDismissComplete()
            }
        }
    }

    BackHandler(enabled = isFullscreenMode || (dismissReason != VideoPeekDismissReason.EnterFullscreen && isDocked)) {
        onRequestCancel()
    }

    val expandProgressForBackdrop = fullscreenExpandProgress.value.coerceIn(0f, 1f)
    val expandingToFullscreen = isFullscreenMode ||
        expandProgressForBackdrop > 0f ||
        dismissReason == VideoPeekDismissReason.EnterFullscreen

    BoxWithConstraints(
        modifier = modifier.then(
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
        val useDockedColumnLayout = isDocked && !expandingToFullscreen
        val dockedGestureModifier = if (isDocked && dismissReason == null) {
            Modifier.pointerInput(media.streamUrl) {
                val dragGestureThreshold = 82f
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    var totalDrag = Offset.Zero
                    var requestedDismiss = false
                    while (true) {
                        val event = awaitPointerEvent(PointerEventPass.Initial)
                        val change = event.changes.firstOrNull { it.id == down.id }
                            ?: event.changes.firstOrNull()
                            ?: break
                        if (!change.pressed) break
                        totalDrag += change.position - change.previousPosition
                        videoPeekController.updateFingerDragOffset(totalDrag)
                        val verticalDominant = abs(totalDrag.y) > abs(totalDrag.x) * 1.15f
                        if (abs(totalDrag.y) > dragGestureThreshold && verticalDominant) {
                            change.consume()
                            requestedDismiss = true
                            onRequestCancel()
                            while (true) {
                                val consumeEvent = awaitPointerEvent(PointerEventPass.Initial)
                                consumeEvent.changes.forEach { it.consume() }
                                if (consumeEvent.changes.all { !it.pressed }) break
                            }
                            break
                        }
                    }
                    if (!requestedDismiss) {
                        videoPeekController.resetFingerDragOffset()
                    }
                }
            }
        } else {
            Modifier
        }

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
                if (isDocked && dismissReason == null) {
                    Modifier.pointerInput(media.streamUrl) {
                        val dragGestureThreshold = 82f
                        awaitEachGesture {
                            val down = awaitFirstDown(requireUnconsumed = false)
                            var totalDrag = Offset.Zero
                            while (true) {
                                val event = awaitPointerEvent(PointerEventPass.Initial)
                                val change = event.changes.firstOrNull { it.id == down.id }
                                    ?: event.changes.firstOrNull()
                                    ?: break
                                if (!change.pressed) {
                                    videoPeekController.resetFingerDragOffset()
                                    break
                                }
                                totalDrag += change.position - change.previousPosition
                                videoPeekController.updateFingerDragOffset(totalDrag)
                                val verticalDominant = abs(totalDrag.y) > abs(totalDrag.x) * 1.15f
                                if (totalDrag.y < -dragGestureThreshold && verticalDominant) {
                                    change.consume()
                                    onRequestCancel()
                                    while (true) {
                                        val consumeEvent = awaitPointerEvent(PointerEventPass.Initial)
                                        consumeEvent.changes.forEach { it.consume() }
                                        if (consumeEvent.changes.all { !it.pressed }) break
                                    }
                                    break
                                }
                                if (totalDrag.y > dragGestureThreshold && verticalDominant) {
                                    change.consume()
                                    onRequestCancel()
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
                } else if (!isDocked && dismissReason == null) {
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
                    onAspectRatio = { aspectRatio = it },
                    onFullscreen = { videoPeekController.enterFullscreen() },
                    onEnterFloatingPlayback = if (isFullscreenMode) {
                        { videoPeekController.exitFullscreenToFloating() }
                    } else {
                        null
                    },
                    enableDoubleTapFullscreen = false,
                    showFullscreenButton = false,
                    showPictureInPictureButton = false,
                    enableLongPressSpeedBoost = isDocked && !isFullscreenMode,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }

        if (expandingToFullscreen) {
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

        Box(
            modifier = if (useDockedColumnLayout) {
                Modifier
                    .fillMaxWidth()
                    .height(dockTop + dockHeight)
            } else {
                Modifier.fillMaxSize()
            },
        ) {
            // Keep the player at one stable composition location while its layout changes.
            // Moving it between conditional branches recreates the PlayerView and briefly
            // exposes the cover fallback at the end of the fullscreen expansion.
            VideoPeekCard(
                modifier = when {
                    isFullscreenMode -> Modifier
                        .fillMaxSize()
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
                        .then(dockedGestureModifier)
                    else -> Modifier
                        .offset(x = holdLeft, y = holdTop)
                        .then(videoCardModifier)
                },
            )
        }
    }
}

@Composable
private fun ImageActionPreviewImage(
    image: FeedImage,
    modifier: Modifier = Modifier,
) {
    var livePlaying by remember(image.largeUrl) { mutableStateOf(image.isLivePhoto) }
    FeedImagePreviewContent(
        image = image,
        livePlaying = livePlaying,
        onLiveEnded = { livePlaying = false },
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
                    rows.forEach { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            row.forEach { image ->
                                val cellIndex = images.indexOf(image)
                                FeedImageCell(
                                    image = image,
                                    allImages = images,
                                    imageIndex = cellIndex,
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
                                Spacer(Modifier.weight(1f))
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

        medias.forEach { media ->
            InlineVideoPlayer(
                media = media,
                playbackOwnerId = playbackOwnerId,
                onClick = { onMediaClick(media, playbackOwnerId) },
                onFullscreenRequest = { onMediaClick(media, playbackOwnerId) },
            )
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
    session: WeiboWebSession? = null,
    relatedPosts: List<FeedItem> = emptyList(),
    emoticonMap: Map<String, String> = emptyMap(),
    statusCache: Map<String, FeedItem> = emptyMap(),
    onOpenStatus: ((FeedItem, Int, Map<String, FeedItem>) -> Unit)? = null,
) {
    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState(pageCount = { images.size }, initialPage = initialIndex)
    val transitionProgress = remember(initialIndex) {
        Animatable(if (animateOpenFromSource && sourceBoundsByIndex[initialIndex] != null) 0f else 1f)
    }
    var transitionClosing by remember { mutableStateOf(false) }
    var dragDismissProgress by remember { mutableFloatStateOf(0f) }
    var closeStartBounds by remember { mutableStateOf<Rect?>(null) }
    fun dismissViewer(startBounds: Rect? = null) {
        if (!transitionClosing) {
            val closeBounds = sourceBoundsByIndex[pagerState.currentPage]
            if (closeBounds != null) {
                closeStartBounds = startBounds
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
            val morphImage = images.getOrNull(morphPageIndex)
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
            val backdropAlpha = (1f - dragDismissProgress * 0.92f).coerceIn(0f, 1f)
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
                        scrimColor = Color.Black.copy(alpha = transition * backdropAlpha),
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
                            .background(
                                Color.Black.copy(
                                    alpha = if (sourceBoundsByIndex.isEmpty()) {
                                        transition * backdropAlpha
                                    } else {
                                        backdropAlpha
                                    },
                                ),
                            ),
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
                ZoomableFullscreenImage(
                    image = images[page],
                    allImages = images,
                    onDismiss = { dismissViewer() },
                    onDismissFromBounds = { bounds -> dismissViewer(bounds) },
                    hasMultipleImages = images.size > 1,
                    onBlockPagerScroll = { blockPagerScroll = it },
                    onDragDismissProgress = { dragDismissProgress = it },
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
    onDismissFromBounds: (Rect) -> Unit = { onDismiss() },
    hasMultipleImages: Boolean = false,
    onBlockPagerScroll: (Boolean) -> Unit = {},
    onDragDismissProgress: (Float) -> Unit = {},
    onRequestPageChange: (Int) -> Unit = {},
) {
    var scale by remember(image.largeUrl) { mutableFloatStateOf(1f) }
    var panOffsetX by remember(image.largeUrl) { mutableFloatStateOf(0f) }
    var panOffsetY by remember(image.largeUrl) { mutableFloatStateOf(0f) }
    var dismissTranslationY by remember(image.largeUrl) { mutableStateOf(0f) }
    var panInertiaJob by remember(image.largeUrl) { mutableStateOf<Job?>(null) }
    var zoomAnimationJob by remember(image.largeUrl) { mutableStateOf<Job?>(null) }
    val dismissSnapAnim = remember(image.largeUrl) { Animatable(0f) }
    val scope = rememberCoroutineScope()
    var fullscreenBitmap by remember(image.largeUrl) {
        mutableStateOf(
            FullscreenBitmapCache.get(fullscreenImageUrlCandidates(image))
                ?.takeIfDrawable()
                ?.takeIf { it.isFullscreenQuality(image) },
        )
    }
    var previewBitmap by remember(image.largeUrl) {
        mutableStateOf(resolveFullscreenPreviewBitmap(image))
    }
    var fullscreenLoading by remember(image.largeUrl) { mutableStateOf(false) }
    var livePlaying by remember(image.largeUrl) { mutableStateOf(image.isLivePhoto) }
    var actionMenuOffset by remember(image.largeUrl) { mutableStateOf<Offset?>(null) }
    var actionMenuVisible by remember(image.largeUrl) { mutableStateOf(false) }

    LaunchedEffect(image.largeUrl) {
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
        val fullscreenCandidates = fullscreenImageUrlCandidates(image)
        FullscreenBitmapCache.get(fullscreenCandidates)?.takeIfDrawable()?.let { cached ->
            if (cached.isFullscreenQuality(image)) {
                fullscreenBitmap = cached
                return@LaunchedEffect
            }
        }
        if (previewBitmap == null) {
            previewBitmap = withContext(Dispatchers.IO) { loadFullscreenPreviewBitmap(image) }
        }
        if (fullscreenBitmap?.isFullscreenQuality(image) == true) {
            return@LaunchedEffect
        }
        fullscreenLoading = true
        val loaded = withContext(Dispatchers.IO) { loadFullscreenBitmap(image) }
        fullscreenLoading = false
        loaded?.let { bitmap ->
            fullscreenBitmap = bitmap
            fullscreenCandidates.firstOrNull()?.let { url ->
                FullscreenBitmapCache.put(url, bitmap)
            } ?: FullscreenBitmapCache.putForImage(image, bitmap)
        }
    }

    val displayBitmap = fullscreenBitmap?.takeIf { it.isFullscreenQuality(image) }
        ?: previewBitmap
        ?: fullscreenBitmap
    val showFullscreenLoading = fullscreenLoading &&
        displayBitmap != null &&
        fullscreenBitmap?.isFullscreenQuality(image) != true

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
        val imageAspect = remember(loadedBitmap.width, loadedBitmap.height, image.width, image.height) {
            val width = (image.width ?: loadedBitmap.width).coerceAtLeast(1)
            val height = (image.height ?: loadedBitmap.height).coerceAtLeast(1)
            width.toFloat() / height.toFloat()
        }
        val fillScreenScale = remember(containerAspect, imageAspect) {
            maxOf(imageAspect / containerAspect, containerAspect / imageAspect)
                .coerceIn(1.35f, 5f)
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

        val pageMenuBackdrop = rememberLayerBackdrop()
        Box(
            modifier = Modifier
                .fillMaxSize()
                .layerBackdrop(pageMenuBackdrop),
        ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(image.largeUrl, containerWidthPx, containerHeightPx, imageAspect) {
                    awaitEachGesture {
                        val velocityTracker = VelocityTracker()
                        val touchSlop = viewConfiguration.touchSlop
                        var panningZoomed = false
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
                                dismissTranslationY = 0f
                                panInertiaJob?.cancel()
                                val first = pressed[0].position
                                val second = pressed[1].position
                                val centroid = Offset((first.x + second.x) / 2f, (first.y + second.y) / 2f)
                                val distance = hypot(first.x - second.x, first.y - second.y)
                                if (lastDistance > 0f) {
                                    val oldScale = gestureScale.coerceAtLeast(0.01f)
                                    val newScale = (oldScale * (distance / lastDistance)).coerceIn(1f, 5f)
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
                                    velocityTracker.addPosition(
                                        pressed.first().uptimeMillis,
                                        centroid,
                                    )
                                }
                                lastDistance = distance
                                event.changes.forEach { it.consume() }
                            } else {
                                lastDistance = 0f
                                val change = pressed.first()
                                val totalDrag = change.position - down.position
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
                                    panningZoomed = true
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
                                onDismiss()
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
                            if (image.isLivePhoto && isInsideDisplayedImage(offset)) {
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
            if (image.isLivePhoto && !livePlaying) {
                Icon(
                    painter = painterResource(R.drawable.ic_live_photo),
                    contentDescription = "Live Photo",
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .offset {
                            IntOffset(iconOffsetX, iconOffsetY)
                        }
                        .zIndex(8f)
                        .size(16.dp),
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

        actionMenuOffset?.let { offset ->
            FullscreenImageActionMenu(
                image = image,
                allImages = allImages,
                pressOffset = offset,
                visible = actionMenuVisible,
                screenWidthPx = containerWidthPx,
                screenHeightPx = containerHeightPx,
                backdrop = pageMenuBackdrop,
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
        ImageActionFrostedCard(backdrop = backdrop) {
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
        videoCoordinator.registerPauseHandler(isFullscreen = false, pauseHandler)
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
            videoCoordinator.unregisterPauseHandler(isFullscreen = false, pauseHandler)
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
    playbackOwnerId: String,
    onClick: () -> Unit = {},
    onFullscreenRequest: () -> Unit = onClick,
) {
    val videoCoordinator = LocalVideoPlaybackCoordinator.current
    val videoPeekController = LocalVideoPeekController.current
    val haptic = LocalHapticFeedback.current
    val playbackKey = remember(media.streamUrl, media.downloadUrl, media.coverUrl, playbackOwnerId) {
        videoPlaybackKey(media, playbackOwnerId)
    }
    val inlinePlaying = videoCoordinator.activeKey == playbackKey &&
        videoCoordinator.fullscreenKey != playbackKey &&
        videoCoordinator.peekPlaybackKey != playbackKey
    val displayAspectRatio = remember(
        media.streamUrl,
        media.videoOrientation,
        media.coverWidth,
        media.coverHeight,
    ) {
        feedVideoDisplayAspectRatio(media)
    }
    var actionOpen by remember(media.streamUrl) { mutableStateOf(false) }
    var peekActive by remember(media.streamUrl) { mutableStateOf(false) }
    var pressHoldProgress by remember(media.streamUrl) { mutableFloatStateOf(0f) }
    var anchorBounds by remember(media.streamUrl) { mutableStateOf<Rect?>(null) }
    var lastTapUptimeMs by remember(media.streamUrl) { mutableStateOf(0L) }
    val mediaHaptics = rememberMediaPeekHaptics()
    val holdScale = mediaPeekHoldScale(if (actionOpen) 0f else pressHoldProgress)

    fun resetPeekState() {
        actionOpen = false
        peekActive = false
        pressHoldProgress = 0f
        videoPeekController.resetFingerDragOffset()
    }

    fun openVideoPeek(pressWindowOffset: Offset) {
        val bounds = anchorBounds ?: return
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

    fun openInlineFloatingPlayback(pressWindowOffset: Offset) {
        val bounds = anchorBounds ?: return
        actionOpen = true
        peekActive = true
        pressHoldProgress = 0f
        videoCoordinator.beginPeekHandoff(playbackKey)
        videoCoordinator.claimPeekPlayback(playbackKey)
        videoPeekController.openFloating(
            VideoPeekRequest(
                media = media,
                anchorBounds = bounds,
                pressOffset = pressWindowOffset,
                playbackOwnerId = playbackOwnerId,
                onCancel = {
                    videoCoordinator.cancelPeekHandoff(playbackKey)
                    resetPeekState()
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

    fun openInlineFullscreenTransition(pressWindowOffset: Offset) {
        val bounds = anchorBounds
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

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(VideoMaxWidthFraction)
                .aspectRatio(displayAspectRatio)
                .onGloballyPositioned { coordinates ->
                    anchorBounds = coordinates.boundsInWindow()
                }
                .graphicsLayer {
                    scaleX = holdScale
                    scaleY = holdScale
                    alpha = if (actionOpen) 0f else 1f
                }
                .clip(RoundedCornerShape(8.dp))
                .background(Color.Black)
                .pointerInput(media.streamUrl, media.type) {
                awaitEachGesture {
                    val bounds = anchorBounds ?: return@awaitEachGesture
                    val down = awaitFirstDown(requireUnconsumed = false)
                    var lastPosition = down.position
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
                            val isDoubleTap = lastTapUptimeMs > 0L &&
                                tapUptime - lastTapUptimeMs <= viewConfiguration.doubleTapTimeoutMillis
                            if (isDoubleTap) {
                                lastTapUptimeMs = 0L
                                if (media.isStreamPlayable()) {
                                    openInlineFloatingPlayback(pressWindowOffset)
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

                    val dragGestureThreshold = 82f
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
                        if (
                            totalDrag.y > dragGestureThreshold &&
                            verticalDominant
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
                        if (
                            totalDrag.y < -dragGestureThreshold &&
                            verticalDominant
                        ) {
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
                    if (!cancelledByDrag && !floatByDrag) {
                        videoPeekController.enterFullscreen()
                    }
                }
            },
    ) {
        if (inlinePlaying && media.isStreamPlayable()) {
            WeiboVideoSurface(
                media = media,
                playbackOwnerId = playbackOwnerId,
                isFullscreen = false,
                enableDoubleTapFullscreen = false,
                videoResizeMode = androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_FIT,
                onAspectRatio = {},
                onFullscreen = {
                    val bounds = anchorBounds
                    val center = bounds?.let {
                        Offset(it.left + it.width / 2f, it.top + it.height / 2f)
                    } ?: Offset.Zero
                    openInlineFullscreenTransition(center)
                },
                onEnterFloatingPlayback = {
                    val bounds = anchorBounds
                    val center = bounds?.let {
                        Offset(it.left + it.width / 2f, it.top + it.height / 2f)
                    } ?: Offset.Zero
                    openInlineFloatingPlayback(center)
                },
                showPictureInPictureButton = false,
            )
        } else {
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
                    modifier = Modifier.size(65.dp),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_video_play),
                        contentDescription = "播放",
                        modifier = Modifier.size(60.dp),
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
}

@Composable
private fun WeiboVideoSurface(
    media: FeedMedia,
    playbackOwnerId: String,
    isFullscreen: Boolean,
    onAspectRatio: (Float) -> Unit,
    onFullscreen: () -> Unit,
    onEnterPictureInPicture: (() -> Unit)? = null,
    onEnterFloatingPlayback: (() -> Unit)? = null,
    modifier: Modifier = Modifier.fillMaxSize(),
    controlsEnabled: Boolean = true,
    resumePosition: Boolean = true,
    savePositionOnDispose: Boolean = true,
    playbackSpeedOverride: Float? = null,
    onPlaybackEnded: (() -> Unit)? = null,
    enableDoubleTapFullscreen: Boolean = true,
    videoResizeMode: Int = androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_FIT,
    initialControlsVisible: Boolean = true,
    trackViewportPauseOverride: Boolean? = null,
    showFullscreenButton: Boolean = true,
    showPictureInPictureButton: Boolean = true,
    enableLongPressSpeedBoost: Boolean = false,
    isPeekPlayback: Boolean = false,
    seamlessOverlayPlayback: Boolean = false,
) {
    val context = LocalContext.current
    val onMessage = LocalUiMessenger.current
    val scope = rememberCoroutineScope()
    val videoCoordinator = LocalVideoPlaybackCoordinator.current
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
    val trackViewportPause = trackViewportPauseOverride ?: (!isFullscreen && controlsEnabled)
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
        delay(5_000)
        controlsVisible = false
    }

    val playerCache = remember { mutableMapOf<String, androidx.media3.exoplayer.ExoPlayer>() }
    val awaitingFullscreenHandoff = !seamlessOverlayPlayback &&
        videoCoordinator.pendingFullscreenHandoffKey == playbackKey &&
        (isFullscreen || isPeekPlayback)
    val awaitingPeekHandoffSource = !seamlessOverlayPlayback &&
        !isFullscreen && !isPeekPlayback &&
        videoCoordinator.pendingPeekHandoffKey == playbackKey
    val awaitingPeekHandoffConsume = !seamlessOverlayPlayback &&
        isPeekPlayback && videoCoordinator.pendingPeekHandoffKey == playbackKey

    fun configureExistingPlayer(target: androidx.media3.exoplayer.ExoPlayer) {
        if (videoCoordinator.consumePeekRestartFromBeginning(playbackKey)) {
            target.seekTo(0)
            videoCoordinator.positions[playbackKey] = 0L
        } else if (target.playbackState == androidx.media3.common.Player.STATE_ENDED &&
            playbackSpeedOverride != null
        ) {
            target.seekTo(0)
            videoCoordinator.positions[playbackKey] = 0L
        }
        target.playWhenReady = true
        if (playbackSpeedOverride != null) {
            target.setPlaybackSpeed(playbackSpeedOverride)
        } else {
            target.setPlaybackSpeed(1f)
        }
        target.play()
    }

    fun createFreshPlayer(): androidx.media3.exoplayer.ExoPlayer =
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
                videoCoordinator.positions[playbackKey] = 0L
            } else if (effectiveResumePosition) {
                videoCoordinator.positions[playbackKey]?.takeIf { it > 0L }?.let { seekTo(it) }
            }
            playbackSpeedOverride?.let { setPlaybackSpeed(it) }
            playWhenReady = true
        }

    fun resolveImmediatePlayer(): androidx.media3.exoplayer.ExoPlayer? {
        videoCoordinator.consumeHandoffPlayer(playbackKey)?.let { handoff ->
            configureExistingPlayer(handoff)
            return handoff
        }
        playerCache[videoUrl]?.let { cached ->
            configureExistingPlayer(cached)
            return cached
        }
        return createFreshPlayer()
    }

    var handoffPlayerResolved by remember(playbackKey, videoUrl) { mutableStateOf(false) }
    val immediatePlayer = remember(
        playbackKey,
        videoUrl,
        awaitingFullscreenHandoff,
        awaitingPeekHandoffSource,
        handoffPlayerResolved,
    ) {
        if (
            handoffPlayerResolved ||
            awaitingFullscreenHandoff ||
            awaitingPeekHandoffSource ||
            awaitingPeekHandoffConsume
        ) {
            null
        } else {
            resolveImmediatePlayer()
        }
    }
    var deferredPlayer by remember(playbackKey, videoUrl) {
        mutableStateOf<androidx.media3.exoplayer.ExoPlayer?>(null)
    }

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
                configureExistingPlayer(handoff)
                deferredPlayer = handoff
                handoffPlayerResolved = true
                return@LaunchedEffect
            }
            if (videoCoordinator.pendingFullscreenHandoffKey != playbackKey &&
                videoCoordinator.pendingPeekHandoffKey != playbackKey
            ) {
                break
            }
            delay(16)
        }
        videoCoordinator.cancelFullscreenHandoff(playbackKey)
        videoCoordinator.cancelPeekHandoff(playbackKey)
        deferredPlayer = resolveImmediatePlayer()
        handoffPlayerResolved = true
    }

    val player = immediatePlayer ?: deferredPlayer
    val playerViewHolder = remember { object { var view: androidx.media3.ui.PlayerView? = null } }
    var transitionFrame by remember(playbackKey) {
        mutableStateOf(videoCoordinator.consumeTransitionFrame(playbackKey))
    }
    if (player == null) {
        Box(
            modifier = modifier.background(Color.Black),
            contentAlignment = Alignment.Center,
        ) {
            if (awaitingFullscreenHandoff && !media.coverUrl.isNullOrBlank()) {
                RemoteImage(
                    url = media.coverUrl.orEmpty(),
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit,
                )
            }
        }
        return
    }
    var videoFrameReady by remember(player) { mutableStateOf(false) }
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
            videoCoordinator.positions[playbackKey] = 0L
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
        if (isPeekPlayback) {
            videoCoordinator.registerPeekPauseHandler(pauseHandler)
        } else {
            videoCoordinator.registerPauseHandler(isFullscreen, pauseHandler)
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
            }

            override fun onVideoSizeChanged(videoSize: androidx.media3.common.VideoSize) {
                val width = videoSize.width.takeIf { it > 0 } ?: return
                val height = videoSize.height.takeIf { it > 0 } ?: return
                val ratio = width.toFloat() / height.toFloat()
                aspectRatio = ratio
                onAspectRatio(ratio)
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
                videoCoordinator.unregisterPeekPauseHandler(pauseHandler)
                videoCoordinator.releasePeekPlayback(playbackKey)
            } else {
                videoCoordinator.unregisterPauseHandler(isFullscreen, pauseHandler)
            }
            if (effectiveSavePosition) {
                if (videoCoordinator.isPeekRestartFromBeginning(playbackKey)) {
                    videoCoordinator.positions[playbackKey] = 0L
                } else {
                    val currentPosition = player.currentPosition.coerceAtLeast(0L)
                    if (currentPosition > 0L || videoCoordinator.positions[playbackKey] == null) {
                        videoCoordinator.positions[playbackKey] = currentPosition
                    }
                }
            }
            player.removeListener(listener)
            val handoffToFullscreen = !seamlessOverlayPlayback && !isFullscreen &&
                videoCoordinator.pendingFullscreenHandoffKey == playbackKey
            val handoffToPeek = !seamlessOverlayPlayback && !isPeekPlayback &&
                videoCoordinator.pendingPeekHandoffKey == playbackKey
            if (handoffToFullscreen || handoffToPeek) {
                if (!videoCoordinator.hasStashedHandoff(playbackKey)) {
                    playerViewHolder.view?.player = null
                    videoCoordinator.stashHandoffPlayer(playbackKey, player)
                }
            } else {
                player.pause()
                if (trackViewportPause && videoCoordinator.activeKey == playbackKey) {
                    videoCoordinator.activeKey = null
                }
                player.release()
            }
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
            if (player.isPlaying) {
                withFrameMillis { }
            } else {
                delay(200)
            }
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
        val videoControlBackdrop = rememberLayerBackdrop()
        Box(
            Modifier
                .fillMaxSize()
                .layerBackdrop(videoControlBackdrop),
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
            modifier = fullscreenVideoModifier
                .then(
                    if (controlsEnabled) {
                        Modifier.pointerInput(player, selectedSpeed, isFullscreen, onFullscreen, enableDoubleTapFullscreen, enableLongPressSpeedBoost) {
                            detectTapGestures(
                                onTap = { toggleControls() },
                                onDoubleTap = {
                                    if (enableDoubleTapFullscreen && !isFullscreen) {
                                        openFullscreenWithCurrentFrame()
                                    }
                                },
                                onPress = {
                                    if (isLiveBroadcast) return@detectTapGestures
                                    val holdTimeout = if (enableLongPressSpeedBoost) {
                                        viewConfiguration.longPressTimeoutMillis.toLong()
                                    } else {
                                        360L
                                    }
                                    val releasedEarly = withTimeoutOrNull(holdTimeout) { tryAwaitRelease() }
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
                    playerViewHolder.view = this
                    this.player = player
                    useController = false
                    resizeMode = effectiveResizeMode
                    keepScreenOn = isPlaying
                    setKeepContentOnPlayerReset(true)
                    setShutterBackgroundColor(android.graphics.Color.BLACK)
                }
            },
            update = { view ->
                playerViewHolder.view = view
                if (view.player !== player) {
                    view.player = null
                    view.player = player
                }
                view.resizeMode = effectiveResizeMode
                view.keepScreenOn = isPlaying
                view.requestLayout()
            },
            onRelease = { view ->
                view.keepScreenOn = false
                view.player = null
                if (playerViewHolder.view === view) {
                    playerViewHolder.view = null
                }
            },
            )
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
                (transitionFrame != null || !media.coverUrl.isNullOrBlank()),
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
                text = if (downloading) "下载中" else "下载",
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
                        if (!isFullscreen) {
                            if (isPeekPlayback) {
                                videoCoordinator.claimPeekPlayback(playbackKey)
                            } else {
                                videoCoordinator.requestInlinePlayback(playbackKey)
                            }
                        }
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

    TransparentLiquidCapsule(
        modifier = modifier
            .pointerInput(durationState) {
                awaitEachGesture {
                    isScrubbing = true
                    try {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        val duration = durationState
                        val width = size.width.toFloat()
                        if (duration <= 0L || width <= 0f) return@awaitEachGesture
                        val anchorPosition = positionState
                        val anchorX = down.position.x
                        var lastSeekPosition = anchorPosition
                        while (true) {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull() ?: break
                            if (change.pressed) {
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
                        isScrubbing = false
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
    onRetweetClick: ((FeedItem) -> Unit)? = null,
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
    val topInset = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    var commentImagePreview by remember { mutableStateOf<Pair<List<FeedImage>, Int>?>(null) }
    val openCommentImage: (List<FeedImage>, Int) -> Unit = { images, index ->
        if (images.isNotEmpty()) {
            commentImagePreview = images to index.coerceIn(0, images.lastIndex)
        }
    }
    val showingReposts = contentSection == DetailContentSection.Reposts
    val sectionItems = if (showingReposts) reposts else comments
    val isLoadingSection = if (showingReposts) isLoadingReposts else isLoadingComments
    val isLoadingMoreSection = if (showingReposts) isLoadingMoreReposts else isLoadingMoreComments
    val isRefreshingSection = isLoadingSection && !isLoadingMoreSection
    val sectionHasMore = if (showingReposts) repostsHasMore else commentsHasMore
    val onLoadMoreSection = if (showingReposts) onLoadMoreReposts else onLoadMoreComments

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
        isRefreshing = isRefreshingSection,
        onRefresh = onRefresh,
        modifier = Modifier.fillMaxSize(),
    ) {
        Column(Modifier.fillMaxSize()) {
            DetailStickyAuthorHeader(
                item = item,
                onUserClick = onUserClick,
                modifier = Modifier.padding(top = topInset),
            )
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(bottom = 24.dp),
            ) {
                item(key = "detail-feed") {
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
                        )
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

                itemsIndexed(sectionItems, key = { _, entry -> entry.id }) { index, entry ->
                    CommentRow(
                        comment = entry,
                        onUserClick = onUserClick,
                        onExpandNestedComments = if (showingReposts) null else onExpandNestedComments,
                        nestedCommentsLoadingIds = nestedCommentsLoadingIds,
                        onCommentImageClick = openCommentImage,
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

    commentImagePreview?.let { (images, index) ->
        FullscreenImageViewer(
            images = images,
            initialIndex = index,
            onDismiss = { commentImagePreview = null },
        )
    }
    }
}

@Composable
private fun DetailStickyAuthorHeader(
    item: FeedItem,
    onUserClick: ((String) -> Unit)?,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 26.dp)
                    .padding(top = 12.dp, bottom = 10.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Box(modifier = Modifier.weight(1f).consumeTouchEvents()) {
                    AuthorRow(
                        item = item,
                        onUserClick = onUserClick,
                        avatarClickable = true,
                    )
                }
                FeedCardActionMenu(item = item)
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
    return existing + appended.filter { it.id !in seen }
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
    val cardColor = Color(0xFFFFFBFF)
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
            .border(1.dp, HintCapsuleBorderColor, RoundedCornerShape(14.dp))
            .background(Color.White)
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
                        color = HintCapsuleText,
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
            color = Color.White,
            shadowElevation = 6.dp,
            border = BorderStroke(1.dp, HintCapsuleBorderColor),
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

@Composable
private fun CommentEmoticonPanel(
    recentEntries: List<Pair<String, String>>,
    allEntries: List<Pair<String, String>>,
    onSelect: (String) -> Unit,
) {
    val displayedRecentEntries = recentEntries.take(14)
    val recentPhrases = displayedRecentEntries.map { it.first }.toSet()
    val remainingEntries = allEntries.filter { it.first !in recentPhrases }
    Column(
        modifier = Modifier
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
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                maxItemsInEachRow = 7,
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
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            maxItemsInEachRow = 7,
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
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun CommentComposerIconButton(
    iconRes: Int,
    contentDescription: String,
    onClick: () -> Unit,
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
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
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
    onCommentImageClick: ((List<FeedImage>, Int) -> Unit)? = null,
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
    val openCommentImage: (Int) -> Unit = { index ->
        onCommentImageClick?.invoke(comment.images, index)
    }
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
                    style = MaterialTheme.typography.bodyMedium,
                    onUserClick = onUserClick,
                    modifier = if (pictureCommentText && onCommentImageClick != null) {
                        Modifier.clickable { openCommentImage(0) }
                    } else {
                        Modifier
                    },
                )
                if (comment.images.isNotEmpty()) {
                    CommentImageStrip(
                        images = comment.images,
                        onImageClick = openCommentImage,
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
        comment.comments.forEach { nested ->
            CommentRow(
                comment = nested,
                depth = depth + 1,
                onUserClick = onUserClick,
                onExpandNestedComments = onExpandNestedComments,
                nestedCommentsLoadingIds = nestedCommentsLoadingIds,
                onCommentImageClick = onCommentImageClick,
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
    onImageClick: (Int) -> Unit,
) {
    if (images.isEmpty()) return

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
                onOpenViewer = { index, _, _, _ -> onImageClick(index) },
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
        color = HintCapsuleText,
        fontSize = 15.sp,
        lineHeight = 20.sp,
    )
    val placeholderStyle = fieldTextStyle.copy(color = HintCapsulePlaceholder)
    SurfaceLiquidCapsule(
        modifier = modifier,
        pill = true,
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
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            color = Color.White.copy(alpha = 0.96f),
            tonalElevation = 0.dp,
            shadowElevation = 8.dp,
        ) {
            Column(
                modifier = Modifier.padding(vertical = 6.dp),
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

@Composable
private fun SearchHistoryRow(
    query: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = "↺",
            modifier = Modifier.width(22.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
        )
        Text(
            text = query,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
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
            Surface(
                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                shape = RoundedCornerShape(6.dp),
            ) {
                Text(
                    text = item.label,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
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
    var suggestResult by remember { mutableStateOf(SearchSuggestResult()) }
    var suggestLoading by remember { mutableStateOf(false) }
    var searchDraft by remember { mutableStateOf(searchBarOverlay.queryInput) }

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
        searchDraft = value
        searchBarOverlay.queryInput = value
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

    LaunchedEffect(searchBarVisible, hasLoginCookie, searchDraft.text) {
        val query = searchDraft.text.trim()
        if (!searchBarVisible || !hasLoginCookie || query.isBlank()) {
            suggestLoading = false
            suggestResult = SearchSuggestResult()
            return@LaunchedEffect
        }
        suggestResult = SearchSuggestResult()
        suggestLoading = true
        delay(120)
        if (query != searchDraft.text.trim()) return@LaunchedEffect
        val result = runCatching { session.loadSearchSuggest(query) }
            .getOrElse { SearchSuggestResult() }
        if (query != searchDraft.text.trim()) return@LaunchedEffect
        suggestResult = result
        suggestLoading = false
    }

    LaunchedEffect(initialResultsReady, activeQuery, searchGeneration) {
        if (activeQuery != null && initialResultsReady) {
            focusManager.clearFocus(force = true)
            keyboardController?.hide()
        }
    }

    val searchGenerationState by rememberUpdatedState(searchGeneration)
    val initialResultsReadyState by rememberUpdatedState(initialResultsReady)
    val latestSearchDraft by rememberUpdatedState(searchDraft)

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
        else -> searchPullRefreshing || resultLoading
    }
    var searchHeaderHeight by remember { mutableStateOf(0.dp) }
    val density = LocalDensity.current
    val bottomNavSpace = 96.dp
    val searchBarGap = SearchBarBottomGap
    val searchBarBottom = bottomNavSpace + searchBarGap
    val searchFieldHeight = 44.dp
    val imeBottom = WindowInsets.ime.asPaddingValues().calculateBottomPadding()
    val imeTargetBottom = WindowInsets.imeAnimationTarget.asPaddingValues().calculateBottomPadding()
    val imeInsetForLayout = if (imeTargetBottom < imeBottom) {
        imeTargetBottom
    } else {
        maxOf(imeBottom, imeTargetBottom)
    }
    val searchFieldBottomTarget = maxOf(searchBarBottom, imeInsetForLayout + searchBarGap)
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
        searchDraft.text.trim().isNotBlank() &&
        activeQuery != searchDraft.text.trim()
    ) {
        176.dp
    } else {
        0.dp
    }
    val listBottomInset = searchFieldBottom + searchFieldHeight + searchBarGap + suggestionPanelInset

    SideEffect {
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
        searchBarOverlay.suggestions = suggestResult
        searchBarOverlay.suggestionsLoading = suggestLoading
        searchBarOverlay.suggestionsVisible = searchBarVisible &&
            searchDraft.text.trim().isNotBlank() &&
            activeQuery != searchDraft.text.trim()
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
                        item {
                            Text(
                                text = "搜索历史",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(bottom = 8.dp),
                            )
                        }
                        val visibleHistory = if (searchHistoryExpanded) {
                            searchHistory
                        } else {
                            searchHistory.take(SearchHistoryStore.DISPLAY_COLLAPSED)
                        }
                        itemsIndexed(visibleHistory, key = { _, query -> "history-$query" }) { index, query ->
                            SearchHistoryRow(
                                query = query,
                                onClick = { submitQuery(query) },
                            )
                            if (index < visibleHistory.lastIndex) {
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
                            }
                        }
                        if (searchHistory.size > SearchHistoryStore.DISPLAY_COLLAPSED) {
                            item {
                                TextButton(
                                    onClick = { searchHistoryExpanded = !searchHistoryExpanded },
                                    contentPadding = PaddingValues(vertical = 4.dp),
                                ) {
                                    Text(
                                        text = if (searchHistoryExpanded) {
                                            "收起"
                                        } else {
                                            "展开更多（${searchHistory.size - SearchHistoryStore.DISPLAY_COLLAPSED}）"
                                        },
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.primary,
                                    )
                                }
                            }
                        }
                        item {
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
                            itemsIndexed(hotSearchItems, key = { _, item -> item.word }) { index, item ->
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
                            items(userResultItems, key = { it.id.ifBlank { it.screenName } }) { user ->
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
                            items(resultItems, key = { it.id }) { item ->
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
            if (activeQuery != null) {
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
    val bottomNavSpace = 96.dp
    val bottomBarGap = 8.dp
    val imeBottom = WindowInsets.ime.asPaddingValues().calculateBottomPadding()
    val bottomInset = if (imeBottom > 0.dp) imeBottom + bottomBarGap else bottomNavSpace + bottomBarGap
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
            setLayerType(View.LAYER_TYPE_HARDWARE, null)
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
            webView.setLayerType(View.LAYER_TYPE_HARDWARE, null)
            webView.visibility = View.VISIBLE
            webView.onResume()
        } else {
            webView.onPause()
            webView.visibility = View.GONE
            webView.setLayerType(View.LAYER_TYPE_NONE, null)
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
    onAlbumTabSelected: () -> Unit = {},
    onRefresh: () -> Unit,
    onLoadMorePosts: () -> Unit,
    onLoadMoreAlbum: () -> Unit,
    onSyncEmoticons: () -> Unit,
    onItemClick: (FeedItem) -> Unit,
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
    selectedThemeColor: MorandiThemeColor = MorandiThemeColors.first(),
    onThemeColorChange: (MorandiThemeColor) -> Unit = {},
    showFollowActions: Boolean = false,
    followLoading: Boolean = false,
    onFollowClick: () -> Unit = {},
    onOpenFollowList: ((String, String, String?, String?, FriendListTab) -> Unit)? = null,
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
                selectedThemeColor = selectedThemeColor,
                onThemeColorChange = onThemeColorChange,
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

    Box(Modifier.fillMaxSize()) {
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
                    key(profile?.id, profile?.description) {
                        MineProfileHeader(
                            profile = profile,
                            hasLoginCookie = hasLoginCookie,
                            loadError = loadError,
                            onOpenSettings = if (enableSettings) {
                                { showSettings = true }
                            } else {
                                null
                            },
                            onAvatarClick = openAvatarViewer,
                            showFollowActions = showFollowActions,
                            followLoading = followLoading,
                            onFollowClick = onFollowClick,
                            onOpenFollowList = openFollowListForProfile,
                        )
                    }
                    }
                }

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

                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    beyondViewportPageCount = 0,
                ) { page ->
                    when (MineContentTab.entries[page]) {
                        MineContentTab.Posts -> {
                            LazyColumn(
                                state = postsListState,
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(bottom = 96.dp),
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
                                            onCommentLongClick = { onCommentLongClick(post) },
                                            isLongTextLoading = isLongTextLoading,
                                            onLoadLongText = onLoadLongText,
                                            onToggleLike = onToggleLike,
                                            onLikeClick = onLikeClick,
                                            onUrlEntityClick = onUrlEntityClick,
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
    selectedThemeColor: MorandiThemeColor,
    onThemeColorChange: (MorandiThemeColor) -> Unit,
    onBack: () -> Unit,
    onSwitchAccount: (String) -> Unit,
    onDeleteAccount: (String) -> Unit,
    onAddAccount: () -> Unit,
    onSyncEmoticons: () -> Unit,
) {
    var accountExpanded by remember { mutableStateOf(false) }
    var emoticonExpanded by remember { mutableStateOf(false) }
    var imageExpanded by remember { mutableStateOf(false) }
    var themeExpanded by remember { mutableStateOf(false) }
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
            "登录后可浏览首页信息流、搜索、个人主页，以及写微博、查看消息等网页功能。",
            "若首页为空，请确认已登录，并在首页下拉刷新或再次点击底部「首页」同步内容。",
        ),
    ),
    HelpSection(
        title = "底部导航",
        items = listOf(
            "底部共有五个入口：首页、消息、搜索、写微博、我的。",
            "向下滚动列表时，底部栏会自动收起到左侧小胶囊；单击小胶囊可展开。",
            "展开后点击空白区域可再次收起；点击其他 Tab 时选中块会滑向目标位置。",
            "在小胶囊上长按并左右拖动，可快速切换 Tab；松手后停留在目标页。",
            "双击小胶囊：在首页刷新信息流；在「我的」回到顶部并刷新资料。",
            "再次点击当前选中的「首页」，也会从顶部刷新关注流。",
            "长按底部「首页」按钮，可在「最新微博」与「朋友圈」之间切换。",
            "在「消息」「写微博」页按系统返回键，优先网页内后退；无法后退时留在当前页。",
            "在其他 Tab 按返回键会先回到首页；在首页连按两次返回键退出应用。",
        ),
    ),
    HelpSection(
        title = "首页信息流",
        items = listOf(
            "在首页下拉可刷新关注流；滚动到底部会自动加载更多。",
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
            "Live Photo 会在全屏时自动播放动态效果。",
            "视频：点击中心播放按钮可在卡片内播放；双击视频卡片打开底部浮窗播放。",
            "长按视频会弹出大预览，松手进入全屏；浮窗中可上滑收起、下滑关闭。",
            "浮窗播放时切换 Tab、进入详情或用户主页，视频会继续播放；返回可回到原页面。",
            "竖屏与横屏视频浮窗使用相同窗口大小；浮窗右上角可点「全屏」。",
            "全屏播放竖屏视频时，右上角可点「横屏」横屏观看；横屏后可点「竖屏」切回。",
            "全屏视频支持「浮窗」退回小窗，也支持画中画；底部胶囊进度条可拖动快进。",
            "可在设置中控制切到后台后是否继续播放声音。",
            "从「我的」或用户主页进入相册，可按日期浏览并查看原图，部分图片可关联到原微博。",
        ),
    ),
    HelpSection(
        title = "微博详情与评论",
        items = listOf(
            "详情页下拉可刷新微博与评论；评论列表滚动到底会自动加载更多。",
            "点击评论排序按钮，可在「按时间」与「按热度」之间切换。",
            "支持楼中楼评论展开；长按评论行可回复该评论。",
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
            "输入关键词后搜索；在结果页按返回键可清空结果并回到搜索首页。",
            "从首页或用户主页点击 #话题# 进入搜索时，会临时使用「搜微博」模式，不影响搜索页原有模式设置。",
            "搜索结果中的微博、用户交互方式与首页、个人主页一致。",
        ),
    ),
    HelpSection(
        title = "写微博与消息",
        items = listOf(
            "「写微博」「消息」使用移动版微博网页（m.weibo.cn），登录态与首页共用。",
            "写微博时可从相册选择图片发布；网页内返回优先于切 Tab。",
            "消息页地址为 m.weibo.cn/message，可查看私信与通知。",
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
            "在用户主页点击微博中的 #话题#，会进入该话题搜索；返回一次回到该用户主页。",
            "列表滚动到顶部附近时，顶部资料区会随滚动逐渐收起。",
        ),
    ),
    HelpSection(
        title = "设置项说明",
        items = listOf(
            "账号管理：登录、添加账号、切换已保存账号；切换后会重新加载对应账号数据。",
            "表情同步：从微博拉取表情配置到本地，改善正文与评论中的表情显示。",
            "浏览信息流时，正文里出现的表情也会自动收录到本地，同步时不会删除这些表情。",
            "图片清晰度：省流 / 标准 / 高清三档，影响信息流缩略图加载规格；全屏仍会尽量加载高清图。",
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
        color = Color.White,
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
        lerp(Color.White, MaterialTheme.colorScheme.primaryContainer, 0.35f)
    } else {
        Color.White
    }
    val dragState = remember(account.id, deleteActionWidthPx, density) {
        AnchoredDraggableState(
            initialValue = SettingsAccountSwipeAnchor.Closed,
            anchors = DraggableAnchors {
                SettingsAccountSwipeAnchor.Closed at 0f
                SettingsAccountSwipeAnchor.Open at -deleteActionWidthPx
            },
            positionalThreshold = { distance -> distance * 0.35f },
            velocityThreshold = { with(density) { 120.dp.toPx() } },
            snapAnimationSpec = spring(
                dampingRatio = 0.82f,
                stiffness = Spring.StiffnessMedium,
            ),
            decayAnimationSpec = exponentialDecay(),
        )
    }
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
                    Column(verticalArrangement = Arrangement.spacedBy(gap)) {
                        MorandiThemeColors.chunked(columns).forEach { rowOptions ->
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
                    .background(Color.White)
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
                    items(users, key = { it.id }) { user ->
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
        color = if (following) Color.White else WeiboFollowOrange,
        border = if (following) BorderStroke(1.dp, Color(0xFFE0E0E0)) else null,
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
                match?.type == "livephoto" && !mergedLiveVideo.isNullOrBlank() -> "livephoto"
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
    onOpenViewer: (Int, Rect?, (() -> Unit)?, (() -> Unit)?) -> Unit,
    onVideoClick: (FeedMedia, String) -> Unit,
) {
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
    val mediaHaptics = rememberMediaPeekHaptics()
    val playbackKey = remember(media.streamUrl, media.downloadUrl, media.coverUrl, playbackOwnerId) {
        videoPlaybackKey(media, playbackOwnerId)
    }
    var actionOpen by remember(media.streamUrl) { mutableStateOf(false) }
    var peekActive by remember(media.streamUrl) { mutableStateOf(false) }
    var pressHoldProgress by remember(media.streamUrl) { mutableFloatStateOf(0f) }
    var anchorBounds by remember(media.streamUrl) { mutableStateOf<Rect?>(null) }
    val holdScale = mediaPeekHoldScale(if (actionOpen) 0f else pressHoldProgress)

    fun resetPeekState() {
        actionOpen = false
        peekActive = false
        pressHoldProgress = 0f
        videoPeekController.resetFingerDragOffset()
    }

    fun openVideoPeek(pressWindowOffset: Offset) {
        val bounds = anchorBounds ?: return
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
            .onGloballyPositioned { coordinates ->
                anchorBounds = coordinates.boundsInWindow()
            }
            .graphicsLayer {
                scaleX = holdScale
                scaleY = holdScale
                alpha = if (actionOpen) 0f else 1f
            }
            .clip(RoundedCornerShape(8.dp))
            .pointerInput(media.streamUrl) {
                awaitEachGesture {
                    val bounds = anchorBounds ?: return@awaitEachGesture
                    val down = awaitFirstDown(requireUnconsumed = false)
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

                    val dragCancelThreshold = 82f
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
                        videoPeekController.updateFingerDragOffset(
                            change.position.toWindowPosition(bounds) - pressWindowOffset,
                        )
                        val totalDrag = change.position - down.position
                        val verticalDominant = abs(totalDrag.y) > abs(totalDrag.x) * 1.15f
                        if (
                            totalDrag.y > dragCancelThreshold &&
                            verticalDominant
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
                        if (
                            totalDrag.y < -dragCancelThreshold &&
                            verticalDominant
                        ) {
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
                    if (cancelledByDrag) {
                        resetPeekState()
                    } else if (!floatByDrag) {
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
                    "\u4F7F\u7528\u4E0E\u6D88\u606F\u3001\u5199\u5FAE\u535A\u76F8\u540C\u7684\u72EC\u7ACB\u7F51\u9875\u5BB9\u5668\u6253\u5F00 passport.weibo.cn\u3002\u767B\u5F55\u6210\u529F\u540E\u70B9\u51FB\u56DE\u5230\u5FAE\u535A\u9996\u9875\u540C\u6B65\u6570\u636E\u3002",
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
            reloadKey = loginReloadKey,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        )
    }
}

@Composable
private fun AccountScreen(session: WeiboWebSession) {
    var loginReloadKey by remember { mutableIntStateOf(0) }

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
                    TextButton(onClick = { loginReloadKey += 1 }) {
                        Text("\u91CD\u65B0\u6253\u5F00")
                    }
                }
            }
        }
        AccountLoginWebView(
            reloadKey = loginReloadKey,
            modifier = Modifier.fillMaxWidth().weight(1f),
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
private fun AccountLoginWebView(
    reloadKey: Int,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val loginUrl = "https://passport.weibo.cn/signin/login"
    val loginUserAgent =
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 " +
            "(KHTML, like Gecko) Chrome/126.0.0.0 Safari/537.36"
    val webView = remember(reloadKey) {
        WebView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
            )
            setLayerType(View.LAYER_TYPE_HARDWARE, null)
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
            webViewClient = object : WebViewClient() {}
            loadUrl(loginUrl)
        }
    }

    DisposableEffect(webView) {
        onDispose {
            webView.stopLoading()
            webView.webViewClient = WebViewClient()
            webView.destroy()
        }
    }

    AndroidView(
        modifier = modifier,
        factory = { webView },
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
            onAspectRatio = {},
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
                    loadRemoteBitmap(
                        url = previewUrl,
                        maxDecodeDim = maxDecodeDim,
                        connectTimeoutMs = 5000,
                        readTimeoutMs = 5000,
                    )
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
    limitConcurrency: Boolean = false,
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

    val imageBitmap = remember(bitmap) { bitmap?.takeIfDrawable()?.asImageBitmap() }

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
    limitConcurrency: Boolean = false,
) {
    var drawable by remember(url) { mutableStateOf<Drawable?>(null) }
    var failed by remember(url) { mutableStateOf(false) }

    LaunchedEffect(url) {
        drawable = null
        failed = false
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
            .onFailure { failed = true }
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
    val bytes = URL(url).openConnection().apply {
        (this as HttpURLConnection).connectTimeout = connectTimeoutMs
        readTimeout = readTimeoutMs
        setRequestProperty("User-Agent", DESKTOP_CHROME_USER_AGENT)
        setRequestProperty("Referer", "https://weibo.com/")
    }.inputStream.use { readRemoteBytesLimited(it, maxReadBytes) }
    RemoteBytesCache.put(url, bytes)
    RemoteDiskBytesCache.put(url, bytes)
    return bytes
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
            FullscreenBitmapCache.put(url, bitmap)
            return bitmap
        }
    }
    return null
}

private fun loadFullscreenBitmap(image: FeedImage): android.graphics.Bitmap? {
    val candidates = fullscreenImageUrlCandidates(image)
    candidates.forEach { url ->
        FullscreenBitmapCache.get(url)?.takeIf { it.isFullscreenQuality(image) }?.let { return it }
    }
    candidates.forEach { url ->
        runCatching {
            val bytes = fetchRemoteBytes(
                url = url,
                connectTimeoutMs = 10_000,
                readTimeoutMs = 20_000,
                maxReadBytes = remoteReadLimitForDecodeDim(4096),
            )
            decodeBitmapFromBytes(bytes, 4096)
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

@Composable
private fun videoControlFixedSp(size: Int): TextUnit {
    val fontScale = LocalDensity.current.fontScale
    return (size / fontScale).sp
}

@Composable
private fun videoControlTextStyle(sizeSp: Int): TextStyle = TextStyle(
    fontSize = videoControlFixedSp(sizeSp),
    lineHeight = videoControlFixedSp(sizeSp),
    platformStyle = PlatformTextStyle(includeFontPadding = false),
)

private const val DESKTOP_CHROME_USER_AGENT =
    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 " +
        "(KHTML, like Gecko) Chrome/126.0.0.0 Safari/537.36"
