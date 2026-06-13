package com.example.myweibo.data

data class TimelinePage(
    val items: List<FeedItem>,
    val nextCursor: String? = null
)

data class FeedItem(
    val id: String,
    val statusId: String,
    val authorId: String,
    val authorName: String,
    val authorAvatarUrl: String?,
    val createdAt: String?,
    val source: String?,
    val ipLocation: String?,
    val text: String,
    val isLongText: Boolean = false,
    val emoticons: Map<String, String> = emptyMap(),
    val repostsCount: String,
    val commentsCount: String,
    val likesCount: String,
    val liked: Boolean = false,
    val images: List<FeedImage>,
    val media: FeedMedia?,
    /** 转发评论等正文短链配图，展示为蓝色「查看图片」 */
    val inlineImageLinks: Map<String, List<FeedImage>> = emptyMap(),
    /** 正文中的可点击链接（文章、网页等） */
    val urlEntities: List<FeedUrlEntity> = emptyList(),
    val retweetedStatus: FeedItem? = null,
    /** 微博 web 接口常见字段：edit_count / edit_at / edited / is_edit */
    val isEdited: Boolean = false,
    val editCount: Int = 0,
)

data class FeedImage(
    val id: String,
    val thumbnailUrl: String,
    val largeUrl: String,
    val downloadUrls: List<String> = emptyList(),
    val livePhotoVideoUrl: String? = null,
    val createdAt: String? = null,
    val statusId: String? = null,
    val width: Int? = null,
    val height: Int? = null,
    val type: String? = null,
) {
    val isLivePhoto: Boolean
        get() = type == "livephoto" && livePhotoVideoUrl != null
    val isGif: Boolean
        get() = type == "gif"
}

data class AlbumPage(
    val images: List<FeedImage>,
    val nextCursor: String? = null
)

data class MinePostsCache(
    val items: List<FeedItem>,
    val page: Int = 1,
    val hasMore: Boolean = true,
)

data class FeedMedia(
    val type: MediaType,
    val title: String,
    val coverUrl: String?,
    val streamUrl: String,
    val downloadUrl: String? = null,
    /** 视频时长（秒），来自 page_info/media_info 的 duration 等字段 */
    val durationSeconds: Int? = null,
    /** 直播状态：1=直播中，3=回放，0=未开播 */
    val liveStatus: Int? = null,
    /** 直播回放地址 replay_hd */
    val replayUrl: String? = null,
) {
    fun resolvedPlaybackUrl(): String? = when {
        type != MediaType.Live -> streamUrl.takeIf { it.isNotBlank() }
        liveStatus == 3 -> replayUrl?.takeIf { it.isNotBlank() } ?: streamUrl.takeIf { it.isNotBlank() }
        liveStatus == 1 -> streamUrl.takeIf { it.isNotBlank() }
        liveStatus == null && !replayUrl.isNullOrBlank() -> replayUrl
        liveStatus == null -> streamUrl.takeIf { it.isNotBlank() }
        else -> null
    }

    fun isLiveBroadcast(): Boolean = type == MediaType.Live && (liveStatus == 1 || (liveStatus == null && replayUrl.isNullOrBlank()))

    fun isLiveReplay(): Boolean = type == MediaType.Live && (liveStatus == 3 || (liveStatus == null && !replayUrl.isNullOrBlank()))

    fun isLivePlayable(): Boolean =
        type == MediaType.Live && !resolvedPlaybackUrl().isNullOrBlank() && (liveStatus == null || liveStatus in setOf(1, 3))

    fun isStreamPlayable(): Boolean = when (type) {
        MediaType.Video -> streamUrl.isNotBlank()
        MediaType.Live -> isLivePlayable()
        else -> false
    }

    fun liveBadgeLabel(): String? = when {
        type != MediaType.Live -> null
        isLiveBroadcast() -> "直播"
        isLiveReplay() -> "回放"
        else -> "未开播"
    }
}

enum class MediaType {
    Video,
    Live,
    Audio
}

enum class CommentSort(val flow: String, val label: String) {
    Time("1", "按时间"),
    Hot("0", "按热度"),
    ;

    companion object {
        fun fromFlow(flow: String?): CommentSort =
            entries.firstOrNull { it.flow == flow } ?: Time
    }
}

data class CommentItem(
    val id: String,
    val authorId: String = "",
    val authorName: String,
    val authorAvatarUrl: String?,
    val text: String,
    val createdAt: String?,
    val likesCount: String,
    val ipLocation: String? = null,
    val emoticons: Map<String, String> = emptyMap(),
    val images: List<FeedImage> = emptyList(),
    val comments: List<CommentItem> = emptyList(),
    val replyToAuthor: String? = null,
    val replyToAuthorId: String? = null,
    val moreInfoText: String? = null,
    val nestedNextCursor: String? = null,
)

data class UserProfile(
    val id: String,
    val screenName: String,
    val avatarUrl: String?,
    val description: String?,
    val location: String?,
    val verifiedReason: String?,
    val followingCount: String,
    val followersCount: String,
    val statusesCount: String,
    val photosCount: String? = null,
    val coverUrls: List<String> = emptyList(),
    val following: Boolean = false,
    val followMe: Boolean = false,
) {
    val coverUrl: String?
        get() = coverUrls.firstOrNull()
}

enum class TimelineKind(val label: String) {
    ForYou("\u63A8\u8350"),
    Following("\u5173\u6CE8")
}

data class WeiboEmoticon(
    val phrase: String,
    val url: String,
)

sealed class ProfileLookup {
    data class Uid(val uid: String) : ProfileLookup()
    data class ScreenName(val screenName: String) : ProfileLookup()
}

data class NativeUiMessage(
    val title: String,
    val detail: String
)

data class HotSearchItem(
    val word: String,
    val label: String = "",
    val heat: Long = 0L,
)

data class SearchUserItem(
    val id: String,
    val screenName: String,
    val name: String,
    val avatarUrl: String?,
    val description: String,
    val followersCount: String,
    val followText: String = "",
)

data class SearchSuggestResult(
    val hotQueries: List<String> = emptyList(),
    val users: List<SearchUserItem> = emptyList(),
)

data class SearchUserPage(
    val items: List<SearchUserItem>,
    val nextCursor: String? = null,
)
