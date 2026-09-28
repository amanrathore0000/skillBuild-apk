package com.skillbuilder.app.ui.screens.chat

import android.net.Uri
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AttachFile
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.SentimentSatisfied
import androidx.compose.material.icons.rounded.Undo
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DoneAll
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.HelpOutline
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.MovieCreation
import androidx.compose.material.icons.rounded.NotificationsOff
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.QuestionAnswer
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material.icons.rounded.ThumbUp
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material.icons.rounded.VideoLibrary
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.material3.TextButton
import com.skillbuilder.app.ui.theme.BrandPrimary
import com.skillbuilder.app.ui.theme.BrandPrimaryLight
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
    isEmbedded: Boolean = false,
    onDismiss: () -> Unit = {},
    onActiveConversationChanged: (Boolean) -> Unit = {}
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
            } ?: try {
                com.skillbuilder.app.data.local.UserAccountRepository.getInstance(context).getRegisteredAccounts()
                    .filter { it.isMentor }
                    .map { it.toUser() }
                    .find {
                        (initialMentorId != null && it.id == initialMentorId) ||
                        (initialMentorName != null && it.name.equals(initialMentorName, ignoreCase = true))
                    }
            } catch (e: Exception) { null }

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
            } ?: try {
                com.skillbuilder.app.data.local.UserAccountRepository.getInstance(context).getRegisteredAccounts()
                    .filter { it.isMentor }
                    .map { it.toUser() }
                    .find {
                        (initialMentorId != null && it.id == initialMentorId) ||
                        (initialMentorName != null && it.name.equals(initialMentorName, ignoreCase = true))
                    }
            } catch (e: Exception) { null }

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

    LaunchedEffect(activeConversationId) {
        onActiveConversationChanged(activeConversationId != null)
    }

    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f

    BackHandler(enabled = true) {
        if (activeConversationId != null) {
            if (!isEmbedded && (initialMentorId != null || initialMentorName != null)) {
                onDismiss()
            } else {
                activeConversationId = null
            }
        } else if (!isEmbedded) {
            onDismiss()
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .then(
                    if (isEmbedded && activeConversationId == null) Modifier.imePadding()
                    else if (isEmbedded && activeConversationId != null) Modifier.windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom))
                    else Modifier
                        .statusBarsPadding()
                        .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom))
                )
        ) {
            if (activeConversationId == null) {
                ConversationsInboxView(
                    conversations = allConversations,
                    currentUserId = currentUser.id,
                    currentUserName = currentUser.name,
                    isMentor = isMentor,
                    isDark = isDark,
                    isEmbedded = isEmbedded,
                    onDismiss = onDismiss,
                    onSelectConversation = { conv ->
                        activeConversationId = conv.id
                    },
                    onStartNewChat = { showNewChatModal = true }
                )
            } else {
                activeConversation?.let { conv ->
                    val threadMessages = allMessages.filter { it.conversationId == conv.id }

                    InstagramThreadTopBar(
                        conversation = conv,
                        currentUserId = currentUser.id,
                        currentUserName = currentUser.name,
                        isDark = isDark,
                        onBack = {
                            if (!isEmbedded && (initialMentorId != null || initialMentorName != null)) {
                                onDismiss()
                            } else {
                                activeConversationId = null
                            }
                        }
                    )

                    ActiveChatThreadView(
                        conversation = conv,
                        messages = threadMessages,
                        currentUserId = currentUser.id,
                        currentUserName = currentUser.name,
                        isMentor = isMentor,
                        isDark = isDark,
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

// =========================================================================
// INSTAGRAM DIRECT TOP APP BARS
// =========================================================================

@Composable
private fun InstagramThreadTopBar(
    conversation: ChatConversation,
    currentUserId: String,
    currentUserName: String,
    isDark: Boolean,
    onBack: () -> Unit
) {
    val isCurrentMentor = conversation.mentorId == currentUserId || conversation.mentorName.equals(currentUserName, ignoreCase = true)
    val partnerName = if (isCurrentMentor) {
        if (conversation.learnerName.isNotBlank() && !conversation.learnerName.equals(currentUserName, ignoreCase = true)) {
            conversation.learnerName
        } else {
            conversation.mentorName
        }
    } else {
        if (conversation.mentorName.isNotBlank() && !conversation.mentorName.equals(currentUserName, ignoreCase = true)) {
            conversation.mentorName
        } else {
            conversation.learnerName
        }
    }
    val partnerAvatar = if (isCurrentMentor) conversation.learnerAvatar else conversation.mentorAvatar
    val brandBlue = if (isDark) BrandPrimaryLight else BrandPrimary
    val textColor = MaterialTheme.colorScheme.onSurface
    val mutedColor = MaterialTheme.colorScheme.onSurfaceVariant

    val context = LocalContext.current

    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Rounded.ArrowBack,
                    contentDescription = "Back",
                    tint = textColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(2.dp))

            // 38dp circular avatar
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (!partnerAvatar.isNullOrBlank()) {
                    AsyncImage(
                        model = partnerAvatar,
                        contentDescription = partnerName,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Surface(
                        shape = CircleShape,
                        color = if (isDark) Color(0xFF383838) else Color(0xFFE4E6EB),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = partnerName.take(1).uppercase(),
                                fontWeight = FontWeight.Bold,
                                color = textColor,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = partnerName,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 16.sp),
                        color = textColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Rounded.Verified,
                        contentDescription = "Verified",
                        tint = brandBlue,
                        modifier = Modifier.size(14.dp)
                    )
                }

                Text(
                    text = if (isCurrentMentor) "Learner · Active now" else "${conversation.mentorCategory} Mentor · Active now",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                    color = mutedColor
                )
            }
        }
    }
}

// =========================================================================
// CONVERSATIONS INBOX VIEW (INSTAGRAM DM STYLE)
// =========================================================================

@Composable
private fun ConversationsInboxView(
    conversations: List<ChatConversation>,
    currentUserId: String = "",
    currentUserName: String = "",
    isMentor: Boolean,
    isDark: Boolean,
    isEmbedded: Boolean = false,
    onDismiss: () -> Unit = {},
    onSelectConversation: (ChatConversation) -> Unit,
    onStartNewChat: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    val brandBlue = if (isDark) BrandPrimaryLight else BrandPrimary
    val textColor = MaterialTheme.colorScheme.onBackground
    val mutedColor = MaterialTheme.colorScheme.onSurfaceVariant
    val searchBarBg = if (isDark) Color(0xFF383838) else Color(0xFFF1F3F5)

    val filteredList = remember(conversations, searchQuery, currentUserName) {
        if (searchQuery.isBlank()) conversations
        else conversations.filter { conv ->
            val partnerName = if (conv.learnerName.isNotBlank() && !conv.learnerName.equals(currentUserName, ignoreCase = true)) {
                conv.learnerName
            } else {
                conv.mentorName
            }
            partnerName.contains(searchQuery, ignoreCase = true) ||
            conv.lastMessage.contains(searchQuery, ignoreCase = true) ||
            conv.mentorCategory.contains(searchQuery, ignoreCase = true)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header Row: "Messages" on left (with back arrow if !isEmbedded)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = if (!isEmbedded) 8.dp else 18.dp,
                        end = 18.dp,
                        top = if (!isEmbedded) 12.dp else 16.dp,
                        bottom = 10.dp
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!isEmbedded) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(38.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ArrowBack,
                            contentDescription = "Back",
                            tint = textColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                }

                Text(
                    text = "Messages",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp
                    ),
                    color = textColor
                )
            }

            // Instagram Pill Search Bar (placed directly below header with balanced spacing)
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = searchBarBg,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .height(42.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Search,
                        contentDescription = "Search",
                        tint = if (isDark) Color(0xFFB3B3B3) else Color(0xFF8E8E93),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Box(modifier = Modifier.weight(1f)) {
                        if (searchQuery.isEmpty()) {
                            Text(
                                text = "Search",
                                color = if (isDark) Color(0xFFB3B3B3) else Color(0xFF8E8E93),
                                fontSize = 15.sp
                            )
                        }
                        BasicTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            singleLine = true,
                            textStyle = MaterialTheme.typography.bodyMedium.copy(
                                color = textColor,
                                fontSize = 15.sp
                            ),
                            cursorBrush = SolidColor(brandBlue),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    if (searchQuery.isNotEmpty()) {
                        IconButton(
                            onClick = { searchQuery = "" },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = "Clear",
                                tint = if (isDark) Color(0xFFB3B3B3) else Color(0xFF8E8E93),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

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
                            tint = mutedColor
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No messages found",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = textColor
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isMentor) "Learner queries and doubts will appear here." else "Your conversations will appear here.",
                            style = MaterialTheme.typography.bodySmall,
                            color = mutedColor,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(top = 4.dp, bottom = 84.dp)
                ) {
                    items(filteredList, key = { it.id }) { conv ->
                        InstagramConversationRow(
                            conversation = conv,
                            currentUserId = currentUserId,
                            currentUserName = currentUserName,
                            isMentor = isMentor,
                            isDark = isDark,
                            onClick = { onSelectConversation(conv) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InstagramConversationRow(
    conversation: ChatConversation,
    currentUserId: String = "",
    currentUserName: String = "",
    isMentor: Boolean,
    isDark: Boolean,
    onClick: () -> Unit
) {
    val isCurrentMentor = conversation.mentorId == currentUserId || conversation.mentorName.equals(currentUserName, ignoreCase = true)
    val partnerName = if (isCurrentMentor) {
        if (conversation.learnerName.isNotBlank() && !conversation.learnerName.equals(currentUserName, ignoreCase = true)) {
            conversation.learnerName
        } else {
            conversation.mentorName
        }
    } else {
        if (conversation.mentorName.isNotBlank() && !conversation.mentorName.equals(currentUserName, ignoreCase = true)) {
            conversation.mentorName
        } else {
            conversation.learnerName
        }
    }
    val partnerAvatar = if (isCurrentMentor) conversation.learnerAvatar else conversation.mentorAvatar
    val isUnread = conversation.unreadCount > 0
    val brandBlue = if (isDark) BrandPrimaryLight else BrandPrimary
    val textColor = MaterialTheme.colorScheme.onBackground
    val mutedColor = MaterialTheme.colorScheme.onSurfaceVariant

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 56dp Circular Avatar
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (!partnerAvatar.isNullOrBlank()) {
                AsyncImage(
                    model = partnerAvatar,
                    contentDescription = partnerName,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Surface(
                    shape = CircleShape,
                    color = if (isDark) Color(0xFF383838) else Color(0xFFE4E6EB),
                    modifier = Modifier.fillMaxSize()
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = partnerName.take(1).uppercase(),
                            fontWeight = FontWeight.Bold,
                            color = textColor,
                            fontSize = 20.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            // Contact Name
            Text(
                text = partnerName,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                ),
                color = textColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(2.dp))

            // Subtitle snippet · timestamp
            Row(verticalAlignment = Alignment.CenterVertically) {
                val snippet = when {
                    !conversation.subtitleOverride.isNullOrBlank() -> conversation.subtitleOverride
                    isUnread -> "${conversation.unreadCount} new message${if (conversation.unreadCount > 1) "s" else ""} · ${conversation.lastMessageTimestamp}"
                    conversation.lastMessage.startsWith("Doubt:") -> "Doubt · ${conversation.lastMessageTimestamp}"
                    conversation.lastMessage.startsWith("Video Request:") -> "Video Request · ${conversation.lastMessageTimestamp}"
                    conversation.lastMessage.isNotBlank() -> "${conversation.lastMessage} · ${conversation.lastMessageTimestamp}"
                    else -> "Sent ${conversation.lastMessageTimestamp}"
                }

                Text(
                    text = snippet,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = if (isUnread) FontWeight.SemiBold else FontWeight.Normal,
                        fontSize = 13.sp
                    ),
                    color = if (isUnread) textColor else mutedColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )

                if (conversation.isMuted) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Rounded.NotificationsOff,
                        contentDescription = "Muted",
                        tint = mutedColor,
                        modifier = Modifier.size(13.dp)
                    )
                }
            }
        }

        // Right side: Blue unread indicator dot
        if (isUnread) {
            Spacer(modifier = Modifier.width(12.dp))
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(color = brandBlue, shape = CircleShape)
            )
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
    isDark: Boolean,
    initialReferenceTopic: String? = null,
    onOpenAskDoubt: () -> Unit,
    onOpenDemandVideo: () -> Unit
) {
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val isCurrentUserMentorInConv = conversation.mentorId == currentUserId || conversation.mentorName.equals(currentUserName, ignoreCase = true)
    val partnerName = if (isCurrentUserMentorInConv) conversation.learnerName else conversation.mentorName

    var inputFieldValue by remember {
        val initialText = if (!initialReferenceTopic.isNullOrBlank()) "Hi $partnerName, I have a doubt regarding \"$initialReferenceTopic\": " else ""
        mutableStateOf(TextFieldValue(text = initialText, selection = TextRange(initialText.length)))
    }

    var selectedMessageForOptions by remember { mutableStateOf<ChatMessage?>(null) }
    var showAttachmentDialog by remember { mutableStateOf(false) }

    val mediaPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { selectedUri ->
            var fileName = "Photo"
            try {
                context.contentResolver.query(selectedUri, null, null, null, null)?.use { cursor ->
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIndex != -1 && cursor.moveToFirst()) {
                        fileName = cursor.getString(nameIndex)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            val isVideo = fileName.endsWith(".mp4", true) || fileName.endsWith(".mov", true) || fileName.endsWith(".mkv", true)
            val attType = if (isVideo) "VIDEO" else "IMAGE"
            SampleData.sendChatMessage(
                conversationId = conversation.id,
                senderId = currentUserId,
                senderName = currentUserName,
                senderRole = if (isMentor) "Mentor" else "Learner",
                recipientId = if (isMentor) conversation.learnerId else conversation.mentorId,
                text = if (isVideo) "📹 Video: $fileName" else "📷 Photo: $fileName",
                type = ChatMessageType.STANDARD,
                attachmentUri = selectedUri.toString(),
                attachmentType = attType,
                attachmentName = fileName
            )
        }
    }

    val documentPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { selectedUri ->
            var fileName = "Document"
            try {
                context.contentResolver.query(selectedUri, null, null, null, null)?.use { cursor ->
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIndex != -1 && cursor.moveToFirst()) {
                        fileName = cursor.getString(nameIndex)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            SampleData.sendChatMessage(
                conversationId = conversation.id,
                senderId = currentUserId,
                senderName = currentUserName,
                senderRole = if (isMentor) "Mentor" else "Learner",
                recipientId = if (isMentor) conversation.learnerId else conversation.mentorId,
                text = "📄 Document: $fileName",
                type = ChatMessageType.STANDARD,
                attachmentUri = selectedUri.toString(),
                attachmentType = "DOCUMENT",
                attachmentName = fileName
            )
        }
    }

    val onSendMessage = {
        val trimmed = inputFieldValue.text.trim()
        if (trimmed.isNotBlank()) {
            SampleData.sendChatMessage(
                conversationId = conversation.id,
                senderId = currentUserId,
                senderName = currentUserName,
                senderRole = if (isMentor) "Mentor" else "Learner",
                recipientId = if (isMentor) conversation.learnerId else conversation.mentorId,
                text = trimmed,
                type = ChatMessageType.STANDARD
            )
            inputFieldValue = TextFieldValue("")
        }
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
                .padding(horizontal = 12.dp),
            contentPadding = PaddingValues(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Reference image: Date Pill
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, bottom = 2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isDark) Color(0xFF1E2328) else Color(0xFFE4E6EB).copy(alpha = 0.8f)
                    ) {
                        Text(
                            text = "19 February 2025",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            color = if (isDark) Color(0xFFB0B0B0) else Color(0xFF616161),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            // Reference image: Compact Amber End-to-End Encryption Banner
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isDark) Color(0xFF1F1A12) else Color(0xFFFFFBEB),
                        border = BorderStroke(1.dp, if (isDark) Color(0x33F59E0B) else Color(0x44F59E0B))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Lock,
                                contentDescription = null,
                                tint = if (isDark) Color(0xFFE5A93C) else Color(0xFFD97706),
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Messages and calls are end-to-end encrypted. Only people in this chat can read, listen to, or share them. Learn more",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, lineHeight = 14.sp),
                                color = if (isDark) Color(0xFFE5A93C) else Color(0xFFD97706),
                                textAlign = TextAlign.Center
                            )
                        }
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
                            },
                            onLongPress = {
                                selectedMessageForOptions = message
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
                            },
                            onLongPress = {
                                selectedMessageForOptions = message
                            }
                        )
                    }
                    ChatMessageType.STANDARD -> {
                        StandardChatBubble(
                            message = message,
                            isOutgoing = isOutgoing,
                            isDark = isDark,
                            onLongPress = {
                                selectedMessageForOptions = message
                            }
                        )
                    }
                }
            }
        }

        // Bottom Input Field Bar (Matching reference image: Emoji, Message, Attachment, and Circle Send)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp, end = 8.dp, top = 4.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(26.dp),
                color = if (isDark) Color(0xFF1E2328) else Color(0xFFF1F3F5),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            Toast.makeText(context, "Emoji picker", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.SentimentSatisfied,
                            contentDescription = "Emoji",
                            tint = if (isDark) Color(0xFF9E9E9E) else Color(0xFF757575),
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (inputFieldValue.text.isEmpty()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Message",
                                    color = if (isDark) Color(0xFF9E9E9E) else Color(0xFF757575),
                                    fontSize = 15.sp
                                )

                                if (clipboardManager.hasText()) {
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = BrandPrimary.copy(alpha = 0.12f),
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(10.dp))
                                            .clickable {
                                                val clip = clipboardManager.getText()?.text ?: ""
                                                if (clip.isNotEmpty()) {
                                                    inputFieldValue = TextFieldValue(clip, TextRange(clip.length))
                                                }
                                            }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.ContentCopy,
                                                contentDescription = "Paste",
                                                tint = BrandPrimary,
                                                modifier = Modifier.size(11.dp)
                                            )
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text(
                                                text = "Paste",
                                                color = BrandPrimary,
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        BasicTextField(
                            value = inputFieldValue,
                            onValueChange = { inputFieldValue = it },
                            singleLine = true,
                            textStyle = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 15.sp
                            ),
                            cursorBrush = SolidColor(if (isDark) BrandPrimaryLight else BrandPrimary),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                            keyboardActions = KeyboardActions(onSend = { onSendMessage() }),
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight()
                                .wrapContentHeight(Alignment.CenterVertically)
                        )
                    }

                    IconButton(
                        onClick = {
                            showAttachmentDialog = true
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.AttachFile,
                            contentDescription = "Attach",
                            tint = if (isDark) Color(0xFF9E9E9E) else Color(0xFF757575),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            Surface(
                shape = CircleShape,
                color = BrandPrimary,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .clickable { onSendMessage() }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Rounded.Send,
                        contentDescription = "Send Message",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        // ==================== Attachment Selection Modal ====================
        if (showAttachmentDialog) {
            Dialog(onDismissRequest = { showAttachmentDialog = false }) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (isDark) Color(0xFF1E2328) else Color.White,
                    tonalElevation = 6.dp,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Share Attachment",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        // 1. Photos & Videos
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isDark) Color(0xFF262C34) else Color(0xFFF3F4F6),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    showAttachmentDialog = false
                                    mediaPickerLauncher.launch("image/*")
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = BrandPrimary.copy(alpha = 0.15f),
                                    modifier = Modifier.size(42.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Rounded.Image,
                                            contentDescription = null,
                                            tint = BrandPrimary,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(14.dp))
                                Column {
                                    Text(
                                        text = "Photo from Device",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Share photos or screenshots",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        // 2. Video
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isDark) Color(0xFF262C34) else Color(0xFFF3F4F6),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    showAttachmentDialog = false
                                    mediaPickerLauncher.launch("video/*")
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFF8B5CF6).copy(alpha = 0.15f),
                                    modifier = Modifier.size(42.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Rounded.MovieCreation,
                                            contentDescription = null,
                                            tint = Color(0xFF8B5CF6),
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(14.dp))
                                Column {
                                    Text(
                                        text = "Video from Device",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Share video clips or lessons",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        // 3. Document
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isDark) Color(0xFF262C34) else Color(0xFFF3F4F6),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    showAttachmentDialog = false
                                    documentPickerLauncher.launch("*/*")
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFF10B981).copy(alpha = 0.15f),
                                    modifier = Modifier.size(42.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Rounded.Description,
                                            contentDescription = null,
                                            tint = Color(0xFF10B981),
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(14.dp))
                                Column {
                                    Text(
                                        text = "Document from Device",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "PDF, doc, text or music sheets",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = { showAttachmentDialog = false }) {
                                Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }

        // ==================== Long Press Message Context Menu ====================
        if (selectedMessageForOptions != null) {
            val msg = selectedMessageForOptions!!
            Dialog(onDismissRequest = { selectedMessageForOptions = null }) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (isDark) Color(0xFF1E2328) else Color.White,
                    tonalElevation = 8.dp,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Message Options",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isDark) Color(0xFF2A3038) else Color(0xFFF1F3F5),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = msg.text,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(10.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // 1. Copy Text
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    clipboardManager.setText(AnnotatedString(msg.text))
                                    Toast.makeText(context, "Message copied to clipboard", Toast.LENGTH_SHORT).show()
                                    selectedMessageForOptions = null
                                }
                                .padding(horizontal = 12.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.ContentCopy,
                                contentDescription = "Copy",
                                tint = BrandPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(14.dp))
                            Text(
                                text = "Copy chat",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // 2. Unsend Chat (chat will not transfer / deleted for everyone)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    SampleData.unsendChatMessage(conversation.id, msg.id)
                                    Toast.makeText(context, "Message unsent (will not transfer)", Toast.LENGTH_SHORT).show()
                                    selectedMessageForOptions = null
                                }
                                .padding(horizontal = 12.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Undo,
                                contentDescription = "Unsend",
                                tint = Color(0xFFF59E0B),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = "Unsend chat",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = Color(0xFFF59E0B)
                                )
                                Text(
                                    text = "Chat will not transfer, removes for everyone",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // 3. Delete Chat
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    SampleData.deleteChatMessage(conversation.id, msg.id)
                                    Toast.makeText(context, "Chat deleted", Toast.LENGTH_SHORT).show()
                                    selectedMessageForOptions = null
                                }
                                .padding(horizontal = 12.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Delete,
                                contentDescription = "Delete",
                                tint = Color(0xFFEF4444),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(14.dp))
                            Text(
                                text = "Delete chat",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = Color(0xFFEF4444)
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = { selectedMessageForOptions = null }) {
                                Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
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
    isOutgoing: Boolean,
    isDark: Boolean = false,
    onLongPress: () -> Unit = {}
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isOutgoing) Alignment.End else Alignment.Start
    ) {
        val bubbleColor = if (isOutgoing) BrandPrimary else (if (isDark) Color(0xFF383838) else Color(0xFFF1F3F5))
        val textColor = if (isOutgoing) Color.White else MaterialTheme.colorScheme.onSurface
        val metaColor = if (isOutgoing) Color.White.copy(alpha = 0.75f) else MaterialTheme.colorScheme.onSurfaceVariant

        Surface(
            shape = RoundedCornerShape(
                topStart = 18.dp,
                topEnd = 18.dp,
                bottomStart = if (isOutgoing) 18.dp else 4.dp,
                bottomEnd = if (isOutgoing) 4.dp else 18.dp
            ),
            color = bubbleColor,
            modifier = Modifier
                .fillMaxWidth(0.82f)
                .clip(
                    RoundedCornerShape(
                        topStart = 18.dp,
                        topEnd = 18.dp,
                        bottomStart = if (isOutgoing) 18.dp else 4.dp,
                        bottomEnd = if (isOutgoing) 4.dp else 18.dp
                    )
                )
                .pointerInput(message.id) {
                    detectTapGestures(onLongPress = { onLongPress() })
                }
        ) {
            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                // If attachment is present
                if (!message.attachmentUri.isNullOrBlank()) {
                    when (message.attachmentType) {
                        "IMAGE" -> {
                            AsyncImage(
                                model = message.attachmentUri,
                                contentDescription = message.attachmentName ?: "Photo",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 240.dp)
                                    .clip(RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                        }
                        "VIDEO" -> {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color.Black.copy(alpha = 0.4f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(130.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Rounded.PlayArrow,
                                        contentDescription = "Video",
                                        tint = Color.White,
                                        modifier = Modifier.size(42.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                        }
                        else -> { // DOCUMENT or file
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isOutgoing) Color.White.copy(alpha = 0.18f) else (if (isDark) Color(0xFF262C34) else Color.White),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Description,
                                        contentDescription = "Document",
                                        tint = if (isOutgoing) Color.White else BrandPrimary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = message.attachmentName ?: "Document.pdf",
                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                            color = textColor,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "Document File",
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                            color = metaColor
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                        }
                    }
                }

                Text(
                    text = message.text,
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 15.sp),
                    color = textColor
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.align(Alignment.End),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = message.timestamp,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = metaColor
                    )
                    if (isOutgoing) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Rounded.DoneAll,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.85f),
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
    onMarkResolved: () -> Unit,
    onLongPress: () -> Unit = {}
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isOutgoing) Alignment.End else Alignment.Start
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.88f)
                .clip(RoundedCornerShape(16.dp))
                .pointerInput(message.id) {
                    detectTapGestures(onLongPress = { onLongPress() })
                },
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
    onUpvoteDemand: () -> Unit,
    onLongPress: () -> Unit = {}
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isOutgoing) Alignment.End else Alignment.Start
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(16.dp))
                .pointerInput(message.id) {
                    detectTapGestures(onLongPress = { onLongPress() })
                },
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
