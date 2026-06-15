package com.diaszano.pratoo.recipe.domain.model

/** Lightweight projection of a recipe used in list screens — avoids loading ingredients, steps, and tags. */
data class RecipeListItem(
    val id: Long,
    val title: String,
    val imageUri: String? = null,
    val isFavorite: Boolean = false,
    val updatedAt: Long = 0L
)
