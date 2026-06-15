package com.diaszano.pratoo.backup.adapter.out.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.diaszano.pratoo.backup.domain.model.BackupStatus
import com.diaszano.pratoo.backup.domain.port.BackupSettingsRepository
import com.diaszano.pratoo.backup.domain.port.CloudBackupStorage
import com.diaszano.pratoo.backup.domain.port.RecipeBackupExporter
import com.google.android.gms.auth.api.signin.GoogleSignIn
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

@HiltWorker
class DriveBackupWorker
    @AssistedInject
    constructor(
        @Assisted appContext: Context,
        @Assisted workerParams: WorkerParameters,
        private val exporter: RecipeBackupExporter,
        private val cloudStorage: CloudBackupStorage,
        private val settingsRepository: BackupSettingsRepository,
    ) : CoroutineWorker(appContext, workerParams) {
        override suspend fun doWork(): Result {
            val settings = settingsRepository.observeBackupSettings().first()

            if (!settings.automaticDriveBackupEnabled) {
                return Result.success()
            }

            val account = GoogleSignIn.getLastSignedInAccount(applicationContext)
            if (account == null) {
                settingsRepository.setLastBackupStatus(BackupStatus.RequiresPermission)
                return Result.success()
            }

            return try {
                settingsRepository.setLastBackupAttemptAt(System.currentTimeMillis())
                settingsRepository.setLastBackupStatus(BackupStatus.InProgress)

                val (jsonContent, metadata) = exporter.exportBackup()
                cloudStorage.uploadLatestBackup(jsonContent, metadata)

                settingsRepository.setLastSuccessfulBackupAt(System.currentTimeMillis())
                settingsRepository.setLastBackupStatus(BackupStatus.Success)
                Result.success()
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
                if (status == BackupStatus.Failed) {
                    Result.retry()
                } else {
                    Result.success()
                }
            }
        }
    }
