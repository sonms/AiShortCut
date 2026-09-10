package com.sonms.aishortcut.data.newsfeed

import com.sonms.aishortcut.core.network.createHttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

private val RSS = """
<rss version="2.0"><channel>
  <item><title>Alpha</title><link>https://e.com/alpha</link><description>a</description></item>
  <item><title>Beta</title><link>https://e.com/beta</link><description>b</description></item>
</channel></rss>
"""

private val HF_PAPERS = """
[
  {"paper":{"id":"2609.001","title":"Featured paper","summary":"abstract","ai_summary":"short","submittedOnDailyAt":"2026-09-07T00:00:00.000Z"}}
]
"""

class NewsFeedRepositoryTest {

    @Test
    fun fetchesEachFeedAndTagsArticlesWithItsSource() = runTest {
        val engine = MockEngine {
            respond(RSS, headers = headersOf(HttpHeaders.ContentType, "application/rss+xml"))
        }
        val repo = NewsFeedRepository(
            httpClient = createHttpClient(engine),
            feeds = listOf(FeedSource("Feed A", "https://a.test/rss")),
            hfDailyPapersEndpoint = null,
        )

        val articles = repo.getArticles()

        assertEquals(listOf("Alpha", "Beta"), articles.map { it.title })
        assertEquals(listOf("Feed A", "Feed A"), articles.map { it.source })
    }

    @Test
    fun oneFailingFeedIsSkippedNotFatal() = runTest {
        val engine = MockEngine { request ->
            if ("good" in request.url.host) {
                respond(RSS, headers = headersOf(HttpHeaders.ContentType, "application/rss+xml"))
            } else {
                throw RuntimeException("network down")
            }
        }
        val repo = NewsFeedRepository(
            httpClient = createHttpClient(engine),
            feeds = listOf(
                FeedSource("Bad", "https://bad.test/rss"),
                FeedSource("Good", "https://good.test/rss"),
            ),
            hfDailyPapersEndpoint = null,
        )

        val articles = repo.getArticles()

        assertEquals(listOf("Alpha", "Beta"), articles.map { it.title })
        assertEquals(listOf("Good", "Good"), articles.map { it.source })
    }

    @Test
    fun hfDailyPapersLeadTheListAndFailSoft() = runTest {
        val engine = MockEngine { request ->
            when {
                "huggingface" in request.url.host ->
                    respond(HF_PAPERS, headers = headersOf(HttpHeaders.ContentType, "application/json"))
                else ->
                    respond(RSS, headers = headersOf(HttpHeaders.ContentType, "application/rss+xml"))
            }
        }
        val repo = NewsFeedRepository(
            httpClient = createHttpClient(engine),
            feeds = listOf(FeedSource("Feed A", "https://a.test/rss")),
            hfDailyPapersEndpoint = "https://huggingface.co/api/daily_papers",
        )

        val articles = repo.getArticles()

        assertEquals(listOf("Featured paper", "Alpha", "Beta"), articles.map { it.title })
        assertEquals("Hugging Face Papers", articles.first().source)
        assertEquals("short", articles.first().summary)
    }

    @Test
    fun hfDailyPapersFailureDoesNotBlankTheFeeds() = runTest {
        val engine = MockEngine { request ->
            if ("huggingface" in request.url.host) throw RuntimeException("HF down")
            else respond(RSS, headers = headersOf(HttpHeaders.ContentType, "application/rss+xml"))
        }
        val repo = NewsFeedRepository(
            httpClient = createHttpClient(engine),
            feeds = listOf(FeedSource("Feed A", "https://a.test/rss")),
            hfDailyPapersEndpoint = "https://huggingface.co/api/daily_papers",
        )

        assertEquals(listOf("Alpha", "Beta"), repo.getArticles().map { it.title })
    }
}
