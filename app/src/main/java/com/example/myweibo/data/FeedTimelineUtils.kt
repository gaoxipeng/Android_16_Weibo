package com.example.myweibo.data

private const val WEIBO_MID_EPOCH_OFFSET = 515483463L

fun feedItemSortKey(item: FeedItem): Long {
    parseWeiboCreatedAtMillis(item.createdAt)?.let { return it }
    return weiboStatusIdToMillis(item.statusId)
        ?: weiboStatusIdToMillis(item.id)
        ?: 0L
}

fun sortFeedTimelineItems(items: List<FeedItem>): List<FeedItem> =
    items.sortedByDescending(::feedItemSortKey)

fun mergeFeedTimelinePages(
    current: List<FeedItem>,
    incoming: List<FeedItem>,
): Pair<List<FeedItem>, Int> {
    val seenIds = current.map { it.id }.toMutableSet()
    val seenStatusIds = current.map { it.statusId }.filter { it.isNotBlank() }.toMutableSet()
    val appended = mutableListOf<FeedItem>()
    for (item in incoming) {
        if (item.id in seenIds) continue
        if (item.statusId.isNotBlank() && item.statusId in seenStatusIds) continue
        seenIds.add(item.id)
        if (item.statusId.isNotBlank()) seenStatusIds.add(item.statusId)
        appended.add(item)
    }
    return (current + appended) to appended.size
}

/** 合并当前微博及整条转发链上的内联表情映射。 */
fun FeedItem.collectEmoticons(): Map<String, String> {
    var map = emoticons
    var node: FeedItem? = retweetedStatus
    while (node != null) {
        map = map + node.emoticons
        node = node.retweetedStatus
    }
    return map
}

private fun weiboStatusIdToMillis(id: String): Long? {
    val numericId = id.trim().toLongOrNull() ?: return null
    val seconds = (numericId shr 22) + WEIBO_MID_EPOCH_OFFSET
    if (seconds < 1_000_000_000L || seconds > 4_102_444_800L) return null
    return seconds * 1000
}
