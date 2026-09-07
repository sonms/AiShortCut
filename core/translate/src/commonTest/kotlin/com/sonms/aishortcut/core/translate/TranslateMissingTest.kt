package com.sonms.aishortcut.core.translate

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

// Records what it was asked to translate so the test can assert the cache
// actually skips work.
private class RecordingTranslator : Translator {
    val calls = mutableListOf<List<String>>()
    override suspend fun translateToKorean(texts: List<String>): List<String> {
        calls += texts
        return texts.map { "ko:$it" }
    }
}

class TranslateMissingTest {

    @Test
    fun translatesEverythingOnAnEmptyCache() = runTest {
        val translator = RecordingTranslator()

        val cache = translator.translateMissing(listOf("a", "b"), emptyMap())

        assertEquals(mapOf("a" to "ko:a", "b" to "ko:b"), cache)
        assertEquals(listOf(listOf("a", "b")), translator.calls)
    }

    @Test
    fun onlySendsStringsMissingFromTheCache() = runTest {
        val translator = RecordingTranslator()

        val cache = translator.translateMissing(listOf("a", "b", "c"), mapOf("a" to "ko:a"))

        assertEquals(mapOf("a" to "ko:a", "b" to "ko:b", "c" to "ko:c"), cache)
        assertEquals(listOf(listOf("b", "c")), translator.calls)
    }

    @Test
    fun skipsBlankAndDuplicateStringsAndDoesNotCallWhenNothingIsMissing() = runTest {
        val translator = RecordingTranslator()

        val cache = translator.translateMissing(listOf("a", "a", "", "  "), mapOf("a" to "ko:a"))

        assertEquals(mapOf("a" to "ko:a"), cache)
        assertEquals(emptyList(), translator.calls)
    }

    @Test
    fun dropsResultsThatCameBackUnchanged() = runTest {
        val passthrough = object : Translator {
            override suspend fun translateToKorean(texts: List<String>) = texts
        }

        val cache = passthrough.translateMissing(listOf("Llama 3"), emptyMap())

        assertEquals(emptyMap(), cache)
    }
}
