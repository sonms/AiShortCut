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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import com.sonms.aishortcut.core.designsystem.FeedCard
import com.sonms.aishortcut.core.designsystem.ScreenHeader
import com.sonms.aishortcut.core.designsystem.Spacing
import com.sonms.aishortcut.core.designsystem.StatLine
import com.sonms.aishortcut.data.newsfeed.NewsArticle
import com.sonms.aishortcut.presentation.detail.DetailSheet
import com.sonms.aishortcut.presentation.detail.DetailTarget
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SavedScreen(viewModel: SavedViewModel = koinViewModel()) {
    val articles by viewModel.articles.collectAsState()
    var detail by remember { mutableStateOf<NewsArticle?>(null) }

    Column(Modifier.fillMaxSize()) {
        ScreenHeader("Saved")

        if (articles.isEmpty()) {
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
                items(articles, key = { it.link }) { article ->
                    SavedArticleCard(
                        article,
                        onOpen = { detail = article },
                        onRemove = { viewModel.remove(article) },
                    )
                }
            }
        }
    }

    detail?.let { article ->
        DetailSheet(
            target = DetailTarget.Article(article),
            onDismiss = { detail = null },
            saved = article in articles,
            onToggleSaved = { viewModel.remove(article) },
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
