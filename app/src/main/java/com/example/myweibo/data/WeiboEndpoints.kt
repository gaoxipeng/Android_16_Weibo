package com.example.myweibo.data

object WeiboEndpoints {
    const val CONFIG = "/ajax/config"
    const val FOR_YOU = "/ajax/feed/unreadfriendstimeline"
    const val FOLLOWING = "/ajax/feed/friendstimeline"
    const val STATUS_COMMENTS = "/ajax/statuses/buildComments"
    const val STATUS_LONG_TEXT = "/ajax/statuses/longtext"
    const val PROFILE_INFO = "/ajax/profile/info"
    const val USER_TIMELINE = "/ajax/statuses/mymblog"
    const val PROFILE_IMAGE_WALL = "/ajax/profile/getImageWall"
    const val PROFILE_ALBUM_DETAIL = "/ajax/profile/getAlbumDetail"
    const val PROFILE_ALBUM = "/ajax/profile/photoContents"
    const val PROFILE_PHOTOS = "/ajax/profile/photolist"

    fun timelinePath(kind: TimelineKind): String =
        when (kind) {
            TimelineKind.ForYou -> FOR_YOU
            TimelineKind.Following -> FOLLOWING
        }
}
