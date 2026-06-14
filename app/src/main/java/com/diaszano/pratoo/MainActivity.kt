package com.diaszano.pratoo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.diaszano.pratoo.data.settings.AppPreferences
import com.diaszano.pratoo.data.settings.ThemeMode
import com.diaszano.pratoo.ui.navigation.PratooNavGraph
import com.diaszano.pratoo.ui.theme.PratooTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var appPreferences: AppPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeMode by appPreferences.themeMode.collectAsStateWithLifecycle(
                initialValue = ThemeMode.SYSTEM
            )
            val isDarkTheme = when (themeMode) {
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
                ThemeMode.MOONLIGHT -> true
                else -> isSystemInDarkTheme()
            }

            PratooTheme(
                darkTheme = isDarkTheme,
                useMoonlight = themeMode == ThemeMode.MOONLIGHT
            ) {
                PratooNavGraph()
            }
        }
    }
}
