package com.sonms.aishortcut.presentation.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import com.sonms.aishortcut.core.designsystem.Spacing
import com.sonms.aishortcut.data.githubtrending.TrendingRepo
import com.sonms.aishortcut.data.hftrending.TrendingModel
import com.sonms.aishortcut.data.newsfeed.NewsArticle

// What the sheet shows. `localized` lets the caller pass through the EN/KO
// translation lookup it already has; identity by default.
sealed interface DetailTarget {
    data class Model(val model: TrendingModel) : DetailTarget
    data class Repo(val repo: TrendingRepo) : DetailTarget
    data class Article(val article: NewsArticle) : DetailTarget
}

private val DetailTarget.externalUrl: String
    get() = when (this) {
        is DetailTarget.Model -> "https://huggingface.co/${model.id}"
        is DetailTarget.Repo -> repo.url
        is DetailTarget.Article -> article.link
    }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailSheet(
    target: DetailTarget,
    onDismiss: () -> Unit,
    localized: (String) -> String = { it },
    saved: Boolean = false,
    onToggleSaved: (() -> Unit)? = null,
) {
    val uriHandler = LocalUriHandler.current
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            Modifier
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.md)
                .padding(bottom = Spacing.xl),
            verticalArrangement = Arrangement.spacedBy(Spacing.xs),
        ) {
            when (target) {
                is DetailTarget.Model -> ModelDetail(target.model)
                is DetailTarget.Repo -> RepoDetail(target.repo, localized)
                is DetailTarget.Article -> ArticleDetail(target.article, localized, saved, onToggleSaved)
            }
            Spacer(Modifier.height(Spacing.sm))
            OutlinedButton(
                onClick = { uriHandler.openUri(target.externalUrl) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("원문 열기")
            }
        }
    }
}

@Composable
private fun ModelDetail(model: TrendingModel) {
    Text(model.id, style = MaterialTheme.typography.titleMedium)
    model.author?.let { Meta("작성자", it) }
    model.pipelineTag?.let { Meta("분류", it) }
    Meta("좋아요", model.likes.toString())
    Meta("다운로드", model.downloads.toString())
}

@Composable
private fun RepoDetail(repo: TrendingRepo, localized: (String) -> String) {
    Text(repo.fullName, style = MaterialTheme.typography.titleMedium)
    repo.description?.let {
        Text(
            localized(it),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
    Meta("스타", repo.stars.toString())
    Meta("포크", repo.forks.toString())
    repo.language?.let { Meta("언어", it) }
    if (repo.topics.isNotEmpty()) Meta("토픽", repo.topics.joinToString(" · "))
}

@Composable
private fun ArticleDetail(
    article: NewsArticle,
    localized: (String) -> String,
    saved: Boolean,
    onToggleSaved: (() -> Unit)?,
) {
    Text(localized(article.title), style = MaterialTheme.typography.titleMedium)
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            buildString {
                append(article.source)
                article.publishedAt?.let { append(" · ${it.take(10)}") }
            },
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (onToggleSaved != null) {
            TextButton(onClick = onToggleSaved) {
                Icon(
                    Icons.Outlined.FavoriteBorder,
                    contentDescription = null,
                    tint = if (saved) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    if (saved) "저장됨" else "저장",
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(start = Spacing.xs),
                )
            }
        }
    }
    Spacer(Modifier.height(Spacing.xs))
    Text(
        localized(article.summary),
        style = MaterialTheme.typography.bodyMedium,
    )
}

@Composable
private fun Meta(label: String, value: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}
