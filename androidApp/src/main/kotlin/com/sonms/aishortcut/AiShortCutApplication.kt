package com.sonms.aishortcut

import android.app.Application
import com.sonms.aishortcut.core.common.PlatformContextHolder

class AiShortCutApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        PlatformContextHolder.initialize(this)
    }
}
