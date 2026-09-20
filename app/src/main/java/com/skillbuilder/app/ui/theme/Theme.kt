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

private val DarkColorScheme = darkColorScheme(
    primary = BrandCrimson,
    onPrimary = ObsidianCanvas,
    secondary = BrandCrimsonDark,
    background = ObsidianCanvas,
    onBackground = ObsidianTextPrimary,
    surface = ObsidianSurface,
    onSurface = ObsidianTextPrimary,
    surfaceVariant = ObsidianSurfaceSecondary,
    onSurfaceVariant = ObsidianTextSecondary,
    outline = ObsidianBorder
)

private val LightColorScheme = lightColorScheme(
    primary = BrandCrimsonLight,
    onPrimary = AlabasterCanvas,
    secondary = BrandCrimson,
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
