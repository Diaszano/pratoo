package com.diaszano.pratoo.recipe.domain.validation

import com.diaszano.pratoo.recipe.domain.model.Recipe

data class ValidationError(val field: String, val message: String)

object RecipeValidator {

    fun validate(recipe: Recipe): List<ValidationError> {
        val errors = mutableListOf<ValidationError>()

        if (recipe.title.isBlank()) {
            errors.add(ValidationError("title", "Título é obrigatório"))
        }
        if (recipe.servings < 1) {
            errors.add(ValidationError("servings", "Porções devem ser pelo menos 1"))
        }
        if (recipe.prepTimeMinutes < 0) {
            errors.add(ValidationError("prepTimeMinutes", "Tempo de preparo não pode ser negativo"))
        }
        if (recipe.cookTimeMinutes < 0) {
            errors.add(ValidationError("cookTimeMinutes", "Tempo de cozinha não pode ser negativo"))
        }
        if (recipe.ingredients.any { it.name.isBlank() }) {
            errors.add(ValidationError("ingredients", "Ingredientes com nome vazio não são permitidos"))
        }

        return errors
    }

    fun normalizeIngredients(ingredients: List<com.diaszano.pratoo.recipe.domain.model.Ingredient>) =
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

    fun normalizeSteps(steps: List<com.diaszano.pratoo.recipe.domain.model.RecipeStep>) =
        steps
            .filter { it.text.isNotBlank() }
            .mapIndexed { index, step ->
                step.copy(
                    id = 0L,
                    text = step.text.trim(),
                    order = index
                )
            }

    fun normalizeTags(tags: List<com.diaszano.pratoo.recipe.domain.model.Tag>) =
        tags
            .map { it.copy(name = it.name.trim()) }
            .filter { it.name.isNotBlank() }
            .distinctBy { it.name.lowercase() }
}
