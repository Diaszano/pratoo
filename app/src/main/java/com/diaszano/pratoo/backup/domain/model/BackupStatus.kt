package com.diaszano.pratoo.backup.domain.model

enum class BackupStatus {
    NeverBackedUp,
    InProgress,
    Success,
    Failed,
    RequiresPermission,
}
