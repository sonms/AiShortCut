package com.sonms.aishortcut.data.openrouter

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

private const val MODELS_ENDPOINT = "https://openrouter.ai/api/v1/models"

class OpenRouterRepository(
    private val httpClient: HttpClient,
) {
    // Keyed by hugging_face_id (lowercased) so a HF trending model can look up
    // its pricing and benchmark data by id. Variant slugs (":batch", ":free",
    // ":thinking") repeat a hugging_face_id, so they're dropped and the
    // canonical entry is kept.
    suspend fun getModelsByHuggingFaceId(): Map<String, OpenRouterModel> {
        val response: OpenRouterModelsResponse = httpClient.get(MODELS_ENDPOINT).body()
        return response.data
            .filterNot { ':' in it.id }
            .mapNotNull { it.toDomain() }
            .associateBy { it.huggingFaceId.lowercase() }
    }
}
