package com.diaszano.pratoo.data.repository

import android.content.Context
import android.net.Uri
import com.diaszano.pratoo.data.local.entity.IngredientEntity
import com.diaszano.pratoo.data.local.entity.RecipeEntity
import com.diaszano.pratoo.data.local.entity.StepEntity
import com.diaszano.pratoo.data.local.entity.TagEntity
import com.diaszano.pratoo.data.local.relation.RecipeWithDetails
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupManager @Inject constructor(
    private val repository: RecipeRepository
) {
    companion object {
        const val BACKUP_VERSION = 1
    }

    private val json = Json { 
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    suspend fun exportToJson(): String {
        val recipes = repository.getAllRecipesWithDetails()
        val backupData = BackupData(
            version = BACKUP_VERSION,
            recipes = recipes.map { recipe ->
                BackupRecipeDto(
                    title = recipe.recipe.title,
                    notes = recipe.recipe.notes,
                    imageUri = recipe.recipe.imageUri,
                    servings = recipe.recipe.servings,
                    prepTimeMinutes = recipe.recipe.prepTimeMinutes,
                    cookTimeMinutes = recipe.recipe.cookTimeMinutes,
                    sourceUrl = recipe.recipe.sourceUrl,
                    isFavorite = recipe.recipe.isFavorite,
                    ingredients = recipe.ingredients.map {
                        BackupIngredientDto(it.name, it.quantity, it.unit, it.position)
                    },
                    steps = recipe.steps.map {
                        BackupStepDto(it.text, it.order)
                    },
                    tags = recipe.tags.map {
                        BackupTagDto(it.name)
                    }
                )
            }
        )
        return json.encodeToString(BackupData.serializer(), backupData)
    }

    suspend fun importFromJson(context: Context, uri: Uri) {
        val inputStream = context.contentResolver.openInputStream(uri)
            ?: throw IllegalStateException("Could not open file")
        val content = inputStream.bufferedReader().use { it.readText() }
        val backupData = json.decodeFromString(BackupData.serializer(), content)

        if (backupData.version > BACKUP_VERSION) {
            throw IllegalStateException(
                "Backup version ${backupData.version} is newer than supported version $BACKUP_VERSION"
            )
        }

        backupData.recipes.forEach { dto ->
            val recipe = RecipeEntity(
                title = dto.title,
                notes = dto.notes,
                imageUri = dto.imageUri,
                servings = dto.servings,
                prepTimeMinutes = dto.prepTimeMinutes,
                cookTimeMinutes = dto.cookTimeMinutes,
                sourceUrl = dto.sourceUrl,
                isFavorite = dto.isFavorite
            )
            val ingredients = dto.ingredients.map {
                IngredientEntity(recipeId = 0L, name = it.name, quantity = it.quantity, unit = it.unit, position = it.position)
            }
            val steps = dto.steps.map {
                StepEntity(recipeId = 0L, text = it.text, order = it.order)
            }
            val tags = dto.tags.map { TagEntity(name = it.name) }
            repository.saveRecipe(RecipeWithDetails(recipe, ingredients, steps, emptyList()), tags)
        }
    }
}
