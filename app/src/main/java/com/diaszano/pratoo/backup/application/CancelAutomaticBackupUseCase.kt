package com.diaszano.pratoo.backup.application

import com.diaszano.pratoo.backup.domain.port.BackupSettingsRepository
import javax.inject.Inject

class CancelAutomaticBackupUseCase
    @Inject
    constructor(
        private val settingsRepository: BackupSettingsRepository,
    ) {
        suspend operator fun invoke() {
            settingsRepository.setAutomaticBackupEnabled(false)
        }
    }
