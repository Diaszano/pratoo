package com.diaszano.pratoo.backup.application

import com.diaszano.pratoo.backup.domain.model.RestoreMode
import com.diaszano.pratoo.backup.domain.port.CloudBackupStorage
import com.diaszano.pratoo.backup.domain.port.RecipeBackupImporter
import javax.inject.Inject

class RestoreDriveBackupUseCase
    @Inject
    constructor(
        private val cloudStorage: CloudBackupStorage,
        private val importer: RecipeBackupImporter,
    ) {
        suspend operator fun invoke(
            fileId: String,
            mode: RestoreMode = RestoreMode.ReplaceLocalData,
        ) {
            val content = cloudStorage.downloadBackup(fileId)
            importer.importBackup(content, mode)
        }
    }
