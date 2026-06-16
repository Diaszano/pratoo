package com.diaszano.pratoo.ui.cooking

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.diaszano.pratoo.R
import com.diaszano.pratoo.ui.shared.LoadingState
import com.diaszano.pratoo.ui.theme.Spacing

@OptIn(ExperimentalMaterial3Api::class)
@Suppress("LocalContextGetResourceValueCall")
@Composable
fun CookingModeScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CookingModeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    KeepScreenOn(enabled = !uiState.isFinished)

    LaunchedEffect(uiState.isFinished) {
        if (uiState.isFinished) {
            if (uiState.completedIngredientIds.isNotEmpty() || uiState.completedStepIds.isNotEmpty()) {
                snackbarHostState.showSnackbar(
                    message = context.getString(R.string.recipe_finished_message),
                )
            }
            onNavigateBack()
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(uiState.recipeTitle.ifBlank { stringResource(R.string.start_cooking) }, maxLines = 1) },
                navigationIcon = {
                    IconButton(onClick = viewModel::onExitCooking) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.exit_cooking_mode))
                    }
                },
            )
        },
        bottomBar = {
            if (!uiState.isLoading) {
                Button(
                    onClick = viewModel::onFinishCooking,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(Spacing.lg),
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null)
                    Spacer(Modifier.padding(Spacing.xs))
                    Text(stringResource(R.string.finish_cooking))
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        if (uiState.isLoading) {
            LoadingState(
                message = stringResource(R.string.cooking_loading),
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(padding),
            )
        } else {
            val totalIngredients = uiState.sections.sumOf { it.ingredients.size }
            val totalSteps = uiState.sections.sumOf { it.steps.size }
            val totalItems = totalIngredients + totalSteps
            val completedItems = uiState.completedIngredientIds.size + uiState.completedStepIds.size
            val progress = if (totalItems > 0) completedItems.toFloat() / totalItems else 0f

            LazyColumn(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(padding),
                contentPadding =
                    androidx.compose.foundation.layout.PaddingValues(
                        start = Spacing.lg,
                        top = Spacing.lg,
                        end = Spacing.lg,
                        bottom = 104.dp,
                    ),
                verticalArrangement = Arrangement.spacedBy(Spacing.lg),
            ) {
                item {
                    CookingProgressHeader(
                        progress = progress,
                        completedIngredientCount = uiState.completedIngredientIds.size,
                        totalIngredientCount = totalIngredients,
                        completedStepCount = uiState.completedStepIds.size,
                        totalStepCount = totalSteps,
                    )
                }

                items(
                    items = uiState.sections,
                    key = { it.id },
                ) { section ->
                    CookingSection(
                        section = section,
                        completedIngredientIds = uiState.completedIngredientIds,
                        completedStepIds = uiState.completedStepIds,
                        onIngredientCheckedChange = viewModel::onIngredientCheckedChange,
                        onStepCheckedChange = viewModel::onStepCheckedChange,
                    )
                }
            }
        }
    }

    if (uiState.showExitDialog) {
        AlertDialog(
            onDismissRequest = viewModel::onDismissExitDialog,
            title = { Text(stringResource(R.string.confirm_exit_cooking_title)) },
            text = { Text(stringResource(R.string.confirm_exit_cooking_message)) },
            confirmButton = {
                TextButton(onClick = viewModel::onConfirmExit) {
                    Text(stringResource(R.string.confirm_exit_cooking_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::onDismissExitDialog) {
                    Text(stringResource(R.string.confirm_exit_cooking_cancel))
                }
            },
        )
    }
}

@Composable
private fun CookingProgressHeader(
    progress: Float,
    completedIngredientCount: Int,
    totalIngredientCount: Int,
    completedStepCount: Int,
    totalStepCount: Int,
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors =
            CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
    ) {
        Column(
            modifier = Modifier.padding(Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth(),
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )
            Text(
                stringResource(R.string.cooking_progress, (progress * 100).toInt()),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold,
            )
            if (totalIngredientCount > 0) {
                Text(
                    stringResource(R.string.completed_ingredients_count, completedIngredientCount, totalIngredientCount),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (totalStepCount > 0) {
                Text(
                    stringResource(R.string.completed_steps_count, completedStepCount, totalStepCount),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                stringResource(R.string.keep_screen_on_notice),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun CookingSection(
    section: CookingSectionUiState,
    completedIngredientIds: Set<String>,
    completedStepIds: Set<String>,
    onIngredientCheckedChange: (String, Boolean) -> Unit,
    onStepCheckedChange: (String, Boolean) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        if (section.showHeader) {
            Text(
                section.name.ifBlank { stringResource(R.string.default_recipe_section) },
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
            )
        }

        if (section.ingredients.isNotEmpty()) {
            Text(
                stringResource(R.string.cooking_ingredients),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
            section.ingredients.forEach { item ->
                val checked = item.id in completedIngredientIds
                CookingItemRow(
                    text = item.displayText,
                    checked = checked,
                    onCheckedChange = { onIngredientCheckedChange(item.id, it) },
                )
            }
        }

        if (section.steps.isNotEmpty()) {
            Spacer(Modifier.height(Spacing.xs))
            Text(
                stringResource(R.string.cooking_steps),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
            section.steps.forEachIndexed { index, item ->
                val checked = item.id in completedStepIds
                CookingItemRow(
                    text = "${index + 1}. ${item.name}",
                    checked = checked,
                    onCheckedChange = { onStepCheckedChange(item.id, it) },
                )
            }
        }
    }
}

@Composable
private fun CookingItemRow(
    text: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    val stateText =
        if (checked) {
            stringResource(R.string.cooking_item_checked, text)
        } else {
            stringResource(R.string.cooking_item_unchecked, text)
        }

    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .animateContentSize()
                .semantics { stateDescription = stateText }
                .toggleable(
                    value = checked,
                    role = Role.Checkbox,
                    onValueChange = onCheckedChange,
                ).padding(vertical = Spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = null,
        )
        Text(
            text = text,
            style =
                MaterialTheme.typography.bodyLarge.copy(
                    textDecoration = if (checked) TextDecoration.LineThrough else TextDecoration.None,
                    color = if (checked) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                ),
            modifier = Modifier.padding(start = Spacing.sm),
        )
    }
}
