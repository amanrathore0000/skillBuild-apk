package com.skillbuilder.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import com.skillbuilder.app.R

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFFFFFFF),
    onPrimary = Color(0xFF2B2B2B),
    primaryContainer = Color(0xFFD4D4D4),
    onPrimaryContainer = Color(0xFF2B2B2B),
    secondary = Color(0xFFD4D4D4),
    onSecondary = Color(0xFF2B2B2B),
    secondaryContainer = Color(0xFF2B2B2B),
    onSecondaryContainer = Color(0xFFFFFFFF),
    tertiary = Color(0xFFB3B3B3),
    onTertiary = Color(0xFF2B2B2B),
    background = Color(0xFF2B2B2B),
    onBackground = Color(0xFFFFFFFF),
    surface = Color(0xFF2B2B2B),
    onSurface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFF2B2B2B),
    onSurfaceVariant = Color(0xFFD4D4D4),
    surfaceContainer = Color(0xFF2B2B2B),
    surfaceContainerHigh = Color(0xFF2B2B2B),
    surfaceContainerHighest = Color(0xFF2B2B2B),
    surfaceContainerLow = Color(0xFF2B2B2B),
    surfaceContainerLowest = Color(0xFF2B2B2B),
    outline = Color(0xFFB3B3B3),
    outlineVariant = Color(0xFFB3B3B3).copy(alpha = 0.4f),
    error = Color(0xFFD4D4D4),
    onError = Color(0xFF2B2B2B)
)

private val LightColorScheme = lightColorScheme(
    primary = BrandPrimary,
    onPrimary = AlabasterCanvas,
    secondary = BrandPrimaryLight,
    background = AlabasterCanvas,
    onBackground = AlabasterTextPrimary,
    surface = AlabasterSurface,
    onSurface = AlabasterTextPrimary,
    surfaceVariant = AlabasterSurfaceSecondary,
    onSurfaceVariant = AlabasterTextSecondary,
    outline = AlabasterBorder
)

@Composable
fun SkillBuilderTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                val insetsController = WindowCompat.getInsetsController(window, view)
                // In light theme, set isAppearanceLightStatusBars = true to render dark notification/status bar icons
                insetsController.isAppearanceLightStatusBars = !darkTheme
                insetsController.isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun rememberAppLogoPainter(): Painter {
    val isDark = MaterialTheme.colorScheme.background == ObsidianCanvas || 
                 MaterialTheme.colorScheme.background.luminance() < 0.5f
    return painterResource(
        id = if (isDark) R.drawable.app_logo_dark else R.drawable.app_logo_light
    )
}
