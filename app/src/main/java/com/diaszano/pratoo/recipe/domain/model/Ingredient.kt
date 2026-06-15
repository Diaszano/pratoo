package com.diaszano.pratoo.recipe.domain.model

/** A single ingredient within a recipe, identified by name with an optional quantity and unit abbreviation. */
data class Ingredient(
    val id: Long = 0,
    val name: String,
    val quantity: String = "",
    val unit: String = "",
    val position: Int = 0,
)
