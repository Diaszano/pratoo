package com.diaszano.pratoo.recipe.application.usecase

import com.diaszano.pratoo.recipe.domain.model.MeasurementUnit
import com.diaszano.pratoo.recipe.domain.repository.RecipeRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveMeasurementUnitsUseCase
    @Inject
    constructor(
        private val repository: RecipeRepository,
    ) {
        operator fun invoke(): Flow<List<MeasurementUnit>> = repository.observeMeasurementUnits()
    }
