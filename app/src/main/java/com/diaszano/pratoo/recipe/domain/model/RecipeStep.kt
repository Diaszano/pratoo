package com.diaszano.pratoo.recipe.domain.model

/** A single step in a recipe's preparation method, ordered by [order]. */
data class RecipeStep(
    val id: Long = 0,
    val text: String,
    val order: Int = 0
)
