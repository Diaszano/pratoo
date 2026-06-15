package com.diaszano.pratoo.recipe.adapter.out.persistence.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recipes")
data class RecipeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val notes: String = "",
    @ColumnInfo(name = "image_uri")
    val imageUri: String? = null,
    val servings: Int = 1,
    @ColumnInfo(name = "prep_time_minutes")
    val prepTimeMinutes: Int = 0,
    @ColumnInfo(name = "cook_time_minutes")
    val cookTimeMinutes: Int = 0,
    @ColumnInfo(name = "source_url")
    val sourceUrl: String? = null,
    @ColumnInfo(name = "is_favorite")
    val isFavorite: Boolean = false,
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis(),
)
