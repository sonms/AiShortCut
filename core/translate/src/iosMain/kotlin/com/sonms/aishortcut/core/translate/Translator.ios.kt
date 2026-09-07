package com.sonms.aishortcut.core.translate

import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

// Apple's Translation framework is Swift-only and its session is vended through
// a SwiftUI `.translationTask` modifier, so the actual translation lives in the
// iosApp Swift layer. It registers a backend here at launch (see
// iosApp/iosApp/AppleTranslator.swift). Until then -- and on the Simulator,
// where the Translation framework doesn't run -- translation returns the input
// unchanged.

interface IosTranslatorBackend {
    // Non-suspend + callback so Swift can implement it (Swift cannot implement a
    // Kotlin `suspend` function). Call [onResult] with the Korean strings
    // index-aligned to [texts] -- or with [texts] itself on any failure. Never
    // return a different count.
    fun translate(texts: List<String>, onResult: (List<String>) -> Unit)
}

object IosTranslatorHolder {
    var backend: IosTranslatorBackend? = null
}

private class IosTranslator : Translator {
    override suspend fun translateToKorean(texts: List<String>): List<String> {
        val backend = IosTranslatorHolder.backend ?: return texts
        return suspendCoroutine { continuation ->
            backend.translate(texts) { result ->
                continuation.resume(if (result.size == texts.size) result else texts)
            }
        }
    }
}

// Reads IosTranslatorHolder.backend on every call, so Swift can register the
// backend after Koin has already built this instance.
actual fun createTranslator(): Translator = IosTranslator()
