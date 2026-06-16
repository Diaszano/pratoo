package com.diaszano.pratoo.ui.recipelist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diaszano.pratoo.recipe.application.usecase.DeleteRecipeUseCase
import com.diaszano.pratoo.recipe.application.usecase.ObserveTagsUseCase
import com.diaszano.pratoo.recipe.application.usecase.PurgeExpiredDeletedRecipesUseCase
import com.diaszano.pratoo.recipe.application.usecase.SearchRecipesUseCase
import com.diaszano.pratoo.recipe.application.usecase.ToggleFavoriteUseCase
import com.diaszano.pratoo.recipe.domain.model.RecipeListItem
import com.diaszano.pratoo.recipe.domain.model.Tag
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RecipeListUiState(
    val recipes: List<RecipeListItem> = emptyList(),
    val allTags: List<Tag> = emptyList(),
    val searchQuery: String = "",
    val selectedTagId: Long? = null,
    val isLoading: Boolean = false,
)

@HiltViewModel
class RecipeListViewModel
    @Inject
    constructor(
        searchRecipes: SearchRecipesUseCase,
        observeTags: ObserveTagsUseCase,
        private val deleteRecipe: DeleteRecipeUseCase,
        private val purgeExpiredDeletedRecipes: PurgeExpiredDeletedRecipesUseCase,
        private val toggleFavorite: ToggleFavoriteUseCase,
    ) : ViewModel() {
        private val _searchQuery = MutableStateFlow("")
        private val _selectedTagId = MutableStateFlow<Long?>(null)

        @OptIn(ExperimentalCoroutinesApi::class)
        val uiState: StateFlow<RecipeListUiState> =
            combine(
                combine(_searchQuery, _selectedTagId) { query, tagId ->
                    query to tagId
                }.debounce(300).flatMapLatest { (query, tagId) ->
                    searchRecipes(
                        query = query.ifBlank { null },
                        tagId = tagId,
                    )
                },
                observeTags(),
                _searchQuery,
                _selectedTagId,
            ) { recipes, tags, query, tagId ->
                RecipeListUiState(
                    recipes = recipes,
                    allTags = tags,
                    searchQuery = query,
                    selectedTagId = tagId,
                    isLoading = false,
                )
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = RecipeListUiState(isLoading = true),
            )

        init {
            viewModelScope.launch {
                purgeExpiredDeletedRecipes()
            }
        }

        fun onSearchQueryChange(query: String) {
            _searchQuery.value = query
        }

        fun onSelectTag(tagId: Long?) {
            val newId = if (_selectedTagId.value == tagId) null else tagId
            _selectedTagId.value = newId
        }

        fun onDeleteRecipe(recipeId: Long) {
            viewModelScope.launch {
                deleteRecipe(recipeId)
            }
        }

        fun onToggleFavorite(recipeId: Long) {
            viewModelScope.launch {
                toggleFavorite(recipeId)
            }
        }

        fun clearFilters() {
            _searchQuery.value = ""
            _selectedTagId.value = null
        }
    }
