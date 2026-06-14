package com.diaszano.pratoo.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.diaszano.pratoo.data.local.entity.IngredientEntity

@Dao
interface IngredientDao {

    @Query("SELECT * FROM ingredients WHERE recipe_id = :recipeId ORDER BY position")
    suspend fun getByRecipeId(recipeId: Long): List<IngredientEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(ingredients: List<IngredientEntity>)

    @Update
    suspend fun update(ingredient: IngredientEntity)

    @Delete
    suspend fun delete(ingredient: IngredientEntity)

    @Query("DELETE FROM ingredients WHERE recipe_id = :recipeId")
    suspend fun deleteByRecipeId(recipeId: Long)
}
