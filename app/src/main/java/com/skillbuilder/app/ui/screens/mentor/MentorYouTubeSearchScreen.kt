package com.skillbuilder.app.ui.screens.mentor

import android.widget.Toast
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.NorthWest
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material.icons.rounded.VideoLibrary
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.skillbuilder.app.data.local.AppSettings
import com.skillbuilder.app.data.local.AppThemeMode
import com.skillbuilder.app.data.local.ExploreData
import com.skillbuilder.app.data.local.SampleData
import com.skillbuilder.app.domain.model.MentorVideo

@Composable
fun MentorYouTubeSearchScreen(
    initialQuery: String = "",
    onDismiss: () -> Unit,
    onOpenVideo: (MentorVideo) -> Unit
) {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }

    // Adaptive Theme Resolution strictly aligned with app design system (Obsidian Dark & Alabaster Light)
    val themeMode by AppSettings.themeMode.collectAsState()
    val systemDark = isSystemInDarkTheme()
    val isDark = when (themeMode) {
        AppThemeMode.SYSTEM -> systemDark
        AppThemeMode.LIGHT -> false
        AppThemeMode.DARK -> true
        else -> systemDark
    }

    // App Design System Adaptive Colors
    val screenBg = if (isDark) Color(0xFF2B2B2B) else Color(0xFFFFFFFF)
    val cardBg = if (isDark) Color(0xFF383838) else Color(0xFFFFFFFF)
    val inputBg = if (isDark) Color(0xFF353535) else Color(0xFFF1F3F5)
    val chipBg = if (isDark) Color(0xFF353535) else Color(0xFFF1F3F5)
    val chipSelectedBg = if (isDark) Color(0xFF5995E5) else Color(0xFF2464B8)
    val chipSelectedText = Color.White
    val chipUnselectedText = if (isDark) Color(0xFFD4D4D4) else Color(0xFF374151)
    val borderStrokeColor = if (isDark) Color(0xFF4A4A4A) else Color(0xFFE5E7EB)
    val textPrimary = if (isDark) Color(0xFFFFFFFF) else Color(0xFF111827)
    val textSecondary = if (isDark) Color(0xFFB3B3B3) else Color(0xFF6B7280)
    val activeAccent = if (isDark) Color(0xFF5995E5) else Color(0xFF2464B8)
    val dividerColor = if (isDark) Color(0xFF4A4A4A) else Color(0xFFE5E7EB)
    val micBtnBg = if (isDark) Color(0xFF353535) else Color(0xFFF1F3F5)

    var searchQuery by remember { mutableStateOf(initialQuery) }
    var isShowingResults by remember { mutableStateOf(false) }
    var selectedCategoryFilter by remember { mutableStateOf("All") }

    val recentSearches = remember {
        mutableStateListOf(
            "Kotlin & Jetpack Compose",
            "Cloud Architecture & AWS",
            "Python Machine Learning",
            "Full-Stack Web Development",
            "UI/UX Design with Figma",
            "Acoustic Guitar Basics"
        )
    }

    val trendingSearches = remember {
        listOf(
            "Jetpack Compose UI",
            "System Design for Mentors",
            "Next.js 15 & React",
            "Cybersecurity Fundamentals",
            "Business Communication"
        )
    }

    // Comprehensive catalog: Live videos + Explore courses + Degree programs
    val allLiveVideos by SampleData.allVideosFlow.collectAsState()
    val allCourses = remember(allLiveVideos) {
        val list = mutableListOf<MentorVideo>()
        list.addAll(allLiveVideos)
        ExploreData.mobileFocusedCourses.forEach { item ->
            if (list.none { it.id == item.mentorVideo.id }) list.add(item.mentorVideo)
        }
        ExploreData.degreePrograms.forEach { item ->
            if (list.none { it.id == item.mentorVideo.id }) list.add(item.mentorVideo)
        }
        ExploreData.industryCertifications.forEach { item ->
            if (list.none { it.id == item.mentorVideo.id }) list.add(item.mentorVideo)
        }
        list
    }

    // Back handling: If on results page, back goes back to search bar. If on search bar, back dismisses screen.
    BackHandler {
        if (isShowingResults) {
            isShowingResults = false
        } else {
            onDismiss()
        }
    }

    // Function to submit search
    val executeSearch: (String) -> Unit = { queryText ->
        val trimmed = queryText.trim()
        if (trimmed.isNotBlank()) {
            searchQuery = trimmed
            if (trimmed !in recentSearches) {
                recentSearches.add(0, trimmed)
            }
            keyboardController?.hide()
            isShowingResults = true
        }
    }

    // Live search suggestions when typing
    val liveSuggestions = remember(searchQuery, allCourses) {
        val q = searchQuery.trim().lowercase()
        if (q.isBlank()) emptyList()
        else {
            val suggestions = mutableSetOf<String>()
            allCourses.forEach { video ->
                if (video.title.lowercase().contains(q)) suggestions.add(video.title)
                if (video.courseTitle.lowercase().contains(q)) suggestions.add(video.courseTitle)
                if (video.mentorName.lowercase().contains(q)) suggestions.add(video.mentorName)
                if (video.category.lowercase().contains(q)) suggestions.add(video.category)
                video.tags.forEach { tag ->
                    if (tag.lowercase().contains(q)) suggestions.add(tag)
                }
            }
            suggestions.take(8).toList()
        }
    }

    // Filtered results for the dedicated results page
    val searchResults = remember(searchQuery, selectedCategoryFilter, allCourses) {
        val q = searchQuery.trim().lowercase()
        allCourses.filter { video ->
            val matchesQuery = q.isBlank() ||
                video.title.lowercase().contains(q) ||
                video.courseTitle.lowercase().contains(q) ||
                video.mentorName.lowercase().contains(q) ||
                video.category.lowercase().contains(q) ||
                video.description.lowercase().contains(q) ||
                video.tags.any { it.lowercase().contains(q) }

            val matchesCategory = when (selectedCategoryFilter) {
                "All" -> true
                "Free" -> video.price.contains("Free", ignoreCase = true)
                "Paid" -> !video.price.contains("Free", ignoreCase = true)
                else -> video.category.contains(selectedCategoryFilter, ignoreCase = true)
            }
            matchesQuery && matchesCategory
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(screenBg)
            .statusBarsPadding()
    ) {
        if (!isShowingResults) {
            // ==================== MODE 1: SEARCH BAR & SUGGESTIONS PAGE ====================
            Column(modifier = Modifier.fillMaxSize()) {
                // Top Search Bar Header
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
                            tint = textPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    // Search Box Pill (Adaptive Theme)
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shape = RoundedCornerShape(22.dp),
                        color = inputBg,
                        border = BorderStroke(1.dp, borderStrokeColor)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Search,
                                contentDescription = null,
                                tint = textSecondary,
                                modifier = Modifier.size(20.dp)
                            )

                            Spacer(modifier = Modifier.width(10.dp))

                            Box(modifier = Modifier.weight(1f)) {
                                if (searchQuery.isEmpty()) {
                                    Text(
                                        text = "Search courses, topics, mentors...",
                                        style = TextStyle(
                                            color = textSecondary,
                                            fontSize = 14.sp
                                        )
                                    )
                                }
                                BasicTextField(
                                    value = searchQuery,
                                    onValueChange = { searchQuery = it },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .focusRequester(focusRequester),
                                    textStyle = TextStyle(
                                        color = textPrimary,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Normal
                                    ),
                                    singleLine = true,
                                    cursorBrush = SolidColor(activeAccent),
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                    keyboardActions = KeyboardActions(onSearch = { executeSearch(searchQuery) })
                                )
                            }

                            if (searchQuery.isNotEmpty()) {
                                IconButton(
                                    onClick = { searchQuery = "" },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Close,
                                        contentDescription = "Clear",
                                        tint = textSecondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Voice / Mic Pill Button
                    Surface(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .clickable {
                                Toast
                                    .makeText(
                                        context,
                                        "Voice search ready: Try 'Kotlin Course'",
                                        Toast.LENGTH_SHORT
                                    )
                                    .show()
                                searchQuery = "Kotlin"
                            },
                        shape = CircleShape,
                        color = micBtnBg,
                        border = BorderStroke(1.dp, borderStrokeColor)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Rounded.Mic,
                                contentDescription = "Voice Search",
                                tint = textPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                HorizontalDivider(color = dividerColor, thickness = 0.8.dp)

                // Search Suggestions & History List
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    // Live auto-complete suggestions if typing
                    if (searchQuery.isNotBlank() && liveSuggestions.isNotEmpty()) {
                        item {
                            Text(
                                text = "MATCHING SUGGESTIONS",
                                color = textSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.8.sp,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                        items(liveSuggestions) { suggestion ->
                            YouTubeSearchSuggestionRow(
                                icon = Icons.Rounded.Search,
                                text = suggestion,
                                iconTint = textSecondary,
                                textColor = textPrimary,
                                onSelect = { executeSearch(suggestion) },
                                onArrowClick = { searchQuery = suggestion }
                            )
                        }
                    }

                    // Recent Searches
                    if (recentSearches.isNotEmpty()) {
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 16.dp, bottom = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "RECENT SEARCHES",
                                    color = textSecondary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.8.sp
                                )
                                Text(
                                    text = "Clear All",
                                    color = activeAccent,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.clickable { recentSearches.clear() }
                                )
                            }
                        }
                        items(recentSearches) { historyItem ->
                            YouTubeSearchSuggestionRow(
                                icon = Icons.Rounded.History,
                                text = historyItem,
                                iconTint = textSecondary,
                                textColor = textPrimary,
                                onSelect = { executeSearch(historyItem) },
                                onArrowClick = { searchQuery = historyItem }
                            )
                        }
                    }

                    // Trending Topics & Searches
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 20.dp, bottom = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.TrendingUp,
                                contentDescription = null,
                                tint = activeAccent,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "TRENDING ON SKILLBUILDER",
                                color = textSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.8.sp
                            )
                        }
                    }
                    items(trendingSearches) { trendingItem ->
                        YouTubeSearchSuggestionRow(
                            icon = Icons.AutoMirrored.Rounded.TrendingUp,
                            text = trendingItem,
                            iconTint = textSecondary,
                            textColor = textPrimary,
                            onSelect = { executeSearch(trendingItem) },
                            onArrowClick = { searchQuery = trendingItem }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }

            // Auto-focus the search bar when opened
            LaunchedEffect(Unit) {
                try {
                    focusRequester.requestFocus()
                } catch (_: Exception) {}
            }
        } else {
            // ==================== MODE 2: DEDICATED SEARCH RESULTS PAGE ====================
            Column(modifier = Modifier.fillMaxSize()) {
                // Top App Bar with Query Pill
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { isShowingResults = false }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back to search",
                            tint = textPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    // Tappable Query Pill that lets user tap to edit query
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                            .clickable { isShowingResults = false },
                        shape = RoundedCornerShape(21.dp),
                        color = inputBg,
                        border = BorderStroke(1.dp, borderStrokeColor)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Search,
                                contentDescription = null,
                                tint = activeAccent,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = searchQuery,
                                color = textPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = {
                                    searchQuery = ""
                                    isShowingResults = false
                                },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Close,
                                    contentDescription = "Clear",
                                    tint = textSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Edit Search Action
                    Surface(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .clickable {
                                isShowingResults = false
                            },
                        shape = CircleShape,
                        color = micBtnBg,
                        border = BorderStroke(1.dp, borderStrokeColor)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Rounded.Search,
                                contentDescription = "Edit Search",
                                tint = textPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                // Adaptive Category Filter Chips
                val filterCategories = listOf("All", "Free", "Tech & Coding", "AI & ML", "Design", "Languages", "Business", "Paid")
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filterCategories) { category ->
                        val isSelected = selectedCategoryFilter.equals(category, ignoreCase = true)
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) chipSelectedBg else chipBg,
                            border = BorderStroke(1.dp, if (isSelected) chipSelectedBg else borderStrokeColor),
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { selectedCategoryFilter = category }
                        ) {
                            Text(
                                text = category,
                                color = if (isSelected) chipSelectedText else chipUnselectedText,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                // Results Count Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "FOUND ${searchResults.size} COURSES FOR \"$searchQuery\"",
                        color = textSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }

                HorizontalDivider(color = dividerColor, thickness = 0.8.dp)

                // Results Vertical List
                if (searchResults.isEmpty()) {
                    // Empty State
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = inputBg,
                            modifier = Modifier.size(72.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Rounded.VideoLibrary,
                                    contentDescription = null,
                                    tint = textSecondary,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No courses match \"$searchQuery\"",
                            color = textPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Try different keywords like Kotlin, S3, Cloud, or Explore below",
                            color = textSecondary,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("Kotlin", "Cloud", "Python", "Design").forEach { topic ->
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = inputBg,
                                    border = BorderStroke(1.dp, borderStrokeColor),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(20.dp))
                                        .clickable { executeSearch(topic) }
                                ) {
                                    Text(
                                        text = topic,
                                        color = activeAccent,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 32.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        items(searchResults, key = { it.id }) { video ->
                            YouTubeCourseResultCard(
                                video = video,
                                isDark = isDark,
                                cardBg = cardBg,
                                borderStrokeColor = borderStrokeColor,
                                textPrimary = textPrimary,
                                textSecondary = textSecondary,
                                activeAccent = activeAccent,
                                onClick = { onOpenVideo(video) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun YouTubeSearchSuggestionRow(
    icon: ImageVector,
    text: String,
    iconTint: Color,
    textColor: Color,
    onSelect: () -> Unit,
    onArrowClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = text,
            color = textColor,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        IconButton(
            onClick = onArrowClick,
            modifier = Modifier.size(28.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.NorthWest,
                contentDescription = "Insert query",
                tint = iconTint.copy(alpha = 0.7f),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun YouTubeCourseResultCard(
    video: MentorVideo,
    isDark: Boolean,
    cardBg: Color,
    borderStrokeColor: Color,
    textPrimary: Color,
    textSecondary: Color,
    activeAccent: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = BorderStroke(1.dp, borderStrokeColor)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // 16:9 Thumbnail Container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .background(if (isDark) Color(0xFF1F1F1F) else Color(0xFFE2E8F0))
            ) {
                if (!video.thumbnailUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = video.thumbnailUrl,
                        contentDescription = video.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = if (isDark) {
                                        listOf(Color(0xFF1E293B), Color(0xFF0F172A))
                                    } else {
                                        listOf(Color(0xFFDBEAFE), Color(0xFFBFDBFE))
                                    }
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.PlayArrow,
                            contentDescription = null,
                            tint = if (isDark) Color.White.copy(alpha = 0.7f) else activeAccent,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }

                // Gradient Shadow Overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.55f))
                            )
                        )
                )

                // Top-Left Price Badge
                val isFree = video.price.contains("Free", ignoreCase = true)
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = if (isFree) Color(0xFF10B981) else activeAccent,
                    modifier = Modifier
                        .padding(10.dp)
                        .align(Alignment.TopStart)
                ) {
                    Text(
                        text = video.price.uppercase(),
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                // Bottom-Right Duration Badge
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = Color.Black.copy(alpha = 0.85f),
                    modifier = Modifier
                        .padding(10.dp)
                        .align(Alignment.BottomEnd)
                ) {
                    Text(
                        text = video.duration.ifBlank { "12:45" },
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            // Course Info Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.Top
            ) {
                // Mentor Avatar Initial / Icon
                Surface(
                    shape = CircleShape,
                    color = activeAccent.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, activeAccent.copy(alpha = 0.35f)),
                    modifier = Modifier.size(38.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = video.mentorName.firstOrNull()?.uppercase() ?: "M",
                            color = activeAccent,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    // Video Title
                    Text(
                        text = video.title,
                        color = textPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 19.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Mentor Name with Verified Badge
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = video.mentorName,
                            color = textSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Rounded.Verified,
                            contentDescription = "Verified Mentor",
                            tint = activeAccent,
                            modifier = Modifier.size(13.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Metadata Row: Views • Upload date • Rating
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "${video.views} views • ${video.uploadDate}",
                            color = textSecondary.copy(alpha = 0.85f),
                            fontSize = 11.sp
                        )
                        Text(
                            text = "•",
                            color = textSecondary.copy(alpha = 0.6f),
                            fontSize = 11.sp
                        )
                        Icon(
                            imageVector = Icons.Rounded.Star,
                            contentDescription = null,
                            tint = Color(0xFFFBBF24),
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "4.9",
                            color = Color(0xFFFBBF24),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Category Tag Chip
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = if (isDark) Color(0xFF2B2B2B) else Color(0xFFF1F3F5),
                        border = BorderStroke(0.8.dp, borderStrokeColor)
                    ) {
                        Text(
                            text = video.category.uppercase(),
                            color = textSecondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}
