package com.example.myweibo.data

object WeiboEndpoints {
    const val CONFIG = "/ajax/config"
    const val FOR_YOU = "/ajax/feed/unreadfriendstimeline"
    const val FOLLOWING = "/ajax/feed/friendstimeline"
    const val STATUS_COMMENTS = "/ajax/statuses/buildComments"
    const val STATUS_LONG_TEXT = "/ajax/statuses/longtext"
    const val STATUS_DETAIL = "/ajax/statuses/show"
    // 微博 web 端「查看编辑记录」对应的未公开接口，命名规律同 likelist / buildComments
    const val STATUS_EDIT_LIST = "/ajax/statuses/editlist"
    val STATUS_EDIT_HISTORY_CANDIDATES = listOf(
        STATUS_EDIT_LIST,
        "/ajax/statuses/editList",
        "/ajax/statuses/editHistory",
        "/ajax/statuses/editHistoryList",
        "/ajax/statuses/getEditHistory",
    )
    const val PROFILE_INFO = "/ajax/profile/info"
    const val USER_TIMELINE = "/ajax/statuses/mymblog"
    const val PROFILE_IMAGE_WALL = "/ajax/profile/getImageWall"
    const val FOLLOW_CREATE = "/ajax/friendships/create"
    const val FOLLOW_DESTROY = "/ajax/friendships/destory"
    const val SET_LIKE = "/ajax/statuses/setLike"
    const val CANCEL_LIKE = "/ajax/statuses/cancelLike"

    fun timelinePath(kind: TimelineKind): String =
        when (kind) {
            TimelineKind.ForYou -> FOR_YOU
            TimelineKind.Following -> FOLLOWING
        }
}
