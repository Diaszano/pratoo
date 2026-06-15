package com.diaszano.pratoo.recipe.domain.model

data class Ingredient(
    val id: Long = 0,
    val name: String,
    val quantity: String = "",
    val unit: String = "",
    val position: Int = 0
)
