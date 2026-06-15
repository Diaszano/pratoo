package com.diaszano.pratoo.recipe.domain.model

/** A user-defined label for categorizing and filtering recipes. */
data class Tag(
    val id: Long = 0,
    val name: String,
)
