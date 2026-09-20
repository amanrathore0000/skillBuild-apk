package com.skillbuilder.app.data.local

import android.content.Context
import android.content.SharedPreferences
import com.skillbuilder.app.domain.model.ChatConversation
import com.skillbuilder.app.domain.model.ChatMessage
import com.skillbuilder.app.domain.model.Course
import com.skillbuilder.app.domain.model.MentorVideo
import com.skillbuilder.app.domain.model.SwapProposal
import com.skillbuilder.app.domain.model.SwapStatus
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
    private const val KEY_SWAP_PROPOSALS = "rt_swap_proposals_json"
    private const val KEY_CONVERSATIONS = "rt_conversations_json"
    private const val KEY_MESSAGES = "rt_messages_json"
    private const val KEY_TRANSACTIONS = "rt_transactions_json"
    private const val KEY_ENROLLED_IDS = "rt_enrolled_ids_json"

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
                _swapProposalsFlow.value = json.decodeFromString(rawSwaps)
            } catch (e: Exception) {
                _swapProposalsFlow.value = emptyList()
            }
        }

        // 3. Load Real Conversations
        val rawConvs = sp.getString(KEY_CONVERSATIONS, null)
        if (!rawConvs.isNullOrBlank()) {
            try {
                _conversationsFlow.value = json.decodeFromString(rawConvs)
            } catch (e: Exception) {
                _conversationsFlow.value = emptyList()
            }
        }

        // 4. Load Real Messages
        val rawMsgs = sp.getString(KEY_MESSAGES, null)
        if (!rawMsgs.isNullOrBlank()) {
            try {
                _messagesFlow.value = json.decodeFromString(rawMsgs)
            } catch (e: Exception) {
                _messagesFlow.value = emptyMap()
            }
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
}
