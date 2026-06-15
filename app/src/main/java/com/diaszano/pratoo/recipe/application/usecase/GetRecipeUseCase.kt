package com.diaszano.pratoo.recipe.application.usecase

import com.diaszano.pratoo.recipe.domain.model.Recipe
import com.diaszano.pratoo.recipe.domain.repository.RecipeRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetRecipeUseCase @Inject constructor(private val repository: RecipeRepository) {
    fun observe(id: Long): Flow<Recipe?> = repository.observeRecipe(id)
    suspend fun once(id: Long): Recipe? = repository.getRecipe(id)
}
