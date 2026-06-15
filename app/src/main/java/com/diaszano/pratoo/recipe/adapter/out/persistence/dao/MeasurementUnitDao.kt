package com.diaszano.pratoo.recipe.adapter.out.persistence.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.diaszano.pratoo.recipe.adapter.out.persistence.entity.MeasurementUnitEntity
import com.diaszano.pratoo.recipe.adapter.out.persistence.relation.MeasurementUnitWithCategoryProjection
import kotlinx.coroutines.flow.Flow

@Dao
interface MeasurementUnitDao {

    @Query("""
        SELECT mu.*
        FROM measurement_units mu
        INNER JOIN measurement_categories mc ON mc.id = mu.category_id
        ORDER BY mc.sort_order ASC, mu.displayName ASC
    """)
    fun observeAll(): Flow<List<MeasurementUnitEntity>>

    @Query("""
        SELECT mu.*
        FROM measurement_units mu
        INNER JOIN measurement_categories mc ON mc.id = mu.category_id
        ORDER BY mc.sort_order ASC, mu.displayName ASC
    """)
    suspend fun getAll(): List<MeasurementUnitEntity>

    @Query("""
        SELECT
            mu.id AS id,
            mu.abbreviation AS abbreviation,
            mu.displayName AS displayName,
            mu.category_id AS categoryId,
            mc.code AS categoryCode,
            mc.display_name AS categoryDisplayName,
            mc.sort_order AS categorySortOrder
        FROM measurement_units mu
        INNER JOIN measurement_categories mc ON mc.id = mu.category_id
        ORDER BY mc.sort_order ASC, mu.displayName ASC
    """)
    fun observeAllWithCategory(): Flow<List<MeasurementUnitWithCategoryProjection>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(units: List<MeasurementUnitEntity>)

    @Query("SELECT COUNT(*) FROM measurement_units")
    suspend fun count(): Int
}
