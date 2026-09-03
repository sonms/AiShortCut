package com.sonms.aishortcut.core.common

import android.annotation.SuppressLint

actual object PlatformContextHolder {
    @SuppressLint("StaticFieldLeak")
    private var appContext: PlatformContext? = null

    fun initialize(context: android.content.Context) {
        appContext = PlatformContext(context.applicationContext)
    }

    actual fun get(): PlatformContext =
        appContext ?: error("PlatformContextHolder.initialize() was never called")
}
