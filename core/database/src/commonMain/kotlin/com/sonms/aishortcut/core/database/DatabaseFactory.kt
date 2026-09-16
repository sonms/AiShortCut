package com.sonms.aishortcut.core.database

import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver

// Per-platform: Android needs a Context + file path, iOS an NSDocumentDirectory
// path. Everything after the builder is shared.
internal expect fun databaseBuilder(): RoomDatabase.Builder<AiShortCutDatabase>

// The one entry point the app wires into Koin. Bundled SQLite so the driver is
// identical on both platforms.
fun createDatabase(): AiShortCutDatabase =
    databaseBuilder()
        .setDriver(BundledSQLiteDriver())
        // Pre-release, single-developer app (see CLAUDE.md) -- no user data to
        // preserve across schema bumps yet, so skip writing real Migrations.
        .fallbackToDestructiveMigration(dropAllTables = true)
        .build()
