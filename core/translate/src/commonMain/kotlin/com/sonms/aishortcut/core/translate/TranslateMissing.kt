package com.sonms.aishortcut.core.translate

// Translates the strings in [texts] that aren't already in [cache] and returns
// the cache with the new results merged in. Blank strings, and strings whose
// translation comes back unchanged (proper nouns, or the passthrough iOS
// binding), are left out -- callers read a translation as `cache[text] ?: text`.
//
// This is the session-level translation cache: pass the previous result back in
// on the next call and only genuinely new strings hit the translator.
suspend fun Translator.translateMissing(
    texts: List<String>,
    cache: Map<String, String>,
): Map<String, String> {
    val missing = texts.filterNot { it.isBlank() || it in cache }.distinct()
    if (missing.isEmpty()) return cache

    val translated = translateToKorean(missing)
    val additions = missing.zip(translated).filter { (original, korean) -> original != korean }
    return cache + additions
}
