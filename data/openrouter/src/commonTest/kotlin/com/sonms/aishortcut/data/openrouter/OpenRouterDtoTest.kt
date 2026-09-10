package com.sonms.aishortcut.data.openrouter

import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

// Fixture trimmed from a real GET /api/v1/models response. First entry keeps the
// nested shape the API sends (architecture, top_provider, supported_parameters,
// pricing.overrides) to catch an ignoreUnknownKeys regression. Second entry has
// no hugging_face_id -> not joinable, dropped. Third is a ":batch" variant that
// repeats a hugging_face_id.
private const val SAMPLE = """
{"data":[
  {"id":"qwen/qwen3.8-27b","hugging_face_id":"Qwen/Qwen3.8-27B","name":"Qwen: Qwen3.8 27B","context_length":1000000,"architecture":{"modality":"text->text"},"pricing":{"prompt":"0.00000042","completion":"0.00000126","overrides":[]},"top_provider":{"context_length":1000000},"supported_parameters":["tools"],"benchmarks":{"artificial_analysis":{"intelligence_index":33.9,"coding_index":28.1,"agentic_index":null}}},
  {"id":"openai/gpt-6-astra","hugging_face_id":null,"name":"OpenAI: GPT-6 Astra","context_length":1050000,"pricing":{"prompt":"0.00001","completion":"0.00005"}},
  {"id":"qwen/qwen3.8-27b:batch","hugging_face_id":"Qwen/Qwen3.8-27B","name":"Qwen: Qwen3.8 27B (batch)","context_length":1000000,"pricing":{"prompt":"0.00000021","completion":"0.00000063"}}
]}
"""

class OpenRouterDtoTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun mapsPricingToPerMillionTokensAndKeepsBenchmarks() {
        val model = json.decodeFromString<OpenRouterModelsResponse>(SAMPLE)
            .data.first().toDomain()!!

        assertEquals("Qwen/Qwen3.8-27B", model.huggingFaceId)
        assertEquals(1_000_000, model.contextLength)
        assertEquals(0.42, model.promptUsdPerMTokens)
        assertEquals(1.26, model.completionUsdPerMTokens)
        assertEquals(33.9, model.intelligenceIndex)
        assertNull(model.agenticIndex)
    }

    @Test
    fun entryWithoutHuggingFaceIdIsNotJoinable() {
        assertNull(json.decodeFromString<OpenRouterModelsResponse>(SAMPLE).data[1].toDomain())
    }

    @Test
    fun repositoryStyleFilteringDropsVariantsAndUnjoinableEntries() {
        val map = json.decodeFromString<OpenRouterModelsResponse>(SAMPLE)
            .data
            .filterNot { ':' in it.id }
            .mapNotNull { it.toDomain() }
            .associateBy { it.huggingFaceId.lowercase() }

        assertEquals(1, map.size)
        assertTrue("qwen/qwen3.8-27b" in map)
        // the canonical entry, not the ":batch" one
        assertEquals(0.42, map.getValue("qwen/qwen3.8-27b").promptUsdPerMTokens)
    }
}
