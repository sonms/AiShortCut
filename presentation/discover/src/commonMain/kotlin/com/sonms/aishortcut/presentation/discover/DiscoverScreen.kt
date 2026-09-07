package com.sonms.aishortcut.presentation.discover

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
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import com.sonms.aishortcut.core.designsystem.FeedCard
import com.sonms.aishortcut.core.designsystem.SectionHeader
import com.sonms.aishortcut.core.designsystem.Spacing
import com.sonms.aishortcut.data.githubtrending.TrendingRepo
import com.sonms.aishortcut.data.hftrending.TrendingModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun DiscoverScreen(viewModel: DiscoverViewModel = koinViewModel()) {
    Column(Modifier.fillMaxSize().padding(Spacing.md)) {
        OutlinedTextField(
            value = viewModel.query,
            onValueChange = { viewModel.query = it },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            placeholder = { Text("모델, 레포, 토픽 검색 (RAG, LoRA…)") },
        )
        Spacer(Modifier.height(Spacing.sm))

        when (val state = viewModel.uiState) {
            DiscoverUiState.Loading -> Centered { CircularProgressIndicator() }

            is DiscoverUiState.Error -> Centered {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(state.message, style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(Spacing.md))
                    Button(onClick = viewModel::load) { Text("Retry") }
                }
            }

            is DiscoverUiState.Content -> Results(state, viewModel.query)
        }
    }
}

@Composable
private fun Results(content: DiscoverUiState.Content, query: String) {
    val q = query.trim()
    val models = remember(content, q) {
        if (q.isEmpty()) content.models
        else content.models.filter { it.matches(q) }
    }
    val repos = remember(content, q) {
        if (q.isEmpty()) content.repos
        else content.repos.filter { it.matches(q) }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = Spacing.md),
        verticalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        item { BenchmarkPlaceholder() }

        if (models.isNotEmpty()) {
            item { SectionHeader("Models") }
            items(models, key = { "model-${it.id}" }) { ModelRow(it) }
        }
        if (repos.isNotEmpty()) {
            item { SectionHeader("Repos") }
            items(repos, key = { "repo-${it.id}" }) { RepoRow(it) }
        }
        if (models.isEmpty() && repos.isEmpty()) {
            item {
                Text(
                    "\"$query\"에 대한 결과가 없습니다",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = Spacing.md),
                )
            }
        }
    }
}

private fun TrendingModel.matches(q: String): Boolean =
    id.contains(q, ignoreCase = true) || pipelineTag?.contains(q, ignoreCase = true) == true

private fun TrendingRepo.matches(q: String): Boolean =
    fullName.contains(q, ignoreCase = true) ||
        description?.contains(q, ignoreCase = true) == true ||
        topics.any { it.contains(q, ignoreCase = true) }

@Composable
private fun BenchmarkPlaceholder() {
    FeedCard {
        Text("벤치마크 그래프", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(Spacing.xs))
        Text(
            "MMLU, HumanEval 등 벤치마크 비교는 실제 데이터 연동 후 제공됩니다.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ModelRow(model: TrendingModel) {
    FeedCard {
        Text(model.id, style = MaterialTheme.typography.titleMedium)
        model.pipelineTag?.let {
            Spacer(Modifier.height(Spacing.xs))
            Text(
                it,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun RepoRow(repo: TrendingRepo) {
    FeedCard {
        Text(repo.fullName, style = MaterialTheme.typography.titleMedium)
        repo.description?.let {
            Spacer(Modifier.height(Spacing.xs))
            Text(
                it,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if (repo.topics.isNotEmpty()) {
            Spacer(Modifier.height(Spacing.xs))
            Text(
                repo.topics.joinToString(" · "),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun Centered(content: @Composable () -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { content() }
}
