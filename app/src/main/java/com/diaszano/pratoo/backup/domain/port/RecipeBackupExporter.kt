package com.diaszano.pratoo.backup.domain.port

import com.diaszano.pratoo.backup.domain.model.BackupMetadata

interface RecipeBackupExporter {
    suspend fun exportBackup(): Pair<String, BackupMetadata>
}
