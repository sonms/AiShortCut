package com.sonms.aishortcut.data.saved

import com.sonms.aishortcut.core.database.SavedArticleDao
import com.sonms.aishortcut.core.database.SavedArticleEntity
import com.sonms.aishortcut.data.newsfeed.NewsArticle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Room-backed bookmarks. The rest of the app talks to this; the DAO and the
// AiShortCutDatabase behind it live in core:database. Swapping storage stays a
// change to this one file plus core:database.
class SavedRepository(
    private val dao: SavedArticleDao,
) {
    val articles: Flow<List<NewsArticle>> =
        dao.observeAll().map { rows -> rows.map(SavedArticleEntity::toDomain) }

    // Save if absent, remove if present.
    suspend fun toggle(article: NewsArticle) {
        if (dao.exists(article.link)) {
            dao.deleteByLink(article.link)
        } else {
            dao.insert(article.toEntity())
        }
    }
}

internal fun SavedArticleEntity.toDomain() = NewsArticle(
    title = title,
    summary = summary,
    link = link,
    source = source,
    publishedAt = publishedAt,
)

internal fun NewsArticle.toEntity() = SavedArticleEntity(
    link = link,
    title = title,
    summary = summary,
    source = source,
    publishedAt = publishedAt,
)
