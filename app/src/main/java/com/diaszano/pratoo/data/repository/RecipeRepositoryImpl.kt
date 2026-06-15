package com.diaszano.pratoo.data.repository

import androidx.room.withTransaction
import com.diaszano.pratoo.data.local.PratooDatabase
import com.diaszano.pratoo.data.local.dao.IngredientDao
import com.diaszano.pratoo.data.local.dao.MeasurementUnitDao
import com.diaszano.pratoo.data.local.dao.RecipeDao
import com.diaszano.pratoo.data.local.dao.StepDao
import com.diaszano.pratoo.data.local.dao.TagDao
import com.diaszano.pratoo.data.local.entity.MeasurementUnit
import com.diaszano.pratoo.data.local.entity.RecipeTagCrossRef
import com.diaszano.pratoo.data.local.entity.TagEntity
import com.diaszano.pratoo.data.local.relation.RecipeListItem
import com.diaszano.pratoo.data.local.relation.RecipeWithDetails
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecipeRepositoryImpl @Inject constructor(
    private val database: PratooDatabase,
    private val recipeDao: RecipeDao,
    private val ingredientDao: IngredientDao,
    private val stepDao: StepDao,
    private val tagDao: TagDao,
    private val measurementUnitDao: MeasurementUnitDao
) : RecipeRepository {

    override fun observeAllRecipes(): Flow<List<RecipeListItem>> =
        recipeDao.observeAll()

    override fun observeFavoriteRecipes(): Flow<List<RecipeListItem>> =
        recipeDao.observeFavoriteRecipes()

    override fun searchRecipes(query: String?, tagId: Long?): Flow<List<RecipeListItem>> =
        recipeDao.search(query, tagId)

    override fun observeRecipe(id: Long): Flow<RecipeWithDetails?> =
        recipeDao.observeById(id)

    override suspend fun getRecipe(id: Long): RecipeWithDetails? =
        recipeDao.getById(id)

    override suspend fun saveRecipe(
        recipeWithDetails: RecipeWithDetails,
        tags: List<TagEntity>
    ): Long = database.withTransaction {
        val recipe = recipeWithDetails.recipe

        val recipeId = if (recipe.id == 0L) {
            recipeDao.insert(recipe)
        } else {
            recipeDao.update(recipe)

            ingredientDao.deleteByRecipeId(recipe.id)
            stepDao.deleteByRecipeId(recipe.id)
            tagDao.deleteCrossRefsByRecipeId(recipe.id)

            recipe.id
        }

        val ingredients = recipeWithDetails.ingredients
            .filter { it.name.isNotBlank() }
            .mapIndexed { index, ingredient ->
                ingredient.copy(
                    id = 0L,
                    recipeId = recipeId,
                    name = ingredient.name.trim(),
                    quantity = ingredient.quantity.trim(),
                    unit = ingredient.unit.trim(),
                    position = index
                )
            }

        val steps = recipeWithDetails.steps
            .filter { it.text.isNotBlank() }
            .mapIndexed { index, step ->
                step.copy(
                    id = 0L,
                    recipeId = recipeId,
                    text = step.text.trim(),
                    order = index
                )
            }

        ingredientDao.insertAll(ingredients)
        stepDao.insertAll(steps)

        tags
            .map { it.name.trim() }
            .filter { it.isNotBlank() }
            .distinctBy { it.lowercase() }
            .forEach { tagName ->
                val existingTag = tagDao.getByName(tagName)
                val tagId = existingTag?.id ?: tagDao.insert(TagEntity(name = tagName))

                tagDao.insertCrossRef(
                    RecipeTagCrossRef(
                        recipeId = recipeId,
                        tagId = tagId
                    )
                )
            }

        recipeId
    }

    override suspend fun deleteRecipe(id: Long) {
        recipeDao.deleteById(id)
    }

    override suspend fun deleteAllRecipes() {
        recipeDao.deleteAll()
    }

    override suspend fun createTag(name: String): Long {
        val trimmed = name.trim()
        if (trimmed.isBlank()) return 0L
        val existing = tagDao.getByName(trimmed)
        if (existing != null) return existing.id
        return tagDao.insert(TagEntity(name = trimmed))
    }

    override suspend fun toggleFavorite(id: Long) {
        val recipe = recipeDao.getById(id) ?: return
        recipeDao.updateFavorite(id, !recipe.recipe.isFavorite)
    }

    override fun observeAllTags(): Flow<List<TagEntity>> =
        tagDao.observeAll()

    override suspend fun getTagByName(name: String): TagEntity? =
        tagDao.getByName(name.trim())

    override suspend fun deleteTag(id: Long) {
        tagDao.deleteById(id)
    }

    override suspend fun getAllRecipesWithDetails(): List<RecipeWithDetails> =
        recipeDao.getAllWithDetails()

    override fun observeMeasurementUnits(): Flow<List<MeasurementUnit>> =
        measurementUnitDao.observeAll()
}
