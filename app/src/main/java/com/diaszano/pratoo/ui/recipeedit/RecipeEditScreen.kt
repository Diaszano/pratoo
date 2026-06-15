package com.diaszano.pratoo.ui.recipeedit

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.diaszano.pratoo.ui.theme.LocalAppColors
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.input.KeyboardType
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.diaszano.pratoo.R

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun RecipeEditScreen(
    onNavigateBack: () -> Unit,
    onRecipeSaved: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RecipeEditViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val appColors = LocalAppColors.current

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let { viewModel.onImageSelected(it) }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(if (uiState.isLoaded && uiState.title.isNotBlank()) uiState.title else stringResource(R.string.add_recipe)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.cancel))
                    }
                }
            )
        },
        bottomBar = {
            Button(
                onClick = { viewModel.onSave(onRecipeSaved) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                enabled = !uiState.isSaving
            ) {
                Text(stringResource(R.string.save_recipe))
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .clip(MaterialTheme.shapes.medium)
                    .clickable { photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                contentAlignment = Alignment.Center
            ) {
                if (uiState.imageUri != null) {
                    AsyncImage(
                        model = uiState.imageUri,
                        contentDescription = stringResource(R.string.photo),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.AddAPhoto,
                            contentDescription = stringResource(R.string.add_photo),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            stringResource(R.string.add_photo),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            FilterChip(
                selected = uiState.isFavorite,
                onClick = viewModel::onToggleFavorite,
                label = { Text(stringResource(R.string.favorite)) },
                leadingIcon = {
                    Icon(
                        Icons.Filled.Star,
                        contentDescription = null,
                            tint = if (uiState.isFavorite) appColors.favorite else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            )

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = uiState.title,
                onValueChange = viewModel::onTitleChange,
                label = { Text(stringResource(R.string.title_required)) },
                isError = uiState.titleError != null,
                supportingText = uiState.titleError?.let { { Text(it) } },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = uiState.servings,
                    onValueChange = viewModel::onServingsChange,
                    label = { Text(stringResource(R.string.servings)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))
                OutlinedTextField(
                    value = uiState.prepTimeMinutes,
                    onValueChange = viewModel::onPrepTimeChange,
                    label = { Text(stringResource(R.string.prep_time) + " (${stringResource(R.string.minutes_short)})") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))
                OutlinedTextField(
                    value = uiState.cookTimeMinutes,
                    onValueChange = viewModel::onCookTimeChange,
                    label = { Text(stringResource(R.string.cook_time) + " (${stringResource(R.string.minutes_short)})") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            Text(stringResource(R.string.ingredients_label), style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))

            uiState.ingredients.forEachIndexed { index, ingredient ->
                IngredientRow(
                    index = index,
                    ingredient = ingredient,
                    units = uiState.measurementUnits,
                    onNameChange = { viewModel.onIngredientChange(index, it, ingredient.quantity, ingredient.unit) },
                    onQuantityChange = { viewModel.onIngredientChange(index, ingredient.name, it, ingredient.unit) },
                    onUnitChange = { viewModel.onIngredientChange(index, ingredient.name, ingredient.quantity, it) },
                    onRemove = { viewModel.onRemoveIngredient(index) }
                )
                Spacer(Modifier.height(4.dp))
            }
            OutlinedButton(
                onClick = viewModel::onAddIngredient,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text(stringResource(R.string.add_ingredient))
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            Text(stringResource(R.string.steps_label), style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))

            uiState.steps.forEachIndexed { index, step ->
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                    Text(
                        "${index + 1}.",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    OutlinedTextField(
                        value = step.text,
                        onValueChange = { viewModel.onStepChange(index, it) },
                        label = { Text("${stringResource(R.string.step_label)} ${index + 1}") },
                        modifier = Modifier
                            .weight(1f)
                            .height(80.dp)
                    )
                    Column {
                        IconButton(
                            onClick = { viewModel.onMoveStepUp(index) },
                            enabled = index > 0
                        ) {
                            Icon(
                                Icons.Default.ArrowUpward,
                                contentDescription = stringResource(R.string.move_up),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        IconButton(
                            onClick = { viewModel.onMoveStepDown(index) },
                            enabled = index < uiState.steps.lastIndex
                        ) {
                            Icon(
                                Icons.Default.ArrowDownward,
                                contentDescription = stringResource(R.string.move_down),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        IconButton(onClick = { viewModel.onRemoveStep(index) }) {
                            Icon(Icons.Default.Close, stringResource(R.string.remove))
                        }
                    }
                }
                Spacer(Modifier.height(4.dp))
            }
            OutlinedButton(
                onClick = viewModel::onAddStep,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text(stringResource(R.string.add_step))
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            Text(stringResource(R.string.tags_label), style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))

            if (uiState.allTags.isNotEmpty()) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    uiState.allTags.forEach { tag ->
                        FilterChip(
                            selected = tag.id in uiState.selectedTagIds,
                            onClick = { viewModel.onToggleTag(tag.id) },
                            label = { Text(tag.name) },
                            trailingIcon = {
                                IconButton(
                                    onClick = { viewModel.onDeleteTag(tag.id) },
                                    modifier = Modifier.size(18.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = stringResource(R.string.delete),
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = uiState.newTagName,
                    onValueChange = viewModel::onNewTagNameChange,
                    label = { Text(stringResource(R.string.new_tag)) },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))
                IconButton(
                    onClick = viewModel::onAddNewTag,
                    enabled = uiState.newTagName.isNotBlank()
                ) {
                    Icon(Icons.Default.Add, stringResource(R.string.add_tag))
                }
            }

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = uiState.notes,
                onValueChange = viewModel::onNotesChange,
                label = { Text(stringResource(R.string.notes_label)) },
                minLines = 3,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = uiState.sourceUrl,
                onValueChange = viewModel::onSourceUrlChange,
                label = { Text(stringResource(R.string.source_url)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(80.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun IngredientRow(
    index: Int,
    ingredient: IngredientFormItem,
    units: List<com.diaszano.pratoo.recipe.domain.model.MeasurementUnit>,
    onNameChange: (String) -> Unit,
    onQuantityChange: (String) -> Unit,
    onUnitChange: (String) -> Unit,
    onRemove: () -> Unit
) {
    var unitExpanded by remember { mutableStateOf(false) }

    val groupedUnits = remember(units) {
        units.groupBy { it.category }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = ingredient.name,
                onValueChange = onNameChange,
                label = { Text(stringResource(R.string.name_label)) },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(8.dp))
            IconButton(onClick = onRemove) {
                Icon(Icons.Default.Close, stringResource(R.string.remove))
            }
        }
        Spacer(Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = ingredient.quantity,
                onValueChange = onQuantityChange,
                label = { Text(stringResource(R.string.quantity_label)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(8.dp))
            ExposedDropdownMenuBox(
                expanded = unitExpanded,
                onExpandedChange = { unitExpanded = !unitExpanded },
                modifier = Modifier.weight(1f)
            ) {
                OutlinedTextField(
                    value = ingredient.unit.ifEmpty { "" },
                    onValueChange = onUnitChange,
                    label = { Text(stringResource(R.string.unit_label)) },
                    singleLine = true,
                    readOnly = true,
                    trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) },
                    modifier = Modifier.menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = unitExpanded,
                    onDismissRequest = { unitExpanded = false }
                ) {
                    groupedUnits.forEach { (category, categoryUnits) ->
                        val categoryLabel = when (category) {
                            "weight" -> "Peso"
                            "volume" -> "Volume"
                            "count" -> "Quantidade"
                            else -> "Outros"
                        }
                        DropdownMenuItem(
                            text = {
                                Text(
                                    categoryLabel,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            },
                            onClick = {},
                            enabled = false
                        )
                        categoryUnits.forEach { unit ->
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            unit.abbreviation,
                                            style = MaterialTheme.typography.titleSmall,
                                            modifier = Modifier.width(48.dp)
                                        )
                                        Text(
                                            unit.displayName,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                },
                                onClick = {
                                    onUnitChange(unit.abbreviation)
                                    unitExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
