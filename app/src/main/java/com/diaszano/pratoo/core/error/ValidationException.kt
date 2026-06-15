package com.diaszano.pratoo.core.error

class ValidationException(val errors: List<com.diaszano.pratoo.recipe.domain.validation.ValidationError>) :
    Exception(errors.joinToString("; ") { it.message })
