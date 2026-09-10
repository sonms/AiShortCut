package com.sonms.aishortcut.presentation.home

import com.sonms.aishortcut.data.newsfeed.NewsArticle
import kotlin.test.Test
import kotlin.test.assertEquals

class KeywordFilterTest {

    private fun article(title: String, vararg keywords: String) = NewsArticle(
        title = title,
        summary = "",
        link = "https://example.com/$title",
        source = "test",
        publishedAt = "2026-09-08",
        keywords = keywords.toList(),
    )

    private val digests = listOf(
        DailyDigest(
            "2026-09-08",
            listOf(
                article("A", "RAG", "agents"),
                article("B", "rag", "diffusion"),
            ),
        ),
        DailyDigest(
            "2026-09-07",
            listOf(
                article("C", "RAG"),
                article("D"),
            ),
        ),
    )

    @Test
    fun ranksByFrequencyThenKeepsFirstSpelling() {
        // RAG x3, agents x1, diffusion x1 -> RAG first, then alphabetical
        assertEquals(listOf("RAG", "agents", "diffusion"), trendingKeywords(digests))
    }

    @Test
    fun dropsBlankAndSingleCharKeywords() {
        val noise = listOf(DailyDigest("d", listOf(article("X", " ", "x", "ok"))))
        assertEquals(listOf("ok"), trendingKeywords(noise))
    }

    @Test
    fun filterKeepsMatchingArticlesCaseInsensitiveAndDropsEmptyDays() {
        val filtered = digests.filterByKeyword("rag")
        assertEquals(listOf("2026-09-08", "2026-09-07"), filtered.map { it.date })
        assertEquals(listOf("A", "B"), filtered[0].articles.map { it.title })
        assertEquals(listOf("C"), filtered[1].articles.map { it.title })
    }

    @Test
    fun filterByNullReturnsInput() {
        assertEquals(digests, digests.filterByKeyword(null))
    }

    @Test
    fun filterDropsDayWithNoMatch() {
        val filtered = digests.filterByKeyword("agents")
        assertEquals(listOf("2026-09-08"), filtered.map { it.date })
    }
}
