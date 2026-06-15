package com.diaszano.pratoo.backup.domain.port

import com.diaszano.pratoo.backup.domain.model.BackupMetadata
import com.diaszano.pratoo.backup.domain.model.CloudBackupFile

interface CloudBackupStorage {
    suspend fun uploadLatestBackup(
        content: String,
        metadata: BackupMetadata,
    )

    suspend fun listBackups(): List<CloudBackupFile>

    suspend fun downloadBackup(fileId: String): String

    suspend fun deleteOldBackups(maxBackupCount: Int)
}
