package com.sonms.aishortcut.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedArticleDao {

    @Query("SELECT * FROM saved_articles ORDER BY id DESC")
    fun observeAll(): Flow<List<SavedArticleEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM saved_articles WHERE link = :link)")
    suspend fun exists(link: String): Boolean

    // IGNORE, not REPLACE: callers check exists() first, and swallowing a rare
    // double-tap race is better than churning the row's id.
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(entity: SavedArticleEntity)

    @Query("DELETE FROM saved_articles WHERE link = :link")
    suspend fun deleteByLink(link: String)
}
