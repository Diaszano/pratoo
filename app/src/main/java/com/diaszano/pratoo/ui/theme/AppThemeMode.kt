package com.diaszano.pratoo.ui.theme

/**
 * Available visual themes for the app.
 *
 * To add a new theme (e.g., Coffee, Forest):
 * 1. Add a new enum entry here
 * 2. Create its ColorSchemes in [AppColorScheme.kt]
 * 3. Add a case in [getColorScheme] and [getAppColors]
 */
enum class AppTheme {
    Pratoo,
    Moonlight
}

/**
 * Controls whether the theme renders in light or dark colors.
 */
enum class AppThemeMode {
    Light,
    Dark,
    System
}
