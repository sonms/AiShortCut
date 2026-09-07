package com.sonms.aishortcut.core.translate

import co.touchlab.kermit.Logger
import kotlin.coroutines.cancellation.CancellationException

// Translates short feed text (article titles, abstract snippets) into Korean,
// fully on device. Source language is assumed to be English -- every feed wired
// so far (arXiv, AI news) publishes in English; add a source parameter here if
// that stops being true.
interface Translator {

    // Result is index-aligned with [texts]. Any element that fails to translate
    // comes back as its original string rather than throwing, so callers can
    // show the untranslated text with a retry affordance.
    suspend fun translateToKorean(texts: List<String>): List<String>
}

// Per-string translation plus the shared fallback contract. Platform
// translators implement translateOne(); the batch loop and the
// "keep the original on failure" rule live here so one commonTest covers them.
abstract class BatchTranslator : Translator {

    protected abstract suspend fun translateOne(text: String): String

    final override suspend fun translateToKorean(texts: List<String>): List<String> =
        // ponytail: sequential. A feed page is ~25 short strings and on-device
        // translation is fast; parallelise only if a profile says to.
        texts.map { text ->
            if (text.isBlank()) return@map text
            try {
                translateOne(text)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Logger.withTag("Translator").w(e) { "translation failed, keeping original" }
                text
            }
        }
}

// Returns every string unchanged. This is the iOS binding until the Apple
// Translation framework actual is wired in, and a safe default anywhere
// translation isn't available.
object PassthroughTranslator : Translator {
    override suspend fun translateToKorean(texts: List<String>): List<String> = texts
}

expect fun createTranslator(): Translator
