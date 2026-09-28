package com.skillbuilder.app.ui.screens.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.CloudDone
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material.icons.rounded.VideoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import coil3.compose.AsyncImage
import com.skillbuilder.app.data.local.AppSettings
import com.skillbuilder.app.data.local.AppThemeMode
import com.skillbuilder.app.data.local.CertificateItem
import com.skillbuilder.app.data.local.DegreeProgramItem
import com.skillbuilder.app.data.local.ExploreCourseItem
import com.skillbuilder.app.data.local.ExploreData
import com.skillbuilder.app.data.local.ExploreTopicItem
import com.skillbuilder.app.data.local.SampleData
import com.skillbuilder.app.data.local.UserSession
import com.skillbuilder.app.domain.model.MentorVideo
import com.skillbuilder.app.domain.model.User
import com.skillbuilder.app.ui.screens.mentor.AllMentorsServerScreen
import com.skillbuilder.app.ui.screens.mentor.FullMentorProfileScreen
import com.skillbuilder.app.ui.screens.mentor.MentorYouTubeSearchScreen

@Composable
fun HomeScreen(
    onNavigateToCareer: () -> Unit = {},
    onNavigateToCourse: (String) -> Unit = {},
    onNavigateToEnrolled: () -> Unit = {},
    onOpenVideo: (MentorVideo) -> Unit = {},
    onOpenSearch: () -> Unit = {}
) {
    val topics = remember { ExploreData.topics }
    val mobileCourses = remember { ExploreData.mobileFocusedCourses }
    val degrees = remember { ExploreData.degreePrograms }
    val certs = remember { ExploreData.industryCertifications }
    val mentors = remember { SampleData.mentors }
    val allVideos by SampleData.allVideosFlow.collectAsState()
    val currentUser by UserSession.currentUser.collectAsState()
    var selectedMentorForProfile by remember { mutableStateOf<User?>(null) }
    var isAllMentorsOpen by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // ==================== SECTION 1: TOPICS ====================
        item {
            Column {
                ExploreSectionHeader(
                    title = "Topics",
                    actionText = "",
                    onActionClick = {}
                )
                Spacer(modifier = Modifier.height(12.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(topics) { topic ->
                        TopicChip(
                            topic = topic,
                            onClick = onNavigateToCareer
                        )
                    }
                }
            }
        }

        // ==================== SECTION 2: SKILLBUILDER PLUS BANNER OR MENTOR SERVER COURSE SEARCH ====================
        item {
            if (currentUser.isMentor) {
                MentorSearchEntryBar(
                    onClick = onOpenSearch
                )
            } else {
                PlusBanner(onFindOutMore = onNavigateToCareer)
            }
        }

        // ==================== SECTION 3: MOBILE FOCUSED COURSES ====================
        item {
            Column {
                ExploreSectionHeader(
                    title = "Mobile Focused",
                    actionText = "See All",
                    onActionClick = onNavigateToCareer
                )
                Spacer(modifier = Modifier.height(12.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(mobileCourses) { course ->
                        ExploreCourseCard(
                            course = course,
                            onClick = { onOpenVideo(course.mentorVideo) }
                        )
                    }
                }
            }
        }

        // ==================== SECTION 4: EARN YOUR DEGREE ====================
        item {
            Column {
                ExploreSectionHeader(
                    title = "Earn Your Degree",
                    actionText = "See All",
                    onActionClick = onNavigateToCareer
                )
                Spacer(modifier = Modifier.height(12.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(degrees) { degree ->
                        DegreeCard(
                            degree = degree,
                            onClick = { onOpenVideo(degree.mentorVideo) }
                        )
                    }
                }
            }
        }

        // ==================== SECTION 5: PREPARE FOR INDUSTRY CERTIFICATION ====================
        item {
            Column {
                ExploreSectionHeader(
                    title = "Prepare for Industry Certification",
                    actionText = "See All",
                    onActionClick = onNavigateToCareer
                )
                Spacer(modifier = Modifier.height(12.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(certs) { cert ->
                        CertificationCard(
                            cert = cert,
                            onClick = { onOpenVideo(cert.mentorVideo) }
                        )
                    }
                }
            }
        }

        // ==================== SECTION 6: COMMUNITY CREATORS & UPLOADED COURSES ====================
        if (allVideos.isNotEmpty()) {
            item {
                Column {
                    ExploreSectionHeader(
                        title = "Community Masterclasses",
                        actionText = "See All",
                        onActionClick = onNavigateToCareer
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(allVideos) { video ->
                            CommunityVideoCard(
                                video = video,
                                onClick = { onOpenVideo(video) }
                            )
                        }
                    }
                }
            }
        }

        // ==================== SECTION 7: TOP MENTORS & INSTRUCTORS ====================
        item {
            Column {
                ExploreSectionHeader(
                    title = "Top Mentors & Instructors",
                    actionText = "See All",
                    onActionClick = { isAllMentorsOpen = true }
                )
                Spacer(modifier = Modifier.height(12.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(mentors) { mentor ->
                        ExploreMentorCard(
                            mentor = mentor,
                            onMentorClick = { selectedMentorForProfile = mentor }
                        )
                    }
                }
            }
        }
    }

    selectedMentorForProfile?.let { mentor ->
        FullMentorProfileScreen(
            mentor = mentor,
            onDismiss = { selectedMentorForProfile = null },
            onVideoClick = onOpenVideo
        )
    }

    if (isAllMentorsOpen) {
        AllMentorsServerScreen(
            onDismiss = { isAllMentorsOpen = false },
            onOpenVideo = onOpenVideo
        )
    }
}

// ==================== UI COMPONENTS ====================

@Composable
fun ExploreSectionHeader(
    title: String,
    actionText: String = "See All",
    onActionClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            ),
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = actionText,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Normal,
                fontSize = 13.sp
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.clickable { onActionClick() }
        )
    }
}

@Composable
fun TopicChip(
    topic: ExploreTopicItem,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = Modifier.clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = topic.icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = topic.name,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                ),
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

@Composable
fun PlusBanner(
    onFindOutMore: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "skillbuilder",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp
                        ),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = "PLUS",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Black,
                                fontSize = 10.sp
                            ),
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "10,000+ courses, 1 price",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Button(
                onClick = onFindOutMore,
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.onBackground
                ),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "Find out more",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp
                    )
                )
            }
        }
    }
}

@Composable
fun MentorSearchEntryBar(
    onClick: () -> Unit
) {
    val themeMode by AppSettings.themeMode.collectAsState()
    val systemDark = isSystemInDarkTheme()
    val isDark = when (themeMode) {
        AppThemeMode.SYSTEM -> systemDark
        AppThemeMode.LIGHT -> false
        AppThemeMode.DARK -> true
        else -> systemDark
    }

    val surfaceCard = if (isDark) Color(0xFF383838) else Color(0xFFF8F9FA)
    val cardBorder = if (isDark) Color(0xFF4A4A4A) else Color(0xFFE5E7EB)
    val textPrimary = if (isDark) Color(0xFFFFFFFF) else Color(0xFF111827)
    val textSecondary = if (isDark) Color(0xFFB3B3B3) else Color(0xFF6B7280)
    val iconTint = if (isDark) Color(0xFF5995E5) else Color(0xFF2464B8)
    val micBg = if (isDark) Color(0xFF2B2B2B) else Color(0xFFF1F3F5)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(RoundedCornerShape(26.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(26.dp),
        color = surfaceCard,
        border = BorderStroke(1.2.dp, cardBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Rounded.Search,
                contentDescription = "Search",
                tint = iconTint,
                modifier = Modifier.size(22.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = "Search courses, topics, mentors...",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal
                ),
                color = textSecondary,
                modifier = Modifier.weight(1f)
            )

            Surface(
                shape = CircleShape,
                color = micBg,
                modifier = Modifier.size(34.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Rounded.Mic,
                        contentDescription = "Voice Search",
                        tint = textSecondary,
                        modifier = Modifier.size(17.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ExploreCourseCard(
    course: ExploreCourseItem,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(185.dp)
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(115.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            AsyncImage(
                model = course.thumbnailUrl,
                contentDescription = course.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            if (!course.orgBadgeText.isNullOrBlank()) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = Color(course.orgBadgeColor),
                    modifier = Modifier
                        .padding(6.dp)
                        .align(Alignment.TopStart)
                ) {
                    Text(
                        text = course.orgBadgeText,
                        color = Color(course.orgTextColor),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        ),
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = course.title,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                lineHeight = 17.sp
            ),
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            minLines = 2
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = course.organization,
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
            color = MaterialTheme.colorScheme.outline,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(1.dp))
        Text(
            text = course.type,
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
            color = MaterialTheme.colorScheme.outline
        )
        Spacer(modifier = Modifier.height(3.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Rounded.Star,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(3.dp))
            Text(
                text = "${course.rating}",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(3.dp))
            Text(
                text = "(${course.reviewCount})",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

@Composable
fun DegreeCard(
    degree: DegreeProgramItem,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(240.dp)
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            AsyncImage(
                model = degree.thumbnailUrl,
                contentDescription = degree.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = Color(degree.badgeBgColor),
                modifier = Modifier
                    .padding(6.dp)
                    .align(Alignment.BottomEnd)
            ) {
                Text(
                    text = degree.badgeText,
                    color = Color(degree.badgeTextColor),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    ),
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = degree.title,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                lineHeight = 17.sp
            ),
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            minLines = 2
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = degree.university,
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
            color = MaterialTheme.colorScheme.outline,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun CertificationCard(
    cert: CertificateItem,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(185.dp)
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(115.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            AsyncImage(
                model = cert.thumbnailUrl,
                contentDescription = cert.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = Color(cert.badgeBgColor),
                modifier = Modifier
                    .padding(6.dp)
                    .align(Alignment.TopStart)
            ) {
                Text(
                    text = cert.badgeText,
                    color = Color(cert.badgeTextColor),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    ),
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = cert.title,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                lineHeight = 17.sp
            ),
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            minLines = 2
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = cert.provider,
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
            color = MaterialTheme.colorScheme.outline,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(1.dp))
        Text(
            text = cert.type,
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
            color = MaterialTheme.colorScheme.outline
        )
        Spacer(modifier = Modifier.height(3.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Rounded.Star,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(3.dp))
            Text(
                text = "${cert.rating}",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(3.dp))
            Text(
                text = "(${cert.reviewCount})",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

@Composable
fun CommunityVideoCard(
    video: MentorVideo,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(185.dp)
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(115.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            AsyncImage(
                model = video.thumbnailUrl,
                contentDescription = video.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = video.title,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                lineHeight = 17.sp
            ),
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            minLines = 2
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = video.mentorName,
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
            color = MaterialTheme.colorScheme.outline,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(1.dp))
        Text(
            text = "Video Lesson",
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
            color = MaterialTheme.colorScheme.outline
        )
    }
}

@Composable
fun ExploreMentorCard(
    mentor: User,
    onMentorClick: () -> Unit
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val surfaceCard = if (isDark) Color(0xFF1E1E1E) else Color.White
    val cardBorder = if (isDark) Color(0xFF333333) else Color(0xFFE2E8F0)
    val activeAccent = if (isDark) Color(0xFF3B82F6) else Color(0xFF2563EB)
    val textPrimary = if (isDark) Color.White else Color(0xFF0F172A)
    val textSecondary = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)

    Card(
        modifier = Modifier
            .width(220.dp)
            .clickable { onMentorClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = surfaceCard),
        border = BorderStroke(1.dp, cardBorder)
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = activeAccent.copy(alpha = 0.15f),
                    modifier = Modifier.size(42.dp)
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
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = activeAccent
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = mentor.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = textPrimary
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Icon(
                            Icons.Rounded.Verified,
                            contentDescription = null,
                            tint = activeAccent,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                    Text(
                        text = "★ ${mentor.rating} (${mentor.reviewCount})",
                        fontSize = 11.sp,
                        color = textSecondary
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Offers: ${mentor.skillsTaught.joinToString(", ")}",
                fontSize = 11.sp,
                color = textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(10.dp))

            // Square card styled "View Profile" button adhering to app color shades
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (isDark) Color(0xFF262626) else Color(0xFFF1F5F9),
                border = BorderStroke(1.dp, if (isDark) Color(0xFF3E3E3E) else Color(0xFFCBD5E1)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onMentorClick() }
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
                        modifier = Modifier.size(14.dp)
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
