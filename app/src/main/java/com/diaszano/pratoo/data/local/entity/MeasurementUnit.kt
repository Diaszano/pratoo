package com.diaszano.pratoo.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "measurement_units")
data class MeasurementUnit(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val abbreviation: String,
    val displayName: String,
    val category: String
)
