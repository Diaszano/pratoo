package com.diaszano.pratoo.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.diaszano.pratoo.data.local.entity.MeasurementUnit
import kotlinx.coroutines.flow.Flow

@Dao
interface MeasurementUnitDao {

    @Query("SELECT * FROM measurement_units ORDER BY category, displayName")
    fun observeAll(): Flow<List<MeasurementUnit>>

    @Query("SELECT * FROM measurement_units ORDER BY category, displayName")
    suspend fun getAll(): List<MeasurementUnit>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(units: List<MeasurementUnit>)

    @Query("SELECT COUNT(*) FROM measurement_units")
    suspend fun count(): Int
}
