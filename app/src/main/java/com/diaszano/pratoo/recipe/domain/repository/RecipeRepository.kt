package com.diaszano.pratoo.recipe.domain.repository

import com.diaszano.pratoo.recipe.domain.model.MeasurementUnit
import com.diaszano.pratoo.recipe.domain.model.Recipe
import com.diaszano.pratoo.recipe.domain.model.RecipeListItem
import com.diaszano.pratoo.recipe.domain.model.Tag
import kotlinx.coroutines.flow.Flow

/** Primary port for all recipe persistence and measurement-unit lookups. */
interface RecipeRepository {
    fun observeAllRecipes(): Flow<List<RecipeListItem>>
    fun observeFavoriteRecipes(): Flow<List<RecipeListItem>>
    fun searchRecipes(query: String?, tagId: Long?): Flow<List<RecipeListItem>>
    fun observeRecipe(id: Long): Flow<Recipe?>
    suspend fun getRecipe(id: Long): Recipe?

    /** Inserts a new recipe or updates an existing one, replacing its ingredients, steps, and tags atomically. */
    suspend fun saveRecipe(recipe: Recipe): Long
    suspend fun deleteRecipe(id: Long)
    suspend fun deleteAllRecipes()
    suspend fun toggleFavorite(id: Long)
    fun observeAllTags(): Flow<List<Tag>>
    suspend fun getTagByName(name: String): Tag?
    suspend fun createTag(name: String): Long
    suspend fun deleteTag(id: Long)
    suspend fun getAllRecipes(): List<Recipe>

    /** Observes all measurement units grouped and sorted by category. */
    fun observeMeasurementUnits(): Flow<List<MeasurementUnit>>
}
