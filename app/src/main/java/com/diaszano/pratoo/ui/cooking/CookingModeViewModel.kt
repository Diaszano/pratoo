package com.diaszano.pratoo.ui.cooking

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.diaszano.pratoo.recipe.application.usecase.GetRecipeUseCase
import com.diaszano.pratoo.ui.navigation.CookingModeRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CookingModeViewModel
    @Inject
    constructor(
        savedStateHandle: SavedStateHandle,
        private val getRecipe: GetRecipeUseCase,
    ) : ViewModel() {
        private val route: CookingModeRoute = savedStateHandle.toRoute<CookingModeRoute>()
        private val recipeId: Long = route.recipeId

        private val _uiState = MutableStateFlow(CookingModeUiState(recipeId = recipeId))
        val uiState: StateFlow<CookingModeUiState> = _uiState

        init {
            viewModelScope.launch {
                val recipe =
                    getRecipe.once(recipeId) ?: run {
                        _uiState.update { it.copy(isLoading = false) }
                        return@launch
                    }

                val showHeader = recipe.sections.size > 1 || recipe.sections.any { it.name.isNotBlank() }

                val sectionList =
                    recipe.sections
                        .mapIndexed { sectionIndex, section ->
                            CookingSectionUiState(
                                id = "section_$sectionIndex",
                                name = section.name,
                                showHeader = showHeader,
                                ingredients =
                                    section.ingredients.mapIndexed { ingIdx, ing ->
                                        CookingItemUiState(
                                            id = "s${sectionIndex}_i$ingIdx",
                                            name = ing.name,
                                            quantity = ing.quantity,
                                            unit = ing.unit,
                                        )
                                    },
                                steps =
                                    section.steps.sortedBy { it.order }.mapIndexed { stepIdx, step ->
                                        CookingItemUiState(
                                            id = "s${sectionIndex}_st$stepIdx",
                                            name = step.text,
                                            order = step.order,
                                        )
                                    },
                            )
                        }.ifEmpty {
                            listOf(
                                CookingSectionUiState(
                                    id = "section_0",
                                    name = "",
                                    showHeader = false,
                                    ingredients = emptyList(),
                                    steps = emptyList(),
                                ),
                            )
                        }

                _uiState.update {
                    it.copy(
                        recipeTitle = recipe.title,
                        sections = sectionList,
                        isLoading = false,
                    )
                }
            }
        }

        fun onIngredientCheckedChange(
            itemId: String,
            checked: Boolean,
        ) {
            _uiState.update { state ->
                val updated =
                    if (checked) {
                        state.completedIngredientIds + itemId
                    } else {
                        state.completedIngredientIds - itemId
                    }
                state.copy(completedIngredientIds = updated)
            }
        }

        fun onStepCheckedChange(
            itemId: String,
            checked: Boolean,
        ) {
            _uiState.update { state ->
                val updated =
                    if (checked) {
                        state.completedStepIds + itemId
                    } else {
                        state.completedStepIds - itemId
                    }
                state.copy(completedStepIds = updated)
            }
        }

        fun onFinishCooking() {
            _uiState.update { it.copy(isFinished = true) }
        }

        fun onExitCooking() {
            val state = _uiState.value
            if (state.completedIngredientIds.isNotEmpty() || state.completedStepIds.isNotEmpty()) {
                _uiState.update { it.copy(showExitDialog = true) }
            } else {
                _uiState.update { it.copy(isFinished = true) }
            }
        }

        fun onConfirmExit() {
            _uiState.update { it.copy(isFinished = true) }
        }

        fun onDismissExitDialog() {
            _uiState.update { it.copy(showExitDialog = false) }
        }
    }
