package com.sonms.aishortcut.data.newsfeed

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// Wire types for Hugging Face's daily papers feed
// (GET https://huggingface.co/api/daily_papers). No auth, same as the rest of
// the HF API. Each element wraps the paper in a one-key object.
@Serializable
data class HfDailyPaperEntryDto(
    val paper: HfPaperDto,
)

@Serializable
data class HfPaperDto(
    val id: String,
    val title: String,
    // The arXiv abstract. `ai_summary` is HF's shorter machine summary, better
    // suited to a feed row; fall back to the abstract when it's absent.
    val summary: String = "",
    @SerialName("ai_summary") val aiSummary: String? = null,
    // ISO-8601. `submittedOnDailyAt` is the day HF featured the paper, which is
    // what "today's papers" should group by; `publishedAt` is the arXiv date.
    val publishedAt: String? = null,
    val submittedOnDailyAt: String? = null,
    val upvotes: Int = 0,
    // HF's machine-extracted topic tags. Absent on older entries.
    @SerialName("ai_keywords") val aiKeywords: List<String> = emptyList(),
)

fun HfPaperDto.toDomain() = NewsArticle(
    title = title.trim(),
    summary = (aiSummary?.takeIf { it.isNotBlank() } ?: summary).trim(),
    link = "https://huggingface.co/papers/$id",
    source = "Hugging Face Papers",
    publishedAt = submittedOnDailyAt ?: publishedAt,
    keywords = aiKeywords.map { it.trim() }.filter { it.isNotEmpty() },
)
