package com.diaszano.pratoo.ui.recipelist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diaszano.pratoo.data.local.entity.TagEntity
import com.diaszano.pratoo.data.local.relation.RecipeListItem
import com.diaszano.pratoo.data.repository.RecipeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RecipeListUiState(
    val recipes: List<RecipeListItem> = emptyList(),
    val allTags: List<TagEntity> = emptyList(),
    val searchQuery: String = "",
    val selectedTagId: Long? = null,
    val isLoading: Boolean = false
)

@HiltViewModel
class RecipeListViewModel @Inject constructor(
    private val repository: RecipeRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _selectedTagId = MutableStateFlow<Long?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<RecipeListUiState> = combine(
        combine(_searchQuery, _selectedTagId) { query, tagId ->
            query to tagId
        }.debounce(300).flatMapLatest { (query, tagId) ->
            repository.searchRecipes(
                query = query.ifBlank { null },
                tagId = tagId
            )
        },
        repository.observeAllTags(),
        _searchQuery,
        _selectedTagId
    ) { recipes, tags, query, tagId ->
        RecipeListUiState(
            recipes = recipes,
            allTags = tags,
            searchQuery = query,
            selectedTagId = tagId,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = RecipeListUiState(isLoading = true)
    )

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onSelectTag(tagId: Long?) {
        val newId = if (_selectedTagId.value == tagId) null else tagId
        _selectedTagId.value = newId
    }

    fun onDeleteRecipe(recipeId: Long) {
        viewModelScope.launch {
            repository.deleteRecipe(recipeId)
        }
    }
}
