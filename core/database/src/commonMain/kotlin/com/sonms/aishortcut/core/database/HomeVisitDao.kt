package com.sonms.aishortcut.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface HomeVisitDao {

    @Query("SELECT * FROM home_visits WHERE id = 0")
    suspend fun get(): HomeVisitEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: HomeVisitEntity)
}
