package com.sonms.aishortcut.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.sonms.aishortcut.core.designsystem.FeedCard
import com.sonms.aishortcut.core.designsystem.SectionHeader
import com.sonms.aishortcut.core.designsystem.Spacing
import com.sonms.aishortcut.data.githubtrending.TrendingRepo
import com.sonms.aishortcut.data.hftrending.TrendingModel
import com.sonms.aishortcut.data.newsfeed.NewsArticle
import org.koin.compose.viewmodel.koinViewModel

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
            val savedLinks by viewModel.savedLinks.collectAsState()
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
                HomeFeed(
                    content = state,
                    localize = localize,
                    isSaved = { it in savedLinks },
                    onToggleSaved = viewModel::toggleSaved,
                )
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
private fun HomeFeed(
    content: HomeUiState.Content,
    localize: (String?) -> String?,
    isSaved: (String) -> Boolean,
    onToggleSaved: (NewsArticle) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(Spacing.md),
        verticalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        if (content.digests.isNotEmpty()) {
            item { SectionHeader("오늘의 AI 소식") }
            item { DigestPager(content.digests, localize, isSaved, onToggleSaved) }
        }

        item { SectionHeader("Trending models") }
        items(content.models, key = { "model-${it.id}" }) { TrendingModelCard(it) }

        item { SectionHeader("Trending AI repos") }
        items(content.repos, key = { "repo-${it.id}" }) { TrendingRepoCard(it, localize) }
    }
}

@Composable
private fun DigestPager(
    digests: List<DailyDigest>,
    localize: (String?) -> String?,
    isSaved: (String) -> Boolean,
    onToggleSaved: (NewsArticle) -> Unit,
) {
    val pagerState = rememberPagerState(pageCount = { digests.size })
    Column {
        HorizontalPager(
            state = pagerState,
            pageSpacing = Spacing.sm,
        ) { page ->
            DigestCard(digests[page], localize, isSaved, onToggleSaved)
        }
        if (digests.size > 1) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(top = Spacing.sm),
                horizontalArrangement = Arrangement.spacedBy(Spacing.xs, Alignment.CenterHorizontally),
            ) {
                repeat(digests.size) { index ->
                    val active = index == pagerState.currentPage
                    Box(
                        Modifier
                            .size(if (active) 8.dp else 6.dp)
                            .clip(CircleShape)
                            .background(
                                if (active) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.outline,
                            ),
                    )
                }
            }
        }
    }
}

@Composable
private fun DigestCard(
    digest: DailyDigest,
    localize: (String?) -> String?,
    isSaved: (String) -> Boolean,
    onToggleSaved: (NewsArticle) -> Unit,
) {
    FeedCard {
        Text(
            digest.date,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            "새 논문 ${digest.articles.size}건",
            style = MaterialTheme.typography.titleMedium,
        )
        Spacer(Modifier.height(Spacing.xs))
        digest.articles.take(3).forEach { article ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    localize(article.title).orEmpty(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                SaveButton(isSaved(article.link)) { onToggleSaved(article) }
            }
        }
        if (digest.articles.size > 3) {
            Text(
                "외 ${digest.articles.size - 3}건",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun SaveButton(saved: Boolean, onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        Icon(
            Icons.Outlined.FavoriteBorder,
            contentDescription = if (saved) "저장 취소" else "저장",
            tint = if (saved) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
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
private fun Centered(content: @Composable () -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { content() }
}
