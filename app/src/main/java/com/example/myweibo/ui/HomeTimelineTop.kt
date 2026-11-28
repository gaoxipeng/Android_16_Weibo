package com.example.myweibo.ui

import androidx.compose.foundation.lazy.LazyListLayoutInfo
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

private const val HomeTimelineJumpTimeoutMillis = 450L

/** Jump locally to the first actual post, not a synthetic row in the folded projection. */
internal fun HomeTimelineReadingState.goToTimelineTop(listState: LazyListState) {
    val firstPost = items.firstOrNull() ?: return
    if (!listState.isScrollInProgress && listState.isMeasuredAtPostTop(listState.layoutInfo, firstPost.id)) return
    longPostController(listState).suppressPagingForLayoutChange()
    if (feedProjection.visibleItems.firstOrNull()?.id != firstPost.id) {
        expandTimelineGap(listState, preserveViewport = false)
    }
    listState.requestScrollToItem(feedProjection.lazyIndexOfPost(0), 0)
}

/** Land on the current first post before starting the same explicit refresh as a pull. */
internal fun HomeTimelineReadingState.goToTimelineTopAndRefresh(
    listState: LazyListState,
    scope: CoroutineScope,
    refresh: () -> Unit,
) {
    val request = timelineRequestGeneration
    val session = timelineCacheSessionGeneration
    val visit = explicitRefreshVisitGeneration
    fun isCurrent() = request == timelineRequestGeneration && session == timelineCacheSessionGeneration &&
        visit == explicitRefreshVisitGeneration

    scope.launch {
        if (!isCurrent()) return@launch
        if (items.isEmpty()) {
            // An empty timeline has nothing to land on; keep the explicit retry.
            refresh()
            return@launch
        }
        val landed = withTimeoutOrNull(HomeTimelineJumpTimeoutMillis) {
            while (isCurrent()) {
                val firstKey = items.firstOrNull()?.id ?: return@withTimeoutOrNull false
                val beforeJump = listState.layoutInfo
                // No jump is needed at an already measured head. Keep its known key intact.
                if (!listState.isScrollInProgress && listState.isMeasuredAtPostTop(beforeJump, firstKey)) {
                    return@withTimeoutOrNull true
                }
                goToTimelineTop(listState)
                // requestScrollToItem clears the last known key until the next measure. Wait for
                // the actual keyed row (not just index 0) so even a fast response retains the post.
                snapshotFlow {
                    val layout = listState.layoutInfo
                    !isCurrent() || items.firstOrNull()?.id != firstKey ||
                        (layout !== beforeJump && listState.isMeasuredAtPostTop(layout, firstKey))
                }.first { it }
                if (items.firstOrNull()?.id == firstKey) {
                    val layout = listState.layoutInfo
                    return@withTimeoutOrNull layout !== beforeJump && listState.isMeasuredAtPostTop(layout, firstKey)
                }
            }
            false
        }
        if (landed == true && isCurrent()) refresh()
    }
}

private fun LazyListState.isMeasuredAtPostTop(layout: LazyListLayoutInfo, key: String): Boolean =
    firstVisibleItemScrollOffset == 0 && layout.visibleItemsInfo.any {
        it.index == firstVisibleItemIndex && it.key == key && it.offset == 0
    }
