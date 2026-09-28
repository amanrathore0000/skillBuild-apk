package com.skillbuilder.app.ui.screens.dashboard

import androidx.compose.animation.Crossfade
import com.skillbuilder.app.R
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.Image
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.QuestionAnswer
import androidx.compose.material.icons.rounded.HeadsetMic
import androidx.compose.material.icons.rounded.Stars
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material.icons.rounded.VideoCall
import androidx.compose.material.icons.rounded.VideoLibrary
import com.skillbuilder.app.ui.screens.swap.SwapScreen
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.skillbuilder.app.data.local.SampleData
import com.skillbuilder.app.data.local.UserSession
import com.skillbuilder.app.data.local.tr
import com.skillbuilder.app.ui.components.RightSideMenuDrawer
import com.skillbuilder.app.ui.screens.chat.EncryptedChatScreen
import com.skillbuilder.app.ui.screens.home.HomeScreen
import com.skillbuilder.app.domain.model.MentorVideo
import com.skillbuilder.app.ui.screens.learn.BrowseVideosScreen
import com.skillbuilder.app.ui.screens.learn.EnrolledCoursesScreen
import com.skillbuilder.app.ui.screens.learn.VideoDetailPlayerScreen
import com.skillbuilder.app.ui.screens.support.HelpSupportDialog
import com.skillbuilder.app.ui.screens.mentor.MentorVideoUploadScreen
import com.skillbuilder.app.ui.screens.mentor.MentorWalletScreen
import com.skillbuilder.app.ui.screens.mentor.MentorYouTubeSearchScreen
import com.skillbuilder.app.ui.screens.mentor.ServerCoursesScreen
import com.skillbuilder.app.ui.screens.profile.ProfileScreen

import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.ChatBubbleOutline
import androidx.compose.material.icons.rounded.Search
import com.skillbuilder.app.ui.screens.search.SearchScreen

sealed class DashboardTabItem(
    val id: String,
    val enTitle: String,
    val hiTitle: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    // Mentor-specific workspace tabs
    data object ServerCourses : DashboardTabItem("mentor_courses", "Explore", "एक्सप्लोर", Icons.Rounded.Explore)
    data object EnrolledCourses : DashboardTabItem("mentor_enrolled", "Learn", "सीखें", Icons.AutoMirrored.Rounded.MenuBook)
    data object VideoUploads : DashboardTabItem("mentor_uploads", "Uploads", "अपलोड", Icons.Rounded.VideoCall)
    data object MentorSwap : DashboardTabItem("mentor_swap", "Swap", "स्वैप", Icons.Rounded.SwapHoriz)
    data object MentorChat : DashboardTabItem("mentor_chat", "Messages", "संदेश", Icons.Rounded.QuestionAnswer)
    data object Wallet : DashboardTabItem("mentor_wallet", "Wallet", "वॉलेट", Icons.Rounded.AccountBalanceWallet)
    data object MentorProfile : DashboardTabItem("mentor_profile", "Profile", "प्रोफ़ाइल", Icons.Rounded.Person)

    // Learner tabs (matching Coursera's 5 bottom items: Explore, Career, Learn, Search, Profile)
    data object LearnerHome : DashboardTabItem("learner_explore", "Explore", "एक्सप्लोर", Icons.Rounded.Explore)
    data object LearnerCareer : DashboardTabItem("learner_career", "Career", "करियर", Icons.Rounded.School)
    data object LearnerCourses : DashboardTabItem("learner_learn", "Learn", "सीखें", Icons.AutoMirrored.Rounded.MenuBook)
    data object LearnerSearch : DashboardTabItem("learner_search", "Search", "खोजें", Icons.Rounded.Search)
    data object LearnerProfile : DashboardTabItem("learner_profile", "Profile", "प्रोफ़ाइल", Icons.Rounded.Person)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainDashboardScreen(
    onLogout: () -> Unit
) {
    val user by UserSession.currentUser.collectAsState()
    val isMentor = user.isMentor

    val mentorTabs = remember {
        listOf(
            DashboardTabItem.ServerCourses,
            DashboardTabItem.EnrolledCourses,
            DashboardTabItem.VideoUploads,
            DashboardTabItem.MentorSwap,
            DashboardTabItem.MentorProfile
        )
    }

    val learnerTabs = remember {
        listOf(
            DashboardTabItem.LearnerHome,
            DashboardTabItem.LearnerCourses,
            DashboardTabItem.LearnerSearch,
            DashboardTabItem.LearnerProfile
        )
    }

    val activeTabs = if (isMentor) mentorTabs else learnerTabs
    var selectedTabId by remember(isMentor) {
        mutableStateOf(if (isMentor) DashboardTabItem.ServerCourses.id else DashboardTabItem.LearnerHome.id)
    }
    var isDrawerOpen by remember { mutableStateOf(false) }
    var isEncryptedChatOpen by remember { mutableStateOf(false) }
    var isHelpSupportOpen by remember { mutableStateOf(false) }
    var helpSupportInitialTab by remember { mutableStateOf(0) }
    var activeEnrolledVideo by remember { mutableStateOf<MentorVideo?>(null) }

    val conversations by SampleData.chatConversationsFlow.collectAsState()
    val unreadChatCount = remember(conversations) { conversations.sumOf { it.unreadCount } }

    var isChatDmOpen by remember { mutableStateOf(false) }
    var isExploreSearchOpen by remember { mutableStateOf(false) }

    LaunchedEffect(selectedTabId) {
        if (selectedTabId != DashboardTabItem.MentorChat.id) {
            isChatDmOpen = false
        }
        isExploreSearchOpen = false
    }

    val hideTopBar = isExploreSearchOpen
    val hideBottomBar = isEncryptedChatOpen || isExploreSearchOpen

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                if (!hideTopBar) {
                    TopAppBar(
                        title = {
                            Row(verticalAlignment = Alignment.CenterVertically) {

                                Text(
                                    text = when (selectedTabId) {
                                        DashboardTabItem.ServerCourses.id -> "Explore"
                                        DashboardTabItem.EnrolledCourses.id -> tr("Learn", "सीखें")
                                        DashboardTabItem.VideoUploads.id -> tr("Upload Studio", "अपलोड स्टूडियो")
                                        DashboardTabItem.MentorSwap.id -> tr("Skill Swap", "स्किल स्वैप")
                                        DashboardTabItem.MentorChat.id -> tr("Messages & Doubts", "संदेश एवं प्रश्न")
                                        DashboardTabItem.Wallet.id -> tr("Mentor Wallet", "मेंटर वॉलेट")
                                        DashboardTabItem.MentorProfile.id -> tr("Mentor Profile", "मेंटर प्रोफ़ाइल")
                                        DashboardTabItem.LearnerHome.id -> "Explore"
                                        DashboardTabItem.LearnerCourses.id -> tr("Learn", "सीखें")
                                        DashboardTabItem.LearnerSearch.id -> tr("Search", "खोजें")
                                        DashboardTabItem.LearnerProfile.id -> tr("Profile", "प्रोफ़ाइल")
                                        else -> tr("Skill Builder", "स्किल बिल्डर")
                                    },
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = if (selectedTabId == DashboardTabItem.LearnerHome.id || selectedTabId == DashboardTabItem.ServerCourses.id) 24.sp else 20.sp
                                    ),
                                    color = MaterialTheme.colorScheme.onBackground
                                )

                                if (isMentor && selectedTabId != DashboardTabItem.ServerCourses.id && selectedTabId != DashboardTabItem.MentorSwap.id) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                Icons.Rounded.Stars,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(12.dp)
                                            )
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text(
                                                text = tr("Mentor", "मेंटर"),
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                                color = MaterialTheme.colorScheme.primary,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        },
                        actions = {
                            val isProfileTab = selectedTabId == DashboardTabItem.MentorProfile.id || selectedTabId == DashboardTabItem.LearnerProfile.id
                            // Chat icon shown at explore page top right corner for mentors and learners
                            val showChatIcon = (isMentor && selectedTabId == DashboardTabItem.ServerCourses.id) ||
                                    (!isMentor && selectedTabId == DashboardTabItem.LearnerHome.id)
                            // Menu bar drawer icon shown ONLY on Profile page
                            val showMenuIcon = isProfileTab

                            if (showChatIcon) {
                                IconButton(onClick = {
                                    isEncryptedChatOpen = true
                                }) {
                                    BadgedBox(
                                        badge = {
                                            if (unreadChatCount > 0) {
                                                Badge(containerColor = MaterialTheme.colorScheme.primary) {
                                                    Text(
                                                        text = if (unreadChatCount > 99) "99+" else "$unreadChatCount",
                                                        fontSize = 9.sp
                                                    )
                                                }
                                            }
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.ChatBubbleOutline,
                                            contentDescription = tr("Encrypted Chat", "एन्क्रिप्टेड चैट"),
                                            tint = MaterialTheme.colorScheme.onBackground,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                            }

                            // 3-Bar Menu Drawer Icon: Shown ONLY on Profile page in both Learner and Mentor sides
                            if (showMenuIcon) {
                                IconButton(onClick = { isDrawerOpen = true }) {
                                    Icon(
                                        imageVector = Icons.Rounded.Menu,
                                        contentDescription = tr("Open Menu", "मेनू खोलें"),
                                        tint = MaterialTheme.colorScheme.onBackground,
                                        modifier = Modifier.size(26.dp)
                                    )
                                }
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.background
                        )
                    )
                }
            },
            bottomBar = {
                if (!hideBottomBar) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 8.dp
                    ) {
                        activeTabs.forEach { tab ->
                            val isSelected = selectedTabId == tab.id
                            NavigationBarItem(
                                selected = isSelected,
                                onClick = { selectedTabId = tab.id },
                                icon = {
                                    if (tab == DashboardTabItem.MentorChat && unreadChatCount > 0) {
                                        BadgedBox(
                                            badge = {
                                                Badge(containerColor = MaterialTheme.colorScheme.primary) {
                                                    Text(
                                                        text = if (unreadChatCount > 99) "99+" else "$unreadChatCount",
                                                        fontSize = 9.sp
                                                    )
                                                }
                                            }
                                        ) {
                                            Icon(
                                                imageVector = tab.icon,
                                                contentDescription = tr(tab.enTitle, tab.hiTitle)
                                            )
                                        }
                                    } else {
                                        Icon(
                                            imageVector = tab.icon,
                                            contentDescription = tr(tab.enTitle, tab.hiTitle)
                                        )
                                    }
                                },
                                label = {
                                    Text(
                                        text = tr(tab.enTitle, tab.hiTitle),
                                        fontSize = if (activeTabs.size > 5) 10.sp else 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        maxLines = 1
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.primary,
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                )
                            )
                        }
                    }
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                Crossfade(targetState = selectedTabId, label = "TabCrossfade") { tabId ->
                    when (tabId) {
                        // Shared & Mentor Interface Views (Explore Section)
                        DashboardTabItem.ServerCourses.id -> HomeScreen(
                            onNavigateToCareer = {},
                            onNavigateToCourse = {},
                            onNavigateToEnrolled = { selectedTabId = DashboardTabItem.EnrolledCourses.id },
                            onOpenVideo = { video -> activeEnrolledVideo = video },
                            onOpenSearch = { isExploreSearchOpen = true }
                        )
                        DashboardTabItem.EnrolledCourses.id -> EnrolledCoursesScreen(
                            onBack = null,
                            onOpenVideo = { video -> activeEnrolledVideo = video }
                        )
                        DashboardTabItem.VideoUploads.id -> MentorVideoUploadScreen(
                            onOpenVideo = { video -> activeEnrolledVideo = video }
                        )
                        DashboardTabItem.MentorSwap.id -> SwapScreen(
                            onOpenVideo = { video -> activeEnrolledVideo = video }
                        )
                        DashboardTabItem.MentorChat.id -> EncryptedChatScreen(
                            isEmbedded = true,
                            onDismiss = {
                                selectedTabId = DashboardTabItem.ServerCourses.id
                            },
                            onActiveConversationChanged = { isOpen ->
                                isChatDmOpen = isOpen
                            }
                        )
                        DashboardTabItem.Wallet.id -> MentorWalletScreen()
                        DashboardTabItem.MentorProfile.id -> ProfileScreen(
                            onLogout = onLogout
                        )

                        // Learner Views
                        DashboardTabItem.LearnerHome.id -> HomeScreen(
                            onNavigateToCareer = { selectedTabId = DashboardTabItem.LearnerCourses.id },
                            onNavigateToCourse = { selectedTabId = DashboardTabItem.LearnerCourses.id },
                            onNavigateToEnrolled = { selectedTabId = DashboardTabItem.LearnerCourses.id },
                            onOpenVideo = { video -> activeEnrolledVideo = video },
                            onOpenSearch = { isExploreSearchOpen = true }
                        )
                        DashboardTabItem.LearnerCourses.id -> BrowseVideosScreen(
                            onOpenVideo = { video -> activeEnrolledVideo = video }
                        )
                        DashboardTabItem.LearnerSearch.id -> SearchScreen(
                            onOpenVideo = { video -> activeEnrolledVideo = video },
                            onNavigateToCareer = { selectedTabId = DashboardTabItem.LearnerCourses.id }
                        )
                        DashboardTabItem.LearnerProfile.id -> ProfileScreen(
                            onLogout = onLogout
                        )

                        else -> HomeScreen(
                            onOpenVideo = { video -> activeEnrolledVideo = video },
                            onOpenSearch = { isExploreSearchOpen = true }
                        )
                    }
                }
            }
        }

        // Full Screen Menu Drawer (accessible from 3-bar menu)
        RightSideMenuDrawer(
            isOpen = isDrawerOpen,
            onClose = { isDrawerOpen = false },
            onLogout = onLogout,
            onOpenChat = {
                isDrawerOpen = false
                isEncryptedChatOpen = true
            },
            onOpenHelpSupport = { tab ->
                isDrawerOpen = false
                helpSupportInitialTab = tab
                isHelpSupportOpen = true
            }
        )

        // Comprehensive Course Selling Help & Support Center
        HelpSupportDialog(
            isOpen = isHelpSupportOpen,
            onClose = { isHelpSupportOpen = false },
            initialTab = helpSupportInitialTab
        )

        // Play Selected Course from Enrolled Courses, Explore & Video Library
        activeEnrolledVideo?.let { video ->
            VideoDetailPlayerScreen(
                video = video,
                onDismiss = { activeEnrolledVideo = null },
                onNavigateToMyCourses = {
                    activeEnrolledVideo = null
                    selectedTabId = if (isMentor) DashboardTabItem.EnrolledCourses.id else DashboardTabItem.LearnerCourses.id
                }
            )
        }

        // Encrypted Chat Full-Screen Modal
        if (isEncryptedChatOpen) {
            EncryptedChatScreen(
                onDismiss = { isEncryptedChatOpen = false }
            )
        }

        // Full Screen YouTube-Style Search Screen in Header
        if (isExploreSearchOpen) {
            MentorYouTubeSearchScreen(
                onDismiss = { isExploreSearchOpen = false },
                onOpenVideo = { video -> activeEnrolledVideo = video }
            )
        }
    }
}
