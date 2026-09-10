package com.sonms.aishortcut.core.database

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

// One bookmarked article/paper. `id` autoincrements so "newest saved first" is
// just ORDER BY id DESC -- no wall-clock timestamp to source in common code.
// `link` is the natural key and stays unique.
//
// ponytail: no `keywords` column. NewsArticle carries topic tags but the Saved
// screen doesn't use them yet; add a column + TypeConverter when it does.
@Entity(
    tableName = "saved_articles",
    indices = [Index(value = ["link"], unique = true)],
)
data class SavedArticleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val link: String,
    val title: String,
    val summary: String,
    val source: String,
    val publishedAt: String?,
)
