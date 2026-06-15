package com.diaszano.pratoo.recipe.domain.model

data class RecipeStep(
    val id: Long = 0,
    val text: String,
    val order: Int = 0
)
