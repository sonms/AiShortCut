package com.sonms.aishortcut.data.saved

import com.sonms.aishortcut.data.newsfeed.NewsArticle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

// ponytail: in-memory, session-only. Bookmarks vanish on app restart until the
// Room migration lands -- that's the piece that gives this a disk backing and
// the tag organisation DESIGN/CLAUDE call for. Keep this the single seam the
// rest of the app talks to so swapping the storage is a one-file change.
class SavedRepository {

    private val _articles = MutableStateFlow<List<NewsArticle>>(emptyList())
    val articles: StateFlow<List<NewsArticle>> = _articles.asStateFlow()

    fun isSaved(link: String): Boolean = _articles.value.any { it.link == link }

    // Save if absent, remove if present. Newest save first.
    fun toggle(article: NewsArticle) = _articles.update { current ->
        if (current.any { it.link == article.link }) {
            current.filterNot { it.link == article.link }
        } else {
            listOf(article) + current
        }
    }
}
