package com.diaszano.pratoo.recipe.domain.model

/** A unit of measurement (e.g. g, kg, ml, xíc) grouped by [category]. */
data class MeasurementUnit(
    val id: Long = 0,
    val abbreviation: String,
    val displayName: String,
    val category: MeasurementCategory,
)
