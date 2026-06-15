package com.diaszano.pratoo.recipe.application.usecase

import com.diaszano.pratoo.recipe.domain.repository.RecipeBackupCodec
import com.diaszano.pratoo.recipe.domain.repository.RecipeRepository

import javax.inject.Inject

class ImportRecipesBackupUseCase @Inject constructor(
    private val repository: RecipeRepository,
    private val codec: RecipeBackupCodec
) {
    suspend operator fun invoke(json: String) {
        val recipes = codec.decode(json)
        recipes.forEach { recipe ->
            repository.saveRecipe(recipe)
        }
    }
}
