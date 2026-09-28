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
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil3.compose.AsyncImage
import com.skillbuilder.app.data.local.SampleData
import com.skillbuilder.app.domain.model.MentorVideo
import com.skillbuilder.app.domain.model.User
import com.skillbuilder.app.ui.screens.learn.VideoDetailPlayerScreen

/**
 * Full page profile for a particular mentor, designed with a premium look
 * adhering strictly to the app design system in both dark and light modes.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FullMentorProfileScreen(
    mentor: User,
    onDismiss: () -> Unit,
    onVideoClick: ((MentorVideo) -> Unit)? = null
) {
    var selectedVideoForPlayer by remember { mutableStateOf<MentorVideo?>(null) }

    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val bg = if (isDark) Color(0xFF121212) else Color(0xFFF8FAFC)
    val surfaceCard = if (isDark) Color(0xFF1E1E1E) else Color.White
    val cardBorder = if (isDark) Color(0xFF333333) else Color(0xFFE2E8F0)
    val activeAccent = if (isDark) Color(0xFF3B82F6) else Color(0xFF2563EB)
    val textPrimary = if (isDark) Color.White else Color(0xFF0F172A)
    val textSecondary = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)

    // Sourced live from real server video database
    val allVideos by SampleData.allVideosFlow.collectAsState()
    val mentorVideos = remember(mentor.name, mentor.id, allVideos) {
        val matches = allVideos.filter { video ->
            video.mentorName.equals(mentor.name, ignoreCase = true) ||
            mentor.skillsTaught.any { skill ->
                video.courseTitle.contains(skill, ignoreCase = true) ||
                video.category.contains(skill, ignoreCase = true)
            }
        }
        if (matches.isNotEmpty()) matches else allVideos.take(3)
    }

    Dialog(
        onDismissRequest = {
            if (selectedVideoForPlayer != null) {
                selectedVideoForPlayer = null
            } else {
                onDismiss()
            }
        },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(bg)
                    .statusBarsPadding()
            ) {
            // Top App Bar
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
                    text = "Mentor Profile",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    ),
                    color = textPrimary,
                    modifier = Modifier.weight(1f)
                )

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = activeAccent.copy(alpha = 0.15f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Verified,
                            contentDescription = null,
                            tint = activeAccent,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "VERIFIED",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            ),
                            color = activeAccent
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
            }

            HorizontalDivider(color = cardBorder, thickness = 0.8.dp)

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Profile Header Card
                item {
                    Spacer(modifier = Modifier.height(6.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = surfaceCard),
                        border = BorderStroke(1.dp, cardBorder)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Avatar
                            Surface(
                                shape = CircleShape,
                                color = activeAccent.copy(alpha = 0.15f),
                                border = BorderStroke(2.dp, activeAccent),
                                modifier = Modifier.size(86.dp)
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
                                            style = MaterialTheme.typography.headlineMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = activeAccent
                                            )
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = mentor.name,
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 20.sp
                                    ),
                                    color = textPrimary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.Rounded.Verified,
                                    contentDescription = "Verified Mentor",
                                    tint = activeAccent,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "Senior Mentor & Instructor",
                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                                color = textSecondary
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Rating and location row
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Star,
                                    contentDescription = null,
                                    tint = Color(0xFFFBBF24),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${mentor.rating} (${mentor.reviewCount} reviews)",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 12.sp
                                    ),
                                    color = textPrimary
                                )

                                if (mentor.location.isNotBlank()) {
                                    Spacer(modifier = Modifier.width(14.dp))
                                    Icon(
                                        imageVector = Icons.Rounded.LocationOn,
                                        contentDescription = null,
                                        tint = textSecondary,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(
                                        text = mentor.location,
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                        color = textSecondary
                                    )
                                }
                            }
                        }
                    }
                }

                // About Mentor Section
                if (mentor.bio.isNotBlank()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = surfaceCard),
                            border = BorderStroke(1.dp, cardBorder)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "ABOUT MENTOR",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    ),
                                    color = textSecondary
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = mentor.bio,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontSize = 14.sp,
                                        lineHeight = 20.sp
                                    ),
                                    color = textPrimary
                                )
                            }
                        }
                    }
                }

                // Skills Offered Section
                if (mentor.skillsTaught.isNotEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = surfaceCard),
                            border = BorderStroke(1.dp, cardBorder)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Rounded.School,
                                        contentDescription = null,
                                        tint = activeAccent,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "SKILLS TAUGHT",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 1.sp
                                        ),
                                        color = textSecondary
                                    )
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    mentor.skillsTaught.forEach { skill ->
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = activeAccent.copy(alpha = 0.12f),
                                            border = BorderStroke(1.dp, activeAccent.copy(alpha = 0.3f))
                                        ) {
                                            Text(
                                                text = skill,
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    fontWeight = FontWeight.SemiBold,
                                                    fontSize = 12.sp
                                                ),
                                                color = activeAccent,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Skills Mentor Wants To Learn (if any)
                if (mentor.skillsWanted.isNotEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = surfaceCard),
                            border = BorderStroke(1.dp, cardBorder)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "TARGET SKILLS TO LEARN",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    ),
                                    color = textSecondary
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    mentor.skillsWanted.forEach { skill ->
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (isDark) Color(0xFF2A2A2A) else Color(0xFFF1F5F9),
                                            border = BorderStroke(1.dp, cardBorder)
                                        ) {
                                            Text(
                                                text = skill,
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    fontWeight = FontWeight.Medium,
                                                    fontSize = 12.sp
                                                ),
                                                color = textPrimary,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Courses Published by this Mentor
                item {
                    Text(
                        text = "COURSES & MASTERCLASSES",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = textSecondary,
                        modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
                    )
                }

                items(mentorVideos, key = { it.id }) { video ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedVideoForPlayer = video
                            },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = surfaceCard),
                        border = BorderStroke(1.dp, cardBorder)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Thumbnail
                            Box(
                                modifier = Modifier
                                    .size(width = 80.dp, height = 56.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF222222)),
                                contentAlignment = Alignment.Center
                            ) {
                                if (!video.thumbnailUrl.isNullOrBlank()) {
                                    AsyncImage(
                                        model = video.thumbnailUrl,
                                        contentDescription = video.title,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                                Surface(
                                    shape = CircleShape,
                                    color = activeAccent.copy(alpha = 0.85f),
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            Icons.Rounded.PlayArrow,
                                            contentDescription = "Play",
                                            tint = Color.White,
                                            modifier = Modifier.size(15.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = video.courseTitle.ifBlank { video.title },
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp
                                    ),
                                    color = textPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${video.category} • ${video.duration}",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                    color = textSecondary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = if (video.price.isNotBlank() && !video.price.equals("Free", ignoreCase = true) && video.price != "0") video.price else "Free Access",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    ),
                                    color = Color(0xFF10B981)
                                )
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(28.dp))
                }
            }
        }

        // Dedicated in-dialog Video Player overlay
        selectedVideoForPlayer?.let { video ->
            VideoDetailPlayerScreen(
                video = video,
                onDismiss = { selectedVideoForPlayer = null },
                onNavigateToMyCourses = {
                    selectedVideoForPlayer = null
                    onDismiss()
                }
            )
        }
    }
}
}
