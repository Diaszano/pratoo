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
    val success: Color,
)

val LocalAppColors =
    staticCompositionLocalOf {
        AppColors(
            favorite = Color(0xFFFFB300),
            success = Color(0xFF4CAF50),
        )
    }

// ── Pratoo Color Schemes ────────────────────────────────────────────

private val PratooLightColorScheme =
    lightColorScheme(
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

private val PratooDarkColorScheme =
    darkColorScheme(
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

// ── Moonlight Purple Color Schemes ─────────────────────────────────

private val MoonPurpleDarkColorScheme =
    darkColorScheme(
        primary = MoonPurpleDarkPrimary,
        onPrimary = MoonPurpleDarkOnPrimary,
        primaryContainer = MoonPurpleDarkPrimaryContainer,
        onPrimaryContainer = MoonPurpleDarkOnPrimaryContainer,
        secondary = MoonPurpleDarkSecondary,
        onSecondary = MoonPurpleDarkOnSecondary,
        secondaryContainer = MoonPurpleDarkSecondaryContainer,
        onSecondaryContainer = MoonPurpleDarkOnSecondaryContainer,
        tertiary = MoonPurpleDarkTertiary,
        onTertiary = MoonPurpleDarkOnTertiary,
        tertiaryContainer = MoonPurpleDarkTertiaryContainer,
        onTertiaryContainer = MoonPurpleDarkOnTertiaryContainer,
        background = MoonPurpleDarkBackground,
        onBackground = MoonPurpleDarkOnBackground,
        surface = MoonPurpleDarkSurface,
        onSurface = MoonPurpleDarkOnSurface,
        surfaceVariant = MoonPurpleDarkSurfaceVariant,
        onSurfaceVariant = MoonPurpleDarkOnSurfaceVariant,
        outline = MoonPurpleDarkOutline,
        outlineVariant = MoonPurpleDarkOutlineVariant,
        error = MoonPurpleDarkError,
        onError = MoonPurpleDarkOnError,
        inverseSurface = MoonPurpleDarkOnBackground,
        inverseOnSurface = MoonPurpleDarkBackground,
        inversePrimary = MoonPurpleLightPrimary,
    )

private val MoonPurpleLightColorScheme =
    lightColorScheme(
        primary = MoonPurpleLightPrimary,
        onPrimary = MoonPurpleLightOnPrimary,
        primaryContainer = MoonPurpleLightPrimaryContainer,
        onPrimaryContainer = MoonPurpleLightOnPrimaryContainer,
        secondary = MoonPurpleLightSecondary,
        onSecondary = MoonPurpleLightOnSecondary,
        secondaryContainer = MoonPurpleLightSecondaryContainer,
        onSecondaryContainer = MoonPurpleLightOnSecondaryContainer,
        tertiary = MoonPurpleLightTertiary,
        onTertiary = MoonPurpleLightOnTertiary,
        tertiaryContainer = MoonPurpleLightTertiaryContainer,
        onTertiaryContainer = MoonPurpleLightOnTertiaryContainer,
        background = MoonPurpleLightBackground,
        onBackground = MoonPurpleLightOnBackground,
        surface = MoonPurpleLightSurface,
        onSurface = MoonPurpleLightOnSurface,
        surfaceVariant = MoonPurpleLightSurfaceVariant,
        onSurfaceVariant = MoonPurpleLightOnSurfaceVariant,
        outline = MoonPurpleLightOutline,
        outlineVariant = MoonPurpleLightOutlineVariant,
        error = MoonPurpleLightError,
        onError = MoonPurpleLightOnError,
        inverseSurface = MoonPurpleLightOnBackground,
        inverseOnSurface = MoonPurpleLightBackground,
        inversePrimary = MoonPurpleDarkPrimary,
    )

// ── Theme-to-ColorScheme factory ─────────────────────────────────────

/**
 * Returns the [ColorScheme] for the given [AppTheme] and darkness flag.
 *
 * To add a new theme:
 * 1. Add its ColorSchemes (light + dark) in this file
 * 2. Add a new [when] branch below
 */
fun getColorScheme(
    appTheme: AppTheme,
    darkTheme: Boolean,
): ColorScheme =
    when (appTheme) {
        AppTheme.Pratoo -> if (darkTheme) PratooDarkColorScheme else PratooLightColorScheme
        AppTheme.Moonlight -> if (darkTheme) MoonPurpleDarkColorScheme else MoonPurpleLightColorScheme
    }

// ── Theme-to-AppColors factory ───────────────────────────────────────

/**
 * Returns app-level semantic colors for the given [AppTheme].
 *
 * To add a new theme, add a new [when] branch below.
 */
fun getAppColors(appTheme: AppTheme): AppColors =
    when (appTheme) {
        AppTheme.Pratoo ->
            AppColors(
                favorite = PratooFavorite,
                success = PratooSuccess,
            )
        AppTheme.Moonlight ->
            AppColors(
                favorite = MoonlightFavorite,
                success = MoonlightSuccess,
            )
    }
