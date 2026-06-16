package com.diaszano.pratoo.recipe.domain.validation

import com.diaszano.pratoo.recipe.domain.model.Ingredient
import com.diaszano.pratoo.recipe.domain.model.Recipe
import com.diaszano.pratoo.recipe.domain.model.RecipeSection
import com.diaszano.pratoo.recipe.domain.model.RecipeStep
import com.diaszano.pratoo.recipe.domain.model.Tag
import java.net.URI

/** Domain-level validation errors for [Recipe] data. */
sealed interface RecipeValidationError {
    data object EmptyTitle : RecipeValidationError

    data object InvalidServings : RecipeValidationError

    data object NegativePrepTime : RecipeValidationError

    data object NegativeCookTime : RecipeValidationError

    data object InvalidSourceUrl : RecipeValidationError

    data object EmptyContent : RecipeValidationError

    data class EmptySectionName(
        val sectionIndex: Int,
    ) : RecipeValidationError

    data class EmptySectionContent(
        val sectionIndex: Int,
    ) : RecipeValidationError

    data class EmptyIngredientName(
        val sectionIndex: Int,
        val ingredientIndex: Int,
    ) : RecipeValidationError

    data class EmptyStepText(
        val sectionIndex: Int,
        val stepIndex: Int,
    ) : RecipeValidationError
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

        val sourceUrl = recipe.sourceUrl?.trim().orEmpty()
        if (sourceUrl.isNotBlank() && !sourceUrl.isValidWebUrl()) {
            errors.add(RecipeValidationError.InvalidSourceUrl)
        }

        val sections = recipe.sections.ifEmpty { listOf(RecipeSection()) }
        val hasMultipleSections = sections.size > 1
        var hasAnyContent = false

        sections.forEachIndexed { sectionIndex, section ->
            val hasNamedSection = section.name.isNotBlank()
            val validIngredientCount = section.ingredients.count { it.name.isNotBlank() }
            val validStepCount = section.steps.count { it.text.isNotBlank() }
            val sectionHasContent = validIngredientCount > 0 || validStepCount > 0
            hasAnyContent = hasAnyContent || sectionHasContent

            if (hasMultipleSections && section.name.isBlank()) {
                errors.add(RecipeValidationError.EmptySectionName(sectionIndex))
            }

            if ((hasMultipleSections || hasNamedSection) && !sectionHasContent) {
                errors.add(RecipeValidationError.EmptySectionContent(sectionIndex))
            }

            section.ingredients.forEachIndexed { ingredientIndex, ingredient ->
                if (ingredient.name.isBlank() && ingredient.hasAnyInput()) {
                    errors.add(RecipeValidationError.EmptyIngredientName(sectionIndex, ingredientIndex))
                }
            }

            section.steps.forEachIndexed { stepIndex, step ->
                if (step.text.isBlank() && section.steps.size > 1) {
                    errors.add(RecipeValidationError.EmptyStepText(sectionIndex, stepIndex))
                }
            }
        }

        if (!hasAnyContent) {
            errors.add(RecipeValidationError.EmptyContent)
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
                    position = index,
                )
            }

    fun normalizeSteps(steps: List<RecipeStep>): List<RecipeStep> =
        steps
            .filter { it.text.isNotBlank() }
            .mapIndexed { index, step ->
                step.copy(
                    id = 0L,
                    text = step.text.trim(),
                    order = index,
                )
            }

    fun normalizeTags(tags: List<Tag>): List<Tag> =
        tags
            .map { it.copy(name = it.name.trim()) }
            .filter { it.name.isNotBlank() }
            .distinctBy { it.name.lowercase() }

    private fun Ingredient.hasAnyInput(): Boolean = name.isNotBlank() || quantity.isNotBlank() || unit.isNotBlank()

    private fun String.isValidWebUrl(): Boolean =
        runCatching {
            val uri = URI(this)
            val scheme = uri.scheme?.lowercase()
            scheme in setOf("http", "https") && !uri.host.isNullOrBlank()
        }.getOrDefault(false)
}
