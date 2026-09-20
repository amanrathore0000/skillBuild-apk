package com.skillbuilder.app.ui.screens.learn

import android.widget.Toast
import androidx.compose.animation.animateContentSize
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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.CloudDone
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.FolderShared
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.LockOpen
import androidx.compose.material.icons.rounded.MovieCreation
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material.icons.rounded.QuestionAnswer
import androidx.compose.material.icons.rounded.RadioButtonChecked
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.Wallet
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil3.compose.AsyncImage
import com.skillbuilder.app.data.local.SampleData
import com.skillbuilder.app.data.local.StoragePlanManager
import com.skillbuilder.app.data.local.UserSession
import com.skillbuilder.app.data.local.tr
import com.skillbuilder.app.domain.model.MentorVideo
import com.skillbuilder.app.ui.components.VideoPlayer
import com.skillbuilder.app.ui.screens.chat.EncryptedChatScreen

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun VideoDetailPlayerScreen(
    video: MentorVideo,
    onDismiss: () -> Unit,
    onNavigateToMyCourses: (() -> Unit)? = null
) {
    val context = LocalContext.current
    var currentVideo by remember(video) { mutableStateOf(video) }
    val enrolledIds by UserSession.enrolledVideoIds.collectAsState()
    val isEnrolled = currentVideo.id in enrolledIds

    var showEnrollSheet by remember { mutableStateOf(false) }
    var paymentSuccess by remember { mutableStateOf(false) }
    var isDescriptionExpanded by remember { mutableStateOf(false) }
    var isLiked by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedPayment by remember { mutableStateOf("UPI") }
    var isEncryptedChatOpen by remember { mutableStateOf(false) }

    val allVideos by SampleData.allVideosFlow.collectAsState()

    // Suggested videos related to this category & course (excluding current video)
    val suggestedVideos = remember(currentVideo, allVideos) {
        val sameCategory = allVideos.filter {
            it.category.equals(currentVideo.category, ignoreCase = true) && it.id != currentVideo.id
        }
        val otherVideos = allVideos.filter {
            !it.category.equals(currentVideo.category, ignoreCase = true) && it.id != currentVideo.id
        }
        (sameCategory + otherVideos).distinctBy { it.id }
    }

    androidx.activity.compose.BackHandler(onBack = onDismiss)

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
            Column(modifier = Modifier.fillMaxSize()) {

                // ── TOP NAVIGATION BAR (Header) ──────────────────────────────
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 3.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(38.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                contentDescription = tr("Back", "वापस"),
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = currentVideo.courseTitle.ifBlank { tr("Course Player", "कोर्स प्लेयर") },
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "by ${currentVideo.mentorName}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                ) {
                                    Text(
                                        text = currentVideo.category,
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // ── FIXED VIDEO SECTION AT TOP ───────────────────────────────
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Black)
                ) {
                    VideoPlayer(
                        videoUrl = currentVideo.videoUrl,
                        title = currentVideo.title,
                        modifier = Modifier.fillMaxWidth(),
                        isTrialMode = !isEnrolled,
                        trialLimitMillis = 30_000L,
                        onTrialExpired = {
                            Toast.makeText(context, "30-second trial finished. Enroll to unlock full course!", Toast.LENGTH_SHORT).show()
                        },
                        onEnrollClick = {
                            showEnrollSheet = true
                        }
                    )

                    // Status Tag on Top-Start of Player
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(10.dp),
                        shape = RoundedCornerShape(6.dp),
                        color = if (isEnrolled) Color(0xFF10B981).copy(alpha = 0.9f) else Color.Black.copy(alpha = 0.75f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isEnrolled) Icons.Rounded.CheckCircle else Icons.Rounded.PlayCircle,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isEnrolled) tr("UNLOCKED ACCESS", "पूर्ण ऐक्सेस उपलब्ध") else tr("TRIAL VIDEO PREVIEW", "ट्रायल वीडियो प्रिव्यू"),
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Storage Delivery Source Tag on Top-End of Player
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(10.dp),
                        shape = RoundedCornerShape(6.dp),
                        color = if (currentVideo.storageProvider == "GOOGLE_DRIVE") Color(0xFF0F9D58).copy(alpha = 0.9f) else Color(0xFF1976D2).copy(alpha = 0.9f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (currentVideo.storageProvider == "GOOGLE_DRIVE") Icons.Rounded.FolderShared else Icons.Rounded.CloudDone,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (currentVideo.storageProvider == "GOOGLE_DRIVE") "Google Drive (Private)" else "AWS S3 Cloud CDN",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                        }
                    }
                }

                // ── SCROLLABLE COURSE DETAILS & RELATED VIDEOS ────────────────
                LazyColumn(modifier = Modifier.weight(1f).navigationBarsPadding()) {

                    // 1. CHIPS (Category, Level, Price Tag)
                    item {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            InfoChip(text = currentVideo.category, isPrimary = true)
                            InfoChip(text = currentVideo.level, isPrimary = false)

                            // Price Tag Chip
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (currentVideo.price == "Free") Color(0xFF10B981).copy(alpha = 0.15f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                border = BorderStroke(1.dp, if (currentVideo.price == "Free") Color(0xFF10B981) else MaterialTheme.colorScheme.primary)
                            ) {
                                Text(
                                    text = if (currentVideo.price == "Free") tr("FREE TRIAL & COURSE", "निःशुल्क कोर्स") else currentVideo.price,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (currentVideo.price == "Free") Color(0xFF10B981) else MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    // 3. TITLE & MENTOR DETAILS
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                        ) {
                            Text(
                                text = currentVideo.title,
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                                color = MaterialTheme.colorScheme.onBackground,
                                lineHeight = 26.sp
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = currentVideo.mentorName.take(2).uppercase(),
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = currentVideo.mentorName,
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = currentVideo.courseTitle,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }

                                IconButton(onClick = { isLiked = !isLiked }) {
                                    Icon(
                                        imageVector = if (isLiked) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                                        contentDescription = "Like",
                                        tint = if (isLiked) Color(0xFFE91E63) else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                MiniStat(icon = Icons.Rounded.Visibility, text = "${currentVideo.views / 1000}K views")
                                MiniStat(icon = Icons.Rounded.Favorite, text = "${currentVideo.likes}", tintOverride = Color(0xFFE91E63))
                                Text(
                                    text = "Duration: ${currentVideo.duration}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "• ${currentVideo.uploadDate}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Quick Chat Action: Ask Doubt (Demand Video removed)
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFFF59E0B).copy(alpha = 0.12f),
                                border = BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.35f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable {
                                        isEncryptedChatOpen = true
                                    }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        Icons.Rounded.QuestionAnswer,
                                        contentDescription = null,
                                        tint = Color(0xFFD97706),
                                        modifier = Modifier.size(17.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Ask Doubt 💬",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = Color(0xFFD97706),
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }
                    }

                    // 4. DIVIDER
                    item {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                        )
                    }

                    // 5. ENROLLMENT / PAYMENT CTA CARD
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                        ) {
                            if (isEnrolled) {
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(14.dp),
                                    color = Color(0xFF10B981).copy(alpha = 0.12f),
                                    border = BorderStroke(1.5.dp, Color(0xFF10B981).copy(alpha = 0.4f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                Icons.Rounded.CheckCircle,
                                                contentDescription = null,
                                                tint = Color(0xFF10B981),
                                                modifier = Modifier.size(24.dp)
                                            )
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column {
                                                Text(
                                                    text = tr("Enrolled in Course", "कोर्स में नामांकित हैं"),
                                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                                    color = Color(0xFF10B981)
                                                )
                                                Text(
                                                    text = tr("You have full lifetime access to this course.", "आपके पास इस कोर्स का आजीवन ऐक्सेस है।"),
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }
                                }
                            } else {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surface
                                    ),
                                    border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(
                                                    text = tr("COURSE PRICE", "कोर्स शुल्क"),
                                                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Text(
                                                    text = if (currentVideo.price == "Free") tr("FREE", "निःशुल्क") else currentVideo.price,
                                                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }

                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                            ) {
                                                Text(
                                                    text = tr("Lifetime Access", "आजीवन ऐक्सेस"),
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.primary,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(14.dp))

                                        Button(
                                            onClick = { showEnrollSheet = true },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(54.dp),
                                            shape = RoundedCornerShape(12.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                        ) {
                                            Icon(Icons.Rounded.LockOpen, contentDescription = null, modifier = Modifier.size(20.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = if (currentVideo.price == "Free") {
                                                    tr("Enroll for Free Now", "निःशुल्क नामांकन करें")
                                                } else {
                                                    tr("Enroll & Buy — ${currentVideo.price}", "नामांकन एवं भुगतान करें — ${currentVideo.price}")
                                                },
                                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold)
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.Center,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                Icons.Rounded.Lock,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(13.dp)
                                            )
                                            Spacer(modifier = Modifier.width(5.dp))
                                            Text(
                                                text = tr("Safe & Instant Access Upon Payment", "भुगतान के बाद तुरंत पूरा कोर्स उपलब्ध"),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 6. ABOUT THIS VIDEO & CURRICULUM
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 16.dp)
                        ) {
                            Text(
                                text = tr("ABOUT THIS COURSE & TRIAL", "कोर्स एवं वीडियो का विवरण"),
                                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = currentVideo.description.ifBlank {
                                    "Learn step-by-step with ${currentVideo.mentorName}. This course offers hands-on exercises, guided trial demonstrations, and full curriculum access upon enrollment."
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = 22.sp,
                                maxLines = if (isDescriptionExpanded) Int.MAX_VALUE else 3,
                                overflow = if (isDescriptionExpanded) TextOverflow.Visible else TextOverflow.Ellipsis,
                                modifier = Modifier.animateContentSize()
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = if (isDescriptionExpanded) tr("Show less ▲", "कम देखें ▲") else tr("Show more ▼", "और पढ़ें ▼"),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.clickable { isDescriptionExpanded = !isDescriptionExpanded }
                            )
                        }
                    }

                    // 7. TAGS
                    if (currentVideo.tags.isNotEmpty()) {
                        item {
                            FlowRow(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                currentVideo.tags.forEach { tag ->
                                    Surface(
                                        shape = RoundedCornerShape(20.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant,
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                                    ) {
                                        Text(
                                            text = "#$tag",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 8. DIVIDER BEFORE SUGGESTED VIDEOS
                    item {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                        )
                    }

                    // 9. SUGGESTED VIDEOS RELATED TO THIS TYPE
                    item {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = tr("SUGGESTED COURSES & VIDEOS", "सुझाई गई संबंधित वीडियो"),
                                        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = tr("Related to ${currentVideo.category}", "${currentVideo.category} से संबंधित कोर्सेज"),
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    Text(
                                        text = "${suggestedVideos.size} " + tr("More", "अन्य"),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }

                    // Suggested Video Cards
                    items(suggestedVideos) { suggested ->
                        SuggestedVideoItem(
                            video = suggested,
                            onClick = {
                                currentVideo = suggested
                                isLiked = false
                                isDescriptionExpanded = false
                            }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(40.dp))
                    }
                }
            }
        }

    // ── PAYMENT BOTTOM SHEET (Enroll Modal) ──────────────────────────────────
    if (showEnrollSheet) {
        ModalBottomSheet(
            onDismissRequest = { showEnrollSheet = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.background,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            if (paymentSuccess) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF10B981).copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.CheckCircle,
                            contentDescription = "Success",
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(46.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = tr("Payment Successful!", "भुगतान सफल रहा!"),
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = tr(
                            "You are now enrolled in ${currentVideo.courseTitle}. Full lifetime video access is unlocked and available in your 'My Courses' column!",
                            "आप ${currentVideo.courseTitle} में नामांकित हो चुके हैं। पूरा वीडियो अनलॉक हो गया है और आपके 'My Courses' कॉलम में उपलब्ध है!"
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Storage Access Confirmation Banner
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        shape = RoundedCornerShape(10.dp),
                        color = if (currentVideo.storageProvider == "GOOGLE_DRIVE") Color(0xFF0F9D58).copy(alpha = 0.1f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        border = BorderStroke(
                            1.dp,
                            if (currentVideo.storageProvider == "GOOGLE_DRIVE") Color(0xFF0F9D58).copy(alpha = 0.3f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (currentVideo.storageProvider == "GOOGLE_DRIVE") Icons.Rounded.FolderShared else Icons.Rounded.CloudDone,
                                contentDescription = null,
                                tint = if (currentVideo.storageProvider == "GOOGLE_DRIVE") Color(0xFF0F9D58) else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (currentVideo.storageProvider == "GOOGLE_DRIVE") "Google Drive Access Linked" else "AWS CloudFront CDN Unlocked",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (currentVideo.storageProvider == "GOOGLE_DRIVE") {
                                        "Private viewer permission added for ${UserSession.currentUser.value.email}."
                                    } else {
                                        "Adaptive 1080p stream active with zero buffering globally."
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            showEnrollSheet = false
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Rounded.PlayCircle, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(tr("Watch Full Video Now", "पूरा वीडियो अभी देखें"), fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            showEnrollSheet = false
                            if (onNavigateToMyCourses != null) {
                                onNavigateToMyCourses()
                            } else {
                                onDismiss()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) {
                        Icon(Icons.Rounded.School, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(tr("View in My Courses", "माई कोर्सेज में देखें"), fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = tr("CONFIRM ENROLLMENT & PAYMENT", "नामांकन एवं भुगतान की पुष्टि"),
                        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = currentVideo.courseTitle,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                        color = MaterialTheme.colorScheme.onBackground,
                        maxLines = 2
                    )
                    Text(
                        text = tr("Mentor: ${currentVideo.mentorName}", "मेंटर: ${currentVideo.mentorName}"),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Price & Features Card
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f))
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = tr("Total Course Fee", "कुल कोर्स शुल्क"),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = if (currentVideo.price == "Free") tr("FREE", "निःशुल्क") else currentVideo.price,
                                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Column {
                                listOf(
                                    tr("Lifetime Access", "आजीवन ऐक्सेस"),
                                    tr("All Video Lessons", "सभी वीडियो पाठ"),
                                    tr("Certificate of Completion", "पूर्णता प्रमाण-पत्र"),
                                    tr("Direct Mentor Q&A", "मेंटर से प्रश्नोत्तर")
                                ).forEach {
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 2.dp)) {
                                        Icon(
                                            Icons.Rounded.CheckCircle,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(5.dp))
                                        Text(text = it, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface)
                                    }
                                }
                            }
                        }
                    }

                    if (currentVideo.price != "Free") {
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            text = tr("SELECT PAYMENT METHOD", "भुगतान का माध्यम चुनें"),
                            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        listOf(
                            Triple("UPI", "Google Pay, PhonePe, Paytm, BHIM", Icons.Rounded.Wallet),
                            Triple("Debit / Credit Card", "Visa, Mastercard, RuPay Cards", Icons.Rounded.CreditCard),
                            Triple("Net Banking", "All Indian & International Banks", Icons.Rounded.AccountBalance),
                            Triple("SkillWallet", "Instant 1-Click Payment", Icons.Rounded.School)
                        ).forEach { (method, sub, icon) ->
                            PaymentMethodRow(
                                method = method,
                                subtitle = sub,
                                icon = icon,
                                isSelected = selectedPayment == method,
                                onClick = { selectedPayment = method }
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            UserSession.enrollInVideo(currentVideo.id)
                            val currentUser = UserSession.currentUser.value
                            if (currentVideo.storageProvider == "GOOGLE_DRIVE") {
                                StoragePlanManager.grantDriveAccess(currentVideo.id, currentUser.email)
                            }
                            paymentSuccess = true
                            val successMsg = if (currentVideo.storageProvider == "GOOGLE_DRIVE") {
                                "🎉 Enrolled! Private Google Drive access granted to ${currentUser.email}"
                            } else {
                                "🎉 " + (if (currentVideo.price == "Free") "Enrolled successfully!" else "Payment successful! Enrolled in ${currentVideo.courseTitle}") + " (Streaming via AWS S3 Cloud CDN)"
                            }
                            Toast.makeText(context, successMsg, Toast.LENGTH_LONG).show()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Rounded.LockOpen, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (currentVideo.price == "Free") {
                                tr("Enroll for Free Now", "निःशुल्क नामांकन करें")
                            } else {
                                tr("Pay ${currentVideo.price} via $selectedPayment", "${currentVideo.price} का भुगतान करें ($selectedPayment)")
                            },
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold)
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }

        // Encrypted Chat with this Video's Mentor
        if (isEncryptedChatOpen) {
            val mentorUser = SampleData.mentors.find {
                it.name.equals(currentVideo.mentorName, ignoreCase = true)
            }
            val mentorId = mentorUser?.id ?: "mentor_${currentVideo.mentorName.replace(" ", "_").lowercase()}"

            EncryptedChatScreen(
                initialMentorId = mentorId,
                initialMentorName = currentVideo.mentorName,
                initialReferenceTopic = currentVideo.title,
                initialOpenAskDoubt = false,
                onDismiss = { isEncryptedChatOpen = false }
            )
        }
    }
}

@Composable
private fun SuggestedVideoItem(
    video: MentorVideo,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Thumbnail
            Box(
                modifier = Modifier
                    .width(110.dp)
                    .aspectRatio(16f / 9f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                if (!video.thumbnailUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = video.thumbnailUrl,
                        contentDescription = video.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Rounded.PlayCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                // Duration badge
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(4.dp),
                    shape = RoundedCornerShape(4.dp),
                    color = Color.Black.copy(alpha = 0.75f)
                ) {
                    Text(
                        text = video.duration,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = video.title,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "by ${video.mentorName}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${video.views / 1000}K views • ${video.level}",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = if (video.price == "Free") Color(0xFF10B981).copy(alpha = 0.15f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = if (video.price == "Free") "FREE" else video.price,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = if (video.price == "Free") Color(0xFF10B981) else MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoChip(text: String, isPrimary: Boolean) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = if (isPrimary) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(1.dp, if (isPrimary) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = if (isPrimary) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = if (isPrimary) FontWeight.Bold else FontWeight.Normal,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun MiniStat(icon: ImageVector, text: String, tintOverride: Color = Color.Unspecified) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (tintOverride == Color.Unspecified) MaterialTheme.colorScheme.onSurfaceVariant else tintOverride,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = text, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun PaymentMethodRow(
    method: String,
    subtitle: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.5.dp, if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
    ) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = method,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
                Text(text = subtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(
                imageVector = if (isSelected) Icons.Rounded.RadioButtonChecked else Icons.Rounded.RadioButtonUnchecked,
                contentDescription = null,
                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}
