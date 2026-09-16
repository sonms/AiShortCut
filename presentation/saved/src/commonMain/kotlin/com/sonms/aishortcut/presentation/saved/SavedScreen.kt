package com.sonms.aishortcut.presentation.saved

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import com.sonms.aishortcut.core.designsystem.FeedCard
import com.sonms.aishortcut.core.designsystem.ScreenHeader
import com.sonms.aishortcut.core.designsystem.SectionHeader
import com.sonms.aishortcut.core.designsystem.Spacing
import com.sonms.aishortcut.core.designsystem.StatLine
import com.sonms.aishortcut.data.hftrending.TrendingModel
import com.sonms.aishortcut.data.newsfeed.NewsArticle
import com.sonms.aishortcut.presentation.detail.DetailSheet
import com.sonms.aishortcut.presentation.detail.DetailTarget
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SavedScreen(viewModel: SavedViewModel = koinViewModel()) {
    val articles by viewModel.articles.collectAsStateWithLifecycle()
    val models by viewModel.models.collectAsStateWithLifecycle()
    var detail by remember { mutableStateOf<DetailTarget?>(null) }

    Column(Modifier.fillMaxSize()) {
        ScreenHeader("Saved")

        if (articles.isEmpty() && models.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    "저장한 항목이 없습니다",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
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
                if (models.isNotEmpty()) {
                    item { SectionHeader("저장한 모델") }
                    items(models, key = { "model-${it.id}" }) { model ->
                        SavedModelCard(
                            model,
                            onOpen = { detail = DetailTarget.Model(model) },
                            onRemove = { viewModel.remove(model) },
                        )
                    }
                }
                if (articles.isNotEmpty()) {
                    item { SectionHeader("저장한 기사") }
                    items(articles, key = { "article-${it.link}" }) { article ->
                        SavedArticleCard(
                            article,
                            onOpen = { detail = DetailTarget.Article(article) },
                            onRemove = { viewModel.remove(article) },
                        )
                    }
                }
            }
        }
    }

    detail?.let { target ->
        when (target) {
            is DetailTarget.Article -> DetailSheet(
                target = target,
                onDismiss = { detail = null },
                saved = target.article in articles,
                onToggleSaved = { viewModel.remove(target.article) },
            )
            else -> DetailSheet(target = target, onDismiss = { detail = null })
        }
    }
}

@Composable
private fun SavedModelCard(model: TrendingModel, onOpen: () -> Unit, onRemove: () -> Unit) {
    FeedCard(modifier = Modifier.clickable(onClick = onOpen)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                model.id,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = onRemove) {
                Icon(
                    Icons.Filled.Favorite,
                    contentDescription = "저장 취소",
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        }
        Spacer(Modifier.height(Spacing.xs))
        StatLine(
            listOfNotNull(
                model.pipelineTag,
                "좋아요 ${model.likes}",
                "다운로드 ${model.downloads}",
            ),
        )
    }
}

@Composable
private fun SavedArticleCard(article: NewsArticle, onOpen: () -> Unit, onRemove: () -> Unit) {
    FeedCard(modifier = Modifier.clickable(onClick = onOpen)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                article.title,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = onRemove) {
                Icon(
                    Icons.Filled.Favorite,
                    contentDescription = "저장 취소",
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        }
        Spacer(Modifier.height(Spacing.xs))
        StatLine(
            listOfNotNull(
                article.source,
                article.publishedAt?.take(10),
            ),
        )
    }
}
