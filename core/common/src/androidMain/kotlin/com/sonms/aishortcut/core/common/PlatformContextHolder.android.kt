package com.sonms.aishortcut.core.common

import android.annotation.SuppressLint

// Android has no global "current Context" -- something has to hand one in.
// AiShortCutApplication.onCreate() calls initialize() once at process start;
// everything else in the KMP code just calls get().
actual object PlatformContextHolder {
    // Lint's StaticFieldLeak check can't see that initialize() already
    // unwraps to applicationContext below, so it flags this as if an
    // Activity-scoped Context could end up here. It can't -- suppressed.
    @SuppressLint("StaticFieldLeak")
    private var appContext: PlatformContext? = null

    fun initialize(context: android.content.Context) {
        appContext = PlatformContext(context.applicationContext)
    }

    actual fun get(): PlatformContext =
        appContext ?: error("PlatformContextHolder.initialize() was never called")
}
