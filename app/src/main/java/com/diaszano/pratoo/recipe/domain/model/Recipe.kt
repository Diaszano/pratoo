package com.diaszano.pratoo.recipe.domain.model

/** Represents a complete recipe with metadata, sections, and tags. */
data class Recipe(
    val id: Long = 0,
    val title: String,
    val notes: String = "",
    val imageUri: String? = null,
    val servings: Int = 1,
    val prepTimeMinutes: Int = 0,
    val cookTimeMinutes: Int = 0,
    val sourceUrl: String? = null,
    val isFavorite: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val sections: List<RecipeSection> = emptyList(),
    val tags: List<Tag> = emptyList()
)
