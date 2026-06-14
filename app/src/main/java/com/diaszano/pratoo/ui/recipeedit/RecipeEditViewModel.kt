package com.diaszano.pratoo.ui.recipeedit

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.diaszano.pratoo.data.local.entity.IngredientEntity
import com.diaszano.pratoo.data.local.entity.MeasurementUnit
import com.diaszano.pratoo.data.local.entity.RecipeEntity
import com.diaszano.pratoo.data.local.entity.StepEntity
import com.diaszano.pratoo.data.local.entity.TagEntity
import com.diaszano.pratoo.data.local.relation.RecipeWithDetails
import com.diaszano.pratoo.data.repository.RecipeRepository
import com.diaszano.pratoo.ui.navigation.RecipeEditRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class IngredientFormItem(
    val name: String = "",
    val quantity: String = "",
    val unit: String = ""
)

data class StepFormItem(
    val text: String = ""
)

data class RecipeEditUiState(
    val title: String = "",
    val notes: String = "",
    val imageUri: String? = null,
    val servings: String = "1",
    val prepTimeMinutes: String = "",
    val cookTimeMinutes: String = "",
    val sourceUrl: String = "",
    val isFavorite: Boolean = false,
    val ingredients: List<IngredientFormItem> = listOf(IngredientFormItem()),
    val steps: List<StepFormItem> = listOf(StepFormItem()),
    val allTags: List<TagEntity> = emptyList(),
    val selectedTagIds: Set<Long> = emptySet(),
    val newTagName: String = "",
    val measurementUnits: List<MeasurementUnit> = emptyList(),
    val isSaving: Boolean = false,
    val isLoaded: Boolean = false,
    val titleError: String? = null
)

@HiltViewModel
class RecipeEditViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: RecipeRepository
) : ViewModel() {

    private val route: RecipeEditRoute = savedStateHandle.toRoute<RecipeEditRoute>()
    private val recipeId: Long? = route.recipeId

    private val _uiState = MutableStateFlow(RecipeEditUiState())

    val uiState: StateFlow<RecipeEditUiState> = combine(
        _uiState,
        repository.observeAllTags(),
        repository.observeMeasurementUnits()
    ) { state, tags, units ->
        state.copy(allTags = tags, measurementUnits = units)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = RecipeEditUiState()
    )

    private val currentState: RecipeEditUiState get() = _uiState.value

    init {
        if (recipeId != null) {
            viewModelScope.launch {
                val recipe = repository.getRecipe(recipeId) ?: return@launch
                _uiState.update {
                    it.copy(
                        title = recipe.recipe.title,
                        notes = recipe.recipe.notes,
                        imageUri = recipe.recipe.imageUri,
                        servings = recipe.recipe.servings.toString(),
                        prepTimeMinutes = if (recipe.recipe.prepTimeMinutes > 0) recipe.recipe.prepTimeMinutes.toString() else "",
                        cookTimeMinutes = if (recipe.recipe.cookTimeMinutes > 0) recipe.recipe.cookTimeMinutes.toString() else "",
                        sourceUrl = recipe.recipe.sourceUrl ?: "",
                        isFavorite = recipe.recipe.isFavorite,
                        ingredients = if (recipe.ingredients.isEmpty()) {
                            listOf(IngredientFormItem())
                        } else {
                            recipe.ingredients.map { IngredientFormItem(it.name, it.quantity, it.unit) }
                        },
                        steps = if (recipe.steps.isEmpty()) {
                            listOf(StepFormItem())
                        } else {
                            recipe.steps.sortedBy { it.order }.map { StepFormItem(it.text) }
                        },
                        selectedTagIds = recipe.tags.map { it.id }.toSet(),
                        isLoaded = true
                    )
                }
            }
        } else {
            _uiState.update { it.copy(isLoaded = true) }
        }
    }

    fun onTitleChange(title: String) {
        _uiState.update { it.copy(title = title, titleError = null) }
    }

    fun onNotesChange(notes: String) {
        _uiState.update { it.copy(notes = notes) }
    }

    fun onServingsChange(servings: String) {
        _uiState.update { it.copy(servings = servings.filter { c -> c.isDigit() }.take(4)) }
    }

    fun onPrepTimeChange(minutes: String) {
        _uiState.update { it.copy(prepTimeMinutes = minutes.filter { c -> c.isDigit() }.take(5)) }
    }

    fun onCookTimeChange(minutes: String) {
        _uiState.update { it.copy(cookTimeMinutes = minutes.filter { c -> c.isDigit() }.take(5)) }
    }

    fun onImageSelected(uri: Uri) {
        _uiState.update { it.copy(imageUri = uri.toString()) }
    }

    fun onSourceUrlChange(url: String) {
        _uiState.update { it.copy(sourceUrl = url) }
    }

    fun onToggleFavorite() {
        _uiState.update { it.copy(isFavorite = !it.isFavorite) }
    }

    // Ingredients
    fun onAddIngredient() {
        _uiState.update { it.copy(ingredients = it.ingredients + IngredientFormItem()) }
    }

    fun onIngredientChange(index: Int, name: String, quantity: String, unit: String) {
        _uiState.update {
            val updated = it.ingredients.toMutableList()
            if (index in updated.indices) {
                updated[index] = IngredientFormItem(name, quantity, unit)
            }
            it.copy(ingredients = updated)
        }
    }

    fun onRemoveIngredient(index: Int) {
        _uiState.update {
            val updated = it.ingredients.toMutableList()
            if (index in updated.indices) updated.removeAt(index)
            if (updated.isEmpty()) updated.add(IngredientFormItem())
            it.copy(ingredients = updated)
        }
    }

    // Steps
    fun onAddStep() {
        _uiState.update { it.copy(steps = it.steps + StepFormItem()) }
    }

    fun onStepChange(index: Int, text: String) {
        _uiState.update {
            val updated = it.steps.toMutableList()
            if (index in updated.indices) {
                updated[index] = StepFormItem(text)
            }
            it.copy(steps = updated)
        }
    }

    fun onRemoveStep(index: Int) {
        _uiState.update {
            val updated = it.steps.toMutableList()
            if (index in updated.indices) updated.removeAt(index)
            if (updated.isEmpty()) updated.add(StepFormItem())
            it.copy(steps = updated)
        }
    }

    fun onMoveStepUp(index: Int) {
        if (index <= 0) return
        _uiState.update {
            val updated = it.steps.toMutableList()
            val temp = updated[index]
            updated[index] = updated[index - 1]
            updated[index - 1] = temp
            it.copy(steps = updated)
        }
    }

    fun onMoveStepDown(index: Int) {
        _uiState.update {
            val updated = it.steps.toMutableList()
            if (index >= updated.lastIndex) return@update it.copy(steps = updated)
            val temp = updated[index]
            updated[index] = updated[index + 1]
            updated[index + 1] = temp
            it.copy(steps = updated)
        }
    }

    // Tags
    fun onToggleTag(tagId: Long) {
        _uiState.update {
            val updated = it.selectedTagIds.toMutableSet()
            if (tagId in updated) updated.remove(tagId) else updated.add(tagId)
            it.copy(selectedTagIds = updated)
        }
    }

    fun onNewTagNameChange(name: String) {
        _uiState.update { it.copy(newTagName = name) }
    }

    fun onAddNewTag() {
        val name = currentState.newTagName.trim()
        if (name.isBlank()) return

        viewModelScope.launch {
            val tagId = repository.createTag(name)
            _uiState.update {
                it.copy(
                    newTagName = "",
                    selectedTagIds = it.selectedTagIds + tagId
                )
            }
        }
    }

    fun onDeleteTag(tagId: Long) {
        viewModelScope.launch {
            repository.deleteTag(tagId)
            _uiState.update {
                it.copy(selectedTagIds = it.selectedTagIds - tagId)
            }
        }
    }

    // Save
    fun onSave(onSuccess: () -> Unit) {
        val state = currentState
        if (state.title.isBlank()) {
            _uiState.update { it.copy(titleError = "Título é obrigatório") }
            return
        }

        _uiState.update { it.copy(isSaving = true) }

        val recipe = RecipeEntity(
            id = recipeId ?: 0L,
            title = state.title.trim(),
            notes = state.notes.trim(),
            imageUri = state.imageUri,
            servings = state.servings.toIntOrNull() ?: 1,
            prepTimeMinutes = state.prepTimeMinutes.toIntOrNull() ?: 0,
            cookTimeMinutes = state.cookTimeMinutes.toIntOrNull() ?: 0,
            sourceUrl = state.sourceUrl.ifBlank { null },
            isFavorite = state.isFavorite,
            createdAt = if (recipeId == null) System.currentTimeMillis() else 0L,
            updatedAt = System.currentTimeMillis()
        )

        val ingredients = state.ingredients
            .filter { it.name.isNotBlank() }
            .map { IngredientEntity(recipeId = recipeId ?: 0L, name = it.name, quantity = it.quantity, unit = it.unit) }

        val steps = state.steps
            .filter { it.text.isNotBlank() }
            .map { StepEntity(recipeId = recipeId ?: 0L, text = it.text) }

        val tags = state.selectedTagIds.map { TagEntity(id = it, name = "") }

        viewModelScope.launch {
            repository.saveRecipe(
                RecipeWithDetails(recipe, ingredients, steps, emptyList()),
                tags
            )
            onSuccess()
        }
    }
}
