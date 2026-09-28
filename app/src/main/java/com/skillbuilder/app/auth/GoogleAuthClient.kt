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

        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(webClientId)
            .setAutoSelectEnabled(false)
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
            val msg = e.message ?: ""
            if (msg.contains("cancel", ignoreCase = true) || msg.contains("28433", ignoreCase = true)) {
                GoogleAuthResult.Canceled
            } else {
                GoogleAuthResult.Failure("Google OAuth Sign-In: $msg", e)
            }
        } catch (e: Exception) {
            val msg = e.message ?: "Unexpected authentication error"
            if (msg.contains("cancel", ignoreCase = true)) {
                GoogleAuthResult.Canceled
            } else {
                GoogleAuthResult.Failure("Authentication error: $msg", e)
            }
        }
    }

    private fun handleCredentialResponse(response: GetCredentialResponse): GoogleAuthResult {
        val credential = response.credential

        return if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            try {
                val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data)

                var email = googleIdToken.id
                var displayName = googleIdToken.displayName
                var givenName = googleIdToken.givenName
                var familyName = googleIdToken.familyName
                var profilePictureUri = googleIdToken.profilePictureUri?.toString()

                // If googleIdToken.id doesn't look like an email (e.g. numeric subject ID),
                // extract claims directly from the JWT ID Token payload:
                if (!email.contains("@") && googleIdToken.idToken.isNotBlank()) {
                    val jwtClaims = decodeJwtPayload(googleIdToken.idToken)
                    jwtClaims["email"]?.let { if (it.isNotBlank()) email = it }
                    if (displayName.isNullOrBlank()) {
                        jwtClaims["name"]?.let { displayName = it }
                    }
                    if (givenName.isNullOrBlank()) {
                        jwtClaims["given_name"]?.let { givenName = it }
                    }
                    if (familyName.isNullOrBlank()) {
                        jwtClaims["family_name"]?.let { familyName = it }
                    }
                    if (profilePictureUri.isNullOrBlank()) {
                        jwtClaims["picture"]?.let { profilePictureUri = it }
                    }
                }

                if (displayName.isNullOrBlank()) {
                    displayName = when {
                        !givenName.isNullOrBlank() && !familyName.isNullOrBlank() -> "$givenName $familyName"
                        !givenName.isNullOrBlank() -> givenName
                        else -> email.substringBefore("@")
                            .split(".", "_", "-")
                            .joinToString(" ") { part -> part.replaceFirstChar { char -> char.uppercase() } }
                    }
                }

                GoogleAuthResult.Success(
                    GoogleUserData(
                        idToken = googleIdToken.idToken,
                        email = email,
                        displayName = displayName,
                        givenName = givenName,
                        familyName = familyName,
                        profilePictureUri = profilePictureUri
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
     * Decodes the payload portion of a JWT without requiring external cryptography libraries.
     */
    private fun decodeJwtPayload(jwt: String): Map<String, String> {
        return try {
            val parts = jwt.split(".")
            if (parts.size >= 2) {
                val decodedBytes = android.util.Base64.decode(
                    parts[1],
                    android.util.Base64.URL_SAFE or android.util.Base64.NO_PADDING or android.util.Base64.NO_WRAP
                )
                val json = org.json.JSONObject(String(decodedBytes, Charsets.UTF_8))
                val map = mutableMapOf<String, String>()
                val keys = json.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    map[key] = json.optString(key)
                }
                map
            } else emptyMap()
        } catch (_: Exception) {
            emptyMap()
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
}
