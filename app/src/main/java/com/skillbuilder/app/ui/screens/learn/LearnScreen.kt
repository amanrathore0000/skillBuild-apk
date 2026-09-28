package com.skillbuilder.app.ui.screens.learn

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material.icons.rounded.School
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.skillbuilder.app.data.local.AppSettings
import com.skillbuilder.app.data.local.AppThemeMode
import com.skillbuilder.app.data.local.SampleData
import com.skillbuilder.app.data.local.UserSession
import com.skillbuilder.app.domain.model.Course
import com.skillbuilder.app.domain.model.Lesson
import com.skillbuilder.app.domain.model.MentorVideo

@Composable
fun LearnScreen(
    onExploreCourses: (() -> Unit)? = null,
    onOpenVideo: (MentorVideo) -> Unit = {}
) {
    var selectedVideoForPlayer by remember { mutableStateOf<MentorVideo?>(null) }
    val enrolledIds by UserSession.enrolledVideoIds.collectAsState()
    val allVideos by SampleData.allVideosFlow.collectAsState()
    val themeMode by AppSettings.themeMode.collectAsState()

    val systemDark = isSystemInDarkTheme()
    val isDark = when (themeMode) {
        AppThemeMode.SYSTEM -> systemDark
        AppThemeMode.LIGHT -> false
        AppThemeMode.DARK -> true
    }

    // Dynamic Theme Colors matching the rest of the application
    val screenBg = if (isDark) Color(0xFF2B2B2B) else Color(0xFFFFFFFF)
    val textPrimary = if (isDark) Color(0xFFFFFFFF) else Color(0xFF111827)
    val textSecondary = if (isDark) Color(0xFFB3B3B3) else Color(0xFF6B7280)
    val headerColor = if (isDark) Color(0xFFB3B3B3) else Color(0xFF6B7280)
    val activeAccent = if (isDark) Color(0xFF5995E5) else Color(0xFF2464B8)
    val cardBg = if (isDark) Color(0xFF383838) else Color(0xFFF3F4F6)
    val cardFooterBg = if (isDark) Color(0xFF303030) else Color(0xFFECEEF2)
    val pillBg = if (isDark) Color(0xFF424242) else Color(0xFFE5E7EB)

    val handlePlayVideo: (MentorVideo) -> Unit = { video ->
        onOpenVideo(video)
        if (onOpenVideo == {}) {
            selectedVideoForPlayer = video
        }
    }

    val enrolledVideos = remember(enrolledIds, allVideos) {
        allVideos.filter { it.id in enrolledIds }
    }

    val totalActiveCount = SampleData.enrolledCourses.size + enrolledVideos.size

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(screenBg),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "MY ENROLLED COURSES & VIDEOS",
                        style = MaterialTheme.typography.labelMedium.copy(
                            letterSpacing = 1.sp,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        ),
                        color = headerColor
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Your Purchased & Active Learning Paths",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                        color = textSecondary
                    )
                }
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = activeAccent.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "$totalActiveCount Active",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = activeAccent,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // Empty state if nothing enrolled
        if (totalActiveCount == 0) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBg)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(activeAccent.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Rounded.School,
                                contentDescription = null,
                                tint = activeAccent,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "No Courses Enrolled Yet",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = textPrimary
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Explore courses, watch free previews, and enroll to unlock full lifetime access.",
                            style = MaterialTheme.typography.bodySmall,
                            color = textSecondary,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        if (onExploreCourses != null) {
                            Button(
                                onClick = onExploreCourses,
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = activeAccent,
                                    contentColor = Color.White
                                )
                            ) {
                                Text("Browse All Videos & Courses", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Newly enrolled videos (from server or mentor uploads)
        items(enrolledVideos) { video ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { handlePlayVideo(video) },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg)
            ) {
                Column {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Thumbnail with duration badge
                        Box(
                            modifier = Modifier
                                .width(110.dp)
                                .aspectRatio(16f / 9f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(cardFooterBg)
                        ) {
                            if (!video.thumbnailUrl.isNullOrBlank()) {
                                AsyncImage(
                                    model = video.thumbnailUrl,
                                    contentDescription = video.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            // Duration
                            Surface(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(4.dp),
                                shape = RoundedCornerShape(4.dp),
                                color = Color(0xFF1E1E1E).copy(alpha = 0.85f)
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
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = activeAccent.copy(alpha = 0.15f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Rounded.CheckCircle,
                                        contentDescription = null,
                                        tint = activeAccent,
                                        modifier = Modifier.size(11.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "UNLOCKED • FULL ACCESS",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                        color = activeAccent,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = video.title,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = textPrimary,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                lineHeight = 18.sp
                            )

                            Spacer(modifier = Modifier.height(2.dp))

                            Text(
                                text = "by ${video.mentorName} • ${video.category}",
                                style = MaterialTheme.typography.labelSmall,
                                color = textSecondary
                            )
                        }
                    }

                    // Bottom Watch Button Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(cardFooterBg)
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Lifetime Access Active",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            color = activeAccent,
                            fontWeight = FontWeight.SemiBold
                        )

                        Button(
                            onClick = { handlePlayVideo(video) },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = activeAccent,
                                contentColor = Color.White
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Rounded.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Watch Full Course", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Standard enrolled courses
        items(SampleData.enrolledCourses) { course ->
            CourseProgressCard(
                course = course,
                isDark = isDark,
                cardBg = cardBg,
                cardFooterBg = cardFooterBg,
                textPrimary = textPrimary,
                textSecondary = textSecondary,
                activeAccent = activeAccent,
                pillBg = pillBg,
                onPlayLesson = { lesson ->
                    handlePlayVideo(course.toMentorVideo(lesson))
                }
            )
        }
    }

    selectedVideoForPlayer?.let { video ->
        VideoDetailPlayerScreen(
            video = video,
            onDismiss = { selectedVideoForPlayer = null }
        )
    }
}

@Composable
private fun CourseProgressCard(
    course: Course,
    isDark: Boolean,
    cardBg: Color,
    cardFooterBg: Color,
    textPrimary: Color,
    textSecondary: Color,
    activeAccent: Color,
    pillBg: Color,
    onPlayLesson: (Lesson) -> Unit
) {
    var isCurriculumExpanded by remember { mutableStateOf(false) }
    val nextLesson = course.lessons.firstOrNull { !it.isCompleted } ?: course.lessons.firstOrNull()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                nextLesson?.let { onPlayLesson(it) }
            },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = activeAccent.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = course.price,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                        color = activeAccent,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Text(
                    text = "${course.progressPercent}% Complete",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                    color = activeAccent,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = course.title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = textPrimary
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Mentor: ${course.mentorName} • ${course.duration}",
                style = MaterialTheme.typography.bodySmall,
                color = textSecondary
            )

            Spacer(modifier = Modifier.height(12.dp))

            LinearProgressIndicator(
                progress = { course.progressPercent / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = activeAccent,
                trackColor = pillBg
            )

            Spacer(modifier = Modifier.height(14.dp))

            // First available lesson CTA
            if (nextLesson != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(cardFooterBg)
                        .clickable { onPlayLesson(nextLesson) }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(activeAccent),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Rounded.PlayArrow,
                                contentDescription = "Play",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Next Lesson",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = textSecondary
                            )
                            Text(
                                text = nextLesson.title,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = textPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Text(
                        text = nextLesson.duration,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                        color = textSecondary
                    )
                }
            }

            // Expandable Lessons List
            if (course.lessons.size > 1) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { isCurriculumExpanded = !isCurriculumExpanded }
                        .padding(vertical = 6.dp, horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (isCurriculumExpanded) "Hide Lessons ▲" else "View All ${course.lessons.size} Lessons ▼",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = activeAccent
                    )
                    Text(
                        text = "Tap to play any lesson",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = textSecondary
                    )
                }

                if (isCurriculumExpanded) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        course.lessons.forEachIndexed { index, lesson ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = cardFooterBg,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onPlayLesson(lesson) }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Surface(
                                            shape = CircleShape,
                                            color = activeAccent.copy(alpha = 0.15f),
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text(
                                                    text = "${index + 1}",
                                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
                                                    color = activeAccent
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = lesson.title,
                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                            color = textPrimary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = lesson.duration,
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                            color = textSecondary
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Icon(
                                            imageVector = Icons.Rounded.PlayCircle,
                                            contentDescription = "Play",
                                            tint = activeAccent,
                                            modifier = Modifier.size(16.dp)
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
}

private fun Course.toMentorVideo(lesson: Lesson): MentorVideo {
    val thumb = when (category) {
        "Culinary Arts" -> "https://images.unsplash.com/photo-1578985545062-69928b1d9587?w=800"
        "Design & Art" -> "https://images.unsplash.com/photo-1561070791-2526d30994b5?w=800"
        "Tech & Coding" -> "https://images.unsplash.com/photo-1517694712202-14dd9538aa97?w=800"
        "Music" -> "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=800"
        "Languages" -> "https://images.unsplash.com/photo-1546410531-bb4caa6b424d?w=800"
        else -> "https://images.unsplash.com/photo-1516321318423-f06f85e504b3?w=800"
    }
    return MentorVideo(
        id = "${id}_${lesson.id}",
        title = lesson.title,
        courseTitle = title,
        duration = lesson.duration,
        views = (rating * 3500).toInt(),
        likes = (rating * 420).toInt(),
        videoUrl = lesson.videoUrl,
        uploadDate = "Active Enrolled Course",
        thumbnailUrl = thumb,
        description = "Full course curriculum for '$title' guided by mentor $mentorName. This video lesson covers '${lesson.title}' with complete step-by-step instructions, practical exercises, and interactive mentor support.",
        category = category,
        level = "All Levels",
        price = "Enrolled",
        mentorName = mentorName,
        tags = listOf(category, "Enrolled", "Full Course", "HD Video", "Certificate Eligible")
    )
}
