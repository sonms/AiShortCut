package com.sonms.aishortcut.core.database

import androidx.room.Entity
import androidx.room.PrimaryKey

// Single-row counter: id is always 0, so insert-with-REPLACE doubles as upsert.
@Entity(tableName = "home_visits")
data class HomeVisitEntity(
    @PrimaryKey val id: Int = 0,
    val count: Int,
)
