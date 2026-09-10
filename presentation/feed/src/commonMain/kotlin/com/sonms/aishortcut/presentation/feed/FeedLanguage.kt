package com.sonms.aishortcut.presentation.feed

// Which language the feed shows for free-text content (news, repo descriptions).
// English is always the original; Korean falls back to the original per string
// until its translation is in the translations cache.
enum class FeedLanguage(val label: String) {
    English("EN"),
    Korean("한국어"),
}

// The per-string lookup a feed screen applies to free text: the original in
// English mode, the cached translation (or the original as fallback) in Korean
// mode. `translations` is the session cache from `Translator.translateMissing`.
fun feedLocalizer(
    language: FeedLanguage,
    translations: Map<String, String>,
): (String?) -> String? = { text ->
    when {
        text == null -> null
        language == FeedLanguage.Korean -> translations[text] ?: text
        else -> text
    }
}
