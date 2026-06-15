package com.diaszano.pratoo.backup.adapter.out.cloud

import android.content.Context
import android.os.Build
import com.diaszano.pratoo.backup.domain.model.BackupMetadata
import com.diaszano.pratoo.backup.domain.port.RecipeBackupExporter
import com.diaszano.pratoo.recipe.adapter.out.backup.JsonRecipeBackupCodec
import com.diaszano.pratoo.recipe.domain.repository.RecipeRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DriveRecipeBackupExporter
    @Inject
    constructor(
        private val repository: RecipeRepository,
        private val codec: JsonRecipeBackupCodec,
        @ApplicationContext private val context: Context,
    ) : RecipeBackupExporter {
        override suspend fun exportBackup(): Pair<String, BackupMetadata> {
            val recipes = repository.getAllRecipes()
            val appVersionName =
                try {
                    context.packageManager.getPackageInfo(context.packageName, 0).versionName
                } catch (_: Exception) {
                    null
                }
            val appVersionCode =
                try {
                    context.packageManager
                        .getPackageInfo(context.packageName, 0)
                        .longVersionCode
                        .toInt()
                } catch (_: Exception) {
                    null
                }

            val metadata =
                BackupMetadata(
                    backupVersion = JsonRecipeBackupCodec.BACKUP_VERSION,
                    exportedAt = System.currentTimeMillis(),
                    appVersionName = appVersionName,
                    appVersionCode = appVersionCode,
                    deviceName = Build.MODEL,
                    recipeCount = recipes.size,
                )

            val jsonContent =
                codec.encodeWithMetadata(
                    recipes = recipes,
                    appVersionName = appVersionName,
                    appVersionCode = appVersionCode,
                    deviceName = Build.MODEL,
                )

            return Pair(jsonContent, metadata)
        }
    }
