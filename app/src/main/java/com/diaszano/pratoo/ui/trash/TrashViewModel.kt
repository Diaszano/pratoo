package com.diaszano.pratoo.ui.trash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diaszano.pratoo.recipe.application.usecase.DeleteRecipePermanentlyUseCase
import com.diaszano.pratoo.recipe.application.usecase.ObserveDeletedRecipesUseCase
import com.diaszano.pratoo.recipe.application.usecase.PurgeExpiredDeletedRecipesUseCase
import com.diaszano.pratoo.recipe.application.usecase.RestoreDeletedRecipeUseCase
import com.diaszano.pratoo.recipe.domain.model.RecipeListItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TrashUiState(
    val recipes: List<RecipeListItem> = emptyList(),
    val isLoading: Boolean = false,
)

@HiltViewModel
class TrashViewModel
    @Inject
    constructor(
        observeDeletedRecipes: ObserveDeletedRecipesUseCase,
        private val deleteRecipePermanently: DeleteRecipePermanentlyUseCase,
        private val purgeExpiredDeletedRecipes: PurgeExpiredDeletedRecipesUseCase,
        private val restoreDeletedRecipe: RestoreDeletedRecipeUseCase,
    ) : ViewModel() {
        val uiState: StateFlow<TrashUiState> =
            observeDeletedRecipes()
                .map { recipes -> TrashUiState(recipes = recipes) }
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5000),
                    initialValue = TrashUiState(isLoading = true),
                )

        init {
            viewModelScope.launch {
                purgeExpiredDeletedRecipes()
            }
        }

        fun onRestoreRecipe(recipeId: Long) {
            viewModelScope.launch {
                restoreDeletedRecipe(recipeId)
            }
        }

        fun onDeleteRecipePermanently(recipeId: Long) {
            viewModelScope.launch {
                deleteRecipePermanently(recipeId)
            }
        }
    }
