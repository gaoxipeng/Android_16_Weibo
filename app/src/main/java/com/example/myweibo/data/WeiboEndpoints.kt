package com.example.myweibo.data

object WeiboEndpoints {
    const val CONFIG = "/ajax/config"
    const val FOLLOWING = "/ajax/feed/friendstimeline"
    const val GROUP_TIMELINE = "/ajax/feed/groupstimeline"
    const val STATUS_COMMENTS = "/ajax/statuses/buildComments"
    const val COMMENT_CREATE = "/ajax/comments/create"
    const val COMMENT_REPLY = "/ajax/comments/reply"
    const val STATUS_LONG_TEXT = "/ajax/statuses/longtext"
    const val STATUS_DETAIL = "/ajax/statuses/show"
    const val STATUS_REPOST_TIMELINE = "/ajax/statuses/repostTimeline"
    const val PROFILE_INFO = "/ajax/profile/info"
    const val PROFILE_DETAIL = "/ajax/profile/detail"
    const val USER_TIMELINE = "/ajax/statuses/mymblog"
    const val PROFILE_IMAGE_WALL = "/ajax/profile/getImageWall"
    const val PROFILE_ALBUM_WATERFALL = "/ajax/profile/getWaterFallContent"
    const val FOLLOW_CREATE = "/ajax/friendships/create"
    const val FOLLOW_DESTROY = "/ajax/friendships/destory"
    const val FRIENDS = "/ajax/friendships/friends"
    const val SET_LIKE = "/ajax/statuses/setLike"
    const val CANCEL_LIKE = "/ajax/statuses/cancelLike"
    const val SEARCH_BAND = "/ajax/side/searchBand"
    const val SEARCH_SIDE = "/ajax/side/search"

    fun timelinePath(kind: TimelineKind): String =
        when (kind) {
            TimelineKind.Following -> FOLLOWING
            TimelineKind.FriendsCircle -> GROUP_TIMELINE
        }
}
