package com.diaszano.pratoo.backup.domain.port

import com.diaszano.pratoo.backup.domain.model.BackupSettings
import com.diaszano.pratoo.backup.domain.model.BackupStatus
import kotlinx.coroutines.flow.Flow

interface BackupSettingsRepository {
    fun observeBackupSettings(): Flow<BackupSettings>

    suspend fun setAutomaticBackupEnabled(enabled: Boolean)

    suspend fun setLastSuccessfulBackupAt(timestamp: Long)

    suspend fun setLastBackupAttemptAt(timestamp: Long)

    suspend fun setLastBackupStatus(status: BackupStatus)

    suspend fun setSelectedGoogleAccountEmail(email: String?)

    suspend fun clearSelectedGoogleAccount()
}
