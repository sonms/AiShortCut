package com.sonms.aishortcut.data.hftrending

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpHeaders
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

private const val SAMPLE_RESPONSE = """
{"recentlyTrending":[
    {"repoData":{"id":"Qwen/Qwen3.8-27B","author":"Qwen","likes":12746,"downloads":2945415,"pipeline_tag":"image-text-to-text"},"repoType":"model"},
    {"repoData":{"id":"unsloth/Qwen3.8-27B-GGUF","author":"unsloth","likes":2927,"downloads":7334695},"repoType":"model"}
]}
"""

class HfTrendingRepositoryTest {
    @Test
    fun requestsTheTrendingModelEndpointAndMapsToDomain() = runTest {
        lateinit var requestedUrl: String
        val engine = MockEngine { request ->
            requestedUrl = request.url.toString()
            respond(
                content = SAMPLE_RESPONSE,
                headers = headersOf(HttpHeaders.ContentType, "application/json"),
            )
        }
        val client = HttpClient(engine) {
            install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
        }

        val models = HfTrendingRepository(client).getTrendingModels(limit = 5)

        assertEquals(
            "https://huggingface.co/api/trending?type=model&limit=5",
            requestedUrl,
        )
        assertEquals(2, models.size)
        assertEquals("Qwen/Qwen3.8-27B", models[0].id)
        assertEquals("image-text-to-text", models[0].pipelineTag)
    }
}
