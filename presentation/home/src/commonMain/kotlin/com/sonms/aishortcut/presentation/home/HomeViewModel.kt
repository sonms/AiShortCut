package com.sonms.aishortcut.presentation.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import com.sonms.aishortcut.data.hftrending.HfTrendingRepository
import com.sonms.aishortcut.data.hftrending.TrendingModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

sealed interface HomeUiState {
    data object Loading : HomeUiState
    data class Content(val models: List<TrendingModel>) : HomeUiState
    data class Error(val message: String) : HomeUiState
}

class HomeViewModel(
    private val repository: HfTrendingRepository,
) : ViewModel() {
    var uiState by mutableStateOf<HomeUiState>(HomeUiState.Loading)
        private set

    init {
        load()
    }

    fun load() {
        uiState = HomeUiState.Loading
        viewModelScope.launch {
            uiState = try {
                HomeUiState.Content(repository.getTrendingModels())
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Logger.withTag("HomeViewModel").e(e) { "trending model load failed" }
                HomeUiState.Error(e.message ?: "Couldn't load trending models")
            }
        }
    }
}
