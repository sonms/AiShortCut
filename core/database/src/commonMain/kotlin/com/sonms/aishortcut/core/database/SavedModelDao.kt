package com.sonms.aishortcut.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedModelDao {

    @Query("SELECT * FROM saved_models ORDER BY id DESC")
    fun observeAll(): Flow<List<SavedModelEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM saved_models WHERE modelId = :modelId)")
    suspend fun exists(modelId: String): Boolean

    // IGNORE, not REPLACE: callers check exists() first, and swallowing a rare
    // double-tap race is better than churning the row's id.
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(entity: SavedModelEntity)

    @Query("DELETE FROM saved_models WHERE modelId = :modelId")
    suspend fun deleteByModelId(modelId: String)
}
