package com.diaszano.pratoo.ui.recipeedit

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.diaszano.pratoo.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeEditScreen(
    onNavigateBack: () -> Unit,
    onRecipeSaved: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RecipeEditViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let { viewModel.onImageSelected(it) }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(if (uiState.isLoaded && uiState.title.isBlank()) "Nova receita" else uiState.title.ifBlank { "Nova receita" }) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Voltar")
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
                Text("Salvar receita")
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
            // Photo section
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
                        contentDescription = "Foto da receita",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.AddAPhoto,
                            contentDescription = "Adicionar foto",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Adicionar foto",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Title
            OutlinedTextField(
                value = uiState.title,
                onValueChange = viewModel::onTitleChange,
                label = { Text("Título *") },
                isError = uiState.titleError != null,
                supportingText = uiState.titleError?.let { { Text(it) } },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            // Metadata: servings, prep, cook
            Row(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = uiState.servings,
                    onValueChange = viewModel::onServingsChange,
                    label = { Text("Porções") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))
                OutlinedTextField(
                    value = uiState.prepTimeMinutes,
                    onValueChange = viewModel::onPrepTimeChange,
                    label = { Text("Preparo (min)") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))
                OutlinedTextField(
                    value = uiState.cookTimeMinutes,
                    onValueChange = viewModel::onCookTimeChange,
                    label = { Text("Cozimento (min)") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(16.dp))

            // Ingredients section
            Text("Ingredientes", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))

            uiState.ingredients.forEachIndexed { index, ingredient ->
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = ingredient.name,
                        onValueChange = { viewModel.onIngredientChange(index, it, ingredient.quantity, ingredient.unit) },
                        label = { Text("Nome") },
                        singleLine = true,
                        modifier = Modifier.weight(2f)
                    )
                    Spacer(Modifier.width(4.dp))
                    OutlinedTextField(
                        value = ingredient.quantity,
                        onValueChange = { viewModel.onIngredientChange(index, ingredient.name, it, ingredient.unit) },
                        label = { Text("Qtd") },
                        singleLine = true,
                        modifier = Modifier.weight(0.7f)
                    )
                    Spacer(Modifier.width(4.dp))
                    OutlinedTextField(
                        value = ingredient.unit,
                        onValueChange = { viewModel.onIngredientChange(index, ingredient.name, ingredient.quantity, it) },
                        label = { Text("Un.") },
                        singleLine = true,
                        modifier = Modifier.weight(0.7f)
                    )
                    IconButton(onClick = { viewModel.onRemoveIngredient(index) }) {
                        Icon(Icons.Default.Close, "Remover")
                    }
                }
                Spacer(Modifier.height(4.dp))
            }
            OutlinedButton(
                onClick = viewModel::onAddIngredient,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text("Adicionar ingrediente")
            }

            Spacer(Modifier.height(16.dp))

            // Steps section
            Text("Modo de preparo", style = MaterialTheme.typography.titleMedium)
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
                        label = { Text("Passo ${index + 1}") },
                        modifier = Modifier
                            .weight(1f)
                            .height(80.dp)
                    )
                    IconButton(onClick = { viewModel.onRemoveStep(index) }) {
                        Icon(Icons.Default.Close, "Remover")
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
                Text("Adicionar passo")
            }

            Spacer(Modifier.height(16.dp))

            // Tags section
            Text("Tags", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))

            if (uiState.allTags.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    uiState.allTags.forEach { tag ->
                        FilterChip(
                            selected = tag.id in uiState.selectedTagIds,
                            onClick = { viewModel.onToggleTag(tag.id) },
                            label = { Text(tag.name) },
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    }
                }
            }

            // Notes
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = uiState.notes,
                onValueChange = viewModel::onNotesChange,
                label = { Text("Anotações") },
                minLines = 3,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(80.dp))
        }
    }
}
