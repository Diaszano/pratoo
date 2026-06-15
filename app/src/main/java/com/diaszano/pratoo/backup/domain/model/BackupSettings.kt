package com.diaszano.pratoo.backup.domain.model

data class BackupSettings(
    val automaticDriveBackupEnabled: Boolean = false,
    val lastSuccessfulBackupAt: Long? = null,
    val lastBackupAttemptAt: Long? = null,
    val lastBackupStatus: BackupStatus = BackupStatus.NeverBackedUp,
    val selectedGoogleAccountEmail: String? = null,
)
