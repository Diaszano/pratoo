package com.diaszano.pratoo.backup.application

import com.diaszano.pratoo.backup.domain.model.CloudBackupFile
import com.diaszano.pratoo.backup.domain.port.CloudBackupStorage
import javax.inject.Inject

class ListDriveBackupsUseCase
    @Inject
    constructor(
        private val cloudStorage: CloudBackupStorage,
    ) {
        suspend operator fun invoke(): List<CloudBackupFile> = cloudStorage.listBackups()
    }
