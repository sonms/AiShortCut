package com.sonms.aishortcut.core.common

// A handle to "the running app" that only makes sense per-platform: Android's
// Context on Android, UIApplication on iOS. Anything that needs it (like
// data:home's SharedPreferences-backed store) goes through PlatformContextHolder
// below instead of importing android.content.Context directly, so that code
// still compiles for iOS.
expect class PlatformContext
