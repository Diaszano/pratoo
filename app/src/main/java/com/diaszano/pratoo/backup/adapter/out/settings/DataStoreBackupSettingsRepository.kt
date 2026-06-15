package com.diaszano.pratoo.backup.adapter.out.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.diaszano.pratoo.backup.domain.model.BackupSettings
import com.diaszano.pratoo.backup.domain.model.BackupStatus
import com.diaszano.pratoo.backup.domain.port.BackupSettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataStoreBackupSettingsRepository
    @Inject
    constructor(
        private val dataStore: DataStore<Preferences>,
    ) : BackupSettingsRepository {
        private companion object {
            val KEY_AUTOMATIC_BACKUP_ENABLED = booleanPreferencesKey("drive_auto_backup_enabled")
            val KEY_LAST_SUCCESSFUL_BACKUP_AT = longPreferencesKey("drive_last_successful_backup_at")
            val KEY_LAST_BACKUP_ATTEMPT_AT = longPreferencesKey("drive_last_backup_attempt_at")
            val KEY_LAST_BACKUP_STATUS = stringPreferencesKey("drive_last_backup_status")
            val KEY_SELECTED_GOOGLE_ACCOUNT_EMAIL = stringPreferencesKey("drive_selected_account_email")
        }

        override fun observeBackupSettings(): Flow<BackupSettings> =
            dataStore.data.map { prefs ->
                BackupSettings(
                    automaticDriveBackupEnabled = prefs[KEY_AUTOMATIC_BACKUP_ENABLED] ?: false,
                    lastSuccessfulBackupAt = prefs[KEY_LAST_SUCCESSFUL_BACKUP_AT],
                    lastBackupAttemptAt = prefs[KEY_LAST_BACKUP_ATTEMPT_AT],
                    lastBackupStatus =
                        parseBackupStatus(prefs[KEY_LAST_BACKUP_STATUS])
                            ?: BackupStatus.NeverBackedUp,
                    selectedGoogleAccountEmail = prefs[KEY_SELECTED_GOOGLE_ACCOUNT_EMAIL],
                )
            }

        override suspend fun setAutomaticBackupEnabled(enabled: Boolean) {
            dataStore.edit { prefs ->
                prefs[KEY_AUTOMATIC_BACKUP_ENABLED] = enabled
            }
        }

        override suspend fun setLastSuccessfulBackupAt(timestamp: Long) {
            dataStore.edit { prefs ->
                prefs[KEY_LAST_SUCCESSFUL_BACKUP_AT] = timestamp
            }
        }

        override suspend fun setLastBackupAttemptAt(timestamp: Long) {
            dataStore.edit { prefs ->
                prefs[KEY_LAST_BACKUP_ATTEMPT_AT] = timestamp
            }
        }

        override suspend fun setLastBackupStatus(status: BackupStatus) {
            dataStore.edit { prefs ->
                prefs[KEY_LAST_BACKUP_STATUS] = status.name
            }
        }

        override suspend fun setSelectedGoogleAccountEmail(email: String?) {
            dataStore.edit { prefs ->
                if (email != null) {
                    prefs[KEY_SELECTED_GOOGLE_ACCOUNT_EMAIL] = email
                } else {
                    prefs.remove(KEY_SELECTED_GOOGLE_ACCOUNT_EMAIL)
                }
            }
        }

        override suspend fun clearSelectedGoogleAccount() {
            dataStore.edit { prefs ->
                prefs.remove(KEY_SELECTED_GOOGLE_ACCOUNT_EMAIL)
                prefs.remove(KEY_AUTOMATIC_BACKUP_ENABLED)
            }
        }

        private fun parseBackupStatus(value: String?): BackupStatus? =
            when (value) {
                "NeverBackedUp" -> BackupStatus.NeverBackedUp
                "InProgress" -> BackupStatus.InProgress
                "Success" -> BackupStatus.Success
                "Failed" -> BackupStatus.Failed
                "RequiresPermission" -> BackupStatus.RequiresPermission
                else -> null
            }
    }
