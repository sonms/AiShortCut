package com.sonms.aishortcut.data.githubtrending

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GithubSearchResponse(
    val items: List<GithubRepoDto> = emptyList()
)

@Serializable
data class GithubRepoDto(
    val id: Long,
    @SerialName("full_name") val fullName: String,
    val description: String? = null,
    @SerialName("html_url") val htmlUrl: String,
    @SerialName("stargazers_count") val stars: Int = 0,
    @SerialName("forks_count") val forks: Int = 0,
    val language: String? = null,
    val topics: List<String> = emptyList(),
)

fun GithubRepoDto.toDomain() = TrendingRepo(
    id = id,
    fullName = fullName,
    description = description,
    url = htmlUrl,
    stars = stars,
    forks = forks,
    language = language,
    topics = topics,
)
