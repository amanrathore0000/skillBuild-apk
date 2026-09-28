package com.skillbuilder.app.ui.screens.swap

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ChatBubbleOutline
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Handshake
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.Payment
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material.icons.rounded.VideoLibrary
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil3.compose.AsyncImage
import com.skillbuilder.app.data.local.RealTimeDataManager
import com.skillbuilder.app.data.local.SampleData
import com.skillbuilder.app.data.local.UserSession
import com.skillbuilder.app.domain.model.MentorVideo
import com.skillbuilder.app.domain.model.SwapProposal
import com.skillbuilder.app.domain.model.SwapStatus
import com.skillbuilder.app.domain.model.User
import com.skillbuilder.app.ui.screens.chat.EncryptedChatScreen
import com.skillbuilder.app.ui.screens.mentor.PublicMentorProfileSheet

/**
 * Reference model representing a course and video from the mentor's playlist.
 */
data class PlaylistCourseReference(
    val courseTitle: String,
    val videoTitle: String,
    val duration: String,
    val videoUrl: String,
    val thumbnailUrl: String,
    val category: String,
    val curriculumTopics: List<String>
)

@Composable
fun SwapScreen(
    onOpenVideo: ((MentorVideo) -> Unit)? = null
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }
    var selectedMentorForProfile by remember { mutableStateOf<User?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    // Dialog state for ₹59 Premium Pass purchase
    var isPurchasePassDialogOpen by remember { mutableStateOf(false) }

    // Proposal targeted for detailed Content & Compatibility Review
    var proposalForContentReview by remember { mutableStateOf<SwapProposal?>(null) }

    // Chat target
    var activeChatMentorId by remember { mutableStateOf<String?>(null) }

    // Send Swap Request dialog state
    var isSendSwapDialogOpen by remember { mutableStateOf(false) }
    var mentorToProposeTo by remember { mutableStateOf<User?>(null) }

    val proposals by RealTimeDataManager.swapProposalsFlow.collectAsState()
    val isSwapPassActive by RealTimeDataManager.mentorSwapPassActive.collectAsState()

    val pendingIncoming = proposals.filter { it.status == SwapStatus.PENDING && !it.isOutgoing }
    val pendingOutgoing = proposals.filter { it.status == SwapStatus.PENDING && it.isOutgoing }
    val activeSwaps = proposals.filter { it.status == SwapStatus.ACTIVE }

    val tabs = listOf(
        "Incoming Requests (${pendingIncoming.size})",
        "Sent Requests (${pendingOutgoing.size})",
        "Active Swaps (${activeSwaps.size})",
        "Explore Mentors"
    )

    val query = searchQuery.trim().lowercase()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Search Bar (moved up towards header)
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 4.dp),
                placeholder = { Text("Search swap mentors, skills, or curriculum...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Rounded.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = "Clear",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )

            // ==================== PROPOSE SWAP ACTION BANNER ====================
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Rounded.SwapHoriz,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Exchange Skills & Video Courses",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (isSwapPassActive) "Provide video in exchange for skills • Pass Active ✓" else "₹59 Mentor Swap Pass required to send request",
                                fontSize = 11.sp,
                                color = if (isSwapPassActive) MaterialTheme.colorScheme.primary else Color(0xFFD97706)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            if (!isSwapPassActive) {
                                isPurchasePassDialogOpen = true
                            } else {
                                mentorToProposeTo = null
                                isSendSwapDialogOpen = true
                            }
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSwapPassActive) MaterialTheme.colorScheme.primary else Color(0xFFD97706),
                            contentColor = Color.White
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isSwapPassActive) "Send Request" else "Get Pass",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // ==================== TAB NAVIGATION ====================
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                edgePadding = 16.dp,
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 13.sp
                            )
                        }
                    )
                }
            }

            // ==================== TAB CONTENT ====================
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                when (selectedTab) {
                    0 -> {
                        // Pending Incoming Swap Proposals
                        val filteredPending = pendingIncoming.filter {
                            query.isEmpty() ||
                                    it.partner.name.lowercase().contains(query) ||
                                    it.partnerSkill.lowercase().contains(query) ||
                                    it.mySkill.lowercase().contains(query)
                        }

                        if (filteredPending.isEmpty()) {
                            item {
                                EmptyStateNotice(
                                    if (searchQuery.isNotBlank()) "No incoming swap requests matching \"$searchQuery\"."
                                    else "No incoming swap requests right now. Explore mentors or tap '+ Send Request' to propose a skill exchange!"
                                )
                            }
                        } else {
                            items(filteredPending, key = { it.id }) { proposal ->
                                IncomingSwapProposalCard(
                                    proposal = proposal,
                                    onReviewContent = {
                                        proposalForContentReview = proposal
                                    }
                                )
                            }
                        }
                    }

                    1 -> {
                        // Outgoing / Sent Swap Proposals
                        val filteredSent = pendingOutgoing.filter {
                            query.isEmpty() ||
                                    it.partner.name.lowercase().contains(query) ||
                                    it.partnerSkill.lowercase().contains(query) ||
                                    it.mySkill.lowercase().contains(query)
                        }

                        if (filteredSent.isEmpty()) {
                            item {
                                EmptyStateNotice(
                                    if (searchQuery.isNotBlank()) "No sent swap requests matching \"$searchQuery\"."
                                    else "You haven't sent any swap requests yet. Tap '+ Send Request' to propose a skill swap with your playlist video!"
                                )
                            }
                        } else {
                            items(filteredSent, key = { it.id }) { proposal ->
                                OutgoingSwapProposalCard(
                                    proposal = proposal,
                                    onOpenVideo = onOpenVideo,
                                    onCancelProposal = {
                                        RealTimeDataManager.deleteSwapProposal(proposal.id)
                                        Toast.makeText(context, "Swap request cancelled.", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }
                        }
                    }

                    2 -> {
                        // Active Swaps
                        val filteredActive = activeSwaps.filter {
                            query.isEmpty() ||
                                    it.partner.name.lowercase().contains(query) ||
                                    it.partnerSkill.lowercase().contains(query)
                        }

                        if (filteredActive.isEmpty()) {
                            item {
                                EmptyStateNotice(
                                    if (searchQuery.isNotBlank()) "No active swaps matching \"$searchQuery\"."
                                    else "No active swaps yet. Review incoming proposals or connect with a mentor to get started!"
                                )
                            }
                        } else {
                            items(filteredActive, key = { it.id }) { proposal ->
                                ActiveMentorSwapCard(
                                    proposal = proposal,
                                    onOpenChat = { activeChatMentorId = proposal.partner.id },
                                    onViewCurriculum = { proposalForContentReview = proposal }
                                )
                            }
                        }
                    }

                    3 -> {
                        // Discover Fellow Mentors Seeking Swaps (with ₹59 Pass)
                        val mentorsList = SampleData.mentors.filter {
                            query.isEmpty() ||
                                    it.name.lowercase().contains(query) ||
                                    it.skillsTaught.any { s -> s.lowercase().contains(query) } ||
                                    it.skillsWanted.any { s -> s.lowercase().contains(query) }
                        }

                        if (mentorsList.isEmpty()) {
                            item {
                                EmptyStateNotice("No mentors found matching \"$searchQuery\".")
                            }
                        } else {
                            items(mentorsList, key = { it.id }) { mentor ->
                                MentorSwapPartnerCard(
                                    mentor = mentor,
                                    hasActivePass = isSwapPassActive,
                                    onViewProfile = { selectedMentorForProfile = mentor },
                                    onProposeSwap = {
                                        if (!isSwapPassActive) {
                                            isPurchasePassDialogOpen = true
                                        } else {
                                            mentorToProposeTo = mentor
                                            isSendSwapDialogOpen = true
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // ==================== FLOATING ACTION BUTTON ====================
        ExtendedFloatingActionButton(
            onClick = {
                if (!isSwapPassActive) {
                    isPurchasePassDialogOpen = true
                } else {
                    mentorToProposeTo = null
                    isSendSwapDialogOpen = true
                }
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = if (isSwapPassActive) MaterialTheme.colorScheme.primary else Color(0xFFD97706),
            contentColor = Color.White,
            icon = {
                Icon(
                    imageVector = Icons.Rounded.SwapHoriz,
                    contentDescription = null
                )
            },
            text = {
                Text(
                    text = if (isSwapPassActive) "Send Swap Request" else "Get Pass to Swap",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        )
    }

    // ==================== MODALS & SHEETS ====================

    // 1. ₹59 Pass Purchase Dialog
    if (isPurchasePassDialogOpen) {
        PurchaseSwapPassDialog(
            onDismiss = { isPurchasePassDialogOpen = false },
            onConfirmPurchase = {
                RealTimeDataManager.purchaseMentorSwapPass()
                isPurchasePassDialogOpen = false
                Toast.makeText(
                    context,
                    "🎉 ₹59 Mentor Swap Pass Activated! You can now propose & accept skill swaps.",
                    Toast.LENGTH_LONG
                ).show()
                // Automatically open Send Swap Dialog after purchasing
                isSendSwapDialogOpen = true
            }
        )
    }

    // 2. Comprehensive Send Swap Request Dialog (With Playlist Video Reference & Request Definition)
    if (isSendSwapDialogOpen) {
        SendSwapRequestDialog(
            initialTargetMentor = mentorToProposeTo,
            allMentors = SampleData.mentors,
            onDismiss = {
                isSendSwapDialogOpen = false
                mentorToProposeTo = null
            },
            onSendProposal = { newProposal ->
                RealTimeDataManager.addSwapProposal(newProposal)
                isSendSwapDialogOpen = false
                mentorToProposeTo = null
                Toast.makeText(
                    context,
                    "🎉 Swap request sent to ${newProposal.partner.name} with your video course reference!",
                    Toast.LENGTH_LONG
                ).show()
            }
        )
    }

    // 3. Dedicated Content & Compatibility Review Screen
    proposalForContentReview?.let { proposal ->
        Dialog(
            onDismissRequest = { proposalForContentReview = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            com.skillbuilder.app.ui.screens.swap.MentorSwapContentReviewScreen(
                proposal = proposal,
                hasActiveSwapPass = isSwapPassActive,
                onDismiss = { proposalForContentReview = null },
                onPurchasePass = { isPurchasePassDialogOpen = true },
                onAcceptSwap = { accepted ->
                    RealTimeDataManager.updateSwapStatus(accepted.id, SwapStatus.ACTIVE)
                    Toast.makeText(
                        context,
                        "🤝 Skill Swap Accepted! Your ₹59 premium pass has been completed for this swap. Private chat unlocked.",
                        Toast.LENGTH_LONG
                    ).show()
                },
                onDeclineSwap = { declined ->
                    RealTimeDataManager.deleteSwapProposal(declined.id)
                    Toast.makeText(context, "Swap request declined.", Toast.LENGTH_SHORT).show()
                },
                onOpenSampleVideo = onOpenVideo
            )
        }
    }

    // 4. Public Mentor Profile Sheet
    selectedMentorForProfile?.let { mentor ->
        PublicMentorProfileSheet(
            mentor = mentor,
            onDismiss = { selectedMentorForProfile = null },
            onVideoClick = onOpenVideo
        )
    }

    // 5. 1-on-1 Encrypted Chat Screen
    activeChatMentorId?.let { mentorId ->
        EncryptedChatScreen(
            initialMentorId = mentorId,
            onDismiss = { activeChatMentorId = null }
        )
    }
}

// ==================== UI COMPONENTS ====================

@Composable
private fun IncomingSwapProposalCard(
    proposal: SwapProposal,
    onReviewContent: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onReviewContent() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (!proposal.partner.avatarUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = proposal.partner.avatarUrl,
                            contentDescription = proposal.partner.name,
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = proposal.partner.name.take(2).uppercase(),
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = proposal.partner.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                Icons.Rounded.Verified,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        Text(
                            text = "★ ${proposal.partner.rating} • ${proposal.proposedDate}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFF10B981).copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.35f))
                ) {
                    Text(
                        text = "${proposal.compatibilityScore}% Match",
                        color = Color(0xFF10B981),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Skill exchange summary
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "OFFERS YOU:",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = proposal.partnerSkill,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (!proposal.sampleVideoTitle.isNullOrBlank()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(top = 2.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.PlayArrow,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "${proposal.sampleVideoTitle} (${proposal.sampleVideoDuration ?: "Lesson"})",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                    Icon(
                        Icons.Rounded.SwapHoriz,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 6.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "WANTS FROM YOU:",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFD97706)
                        )
                        Text(
                            text = proposal.mySkill,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            val cleanMsg = remember(proposal.message) {
                proposal.message
                    .replace("I have activated my ₹59 Swap Pass and ", "")
                    .replace("I have activated my ₹59 Swap Pass ", "")
                    .replace("I've got my ₹59 swap pass ready—", "")
                    .replace("₹59", "")
                    .replace("swap pass", "swap proposal", ignoreCase = true)
                    .trim()
            }
            if (cleanMsg.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "\"$cleanMsg\"",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            val isDark = MaterialTheme.colorScheme.background == Color(0xFF2B2B2B)
            Button(
                onClick = onReviewContent,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isDark) Color(0xFF3B82F6) else MaterialTheme.colorScheme.primary,
                    contentColor = Color.White
                ),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                Icon(Icons.Rounded.Visibility, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Review Content & Compatibility", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun OutgoingSwapProposalCard(
    proposal: SwapProposal,
    onOpenVideo: ((MentorVideo) -> Unit)? = null,
    onCancelProposal: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header with target mentor & status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (!proposal.partner.avatarUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = proposal.partner.avatarUrl,
                            contentDescription = proposal.partner.name,
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = proposal.partner.name.take(2).uppercase(),
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = proposal.partner.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                Icons.Rounded.Verified,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        Text(
                            text = "Recipient Mentor • ${proposal.proposedDate}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFFD97706).copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, Color(0xFFD97706).copy(alpha = 0.35f))
                ) {
                    Text(
                        text = "SENT • PENDING",
                        color = Color(0xFFD97706),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // REQUEST COLUMNS (What You Offer vs What You Want)
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // OFFER ROW
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top
                    ) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            modifier = Modifier.padding(top = 2.dp)
                        ) {
                            Text(
                                text = "YOU OFFER",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = proposal.mySkill,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (proposal.courseOverview.isNotBlank()) {
                                Text(
                                    text = "Course: ${proposal.courseOverview}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    // VIDEO ATTACHMENT FROM PLAYLIST
                    if (!proposal.sampleVideoTitle.isNullOrBlank()) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Rounded.PlayArrow,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = proposal.sampleVideoTitle,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "Playlist Video • ${proposal.sampleVideoDuration ?: "15:00"}",
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                TextButton(
                                    onClick = {
                                        val sampleVideo = MentorVideo(
                                            id = "video_${proposal.id}",
                                            title = proposal.sampleVideoTitle,
                                            courseTitle = proposal.courseOverview.ifBlank { proposal.mySkill },
                                            duration = proposal.sampleVideoDuration ?: "15:00",
                                            views = 1,
                                            likes = 0,
                                            videoUrl = proposal.sampleVideoUrl ?: "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
                                            uploadDate = "Recently",
                                            thumbnailUrl = proposal.sampleVideoThumbnailUrl ?: "https://images.unsplash.com/photo-1510915361894-db8b60106cb1?w=800",
                                            description = proposal.message,
                                            category = proposal.mySkill,
                                            mentorName = "You"
                                        )
                                        onOpenVideo?.invoke(sampleVideo)
                                    },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text("Preview", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // WANT ROW
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top
                    ) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFFD97706).copy(alpha = 0.15f),
                            modifier = Modifier.padding(top = 2.dp)
                        ) {
                            Text(
                                text = "YOU WANT",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFFD97706),
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = proposal.partnerSkill,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            if (proposal.message.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "\"${proposal.message}\"",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action: Cancel Proposal
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                OutlinedButton(
                    onClick = onCancelProposal,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Rounded.Delete, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Cancel Request", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun ActiveMentorSwapCard(
    proposal: SwapProposal,
    onOpenChat: () -> Unit,
    onViewCurriculum: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.5.dp, Color(0xFF10B981).copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (!proposal.partner.avatarUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = proposal.partner.avatarUrl,
                            contentDescription = proposal.partner.name,
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF10B981).copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = proposal.partner.name.take(2).uppercase(),
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF10B981)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = proposal.partner.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Swapping: ${proposal.partnerSkill} ↔ ${proposal.mySkill}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFF10B981).copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "ACTIVE",
                        color = Color(0xFF10B981),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold, fontSize = 10.sp),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onViewCurriculum,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(vertical = 6.dp)
                ) {
                    Icon(Icons.Rounded.MenuBook, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Curriculum", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                val isDark = MaterialTheme.colorScheme.background == Color(0xFF2B2B2B)
                Button(
                    onClick = onOpenChat,
                    modifier = Modifier.weight(1.3f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isDark) Color(0xFF3B82F6) else MaterialTheme.colorScheme.primary,
                        contentColor = Color.White
                    ),
                    contentPadding = PaddingValues(vertical = 6.dp)
                ) {
                    Icon(Icons.Rounded.ChatBubbleOutline, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("1-on-1 Chat", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun MentorSwapPartnerCard(
    mentor: User,
    hasActivePass: Boolean,
    onViewProfile: () -> Unit,
    onProposeSwap: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onViewProfile() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        modifier = Modifier.size(44.dp),
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = mentor.name.take(2).uppercase(),
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = mentor.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                Icons.Rounded.Verified,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        Text(
                            text = "★ ${mentor.rating} (${mentor.reviewCount} reviews)",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFFD97706).copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, Color(0xFFD97706).copy(alpha = 0.3f))
                ) {
                    Text(
                        text = "₹59 Swapper",
                        color = Color(0xFFD97706),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Offers: ${mentor.skillsTaught.joinToString(", ")}",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Seeking: ${mentor.skillsWanted.joinToString(", ")}",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onViewProfile,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(vertical = 6.dp)
                ) {
                    Text("View Profile", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                val isDark = MaterialTheme.colorScheme.background == Color(0xFF2B2B2B)
                Button(
                    onClick = onProposeSwap,
                    modifier = Modifier.weight(1.2f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (hasActivePass) (if (isDark) Color(0xFF3B82F6) else MaterialTheme.colorScheme.primary) else Color(0xFFD97706),
                        contentColor = Color.White
                    ),
                    contentPadding = PaddingValues(vertical = 6.dp)
                ) {
                    Icon(Icons.Rounded.SwapHoriz, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (hasActivePass) "Propose Swap" else "Get ₹59 Pass",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

/**
 * SendSwapRequestDialog:
 * Comprehensive modal where a mentor specifies what they offer, what they want in return,
 * and provides their video lesson referencing a course from their playlist.
 */
@Composable
private fun SendSwapRequestDialog(
    initialTargetMentor: User? = null,
    allMentors: List<User>,
    onDismiss: () -> Unit,
    onSendProposal: (proposal: SwapProposal) -> Unit
) {
    val context = LocalContext.current
    val currentUser = UserSession.currentUser.collectAsState().value
    val allUploadedVideos by SampleData.allVideosFlow.collectAsState()

    var selectedMentor by remember {
        mutableStateOf(initialTargetMentor ?: allMentors.firstOrNull() ?: SampleData.mentors.first())
    }

    // Request Column Definition
    var offerSkill by remember {
        mutableStateOf(currentUser.skillsTaught.firstOrNull() ?: "Acoustic Guitar & Fingerstyle")
    }
    var wantSkill by remember {
        mutableStateOf(selectedMentor.skillsTaught.firstOrNull() ?: "UI/UX Design")
    }

    // Playlist course references
    val playlistItems = remember(allUploadedVideos) {
        val items = mutableListOf<PlaylistCourseReference>()
        allUploadedVideos.forEach { v ->
            items.add(
                PlaylistCourseReference(
                    courseTitle = v.courseTitle.ifBlank { "Playlist: ${v.title}" },
                    videoTitle = v.title,
                    duration = v.duration.ifBlank { "15:00" },
                    videoUrl = v.videoUrl,
                    thumbnailUrl = v.thumbnailUrl ?: "https://images.unsplash.com/photo-1516321318423-f06f85e504b3?w=800",
                    category = v.category,
                    curriculumTopics = listOf(
                        "Module 1: Foundations & Architecture",
                        "Module 2: Practical Exercises & Live Demo",
                        "Module 3: Advanced Optimization & Production Standards",
                        "Module 4: 1-on-1 Code Review & Feedback"
                    )
                )
            )
        }
        if (items.size < 4) {
            items.addAll(
                listOf(
                    PlaylistCourseReference(
                        courseTitle = "Acoustic Fingerstyle Guitar Masterclass",
                        videoTitle = "Lesson 1: Hand Posture & P-I-M-A Patterns",
                        duration = "16:45",
                        videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
                        thumbnailUrl = "https://images.unsplash.com/photo-1510915361894-db8b60106cb1?w=800",
                        category = "Music",
                        curriculumTopics = listOf(
                            "Module 1: Hand Posture & Finger Ergonomics",
                            "Module 2: Travis Picking & Alternating Bass",
                            "Module 3: Percussive Slap & Harmonic Tapping",
                            "Module 4: Arranging Pop Melodies for Solo Guitar"
                        )
                    ),
                    PlaylistCourseReference(
                        courseTitle = "Production Android with Jetpack Compose",
                        videoTitle = "Lesson 1: Declarative State & Flow Hoisting",
                        duration = "22:10",
                        videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
                        thumbnailUrl = "https://images.unsplash.com/photo-1555066931-4365d14bab8c?w=800",
                        category = "Tech & Coding",
                        curriculumTopics = listOf(
                            "Module 1: Composable Lifecycles & State Hoisting",
                            "Module 2: Clean Architecture & MVI Pattern",
                            "Module 3: Custom Canvas Animations & Shaders",
                            "Module 4: Performance Profiling & Recomposition Tuning"
                        )
                    ),
                    PlaylistCourseReference(
                        courseTitle = "Full Stack Web & API Engineering",
                        videoTitle = "Lesson 1: Scalable REST & Microservices",
                        duration = "19:30",
                        videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
                        thumbnailUrl = "https://images.unsplash.com/photo-1517694712202-14dd9538aa97?w=800",
                        category = "Tech & Coding",
                        curriculumTopics = listOf(
                            "Module 1: Database Schemas & Indexing",
                            "Module 2: JWT & Secure OAuth2 Sessions",
                            "Module 3: Real-Time WebSockets & Event Queues",
                            "Module 4: Docker Containers & Cloud Deployment"
                        )
                    ),
                    PlaylistCourseReference(
                        courseTitle = "Artisan Sourdough & Fermentation",
                        videoTitle = "Lesson 1: Wild Yeast Starter Activation",
                        duration = "14:15",
                        videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4",
                        thumbnailUrl = "https://images.unsplash.com/photo-1509440159596-0249088772ff?w=800",
                        category = "Culinary Arts",
                        curriculumTopics = listOf(
                            "Module 1: Starter Hydration & Microbial Biology",
                            "Module 2: Autolyse, Stretch & Fold Mechanics",
                            "Module 3: Banneton Proofing & Scoring Techniques",
                            "Module 4: Dutch Oven Steam & Crust Caramelization"
                        )
                    )
                )
            )
        }
        items
    }

    var selectedPlaylistCourse by remember { mutableStateOf(playlistItems.firstOrNull()) }

    var courseReference by remember {
        mutableStateOf(selectedPlaylistCourse?.courseTitle ?: "Acoustic Fingerstyle Guitar Masterclass")
    }
    var videoTitle by remember {
        mutableStateOf(selectedPlaylistCourse?.videoTitle ?: "Lesson 1: Hand Posture & P-I-M-A Patterns")
    }
    var videoUrl by remember {
        mutableStateOf(selectedPlaylistCourse?.videoUrl ?: "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4")
    }
    var videoDuration by remember {
        mutableStateOf(selectedPlaylistCourse?.duration ?: "16:45")
    }
    var selectedThumbnail by remember {
        mutableStateOf(selectedPlaylistCourse?.thumbnailUrl ?: "https://images.unsplash.com/photo-1510915361894-db8b60106cb1?w=800")
    }
    var curriculumTopicsText by remember {
        mutableStateOf(
            selectedPlaylistCourse?.curriculumTopics?.joinToString("\n") ?:
            "Module 1: Foundations & Architecture\nModule 2: Practical Exercises & Live Demo\nModule 3: Advanced Optimization & Production Standards\nModule 4: 1-on-1 Code Review & Feedback"
        )
    }
    var message by remember {
        mutableStateOf("Hi ${selectedMentor.name}! I would love to exchange my course video and mentorship for your coaching in $wantSkill. Please review my lesson video and syllabus!")
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxSize(0.92f),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // DIALOG HEADER
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Rounded.SwapHoriz,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Propose Skill Swap & Video Exchange",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "★ Premium Pass Active • Provide video for reciprocal skill",
                                    fontSize = 11.sp,
                                    color = Color(0xFF10B981),
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = "Close",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // SCROLLABLE FORM BODY
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 1. RECIPIENT MENTOR PICKER
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "1. SELECT RECIPIENT MENTOR",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(allMentors, key = { it.id }) { mentor ->
                                    val isSelected = selectedMentor.id == mentor.id
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface,
                                        border = BorderStroke(
                                            if (isSelected) 1.5.dp else 1.dp,
                                            if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                                        ),
                                        modifier = Modifier.clickable {
                                            selectedMentor = mentor
                                            wantSkill = mentor.skillsTaught.firstOrNull() ?: wantSkill
                                            message = "Hi ${mentor.name}! I would love to exchange my course video and mentorship for your coaching in $wantSkill. Please review my lesson video and syllabus!"
                                        }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Surface(
                                                shape = CircleShape,
                                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Text(
                                                        text = mentor.name.take(1),
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 11.sp,
                                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Column {
                                                Text(
                                                    text = mentor.name,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                    fontSize = 12.sp,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Text(
                                                    text = mentor.skillsTaught.firstOrNull() ?: "Mentor",
                                                    fontSize = 10.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 2. DEFINE REQUEST COLUMNS (WHAT YOU OFFER vs WHAT YOU WANT)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(
                                text = "2. DEFINE REQUEST (OFFER vs WANT)",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp),
                                color = MaterialTheme.colorScheme.primary
                            )

                            // WHAT YOU OFFER
                            Column {
                                Text(
                                    text = "What You Offer (Your Expertise):",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                OutlinedTextField(
                                    value = offerSkill,
                                    onValueChange = { offerSkill = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    shape = RoundedCornerShape(8.dp)
                                )
                            }

                            // Quick Offer Suggestion Chips
                            val defaultOfferSkills = remember(currentUser) {
                                (currentUser.skillsTaught + listOf("Acoustic Guitar", "Android Development", "Music Theory", "Full Stack Web")).distinct()
                            }
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                items(defaultOfferSkills) { skill ->
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (offerSkill == skill) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface,
                                        border = BorderStroke(1.dp, if (offerSkill == skill) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant),
                                        modifier = Modifier.clickable { offerSkill = skill }
                                    ) {
                                        Text(
                                            text = skill,
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(2.dp))

                            // WHAT YOU WANT
                            Column {
                                Text(
                                    text = "What You Want (From ${selectedMentor.name}):",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFFD97706)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                OutlinedTextField(
                                    value = wantSkill,
                                    onValueChange = { wantSkill = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    shape = RoundedCornerShape(8.dp)
                                )
                            }

                            // Quick Want Suggestion Chips from mentor
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                items(selectedMentor.skillsTaught) { skill ->
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (wantSkill == skill) Color(0xFFD97706).copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface,
                                        border = BorderStroke(1.dp, if (wantSkill == skill) Color(0xFFD97706) else MaterialTheme.colorScheme.outlineVariant),
                                        modifier = Modifier.clickable {
                                            wantSkill = skill
                                            message = "Hi ${selectedMentor.name}! I would love to exchange my course video and mentorship for your coaching in $wantSkill. Please review my lesson video and syllabus!"
                                        }
                                    ) {
                                        Text(
                                            text = skill,
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 3. YOUR COURSE & VIDEO FROM PLAYLIST
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Rounded.VideoLibrary,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "3. SELECT COURSE & VIDEO FROM PLAYLIST",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Text(
                                    text = "Tap to auto-fill",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Text(
                                text = "Provide your video in exchange for the skill you are getting. Pick a course reference from your playlist below:",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            // PLAYLIST GALLERY PICKER
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(playlistItems) { item ->
                                    val isSelected = selectedPlaylistCourse?.videoTitle == item.videoTitle &&
                                            selectedPlaylistCourse?.courseTitle == item.courseTitle
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface,
                                        border = BorderStroke(
                                            if (isSelected) 2.dp else 1.dp,
                                            if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                                        ),
                                        modifier = Modifier
                                            .width(200.dp)
                                            .clickable {
                                                selectedPlaylistCourse = item
                                                courseReference = item.courseTitle
                                                videoTitle = item.videoTitle
                                                videoDuration = item.duration
                                                videoUrl = item.videoUrl
                                                selectedThumbnail = item.thumbnailUrl
                                                curriculumTopicsText = item.curriculumTopics.joinToString("\n")
                                                message = "Hi ${selectedMentor.name}! I would love to exchange my course '${item.courseTitle}' (${item.videoTitle}) for your coaching in $wantSkill. Please review my lesson video and syllabus!"
                                            }
                                    ) {
                                        Column(modifier = Modifier.padding(8.dp)) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(85.dp)
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(Color(0xFF1E1E1E))
                                            ) {
                                                AsyncImage(
                                                    model = item.thumbnailUrl,
                                                    contentDescription = item.videoTitle,
                                                    modifier = Modifier.fillMaxSize(),
                                                    contentScale = ContentScale.Crop
                                                )
                                                Surface(
                                                    shape = RoundedCornerShape(4.dp),
                                                    color = Color.Black.copy(alpha = 0.7f),
                                                    modifier = Modifier
                                                        .align(Alignment.BottomEnd)
                                                        .padding(4.dp)
                                                ) {
                                                    Text(
                                                        text = item.duration,
                                                        color = Color.White,
                                                        fontSize = 9.sp,
                                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                    )
                                                }
                                                if (isSelected) {
                                                    Surface(
                                                        shape = CircleShape,
                                                        color = MaterialTheme.colorScheme.primary,
                                                        modifier = Modifier
                                                            .align(Alignment.TopEnd)
                                                            .padding(4.dp)
                                                            .size(20.dp)
                                                    ) {
                                                        Box(contentAlignment = Alignment.Center) {
                                                            Icon(
                                                                imageVector = Icons.Rounded.Check,
                                                                contentDescription = "Selected",
                                                                tint = Color.White,
                                                                modifier = Modifier.size(14.dp)
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                text = item.courseTitle,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = item.videoTitle,
                                                fontSize = 10.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }

                            // DETAILED VIDEO & COURSE EDITABLE FIELDS
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Column {
                                    Text(
                                        text = "Course / Playlist Name Reference:",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    OutlinedTextField(
                                        value = courseReference,
                                        onValueChange = { courseReference = it },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Column(modifier = Modifier.weight(1.4f)) {
                                        Text(
                                            text = "Video Lesson Title:",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        OutlinedTextField(
                                            value = videoTitle,
                                            onValueChange = { videoTitle = it },
                                            modifier = Modifier.fillMaxWidth(),
                                            singleLine = true,
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                    }

                                    Column(modifier = Modifier.weight(0.6f)) {
                                        Text(
                                            text = "Duration:",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        OutlinedTextField(
                                            value = videoDuration,
                                            onValueChange = { videoDuration = it },
                                            modifier = Modifier.fillMaxWidth(),
                                            singleLine = true,
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                    }
                                }

                                Column {
                                    Text(
                                        text = "Video Stream Link / URL:",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    OutlinedTextField(
                                        value = videoUrl,
                                        onValueChange = { videoUrl = it },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                }

                                Column {
                                    Text(
                                        text = "Syllabus / Modules Overview (One module per line):",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    OutlinedTextField(
                                        value = curriculumTopicsText,
                                        onValueChange = { curriculumTopicsText = it },
                                        modifier = Modifier.fillMaxWidth(),
                                        maxLines = 4,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                }
                            }
                        }
                    }

                    // 4. PERSONAL PROPOSAL NOTE
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "4. NOTE TO RECIPIENT MENTOR",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = message,
                                onValueChange = { message = it },
                                modifier = Modifier.fillMaxWidth(),
                                maxLines = 3,
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                    }
                }

                // DIALOG FOOTER ACTIONS
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Cancel")
                        }

                        Button(
                            onClick = {
                                if (offerSkill.isBlank() || wantSkill.isBlank() || videoTitle.isBlank()) {
                                    Toast.makeText(context, "Please fill in what you offer, what you want, and your video title.", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                val parsedTopics = curriculumTopicsText
                                    .split("\n")
                                    .map { it.trim() }
                                    .filter { it.isNotBlank() }

                                val proposal = SwapProposal(
                                    id = "sp_outgoing_${System.currentTimeMillis()}",
                                    partner = selectedMentor,
                                    mySkill = offerSkill.trim(),
                                    partnerSkill = wantSkill.trim(),
                                    status = SwapStatus.PENDING,
                                    proposedDate = "Today",
                                    message = message.trim(),
                                    isPremiumSwap = true,
                                    courseOverview = courseReference.trim(),
                                    sampleVideoTitle = videoTitle.trim(),
                                    sampleVideoDuration = videoDuration.trim().ifBlank { "15:00" },
                                    sampleVideoUrl = videoUrl.trim().ifBlank { "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4" },
                                    sampleVideoThumbnailUrl = selectedThumbnail,
                                    curriculumTopics = if (parsedTopics.isNotEmpty()) parsedTopics else listOf(
                                        "Module 1: Foundations & Architecture",
                                        "Module 2: Practical Exercises & Live Implementation",
                                        "Module 3: Advanced Optimization & Standards",
                                        "Module 4: 1-on-1 Review & Feedback"
                                    ),
                                    compatibilityScore = 96,
                                    isOutgoing = true
                                )
                                onSendProposal(proposal)
                            },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Rounded.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Send Swap Request", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PurchaseSwapPassDialog(
    onDismiss: () -> Unit,
    onConfirmPurchase: () -> Unit
) {
    var selectedPaymentMethod by remember { mutableStateOf("UPI") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFFD97706).copy(alpha = 0.15f),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.AccountBalanceWallet,
                            contentDescription = null,
                            tint = Color(0xFFD97706),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Activate Mentor Swap Pass",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Price box
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    border = BorderStroke(1.dp, Color(0xFFD97706).copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "1 SkillBuilder Swap Pass",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Valid for 1 accepted skill swap",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Text(
                            text = "₹59",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 22.sp,
                            color = Color(0xFFD97706)
                        )
                    }
                }

                Text(
                    text = "PASS ADVANTAGES:",
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.5.sp, fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    BenefitRow("Send swap requests to other mentors with your video course reference")
                    BenefitRow("Match with verified mentors looking to exchange skills & video courses")
                    BenefitRow("Full syllabus & video preview inspection before accepting any swap")
                    BenefitRow("Direct 1-on-1 private encrypted chat unlocked on acceptance")
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "SELECT PAYMENT METHOD:",
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.5.sp, fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                PaymentOptionItem(
                    title = "Instant UPI (GPay / PhonePe / Paytm)",
                    isSelected = selectedPaymentMethod == "UPI",
                    onClick = { selectedPaymentMethod = "UPI" }
                )
                PaymentOptionItem(
                    title = "Credit / Debit Card",
                    isSelected = selectedPaymentMethod == "CARD",
                    onClick = { selectedPaymentMethod = "CARD" }
                )
                PaymentOptionItem(
                    title = "SkillBuilder Mentor Wallet",
                    isSelected = selectedPaymentMethod == "WALLET",
                    onClick = { selectedPaymentMethod = "WALLET" }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirmPurchase,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFD97706),
                    contentColor = Color.White
                )
            ) {
                Text("Pay ₹59 & Activate Pass", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun BenefitRow(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = Icons.Rounded.CheckCircle,
            contentDescription = null,
            tint = Color(0xFF10B981),
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun PaymentOptionItem(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        border = BorderStroke(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = isSelected,
                onClick = onClick,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun EmptyStateNotice(message: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Rounded.SwapHoriz,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(36.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}
