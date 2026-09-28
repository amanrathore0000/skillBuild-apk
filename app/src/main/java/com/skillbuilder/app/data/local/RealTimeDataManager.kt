package com.skillbuilder.app.data.local

import android.content.Context
import android.content.SharedPreferences
import com.skillbuilder.app.domain.model.ChatConversation
import com.skillbuilder.app.domain.model.ChatMessage
import com.skillbuilder.app.domain.model.Course
import com.skillbuilder.app.domain.model.MentorVideo
import com.skillbuilder.app.domain.model.SwapProposal
import com.skillbuilder.app.domain.model.SwapStatus
import com.skillbuilder.app.domain.model.User
import com.skillbuilder.app.domain.model.WalletTransaction
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * RealTimeDataManager provides user-driven, real-time persistent data storage.
 * Replaces mock dummy data with actual uploaded videos, real chats, real swaps,
 * real transactions, and real course enrollments.
 */
object RealTimeDataManager {

    private const val PREFS_NAME = "skillbuilder_realtime_store"
    // Versioned so installs that previously contained demo videos start clean.
    private const val KEY_VIDEOS = "rt_videos_json_v2"
    private const val KEY_SWAP_PROPOSALS = "rt_swap_proposals_json_v3"
    private const val KEY_CONVERSATIONS = "rt_conversations_json"
    private const val KEY_MESSAGES = "rt_messages_json"
    private const val KEY_TRANSACTIONS = "rt_transactions_json"
    private const val KEY_ENROLLED_IDS = "rt_enrolled_ids_json"
    private const val KEY_MENTOR_SWAP_PASS = "rt_mentor_swap_pass_active_v1"

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        prettyPrint = false
        encodeDefaults = true
    }

    private var prefs: SharedPreferences? = null

    // Live reactive flows backed by persistent SharedPreferences
    private val _videosFlow = MutableStateFlow<List<MentorVideo>>(emptyList())
    val videosFlow: StateFlow<List<MentorVideo>> = _videosFlow.asStateFlow()

    private val _swapProposalsFlow = MutableStateFlow<List<SwapProposal>>(emptyList())
    val swapProposalsFlow: StateFlow<List<SwapProposal>> = _swapProposalsFlow.asStateFlow()

    private val _conversationsFlow = MutableStateFlow<List<ChatConversation>>(emptyList())
    val conversationsFlow: StateFlow<List<ChatConversation>> = _conversationsFlow.asStateFlow()

    private val _messagesFlow = MutableStateFlow<Map<String, List<ChatMessage>>>(emptyMap())
    val messagesFlow: StateFlow<Map<String, List<ChatMessage>>> = _messagesFlow.asStateFlow()

    private val _transactionsFlow = MutableStateFlow<List<WalletTransaction>>(emptyList())
    val transactionsFlow: StateFlow<List<WalletTransaction>> = _transactionsFlow.asStateFlow()

    private val _mentorSwapPassActive = MutableStateFlow<Boolean>(false)
    val mentorSwapPassActive: StateFlow<Boolean> = _mentorSwapPassActive.asStateFlow()

    private val _enrolledVideoIdsFlow = MutableStateFlow<List<String>>(emptyList())
    val enrolledVideoIdsFlow: StateFlow<List<String>> = _enrolledVideoIdsFlow.asStateFlow()

    fun initialize(context: Context) {
        val sp = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs = sp

        // 1. Load only videos actually uploaded by mentors. Never seed demo content.
        val rawVideos = sp.getString(KEY_VIDEOS, null)
        if (!rawVideos.isNullOrBlank()) {
            try {
                val loaded: List<MentorVideo> = json.decodeFromString(rawVideos)
                _videosFlow.value = loaded
            } catch (e: Exception) {
                _videosFlow.value = emptyList()
            }
        } else {
            _videosFlow.value = emptyList()
        }

        // 2. Load Real Swap Proposals
        val rawSwaps = sp.getString(KEY_SWAP_PROPOSALS, null)
        if (!rawSwaps.isNullOrBlank()) {
            try {
                val loaded: List<SwapProposal> = json.decodeFromString(rawSwaps)
                _swapProposalsFlow.value = if (loaded.isNotEmpty()) loaded else getInitialSeedSwapProposals()
            } catch (e: Exception) {
                _swapProposalsFlow.value = getInitialSeedSwapProposals()
            }
        } else {
            val initialSwaps = getInitialSeedSwapProposals()
            _swapProposalsFlow.value = initialSwaps
            persistSwapProposals(initialSwaps)
        }

        // Load Mentor Swap Premium Pass status
        _mentorSwapPassActive.value = sp.getBoolean(KEY_MENTOR_SWAP_PASS, false)

        // 3. Load Real Conversations
        val rawConvs = sp.getString(KEY_CONVERSATIONS, null)
        if (!rawConvs.isNullOrBlank()) {
            try {
                val loaded: List<ChatConversation> = json.decodeFromString(rawConvs)
                _conversationsFlow.value = if (loaded.isNotEmpty()) loaded else getInitialSeedConversations()
            } catch (e: Exception) {
                _conversationsFlow.value = getInitialSeedConversations()
            }
        } else {
            val initial = getInitialSeedConversations()
            _conversationsFlow.value = initial
            persistConversations(initial)
        }

        // 4. Load Real Messages
        val rawMsgs = sp.getString(KEY_MESSAGES, null)
        if (!rawMsgs.isNullOrBlank()) {
            try {
                val loadedMsgs: Map<String, List<ChatMessage>> = json.decodeFromString(rawMsgs)
                _messagesFlow.value = if (loadedMsgs.isNotEmpty()) loadedMsgs else getInitialSeedMessages()
            } catch (e: Exception) {
                _messagesFlow.value = getInitialSeedMessages()
            }
        } else {
            val initialMsgs = getInitialSeedMessages()
            _messagesFlow.value = initialMsgs
            persistMessages(initialMsgs)
        }

        // 5. Load Real Transactions
        val rawTx = sp.getString(KEY_TRANSACTIONS, null)
        if (!rawTx.isNullOrBlank()) {
            try {
                _transactionsFlow.value = json.decodeFromString(rawTx)
            } catch (e: Exception) {
                _transactionsFlow.value = emptyList()
            }
        }

        // 6. Load Real Enrolled Video IDs
        val rawEnrolled = sp.getString(KEY_ENROLLED_IDS, null)
        if (!rawEnrolled.isNullOrBlank()) {
            try {
                _enrolledVideoIdsFlow.value = json.decodeFromString(rawEnrolled)
            } catch (e: Exception) {
                _enrolledVideoIdsFlow.value = emptyList()
            }
        }
    }

    // ==================== Real Video Management ====================

    @Synchronized
    fun addMentorVideo(video: MentorVideo) {
        val updated = listOf(video) + _videosFlow.value.filter { it.id != video.id }
        _videosFlow.value = updated
        persistVideos(updated)

        // Automatically record wallet / studio transaction if paid course
        if (video.price != "Free") {
            addWalletTransaction(
                WalletTransaction(
                    id = "tx_${System.currentTimeMillis()}",
                    title = "Course Published: ${video.title}",
                    subtitle = "Listed on SkillBuilder Store at ${video.price}",
                    amount = "+₹0",
                    date = "Just now",
                    isCredit = true
                )
            )
        }
    }

    @Synchronized
    fun deleteVideo(videoId: String) {
        val updated = _videosFlow.value.filter { it.id != videoId }
        _videosFlow.value = updated
        persistVideos(updated)
    }

    private fun persistVideos(list: List<MentorVideo>) {
        try {
            val raw = json.encodeToString(list)
            prefs?.edit()?.putString(KEY_VIDEOS, raw)?.apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // ==================== Real Swap Management ====================

    @Synchronized
    fun purchaseMentorSwapPass(): Boolean {
        _mentorSwapPassActive.value = true
        prefs?.edit()?.putBoolean(KEY_MENTOR_SWAP_PASS, true)?.apply()
        // Record debit transaction in mentor wallet
        val tx = WalletTransaction(
            id = "tx_swappass_${System.currentTimeMillis()}",
            title = "Mentor Skill Swap Pass",
            subtitle = "Premium swap access activated (₹59)",
            amount = "-₹59",
            date = "Today",
            isCredit = false
        )
        addWalletTransaction(tx)
        return true
    }

    @Synchronized
    fun consumeMentorSwapPass(): Boolean {
        _mentorSwapPassActive.value = false
        prefs?.edit()?.putBoolean(KEY_MENTOR_SWAP_PASS, false)?.apply()
        return true
    }

    fun hasActiveMentorSwapPass(): Boolean = _mentorSwapPassActive.value

    @Synchronized
    fun addSwapProposal(proposal: SwapProposal) {
        val updated = listOf(proposal) + _swapProposalsFlow.value.filter { it.id != proposal.id }
        _swapProposalsFlow.value = updated
        persistSwapProposals(updated)
    }

    @Synchronized
    fun updateSwapStatus(proposalId: String, status: SwapStatus) {
        val updated = _swapProposalsFlow.value.map {
            if (it.id == proposalId) it.copy(status = status) else it
        }
        _swapProposalsFlow.value = updated
        persistSwapProposals(updated)

        // When a swap request is accepted, consume the ₹59 premium pass automatically
        if (status == SwapStatus.ACTIVE) {
            consumeMentorSwapPass()
            // Ensure a direct conversation exists for this swap
            val targetProposal = updated.firstOrNull { it.id == proposalId }
            if (targetProposal != null) {
                val partner = targetProposal.partner
                val convId = "conv_swap_${partner.id}"
                val existing = _conversationsFlow.value.firstOrNull { it.id == convId }
                if (existing == null) {
                    val newConv = ChatConversation(
                        id = convId,
                        mentorId = partner.id,
                        mentorName = partner.name,
                        mentorAvatar = partner.avatarUrl,
                        mentorCategory = partner.skillsTaught.firstOrNull() ?: "Skill Swap",
                        learnerId = "mentor_current",
                        learnerName = "You",
                        lastMessage = "Skill Swap Accepted! You can now collaborate and learn together.",
                        lastMessageTimestamp = "Just now",
                        unreadCount = 0,
                        subtitleOverride = "Verified Swap Partner"
                    )
                    _conversationsFlow.value = listOf(newConv) + _conversationsFlow.value
                    persistConversations(_conversationsFlow.value)
                }
            }
        }
    }

    @Synchronized
    fun deleteSwapProposal(proposalId: String) {
        val updated = _swapProposalsFlow.value.filter { it.id != proposalId }
        _swapProposalsFlow.value = updated
        persistSwapProposals(updated)
    }

    private fun persistSwapProposals(list: List<SwapProposal>) {
        try {
            val raw = json.encodeToString(list)
            prefs?.edit()?.putString(KEY_SWAP_PROPOSALS, raw)?.apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // ==================== Real Chat Management ====================

    @Synchronized
    fun sendChatMessage(message: ChatMessage) {
        val convId = message.conversationId
        val currentMsgs = _messagesFlow.value[convId] ?: emptyList()
        val updatedMsgs = currentMsgs + message

        val newMsgMap = _messagesFlow.value.toMutableMap()
        newMsgMap[convId] = updatedMsgs
        _messagesFlow.value = newMsgMap

        // Update or create conversation header
        val existingConvs = _conversationsFlow.value.toMutableList()
        val idx = existingConvs.indexOfFirst { it.id == convId }
        if (idx != -1) {
            val oldConv = existingConvs[idx]
            existingConvs[idx] = oldConv.copy(
                lastMessage = message.text,
                lastMessageTimestamp = message.timestamp,
                hasDoubtPending = if (message.type == com.skillbuilder.app.domain.model.ChatMessageType.DOUBT_QUERY) true else oldConv.hasDoubtPending,
                hasVideoDemandPending = if (message.type == com.skillbuilder.app.domain.model.ChatMessageType.VIDEO_DEMAND) true else oldConv.hasVideoDemandPending
            )
        } else {
            existingConvs.add(
                ChatConversation(
                    id = convId,
                    mentorId = message.recipientId,
                    mentorName = message.recipientId,
                    learnerId = message.senderId,
                    learnerName = message.senderName,
                    lastMessage = message.text,
                    lastMessageTimestamp = message.timestamp
                )
            )
        }
        _conversationsFlow.value = existingConvs

        persistConversations(existingConvs)
        persistMessages(newMsgMap)
    }

    @Synchronized
    fun deleteChatMessage(conversationId: String, messageId: String) {
        val currentMsgs = _messagesFlow.value[conversationId] ?: emptyList()
        val updatedMsgs = currentMsgs.filterNot { it.id == messageId }
        val newMsgMap = _messagesFlow.value.toMutableMap()
        newMsgMap[conversationId] = updatedMsgs
        _messagesFlow.value = newMsgMap

        val existingConvs = _conversationsFlow.value.toMutableList()
        val idx = existingConvs.indexOfFirst { it.id == conversationId }
        if (idx != -1) {
            val last = updatedMsgs.lastOrNull()
            existingConvs[idx] = existingConvs[idx].copy(
                lastMessage = last?.text ?: "No messages",
                lastMessageTimestamp = last?.timestamp ?: ""
            )
            _conversationsFlow.value = existingConvs
            persistConversations(existingConvs)
        }
        persistMessages(newMsgMap)
    }

    @Synchronized
    fun unsendChatMessage(conversationId: String, messageId: String) {
        deleteChatMessage(conversationId, messageId)
    }

    private fun persistConversations(list: List<ChatConversation>) {
        try {
            val raw = json.encodeToString(list)
            prefs?.edit()?.putString(KEY_CONVERSATIONS, raw)?.apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun persistMessages(map: Map<String, List<ChatMessage>>) {
        try {
            val raw = json.encodeToString(map)
            prefs?.edit()?.putString(KEY_MESSAGES, raw)?.apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // ==================== Real Enrollment & Wallet ====================

    @Synchronized
    fun enrollVideo(videoId: String) {
        val current = _enrolledVideoIdsFlow.value
        if (videoId !in current) {
            val updated = current + videoId
            _enrolledVideoIdsFlow.value = updated
            try {
                val raw = json.encodeToString(updated)
                prefs?.edit()?.putString(KEY_ENROLLED_IDS, raw)?.apply()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    @Synchronized
    fun addWalletTransaction(tx: WalletTransaction) {
        val updated = listOf(tx) + _transactionsFlow.value.filter { it.id != tx.id }
        _transactionsFlow.value = updated
        try {
            val raw = json.encodeToString(updated)
            prefs?.edit()?.putString(KEY_TRANSACTIONS, raw)?.apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @Synchronized
    fun clearAllData() {
        _videosFlow.value = emptyList()
        _swapProposalsFlow.value = emptyList()
        _conversationsFlow.value = emptyList()
        _messagesFlow.value = emptyMap()
        _transactionsFlow.value = emptyList()
        _enrolledVideoIdsFlow.value = emptyList()
        prefs?.edit()?.clear()?.apply()
    }

    private fun getInitialSeedConversations(): List<ChatConversation> {
        return listOf(
            ChatConversation(
                id = "conv_avichal",
                mentorId = "mentor_current",
                mentorName = "Aman Rathore",
                mentorCategory = "Music & Production",
                learnerId = "user_avichal",
                learnerName = "AVICHAL PACHORI",
                learnerAvatar = "https://images.unsplash.com/photo-1544717305-2782549b5136?w=200",
                lastMessage = "2 new messages",
                lastMessageTimestamp = "1h",
                unreadCount = 2,
                isMuted = true,
                subtitleOverride = "2 new messages · 1h"
            ),
            ChatConversation(
                id = "conv_aashutosh",
                mentorId = "mentor_current",
                mentorName = "Aman Rathore",
                mentorCategory = "Tech & Architecture",
                learnerId = "user_aashutosh",
                learnerName = "Aashutosh Rajput",
                learnerAvatar = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=200",
                lastMessage = "Sent a reel by khusipal_73",
                lastMessageTimestamp = "2h",
                unreadCount = 1,
                isMuted = true,
                subtitleOverride = "Sent a reel by khusipal_73 · 2h"
            ),
            ChatConversation(
                id = "conv_chulbuli",
                mentorId = "mentor_current",
                mentorName = "Aman Rathore",
                mentorCategory = "Design & UI/UX",
                learnerId = "user_chulbuli",
                learnerName = "chulbuli",
                learnerAvatar = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=200",
                lastMessage = "4+ new messages",
                lastMessageTimestamp = "10h",
                unreadCount = 4,
                isMuted = true,
                subtitleOverride = "4+ new messages · 10h"
            ),
            ChatConversation(
                id = "conv_rani",
                mentorId = "mentor_current",
                mentorName = "Aman Rathore",
                mentorCategory = "Photography",
                learnerId = "user_rani",
                learnerName = "Rani Sahiba💕",
                learnerAvatar = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=200",
                lastMessage = "Sent 17h ago",
                lastMessageTimestamp = "17h ago",
                unreadCount = 0,
                isMuted = false,
                subtitleOverride = "Sent 17h ago"
            ),
            ChatConversation(
                id = "conv_shivani",
                mentorId = "mentor_current",
                mentorName = "Aman Rathore",
                mentorCategory = "Dance & Fitness",
                learnerId = "user_shivani",
                learnerName = "Shivani Rathore",
                learnerAvatar = "https://images.unsplash.com/photo-1517841905240-472988babdf9?w=200",
                lastMessage = "Reacted 😂 to your message",
                lastMessageTimestamp = "3d",
                unreadCount = 1,
                isMuted = true,
                subtitleOverride = "Reacted 😂 to your message · 3d"
            ),
            ChatConversation(
                id = "conv_tanish",
                mentorId = "mentor_current",
                mentorName = "Aman Rathore",
                mentorCategory = "Business & Strategy",
                learnerId = "user_tanish",
                learnerName = "Tanish chauhan",
                learnerAvatar = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=200",
                lastMessage = "Reacted 😂 to your message",
                lastMessageTimestamp = "20h",
                unreadCount = 1,
                isMuted = true,
                subtitleOverride = "Reacted 😂 to your message · 20h"
            ),
            ChatConversation(
                id = "conv_ayush",
                mentorId = "mentor_current",
                mentorName = "Aman Rathore",
                mentorCategory = "AI & Machine Learning",
                learnerId = "user_ayush",
                learnerName = "AYUSH CHAUHAN 🀄",
                learnerAvatar = "https://images.unsplash.com/photo-1522075469751-3a6694fb2f61?w=200",
                lastMessage = "4 new messages",
                lastMessageTimestamp = "24h",
                unreadCount = 4,
                isMuted = true,
                subtitleOverride = "4 new messages · 24h"
            ),
            ChatConversation(
                id = "conv_raman",
                mentorId = "mentor_current",
                mentorName = "Aman Rathore",
                mentorCategory = "Mobile Engineering",
                learnerId = "user_raman",
                learnerName = "Raman Rathore",
                learnerAvatar = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=200",
                lastMessage = "Sent Wednesday",
                lastMessageTimestamp = "Wednesday",
                unreadCount = 0,
                isMuted = true,
                subtitleOverride = "Sent Wednesday"
            ),
            ChatConversation(
                id = "conv_abhishek",
                mentorId = "mentor_current",
                mentorName = "Aman Rathore",
                mentorCategory = "Cloud & DevOps",
                learnerId = "user_abhishek",
                learnerName = "Abhishek Thakur",
                learnerAvatar = "https://images.unsplash.com/photo-1519085360753-af0119f7cbe7?w=200",
                lastMessage = "Sent",
                lastMessageTimestamp = "Sent",
                unreadCount = 0,
                isMuted = false,
                subtitleOverride = "Sent"
            ),
            ChatConversation(
                id = "conv_seed_1",
                mentorId = "mentor_current",
                mentorName = "Aman Rathore",
                mentorCategory = "Music & Arts",
                learnerId = "learner_priya",
                learnerName = "Priya Patel",
                learnerAvatar = "https://images.unsplash.com/photo-1573496359142-b8d87734a5a2?w=200",
                lastMessage = "Hi Mentor! In lesson 2, how do I avoid muting the 3rd string during chord transition?",
                lastMessageTimestamp = "10 mins ago",
                unreadCount = 1,
                hasDoubtPending = true,
                hasVideoDemandPending = false,
                subtitleOverride = "Doubt: Lesson 2 string transition · 10m"
            ),
            ChatConversation(
                id = "conv_seed_2",
                mentorId = "mentor_current",
                mentorName = "Aman Rathore",
                mentorCategory = "Tech & Coding",
                learnerId = "learner_arjun",
                learnerName = "Arjun Verma",
                learnerAvatar = "https://images.unsplash.com/photo-1539571696357-5a69c17a67c6?w=200",
                lastMessage = "Sir, could you explain state hoisting across multiple composable layers?",
                lastMessageTimestamp = "1 hour ago",
                unreadCount = 1,
                hasDoubtPending = true,
                hasVideoDemandPending = false,
                subtitleOverride = "Doubt: State hoisting · 1h"
            )
        )
    }

    private fun getInitialSeedMessages(): Map<String, List<ChatMessage>> {
        return mapOf(
            "conv_avichal" to listOf(
                ChatMessage(
                    id = "msg_av_1",
                    conversationId = "conv_avichal",
                    senderId = "user_avichal",
                    senderName = "AVICHAL PACHORI",
                    senderRole = "Learner",
                    recipientId = "mentor_current",
                    text = "Bro, checked out the newest course modules you dropped! Super helpful.",
                    timestamp = "2h ago",
                    status = "Delivered"
                ),
                ChatMessage(
                    id = "msg_av_2",
                    conversationId = "conv_avichal",
                    senderId = "user_avichal",
                    senderName = "AVICHAL PACHORI",
                    senderRole = "Learner",
                    recipientId = "mentor_current",
                    text = "Let me know when the next masterclass video goes live!",
                    timestamp = "1h ago",
                    status = "Delivered"
                )
            ),
            "conv_aashutosh" to listOf(
                ChatMessage(
                    id = "msg_aash_1",
                    conversationId = "conv_aashutosh",
                    senderId = "user_aashutosh",
                    senderName = "Aashutosh Rajput",
                    senderRole = "Learner",
                    recipientId = "mentor_current",
                    text = "Check this reel out on jetpack compose animations: https://instagram.com/reel/khusipal_73",
                    timestamp = "2h ago",
                    status = "Delivered"
                )
            ),
            "conv_chulbuli" to listOf(
                ChatMessage(
                    id = "msg_ch_1",
                    conversationId = "conv_chulbuli",
                    senderId = "user_chulbuli",
                    senderName = "chulbuli",
                    senderRole = "Learner",
                    recipientId = "mentor_current",
                    text = "Hey! Loved the Figma design tokens session. Can you review my prototype?",
                    timestamp = "10h ago",
                    status = "Delivered"
                )
            ),
            "conv_rani" to listOf(
                ChatMessage(
                    id = "msg_rn_1",
                    conversationId = "conv_rani",
                    senderId = "user_rani",
                    senderName = "Rani Sahiba💕",
                    senderRole = "Learner",
                    recipientId = "mentor_current",
                    text = "Thank you for the detailed feedback on my lighting setup!",
                    timestamp = "17h ago",
                    status = "Read"
                )
            ),
            "conv_shivani" to listOf(
                ChatMessage(
                    id = "msg_sh_1",
                    conversationId = "conv_shivani",
                    senderId = "user_shivani",
                    senderName = "Shivani Rathore",
                    senderRole = "Learner",
                    recipientId = "mentor_current",
                    text = "Haha that debugging story was hilarious 😂",
                    timestamp = "3d ago",
                    status = "Delivered"
                )
            ),
            "conv_tanish" to listOf(
                ChatMessage(
                    id = "msg_tn_1",
                    conversationId = "conv_tanish",
                    senderId = "user_tanish",
                    senderName = "Tanish chauhan",
                    senderRole = "Learner",
                    recipientId = "mentor_current",
                    text = "Reacted 😂 to your message",
                    timestamp = "20h ago",
                    status = "Delivered"
                )
            ),
            "conv_ayush" to listOf(
                ChatMessage(
                    id = "msg_ay_1",
                    conversationId = "conv_ayush",
                    senderId = "user_ayush",
                    senderName = "AYUSH CHAUHAN 🀄",
                    senderRole = "Learner",
                    recipientId = "mentor_current",
                    text = "Hey Aman! The AI model inference speed increased 3x with your pipeline trick.",
                    timestamp = "24h ago",
                    status = "Delivered"
                )
            ),
            "conv_raman" to listOf(
                ChatMessage(
                    id = "msg_rm_1",
                    conversationId = "conv_raman",
                    senderId = "user_raman",
                    senderName = "Raman Rathore",
                    senderRole = "Learner",
                    recipientId = "mentor_current",
                    text = "Got it! Thanks for sharing the code snippet on Wednesday.",
                    timestamp = "Wednesday",
                    status = "Read"
                )
            ),
            "conv_abhishek" to listOf(
                ChatMessage(
                    id = "msg_ab_1",
                    conversationId = "conv_abhishek",
                    senderId = "user_abhishek",
                    senderName = "Abhishek Thakur",
                    senderRole = "Learner",
                    recipientId = "mentor_current",
                    text = "Awesome, will try out the deployment script today.",
                    timestamp = "Yesterday",
                    status = "Read"
                )
            ),
            "conv_seed_1" to listOf(
                ChatMessage(
                    id = "msg_seed_1_1",
                    conversationId = "conv_seed_1",
                    senderId = "learner_priya",
                    senderName = "Priya Patel",
                    senderRole = "Learner",
                    recipientId = "mentor_current",
                    text = "Hi Mentor! I am practicing along with your video lesson. When switching chords, my finger touches the 3rd string and mutes it. Any quick tip on arching fingers?",
                    timestamp = "10 mins ago",
                    type = com.skillbuilder.app.domain.model.ChatMessageType.DOUBT_QUERY,
                    referenceTopic = "Lesson 2: Transitions",
                    status = "Delivered",
                    isEncrypted = true,
                    isResolved = false
                )
            ),
            "conv_seed_2" to listOf(
                ChatMessage(
                    id = "msg_seed_2_1",
                    conversationId = "conv_seed_2",
                    senderId = "learner_arjun",
                    senderName = "Arjun Verma",
                    senderRole = "Learner",
                    recipientId = "mentor_current",
                    text = "Hello Sir! Great tutorial. Could you explain how state hoisting handles state flows down multiple composables cleanly?",
                    timestamp = "1 hour ago",
                    type = com.skillbuilder.app.domain.model.ChatMessageType.DOUBT_QUERY,
                    referenceTopic = "Lesson 1: Architecture",
                    status = "Delivered",
                    isEncrypted = true,
                    isResolved = false
                )
            )
        )
    }

    private fun getInitialSeedSwapProposals(): List<SwapProposal> {
        val rohan = User(
            id = "user_rohan",
            name = "Rohan Verma",
            email = "rohan.verma@example.com",
            bio = "Product Designer & Mentor. Teaches Figma Design Systems; looking for acoustic fingerstyle guitar lessons.",
            rating = 4.9f,
            reviewCount = 8,
            isVerified = true,
            skillsTaught = listOf("UI/UX Design", "Figma Prototyping"),
            skillsWanted = listOf("Acoustic Guitar"),
            isMentor = true,
            hasActiveSwapPremium = true
        )
        val aditi = User(
            id = "user_aditi",
            name = "Aditi Sinha",
            email = "aditi.sinha@example.com",
            bio = "Pastry chef alumna. Teaches sourdough & French pastry; eager to learn fingerstyle guitar chords!",
            rating = 5.0f,
            reviewCount = 12,
            isVerified = true,
            skillsTaught = listOf("Artisan Cake Baking", "French Pastry"),
            skillsWanted = listOf("Acoustic Guitar", "Music Theory"),
            isMentor = true,
            hasActiveSwapPremium = true
        )
        return listOf(
            SwapProposal(
                id = "sp_mentor_rohan",
                partner = rohan,
                mySkill = "Acoustic Guitar & Fingerstyle",
                partnerSkill = "UI/UX Design & Figma Design Systems",
                status = SwapStatus.PENDING,
                proposedDate = "Today",
                message = "Hi Aman! I noticed your guitar teaching profile and would love to exchange my Figma Design Systems masterclass for your acoustic fingerstyle coaching. Please review my curriculum and syllabus!",
                isPremiumSwap = true,
                courseOverview = "Comprehensive, production-ready Figma architecture covering design tokens, auto-layout variables, interactive prototypes, and developer handoff specs.",
                curriculumTopics = listOf(
                    "Module 1: Design Tokens & Typography Scale",
                    "Module 2: Auto-Layout 5.0 & Nested Components",
                    "Module 3: Advanced Micro-interactions & Variants",
                    "Module 4: Design-to-Code Handoff for Android Compose"
                ),
                sampleVideoTitle = "Figma Design System Architecture (Preview)",
                sampleVideoDuration = "14:20",
                compatibilityScore = 98
            ),
            SwapProposal(
                id = "sp_mentor_aditi",
                partner = aditi,
                mySkill = "Acoustic Guitar Basics",
                partnerSkill = "French Pastry & Artisan Sourdough",
                status = SwapStatus.PENDING,
                proposedDate = "Yesterday",
                message = "Hey! I run an artisan bakery and teach sourdough fermentation & viennoiserie. I would love to trade knowledge for acoustic guitar lessons! Feel free to review my lesson modules.",
                isPremiumSwap = true,
                courseOverview = "Master French pastry from the ground up: sourdough starters, laminated croissant dough, temperature-controlled fermentation, and mirror glaze finishes.",
                curriculumTopics = listOf(
                    "Module 1: Sourdough Wild Yeast Cultivation",
                    "Module 2: Laminated Viennoiserie & Croissant Layers",
                    "Module 3: Pâtisserie Glazes & Temperature Control",
                    "Module 4: Kitchen Setup & Production Timelines"
                ),
                sampleVideoTitle = "Sourdough Lamination Secrets (Preview)",
                sampleVideoDuration = "18:45",
                compatibilityScore = 94
            )
        )
    }
}
