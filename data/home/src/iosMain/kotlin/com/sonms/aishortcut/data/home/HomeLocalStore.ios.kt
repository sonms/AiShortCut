package com.sonms.aishortcut.data.home

import platform.Foundation.NSUserDefaults

private const val KEY_VISIT_COUNT = "visit_count"

class IosHomeLocalStore : HomeLocalStore {
    private val defaults = NSUserDefaults.standardUserDefaults

    override fun recordVisit(): Int {
        val next = defaults.integerForKey(KEY_VISIT_COUNT).toInt() + 1
        defaults.setInteger(next.toLong(), KEY_VISIT_COUNT)
        return next
    }
}

actual fun createHomeLocalStore(): HomeLocalStore = IosHomeLocalStore()
