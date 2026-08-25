package com.sonms.aishortcut.data.home

import com.sonms.aishortcut.core.common.PlatformContextHolder

private const val PREFS_NAME = "home_prefs"
private const val KEY_VISIT_COUNT = "visit_count"

class AndroidHomeLocalStore : HomeLocalStore {
    override fun recordVisit(): Int {
        val prefs = PlatformContextHolder.get().context
            .getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE)
        val next = prefs.getInt(KEY_VISIT_COUNT, 0) + 1
        prefs.edit().putInt(KEY_VISIT_COUNT, next).apply()
        return next
    }
}

actual fun createHomeLocalStore(): HomeLocalStore = AndroidHomeLocalStore()
