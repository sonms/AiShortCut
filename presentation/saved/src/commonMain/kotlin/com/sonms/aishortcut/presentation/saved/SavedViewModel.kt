package com.sonms.aishortcut.presentation.saved

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sonms.aishortcut.data.newsfeed.NewsArticle
import com.sonms.aishortcut.data.saved.SavedRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class SavedViewModel(
    private val saved: SavedRepository,
) : ViewModel() {

    val articles: StateFlow<List<NewsArticle>> =
        saved.articles.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun remove(article: NewsArticle) = saved.toggle(article)
}
