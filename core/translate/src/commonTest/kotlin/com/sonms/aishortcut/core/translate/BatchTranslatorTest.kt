package com.sonms.aishortcut.core.translate

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

private class FakeBatchTranslator(
    private val failOn: Set<String> = emptySet(),
) : BatchTranslator() {
    override suspend fun translateOne(text: String): String {
        if (text in failOn) error("boom")
        return "[ko]$text"
    }
}

class BatchTranslatorTest {

    @Test
    fun translatesEveryStringInOrder() = runTest {
        val out = FakeBatchTranslator().translateToKorean(listOf("a", "b", "c"))
        assertEquals(listOf("[ko]a", "[ko]b", "[ko]c"), out)
    }

    @Test
    fun keepsOriginalForStringsThatFailAndStaysIndexAligned() = runTest {
        val out = FakeBatchTranslator(failOn = setOf("b")).translateToKorean(listOf("a", "b", "c"))
        assertEquals(listOf("[ko]a", "b", "[ko]c"), out)
    }

    @Test
    fun passesBlankStringsThroughWithoutCallingTranslate() = runTest {
        // "" is in failOn, so if the loop tried to translate it the result
        // would still be "" -- but a blank " " must also come back untouched.
        val out = FakeBatchTranslator(failOn = setOf("")).translateToKorean(listOf("", "  ", "x"))
        assertEquals(listOf("", "  ", "[ko]x"), out)
    }

    @Test
    fun passthroughReturnsInputUnchanged() = runTest {
        val input = listOf("hello", "world")
        assertEquals(input, PassthroughTranslator.translateToKorean(input))
    }
}
