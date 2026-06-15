package com.diaszano.pratoo.backup.adapter.out.cloud

import android.content.Context
import com.diaszano.pratoo.backup.domain.model.BackupMetadata
import com.diaszano.pratoo.backup.domain.model.CloudBackupFile
import com.diaszano.pratoo.backup.domain.port.CloudBackupStorage
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.Scope
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoogleDriveBackupStorage
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) : CloudBackupStorage {
        private val json = Json { ignoreUnknownKeys = true }

        private val client =
            OkHttpClient
                .Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build()

        private val driveBaseUrl = "https://www.googleapis.com/drive/v3"
        private val uploadBaseUrl = "https://www.googleapis.com/upload/drive/v3"

        companion object {
            private const val LATEST_FILE_NAME = "pratoo-backup-latest.json"
            private const val MIME_TYPE_JSON = "application/json"
            private const val APP_DATA_FOLDER = "appDataFolder"
            private const val MAX_BACKUP_COUNT = 5
        }

        private fun getAccessToken(): String {
            val signedInAccount =
                GoogleSignIn.getLastSignedInAccount(context)
                    ?: throw IllegalStateException("No signed in account")
            val androidAccount =
                signedInAccount.account
                    ?: throw IllegalStateException("No Android account found")
            val scope = Scope(DriveScopes.DRIVE_APPDATA)
            return com.google.android.gms.auth.GoogleAuthUtil.getToken(
                context,
                androidAccount,
                "oauth2:${scope.scopeUri}",
            )
        }

        private fun buildAuthRequest(url: String): Request.Builder =
            Request
                .Builder()
                .addHeader("Authorization", "Bearer ${getAccessToken()}")
                .url(url)

        override suspend fun uploadLatestBackup(
            content: String,
            metadata: BackupMetadata,
        ) {
            val fileId = findOrCreateLatestFile()
            if (fileId != null) {
                updateFileMetadata(fileId, metadata)
                updateFile(fileId, content)
            } else {
                createFile(content, LATEST_FILE_NAME, metadata)
            }
            deleteOldBackups(MAX_BACKUP_COUNT)
        }

        override suspend fun listBackups(): List<CloudBackupFile> {
            val query =
                "name contains 'pratoo-backup' and 'appDataFolder' in parents and trashed=false"
            val fields = "files(id,name,createdTime,modifiedTime,size,appProperties)"
            val url =
                "$driveBaseUrl/files?q=${java.net.URLEncoder.encode(
                    query,
                    "UTF-8",
                )}&fields=$fields&spaces=appDataFolder&orderBy=modifiedTime desc"

            val request = buildAuthRequest(url).get().build()
            val response = client.newCall(request).execute()

            if (!response.isSuccessful) {
                throw IllegalStateException("Drive API error: ${response.code} ${response.message}")
            }

            val body = response.body?.string() ?: "{}"
            val parsed = json.decodeFromString<DriveFileList>(body)

            return parsed.files.map { driveFile ->
                val recipeCount =
                    driveFile.appProperties?.get("recipeCount")?.toIntOrNull()
                val backupVersion =
                    driveFile.appProperties?.get("backupVersion")?.toIntOrNull()
                CloudBackupFile(
                    id = driveFile.id,
                    name = driveFile.name,
                    createdAt = parseRfc3339(driveFile.createdTime),
                    modifiedAt = parseRfc3339(driveFile.modifiedTime),
                    sizeBytes = driveFile.size?.toLongOrNull(),
                    recipeCount = recipeCount,
                    backupVersion = backupVersion,
                )
            }
        }

        override suspend fun downloadBackup(fileId: String): String {
            val url = "$driveBaseUrl/files/$fileId?alt=media"
            val request = buildAuthRequest(url).get().build()
            val response = client.newCall(request).execute()

            if (!response.isSuccessful) {
                throw IllegalStateException("Download error: ${response.code} ${response.message}")
            }

            return response.body?.string() ?: ""
        }

        override suspend fun deleteOldBackups(maxBackupCount: Int) {
            val allFiles = listBackups()
            if (allFiles.size <= maxBackupCount) return

            val toDelete = allFiles.drop(maxBackupCount)
            for (file in toDelete) {
                deleteFile(file.id)
            }
        }

        private fun findOrCreateLatestFile(): String? {
            val query =
                "name='$LATEST_FILE_NAME' and 'appDataFolder' in parents and trashed=false"
            val fields = "files(id)"
            val url =
                "$driveBaseUrl/files?q=${java.net.URLEncoder.encode(query, "UTF-8")}&fields=$fields&spaces=appDataFolder"

            val request = buildAuthRequest(url).get().build()
            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: "{}"
            val parsed = json.decodeFromString<DriveFileList>(body)
            return parsed.files.firstOrNull()?.id
        }

        private fun createFile(
            content: String,
            fileName: String,
            metadata: BackupMetadata? = null,
        ) {
            val metadataUrl = "$uploadBaseUrl/files?uploadType=multipart&fields=id"
            val appPropsJson =
                if (metadata != null) {
                    buildJsonObject {
                        put("backupVersion", metadata.backupVersion.toString())
                        put("recipeCount", metadata.recipeCount.toString())
                        metadata.appVersionName?.let { put("appVersionName", it) }
                        metadata.appVersionCode?.let { put("appVersionCode", it.toString()) }
                    }.toString()
                } else {
                    null
                }
            val appPropsField =
                if (appPropsJson != null) {
                    ""","appProperties":$appPropsJson"""
                } else {
                    ""
                }
            val jsonMetadata =
                """{"name":"$fileName","parents":["$APP_DATA_FOLDER"],"mimeType":"$MIME_TYPE_JSON"$appPropsField}"""
                    .toRequestBody("application/json; charset=utf-8".toMediaType())

            val fileContent = content.toRequestBody("$MIME_TYPE_JSON; charset=utf-8".toMediaType())

            val body =
                MultipartBody
                    .Builder()
                    .setType(MultipartBody.FORM)
                    .addPart(
                        jsonMetadata,
                    ).addPart(
                        fileContent,
                    ).build()

            val request =
                Request
                    .Builder()
                    .addHeader("Authorization", "Bearer ${getAccessToken()}")
                    .url(metadataUrl)
                    .post(body)
                    .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                throw IllegalStateException("Create file error: ${response.code} ${response.message}")
            }
        }

        private fun updateFile(
            fileId: String,
            content: String,
        ) {
            val url = "$uploadBaseUrl/files/$fileId?uploadType=media"
            val fileContent = content.toRequestBody("$MIME_TYPE_JSON; charset=utf-8".toMediaType())

            val request =
                Request
                    .Builder()
                    .addHeader("Authorization", "Bearer ${getAccessToken()}")
                    .url(url)
                    .patch(fileContent)
                    .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                throw IllegalStateException("Update file error: ${response.code} ${response.message}")
            }
        }

        private fun updateFileMetadata(
            fileId: String,
            metadata: BackupMetadata,
        ) {
            val url = "$driveBaseUrl/files/$fileId"
            val appPropsJson =
                buildJsonObject {
                    put("backupVersion", metadata.backupVersion.toString())
                    put("recipeCount", metadata.recipeCount.toString())
                    metadata.appVersionName?.let { put("appVersionName", it) }
                    metadata.appVersionCode?.let { put("appVersionCode", it.toString()) }
                }.toString()
            val jsonBody = """{"appProperties":$appPropsJson}"""
            val request =
                Request
                    .Builder()
                    .addHeader("Authorization", "Bearer ${getAccessToken()}")
                    .url(url)
                    .patch(jsonBody.toRequestBody("application/json; charset=utf-8".toMediaType()))
                    .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                throw IllegalStateException(
                    "Update file metadata error: ${response.code} ${response.message}",
                )
            }
        }

        private fun deleteFile(fileId: String) {
            val url = "$driveBaseUrl/files/$fileId"
            val request = buildAuthRequest(url).delete().build()
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                throw IllegalStateException("Delete file error: ${response.code} ${response.message}")
            }
        }

        private fun parseRfc3339(rfc3339: String?): Long? {
            if (rfc3339 == null) return null
            return try {
                val formats =
                    listOf(
                        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US),
                        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US),
                    )
                for (fmt in formats) {
                    fmt.timeZone = TimeZone.getTimeZone("UTC")
                    try {
                        return fmt.parse(rfc3339)?.time
                    } catch (_: Exception) {
                    }
                }
                null
            } catch (_: Exception) {
                null
            }
        }

        @Serializable
        private data class DriveFileList(
            val files: List<DriveFile> = emptyList(),
        )

        @Serializable
        private data class DriveFile(
            val id: String,
            val name: String,
            val createdTime: String? = null,
            val modifiedTime: String? = null,
            val size: String? = null,
            val appProperties: Map<String, String>? = null,
        )
    }
