package com.sonms.aishortcut.core.common

// iOS has no injection story to write here: UIApplication.shared is already a
// global singleton, so there's nothing for iOSApp.swift to hand in at launch
// the way AiShortCutApplication.onCreate() does on Android.
actual object PlatformContextHolder {
    actual fun get(): PlatformContext = PlatformContext(platform.UIKit.UIApplication.sharedApplication)
}
