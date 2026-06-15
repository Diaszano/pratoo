package com.diaszano.pratoo.backup.domain.model

sealed class CloudBackupError(
    message: String? = null,
    cause: Throwable? = null,
) : Exception(message, cause) {
    data object PermissionRequired : CloudBackupError("Drive permission is required")

    data object NetworkUnavailable : CloudBackupError("Network is not available")

    data object NoBackupsFound : CloudBackupError("No backups found in Drive")

    data object InvalidBackupFile : CloudBackupError("Backup file is corrupted or invalid")

    data object DriveApiError : CloudBackupError("Drive API returned an error")

    data class Unknown(
        override val message: String? = null,
        override val cause: Throwable? = null,
    ) : CloudBackupError(message, cause)
}
