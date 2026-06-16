package com.diaszano.pratoo.ui.recipeedit

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.diaszano.pratoo.R
import com.diaszano.pratoo.recipe.domain.model.MeasurementUnit
import com.diaszano.pratoo.recipe.domain.model.Tag
import com.diaszano.pratoo.ui.shared.ErrorState
import com.diaszano.pratoo.ui.shared.LoadingState
import com.diaszano.pratoo.ui.theme.LocalAppColors
import com.diaszano.pratoo.ui.theme.Spacing

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun RecipeEditScreen(
    onNavigateBack: () -> Unit,
    onRecipeSaved: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RecipeEditViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val loadErrorResId = uiState.loadErrorResId

    val photoPickerLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.PickVisualMedia(),
        ) { uri: Uri? ->
            uri?.let { viewModel.onImageSelected(it) }
        }

    fun requestBack() {
        if (uiState.hasUnsavedChanges) {
            viewModel.onRequestNavigateBack()
        } else {
            onNavigateBack()
        }
    }

    BackHandler(enabled = uiState.hasUnsavedChanges) {
        viewModel.onRequestNavigateBack()
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (uiState.isLoaded && uiState.title.isNotBlank()) {
                            uiState.title
                        } else {
                            stringResource(R.string.add_recipe)
                        },
                        maxLines = 1,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { requestBack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.cancel))
                    }
                },
            )
        },
        bottomBar = {
            Button(
                onClick = { viewModel.onSave(onRecipeSaved) },
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(Spacing.lg),
                enabled = uiState.isLoaded && uiState.loadErrorResId == null && !uiState.isSaving,
            ) {
                if (uiState.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                    Spacer(Modifier.width(Spacing.sm))
                } else {
                    Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(Spacing.sm))
                }
                Text(
                    if (uiState.isSaving) {
                        stringResource(R.string.saving_recipe)
                    } else {
                        stringResource(R.string.save_recipe)
                    },
                )
            }
        },
    ) { padding ->
        when {
            !uiState.isLoaded -> {
                LoadingState(
                    message = stringResource(R.string.loading_recipe),
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(padding),
                )
            }
            loadErrorResId != null -> {
                ErrorState(
                    message = stringResource(loadErrorResId),
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(padding),
                )
            }
            else -> {
                LazyColumn(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(padding),
                    contentPadding =
                        PaddingValues(
                            start = Spacing.lg,
                            top = Spacing.lg,
                            end = Spacing.lg,
                            bottom = 104.dp,
                        ),
                    verticalArrangement = Arrangement.spacedBy(Spacing.lg),
                ) {
                    item {
                        RecipeImagePicker(
                            imageUri = uiState.imageUri,
                            onPickImage = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                                )
                            },
                        )
                    }

                    item {
                        FavoriteChip(
                            selected = uiState.isFavorite,
                            onClick = viewModel::onToggleFavorite,
                        )
                    }

                    item {
                        BasicInfoFields(
                            uiState = uiState,
                            onTitleChange = viewModel::onTitleChange,
                            onServingsChange = viewModel::onServingsChange,
                            onPrepTimeChange = viewModel::onPrepTimeChange,
                            onCookTimeChange = viewModel::onCookTimeChange,
                        )
                    }

                    item {
                        HorizontalDivider()
                    }

                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                            Text(
                                text = stringResource(R.string.recipe_content),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                            )
                            uiState.contentErrorResId?.let { errorResId ->
                                FieldErrorText(errorResId)
                            }
                        }
                    }

                    itemsIndexed(
                        items = uiState.sections,
                        key = { index, _ -> "section_$index" },
                    ) { sectionIndex, section ->
                        RecipeSectionCard(
                            sectionIndex = sectionIndex,
                            section = section,
                            showSectionHeader =
                                uiState.sections.size > 1 ||
                                    section.name.isNotBlank() ||
                                    sectionIndex > 0,
                            canRemoveSection = uiState.sections.size > 1,
                            measurementUnits = uiState.measurementUnits,
                            sectionNameErrorResId = uiState.sectionNameErrors[sectionIndex],
                            sectionContentErrorResId = uiState.sectionContentErrors[sectionIndex],
                            ingredientNameErrors = uiState.ingredientNameErrors,
                            stepTextErrors = uiState.stepTextErrors,
                            onSectionNameChange = { viewModel.onSectionNameChange(sectionIndex, it) },
                            onRemoveSection = { viewModel.onRemoveSection(sectionIndex) },
                            onIngredientNameChange = { ingredientIndex, name ->
                                val ingredient = section.ingredients[ingredientIndex]
                                viewModel.onIngredientChange(
                                    sectionIndex,
                                    ingredientIndex,
                                    name,
                                    ingredient.quantity,
                                    ingredient.unit,
                                )
                            },
                            onIngredientQuantityChange = { ingredientIndex, quantity ->
                                val ingredient = section.ingredients[ingredientIndex]
                                viewModel.onIngredientChange(
                                    sectionIndex,
                                    ingredientIndex,
                                    ingredient.name,
                                    quantity,
                                    ingredient.unit,
                                )
                            },
                            onIngredientUnitChange = { ingredientIndex, unit ->
                                val ingredient = section.ingredients[ingredientIndex]
                                viewModel.onIngredientChange(
                                    sectionIndex,
                                    ingredientIndex,
                                    ingredient.name,
                                    ingredient.quantity,
                                    unit,
                                )
                            },
                            onRemoveIngredient = { viewModel.onRemoveIngredient(sectionIndex, it) },
                            onAddIngredient = { viewModel.onAddIngredient(sectionIndex) },
                            onStepChange = { stepIndex, text -> viewModel.onStepChange(sectionIndex, stepIndex, text) },
                            onRemoveStep = { viewModel.onRemoveStep(sectionIndex, it) },
                            onMoveStepUp = { viewModel.onMoveStepUp(sectionIndex, it) },
                            onMoveStepDown = { viewModel.onMoveStepDown(sectionIndex, it) },
                            onAddStep = { viewModel.onAddStep(sectionIndex) },
                        )
                    }

                    item {
                        OutlinedButton(
                            onClick = viewModel::onAddSection,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(Spacing.sm))
                            Text(stringResource(R.string.add_recipe_section))
                        }
                    }

                    item {
                        TagsSection(
                            uiState = uiState,
                            onToggleTag = viewModel::onToggleTag,
                            onNewTagNameChange = viewModel::onNewTagNameChange,
                            onAddNewTag = viewModel::onAddNewTag,
                            onDeleteTag = viewModel::onDeleteTag,
                        )
                    }

                    item {
                        NotesAndSourceFields(
                            notes = uiState.notes,
                            sourceUrl = uiState.sourceUrl,
                            sourceUrlErrorResId = uiState.sourceUrlErrorResId,
                            onNotesChange = viewModel::onNotesChange,
                            onSourceUrlChange = viewModel::onSourceUrlChange,
                        )
                    }

                    uiState.saveErrorResId?.let { errorResId ->
                        item {
                            FieldErrorText(errorResId)
                        }
                    }
                }
            }
        }
    }

    if (uiState.showUnsavedChangesDialog) {
        UnsavedChangesDialog(
            onDiscard = {
                viewModel.onDiscardChanges()
                onNavigateBack()
            },
            onKeepEditing = viewModel::onDismissUnsavedChangesDialog,
        )
    }
}

@Composable
private fun RecipeImagePicker(
    imageUri: String?,
    onPickImage: () -> Unit,
) {
    val description =
        if (imageUri == null) {
            stringResource(R.string.add_photo)
        } else {
            stringResource(R.string.change_photo)
        }

    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .clip(MaterialTheme.shapes.medium)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .clickable(onClick = onPickImage),
        contentAlignment = Alignment.Center,
    ) {
        if (imageUri != null) {
            AsyncImage(
                model = imageUri,
                contentDescription = stringResource(R.string.photo),
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(Spacing.sm),
            ) {
                Icon(
                    Icons.Default.AddAPhoto,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(40.dp),
                )
                Text(
                    text = stringResource(R.string.recipe_photo_placeholder),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Text(
            text = description,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier =
                Modifier
                    .align(Alignment.BottomEnd)
                    .padding(Spacing.md),
        )
    }
}

@Composable
private fun FavoriteChip(
    selected: Boolean,
    onClick: () -> Unit,
) {
    val appColors = LocalAppColors.current

    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Text(
                if (selected) {
                    stringResource(R.string.unfavorite)
                } else {
                    stringResource(R.string.favorite)
                },
            )
        },
        leadingIcon = {
            Icon(
                Icons.Filled.Star,
                contentDescription = null,
                tint = if (selected) appColors.favorite else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp),
            )
        },
    )
}

@Composable
private fun BasicInfoFields(
    uiState: RecipeEditUiState,
    onTitleChange: (String) -> Unit,
    onServingsChange: (String) -> Unit,
    onPrepTimeChange: (String) -> Unit,
    onCookTimeChange: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
        Text(
            text = stringResource(R.string.recipe_basic_info),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        OutlinedTextField(
            value = uiState.title,
            onValueChange = onTitleChange,
            label = { Text(stringResource(R.string.title_required)) },
            isError = uiState.titleErrorResId != null,
            supportingText = uiState.titleErrorResId?.let { errorResId -> { Text(stringResource(errorResId)) } },
            singleLine = true,
            keyboardOptions =
                KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next,
                ),
            modifier = Modifier.fillMaxWidth(),
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            OutlinedTextField(
                value = uiState.servings,
                onValueChange = onServingsChange,
                label = { Text(stringResource(R.string.servings)) },
                isError = uiState.servingsErrorResId != null,
                supportingText = uiState.servingsErrorResId?.let { errorResId -> { Text(stringResource(errorResId)) } },
                singleLine = true,
                keyboardOptions =
                    KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next,
                    ),
                modifier = Modifier.weight(1f),
            )
            OutlinedTextField(
                value = uiState.prepTimeMinutes,
                onValueChange = onPrepTimeChange,
                label = { Text("${stringResource(R.string.prep_time)} (${stringResource(R.string.minutes_short)})") },
                isError = uiState.prepTimeErrorResId != null,
                supportingText = uiState.prepTimeErrorResId?.let { errorResId -> { Text(stringResource(errorResId)) } },
                singleLine = true,
                keyboardOptions =
                    KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next,
                    ),
                modifier = Modifier.weight(1f),
            )
        }
        OutlinedTextField(
            value = uiState.cookTimeMinutes,
            onValueChange = onCookTimeChange,
            label = { Text("${stringResource(R.string.cook_time)} (${stringResource(R.string.minutes_short)})") },
            isError = uiState.cookTimeErrorResId != null,
            supportingText = uiState.cookTimeErrorResId?.let { errorResId -> { Text(stringResource(errorResId)) } },
            singleLine = true,
            keyboardOptions =
                KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next,
                ),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecipeSectionCard(
    sectionIndex: Int,
    section: SectionFormItem,
    showSectionHeader: Boolean,
    canRemoveSection: Boolean,
    measurementUnits: List<MeasurementUnit>,
    @StringRes sectionNameErrorResId: Int?,
    @StringRes sectionContentErrorResId: Int?,
    ingredientNameErrors: Map<FormItemKey, Int>,
    stepTextErrors: Map<FormItemKey, Int>,
    onSectionNameChange: (String) -> Unit,
    onRemoveSection: () -> Unit,
    onIngredientNameChange: (Int, String) -> Unit,
    onIngredientQuantityChange: (Int, String) -> Unit,
    onIngredientUnitChange: (Int, String) -> Unit,
    onRemoveIngredient: (Int) -> Unit,
    onAddIngredient: () -> Unit,
    onStepChange: (Int, String) -> Unit,
    onRemoveStep: (Int) -> Unit,
    onMoveStepUp: (Int) -> Unit,
    onMoveStepDown: (Int) -> Unit,
    onAddStep: () -> Unit,
) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.md),
        ) {
            if (showSectionHeader) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                ) {
                    OutlinedTextField(
                        value = section.name,
                        onValueChange = onSectionNameChange,
                        label = { Text(stringResource(R.string.recipe_section_name)) },
                        placeholder = { Text(stringResource(R.string.recipe_section_name_hint)) },
                        isError = sectionNameErrorResId != null,
                        supportingText = sectionNameErrorResId?.let { errorResId -> { Text(stringResource(errorResId)) } },
                        singleLine = true,
                        keyboardOptions =
                            KeyboardOptions(
                                keyboardType = KeyboardType.Text,
                                imeAction = ImeAction.Next,
                            ),
                        modifier = Modifier.weight(1f),
                    )
                    if (canRemoveSection) {
                        IconButton(onClick = onRemoveSection) {
                            Icon(Icons.Default.Close, stringResource(R.string.remove_recipe_section))
                        }
                    }
                }
            }

            sectionContentErrorResId?.let { errorResId ->
                FieldErrorText(errorResId)
            }

            Text(
                text =
                    if (showSectionHeader) {
                        stringResource(R.string.section_ingredients)
                    } else {
                        stringResource(R.string.ingredients_label)
                    },
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )

            section.ingredients.forEachIndexed { ingredientIndex, ingredient ->
                IngredientInputRow(
                    ingredient = ingredient,
                    units = measurementUnits,
                    nameErrorResId = ingredientNameErrors[FormItemKey(sectionIndex, ingredientIndex)],
                    onNameChange = { onIngredientNameChange(ingredientIndex, it) },
                    onQuantityChange = { onIngredientQuantityChange(ingredientIndex, it) },
                    onUnitChange = { onIngredientUnitChange(ingredientIndex, it) },
                    onRemove = { onRemoveIngredient(ingredientIndex) },
                )
            }

            OutlinedButton(
                onClick = onAddIngredient,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(Spacing.sm))
                Text(stringResource(R.string.add_ingredient))
            }

            HorizontalDivider()

            Text(
                text =
                    if (showSectionHeader) {
                        stringResource(R.string.section_steps)
                    } else {
                        stringResource(R.string.steps_label)
                    },
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )

            section.steps.forEachIndexed { stepIndex, step ->
                StepInputRow(
                    index = stepIndex,
                    step = step,
                    errorResId = stepTextErrors[FormItemKey(sectionIndex, stepIndex)],
                    isFirst = stepIndex == 0,
                    isLast = stepIndex == section.steps.lastIndex,
                    onTextChange = { onStepChange(stepIndex, it) },
                    onMoveUp = { onMoveStepUp(stepIndex) },
                    onMoveDown = { onMoveStepDown(stepIndex) },
                    onRemove = { onRemoveStep(stepIndex) },
                )
            }

            OutlinedButton(
                onClick = onAddStep,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(Spacing.sm))
                Text(stringResource(R.string.add_step))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun IngredientInputRow(
    ingredient: IngredientFormItem,
    units: List<MeasurementUnit>,
    @StringRes nameErrorResId: Int?,
    onNameChange: (String) -> Unit,
    onQuantityChange: (String) -> Unit,
    onUnitChange: (String) -> Unit,
    onRemove: () -> Unit,
) {
    var unitExpanded by remember { mutableStateOf(false) }
    val groupedUnits = remember(units) { units.groupBy { it.category } }

    Column(verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            OutlinedTextField(
                value = ingredient.name,
                onValueChange = onNameChange,
                label = { Text(stringResource(R.string.name_label)) },
                isError = nameErrorResId != null,
                supportingText = nameErrorResId?.let { errorResId -> { Text(stringResource(errorResId)) } },
                singleLine = true,
                keyboardOptions =
                    KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next,
                    ),
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = onRemove) {
                Icon(Icons.Default.Close, stringResource(R.string.remove))
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            OutlinedTextField(
                value = ingredient.quantity,
                onValueChange = onQuantityChange,
                label = { Text(stringResource(R.string.quantity_label)) },
                singleLine = true,
                keyboardOptions =
                    KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next,
                    ),
                modifier = Modifier.weight(1f),
            )
            ExposedDropdownMenuBox(
                expanded = unitExpanded,
                onExpandedChange = { unitExpanded = !unitExpanded },
                modifier = Modifier.weight(1f),
            ) {
                OutlinedTextField(
                    value = ingredient.unit,
                    onValueChange = onUnitChange,
                    label = { Text(stringResource(R.string.unit_label)) },
                    singleLine = true,
                    readOnly = true,
                    trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) },
                    modifier = Modifier.menuAnchor(),
                )
                ExposedDropdownMenu(
                    expanded = unitExpanded,
                    onDismissRequest = { unitExpanded = false },
                ) {
                    groupedUnits.forEach { (category, categoryUnits) ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    category.displayName,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                            },
                            onClick = {},
                            enabled = false,
                        )
                        categoryUnits.forEach { unit ->
                            DropdownMenuItem(
                                text = { Text(unit.displayName) },
                                onClick = {
                                    onUnitChange(unit.abbreviation)
                                    unitExpanded = false
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StepInputRow(
    index: Int,
    step: StepFormItem,
    @StringRes errorResId: Int?,
    isFirst: Boolean,
    isLast: Boolean,
    onTextChange: (String) -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onRemove: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        Text(
            text = "${index + 1}.",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = 18.dp),
        )
        OutlinedTextField(
            value = step.text,
            onValueChange = onTextChange,
            label = { Text("${stringResource(R.string.step_label)} ${index + 1}") },
            isError = errorResId != null,
            supportingText = errorResId?.let { resId -> { Text(stringResource(resId)) } },
            minLines = 2,
            keyboardOptions =
                KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next,
                ),
            modifier = Modifier.weight(1f),
        )
        Column {
            IconButton(
                onClick = onMoveUp,
                enabled = !isFirst,
            ) {
                Icon(Icons.Default.ArrowUpward, stringResource(R.string.move_up), modifier = Modifier.size(18.dp))
            }
            IconButton(
                onClick = onMoveDown,
                enabled = !isLast,
            ) {
                Icon(Icons.Default.ArrowDownward, stringResource(R.string.move_down), modifier = Modifier.size(18.dp))
            }
            IconButton(onClick = onRemove) {
                Icon(Icons.Default.Close, stringResource(R.string.remove), modifier = Modifier.size(18.dp))
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TagsSection(
    uiState: RecipeEditUiState,
    onToggleTag: (Long) -> Unit,
    onNewTagNameChange: (String) -> Unit,
    onAddNewTag: () -> Unit,
    onDeleteTag: (Long) -> Unit,
) {
    var tagPendingDelete by remember { mutableStateOf<Tag?>(null) }

    Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        Text(
            text = stringResource(R.string.tags_label),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )

        if (uiState.allTags.isNotEmpty()) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                verticalArrangement = Arrangement.spacedBy(Spacing.xs),
                modifier = Modifier.fillMaxWidth(),
            ) {
                uiState.allTags.forEach { tag ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
                    ) {
                        FilterChip(
                            selected = tag.id in uiState.selectedTagIds,
                            onClick = { onToggleTag(tag.id) },
                            label = { Text(tag.name) },
                        )
                        IconButton(
                            onClick = { tagPendingDelete = tag },
                            modifier = Modifier.size(40.dp),
                        ) {
                            Icon(
                                Icons.Default.Close,
                                stringResource(R.string.delete_tag),
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            OutlinedTextField(
                value = uiState.newTagName,
                onValueChange = onNewTagNameChange,
                label = { Text(stringResource(R.string.new_tag)) },
                singleLine = true,
                keyboardOptions =
                    KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Done,
                    ),
                modifier = Modifier.weight(1f),
            )
            IconButton(
                onClick = onAddNewTag,
                enabled = uiState.newTagName.isNotBlank(),
            ) {
                Icon(Icons.Default.Add, stringResource(R.string.add_tag))
            }
        }
    }

    tagPendingDelete?.let { tag ->
        ConfirmDeleteTagDialog(
            tagName = tag.name,
            onConfirm = {
                tagPendingDelete = null
                onDeleteTag(tag.id)
            },
            onDismiss = { tagPendingDelete = null },
        )
    }
}

@Composable
private fun ConfirmDeleteTagDialog(
    tagName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.delete_tag_title)) },
        text = { Text(stringResource(R.string.delete_tag_message, tagName)) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = stringResource(R.string.delete),
                    color = MaterialTheme.colorScheme.error,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        },
    )
}

@Composable
private fun NotesAndSourceFields(
    notes: String,
    sourceUrl: String,
    @StringRes sourceUrlErrorResId: Int?,
    onNotesChange: (String) -> Unit,
    onSourceUrlChange: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
        OutlinedTextField(
            value = notes,
            onValueChange = onNotesChange,
            label = { Text(stringResource(R.string.notes_label)) },
            minLines = 3,
            keyboardOptions =
                KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Default,
                ),
            modifier = Modifier.fillMaxWidth(),
        )

        OutlinedTextField(
            value = sourceUrl,
            onValueChange = onSourceUrlChange,
            label = { Text(stringResource(R.string.source_url)) },
            isError = sourceUrlErrorResId != null,
            supportingText = sourceUrlErrorResId?.let { errorResId -> { Text(stringResource(errorResId)) } },
            singleLine = true,
            keyboardOptions =
                KeyboardOptions(
                    keyboardType = KeyboardType.Uri,
                    imeAction = ImeAction.Done,
                ),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun FieldErrorText(
    @StringRes errorResId: Int,
) {
    Text(
        text = stringResource(errorResId),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.error,
    )
}

@Composable
private fun UnsavedChangesDialog(
    onDiscard: () -> Unit,
    onKeepEditing: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onKeepEditing,
        title = { Text(stringResource(R.string.recipe_unsaved_changes_title)) },
        text = { Text(stringResource(R.string.recipe_unsaved_changes_message)) },
        confirmButton = {
            TextButton(onClick = onDiscard) {
                Text(stringResource(R.string.discard_changes))
            }
        },
        dismissButton = {
            TextButton(onClick = onKeepEditing) {
                Text(stringResource(R.string.keep_editing))
            }
        },
    )
}
