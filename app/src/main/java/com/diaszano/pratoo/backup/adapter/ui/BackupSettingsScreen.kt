package com.diaszano.pratoo.backup.adapter.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.diaszano.pratoo.R
import com.diaszano.pratoo.backup.domain.model.BackupStatus
import com.diaszano.pratoo.backup.domain.model.CloudBackupFile
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupSettingsScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: BackupSettingsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    val signInLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartActivityForResult(),
        ) { result ->
            viewModel.handleSignInResult(result.data)
        }

    LaunchedEffect(uiState.message) {
        uiState.message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    // Restore confirmation dialog
    if (uiState.isShowingRestoreConfirmation) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissRestoreConfirmation() },
            title = { Text(stringResource(R.string.restore_backup_title)) },
            text = { Text(stringResource(R.string.restore_backup_message)) },
            confirmButton = {
                Button(onClick = { viewModel.confirmRestore() }) {
                    Text(stringResource(R.string.restore_backup_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissRestoreConfirmation() }) {
                    Text(stringResource(R.string.restore_backup_cancel))
                }
            },
        )
    }

    // Backup list dialog
    if (uiState.isShowingBackupList) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissBackupList() },
            title = { Text(stringResource(R.string.restore_backup_title)) },
            text = {
                if (uiState.isLoadingBackups) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        CircularProgressIndicator()
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.loading_backups),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                } else if (uiState.availableBackups.isEmpty()) {
                    Text(stringResource(R.string.no_drive_backups_found))
                } else {
                    LazyColumn {
                        items(uiState.availableBackups) { backup ->
                            BackupFileItem(
                                backup = backup,
                                onClick = { viewModel.showRestoreConfirmation(backup) },
                            )
                            HorizontalDivider()
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { viewModel.dismissBackupList() }) {
                    Text(stringResource(R.string.cancel))
                }
            },
        )
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.backup_settings_title)) },
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
            // Restore in progress indicator
            if (uiState.isRestoring) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(16.dp))
            }

            // Google account section
            Text(
                stringResource(R.string.google_drive_backup),
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                stringResource(R.string.backup_permission_explanation),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(16.dp))

            if (!uiState.isSignedIn) {
                OutlinedButton(
                    onClick = { signInLauncher.launch(viewModel.getSignInIntent()) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(Icons.Default.AccountCircle, null)
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.connect_google_account))
                }
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors =
                        CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        ),
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AccountCircle, null)
                            Spacer(Modifier.width(8.dp))
                            Column {
                                Text(
                                    uiState.signedInEmail ?: "",
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                                Text(
                                    stringResource(R.string.google_drive_backup),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        TextButton(onClick = { viewModel.signOut() }) {
                            Text(stringResource(R.string.disconnect_google_account))
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // Automatic backup toggle
            if (uiState.isSignedIn) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            stringResource(R.string.enable_automatic_backup),
                            style = MaterialTheme.typography.bodyLarge,
                        )
                        Text(
                            stringResource(R.string.backup_enabled_description),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Switch(
                        checked = uiState.automaticBackupEnabled,
                        onCheckedChange = { viewModel.onAutomaticBackupChange(it) },
                    )
                }

                Spacer(Modifier.height(16.dp))

                // Status display
                BackupStatusDisplay(
                    status = uiState.lastBackupStatus,
                    lastSuccessfulBackupAt = uiState.lastSuccessfulBackupAt,
                )

                Spacer(Modifier.height(16.dp))

                // Backup now button
                Button(
                    onClick = { viewModel.backupNow() },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isBackingUp,
                ) {
                    if (uiState.isBackingUp) {
                        CircularProgressIndicator(
                            modifier = Modifier.height(20.dp).width(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary,
                        )
                        Spacer(Modifier.width(8.dp))
                    }
                    Text(
                        if (uiState.isBackingUp) {
                            stringResource(R.string.backup_in_progress)
                        } else {
                            stringResource(R.string.backup_now)
                        },
                    )
                }

                Spacer(Modifier.height(24.dp))
                HorizontalDivider()
                Spacer(Modifier.height(16.dp))

                // Restore section
                OutlinedButton(
                    onClick = { viewModel.loadBackups() },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isRestoring,
                ) {
                    Icon(Icons.Default.CloudDownload, null)
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.restore_from_google_drive))
                }

                Spacer(Modifier.height(8.dp))
                Text(
                    stringResource(R.string.automatic_backup_notice),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun BackupStatusDisplay(
    status: BackupStatus,
    lastSuccessfulBackupAt: Long?,
) {
    val statusText =
        when (status) {
            BackupStatus.NeverBackedUp -> stringResource(R.string.backup_never_done)
            BackupStatus.InProgress -> stringResource(R.string.backup_in_progress)
            BackupStatus.Success -> {
                val dateStr =
                    lastSuccessfulBackupAt?.let { formatDateTime(it) }
                        ?: stringResource(R.string.backup_file_unknown_date)
                stringResource(R.string.last_backup, dateStr)
            }
            BackupStatus.Failed -> stringResource(R.string.backup_failed)
            BackupStatus.RequiresPermission -> stringResource(R.string.backup_requires_permission)
        }

    val color =
        when (status) {
            BackupStatus.Success -> MaterialTheme.colorScheme.primary
            BackupStatus.Failed -> MaterialTheme.colorScheme.error
            BackupStatus.RequiresPermission -> MaterialTheme.colorScheme.error
            else -> MaterialTheme.colorScheme.onSurfaceVariant
        }

    Text(
        text = statusText,
        style = MaterialTheme.typography.bodyMedium,
        color = color,
    )
}

@Composable
private fun BackupFileItem(
    backup: CloudBackupFile,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            val dateStr =
                backup.modifiedAt?.let { formatDateTime(it) }
                    ?: backup.createdAt?.let { formatDateTime(it) }
                    ?: stringResource(R.string.backup_file_unknown_date)
            Text(dateStr, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            if (backup.recipeCount != null) {
                Text(
                    stringResource(R.string.backup_file_recipe_count, backup.recipeCount),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (backup.backupVersion != null) {
                Text(
                    stringResource(R.string.backup_file_version, backup.backupVersion),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

private fun formatDateTime(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("pt", "BR"))
    return sdf.format(Date(timestamp))
}
