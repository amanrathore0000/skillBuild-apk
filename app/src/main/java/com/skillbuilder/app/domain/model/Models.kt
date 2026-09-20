package com.skillbuilder.app.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String,
    val name: String,
    val email: String,
    val avatarUrl: String? = null,
    val phone: String = "",
    val dob: String = "",
    val location: String = "",
    val bio: String = "",
    val rating: Float = 5.0f,
    val reviewCount: Int = 0,
    val isVerified: Boolean = true,
    val skillsTaught: List<String> = emptyList(),
    val skillsWanted: List<String> = emptyList(),
    val isMentor: Boolean = false
)

@Serializable
data class SkillCategory(
    val id: String,
    val name: String,
    val iconName: String
)

@Serializable
data class Skill(
    val id: String,
    val title: String,
    val category: String,
    val description: String,
    val mentorCount: Int,
    val level: String = "All Levels"
)

@Serializable
enum class SwapStatus {
    PENDING,
    ACCEPTED,
    ACTIVE,
    COMPLETED,
    REJECTED
}

@Serializable
data class SwapProposal(
    val id: String,
    val partner: User,
    val mySkill: String,
    val partnerSkill: String,
    val status: SwapStatus,
    val proposedDate: String,
    val message: String
)

@Serializable
data class Lesson(
    val id: String,
    val title: String,
    val duration: String,
    val videoUrl: String,
    val isCompleted: Boolean = false
)

@Serializable
data class Course(
    val id: String,
    val title: String,
    val mentorName: String,
    val mentorAvatar: String? = null,
    val category: String,
    val rating: Float,
    val reviewCount: Int,
    val duration: String,
    val price: String = "Free with Swap",
    val progressPercent: Int = 0,
    val lessons: List<Lesson> = emptyList()
)

@Serializable
data class MentorVideo(
    val id: String,
    val title: String,
    val courseTitle: String,
    val duration: String,
    val views: Int,
    val videoUrl: String,
    val uploadDate: String,
    val thumbnailUrl: String? = null,
    val description: String = "",
    val visibility: String = "Public", // Public, Unlisted, Private
    val category: String = "General",
    val tags: List<String> = emptyList(),
    val level: String = "All Levels",
    val likes: Int = 0,
    val price: String = "Free",          // e.g. "₹399", "₹999", "Free"
    val mentorName: String = "Mentor",   // Display name of the uploading mentor
    val storageProvider: String = "AWS_S3", // "GOOGLE_DRIVE" or "AWS_S3"
    val driveFileId: String? = null,
    val driveSharingLink: String? = null,
    val storagePlanType: String = "FREE"
)

enum class StorageProviderType {
    GOOGLE_DRIVE,
    AWS_S3
}

@Serializable
data class StoragePlan(
    val id: String,
    val name: String,
    val provider: String, // "GOOGLE_DRIVE" or "AWS_S3"
    val price: String,
    val storageLimitBytes: Long,
    val storageLimitFormatted: String,
    val features: List<String>,
    val isRecommended: Boolean = false
)

@Serializable
data class MentorStorageAccount(
    val currentPlanId: String = "plan_gdrive_free",
    val usedBytes: Long = 0L,
    val totalBytes: Long = 16_106_127_360L, // 15 GB default for Google Drive
    val isGoogleDriveConnected: Boolean = false,
    val isAwsConnected: Boolean = false,
    val googleDriveEmail: String? = null
) {
    val isConnected: Boolean get() = isGoogleDriveConnected || isAwsConnected
}

@Serializable
data class WalletTransaction(
    val id: String,
    val title: String,
    val subtitle: String,
    val amount: String,
    val date: String,
    val isCredit: Boolean = true
)

data class MentorWallet(
    val balance: String,
    val pendingPayout: String,
    val totalEarned: String,
    val monthlyRevenue: String,
    val transactions: List<WalletTransaction> = emptyList()
)

data class MentorStats(
    val totalRevenue: String,
    val activeStudents: Int,
    val totalHoursTaught: Int,
    val averageRating: Float
)

enum class ChatMessageType {
    STANDARD,
    DOUBT_QUERY,
    VIDEO_DEMAND
}

@Serializable
data class ChatMessage(
    val id: String,
    val conversationId: String,
    val senderId: String,
    val senderName: String,
    val senderRole: String, // "Learner" or "Mentor"
    val recipientId: String,
    val text: String,
    val timestamp: String,
    val type: ChatMessageType = ChatMessageType.STANDARD,
    val referenceTopic: String? = null, // e.g., "Lesson 1: Acoustic Anatomy", "Compose Recomposition"
    val referenceCourseId: String? = null,
    val status: String = "Delivered", // "Sent", "Delivered", "Read"
    val isEncrypted: Boolean = true,
    val isResolved: Boolean = false, // For doubts
    val isAccepted: Boolean = false, // For video demands
    val demandVotes: Int = 1         // Upvotes for video requests
)

@Serializable
data class ChatConversation(
    val id: String,
    val mentorId: String,
    val mentorName: String,
    val mentorAvatar: String? = null,
    val mentorCategory: String = "Music",
    val learnerId: String,
    val learnerName: String,
    val learnerAvatar: String? = null,
    val lastMessage: String,
    val lastMessageTimestamp: String,
    val unreadCount: Int = 0,
    val hasDoubtPending: Boolean = false,
    val hasVideoDemandPending: Boolean = false
)

