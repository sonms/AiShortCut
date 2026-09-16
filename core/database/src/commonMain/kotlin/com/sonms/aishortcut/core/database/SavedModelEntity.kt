package com.sonms.aishortcut.core.database

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

// One favorited model. `id` autoincrements so "newest saved first" is just
// ORDER BY id DESC. `modelId` (the HF repo id, e.g. "deepseek-ai/DeepSeek-V4.1")
// is the natural key and stays unique.
@Entity(
    tableName = "saved_models",
    indices = [Index(value = ["modelId"], unique = true)],
)
data class SavedModelEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val modelId: String,
    val author: String?,
    val likes: Int,
    val downloads: Int,
    val pipelineTag: String?,
)
