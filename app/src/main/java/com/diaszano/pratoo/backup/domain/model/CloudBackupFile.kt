package com.diaszano.pratoo.backup.domain.model

data class CloudBackupFile(
    val id: String,
    val name: String,
    val createdAt: Long? = null,
    val modifiedAt: Long? = null,
    val sizeBytes: Long? = null,
    val recipeCount: Int? = null,
    val backupVersion: Int? = null,
)
