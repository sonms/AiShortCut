package com.sonms.aishortcut.data.hftrending

// Domain type the rest of the app consumes. Deliberately free of any
// serialization / transport concern -- HfRepoDataDto.toDomain() bridges the two.
data class TrendingModel(
    val id: String,
    val author: String?,
    val likes: Int,
    val downloads: Int,
    val pipelineTag: String?,
)
