package com.diaszano.pratoo.ui.trash

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.RestoreFromTrash
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.diaszano.pratoo.R
import com.diaszano.pratoo.recipe.domain.model.RecipeListItem
import com.diaszano.pratoo.ui.shared.EmptyState
import com.diaszano.pratoo.ui.shared.LoadingState
import com.diaszano.pratoo.ui.theme.Spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrashScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TrashViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var recipePendingPermanentDelete by remember { mutableStateOf<RecipeListItem?>(null) }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.trash)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.cancel))
                    }
                },
            )
        },
    ) { padding ->
        when {
            uiState.isLoading -> {
                LoadingState(
                    message = stringResource(R.string.loading_recipe),
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(padding),
                )
            }
            uiState.recipes.isEmpty() -> {
                EmptyState(
                    title = stringResource(R.string.trash_empty_title),
                    message = stringResource(R.string.trash_empty_message),
                    icon = Icons.Default.Delete,
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
                    contentPadding = PaddingValues(Spacing.lg),
                    verticalArrangement = Arrangement.spacedBy(Spacing.md),
                ) {
                    items(uiState.recipes, key = { it.id }) { recipe ->
                        DeletedRecipeRow(
                            recipe = recipe,
                            onRestore = { viewModel.onRestoreRecipe(recipe.id) },
                            onDeletePermanently = { recipePendingPermanentDelete = recipe },
                        )
                    }
                }
            }
        }
    }

    recipePendingPermanentDelete?.let { recipe ->
        ConfirmPermanentDeleteDialog(
            recipeTitle = recipe.title,
            onConfirm = {
                recipePendingPermanentDelete = null
                viewModel.onDeleteRecipePermanently(recipe.id)
            },
            onDismiss = { recipePendingPermanentDelete = null },
        )
    }
}

@Composable
private fun DeletedRecipeRow(
    recipe: RecipeListItem,
    onRestore: () -> Unit,
    onDeletePermanently: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(Spacing.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(Spacing.xs),
            ) {
                Text(
                    text = recipe.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = stringResource(R.string.trash_recipe_auto_delete),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            IconButton(onClick = onRestore) {
                Icon(Icons.Default.RestoreFromTrash, stringResource(R.string.restore_recipe))
            }
            IconButton(onClick = onDeletePermanently) {
                Icon(
                    Icons.Default.DeleteForever,
                    stringResource(R.string.delete_recipe_permanently),
                    tint = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}

@Composable
private fun ConfirmPermanentDeleteDialog(
    recipeTitle: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.delete_recipe_permanently_title)) },
        text = { Text(stringResource(R.string.delete_recipe_permanently_message, recipeTitle)) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = stringResource(R.string.delete_permanently),
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
