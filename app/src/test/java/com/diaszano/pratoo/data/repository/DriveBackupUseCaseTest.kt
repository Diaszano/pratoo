package com.diaszano.pratoo.data.repository

import com.diaszano.pratoo.backup.application.BackupNowUseCase
import com.diaszano.pratoo.backup.application.ObserveBackupSettingsUseCase
import com.diaszano.pratoo.backup.application.RestoreDriveBackupUseCase
import com.diaszano.pratoo.backup.domain.model.BackupMetadata
import com.diaszano.pratoo.backup.domain.model.BackupStatus
import com.diaszano.pratoo.backup.domain.model.CloudBackupFile
import com.diaszano.pratoo.backup.domain.model.RestoreMode
import com.diaszano.pratoo.backup.domain.port.BackupSettingsRepository
import com.diaszano.pratoo.backup.domain.port.CloudBackupStorage
import com.diaszano.pratoo.backup.domain.port.RecipeBackupExporter
import com.diaszano.pratoo.recipe.adapter.out.backup.JsonRecipeBackupCodec
import com.diaszano.pratoo.recipe.domain.model.Recipe
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class DriveBackupUseCaseTest {
    private lateinit var repository: FakeRecipeRepository
    private lateinit var codec: JsonRecipeBackupCodec
    private lateinit var fakeCloudStorage: FakeCloudBackupStorage
    private lateinit var fakeSettings: FakeBackupSettingsRepository
    private lateinit var fakeExporter: FakeRecipeBackupExporter

    @Before
    fun setup() {
        repository = FakeRecipeRepository()
        codec = JsonRecipeBackupCodec()
        fakeCloudStorage = FakeCloudBackupStorage()
        fakeSettings = FakeBackupSettingsRepository()
        fakeExporter = FakeRecipeBackupExporter(repository, codec)
    }

    @Test
    fun `backup now sets success status on successful upload`() =
        runTest {
            val useCase = BackupNowUseCase(fakeExporter, fakeCloudStorage, fakeSettings)
            useCase()
            assertEquals(BackupStatus.Success, fakeSettings.lastStatus)
            assertNotNull(fakeSettings.lastSuccessfulBackupAt)
        }

    @Test
    fun `backup now sets failed status on cloud storage error`() =
        runTest {
            fakeCloudStorage.shouldThrow = true
            val useCase = BackupNowUseCase(fakeExporter, fakeCloudStorage, fakeSettings)
            useCase()
            assertEquals(BackupStatus.Failed, fakeSettings.lastStatus)
        }

    @Test
    fun `backup now sets requires permission status on permission error`() =
        runTest {
            fakeCloudStorage.shouldThrow = true
            fakeCloudStorage.errorMessage = "Permission denied"
            val useCase = BackupNowUseCase(fakeExporter, fakeCloudStorage, fakeSettings)
            useCase()
            assertEquals(BackupStatus.RequiresPermission, fakeSettings.lastStatus)
        }

    @Test
    fun `backup now exports and uploads content`() =
        runTest {
            repository.saveRecipe(Recipe(title = "Test Recipe", isFavorite = true))
            val useCase = BackupNowUseCase(fakeExporter, fakeCloudStorage, fakeSettings)
            useCase()
            assertTrue(fakeCloudStorage.uploadedContent.isNotEmpty())
            assertTrue(fakeCloudStorage.uploadedContent.contains("Test Recipe"))
        }

    @Test
    fun `restore drive backup replaces local data`() =
        runTest {
            repository.saveRecipe(Recipe(title = "Old Recipe"))

            val backupRecipes =
                listOf(
                    Recipe(title = "New Recipe 1"),
                    Recipe(title = "New Recipe 2"),
                )
            val json = codec.encode(backupRecipes)
            fakeCloudStorage.addBackup("file1", json)

            val importer = FakeRecipeBackupImporter(repository, codec)
            val useCase = RestoreDriveBackupUseCase(fakeCloudStorage, importer)
            useCase("file1", RestoreMode.ReplaceLocalData)

            val all = repository.getAllRecipes()
            assertEquals(2, all.size)
            assertEquals("New Recipe 1", all[0].title)
            assertEquals("New Recipe 2", all[1].title)
        }

    @Test
    fun `restore drive backup merges with local data`() =
        runTest {
            repository.saveRecipe(Recipe(title = "Old Recipe"))

            val backupRecipes = listOf(Recipe(title = "New Recipe"))
            val json = codec.encode(backupRecipes)
            fakeCloudStorage.addBackup("file2", json)

            val importer = FakeRecipeBackupImporter(repository, codec)
            val useCase = RestoreDriveBackupUseCase(fakeCloudStorage, importer)
            useCase("file2", RestoreMode.Merge)

            val all = repository.getAllRecipes()
            assertEquals(2, all.size)
        }

    @Test
    fun `cloud backup storage lists backups correctly`() =
        runTest {
            fakeCloudStorage.addBackup(
                "id1",
                "{}",
                recipeCount = 5,
                backupVersion = 3,
            )
            fakeCloudStorage.addBackup(
                "id2",
                "{}",
                recipeCount = 3,
                backupVersion = 2,
            )

            val backups = fakeCloudStorage.listBackups()
            assertEquals(2, backups.size)
            assertEquals(5, backups[0].recipeCount)
            assertEquals(3, backups[1].recipeCount)
        }

    @Test
    fun `observe backup settings emits correct status`() =
        runTest {
            val useCase = ObserveBackupSettingsUseCase(fakeSettings)
            val settings = useCase().first()
            assertEquals(BackupStatus.NeverBackedUp, settings.lastBackupStatus)
            assertEquals(false, settings.automaticDriveBackupEnabled)
        }

    @Test
    fun `encode with metadata includes metadata fields`() {
        val recipes = listOf(Recipe(title = "Test"))
        val json = codec.encodeWithMetadata(recipes, "1.0.0", 1, "Pixel")
        assertTrue(json.contains("appVersionName"))
        assertTrue(json.contains("1.0.0"))
        assertTrue(json.contains("deviceName"))
        assertTrue(json.contains("recipeCount"))
    }

    @Test
    fun `legacy backup without sections falls back to root ingredients and steps`() {
        val legacyJson = """{
            "version": 1,
            "recipes": [{
                "title": "Legacy Recipe",
                "ingredients": [{"name": "Farinha", "quantity": "200g", "unit": "g", "position": 0}],
                "steps": [{"text": "Misturar", "order": 0}],
                "tags": []
            }]
        }"""
        val recipes = codec.decode(legacyJson)
        assertEquals(1, recipes.size)
        assertEquals("Legacy Recipe", recipes[0].title)
        assertEquals(1, recipes[0].sections.size)
        assertEquals("Farinha", recipes[0].sections[0].ingredients[0].name)
        assertEquals("Misturar", recipes[0].sections[0].steps[0].text)
    }

    @Test
    fun `corrupted backup throws exception`() {
        try {
            codec.decode("{invalid json")
        } catch (_: Exception) {
            return
        }
        throw AssertionError("Expected exception for corrupted backup")
    }

    @Test
    fun `newer backup version throws exception`() {
        val json = """{"version": 99, "recipes": []}"""
        try {
            codec.decode(json)
        } catch (e: IllegalStateException) {
            assertTrue(e.message!!.contains("newer"))
            return
        }
        throw AssertionError("Expected exception for newer backup version")
    }

    @Test
    fun `delete old backups removes excess files`() =
        runTest {
            fakeCloudStorage.addBackup("old1", "{}")
            fakeCloudStorage.addBackup("old2", "{}")
            fakeCloudStorage.addBackup("old3", "{}")
            fakeCloudStorage.addBackup("latest", "{}")

            fakeCloudStorage.deleteOldBackups(maxBackupCount = 2)

            val remaining = fakeCloudStorage.listBackups()
            assertEquals(2, remaining.size)
        }
}

// ── Fake implementations for testing ──────────────────────────────

class FakeCloudBackupStorage : CloudBackupStorage {
    var shouldThrow = false
    var errorMessage: String? = null
    var uploadedContent: String = ""

    private val files = mutableListOf<FakeDriveFile>()

    data class FakeDriveFile(
        val id: String,
        val name: String,
        val content: String,
        val recipeCount: Int? = null,
        val backupVersion: Int? = null,
    )

    fun addBackup(
        id: String,
        content: String,
        recipeCount: Int? = null,
        backupVersion: Int? = null,
    ) {
        files.add(
            FakeDriveFile(
                id = id,
                name = "pratoo-backup-$id.json",
                content = content,
                recipeCount = recipeCount,
                backupVersion = backupVersion,
            ),
        )
    }

    override suspend fun uploadLatestBackup(
        content: String,
        metadata: BackupMetadata,
    ) {
        if (shouldThrow) {
            throw IllegalStateException(errorMessage ?: "Upload failed")
        }
        uploadedContent = content
        files.add(
            FakeDriveFile(
                id = "latest",
                name = "pratoo-backup-latest.json",
                content = content,
                recipeCount = metadata.recipeCount,
                backupVersion = metadata.backupVersion,
            ),
        )
    }

    override suspend fun listBackups(): List<CloudBackupFile> =
        files.mapIndexed { index, file ->
            CloudBackupFile(
                id = file.id,
                name = file.name,
                modifiedAt = System.currentTimeMillis() - index * 1000L,
                recipeCount = file.recipeCount,
                backupVersion = file.backupVersion,
            )
        }

    override suspend fun downloadBackup(fileId: String): String {
        if (shouldThrow) throw IllegalStateException(errorMessage ?: "Download failed")
        return files.firstOrNull { it.id == fileId }?.content
            ?: throw IllegalStateException("File not found: $fileId")
    }

    override suspend fun deleteOldBackups(maxBackupCount: Int) {
        val sorted = files.sortedByDescending { it.id }
        if (sorted.size > maxBackupCount) {
            val toRemove = sorted.drop(maxBackupCount).map { it.id }.toSet()
            files.removeAll { it.id in toRemove }
        }
    }
}

class FakeBackupSettingsRepository : BackupSettingsRepository {
    private val _settingsFlow =
        MutableStateFlow(
            com.diaszano.pratoo.backup.domain.model
                .BackupSettings(),
        )

    var lastStatus: BackupStatus? = null
    var lastSuccessfulBackupAt: Long? = null

    override fun observeBackupSettings(): Flow<com.diaszano.pratoo.backup.domain.model.BackupSettings> = _settingsFlow

    override suspend fun setAutomaticBackupEnabled(enabled: Boolean) {
        _settingsFlow.value = _settingsFlow.value.copy(automaticDriveBackupEnabled = enabled)
    }

    override suspend fun setLastSuccessfulBackupAt(timestamp: Long) {
        lastSuccessfulBackupAt = timestamp
        _settingsFlow.value =
            _settingsFlow.value.copy(
                lastSuccessfulBackupAt = timestamp,
                lastBackupStatus = BackupStatus.Success,
            )
    }

    override suspend fun setLastBackupAttemptAt(timestamp: Long) {
        _settingsFlow.value = _settingsFlow.value.copy(lastBackupAttemptAt = timestamp)
    }

    override suspend fun setLastBackupStatus(status: BackupStatus) {
        lastStatus = status
        _settingsFlow.value = _settingsFlow.value.copy(lastBackupStatus = status)
    }

    override suspend fun setSelectedGoogleAccountEmail(email: String?) {
        _settingsFlow.value = _settingsFlow.value.copy(selectedGoogleAccountEmail = email)
    }

    override suspend fun clearSelectedGoogleAccount() {
        _settingsFlow.value =
            com.diaszano.pratoo.backup.domain.model
                .BackupSettings()
    }
}

class FakeRecipeBackupExporter(
    private val repository: FakeRecipeRepository,
    private val codec: JsonRecipeBackupCodec,
) : RecipeBackupExporter {
    override suspend fun exportBackup(): Pair<String, BackupMetadata> {
        val recipes = repository.getAllRecipes()
        val metadata =
            BackupMetadata(
                backupVersion = JsonRecipeBackupCodec.BACKUP_VERSION,
                exportedAt = System.currentTimeMillis(),
                appVersionName = "0.1.0-test",
                appVersionCode = 1,
                recipeCount = recipes.size,
            )
        val json = codec.encodeWithMetadata(recipes, "0.1.0-test", 1, null)
        return Pair(json, metadata)
    }
}

class FakeRecipeBackupImporter(
    private val repository: FakeRecipeRepository,
    private val codec: JsonRecipeBackupCodec,
) : com.diaszano.pratoo.backup.domain.port.RecipeBackupImporter {
    override suspend fun importBackup(
        jsonContent: String,
        mode: RestoreMode,
    ) {
        val recipes = codec.decode(jsonContent)
        when (mode) {
            RestoreMode.ReplaceLocalData -> {
                repository.deleteAllRecipes()
                for (recipe in recipes) {
                    repository.saveRecipe(recipe)
                }
            }
            RestoreMode.Merge -> {
                for (recipe in recipes) {
                    repository.saveRecipe(recipe)
                }
            }
        }
    }
}
