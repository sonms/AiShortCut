package com.sonms.aishortcut.data.openrouter

// Domain type the rest of the app consumes. No serialization / transport
// concern -- OpenRouterModelDto.toDomain() bridges the two.
//
// This exists only to enrich a Hugging Face trending model with the commercial
// facts HF doesn't carry: how much context it takes, what it costs to call, and
// its Artificial Analysis benchmark indices (bundled free in OpenRouter's
// no-auth models endpoint). [huggingFaceId] is the join key back to TrendingModel.
data class OpenRouterModel(
    val id: String,
    val huggingFaceId: String,
    val name: String,
    val contextLength: Int?,
    val promptUsdPerMTokens: Double?,
    val completionUsdPerMTokens: Double?,
    val intelligenceIndex: Double?,
    val codingIndex: Double?,
    val agenticIndex: Double?,
)
