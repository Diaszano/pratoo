package com.diaszano.pratoo.recipe.adapter.out.persistence.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "measurement_units",
    foreignKeys = [
        ForeignKey(
            entity = MeasurementCategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["category_id"],
            onDelete = ForeignKey.RESTRICT,
        ),
    ],
    indices = [
        Index(value = ["abbreviation"], unique = true),
        Index(value = ["category_id"]),
        Index(value = ["category_id", "displayName"]),
    ],
)
data class MeasurementUnitEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val abbreviation: String,
    val displayName: String,
    @ColumnInfo(name = "category_id")
    val categoryId: Long,
)
