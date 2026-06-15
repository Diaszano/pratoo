package com.diaszano.pratoo.recipe.adapter.out.persistence.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.diaszano.pratoo.recipe.adapter.out.persistence.entity.MeasurementCategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MeasurementCategoryDao {
    @Query("SELECT * FROM measurement_categories ORDER BY sort_order ASC, display_name ASC")
    fun observeAll(): Flow<List<MeasurementCategoryEntity>>

    @Query("SELECT * FROM measurement_categories ORDER BY sort_order ASC, display_name ASC")
    suspend fun getAll(): List<MeasurementCategoryEntity>

    @Query("SELECT * FROM measurement_categories WHERE code = :code LIMIT 1")
    suspend fun getByCode(code: String): MeasurementCategoryEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(categories: List<MeasurementCategoryEntity>)

    @Query("SELECT COUNT(*) FROM measurement_categories")
    suspend fun count(): Int
}
