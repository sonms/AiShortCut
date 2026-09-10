package com.sonms.aishortcut.data.newsfeed

import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

// Fixture trimmed from a real GET /api/daily_papers response. First entry keeps
// the extra fields the API sends (authors, discussionId, githubRepo, ai_keywords)
// to catch an ignoreUnknownKeys regression. Second entry has no ai_summary, so
// toDomain() must fall back to the abstract, and no submittedOnDailyAt, so it
// must fall back to publishedAt.
private const val SAMPLE = """
[
  {"paper":{"id":"2609.03729","authors":[{"name":"Yijun Yang"}],"title":"Unfold The World","summary":"Long arXiv abstract here.","ai_summary":"Short machine summary.","publishedAt":"2026-09-03T00:00:00.000Z","submittedOnDailyAt":"2026-09-07T00:00:00.000Z","upvotes":12,"discussionId":"abc","githubRepo":"https://github.com/x/y","ai_keywords":["a","b"]}},
  {"paper":{"id":"2609.04444","title":"HarvestBench","summary":"Fallback abstract.","publishedAt":"2026-09-03T00:00:00.000Z"}}
]
"""

class HfDailyPapersDtoTest {
    @Test
    fun parsesDailyPapersIntoNewsArticles() {
        val json = Json { ignoreUnknownKeys = true }
        val articles = json.decodeFromString<List<HfDailyPaperEntryDto>>(SAMPLE)
            .map { it.paper.toDomain() }

        assertEquals(2, articles.size)
        assertEquals("Unfold The World", articles[0].title)
        assertEquals("Short machine summary.", articles[0].summary)
        assertEquals("https://huggingface.co/papers/2609.03729", articles[0].link)
        assertEquals("Hugging Face Papers", articles[0].source)
        // submittedOnDailyAt wins over publishedAt
        assertEquals("2026-09-07T00:00:00.000Z", articles[0].publishedAt)
        assertEquals(listOf("a", "b"), articles[0].keywords)

        // no ai_summary -> abstract; no submittedOnDailyAt -> publishedAt;
        // no ai_keywords -> empty
        assertEquals("Fallback abstract.", articles[1].summary)
        assertEquals("2026-09-03T00:00:00.000Z", articles[1].publishedAt)
        assertEquals(emptyList<String>(), articles[1].keywords)
    }
}
