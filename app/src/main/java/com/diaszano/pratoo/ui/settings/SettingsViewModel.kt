package com.diaszano.pratoo.ui.settings

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diaszano.pratoo.R
import com.diaszano.pratoo.data.settings.AppPreferences
import com.diaszano.pratoo.data.settings.ThemeMode
import com.diaszano.pratoo.recipe.adapter.out.backup.AndroidBackupFileReader
import com.diaszano.pratoo.recipe.application.usecase.ExportRecipesBackupUseCase
import com.diaszano.pratoo.recipe.application.usecase.ImportRecipesBackupUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class SettingsUiState(
    val isExporting: Boolean = false,
    val isImporting: Boolean = false,
    val message: String? = null,
    val appTheme: String = "pratoo",
    val themeMode: String = "system",
    val unitSystem: String = "metric",
)

@HiltViewModel
class SettingsViewModel
    @Inject
    constructor(
        private val exportBackup: ExportRecipesBackupUseCase,
        private val importBackup: ImportRecipesBackupUseCase,
        private val backupFileReader: AndroidBackupFileReader,
        private val appPreferences: AppPreferences,
        @ApplicationContext private val context: Context,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(SettingsUiState())
        val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

        init {
            viewModelScope.launch {
                combine(
                    appPreferences.appTheme,
                    appPreferences.themeMode,
                    appPreferences.unitSystem,
                ) { appTheme, themeMode, unitSystem ->
                    Triple(appTheme, themeMode, unitSystem)
                }.collect { (appTheme, themeMode, unitSystem) ->
                    _uiState.update {
                        it.copy(
                            appTheme = appTheme,
                            themeMode = themeMode.name.lowercase(),
                            unitSystem = unitSystem,
                        )
                    }
                }
            }
        }

        fun onAppThemeChange(palette: String) {
            viewModelScope.launch {
                appPreferences.setAppTheme(palette)
            }
        }

        fun onThemeModeChange(mode: String) {
            viewModelScope.launch {
                val themeMode =
                    when (mode) {
                        "light" -> ThemeMode.LIGHT
                        "dark" -> ThemeMode.DARK
                        else -> ThemeMode.SYSTEM
                    }
                appPreferences.setThemeMode(themeMode)
            }
        }

        fun exportToUri(
            uri: Uri,
            context: Context,
        ) {
            viewModelScope.launch {
                _uiState.value = SettingsUiState(isExporting = true)
                try {
                    val json = withContext(Dispatchers.IO) { exportBackup() }
                    withContext(Dispatchers.IO) {
                        context.contentResolver.openOutputStream(uri)?.use {
                            it.write(json.toByteArray())
                        }
                    }
                    _uiState.value = SettingsUiState(message = context.getString(R.string.export_success))
                } catch (e: Exception) {
                    _uiState.value = SettingsUiState(message = context.getString(R.string.export_error_detail, e.message ?: ""))
                }
            }
        }

        fun importFromUri(
            uri: Uri,
            context: Context,
        ) {
            viewModelScope.launch {
                _uiState.value = SettingsUiState(isImporting = true)
                try {
                    val content = withContext(Dispatchers.IO) { backupFileReader.read(uri) }
                    importBackup(content)
                    val successMsg = context.getString(R.string.import_success)
                    _uiState.value = SettingsUiState(message = successMsg)
                    Toast.makeText(context, successMsg, Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    _uiState.value = SettingsUiState(message = context.getString(R.string.import_error_detail, e.message ?: ""))
                }
            }
        }

        fun clearMessage() {
            _uiState.value = _uiState.value.copy(message = null)
        }

        fun onUnitSystemChange(unit: String) {
            viewModelScope.launch {
                appPreferences.setUnitSystem(unit)
            }
        }
    }
