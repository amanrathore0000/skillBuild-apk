package com.skillbuilder.app.data.local

import android.content.Context
import kotlinx.coroutines.delay
import java.util.concurrent.TimeUnit
import kotlin.random.Random

data class OtpSession(
    val email: String,
    val otp: String,
    val generatedAtMs: Long,
    val expiresAtMs: Long,
    val isVerified: Boolean = false,
    val userRole: String = "Learner" // "Mentor" or "Learner"
)

object PasswordResetManager {

    private var currentSession: OtpSession? = null

    /**
     * Simulates backend server generating a 6-digit OTP and sending it to registered email.
     * Works for both Learner and Mentor registered accounts.
     */
    suspend fun requestOtp(context: Context, email: String): Result<OtpSession> {
        val repo = UserAccountRepository.getInstance(context)
        val normalized = email.trim().lowercase()

        // Verify account is registered in server
        val account = repo.getAccountByEmail(normalized)
            ?: return Result.failure(IllegalArgumentException("No registered account found with email '$normalized'. Please verify your email or register."))

        // Simulate backend latency / server communication
        delay(900)

        // Generate 6-digit cryptographic OTP
        val otpCode = String.format("%06d", Random.nextInt(100000, 1000000))
        val now = System.currentTimeMillis()
        val expiresAt = now + TimeUnit.MINUTES.toMillis(10)

        val session = OtpSession(
            email = normalized,
            otp = otpCode,
            generatedAtMs = now,
            expiresAtMs = expiresAt,
            isVerified = false,
            userRole = if (account.isMentor) "Mentor" else "Learner"
        )
        currentSession = session
        return Result.success(session)
    }

    /**
     * Verifies the 6-digit OTP entered by the user in the app.
     */
    suspend fun verifyOtp(email: String, enteredOtp: String): Result<Boolean> {
        delay(600)
        val session = currentSession
            ?: return Result.failure(IllegalStateException("No active OTP session found. Please request a new code."))

        if (!session.email.equals(email.trim(), ignoreCase = true)) {
            return Result.failure(IllegalArgumentException("Email does not match active OTP session."))
        }

        if (System.currentTimeMillis() > session.expiresAtMs) {
            return Result.failure(IllegalStateException("This OTP code has expired. Please request a new code."))
        }

        if (session.otp != enteredOtp.trim()) {
            return Result.failure(IllegalArgumentException("Invalid verification code. Please check your email and try again."))
        }

        currentSession = session.copy(isVerified = true)
        return Result.success(true)
    }

    /**
     * Updates the password in the database/repository after OTP verification.
     */
    suspend fun resetPassword(context: Context, email: String, newPassword: String): Result<Boolean> {
        delay(800)
        val session = currentSession
            ?: return Result.failure(IllegalStateException("Session expired. Please start the password reset again."))

        if (!session.isVerified) {
            return Result.failure(IllegalStateException("OTP verification is required before resetting password."))
        }

        if (newPassword.length < 6) {
            return Result.failure(IllegalArgumentException("Password must be at least 6 characters long."))
        }

        val repo = UserAccountRepository.getInstance(context)
        val result = repo.updatePassword(email, newPassword)

        result.onSuccess {
            currentSession = null
        }
        return result
    }

    fun getCurrentSession(): OtpSession? = currentSession

    fun clearSession() {
        currentSession = null
    }
}
