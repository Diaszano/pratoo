package com.diaszano.pratoo.core.error

import com.diaszano.pratoo.recipe.domain.validation.RecipeValidationError

/** Thrown when [RecipeValidator] finds one or more validation errors. */
class ValidationException(
    val errors: List<RecipeValidationError>,
) : Exception(errors.joinToString("; ") { it::class.simpleName ?: it.toString() })
