package com.sonms.aishortcut.data.newsfeed

import co.touchlab.kermit.Logger
import com.sonms.aishortcut.core.network.createHttpClient
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.CancellationException

data class FeedSource(val name: String, val url: String)

// arXiv Atom API -- newest cs.AI submissions. No auth, same as Hugging Face.
val DEFAULT_FEEDS: List<FeedSource> = listOf(
    FeedSource(
        name = "arXiv cs.AI",
        url = "https://export.arxiv.org/api/query" +
            "?search_query=cat:cs.AI&sortBy=submittedDate&sortOrder=descending&max_results=25",
    ),
)

class NewsFeedRepository(
    private val httpClient: HttpClient = createHttpClient(),
    private val feeds: List<FeedSource> = DEFAULT_FEEDS,
) {
    // One flaky feed shouldn't blank the whole list, so failures are logged and
    // skipped per source rather than thrown.
    suspend fun getArticles(): List<NewsArticle> = feeds.flatMap { feed ->
        try {
            FeedParser.parse(httpClient.get(feed.url).bodyAsText(), feed.name)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Logger.withTag("NewsFeedRepository").e(e) { "feed load failed: ${feed.name}" }
            emptyList()
        }
    }
}
