package com.skillbuilder.app.data.local

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.skillbuilder.app.auth.GoogleAuthClient
import com.skillbuilder.app.auth.GoogleUserData
import com.skillbuilder.app.domain.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

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

    private const val SESSION_PREFS = "skillbuilder_auth_session"
    private const val KEY_SESSION_TOKEN = "session_token"
    private const val KEY_ACCESS_TOKEN = "access_token"
    private const val KEY_REFRESH_TOKEN = "refresh_token"

    private const val ENROLLMENTS_PREF = "skillbuilder_enrollments"
    private const val KEY_ENROLLED_IDS = "enrolled_video_ids"

    private var sessionPrefs: SharedPreferences? = null
    private var enrollPrefs: SharedPreferences? = null
    private var repository: UserAccountRepository? = null

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()
    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.SECONDS)
        .build()

    private val candidateBaseUrls = listOf(
        "http://localhost:5000",       // Works with adb reverse tcp:5000 tcp:5000
        "http://10.0.2.2:5000",        // Android emulator localhost
        "http://192.168.29.198:5000"   // Wi-Fi LAN IP
    )

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

    fun initialize(context: Context) {
        val repo = UserAccountRepository.getInstance(context)
        repository = repo

        sessionPrefs = context.getSharedPreferences(SESSION_PREFS, Context.MODE_PRIVATE)
        enrollPrefs = context.getSharedPreferences(ENROLLMENTS_PREF, Context.MODE_PRIVATE)

        val active = repo.getActiveUser()
        if (active != null) {
            _currentUser.value = active
        }

        val saved = enrollPrefs?.getStringSet(KEY_ENROLLED_IDS, null)
        if (saved != null) {
            _enrolledVideoIds.value = saved
        }
    }

    fun isUserLoggedIn(): Boolean {
        return _currentUser.value.id != "user_guest"
    }

    fun getSessionToken(): String? = sessionPrefs?.getString(KEY_SESSION_TOKEN, null)
    fun getAccessToken(): String? = sessionPrefs?.getString(KEY_ACCESS_TOKEN, null)
    fun getRefreshToken(): String? = sessionPrefs?.getString(KEY_REFRESH_TOKEN, null)

    fun saveTokens(sessionToken: String?, accessToken: String?, refreshToken: String?) {
        sessionPrefs?.edit()?.apply {
            putString(KEY_SESSION_TOKEN, sessionToken)
            putString(KEY_ACCESS_TOKEN, accessToken)
            putString(KEY_REFRESH_TOKEN, refreshToken)
            apply()
        }
    }

    fun setUser(user: User) {
        _currentUser.value = user
        repository?.setActiveUser(user)
    }

    /**
     * Authenticates with Google via backend OpenID Connect verification.
     * On backend success: creates/links Account, establishes Session, and logs in.
     * On backend failure/unreachable: seamlessly completes local authentication.
     */
    suspend fun authenticateWithGoogle(
        googleUser: GoogleUserData,
        isMentor: Boolean = false
    ): Result<User> = withContext(Dispatchers.IO) {
        // 1. Attempt backend server authentication and session creation
        val payload = JSONObject().apply {
            put("idToken", googleUser.idToken)
            put("isMentor", isMentor)
            if (!googleUser.displayName.isNullOrBlank()) put("name", googleUser.displayName)
            if (!googleUser.profilePictureUri.isNullOrBlank()) put("picture", googleUser.profilePictureUri)
        }.toString().toRequestBody(jsonMediaType)

        var backendUser: User? = null
        var sessionToken: String? = null
        var accessToken: String? = null
        var refreshToken: String? = null

        for (baseUrl in candidateBaseUrls) {
            try {
                val req = Request.Builder()
                    .url("$baseUrl/api/v1/auth/google")
                    .post(payload)
                    .build()

                httpClient.newCall(req).execute().use { response ->
                    val body = response.body?.string() ?: ""
                    Log.d("SkillBuilderOAuth", "Backend response [${response.code}] from $baseUrl: $body")
                    if (response.isSuccessful) {
                        val resJson = JSONObject(body)
                        if (resJson.optBoolean("success", false)) {
                            val data = resJson.getJSONObject("data")
                            sessionToken = data.optString("sessionToken", null)
                            accessToken = data.optString("accessToken", null)
                            refreshToken = data.optString("refreshToken", null)

                            val uJson = data.getJSONObject("user")
                            val skillsTaught = mutableListOf<String>()
                            val stArr = uJson.optJSONArray("skillsTaught")
                            if (stArr != null) {
                                for (i in 0 until stArr.length()) skillsTaught.add(stArr.getString(i))
                            }
                            val skillsWanted = mutableListOf<String>()
                            val swArr = uJson.optJSONArray("skillsWanted")
                            if (swArr != null) {
                                for (i in 0 until swArr.length()) skillsWanted.add(swArr.getString(i))
                            }

                            backendUser = User(
                                id = uJson.optString("id", googleUser.email),
                                name = uJson.optString("name", googleUser.displayName ?: googleUser.email.substringBefore("@")),
                                email = uJson.optString("email", googleUser.email),
                                avatarUrl = uJson.optString("avatarUrl", googleUser.profilePictureUri),
                                phone = uJson.optString("phone", ""),
                                dob = uJson.optString("dob", ""),
                                location = uJson.optString("location", ""),
                                bio = uJson.optString("bio", ""),
                                rating = uJson.optDouble("rating", 5.0).toFloat(),
                                reviewCount = uJson.optInt("reviewCount", 0),
                                isVerified = uJson.optBoolean("isVerified", true),
                                skillsTaught = skillsTaught,
                                skillsWanted = skillsWanted,
                                isMentor = uJson.optBoolean("isMentor", isMentor)
                            )
                        }
                    }
                }
                if (backendUser != null) break
            } catch (e: Exception) {
                Log.w("SkillBuilderOAuth", "Failed connecting to $baseUrl: ${e.message}")
            }
        }

        if (backendUser != null) {
            saveTokens(sessionToken, accessToken, refreshToken)
            setUser(backendUser!!)
            return@withContext Result.success(backendUser!!)
        }

        // 2. Seamless local fallback if backend network is unreachable
        val repo = repository
            ?: return@withContext Result.failure(IllegalStateException("Repository not initialized"))
        val localResult = repo.loginWithGoogle(googleUser, isMentor)
        localResult.onSuccess { user ->
            setUser(user)
        }
        return@withContext localResult
    }

    /**
     * Backward-compatible helper for existing screens.
     */
    fun loginWithGoogle(googleUser: GoogleUserData, defaultIsMentor: Boolean = false): Result<User> {
        val repo = repository ?: return Result.failure(IllegalStateException("Session not initialized"))
        val result = repo.loginWithGoogle(googleUser, defaultIsMentor)
        result.onSuccess { user ->
            setUser(user)
        }
        return result
    }

    fun registerWithGoogle(googleUser: GoogleUserData, defaultIsMentor: Boolean = false): Result<User> {
        val repo = repository ?: return Result.failure(IllegalStateException("Session not initialized"))
        val result = repo.registerWithGoogle(googleUser, defaultIsMentor)
        result.onSuccess { user ->
            setUser(user)
        }
        return result
    }

    fun updateProfile(updatedUser: User) {
        _currentUser.value = updatedUser
        repository?.updateUserProfile(updatedUser)
    }

    /**
     * Complete Logout:
     * 1. Invalidates backend database session.
     * 2. Clears Google Credential Manager state.
     * 3. Clears local session tokens and resets user to guest.
     */
    suspend fun logout(googleAuthClient: GoogleAuthClient? = null) = withContext(Dispatchers.IO) {
        val token = getAccessToken()
        val sessToken = getSessionToken()

        // 1. Invalidate backend session
        if (!token.isNullOrBlank()) {
            for (baseUrl in candidateBaseUrls) {
                try {
                    val req = Request.Builder()
                        .url("$baseUrl/api/v1/auth/logout")
                        .addHeader("Authorization", "Bearer $token")
                        .apply {
                            if (!sessToken.isNullOrBlank()) addHeader("x-session-token", sessToken)
                        }
                        .post("{}".toRequestBody(jsonMediaType))
                        .build()

                    httpClient.newCall(req).execute().close()
                    break
                } catch (_: Exception) {}
            }
        }

        // 2. Clear Google Credential state
        try {
            googleAuthClient?.signOut()
        } catch (_: Exception) {}

        // 3. Clear local storage
        saveTokens(null, null, null)
        _currentUser.value = guestUser
        repository?.logout()
    }

    fun clear() {
        saveTokens(null, null, null)
        _currentUser.value = guestUser
        repository?.logout()
    }
}
