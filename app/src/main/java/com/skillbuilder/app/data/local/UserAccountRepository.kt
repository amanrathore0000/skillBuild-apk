package com.skillbuilder.app.data.local

import android.content.Context
import android.content.SharedPreferences
import com.skillbuilder.app.auth.GoogleUserData
import com.skillbuilder.app.domain.model.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class UserAccountRepository(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("skill_builder_users", Context.MODE_PRIVATE)
    private val json = Json { ignoreUnknownKeys = true; prettyPrint = false }

    companion object {
        private const val KEY_ACCOUNTS = "registered_accounts_json"
        private const val KEY_ACTIVE_USER_ID = "active_user_id"

        @Volatile
        private var INSTANCE: UserAccountRepository? = null

        fun getInstance(context: Context): UserAccountRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: UserAccountRepository(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    init {
        // Seed default demo registered accounts if accounts list is empty,
        // allowing immediate out-of-the-box testing for both Learner and Mentor roles.
        val existing = getRegisteredAccounts()
        if (existing.isEmpty()) {
            val demoAccounts = listOf(
                UserAccount(
                    id = "mentor@skillbuilder.app",
                    email = "mentor@skillbuilder.app",
                    password = "password123",
                    name = "Alex Vance (Mentor)",
                    phone = "+1 555-0192",
                    dob = "14/08/1990",
                    location = "San Francisco, USA",
                    skills = listOf("Jetpack Compose", "Kotlin Coroutines", "Cloud Architecture"),
                    skillsWanted = listOf("Artisan Sourdough", "Acoustic Guitar"),
                    isMentor = true,
                    isGoogleUser = false
                ),
                UserAccount(
                    id = "learner@skillbuilder.app",
                    email = "learner@skillbuilder.app",
                    password = "password123",
                    name = "Jordan Lee (Learner)",
                    phone = "+1 555-0144",
                    dob = "22/03/1996",
                    location = "Austin, USA",
                    skills = listOf("Python Basics", "Graphic Design"),
                    skillsWanted = listOf("Android Dev", "Jetpack Compose"),
                    isMentor = false,
                    isGoogleUser = false
                )
            )
            saveAccounts(demoAccounts)
        }
    }

    @Synchronized
    fun getRegisteredAccounts(): List<UserAccount> {
        val raw = prefs.getString(KEY_ACCOUNTS, null) ?: return emptyList()
        return try {
            json.decodeFromString(raw)
        } catch (e: Exception) {
            emptyList()
        }
    }

    @Synchronized
    private fun saveAccounts(accounts: List<UserAccount>) {
        val raw = json.encodeToString(accounts)
        prefs.edit().putString(KEY_ACCOUNTS, raw).apply()
    }

    @Synchronized
    fun isEmailRegistered(email: String): Boolean {
        val normalized = email.trim().lowercase()
        return getRegisteredAccounts().any { it.email.trim().lowercase() == normalized }
    }

    @Synchronized
    fun getAccountByEmail(email: String): UserAccount? {
        val normalized = email.trim().lowercase()
        return getRegisteredAccounts().find { it.email.trim().lowercase() == normalized }
    }

    @Synchronized
    fun updatePassword(email: String, newPassword: String): Result<Boolean> {
        val normalized = email.trim().lowercase()
        val accounts = getRegisteredAccounts().toMutableList()
        val index = accounts.indexOfFirst { it.email.trim().lowercase() == normalized }
        if (index < 0) {
            return Result.failure(IllegalArgumentException("No registered account found for $normalized."))
        }
        val existing = accounts[index]
        accounts[index] = existing.copy(password = newPassword)
        saveAccounts(accounts)
        return Result.success(true)
    }

    @Synchronized
    fun registerUser(account: UserAccount): Result<User> {
        val accounts = getRegisteredAccounts().toMutableList()
        val normalizedEmail = account.email.trim().lowercase()

        if (accounts.any { it.email.trim().lowercase() == normalizedEmail }) {
            return Result.failure(IllegalArgumentException("An account with this email ($normalizedEmail) is already registered. Please log in."))
        }

        val cleanAccount = account.copy(email = normalizedEmail)
        accounts.add(cleanAccount)
        saveAccounts(accounts)
        setActiveUser(cleanAccount.toUser())
        return Result.success(cleanAccount.toUser())
    }

    @Synchronized
    fun loginWithEmail(email: String, password: String): Result<User> {
        val normalizedEmail = email.trim().lowercase()
        val accounts = getRegisteredAccounts()
        val matched = accounts.find { it.email.trim().lowercase() == normalizedEmail }

        if (matched == null) {
            return Result.failure(IllegalArgumentException("No account found for $normalizedEmail. Please register first."))
        }

        if (matched.isGoogleUser && matched.password.isBlank()) {
            return Result.failure(IllegalArgumentException("This account was registered via Google Sign-In. Please tap 'Continue with Google' to log in."))
        }

        if (matched.password != password) {
            return Result.failure(IllegalArgumentException("Incorrect password for $normalizedEmail."))
        }

        val user = matched.toUser()
        setActiveUser(user)
        return Result.success(user)
    }

    @Synchronized
    fun loginWithGoogle(googleUser: GoogleUserData, defaultIsMentor: Boolean = false): Result<User> {
        val normalizedEmail = googleUser.email.trim().lowercase()
        val accounts = getRegisteredAccounts().toMutableList()
        val index = accounts.indexOfFirst { it.email.trim().lowercase() == normalizedEmail }

        if (index < 0) {
            // User tapped Google login for the first time: seamlessly create account and log in
            return registerWithGoogle(googleUser, defaultIsMentor)
        }

        val existing = accounts[index]
        val updated = existing.copy(
            avatarUrl = googleUser.profilePictureUri ?: existing.avatarUrl,
            name = if (existing.name.isNotBlank()) existing.name else (googleUser.displayName ?: existing.name),
            isGoogleUser = true
        )
        accounts[index] = updated
        saveAccounts(accounts)
        val user = updated.toUser()
        setActiveUser(user)
        syncGoogleUserAsync(googleUser)
        return Result.success(user)
    }

    @Synchronized
    fun registerWithGoogle(googleUser: GoogleUserData, defaultIsMentor: Boolean = false): Result<User> {
        val normalizedEmail = googleUser.email.trim().lowercase()
        val accounts = getRegisteredAccounts().toMutableList()
        val index = accounts.indexOfFirst { it.email.trim().lowercase() == normalizedEmail }

        if (index >= 0) {
            // Already registered: log them in seamlessly and update role if specified
            val existing = accounts[index]
            val updated = existing.copy(
                avatarUrl = googleUser.profilePictureUri ?: existing.avatarUrl,
                isGoogleUser = true,
                isMentor = if (defaultIsMentor) true else existing.isMentor
            )
            accounts[index] = updated
            saveAccounts(accounts)
            val user = updated.toUser()
            setActiveUser(user)
            syncGoogleUserAsync(googleUser)
            return Result.success(user)
        }

        val displayName = when {
            !googleUser.displayName.isNullOrBlank() -> googleUser.displayName
            !googleUser.givenName.isNullOrBlank() && !googleUser.familyName.isNullOrBlank() -> "${googleUser.givenName} ${googleUser.familyName}"
            else -> normalizedEmail.substringBefore("@")
                .split(".", "_", "-")
                .joinToString(" ") { part -> part.replaceFirstChar { it.uppercase() } }
        }

        val userAccount = UserAccount(
            id = normalizedEmail,
            email = normalizedEmail,
            name = displayName,
            avatarUrl = googleUser.profilePictureUri,
            phone = "",
            dob = "",
            location = "",
            skills = emptyList(),
            isMentor = defaultIsMentor,
            isGoogleUser = true
        ).also { accounts.add(it) }

        saveAccounts(accounts)
        val user = userAccount.toUser()
        setActiveUser(user)
        syncGoogleUserAsync(googleUser)
        return Result.success(user)
    }

    private fun syncGoogleUserAsync(googleUser: GoogleUserData) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val client = OkHttpClient.Builder()
                    .connectTimeout(4, TimeUnit.SECONDS)
                    .build()

                val payload = JSONObject().apply {
                    put("idToken", googleUser.idToken.ifBlank { "google_sync_${googleUser.email}" })
                    put("email", googleUser.email)
                    put("name", googleUser.displayName ?: "")
                    if (!googleUser.profilePictureUri.isNullOrBlank()) {
                        put("picture", googleUser.profilePictureUri)
                    }
                }.toString().toRequestBody("application/json; charset=utf-8".toMediaType())

                val candidateUrls = listOf("http://localhost:5000", "http://192.168.29.198:5000", "http://10.0.2.2:5000")
                for (url in candidateUrls) {
                    try {
                        val req = Request.Builder().url("$url/api/v1/auth/google").post(payload).build()
                        val response = client.newCall(req).execute()
                        response.close()
                        break
                    } catch (_: Exception) {}
                }
            } catch (_: Exception) {}
        }
    }

    @Synchronized
    fun loginOrRegisterGoogleUser(googleUser: GoogleUserData, defaultIsMentor: Boolean = false): User {
        val result = loginWithGoogle(googleUser, defaultIsMentor)
        return result.getOrElse {
            val fallback = registerWithGoogle(googleUser, defaultIsMentor).getOrThrow()
            fallback
        }
    }

    @Synchronized
    fun updateUserProfile(updatedUser: User) {
        val normalizedEmail = updatedUser.email.trim().lowercase()
        val accounts = getRegisteredAccounts().toMutableList()
        val index = accounts.indexOfFirst { it.email.trim().lowercase() == normalizedEmail }

        if (index >= 0) {
            val existing = accounts[index]
            accounts[index] = existing.copy(
                name = updatedUser.name,
                phone = updatedUser.phone,
                dob = updatedUser.dob,
                location = updatedUser.location,
                avatarUrl = updatedUser.avatarUrl ?: existing.avatarUrl,
                skills = if (updatedUser.isMentor) updatedUser.skillsTaught else updatedUser.skillsWanted,
                skillsWanted = updatedUser.skillsWanted,
                isMentor = updatedUser.isMentor
            )
            saveAccounts(accounts)
        }
        setActiveUser(updatedUser)
    }

    @Synchronized
    fun getActiveUser(): User? {
        val activeId = prefs.getString(KEY_ACTIVE_USER_ID, null) ?: return null
        val accounts = getRegisteredAccounts()
        return accounts.find { it.email.equals(activeId, ignoreCase = true) || it.id == activeId }?.toUser()
    }

    @Synchronized
    fun setActiveUser(user: User?) {
        if (user == null) {
            prefs.edit().remove(KEY_ACTIVE_USER_ID).apply()
        } else {
            prefs.edit().putString(KEY_ACTIVE_USER_ID, user.email.trim().lowercase()).apply()
        }
    }

    @Synchronized
    fun logout() {
        setActiveUser(null)
    }
}
