package com.diaszano.pratoo.data.repository

import kotlinx.serialization.Serializable

@Serializable
data class BackupRecipeDto(
    val title: String,
    val notes: String = "",
    val imageUri: String? = null,
    val servings: Int = 1,
    val prepTimeMinutes: Int = 0,
    val cookTimeMinutes: Int = 0,
    val sourceUrl: String? = null,
    val isFavorite: Boolean = false,
    val ingredients: List<BackupIngredientDto> = emptyList(),
    val steps: List<BackupStepDto> = emptyList(),
    val tags: List<BackupTagDto> = emptyList()
)

@Serializable
data class BackupIngredientDto(
    val name: String,
    val quantity: String = "",
    val unit: String = "",
    val position: Int = 0
)

@Serializable
data class BackupStepDto(
    val text: String,
    val order: Int = 0
)

@Serializable
data class BackupTagDto(
    val name: String
)

@Serializable
data class BackupData(
    val version: Int = 1,
    val exportedAt: Long = System.currentTimeMillis(),
    val recipes: List<BackupRecipeDto> = emptyList()
)
