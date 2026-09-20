package com.skillbuilder.app.ui.screens.chat

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DoneAll
import androidx.compose.material.icons.rounded.HelpOutline
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.MovieCreation
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.QuestionAnswer
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material.icons.rounded.ThumbUp
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material.icons.rounded.VideoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil3.compose.AsyncImage
import com.skillbuilder.app.data.local.SampleData
import com.skillbuilder.app.data.local.UserSession
import com.skillbuilder.app.domain.model.ChatConversation
import com.skillbuilder.app.domain.model.ChatMessage
import com.skillbuilder.app.domain.model.ChatMessageType
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EncryptedChatScreen(
    initialConversationId: String? = null,
    initialMentorId: String? = null,
    initialMentorName: String? = null,
    initialReferenceTopic: String? = null,
    initialOpenAskDoubt: Boolean = false,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val currentUser by UserSession.currentUser.collectAsState()
    val isMentor = currentUser.isMentor

    val allConversations by SampleData.chatConversationsFlow.collectAsState()
    val allMessages by SampleData.chatMessagesFlow.collectAsState()

    val initialTargetConv = remember(initialConversationId, initialMentorId, initialMentorName) {
        if (initialConversationId != null) {
            allConversations.find { it.id == initialConversationId }
        } else if (initialMentorId != null || initialMentorName != null) {
            val mentor = SampleData.mentors.find {
                (initialMentorId != null && it.id == initialMentorId) ||
                (initialMentorName != null && it.name.equals(initialMentorName, ignoreCase = true))
            }
            val mentorName = initialMentorName ?: mentor?.name ?: "Mentor"
            val mId = mentor?.id ?: initialMentorId ?: "mentor_${mentorName.replace(" ", "_").lowercase()}"
            SampleData.getOrCreateConversation(
                mentorId = mId,
                mentorName = mentorName,
                mentorAvatar = mentor?.avatarUrl,
                mentorCategory = mentor?.skillsTaught?.firstOrNull() ?: "General",
                learnerId = currentUser.id,
                learnerName = currentUser.name,
                learnerAvatar = currentUser.avatarUrl
            )
        } else null
    }

    var activeConversationId by remember(initialTargetConv) {
        mutableStateOf(initialTargetConv?.id ?: initialConversationId)
    }

    // Auto-create/find conversation if initiated with a mentorId
    LaunchedEffect(initialMentorId, initialMentorName) {
        if ((initialMentorId != null || initialMentorName != null) && activeConversationId == null) {
            val mentor = SampleData.mentors.find {
                (initialMentorId != null && it.id == initialMentorId) ||
                (initialMentorName != null && it.name.equals(initialMentorName, ignoreCase = true))
            }
            val mentorName = initialMentorName ?: mentor?.name ?: "Mentor"
            val mId = mentor?.id ?: initialMentorId ?: "mentor_${mentorName.replace(" ", "_").lowercase()}"
            val conv = SampleData.getOrCreateConversation(
                mentorId = mId,
                mentorName = mentorName,
                mentorAvatar = mentor?.avatarUrl,
                mentorCategory = mentor?.skillsTaught?.firstOrNull() ?: "General",
                learnerId = currentUser.id,
                learnerName = currentUser.name,
                learnerAvatar = currentUser.avatarUrl
            )
            activeConversationId = conv.id
        }
    }

    var showAskDoubtModal by remember { mutableStateOf(initialOpenAskDoubt) }
    var showDemandVideoModal by remember { mutableStateOf(false) }
    var showNewChatModal by remember { mutableStateOf(false) }

    val activeConversation = allConversations.find { it.id == activeConversationId }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .imePadding()
            ) {
                // Top E2EE Security Bar
                SecurityBannerHeader(
                    activeConversation = activeConversation,
                    currentUserId = currentUser.id,
                    currentUserName = currentUser.name,
                    isMentor = isMentor,
                    onBack = {
                        if (activeConversationId != null) {
                            activeConversationId = null
                        } else {
                            onDismiss()
                        }
                    },
                    onClose = onDismiss
                )

                if (activeConversationId == null) {
                    // Inbox / Conversations List
                    ConversationsInboxView(
                        conversations = allConversations,
                        currentUserId = currentUser.id,
                        currentUserName = currentUser.name,
                        isMentor = isMentor,
                        onSelectConversation = { conv ->
                            activeConversationId = conv.id
                        },
                        onStartNewChat = { showNewChatModal = true }
                    )
                } else {
                    // Active Direct Chat Thread
                    activeConversation?.let { conv ->
                        val threadMessages = allMessages.filter { it.conversationId == conv.id }

                        ActiveChatThreadView(
                            conversation = conv,
                            messages = threadMessages,
                            currentUserId = currentUser.id,
                            currentUserName = currentUser.name,
                            isMentor = isMentor,
                            initialReferenceTopic = initialReferenceTopic,
                            onOpenAskDoubt = { showAskDoubtModal = true },
                            onOpenDemandVideo = { showDemandVideoModal = true }
                        )
                    }
                }
            }
        }

        // Modal: Ask Doubt
        if (showAskDoubtModal && activeConversation != null) {
            AskDoubtDialog(
                mentorName = activeConversation.mentorName,
                prefilledTopic = initialReferenceTopic,
                onDismiss = { showAskDoubtModal = false },
                onSubmit = { doubtTopic, doubtText ->
                    SampleData.sendChatMessage(
                        conversationId = activeConversation.id,
                        senderId = currentUser.id,
                        senderName = currentUser.name,
                        senderRole = if (isMentor) "Mentor" else "Learner",
                        recipientId = activeConversation.mentorId,
                        text = doubtText,
                        type = ChatMessageType.DOUBT_QUERY,
                        referenceTopic = doubtTopic
                    )
                    showAskDoubtModal = false
                    Toast.makeText(context, "Encrypted doubt sent to ${activeConversation.mentorName}!", Toast.LENGTH_SHORT).show()
                }
            )
        }

        // Modal: Demand / Request Video
        if (showDemandVideoModal && activeConversation != null) {
            DemandVideoDialog(
                mentorName = activeConversation.mentorName,
                onDismiss = { showDemandVideoModal = false },
                onSubmit = { videoTopic, description ->
                    SampleData.sendChatMessage(
                        conversationId = activeConversation.id,
                        senderId = currentUser.id,
                        senderName = currentUser.name,
                        senderRole = if (isMentor) "Mentor" else "Learner",
                        recipientId = activeConversation.mentorId,
                        text = description,
                        type = ChatMessageType.VIDEO_DEMAND,
                        referenceTopic = videoTopic
                    )
                    showDemandVideoModal = false
                    Toast.makeText(context, "Video request delivered to ${activeConversation.mentorName}'s Creator Studio!", Toast.LENGTH_SHORT).show()
                }
            )
        }

        // Modal: Start New Chat with a Platform Mentor
        if (showNewChatModal) {
            SelectMentorToChatDialog(
                onDismiss = { showNewChatModal = false },
                onSelectMentor = { mentor ->
                    val conv = SampleData.getOrCreateConversation(
                        mentorId = mentor.id,
                        mentorName = mentor.name,
                        mentorAvatar = mentor.avatarUrl,
                        mentorCategory = mentor.skillsTaught.firstOrNull() ?: "General",
                        learnerId = currentUser.id,
                        learnerName = currentUser.name,
                        learnerAvatar = currentUser.avatarUrl
                    )
                    activeConversationId = conv.id
                    showNewChatModal = false
                }
            )
        }
    }
}

// =========================================================================
// HEADER
// =========================================================================

@Composable
private fun SecurityBannerHeader(
    activeConversation: ChatConversation?,
    currentUserId: String = "",
    currentUserName: String = "",
    isMentor: Boolean = false,
    onBack: () -> Unit,
    onClose: () -> Unit
) {
    val isCurrentUserMentorInConv = activeConversation != null && (activeConversation.mentorId == currentUserId || activeConversation.mentorName.equals(currentUserName, ignoreCase = true))
    val partnerName = if (activeConversation != null) {
        if (isCurrentUserMentorInConv) activeConversation.learnerName else activeConversation.mentorName
    } else ""
    val partnerCategory = if (activeConversation != null) {
        if (isCurrentUserMentorInConv) "Learner" else "${activeConversation.mentorCategory} Mentor"
    } else ""

    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Rounded.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            if (activeConversation != null) {
                Spacer(modifier = Modifier.width(4.dp))
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                    modifier = Modifier.size(38.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = partnerName.take(1),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 16.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = partnerName,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Rounded.Verified,
                            contentDescription = "Verified",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Text(
                        text = partnerCategory,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Chats",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

// =========================================================================
// CONVERSATIONS INBOX VIEW
// =========================================================================

@Composable
private fun ConversationsInboxView(
    conversations: List<ChatConversation>,
    currentUserId: String = "",
    currentUserName: String = "",
    isMentor: Boolean,
    onSelectConversation: (ChatConversation) -> Unit,
    onStartNewChat: () -> Unit
) {
    var selectedFilterIndex by remember { mutableIntStateOf(0) }
    val filterTabs = listOf("All Chats", "Doubts ❓", "Video Requests 🎬")
    var searchQuery by remember { mutableStateOf("") }

    val filteredList = remember(conversations, selectedFilterIndex, searchQuery, isMentor) {
        conversations.filter { conv ->
            val matchesSearch = if (isMentor) {
                conv.learnerName.contains(searchQuery, ignoreCase = true) || conv.lastMessage.contains(searchQuery, ignoreCase = true)
            } else {
                conv.mentorName.contains(searchQuery, ignoreCase = true) || conv.mentorCategory.contains(searchQuery, ignoreCase = true) || conv.lastMessage.contains(searchQuery, ignoreCase = true)
            }

            val matchesTab = when (selectedFilterIndex) {
                1 -> conv.hasDoubtPending || conv.lastMessage.contains("Doubt:", ignoreCase = true)
                2 -> conv.hasVideoDemandPending || conv.lastMessage.contains("Video Request:", ignoreCase = true)
                else -> true
            }

            matchesSearch && matchesTab
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Search and Start Chat Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search conversations...") },
                leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(
                                Icons.Rounded.Close,
                                contentDescription = "Clear",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            if (!isMentor) {
                Button(
                    onClick = onStartNewChat,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp)
                ) {
                    Icon(Icons.Rounded.QuestionAnswer, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("New Chat", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Sub-filter tabs
        ScrollableTabRow(
            selectedTabIndex = selectedFilterIndex,
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.primary,
            edgePadding = 16.dp,
            divider = {}
        ) {
            filterTabs.forEachIndexed { idx, title ->
                Tab(
                    selected = selectedFilterIndex == idx,
                    onClick = { selectedFilterIndex = idx },
                    text = {
                        Text(
                            text = title,
                            fontWeight = if (selectedFilterIndex == idx) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 13.sp
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (filteredList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Rounded.QuestionAnswer,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No encrypted conversations found",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isMentor) "Learner doubts and video requests will appear here." else "Tap 'New Chat' to ask a doubt or request a video from any mentor.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredList, key = { it.id }) { conv ->
                    ConversationItemCard(
                        conversation = conv,
                        currentUserId = currentUserId,
                        currentUserName = currentUserName,
                        isMentor = isMentor,
                        onClick = { onSelectConversation(conv) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ConversationItemCard(
    conversation: ChatConversation,
    currentUserId: String = "",
    currentUserName: String = "",
    isMentor: Boolean,
    onClick: () -> Unit
) {
    val isCurrentUserMentorInConv = conversation.mentorId == currentUserId || conversation.mentorName.equals(currentUserName, ignoreCase = true)
    val partnerName = if (isCurrentUserMentorInConv) conversation.learnerName else conversation.mentorName

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                modifier = Modifier.size(46.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = partnerName.take(1),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = partnerName,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Rounded.Verified,
                            contentDescription = "Verified",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(13.dp)
                        )
                    }

                    Text(
                        text = conversation.lastMessageTimestamp,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Tag badge if doubt or video demand
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (conversation.hasDoubtPending || conversation.lastMessage.startsWith("Doubt:")) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFFF59E0B).copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "DOUBT QUERY",
                                color = Color(0xFFD97706),
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                    } else if (conversation.hasVideoDemandPending || conversation.lastMessage.startsWith("Video Request:")) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFF8B5CF6).copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "VIDEO DEMAND",
                                color = Color(0xFF7C3AED),
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                    }

                    Text(
                        text = conversation.lastMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            if (conversation.unreadCount > 0) {
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "${conversation.unreadCount}",
                            color = MaterialTheme.colorScheme.onPrimary,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }
    }
}

// =========================================================================
// ACTIVE CHAT THREAD VIEW
// =========================================================================

@Composable
private fun ActiveChatThreadView(
    conversation: ChatConversation,
    messages: List<ChatMessage>,
    currentUserId: String,
    currentUserName: String,
    isMentor: Boolean,
    initialReferenceTopic: String? = null,
    onOpenAskDoubt: () -> Unit,
    onOpenDemandVideo: () -> Unit
) {
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val isCurrentUserMentorInConv = conversation.mentorId == currentUserId || conversation.mentorName.equals(currentUserName, ignoreCase = true)
    val partnerName = if (isCurrentUserMentorInConv) conversation.learnerName else conversation.mentorName
    var inputText by remember {
        mutableStateOf(
            if (!initialReferenceTopic.isNullOrBlank()) "Hi $partnerName, I have a doubt regarding \"$initialReferenceTopic\": " else ""
        )
    }

    // Auto scroll to bottom when new message arrives
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Message Stream
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 14.dp),
            contentPadding = PaddingValues(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // E2EE System Disclaimer Card
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Rounded.Lock,
                            contentDescription = null,
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "End-to-End Encrypted Session",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Messages, doubt queries, and video demands are securely encrypted. Only ${conversation.learnerName} and ${conversation.mentorName} have the decryption keys.",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            items(messages, key = { it.id }) { message ->
                val isOutgoing = message.senderId == currentUserId ||
                        (isMentor && message.senderRole == "Mentor") ||
                        (!isMentor && message.senderRole == "Learner" && message.senderId != conversation.mentorId)

                when (message.type) {
                    ChatMessageType.DOUBT_QUERY -> {
                        DoubtQueryBubble(
                            message = message,
                            isOutgoing = isOutgoing,
                            isMentor = isMentor,
                            onMarkResolved = {
                                SampleData.markDoubtResolved(message.id)
                            }
                        )
                    }
                    ChatMessageType.VIDEO_DEMAND -> {
                        VideoDemandBubble(
                            message = message,
                            isOutgoing = isOutgoing,
                            isMentor = isMentor,
                            onAcceptDemand = {
                                SampleData.acceptVideoDemand(message.id)
                            },
                            onUpvoteDemand = {
                                SampleData.upvoteVideoDemand(message.id)
                            }
                        )
                    }
                    ChatMessageType.STANDARD -> {
                        StandardChatBubble(
                            message = message,
                            isOutgoing = isOutgoing
                        )
                    }
                }
            }
        }

        // Quick Action Chips Row
        Surface(
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
        ) {
            Column {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (!isMentor) {
                        item {
                            QuickActionChip(
                                label = "Ask a Doubt ❓",
                                backgroundColor = Color(0xFFF59E0B).copy(alpha = 0.15f),
                                contentColor = Color(0xFFD97706),
                                onClick = onOpenAskDoubt
                            )
                        }
                        item {
                            QuickActionChip(
                                label = "1-on-1 Consultation 🤝",
                                backgroundColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                contentColor = MaterialTheme.colorScheme.primary,
                                onClick = {
                                    inputText = "Hi mentor, I would like to schedule a 1-on-1 consultation session regarding our curriculum."
                                }
                            )
                        }
                    } else {
                        item {
                            QuickActionChip(
                                label = "Send Doubt Answer 💡",
                                backgroundColor = Color(0xFF10B981).copy(alpha = 0.15f),
                                contentColor = Color(0xFF059669),
                                onClick = {
                                    inputText = "Here is the solution to your doubt: "
                                }
                            )
                        }
                        item {
                            QuickActionChip(
                                label = "Announce Video in Production 🎥",
                                backgroundColor = Color(0xFF8B5CF6).copy(alpha = 0.15f),
                                contentColor = Color(0xFF7C3AED),
                                onClick = {
                                    inputText = "Good news! I have added this requested video lesson to my Creator Studio schedule. Shooting starts this week!"
                                }
                            )
                        }
                    }
                }

                // Bottom Input Field Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = { Text("Encrypted message...", fontSize = 14.sp) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(24.dp),
                        singleLine = false,
                        maxLines = 4,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(46.dp).clip(CircleShape).clickable {
                            if (inputText.isNotBlank()) {
                                SampleData.sendChatMessage(
                                    conversationId = conversation.id,
                                    senderId = currentUserId,
                                    senderName = currentUserName,
                                    senderRole = if (isMentor) "Mentor" else "Learner",
                                    recipientId = if (isMentor) conversation.learnerId else conversation.mentorId,
                                    text = inputText.trim(),
                                    type = ChatMessageType.STANDARD
                                )
                                inputText = ""
                            }
                        }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Rounded.Send,
                                contentDescription = "Send",
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// =========================================================================
// MESSAGE BUBBLES: STANDARD, DOUBT, & VIDEO DEMAND
// =========================================================================

@Composable
private fun StandardChatBubble(
    message: ChatMessage,
    isOutgoing: Boolean
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isOutgoing) Alignment.End else Alignment.Start
    ) {
        Surface(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isOutgoing) 16.dp else 4.dp,
                bottomEnd = if (isOutgoing) 4.dp else 16.dp
            ),
            color = if (isOutgoing) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
            border = if (isOutgoing) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            modifier = Modifier.fillMaxWidth(0.82f)
        ) {
            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                Text(
                    text = message.text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isOutgoing) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.align(Alignment.End),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = message.timestamp,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = if (isOutgoing) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.75f) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (isOutgoing) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Rounded.DoneAll,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f),
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DoubtQueryBubble(
    message: ChatMessage,
    isOutgoing: Boolean,
    isMentor: Boolean,
    onMarkResolved: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isOutgoing) Alignment.End else Alignment.Start
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(0.88f),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.5.dp, Color(0xFFF59E0B).copy(alpha = 0.6f))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                // Doubt Header Badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFF59E0B).copy(alpha = 0.15f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Rounded.HelpOutline,
                                contentDescription = null,
                                tint = Color(0xFFD97706),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "STUDENT DOUBT QUERY",
                                color = Color(0xFFD97706),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp)
                            )
                        }
                    }

                    if (message.isResolved) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF10B981).copy(alpha = 0.15f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "Resolved",
                                    color = Color(0xFF10B981),
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Attached Course / Video Topic
                if (!message.referenceTopic.isNullOrBlank()) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Rounded.MenuBook,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = message.referenceTopic,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Doubt Query Text
                Text(
                    text = message.text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${message.senderName} • ${message.timestamp}",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (isMentor && !message.isResolved) {
                        Button(
                            onClick = onMarkResolved,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Icon(Icons.Rounded.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Mark Resolved", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun VideoDemandBubble(
    message: ChatMessage,
    isOutgoing: Boolean,
    isMentor: Boolean,
    onAcceptDemand: () -> Unit,
    onUpvoteDemand: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isOutgoing) Alignment.End else Alignment.Start
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(0.92f),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.5.dp, Color(0xFF8B5CF6).copy(alpha = 0.6f))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                // Header: Video Demand Badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFF8B5CF6).copy(alpha = 0.15f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Rounded.MovieCreation,
                                contentDescription = null,
                                tint = Color(0xFF7C3AED),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "TUTORIAL VIDEO DEMAND",
                                color = Color(0xFF7C3AED),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp)
                            )
                        }
                    }

                    if (message.isAccepted) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF10B981).copy(alpha = 0.15f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Rounded.VideoLibrary, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Added to Studio Schedule",
                                    color = Color(0xFF10B981),
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Requested Video Title
                Text(
                    text = message.referenceTopic ?: "Requested Topic",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Detail text / why learners want this
                Text(
                    text = message.text,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Votes & Mentor Accept Controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Learner Upvote Pill
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF8B5CF6).copy(alpha = 0.1f),
                        modifier = Modifier.clip(RoundedCornerShape(8.dp)).clickable { onUpvoteDemand() }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Rounded.ThumbUp, contentDescription = null, tint = Color(0xFF7C3AED), modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${message.demandVotes} learners want this",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                                color = Color(0xFF7C3AED)
                            )
                        }
                    }

                    if (isMentor && !message.isAccepted) {
                        Button(
                            onClick = onAcceptDemand,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED)),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Icon(Icons.Rounded.MovieCreation, contentDescription = null, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Accept to Schedule", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickActionChip(
    label: String,
    backgroundColor: Color,
    contentColor: Color,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = backgroundColor,
        border = BorderStroke(1.dp, contentColor.copy(alpha = 0.3f)),
        modifier = Modifier.clip(RoundedCornerShape(16.dp)).clickable { onClick() }
    ) {
        Text(
            text = label,
            color = contentColor,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        )
    }
}

// =========================================================================
// MODALS: ASK DOUBT & DEMAND VIDEO
// =========================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AskDoubtDialog(
    mentorName: String,
    prefilledTopic: String?,
    onDismiss: () -> Unit,
    onSubmit: (topic: String, doubt: String) -> Unit
) {
    var selectedTopic by remember {
        mutableStateOf(prefilledTopic ?: "Lesson 1: Acoustic Guitar Fundamentals")
    }
    var doubtText by remember { mutableStateOf("") }

    val suggestedTopics = listOf(
        "Lesson 1: Acoustic Guitar Anatomy & Barre Chords",
        "Lesson 2: Clean Finger Transitions & Metronome Drills",
        "Fingerstyle Arrangement & Thumb Bass Coordination",
        "General Music Theory & Chord Progression Query"
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFF59E0B).copy(alpha = 0.15f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Rounded.HelpOutline, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(20.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Ask Doubt to $mentorName",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Rounded.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "SELECT VIDEO / LESSON TOPIC",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = selectedTopic,
                    onValueChange = { selectedTopic = it },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "YOUR DOUBT QUERY",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = doubtText,
                    onValueChange = { doubtText = it },
                    placeholder = { Text("Describe what you are struggling with or where you need help...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (doubtText.isNotBlank()) {
                            onSubmit(selectedTopic, doubtText.trim())
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                    enabled = doubtText.isNotBlank()
                ) {
                    Icon(Icons.Rounded.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Send Encrypted Doubt", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun DemandVideoDialog(
    mentorName: String,
    onDismiss: () -> Unit,
    onSubmit: (topic: String, description: String) -> Unit
) {
    var topicTitle by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF8B5CF6).copy(alpha = 0.15f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Rounded.MovieCreation, contentDescription = null, tint = Color(0xFF7C3AED), modifier = Modifier.size(20.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Request Video from $mentorName",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Rounded.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "REQUESTED TUTORIAL TITLE / TOPIC",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = topicTitle,
                    onValueChange = { topicTitle = it },
                    placeholder = { Text("e.g. Percussive Slap Harmonics on Acoustic") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "WHY SHOULD THE MENTOR COVER THIS?",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    placeholder = { Text("Explain what concepts or exercises you want covered in this video...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (topicTitle.isNotBlank() && description.isNotBlank()) {
                            onSubmit(topicTitle.trim(), description.trim())
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED)),
                    enabled = topicTitle.isNotBlank() && description.isNotBlank()
                ) {
                    Icon(Icons.Rounded.MovieCreation, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Submit Video Demand to Mentor", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun SelectMentorToChatDialog(
    onDismiss: () -> Unit,
    onSelectMentor: (com.skillbuilder.app.domain.model.User) -> Unit
) {
    val mentors = SampleData.mentors
    var mentorSearch by remember { mutableStateOf("") }
    val filteredMentors = remember(mentors, mentorSearch) {
        val q = mentorSearch.trim().lowercase()
        if (q.isBlank()) mentors
        else mentors.filter {
            it.name.lowercase().contains(q) ||
            it.bio.lowercase().contains(q) ||
            it.skillsTaught.any { s -> s.lowercase().contains(q) }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Select Mentor to Chat",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Rounded.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = mentorSearch,
                    onValueChange = { mentorSearch = it },
                    placeholder = { Text("Search by mentor name or skill...") },
                    leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                    trailingIcon = {
                        if (mentorSearch.isNotBlank()) {
                            IconButton(onClick = { mentorSearch = "" }) {
                                Icon(Icons.Rounded.Close, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                            }
                        }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.height(300.dp)
                ) {
                    items(filteredMentors, key = { it.id }) { mentor ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onSelectMentor(mentor) },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = mentor.name.take(1),
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = mentor.name,
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(
                                            Icons.Rounded.Verified,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                    Text(
                                        text = mentor.skillsTaught.joinToString(", "),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
