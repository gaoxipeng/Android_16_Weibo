package com.example.myweibo.data

enum class FriendListTab(val label: String) {
    Fans("粉丝"),
    Following("关注"),
}

data class RelationUser(
    val id: String,
    val name: String,
    val screenName: String,
    val avatarUrl: String?,
    val avatarLarge: String?,
    val description: String?,
    val followersCount: String,
    val followersCountStr: String?,
    val friendsCount: String,
    val following: Boolean,
    val followMe: Boolean,
    val verified: Boolean,
    val verifiedReason: String?,
    val location: String?,
)

data class RelationPage(
    val items: List<RelationUser>,
    val hasNextPage: Boolean,
    val totalNumber: Int = 0,
    val errorMsg: String? = null,
)

fun RelationUser.toUserProfile(): UserProfile =
    UserProfile(
        id = id,
        screenName = screenName.ifBlank { name },
        avatarUrl = avatarLarge?.takeIf { it.isNotBlank() } ?: avatarUrl,
        description = description,
        location = location,
        verifiedReason = verifiedReason?.takeIf { verified },
        followingCount = friendsCount,
        followersCount = followersCountStr?.takeIf { it.isNotBlank() } ?: followersCount,
        statusesCount = "--",
        following = following,
        followMe = followMe,
    )

fun SearchUserItem.toRelationUser(): RelationUser {
    val displayFollowers = followersCount.trim()
    val followersLabel = displayFollowers.removeSuffix("粉丝").trim()
    val following = followText in setOf("已关注", "相互关注", "互相关注")
    val followMe = followText in setOf("相互关注", "互相关注")
    return RelationUser(
        id = id,
        name = name.ifBlank { screenName }.ifBlank { "微博用户" },
        screenName = screenName.ifBlank { name }.ifBlank { id },
        avatarUrl = avatarUrl,
        avatarLarge = avatarUrl,
        description = description.takeIf { it.isNotBlank() },
        followersCount = followersLabel.ifBlank { displayFollowers },
        followersCountStr = followersLabel.takeIf { it.isNotBlank() },
        friendsCount = "--",
        following = following,
        followMe = followMe,
        verified = false,
        verifiedReason = null,
        location = null,
    )
}
