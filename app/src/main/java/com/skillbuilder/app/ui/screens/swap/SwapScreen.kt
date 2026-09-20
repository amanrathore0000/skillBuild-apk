package com.skillbuilder.app.ui.screens.swap

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChatBubbleOutline
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.skillbuilder.app.data.local.SampleData
import com.skillbuilder.app.domain.model.SwapProposal
import com.skillbuilder.app.domain.model.SwapStatus
import com.skillbuilder.app.domain.model.User
import com.skillbuilder.app.ui.screens.mentor.PublicMentorProfileSheet

@Composable
fun SwapScreen() {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }
    var selectedMentorForProfile by remember { mutableStateOf<User?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    val proposals by com.skillbuilder.app.data.local.RealTimeDataManager.swapProposalsFlow.collectAsState()

    val pendingCount = proposals.count { it.status == SwapStatus.PENDING }
    val activeCount = proposals.count { it.status == SwapStatus.ACTIVE }
    val tabs = listOf("Proposals ($pendingCount)", "Active Swaps ($activeCount)", "Matches")

    val query = searchQuery.trim().lowercase()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Universal Search Bar for Swaps
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            placeholder = { Text("Search swaps, mentors, or skills...") },
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

        // Tab Row
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary,
            edgePadding = 16.dp
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = title,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }
        }

        // Tab Content
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when (selectedTab) {
                0 -> {
                    // Proposals Inbox
                    val pendingList = proposals.filter {
                        it.status == SwapStatus.PENDING && (
                            query.isEmpty() ||
                            it.partner.name.lowercase().contains(query) ||
                            it.mySkill.lowercase().contains(query) ||
                            it.partnerSkill.lowercase().contains(query) ||
                            it.message.lowercase().contains(query)
                        )
                    }
                    if (pendingList.isEmpty()) {
                        item {
                            if (query.isNotBlank()) {
                                EmptyStateNotice("No pending swap proposals matching \"$searchQuery\".")
                            } else {
                                EmptyStateNotice("No pending swap proposals right now.")
                            }
                        }
                    } else {
                        items(pendingList, key = { it.id }) { proposal ->
                            ProposalCard(
                                proposal = proposal,
                                onAccept = {
                                    com.skillbuilder.app.data.local.RealTimeDataManager.updateSwapStatus(proposal.id, SwapStatus.ACTIVE)
                                    Toast.makeText(context, "Skill Swap Accepted! Collaboration unlocked.", Toast.LENGTH_SHORT).show()
                                },
                                onDecline = {
                                    com.skillbuilder.app.data.local.RealTimeDataManager.deleteSwapProposal(proposal.id)
                                    Toast.makeText(context, "Proposal declined.", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    }
                }

                1 -> {
                    // Active Swaps
                    val activeList = proposals.filter {
                        it.status == SwapStatus.ACTIVE && (
                            query.isEmpty() ||
                            it.partner.name.lowercase().contains(query) ||
                            it.mySkill.lowercase().contains(query) ||
                            it.partnerSkill.lowercase().contains(query)
                        )
                    }
                    if (activeList.isEmpty()) {
                        item {
                            if (query.isNotBlank()) {
                                EmptyStateNotice("No active swaps matching \"$searchQuery\".")
                            } else {
                                EmptyStateNotice("No active swaps. Accept a proposal or match with a mentor to start!")
                            }
                        }
                    } else {
                        items(activeList, key = { it.id }) { proposal ->
                            ActiveSwapCard(
                                proposal = proposal,
                                onOpenChat = {
                                    Toast.makeText(context, "Opening chat with ${proposal.partner.name}...", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    }
                }

                2 -> {
                    // Reciprocal Matches
                    val matchesList = SampleData.reciprocalMatches.filter {
                        query.isEmpty() ||
                        it.name.lowercase().contains(query) ||
                        it.skillsTaught.any { s -> s.lowercase().contains(query) } ||
                        it.skillsWanted.any { s -> s.lowercase().contains(query) }
                    }
                    if (matchesList.isEmpty()) {
                        item {
                            EmptyStateNotice("No reciprocal matches found for \"$searchQuery\".")
                        }
                    } else {
                        items(matchesList, key = { it.id }) { match ->
                            ReciprocalMatchCard(
                                user = match,
                                onViewProfile = { selectedMentorForProfile = match },
                                onRequestSwap = {
                                    val newProposal = SwapProposal(
                                        id = "sp_${System.currentTimeMillis()}",
                                        partner = match,
                                        mySkill = "Mobile & Web Dev",
                                        partnerSkill = match.skillsTaught.firstOrNull() ?: "Expertise",
                                        status = SwapStatus.PENDING,
                                        proposedDate = "Today",
                                        message = "Hi ${match.name}, I'd love to swap skills with you!"
                                    )
                                    com.skillbuilder.app.data.local.RealTimeDataManager.addSwapProposal(newProposal)
                                    Toast.makeText(context, "Swap proposal sent to ${match.name}!", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    selectedMentorForProfile?.let { mentor ->
        PublicMentorProfileSheet(
            mentor = mentor,
            onDismiss = { selectedMentorForProfile = null }
        )
    }
}

@Composable
private fun ProposalCard(
    proposal: SwapProposal,
    onAccept: () -> Unit,
    onDecline: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
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
                    Box(
                        modifier = Modifier
                            .size(40.dp)
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
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = proposal.partner.name,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = proposal.proposedDate,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFFF59E0B).copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "PROPOSED",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFF59E0B),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Barter exchange chip layout
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("YOU TEACH", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(proposal.mySkill, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
                }

                Icon(Icons.Rounded.SwapHoriz, contentDescription = null, tint = MaterialTheme.colorScheme.primary)

                Column(horizontalAlignment = Alignment.End) {
                    Text("YOU LEARN", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(proposal.partnerSkill, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "\"${proposal.message}\"",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onDecline,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Rounded.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Decline")
                }

                Button(
                    onClick = onAccept,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Rounded.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Accept Swap")
                }
            }
        }
    }
}

@Composable
private fun ActiveSwapCard(
    proposal: SwapProposal,
    onOpenChat: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Active Swap with ${proposal.partner.name}",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFF10B981).copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "ACTIVE",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF10B981),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Exchanging ${proposal.mySkill} for ${proposal.partnerSkill}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = onOpenChat,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Rounded.ChatBubbleOutline, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Open 1-on-1 Swap Chat")
            }
        }
    }
}

@Composable
private fun ReciprocalMatchCard(
    user: com.skillbuilder.app.domain.model.User,
    onViewProfile: () -> Unit,
    onRequestSwap: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = user.name,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = user.bio,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onViewProfile,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("View Playlist")
                }
                Button(
                    onClick = onRequestSwap,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Propose Swap")
                }
            }
        }
    }
}

@Composable
private fun EmptyStateNotice(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 40.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
