package com.diaszano.pratoo.recipe.adapter.out.persistence

import androidx.room.withTransaction
import com.diaszano.pratoo.recipe.adapter.out.persistence.dao.IngredientDao
import com.diaszano.pratoo.recipe.adapter.out.persistence.dao.MeasurementUnitDao
import com.diaszano.pratoo.recipe.adapter.out.persistence.dao.RecipeDao
import com.diaszano.pratoo.recipe.adapter.out.persistence.dao.StepDao
import com.diaszano.pratoo.recipe.adapter.out.persistence.dao.TagDao
import com.diaszano.pratoo.recipe.adapter.out.persistence.entity.RecipeTagCrossRef
import com.diaszano.pratoo.recipe.adapter.out.persistence.entity.TagEntity
import com.diaszano.pratoo.recipe.adapter.out.persistence.mapper.RecipeMapper.toDomain
import com.diaszano.pratoo.recipe.adapter.out.persistence.mapper.RecipeMapper.toEntity
import com.diaszano.pratoo.recipe.database.AppDatabase
import com.diaszano.pratoo.recipe.domain.model.MeasurementUnit
import com.diaszano.pratoo.recipe.domain.model.Recipe
import com.diaszano.pratoo.recipe.domain.model.RecipeListItem
import com.diaszano.pratoo.recipe.domain.model.Tag
import com.diaszano.pratoo.recipe.domain.repository.RecipeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoomRecipeRepository @Inject constructor(
    private val database: AppDatabase,
    private val recipeDao: RecipeDao,
    private val ingredientDao: IngredientDao,
    private val stepDao: StepDao,
    private val tagDao: TagDao,
    private val measurementUnitDao: MeasurementUnitDao
) : RecipeRepository {

    override fun observeAllRecipes(): Flow<List<RecipeListItem>> =
        recipeDao.observeAll().map { list -> list.map { it.toDomain() } }

    override fun observeFavoriteRecipes(): Flow<List<RecipeListItem>> =
        recipeDao.observeFavoriteRecipes().map { list -> list.map { it.toDomain() } }

    override fun searchRecipes(query: String?, tagId: Long?): Flow<List<RecipeListItem>> =
        recipeDao.search(query, tagId).map { list -> list.map { it.toDomain() } }

    override fun observeRecipe(id: Long): Flow<Recipe?> =
        recipeDao.observeById(id).map { it?.toDomain() }

    override suspend fun getRecipe(id: Long): Recipe? =
        recipeDao.getById(id)?.toDomain()

    override suspend fun saveRecipe(recipe: Recipe): Long = database.withTransaction {
        val recipeId = if (recipe.id == 0L) {
            recipeDao.insert(recipe.toEntity())
        } else {
            recipeDao.update(recipe.toEntity())
            ingredientDao.deleteByRecipeId(recipe.id)
            stepDao.deleteByRecipeId(recipe.id)
            tagDao.deleteCrossRefsByRecipeId(recipe.id)
            recipe.id
        }

        val ingredients = recipe.ingredients
            .filter { it.name.isNotBlank() }
            .mapIndexed { index, ingredient ->
                ingredient.copy(id = 0L, position = index)
            }
        ingredientDao.insertAll(ingredients.map { it.toEntity(recipeId) })

        val steps = recipe.steps
            .filter { it.text.isNotBlank() }
            .mapIndexed { index, step ->
                step.copy(id = 0L, order = index)
            }
        stepDao.insertAll(steps.map { it.toEntity(recipeId) })

        recipe.tags
            .map { it.copy(name = it.name.trim()) }
            .filter { it.name.isNotBlank() }
            .distinctBy { it.name.lowercase() }
            .forEach { tag ->
                val existingTag = tagDao.getByName(tag.name)
                val tagId = existingTag?.id ?: tagDao.insert(TagEntity(name = tag.name))
                tagDao.insertCrossRef(RecipeTagCrossRef(recipeId, tagId))
            }

        recipeId
    }

    override suspend fun deleteRecipe(id: Long) {
        recipeDao.deleteById(id)
    }

    override suspend fun deleteAllRecipes() {
        recipeDao.deleteAll()
    }

    override suspend fun toggleFavorite(id: Long) {
        val recipe = recipeDao.getById(id) ?: return
        recipeDao.updateFavorite(id, !recipe.recipe.isFavorite)
    }

    override fun observeAllTags(): Flow<List<Tag>> =
        tagDao.observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun getTagByName(name: String): Tag? =
        tagDao.getByName(name.trim())?.toDomain()

    override suspend fun createTag(name: String): Long {
        val trimmed = name.trim()
        if (trimmed.isBlank()) return 0L
        val existing = tagDao.getByName(trimmed)
        if (existing != null) return existing.id
        return tagDao.insert(TagEntity(name = trimmed))
    }

    override suspend fun deleteTag(id: Long) {
        tagDao.deleteById(id)
    }

    override suspend fun getAllRecipes(): List<Recipe> =
        recipeDao.getAllWithDetails().map { it.toDomain() }

    override fun observeMeasurementUnits(): Flow<List<MeasurementUnit>> =
        measurementUnitDao.observeAllWithCategory().map { list -> list.map { it.toDomain() } }
}
