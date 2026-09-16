package com.sonms.aishortcut.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.sonms.aishortcut.core.designsystem.BrandGradient
import com.sonms.aishortcut.core.designsystem.FeedCard
import com.sonms.aishortcut.core.designsystem.ScreenHeader
import com.sonms.aishortcut.core.designsystem.SectionHeader
import com.sonms.aishortcut.core.designsystem.Spacing
import com.sonms.aishortcut.core.designsystem.StatLine
import com.sonms.aishortcut.data.githubtrending.TrendingRepo
import com.sonms.aishortcut.data.hftrending.TrendingModel
import com.sonms.aishortcut.data.newsfeed.NewsArticle
import com.sonms.aishortcut.presentation.detail.DetailSheet
import com.sonms.aishortcut.presentation.detail.DetailTarget
import com.sonms.aishortcut.presentation.feed.LanguageToggle
import com.sonms.aishortcut.presentation.feed.feedLocalizer
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

        is HomeUiState.Content -> Content(state, viewModel)
    }
}

@Composable
private fun Content(state: HomeUiState.Content, viewModel: HomeViewModel) {
    val language = viewModel.language
    val savedLinks by viewModel.savedLinks.collectAsStateWithLifecycle()
    var detail by remember { mutableStateOf<DetailTarget?>(null) }

    val localize = feedLocalizer(language, viewModel.translations)

    val keywords = remember(state.digests) { trendingKeywords(state.digests) }
    val shownDigests = remember(state.digests, viewModel.selectedKeyword) {
        state.digests.filterByKeyword(viewModel.selectedKeyword)
    }

    Column(Modifier.fillMaxSize()) {
        ScreenHeader(
            title = "Home",
            trailing = {
                LanguageToggle(selected = language, onSelect = { viewModel.language = it })
            },
        )
        if (keywords.isNotEmpty()) {
            KeywordChipRow(
                keywords = keywords,
                selected = viewModel.selectedKeyword,
                onSelect = { viewModel.selectedKeyword = it },
            )
        }
        HomeFeed(
            content = state,
            digests = shownDigests,
            localize = localize,
            isSaved = { it in savedLinks },
            onToggleSaved = viewModel::toggleSaved,
            onOpen = { detail = it },
        )
    }

    detail?.let { target ->
        DetailSheet(
            target = target,
            onDismiss = { detail = null },
            language = language,
            localized = { localize(it) ?: it },
            saved = target is DetailTarget.Article && target.article.link in savedLinks,
            onToggleSaved = (target as? DetailTarget.Article)?.let { a -> { viewModel.toggleSaved(a.article) } },
        )
    }
}

// Horizontally scrolling row of trending-keyword chips. Tapping the selected
// chip clears the filter.
@Composable
private fun KeywordChipRow(
    keywords: List<String>,
    selected: String?,
    onSelect: (String?) -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = Spacing.md, vertical = Spacing.xs),
        horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
    ) {
        keywords.forEach { keyword ->
            FilterChip(
                selected = keyword == selected,
                onClick = { onSelect(if (keyword == selected) null else keyword) },
                label = { Text(keyword, style = MaterialTheme.typography.labelMedium) },
            )
        }
    }
}

@Composable
private fun HomeFeed(
    content: HomeUiState.Content,
    digests: List<DailyDigest>,
    localize: (String?) -> String?,
    isSaved: (String) -> Boolean,
    onToggleSaved: (NewsArticle) -> Unit,
    onOpen: (DetailTarget) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = Spacing.md,
            end = Spacing.md,
            top = Spacing.sm,
            bottom = Spacing.md,
        ),
        verticalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        if (digests.isNotEmpty()) {
            item { SectionHeader("오늘의 AI 소식") }
            // Key on size so the pager state resets when a keyword filter
            // shrinks the day count out from under the current page.
            item(key = "digest-${digests.size}") {
                DigestPager(digests, localize, isSaved, onToggleSaved, onOpen)
            }
        }

        item { SectionHeader("트렌딩 모델") }
        items(content.models, key = { "model-${it.id}" }) { model ->
            FeedCard(modifier = Modifier.clickable { onOpen(DetailTarget.Model(model)) }) {
                TrendingModelCard(model)
            }
        }

        item { SectionHeader("트렌딩 리포지토리") }
        items(content.repos, key = { "repo-${it.id}" }) { repo ->
            FeedCard(modifier = Modifier.clickable { onOpen(DetailTarget.Repo(repo)) }) {
                TrendingRepoCard(repo, localize)
            }
        }
    }
}

@Composable
private fun DigestPager(
    digests: List<DailyDigest>,
    localize: (String?) -> String?,
    isSaved: (String) -> Boolean,
    onToggleSaved: (NewsArticle) -> Unit,
    onOpen: (DetailTarget) -> Unit,
) {
    val pagerState = rememberPagerState(pageCount = { digests.size })
    Column {
        HorizontalPager(
            state = pagerState,
            pageSpacing = Spacing.sm,
        ) { page ->
            DigestCard(digests[page], localize, isSaved, onToggleSaved, onOpen)
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
                            .height(6.dp)
                            .width(if (active) 18.dp else 6.dp)
                            .clip(CircleShape)
                            .then(
                                if (active) Modifier.background(BrandGradient)
                                else Modifier.background(MaterialTheme.colorScheme.outline),
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
    onOpen: (DetailTarget) -> Unit,
) {
    FeedCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Spacer(
                Modifier
                    .size(width = 3.dp, height = 16.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(BrandGradient),
            )
            Spacer(Modifier.width(Spacing.xs))
            Text(
                digest.date,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(Modifier.height(Spacing.xs))
        Text(
            "새 논문 ${digest.articles.size}건",
            style = MaterialTheme.typography.titleMedium,
        )
        Spacer(Modifier.height(Spacing.sm))
        digest.articles.take(3).forEach { article ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    localize(article.title).orEmpty(),
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onOpen(DetailTarget.Article(article)) },
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
            if (saved) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
            contentDescription = if (saved) "저장 취소" else "저장",
            tint = if (saved) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun TrendingModelCard(model: TrendingModel) {
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
    StatLine(
        listOf(
            "좋아요 ${model.likes}",
            "다운로드 ${model.downloads}",
        ),
    )
}

@Composable
private fun TrendingRepoCard(repo: TrendingRepo, localize: (String?) -> String?) {
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
    StatLine(
        listOfNotNull(
            "스타 ${repo.stars}",
            "포크 ${repo.forks}",
            repo.language,
        ),
    )
}

@Composable
private fun Centered(content: @Composable () -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { content() }
}
