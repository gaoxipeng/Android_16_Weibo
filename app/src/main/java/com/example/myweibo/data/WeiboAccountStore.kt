package com.example.myweibo.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

data class StoredWeiboAccount(
    val id: String,
    val screenName: String,
    val avatarUrl: String?,
    val cookies: Map<String, String>,
)

class WeiboAccountStore(context: Context) {
    private val file = File(context.filesDir, "weibo_accounts.json")

    @Synchronized
    fun readAccounts(): List<StoredWeiboAccount> {
        if (!file.exists()) return emptyList()
        return runCatching {
            val root = JSONObject(file.readText(Charsets.UTF_8))
            root.optJSONArray("accounts").toStoredAccounts()
        }.getOrDefault(emptyList())
    }

    @Synchronized
    fun readActiveAccountId(): String? =
        runCatching {
            if (!file.exists()) return null
            JSONObject(file.readText(Charsets.UTF_8)).optString("active_id").takeIf { it.isNotBlank() }
        }.getOrNull()

    @Synchronized
    fun getAccount(id: String): StoredWeiboAccount? =
        readAccounts().firstOrNull { it.id == id }

    @Synchronized
    fun upsertAccount(account: StoredWeiboAccount, makeActive: Boolean = false) {
        val accounts = readAccounts()
            .filterNot { it.id == account.id } + account
        val currentActiveId = readActiveAccountId()
        val activeId = when {
            makeActive -> account.id
            currentActiveId.isNullOrBlank() -> account.id
            else -> currentActiveId
        }
        write(accounts, activeId)
    }

    @Synchronized
    fun setActiveAccountId(id: String?) {
        write(readAccounts(), id)
    }

    @Synchronized
    fun removeAccount(id: String) {
        val accounts = readAccounts().filterNot { it.id == id }
        val activeId = readActiveAccountId()
        val nextActive = when {
            activeId != id -> activeId
            else -> accounts.firstOrNull()?.id
        }
        write(accounts, nextActive)
    }

    @Synchronized
    private fun write(accounts: List<StoredWeiboAccount>, activeId: String?) {
        val root = JSONObject()
            .put("active_id", activeId.orEmpty())
            .put("accounts", accounts.toJsonArray())
        file.parentFile?.mkdirs()
        file.writeText(root.toString(), Charsets.UTF_8)
    }

    private fun JSONArray?.toStoredAccounts(): List<StoredWeiboAccount> {
        if (this == null) return emptyList()
        return buildList(length()) {
            for (index in 0 until length()) {
                val item = optJSONObject(index) ?: continue
                val id = item.optString("id").takeIf { it.isNotBlank() } ?: continue
                val cookies = mutableMapOf<String, String>()
                item.optJSONObject("cookies")?.let { cookieJson ->
                    cookieJson.keys().forEach { key ->
                        val value = cookieJson.optString(key)
                        if (value.isNotBlank()) cookies[key] = value
                    }
                }
                add(
                    StoredWeiboAccount(
                        id = id,
                        screenName = item.optString("screen_name"),
                        avatarUrl = item.optNullableString("avatar_url"),
                        cookies = cookies,
                    )
                )
            }
        }
    }

    private fun List<StoredWeiboAccount>.toJsonArray(): JSONArray =
        JSONArray().also { array ->
            forEach { account ->
                array.put(
                    JSONObject()
                        .put("id", account.id)
                        .put("screen_name", account.screenName)
                        .put("avatar_url", account.avatarUrl)
                        .put(
                            "cookies",
                            JSONObject().also { cookieJson ->
                                account.cookies.forEach { (origin, value) ->
                                    cookieJson.put(origin, value)
                                }
                            },
                        )
                )
            }
        }

    private fun JSONObject.optNullableString(key: String): String? =
        if (has(key) && !isNull(key)) optString(key).takeIf { it.isNotBlank() } else null
}
