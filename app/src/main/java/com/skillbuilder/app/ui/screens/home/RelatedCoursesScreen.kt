package com.skillbuilder.app.ui.screens.home

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.FilterList
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil3.compose.AsyncImage
import com.skillbuilder.app.data.local.CertificateItem
import com.skillbuilder.app.data.local.DegreeProgramItem
import com.skillbuilder.app.data.local.ExploreCourseItem
import com.skillbuilder.app.data.local.ExploreData
import com.skillbuilder.app.data.local.SampleData
import com.skillbuilder.app.domain.model.MentorVideo
import com.skillbuilder.app.ui.screens.learn.VideoDetailPlayerScreen
import com.skillbuilder.app.ui.theme.ObsidianCanvas

enum class RelatedCourseType {
    MOBILE_FOCUSED,
    DEGREES,
    CERTIFICATIONS,
    COMMUNITY_MASTERCLASSES,
    TOPIC,
    ALL
}

data class RelatedCoursesArgs(
    val type: RelatedCourseType,
    val title: String,
    val subtitle: String? = null,
    val topicFilter: String? = null
)

/**
 * Unified representation of a course across Explore, Degrees, Certifications, and Masterclasses.
 */
data class UnifiedCourseItem(
    val id: String,
    val title: String,
    val provider: String,
    val type: String,
    val rating: Float,
    val reviewCount: String,
    val thumbnailUrl: String,
    val badgeText: String? = null,
    val badgeBgColor: Long = 0xFF2464B8,
    val badgeTextColor: Long = 0xFFFFFFFF,
    val duration: String = "Self-paced",
    val level: String = "All Levels",
    val category: String = "General",
    val price: String = "Free with SkillBuilder",
    val description: String = "",
    val mentorVideo: MentorVideo
)

fun ExploreCourseItem.toUnified() = UnifiedCourseItem(
    id = id,
    title = title,
    provider = organization,
    type = type,
    rating = rating,
    reviewCount = reviewCount,
    thumbnailUrl = thumbnailUrl,
    badgeText = orgBadgeText,
    badgeBgColor = orgBadgeColor,
    badgeTextColor = orgTextColor,
    duration = mentorVideo.duration,
    level = mentorVideo.level,
    category = mentorVideo.category,
    price = mentorVideo.price,
    description = mentorVideo.description,
    mentorVideo = mentorVideo
)

fun DegreeProgramItem.toUnified() = UnifiedCourseItem(
    id = id,
    title = title,
    provider = university,
    type = degreeType,
    rating = 4.9f,
    reviewCount = "${(mentorVideo.views / 95).coerceAtLeast(120)} reviews",
    thumbnailUrl = thumbnailUrl,
    badgeText = badgeText,
    badgeBgColor = badgeBgColor,
    badgeTextColor = badgeTextColor,
    duration = mentorVideo.duration,
    level = mentorVideo.level,
    category = mentorVideo.category,
    price = mentorVideo.price,
    description = mentorVideo.description,
    mentorVideo = mentorVideo
)

fun CertificateItem.toUnified() = UnifiedCourseItem(
    id = id,
    title = title,
    provider = provider,
    type = type,
    rating = rating,
    reviewCount = reviewCount,
    thumbnailUrl = thumbnailUrl,
    badgeText = badgeText,
    badgeBgColor = badgeBgColor,
    badgeTextColor = badgeTextColor,
    duration = mentorVideo.duration,
    level = mentorVideo.level,
    category = mentorVideo.category,
    price = mentorVideo.price,
    description = mentorVideo.description,
    mentorVideo = mentorVideo
)

fun MentorVideo.toUnified() = UnifiedCourseItem(
    id = id,
    title = if (courseTitle.isNotBlank() && !title.contains(courseTitle, ignoreCase = true)) "$courseTitle: $title" else title,
    provider = mentorName.ifBlank { "SkillBuilder Mentor" },
    type = if (price.contains("Degree", ignoreCase = true)) "Degree" else if (price.contains("Cert", ignoreCase = true)) "Certificate" else "Masterclass",
    rating = 4.8f,
    reviewCount = "${(views / 65).coerceAtLeast(24)} reviews",
    thumbnailUrl = thumbnailUrl ?: "https://images.unsplash.com/photo-1516321318423-f06f85e504b3?w=600",
    badgeText = if (mentorName.isNotBlank()) mentorName.take(2).uppercase() else "SB",
    badgeBgColor = 0xFF5995E5,
    badgeTextColor = 0xFFFFFFFF,
    duration = duration,
    level = level,
    category = category,
    price = price,
    description = description,
    mentorVideo = this
)

/**
 * Full-screen dedicated Related Courses page opened when user clicks "See All"
 * on any course category or topic in both Mentor and Learner interfaces.
 */
@Composable
fun RelatedCoursesScreen(
    args: RelatedCoursesArgs,
    onDismiss: () -> Unit,
    onOpenVideo: (MentorVideo) -> Unit
) {
    BackHandler(onBack = onDismiss)

    val focusManager = LocalFocusManager.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") }
    var selectedVideoForPlayer by remember { mutableStateOf<MentorVideo?>(null) }

    val allVideos by SampleData.allVideosFlow.collectAsState()

    // Dynamic theme colors
    val isDark = MaterialTheme.colorScheme.background == ObsidianCanvas ||
            MaterialTheme.colorScheme.background.luminance() < 0.5f

    val bg = if (isDark) Color(0xFF121212) else Color(0xFFF8FAFC)
    val surfaceCard = if (isDark) Color(0xFF1E1E1E) else Color.White
    val cardBorder = if (isDark) Color(0xFF2E2E2E) else Color(0xFFE2E8F0)
    val textPrimary = if (isDark) Color.White else Color(0xFF0F172A)
    val textSecondary = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
    val primaryColor = MaterialTheme.colorScheme.primary
    val emeraldColor = Color(0xFF10B981)

    // Assemble course list according to section type
    val rawList = remember(args, allVideos) {
        when (args.type) {
            RelatedCourseType.MOBILE_FOCUSED -> {
                ExploreData.mobileFocusedCourses.map { it.toUnified() }
            }
            RelatedCourseType.DEGREES -> {
                ExploreData.degreePrograms.map { it.toUnified() }
            }
            RelatedCourseType.CERTIFICATIONS -> {
                ExploreData.industryCertifications.map { it.toUnified() }
            }
            RelatedCourseType.COMMUNITY_MASTERCLASSES -> {
                allVideos.map { it.toUnified() }
            }
            RelatedCourseType.TOPIC -> {
                val topic = args.topicFilter.orEmpty().lowercase()
                val fromExplore = ExploreData.mobileFocusedCourses
                    .filter { it.mentorVideo.category.lowercase().contains(topic) }
                    .map { it.toUnified() }
                val fromDegrees = ExploreData.degreePrograms
                    .filter { it.mentorVideo.category.lowercase().contains(topic) }
                    .map { it.toUnified() }
                val fromCerts = ExploreData.industryCertifications
                    .filter { it.mentorVideo.category.lowercase().contains(topic) }
                    .map { it.toUnified() }
                val fromVideos = allVideos
                    .filter { it.category.lowercase().contains(topic) }
                    .map { it.toUnified() }

                val combined = fromExplore + fromDegrees + fromCerts + fromVideos
                if (combined.isNotEmpty()) combined else allVideos.take(6).map { it.toUnified() }
            }
            RelatedCourseType.ALL -> {
                val fromExplore = ExploreData.mobileFocusedCourses.map { it.toUnified() }
                val fromDegrees = ExploreData.degreePrograms.map { it.toUnified() }
                val fromCerts = ExploreData.industryCertifications.map { it.toUnified() }
                val fromVideos = allVideos.map { it.toUnified() }
                (fromExplore + fromDegrees + fromCerts + fromVideos).distinctBy { it.id }
            }
        }
    }

    // Dynamic filter chips derived from available course categories & levels
    val filterOptions = remember(rawList) {
        val categories = rawList.map { it.category }.distinct().filter { it.isNotBlank() }
        val levels = listOf("Beginner", "Intermediate", "Advanced")
        listOf("All") + categories + levels
    }

    val filteredList = remember(rawList, searchQuery, selectedFilter) {
        val q = searchQuery.trim().lowercase()
        rawList.filter { item ->
            val matchesSearch = q.isBlank() ||
                    item.title.lowercase().contains(q) ||
                    item.provider.lowercase().contains(q) ||
                    item.category.lowercase().contains(q) ||
                    item.level.lowercase().contains(q) ||
                    item.description.lowercase().contains(q)

            val matchesFilter = when (selectedFilter) {
                "All" -> true
                "Beginner", "Intermediate", "Advanced" -> item.level.equals(selectedFilter, ignoreCase = true)
                else -> item.category.equals(selectedFilter, ignoreCase = true)
            }

            matchesSearch && matchesFilter
        }
    }

    val handleOpenVideo: (MentorVideo) -> Unit = { video ->
        onOpenVideo(video)
        selectedVideoForPlayer = video
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
                // ==================== TOP NAVIGATION HEADER ====================
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
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

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = args.title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            ),
                            color = textPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        val countText = "${filteredList.size} ${if (filteredList.size == 1) "course" else "courses"} available"
                        Text(
                            text = if (!args.subtitle.isNullOrBlank()) "${args.subtitle} • $countText" else countText,
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = textSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = primaryColor.copy(alpha = 0.12f),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text(
                            text = "${filteredList.size}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            ),
                            color = primaryColor,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                HorizontalDivider(color = cardBorder, thickness = 1.dp)

                // ==================== SEARCH BAR ====================
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text(
                                text = "Search in ${args.title}...",
                                color = textSecondary,
                                fontSize = 13.sp
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Rounded.Search,
                                contentDescription = null,
                                tint = textSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(
                                        Icons.Rounded.Clear,
                                        contentDescription = "Clear search",
                                        tint = textSecondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = surfaceCard,
                            unfocusedContainerColor = surfaceCard,
                            focusedBorderColor = primaryColor,
                            unfocusedBorderColor = cardBorder,
                            focusedTextColor = textPrimary,
                            unfocusedTextColor = textPrimary
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // ==================== FILTER CHIPS ====================
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        filterOptions.forEach { filter ->
                            val isSelected = selectedFilter.equals(filter, ignoreCase = true)
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    selectedFilter = if (isSelected && filter != "All") "All" else filter
                                },
                                label = {
                                    Text(
                                        text = filter,
                                        fontSize = 11.5.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                leadingIcon = if (isSelected) {
                                    {
                                        Icon(
                                            Icons.Rounded.CheckCircle,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                } else null,
                                shape = RoundedCornerShape(8.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = primaryColor.copy(alpha = 0.16f),
                                    selectedLabelColor = primaryColor,
                                    selectedLeadingIconColor = primaryColor,
                                    containerColor = surfaceCard,
                                    labelColor = textSecondary
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = isSelected,
                                    borderColor = cardBorder,
                                    selectedBorderColor = primaryColor
                                )
                            )
                        }
                    }
                }

                // ==================== LIST OF COURSES ====================
                if (filteredList.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Rounded.Search,
                                contentDescription = null,
                                tint = textSecondary.copy(alpha = 0.5f),
                                modifier = Modifier.size(54.dp)
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "No courses found",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = textPrimary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Try adjusting your search query or removing active filters.",
                                style = MaterialTheme.typography.bodySmall,
                                color = textSecondary
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = {
                                    searchQuery = ""
                                    selectedFilter = "All"
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Reset Filters", fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 48.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(filteredList, key = { it.id }) { course ->
                            RelatedCourseCard(
                                course = course,
                                isDark = isDark,
                                surfaceCard = surfaceCard,
                                cardBorder = cardBorder,
                                textPrimary = textPrimary,
                                textSecondary = textSecondary,
                                primaryColor = primaryColor,
                                emeraldColor = emeraldColor,
                                onClick = { handleOpenVideo(course.mentorVideo) }
                            )
                        }
                    }
                }
            }

            // In-dialog video player fallback
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

/**
 * Individual course card displaying organization badge, title, rating, level, duration, and play action.
 */
@Composable
private fun RelatedCourseCard(
    course: UnifiedCourseItem,
    isDark: Boolean,
    surfaceCard: Color,
    cardBorder: Color,
    textPrimary: Color,
    textSecondary: Color,
    primaryColor: Color,
    emeraldColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = surfaceCard),
        border = BorderStroke(1.dp, cardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Thumbnail container with overlays
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .clip(RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp))
                    .background(if (isDark) Color(0xFF262626) else Color(0xFFE2E8F0))
            ) {
                AsyncImage(
                    model = course.thumbnailUrl,
                    contentDescription = course.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Type badge on top left
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color.Black.copy(alpha = 0.72f),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(10.dp)
                ) {
                    Text(
                        text = course.type.uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.5.sp,
                            letterSpacing = 0.6.sp
                        ),
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                // Duration badge on bottom right
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color.Black.copy(alpha = 0.78f),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(10.dp)
                ) {
                    Text(
                        text = course.duration,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 10.sp
                        ),
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            // Course content details
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Provider Row with badge & verified status
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (!course.badgeText.isNullOrBlank()) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(course.badgeBgColor),
                            modifier = Modifier.size(22.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = course.badgeText.take(3),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Black,
                                        fontSize = 9.sp
                                    ),
                                    color = Color(course.badgeTextColor)
                                )
                            }
                        }
                    }

                    Text(
                        text = course.provider,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp
                        ),
                        color = textSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    Icon(
                        Icons.Rounded.Verified,
                        contentDescription = "Verified Provider",
                        tint = primaryColor,
                        modifier = Modifier.size(14.dp)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Course Title
                Text(
                    text = course.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        lineHeight = 22.sp
                    ),
                    color = textPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                if (course.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = course.description,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 12.sp,
                            lineHeight = 17.sp
                        ),
                        color = textSecondary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Rating, Level & Category metadata row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        Icons.Rounded.Star,
                        contentDescription = "Rating",
                        tint = Color(0xFFF59E0B),
                        modifier = Modifier.size(15.dp)
                    )
                    Text(
                        text = "${course.rating}",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        ),
                        color = textPrimary
                    )
                    Text(
                        text = "(${course.reviewCount})",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = textSecondary
                    )

                    Text(text = "•", color = textSecondary, fontSize = 11.sp)

                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = textSecondary.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = course.level,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            color = textSecondary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Text(text = "•", color = textSecondary, fontSize = 11.sp)

                    Text(
                        text = course.category,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = textSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = cardBorder.copy(alpha = 0.6f), thickness = 0.8.dp)
                Spacer(modifier = Modifier.height(12.dp))

                // Footer with price badge and watch button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = course.price.ifBlank { "Free with SkillBuilder" },
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.5.sp
                        ),
                        color = emeraldColor
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(primaryColor.copy(alpha = 0.12f))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            Icons.Rounded.PlayCircle,
                            contentDescription = "Watch",
                            tint = primaryColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Watch Course",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            ),
                            color = primaryColor
                        )
                    }
                }
            }
        }
    }
}
