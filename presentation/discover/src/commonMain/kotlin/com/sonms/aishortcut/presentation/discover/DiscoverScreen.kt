package com.sonms.aishortcut.presentation.discover

import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.sonms.aishortcut.core.designsystem.FeedCard
import com.sonms.aishortcut.core.designsystem.ScreenHeader
import com.sonms.aishortcut.core.designsystem.SectionHeader
import com.sonms.aishortcut.core.designsystem.Spacing
import com.sonms.aishortcut.core.designsystem.StatLine
import com.sonms.aishortcut.data.githubtrending.TrendingRepo
import com.sonms.aishortcut.data.hftrending.TrendingModel
import com.sonms.aishortcut.data.openrouter.OpenRouterModel
import com.sonms.aishortcut.presentation.detail.DetailSheet
import com.sonms.aishortcut.presentation.detail.DetailTarget
import com.sonms.aishortcut.presentation.detail.formatTokens
import com.sonms.aishortcut.presentation.detail.trimZero
import com.sonms.aishortcut.presentation.feed.LanguageToggle
import com.sonms.aishortcut.presentation.feed.feedLocalizer
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun DiscoverScreen(viewModel: DiscoverViewModel = koinViewModel()) {
    Column(Modifier.fillMaxSize()) {
        ScreenHeader(
            title = "Discover",
            trailing = {
                LanguageToggle(
                    selected = viewModel.language,
                    onSelect = { viewModel.language = it },
                )
            },
        )
        OutlinedTextField(
            value = viewModel.query,
            onValueChange = { viewModel.query = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.md),
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
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

            is DiscoverUiState.Content -> Results(
                content = state,
                query = viewModel.query,
                localize = feedLocalizer(viewModel.language, viewModel.translations),
            )
        }
    }
}

@Composable
private fun Results(
    content: DiscoverUiState.Content,
    query: String,
    localize: (String?) -> String?,
) {
    val q = query.trim()
    val models = remember(content, q) {
        if (q.isEmpty()) content.models
        else content.models.filter { it.matches(q) }
    }
    val repos = remember(content, q) {
        if (q.isEmpty()) content.repos
        else content.repos.filter { it.matches(q) }
    }
    var detail by remember { mutableStateOf<DetailTarget?>(null) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = Spacing.md,
            end = Spacing.md,
            bottom = Spacing.md,
        ),
        verticalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        item { BenchmarkPlaceholder() }

        if (models.isNotEmpty()) {
            item { SectionHeader("모델") }
            items(models, key = { "model-${it.id}" }) { model ->
                val enrichment = content.openRouter[model.id.lowercase()]
                FeedCard(
                    modifier = Modifier.clickable {
                        detail = DetailTarget.Model(model, enrichment)
                    },
                ) {
                    ModelRow(model, enrichment)
                }
            }
        }
        if (repos.isNotEmpty()) {
            item { SectionHeader("리포지토리") }
            items(repos, key = { "repo-${it.id}" }) { repo ->
                FeedCard(modifier = Modifier.clickable { detail = DetailTarget.Repo(repo) }) {
                    RepoRow(repo, localize)
                }
            }
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

    detail?.let { target ->
        DetailSheet(
            target = target,
            onDismiss = { detail = null },
            localized = { localize(it) ?: it },
        )
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
            "MMLU, HumanEval 등 벤치마크 비교는 실제 데이터 연동 후 제공됩니다. " +
                "지금은 모델 상세에서 OpenRouter 지능·코딩 지수를 확인할 수 있습니다.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ModelRow(model: TrendingModel, openRouter: OpenRouterModel?) {
    Text(model.id, style = MaterialTheme.typography.titleMedium)
    model.pipelineTag?.let {
        Spacer(Modifier.height(Spacing.xs))
        Text(
            it,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
    openRouter?.let { or ->
        Spacer(Modifier.height(Spacing.sm))
        StatLine(
            parts = listOfNotNull(
                or.contextLength?.let { "컨텍스트 ${formatTokens(it)}" },
                or.intelligenceIndex?.let { "지능 ${trimZero(it)}" },
                or.promptUsdPerMTokens?.let { "입력 $${trimZero(it)}/1M" },
            ),
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

@Composable
private fun RepoRow(repo: TrendingRepo, localize: (String?) -> String?) {
    Text(repo.fullName, style = MaterialTheme.typography.titleMedium)
    localize(repo.description)?.let {
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
        Spacer(Modifier.height(Spacing.sm))
        StatLine(repo.topics)
    }
}

@Composable
private fun Centered(content: @Composable () -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { content() }
}
