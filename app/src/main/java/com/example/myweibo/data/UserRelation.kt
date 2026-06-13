package com.example.myweibo.data

enum class FriendListTab(val label: String) {
    Following("关注"),
    Fans("粉丝"),
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
