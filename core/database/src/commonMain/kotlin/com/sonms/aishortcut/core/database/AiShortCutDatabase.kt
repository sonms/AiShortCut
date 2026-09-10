package com.sonms.aishortcut.core.database

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor

@Database(entities = [SavedArticleEntity::class], version = 1, exportSchema = true)
@ConstructedBy(AiShortCutDatabaseConstructor::class)
abstract class AiShortCutDatabase : RoomDatabase() {
    abstract fun savedArticleDao(): SavedArticleDao
}

// Room's KSP processor generates the actual object per platform (KMP has no
// reflection on native, so the constructor can't be found at runtime).
@Suppress("NO_ACTUAL_FOR_EXPECT")
expect object AiShortCutDatabaseConstructor : RoomDatabaseConstructor<AiShortCutDatabase> {
    override fun initialize(): AiShortCutDatabase
}
