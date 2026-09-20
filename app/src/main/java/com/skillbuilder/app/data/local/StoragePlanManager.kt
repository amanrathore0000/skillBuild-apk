package com.skillbuilder.app.data.local

import android.content.Context
import android.content.SharedPreferences
import com.skillbuilder.app.domain.model.MentorStorageAccount
import com.skillbuilder.app.domain.model.StoragePlan
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.DecimalFormat

/**
 * StoragePlanManager handles the dual-storage tiering system:
 * 1. Google Drive (Free BYO Storage)
 * 2. SkillBuilder AWS S3 Cloud (Paid High-Performance CDN Plans)
 */
object StoragePlanManager {

    private const val PREFS_NAME = "skillbuilder_storage_prefs"
    private const val KEY_PLAN_ID = "storage_plan_id"
    private const val KEY_USED_BYTES = "storage_used_bytes"
    private const val KEY_TOTAL_BYTES = "storage_total_bytes"
    private const val KEY_DRIVE_CONNECTED = "storage_drive_connected"
    private const val KEY_AWS_CONNECTED = "storage_aws_connected"
    private const val KEY_DRIVE_EMAIL = "storage_drive_email"

    private var prefs: SharedPreferences? = null

    val availablePlans = listOf(
        StoragePlan(
            id = "plan_gdrive_free",
            name = "Google Drive (Free BYOD)",
            provider = "GOOGLE_DRIVE",
            price = "Free (₹0)",
            storageLimitBytes = 16_106_127_360L, // 15 GB
            storageLimitFormatted = "15 GB Free",
            features = listOf(
                "Uses your 15GB Google Drive quota",
                "Zero hosting cost for creator",
                "Automatic private permissions for enrolled students",
                "Ideal for 1-to-1 mentorship & skill swaps"
            ),
            isRecommended = false
        ),
        StoragePlan(
            id = "plan_aws_starter",
            name = "AWS Cloud Starter",
            provider = "AWS_S3",
            price = "₹299/mo",
            storageLimitBytes = 26_843_545_600L, // 25 GB
            storageLimitFormatted = "25 GB Cloud",
            features = listOf(
                "Lightning-fast AWS CloudFront CDN delivery",
                "1080p Full HD adaptive bitrate streaming",
                "Zero Google Drive daily view quotas",
                "Instant playback on all Android devices"
            ),
            isRecommended = false
        ),
        StoragePlan(
            id = "plan_aws_pro",
            name = "AWS Studio Pro",
            provider = "AWS_S3",
            price = "₹699/mo",
            storageLimitBytes = 107_374_182_400L, // 100 GB
            storageLimitFormatted = "100 GB Cloud",
            features = listOf(
                "Ultra-fast AWS CloudFront global edge CDN",
                "4K Ultra HD & 60fps streaming support",
                "No student count or bandwidth limits",
                "Automatic HLS multi-bitrate transcoding",
                "Priority creator encoding queue"
            ),
            isRecommended = true
        ),
        StoragePlan(
            id = "plan_aws_enterprise",
            name = "AWS Creator Scale",
            provider = "AWS_S3",
            price = "₹1,499/mo",
            storageLimitBytes = 536_870_912_000L, // 500 GB
            storageLimitFormatted = "500 GB Cloud",
            features = listOf(
                "Massive 500 GB high-speed media vault",
                "Enterprise SLA & multi-region redundancy",
                "Unlimited concurrent student video streams",
                "DRM encryption & watermarking protection"
            ),
            isRecommended = false
        )
    )

    private val _storageAccount = MutableStateFlow(
        MentorStorageAccount(
            currentPlanId = "plan_gdrive_free",
            usedBytes = 0L,
            totalBytes = 16_106_127_360L,
            isGoogleDriveConnected = false,
            isAwsConnected = false,
            googleDriveEmail = null
        )
    )
    val storageAccount: StateFlow<MentorStorageAccount> = _storageAccount.asStateFlow()

    // Tracks videoId -> set of learner emails granted Google Drive access
    private val _grantedDriveAccess = MutableStateFlow<Map<String, Set<String>>>(emptyMap())
    val grantedDriveAccess: StateFlow<Map<String, Set<String>>> = _grantedDriveAccess.asStateFlow()

    fun initialize(context: Context) {
        val sp = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs = sp
        val isDrive = sp.getBoolean(KEY_DRIVE_CONNECTED, false)
        val isAws = sp.getBoolean(KEY_AWS_CONNECTED, false)
        val planId = sp.getString(KEY_PLAN_ID, "plan_gdrive_free") ?: "plan_gdrive_free"
        val driveEmail = sp.getString(KEY_DRIVE_EMAIL, null)
        val used = sp.getLong(KEY_USED_BYTES, 0L)
        val total = sp.getLong(KEY_TOTAL_BYTES, 16_106_127_360L)

        _storageAccount.value = MentorStorageAccount(
            currentPlanId = planId,
            usedBytes = used,
            totalBytes = total,
            isGoogleDriveConnected = isDrive,
            isAwsConnected = isAws,
            googleDriveEmail = driveEmail
        )
    }

    fun isStorageConnected(): Boolean {
        return _storageAccount.value.isConnected
    }

    fun connectGoogleDrive(email: String) {
        _storageAccount.value = _storageAccount.value.copy(
            isGoogleDriveConnected = true,
            isAwsConnected = false,
            currentPlanId = "plan_gdrive_free",
            totalBytes = 16_106_127_360L,
            googleDriveEmail = email
        )
        prefs?.edit()
            ?.putBoolean(KEY_DRIVE_CONNECTED, true)
            ?.putBoolean(KEY_AWS_CONNECTED, false)
            ?.putString(KEY_PLAN_ID, "plan_gdrive_free")
            ?.putString(KEY_DRIVE_EMAIL, email)
            ?.putLong(KEY_TOTAL_BYTES, 16_106_127_360L)
            ?.apply()
    }

    fun connectAwsStorage(planId: String) {
        val plan = availablePlans.find { it.id == planId } ?: availablePlans[1]
        _storageAccount.value = _storageAccount.value.copy(
            isGoogleDriveConnected = false,
            isAwsConnected = true,
            currentPlanId = plan.id,
            totalBytes = plan.storageLimitBytes
        )
        prefs?.edit()
            ?.putBoolean(KEY_DRIVE_CONNECTED, false)
            ?.putBoolean(KEY_AWS_CONNECTED, true)
            ?.putString(KEY_PLAN_ID, plan.id)
            ?.putLong(KEY_TOTAL_BYTES, plan.storageLimitBytes)
            ?.apply()
    }

    fun disconnectStorage() {
        _storageAccount.value = _storageAccount.value.copy(
            isGoogleDriveConnected = false,
            isAwsConnected = false,
            googleDriveEmail = null
        )
        prefs?.edit()
            ?.putBoolean(KEY_DRIVE_CONNECTED, false)
            ?.putBoolean(KEY_AWS_CONNECTED, false)
            ?.putString(KEY_DRIVE_EMAIL, null)
            ?.apply()
    }

    fun getPlan(planId: String): StoragePlan {
        return availablePlans.find { it.id == planId } ?: availablePlans.first()
    }

    fun getCurrentPlan(): StoragePlan {
        return getPlan(_storageAccount.value.currentPlanId)
    }

    fun isAwsPlanActive(): Boolean {
        return _storageAccount.value.isAwsConnected
    }

    /**
     * Upgrades or subscribes the mentor to an AWS S3 plan or switches to Google Drive.
     */
    fun subscribePlan(planId: String): Boolean {
        val plan = availablePlans.find { it.id == planId } ?: return false
        if (plan.provider == "GOOGLE_DRIVE") {
            connectGoogleDrive(_storageAccount.value.googleDriveEmail ?: "mentor@gmail.com")
        } else {
            connectAwsStorage(plan.id)
        }
        return true
    }

    /**
     * Records additional storage consumed when a mentor publishes a video.
     */
    fun consumeStorage(bytes: Long) {
        val current = _storageAccount.value
        val updatedUsed = (current.usedBytes + bytes).coerceAtMost(current.totalBytes)
        _storageAccount.value = current.copy(usedBytes = updatedUsed)
        prefs?.edit()?.putLong(KEY_USED_BYTES, updatedUsed)?.apply()
    }

    /**
     * Grants a learner viewer access to a private Google Drive course video.
     */
    fun grantDriveAccess(videoId: String, learnerEmail: String) {
        val currentMap = _grantedDriveAccess.value.toMutableMap()
        val existingLearners = currentMap[videoId] ?: emptySet()
        currentMap[videoId] = existingLearners + learnerEmail
        _grantedDriveAccess.value = currentMap
    }

    fun hasDriveAccess(videoId: String, learnerEmail: String): Boolean {
        val learners = _grantedDriveAccess.value[videoId] ?: return false
        return learnerEmail in learners
    }

    fun formatBytes(bytes: Long): String {
        val df = DecimalFormat("#.##")
        return when {
            bytes >= 1_073_741_824L -> "${df.format(bytes.toDouble() / 1_073_741_824L)} GB"
            bytes >= 1_048_576L -> "${df.format(bytes.toDouble() / 1_048_576L)} MB"
            bytes >= 1024L -> "${df.format(bytes.toDouble() / 1024L)} KB"
            else -> "$bytes B"
        }
    }
}
