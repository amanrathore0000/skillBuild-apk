package com.skillbuilder.app.data.local

import android.content.Context
import com.skillbuilder.app.auth.GoogleUserData
import com.skillbuilder.app.domain.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object UserSession {

    private val guestUser = User(
        id = "user_guest",
        name = "Guest Explorer",
        email = "guest@skillbuilder.app",
        avatarUrl = null,
        phone = "",
        dob = "",
        location = "Global",
        bio = "Exploring new skills and looking for reciprocal swap partners.",
        rating = 5.0f,
        reviewCount = 0,
        isVerified = false,
        skillsTaught = listOf("Introductory Coding", "Guitar"),
        skillsWanted = listOf("Baking", "Pottery"),
        isMentor = false
    )

    private val _currentUser = MutableStateFlow(guestUser)
    val currentUser: StateFlow<User> = _currentUser.asStateFlow()

    private const val ENROLLMENTS_PREF = "skillbuilder_enrollments"
    private const val KEY_ENROLLED_IDS = "enrolled_video_ids"
    private var enrollPrefs: android.content.SharedPreferences? = null

    // Tracks video IDs the learner has enrolled/paid for
    private val _enrolledVideoIds = MutableStateFlow<Set<String>>(emptySet())
    val enrolledVideoIds: StateFlow<Set<String>> = _enrolledVideoIds.asStateFlow()

    fun enrollInVideo(videoId: String) {
        val updated = _enrolledVideoIds.value + videoId
        _enrolledVideoIds.value = updated
        enrollPrefs?.edit()?.putStringSet(KEY_ENROLLED_IDS, updated)?.apply()
    }

    fun unenrollVideo(videoId: String) {
        val updated = _enrolledVideoIds.value - videoId
        _enrolledVideoIds.value = updated
        enrollPrefs?.edit()?.putStringSet(KEY_ENROLLED_IDS, updated)?.apply()
    }

    fun isEnrolledIn(videoId: String): Boolean = videoId in _enrolledVideoIds.value

    private var repository: UserAccountRepository? = null

    fun initialize(context: Context) {
        val repo = UserAccountRepository.getInstance(context)
        repository = repo
        val active = repo.getActiveUser()
        if (active != null) {
            _currentUser.value = active
        }
        enrollPrefs = context.getSharedPreferences(ENROLLMENTS_PREF, Context.MODE_PRIVATE)
        val saved = enrollPrefs?.getStringSet(KEY_ENROLLED_IDS, null)
        if (saved != null) {
            _enrolledVideoIds.value = saved
        }
    }

    fun isUserLoggedIn(): Boolean {
        return _currentUser.value.id != "user_guest"
    }

    fun setUser(user: User) {
        _currentUser.value = user
        repository?.setActiveUser(user)
    }

    /**
     * Authenticates an already-registered Google user during login.
     */
    fun loginWithGoogle(googleUser: GoogleUserData): Result<User> {
        val repo = repository ?: return Result.failure(IllegalStateException("Session not initialized"))
        val result = repo.loginWithGoogle(googleUser)
        result.onSuccess { user ->
            _currentUser.value = user
        }
        return result
    }

    /**
     * Registers a new Mentor or Learner using Google OAuth.
     */
    fun registerWithGoogle(googleUser: GoogleUserData, defaultIsMentor: Boolean = false): Result<User> {
        val repo = repository ?: return Result.failure(IllegalStateException("Session not initialized"))
        val result = repo.registerWithGoogle(googleUser, defaultIsMentor)
        result.onSuccess { user ->
            _currentUser.value = user
        }
        return result
    }

    /**
     * Backward-compatible fallback.
     */
    fun updateFromGoogle(googleUser: GoogleUserData, defaultIsMentor: Boolean = false): User {
        val repo = repository
        val user = if (repo != null) {
            repo.loginOrRegisterGoogleUser(googleUser, defaultIsMentor)
        } else {
            val displayName = when {
                !googleUser.displayName.isNullOrBlank() -> googleUser.displayName
                !googleUser.givenName.isNullOrBlank() && !googleUser.familyName.isNullOrBlank() -> "${googleUser.givenName} ${googleUser.familyName}"
                else -> googleUser.email.substringBefore("@")
            }
            User(
                id = googleUser.email,
                name = displayName,
                email = googleUser.email,
                avatarUrl = googleUser.profilePictureUri,
                isMentor = defaultIsMentor
            )
        }
        _currentUser.value = user
        return user
    }

    fun updateProfile(updatedUser: User) {
        _currentUser.value = updatedUser
        repository?.updateUserProfile(updatedUser)
    }

    /**
     * Resets session on sign out.
     */
    fun clear() {
        _currentUser.value = guestUser
        repository?.logout()
    }
}
