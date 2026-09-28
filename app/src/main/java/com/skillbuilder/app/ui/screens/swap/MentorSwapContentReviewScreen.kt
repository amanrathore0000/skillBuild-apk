package com.skillbuilder.app.ui.screens.swap

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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Handshake
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.skillbuilder.app.domain.model.MentorVideo
import com.skillbuilder.app.domain.model.SwapProposal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MentorSwapContentReviewScreen(
    proposal: SwapProposal,
    hasActiveSwapPass: Boolean,
    onDismiss: () -> Unit,
    onPurchasePass: () -> Unit,
    onAcceptSwap: (SwapProposal) -> Unit,
    onDeclineSwap: (SwapProposal) -> Unit,
    onOpenSampleVideo: ((MentorVideo) -> Unit)? = null
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // Header Top Bar
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Content & Compatibility Review",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Verify syllabus & teaching quality before accepting",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Compatibility Synergy Score Banner
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.5.dp, Color(0xFF10B981).copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = CircleShape,
                                        color = Color(0xFF10B981).copy(alpha = 0.15f),
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Rounded.Handshake,
                                                contentDescription = null,
                                                tint = Color(0xFF10B981),
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Skill Exchange Match",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFF10B981).copy(alpha = 0.15f),
                                    border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.35f))
                                ) {
                                    Text(
                                        text = "${proposal.compatibilityScore}% Compatibility",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                                        color = Color(0xFF10B981),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Two-way exchange box
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Partner offers
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "YOU WILL RECEIVE",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, letterSpacing = 0.5.sp),
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = proposal.partnerSkill,
                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.padding(8.dp),
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .padding(horizontal = 8.dp)
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.surfaceVariant),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.SwapHoriz,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                // You offer
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "YOU WILL TEACH",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, letterSpacing = 0.5.sp),
                                        color = Color(0xFFD97706),
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color(0xFFD97706).copy(alpha = 0.10f),
                                        border = BorderStroke(1.dp, Color(0xFFD97706).copy(alpha = 0.25f)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = proposal.mySkill,
                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                            color = Color(0xFFD97706),
                                            modifier = Modifier.padding(8.dp),
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Partner Mentor Profile Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (!proposal.partner.avatarUrl.isNullOrBlank()) {
                                    AsyncImage(
                                        model = proposal.partner.avatarUrl,
                                        contentDescription = proposal.partner.name,
                                        modifier = Modifier
                                            .size(52.dp)
                                            .clip(CircleShape),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .size(52.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = proposal.partner.name.take(2).uppercase(),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 18.sp,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = proposal.partner.name,
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(
                                            imageVector = Icons.Rounded.Verified,
                                            contentDescription = "Verified",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(2.dp))

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Rounded.Star,
                                            contentDescription = null,
                                            tint = Color(0xFFFFB800),
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(
                                            text = "${proposal.partner.rating} (${proposal.partner.reviewCount} mentor reviews)",
                                            style = MaterialTheme.typography.labelSmall,
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
                                        text = "Verified Mentor",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                                        color = Color(0xFF10B981),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                    )
                                }
                            }

                            if (proposal.partner.bio.isNotBlank()) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = proposal.partner.bio,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Message box (strictly no discussion of money or premium)
                            val cleanMessage = remember(proposal.message) {
                                proposal.message
                                    .replace("I have activated my ₹59 Swap Pass and ", "")
                                    .replace("I have activated my ₹59 Swap Pass ", "")
                                    .replace("I've got my ₹59 swap pass ready—", "")
                                    .replace("₹59", "")
                                    .replace("swap pass", "swap proposal", ignoreCase = true)
                                    .trim()
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = "NOTE FROM MENTOR:",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(
                                        text = "\"$cleanMessage\"",
                                        style = MaterialTheme.typography.bodySmall.copy(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }

                // Curriculum & Content Inspection ("Is this content of your type?")
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Rounded.MenuBook,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "SYLLABUS & CURRICULUM INSPECTION",
                                    style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 0.5.sp, fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Is this content suited for your learning objectives? Inspect the planned modules below:",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            if (proposal.courseOverview.isNotBlank()) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = proposal.courseOverview,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(10.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Module Topics list
                            val topics = if (proposal.curriculumTopics.isNotEmpty()) {
                                proposal.curriculumTopics
                            } else {
                                listOf(
                                    "Module 1: Core Fundamentals & Concept Breakdown",
                                    "Module 2: Practical Live Demonstrations & Exercises",
                                    "Module 3: Advanced Optimization & Production Techniques",
                                    "Module 4: 1-on-1 Code & Implementation Review"
                                )
                            }

                            topics.forEachIndexed { index, topic ->
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 3.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(
                                            shape = CircleShape,
                                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                            modifier = Modifier.size(22.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text(
                                                    text = "${index + 1}",
                                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = topic,
                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                            color = MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Icon(
                                            imageVector = Icons.Rounded.CheckCircle,
                                            contentDescription = null,
                                            tint = Color(0xFF10B981),
                                            modifier = Modifier.size(15.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Sample Video Preview Card
                item {
                    val previewTitle = proposal.sampleVideoTitle ?: "${proposal.partnerSkill} Masterclass Lesson 1"
                    val previewDuration = proposal.sampleVideoDuration ?: "14:20"

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
                                Text(
                                    text = "SAMPLE LESSON PREVIEW",
                                    style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 0.5.sp, fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Tap to verify quality",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Preview player box
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(16f / 9f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFF1F1F1F))
                                    .clickable {
                                        val sampleVideo = MentorVideo(
                                            id = "video_${proposal.id}",
                                            title = previewTitle,
                                            courseTitle = proposal.courseOverview.ifBlank { "${proposal.partnerSkill} Swap Curriculum" },
                                            duration = previewDuration,
                                            views = 1420,
                                            likes = 290,
                                            videoUrl = proposal.sampleVideoUrl ?: "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
                                            uploadDate = "Recently uploaded",
                                            thumbnailUrl = proposal.sampleVideoThumbnailUrl ?: "https://images.unsplash.com/photo-1516321318423-f06f85e504b3?w=800",
                                            description = proposal.message.ifBlank { "Sample preview lesson provided by ${proposal.partner.name} for verifying curriculum quality before accepting the skill swap proposal." },
                                            category = proposal.partnerSkill,
                                            level = "All Levels",
                                            price = "Free (Swap Preview)",
                                            mentorName = proposal.partner.name,
                                            tags = listOf("Skill Swap", "Curriculum Preview", "1-on-1")
                                        )
                                        onOpenSampleVideo?.invoke(sampleVideo)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                AsyncImage(
                                    model = proposal.sampleVideoThumbnailUrl ?: "https://images.unsplash.com/photo-1516321318423-f06f85e504b3?w=800",
                                    contentDescription = previewTitle,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )

                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(54.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Rounded.PlayArrow,
                                            contentDescription = "Play Preview",
                                            tint = Color.White,
                                            modifier = Modifier.size(32.dp)
                                        )
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color.Black.copy(alpha = 0.8f),
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .padding(8.dp)
                                ) {
                                    Text(
                                        text = previewDuration,
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = previewTitle,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // Bottom Action Controls
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = {
                            onDeclineSwap(proposal)
                            onDismiss()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
                    ) {
                        Icon(Icons.Rounded.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Decline", fontWeight = FontWeight.Bold)
                    }

                    val isDark = MaterialTheme.colorScheme.background == Color(0xFF2B2B2B)
                    Button(
                        onClick = {
                            if (!hasActiveSwapPass) {
                                onPurchasePass()
                            } else {
                                onAcceptSwap(proposal)
                                onDismiss()
                            }
                        },
                        modifier = Modifier
                            .weight(1.6f)
                            .height(48.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (hasActiveSwapPass) Color(0xFF10B981) else (if (isDark) Color(0xFF3B82F6) else MaterialTheme.colorScheme.primary),
                            contentColor = Color.White
                        )
                    ) {
                        Icon(
                            imageVector = if (hasActiveSwapPass) Icons.Rounded.CheckCircle else Icons.Rounded.School,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (hasActiveSwapPass) "Accept Swap Request" else "Get Pass to Accept",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}
