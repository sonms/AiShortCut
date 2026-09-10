package com.sonms.aishortcut.data.githubtrending

// Domain type the rest of the app consumes. Deliberately free of any
// serialization / transport concern -- GithubRepoDto.toDomain() bridges the two.
data class TrendingRepo(
    val id: Long,
    val fullName: String,
    val description: String?,
    val url: String,
    val stars: Int,
    val forks: Int,
    val language: String?,
    val topics: List<String>,
)
