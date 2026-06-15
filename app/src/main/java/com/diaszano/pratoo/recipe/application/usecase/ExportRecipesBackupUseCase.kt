package com.diaszano.pratoo.recipe.application.usecase

import com.diaszano.pratoo.recipe.domain.repository.RecipeBackupCodec
import com.diaszano.pratoo.recipe.domain.repository.RecipeRepository

import javax.inject.Inject

class ExportRecipesBackupUseCase @Inject constructor(
    private val repository: RecipeRepository,
    private val codec: RecipeBackupCodec
) {
    suspend operator fun invoke(): String {
        val recipes = repository.getAllRecipes()
        return codec.encode(recipes)
    }
}
