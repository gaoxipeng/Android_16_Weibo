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
    val emoticons: Map<String, String> = emptyMap(),
    val repostsCount: String,
    val commentsCount: String,
    val likesCount: String,
    val images: List<FeedImage>,
    val media: FeedMedia?,
    val retweetedStatus: FeedItem? = null
)

data class FeedImage(
    val id: String,
    val thumbnailUrl: String,
    val largeUrl: String,
    val downloadUrls: List<String> = emptyList(),
    val livePhotoVideoUrl: String? = null,
    val createdAt: String? = null,
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
    val downloadUrl: String? = null
)

enum class MediaType {
    Video,
    Live,
    Audio
}

data class CommentItem(
    val id: String,
    val authorName: String,
    val authorAvatarUrl: String?,
    val text: String,
    val createdAt: String?,
    val likesCount: String,
    val ipLocation: String? = null,
    val emoticons: Map<String, String> = emptyMap(),
    val comments: List<CommentItem> = emptyList(),
    val replyToAuthor: String? = null,
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
    val coverUrl: String? = null,
)

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
