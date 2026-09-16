package com.sonms.aishortcut.data.saved

import com.sonms.aishortcut.core.database.SavedArticleDao
import com.sonms.aishortcut.core.database.SavedArticleEntity
import com.sonms.aishortcut.core.database.SavedModelDao
import com.sonms.aishortcut.core.database.SavedModelEntity
import com.sonms.aishortcut.data.hftrending.TrendingModel
import com.sonms.aishortcut.data.newsfeed.NewsArticle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Room-backed bookmarks. The rest of the app talks to this; the DAOs and the
// AiShortCutDatabase behind them live in core:database. Swapping storage stays a
// change to this one file plus core:database.
class SavedRepository(
    private val articleDao: SavedArticleDao,
    private val modelDao: SavedModelDao,
) {
    val articles: Flow<List<NewsArticle>> =
        articleDao.observeAll().map { rows -> rows.map(SavedArticleEntity::toDomain) }

    val models: Flow<List<TrendingModel>> =
        modelDao.observeAll().map { rows -> rows.map(SavedModelEntity::toDomain) }

    // Save if absent, remove if present.
    suspend fun toggle(article: NewsArticle) {
        if (articleDao.exists(article.link)) {
            articleDao.deleteByLink(article.link)
        } else {
            articleDao.insert(article.toEntity())
        }
    }

    // Save if absent, remove if present.
    suspend fun toggle(model: TrendingModel) {
        if (modelDao.exists(model.id)) {
            modelDao.deleteByModelId(model.id)
        } else {
            modelDao.insert(model.toEntity())
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

internal fun SavedModelEntity.toDomain() = TrendingModel(
    id = modelId,
    author = author,
    likes = likes,
    downloads = downloads,
    pipelineTag = pipelineTag,
)

internal fun TrendingModel.toEntity() = SavedModelEntity(
    modelId = id,
    author = author,
    likes = likes,
    downloads = downloads,
    pipelineTag = pipelineTag,
)
