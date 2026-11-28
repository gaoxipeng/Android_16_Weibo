package com.example.myweibo.data

/** An account ID here must already have been matched to the authenticated session. */
internal data class TimelineRefreshRoute private constructor(
    val accountId: String,
    val kind: TimelineKind,
    val groupId: String?,
) {
    companion object {
        fun of(verifiedAccountId: String?, kind: TimelineKind, groupId: String?): TimelineRefreshRoute? {
            val account = verifiedAccountId?.takeIf { id ->
                id.isNotEmpty() && id != "0" && id.all { it in '0'..'9' }
            } ?: return null
            if (groupId != null) {
                val group = groupId.trim().takeIf { it.isNotEmpty() && it != "0" } ?: return null
                return TimelineRefreshRoute(account, TimelineKind.SpecialFollowing, group)
            }
            return when (kind) {
                TimelineKind.Following, TimelineKind.FriendsCircle, TimelineKind.SpecialFollowing ->
                    TimelineRefreshRoute(account, kind, null)
                TimelineKind.Hot, TimelineKind.Nearby, TimelineKind.Favorites, TimelineKind.Liked -> null
            }
        }
    }
}

/** Row keys drive Compose; status IDs resolve the same post after aliases. */
internal data class TimelineRefreshPostIdentity(val rowId: String, val statusId: String?) {
    fun matches(item: FeedItem): Boolean = !item.isPinned &&
        (rowId == item.id || statusId != null && statusId == item.statusId)

    fun resolve(items: List<FeedItem>): FeedItem? = items.singleOrNull(::matches)

    companion object {
        fun from(item: FeedItem): TimelineRefreshPostIdentity? = item.id.takeIf { it.isNotBlank() && it != "0" }
            ?.let { id -> TimelineRefreshPostIdentity(id, item.statusId.takeIf { it.isNotBlank() && it != "0" }) }
    }
}

/** Standalone Special Following is not guaranteed to be chronological, unlike user groups. */
internal fun hasChronologicalResponse(items: List<FeedItem>): Boolean {
    val keys = items.asSequence().filterNot { it.isPinned }.map(::feedItemSortKey).toList()
    return keys.isNotEmpty() && keys.all { it > 0 } && keys.zipWithNext().all { (newer, older) -> newer >= older }
}

/** Null means unavailable or inconclusive; zero is a confirmed nonempty no-new result. */
internal fun countNewTimelinePosts(previous: List<FeedItem>, page: List<FeedItem>, ordered: Boolean): Int? {
    val head = previous.firstOrNull { !it.isPinned } ?: return null
    if (page.none { !it.isPinned }) return null
    val rowIds = previous.mapTo(HashSet()) { it.id }
    val statusIds = previous.mapNotNullTo(HashSet()) { TimelineRefreshPostIdentity.from(it)?.statusId }
    val headIdentity = TimelineRefreshPostIdentity.from(head)
    val headIndex = page.indexOfFirst { headIdentity?.matches(it) == true }
    val headTime = if (ordered) feedItemSortKey(head) else 0L
    var count = 0
    for ((index, item) in page.withIndex()) {
        val status = TimelineRefreshPostIdentity.from(item)?.statusId
        if (item.isPinned || item.id in rowIds || status?.let { it in statusIds } == true) continue
        rowIds += item.id
        if (status != null) statusIds += status
        val time = if (ordered) feedItemSortKey(item) else 0L
        val newer = when {
            !ordered -> true
            time > 0 && headTime > 0 && time != headTime -> time > headTime
            headIndex >= 0 -> index < headIndex
            // An unfamiliar, tied/undated row on a disjoint page proves neither that
            // the response added news nor that it had none. Do not display a zero.
            else -> return null
        }
        if (newer) count++
    }
    return count
}

/** Request-time head and the confirmed new posts, ordered newest to oldest. An empty
 * batch is a confirmed zero and clears any previous fold. This state is never persisted. */
internal data class ConfirmedTimelineRefresh(
    val baseline: TimelineRefreshPostIdentity,
    val newPosts: List<TimelineRefreshPostIdentity>,
)

/** A first page must actually reconnect to the request-time head; dates do not bridge a gap. */
internal fun classifyExplicitTimelineRefresh(
    previous: List<FeedItem>,
    page: List<FeedItem>,
    requiredSeams: List<Pair<TimelineRefreshPostIdentity, TimelineRefreshPostIdentity>> = emptyList(),
): ConfirmedTimelineRefresh? {
    val old = previous.filterNot { it.isPinned }
    val head = old.firstOrNull() ?: return null
    val baseline = TimelineRefreshPostIdentity.from(head) ?: return null
    val rowPositions = old.mapIndexed { index, item -> item.id to index }.toMap()
    val statusPositions = old.mapIndexedNotNull { index, item ->
        TimelineRefreshPostIdentity.from(item)?.statusId?.let { it to index }
    }.toMap()
    val knownRows = previous.mapTo(HashSet()) { it.id }
    val knownStatuses = previous.mapNotNullTo(HashSet()) { TimelineRefreshPostIdentity.from(it)?.statusId }
    val incoming = mutableListOf<FeedItem>()
    val statusesByRow = HashMap<String, String>()
    val seenRows = HashSet<String>()
    val seenStatuses = HashSet<String>()
    for (item in page) {
        if (item.isPinned) continue
        val status = TimelineRefreshPostIdentity.from(item)?.statusId
        if (status != null) {
            // Check every raw row, even aliases we later deduplicate by status ID.
            val previousStatus = statusesByRow.put(item.id, status)
            if (previousStatus != null && previousStatus != status) return null
        }
        if (item.id in seenRows || status != null && status in seenStatuses) continue
        seenRows += item.id
        if (status != null) seenStatuses += status
        incoming += item
    }
    val headIndex = incoming.indexOfFirst(baseline::matches)
    if (headIndex < 0) return null
    val headTime = feedItemSortKey(head)
    val batch = mutableListOf<TimelineRefreshPostIdentity>()
    var lastKnown = -1
    var lastTime = 0L
    for ((index, item) in incoming.withIndex()) {
        val identity = TimelineRefreshPostIdentity.from(item) ?: return null
        val rowPosition = rowPositions[item.id]
        val statusPosition = identity.statusId?.let(statusPositions::get)
        if (rowPosition != null && statusPosition != null && rowPosition != statusPosition) return null
        val known = rowPosition ?: statusPosition
        if (known != null) {
            if (known <= lastKnown || index < headIndex) return null
            lastKnown = known
        } else if (item.id in knownRows || identity.statusId?.let(knownStatuses::contains) == true) {
            // A known pin changing treatment doesn't become a fresh ordinary post.
            if (index < headIndex) return null
            continue
        }
        val time = feedItemSortKey(item)
        if (time > 0) {
            if (lastTime > 0 && time > lastTime) return null
            lastTime = time
        }
        if (index < headIndex) {
            // An exact ordered head overlap establishes which side of a timestamp tie this row belongs on.
            if (time > 0 && headTime > 0 && time < headTime) return null
            batch += identity
        } else if (index > headIndex && known == null && time > 0 && headTime > 0 && time > headTime) {
            return null
        }
    }
    if (requiredSeams.any { (newer, older) ->
            val newerIndex = incoming.indexOfFirst(newer::matches)
            newerIndex < 0 || incoming.indexOfFirst(older::matches) <= newerIndex
        }) return null
    return ConfirmedTimelineRefresh(baseline, batch)
}
