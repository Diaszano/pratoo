package com.diaszano.pratoo.ui.recipeedit

import android.content.Context
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.diaszano.pratoo.recipe.application.usecase.ObserveMeasurementUnitsUseCase
import com.diaszano.pratoo.recipe.application.usecase.ObserveTagsUseCase
import com.diaszano.pratoo.recipe.application.usecase.SaveRecipeUseCase
import com.diaszano.pratoo.R
import com.diaszano.pratoo.recipe.domain.model.Ingredient
import com.diaszano.pratoo.recipe.domain.model.MeasurementUnit
import com.diaszano.pratoo.recipe.domain.model.Recipe
import com.diaszano.pratoo.recipe.domain.model.RecipeStep
import com.diaszano.pratoo.recipe.domain.model.Tag
import com.diaszano.pratoo.recipe.domain.repository.RecipeRepository
import com.diaszano.pratoo.ui.navigation.RecipeEditRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID
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
    val allTags: List<Tag> = emptyList(),
    val selectedTagIds: Set<Long> = emptySet(),
    val newTagName: String = "",
    val measurementUnits: List<MeasurementUnit> = emptyList(),
    val isSaving: Boolean = false,
    val isLoaded: Boolean = false,
    @androidx.annotation.StringRes val titleErrorResId: Int? = null
)

@HiltViewModel
class RecipeEditViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    observeTags: ObserveTagsUseCase,
    observeMeasurementUnits: ObserveMeasurementUnitsUseCase,
    private val saveRecipe: SaveRecipeUseCase,
    private val repository: RecipeRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val route: RecipeEditRoute = savedStateHandle.toRoute<RecipeEditRoute>()
    private val recipeId: Long? = route.recipeId

    private val _uiState = MutableStateFlow(RecipeEditUiState())

    val uiState: StateFlow<RecipeEditUiState> = combine(
        _uiState,
        observeTags(),
        observeMeasurementUnits()
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
                        title = recipe.title,
                        notes = recipe.notes,
                        imageUri = recipe.imageUri,
                        servings = recipe.servings.toString(),
                        prepTimeMinutes = if (recipe.prepTimeMinutes > 0) recipe.prepTimeMinutes.toString() else "",
                        cookTimeMinutes = if (recipe.cookTimeMinutes > 0) recipe.cookTimeMinutes.toString() else "",
                        sourceUrl = recipe.sourceUrl ?: "",
                        isFavorite = recipe.isFavorite,
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
        _uiState.update { it.copy(title = title, titleErrorResId = null) }
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
        viewModelScope.launch {
            val savedPath = copyImageToInternalStorage(uri)
            if (savedPath != null) {
                _uiState.update { it.copy(imageUri = savedPath) }
            }
        }
    }

    private suspend fun copyImageToInternalStorage(uri: Uri): String? {
        return withContext(Dispatchers.IO) {
            try {
                val imagesDir = File(context.filesDir, "recipe_images")
                if (!imagesDir.exists()) imagesDir.mkdirs()

                val fileName = "recipe_${UUID.randomUUID()}.jpg"
                val destFile = File(imagesDir, fileName)

                context.contentResolver.openInputStream(uri)?.use { input ->
                    destFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }

                destFile.absolutePath
            } catch (_: Exception) {
                null
            }
        }
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
            _uiState.update { it.copy(titleErrorResId = R.string.validation_empty_title) }
            return
        }

        _uiState.update { it.copy(isSaving = true) }

        val recipe = Recipe(
            id = recipeId ?: 0L,
            title = state.title.trim(),
            notes = state.notes.trim(),
            imageUri = state.imageUri,
            servings = state.servings.toIntOrNull() ?: 1,
            prepTimeMinutes = state.prepTimeMinutes.toIntOrNull() ?: 0,
            cookTimeMinutes = state.cookTimeMinutes.toIntOrNull() ?: 0,
            sourceUrl = state.sourceUrl.ifBlank { null },
            isFavorite = state.isFavorite,
            ingredients = state.ingredients
                .filter { it.name.isNotBlank() }
                .mapIndexed { index, ing ->
                    Ingredient(name = ing.name, quantity = ing.quantity, unit = ing.unit, position = index)
                },
            steps = state.steps
                .filter { it.text.isNotBlank() }
                .mapIndexed { index, step ->
                    RecipeStep(text = step.text, order = index)
                },
            tags = state.selectedTagIds.map { Tag(id = it, name = "") }
        )

        viewModelScope.launch {
            saveRecipe(recipe)
            onSuccess()
        }
    }
}
