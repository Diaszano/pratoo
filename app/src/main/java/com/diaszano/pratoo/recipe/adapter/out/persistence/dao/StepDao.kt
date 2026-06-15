package com.diaszano.pratoo.recipe.adapter.out.persistence.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.diaszano.pratoo.recipe.adapter.out.persistence.entity.StepEntity

@Dao
interface StepDao {

    @Query("SELECT * FROM steps WHERE section_id = :sectionId ORDER BY step_order")
    suspend fun getBySectionId(sectionId: Long): List<StepEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(steps: List<StepEntity>)

    @Delete
    suspend fun delete(step: StepEntity)
}
