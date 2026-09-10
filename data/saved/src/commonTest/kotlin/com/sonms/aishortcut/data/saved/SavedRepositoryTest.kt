package com.sonms.aishortcut.data.saved

import com.sonms.aishortcut.core.database.SavedArticleDao
import com.sonms.aishortcut.core.database.SavedArticleEntity
import com.sonms.aishortcut.data.newsfeed.NewsArticle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

private fun article(link: String) = NewsArticle(
    title = link, summary = "", link = link, source = "s", publishedAt = null,
    keywords = listOf("k"),
)

// In-memory stand-in for the Room DAO: newest id first, unique link, IGNORE on
// conflict -- the same contract SavedArticleDao's annotations declare.
private class FakeSavedArticleDao : SavedArticleDao {
    private val rows = MutableStateFlow<List<SavedArticleEntity>>(emptyList())
    private var nextId = 1L

    override fun observeAll(): Flow<List<SavedArticleEntity>> =
        rows.map { list -> list.sortedByDescending { it.id } }

    override suspend fun exists(link: String): Boolean = rows.value.any { it.link == link }

    override suspend fun insert(entity: SavedArticleEntity) {
        if (rows.value.any { it.link == entity.link }) return
        rows.value = rows.value + entity.copy(id = nextId++)
    }

    override suspend fun deleteByLink(link: String) {
        rows.value = rows.value.filterNot { it.link == link }
    }
}

class SavedRepositoryTest {

    @Test
    fun togglesAnArticleOnThenOff() = runTest {
        val repo = SavedRepository(FakeSavedArticleDao())
        val a = article("https://e.com/a")

        repo.toggle(a)
        assertEquals(listOf(a.link), repo.articles.first().map { it.link })

        repo.toggle(a)
        assertEquals(emptyList(), repo.articles.first())
    }

    @Test
    fun keepsNewestSaveFirstAndDedupesByLink() = runTest {
        val repo = SavedRepository(FakeSavedArticleDao())
        val a = article("https://e.com/a")
        val b = article("https://e.com/b")

        repo.toggle(a)
        repo.toggle(b)
        assertEquals(listOf(b.link, a.link), repo.articles.first().map { it.link })

        repo.toggle(a)
        assertEquals(listOf(b.link), repo.articles.first().map { it.link })
    }

    @Test
    fun mappingDropsKeywordsButKeepsTheRest() {
        val a = article("https://e.com/a")
        assertEquals(
            a.copy(keywords = emptyList()),
            a.toEntity().toDomain(),
        )
    }
}
