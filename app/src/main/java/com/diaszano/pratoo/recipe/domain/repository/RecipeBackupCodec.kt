package com.diaszano.pratoo.recipe.domain.repository

import com.diaszano.pratoo.recipe.domain.model.Recipe

interface RecipeBackupCodec {
    fun encode(recipes: List<Recipe>): String
    fun decode(json: String): List<Recipe>
}
