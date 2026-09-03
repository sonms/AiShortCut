package com.sonms.aishortcut.presentation.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.koin.compose.viewmodel.koinViewModel

// Stub. Real content (trending-model list from HfTrendingRepository, loading /
// error states, DESIGN.md cards) lands in step 3.
@Composable
fun HomeScreen(viewModel: HomeViewModel = koinViewModel()) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            "Home — visited ${viewModel.visitCount} times",
            style = MaterialTheme.typography.headlineSmall,
        )
    }
}
