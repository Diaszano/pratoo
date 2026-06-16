package com.diaszano.pratoo.backup.adapter.ui

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diaszano.pratoo.R
import com.diaszano.pratoo.backup.adapter.out.cloud.GoogleDriveAuthorizationManager
import com.diaszano.pratoo.backup.adapter.out.worker.BackupWorkerScheduler
import com.diaszano.pratoo.backup.application.BackupNowUseCase
import com.diaszano.pratoo.backup.application.DisableDriveBackupUseCase
import com.diaszano.pratoo.backup.application.EnableDriveBackupUseCase
import com.diaszano.pratoo.backup.application.ListDriveBackupsUseCase
import com.diaszano.pratoo.backup.application.ObserveBackupSettingsUseCase
import com.diaszano.pratoo.backup.application.RestoreDriveBackupUseCase
import com.diaszano.pratoo.backup.domain.model.BackupStatus
import com.diaszano.pratoo.backup.domain.model.CloudBackupFile
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BackupSettingsUiState(
    val isSignedIn: Boolean = false,
    val signedInEmail: String? = null,
    val automaticBackupEnabled: Boolean = false,
    val isBackingUp: Boolean = false,
    val isRestoring: Boolean = false,
    val lastBackupStatus: BackupStatus = BackupStatus.NeverBackedUp,
    val lastSuccessfulBackupAt: Long? = null,
    val lastBackupAttemptAt: Long? = null,
    val availableBackups: List<CloudBackupFile> = emptyList(),
    val isShowingBackupList: Boolean = false,
    val isShowingRestoreConfirmation: Boolean = false,
    val selectedBackup: CloudBackupFile? = null,
    val message: String? = null,
    val isLoadingBackups: Boolean = false,
)

@HiltViewModel
class BackupSettingsViewModel
    @Inject
    constructor(
        private val observeBackupSettings: ObserveBackupSettingsUseCase,
        private val enableDriveBackup: EnableDriveBackupUseCase,
        private val disableDriveBackup: DisableDriveBackupUseCase,
        private val backupNow: BackupNowUseCase,
        private val listDriveBackups: ListDriveBackupsUseCase,
        private val restoreDriveBackup: RestoreDriveBackupUseCase,
        private val authorizationManager: GoogleDriveAuthorizationManager,
        private val workerScheduler: BackupWorkerScheduler,
        @param:ApplicationContext private val context: Context,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(BackupSettingsUiState())
        val uiState: StateFlow<BackupSettingsUiState> = _uiState.asStateFlow()

        init {
            viewModelScope.launch {
                observeBackupSettings().collect { settings ->
                    _uiState.update {
                        it.copy(
                            isSignedIn = authorizationManager.isSignedIn(),
                            signedInEmail =
                                authorizationManager.getLastSignedInAccount()?.email
                                    ?: settings.selectedGoogleAccountEmail,
                            automaticBackupEnabled = settings.automaticDriveBackupEnabled,
                            lastBackupStatus = settings.lastBackupStatus,
                            lastSuccessfulBackupAt = settings.lastSuccessfulBackupAt,
                            lastBackupAttemptAt = settings.lastBackupAttemptAt,
                        )
                    }
                }
            }
        }

        fun getSignInIntent(): Intent = authorizationManager.getSignInIntent()

        fun handleSignInResult(data: Intent?) {
            viewModelScope.launch {
                authorizationManager.handleSignInResult(
                    data = data,
                    onSuccess = { account ->
                        _uiState.update {
                            it.copy(
                                isSignedIn = true,
                                signedInEmail = account.email,
                                message = null,
                            )
                        }
                    },
                    onError = {
                        _uiState.update {
                            it.copy(
                                message = context.getString(R.string.backup_connect_error),
                            )
                        }
                    },
                )
            }
        }

        fun signOut() {
            viewModelScope.launch {
                authorizationManager.signOut()
                workerScheduler.cancelPeriodicBackup()
                _uiState.update {
                    it.copy(
                        isSignedIn = false,
                        signedInEmail = null,
                        automaticBackupEnabled = false,
                    )
                }
            }
        }

        fun onAutomaticBackupChange(enabled: Boolean) {
            viewModelScope.launch {
                if (enabled) {
                    enableDriveBackup()
                    workerScheduler.schedulePeriodicBackup()
                } else {
                    disableDriveBackup()
                    workerScheduler.cancelPeriodicBackup()
                }
                _uiState.update { it.copy(automaticBackupEnabled = enabled) }
            }
        }

        fun backupNow() {
            viewModelScope.launch {
                _uiState.update { it.copy(isBackingUp = true, message = null) }
                try {
                    backupNow.invoke()
                    _uiState.update {
                        it.copy(
                            isBackingUp = false,
                            lastBackupStatus = BackupStatus.Success,
                            message = context.getString(R.string.backup_now_success),
                        )
                    }
                } catch (_: Exception) {
                    _uiState.update {
                        it.copy(
                            isBackingUp = false,
                            message = context.getString(R.string.backup_now_error),
                        )
                    }
                }
            }
        }

        fun loadBackups() {
            viewModelScope.launch {
                _uiState.update {
                    it.copy(
                        isLoadingBackups = true,
                        isShowingBackupList = true,
                        message = null,
                    )
                }
                try {
                    val backups = listDriveBackups.invoke()
                    _uiState.update {
                        it.copy(
                            availableBackups = backups,
                            isLoadingBackups = false,
                        )
                    }
                } catch (_: Exception) {
                    _uiState.update {
                        it.copy(
                            isLoadingBackups = false,
                            message = context.getString(R.string.backup_list_error),
                        )
                    }
                }
            }
        }

        fun showRestoreConfirmation(backup: CloudBackupFile) {
            _uiState.update {
                it.copy(
                    selectedBackup = backup,
                    isShowingRestoreConfirmation = true,
                )
            }
        }

        fun dismissRestoreConfirmation() {
            _uiState.update {
                it.copy(
                    selectedBackup = null,
                    isShowingRestoreConfirmation = false,
                )
            }
        }

        fun confirmRestore() {
            val backup = _uiState.value.selectedBackup ?: return
            viewModelScope.launch {
                _uiState.update {
                    it.copy(
                        isShowingRestoreConfirmation = false,
                        isRestoring = true,
                        message = null,
                    )
                }
                try {
                    restoreDriveBackup.invoke(backup.id)
                    _uiState.update {
                        it.copy(
                            isRestoring = false,
                            message = context.getString(R.string.restore_backup_success),
                            isShowingBackupList = false,
                            availableBackups = emptyList(),
                        )
                    }
                } catch (_: Exception) {
                    _uiState.update {
                        it.copy(
                            isRestoring = false,
                            message = context.getString(R.string.restore_backup_error),
                        )
                    }
                }
            }
        }

        fun dismissBackupList() {
            _uiState.update { it.copy(isShowingBackupList = false, availableBackups = emptyList()) }
        }

        fun clearMessage() {
            _uiState.update { it.copy(message = null) }
        }
    }
