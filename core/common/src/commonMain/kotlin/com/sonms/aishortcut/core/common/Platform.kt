package com.sonms.aishortcut.core.common

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform
