package com.example.myweibo.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class MentionSuggestionCacheStore(context: Context) {
    private val cacheFile = File(context.filesDir, "mention_suggestion_cache.json")

    suspend fun read(uid: String): MentionCandidateBundle? =
        withContext(Dispatchers.IO) {
            val targetUid = uid.trim()
            if (targetUid.isBlank()) return@withContext null
            runCatching {
                if (!cacheFile.exists()) return@runCatching null
                val root = JSONObject(cacheFile.readText(Charsets.UTF_8))
                val entry = root.optJSONObject(targetUid) ?: return@runCatching null
                entry.toMentionCandidateBundle()
            }.getOrNull()
        }

    suspend fun write(uid: String, bundle: MentionCandidateBundle) {
        val targetUid = uid.trim()
        if (targetUid.isBlank()) return
        withContext(Dispatchers.IO) {
            runCatching {
                val root = if (cacheFile.exists()) {
                    JSONObject(cacheFile.readText(Charsets.UTF_8))
                } else {
                    JSONObject()
                }
                root.put(targetUid, bundle.toJson())
                cacheFile.writeText(root.toString(), Charsets.UTF_8)
            }
        }
    }

    private fun MentionCandidateBundle.toJson(): JSONObject =
        JSONObject()
            .put("avatar_suggestions", avatarSuggestions.toJsonArray())
            .put("name_index", nameIndex.toJsonArray())

    private fun List<MentionCandidate>.toJsonArray(): JSONArray =
        JSONArray().also { array ->
            forEach { candidate ->
                array.put(
                    JSONObject()
                        .put("id", candidate.id)
                        .put("name", candidate.name)
                        .put("avatar_url", candidate.avatarUrl),
                )
            }
        }

    private fun JSONObject.toMentionCandidateBundle(): MentionCandidateBundle? {
        val avatarSuggestions = optJSONArray("avatar_suggestions").toMentionCandidates()
        val nameIndex = optJSONArray("name_index").toMentionCandidates()
        if (avatarSuggestions.isEmpty() && nameIndex.isEmpty()) return null
        return MentionCandidateBundle(
            avatarSuggestions = avatarSuggestions,
            nameIndex = nameIndex,
        )
    }

    private fun JSONArray?.toMentionCandidates(): List<MentionCandidate> {
        if (this == null) return emptyList()
        return buildList {
            for (index in 0 until length()) {
                val item = optJSONObject(index) ?: continue
                val name = item.optString("name").trim()
                if (!isValidMentionName(name)) continue
                add(
                    MentionCandidate(
                        id = item.optString("id").trim(),
                        name = name,
                        avatarUrl = item.optNullableString("avatar_url"),
                    ),
                )
            }
        }
    }

    private fun JSONObject.optNullableString(name: String): String? {
        if (!has(name) || isNull(name)) return null
        return opt(name)?.toString()?.trim()?.takeIf { it.isNotBlank() && it != "null" }
    }
}
