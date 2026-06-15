package com.diaszano.pratoo.ui.recipeedit

import android.content.Context
import android.net.Uri
import androidx.annotation.StringRes
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.diaszano.pratoo.R
import com.diaszano.pratoo.core.error.ValidationException
import com.diaszano.pratoo.recipe.application.usecase.GetRecipeUseCase
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
import com.diaszano.pratoo.recipe.domain.validation.RecipeValidationError
import com.diaszano.pratoo.recipe.domain.validation.RecipeValidator
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

data class FormItemKey(
    val sectionIndex: Int,
    val itemIndex: Int,
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
    val hasUnsavedChanges: Boolean = false,
    val showUnsavedChangesDialog: Boolean = false,
    @param:StringRes val loadErrorResId: Int? = null,
    @param:StringRes val saveErrorResId: Int? = null,
    @param:StringRes val titleErrorResId: Int? = null,
    @param:StringRes val servingsErrorResId: Int? = null,
    @param:StringRes val prepTimeErrorResId: Int? = null,
    @param:StringRes val cookTimeErrorResId: Int? = null,
    @param:StringRes val sourceUrlErrorResId: Int? = null,
    @param:StringRes val contentErrorResId: Int? = null,
    val sectionNameErrors: Map<Int, Int> = emptyMap(),
    val sectionContentErrors: Map<Int, Int> = emptyMap(),
    val ingredientNameErrors: Map<FormItemKey, Int> = emptyMap(),
    val stepTextErrors: Map<FormItemKey, Int> = emptyMap(),
)

@HiltViewModel
class RecipeEditViewModel
    @Inject
    constructor(
        savedStateHandle: SavedStateHandle,
        observeTags: ObserveTagsUseCase,
        observeMeasurementUnits: ObserveMeasurementUnitsUseCase,
        private val getRecipe: GetRecipeUseCase,
        private val saveRecipe: SaveRecipeUseCase,
        private val repository: RecipeRepository,
        @param:ApplicationContext private val context: Context,
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
                loadRecipe(recipeId)
            } else {
                _uiState.update { it.copy(isLoaded = true) }
            }
        }

        private fun loadRecipe(id: Long) {
            viewModelScope.launch {
                val recipe = getRecipe.once(id)
                if (recipe == null) {
                    _uiState.update {
                        it.copy(
                            isLoaded = true,
                            loadErrorResId = R.string.recipe_not_found,
                        )
                    }
                    return@launch
                }

                _uiState.update {
                    it.copy(
                        title = recipe.title,
                        notes = recipe.notes,
                        imageUri = recipe.imageUri,
                        servings = recipe.servings.toString(),
                        prepTimeMinutes = if (recipe.prepTimeMinutes > 0) recipe.prepTimeMinutes.toString() else "",
                        cookTimeMinutes = if (recipe.cookTimeMinutes > 0) recipe.cookTimeMinutes.toString() else "",
                        sourceUrl = recipe.sourceUrl.orEmpty(),
                        isFavorite = recipe.isFavorite,
                        sections = recipe.toFormSections(),
                        selectedTagIds = recipe.tags.map { tag -> tag.id }.toSet(),
                        isLoaded = true,
                        hasUnsavedChanges = false,
                    )
                }
            }
        }

        fun onTitleChange(title: String) {
            _uiState.update {
                it.copy(
                    title = title,
                    titleErrorResId = null,
                    saveErrorResId = null,
                    hasUnsavedChanges = true,
                )
            }
        }

        fun onNotesChange(notes: String) {
            _uiState.update { it.copy(notes = notes, saveErrorResId = null, hasUnsavedChanges = true) }
        }

        fun onServingsChange(servings: String) {
            _uiState.update {
                it.copy(
                    servings = servings.filter { char -> char.isDigit() }.take(4),
                    servingsErrorResId = null,
                    saveErrorResId = null,
                    hasUnsavedChanges = true,
                )
            }
        }

        fun onPrepTimeChange(minutes: String) {
            _uiState.update {
                it.copy(
                    prepTimeMinutes = minutes.filter { char -> char.isDigit() }.take(5),
                    prepTimeErrorResId = null,
                    saveErrorResId = null,
                    hasUnsavedChanges = true,
                )
            }
        }

        fun onCookTimeChange(minutes: String) {
            _uiState.update {
                it.copy(
                    cookTimeMinutes = minutes.filter { char -> char.isDigit() }.take(5),
                    cookTimeErrorResId = null,
                    saveErrorResId = null,
                    hasUnsavedChanges = true,
                )
            }
        }

        fun onImageSelected(uri: Uri) {
            viewModelScope.launch {
                val savedPath = copyImageToInternalStorage(uri)
                if (savedPath != null) {
                    _uiState.update {
                        it.copy(
                            imageUri = savedPath,
                            saveErrorResId = null,
                            hasUnsavedChanges = true,
                        )
                    }
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
            _uiState.update {
                it.copy(
                    sourceUrl = url,
                    sourceUrlErrorResId = null,
                    saveErrorResId = null,
                    hasUnsavedChanges = true,
                )
            }
        }

        fun onToggleFavorite() {
            _uiState.update { it.copy(isFavorite = !it.isFavorite, saveErrorResId = null, hasUnsavedChanges = true) }
        }

        fun onRequestNavigateBack() {
            if (currentState.hasUnsavedChanges) {
                _uiState.update { it.copy(showUnsavedChangesDialog = true) }
            }
        }

        fun onDismissUnsavedChangesDialog() {
            _uiState.update { it.copy(showUnsavedChangesDialog = false) }
        }

        fun onDiscardChanges() {
            _uiState.update { it.copy(showUnsavedChangesDialog = false, hasUnsavedChanges = false) }
        }

        fun onAddSection() {
            _uiState.update {
                val updatedState =
                    it.copy(
                        sections = it.sections + SectionFormItem(),
                        hasUnsavedChanges = true,
                    )
                updatedState.clearValidationErrors()
            }
        }

        fun onRemoveSection(sectionIndex: Int) {
            _uiState.update {
                val updated = it.sections.toMutableList()
                if (sectionIndex in updated.indices) updated.removeAt(sectionIndex)
                if (updated.isEmpty()) updated.add(SectionFormItem())
                val updatedState =
                    it.copy(
                        sections = updated,
                        hasUnsavedChanges = true,
                    )
                updatedState.clearValidationErrors()
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
                it.copy(
                    sections = updated,
                    sectionNameErrors = it.sectionNameErrors - sectionIndex,
                    saveErrorResId = null,
                    hasUnsavedChanges = true,
                )
            }
        }

        fun onAddIngredient(sectionIndex: Int) {
            _uiState.update {
                val updated = it.sections.toMutableList()
                if (sectionIndex in updated.indices) {
                    val section = updated[sectionIndex]
                    updated[sectionIndex] = section.copy(ingredients = section.ingredients + IngredientFormItem())
                }
                it.copy(
                    sections = updated,
                    saveErrorResId = null,
                    hasUnsavedChanges = true,
                )
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
                it.copy(
                    sections = updated,
                    contentErrorResId = null,
                    sectionContentErrors = it.sectionContentErrors - sectionIndex,
                    ingredientNameErrors = it.ingredientNameErrors - FormItemKey(sectionIndex, ingredientIndex),
                    saveErrorResId = null,
                    hasUnsavedChanges = true,
                )
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
                val updatedState =
                    it.copy(
                        sections = updated,
                        saveErrorResId = null,
                        hasUnsavedChanges = true,
                    )
                updatedState.clearValidationErrors()
            }
        }

        fun onAddStep(sectionIndex: Int) {
            _uiState.update {
                val updated = it.sections.toMutableList()
                if (sectionIndex in updated.indices) {
                    val section = updated[sectionIndex]
                    updated[sectionIndex] = section.copy(steps = section.steps + StepFormItem())
                }
                it.copy(
                    sections = updated,
                    saveErrorResId = null,
                    hasUnsavedChanges = true,
                )
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
                it.copy(
                    sections = updated,
                    contentErrorResId = null,
                    sectionContentErrors = it.sectionContentErrors - sectionIndex,
                    stepTextErrors = it.stepTextErrors - FormItemKey(sectionIndex, stepIndex),
                    saveErrorResId = null,
                    hasUnsavedChanges = true,
                )
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
                val updatedState =
                    it.copy(
                        sections = updated,
                        saveErrorResId = null,
                        hasUnsavedChanges = true,
                    )
                updatedState.clearValidationErrors()
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
                    if (stepIndex in steps.indices) {
                        val item = steps.removeAt(stepIndex)
                        steps.add(stepIndex - 1, item)
                        updated[sectionIndex] = section.copy(steps = steps)
                    }
                }
                val updatedState =
                    it.copy(
                        sections = updated,
                        saveErrorResId = null,
                        hasUnsavedChanges = true,
                    )
                updatedState.clearValidationErrors()
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
                    if (stepIndex >= 0 && stepIndex < steps.lastIndex) {
                        val item = steps.removeAt(stepIndex)
                        steps.add(stepIndex + 1, item)
                        updated[sectionIndex] = section.copy(steps = steps)
                    }
                }
                val updatedState =
                    it.copy(
                        sections = updated,
                        saveErrorResId = null,
                        hasUnsavedChanges = true,
                    )
                updatedState.clearValidationErrors()
            }
        }

        fun onToggleTag(tagId: Long) {
            _uiState.update {
                val updated = it.selectedTagIds.toMutableSet()
                if (tagId in updated) updated.remove(tagId) else updated.add(tagId)
                it.copy(
                    selectedTagIds = updated,
                    saveErrorResId = null,
                    hasUnsavedChanges = true,
                )
            }
        }

        fun onNewTagNameChange(name: String) {
            _uiState.update { it.copy(newTagName = name, saveErrorResId = null, hasUnsavedChanges = true) }
        }

        fun onAddNewTag() {
            val name = currentState.newTagName.trim()
            if (name.isBlank()) return

            viewModelScope.launch {
                val tagId = repository.createTag(name)
                _uiState.update {
                    it.copy(
                        newTagName = "",
                        selectedTagIds = if (tagId > 0L) it.selectedTagIds + tagId else it.selectedTagIds,
                        saveErrorResId = null,
                        hasUnsavedChanges = true,
                    )
                }
            }
        }

        fun onDeleteTag(tagId: Long) {
            viewModelScope.launch {
                repository.deleteTag(tagId)
                _uiState.update {
                    it.copy(
                        selectedTagIds = it.selectedTagIds - tagId,
                        saveErrorResId = null,
                        hasUnsavedChanges = true,
                    )
                }
            }
        }

        fun onSave(onSuccess: () -> Unit) {
            val state = currentState
            if (state.isSaving) return

            val recipe = state.toRecipe(recipeId)
            val errors = RecipeValidator.validate(recipe)
            if (errors.isNotEmpty()) {
                _uiState.update { it.withValidationErrors(errors) }
                return
            }

            _uiState.update {
                it.copy(
                    isSaving = true,
                    saveErrorResId = null,
                )
            }

            viewModelScope.launch {
                try {
                    saveRecipe(recipe)
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            hasUnsavedChanges = false,
                        )
                    }
                    onSuccess()
                } catch (exception: ValidationException) {
                    _uiState.update {
                        it.copy(isSaving = false).withValidationErrors(exception.errors)
                    }
                } catch (_: Exception) {
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            saveErrorResId = R.string.recipe_save_error,
                        )
                    }
                }
            }
        }

        private fun Recipe.toFormSections(): List<SectionFormItem> =
            sections
                .map { section ->
                    SectionFormItem(
                        name = section.name,
                        ingredients =
                            section.ingredients
                                .map { ingredient ->
                                    IngredientFormItem(
                                        name = ingredient.name,
                                        quantity = ingredient.quantity,
                                        unit = ingredient.unit,
                                    )
                                }.ifEmpty { listOf(IngredientFormItem()) },
                        steps =
                            section.steps
                                .sortedBy { step -> step.order }
                                .map { step -> StepFormItem(step.text) }
                                .ifEmpty { listOf(StepFormItem()) },
                    )
                }.ifEmpty { listOf(SectionFormItem()) }

        private fun RecipeEditUiState.toRecipe(recipeId: Long?): Recipe {
            val selectedTags =
                allTags
                    .filter { tag -> tag.id in selectedTagIds }
                    .map { tag -> Tag(id = tag.id, name = tag.name) }
            val inlineTag =
                newTagName
                    .trim()
                    .takeIf { it.isNotBlank() }
                    ?.let { Tag(name = it) }

            return Recipe(
                id = recipeId ?: 0L,
                title = title.trim(),
                notes = notes.trim(),
                imageUri = imageUri,
                servings = servings.toIntOrNull() ?: 0,
                prepTimeMinutes = prepTimeMinutes.toIntOrNull() ?: 0,
                cookTimeMinutes = cookTimeMinutes.toIntOrNull() ?: 0,
                sourceUrl = sourceUrl.trim().ifBlank { null },
                isFavorite = isFavorite,
                sections =
                    sections.mapIndexed { sectionIndex, section ->
                        RecipeSection(
                            name = section.name.trim(),
                            position = sectionIndex,
                            ingredients =
                                section.ingredients.mapIndexed { ingredientIndex, ingredient ->
                                    Ingredient(
                                        name = ingredient.name.trim(),
                                        quantity = ingredient.quantity.trim(),
                                        unit = ingredient.unit.trim(),
                                        position = ingredientIndex,
                                    )
                                },
                            steps =
                                section.steps.mapIndexed { stepIndex, step ->
                                    RecipeStep(
                                        text = step.text.trim(),
                                        order = stepIndex,
                                    )
                                },
                        )
                    },
                tags = RecipeValidator.normalizeTags(selectedTags + listOfNotNull(inlineTag)),
            )
        }

        private fun RecipeEditUiState.withValidationErrors(errors: List<RecipeValidationError>): RecipeEditUiState {
            var titleError: Int? = null
            var servingsError: Int? = null
            var prepTimeError: Int? = null
            var cookTimeError: Int? = null
            var sourceUrlError: Int? = null
            var contentError: Int? = null
            val sectionNameErrors = mutableMapOf<Int, Int>()
            val sectionContentErrors = mutableMapOf<Int, Int>()
            val ingredientNameErrors = mutableMapOf<FormItemKey, Int>()
            val stepTextErrors = mutableMapOf<FormItemKey, Int>()

            errors.forEach { error ->
                when (error) {
                    RecipeValidationError.EmptyTitle -> titleError = R.string.recipe_title_required
                    RecipeValidationError.InvalidServings -> servingsError = R.string.recipe_servings_invalid
                    RecipeValidationError.NegativePrepTime -> prepTimeError = R.string.recipe_prep_time_invalid
                    RecipeValidationError.NegativeCookTime -> cookTimeError = R.string.recipe_cook_time_invalid
                    RecipeValidationError.InvalidSourceUrl -> sourceUrlError = R.string.recipe_source_url_invalid
                    RecipeValidationError.EmptyContent -> contentError = R.string.recipe_content_required
                    is RecipeValidationError.EmptySectionName ->
                        sectionNameErrors[error.sectionIndex] = R.string.recipe_section_name_required
                    is RecipeValidationError.EmptySectionContent ->
                        sectionContentErrors[error.sectionIndex] = R.string.recipe_section_empty
                    is RecipeValidationError.EmptyIngredientName ->
                        ingredientNameErrors[FormItemKey(error.sectionIndex, error.ingredientIndex)] =
                            R.string.recipe_ingredient_required
                    is RecipeValidationError.EmptyStepText ->
                        stepTextErrors[FormItemKey(error.sectionIndex, error.stepIndex)] =
                            R.string.recipe_step_required
                }
            }

            return copy(
                isSaving = false,
                saveErrorResId = null,
                titleErrorResId = titleError,
                servingsErrorResId = servingsError,
                prepTimeErrorResId = prepTimeError,
                cookTimeErrorResId = cookTimeError,
                sourceUrlErrorResId = sourceUrlError,
                contentErrorResId = contentError,
                sectionNameErrors = sectionNameErrors,
                sectionContentErrors = sectionContentErrors,
                ingredientNameErrors = ingredientNameErrors,
                stepTextErrors = stepTextErrors,
            )
        }

        private fun RecipeEditUiState.clearValidationErrors(): RecipeEditUiState =
            copy(
                saveErrorResId = null,
                titleErrorResId = null,
                servingsErrorResId = null,
                prepTimeErrorResId = null,
                cookTimeErrorResId = null,
                sourceUrlErrorResId = null,
                contentErrorResId = null,
                sectionNameErrors = emptyMap(),
                sectionContentErrors = emptyMap(),
                ingredientNameErrors = emptyMap(),
                stepTextErrors = emptyMap(),
            )
    }
