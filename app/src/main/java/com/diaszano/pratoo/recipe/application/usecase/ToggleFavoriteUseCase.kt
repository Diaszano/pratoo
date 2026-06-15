package com.diaszano.pratoo.recipe.application.usecase

import com.diaszano.pratoo.recipe.domain.repository.RecipeRepository
import javax.inject.Inject

class ToggleFavoriteUseCase @Inject constructor(private val repository: RecipeRepository) {
    suspend operator fun invoke(id: Long) = repository.toggleFavorite(id)
}
