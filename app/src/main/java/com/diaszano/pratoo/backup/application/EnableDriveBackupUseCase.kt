package com.diaszano.pratoo.backup.application

import com.diaszano.pratoo.backup.domain.port.BackupSettingsRepository
import com.diaszano.pratoo.backup.domain.port.CloudBackupStorage
import javax.inject.Inject

class EnableDriveBackupUseCase
    @Inject
    constructor(
        private val settingsRepository: BackupSettingsRepository,
        private val cloudBackupStorage: CloudBackupStorage,
    ) {
        suspend operator fun invoke() {
            settingsRepository.setAutomaticBackupEnabled(true)
        }
    }
