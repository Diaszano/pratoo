package com.diaszano.pratoo.recipe.adapter.out.persistence.relation

data class MeasurementUnitWithCategoryProjection(
    val id: Long,
    val abbreviation: String,
    val displayName: String,
    val categoryId: Long,
    val categoryCode: String,
    val categoryDisplayName: String,
    val categorySortOrder: Int,
)
