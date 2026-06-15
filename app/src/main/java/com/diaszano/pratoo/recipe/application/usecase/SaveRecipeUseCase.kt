package com.diaszano.pratoo.recipe.application.usecase

import com.diaszano.pratoo.core.error.ValidationException
import com.diaszano.pratoo.recipe.domain.model.Recipe
import com.diaszano.pratoo.recipe.domain.repository.RecipeRepository
import com.diaszano.pratoo.recipe.domain.validation.RecipeValidator
import javax.inject.Inject

/** Validates, normalizes, and persists a recipe in a single transaction. */
class SaveRecipeUseCase
    @Inject
    constructor(
        private val repository: RecipeRepository,
    ) {
        suspend operator fun invoke(recipe: Recipe): Long {
            val errors = RecipeValidator.validate(recipe)
            if (errors.isNotEmpty()) throw ValidationException(errors)

            val normalized =
                recipe.copy(
                    sections =
                        recipe.sections
                            .map { section ->
                                section.copy(
                                    name = section.name.trim(),
                                    ingredients = RecipeValidator.normalizeIngredients(section.ingredients),
                                    steps = RecipeValidator.normalizeSteps(section.steps),
                                )
                            }.filter { section ->
                                section.ingredients.isNotEmpty() || section.steps.isNotEmpty()
                            }.mapIndexed { sectionIndex, section ->
                                section.copy(position = sectionIndex)
                            },
                    tags = RecipeValidator.normalizeTags(recipe.tags),
                )
            return repository.saveRecipe(normalized)
        }
    }
