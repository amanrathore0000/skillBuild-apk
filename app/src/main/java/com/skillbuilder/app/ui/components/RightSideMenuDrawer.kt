package com.skillbuilder.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.HelpOutline
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.BugReport
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.Translate
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.skillbuilder.app.data.local.AppLanguage
import com.skillbuilder.app.data.local.AppSettings
import com.skillbuilder.app.data.local.AppThemeMode
import com.skillbuilder.app.data.local.SampleData
import com.skillbuilder.app.data.local.UserSession

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RightSideMenuDrawer(
    isOpen: Boolean,
    onClose: () -> Unit,
    onLogout: () -> Unit,
    onOpenChat: () -> Unit = {},
    onOpenHelpSupport: (initialTab: Int) -> Unit = {}
) {
    val user by UserSession.currentUser.collectAsState()
    val themeMode by AppSettings.themeMode.collectAsState()
    val language by AppSettings.language.collectAsState()
    val isHindi = language == AppLanguage.HINDI
    val myTickets by SampleData.userSupportTicketsFlow.collectAsState()

    val systemDark = isSystemInDarkTheme()
    val isDark = when (themeMode) {
        AppThemeMode.SYSTEM -> systemDark
        AppThemeMode.LIGHT -> false
        AppThemeMode.DARK -> true
    }

    // Dynamic Theme Colors: dark mode uses #2B2B2B, light mode uses #FFFFFF
    val drawerBg = if (isDark) Color(0xFF2B2B2B) else Color(0xFFFFFFFF)
    val textPrimary = if (isDark) Color(0xFFFFFFFF) else Color(0xFF111827)
    val textSecondary = if (isDark) Color(0xFFB3B3B3) else Color(0xFF6B7280)
    val headerColor = if (isDark) Color(0xFFB3B3B3) else Color(0xFF6B7280)
    val dividerColor = if (isDark) Color(0xFFB3B3B3).copy(alpha = 0.2f) else Color(0xFFE5E7EB)
    val iconTint = if (isDark) Color(0xFFB3B3B3) else Color(0xFF6B7280)
    val activeAccent = if (isDark) Color(0xFF5995E5) else Color(0xFF2464B8)
    val logoutBg = if (isDark) Color(0xFF383838) else Color(0xFFF3F4F6)

    AnimatedVisibility(
        visible = isOpen,
        enter = slideInHorizontally(initialOffsetX = { it }) + fadeIn(),
        exit = slideOutHorizontally(targetOffsetX = { it }) + fadeOut()
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = drawerBg
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
            ) {
                // Top App Bar with Back button
                TopAppBar(
                    title = {
                        Text(
                            text = if (isHindi) "मेनू और सेटिंग्स" else "Menu & Settings",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            ),
                            color = textPrimary
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onClose) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                contentDescription = if (isHindi) "वापस जाएं" else "Back",
                                tint = textPrimary
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = drawerBg
                    )
                )

                HorizontalDivider(color = dividerColor, thickness = 0.8.dp)

                // Scrollable Content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    // ==================== SECTION 1: ACCOUNT ====================
                    DrawerSectionHeader(
                        title = if (isHindi) "खाता" else "ACCOUNT",
                        color = headerColor
                    )

                    // Profile User Info Row (Clean borderless reference style)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (!user.avatarUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = user.avatarUrl,
                                contentDescription = user.name,
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
                                    .background(activeAccent.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = user.name.take(2).uppercase(),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = activeAccent,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = user.name,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                ),
                                color = textPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = user.email,
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
                                color = textSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = activeAccent.copy(alpha = 0.12f)
                            ) {
                                Text(
                                    text = if (user.isMentor) {
                                        if (isHindi) "मेंटर खाता" else "Mentor Account"
                                    } else {
                                        if (isHindi) "लर्नर खाता" else "Learner Account"
                                    },
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                    color = activeAccent,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = dividerColor, thickness = 0.8.dp)

                    // ==================== SECTION 2: APPEARANCE (THEME) ====================
                    DrawerSectionHeader(
                        title = if (isHindi) "दिखावट" else "APPEARANCE",
                        color = headerColor
                    )

                    // 1. Dark Mode
                    DrawerOptionRow(
                        title = if (isHindi) "डार्क मोड" else "Dark Mode",
                        leadingIcon = Icons.Rounded.DarkMode,
                        isSelected = themeMode == AppThemeMode.DARK,
                        activeColor = activeAccent,
                        defaultTextColor = textPrimary,
                        defaultIconColor = iconTint,
                        onClick = { AppSettings.setThemeMode(AppThemeMode.DARK) }
                    )

                    // 2. Light Mode
                    DrawerOptionRow(
                        title = if (isHindi) "लाइट मोड" else "Light Mode",
                        leadingIcon = Icons.Rounded.LightMode,
                        isSelected = themeMode == AppThemeMode.LIGHT,
                        activeColor = activeAccent,
                        defaultTextColor = textPrimary,
                        defaultIconColor = iconTint,
                        onClick = { AppSettings.setThemeMode(AppThemeMode.LIGHT) }
                    )

                    // 3. System Default
                    DrawerOptionRow(
                        title = if (isHindi) "सिस्टम डिफ़ॉल्ट" else "Use Device Settings",
                        subtitle = if (isHindi)
                            "डिवाइस डिस्प्ले सेटिंग्स के अनुसार डार्क या लाइट मोड का उपयोग करें"
                        else
                            "Set the Dark Mode to use the Light or Dark selection located in your device Display settings",
                        leadingIcon = Icons.Rounded.PhoneAndroid,
                        isSelected = themeMode == AppThemeMode.SYSTEM,
                        activeColor = activeAccent,
                        defaultTextColor = textPrimary,
                        defaultIconColor = iconTint,
                        subtitleColor = textSecondary,
                        onClick = { AppSettings.setThemeMode(AppThemeMode.SYSTEM) }
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = dividerColor, thickness = 0.8.dp)

                    // ==================== SECTION 3: LANGUAGE ====================
                    DrawerSectionHeader(
                        title = if (isHindi) "भाषा" else "LANGUAGE",
                        color = headerColor
                    )

                    // English
                    DrawerOptionRow(
                        title = "English",
                        subtitle = "English Language",
                        leadingIcon = Icons.Rounded.Language,
                        isSelected = language == AppLanguage.ENGLISH,
                        activeColor = activeAccent,
                        defaultTextColor = textPrimary,
                        defaultIconColor = iconTint,
                        subtitleColor = textSecondary,
                        onClick = { AppSettings.setLanguage(AppLanguage.ENGLISH) }
                    )

                    // Hindi
                    DrawerOptionRow(
                        title = "हिंदी (Hindi)",
                        subtitle = "हिंदी भाषा में उपयोग करें",
                        leadingIcon = Icons.Rounded.Translate,
                        isSelected = language == AppLanguage.HINDI,
                        activeColor = activeAccent,
                        defaultTextColor = textPrimary,
                        defaultIconColor = iconTint,
                        subtitleColor = textSecondary,
                        onClick = { AppSettings.setLanguage(AppLanguage.HINDI) }
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = dividerColor, thickness = 0.8.dp)

                    // ==================== SECTION 4: SUPPORT ====================
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isHindi) "सहायता और शिकायत" else "SUPPORT",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                letterSpacing = 1.sp
                            ),
                            color = headerColor
                        )
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = activeAccent.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = "24/7 Safety Desk",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = activeAccent,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                            )
                        }
                    }

                    // Learner Help Center
                    DrawerNavigationRow(
                        title = if (isHindi) "शिक्षार्थी सहायता केंद्र" else "Learner Help Center",
                        subtitle = if (isHindi) "समस्या, रिफंड व विवाद समाधान" else "Course issues, 30-day refunds & mentor disputes",
                        leadingIcon = Icons.AutoMirrored.Rounded.HelpOutline,
                        textColor = textPrimary,
                        iconColor = iconTint,
                        subtitleColor = textSecondary,
                        onClick = {
                            onClose()
                            onOpenHelpSupport(0)
                        }
                    )

                    // Report an Issue
                    DrawerNavigationRow(
                        title = if (isHindi) "समस्या की रिपोर्ट करें" else "Report an Issue",
                        subtitle = if (isHindi) "कोई बग या शिकायत दर्ज करें" else "Find an issue? Send it to us to make the app better.",
                        leadingIcon = Icons.Rounded.BugReport,
                        textColor = textPrimary,
                        iconColor = iconTint,
                        subtitleColor = textSecondary,
                        onClick = {
                            onClose()
                            onOpenHelpSupport(0)
                        }
                    )

                    // My Tickets
                    DrawerNavigationRow(
                        title = if (isHindi) "मेरी टिकटें" else "My Tickets",
                        subtitle = if (isHindi) "शिकायत की स्थिति जांचें" else "Track complaint review status",
                        leadingIcon = Icons.Rounded.ReceiptLong,
                        textColor = textPrimary,
                        iconColor = iconTint,
                        subtitleColor = textSecondary,
                        badgeCount = myTickets.size,
                        badgeColor = activeAccent,
                        onClick = {
                            onClose()
                            onOpenHelpSupport(1)
                        }
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = dividerColor, thickness = 0.8.dp)

                    // ==================== SECTION 5: ACCOUNT ACTIONS (LOGOUT) ====================
                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            UserSession.clear()
                            onClose()
                            onLogout()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .height(48.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = logoutBg,
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.Logout,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isHindi) "खाते से लॉग आउट करें" else "Log Out of Account",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                ),
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(36.dp))
                }
            }
        }
    }
}

@Composable
private fun DrawerSectionHeader(title: String, color: Color) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelMedium.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            letterSpacing = 1.sp
        ),
        color = color,
        modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 6.dp)
    )
}

@Composable
private fun DrawerOptionRow(
    title: String,
    leadingIcon: ImageVector,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    activeColor: Color,
    defaultTextColor: Color,
    defaultIconColor: Color,
    subtitleColor: Color = defaultIconColor,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = leadingIcon,
            contentDescription = null,
            tint = if (isSelected) activeColor else defaultIconColor,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 15.sp,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                ),
                color = if (isSelected) activeColor else defaultTextColor
            )
            if (!subtitle.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    ),
                    color = subtitleColor
                )
            }
        }

        if (isSelected) {
            Spacer(modifier = Modifier.width(12.dp))
            Icon(
                imageVector = Icons.Rounded.Check,
                contentDescription = "Selected",
                tint = activeColor,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun DrawerNavigationRow(
    title: String,
    leadingIcon: ImageVector,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    textColor: Color,
    iconColor: Color,
    subtitleColor: Color = iconColor,
    badgeCount: Int = 0,
    badgeColor: Color = Color.Transparent,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = leadingIcon,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Normal
                ),
                color = textColor
            )
            if (!subtitle.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    ),
                    color = subtitleColor
                )
            }
        }

        if (badgeCount > 0) {
            Surface(
                shape = CircleShape,
                color = badgeColor
            ) {
                Text(
                    text = "$badgeCount",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Icon(
            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(20.dp)
        )
    }
}
