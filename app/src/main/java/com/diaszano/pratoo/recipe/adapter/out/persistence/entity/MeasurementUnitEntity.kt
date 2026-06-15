package com.diaszano.pratoo.recipe.adapter.out.persistence.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "measurement_units")
data class MeasurementUnitEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val abbreviation: String,
    val displayName: String,
    val category: String
)
