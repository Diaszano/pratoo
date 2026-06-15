package com.diaszano.pratoo.recipe.adapter.out.persistence.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.diaszano.pratoo.recipe.adapter.out.persistence.entity.MeasurementCategoryEntity
import com.diaszano.pratoo.recipe.adapter.out.persistence.entity.MeasurementUnitEntity

data class MeasurementUnitWithCategory(
    @Embedded val unit: MeasurementUnitEntity,
    @Relation(
        parentColumn = "category_id",
        entityColumn = "id"
    )
    val category: MeasurementCategoryEntity
)
