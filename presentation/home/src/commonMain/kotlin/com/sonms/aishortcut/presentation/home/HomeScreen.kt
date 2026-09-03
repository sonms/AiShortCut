package com.sonms.aishortcut.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.sonms.aishortcut.core.designsystem.Spacing
import com.sonms.aishortcut.data.hftrending.TrendingModel
import org.koin.compose.viewmodel.koinViewModel

// DESIGN.md: card corner 16dp, 1dp outline border instead of elevation,
// space-md inner padding, space-sm between cards, space-md screen margin.
private val CardShape = RoundedCornerShape(16.dp)

@Composable
fun HomeScreen(viewModel: HomeViewModel = koinViewModel()) {
    when (val state = viewModel.uiState) {
        HomeUiState.Loading -> Centered { CircularProgressIndicator() }

        is HomeUiState.Error -> Centered {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(state.message, style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(Spacing.md))
                Button(onClick = viewModel::load) { Text("Retry") }
            }
        }

        is HomeUiState.Content -> TrendingList(state.models)
    }
}

@Composable
private fun TrendingList(models: List<TrendingModel>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(Spacing.md),
        verticalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        item {
            Text(
                "Trending models",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = Spacing.sm),
            )
        }
        items(models, key = { it.id }) { TrendingModelCard(it) }
    }
}

@Composable
private fun TrendingModelCard(model: TrendingModel) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(CardShape)
            .border(1.dp, MaterialTheme.colorScheme.outline, CardShape)
            .background(MaterialTheme.colorScheme.surface)
            .padding(Spacing.md),
    ) {
        Text(model.id, style = MaterialTheme.typography.titleMedium)
        model.pipelineTag?.let { tag ->
            Spacer(Modifier.height(Spacing.xs))
            Text(
                tag,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(Modifier.height(Spacing.sm))
        Text(
            "likes ${model.likes} · downloads ${model.downloads}",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun Centered(content: @Composable () -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { content() }
}
