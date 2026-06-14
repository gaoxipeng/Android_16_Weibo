package com.example.myweibo.data

fun RelationUser.toMentionCandidate(): MentionCandidate? {
    val name = screenName.ifBlank { name }.removePrefix("@").trim()
    if (!isValidMentionName(name)) return null
    return MentionCandidate(
        id = id.trim(),
        name = name,
        avatarUrl = avatarLarge?.takeIf { it.isNotBlank() } ?: avatarUrl?.takeIf { it.isNotBlank() },
    )
}

private val blockedMentionNames = setOf(
    "关注",
    "粉丝",
    "消息",
    "私信",
    "互相关注",
    "相互关注",
    "已关注",
    "最新微博",
    "好友圈",
)

fun isValidMentionName(name: String): Boolean {
    if (name.isBlank() || name.length > 32) return false
    if (name in blockedMentionNames) return false
    if (name.all { it.isDigit() }) return false
    return true
}

fun MentionCandidate.isValidMentionCandidate(): Boolean = isValidMentionName(name)

fun mentionCandidateKey(candidate: MentionCandidate): String =
    candidate.id.ifBlank { candidate.name }

fun mergeRelationUsers(vararg lists: List<RelationUser>): List<RelationUser> {
    val merged = linkedMapOf<String, RelationUser>()
    for (user in lists.asSequence().flatten()) {
        val key = user.id.ifBlank { user.name }
        if (key.isBlank()) continue
        val existing = merged[key]
        merged[key] = if (existing == null) {
            user
        } else {
            existing.copy(
                name = existing.name.ifBlank { user.name },
                screenName = existing.screenName.ifBlank { user.screenName },
                avatarUrl = existing.avatarUrl ?: user.avatarUrl,
                avatarLarge = existing.avatarLarge ?: user.avatarLarge,
                following = existing.following || user.following,
                followMe = existing.followMe || user.followMe,
            )
        }
    }
    return merged.values.toList()
}

fun dedupeMentionCandidates(
    primary: List<MentionCandidate>,
    secondary: List<MentionCandidate>,
): List<MentionCandidate> {
    val seen = linkedSetOf<String>()
    return buildList {
        for (candidate in primary + secondary) {
            val key = mentionCandidateKey(candidate)
            if (key.isBlank() || !seen.add(key)) continue
            if (!candidate.isValidMentionCandidate()) continue
            add(candidate)
        }
    }
}

fun extractActiveMentionQuery(text: String, cursor: Int): String? {
    val safeCursor = cursor.coerceIn(0, text.length)
    val before = text.substring(0, safeCursor)
    val atIndex = before.lastIndexOf('@')
    if (atIndex < 0) return null
    val segment = before.substring(atIndex + 1)
    if (segment.any { it.isWhitespace() }) return null
    return segment
}

fun filterMentionCandidatesByQuery(
    candidates: List<MentionCandidate>,
    query: String,
    limit: Int = 40,
): List<MentionCandidate> {
    val trimmed = query.trim()
    if (trimmed.isEmpty()) return candidates
    return candidates
        .asSequence()
        .filter { it.name.contains(trimmed, ignoreCase = true) }
        .sortedWith(
            compareBy<MentionCandidate>(
                { !it.name.startsWith(trimmed, ignoreCase = true) },
                { it.name.lowercase() },
            ),
        )
        .take(limit)
        .toList()
}
