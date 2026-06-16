package com.diaszano.pratoo.ui.recipedetail

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.diaszano.pratoo.R
import com.diaszano.pratoo.recipe.domain.model.Recipe
import com.diaszano.pratoo.ui.shared.ErrorState
import com.diaszano.pratoo.ui.shared.LoadingState
import com.diaszano.pratoo.ui.theme.LocalAppColors
import com.diaszano.pratoo.ui.theme.Spacing

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Suppress("LocalContextGetResourceValueCall")
@Composable
fun RecipeDetailScreen(
    onNavigateBack: () -> Unit,
    onEditRecipe: (Long) -> Unit,
    onStartCooking: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RecipeDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showDeleteDialog by remember { mutableStateOf(false) }
    val appColors = LocalAppColors.current
    val recipe = uiState.recipe

    LaunchedEffect(uiState.isDeleted) {
        if (uiState.isDeleted) {
            Toast.makeText(context, context.getString(R.string.recipe_deleted), Toast.LENGTH_SHORT).show()
            onNavigateBack()
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(uiState.recipe?.title.orEmpty(), maxLines = 1) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.cancel))
                    }
                },
                actions = {
                    recipe?.let { currentRecipe ->
                        IconButton(onClick = { shareRecipe(currentRecipe, context.findActivity()) }) {
                            Icon(Icons.Default.Share, stringResource(R.string.share))
                        }
                        IconButton(onClick = viewModel::onToggleFavorite) {
                            Icon(
                                Icons.Filled.Star,
                                contentDescription =
                                    if (currentRecipe.isFavorite) {
                                        stringResource(R.string.remove_from_favorites)
                                    } else {
                                        stringResource(R.string.add_to_favorites)
                                    },
                                tint = if (currentRecipe.isFavorite) appColors.favorite else MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        IconButton(onClick = { onEditRecipe(currentRecipe.id) }) {
                            Icon(Icons.Default.Edit, stringResource(R.string.edit))
                        }
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Default.Delete, stringResource(R.string.delete))
                        }
                    }
                },
            )
        },
        bottomBar = {
            recipe?.let { currentRecipe ->
                Button(
                    onClick = { onStartCooking(currentRecipe.id) },
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(Spacing.lg),
                ) {
                    Icon(Icons.Default.Restaurant, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(Spacing.sm))
                    Text(stringResource(R.string.start_cooking))
                }
            }
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
            recipe == null -> {
                ErrorState(
                    message = stringResource(R.string.recipe_not_found),
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(padding),
                )
            }
            else -> {
                RecipeDetailContent(
                    recipe = recipe,
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(padding),
                )
            }
        }
    }

    if (showDeleteDialog) {
        ConfirmDeleteDialog(
            onConfirm = {
                showDeleteDialog = false
                viewModel.onDelete()
            },
            onDismiss = { showDeleteDialog = false },
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RecipeDetailContent(
    recipe: Recipe,
    modifier: Modifier = Modifier,
) {
    val uriHandler = LocalUriHandler.current

    Column(
        modifier =
            modifier
                .verticalScroll(rememberScrollState()),
    ) {
        RecipeImageHeader(recipe = recipe)

        Column(
            modifier = Modifier.padding(Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.lg),
        ) {
            Text(
                text = recipe.title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.SemiBold,
            )

            RecipeMetadata(recipe = recipe)

            if (recipe.tags.isNotEmpty()) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                    verticalArrangement = Arrangement.spacedBy(Spacing.xs),
                ) {
                    recipe.tags.forEach { tag ->
                        SuggestionChip(
                            onClick = {},
                            label = { Text(tag.name) },
                        )
                    }
                }
            }

            if (recipe.sourceUrl?.isNotBlank() == true) {
                Text(
                    text = "${stringResource(R.string.source_label)}: ${recipe.sourceUrl}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier =
                        Modifier.clickable {
                            runCatching { uriHandler.openUri(recipe.sourceUrl) }
                        },
                )
            }

            recipe.sections.forEach { section ->
                val hasContent = section.ingredients.isNotEmpty() || section.steps.isNotEmpty()
                if (!hasContent) return@forEach

                val showSectionHeader = recipe.sections.size > 1 || section.name.isNotBlank()

                if (showSectionHeader) {
                    HorizontalDivider()
                    Text(
                        text = section.name.ifBlank { stringResource(R.string.default_recipe_section) },
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold,
                    )
                }

                if (section.ingredients.isNotEmpty()) {
                    RecipeTextSection(title = stringResource(R.string.ingredients_label)) {
                        section.ingredients.forEach { ingredient ->
                            Text(
                                text = "• ${formatIngredient(ingredient.name, ingredient.quantity, ingredient.unit)}",
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(vertical = 2.dp),
                            )
                        }
                    }
                }

                if (section.steps.isNotEmpty()) {
                    RecipeTextSection(title = stringResource(R.string.steps_label)) {
                        section.steps.sortedBy { it.order }.forEachIndexed { index, step ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                            ) {
                                Text(
                                    text = "${index + 1}.",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold,
                                )
                                Text(
                                    text = step.text,
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.weight(1f),
                                )
                            }
                        }
                    }
                }
            }

            if (recipe.notes.isNotBlank()) {
                HorizontalDivider()
                RecipeTextSection(title = stringResource(R.string.notes_label)) {
                    Text(
                        recipe.notes,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }

            Spacer(Modifier.height(80.dp))
        }
    }
}

@Composable
private fun RecipeImageHeader(recipe: Recipe) {
    if (recipe.imageUri != null) {
        AsyncImage(
            model = recipe.imageUri,
            contentDescription = recipe.title,
            contentScale = ContentScale.Crop,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f),
        )
    } else {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(Spacing.sm),
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.MenuBook,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(48.dp),
                )
                Text(
                    stringResource(R.string.recipe_photo_placeholder),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RecipeMetadata(recipe: Recipe) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        verticalArrangement = Arrangement.spacedBy(Spacing.xs),
    ) {
        if (recipe.servings > 0) {
            AssistChip(
                onClick = {},
                leadingIcon = {
                    Icon(Icons.Default.People, contentDescription = null, modifier = Modifier.size(18.dp))
                },
                label = { Text("${recipe.servings} ${stringResource(R.string.servings).lowercase()}") },
            )
        }
        if (recipe.prepTimeMinutes > 0) {
            AssistChip(
                onClick = {},
                leadingIcon = {
                    Icon(Icons.Default.AccessTime, contentDescription = null, modifier = Modifier.size(18.dp))
                },
                label = {
                    Text(
                        "${stringResource(R.string.prep_time)}: " +
                            "${recipe.prepTimeMinutes} ${stringResource(R.string.minutes_short)}",
                    )
                },
            )
        }
        if (recipe.cookTimeMinutes > 0) {
            AssistChip(
                onClick = {},
                leadingIcon = {
                    Icon(Icons.Default.AccessTime, contentDescription = null, modifier = Modifier.size(18.dp))
                },
                label = {
                    Text(
                        "${stringResource(R.string.cook_time)}: " +
                            "${recipe.cookTimeMinutes} ${stringResource(R.string.minutes_short)}",
                    )
                },
            )
        }
    }
}

@Composable
private fun RecipeTextSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
        )
        content()
    }
}

@Composable
private fun ConfirmDeleteDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.delete_recipe_title)) },
        text = { Text(stringResource(R.string.delete_recipe_message)) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = stringResource(R.string.move_to_trash),
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

private fun shareRecipe(
    recipe: Recipe,
    activity: Activity?,
) {
    val context = activity ?: return
    val text = formatRecipeAsText(recipe, context)
    val sendIntent =
        Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
            setPackage("com.whatsapp")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    val chooserIntent =
        Intent.createChooser(sendIntent, null).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    try {
        context.startActivity(chooserIntent)
    } catch (_: Exception) {
        try {
            context.startActivity(sendIntent)
        } catch (_: Exception) {
        }
    }
}

private tailrec fun Context.findActivity(): Activity? =
    when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }

private fun formatIngredient(
    name: String,
    quantity: String,
    unit: String,
): String =
    buildString {
        if (quantity.isNotBlank()) append("$quantity ")
        if (unit.isNotBlank()) append("$unit ")
        append(name)
    }

private fun formatRecipeAsText(
    recipe: Recipe,
    context: android.content.Context,
): String =
    buildString {
        appendLine(recipe.title)
        appendLine()

        val hasMeta =
            recipe.servings > 0 ||
                recipe.prepTimeMinutes > 0 ||
                recipe.cookTimeMinutes > 0
        if (hasMeta) {
            val meta = mutableListOf<String>()
            if (recipe.servings > 0) meta.add("${context.getString(R.string.share_servings)}: ${recipe.servings}")
            if (recipe.prepTimeMinutes > 0) meta.add("${context.getString(R.string.share_prep_time)}: ${recipe.prepTimeMinutes}min")
            if (recipe.cookTimeMinutes > 0) meta.add("${context.getString(R.string.share_cook_time)}: ${recipe.cookTimeMinutes}min")
            appendLine(meta.joinToString(" | "))
            appendLine()
        }

        if (recipe.tags.isNotEmpty()) {
            appendLine(recipe.tags.joinToString("  ") { "#${it.name}" })
            appendLine()
        }

        recipe.sections.forEach { section ->
            val showSectionHeader = recipe.sections.size > 1 || section.name.isNotBlank()
            if (showSectionHeader) {
                appendLine(section.name.ifBlank { context.getString(R.string.default_recipe_section) })
                appendLine()
            }

            if (section.ingredients.isNotEmpty()) {
                appendLine(context.getString(R.string.share_ingredients_header))
                section.ingredients.forEach { ing ->
                    appendLine("- ${formatIngredient(ing.name, ing.quantity, ing.unit)}")
                }
                appendLine()
            }

            if (section.steps.isNotEmpty()) {
                appendLine(context.getString(R.string.share_steps_header))
                section.steps.sortedBy { it.order }.forEachIndexed { index, step ->
                    appendLine("${index + 1}. ${step.text}")
                }
                appendLine()
            }
        }

        if (recipe.notes.isNotBlank()) {
            appendLine(context.getString(R.string.share_notes_header))
            appendLine(recipe.notes)
        }
    }.trimEnd()
