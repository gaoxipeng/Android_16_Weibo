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
    /** 微博发布时附带的 POI 地点，不同于用户 IP 属地。 */
    val locationName: String? = null,
    val text: String,
    val isLongText: Boolean = false,
    /** 仅用于详情页自动尝试全文接口；不决定是否展示「阅读全文」。 */
    val requiresLongTextFetch: Boolean = false,
    val emoticons: Map<String, String> = emptyMap(),
    val repostsCount: String,
    val commentsCount: String,
    val likesCount: String,
    val liked: Boolean = false,
    val images: List<FeedImage>,
    val medias: List<FeedMedia> = emptyList(),
    /** 转发评论等正文短链配图，展示为蓝色「查看图片」 */
    val inlineImageLinks: Map<String, List<FeedImage>> = emptyMap(),
    /** 正文中的可点击链接（文章、网页等） */
    val urlEntities: List<FeedUrlEntity> = emptyList(),
    val retweetedStatus: FeedItem? = null,
    /** 微博 web 接口常见字段：edit_count / edit_at / edited / is_edit */
    val isEdited: Boolean = false,
    val editCount: Int = 0,
) {
    val media: FeedMedia?
        get() = medias.firstOrNull()
}

data class FeedImage(
    val id: String,
    val thumbnailUrl: String,
    val largeUrl: String,
    val downloadUrls: List<String> = emptyList(),
    val livePhotoVideoUrl: String? = null,
    val videoStreamUrl: String? = null,
    val createdAt: String? = null,
    val statusId: String? = null,
    val width: Int? = null,
    val height: Int? = null,
    val type: String? = null,
    /** 相册/接口可能提供的拍摄时间，不等同于微博发布时间 */
    val shootTime: String? = null,
    val cameraMake: String? = null,
    val cameraModel: String? = null,
) {
    val isLivePhoto: Boolean
        get() = !livePhotoVideoUrl.isNullOrBlank() && !isGif
    val isGif: Boolean
        get() = type == "gif"
    val isAlbumVideo: Boolean
        get() = type == "video" && !videoStreamUrl.isNullOrBlank()
}

data class ImageSaveMetadata(
    val publishTime: String? = null,
    val publishSource: String? = null,
    val shootTime: String? = null,
    val cameraMake: String? = null,
    val cameraModel: String? = null,
    val statusId: String? = null,
    val authorName: String? = null,
)

private val sinaimgPidRegex =
    Regex("""/([^/?#]+)\.(?:jpg|jpeg|png|gif|webp)""", RegexOption.IGNORE_CASE)

private fun FeedImage.imageMatchKeys(): Set<String> {
    val keys = mutableSetOf<String>()
    sinaimgPidRegex.find(largeUrl)?.groupValues?.get(1)?.let(keys::add)
    sinaimgPidRegex.find(thumbnailUrl)?.groupValues?.get(1)?.let(keys::add)
    downloadUrls.forEach { url ->
        sinaimgPidRegex.find(url)?.groupValues?.get(1)?.let(keys::add)
    }
    if (id.isNotBlank() && !id.startsWith("http")) {
        keys += id.substringBeforeLast('.')
    }
    return keys
}

private fun FeedImage.refersToSamePhoto(other: FeedImage): Boolean {
    if (largeUrl == other.largeUrl || id == other.id) return true
    val keys = imageMatchKeys()
    val otherKeys = other.imageMatchKeys()
    return keys.isNotEmpty() && keys.any { it in otherKeys }
}

fun FeedImage.resolveForSave(
    status: FeedItem? = null,
    relatedImages: List<FeedImage> = emptyList(),
): FeedImage {
    if (isLivePhoto) return this
    val candidates = buildList {
        status?.images?.let(::addAll)
        status?.retweetedStatus?.images?.let(::addAll)
        addAll(relatedImages)
    }
    val match = candidates.firstOrNull { candidate ->
        candidate.isLivePhoto && refersToSamePhoto(candidate)
    } ?: candidates.firstOrNull { candidate ->
        !candidate.livePhotoVideoUrl.isNullOrBlank() && refersToSamePhoto(candidate)
    }
    val liveVideo = livePhotoVideoUrl ?: match?.livePhotoVideoUrl
    if (liveVideo.isNullOrBlank()) return this
    return copy(
        livePhotoVideoUrl = liveVideo,
        type = if (isGif) type else "livephoto",
        downloadUrls = (downloadUrls + (match?.downloadUrls ?: emptyList())).distinct(),
        width = width ?: match?.width,
        height = height ?: match?.height,
    )
}

fun FeedImage.buildSaveMetadata(status: FeedItem? = null): ImageSaveMetadata =
    ImageSaveMetadata(
        publishTime = status?.createdAt ?: createdAt,
        publishSource = status?.source?.takeIf { it.isNotBlank() },
        shootTime = shootTime ?: createdAt,
        cameraMake = cameraMake,
        cameraModel = cameraModel,
        statusId = statusId ?: status?.statusId,
        authorName = status?.authorName,
    )

fun FeedImage.toAlbumFeedMedia(): FeedMedia? {
    val streamUrl = videoStreamUrl?.takeIf { it.isNotBlank() } ?: return null
    return FeedMedia(
        type = MediaType.Video,
        title = "微博视频",
        coverUrl = thumbnailUrl.ifBlank { largeUrl }.takeIf { it.isNotBlank() },
        streamUrl = streamUrl,
        downloadUrl = downloadUrls.firstOrNull { it.contains(".mp4", ignoreCase = true) },
    )
}

data class AlbumPage(
    val images: List<FeedImage>,
    val nextCursor: String? = null,
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
    /** 视频方向：vertical / horizontal，来自 media_info.video_orientation */
    val videoOrientation: String? = null,
    /** 封面图宽度，来自 big_pic_info */
    val coverWidth: Int? = null,
    /** 封面图高度，来自 big_pic_info */
    val coverHeight: Int? = null,
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

fun FeedMedia.isSavableToAlbum(): Boolean =
    listOfNotNull(downloadUrl, streamUrl).any {
        it.isNotBlank() && !it.contains(".m3u8", ignoreCase = true)
    }

fun FeedItem.albumSaveEntryCount(images: List<FeedImage> = this.images): Int =
    images.size + medias.count { it.isSavableToAlbum() }

fun FeedItem.shouldOfferSaveAll(images: List<FeedImage> = this.images): Boolean =
    albumSaveEntryCount(images) > 1

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

data class RepostsPage(
    val items: List<CommentItem>,
    val nextPage: Int?,
    val totalCount: Int? = null,
)

data class UserProfile(
    val id: String,
    val screenName: String,
    val avatarUrl: String?,
    val description: String?,
    val location: String?,
    val ipLocation: String? = null,
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
    Following("\u6700\u65B0\u5FAE\u535A"),
    FriendsCircle("\u597D\u53CB\u5708"),
}

data class WeiboEmoticon(
    val phrase: String,
    val url: String,
)

data class MentionCandidate(
    val id: String,
    val name: String,
    val avatarUrl: String?,
)

data class MentionCandidateBundle(
    val avatarSuggestions: List<MentionCandidate>,
    val nameIndex: List<MentionCandidate>,
)

data class LikeUsersPage(
    val users: List<MentionCandidate>,
    val nextPage: Int? = null,
    val totalCount: Int? = null,
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
