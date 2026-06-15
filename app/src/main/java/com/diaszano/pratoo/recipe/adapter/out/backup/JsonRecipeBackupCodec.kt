package com.diaszano.pratoo.recipe.adapter.out.backup

import com.diaszano.pratoo.recipe.domain.model.Ingredient
import com.diaszano.pratoo.recipe.domain.model.Recipe
import com.diaszano.pratoo.recipe.domain.model.RecipeStep
import com.diaszano.pratoo.recipe.domain.model.Tag
import com.diaszano.pratoo.recipe.domain.repository.RecipeBackupCodec
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class JsonRecipeBackupCodec @Inject constructor() : RecipeBackupCodec {

    companion object {
        const val BACKUP_VERSION = 1
    }

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    override fun encode(recipes: List<Recipe>): String {
        val data = BackupData(
            version = BACKUP_VERSION,
            recipes = recipes.map { recipe ->
                BackupRecipeDto(
                    title = recipe.title,
                    notes = recipe.notes,
                    imageUri = recipe.imageUri,
                    servings = recipe.servings,
                    prepTimeMinutes = recipe.prepTimeMinutes,
                    cookTimeMinutes = recipe.cookTimeMinutes,
                    sourceUrl = recipe.sourceUrl,
                    isFavorite = recipe.isFavorite,
                    createdAt = recipe.createdAt,
                    updatedAt = recipe.updatedAt,
                    ingredients = recipe.ingredients.map {
                        BackupIngredientDto(it.name, it.quantity, it.unit, it.position)
                    },
                    steps = recipe.steps.map {
                        BackupStepDto(it.text, it.order)
                    },
                    tags = recipe.tags.map { BackupTagDto(it.name) }
                )
            }
        )
        return json.encodeToString(BackupData.serializer(), data)
    }

    override fun decode(json: String): List<Recipe> {
        val data = this.json.decodeFromString(BackupData.serializer(), json)

        if (data.version > BACKUP_VERSION) {
            throw IllegalStateException(
                "Backup version ${data.version} is newer than supported version $BACKUP_VERSION"
            )
        }

        return data.recipes.map { dto ->
            Recipe(
                title = dto.title.trim().ifBlank { "Receita sem título" },
                notes = dto.notes.trim(),
                imageUri = dto.imageUri,
                servings = dto.servings.coerceAtLeast(1),
                prepTimeMinutes = dto.prepTimeMinutes.coerceAtLeast(0),
                cookTimeMinutes = dto.cookTimeMinutes.coerceAtLeast(0),
                sourceUrl = dto.sourceUrl,
                isFavorite = dto.isFavorite,
                createdAt = dto.createdAt ?: System.currentTimeMillis(),
                updatedAt = dto.updatedAt ?: System.currentTimeMillis(),
                ingredients = dto.ingredients
                    .filter { it.name.isNotBlank() }
                    .mapIndexed { index, ing ->
                        Ingredient(
                            name = ing.name.trim(),
                            quantity = ing.quantity.trim(),
                            unit = ing.unit.trim(),
                            position = index
                        )
                    },
                steps = dto.steps
                    .filter { it.text.isNotBlank() }
                    .mapIndexed { index, step ->
                        RecipeStep(text = step.text.trim(), order = index)
                    },
                tags = dto.tags
                    .map { it.name.trim() }
                    .filter { it.isNotBlank() }
                    .distinctBy { it.lowercase() }
                    .map { Tag(name = it) }
            )
        }
    }

    // ── Internal DTOs ──────────────────────────────────────────────

    @Serializable
    private data class BackupData(
        val version: Int = 1,
        val exportedAt: Long = System.currentTimeMillis(),
        val recipes: List<BackupRecipeDto> = emptyList()
    )

    @Serializable
    private data class BackupRecipeDto(
        val title: String,
        val notes: String = "",
        val imageUri: String? = null,
        val servings: Int = 1,
        val prepTimeMinutes: Int = 0,
        val cookTimeMinutes: Int = 0,
        val sourceUrl: String? = null,
        val isFavorite: Boolean = false,
        val createdAt: Long? = null,
        val updatedAt: Long? = null,
        val ingredients: List<BackupIngredientDto> = emptyList(),
        val steps: List<BackupStepDto> = emptyList(),
        val tags: List<BackupTagDto> = emptyList()
    )

    @Serializable
    private data class BackupIngredientDto(
        val name: String,
        val quantity: String = "",
        val unit: String = "",
        val position: Int = 0
    )

    @Serializable
    private data class BackupStepDto(
        val text: String,
        val order: Int = 0
    )

    @Serializable
    private data class BackupTagDto(
        val name: String
    )
}
