package com.sonms.aishortcut.core.database

import androidx.room.Room
import androidx.room.RoomDatabase
import com.sonms.aishortcut.core.common.PlatformContextHolder

internal actual fun databaseBuilder(): RoomDatabase.Builder<AiShortCutDatabase> {
    val context = PlatformContextHolder.get().context.applicationContext
    val dbFile = context.getDatabasePath("aishortcut.db")
    return Room.databaseBuilder<AiShortCutDatabase>(context, dbFile.absolutePath)
}
