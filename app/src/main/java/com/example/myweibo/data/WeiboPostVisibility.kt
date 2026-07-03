package com.example.myweibo.data

enum class WeiboPostVisibility(
    val apiValue: Int,
    val label: String,
    val subtitle: String,
) {
    Public(0, "公开", "所有人可见"),
    FriendsCircle(6, "好友圈", "相互关注好友可见"),
    Followers(10, "粉丝", "关注你的人可见"),
    Private(1, "仅自己可见", ""),
    ;
}
