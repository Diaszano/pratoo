package com.diaszano.pratoo.backup.adapter.out.cloud

import androidx.room.withTransaction
import com.diaszano.pratoo.backup.domain.model.RestoreMode
import com.diaszano.pratoo.backup.domain.port.RecipeBackupImporter
import com.diaszano.pratoo.recipe.adapter.out.backup.JsonRecipeBackupCodec
import com.diaszano.pratoo.recipe.database.AppDatabase
import com.diaszano.pratoo.recipe.domain.repository.RecipeRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DriveRecipeBackupImporter
    @Inject
    constructor(
        private val repository: RecipeRepository,
        private val codec: JsonRecipeBackupCodec,
        private val database: AppDatabase,
    ) : RecipeBackupImporter {
        override suspend fun importBackup(
            jsonContent: String,
            mode: RestoreMode,
        ) {
            val recipes = codec.decode(jsonContent)

            when (mode) {
                RestoreMode.ReplaceLocalData -> {
                    database.withTransaction {
                        repository.deleteAllRecipes()
                        for (recipe in recipes) {
                            repository.saveRecipe(recipe)
                        }
                    }
                }
                RestoreMode.Merge -> {
                    for (recipe in recipes) {
                        repository.saveRecipe(recipe)
                    }
                }
            }
        }
    }
