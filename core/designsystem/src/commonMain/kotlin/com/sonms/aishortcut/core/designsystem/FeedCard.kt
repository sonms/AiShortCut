package com.sonms.aishortcut.core.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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

// The one screen-level title (DESIGN.md section 4 "화면 타이틀", 22sp). The short
// gradient rule under it is the app's single recurring brand accent. `trailing`
// hangs an action (a language toggle, say) off the right edge, centred on the
// title row.
@Composable
fun ScreenHeader(
    title: String,
    modifier: Modifier = Modifier,
    trailing: @Composable (RowScope.() -> Unit)? = null,
) {
    Row(
        modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.md)
            .padding(top = Spacing.md, bottom = Spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(Spacing.xs))
            Spacer(
                Modifier
                    .width(28.dp)
                    .height(3.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(BrandGradient),
            )
        }
        trailing?.invoke(this)
    }
}

// DESIGN.md section 3: sections are separated by space-lg. Screens put this
// above every group of cards.
@Composable
fun SectionHeader(text: String, modifier: Modifier = Modifier) {
    Text(
        text,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier.padding(top = Spacing.lg, bottom = Spacing.xs),
    )
}

// One line of numeric metadata in the "Data" type style (DESIGN.md section 4):
// dot-separated stats, muted colour, tabular figures. Blank parts are dropped;
// renders nothing if that leaves the list empty.
@Composable
fun StatLine(
    parts: List<String>,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurfaceVariant,
) {
    val shown = parts.filter { it.isNotBlank() }
    if (shown.isEmpty()) return
    Text(
        shown.joinToString("   ·   "),
        style = MaterialTheme.typography.bodySmall,
        color = color,
        modifier = modifier,
    )
}
