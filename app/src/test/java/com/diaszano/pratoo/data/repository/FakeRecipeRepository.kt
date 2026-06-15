package com.diaszano.pratoo.data.repository

import com.diaszano.pratoo.recipe.domain.model.Ingredient
import com.diaszano.pratoo.recipe.domain.model.MeasurementCategory
import com.diaszano.pratoo.recipe.domain.model.MeasurementUnit
import com.diaszano.pratoo.recipe.domain.model.Recipe
import com.diaszano.pratoo.recipe.domain.model.RecipeListItem
import com.diaszano.pratoo.recipe.domain.model.RecipeStep
import com.diaszano.pratoo.recipe.domain.model.Tag
import com.diaszano.pratoo.recipe.domain.repository.RecipeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeRecipeRepository : RecipeRepository {

    private val recipes = mutableListOf<Recipe>()
    private val tags = mutableListOf<Tag>()
    private val crossRefs = mutableListOf<Pair<Long, Long>>()
    private val recipesFlow = MutableStateFlow<List<Recipe>>(emptyList())
    private val tagsFlow = MutableStateFlow<List<Tag>>(emptyList())
    private var nextId = 1L

    override fun observeAllRecipes(): Flow<List<RecipeListItem>> =
        recipesFlow.map { list ->
            list.map {
                RecipeListItem(
                    id = it.id,
                    title = it.title,
                    imageUri = it.imageUri,
                    isFavorite = it.isFavorite,
                    updatedAt = it.updatedAt
                )
            }
        }

    override fun observeFavoriteRecipes(): Flow<List<RecipeListItem>> =
        recipesFlow.map { list ->
            list
                .filter { it.isFavorite }
                .map {
                    RecipeListItem(
                        id = it.id,
                        title = it.title,
                        imageUri = it.imageUri,
                        isFavorite = it.isFavorite,
                        updatedAt = it.updatedAt
                    )
                }
        }

    override fun searchRecipes(query: String?, tagId: Long?): Flow<List<RecipeListItem>> =
        recipesFlow.map { list ->
            list
                .filter { recipe ->
                    val matchesQuery = query.isNullOrBlank() ||
                        recipe.title.contains(query, ignoreCase = true) ||
                        recipe.ingredients.any { it.name.contains(query, ignoreCase = true) }
                    val matchesTag = tagId == null ||
                        crossRefs.any { it.first == recipe.id && it.second == tagId }
                    matchesQuery && matchesTag
                }
                .map {
                    RecipeListItem(
                        id = it.id,
                        title = it.title,
                        imageUri = it.imageUri,
                        isFavorite = it.isFavorite,
                        updatedAt = it.updatedAt
                    )
                }
        }

    override fun observeRecipe(id: Long): Flow<Recipe?> =
        recipesFlow.map { list -> list.find { it.id == id } }

    override suspend fun getRecipe(id: Long): Recipe? =
        recipes.find { it.id == id }

    override suspend fun saveRecipe(recipe: Recipe): Long {
        val id = if (recipe.id == 0L) {
            nextId++
        } else {
            recipes.removeAll { it.id == recipe.id }
            crossRefs.removeAll { it.first == recipe.id }
            recipe.id
        }

        val resolvedTags = recipe.tags.map { tag ->
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

        val savedRecipe = recipe.copy(
            id = id,
            ingredients = recipe.ingredients.map { it.copy(id = 0L) },
            steps = recipe.steps.map { it.copy(id = 0L) },
            tags = resolvedTags
        )
        recipes.add(savedRecipe)

        recipesFlow.value = recipes.toList()
        tagsFlow.value = this.tags.toList()
        return id
    }

    override suspend fun deleteRecipe(id: Long) {
        recipes.removeAll { it.id == id }
        crossRefs.removeAll { it.first == id }
        recipesFlow.value = recipes.toList()
    }

    override suspend fun deleteAllRecipes() {
        recipes.clear()
        crossRefs.clear()
        recipesFlow.value = emptyList()
    }

    override suspend fun toggleFavorite(id: Long) {
        val index = recipes.indexOfFirst { it.id == id }
        if (index >= 0) {
            val recipe = recipes[index]
            recipes[index] = recipe.copy(isFavorite = !recipe.isFavorite)
            recipesFlow.value = recipes.toList()
        }
    }

    override fun observeAllTags(): Flow<List<Tag>> = tagsFlow

    override suspend fun getTagByName(name: String): Tag? {
        val trimmed = name.trim()
        if (trimmed.isBlank()) return null
        return tags.find { it.name.equals(trimmed, ignoreCase = true) }
    }

    override suspend fun createTag(name: String): Long {
        val trimmed = name.trim()
        if (trimmed.isBlank()) return 0L
        val existing = tags.find { it.name.equals(trimmed, ignoreCase = true) }
        if (existing != null) return existing.id
        val id = nextId++
        tags.add(Tag(id = id, name = trimmed))
        tagsFlow.value = tags.toList()
        return id
    }

    override suspend fun deleteTag(id: Long) {
        tags.removeAll { it.id == id }
        tagsFlow.value = tags.toList()
    }

    override suspend fun getAllRecipes(): List<Recipe> = recipes.toList()

    override fun observeMeasurementUnits(): Flow<List<MeasurementUnit>> =
        MutableStateFlow(emptyList())
}
