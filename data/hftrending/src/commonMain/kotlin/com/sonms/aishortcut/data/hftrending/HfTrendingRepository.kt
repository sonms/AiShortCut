package com.sonms.aishortcut.data.hftrending

import com.sonms.aishortcut.core.network.createHttpClient
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

private const val TRENDING_ENDPOINT = "https://huggingface.co/api/trending"

class HfTrendingRepository(
    private val httpClient: HttpClient = createHttpClient()
) {
    suspend fun getTrendingModels(limit: Int = 20): List<TrendingModel> {
        val response: HfTrendingResponse = httpClient.get(TRENDING_ENDPOINT) {
            parameter("type", "model")
            parameter("limit", limit)
        }.body()

        return response.recentlyTrending.map { it.repoData.toDomain() }
    }
}
