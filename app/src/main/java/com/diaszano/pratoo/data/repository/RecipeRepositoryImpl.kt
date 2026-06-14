package com.diaszano.pratoo.data.repository

import com.diaszano.pratoo.data.local.dao.IngredientDao
import com.diaszano.pratoo.data.local.dao.MeasurementUnitDao
import com.diaszano.pratoo.data.local.dao.RecipeDao
import com.diaszano.pratoo.data.local.dao.StepDao
import com.diaszano.pratoo.data.local.dao.TagDao
import com.diaszano.pratoo.data.local.entity.IngredientEntity
import com.diaszano.pratoo.data.local.entity.MeasurementUnit
import com.diaszano.pratoo.data.local.entity.RecipeEntity
import com.diaszano.pratoo.data.local.entity.RecipeTagCrossRef
import com.diaszano.pratoo.data.local.entity.StepEntity
import com.diaszano.pratoo.data.local.entity.TagEntity
import com.diaszano.pratoo.data.local.relation.RecipeListItem
import com.diaszano.pratoo.data.local.relation.RecipeWithDetails
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecipeRepositoryImpl @Inject constructor(
    private val recipeDao: RecipeDao,
    private val ingredientDao: IngredientDao,
    private val stepDao: StepDao,
    private val tagDao: TagDao,
    private val measurementUnitDao: MeasurementUnitDao
) : RecipeRepository {

    override fun observeAllRecipes(): Flow<List<RecipeListItem>> =
        recipeDao.observeAll()

    override fun searchRecipes(query: String?, tagId: Long?): Flow<List<RecipeListItem>> =
        recipeDao.search(query, tagId)

    override fun observeRecipe(id: Long): Flow<RecipeWithDetails?> =
        recipeDao.observeById(id)

    override suspend fun getRecipe(id: Long): RecipeWithDetails? =
        recipeDao.getById(id)

    override suspend fun saveRecipe(
        recipeWithDetails: RecipeWithDetails,
        tags: List<TagEntity>
    ): Long {
        val recipe = recipeWithDetails.recipe
        val ingredients = recipeWithDetails.ingredients.mapIndexed { index, ingredient ->
            ingredient.copy(recipeId = recipe.id, position = index)
        }
        val steps = recipeWithDetails.steps.mapIndexed { index, step ->
            step.copy(recipeId = recipe.id, order = index)
        }

        val recipeId = if (recipe.id == 0L) {
            recipeDao.insert(recipe)
        } else {
            recipeDao.update(recipe)
            ingredientDao.deleteByRecipeId(recipe.id)
            stepDao.deleteByRecipeId(recipe.id)
            tagDao.deleteCrossRefsByRecipeId(recipe.id)
            recipe.id
        }

        ingredientDao.insertAll(ingredients.map { it.copy(recipeId = recipeId) })
        stepDao.insertAll(steps.map { it.copy(recipeId = recipeId) })

        tags.forEach { tag ->
            val tagId = if (tag.id == 0L) {
                val existing = tagDao.getByName(tag.name)
                if (existing != null) existing.id else tagDao.insert(tag)
            } else {
                tag.id
            }
            tagDao.insertCrossRef(RecipeTagCrossRef(recipeId, tagId))
        }

        return recipeId
    }

    override suspend fun deleteRecipe(id: Long) {
        recipeDao.deleteById(id)
    }

    override suspend fun createTag(name: String): Long {
        val existing = tagDao.getByName(name)
        if (existing != null) return existing.id
        return tagDao.insert(TagEntity(name = name))
    }

    override suspend fun toggleFavorite(id: Long) {
        val recipe = recipeDao.getById(id) ?: return
        recipeDao.updateFavorite(id, !recipe.recipe.isFavorite)
    }

    override fun observeAllTags(): Flow<List<TagEntity>> =
        tagDao.observeAll()

    override suspend fun deleteTag(id: Long) {
        tagDao.deleteById(id)
    }

    override suspend fun getAllRecipesWithDetails(): List<RecipeWithDetails> =
        recipeDao.getAllWithDetails()

    override fun observeMeasurementUnits(): Flow<List<MeasurementUnit>> =
        measurementUnitDao.observeAll()
}
