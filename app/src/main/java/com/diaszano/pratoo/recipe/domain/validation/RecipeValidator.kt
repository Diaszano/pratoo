package com.diaszano.pratoo.recipe.domain.validation

import com.diaszano.pratoo.R
import com.diaszano.pratoo.recipe.domain.model.Ingredient
import com.diaszano.pratoo.recipe.domain.model.Recipe
import com.diaszano.pratoo.recipe.domain.model.RecipeStep
import com.diaszano.pratoo.recipe.domain.model.Tag

/**
 * Domain-level validation errors for [Recipe] data.
 *
 * Each variant maps to a user-facing message via [toLocalizedMessageRes].
 * Internal names are in English; the resolved message is in the device locale.
 */
sealed interface RecipeValidationError {

    data object EmptyTitle : RecipeValidationError
    data object InvalidServings : RecipeValidationError
    data object NegativePrepTime : RecipeValidationError
    data object NegativeCookTime : RecipeValidationError
    data object EmptyIngredientName : RecipeValidationError

    /** Returns the `@StringRes` identifier for the localized message. */
    fun toLocalizedMessageRes(): Int = when (this) {
        is EmptyTitle -> R.string.validation_empty_title
        is InvalidServings -> R.string.validation_invalid_servings
        is NegativePrepTime -> R.string.validation_negative_prep_time
        is NegativeCookTime -> R.string.validation_negative_cook_time
        is EmptyIngredientName -> R.string.validation_empty_ingredient_name
    }
}

object RecipeValidator {

    fun validate(recipe: Recipe): List<RecipeValidationError> {
        val errors = mutableListOf<RecipeValidationError>()

        if (recipe.title.isBlank()) {
            errors.add(RecipeValidationError.EmptyTitle)
        }
        if (recipe.servings < 1) {
            errors.add(RecipeValidationError.InvalidServings)
        }
        if (recipe.prepTimeMinutes < 0) {
            errors.add(RecipeValidationError.NegativePrepTime)
        }
        if (recipe.cookTimeMinutes < 0) {
            errors.add(RecipeValidationError.NegativeCookTime)
        }
        if (recipe.ingredients.any { it.name.isBlank() }) {
            errors.add(RecipeValidationError.EmptyIngredientName)
        }

        return errors
    }

    fun normalizeIngredients(ingredients: List<Ingredient>): List<Ingredient> =
        ingredients
            .filter { it.name.isNotBlank() }
            .mapIndexed { index, ingredient ->
                ingredient.copy(
                    id = 0L,
                    name = ingredient.name.trim(),
                    quantity = ingredient.quantity.trim(),
                    unit = ingredient.unit.trim(),
                    position = index
                )
            }

    fun normalizeSteps(steps: List<RecipeStep>): List<RecipeStep> =
        steps
            .filter { it.text.isNotBlank() }
            .mapIndexed { index, step ->
                step.copy(
                    id = 0L,
                    text = step.text.trim(),
                    order = index
                )
            }

    fun normalizeTags(tags: List<Tag>): List<Tag> =
        tags
            .map { it.copy(name = it.name.trim()) }
            .filter { it.name.isNotBlank() }
            .distinctBy { it.name.lowercase() }
}
