package com.diaszano.pratoo.recipe.adapter.out.persistence.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.diaszano.pratoo.recipe.adapter.out.persistence.entity.MeasurementUnitEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MeasurementUnitDao {

    @Query("SELECT * FROM measurement_units ORDER BY category, displayName")
    fun observeAll(): Flow<List<MeasurementUnitEntity>>

    @Query("SELECT * FROM measurement_units ORDER BY category, displayName")
    suspend fun getAll(): List<MeasurementUnitEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(units: List<MeasurementUnitEntity>)

    @Query("SELECT COUNT(*) FROM measurement_units")
    suspend fun count(): Int
}
