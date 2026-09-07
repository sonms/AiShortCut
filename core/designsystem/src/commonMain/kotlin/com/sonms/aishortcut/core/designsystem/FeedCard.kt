package com.sonms.aishortcut.core.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

// DESIGN.md section 5: corner radius 16dp, a 1dp outline border instead of
// elevation, space-md inner padding. Every feed/list card across the app.
private val CardShape = RoundedCornerShape(16.dp)

@Composable
fun FeedCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier
            .fillMaxWidth()
            .clip(CardShape)
            .border(1.dp, MaterialTheme.colorScheme.outline, CardShape)
            .background(MaterialTheme.colorScheme.surface)
            .padding(Spacing.md),
        content = content,
    )
}

@Composable
fun SectionHeader(text: String, modifier: Modifier = Modifier) {
    Text(
        text,
        style = MaterialTheme.typography.headlineSmall,
        modifier = modifier.padding(top = Spacing.sm, bottom = Spacing.xs),
    )
}
