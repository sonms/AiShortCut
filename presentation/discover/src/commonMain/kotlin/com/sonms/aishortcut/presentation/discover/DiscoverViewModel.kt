package com.sonms.aishortcut.presentation.discover

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
import com.sonms.aishortcut.data.openrouter.OpenRouterModel
import com.sonms.aishortcut.data.openrouter.OpenRouterRepository
import com.sonms.aishortcut.presentation.feed.FeedLanguage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

sealed interface DiscoverUiState {
    data object Loading : DiscoverUiState
    data class Content(
        val models: List<TrendingModel>,
        val repos: List<TrendingRepo>,
        // HF model id (lowercased) -> OpenRouter pricing/benchmark data, for the
        // subset of trending models that OpenRouter also serves. Empty if the
        // OpenRouter call failed.
        val openRouter: Map<String, OpenRouterModel>,
    ) : DiscoverUiState
    data class Error(val message: String) : DiscoverUiState
}

class DiscoverViewModel(
    private val hfTrending: HfTrendingRepository,
    private val githubTrending: GithubTrendingRepository,
    private val openRouter: OpenRouterRepository,
    private val translator: Translator,
) : ViewModel() {
    var uiState by mutableStateOf<DiscoverUiState>(DiscoverUiState.Loading)
        private set

    // The search box. Filtering happens in the screen against uiState.
    var query by mutableStateOf("")

    // Pure view state, no invariant -- the screen sets it directly.
    var language by mutableStateOf(FeedLanguage.Korean)

    // original English string -> Korean. Session-scoped cache, same as Home:
    // survives reloads and language toggles, only new strings hit the translator.
    var translations by mutableStateOf<Map<String, String>>(emptyMap())
        private set

    init {
        load()
    }

    fun load() {
        uiState = DiscoverUiState.Loading
        viewModelScope.launch {
            val content = try {
                coroutineScope {
                    val models = async { hfTrending.getTrendingModels() }
                    val repos = async { githubTrending.getTrendingRepos() }
                    // Enrichment only -- a failure here must not blank the list.
                    val enrichment = async {
                        try {
                            openRouter.getModelsByHuggingFaceId()
                        } catch (e: CancellationException) {
                            throw e
                        } catch (e: Exception) {
                            Logger.withTag("DiscoverViewModel").w(e) { "openrouter enrich failed" }
                            emptyMap<String, OpenRouterModel>()
                        }
                    }
                    DiscoverUiState.Content(models.await(), repos.await(), enrichment.await())
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Logger.withTag("DiscoverViewModel").e(e) { "discover load failed" }
                uiState = DiscoverUiState.Error(e.message ?: "Couldn't load")
                return@launch
            }
            uiState = content
            translate(content)
        }
    }

    // Runs after results are on screen -- on-device translation plus a first-run
    // model download is too slow to block the load path.
    private suspend fun translate(content: DiscoverUiState.Content) {
        val texts = content.repos.mapNotNull { it.description }
        try {
            translations = translator.translateMissing(texts, translations)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Logger.withTag("DiscoverViewModel").w(e) { "translation failed; showing originals" }
        }
    }
}
