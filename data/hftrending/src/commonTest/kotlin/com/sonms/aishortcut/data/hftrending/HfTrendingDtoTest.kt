package com.sonms.aishortcut.data.hftrending

import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

// Fixture captured from a real `GET /api/trending?type=model` response.
// First entry keeps the extra nested fields the API actually sends (authorData,
// numParameters, availableInferenceProviders, gated, private) so that a regression
// in `ignoreUnknownKeys` handling or a field rename is caught here.
// Second entry omits `pipeline_tag`, which is absent on some real entries.
private const val SAMPLE_RESPONSE = """
{"recentlyTrending":[
    {"repoData":{"author":"Qwen","authorData":{"_id":"64c8b5837fe12ecd0a7e92eb","fullname":"Qwen","type":"org","followerCount":101822},"downloads":2945415,"gated":false,"id":"Qwen/Qwen3.8-27B","availableInferenceProviders":[],"lastModified":"2026-08-27T05:03:36.000Z","likes":12746,"pipeline_tag":"image-text-to-text","private":false,"repoType":"model","numParameters":179999981459},"repoType":"model"},
    {"repoData":{"id":"unsloth/Qwen3.8-27B-GGUF","author":"unsloth","likes":2927,"downloads":7334695},"repoType":"model"}
]}
"""

class HfTrendingDtoTest {
    @Test
    fun parsesRecentlyTrendingIntoDomainModels() {
        val json = Json { ignoreUnknownKeys = true }
        val response = json.decodeFromString<HfTrendingResponse>(SAMPLE_RESPONSE)
        val models = response.recentlyTrending.map { it.repoData.toDomain() }

        assertEquals(2, models.size)
        assertEquals("Qwen/Qwen3.8-27B", models[0].id)
        assertEquals("image-text-to-text", models[0].pipelineTag)
        assertNull(models[1].pipelineTag)
    }
}
