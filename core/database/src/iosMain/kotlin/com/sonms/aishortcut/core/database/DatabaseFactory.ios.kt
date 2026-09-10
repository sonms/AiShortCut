package com.sonms.aishortcut.core.database

import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

@OptIn(ExperimentalForeignApi::class)
internal actual fun databaseBuilder(): RoomDatabase.Builder<AiShortCutDatabase> {
    val documentsDir = NSFileManager.defaultManager.URLForDirectory(
        directory = NSDocumentDirectory,
        inDomain = NSUserDomainMask,
        appropriateForURL = null,
        create = false,
        error = null,
    )
    val path = requireNotNull(documentsDir?.path) { "no documents directory" } + "/aishortcut.db"
    return Room.databaseBuilder<AiShortCutDatabase>(name = path)
}
