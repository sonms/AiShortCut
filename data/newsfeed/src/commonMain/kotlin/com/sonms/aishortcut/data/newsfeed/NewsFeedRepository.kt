package com.sonms.aishortcut.data.newsfeed

import co.touchlab.kermit.Logger
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
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

// Hugging Face's human-curated daily papers list -- upvoted, deduped, and far
// less noisy than the raw arXiv firehose. No auth. `null` disables it in tests.
const val HF_DAILY_PAPERS_ENDPOINT = "https://huggingface.co/api/daily_papers"

class NewsFeedRepository(
    private val httpClient: HttpClient,
    private val feeds: List<FeedSource> = DEFAULT_FEEDS,
    private val hfDailyPapersEndpoint: String? = HF_DAILY_PAPERS_ENDPOINT,
) {
    // HF's curated papers lead, then the raw feeds. One flaky source shouldn't
    // blank the whole list, so failures are logged and skipped per source.
    suspend fun getArticles(): List<NewsArticle> =
        hfDailyPapers() + feeds.flatMap { feed ->
            try {
                FeedParser.parse(httpClient.get(feed.url).bodyAsText(), feed.name)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Logger.withTag("NewsFeedRepository").e(e) { "feed load failed: ${feed.name}" }
                emptyList()
            }
        }

    private suspend fun hfDailyPapers(): List<NewsArticle> {
        val endpoint = hfDailyPapersEndpoint ?: return emptyList()
        return try {
            httpClient.get(endpoint) { parameter("limit", 30) }
                .body<List<HfDailyPaperEntryDto>>()
                .map { it.paper.toDomain() }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Logger.withTag("NewsFeedRepository").e(e) { "HF daily papers load failed" }
            emptyList()
        }
    }
}
