package com.sonms.aishortcut.presentation.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import com.sonms.aishortcut.core.designsystem.Spacing
import com.sonms.aishortcut.data.githubtrending.TrendingRepo
import com.sonms.aishortcut.data.hftrending.TrendingModel
import com.sonms.aishortcut.data.newsfeed.NewsArticle
import com.sonms.aishortcut.data.openrouter.OpenRouterModel
import com.sonms.aishortcut.presentation.feed.FeedLanguage
import com.sonms.aishortcut.presentation.feed.pick

// What the sheet shows. `localized` lets the caller pass through the EN/KO
// translation lookup it already has; identity by default.
sealed interface DetailTarget {
    // `openRouter` is the matching OpenRouter entry when one exists (context,
    // pricing, benchmark indices); null for models OpenRouter doesn't serve.
    data class Model(
        val model: TrendingModel,
        val openRouter: OpenRouterModel? = null,
    ) : DetailTarget
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
    language: FeedLanguage = FeedLanguage.Korean,
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
                is DetailTarget.Model -> ModelDetail(target.model, target.openRouter, language)
                is DetailTarget.Repo -> RepoDetail(target.repo, localized, language)
                is DetailTarget.Article -> ArticleDetail(target.article, localized, saved, onToggleSaved, language)
            }
            Spacer(Modifier.height(Spacing.sm))
            OutlinedButton(
                onClick = { uriHandler.openUri(target.externalUrl) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(language.pick("원문 열기", "Open original"))
            }
        }
    }
}

@Composable
private fun ModelDetail(model: TrendingModel, openRouter: OpenRouterModel?, language: FeedLanguage) {
    SheetTitle(model.id)
    Spacer(Modifier.height(Spacing.xs))
    model.author?.let { Meta(language.pick("작성자", "Author"), it) }
    model.pipelineTag?.let { Meta(language.pick("분류", "Type"), it) }
    Meta(language.pick("좋아요", "Likes"), model.likes.toString())
    Meta(language.pick("다운로드", "Downloads"), model.downloads.toString())

    openRouter?.let { or ->
        MetaGroupHeader(language.pick("OpenRouter 제공", "From OpenRouter"))
        or.contextLength?.let { Meta(language.pick("컨텍스트", "Context"), formatTokens(it)) }
        formatPricePair(or.promptUsdPerMTokens, or.completionUsdPerMTokens, language)
            ?.let { Meta(language.pick("가격(1M 토큰)", "Price (1M tokens)"), it) }
        or.intelligenceIndex?.let { Meta(language.pick("지능 지수", "Intelligence"), trimZero(it)) }
        or.codingIndex?.let { Meta(language.pick("코딩 지수", "Coding"), trimZero(it)) }
        or.agenticIndex?.let { Meta(language.pick("에이전트 지수", "Agentic"), trimZero(it)) }
    }
}

// Shared with the Discover list row. 131072 -> "131K", 1048576 -> "1.0M".
// Rough on purpose; the exact token count isn't what a reader is after here.
fun formatTokens(tokens: Int): String = when {
    tokens >= 1_000_000 -> "${trimZero(tokens / 1_000_000.0)}M"
    tokens >= 1_000 -> "${tokens / 1_000}K"
    else -> tokens.toString()
}

fun formatPricePair(prompt: Double?, completion: Double?, language: FeedLanguage = FeedLanguage.Korean): String? {
    if (prompt == null && completion == null) return null
    val p = prompt?.let { "${language.pick("입력", "Input")} $${trimZero(it)}" }
    val c = completion?.let { "${language.pick("출력", "Output")} $${trimZero(it)}" }
    return listOfNotNull(p, c).joinToString(" / ")
}

// Drops a trailing ".0" so "33.0" reads as "33", keeps "33.9".
fun trimZero(value: Double): String =
    if (value % 1.0 == 0.0) value.toLong().toString() else value.toString()

@Composable
private fun RepoDetail(repo: TrendingRepo, localized: (String) -> String, language: FeedLanguage) {
    SheetTitle(repo.fullName)
    repo.description?.let {
        Spacer(Modifier.height(Spacing.xs))
        Text(
            localized(it),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
    Spacer(Modifier.height(Spacing.xs))
    Meta(language.pick("스타", "Stars"), repo.stars.toString())
    Meta(language.pick("포크", "Forks"), repo.forks.toString())
    repo.language?.let { Meta(language.pick("언어", "Language"), it) }
    if (repo.topics.isNotEmpty()) Meta(language.pick("토픽", "Topics"), repo.topics.joinToString(" · "))
}

@Composable
private fun ArticleDetail(
    article: NewsArticle,
    localized: (String) -> String,
    saved: Boolean,
    onToggleSaved: (() -> Unit)?,
    language: FeedLanguage,
) {
    SheetTitle(localized(article.title))
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            buildString {
                append(article.source)
                article.publishedAt?.let { append(" · ${it.take(10)}") }
            },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (onToggleSaved != null) {
            TextButton(onClick = onToggleSaved) {
                Icon(
                    if (saved) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = null,
                    tint = if (saved) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    language.pick(if (saved) "저장됨" else "저장", if (saved) "Saved" else "Save"),
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

// Sheet-level title. Larger than a card header (DESIGN.md "화면 타이틀" slot) so
// the sheet reads as its own surface, not a detached card.
@Composable
private fun SheetTitle(text: String) {
    Text(text, style = MaterialTheme.typography.headlineSmall)
}

// Labels a group of Meta rows and rules a line above it.
@Composable
private fun MetaGroupHeader(text: String) {
    Spacer(Modifier.height(Spacing.sm))
    HorizontalDivider(color = MaterialTheme.colorScheme.outline)
    Spacer(Modifier.height(Spacing.sm))
    Text(
        text,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.primary,
    )
}

// Fixed label column so the values line up (DESIGN.md "Data" style, tabular).
@Composable
private fun Meta(label: String, value: String) {
    Row(verticalAlignment = Alignment.Top) {
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(92.dp),
        )
        Text(value, style = MaterialTheme.typography.bodySmall)
    }
}
