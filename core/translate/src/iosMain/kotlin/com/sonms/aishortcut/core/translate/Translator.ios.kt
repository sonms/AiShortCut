package com.sonms.aishortcut.core.translate

// ponytail: passthrough until the Apple Translation framework actual lands.
// That implementation belongs in the iosApp Swift layer -- TranslationSession
// is vended through a SwiftUI .translationTask modifier, so a hidden host view
// owns the session and exposes an async translate call that gets injected back
// through this factory. Deployment target is already iOS 18.2, which has the
// full programmatic API.
actual fun createTranslator(): Translator = PassthroughTranslator
