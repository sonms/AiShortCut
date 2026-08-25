package com.sonms.aishortcut.presentation.home

import com.sonms.aishortcut.core.common.getPlatform

class Greeting {
    private val platform = getPlatform()

    fun greet(): String {
        return sayHello(platform.name)
    }
}
