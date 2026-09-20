package com.skillbuilder.app.auth

import android.app.Activity
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.security.MessageDigest
import java.util.UUID

data class GoogleUserData(
    val idToken: String,
    val email: String,
    val displayName: String?,
    val givenName: String? = null,
    val familyName: String? = null,
    val profilePictureUri: String? = null
)

sealed class GoogleAuthResult {
    data class Success(val user: GoogleUserData) : GoogleAuthResult()
    data object Canceled : GoogleAuthResult()
    data class Failure(val errorMessage: String, val exception: Throwable? = null) : GoogleAuthResult()
}

/**
 * Modern Google OAuth 2.0 / OpenID Connect Authentication Client
 * Powered by the Android Credential Manager API and Google Identity Services.
 */
class GoogleAuthClient(
    private val context: Activity,
    private val webClientId: String
) {
    private val credentialManager: CredentialManager by lazy {
        CredentialManager.create(context)
    }

    /**
     * Checks if a real Web Client ID has been configured in strings.xml
     */
    val isConfigured: Boolean
        get() = webClientId.isNotBlank() && !webClientId.startsWith("YOUR_GOOGLE_WEB_CLIENT_ID")

    /**
     * Triggers the native Android 1-tap Google Account Chooser bottom sheet.
     */
    suspend fun signInWithGoogle(): GoogleAuthResult = withContext(Dispatchers.IO) {
        if (!isConfigured) {
            return@withContext GoogleAuthResult.Failure(
                "Google OAuth 2.0 Web Client ID is not configured yet. Please add your Web Client ID from Google Cloud Console to strings.xml."
            )
        }

        // Generate cryptographic nonce for OAuth 2.0 OpenID Connect security
        val rawNonce = UUID.randomUUID().toString()
        val hashedNonce = hashNonce(rawNonce)

        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(webClientId)
            .setAutoSelectEnabled(true)
            .setNonce(hashedNonce)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        try {
            val response: GetCredentialResponse = credentialManager.getCredential(
                context = context,
                request = request
            )
            handleCredentialResponse(response)
        } catch (e: GetCredentialCancellationException) {
            GoogleAuthResult.Canceled
        } catch (e: GetCredentialException) {
            GoogleAuthResult.Failure("Google OAuth Sign-In failed: ${e.message}", e)
        } catch (e: Exception) {
            GoogleAuthResult.Failure("Unexpected authentication error: ${e.message}", e)
        }
    }

    private fun handleCredentialResponse(response: GetCredentialResponse): GoogleAuthResult {
        val credential = response.credential

        return if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            try {
                val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data)
                GoogleAuthResult.Success(
                    GoogleUserData(
                        idToken = googleIdToken.idToken,
                        email = googleIdToken.id,
                        displayName = googleIdToken.displayName,
                        givenName = googleIdToken.givenName,
                        familyName = googleIdToken.familyName,
                        profilePictureUri = googleIdToken.profilePictureUri?.toString()
                    )
                )
            } catch (e: GoogleIdTokenParsingException) {
                GoogleAuthResult.Failure("Failed to parse Google ID Token: ${e.message}", e)
            }
        } else {
            GoogleAuthResult.Failure("Unsupported credential type returned: ${credential.type}")
        }
    }

    /**
     * Clears local credential state on logout.
     */
    suspend fun signOut() = withContext(Dispatchers.IO) {
        try {
            credentialManager.clearCredentialState(ClearCredentialStateRequest())
        } catch (_: Exception) {
            // Best effort clear
        }
    }

    private fun hashNonce(rawNonce: String): String {
        val bytes = rawNonce.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.fold("") { str, it -> str + "%02x".format(it) }
    }
}
