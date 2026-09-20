package com.skillbuilder.app.data.local

import android.content.Context
import android.content.SharedPreferences
import com.skillbuilder.app.auth.GoogleUserData
import com.skillbuilder.app.domain.model.User
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

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
    fun loginWithGoogle(googleUser: GoogleUserData): Result<User> {
        val normalizedEmail = googleUser.email.trim().lowercase()
        val accounts = getRegisteredAccounts().toMutableList()
        val index = accounts.indexOfFirst { it.email.trim().lowercase() == normalizedEmail }

        if (index < 0) {
            return Result.failure(
                IllegalArgumentException("No registered account found for $normalizedEmail. Please register first as a Mentor or Learner.")
            )
        }

        val existing = accounts[index]
        val updated = existing.copy(
            avatarUrl = googleUser.profilePictureUri ?: existing.avatarUrl,
            isGoogleUser = true
        )
        accounts[index] = updated
        saveAccounts(accounts)
        val user = updated.toUser()
        setActiveUser(user)
        return Result.success(user)
    }

    @Synchronized
    fun registerWithGoogle(googleUser: GoogleUserData, defaultIsMentor: Boolean = false): Result<User> {
        val normalizedEmail = googleUser.email.trim().lowercase()
        val accounts = getRegisteredAccounts().toMutableList()
        val index = accounts.indexOfFirst { it.email.trim().lowercase() == normalizedEmail }

        val displayName = when {
            !googleUser.displayName.isNullOrBlank() -> googleUser.displayName
            !googleUser.givenName.isNullOrBlank() && !googleUser.familyName.isNullOrBlank() -> "${googleUser.givenName} ${googleUser.familyName}"
            else -> normalizedEmail.substringBefore("@")
                .split(".", "_", "-")
                .joinToString(" ") { part -> part.replaceFirstChar { it.uppercase() } }
        }

        val userAccount = if (index >= 0) {
            val existing = accounts[index]
            existing.copy(
                name = if (existing.name.isNotBlank()) existing.name else displayName,
                avatarUrl = googleUser.profilePictureUri ?: existing.avatarUrl,
                isGoogleUser = true
            ).also { accounts[index] = it }
        } else {
            UserAccount(
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
        }

        saveAccounts(accounts)
        val user = userAccount.toUser()
        setActiveUser(user)
        return Result.success(user)
    }

    @Synchronized
    fun loginOrRegisterGoogleUser(googleUser: GoogleUserData, defaultIsMentor: Boolean = false): User {
        return registerWithGoogle(googleUser, defaultIsMentor).getOrThrow()
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
