package com.example.myweibo.data

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

object WeiboStatusActions {
    /** Open a super-topic with the official client when its native container ID is present. */
    fun openSuperTopicInWeiboApp(context: Context, vararg rawUrls: String): Boolean {
        val containerId = rawUrls.asSequence()
            .map(String::trim)
            .filter(String::isNotBlank)
            .mapNotNull(::superTopicContainerId)
            .firstOrNull()
            ?: return false
        val packageManager = context.packageManager
        val originalNativeUri = rawUrls.asSequence()
            .map(String::trim)
            .firstOrNull { it.startsWith("sinaweibo://pageinfo", ignoreCase = true) }
            ?.let(Uri::parse)
        val candidates = buildList {
            originalNativeUri?.let { add(it) }
            add(Uri.parse("sinaweibo://pageinfo?containerid=${Uri.encode(containerId)}"))
        }
        val intent = candidates.asSequence()
            .map { uri ->
                Intent(Intent.ACTION_VIEW, uri).apply {
                    setPackage("com.sina.weibo")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            }
            .firstOrNull { it.resolveActivity(packageManager) != null }
            ?: return false
        return runCatching {
            context.startActivity(intent)
            true
        }.getOrDefault(false)
    }

    private fun superTopicContainerId(rawUrl: String): String? {
        // Weibo super-topic pages use container IDs beginning with 100808. Links can
        // arrive as a direct /p/<id> URL or nested/percent-encoded containerid query.
        val decoded = generateSequence(rawUrl) { previous ->
            Uri.decode(previous).takeIf { it != previous }
        }.take(4).toList().joinToString(" ")
        val match = Regex(
            "(?i)(?:^|[?&](?:containerid|pageid)=|(?:containerid|pageid)%3D|/p/)(100808[A-Za-z0-9_-]+)",
        ).find(decoded)
            ?: Regex("(?i)^(100808[A-Za-z0-9_-]+)$").find(decoded.trim())
        return match?.groupValues?.get(1)
    }

    fun weiboUrl(item: FeedItem): String? {
        val authorId = item.authorId.takeIf { it.isNotBlank() } ?: return null
        val statusKey = item.statusId.takeIf { it.isNotBlank() }
            ?: item.id.takeIf { it.isNotBlank() }
            ?: return null
        return "https://weibo.com/$authorId/$statusKey"
    }

    fun openInWeiboApp(context: Context, item: FeedItem) {
        val url = weiboUrl(item)
        val statusKey = item.statusId.takeIf { it.isNotBlank() } ?: item.id.takeIf { it.isNotBlank() }
        if (url == null || statusKey == null) {
            Toast.makeText(context, "无法生成微博链接", Toast.LENGTH_SHORT).show()
            return
        }

        val packageManager = context.packageManager
        val candidates = listOf(
            Intent(Intent.ACTION_VIEW, Uri.parse("sinaweibo://detail?mblogid=$statusKey")).apply {
                setPackage("com.sina.weibo")
            },
            Intent(Intent.ACTION_VIEW, Uri.parse("sinaweibo://detail?blogid=$statusKey")).apply {
                setPackage("com.sina.weibo")
            },
            Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                setPackage("com.sina.weibo")
            },
            Intent(Intent.ACTION_VIEW, Uri.parse(url)),
        )
        val intent = candidates.firstOrNull { it.resolveActivity(packageManager) != null }
        if (intent != null) {
            context.startActivity(intent)
        } else {
            Toast.makeText(context, "未安装微博客户端", Toast.LENGTH_SHORT).show()
        }
    }

    /** 打开地点 / 外链：优先官方微博 App，否则系统浏览器。 */
    fun openExternalUrl(context: Context, rawUrl: String, failureMessage: String = "无法打开链接") {
        val url = rawUrl.trim().takeIf { it.isNotBlank() } ?: run {
            Toast.makeText(context, failureMessage, Toast.LENGTH_SHORT).show()
            return
        }
        val uri = runCatching { Uri.parse(url) }.getOrNull() ?: run {
            Toast.makeText(context, failureMessage, Toast.LENGTH_SHORT).show()
            return
        }
        val packageManager = context.packageManager
        val candidates = buildList {
            if (url.contains("weibo.com", ignoreCase = true) ||
                url.contains("weibo.cn", ignoreCase = true) ||
                url.startsWith("sinaweibo://", ignoreCase = true)
            ) {
                add(
                    Intent(Intent.ACTION_VIEW, uri).apply {
                        setPackage("com.sina.weibo")
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    },
                )
            }
            add(
                Intent(Intent.ACTION_VIEW, uri).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                },
            )
        }
        val intent = candidates.firstOrNull { it.resolveActivity(packageManager) != null }
        if (intent != null) {
            runCatching { context.startActivity(intent) }
                .onFailure {
                    Toast.makeText(context, failureMessage, Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(context, failureMessage, Toast.LENGTH_SHORT).show()
        }
    }

    fun shareLink(context: Context, item: FeedItem) {
        val url = weiboUrl(item)
        if (url == null) {
            Toast.makeText(context, "无法生成微博链接", Toast.LENGTH_SHORT).show()
            return
        }
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, url)
        }
        context.startActivity(Intent.createChooser(intent, "分享微博"))
    }
}
