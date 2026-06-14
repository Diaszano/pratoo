package com.diaszano.pratoo.ui.settings

import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diaszano.pratoo.data.repository.BackupManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class SettingsUiState(
    val isExporting: Boolean = false,
    val isImporting: Boolean = false,
    val message: String? = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val backupManager: BackupManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    fun exportToUri(uri: Uri, context: android.content.Context) {
        viewModelScope.launch {
            _uiState.value = SettingsUiState(isExporting = true)
            try {
                val json = withContext(Dispatchers.IO) { backupManager.exportToJson() }
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
                backupManager.importFromJson(context, uri)
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
}
