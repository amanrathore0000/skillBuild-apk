package com.skillbuilder.app.ui.screens.mentor

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil3.compose.AsyncImage
import com.skillbuilder.app.data.local.SampleData
import com.skillbuilder.app.data.local.UserAccountRepository
import com.skillbuilder.app.domain.model.MentorVideo
import com.skillbuilder.app.domain.model.User

/**
 * Full page displaying all mentors on the server with live search and quick profile view.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AllMentorsServerScreen(
    onDismiss: () -> Unit,
    onOpenVideo: (MentorVideo) -> Unit
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedMentorForFullProfile by remember { mutableStateOf<User?>(null) }

    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val bg = if (isDark) Color(0xFF121212) else Color(0xFFF8FAFC)
    val surfaceCard = if (isDark) Color(0xFF1E1E1E) else Color.White
    val cardBorder = if (isDark) Color(0xFF333333) else Color(0xFFE2E8F0)
    val activeAccent = if (isDark) Color(0xFF3B82F6) else Color(0xFF2563EB)
    val textPrimary = if (isDark) Color.White else Color(0xFF0F172A)
    val textSecondary = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)

    // Gather all mentors from sample data + user registered mentors on the server
    val allServerMentors = remember {
        val repo = UserAccountRepository.getInstance(context)
        val registeredMentors = repo.getRegisteredAccounts()
            .filter { it.isMentor }
            .map { it.toUser() }

        val combined = (SampleData.mentors + registeredMentors).distinctBy { it.email.lowercase() }
        combined
    }

    val filteredMentors = remember(searchQuery, allServerMentors) {
        val q = searchQuery.trim().lowercase()
        if (q.isBlank()) allServerMentors
        else allServerMentors.filter { mentor ->
            mentor.name.lowercase().contains(q) ||
            mentor.bio.lowercase().contains(q) ||
            mentor.skillsTaught.any { it.lowercase().contains(q) }
        }
    }

    selectedMentorForFullProfile?.let { mentor ->
        FullMentorProfileScreen(
            mentor = mentor,
            onDismiss = { selectedMentorForFullProfile = null },
            onVideoClick = onOpenVideo
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(bg)
                .statusBarsPadding()
        ) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Back",
                        tint = textPrimary
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                Text(
                    text = "All Mentors on Server",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    ),
                    color = textPrimary,
                    modifier = Modifier.weight(1f)
                )

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFF10B981).copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "${allServerMentors.size} ONLINE",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        ),
                        color = Color(0xFF10B981),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
            }

            HorizontalDivider(color = cardBorder, thickness = 0.8.dp)

            // Search Bar
            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search mentors by name or skill...", fontSize = 13.sp, color = textSecondary) },
                    leadingIcon = {
                        Icon(Icons.Rounded.Search, contentDescription = null, tint = activeAccent, modifier = Modifier.size(18.dp))
                    },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Rounded.Close, contentDescription = "Clear", tint = textSecondary, modifier = Modifier.size(16.dp))
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = activeAccent,
                        unfocusedBorderColor = cardBorder,
                        focusedContainerColor = surfaceCard,
                        unfocusedContainerColor = surfaceCard,
                        focusedTextColor = textPrimary,
                        unfocusedTextColor = textPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredMentors, key = { it.id }) { mentor ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = surfaceCard),
                        border = BorderStroke(1.dp, cardBorder)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // Avatar
                                Surface(
                                    shape = CircleShape,
                                    color = activeAccent.copy(alpha = 0.15f),
                                    modifier = Modifier.size(50.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        if (!mentor.avatarUrl.isNullOrBlank()) {
                                            AsyncImage(
                                                model = mentor.avatarUrl,
                                                contentDescription = mentor.name,
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = ContentScale.Crop
                                            )
                                        } else {
                                            Text(
                                                text = mentor.name.take(2).uppercase(),
                                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                                color = activeAccent
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = mentor.name,
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = textPrimary
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(
                                            Icons.Rounded.Verified,
                                            contentDescription = "Verified",
                                            tint = activeAccent,
                                            modifier = Modifier.size(15.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(2.dp))

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Rounded.Star,
                                            contentDescription = null,
                                            tint = Color(0xFFFBBF24),
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(
                                            text = "${mentor.rating} (${mentor.reviewCount} reviews)",
                                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                            color = textSecondary
                                        )
                                    }
                                }
                            }

                            if (mentor.bio.isNotBlank()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = mentor.bio,
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, lineHeight = 17.sp),
                                    color = textSecondary,
                                    maxLines = 2
                                )
                            }

                            if (mentor.skillsTaught.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(10.dp))
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    mentor.skillsTaught.forEach { skill ->
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = activeAccent.copy(alpha = 0.12f),
                                            border = BorderStroke(0.8.dp, activeAccent.copy(alpha = 0.25f))
                                        ) {
                                            Text(
                                                text = skill,
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Medium),
                                                color = activeAccent,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Square card style "View Profile" button
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isDark) Color(0xFF262626) else Color(0xFFF1F5F9),
                                border = BorderStroke(1.dp, if (isDark) Color(0xFF3E3E3E) else Color(0xFFCBD5E1)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { selectedMentorForFullProfile = mentor }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Person,
                                        contentDescription = null,
                                        tint = activeAccent,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "View Profile",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (isDark) Color.White else Color(0xFF1E293B)
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(28.dp))
                }
            }
        }
    }
}
