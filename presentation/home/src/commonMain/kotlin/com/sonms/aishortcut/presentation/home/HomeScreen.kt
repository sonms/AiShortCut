package com.sonms.aishortcut.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
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
import com.sonms.aishortcut.data.newsfeed.NewsArticle
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

        is HomeUiState.Content -> {
            val language = viewModel.language
            val localize: (String?) -> String? = { text ->
                when {
                    text == null -> null
                    language == FeedLanguage.Korean -> viewModel.translations[text] ?: text
                    else -> text
                }
            }
            Column(Modifier.fillMaxSize()) {
                LanguageToggle(
                    selected = language,
                    onSelect = { viewModel.language = it },
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(horizontal = Spacing.md, vertical = Spacing.sm),
                )
                HomeFeed(state, localize)
            }
        }
    }
}

@Composable
private fun LanguageToggle(
    selected: FeedLanguage,
    onSelect: (FeedLanguage) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
        FeedLanguage.entries.forEach { language ->
            FilterChip(
                selected = selected == language,
                onClick = { onSelect(language) },
                label = { Text(language.label, style = MaterialTheme.typography.labelMedium) },
            )
        }
    }
}

@Composable
private fun HomeFeed(content: HomeUiState.Content, localize: (String?) -> String?) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(Spacing.md),
        verticalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        item { SectionHeader("Trending models") }
        items(content.models, key = { "model-${it.id}" }) { TrendingModelCard(it) }

        item { SectionHeader("Trending AI repos") }
        items(content.repos, key = { "repo-${it.id}" }) { TrendingRepoCard(it, localize) }

        if (content.articles.isNotEmpty()) {
            item { SectionHeader("AI news") }
            items(content.articles, key = { "news-${it.link}" }) { NewsCard(it, localize) }
        }
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
private fun TrendingRepoCard(repo: TrendingRepo, localize: (String?) -> String?) {
    FeedCard {
        Text(repo.fullName, style = MaterialTheme.typography.titleMedium)
        localize(repo.description)?.let { description ->
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
private fun NewsCard(article: NewsArticle, localize: (String?) -> String?) {
    val summary = localize(article.summary).orEmpty()
    FeedCard {
        Text(localize(article.title).orEmpty(), style = MaterialTheme.typography.titleMedium)
        if (summary.isNotBlank()) {
            Spacer(Modifier.height(Spacing.xs))
            Text(
                summary,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Spacer(Modifier.height(Spacing.sm))
        Text(
            article.source,
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
