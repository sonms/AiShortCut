package com.sonms.aishortcut.data.home

interface HomeLocalStore {
    // Persists one more visit and returns the new total.
    fun recordVisit(): Int
}

expect fun createHomeLocalStore(): HomeLocalStore
