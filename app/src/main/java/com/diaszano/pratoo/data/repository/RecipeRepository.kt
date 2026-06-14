package com.diaszano.pratoo.data.repository

import com.diaszano.pratoo.data.local.entity.MeasurementUnit
import com.diaszano.pratoo.data.local.entity.TagEntity
import com.diaszano.pratoo.data.local.relation.RecipeListItem
import com.diaszano.pratoo.data.local.relation.RecipeWithDetails
import kotlinx.coroutines.flow.Flow

interface RecipeRepository {
    fun observeAllRecipes(): Flow<List<RecipeListItem>>
    fun searchRecipes(query: String?, tagId: Long?): Flow<List<RecipeListItem>>
    fun observeRecipe(id: Long): Flow<RecipeWithDetails?>
    suspend fun getRecipe(id: Long): RecipeWithDetails?
    suspend fun saveRecipe(
        recipeWithDetails: RecipeWithDetails,
        tags: List<TagEntity>
    ): Long
    suspend fun deleteRecipe(id: Long)
    suspend fun createTag(name: String): Long
    suspend fun toggleFavorite(id: Long)
    fun observeAllTags(): Flow<List<TagEntity>>
    suspend fun deleteTag(id: Long)
    suspend fun getAllRecipesWithDetails(): List<RecipeWithDetails>
    fun observeMeasurementUnits(): Flow<List<MeasurementUnit>>
}
