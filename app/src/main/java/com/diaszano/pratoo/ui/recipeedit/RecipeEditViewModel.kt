package com.diaszano.pratoo.ui.recipeedit

import android.content.Context
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.diaszano.pratoo.R
import com.diaszano.pratoo.recipe.application.usecase.ObserveMeasurementUnitsUseCase
import com.diaszano.pratoo.recipe.application.usecase.ObserveTagsUseCase
import com.diaszano.pratoo.recipe.application.usecase.SaveRecipeUseCase
import com.diaszano.pratoo.recipe.domain.model.Ingredient
import com.diaszano.pratoo.recipe.domain.model.MeasurementUnit
import com.diaszano.pratoo.recipe.domain.model.Recipe
import com.diaszano.pratoo.recipe.domain.model.RecipeSection
import com.diaszano.pratoo.recipe.domain.model.RecipeStep
import com.diaszano.pratoo.recipe.domain.model.Tag
import com.diaszano.pratoo.recipe.domain.repository.RecipeRepository
import com.diaszano.pratoo.ui.navigation.RecipeEditRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
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
    val unit: String = "",
)

data class StepFormItem(
    val text: String = "",
)

data class SectionFormItem(
    val name: String = "",
    val ingredients: List<IngredientFormItem> = listOf(IngredientFormItem()),
    val steps: List<StepFormItem> = listOf(StepFormItem()),
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
    val sections: List<SectionFormItem> = listOf(SectionFormItem()),
    val allTags: List<Tag> = emptyList(),
    val selectedTagIds: Set<Long> = emptySet(),
    val newTagName: String = "",
    val measurementUnits: List<MeasurementUnit> = emptyList(),
    val isSaving: Boolean = false,
    val isLoaded: Boolean = false,
    @androidx.annotation.StringRes val titleErrorResId: Int? = null,
)

@HiltViewModel
class RecipeEditViewModel
    @Inject
    constructor(
        savedStateHandle: SavedStateHandle,
        observeTags: ObserveTagsUseCase,
        observeMeasurementUnits: ObserveMeasurementUnitsUseCase,
        private val saveRecipe: SaveRecipeUseCase,
        private val repository: RecipeRepository,
        @ApplicationContext private val context: Context,
    ) : ViewModel() {
        private val route: RecipeEditRoute = savedStateHandle.toRoute<RecipeEditRoute>()
        private val recipeId: Long? = route.recipeId

        private val _uiState = MutableStateFlow(RecipeEditUiState())

        val uiState: StateFlow<RecipeEditUiState> =
            combine(
                _uiState,
                observeTags(),
                observeMeasurementUnits(),
            ) { state, tags, units ->
                state.copy(allTags = tags, measurementUnits = units)
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = RecipeEditUiState(),
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
                            sections =
                                recipe.sections
                                    .map { section ->
                                        SectionFormItem(
                                            name = section.name,
                                            ingredients =
                                                section.ingredients
                                                    .map {
                                                        IngredientFormItem(it.name, it.quantity, it.unit)
                                                    }.ifEmpty { listOf(IngredientFormItem()) },
                                            steps =
                                                section.steps
                                                    .sortedBy { it.order }
                                                    .map {
                                                        StepFormItem(it.text)
                                                    }.ifEmpty { listOf(StepFormItem()) },
                                        )
                                    }.ifEmpty { listOf(SectionFormItem()) },
                            selectedTagIds = recipe.tags.map { it.id }.toSet(),
                            isLoaded = true,
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

        private suspend fun copyImageToInternalStorage(uri: Uri): String? =
            withContext(Dispatchers.IO) {
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

        fun onSourceUrlChange(url: String) {
            _uiState.update { it.copy(sourceUrl = url) }
        }

        fun onToggleFavorite() {
            _uiState.update { it.copy(isFavorite = !it.isFavorite) }
        }

        // ── Sections ──────────────────────────────────────────────────

        fun onAddSection() {
            _uiState.update { it.copy(sections = it.sections + SectionFormItem()) }
        }

        fun onRemoveSection(sectionIndex: Int) {
            _uiState.update {
                val updated = it.sections.toMutableList()
                if (sectionIndex in updated.indices) updated.removeAt(sectionIndex)
                if (updated.isEmpty()) updated.add(SectionFormItem())
                it.copy(sections = updated)
            }
        }

        fun onSectionNameChange(
            sectionIndex: Int,
            name: String,
        ) {
            _uiState.update {
                val updated = it.sections.toMutableList()
                if (sectionIndex in updated.indices) {
                    updated[sectionIndex] = updated[sectionIndex].copy(name = name)
                }
                it.copy(sections = updated)
            }
        }

        // ── Ingredients per section ───────────────────────────────────

        fun onAddIngredient(sectionIndex: Int) {
            _uiState.update {
                val updated = it.sections.toMutableList()
                if (sectionIndex in updated.indices) {
                    val section = updated[sectionIndex]
                    updated[sectionIndex] = section.copy(ingredients = section.ingredients + IngredientFormItem())
                }
                it.copy(sections = updated)
            }
        }

        fun onIngredientChange(
            sectionIndex: Int,
            ingredientIndex: Int,
            name: String,
            quantity: String,
            unit: String,
        ) {
            _uiState.update {
                val updated = it.sections.toMutableList()
                if (sectionIndex in updated.indices) {
                    val section = updated[sectionIndex]
                    val ingredients = section.ingredients.toMutableList()
                    if (ingredientIndex in ingredients.indices) {
                        ingredients[ingredientIndex] = IngredientFormItem(name, quantity, unit)
                    }
                    updated[sectionIndex] = section.copy(ingredients = ingredients)
                }
                it.copy(sections = updated)
            }
        }

        fun onRemoveIngredient(
            sectionIndex: Int,
            ingredientIndex: Int,
        ) {
            _uiState.update {
                val updated = it.sections.toMutableList()
                if (sectionIndex in updated.indices) {
                    val section = updated[sectionIndex]
                    val ingredients = section.ingredients.toMutableList()
                    if (ingredientIndex in ingredients.indices) ingredients.removeAt(ingredientIndex)
                    if (ingredients.isEmpty()) ingredients.add(IngredientFormItem())
                    updated[sectionIndex] = section.copy(ingredients = ingredients)
                }
                it.copy(sections = updated)
            }
        }

        // ── Steps per section ─────────────────────────────────────────

        fun onAddStep(sectionIndex: Int) {
            _uiState.update {
                val updated = it.sections.toMutableList()
                if (sectionIndex in updated.indices) {
                    val section = updated[sectionIndex]
                    updated[sectionIndex] = section.copy(steps = section.steps + StepFormItem())
                }
                it.copy(sections = updated)
            }
        }

        fun onStepChange(
            sectionIndex: Int,
            stepIndex: Int,
            text: String,
        ) {
            _uiState.update {
                val updated = it.sections.toMutableList()
                if (sectionIndex in updated.indices) {
                    val section = updated[sectionIndex]
                    val steps = section.steps.toMutableList()
                    if (stepIndex in steps.indices) {
                        steps[stepIndex] = StepFormItem(text)
                    }
                    updated[sectionIndex] = section.copy(steps = steps)
                }
                it.copy(sections = updated)
            }
        }

        fun onRemoveStep(
            sectionIndex: Int,
            stepIndex: Int,
        ) {
            _uiState.update {
                val updated = it.sections.toMutableList()
                if (sectionIndex in updated.indices) {
                    val section = updated[sectionIndex]
                    val steps = section.steps.toMutableList()
                    if (stepIndex in steps.indices) steps.removeAt(stepIndex)
                    if (steps.isEmpty()) steps.add(StepFormItem())
                    updated[sectionIndex] = section.copy(steps = steps)
                }
                it.copy(sections = updated)
            }
        }

        fun onMoveStepUp(
            sectionIndex: Int,
            stepIndex: Int,
        ) {
            if (stepIndex <= 0) return
            _uiState.update {
                val updated = it.sections.toMutableList()
                if (sectionIndex in updated.indices) {
                    val section = updated[sectionIndex]
                    val steps = section.steps.toMutableList()
                    val temp = steps[stepIndex]
                    steps[stepIndex] = steps[stepIndex - 1]
                    steps[stepIndex - 1] = temp
                    updated[sectionIndex] = section.copy(steps = steps)
                }
                it.copy(sections = updated)
            }
        }

        fun onMoveStepDown(
            sectionIndex: Int,
            stepIndex: Int,
        ) {
            _uiState.update {
                val updated = it.sections.toMutableList()
                if (sectionIndex in updated.indices) {
                    val section = updated[sectionIndex]
                    val steps = section.steps.toMutableList()
                    if (stepIndex >= steps.lastIndex) return@update it.copy(sections = updated)
                    val temp = steps[stepIndex]
                    steps[stepIndex] = steps[stepIndex + 1]
                    steps[stepIndex + 1] = temp
                    updated[sectionIndex] = section.copy(steps = steps)
                }
                it.copy(sections = updated)
            }
        }

        // ── Tags ──────────────────────────────────────────────────────

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
                        selectedTagIds = it.selectedTagIds + tagId,
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

        // ── Save ──────────────────────────────────────────────────────

        fun onSave(onSuccess: () -> Unit) {
            val state = currentState
            if (state.title.isBlank()) {
                _uiState.update { it.copy(titleErrorResId = R.string.validation_empty_title) }
                return
            }

            _uiState.update { it.copy(isSaving = true) }

            val recipe =
                Recipe(
                    id = recipeId ?: 0L,
                    title = state.title.trim(),
                    notes = state.notes.trim(),
                    imageUri = state.imageUri,
                    servings = state.servings.toIntOrNull() ?: 1,
                    prepTimeMinutes = state.prepTimeMinutes.toIntOrNull() ?: 0,
                    cookTimeMinutes = state.cookTimeMinutes.toIntOrNull() ?: 0,
                    sourceUrl = state.sourceUrl.ifBlank { null },
                    isFavorite = state.isFavorite,
                    sections =
                        state.sections
                            .mapIndexed { sectionIndex, section ->
                                RecipeSection(
                                    name = section.name.trim(),
                                    position = sectionIndex,
                                    ingredients =
                                        section.ingredients
                                            .filter { it.name.isNotBlank() }
                                            .mapIndexed { idx, ing ->
                                                Ingredient(name = ing.name, quantity = ing.quantity, unit = ing.unit, position = idx)
                                            },
                                    steps =
                                        section.steps
                                            .filter { it.text.isNotBlank() }
                                            .mapIndexed { idx, step ->
                                                RecipeStep(text = step.text, order = idx)
                                            },
                                )
                            }.filter { it.ingredients.isNotEmpty() || it.steps.isNotEmpty() }
                            .ifEmpty { listOf(RecipeSection()) },
                    tags = state.selectedTagIds.map { Tag(id = it, name = "") },
                )

            viewModelScope.launch {
                saveRecipe(recipe)
                onSuccess()
            }
        }
    }
