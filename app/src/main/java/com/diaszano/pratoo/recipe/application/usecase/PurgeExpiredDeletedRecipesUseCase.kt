package com.diaszano.pratoo.recipe.application.usecase

import com.diaszano.pratoo.recipe.domain.repository.RecipeRepository
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class PurgeExpiredDeletedRecipesUseCase
    @Inject
    constructor(
        private val repository: RecipeRepository,
    ) {
        suspend operator fun invoke(nowMillis: Long = System.currentTimeMillis()) {
            repository.deleteDeletedRecipesOlderThan(nowMillis - TRASH_RETENTION_MILLIS)
        }

        companion object {
            val TRASH_RETENTION_MILLIS: Long = TimeUnit.DAYS.toMillis(30)
        }
    }
