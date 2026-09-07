package com.sonms.aishortcut.presentation.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import com.sonms.aishortcut.core.translate.Translator
import com.sonms.aishortcut.core.translate.translateMissing
import com.sonms.aishortcut.data.githubtrending.GithubTrendingRepository
import com.sonms.aishortcut.data.githubtrending.TrendingRepo
import com.sonms.aishortcut.data.hftrending.HfTrendingRepository
import com.sonms.aishortcut.data.hftrending.TrendingModel
import com.sonms.aishortcut.data.newsfeed.NewsArticle
import com.sonms.aishortcut.data.newsfeed.NewsFeedRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

sealed interface HomeUiState {
    data object Loading : HomeUiState
    data class Content(
        val models: List<TrendingModel>,
        val repos: List<TrendingRepo>,
        val articles: List<NewsArticle>,
    ) : HomeUiState
    data class Error(val message: String) : HomeUiState
}

// Which language the feed shows for free-text content (news titles/summaries,
// repo descriptions). English is always the original; Korean falls back to the
// original per string until its translation is in `translations`.
enum class FeedLanguage(val label: String) {
    English("EN"),
    Korean("한국어"),
}

class HomeViewModel(
    private val hfTrending: HfTrendingRepository,
    private val githubTrending: GithubTrendingRepository,
    private val newsFeed: NewsFeedRepository,
    private val translator: Translator,
) : ViewModel() {
    var uiState by mutableStateOf<HomeUiState>(HomeUiState.Loading)
        private set

    // Pure view state, no invariant -- the screen sets it directly.
    var language by mutableStateOf(FeedLanguage.Korean)

    // original English string -> Korean. Session-scoped cache; survives feed
    // reloads and language toggles, only genuinely new strings hit the translator.
    var translations by mutableStateOf<Map<String, String>>(emptyMap())
        private set

    init {
        load()
    }

    fun load() {
        uiState = HomeUiState.Loading
        viewModelScope.launch {
            val content = try {
                // ponytail: all-or-nothing. If one source turns flaky enough to
                // matter, give each section its own state instead.
                coroutineScope {
                    val models = async { hfTrending.getTrendingModels() }
                    val repos = async { githubTrending.getTrendingRepos() }
                    val articles = async { newsFeed.getArticles() }
                    HomeUiState.Content(models.await(), repos.await(), articles.await())
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Logger.withTag("HomeViewModel").e(e) { "home feed load failed" }
                uiState = HomeUiState.Error(e.message ?: "Couldn't load the feed")
                return@launch
            }
            uiState = content
            translate(content)
        }
    }

    // Runs after the feed is on screen -- on-device translation of a whole page,
    // plus a first-run model download, is too slow to block the load path.
    private suspend fun translate(content: HomeUiState.Content) {
        val texts = buildList {
            content.articles.forEach { add(it.title); add(it.summary) }
            content.repos.forEach { it.description?.let(::add) }
        }
        try {
            translations = translator.translateMissing(texts, translations)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Logger.withTag("HomeViewModel").w(e) { "translation failed; showing originals" }
        }
    }
}
