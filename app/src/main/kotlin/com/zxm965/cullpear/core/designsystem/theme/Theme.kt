package com.zxm965.cullpear.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.zxm965.cullpear.core.preferences.Accent
import com.zxm965.cullpear.core.preferences.ThemeMode

@Composable
fun CullPearTheme(
    mode: ThemeMode,
    accent: Accent,
    content: @Composable () -> Unit,
) {
    val dark = when (mode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    val primary = accent.color()
    val colors = if (dark) {
        darkColorScheme(
            primary = primary,
            secondary = Color(0xFF9FC8BA),
            background = Night,
            surface = NightSurface,
            surfaceVariant = Color(0xFF24302D),
            onBackground = Color(0xFFE7EEEB),
            onSurface = Color(0xFFE7EEEB),
        )
    } else {
        lightColorScheme(
            primary = primary,
            secondary = Color(0xFF4D6E64),
            background = Paper,
            surface = Color(0xFFFFFCF7),
            surfaceVariant = Color(0xFFEAE7DF),
            onBackground = Ink,
            onSurface = Ink,
        )
    }

    MaterialTheme(colorScheme = colors, typography = Typography, content = content)
}
