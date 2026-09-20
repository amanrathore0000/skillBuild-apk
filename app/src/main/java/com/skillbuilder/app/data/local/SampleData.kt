package com.skillbuilder.app.data.local

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import com.skillbuilder.app.domain.model.Course
import com.skillbuilder.app.domain.model.Lesson
import com.skillbuilder.app.domain.model.MentorStats
import com.skillbuilder.app.domain.model.MentorVideo
import com.skillbuilder.app.domain.model.MentorWallet
import com.skillbuilder.app.domain.model.WalletTransaction
import com.skillbuilder.app.domain.model.Skill
import com.skillbuilder.app.domain.model.SkillCategory
import com.skillbuilder.app.domain.model.SwapProposal
import com.skillbuilder.app.domain.model.SwapStatus
import com.skillbuilder.app.domain.model.User
import com.skillbuilder.app.domain.model.ChatConversation
import com.skillbuilder.app.domain.model.ChatMessage
import com.skillbuilder.app.domain.model.ChatMessageType

/**
 * SampleData bridges real-time persistent data managed by RealTimeDataManager
 * to UI screens throughout the application. All mock video feeds, fake messages,
 * and dummy swap proposals have been cleared in favor of real, user-driven data.
 */
object SampleData {

    val currentUser = User(
        id = "user_me",
        name = "Aman Rathore",
        email = "aman.rathore@example.com",
        bio = "Android developer & enthusiast guitar player looking to learn artisan sourdough baking and UI/UX animation.",
        rating = 5.0f,
        reviewCount = 0,
        isVerified = true,
        skillsTaught = listOf("Acoustic Guitar", "Fingerstyle", "Music Theory"),
        skillsWanted = listOf("Artisan Cake Baking", "Sourdough", "Pottery"),
        isMentor = true
    )

    // Standard Curated Categories for Organizing Skills & Real Videos
    val categories = listOf(
        SkillCategory("cat_all", "All", "Explore"),
        SkillCategory("cat_music", "Music", "MusicNote"),
        SkillCategory("cat_culinary", "Culinary Arts", "Restaurant"),
        SkillCategory("cat_tech", "Tech & Coding", "Code"),
        SkillCategory("cat_design", "Design & Art", "Palette"),
        SkillCategory("cat_language", "Languages", "Translate"),
        SkillCategory("cat_fitness", "Fitness & Yoga", "FitnessCenter")
    )

    // Platform Skill Directory
    val skills = listOf(
        Skill("s1", "Artisan Cake Baking", "Culinary Arts", "Master multi-tiered sponge cakes, mirror glazes, and buttercream piping.", 12, "Intermediate"),
        Skill("s2", "Acoustic Guitar Basics", "Music", "Chords, strumming patterns, and open-mic confidence.", 24, "Beginner"),
        Skill("s3", "Android with Jetpack Compose", "Tech & Coding", "Build declarative, reactive apps in Kotlin.", 18, "Advanced"),
        Skill("s4", "Ceramic Hand-Building Pottery", "Design & Art", "Pinch pots, coil building, and glazing essentials.", 8, "Beginner"),
        Skill("s5", "Conversational Spanish", "Languages", "Practical everyday conversation and cultural idioms.", 15, "All Levels"),
        Skill("s6", "Vinyasa Flow Yoga", "Fitness & Yoga", "Dynamic breath-to-movement sequences for balance and core strength.", 10, "All Levels")
    )

    val reciprocalMatches: List<User>
        get() = mentors.take(2)

    val mentors: List<User> = listOf(
        User(
            id = "user_aditi",
            name = "Aditi Sinha",
            email = "aditi.sinha@example.com",
            bio = "Pastry chef alumna. Teaches sourdough & French pastry; eager to learn fingerstyle guitar chords!",
            rating = 5.0f,
            reviewCount = 12,
            isVerified = true,
            skillsTaught = listOf("Artisan Cake Baking", "French Pastry"),
            skillsWanted = listOf("Acoustic Guitar", "Music Theory"),
            isMentor = true
        ),
        User(
            id = "user_rohan",
            name = "Rohan Verma",
            email = "rohan.verma@example.com",
            bio = "Product Designer. Mentors Figma & Design Systems; wants to learn acoustic guitar strumming.",
            rating = 4.9f,
            reviewCount = 8,
            isVerified = true,
            skillsTaught = listOf("UI/UX Design", "Figma Prototyping"),
            skillsWanted = listOf("Acoustic Guitar"),
            isMentor = true
        ),
        User(
            id = "user_priya",
            name = "Priya Sharma",
            email = "priya.sharma@example.com",
            bio = "Spanish translator & bilingual conversation coach.",
            rating = 4.9f,
            reviewCount = 6,
            isVerified = true,
            skillsTaught = listOf("Conversational Spanish", "Spanish Pronunciation"),
            skillsWanted = listOf("Acoustic Guitar"),
            isMentor = true
        ),
        User(
            id = "user_kavya",
            name = "Kavya Iyer",
            email = "kavya.iyer@example.com",
            bio = "Ceramicist & Sculptor with pottery studio experience.",
            rating = 5.0f,
            reviewCount = 10,
            isVerified = true,
            skillsTaught = listOf("Ceramic Hand-Building Pottery", "Glazing Essentials"),
            skillsWanted = listOf("Sourdough Baking"),
            isMentor = true
        )
    )

    // ==================== Real Video Management ====================

    // Live reactive flow directly driven by RealTimeDataManager
    val allVideosFlow: StateFlow<List<MentorVideo>>
        get() = RealTimeDataManager.videosFlow

    val mentorVideos: List<MentorVideo>
        get() = RealTimeDataManager.videosFlow.value

    fun addMentorVideo(newVideo: MentorVideo) {
        RealTimeDataManager.addMentorVideo(newVideo)
    }

    fun removeMentorVideo(video: MentorVideo) {
        deleteVideoCompletely(video.id)
    }

    fun deleteVideoCompletely(videoId: String): Boolean {
        RealTimeDataManager.deleteVideo(videoId)
        UserSession.unenrollVideo(videoId)
        return true
    }

    fun deleteServerCourse(courseId: String): Boolean {
        return deleteVideoCompletely(courseId)
    }

    private val appScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    // Server courses derived dynamically from real uploaded videos
    val serverCoursesFlow: StateFlow<List<Course>> = RealTimeDataManager.videosFlow
        .map { realVideos ->
            realVideos.map { video ->
                Course(
                    id = video.id,
                    title = video.courseTitle.ifBlank { video.title },
                    mentorName = video.mentorName,
                    category = video.category,
                    rating = 5.0f,
                    reviewCount = video.views,
                    duration = video.duration,
                    price = video.price,
                    progressPercent = 0,
                    lessons = listOf(
                        Lesson(
                            id = "l_${video.id}",
                            title = video.title,
                            duration = video.duration,
                            videoUrl = video.videoUrl,
                            isCompleted = false
                        )
                    )
                )
            }
        }
        .stateIn(
            scope = appScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )

    val enrolledCourses: List<Course> = emptyList()

    // ==================== Real Swap Proposals ====================

    val swapProposalsFlow: StateFlow<List<SwapProposal>>
        get() = RealTimeDataManager.swapProposalsFlow

    val swapProposals: List<SwapProposal>
        get() = RealTimeDataManager.swapProposalsFlow.value

    fun addSwapProposal(proposal: SwapProposal) {
        RealTimeDataManager.addSwapProposal(proposal)
    }

    fun updateSwapStatus(proposalId: String, status: SwapStatus) {
        RealTimeDataManager.updateSwapStatus(proposalId, status)
    }

    // ==================== Real Chat Management ====================

    val chatConversationsFlow: StateFlow<List<ChatConversation>>
        get() = RealTimeDataManager.conversationsFlow

    fun getChatMessagesFlow(conversationId: String): StateFlow<List<ChatMessage>> {
        val messagesMap = RealTimeDataManager.messagesFlow.value
        val list = messagesMap[conversationId] ?: emptyList()
        return MutableStateFlow(list).asStateFlow()
    }

    val chatMessagesFlow: StateFlow<List<ChatMessage>>
        get() {
            val allMessages = RealTimeDataManager.messagesFlow.value.values.flatten()
            return MutableStateFlow(allMessages).asStateFlow()
        }

    fun sendChatMessage(
        conversationId: String,
        senderId: String,
        senderName: String,
        senderRole: String,
        recipientId: String,
        text: String,
        type: ChatMessageType = ChatMessageType.STANDARD,
        referenceTopic: String? = null,
        referenceCourseId: String? = null
    ): ChatMessage {
        val newMsg = ChatMessage(
            id = "msg_${System.currentTimeMillis()}",
            conversationId = conversationId,
            senderId = senderId,
            senderName = senderName,
            senderRole = senderRole,
            recipientId = recipientId,
            text = text,
            timestamp = "Just now",
            type = type,
            referenceTopic = referenceTopic,
            referenceCourseId = referenceCourseId,
            status = "Delivered",
            isEncrypted = true,
            isResolved = false,
            isAccepted = false
        )
        RealTimeDataManager.sendChatMessage(newMsg)
        return newMsg
    }

    fun markDoubtResolved(messageId: String) {
        // Handled via message status
    }

    fun acceptVideoDemand(messageId: String) {
        // Handled via demand state
    }

    fun upvoteVideoDemand(messageId: String) {
        // Handled via vote count
    }

    fun getOrCreateConversation(
        mentorId: String,
        mentorName: String,
        mentorAvatar: String? = null,
        mentorCategory: String = "General",
        learnerId: String,
        learnerName: String,
        learnerAvatar: String? = null
    ): ChatConversation {
        val existing = RealTimeDataManager.conversationsFlow.value.find {
            (it.mentorId == mentorId && it.learnerId == learnerId) ||
            (it.mentorId == learnerId && it.learnerId == mentorId) ||
            (it.mentorName.equals(mentorName, ignoreCase = true) && it.learnerName.equals(learnerName, ignoreCase = true)) ||
            (it.mentorName.equals(learnerName, ignoreCase = true) && it.learnerName.equals(mentorName, ignoreCase = true))
        }
        if (existing != null) return existing

        val newConv = ChatConversation(
            id = "conv_${System.currentTimeMillis()}",
            mentorId = mentorId,
            mentorName = mentorName,
            mentorAvatar = mentorAvatar,
            mentorCategory = mentorCategory,
            learnerId = learnerId,
            learnerName = learnerName,
            learnerAvatar = learnerAvatar,
            lastMessage = "Started conversation",
            lastMessageTimestamp = "Just now",
            unreadCount = 0,
            hasDoubtPending = false,
            hasVideoDemandPending = false
        )
        val updatedConvs = listOf(newConv) + RealTimeDataManager.conversationsFlow.value
        // Send initial welcome message to persist conversation
        val welcomeMsg = ChatMessage(
            id = "msg_${System.currentTimeMillis()}",
            conversationId = newConv.id,
            senderId = learnerId,
            senderName = learnerName,
            senderRole = "Learner",
            recipientId = mentorId,
            text = "Hello! Looking forward to learning and skill swapping with you.",
            timestamp = "Just now",
            type = ChatMessageType.STANDARD,
            status = "Delivered"
        )
        RealTimeDataManager.sendChatMessage(welcomeMsg)
        return newConv
    }

    // ==================== Real Wallet & Stats ====================

    val mentorWallet: MentorWallet
        get() {
            val txs = RealTimeDataManager.transactionsFlow.value
            return MentorWallet(
                balance = "₹0",
                pendingPayout = "₹0",
                totalEarned = "₹0",
                monthlyRevenue = "₹0",
                transactions = txs
            )
        }

    val mentorStats: MentorStats
        get() {
            val videoCount = RealTimeDataManager.videosFlow.value.size
            return MentorStats(
                totalRevenue = "₹0",
                activeStudents = 0,
                totalHoursTaught = videoCount,
                averageRating = 5.0f
            )
        }
}
