package com.diaszano.pratoo.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.unit.dp

private val AppShapes =
    Shapes(
        small = RoundedCornerShape(8.dp),
        medium = RoundedCornerShape(12.dp),
        large = RoundedCornerShape(16.dp),
    )

/**
 * Main theme entry point for Pratoo.
 *
 * @param appTheme   Which visual theme to use (Pratoo, Moonlight, etc.)
 * @param themeMode  Whether to use light, dark, or follow system setting
 * @param content    Composable content wrapped by the theme
 */
@Composable
fun PratooTheme(
    appTheme: AppTheme = AppTheme.Pratoo,
    themeMode: AppThemeMode = AppThemeMode.System,
    content: @Composable () -> Unit,
) {
    val darkTheme =
        when (themeMode) {
            AppThemeMode.Light -> false
            AppThemeMode.Dark -> true
            AppThemeMode.System -> isSystemInDarkTheme()
        }

    val colorScheme = getColorScheme(appTheme, darkTheme)
    val appColors = getAppColors(appTheme)

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = AppShapes,
    ) {
        CompositionLocalProvider(LocalAppColors provides appColors) {
            content()
        }
    }
}
