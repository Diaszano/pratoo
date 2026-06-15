package com.diaszano.pratoo.recipe.domain.model

data class RecipeListItem(
    val id: Long,
    val title: String,
    val imageUri: String? = null,
    val isFavorite: Boolean = false,
    val updatedAt: Long = 0L
)
