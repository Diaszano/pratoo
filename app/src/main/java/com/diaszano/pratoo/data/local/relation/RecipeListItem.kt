package com.diaszano.pratoo.data.local.relation

data class RecipeListItem(
    val id: Long,
    val title: String,
    val imageUri: String?,
    val isFavorite: Boolean,
    val updatedAt: Long
)
