package com.sonms.aishortcut.presentation.discover

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import com.sonms.aishortcut.data.githubtrending.GithubTrendingRepository
import com.sonms.aishortcut.data.githubtrending.TrendingRepo
import com.sonms.aishortcut.data.hftrending.HfTrendingRepository
import com.sonms.aishortcut.data.hftrending.TrendingModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

sealed interface DiscoverUiState {
    data object Loading : DiscoverUiState
    data class Content(
        val models: List<TrendingModel>,
        val repos: List<TrendingRepo>,
    ) : DiscoverUiState
    data class Error(val message: String) : DiscoverUiState
}

class DiscoverViewModel(
    private val hfTrending: HfTrendingRepository,
    private val githubTrending: GithubTrendingRepository,
) : ViewModel() {
    var uiState by mutableStateOf<DiscoverUiState>(DiscoverUiState.Loading)
        private set

    // The search box. Filtering happens in the screen against uiState.
    var query by mutableStateOf("")

    init {
        load()
    }

    fun load() {
        uiState = DiscoverUiState.Loading
        viewModelScope.launch {
            uiState = try {
                coroutineScope {
                    val models = async { hfTrending.getTrendingModels() }
                    val repos = async { githubTrending.getTrendingRepos() }
                    DiscoverUiState.Content(models.await(), repos.await())
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Logger.withTag("DiscoverViewModel").e(e) { "discover load failed" }
                DiscoverUiState.Error(e.message ?: "Couldn't load")
            }
        }
    }
}
