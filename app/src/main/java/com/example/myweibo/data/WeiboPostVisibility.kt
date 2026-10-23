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

    // 发布接口的好友圈值为 6；网页修改范围接口使用 2，不能混用。
    val modifyApiValue: Int get() = if (this == FriendsCircle) 2 else apiValue
    val scopeLabel: String get() = when (this) {
        Public -> "公开可见"
        FriendsCircle -> "仅好友可见"
        Followers -> "仅粉丝可见"
        Private -> "仅自己可见"
    }
    val changeLabel: String get() = when (this) {
        Public -> "设为公开可见"
        FriendsCircle -> "设为好友可见"
        Followers -> "设为粉丝可见"
        Private -> "设为自己可见"
    }

    companion object {
        fun fromApiValue(value: Int): WeiboPostVisibility? =
            if (value == 2) FriendsCircle else entries.firstOrNull { it.apiValue == value }
    }
}
