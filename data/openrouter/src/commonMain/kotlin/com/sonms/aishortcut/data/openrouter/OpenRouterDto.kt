package com.sonms.aishortcut.data.openrouter

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// Wire types for GET https://openrouter.ai/api/v1/models. No auth. Only the
// fields this app uses are declared; the shared Json ignores the rest.
@Serializable
data class OpenRouterModelsResponse(
    val data: List<OpenRouterModelDto> = emptyList(),
)

@Serializable
data class OpenRouterModelDto(
    val id: String,
    @SerialName("hugging_face_id") val huggingFaceId: String? = null,
    val name: String = "",
    @SerialName("context_length") val contextLength: Int? = null,
    val pricing: OpenRouterPricingDto? = null,
    val benchmarks: OpenRouterBenchmarksDto? = null,
)

@Serializable
data class OpenRouterPricingDto(
    // USD per token, as a decimal string ("0.00000042").
    val prompt: String? = null,
    val completion: String? = null,
)

@Serializable
data class OpenRouterBenchmarksDto(
    @SerialName("artificial_analysis") val artificialAnalysis: ArtificialAnalysisDto? = null,
)

@Serializable
data class ArtificialAnalysisDto(
    @SerialName("intelligence_index") val intelligenceIndex: Double? = null,
    @SerialName("coding_index") val codingIndex: Double? = null,
    @SerialName("agentic_index") val agenticIndex: Double? = null,
)

// Returns null when there's no hugging_face_id -- without it the entry can't be
// joined to a HF trending model, which is the only reason this data is fetched.
fun OpenRouterModelDto.toDomain(): OpenRouterModel? {
    val hfId = huggingFaceId ?: return null
    val aa = benchmarks?.artificialAnalysis
    return OpenRouterModel(
        id = id,
        huggingFaceId = hfId,
        name = name,
        contextLength = contextLength,
        promptUsdPerMTokens = pricing?.prompt.toUsdPerMTokens(),
        completionUsdPerMTokens = pricing?.completion.toUsdPerMTokens(),
        intelligenceIndex = aa?.intelligenceIndex,
        codingIndex = aa?.codingIndex,
        agenticIndex = aa?.agenticIndex,
    )
}

// OpenRouter quotes price per single token; ×1e6 gives the per-million figure
// people actually compare on.
private fun String?.toUsdPerMTokens(): Double? =
    this?.toDoubleOrNull()?.let { it * 1_000_000 }
