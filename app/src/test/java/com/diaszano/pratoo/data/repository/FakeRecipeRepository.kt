package com.diaszano.pratoo.data.repository

import com.diaszano.pratoo.data.local.entity.MeasurementUnit
import com.diaszano.pratoo.data.local.entity.TagEntity
import com.diaszano.pratoo.data.local.relation.RecipeListItem
import com.diaszano.pratoo.data.local.relation.RecipeWithDetails
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeRecipeRepository : RecipeRepository {

    private val recipes = mutableListOf<RecipeWithDetails>()
    private val tags = mutableListOf<TagEntity>()
    private val crossRefs = mutableListOf<Pair<Long, Long>>()
    private val recipesFlow = MutableStateFlow<List<RecipeWithDetails>>(emptyList())
    private val tagsFlow = MutableStateFlow<List<TagEntity>>(emptyList())
    private var nextId = 1L

    override fun observeAllRecipes(): Flow<List<RecipeListItem>> =
        recipesFlow.map { list ->
            list.map {
                RecipeListItem(
                    id = it.recipe.id,
                    title = it.recipe.title,
                    imageUri = it.recipe.imageUri,
                    isFavorite = it.recipe.isFavorite,
                    updatedAt = it.recipe.updatedAt
                )
            }
        }

    override fun observeFavoriteRecipes(): Flow<List<RecipeListItem>> =
        recipesFlow.map { list ->
            list
                .filter { it.recipe.isFavorite }
                .map {
                    RecipeListItem(
                        id = it.recipe.id,
                        title = it.recipe.title,
                        imageUri = it.recipe.imageUri,
                        isFavorite = it.recipe.isFavorite,
                        updatedAt = it.recipe.updatedAt
                    )
                }
        }

    override fun searchRecipes(query: String?, tagId: Long?): Flow<List<RecipeListItem>> =
        recipesFlow.map { list ->
            list
                .filter { recipe ->
                    val matchesQuery = query.isNullOrBlank() ||
                        recipe.recipe.title.contains(query, ignoreCase = true) ||
                        recipe.ingredients.any { it.name.contains(query, ignoreCase = true) }
                    val matchesTag = tagId == null ||
                        crossRefs.any { it.first == recipe.recipe.id && it.second == tagId }
                    matchesQuery && matchesTag
                }
                .map {
                    RecipeListItem(
                        id = it.recipe.id,
                        title = it.recipe.title,
                        imageUri = it.recipe.imageUri,
                        isFavorite = it.recipe.isFavorite,
                        updatedAt = it.recipe.updatedAt
                    )
                }
        }

    override fun observeRecipe(id: Long): Flow<RecipeWithDetails?> =
        recipesFlow.map { list -> list.find { it.recipe.id == id } }

    override suspend fun getRecipe(id: Long): RecipeWithDetails? =
        recipes.find { it.recipe.id == id }

    override suspend fun saveRecipe(
        recipeWithDetails: RecipeWithDetails,
        tags: List<TagEntity>
    ): Long {
        val recipe = recipeWithDetails.recipe
        val id = if (recipe.id == 0L) {
            nextId++
        } else {
            recipes.removeAll { it.recipe.id == recipe.id }
            crossRefs.removeAll { it.first == recipe.id }
            recipe.id
        }

        val resolvedTags = tags.map { tag ->
            val trimmed = tag.name.trim()
            if (trimmed.isBlank()) return@map null
            val existing = this.tags.find { it.name.equals(trimmed, ignoreCase = true) }
            if (existing != null) {
                crossRefs.add(id to existing.id)
                existing
            } else {
                val newId = nextId++
                val newTag = tag.copy(id = newId, name = trimmed)
                this.tags.add(newTag)
                crossRefs.add(id to newTag.id)
                newTag
            }
        }.filterNotNull()

        val savedRecipe = RecipeWithDetails(
            recipe = recipe.copy(id = id),
            ingredients = recipeWithDetails.ingredients.map { it.copy(recipeId = id) },
            steps = recipeWithDetails.steps.map { it.copy(recipeId = id) },
            tags = resolvedTags
        )
        recipes.add(savedRecipe)

        recipesFlow.value = recipes.toList()
        tagsFlow.value = this.tags.toList()
        return id
    }

    override suspend fun deleteRecipe(id: Long) {
        recipes.removeAll { it.recipe.id == id }
        crossRefs.removeAll { it.first == id }
        recipesFlow.value = recipes.toList()
    }

    override suspend fun deleteAllRecipes() {
        recipes.clear()
        crossRefs.clear()
        recipesFlow.value = emptyList()
    }

    override suspend fun toggleFavorite(id: Long) {
        val index = recipes.indexOfFirst { it.recipe.id == id }
        if (index >= 0) {
            val recipe = recipes[index]
            val updated = recipe.copy(recipe = recipe.recipe.copy(isFavorite = !recipe.recipe.isFavorite))
            recipes[index] = updated
            recipesFlow.value = recipes.toList()
        }
    }

    override suspend fun createTag(name: String): Long {
        val trimmed = name.trim()
        if (trimmed.isBlank()) return 0L
        val existing = tags.find { it.name.equals(trimmed, ignoreCase = true) }
        if (existing != null) return existing.id
        val id = nextId++
        tags.add(TagEntity(id = id, name = trimmed))
        tagsFlow.value = tags.toList()
        return id
    }

    override fun observeAllTags(): Flow<List<TagEntity>> = tagsFlow

    override suspend fun getTagByName(name: String): TagEntity? {
        val trimmed = name.trim()
        if (trimmed.isBlank()) return null
        return tags.find { it.name.equals(trimmed, ignoreCase = true) }
    }

    override suspend fun deleteTag(id: Long) {
        tags.removeAll { it.id == id }
        tagsFlow.value = tags.toList()
    }

    override suspend fun getAllRecipesWithDetails(): List<RecipeWithDetails> = recipes.toList()

    override fun observeMeasurementUnits(): Flow<List<MeasurementUnit>> =
        MutableStateFlow(emptyList())
}
