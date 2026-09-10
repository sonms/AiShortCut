package com.sonms.aishortcut.core.designsystem

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// DESIGN.md section 2: the #3B82F6 -> #7C3AED gradient is a brand token that
// lives outside the Material ColorScheme. Use it sparingly for brand accents
// (screen-header underline, active pager dot) -- never as a fill behind text.
private val GradientStart = Color(0xFF3B82F6)
private val GradientEnd = Color(0xFF7C3AED)

val BrandGradient: Brush = Brush.horizontalGradient(listOf(GradientStart, GradientEnd))
