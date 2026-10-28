package com.example.myweibo.data

import org.json.JSONObject

enum class UserVerification { Red, Orange, Blue, Yellow, Gold, Grey }

internal fun parseUserVerification(user: JSONObject?): UserVerification? {
    if (user == null || (user.has("verified") && !user.optBoolean("verified"))) return null
    return when (user.optInt("verified_type", -1)) {
        // getVerifiedIcon() from the current logged-in weibo-pro-next web bundle.
        0 -> when (user.optInt("verified_type_ext", 0)) {
            1 -> UserVerification.Gold
            2 -> UserVerification.Orange
            else -> UserVerification.Yellow
        }
        in 1..7 -> when {
            user.optInt("verified_type_ext", 0) == -1 -> UserVerification.Grey
            user.optInt("verified_type", -1) == 3 && user.optInt("verified_type_ext", 0) == 53 -> UserVerification.Red
            else -> UserVerification.Blue
        }
        else -> null
    }
}

/** Search HTML exposes badge classes rather than JSON verification fields. */
internal fun parseHtmlUserVerification(html: String): UserVerification? {
    val classes = Regex("""\bclass\s*=\s*["']([^"']*)["']""", RegexOption.IGNORE_CASE)
        .findAll(html).flatMap { it.groupValues[1].split(Regex("\\s+")).asSequence() }.toSet()
    return when {
        "woo-icon--vgold" in classes || "icon_approve_gold" in classes -> UserVerification.Gold
        "woo-icon--vred" in classes -> UserVerification.Red
        "woo-icon--vorange" in classes -> UserVerification.Orange
        "woo-icon--vgrey" in classes -> UserVerification.Grey
        "woo-icon--vblue" in classes || "icon_approve_co" in classes -> UserVerification.Blue
        "woo-icon--vyellow" in classes || "icon_approve" in classes -> UserVerification.Yellow
        else -> null
    }
}
