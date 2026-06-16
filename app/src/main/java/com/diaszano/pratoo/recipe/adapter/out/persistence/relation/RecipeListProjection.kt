package com.diaszano.pratoo.recipe.adapter.out.persistence.relation

data class RecipeListProjection(
    val id: Long,
    val title: String,
    val imageUri: String?,
    val isFavorite: Boolean,
    val updatedAt: Long,
    val deletedAt: Long?,
)
