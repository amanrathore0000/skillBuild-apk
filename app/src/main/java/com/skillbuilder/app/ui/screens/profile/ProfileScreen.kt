package com.skillbuilder.app.ui.screens.profile

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForwardIos
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil3.compose.AsyncImage
import com.skillbuilder.app.data.local.AppSettings
import com.skillbuilder.app.data.local.AppThemeMode
import com.skillbuilder.app.data.local.UserSession
import com.skillbuilder.app.domain.model.MentorVideo
import com.skillbuilder.app.domain.model.User
import com.skillbuilder.app.ui.screens.learn.VideoDetailPlayerScreen
import com.skillbuilder.app.ui.screens.mentor.MentorWalletScreen
import com.skillbuilder.app.util.CircularAvatarPicker

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileScreen(
    onLogout: () -> Unit = {}
) {
    val context = LocalContext.current
    val user by UserSession.currentUser.collectAsState()
    val themeMode by AppSettings.themeMode.collectAsState()

    val systemDark = isSystemInDarkTheme()
    val isDark = when (themeMode) {
        AppThemeMode.SYSTEM -> systemDark
        AppThemeMode.LIGHT -> false
        AppThemeMode.DARK -> true
    }

    // Dynamic Theme Colors for both Light and Dark modes
    val screenBg = if (isDark) Color(0xFF2B2B2B) else Color(0xFFFFFFFF)
    val textPrimary = if (isDark) Color(0xFFFFFFFF) else Color(0xFF111827)
    val textSecondary = if (isDark) Color(0xFFB3B3B3) else Color(0xFF6B7280)
    val headerColor = if (isDark) Color(0xFFB3B3B3) else Color(0xFF6B7280)
    val dividerColor = if (isDark) Color(0xFFB3B3B3).copy(alpha = 0.2f) else Color(0xFFE5E7EB)
    val activeAccent = if (isDark) Color(0xFF5995E5) else Color(0xFF2464B8)
    val cardOrPillBg = if (isDark) Color(0xFF383838) else Color(0xFFF3F4F6)

    var selectedEnrolledVideo by remember { mutableStateOf<MentorVideo?>(null) }
    var isEditingProfile by remember { mutableStateOf(false) }
    var isWalletDetailsOpen by remember { mutableStateOf(false) }
    var isAddSkillDialogOpen by remember { mutableStateOf(false) }

    // Dialog for adding a skill the mentor/learner wants to learn
    if (isAddSkillDialogOpen) {
        var newSkillInput by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { isAddSkillDialogOpen = false },
            title = {
                Text(
                    text = "Add Skill You Want to Learn",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = textPrimary
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Specify a target skill for swap exchange and mentor guidance:",
                        style = MaterialTheme.typography.bodySmall,
                        color = textSecondary
                    )
                    OutlinedTextField(
                        value = newSkillInput,
                        onValueChange = { newSkillInput = it },
                        placeholder = { Text("e.g. Acoustic Guitar, Baking, React...", color = textSecondary) },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = "Suggested:",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = textSecondary
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("Acoustic Guitar", "Artisan Sourdough", "AI & ML", "UI/UX", "Music Theory", "Photography").forEach { tag ->
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = cardOrPillBg,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable { newSkillInput = tag }
                            ) {
                                Text(
                                    text = tag,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = textPrimary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val clean = newSkillInput.trim()
                        if (clean.isNotBlank()) {
                            if (!user.skillsWanted.contains(clean)) {
                                val updated = user.copy(skillsWanted = user.skillsWanted + clean)
                                UserSession.updateProfile(updated)
                                Toast.makeText(context, "Added '$clean' to target skills!", Toast.LENGTH_SHORT).show()
                            }
                            isAddSkillDialogOpen = false
                        }
                    },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = activeAccent, contentColor = Color.White)
                ) {
                    Text("Add Skill", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { isAddSkillDialogOpen = false }) {
                    Text("Cancel", color = textSecondary)
                }
            },
            shape = RoundedCornerShape(16.dp),
            containerColor = if (isDark) Color(0xFF1E1E1E) else Color(0xFFFFFFFF)
        )
    }

    // When Wallet Option is clicked by mentor, open full-screen wallet window
    if (isWalletDetailsOpen) {
        Dialog(
            onDismissRequest = { isWalletDetailsOpen = false },
            properties = DialogProperties(
                usePlatformDefaultWidth = false
            )
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = screenBg
            ) {
                MentorWalletScreen(
                    onBack = { isWalletDetailsOpen = false }
                )
            }
        }
    }

    // When Edit Profile is clicked, open a dedicated full-screen page with top-left back arrow
    if (isEditingProfile) {
        Dialog(
            onDismissRequest = { isEditingProfile = false },
            properties = DialogProperties(
                usePlatformDefaultWidth = false
            )
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = screenBg
            ) {
                EditProfileScreen(
                    currentUser = user,
                    isDark = isDark,
                    onBack = { isEditingProfile = false },
                    onSave = { updated ->
                        UserSession.updateProfile(updated)
                        isEditingProfile = false
                        Toast.makeText(context, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(screenBg),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp)
    ) {
        // ==================== SECTION 1: PROFILE OVERVIEW ====================
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp, bottom = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (!user.avatarUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = user.avatarUrl,
                        contentDescription = user.name,
                        modifier = Modifier
                            .size(88.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(88.dp)
                            .clip(CircleShape)
                            .background(activeAccent.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = user.name.take(2).uppercase(),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = activeAccent
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = user.name,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp
                    ),
                    color = textPrimary
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = user.email,
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                    color = textSecondary
                )

                if (user.bio.isNotBlank()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = user.bio,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        ),
                        color = textSecondary,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Edit Profile Button (clean reference style button)
                Button(
                    onClick = { isEditingProfile = true },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = cardOrPillBg,
                        contentColor = textPrimary
                    )
                ) {
                    Icon(
                        Icons.Rounded.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = textPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (user.isMentor) "Edit Mentor Profile" else "Edit Profile Details",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = textPrimary
                    )
                }
            }
        }

        item {
            HorizontalDivider(color = dividerColor, thickness = 0.8.dp)
        }

        // ==================== SECTION 2: PERSONAL DETAILS ====================
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 18.dp, bottom = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "DETAILS",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        letterSpacing = 1.sp
                    ),
                    color = headerColor
                )
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = cardOrPillBg
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Rounded.Lock,
                            contentDescription = null,
                            tint = textSecondary,
                            modifier = Modifier.size(11.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Private To You",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = textSecondary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        item {
            Text(
                text = "Your phone number, email address, and date of birth are private and strictly hidden from learners and mentors.",
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, lineHeight = 16.sp),
                color = textSecondary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        // Phone Number Row
        item {
            ProfileInfoRow(
                leadingIcon = Icons.Rounded.Phone,
                title = "Phone Number",
                value = user.phone.ifBlank { "Not provided" },
                textPrimary = textPrimary,
                textSecondary = textSecondary,
                iconTint = textSecondary
            )
        }

        // Date of Birth Row
        item {
            ProfileInfoRow(
                leadingIcon = Icons.Rounded.CalendarMonth,
                title = "Date of Birth",
                value = user.dob.ifBlank { "Not provided" },
                textPrimary = textPrimary,
                textSecondary = textSecondary,
                iconTint = textSecondary
            )
        }

        // Location Row
        item {
            ProfileInfoRow(
                leadingIcon = Icons.Rounded.LocationOn,
                title = "Location",
                value = user.location.ifBlank { "Not specified" },
                textPrimary = textPrimary,
                textSecondary = textSecondary,
                iconTint = textSecondary
            )
        }

        item {
            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = dividerColor, thickness = 0.8.dp)
        }

        // ==================== SECTION 3: SKILLS I CAN SHARE ====================
        item {
            Text(
                text = if (user.isMentor) "MENTOR SKILLS I TEACH" else "SKILLS I CAN SHARE",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    letterSpacing = 1.sp
                ),
                color = headerColor,
                modifier = Modifier.padding(top = 18.dp, bottom = 8.dp)
            )

            if (user.skillsTaught.isEmpty()) {
                Text(
                    text = "No skills added yet. Tap 'Edit Profile' to add your specialties.",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
                    color = textSecondary,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            } else {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    user.skillsTaught.forEach { skill ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = cardOrPillBg
                        ) {
                            Text(
                                text = skill,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = textPrimary,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }

        // ==================== SECTION 4: SKILLS I WANT TO LEARN ====================
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 18.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "SKILLS I WANT TO LEARN",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            letterSpacing = 1.sp
                        ),
                        color = headerColor
                    )
                    Text(
                        text = "Target skills for swap matching & learning",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = textSecondary
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = activeAccent.copy(alpha = 0.12f),
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { isAddSkillDialogOpen = true }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = "Add Skill",
                            tint = activeAccent,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "Add Skill",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = activeAccent
                            )
                        )
                    }
                }
            }

            if (user.skillsWanted.isEmpty()) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = cardOrPillBg,
                    border = BorderStroke(1.dp, dividerColor),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No target skills added yet. Add skills you want to learn to get matched with other mentors for skill swapping!",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, lineHeight = 16.sp),
                            color = textSecondary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Quick Suggestions:",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = textPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf("Artisan Sourdough", "Acoustic Guitar", "Mobile App Architecture", "Photography", "AI & ML").forEach { suggestion ->
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (isDark) Color(0xFF2A2A2A) else Color.White,
                                    border = BorderStroke(0.8.dp, activeAccent.copy(alpha = 0.5f)),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .clickable {
                                            if (!user.skillsWanted.contains(suggestion)) {
                                                val updated = user.copy(skillsWanted = user.skillsWanted + suggestion)
                                                UserSession.updateProfile(updated)
                                                Toast.makeText(context, "Added '$suggestion'!", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Add,
                                            contentDescription = null,
                                            tint = activeAccent,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(
                                            text = suggestion,
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                                            color = textPrimary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    user.skillsWanted.forEach { skill ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = cardOrPillBg,
                            border = BorderStroke(0.8.dp, dividerColor)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(start = 10.dp, end = 6.dp, top = 4.dp, bottom = 4.dp)
                            ) {
                                Text(
                                    text = skill,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    color = textPrimary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .size(18.dp)
                                        .clip(CircleShape)
                                        .clickable {
                                            val updated = user.copy(skillsWanted = user.skillsWanted - skill)
                                            UserSession.updateProfile(updated)
                                            Toast.makeText(context, "Removed '$skill'", Toast.LENGTH_SHORT).show()
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Close,
                                        contentDescription = "Remove skill",
                                        tint = textSecondary,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Extra "+ Add More" chip
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = activeAccent.copy(alpha = 0.1f),
                        border = BorderStroke(0.8.dp, activeAccent.copy(alpha = 0.3f)),
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { isAddSkillDialogOpen = true }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Add,
                                contentDescription = null,
                                tint = activeAccent,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Add More",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = activeAccent
                            )
                        }
                    }
                }
            }
        }

        // ==================== FOOTER: MENTOR WALLET OPTION ====================
        if (user.isMentor) {
            item {
                HorizontalDivider(color = dividerColor, thickness = 0.8.dp)
            }
            item {
                Column {
                    Text(
                        text = "FINANCIAL OVERVIEW",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp
                        ),
                        color = textSecondary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isWalletDetailsOpen = true },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isDark) Color(0xFF13231B) else Color(0xFFF0FDF4)
                        ),
                        border = BorderStroke(
                            1.2.dp,
                            if (isDark) Color(0xFF10B981).copy(alpha = 0.5f) else Color(0xFF10B981).copy(alpha = 0.35f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFF10B981).copy(alpha = 0.18f),
                                modifier = Modifier.size(46.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Rounded.AccountBalanceWallet,
                                        contentDescription = null,
                                        tint = Color(0xFF10B981),
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Mentor Wallet",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp
                                        ),
                                        color = textPrimary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = Color(0xFF10B981)
                                    ) {
                                        Text(
                                            text = "EARNINGS",
                                            color = Color.White,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.ExtraBold
                                            ),
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = "View balance, course sales, royalties & instant payouts",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                    color = textSecondary
                                )
                            }

                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowForwardIos,
                                contentDescription = "Open Wallet",
                                tint = textSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }

        // Bottom spacing for smooth scrolling (Account actions/logout removed from profile page)
        item {
            Spacer(modifier = Modifier.height(36.dp))
        }
    }

    selectedEnrolledVideo?.let { video ->
        VideoDetailPlayerScreen(
            video = video,
            onDismiss = { selectedEnrolledVideo = null }
        )
    }
}

@Composable
private fun ProfileInfoRow(
    leadingIcon: ImageVector,
    title: String,
    value: String,
    textPrimary: Color,
    textSecondary: Color,
    iconTint: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = leadingIcon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(22.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                color = textSecondary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Normal
                ),
                color = textPrimary
            )
        }
    }
}

/**
 * Full-screen Edit Profile Page with top-left back navigation arrow,
 * responsive to both Light and Dark themes according to the reference design.
 */
@Composable
fun EditProfileScreen(
    currentUser: User,
    isDark: Boolean,
    onBack: () -> Unit,
    onSave: (User) -> Unit
) {
    var name by remember { mutableStateOf(currentUser.name) }
    var phone by remember { mutableStateOf(currentUser.phone) }
    var dob by remember { mutableStateOf(currentUser.dob) }
    var location by remember { mutableStateOf(currentUser.location) }
    var bio by remember { mutableStateOf(currentUser.bio) }
    var avatarUrl by remember { mutableStateOf(currentUser.avatarUrl ?: "") }
    var skillsTaughtText by remember {
        mutableStateOf(currentUser.skillsTaught.joinToString(", "))
    }
    var skillsWantedText by remember {
        mutableStateOf(currentUser.skillsWanted.joinToString(", "))
    }

    // Dynamic Theme Colors
    val screenBg = if (isDark) Color(0xFF2B2B2B) else Color(0xFFFFFFFF)
    val textPrimary = if (isDark) Color(0xFFFFFFFF) else Color(0xFF111827)
    val textSecondary = if (isDark) Color(0xFFB3B3B3) else Color(0xFF6B7280)
    val headerColor = if (isDark) Color(0xFFB3B3B3) else Color(0xFF6B7280)
    val dividerColor = if (isDark) Color(0xFFB3B3B3).copy(alpha = 0.2f) else Color(0xFFE5E7EB)
    val activeAccent = if (isDark) Color(0xFF5995E5) else Color(0xFF2464B8)
    val inputBg = if (isDark) Color(0xFF383838) else Color(0xFFF9FAFB)

    val scrollState = rememberScrollState()

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = activeAccent,
        unfocusedBorderColor = if (isDark) Color(0xFF555555) else Color(0xFFD1D5DB),
        focusedLabelColor = activeAccent,
        unfocusedLabelColor = textSecondary,
        focusedTextColor = textPrimary,
        unfocusedTextColor = textPrimary,
        cursorColor = activeAccent,
        focusedContainerColor = inputBg,
        unfocusedContainerColor = inputBg
    )

    fun submitSave() {
        val taughtList = skillsTaughtText.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        val wantedList = skillsWantedText.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        val updated = currentUser.copy(
            name = name.trim().ifBlank { currentUser.name },
            phone = phone.trim(),
            dob = dob.trim(),
            location = location.trim(),
            bio = bio.trim(),
            avatarUrl = avatarUrl.trim().ifEmpty { currentUser.avatarUrl },
            skillsTaught = taughtList,
            skillsWanted = wantedList
        )
        onSave(updated)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(screenBg)
    ) {
        // Top App Bar with back navigation arrow at top-left corner and title
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "Back",
                    tint = textPrimary
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            Text(
                text = if (currentUser.isMentor) "Edit Mentor Profile" else "Edit Profile Details",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 19.sp
                ),
                color = textPrimary,
                modifier = Modifier.weight(1f)
            )
        }

        HorizontalDivider(color = dividerColor, thickness = 0.8.dp)

        // Scrollable Form Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .imePadding()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            // Circular Avatar Picker Section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularAvatarPicker(
                        avatarUri = avatarUrl.ifBlank { null },
                        onAvatarSelected = { avatarUrl = it },
                        sizeDp = 92
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Change Profile Picture",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = activeAccent
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "BASIC INFORMATION",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    letterSpacing = 1.sp
                ),
                color = headerColor
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Full Name *") },
                singleLine = true,
                shape = RoundedCornerShape(8.dp),
                colors = textFieldColors,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Phone Number") },
                singleLine = true,
                shape = RoundedCornerShape(8.dp),
                colors = textFieldColors,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = dob,
                onValueChange = { dob = it },
                label = { Text("Date of Birth (DD/MM/YYYY)") },
                singleLine = true,
                shape = RoundedCornerShape(8.dp),
                colors = textFieldColors,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                label = { Text("Location (City, Country)") },
                singleLine = true,
                shape = RoundedCornerShape(8.dp),
                colors = textFieldColors,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "SKILLS & BIO",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    letterSpacing = 1.sp
                ),
                color = headerColor
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = skillsTaughtText,
                onValueChange = { skillsTaughtText = it },
                label = { Text(if (currentUser.isMentor) "Skills I Mentor (comma-separated)" else "Skills I Can Share (comma-separated)") },
                shape = RoundedCornerShape(8.dp),
                colors = textFieldColors,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = skillsWantedText,
                onValueChange = { skillsWantedText = it },
                label = { Text("Skills I Want to Learn (comma-separated)") },
                shape = RoundedCornerShape(8.dp),
                colors = textFieldColors,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = bio,
                onValueChange = { bio = it },
                label = { Text("About Me / Bio") },
                shape = RoundedCornerShape(8.dp),
                colors = textFieldColors,
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Prominent "Save Changes" Button
            Button(
                onClick = { submitSave() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = activeAccent,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = "Save Changes",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    ),
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(36.dp))
        }
    }
}
