package com.diaszano.pratoo.backup.domain.port

import com.diaszano.pratoo.backup.domain.model.RestoreMode

interface RecipeBackupImporter {
    suspend fun importBackup(
        jsonContent: String,
        mode: RestoreMode,
    )
}
