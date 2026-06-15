package com.diaszano.pratoo.backup.application

import com.diaszano.pratoo.backup.domain.model.BackupSettings
import com.diaszano.pratoo.backup.domain.port.BackupSettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveBackupSettingsUseCase
    @Inject
    constructor(
        private val settingsRepository: BackupSettingsRepository,
    ) {
        operator fun invoke(): Flow<BackupSettings> = settingsRepository.observeBackupSettings()
    }
