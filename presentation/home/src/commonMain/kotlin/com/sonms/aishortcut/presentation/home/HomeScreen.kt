package com.sonms.aishortcut.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.sonms.aishortcut.core.designsystem.Spacing
import com.sonms.aishortcut.data.githubtrending.TrendingRepo
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

        is HomeUiState.Content -> HomeFeed(state.models, state.repos)
    }
}

@Composable
private fun HomeFeed(models: List<TrendingModel>, repos: List<TrendingRepo>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(Spacing.md),
        verticalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        item { SectionHeader("Trending models") }
        items(models, key = { "model-${it.id}" }) { TrendingModelCard(it) }

        item { SectionHeader("Trending AI repos") }
        items(repos, key = { "repo-${it.id}" }) { TrendingRepoCard(it) }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.headlineSmall,
        modifier = Modifier.padding(top = Spacing.sm, bottom = Spacing.xs),
    )
}

@Composable
private fun TrendingModelCard(model: TrendingModel) {
    FeedCard {
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
private fun TrendingRepoCard(repo: TrendingRepo) {
    FeedCard {
        Text(repo.fullName, style = MaterialTheme.typography.titleMedium)
        repo.description?.let { description ->
            Spacer(Modifier.height(Spacing.xs))
            Text(
                description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Spacer(Modifier.height(Spacing.sm))
        Text(
            buildString {
                append("stars ${repo.stars} · forks ${repo.forks}")
                repo.language?.let { append(" · $it") }
            },
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun FeedCard(content: @Composable ColumnScope.() -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(CardShape)
            .border(1.dp, MaterialTheme.colorScheme.outline, CardShape)
            .background(MaterialTheme.colorScheme.surface)
            .padding(Spacing.md),
        content = content,
    )
}

@Composable
private fun Centered(content: @Composable () -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { content() }
}
