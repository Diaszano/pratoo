package com.diaszano.pratoo.recipe.application.usecase

import com.diaszano.pratoo.recipe.domain.model.RecipeListItem
import com.diaszano.pratoo.recipe.domain.repository.RecipeRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SearchRecipesUseCase
    @Inject
    constructor(
        private val repository: RecipeRepository,
    ) {
        operator fun invoke(
            query: String? = null,
            tagId: Long? = null,
        ): Flow<List<RecipeListItem>> = repository.searchRecipes(query, tagId)
    }
