package com.diaszano.pratoo.backup.application

import com.diaszano.pratoo.backup.domain.model.BackupStatus
import com.diaszano.pratoo.backup.domain.port.BackupSettingsRepository
import com.diaszano.pratoo.backup.domain.port.CloudBackupStorage
import com.diaszano.pratoo.backup.domain.port.RecipeBackupExporter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class BackupNowUseCase
    @Inject
    constructor(
        private val exporter: RecipeBackupExporter,
        private val cloudStorage: CloudBackupStorage,
        private val settingsRepository: BackupSettingsRepository,
    ) {
        suspend operator fun invoke() {
            settingsRepository.setLastBackupAttemptAt(System.currentTimeMillis())
            settingsRepository.setLastBackupStatus(BackupStatus.InProgress)

            withContext(Dispatchers.IO) {
                try {
                    val (jsonContent, metadata) = exporter.exportBackup()
                    cloudStorage.uploadLatestBackup(jsonContent, metadata)
                    settingsRepository.setLastSuccessfulBackupAt(System.currentTimeMillis())
                    settingsRepository.setLastBackupStatus(BackupStatus.Success)
                } catch (e: Exception) {
                    val status =
                        if (e.message?.contains("permission", ignoreCase = true) == true ||
                            e.message?.contains("Unauthorized", ignoreCase = true) == true
                        ) {
                            BackupStatus.RequiresPermission
                        } else {
                            BackupStatus.Failed
                        }
                    settingsRepository.setLastBackupStatus(status)
                }
            }
        }
    }
