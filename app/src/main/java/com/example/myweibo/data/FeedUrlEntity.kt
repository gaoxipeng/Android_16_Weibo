package com.example.myweibo.data

import androidx.compose.runtime.Immutable

@Immutable
data class FeedUrlEntity(
    val shortUrl: String,
    val title: String,
    val url: String,
)
