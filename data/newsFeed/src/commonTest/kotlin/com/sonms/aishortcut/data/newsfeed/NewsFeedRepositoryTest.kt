package com.sonms.aishortcut.data.newsfeed

import com.sonms.aishortcut.core.network.createHttpClient
import com.sonms.aishortcut.core.translate.PassthroughTranslator
import com.sonms.aishortcut.core.translate.Translator
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

private val RSS = """
<rss version="2.0"><channel>
  <item><title>Alpha</title><link>https://e.com/alpha</link><description>a</description></item>
  <item><title>Beta</title><link>https://e.com/beta</link><description>b</description></item>
</channel></rss>
"""

// Fakes translation with a reversible, obviously-different transform.
private object PrefixTranslator : Translator {
    override suspend fun translateToKorean(texts: List<String>) = texts.map { "ko:$it" }
}

class NewsFeedRepositoryTest {

    @Test
    fun fetchesEachFeedAndTagsArticlesWithItsSource() = runTest {
        val engine = MockEngine {
            respond(RSS, headers = headersOf(HttpHeaders.ContentType, "application/rss+xml"))
        }
        val repo = NewsFeedRepository(
            translator = PassthroughTranslator,
            httpClient = createHttpClient(engine),
            feeds = listOf(FeedSource("Feed A", "https://a.test/rss")),
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
            translator = PassthroughTranslator,
            httpClient = createHttpClient(engine),
            feeds = listOf(
                FeedSource("Bad", "https://bad.test/rss"),
                FeedSource("Good", "https://good.test/rss"),
            ),
        )

        val articles = repo.getArticles()

        assertEquals(listOf("Alpha", "Beta"), articles.map { it.title })
        assertEquals(listOf("Good", "Good"), articles.map { it.source })
    }

    @Test
    fun translateFillsKoreanFieldsIndexAlignedFromOneBatch() = runTest {
        val repo = NewsFeedRepository(translator = PrefixTranslator, httpClient = createHttpClient(MockEngine { respond("") }))
        val input = listOf(
            NewsArticle("Title one", "Summary one", "l1", "s", null),
            NewsArticle("Title two", "Summary two", "l2", "s", null),
        )

        val out = repo.translate(input)

        assertEquals("ko:Title one", out[0].titleKo)
        assertEquals("ko:Summary one", out[0].summaryKo)
        assertEquals("ko:Title two", out[1].titleKo)
        assertEquals("ko:Summary two", out[1].summaryKo)
    }

    @Test
    fun translateLeavesKoreanFieldsNullWhenTranslationEqualsOriginal() = runTest {
        val repo = NewsFeedRepository(translator = PassthroughTranslator, httpClient = createHttpClient(MockEngine { respond("") }))

        val out = repo.translate(listOf(NewsArticle("Llama 3", "GPT-4o", "l", "s", null)))

        assertNull(out[0].titleKo)
        assertNull(out[0].summaryKo)
    }
}
