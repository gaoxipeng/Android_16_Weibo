package com.example.myweibo.data

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

object WeiboStatusActions {
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
