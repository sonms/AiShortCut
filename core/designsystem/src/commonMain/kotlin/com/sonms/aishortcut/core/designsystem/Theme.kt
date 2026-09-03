package com.sonms.aishortcut.core.designsystem

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Palette from DESIGN.md section 2. The app is dark-only ("다크 테크 에스테틱"),
// so there is a single ColorScheme, not a light/dark pair.
private val Background = Color(0xFF0B0D14)
private val Surface = Color(0xFF151927)
private val SurfaceBright = Color(0xFF353948) // also the 1dp border color (section 5)
private val Primary = Color(0xFF7C3AED)
private val Secondary = Color(0xFF3B82F6)
private val OnSurface = Color(0xFFFFFFFF)
private val OnSurfaceVariant = Color(0xFF94A3B8)

private val AiShortCutColorScheme = darkColorScheme(
    primary = Primary,
    onPrimary = OnSurface,
    secondary = Secondary,
    onSecondary = OnSurface,
    background = Background,
    onBackground = OnSurface,
    surface = Surface,
    onSurface = OnSurface,
    surfaceVariant = SurfaceBright,
    onSurfaceVariant = OnSurfaceVariant,
    outline = SurfaceBright,
)

@Composable
fun AiShortCutTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AiShortCutColorScheme,
        typography = AiShortCutTypography,
        content = content,
    )
}
