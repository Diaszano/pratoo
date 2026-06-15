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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.diaszano.pratoo.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CookingModeScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CookingModeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    KeepScreenOn(enabled = !uiState.isFinished)

    LaunchedEffect(uiState.isFinished) {
        if (uiState.isFinished) {
            if (uiState.completedIngredientIds.isNotEmpty() || uiState.completedStepIds.isNotEmpty()) {
                snackbarHostState.showSnackbar(
                    message = com.diaszano.pratoo.R.string.recipe_finished_message.let { resId ->
                        ""
                    }
                )
            }
            onNavigateBack()
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(uiState.recipeTitle.ifBlank { stringResource(R.string.start_cooking) }) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.onExitCooking() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.exit_cooking_mode))
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        if (uiState.isLoading) {
            Column(
                modifier = Modifier.fillMaxSize().padding(padding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(stringResource(R.string.cooking_ingredients), style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
            ) {
                val totalIngredients = uiState.sections.sumOf { it.ingredients.size }
                val totalSteps = uiState.sections.sumOf { it.steps.size }
                val totalItems = totalIngredients + totalSteps
                val completedItems = uiState.completedIngredientIds.size + uiState.completedStepIds.size
                val progress = if (totalItems > 0) completedItems.toFloat() / totalItems else 0f

                Spacer(Modifier.height(8.dp))

                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth(),
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    stringResource(R.string.cooking_progress, (progress * 100).toInt()),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (totalIngredients > 0) {
                    Text(
                        stringResource(R.string.completed_ingredients_count, uiState.completedIngredientIds.size, totalIngredients),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (totalSteps > 0) {
                    Text(
                        stringResource(R.string.completed_steps_count, uiState.completedStepIds.size, totalSteps),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    stringResource(R.string.keep_screen_on_notice),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                uiState.sections.forEach { section ->
                    if (section.showHeader) {
                        Spacer(Modifier.height(16.dp))
                        Text(
                            section.name.ifBlank { stringResource(R.string.default_recipe_section) },
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (section.ingredients.isNotEmpty()) {
                        Spacer(Modifier.height(if (section.showHeader) 8.dp else 16.dp))
                        Text(
                            stringResource(R.string.cooking_ingredients),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(4.dp))
                        section.ingredients.forEach { item ->
                            val checked = item.id in uiState.completedIngredientIds
                            CookingItemRow(
                                text = item.displayText,
                                checked = checked,
                                onCheckedChange = { viewModel.onIngredientCheckedChange(item.id, it) }
                            )
                        }
                    }

                    if (section.steps.isNotEmpty()) {
                        Spacer(Modifier.height(12.dp))
                        Text(
                            stringResource(R.string.cooking_steps),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(4.dp))
                        section.steps.forEach { item ->
                            val checked = item.id in uiState.completedStepIds
                            CookingItemRow(
                                text = "${item.order + 1}. ${item.name}",
                                checked = checked,
                                onCheckedChange = { viewModel.onStepCheckedChange(item.id, it) }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))
            }
        }
    }

    if (uiState.showExitDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.onDismissExitDialog() },
            title = { Text(stringResource(R.string.confirm_exit_cooking_title)) },
            text = { Text(stringResource(R.string.confirm_exit_cooking_message)) },
            confirmButton = {
                TextButton(onClick = { viewModel.onConfirmExit() }) {
                    Text(stringResource(R.string.confirm_exit_cooking_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onDismissExitDialog() }) {
                    Text(stringResource(R.string.confirm_exit_cooking_cancel))
                }
            }
        )
    }
}

@Composable
private fun CookingItemRow(
    text: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge.copy(
                textDecoration = if (checked) TextDecoration.LineThrough else TextDecoration.None,
                color = if (checked) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
            ),
            modifier = Modifier.padding(start = 4.dp)
        )
    }
}
