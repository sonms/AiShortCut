package com.sonms.aishortcut.presentation.feed

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.sonms.aishortcut.core.designsystem.Spacing

@Composable
fun LanguageToggle(
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
