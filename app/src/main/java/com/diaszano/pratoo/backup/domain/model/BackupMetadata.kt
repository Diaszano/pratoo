package com.diaszano.pratoo.backup.domain.model

data class BackupMetadata(
    val backupVersion: Int = 1,
    val exportedAt: Long = System.currentTimeMillis(),
    val appVersionName: String? = null,
    val appVersionCode: Int? = null,
    val deviceName: String? = null,
    val recipeCount: Int = 0,
    val schemaVersion: Int? = null,
)
