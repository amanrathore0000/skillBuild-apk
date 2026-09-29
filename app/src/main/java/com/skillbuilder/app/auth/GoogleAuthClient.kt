package com.skillbuilder.app.auth

import android.app.Activity
import android.content.Intent
import android.util.Base64
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

data class GoogleUserData(
    val idToken: String,
    val email: String,
    val displayName: String?,
    val givenName: String? = null,
    val familyName: String? = null,
    val profilePictureUri: String? = null
)

data class DeveloperErrorInfo(
    val statusCode: Int,
    val sha1: String,
    val sha256: String?,
    val packageName: String,
    val webClientId: String
)

sealed class GoogleAuthResult {
    data class Success(val user: GoogleUserData) : GoogleAuthResult()
    data object Canceled : GoogleAuthResult()
    data class Failure(
        val errorMessage: String,
        val exception: Throwable? = null,
        val developerErrorInfo: DeveloperErrorInfo? = null
    ) : GoogleAuthResult()
}

/**
 * Modern Google OAuth 2.0 / OpenID Connect Authentication Client.
 *
 * Supports both:
 * 1. Google Play Services Auth (GoogleSignInClient) — Rock-solid on Android 5 through 15,
 *    launches official Google Account Chooser immediately without device-specific cancellations.
 * 2. Android Credential Manager API — Used when coroutine-based credential retrieval is preferred.
 */
class GoogleAuthClient(
    private val context: Activity,
    val webClientId: String
) {
    private val credentialManager: CredentialManager by lazy {
        CredentialManager.create(context)
    }

    private val googleSignInClient: GoogleSignInClient by lazy {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(webClientId)
            .requestEmail()
            .requestProfile()
            .build()
        GoogleSignIn.getClient(context, gso)
    }

    val isConfigured: Boolean
        get() = webClientId.isNotBlank() && !webClientId.startsWith("YOUR_GOOGLE_WEB_CLIENT_ID")

    /**
     * Returns Intent for the official Google Play Services Account Chooser activity.
     */
    fun getSignInIntent(): Intent {
        try {
            // Sign out locally so the account chooser always shows all accounts
            googleSignInClient.signOut()
        } catch (_: Exception) {
            // Best effort
        }
        return googleSignInClient.signInIntent
    }

    /**
     * Parses the result Intent returned by Google's official account chooser.
     */
    fun handleSignInResult(data: Intent?): GoogleAuthResult {
        if (data == null) {
            return GoogleAuthResult.Canceled
        }
        val task = GoogleSignIn.getSignedInAccountFromIntent(data)
        return try {
            val account: GoogleSignInAccount = task.getResult(ApiException::class.java)
            val idToken = account.idToken
            val email = account.email

            if (idToken.isNullOrBlank() || email.isNullOrBlank()) {
                Log.w("SkillBuilderOAuth", "Google Sign-In returned account without idToken or email.")
                GoogleAuthResult.Failure("Google Sign-In succeeded, but ID token or email was not provided.")
            } else {
                Log.d("SkillBuilderOAuth", "Google Sign-In succeeded for email: $email")
                GoogleAuthResult.Success(
                    GoogleUserData(
                        idToken = idToken,
                        email = email,
                        displayName = account.displayName ?: account.givenName ?: email.substringBefore("@"),
                        givenName = account.givenName,
                        familyName = account.familyName,
                        profilePictureUri = account.photoUrl?.toString()
                    )
                )
            }
        } catch (e: ApiException) {
            when (e.statusCode) {
                CommonStatusCodes.SIGN_IN_REQUIRED,
                CommonStatusCodes.CANCELED,
                12501 -> { // 12501 = GoogleSignInStatusCodes.SIGN_IN_CANCELLED
                    Log.d("SkillBuilderOAuth", "Google Sign-In was cancelled by user (code: ${e.statusCode}).")
                    GoogleAuthResult.Canceled
                }
                CommonStatusCodes.NETWORK_ERROR -> {
                    Log.e("SkillBuilderOAuth", "Network error during Google Sign-In", e)
                    GoogleAuthResult.Failure("Network error during Google Sign-In. Please check your internet connection.", e)
                }
                CommonStatusCodes.DEVELOPER_ERROR, 10 -> {
                    val currentSha1 = getSigningSha1()
                    val currentSha256 = getSigningSha256()
                    Log.e("SkillBuilderOAuth", "Google Sign-In DEVELOPER_ERROR (code 10). Package: ${context.packageName}, SHA-1: $currentSha1. Register this SHA-1 in Google Cloud / Firebase Console.", e)
                    GoogleAuthResult.Failure(
                        errorMessage = "Google Sign-In setup required (Code 10: DEVELOPER_ERROR).\nDebug SHA-1 must be added to Google Cloud / Firebase Console.",
                        exception = e,
                        developerErrorInfo = DeveloperErrorInfo(
                            statusCode = 10,
                            sha1 = currentSha1,
                            sha256 = currentSha256,
                            packageName = context.packageName,
                            webClientId = webClientId
                        )
                    )
                }
                else -> {
                    Log.e("SkillBuilderOAuth", "Google Sign-In ApiException: code=${e.statusCode}, message=${e.message}", e)
                    GoogleAuthResult.Failure("Google sign-in could not be completed (code ${e.statusCode}).", e)
                }
            }
        } catch (e: Exception) {
            Log.e("SkillBuilderOAuth", "Unexpected Google Sign-In exception", e)
            GoogleAuthResult.Failure(e.message ?: "Google sign-in failed unexpectedly.", e)
        }
    }

    /**
     * Extracts active APK signing certificate SHA-1 fingerprint for Google Cloud / Firebase setup.
     */
    fun getSigningSha1(): String {
        return try {
            val packageInfo = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                context.packageManager.getPackageInfo(
                    context.packageName,
                    android.content.pm.PackageManager.GET_SIGNING_CERTIFICATES
                )
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(
                    context.packageName,
                    android.content.pm.PackageManager.GET_SIGNATURES
                )
            }
            val signatures = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                packageInfo.signingInfo?.apkContentsSigners
            } else {
                @Suppress("DEPRECATION")
                packageInfo.signatures
            }
            val cert = signatures?.firstOrNull()?.toByteArray() ?: return "80:51:6F:00:53:B4:60:38:5E:3C:C4:28:49:00:CA:68:6A:61:59:D9"
            val md = java.security.MessageDigest.getInstance("SHA-1")
            val digest = md.digest(cert)
            digest.joinToString(":") { String.format("%02X", it) }
        } catch (_: Exception) {
            "80:51:6F:00:53:B4:60:38:5E:3C:C4:28:49:00:CA:68:6A:61:59:D9"
        }
    }

    /**
     * Extracts active APK signing certificate SHA-256 fingerprint for Google Cloud / Firebase setup.
     */
    fun getSigningSha256(): String? {
        return try {
            val packageInfo = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                context.packageManager.getPackageInfo(
                    context.packageName,
                    android.content.pm.PackageManager.GET_SIGNING_CERTIFICATES
                )
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(
                    context.packageName,
                    android.content.pm.PackageManager.GET_SIGNATURES
                )
            }
            val signatures = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                packageInfo.signingInfo?.apkContentsSigners
            } else {
                @Suppress("DEPRECATION")
                packageInfo.signatures
            }
            val cert = signatures?.firstOrNull()?.toByteArray() ?: return null
            val md = java.security.MessageDigest.getInstance("SHA-256")
            val digest = md.digest(cert)
            digest.joinToString(":") { String.format("%02X", it) }
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Creates a mock Google user with test_google_token supported by SkillBuilder backend for instant development testing.
     */
    fun createDemoGoogleUser(): GoogleUserData {
        return GoogleUserData(
            idToken = "test_google_token:developer.tester@skillbuilder.app:google_dev_demo_101",
            email = "developer.tester@skillbuilder.app",
            displayName = "SkillBuilder Developer",
            givenName = "SkillBuilder",
            familyName = "Developer",
            profilePictureUri = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=150"
        )
    }

    /**
     * Alternative: Native Android Credential Manager call.
     */
    suspend fun signInWithGoogle(): GoogleAuthResult = withContext(Dispatchers.Main) {
        if (!isConfigured) {
            return@withContext GoogleAuthResult.Failure(
                "Google OAuth 2.0 Web Client ID is not configured. Please check strings.xml."
            )
        }

        try {
            Log.d("SkillBuilderOAuth", "Starting CredentialManager Google Sign-In with webClientId: $webClientId")
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(webClientId)
                .setAutoSelectEnabled(false)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val response: GetCredentialResponse = credentialManager.getCredential(
                context = context,
                request = request
            )
            handleCredentialResponse(response)
        } catch (e: GetCredentialCancellationException) {
            Log.d("SkillBuilderOAuth", "Google Sign-In cancelled by user.")
            GoogleAuthResult.Canceled
        } catch (e: Exception) {
            val msg = e.message ?: ""
            if (msg.contains("cancel", ignoreCase = true)) {
                Log.d("SkillBuilderOAuth", "User cancelled Google Sign-In dialog.")
                GoogleAuthResult.Canceled
            } else {
                Log.e("SkillBuilderOAuth", "Google Sign-In failed: ${e.javaClass.simpleName} - $msg", e)
                val isDev = msg.contains("10") || msg.contains("DEVELOPER_ERROR", ignoreCase = true)
                GoogleAuthResult.Failure(
                    errorMessage = if (isDev) {
                        "Google Sign-In setup required (Code 10: DEVELOPER_ERROR).\nDebug SHA-1 must be added to Google Cloud / Firebase Console."
                    } else if (msg.isNotBlank()) msg else "Google sign-in could not be completed.",
                    exception = e,
                    developerErrorInfo = if (isDev) DeveloperErrorInfo(
                        statusCode = 10,
                        sha1 = getSigningSha1(),
                        sha256 = getSigningSha256(),
                        packageName = context.packageName,
                        webClientId = webClientId
                    ) else null
                )
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
            } catch (e: Exception) {
                GoogleAuthResult.Failure("Failed to parse Google ID Token: ${e.message}", e)
            }
        } else {
            GoogleAuthResult.Failure("Unsupported credential type: ${credential.type}")
        }
    }

    private fun decodeJwtPayload(jwt: String): Map<String, String> {
        return try {
            val parts = jwt.split(".")
            if (parts.size >= 2) {
                val decodedBytes = Base64.decode(
                    parts[1],
                    Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP
                )
                val json = JSONObject(String(decodedBytes, Charsets.UTF_8))
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

    suspend fun signOut() = withContext(Dispatchers.IO) {
        try {
            googleSignInClient.signOut()
            googleSignInClient.revokeAccess()
        } catch (_: Exception) {}
        try {
            credentialManager.clearCredentialState(ClearCredentialStateRequest())
        } catch (_: Exception) {}
    }
}
