package com.diaszano.pratoo.recipe.application.usecase

import com.diaszano.pratoo.core.error.ValidationException
import com.diaszano.pratoo.recipe.domain.model.Recipe
import com.diaszano.pratoo.recipe.domain.repository.RecipeRepository
import com.diaszano.pratoo.recipe.domain.validation.RecipeValidator
import javax.inject.Inject

class SaveRecipeUseCase @Inject constructor(
    private val repository: RecipeRepository
) {
    suspend operator fun invoke(recipe: Recipe): Long {
        val errors = RecipeValidator.validate(recipe)
        if (errors.isNotEmpty()) throw ValidationException(errors)

        val normalized = recipe.copy(
            ingredients = RecipeValidator.normalizeIngredients(recipe.ingredients),
            steps = RecipeValidator.normalizeSteps(recipe.steps),
            tags = RecipeValidator.normalizeTags(recipe.tags)
        )
        return repository.saveRecipe(normalized)
    }
}
