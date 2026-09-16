package com.sonms.aishortcut.presentation.discover

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
import com.sonms.aishortcut.presentation.feed.FeedLanguage
import com.sonms.aishortcut.presentation.feed.LanguageToggle
import com.sonms.aishortcut.presentation.feed.feedLocalizer
import com.sonms.aishortcut.presentation.feed.pick
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun DiscoverScreen(viewModel: DiscoverViewModel = koinViewModel()) {
    val savedModelIds by viewModel.savedModelIds.collectAsStateWithLifecycle()

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
            placeholder = {
                Text(viewModel.language.pick("모델, 레포, 토픽 검색 (RAG, LoRA…)", "Search models, repos, topics (RAG, LoRA…)"))
            },
        )
        Spacer(Modifier.height(Spacing.sm))

        when (val state = viewModel.uiState) {
            DiscoverUiState.Loading -> Centered { CircularProgressIndicator() }

            is DiscoverUiState.Error -> Centered {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(state.message, style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(Spacing.md))
                    Button(onClick = viewModel::load) { Text(viewModel.language.pick("다시 시도", "Retry")) }
                }
            }

            is DiscoverUiState.Content -> Results(
                content = state,
                query = viewModel.query,
                language = viewModel.language,
                localize = feedLocalizer(viewModel.language, viewModel.translations),
                savedModelIds = savedModelIds,
                onToggleSavedModel = viewModel::toggleSavedModel,
            )
        }
    }
}

@Composable
private fun Results(
    content: DiscoverUiState.Content,
    query: String,
    language: FeedLanguage,
    localize: (String?) -> String?,
    savedModelIds: Set<String>,
    onToggleSavedModel: (TrendingModel) -> Unit,
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
        item { BenchmarkPlaceholder(language) }

        if (models.isNotEmpty()) {
            item { SectionHeader(language.pick("모델", "Models")) }
            items(models, key = { "model-${it.id}" }) { model ->
                val enrichment = content.openRouter[model.id.lowercase()]
                FeedCard(
                    modifier = Modifier.clickable {
                        detail = DetailTarget.Model(model, enrichment)
                    },
                ) {
                    ModelRow(
                        model = model,
                        openRouter = enrichment,
                        language = language,
                        saved = model.id in savedModelIds,
                        onToggleSaved = { onToggleSavedModel(model) },
                    )
                }
            }
        }
        if (repos.isNotEmpty()) {
            item { SectionHeader(language.pick("리포지토리", "Repositories")) }
            items(repos, key = { "repo-${it.id}" }) { repo ->
                FeedCard(modifier = Modifier.clickable { detail = DetailTarget.Repo(repo) }) {
                    RepoRow(repo, localize)
                }
            }
        }
        if (models.isEmpty() && repos.isEmpty()) {
            item {
                Text(
                    language.pick("\"$query\"에 대한 결과가 없습니다", "No results for \"$query\""),
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
            language = language,
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
private fun BenchmarkPlaceholder(language: FeedLanguage) {
    FeedCard {
        Text(language.pick("벤치마크 그래프", "Benchmark graphs"), style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(Spacing.xs))
        Text(
            language.pick(
                "MMLU, HumanEval 등 벤치마크 비교는 실제 데이터 연동 후 제공됩니다. " +
                    "지금은 모델 상세에서 OpenRouter 지능·코딩 지수를 확인할 수 있습니다.",
                "MMLU, HumanEval and other benchmark comparisons will arrive once real data " +
                    "is wired up. For now, check a model's OpenRouter intelligence/coding index in its detail sheet.",
            ),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ModelRow(
    model: TrendingModel,
    openRouter: OpenRouterModel?,
    language: FeedLanguage,
    saved: Boolean,
    onToggleSaved: () -> Unit,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(model.id, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
        IconButton(onClick = onToggleSaved) {
            Icon(
                if (saved) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                contentDescription = language.pick(if (saved) "저장 취소" else "저장", if (saved) "Unsave" else "Save"),
                tint = if (saved) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
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
                or.contextLength?.let { "${language.pick("컨텍스트", "Context")} ${formatTokens(it)}" },
                or.intelligenceIndex?.let { "${language.pick("지능", "Intelligence")} ${trimZero(it)}" },
                or.promptUsdPerMTokens?.let { "${language.pick("입력", "Input")} $${trimZero(it)}/1M" },
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
