package com.skillbuilder.app.ui.screens.support

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.AttachFile
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.HeadsetMic
import androidx.compose.material.icons.rounded.Help
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.Payment
import androidx.compose.material.icons.rounded.ReportProblem
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material.icons.rounded.SmartDisplay
import androidx.compose.material.icons.rounded.SupportAgent
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material.icons.rounded.VideoCall
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.skillbuilder.app.data.local.AppLanguage
import com.skillbuilder.app.data.local.AppSettings
import com.skillbuilder.app.data.local.SampleData
import com.skillbuilder.app.data.local.UserSession
import com.skillbuilder.app.domain.model.SupportTicket
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class SupportCategoryItem(
    val id: String,
    val titleEn: String,
    val titleHi: String,
    val subtitle: String,
    val icon: ImageVector,
    val isMentorOnly: Boolean = false,
    val isLearnerOnly: Boolean = false
)

val ALL_SUPPORT_CATEGORIES = listOf(
    // Learner centric
    SupportCategoryItem(
        id = "PAYMENT_ISSUE",
        titleEn = "Payment & 30-Day Refund",
        titleHi = "भुगतान और 30-दिन रिफंड",
        subtitle = "Charged twice, 30-day money-back guarantee, invoice issue",
        icon = Icons.Rounded.Payment,
        isLearnerOnly = true
    ),
    SupportCategoryItem(
        id = "VIDEO_STREAM_BUG",
        titleEn = "Video Player & Buffering",
        titleHi = "वीडियो प्लेयर और बफरिंग",
        subtitle = "Playback pauses, audio desync, video quality bug",
        icon = Icons.Rounded.SmartDisplay,
        isLearnerOnly = true
    ),
    SupportCategoryItem(
        id = "COURSE_CONTENT_DISPUTE",
        titleEn = "Course Content & Code",
        titleHi = "कोर्स सामग्री और कोड",
        subtitle = "Missing downloadable files, inaccurate or outdated lessons",
        icon = Icons.Rounded.MenuBook,
        isLearnerOnly = true
    ),
    SupportCategoryItem(
        id = "CERTIFICATE_ISSUE",
        titleEn = "Certificate Generation",
        titleHi = "प्रमाणपत्र जारी करना",
        subtitle = "Completed 100% course but certificate not generated",
        icon = Icons.Rounded.Verified,
        isLearnerOnly = true
    ),
    SupportCategoryItem(
        id = "MENTOR_DISPUTE",
        titleEn = "Mentor Conduct & Dispute",
        titleHi = "मेंटर विवाद और आचरण",
        subtitle = "Unresponsive in encrypted chat, inappropriate conduct",
        icon = Icons.Rounded.Security,
        isLearnerOnly = true
    ),

    // Mentor centric
    SupportCategoryItem(
        id = "COURSE_UPLOAD_BUG",
        titleEn = "Upload & Video Transcoding",
        titleHi = "अपलोड और वीडियो प्रोसेसिंग",
        subtitle = "Upload stuck at 99%, transcoding error, thumbnail failed",
        icon = Icons.Rounded.VideoCall,
        isMentorOnly = true
    ),
    SupportCategoryItem(
        id = "MENTOR_PAYOUT",
        titleEn = "Earnings & Bank Payout",
        titleHi = "कमाई और बैंक ट्रांसफर",
        subtitle = "Delayed monthly payout, bank IFSC / PayPal update",
        icon = Icons.Rounded.AccountBalanceWallet,
        isMentorOnly = true
    ),
    SupportCategoryItem(
        id = "STUDENT_DISPUTE",
        titleEn = "Student Dispute & Bad Review Appeal",
        titleHi = "छात्र विवाद और गलत समीक्षा अपील",
        subtitle = "Request review removal for unfair or abusive student rating",
        icon = Icons.Rounded.ReportProblem,
        isMentorOnly = true
    ),
    SupportCategoryItem(
        id = "DMCA_COPYRIGHT",
        titleEn = "Copyright & Piracy (DMCA)",
        titleHi = "कॉपीराइट और पायरेसी",
        subtitle = "Report unauthorized re-upload of your course materials",
        icon = Icons.Rounded.Security,
        isMentorOnly = true
    ),

    // Universal
    SupportCategoryItem(
        id = "ACCOUNT_ACCESS",
        titleEn = "Account & Login Security",
        titleHi = "खाता और सुरक्षा",
        subtitle = "Password reset, login issue, session recovery",
        icon = Icons.Rounded.Lock
    ),
    SupportCategoryItem(
        id = "OTHER",
        titleEn = "General Inquiries",
        titleHi = "सामान्य पूछताछ",
        subtitle = "Platform feature suggestions and partner inquiries",
        icon = Icons.Rounded.HeadsetMic
    )
)

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun HelpSupportDialog(
    isOpen: Boolean,
    onClose: () -> Unit,
    initialTab: Int = 0
) {
    if (!isOpen) return

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val user by UserSession.currentUser.collectAsState()
    val language by AppSettings.language.collectAsState()
    val isHindi = language == AppLanguage.HINDI

    val isMentor = user.isMentor
    val filteredCategories = remember(isMentor) {
        ALL_SUPPORT_CATEGORIES.filter {
            if (isMentor) !it.isLearnerOnly else !it.isMentorOnly
        }
    }

    var selectedTabIndex by remember(initialTab) { mutableIntStateOf(initialTab.coerceIn(0, 1)) }

    // Form state
    var selectedCategory by remember(filteredCategories) { mutableStateOf(filteredCategories.first()) }
    var selectedPriority by remember { mutableStateOf("MEDIUM") }
    var associatedCourse by remember { mutableStateOf("") }
    var orderId by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }
    var isSubmittedSuccess by remember { mutableStateOf(false) }
    var generatedTicketId by remember { mutableStateOf("") }

    // My Tickets Flow
    val myTickets by SampleData.userSupportTicketsFlow.collectAsState()

    Dialog(
        onDismissRequest = { if (!isSubmitting) onClose() },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnClickOutside = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header Bar
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = if (isHindi) "सहायता और शिकायत केंद्र" else "Help & Support Center",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = if (isMentor) {
                                    if (isHindi) "मेंटर समाधान और सुरक्षा सहायता" else "Mentor Solutions & Safety Desk"
                                } else {
                                    if (isHindi) "छात्र सहायता और शिकायत निवारण" else "Student Assistance & Dispute Resolution"
                                },
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onClose) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                contentDescription = "Close",
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    },
                    actions = {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                            modifier = Modifier.padding(end = 12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(Color(0xFF10B981), CircleShape)
                                )
                                Text(
                                    text = "24/7 Live Desk",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

                // Modern Tab Navigation (2 Tabs: Raise Issue, My Tickets)
                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary,
                    indicator = { tabPositions ->
                        if (selectedTabIndex in tabPositions.indices) {
                            TabRowDefaults.SecondaryIndicator(
                                Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                                color = MaterialTheme.colorScheme.primary,
                                height = 3.dp
                            )
                        }
                    }
                ) {
                    Tab(
                        selected = selectedTabIndex == 0,
                        onClick = { selectedTabIndex = 0 },
                        text = {
                            Text(
                                text = if (isHindi) "शिकायत दर्ज करें" else "Raise Issue",
                                fontWeight = if (selectedTabIndex == 0) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 13.sp
                            )
                        },
                        icon = {
                            Icon(Icons.Rounded.ReportProblem, contentDescription = null, modifier = Modifier.size(18.dp))
                        }
                    )
                    Tab(
                        selected = selectedTabIndex == 1,
                        onClick = { selectedTabIndex = 1 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = if (isHindi) "मेरी टिकटें" else "My Tickets",
                                    fontWeight = if (selectedTabIndex == 1) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 13.sp
                                )
                                if (myTickets.isNotEmpty()) {
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(start = 2.dp)
                                    ) {
                                        Text(
                                            text = "${myTickets.size}",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onPrimary,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        },
                        icon = {
                            Icon(Icons.AutoMirrored.Rounded.ReceiptLong, contentDescription = null, modifier = Modifier.size(18.dp))
                        }
                    )
                }

                // Tab Content Body
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f))
                ) {
                    when (selectedTabIndex) {
                        0 -> RaiseIssueTab(
                            user = user,
                            isHindi = isHindi,
                            categories = filteredCategories,
                            selectedCategory = selectedCategory,
                            onSelectCategory = { selectedCategory = it },
                            selectedPriority = selectedPriority,
                            onSelectPriority = { selectedPriority = it },
                            associatedCourse = associatedCourse,
                            onAssociatedCourseChange = { associatedCourse = it },
                            orderId = orderId,
                            onOrderIdChange = { orderId = it },
                            subject = subject,
                            onSubjectChange = { subject = it },
                            description = description,
                            onDescriptionChange = { description = it },
                            isSubmitting = isSubmitting,
                            isSubmittedSuccess = isSubmittedSuccess,
                            generatedTicketId = generatedTicketId,
                            onSubmit = {
                                if (subject.isBlank()) {
                                    Toast.makeText(context, if (isHindi) "कृपया विषय दर्ज करें" else "Please enter a subject", Toast.LENGTH_SHORT).show()
                                    return@RaiseIssueTab
                                }
                                if (description.trim().length < 10) {
                                    Toast.makeText(context, if (isHindi) "कृपया समस्या का विवरण (कम से कम 10 अक्षर) लिखें" else "Please explain in detail (at least 10 characters)", Toast.LENGTH_SHORT).show()
                                    return@RaiseIssueTab
                                }

                                scope.launch {
                                    isSubmitting = true
                                    delay(900)

                                    val tktId = "TKT-${java.util.UUID.randomUUID()}"
                                    val nowFormatted = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.US).format(java.util.Date())

                                    val newTicket = SupportTicket(
                                        id = tktId,
                                        userId = user.id,
                                        userName = user.name,
                                        userEmail = user.email,
                                        userPhone = user.phone,
                                        userRole = if (user.isMentor) "MENTOR" else "LEARNER",
                                        categoryId = selectedCategory.id,
                                        categoryTitle = selectedCategory.titleEn,
                                        subject = subject.trim(),
                                        description = description.trim(),
                                        courseName = associatedCourse.trim().ifBlank { null },
                                        orderId = orderId.trim().ifBlank { null },
                                        priority = selectedPriority,
                                        status = "PENDING",
                                        createdAt = nowFormatted,
                                        adminResponse = null
                                    )

                                    SampleData.submitSupportTicket(newTicket)

                                    generatedTicketId = tktId
                                    isSubmitting = false
                                    isSubmittedSuccess = true

                                    Toast.makeText(context, if (isHindi) "शिकायत एडमिन को भेजी गई! (टिकट: $tktId)" else "Ticket $tktId sent to Admin Portal!", Toast.LENGTH_LONG).show()
                                }
                            },
                            onResetForm = {
                                isSubmittedSuccess = false
                                subject = ""
                                description = ""
                                associatedCourse = ""
                                orderId = ""
                            },
                            onViewMyTickets = {
                                isSubmittedSuccess = false
                                selectedTabIndex = 1
                            }
                        )

                        else -> MyTicketsTab(
                            tickets = myTickets,
                            isHindi = isHindi,
                            onRaiseNew = { selectedTabIndex = 0 }
                        )
                    }
                }
            }
        }
    }
}

// =========================================================================
// TAB 1: RAISE AN ISSUE / COMPLAINT
// =========================================================================

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RaiseIssueTab(
    user: com.skillbuilder.app.domain.model.User,
    isHindi: Boolean,
    categories: List<SupportCategoryItem>,
    selectedCategory: SupportCategoryItem,
    onSelectCategory: (SupportCategoryItem) -> Unit,
    selectedPriority: String,
    onSelectPriority: (String) -> Unit,
    associatedCourse: String,
    onAssociatedCourseChange: (String) -> Unit,
    orderId: String,
    onOrderIdChange: (String) -> Unit,
    subject: String,
    onSubjectChange: (String) -> Unit,
    description: String,
    onDescriptionChange: (String) -> Unit,
    isSubmitting: Boolean,
    isSubmittedSuccess: Boolean,
    generatedTicketId: String,
    onSubmit: () -> Unit,
    onResetForm: () -> Unit,
    onViewMyTickets: () -> Unit
) {
    if (isSubmittedSuccess) {
        // Success View
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                modifier = Modifier.size(72.dp),
                shape = CircleShape,
                color = Color(0xFFECFDF5)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Rounded.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(42.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = if (isHindi) "शिकायत सफलतापूर्वक दर्ज हुई!" else "Ticket Successfully Dispatched!",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(6.dp))

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                modifier = Modifier.padding(vertical = 6.dp)
            ) {
                Text(
                    text = "Ticket ID: $generatedTicketId",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (isHindi)
                    "आपकी शिकायत एडमिन पोर्टल पर भेज दी गई है। सुरक्षा टीम 2-4 घंटे के भीतर कार्रवाई करेगी।"
                else
                    "Your ticket has been delivered to the SkillBuilder Admin & Governance Portal. Our safety officer will review it within 2-4 hours.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(28.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onResetForm,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(text = if (isHindi) "नई समस्या लिखें" else "New Complaint")
                }

                Button(
                    onClick = onViewMyTickets,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(text = if (isHindi) "स्थिति देखें" else "Track Status", fontWeight = FontWeight.Bold)
                }
            }
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // User identity context pill
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Surface(
                            modifier = Modifier.size(36.dp),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Rounded.Email, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            }
                        }
                        Column {
                            Text(text = user.name, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                            Text(text = user.email, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (user.isMentor) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color(0xFFF1F5F9)
                    ) {
                        Text(
                            text = if (user.isMentor) "MENTOR" else "LEARNER",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            fontWeight = FontWeight.Bold,
                            color = if (user.isMentor) MaterialTheme.colorScheme.primary else Color(0xFF475569),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }
        }

        // 1. Issue Category
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = if (isHindi) "1. समस्या की श्रेणी चुनें:" else "1. Select Complaint Category:",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    categories.forEach { cat ->
                        val isSelected = selectedCategory.id == cat.id
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { onSelectCategory(cat) },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = cat.icon,
                                    contentDescription = null,
                                    tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(15.dp)
                                )
                                Text(
                                    text = if (isHindi) cat.titleHi else cat.titleEn,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                Text(
                    text = selectedCategory.subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(top = 2.dp, start = 2.dp)
                )
            }
        }

        // 2. Priority Level Selector
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = if (isHindi) "2. गंभीरता / प्राथमिकता:" else "2. Priority Severity:",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("LOW" to "Low", "MEDIUM" to "Medium", "HIGH" to "High", "CRITICAL" to "Urgent").forEach { (valKey, label) ->
                        val isSelected = selectedPriority == valKey
                        val chipColor = when (valKey) {
                            "CRITICAL" -> Color(0xFF163E75)
                            "HIGH" -> Color(0xFFF97316)
                            "MEDIUM" -> MaterialTheme.colorScheme.primary
                            else -> Color(0xFF64748B)
                        }

                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onSelectPriority(valKey) },
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) chipColor else MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, if (isSelected) chipColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                        ) {
                            Box(modifier = Modifier.padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }

        // 3. Course Name / Transaction ID (Optional)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = associatedCourse,
                    onValueChange = onAssociatedCourseChange,
                    modifier = Modifier.weight(1.2f),
                    label = { Text(if (isHindi) "कोर्स का नाम (वैकल्पिक)" else "Course Name (Optional)") },
                    placeholder = { Text("e.g. Figma Mastery") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )

                OutlinedTextField(
                    value = orderId,
                    onValueChange = onOrderIdChange,
                    modifier = Modifier.weight(1f),
                    label = { Text(if (isHindi) "ऑर्डर ID / TXN" else "Order / Txn ID") },
                    placeholder = { Text("TXN-FIG-8829") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }
        }

        // 4. Subject & Detailed Description
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = subject,
                    onValueChange = onSubjectChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(if (isHindi) "विषय / समस्या का शीर्षक *" else "Subject / Issue Title *") },
                    placeholder = { Text("e.g. Refund request under 30-day guarantee") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = onDescriptionChange,
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 4,
                    maxLines = 6,
                    label = { Text(if (isHindi) "विस्तृत विवरण (कम से कम 10 अक्षर) *" else "Detailed Explanation (Minimum 10 chars) *") },
                    placeholder = { Text("Describe exactly what occurred, timestamps, affected lessons, or transactions...") },
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }
        }

        // Submit Action Button
        item {
            Button(
                onClick = onSubmit,
                enabled = !isSubmitting,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(text = if (isHindi) "एडमिन को भेजा जा रहा है..." else "Dispatching Ticket to Admin...", fontWeight = FontWeight.Bold)
                } else {
                    Icon(imageVector = Icons.Rounded.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isHindi) "शिकायत एडमिन को भेजें" else "Submit Report to Admin Portal",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}

// =========================================================================
// TAB 2: MY TICKETS TRACKING
// =========================================================================

@Composable
private fun MyTicketsTab(
    tickets: List<SupportTicket>,
    isHindi: Boolean,
    onRaiseNew: () -> Unit
) {
    if (tickets.isEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                modifier = Modifier.size(64.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ReceiptLong,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = if (isHindi) "कोई सक्रिय शिकायत नहीं है" else "No Active Tickets Found",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = if (isHindi) "जब आप शिकायत दर्ज करेंगे, तो उसकी स्थिति यहाँ दिखाई देगी।" else "When you report an issue, track the administrator review status and response here.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = onRaiseNew,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text(text = if (isHindi) "नई शिकायत दर्ज करें" else "Raise New Ticket", fontWeight = FontWeight.Bold)
            }
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(tickets, key = { it.id }) { ticket ->
            var isExpanded by remember { mutableStateOf(false) }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .clickable { isExpanded = !isExpanded },
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Header row: ID, Status, Priority
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                            ) {
                                Text(
                                    text = "#${ticket.id}",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Text(
                                text = "• ${ticket.createdAt}",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Status Badge
                        val statusColor = when (ticket.status) {
                            "RESOLVED" -> Color(0xFF10B981)
                            "IN_REVIEW" -> Color(0xFF2464B8)
                            else -> Color(0xFFF59E0B)
                        }
                        val statusBg = when (ticket.status) {
                            "RESOLVED" -> Color(0xFFECFDF5)
                            "IN_REVIEW" -> Color(0xFFEEF5FD)
                            else -> Color(0xFFFFFBEB)
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = statusBg,
                            border = BorderStroke(1.dp, statusColor.copy(alpha = 0.4f))
                        ) {
                            Text(
                                text = when (ticket.status) {
                                    "RESOLVED" -> if (isHindi) "हल किया गया" else "RESOLVED"
                                    "IN_REVIEW" -> if (isHindi) "समीक्षा जारी" else "IN REVIEW"
                                    else -> if (isHindi) "लंबित" else "PENDING"
                                },
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                fontWeight = FontWeight.Bold,
                                color = statusColor,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = ticket.subject,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = ticket.categoryTitle,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )

                    if (isExpanded) {
                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                        Spacer(modifier = Modifier.height(10.dp))

                        if (!ticket.courseName.isNullOrBlank()) {
                            Text(
                                text = "Course: ${ticket.courseName}",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (!ticket.orderId.isNullOrBlank()) {
                            Text(
                                text = "Order ID: ${ticket.orderId}",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = ticket.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        if (!ticket.adminResponse.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFFF0FDF4),
                                border = BorderStroke(1.dp, Color(0xFFBBF7D0)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "Admin Officer Resolution Note:",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = Color(0xFF15803D)
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = ticket.adminResponse,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFF166534)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isExpanded) "Show Less" else "View Details",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Icon(
                            imageVector = if (isExpanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}
