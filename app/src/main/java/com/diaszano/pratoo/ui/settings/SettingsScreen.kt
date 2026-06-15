package com.diaszano.pratoo.ui.settings

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.diaszano.pratoo.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToBackupSettings: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    val exportLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.CreateDocument("application/json"),
        ) { uri: Uri? ->
            uri?.let { viewModel.exportToUri(it, context) }
        }

    val importLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.OpenDocument(),
        ) { uri: Uri? ->
            uri?.let { viewModel.importFromUri(it, context) }
        }

    LaunchedEffect(uiState.message) {
        uiState.message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.cancel))
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
        ) {
            Text(stringResource(R.string.backup), style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            Text(
                stringResource(R.string.backup_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(16.dp))

            Button(
                onClick = {
                    exportLauncher.launch("pratoo-backup.json")
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isExporting,
            ) {
                Icon(Icons.Default.FileDownload, null)
                Spacer(Modifier.padding(ButtonDefaults.IconSpacing))
                Text(if (uiState.isExporting) stringResource(R.string.exporting) else stringResource(R.string.export_recipes))
            }

            Spacer(Modifier.height(12.dp))

            OutlinedButton(
                onClick = { importLauncher.launch(arrayOf("application/json")) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isImporting,
            ) {
                Icon(Icons.Default.FileUpload, null)
                Spacer(Modifier.padding(ButtonDefaults.IconSpacing))
                Text(if (uiState.isImporting) stringResource(R.string.importing) else stringResource(R.string.import_recipes))
            }

            Spacer(Modifier.height(24.dp))
            Text(stringResource(R.string.google_drive_backup), style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            Text(
                stringResource(R.string.backup_permission_explanation),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(12.dp))
            OutlinedButton(
                onClick = onNavigateToBackupSettings,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(Icons.Default.Cloud, null)
                Spacer(Modifier.padding(ButtonDefaults.IconSpacing))
                Text(stringResource(R.string.backup_settings_title))
            }

            Spacer(Modifier.height(24.dp))
            Text(stringResource(R.string.appearance), style = MaterialTheme.typography.titleMedium)

            // ── Palette selection ─────────────────────────────────────
            Spacer(Modifier.height(8.dp))
            Text(
                stringResource(R.string.theme_palette),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                val paletteOptions =
                    listOf(
                        "pratoo" to R.string.theme_pratoo,
                        "moonlight" to R.string.theme_moonlight,
                    )
                paletteOptions.forEach { (value, labelRes) ->
                    FilterChip(
                        selected = uiState.appTheme == value,
                        onClick = { viewModel.onAppThemeChange(value) },
                        label = { Text(stringResource(labelRes)) },
                        modifier = Modifier.padding(end = 8.dp),
                    )
                }
            }

            // ── Mode selection ────────────────────────────────────────
            Spacer(Modifier.height(12.dp))
            Text(
                stringResource(R.string.theme_mode),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                val modeOptions =
                    listOf(
                        "system" to R.string.theme_system,
                        "light" to R.string.theme_light,
                        "dark" to R.string.theme_dark,
                    )
                modeOptions.forEach { (value, labelRes) ->
                    FilterChip(
                        selected = uiState.themeMode == value,
                        onClick = { viewModel.onThemeModeChange(value) },
                        label = { Text(stringResource(labelRes)) },
                        modifier = Modifier.padding(end = 8.dp),
                    )
                }
            }

            Spacer(Modifier.height(24.dp))
            Text(stringResource(R.string.preferences), style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            Text(
                stringResource(R.string.unit_system),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                val unitOptions =
                    listOf(
                        "metric" to R.string.unit_metric,
                        "imperial" to R.string.unit_imperial,
                    )
                unitOptions.forEach { (value, labelRes) ->
                    FilterChip(
                        selected = uiState.unitSystem == value,
                        onClick = { viewModel.onUnitSystemChange(value) },
                        label = { Text(stringResource(labelRes)) },
                        modifier = Modifier.padding(end = 8.dp),
                    )
                }
            }
        }
    }
}
