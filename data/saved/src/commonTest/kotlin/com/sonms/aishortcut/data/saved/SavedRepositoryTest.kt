package com.sonms.aishortcut.data.saved

import com.sonms.aishortcut.data.newsfeed.NewsArticle
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

private fun article(link: String) = NewsArticle(
    title = link, summary = "", link = link, source = "s", publishedAt = null,
)

class SavedRepositoryTest {

    @Test
    fun togglesAnArticleOnThenOff() {
        val repo = SavedRepository()
        val a = article("https://e.com/a")

        repo.toggle(a)
        assertTrue(repo.isSaved(a.link))
        assertEquals(listOf(a), repo.articles.value)

        repo.toggle(a)
        assertFalse(repo.isSaved(a.link))
        assertEquals(emptyList(), repo.articles.value)
    }

    @Test
    fun keepsNewestSaveFirstAndDedupesByLink() {
        val repo = SavedRepository()
        val a = article("https://e.com/a")
        val b = article("https://e.com/b")

        repo.toggle(a)
        repo.toggle(b)
        assertEquals(listOf(b, a), repo.articles.value)

        // toggling an already-saved link removes it, never duplicates
        repo.toggle(a)
        assertEquals(listOf(b), repo.articles.value)
    }
}
