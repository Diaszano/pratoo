package com.diaszano.pratoo.backup.di

import com.diaszano.pratoo.backup.adapter.out.cloud.DriveRecipeBackupExporter
import com.diaszano.pratoo.backup.adapter.out.cloud.DriveRecipeBackupImporter
import com.diaszano.pratoo.backup.adapter.out.cloud.GoogleDriveBackupStorage
import com.diaszano.pratoo.backup.adapter.out.settings.DataStoreBackupSettingsRepository
import com.diaszano.pratoo.backup.domain.port.BackupSettingsRepository
import com.diaszano.pratoo.backup.domain.port.CloudBackupStorage
import com.diaszano.pratoo.backup.domain.port.RecipeBackupExporter
import com.diaszano.pratoo.backup.domain.port.RecipeBackupImporter
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class BackupModule {
    @Binds
    @Singleton
    abstract fun bindBackupSettingsRepository(impl: DataStoreBackupSettingsRepository): BackupSettingsRepository

    @Binds
    @Singleton
    abstract fun bindCloudBackupStorage(impl: GoogleDriveBackupStorage): CloudBackupStorage

    @Binds
    @Singleton
    abstract fun bindRecipeBackupExporter(impl: DriveRecipeBackupExporter): RecipeBackupExporter

    @Binds
    @Singleton
    abstract fun bindRecipeBackupImporter(impl: DriveRecipeBackupImporter): RecipeBackupImporter
}
