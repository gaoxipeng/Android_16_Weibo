package com.example.myweibo.data

object WeiboEndpoints {
    const val CONFIG = "/ajax/config"
    const val FOLLOWING = "/ajax/feed/friendstimeline"
    const val GROUP_TIMELINE = "/ajax/feed/groupstimeline"
    const val HOT_TIMELINE = "/ajax/feed/hottimeline"
    const val ALL_GROUPS = "/ajax/feed/allGroups"
    const val FAVORITES = "/ajax/favorites/all_fav"
    const val FAVORITE_CREATE = "/ajax/favorites/create"
    const val FAVORITE_DESTROY = "/ajax/favorites/destroy"
    const val STATUS_FAVORITE_CREATE = "/ajax/statuses/createFavorites"
    const val STATUS_FAVORITE_DESTROY = "/ajax/statuses/destoryFavorites"
    const val LIKED_STATUSES = "/ajax/statuses/likelist"
    const val STATUS_COMMENTS = "/ajax/statuses/buildComments"
    const val COMMENT_CREATE = "/ajax/comments/create"
    const val COMMENT_REPLY = "/ajax/comments/reply"
    const val STATUS_LONG_TEXT = "/ajax/statuses/longtext"
    const val STATUS_DETAIL = "/ajax/statuses/show"
    const val STATUS_REPOST_TIMELINE = "/ajax/statuses/repostTimeline"
    const val PROFILE_INFO = "/ajax/profile/info"
    const val PROFILE_DETAIL = "/ajax/profile/detail"
    const val USER_TIMELINE = "/ajax/statuses/mymblog"
    const val USER_SEARCH_BLOG = "/ajax/profile/searchblog"
    const val PROFILE_IMAGE_WALL = "/ajax/profile/getImageWall"
    const val PROFILE_ALBUM_WATERFALL = "/ajax/profile/getWaterFallContent"
    const val FOLLOW_CREATE = "/ajax/friendships/create"
    const val FOLLOW_DESTROY = "/ajax/friendships/destory"
    const val FRIENDS = "/ajax/friendships/friends"
    const val SET_LIKE = "/ajax/statuses/setLike"
    const val CANCEL_LIKE = "/ajax/statuses/cancelLike"
    const val STATUS_DESTROY = "/ajax/statuses/destroy"
    const val SEARCH_BAND = "/ajax/side/searchBand"
    const val PERSONAL_SEARCH_BAND = "/ajax/statuses/mineBand"
    const val SEARCH_SIDE = "/ajax/side/search"

    fun timelinePath(kind: TimelineKind): String =
        when (kind) {
            TimelineKind.Following -> FOLLOWING
            TimelineKind.FriendsCircle, TimelineKind.SpecialFollowing -> GROUP_TIMELINE
            TimelineKind.Favorites -> FAVORITES
            TimelineKind.Liked -> LIKED_STATUSES
            TimelineKind.Hot, TimelineKind.Nearby -> HOT_TIMELINE
        }
}
