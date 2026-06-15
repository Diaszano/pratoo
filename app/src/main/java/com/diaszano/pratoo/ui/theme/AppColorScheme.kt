package com.diaszano.pratoo.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// ── App-specific semantic colors (not in Material 3 spec) ────────────

@Immutable
data class AppColors(
    val favorite: Color,
    val success: Color
)

val LocalAppColors = staticCompositionLocalOf {
    AppColors(
        favorite = Color(0xFFFFB300),
        success = Color(0xFF4CAF50)
    )
}

// ── Pratoo Color Schemes ────────────────────────────────────────────

private val PratooLightColorScheme = lightColorScheme(
    primary = PratooLightPrimary,
    onPrimary = PratooLightOnPrimary,
    primaryContainer = PratooLightPrimaryContainer,
    onPrimaryContainer = PratooLightOnPrimaryContainer,
    secondary = PratooLightSecondary,
    onSecondary = PratooLightOnSecondary,
    secondaryContainer = PratooLightSecondaryContainer,
    onSecondaryContainer = PratooLightOnSecondaryContainer,
    tertiary = PratooLightTertiary,
    onTertiary = PratooLightOnTertiary,
    tertiaryContainer = PratooLightTertiaryContainer,
    onTertiaryContainer = PratooLightOnTertiaryContainer,
    error = PratooLightError,
    onError = PratooLightOnError,
    errorContainer = PratooLightErrorContainer,
    onErrorContainer = PratooLightOnErrorContainer,
    background = PratooLightBackground,
    onBackground = PratooLightOnBackground,
    surface = PratooLightSurface,
    onSurface = PratooLightOnSurface,
    surfaceVariant = PratooLightSurfaceVariant,
    onSurfaceVariant = PratooLightOnSurfaceVariant,
    outline = PratooLightOutline,
    outlineVariant = PratooLightOutlineVariant,
    inverseSurface = PratooLightInverseSurface,
    inverseOnSurface = PratooLightInverseOnSurface,
    inversePrimary = PratooLightInversePrimary,
)

private val PratooDarkColorScheme = darkColorScheme(
    primary = PratooDarkPrimary,
    onPrimary = PratooDarkOnPrimary,
    primaryContainer = PratooDarkPrimaryContainer,
    onPrimaryContainer = PratooDarkOnPrimaryContainer,
    secondary = PratooDarkSecondary,
    onSecondary = PratooDarkOnSecondary,
    secondaryContainer = PratooDarkSecondaryContainer,
    onSecondaryContainer = PratooDarkOnSecondaryContainer,
    tertiary = PratooDarkTertiary,
    onTertiary = PratooDarkOnTertiary,
    tertiaryContainer = PratooDarkTertiaryContainer,
    onTertiaryContainer = PratooDarkOnTertiaryContainer,
    error = PratooDarkError,
    onError = PratooDarkOnError,
    errorContainer = PratooDarkErrorContainer,
    onErrorContainer = PratooDarkOnErrorContainer,
    background = PratooDarkBackground,
    onBackground = PratooDarkOnBackground,
    surface = PratooDarkSurface,
    onSurface = PratooDarkOnSurface,
    surfaceVariant = PratooDarkSurfaceVariant,
    onSurfaceVariant = PratooDarkOnSurfaceVariant,
    outline = PratooDarkOutline,
    outlineVariant = PratooDarkOutlineVariant,
    inverseSurface = PratooDarkInverseSurface,
    inverseOnSurface = PratooDarkInverseOnSurface,
    inversePrimary = PratooDarkInversePrimary,
)

// ── Moonlight Color Scheme (dark-only, blue-toned) ──────────────────

private val MoonlightColorScheme = darkColorScheme(
    primary = MoonlightBlue,
    onPrimary = MoonlightGray1,
    primaryContainer = MoonlightGray5,
    onPrimaryContainer = MoonlightGray10,
    secondary = MoonlightPurple,
    onSecondary = MoonlightGray1,
    secondaryContainer = MoonlightGray5,
    onSecondaryContainer = MoonlightGray9,
    tertiary = MoonlightTeal,
    onTertiary = MoonlightGray1,
    tertiaryContainer = MoonlightGray5,
    onTertiaryContainer = MoonlightGreen,
    error = MoonlightDarkRed,
    onError = MoonlightGray1,
    background = MoonlightGray4,
    onBackground = MoonlightGray10,
    surface = MoonlightGray3,
    onSurface = MoonlightGray10,
    surfaceVariant = MoonlightGray5,
    onSurfaceVariant = MoonlightGray8,
    outline = MoonlightGray6,
    outlineVariant = MoonlightGray5,
    inverseSurface = MoonlightGray10,
    inverseOnSurface = MoonlightGray1,
    inversePrimary = Color(0xFF3D6FE0),
)

// ── Theme-to-ColorScheme factory ─────────────────────────────────────
/**
 * Returns the [ColorScheme] for the given [AppTheme] and darkness flag.
 *
 * To add a new theme:
 * 1. Add its ColorSchemes (light + dark) in this file
 * 2. Add a new [when] branch below
 */
fun getColorScheme(appTheme: AppTheme, darkTheme: Boolean): ColorScheme {
    return when (appTheme) {
        AppTheme.Pratoo -> if (darkTheme) PratooDarkColorScheme else PratooLightColorScheme
        AppTheme.Moonlight -> MoonlightColorScheme
    }
}

// ── Theme-to-AppColors factory ───────────────────────────────────────
/**
 * Returns app-level semantic colors for the given [AppTheme].
 *
 * To add a new theme, add a new [when] branch below.
 */
fun getAppColors(appTheme: AppTheme): AppColors {
    return when (appTheme) {
        AppTheme.Pratoo -> AppColors(
            favorite = PratooFavorite,
            success = PratooSuccess
        )
        AppTheme.Moonlight -> AppColors(
            favorite = MoonlightFavorite,
            success = MoonlightSuccess
        )
    }
}
