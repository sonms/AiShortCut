package com.sonms.aishortcut.data.hftrending

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class HfTrendingResponse(
    val recentlyTrending: List<HfTrendingEntryDto> = emptyList()
)

@Serializable
data class HfTrendingEntryDto(
    val repoData: HfRepoDataDto
)

@Serializable
data class HfRepoDataDto(
    val id: String,
    val author: String? = null,
    val likes: Int = 0,
    val downloads: Int = 0,
    @SerialName("pipeline_tag") val pipelineTag: String? = null,
    val lastModified: String? = null
)

data class TrendingModel(
    val id: String,
    val author: String?,
    val likes: Int,
    val downloads: Int,
    val pipelineTag: String?
)

fun HfRepoDataDto.toDomain() = TrendingModel(
    id = id,
    author = author,
    likes = likes,
    downloads = downloads,
    pipelineTag = pipelineTag
)
