package com.diaszano.pratoo.recipe.adapter.out.persistence.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.diaszano.pratoo.recipe.adapter.out.persistence.entity.StepEntity

@Dao
interface StepDao {

    @Query("SELECT * FROM steps WHERE recipe_id = :recipeId ORDER BY step_order")
    suspend fun getByRecipeId(recipeId: Long): List<StepEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(steps: List<StepEntity>)

    @Update
    suspend fun update(step: StepEntity)

    @Delete
    suspend fun delete(step: StepEntity)

    @Query("DELETE FROM steps WHERE recipe_id = :recipeId")
    suspend fun deleteByRecipeId(recipeId: Long)
}
