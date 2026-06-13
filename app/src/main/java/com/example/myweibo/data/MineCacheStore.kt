package com.example.myweibo.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class MineCacheStore(context: Context) {
    private val profileFile = File(context.filesDir, "mine_profile_cache.json")
    private val postsFile = File(context.filesDir, "mine_posts_cache.json")
    private val albumFile = File(context.filesDir, "mine_album_cache.json")

    suspend fun readProfile(): UserProfile? =
        withContext(Dispatchers.IO) {
            runCatching {
                if (!profileFile.exists()) return@withContext null
                profileFromJson(JSONObject(profileFile.readText(Charsets.UTF_8)))
            }.getOrNull()
        }

    suspend fun writeProfile(profile: UserProfile) {
        withContext(Dispatchers.IO) {
            runCatching { profileFile.writeText(profile.toJson().toString(), Charsets.UTF_8) }
        }
    }

    suspend fun readPosts(): MinePostsCache? =
        withContext(Dispatchers.IO) {
            runCatching {
                if (!postsFile.exists()) return@withContext null
                val root = JSONObject(postsFile.readText(Charsets.UTF_8))
                MinePostsCache(
                    items = root.optJSONArray("items").toFeedItems(),
                    page = root.optInt("page", 1).coerceAtLeast(1),
                    hasMore = root.optBoolean("has_more", true),
                )
            }.getOrNull()
        }

    suspend fun writePosts(cache: MinePostsCache) {
        withContext(Dispatchers.IO) {
            runCatching {
                postsFile.writeText(
                    JSONObject()
                        .put("page", cache.page)
                        .put("has_more", cache.hasMore)
                        .put("items", cache.items.toFeedItemsJsonArray())
                        .toString(),
                    Charsets.UTF_8,
                )
            }
        }
    }

    suspend fun readAlbum(): AlbumPage? =
        withContext(Dispatchers.IO) {
            runCatching {
                if (!albumFile.exists()) return@withContext null
                val root = JSONObject(albumFile.readText(Charsets.UTF_8))
                AlbumPage(
                    images = root.optJSONArray("images").toFeedImages(),
                    nextCursor = root.optNullableString("next_cursor"),
                )
            }.getOrNull()
        }

    suspend fun writeAlbum(page: AlbumPage) {
        withContext(Dispatchers.IO) {
            runCatching {
                albumFile.writeText(
                    JSONObject()
                        .put("next_cursor", page.nextCursor)
                        .put("images", page.images.toFeedImagesJsonArray())
                        .toString(),
                    Charsets.UTF_8,
                )
            }
        }
    }

    private fun UserProfile.toJson(): JSONObject =
        JSONObject()
            .put("id", id)
            .put("screen_name", screenName)
            .put("avatar_url", avatarUrl)
            .put("description", description)
            .put("location", location)
            .put("verified_reason", verifiedReason)
            .put("following_count", followingCount)
            .put("followers_count", followersCount)
            .put("statuses_count", statusesCount)
            .put("photos_count", photosCount)
            .put("cover_urls", JSONArray(coverUrls))
            .put("cover_url", coverUrl)

    private fun profileFromJson(json: JSONObject): UserProfile {
        val coverUrls = json.optJSONArray("cover_urls")?.let { array ->
            buildList {
                for (index in 0 until array.length()) {
                    array.optString(index).takeIf { it.isNotBlank() }?.let(::add)
                }
            }
        } ?: json.optNullableString("cover_url")?.let { listOf(it) } ?: emptyList()
        return UserProfile(
            id = json.optString("id"),
            screenName = json.optString("screen_name", "\u5FAE\u535A\u7528\u6237"),
            avatarUrl = json.optNullableString("avatar_url"),
            description = json.optNullableString("description"),
            location = json.optNullableString("location"),
            verifiedReason = json.optNullableString("verified_reason"),
            followingCount = json.optString("following_count", "--"),
            followersCount = json.optString("followers_count", "--"),
            statusesCount = json.optString("statuses_count", "--"),
            photosCount = json.optNullableString("photos_count"),
            coverUrls = coverUrls,
        )
    }

    private fun List<FeedItem>.toFeedItemsJsonArray(): JSONArray =
        JSONArray().also { array -> forEach { array.put(it.toJson()) } }

    private fun FeedItem.toJson(): JSONObject =
        JSONObject()
            .put("id", id)
            .put("status_id", statusId)
            .put("author_id", authorId)
            .put("author_name", authorName)
            .put("author_avatar_url", authorAvatarUrl)
            .put("created_at", createdAt)
            .put("source", source)
            .put("ip_location", ipLocation)
            .put("text", text)
            .put("is_long_text", isLongText)
            .put("emoticons", JSONObject(emoticons))
            .put("reposts_count", repostsCount)
            .put("comments_count", commentsCount)
            .put("likes_count", likesCount)
            .put("liked", liked)
            .put("is_edited", isEdited)
            .put("edit_count", editCount)
            .put("images", images.toFeedImagesJsonArray())
            .put("media", media?.toJson())
            .put("retweeted_status", retweetedStatus?.toJson())

    private fun JSONArray?.toFeedItems(): List<FeedItem> {
        if (this == null) return emptyList()
        return buildList {
            for (index in 0 until length()) {
                optJSONObject(index)?.let { add(it.toFeedItem()) }
            }
        }
    }

    private fun JSONObject.toFeedItem(): FeedItem =
        FeedItem(
            id = optString("id"),
            statusId = optString("status_id"),
            authorId = optString("author_id"),
            authorName = optString("author_name", "\u5FAE\u535A\u7528\u6237"),
            authorAvatarUrl = optNullableString("author_avatar_url"),
            createdAt = optNullableString("created_at"),
            source = optNullableString("source"),
            ipLocation = optNullableString("ip_location"),
            text = optString("text"),
            isLongText = optBoolean("is_long_text"),
            emoticons = optJSONObject("emoticons").toStringMap(),
            repostsCount = optString("reposts_count", "0"),
            commentsCount = optString("comments_count", "0"),
            likesCount = optString("likes_count", "0"),
            liked = optBoolean("liked"),
            isEdited = optBoolean("is_edited"),
            editCount = optInt("edit_count"),
            images = optJSONArray("images").toFeedImages(),
            media = optJSONObject("media")?.toFeedMedia(),
            retweetedStatus = optJSONObject("retweeted_status")?.toFeedItem(),
        )

    private fun List<FeedImage>.toFeedImagesJsonArray(): JSONArray =
        JSONArray().also { array -> forEach { array.put(it.toJson()) } }

    private fun FeedImage.toJson(): JSONObject =
        JSONObject()
            .put("id", id)
            .put("thumbnail_url", thumbnailUrl)
            .put("large_url", largeUrl)
            .put("download_urls", JSONArray(downloadUrls))
            .put("live_photo_video_url", livePhotoVideoUrl)
            .put("created_at", createdAt)
            .put("status_id", statusId)
            .put("width", width)
            .put("height", height)

    private fun JSONArray?.toFeedImages(): List<FeedImage> {
        if (this == null) return emptyList()
        return buildList {
            for (index in 0 until length()) {
                val item = optJSONObject(index) ?: continue
                add(
                    FeedImage(
                        id = item.optString("id"),
                        thumbnailUrl = item.optString("thumbnail_url"),
                        largeUrl = item.optString("large_url"),
                        downloadUrls = item.optJSONArray("download_urls").toStringList(),
                        livePhotoVideoUrl = item.optNullableString("live_photo_video_url"),
                        createdAt = item.optNullableString("created_at"),
                        statusId = item.optNullableString("status_id"),
                        width = item.optInt("width").takeIf { it > 0 },
                        height = item.optInt("height").takeIf { it > 0 },
                    )
                )
            }
        }
    }

    private fun FeedMedia.toJson(): JSONObject =
        JSONObject()
            .put("type", type.name)
            .put("title", title)
            .put("cover_url", coverUrl)
            .put("stream_url", streamUrl)
            .put("download_url", downloadUrl)
            .put("duration_seconds", durationSeconds)

    private fun JSONObject.toFeedMedia(): FeedMedia? {
        val streamUrl = optNullableString("stream_url") ?: return null
        return FeedMedia(
            type = runCatching { MediaType.valueOf(optString("type")) }.getOrDefault(MediaType.Video),
            title = optString("title", "\u5FAE\u535A\u89C6\u9891"),
            coverUrl = optNullableString("cover_url"),
            streamUrl = streamUrl,
            downloadUrl = optNullableString("download_url"),
            durationSeconds = optInt("duration_seconds").takeIf { it > 0 },
        )
    }

    private fun JSONObject?.toStringMap(): Map<String, String> {
        if (this == null) return emptyMap()
        return buildMap {
            val keys = keys()
            while (keys.hasNext()) {
                val key = keys.next()
                optNullableString(key)?.let { put(key, it) }
            }
        }
    }

    private fun JSONArray?.toStringList(): List<String> {
        if (this == null) return emptyList()
        return buildList {
            for (index in 0 until length()) {
                optString(index).takeIf { it.isNotBlank() }?.let(::add)
            }
        }
    }

    private fun JSONObject.optNullableString(name: String): String? {
        if (!has(name) || isNull(name)) return null
        return opt(name)?.toString()?.takeIf { it.isNotBlank() && it != "null" }
    }
}
