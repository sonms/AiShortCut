package com.sonms.aishortcut.data.home

// presentation:home talks to this, never to HomeLocalStore or the
// android.content.SharedPreferences / NSUserDefaults behind it directly.
class HomeRepository(
    private val localStore: HomeLocalStore = createHomeLocalStore()
) {
    fun recordVisit(): Int = localStore.recordVisit()
}
