package com.diaszano.pratoo.ui.recipedetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.diaszano.pratoo.recipe.application.usecase.DeleteRecipeUseCase
import com.diaszano.pratoo.recipe.application.usecase.GetRecipeUseCase
import com.diaszano.pratoo.recipe.application.usecase.ToggleFavoriteUseCase
import com.diaszano.pratoo.recipe.domain.model.Recipe
import com.diaszano.pratoo.ui.navigation.RecipeDetailRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RecipeDetailUiState(
    val recipe: Recipe? = null,
    val isLoading: Boolean = false,
    val isDeleted: Boolean = false,
)

@HiltViewModel
class RecipeDetailViewModel
    @Inject
    constructor(
        savedStateHandle: SavedStateHandle,
        getRecipe: GetRecipeUseCase,
        private val deleteRecipe: DeleteRecipeUseCase,
        private val toggleFavorite: ToggleFavoriteUseCase,
    ) : ViewModel() {
        private val route: RecipeDetailRoute = savedStateHandle.toRoute<RecipeDetailRoute>()
        private val recipeId: Long = route.recipeId
        private val _isDeleted = MutableStateFlow(false)

        val uiState: StateFlow<RecipeDetailUiState> =
            combine(
                getRecipe.observe(recipeId),
                _isDeleted,
            ) { recipe, isDeleted ->
                RecipeDetailUiState(recipe = recipe, isDeleted = isDeleted)
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = RecipeDetailUiState(isLoading = true),
            )

        fun onDelete() {
            viewModelScope.launch {
                deleteRecipe(recipeId)
                _isDeleted.value = true
            }
        }

        fun onToggleFavorite() {
            viewModelScope.launch {
                toggleFavorite(recipeId)
            }
        }
    }
