package com.diaszano.pratoo.recipe.domain.model

/** A section within a recipe that groups related ingredients and preparation steps. */
data class RecipeSection(
    val id: Long = 0,
    val name: String = "",
    val position: Int = 0,
    val ingredients: List<Ingredient> = emptyList(),
    val steps: List<RecipeStep> = emptyList()
)
