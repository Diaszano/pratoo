package com.diaszano.pratoo.recipe.adapter.out.persistence.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "measurement_categories",
    indices = [
        Index(value = ["code"], unique = true),
        Index(value = ["sort_order"]),
    ],
)
data class MeasurementCategoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val code: String,
    @ColumnInfo(name = "display_name")
    val displayName: String,
    @ColumnInfo(name = "sort_order")
    val sortOrder: Int,
)
