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
import androidx.compose.material.icons.rounded.Stars
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material.icons.rounded.VideoCall
import androidx.compose.material.icons.rounded.VideoLibrary
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
import com.skillbuilder.app.ui.screens.mentor.MentorVideoUploadScreen
import com.skillbuilder.app.ui.screens.mentor.MentorWalletScreen
import com.skillbuilder.app.ui.screens.mentor.ServerCoursesScreen
import com.skillbuilder.app.ui.screens.profile.ProfileScreen
import com.skillbuilder.app.ui.screens.swap.SwapScreen

sealed class DashboardTabItem(
    val id: String,
    val enTitle: String,
    val hiTitle: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    // Mentor-specific workspace tabs
    data object ServerCourses : DashboardTabItem("mentor_courses", "Courses", "कोर्सेज", Icons.Rounded.Home)
    data object EnrolledCourses : DashboardTabItem("mentor_enrolled", "Enrolled", "नामांकित", Icons.Rounded.VideoLibrary)
    data object VideoUploads : DashboardTabItem("mentor_uploads", "Uploads", "अपलोड", Icons.Rounded.VideoCall)
    data object Wallet : DashboardTabItem("mentor_wallet", "Wallet", "वॉलेट", Icons.Rounded.AccountBalanceWallet)
    data object MentorProfile : DashboardTabItem("mentor_profile", "Profile", "प्रोफ़ाइल", Icons.Rounded.Person)

    // Learner tabs
    data object LearnerHome : DashboardTabItem("learner_explore", "Explore", "एक्सप्लोर", Icons.Rounded.Explore)
    data object LearnerCourses : DashboardTabItem("learner_learn", "Learn", "सीखें", Icons.Rounded.School)
    data object LearnerSwap : DashboardTabItem("learner_swap", "Swap", "स्वैप", Icons.Rounded.SwapHoriz)
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
            DashboardTabItem.VideoUploads,
            DashboardTabItem.Wallet,
            DashboardTabItem.MentorProfile
        )
    }

    val learnerTabs = remember {
        listOf(
            DashboardTabItem.LearnerHome,
            DashboardTabItem.LearnerCourses,
            DashboardTabItem.LearnerSwap,
            DashboardTabItem.LearnerProfile
        )
    }

    val activeTabs = if (isMentor) mentorTabs else learnerTabs
    var selectedTabId by remember(isMentor) {
        mutableStateOf(if (isMentor) DashboardTabItem.ServerCourses.id else DashboardTabItem.LearnerHome.id)
    }
    var isDrawerOpen by remember { mutableStateOf(false) }
    var isEncryptedChatOpen by remember { mutableStateOf(false) }
    var activeEnrolledVideo by remember { mutableStateOf<MentorVideo?>(null) }

    val conversations by SampleData.chatConversationsFlow.collectAsState()
    val unreadChatCount = remember(conversations) { conversations.sumOf { it.unreadCount } }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val isSkillBuilderHome = selectedTabId == DashboardTabItem.ServerCourses.id || selectedTabId == DashboardTabItem.LearnerHome.id
                            if (isSkillBuilderHome) {
                                Image(
                                    painter = painterResource(id = R.drawable.app_logo),
                                    contentDescription = "Skill Builder",
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                            }

                            Text(
                                text = when (selectedTabId) {
                                    DashboardTabItem.ServerCourses.id -> tr("Skill Builder", "स्किल बिल्डर")
                                    DashboardTabItem.EnrolledCourses.id -> tr("Enrolled Courses", "नामांकित कोर्सेज")
                                    DashboardTabItem.VideoUploads.id -> tr("Upload Studio", "अपलोड स्टूडियो")
                                    DashboardTabItem.Wallet.id -> tr("Mentor Wallet", "मेंटर वॉलेट")
                                    DashboardTabItem.MentorProfile.id -> tr("Mentor Profile", "मेंटर प्रोफ़ाइल")
                                    DashboardTabItem.LearnerHome.id -> tr("Explore", "एक्सप्लोर")
                                    DashboardTabItem.LearnerCourses.id -> tr("Learn Courses", "सीखें")
                                    DashboardTabItem.LearnerSwap.id -> tr("Skill Swap", "स्किल स्वैप")
                                    DashboardTabItem.LearnerProfile.id -> tr("My Profile", "मेरी प्रोफ़ाइल")
                                    else -> tr("Skill Builder", "स्किल बिल्डर")
                                },
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onBackground
                            )

                            if (isMentor) {
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
                        IconButton(onClick = { isEncryptedChatOpen = true }) {
                            BadgedBox(
                                badge = {
                                    if (unreadChatCount > 0) {
                                        Badge(containerColor = MaterialTheme.colorScheme.primary) {
                                            Text("$unreadChatCount")
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.QuestionAnswer,
                                    contentDescription = tr("Encrypted Chat", "एन्क्रिप्टेड चैट"),
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        IconButton(onClick = { isDrawerOpen = true }) {
                            Icon(
                                Icons.Rounded.Menu,
                                contentDescription = tr("Open Menu", "मेनू खोलें"),
                                tint = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
            },
            bottomBar = {
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
                                Icon(
                                    imageVector = tab.icon,
                                    contentDescription = tr(tab.enTitle, tab.hiTitle)
                                )
                            },
                            label = {
                                Text(
                                    text = tr(tab.enTitle, tab.hiTitle),
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
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
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                Crossfade(targetState = selectedTabId, label = "TabCrossfade") { tabId ->
                    when (tabId) {
                        // Shared & Mentor Interface Views
                        DashboardTabItem.ServerCourses.id -> ServerCoursesScreen()
                        DashboardTabItem.EnrolledCourses.id -> EnrolledCoursesScreen(
                            onBack = {
                                selectedTabId = if (isMentor) DashboardTabItem.ServerCourses.id else DashboardTabItem.LearnerHome.id
                            },
                            onOpenVideo = { video -> activeEnrolledVideo = video }
                        )
                        DashboardTabItem.VideoUploads.id -> MentorVideoUploadScreen()
                        DashboardTabItem.Wallet.id -> MentorWalletScreen()
                        DashboardTabItem.MentorProfile.id -> ProfileScreen(
                            onLogout = onLogout
                        )

                        // Learner Views
                        DashboardTabItem.LearnerHome.id -> HomeScreen(
                            onNavigateToSwap = { selectedTabId = DashboardTabItem.LearnerSwap.id },
                            onNavigateToCourse = { selectedTabId = DashboardTabItem.LearnerCourses.id },
                            onNavigateToEnrolled = { selectedTabId = DashboardTabItem.EnrolledCourses.id },
                            onOpenVideo = { video -> activeEnrolledVideo = video }
                        )
                        DashboardTabItem.LearnerCourses.id -> BrowseVideosScreen(
                            onOpenVideo = { video -> activeEnrolledVideo = video }
                        )
                        DashboardTabItem.LearnerSwap.id -> SwapScreen()
                        DashboardTabItem.LearnerProfile.id -> ProfileScreen(
                            onLogout = onLogout
                        )

                        else -> ServerCoursesScreen()
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
            }
        )

        // Play Selected Course from Enrolled Courses, Explore & Video Library
        activeEnrolledVideo?.let { video ->
            VideoDetailPlayerScreen(
                video = video,
                onDismiss = { activeEnrolledVideo = null },
                onNavigateToMyCourses = {
                    activeEnrolledVideo = null
                    selectedTabId = DashboardTabItem.LearnerCourses.id
                }
            )
        }

        // Encrypted Chat Full-Screen Modal
        if (isEncryptedChatOpen) {
            EncryptedChatScreen(
                onDismiss = { isEncryptedChatOpen = false }
            )
        }
    }
}
