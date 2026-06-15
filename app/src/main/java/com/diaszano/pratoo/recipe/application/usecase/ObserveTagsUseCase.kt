package com.diaszano.pratoo.recipe.application.usecase

import com.diaszano.pratoo.recipe.domain.model.Tag
import com.diaszano.pratoo.recipe.domain.repository.RecipeRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveTagsUseCase @Inject constructor(private val repository: RecipeRepository) {
    operator fun invoke(): Flow<List<Tag>> = repository.observeAllTags()
}
