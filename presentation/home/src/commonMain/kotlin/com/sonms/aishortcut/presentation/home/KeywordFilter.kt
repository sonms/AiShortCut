package com.sonms.aishortcut.presentation.home

// Keyword clustering over the daily digest feed. HF daily papers carry
// `ai_keywords`; the arXiv/RSS feed carries none, so this stays empty until the
// HF source is in the mix. Pure functions -- one commonTest covers them.

// Topic tags across the whole feed, most frequent first. Counting is
// case-insensitive, but the first spelling seen is what gets displayed. Ties
// break alphabetically so the chip row is stable across reloads.
fun trendingKeywords(digests: List<DailyDigest>, limit: Int = 8): List<String> {
    val firstSpelling = LinkedHashMap<String, String>()
    val counts = HashMap<String, Int>()
    digests.asSequence()
        .flatMap { it.articles.asSequence() }
        .flatMap { it.keywords.asSequence() }
        .forEach { raw ->
            val trimmed = raw.trim()
            val key = trimmed.lowercase()
            if (key.length < 2) return@forEach
            if (key !in firstSpelling) firstSpelling[key] = trimmed
            counts[key] = (counts[key] ?: 0) + 1
        }
    return counts.entries
        .sortedWith(compareByDescending<Map.Entry<String, Int>> { it.value }.thenBy { it.key })
        .take(limit)
        .map { firstSpelling.getValue(it.key) }
}

// The digests with only the articles carrying [keyword] (case-insensitive).
// Days that end up empty are dropped. A null keyword returns the list unchanged.
fun List<DailyDigest>.filterByKeyword(keyword: String?): List<DailyDigest> {
    if (keyword == null) return this
    val key = keyword.trim().lowercase()
    return mapNotNull { digest ->
        val kept = digest.articles.filter { article ->
            article.keywords.any { it.trim().lowercase() == key }
        }
        if (kept.isEmpty()) null else digest.copy(articles = kept)
    }
}
