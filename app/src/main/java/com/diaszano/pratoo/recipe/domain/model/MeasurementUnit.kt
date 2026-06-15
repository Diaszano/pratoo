package com.diaszano.pratoo.recipe.domain.model

data class MeasurementUnit(
    val id: Long = 0,
    val abbreviation: String,
    val displayName: String,
    val category: String
)
