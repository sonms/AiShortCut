package com.sonms.aishortcut.presentation.feed

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class FeedLocalizerTest {

    private val cache = mapOf("Hello" to "안녕")

    @Test
    fun englishModeReturnsOriginal() {
        val localize = feedLocalizer(FeedLanguage.English, cache)
        assertEquals("Hello", localize("Hello"))
    }

    @Test
    fun koreanModeReturnsTranslationWhenCached() {
        val localize = feedLocalizer(FeedLanguage.Korean, cache)
        assertEquals("안녕", localize("Hello"))
    }

    @Test
    fun koreanModeFallsBackToOriginalWhenMissing() {
        val localize = feedLocalizer(FeedLanguage.Korean, cache)
        assertEquals("World", localize("World"))
    }

    @Test
    fun nullStaysNull() {
        assertNull(feedLocalizer(FeedLanguage.Korean, cache)(null))
    }
}
