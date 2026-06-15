package com.diaszano.pratoo.backup.adapter.out.cloud

import android.content.Context
import android.content.Intent
import com.diaszano.pratoo.backup.domain.model.CloudBackupError
import com.diaszano.pratoo.backup.domain.port.BackupSettingsRepository
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoogleDriveAuthorizationManager
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
        private val settingsRepository: BackupSettingsRepository,
    ) {
        private val googleSignInClient: GoogleSignInClient by lazy {
            val gso =
                GoogleSignInOptions
                    .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestEmail()
                    .requestScopes(Scope(DriveScopes.DRIVE_APPDATA))
                    .build()
            GoogleSignIn.getClient(context, gso)
        }

        fun getSignInIntent(): Intent = googleSignInClient.signInIntent

        fun getLastSignedInAccount(): GoogleSignInAccount? = GoogleSignIn.getLastSignedInAccount(context)

        suspend fun handleSignInResult(
            data: Intent?,
            onSuccess: (GoogleSignInAccount) -> Unit,
            onError: (CloudBackupError) -> Unit,
        ) {
            try {
                val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                val account = task.getResult(ApiException::class.java)
                settingsRepository.setSelectedGoogleAccountEmail(account.email)
                onSuccess(account)
            } catch (e: ApiException) {
                when (e.statusCode) {
                    12501 -> onError(CloudBackupError.PermissionRequired) // cancelled by user
                    12500 -> onError(CloudBackupError.DriveApiError) // sign in failed
                    else ->
                        onError(
                            CloudBackupError.Unknown(
                                message = "Sign in failed: ${e.localizedMessage}",
                                cause = e,
                            ),
                        )
                }
            }
        }

        fun isSignedIn(): Boolean = getLastSignedInAccount() != null

        suspend fun signOut() {
            settingsRepository.clearSelectedGoogleAccount()
            googleSignInClient.signOut()
        }

        fun revokeAccess() {
            googleSignInClient.revokeAccess()
        }
    }

internal object DriveScopes {
    const val DRIVE_APPDATA = "https://www.googleapis.com/auth/drive.appdata"
}
