package com.sonms.aishortcut.presentation.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.sonms.aishortcut.data.home.HomeRepository

class HomeViewModel(
    private val repository: HomeRepository,
) : ViewModel() {
    var visitCount by mutableStateOf(0)
        private set

    init {
        visitCount = repository.recordVisit()
    }
}
