package com.diaszano.pratoo.data.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

enum class ThemeMode { SYSTEM, LIGHT, DARK }

@Singleton
class AppPreferences
    @Inject
    constructor(
        private val dataStore: DataStore<Preferences>,
    ) {
        private companion object {
            val KEY_THEME_MODE = stringPreferencesKey("theme_mode")
            val KEY_APP_THEME = stringPreferencesKey("app_theme")
            val KEY_UNIT_SYSTEM = stringPreferencesKey("unit_system")

            const val THEME_SYSTEM = "system"
            const val THEME_LIGHT = "light"
            const val THEME_DARK = "dark"
            const val THEME_MOONLIGHT = "moonlight"
            const val THEME_PRATOO = "pratoo"
            const val UNIT_METRIC = "metric"
        }

        /**
         * Palette preference — which visual theme to use.
         * Backward-compatible: if [KEY_THEME_MODE] is "moonlight" (legacy),
         * Moonlight is returned regardless of [KEY_APP_THEME].
         */
        val appTheme: Flow<String> =
            dataStore.data.map { prefs ->
                when {
                    prefs[KEY_THEME_MODE] == THEME_MOONLIGHT -> THEME_MOONLIGHT
                    else -> prefs[KEY_APP_THEME] ?: THEME_PRATOO
                }
            }

        /**
         * Light/dark mode preference.
         * Backward-compatible: legacy "moonlight" value is treated as SYSTEM.
         */
        val themeMode: Flow<ThemeMode> =
            dataStore.data.map { prefs ->
                when (prefs[KEY_THEME_MODE]) {
                    THEME_LIGHT -> ThemeMode.LIGHT
                    THEME_DARK -> ThemeMode.DARK
                    else -> ThemeMode.SYSTEM
                }
            }

        val unitSystem: Flow<String> =
            dataStore.data.map { prefs ->
                prefs[KEY_UNIT_SYSTEM] ?: UNIT_METRIC
            }

        suspend fun setAppTheme(palette: String) {
            dataStore.edit { prefs ->
                prefs[KEY_APP_THEME] = palette
                // If switching palette explicitly, clear legacy moonlight mode flag
                if (prefs[KEY_THEME_MODE] == THEME_MOONLIGHT && palette == THEME_PRATOO) {
                    prefs[KEY_THEME_MODE] = THEME_SYSTEM
                }
            }
        }

        suspend fun setThemeMode(mode: ThemeMode) {
            dataStore.edit { prefs ->
                prefs[KEY_THEME_MODE] =
                    when (mode) {
                        ThemeMode.LIGHT -> THEME_LIGHT
                        ThemeMode.DARK -> THEME_DARK
                        ThemeMode.SYSTEM -> THEME_SYSTEM
                    }
            }
        }

        suspend fun setUnitSystem(unit: String) {
            dataStore.edit { prefs ->
                prefs[KEY_UNIT_SYSTEM] = unit
            }
        }
    }
