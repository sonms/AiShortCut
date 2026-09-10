package com.sonms.aishortcut.data.githubtrending

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.http.HttpHeaders
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days

private const val SEARCH_ENDPOINT = "https://api.github.com/search/repositories"

// GitHub has no trending endpoint. Approximate it: repos tagged with the
// `artificial-intelligence` topic that were created inside a recent window,
// ranked by stars -- i.e. new AI projects picking up traction fast. The
// Search API allows this unauthenticated at 10 requests/minute.
class GithubTrendingRepository(
    private val httpClient: HttpClient,
) {
    suspend fun getTrendingRepos(limit: Int = 20, withinDays: Int = 30): List<TrendingRepo> {
        val since = (Clock.System.now() - withinDays.days).toString().substringBefore('T')

        val response: GithubSearchResponse = httpClient.get(SEARCH_ENDPOINT) {
            parameter("q", "topic:artificial-intelligence created:>$since")
            parameter("sort", "stars")
            parameter("order", "desc")
            parameter("per_page", limit)
            header(HttpHeaders.Accept, "application/vnd.github+json")
        }.body()

        return response.items.map { it.toDomain() }
    }
}
