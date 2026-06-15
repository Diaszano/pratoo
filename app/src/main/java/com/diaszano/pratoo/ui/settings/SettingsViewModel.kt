package com.diaszano.pratoo.ui.settings

import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diaszano.pratoo.data.settings.AppPreferences
import com.diaszano.pratoo.data.settings.ThemeMode
import com.diaszano.pratoo.recipe.adapter.out.backup.AndroidBackupFileReader
import com.diaszano.pratoo.recipe.application.usecase.ExportRecipesBackupUseCase
import com.diaszano.pratoo.recipe.application.usecase.ImportRecipesBackupUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class SettingsUiState(
    val isExporting: Boolean = false,
    val isImporting: Boolean = false,
    val message: String? = null,
    val themeMode: String = "system",
    val unitSystem: String = "metric"
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val exportBackup: ExportRecipesBackupUseCase,
    private val importBackup: ImportRecipesBackupUseCase,
    private val backupFileReader: AndroidBackupFileReader,
    private val appPreferences: AppPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            appPreferences.themeMode.collect { mode ->
                _uiState.update { it.copy(themeMode = mode.name.lowercase()) }
            }
        }
        viewModelScope.launch {
            appPreferences.unitSystem.collect { unit ->
                _uiState.update { it.copy(unitSystem = unit) }
            }
        }
    }

    fun exportToUri(uri: Uri, context: android.content.Context) {
        viewModelScope.launch {
            _uiState.value = SettingsUiState(isExporting = true)
            try {
                val json = withContext(Dispatchers.IO) { exportBackup() }
                withContext(Dispatchers.IO) {
                    context.contentResolver.openOutputStream(uri)?.use {
                        it.write(json.toByteArray())
                    }
                }
                _uiState.value = SettingsUiState(message = "Backup exportado com sucesso!")
            } catch (e: Exception) {
                _uiState.value = SettingsUiState(message = "Erro ao exportar: ${e.message}")
            }
        }
    }

    fun importFromUri(uri: Uri, context: android.content.Context) {
        viewModelScope.launch {
            _uiState.value = SettingsUiState(isImporting = true)
            try {
                val content = withContext(Dispatchers.IO) { backupFileReader.read(uri) }
                importBackup(content)
                _uiState.value = SettingsUiState(message = "Receitas importadas com sucesso!")
                Toast.makeText(context, "Receitas importadas com sucesso!", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                _uiState.value = SettingsUiState(message = "Erro ao importar: ${e.message}")
            }
        }
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(message = null)
    }

    fun onThemeModeChange(mode: String) {
        viewModelScope.launch {
            val themeMode = when (mode) {
                "light" -> ThemeMode.LIGHT
                "dark" -> ThemeMode.DARK
                "moonlight" -> ThemeMode.MOONLIGHT
                else -> ThemeMode.SYSTEM
            }
            appPreferences.setThemeMode(themeMode)
        }
    }

    fun onUnitSystemChange(unit: String) {
        viewModelScope.launch {
            appPreferences.setUnitSystem(unit)
        }
    }
}
