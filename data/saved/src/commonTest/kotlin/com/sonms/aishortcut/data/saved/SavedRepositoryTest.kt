package com.sonms.aishortcut.data.saved

import com.sonms.aishortcut.core.database.SavedArticleDao
import com.sonms.aishortcut.core.database.SavedArticleEntity
import com.sonms.aishortcut.core.database.SavedModelDao
import com.sonms.aishortcut.core.database.SavedModelEntity
import com.sonms.aishortcut.data.hftrending.TrendingModel
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

private fun model(id: String) = TrendingModel(
    id = id, author = "a", likes = 1, downloads = 2, pipelineTag = "text-generation",
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

// Same contract as FakeSavedArticleDao, mirrored for SavedModelDao.
private class FakeSavedModelDao : SavedModelDao {
    private val rows = MutableStateFlow<List<SavedModelEntity>>(emptyList())
    private var nextId = 1L

    override fun observeAll(): Flow<List<SavedModelEntity>> =
        rows.map { list -> list.sortedByDescending { it.id } }

    override suspend fun exists(modelId: String): Boolean = rows.value.any { it.modelId == modelId }

    override suspend fun insert(entity: SavedModelEntity) {
        if (rows.value.any { it.modelId == entity.modelId }) return
        rows.value = rows.value + entity.copy(id = nextId++)
    }

    override suspend fun deleteByModelId(modelId: String) {
        rows.value = rows.value.filterNot { it.modelId == modelId }
    }
}

private fun repository() = SavedRepository(FakeSavedArticleDao(), FakeSavedModelDao())

class SavedRepositoryTest {

    @Test
    fun togglesAnArticleOnThenOff() = runTest {
        val repo = repository()
        val a = article("https://e.com/a")

        repo.toggle(a)
        assertEquals(listOf(a.link), repo.articles.first().map { it.link })

        repo.toggle(a)
        assertEquals(emptyList(), repo.articles.first())
    }

    @Test
    fun keepsNewestSaveFirstAndDedupesByLink() = runTest {
        val repo = repository()
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

    @Test
    fun togglesAModelOnThenOff() = runTest {
        val repo = repository()
        val m = model("org/model-a")

        repo.toggle(m)
        assertEquals(listOf(m.id), repo.models.first().map { it.id })

        repo.toggle(m)
        assertEquals(emptyList(), repo.models.first())
    }

    @Test
    fun keepsNewestSavedModelFirstAndDedupesById() = runTest {
        val repo = repository()
        val a = model("org/model-a")
        val b = model("org/model-b")

        repo.toggle(a)
        repo.toggle(b)
        assertEquals(listOf(b.id, a.id), repo.models.first().map { it.id })

        repo.toggle(a)
        assertEquals(listOf(b.id), repo.models.first().map { it.id })
    }

    @Test
    fun modelMappingRoundTrips() {
        val m = model("org/model-a")
        assertEquals(m, m.toEntity().toDomain())
    }
}
