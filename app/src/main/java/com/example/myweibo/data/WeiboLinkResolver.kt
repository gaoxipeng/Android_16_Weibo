package com.example.myweibo.data

object WeiboLinkResolver {
    fun statusIdCandidates(vararg urls: String): List<String> {
        return urls
            .flatMap(::statusIdCandidatesFromUrl)
            .distinct()
            .filter { it.isNotBlank() }
    }

    private fun statusIdCandidatesFromUrl(rawUrl: String): List<String> {
        val url = rawUrl.trim()
        if (url.isBlank() || resolveArticleId(url) != null) return emptyList()

        val candidates = mutableListOf<String>()

        Regex("""(?i)m\.weibo\.cn/(?:status|detail)/(\d+)""").find(url)?.groupValues?.get(1)?.let {
            candidates += it
        }
        Regex("""(?i)weibo\.com/detail/(\d+)""").find(url)?.groupValues?.get(1)?.let {
            candidates += it
        }
        Regex("""(?i)weibo\.com/\d+/([A-Za-z0-9]+)""").find(url)?.groupValues?.get(1)?.let {
            candidates += it
        }
        Regex("""(?i)weibo\.com/ajax/statuses/show\?[^#]*[?&]id=([^&#]+)""").find(url)?.groupValues?.get(1)?.let {
            candidates += it
        }
        Regex("""(?i)[?&]mblogid=([^&#]+)""").find(url)?.groupValues?.get(1)?.let {
            candidates += it
        }
        Regex("""(?i)[?&]blogid=([^&#]+)""").find(url)?.groupValues?.get(1)?.let {
            candidates += it
        }
        Regex("""(?i)sinaweibo://detail\?[^#]*""").find(url)?.let {
            Regex("""(?i)[?&](?:mblogid|blogid)=([^&#]+)""").find(url)?.groupValues?.get(1)?.let { id ->
                candidates += id
            }
        }
        if (!url.contains("/ttarticle/", ignoreCase = true) &&
            !url.contains("/article/", ignoreCase = true)
        ) {
            Regex("""[?&]id=(\d+)""").find(url)?.groupValues?.get(1)?.let {
                candidates += it
            }
        }

        return candidates
    }
}
