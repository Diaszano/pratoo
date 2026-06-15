package com.diaszano.pratoo.recipe.domain.model

/** Lookup category for measurement units (e.g. weight, volume, kitchen). */
data class MeasurementCategory(
    val id: Long,
    val code: String,
    val displayName: String,
    val sortOrder: Int,
)
