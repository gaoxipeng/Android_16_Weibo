package com.example.myweibo.data

/** Insert the gap immediately before the post at [insertionIndex] in the visible list. */
data class FeedTimelineGap(val insertionIndex: Int, val hiddenPostCount: Int)

data class FeedTimelineRefreshProjection(
    val visibleItems: List<FeedItem>,
    val gap: FeedTimelineGap?,
    val previousHeadIndex: Int? = null,
    val newPostsMarkerIndex: Int? = null,
) {
    val dividerIndex: Int? get() = gap?.insertionIndex ?: newPostsMarkerIndex
    val previousHeadLazyIndex: Int? get() = previousHeadIndex?.let(::lazyIndexOfPost)

    fun lazyIndexOfPost(postIndex: Int): Int =
        postIndex + if (dividerIndex?.let { postIndex >= it } == true) 1 else 0
}

/**
 * Merge refresh results into the full in-memory timeline (not its visible projection). Ordinary
 * posts should be latest-first; existing pins need not be in chronological order. Like pagination,
 * match by item ID or nonblank status ID. Refresh the matched payload without changing its row
 * ID, pin treatment, or position relative to other existing rows.
 *
 * A shared unpinned post anchors the order when timestamps cannot settle it. Pinned rows can
 * move in a refresh response, so they aren't overlap anchors. A dated older row never moves
 * ahead of a dated newer row based on response position alone. Nothing is inferred about
 * posts missing from the response.
 */
fun mergeFeedTimelineRefresh(current: List<FeedItem>, incoming: List<FeedItem>): List<FeedItem> {
    val existing = FeedRefreshIndex()
    val previous = current.filter { existing.add(it) }
    val page = FeedRefreshIndex().let { index -> incoming.filter { index.add(it) } }
    if (previous.isEmpty()) return page
    if (page.isEmpty()) return previous

    val refreshed = previous.toMutableList()
    val updated = BooleanArray(previous.size)
    for (item in page) {
        val index = existing.indexOf(item) ?: continue
        if (!updated[index]) {
            refreshed[index] = mergeRefreshedFeedItem(previous[index], item)
            updated[index] = true
        }
    }

    val pinned = page.filter { it.isPinned && existing.indexOf(it) == null }
    val chronological = page.filterNot { it.isPinned }
    val matches = chronological.map(existing::indexOf)
    val anchors = matches.map { match -> match?.takeUnless { previous[it].isPinned } }
    val nextAnchor = IntArray(chronological.size)
    var upper = previous.size
    for (i in chronological.indices.reversed()) {
        nextAnchor[i] = upper
        anchors[i]?.let { upper = it }
    }

    val merged = mutableListOf<FeedItem>()
    val previousKeys = previous.map(::feedItemSortKey)
    var oldIndex = 0
    for ((i, item) in chronological.withIndex()) {
        if (matches[i] != null) {
            anchors[i]?.let { anchor ->
                while (oldIndex <= anchor) merged.add(refreshed[oldIndex++])
            }
            continue
        }
        val key = feedItemSortKey(item)
        while (oldIndex < previous.size) {
            if (!previous[oldIndex].isPinned) {
                val oldKey = previousKeys[oldIndex]
                val insertBefore = if (key != 0L && oldKey != 0L && key != oldKey) {
                    key > oldKey
                } else {
                    oldIndex >= nextAnchor[i]
                }
                if (insertBefore) break
            }
            merged.add(refreshed[oldIndex++])
        }
        merged.add(item)
    }
    while (oldIndex < previous.size) merged.add(refreshed[oldIndex++])

    // Never use a pin's response position to place it or other posts in chronological history.
    for ((item, key) in pinned.map { it to feedItemSortKey(it) }.sortedByDescending { it.second }) {
        val index = if (key == 0L) -1 else merged.indexOfFirst {
            if (it.isPinned) false else feedItemSortKey(it).let { oldKey -> oldKey != 0L && oldKey < key }
        }
        if (index == -1) merged.add(item) else merged.add(index, item)
    }
    return merged
}

/** Replace the first page in server order, without remounting overlapping rows or downgrading their bodies. */
internal fun replaceFeedTimelineRefresh(current: List<FeedItem>, incoming: List<FeedItem>): List<FeedItem> {
    if (current.isEmpty()) return incoming
    val existing = FeedRefreshIndex()
    val previous = current.filter { existing.add(it) }
    val emitted = FeedRefreshIndex()
    return incoming.mapNotNull { fresh ->
        val known = existing.indexOf(fresh)?.let(previous::get)
        val row = if (known != null) mergeRefreshedFeedItem(known, fresh, retainPin = false) else fresh
        row.takeIf { emitted.add(it) }
    }
}

/** Detail/full-text fallbacks may return aliases; payload updates must not replace home row owners. */
internal fun FeedItem.withHomeRowIdentity(row: FeedItem): FeedItem {
    val original = retweetedStatus
    val known = row.retweetedStatus
    val quote = if (original != null && known != null && known.matchesRefreshStatus(original)) {
        original.withHomeRowIdentity(known)
    } else original
    return if (id == row.id && quote === original) this else copy(id = row.id, retweetedStatus = quote)
}

private fun FeedItem.matchesRefreshStatus(other: FeedItem): Boolean =
    id == other.id || (statusId.isNotBlank() && statusId == other.statusId)

/**
 * Capture [previousHeadId] from the first non-pinned post in the full timeline immediately
 * before an explicit refresh. Keep the confirmed batch through automatic updates and pagination;
 * replace it only on the next confirmed explicit refresh or when the feed resets. A null, pinned,
 * or missing boundary shows everything; don't substitute a later post from the merged list.
 *
 * [items] is the full merged timeline. Only non-pinned posts before the boundary that aren't
 * known to be older count as new. Keep the latest [maxVisibleNewPosts] of those posts. Neither
 * pinned posts, the boundary post, nor its older history is hidden. Pass the full list to pagination.
 * The boundary gets a static marker only when there are confirmed new posts and no overflow. Once
 * the user reveals a fold, posts retain their original order and the divider is removed for that batch.
 */
fun projectFeedTimelineRefresh(
    items: List<FeedItem>,
    previousHeadId: String?,
    maxVisibleNewPosts: Int,
    gapExpanded: Boolean = false,
    confirmedNewRowIds: Set<String>? = null,
): FeedTimelineRefreshProjection {
    val boundary = items.indexOfFirst { it.id == previousHeadId }
    if (boundary < 0 || items[boundary].isPinned) return FeedTimelineRefreshProjection(items, gap = null)

    if (gapExpanded) return FeedTimelineRefreshProjection(items, gap = null, previousHeadIndex = boundary)

    val boundaryKey = feedItemSortKey(items[boundary])
    val limit = maxVisibleNewPosts.coerceAtLeast(0)
    var newCount = 0
    val visiblePrefix = items.subList(0, boundary).filter { item ->
        if (item.isPinned || confirmedNewRowIds?.contains(item.id) == false) return@filter true
        val key = feedItemSortKey(item)
        if (key != 0L && boundaryKey != 0L && key < boundaryKey) return@filter true
        ++newCount <= limit
    }
    if (newCount <= limit) {
        return FeedTimelineRefreshProjection(
            visibleItems = items,
            gap = null,
            previousHeadIndex = boundary,
            newPostsMarkerIndex = boundary.takeIf { newCount > 0 },
        )
    }

    return FeedTimelineRefreshProjection(
        visibleItems = visiblePrefix + items.drop(boundary),
        gap = FeedTimelineGap(insertionIndex = visiblePrefix.size, hiddenPostCount = newCount - limit),
        previousHeadIndex = visiblePrefix.size,
    )
}

private fun mergeRefreshedFeedItem(
    current: FeedItem,
    incoming: FeedItem,
    retainPin: Boolean = true,
): FeedItem {
    val retweet = incoming.retweetedStatus?.let { fresh ->
        val known = current.retweetedStatus
        if (known != null && known.matchesRefreshStatus(fresh)) {
            mergeRefreshedFeedItem(known, fresh, retainPin)
        } else fresh
    }
    val updated = incoming.copy(
        id = current.id,
        // The parser falls back to the row ID when mblogid is omitted.
        statusId = incoming.statusId.takeUnless { it.isBlank() || it == incoming.id }
            ?: current.statusId.ifBlank { incoming.statusId },
        createdAt = incoming.createdAt ?: current.createdAt,
        isPinned = if (retainPin) current.isPinned else incoming.isPinned,
        retweetedStatus = retweet,
    )
    // Long-text expansion clears both flags and adds body-bound links, images, and emoticons.
    // Don't downgrade that body to a preview unless the server reports a newer edit.
    val keepBody = (incoming.requiresLongTextFetch || incoming.isLongText) &&
        !current.requiresLongTextFetch && !current.isLongText && current.text.isNotBlank() &&
        incoming.editCount <= current.editCount
    if (!keepBody) return updated
    return updated.copy(
        text = current.text,
        isLongText = false,
        requiresLongTextFetch = false,
        emoticons = current.emoticons,
        images = current.images,
        inlineImageLinks = current.inlineImageLinks,
        urlEntities = current.urlEntities,
    )
}

private class FeedRefreshIndex {
    private val ids = mutableMapOf<String, Int>()
    private val statusIds = mutableMapOf<String, Int>()

    fun indexOf(item: FeedItem): Int? = ids[item.id] ?: statusIds[item.statusId]

    fun add(item: FeedItem): Boolean {
        if (indexOf(item) != null) return false
        val index = ids.size
        ids[item.id] = index
        if (item.statusId.isNotBlank()) statusIds[item.statusId] = index
        return true
    }
}
