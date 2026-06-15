package com.diaszano.pratoo.recipe.adapter.out.persistence.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.diaszano.pratoo.recipe.adapter.out.persistence.entity.IngredientEntity

@Dao
interface IngredientDao {

    @Query("SELECT * FROM ingredients WHERE section_id = :sectionId ORDER BY position")
    suspend fun getBySectionId(sectionId: Long): List<IngredientEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(ingredients: List<IngredientEntity>)

    @Delete
    suspend fun delete(ingredient: IngredientEntity)
}
