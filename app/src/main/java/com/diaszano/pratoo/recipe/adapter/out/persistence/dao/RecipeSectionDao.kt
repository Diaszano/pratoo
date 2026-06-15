package com.diaszano.pratoo.recipe.adapter.out.persistence.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.diaszano.pratoo.recipe.adapter.out.persistence.entity.RecipeSectionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecipeSectionDao {

    @Query("SELECT * FROM recipe_sections WHERE recipe_id = :recipeId ORDER BY position ASC")
    fun observeByRecipeId(recipeId: Long): Flow<List<RecipeSectionEntity>>

    @Query("SELECT * FROM recipe_sections WHERE recipe_id = :recipeId ORDER BY position ASC")
    suspend fun getByRecipeId(recipeId: Long): List<RecipeSectionEntity>

    @Insert
    suspend fun insert(section: RecipeSectionEntity): Long

    @Insert
    suspend fun insertAll(sections: List<RecipeSectionEntity>): List<Long>

    @Query("DELETE FROM recipe_sections WHERE recipe_id = :recipeId")
    suspend fun deleteByRecipeId(recipeId: Long)
}
