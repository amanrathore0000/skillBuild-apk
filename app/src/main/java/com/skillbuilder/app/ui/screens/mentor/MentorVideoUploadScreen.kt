package com.skillbuilder.app.ui.screens.mentor

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddPhotoAlternate
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Cloud
import androidx.compose.material.icons.rounded.CloudDone
import androidx.compose.material.icons.rounded.CloudUpload
import androidx.compose.material.icons.rounded.Collections
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material.icons.rounded.FolderShared
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.RadioButtonChecked
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material.icons.rounded.ThumbUp
import androidx.compose.material.icons.rounded.VideoCall
import androidx.compose.material.icons.rounded.VideoLibrary
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import coil3.compose.AsyncImage
import com.skillbuilder.app.data.local.SampleData
import com.skillbuilder.app.data.local.StoragePlanManager
import com.skillbuilder.app.data.local.UserSession
import com.skillbuilder.app.domain.model.MentorVideo
import com.skillbuilder.app.domain.model.StoragePlan
import com.skillbuilder.app.ui.components.VideoPlayer
import com.skillbuilder.app.util.AvatarImageHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MentorVideoUploadScreen() {
    val context = LocalContext.current
    var isUploadFormOpen by remember { mutableStateOf(false) }
    var activeVideoPreview by remember { mutableStateOf<MentorVideo?>(null) }

    // Observes live videos synced from central server and mentor's account
    val allVideos by SampleData.allVideosFlow.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var videoPendingDeletion by remember { mutableStateOf<MentorVideo?>(null) }

    activeVideoPreview?.let { video ->
        com.skillbuilder.app.ui.screens.learn.VideoDetailPlayerScreen(
            video = video,
            onDismiss = { activeVideoPreview = null }
        )
    }

    // Confirmation Dialog before deleting video from server
    videoPendingDeletion?.let { video ->
        AlertDialog(
            onDismissRequest = { videoPendingDeletion = null },
            icon = {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.12f),
                    modifier = Modifier.size(52.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.DeleteForever,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            },
            title = {
                Text(
                    text = "Delete Video from Server?",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Are you sure you want to permanently delete \"${video.title}\"?",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "This will remove the video lesson from your account and wipe it from the central cloud server. Learners will no longer be able to discover, view trial, or access this content.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        SampleData.deleteVideoCompletely(video.id)
                        videoPendingDeletion = null
                        Toast.makeText(context, "Video deleted from server successfully", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Rounded.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Delete Permanently")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { videoPendingDeletion = null },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Cancel")
                }
            },
            shape = RoundedCornerShape(18.dp),
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    var showStoragePlanDialog by remember { mutableStateOf(false) }
    var showConnectStorageDialog by remember { mutableStateOf(false) }
    var pendingUploadAfterConnect by remember { mutableStateOf(false) }
    val storageAccount by StoragePlanManager.storageAccount.collectAsState()
    val currentUser by UserSession.currentUser.collectAsState()
    val currentPlan = StoragePlanManager.getCurrentPlan()

    if (showConnectStorageDialog) {
        ConnectStorageDialog(
            userEmail = currentUser.email.ifBlank { "mentor@gmail.com" },
            onDismiss = {
                showConnectStorageDialog = false
                pendingUploadAfterConnect = false
            },
            onConnectDrive = {
                val email = currentUser.email.ifBlank { "mentor@gmail.com" }
                StoragePlanManager.connectGoogleDrive(email)
                showConnectStorageDialog = false
                Toast.makeText(context, "Connected to Google Drive ($email)!", Toast.LENGTH_SHORT).show()
                if (pendingUploadAfterConnect) {
                    pendingUploadAfterConnect = false
                    isUploadFormOpen = true
                }
            },
            onSelectAws = {
                showConnectStorageDialog = false
                showStoragePlanDialog = true
            }
        )
    }

    if (showStoragePlanDialog) {
        StorageSubscriptionDialog(
            onDismiss = {
                showStoragePlanDialog = false
                pendingUploadAfterConnect = false
            },
            onPlanSelected = { selectedPlan ->
                StoragePlanManager.subscribePlan(selectedPlan.id)
                showStoragePlanDialog = false
                Toast.makeText(context, "Storage Plan Updated: ${selectedPlan.name}", Toast.LENGTH_SHORT).show()
                if (pendingUploadAfterConnect) {
                    pendingUploadAfterConnect = false
                    isUploadFormOpen = true
                }
            }
        )
    }

    if (isUploadFormOpen) {
        YouTubeStyleUploadModal(
            onDismiss = { isUploadFormOpen = false },
            onUploadComplete = { newVideo ->
                SampleData.addMentorVideo(newVideo)
                isUploadFormOpen = false
                val providerLabel = if (newVideo.storageProvider == "GOOGLE_DRIVE") "Google Drive (Private)" else "SkillBuilder AWS S3 Cloud"
                Toast.makeText(context, "Video '${newVideo.title}' published via $providerLabel successfully!", Toast.LENGTH_LONG).show()
            }
        )
    }

    val mentorQuery = searchQuery.trim().lowercase()
    val filteredVideos = remember(allVideos, mentorQuery) {
        if (mentorQuery.isBlank()) allVideos
        else allVideos.filter {
            it.title.lowercase().contains(mentorQuery) ||
            it.courseTitle.lowercase().contains(mentorQuery) ||
            it.mentorName.lowercase().contains(mentorQuery) ||
            it.category.lowercase().contains(mentorQuery) ||
            it.description.lowercase().contains(mentorQuery) ||
            it.tags.any { tag -> tag.lowercase().contains(mentorQuery) }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "CREATOR STUDIO",
                        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Video Upload & Manager",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                Button(
                    onClick = {
                        if (!StoragePlanManager.isStorageConnected()) {
                            pendingUploadAfterConnect = true
                            showConnectStorageDialog = true
                        } else {
                            isUploadFormOpen = true
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Rounded.VideoCall, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Upload Video", fontWeight = FontWeight.Bold)
                }
            }
        }

        // Storage Tier & Delivery Infrastructure Status Card
        item {
            if (!storageAccount.isConnected) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                    modifier = Modifier.size(42.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Rounded.Storage,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Storage Not Connected",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Connect Google Drive or AWS to upload videos",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Button(
                                onClick = {
                                    pendingUploadAfterConnect = false
                                    showConnectStorageDialog = true
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Text("Connect", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                            }
                        }
                    }
                }
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (storageAccount.isAwsConnected) {
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                        } else {
                            Color(0xFF0F9D58).copy(alpha = 0.08f)
                        }
                    ),
                    border = BorderStroke(
                        1.dp,
                        if (storageAccount.isAwsConnected) MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
                        else Color(0xFF0F9D58).copy(alpha = 0.35f)
                    )
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = if (storageAccount.isAwsConnected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color(0xFF0F9D58).copy(alpha = 0.15f),
                                    modifier = Modifier.size(38.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = if (storageAccount.isAwsConnected) Icons.Rounded.CloudUpload else Icons.Rounded.FolderShared,
                                            contentDescription = null,
                                            tint = if (storageAccount.isAwsConnected) MaterialTheme.colorScheme.primary else Color(0xFF0F9D58),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = if (storageAccount.isAwsConnected) "SkillBuilder AWS S3 Cloud" else "Personal Google Drive",
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = if (storageAccount.isAwsConnected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color(0xFF0F9D58).copy(alpha = 0.15f)
                                        ) {
                                            Text(
                                                text = "Connected ✓",
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                                                color = if (storageAccount.isAwsConnected) MaterialTheme.colorScheme.primary else Color(0xFF0F9D58),
                                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = if (storageAccount.isAwsConnected) "${currentPlan.name} • ${currentPlan.price}" else "${storageAccount.googleDriveEmail ?: currentUser.email} • Free (₹0)",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                OutlinedButton(
                                    onClick = {
                                        pendingUploadAfterConnect = false
                                        showStoragePlanDialog = true
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = if (storageAccount.isAwsConnected) "Manage" else "Upgrade",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                                    )
                                }
                                Spacer(modifier = Modifier.width(4.dp))
                                IconButton(
                                    onClick = {
                                        StoragePlanManager.disconnectStorage()
                                        Toast.makeText(context, "Storage disconnected", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        Icons.Rounded.Close,
                                        contentDescription = "Disconnect Storage",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        val usedRatio = (storageAccount.usedBytes.toFloat() / storageAccount.totalBytes.toFloat()).coerceIn(0f, 1f)
                        LinearProgressIndicator(
                            progress = { usedRatio },
                            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                            color = if (storageAccount.isAwsConnected) MaterialTheme.colorScheme.primary else Color(0xFF0F9D58),
                            trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Used: ${StoragePlanManager.formatBytes(storageAccount.usedBytes)} of ${StoragePlanManager.formatBytes(storageAccount.totalBytes)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = if (storageAccount.isAwsConnected) "⚡ CloudFront CDN Active" else "🔒 Private Drive Link Active",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = if (storageAccount.isAwsConnected) MaterialTheme.colorScheme.primary else Color(0xFF0F9D58)
                            )
                        }
                    }
                }
            }
        }

        // Stats summary banner
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${allVideos.size}",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text("Uploaded Videos", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Box(modifier = Modifier.height(28.dp).width(1.dp).background(MaterialTheme.colorScheme.outline))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        val totalViews = allVideos.sumOf { it.views }
                        Text(
                            text = if (totalViews > 1000) "${totalViews / 1000}k" else "$totalViews",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text("Total Views", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Box(modifier = Modifier.height(28.dp).width(1.dp).background(MaterialTheme.colorScheme.outline))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        val totalLikes = allVideos.sumOf { it.likes }
                        Text(
                            text = "$totalLikes",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF4CAF50)
                        )
                        Text("Total Likes", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        // Section Title & Search
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "YOUR UPLOADED CONTENT",
                        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${filteredVideos.size} of ${allVideos.size} Videos",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                if (allVideos.isNotEmpty() || searchQuery.isNotBlank()) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Search your videos by title, course, or tag...") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Rounded.Search,
                                contentDescription = "Search",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotBlank()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(
                                        imageVector = Icons.Rounded.Close,
                                        contentDescription = "Clear",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                        )
                    )
                }
            }
        }

        if (filteredVideos.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Rounded.VideoLibrary,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (searchQuery.isNotBlank()) "No videos match \"$searchQuery\"" else "No videos uploaded yet",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = if (searchQuery.isNotBlank()) "Try another keyword or clear the search bar" else "Click 'Upload Video' above to publish to the server.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                        if (searchQuery.isNotBlank()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedButton(
                                onClick = { searchQuery = "" },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Clear Search")
                            }
                        }
                    }
                }
            }
        }

        // Videos List (YouTube Style Card)
        items(filteredVideos, key = { it.id }) { video ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .clickable { activeVideoPreview = video },
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column {
                    // 16:9 Thumbnail Box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16f / 9f)
                            .background(Color.Black),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!video.thumbnailUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = video.thumbnailUrl,
                                contentDescription = video.title,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }

                        // Play overlay button
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.9f),
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Rounded.PlayArrow,
                                    contentDescription = "Play Video",
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }

                        // Duration Badge bottom right
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color.Black.copy(alpha = 0.8f),
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(8.dp)
                        ) {
                            Text(
                                text = video.duration,
                                color = Color.White,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        // Storage Provider Badge bottom left
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = if (video.storageProvider == "GOOGLE_DRIVE") Color(0xFF0F9D58).copy(alpha = 0.9f) else Color(0xFF1976D2).copy(alpha = 0.9f),
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (video.storageProvider == "GOOGLE_DRIVE") Icons.Rounded.FolderShared else Icons.Rounded.CloudDone,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(11.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = if (video.storageProvider == "GOOGLE_DRIVE") "Google Drive (Free)" else "AWS S3 Cloud",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                                    color = Color.White
                                )
                            }
                        }

                        // Visibility Badge top left
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (video.visibility == "Public") Icons.Rounded.Public else Icons.Rounded.Lock,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(11.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = video.visibility,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        // Quick Delete Action top right
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.error.copy(alpha = 0.85f),
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp)
                                .clip(CircleShape)
                                .clickable { videoPendingDeletion = video }
                        ) {
                            Box(
                                modifier = Modifier.size(30.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Delete,
                                    contentDescription = "Delete from Server",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    // Video Meta Information
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = video.title,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "${video.courseTitle} • ${video.category}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                            ) {
                                Text(
                                    text = video.price,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "${video.views} views",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "• ${video.uploadDate}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Rounded.ThumbUp,
                                        contentDescription = null,
                                        modifier = Modifier.size(12.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(
                                        text = "${video.likes}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            // Delete button with explicit server warning prompt
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.error.copy(alpha = 0.12f),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.3f)),
                                modifier = Modifier.clip(RoundedCornerShape(8.dp))
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clickable { videoPendingDeletion = video }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Icon(
                                        Icons.Rounded.Delete,
                                        contentDescription = "Delete from Server",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Delete",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.error
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YouTubeStyleUploadModal(
    onDismiss: () -> Unit,
    onUploadComplete: (MentorVideo) -> Unit
) {
    val context = LocalContext.current
    val currentUser by UserSession.currentUser.collectAsState()

    // Video File Picker (MANDATORY)
    var selectedVideoUri by remember { mutableStateOf<Uri?>(null) }
    var selectedVideoName by remember { mutableStateOf<String?>(null) }
    var videoDurationText by remember { mutableStateOf("14:20") }

    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedVideoUri = uri
            selectedVideoName = "course_video_${System.currentTimeMillis()}.mp4"
        }
    }

    // Thumbnail Picker (Camera / Gallery) (MANDATORY)
    var thumbnailUri by remember { mutableStateOf<String?>(null) }
    var showThumbnailSourceDialog by remember { mutableStateOf(false) }

    val cameraThumbnailLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            val savedUri = AvatarImageHelper.saveBitmapToInternalStorage(context, bitmap)
            if (savedUri != null) {
                thumbnailUri = savedUri
            }
        }
    }

    val galleryThumbnailLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val savedUri = AvatarImageHelper.saveContentUriToInternalStorage(context, uri)
            if (savedUri != null) {
                thumbnailUri = savedUri
            }
        }
    }

    // Course Video Metadata (ALL MANDATORY)
    var title by remember { mutableStateOf("") }
    var courseTitle by remember { mutableStateOf("") }
    var priceText by remember { mutableStateOf("499") } // Mentor sets custom price
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Music") }
    var visibility by remember { mutableStateOf("Public") }
    var level by remember { mutableStateOf("Beginner") }
    var tagsText by remember { mutableStateOf("#masterclass, #tutorial, #lesson") }

    val storageAccount by StoragePlanManager.storageAccount.collectAsState()
    val isAwsActive = StoragePlanManager.isAwsPlanActive()
    var selectedStorageProvider by remember {
        mutableStateOf(if (storageAccount.isAwsConnected) "AWS_S3" else "GOOGLE_DRIVE")
    }
    var showPlanPickerInModal by remember { mutableStateOf(false) }

    if (showPlanPickerInModal) {
        StorageSubscriptionDialog(
            onDismiss = { showPlanPickerInModal = false },
            onPlanSelected = { newPlan ->
                StoragePlanManager.subscribePlan(newPlan.id)
                selectedStorageProvider = newPlan.provider
                showPlanPickerInModal = false
                Toast.makeText(context, "Storage Plan Updated: ${newPlan.name}", Toast.LENGTH_SHORT).show()
            }
        )
    }

    var isUploading by remember { mutableStateOf(false) }
    var attemptedSubmit by remember { mutableStateOf(false) }

    val categories = listOf("Music", "Tech & Coding", "Culinary Arts", "Design & Art", "Languages", "Fitness")
    val visibilityOptions = listOf("Public", "Unlisted", "Private")
    val levelOptions = listOf("Beginner", "Intermediate", "Advanced", "All Levels")
    val presetPrices = listOf("Free", "299", "499", "999", "1499", "1999")

    val scrollState = rememberScrollState()

    if (showThumbnailSourceDialog) {
        AlertDialog(
            onDismissRequest = { showThumbnailSourceDialog = false },
            title = { Text("Choose Course Thumbnail Source", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showThumbnailSourceDialog = false
                                cameraThumbnailLauncher.launch(null)
                            },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.CameraAlt, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Capture with Camera", fontWeight = FontWeight.SemiBold)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showThumbnailSourceDialog = false
                                galleryThumbnailLauncher.launch("image/*")
                            },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.Collections, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Pick from Gallery / Files", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showThumbnailSourceDialog = false }) { Text("Cancel") }
            }
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .imePadding(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(20.dp)
            ) {
                // Top Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "COURSE & VIDEO UPLOADER",
                            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Publish Course Video",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Rounded.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Mandatory notice card
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "📌 Note: All course details, video file, thumbnail, and pricing are mandatory for publication.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 1. SELECT VIDEO FILE (MANDATORY)
                Text(
                    text = "1. Course Video File *",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = if (attemptedSubmit && selectedVideoUri == null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { videoPickerLauncher.launch("video/*") },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = when {
                            selectedVideoUri != null -> MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                            attemptedSubmit -> MaterialTheme.colorScheme.error.copy(alpha = 0.08f)
                            else -> MaterialTheme.colorScheme.surface
                        }
                    ),
                    border = BorderStroke(
                        1.5.dp,
                        when {
                            selectedVideoUri != null -> MaterialTheme.colorScheme.primary
                            attemptedSubmit -> MaterialTheme.colorScheme.error
                            else -> MaterialTheme.colorScheme.outline
                        }
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (selectedVideoUri != null) Icons.Rounded.CheckCircle else Icons.Rounded.VideoCall,
                            contentDescription = null,
                            tint = when {
                                selectedVideoUri != null -> MaterialTheme.colorScheme.primary
                                attemptedSubmit -> MaterialTheme.colorScheme.error
                                else -> MaterialTheme.colorScheme.primary
                            },
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = if (selectedVideoUri != null) "Video Selected ✓" else "Choose Video File * (Mandatory)",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (attemptedSubmit && selectedVideoUri == null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = selectedVideoName ?: "Select MP4, MOV, or MKV file from device",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 2. VIDEO STORAGE & DELIVERY INFRASTRUCTURE
                Text(
                    text = "2. Video Storage & Delivery Infrastructure *",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Videos are automatically delivered through your connected storage destination.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))

                val isCurrentAws = selectedStorageProvider == "AWS_S3"
                val activeColor = if (isCurrentAws) MaterialTheme.colorScheme.primary else Color(0xFF0F9D58)
                val currentAwsPlan = StoragePlanManager.getCurrentPlan()
                val driveEmail = storageAccount.googleDriveEmail ?: currentUser.email.ifBlank { "mentor@gmail.com" }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = activeColor.copy(alpha = 0.08f)),
                    border = BorderStroke(1.5.dp, activeColor.copy(alpha = 0.45f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = CircleShape,
                                    color = activeColor.copy(alpha = 0.15f),
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = if (isCurrentAws) Icons.Rounded.CloudUpload else Icons.Rounded.FolderShared,
                                            contentDescription = null,
                                            tint = activeColor,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = if (isCurrentAws) "SkillBuilder AWS S3 Cloud" else "Personal Google Drive",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = if (isCurrentAws) "${currentAwsPlan.name} • ${currentAwsPlan.price}" else "$driveEmail • Free (₹0)",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = activeColor.copy(alpha = 0.18f)
                            ) {
                                Text(
                                    text = "Connected ✓",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = activeColor,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = if (isCurrentAws) {
                                "• Global AWS CloudFront CDN delivery with adaptive bitrate streaming (HLS 1080p/4K).\n• Zero Google Drive view quotas; unlimited concurrent student streams."
                            } else {
                                "• Stored securely in your personal Google Drive ($driveEmail).\n• Kept private: SkillBuilder automatically grants viewer permissions only to enrolled learners or accepted swap partners."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(
                                onClick = {
                                    if (isCurrentAws) {
                                        selectedStorageProvider = "GOOGLE_DRIVE"
                                        StoragePlanManager.connectGoogleDrive(driveEmail)
                                        Toast.makeText(context, "Switched destination to Personal Google Drive", Toast.LENGTH_SHORT).show()
                                    } else {
                                        showPlanPickerInModal = true
                                    }
                                },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (isCurrentAws) "Switch to Google Drive (Free)" else "Switch / Upgrade to AWS Cloud CDN",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = activeColor
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 3. CUSTOM 16:9 THUMBNAIL PICKER (MANDATORY)
                Text(
                    text = "3. Course Thumbnail (16:9) *",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = if (attemptedSubmit && thumbnailUri.isNullOrBlank()) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .border(
                            1.5.dp,
                            when {
                                !thumbnailUri.isNullOrBlank() -> MaterialTheme.colorScheme.primary
                                attemptedSubmit -> MaterialTheme.colorScheme.error
                                else -> MaterialTheme.colorScheme.outline
                            },
                            RoundedCornerShape(12.dp)
                        )
                        .clickable { showThumbnailSourceDialog = true },
                    contentAlignment = Alignment.Center
                ) {
                    if (!thumbnailUri.isNullOrBlank()) {
                        AsyncImage(
                            model = thumbnailUri,
                            contentDescription = "Thumbnail",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        Surface(
                            shape = CircleShape,
                            color = Color.Black.copy(alpha = 0.6f),
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(8.dp)
                        ) {
                            Icon(
                                Icons.Rounded.CameraAlt,
                                contentDescription = "Change Thumbnail",
                                tint = Color.White,
                                modifier = Modifier
                                    .padding(6.dp)
                                    .size(16.dp)
                            )
                        }
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Rounded.AddPhotoAlternate,
                                contentDescription = null,
                                tint = if (attemptedSubmit) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Click to capture or select 16:9 thumbnail *",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                color = if (attemptedSubmit) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 3. COURSE / VIDEO TITLE (MANDATORY)
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("3. Course / Lesson Title *") },
                    placeholder = { Text("e.g., Complete Fingerstyle Acoustic Guitar Masterclass") },
                    isError = attemptedSubmit && title.trim().isBlank(),
                    supportingText = {
                        if (attemptedSubmit && title.trim().isBlank()) {
                            Text("Title is mandatory", color = MaterialTheme.colorScheme.error)
                        }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 4. COURSE SERIES / CURRICULUM NAME (MANDATORY)
                OutlinedTextField(
                    value = courseTitle,
                    onValueChange = { courseTitle = it },
                    label = { Text("4. Course Series / Collection Name *") },
                    placeholder = { Text("e.g., Acoustic Guitar Foundations, French Pastry 101") },
                    isError = attemptedSubmit && courseTitle.trim().isBlank(),
                    supportingText = {
                        if (attemptedSubmit && courseTitle.trim().isBlank()) {
                            Text("Course series name is mandatory", color = MaterialTheme.colorScheme.error)
                        }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 5. SET COURSE PRICE (MANDATORY)
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "5. Set Video / Course Price (₹) *",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (attemptedSubmit && priceText.trim().isBlank()) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    // Preset Price Chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        presetPrices.forEach { preset ->
                            val isSelected = priceText.trim().equals(preset, ignoreCase = true)
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                border = BorderStroke(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline),
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { priceText = preset }
                            ) {
                                Text(
                                    text = if (preset == "Free") "Free" else "₹$preset",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = priceText,
                        onValueChange = { priceText = it },
                        label = { Text("Course Price (enter numeric amount or Free) *") },
                        placeholder = { Text("e.g., 499 or Free") },
                        prefix = { Text("₹ ", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                        isError = attemptedSubmit && priceText.trim().isBlank(),
                        supportingText = {
                            if (attemptedSubmit && priceText.trim().isBlank()) {
                                Text("Please set a price for this course", color = MaterialTheme.colorScheme.error)
                            }
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 6. DESCRIPTION & LESSON NOTES (MANDATORY)
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("6. Description & Curriculum Notes *") },
                    placeholder = { Text("Outline what learners will master, lesson timestamps, prerequisites, and resources...") },
                    isError = attemptedSubmit && (description.trim().isBlank() || description.trim().length < 8),
                    supportingText = {
                        if (attemptedSubmit && (description.trim().isBlank() || description.trim().length < 8)) {
                            Text("Description must be at least 8 characters", color = MaterialTheme.colorScheme.error)
                        }
                    },
                    minLines = 3,
                    maxLines = 5,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // 7. CATEGORY (MANDATORY)
                Text("7. Course Category *", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    categories.take(3).forEach { cat ->
                        val isSelected = category == cat
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            border = BorderStroke(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline),
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { category = cat }
                        ) {
                            Text(
                                text = cat,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal),
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(vertical = 8.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    categories.drop(3).forEach { cat ->
                        val isSelected = category == cat
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            border = BorderStroke(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline),
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { category = cat }
                        ) {
                            Text(
                                text = cat,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal),
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(vertical = 8.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 8. DIFFICULTY LEVEL (MANDATORY)
                Text("8. Difficulty Level *", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    levelOptions.forEach { opt ->
                        val isSelected = level == opt
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surfaceVariant,
                            border = BorderStroke(1.dp, if (isSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outline),
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { level = opt }
                        ) {
                            Text(
                                text = opt,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal),
                                color = if (isSelected) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(vertical = 8.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 9. VISIBILITY
                Text("9. Visibility", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    visibilityOptions.forEach { opt ->
                        val isSelected = visibility == opt
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
                            border = BorderStroke(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { visibility = opt }
                        ) {
                            Text(
                                text = opt,
                                style = MaterialTheme.typography.labelMedium,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier.padding(vertical = 10.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 10. TAGS (MANDATORY)
                OutlinedTextField(
                    value = tagsText,
                    onValueChange = { tagsText = it },
                    label = { Text("10. Course Tags (Comma-separated) *") },
                    placeholder = { Text("#guitar, #chords, #beginner") },
                    isError = attemptedSubmit && tagsText.trim().isBlank(),
                    supportingText = {
                        if (attemptedSubmit && tagsText.trim().isBlank()) {
                            Text("Please provide at least one tag", color = MaterialTheme.colorScheme.error)
                        }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(22.dp))

                if (isUploading) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        LinearProgressIndicator(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Encoding video, generating preview stream & publishing to central server...",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                } else {
                    Button(
                        onClick = {
                            attemptedSubmit = true

                            // 1. Mandatory Video File Validation
                            if (selectedVideoUri == null) {
                                Toast.makeText(context, "⚠️ Please select a course video file", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            // 2. Mandatory Thumbnail Validation
                            if (thumbnailUri.isNullOrBlank()) {
                                Toast.makeText(context, "⚠️ Course thumbnail (16:9) is mandatory", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            // 3. Mandatory Title Validation
                            if (title.trim().isBlank()) {
                                Toast.makeText(context, "⚠️ Course / video title is mandatory", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            // 4. Mandatory Course Series Name Validation
                            if (courseTitle.trim().isBlank()) {
                                Toast.makeText(context, "⚠️ Course series name is mandatory", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            // 5. Mandatory Price Validation
                            if (priceText.trim().isBlank()) {
                                Toast.makeText(context, "⚠️ Please set a course price (enter 0 for Free)", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            // 6. Mandatory Description Validation
                            if (description.trim().isBlank() || description.trim().length < 8) {
                                Toast.makeText(context, "⚠️ Description must be at least 8 characters", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            // 7. Mandatory Category Validation
                            if (category.trim().isBlank()) {
                                Toast.makeText(context, "⚠️ Please select a course category", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            // 8. Mandatory Tags Validation
                            if (tagsText.trim().isBlank()) {
                                Toast.makeText(context, "⚠️ Please enter course tags", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            if (selectedStorageProvider == "AWS_S3" && !StoragePlanManager.isAwsPlanActive()) {
                                Toast.makeText(context, "⚠️ Please select an AWS S3 Storage Plan to host with AWS Cloud", Toast.LENGTH_LONG).show()
                                showPlanPickerInModal = true
                                return@Button
                            }

                            val cleanPrice = priceText.trim()
                            val formattedPrice = when {
                                cleanPrice.equals("Free", ignoreCase = true) || cleanPrice == "0" -> "Free"
                                cleanPrice.startsWith("₹") -> cleanPrice
                                else -> "₹$cleanPrice"
                            }

                            val tagsList = tagsText.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                            val newVideo = MentorVideo(
                                id = "mv_${System.currentTimeMillis()}",
                                title = title.trim(),
                                courseTitle = courseTitle.trim(),
                                duration = videoDurationText,
                                views = 1,
                                likes = 1,
                                videoUrl = selectedVideoUri.toString(),
                                thumbnailUrl = thumbnailUri,
                                uploadDate = "Just now",
                                description = description.trim(),
                                visibility = visibility,
                                category = category.trim(),
                                tags = tagsList,
                                level = level.trim(),
                                price = formattedPrice,
                                mentorName = currentUser.name.ifBlank { "Mentor" },
                                storageProvider = selectedStorageProvider,
                                driveFileId = if (selectedStorageProvider == "GOOGLE_DRIVE") "gdrive_${System.currentTimeMillis()}" else null,
                                driveSharingLink = if (selectedStorageProvider == "GOOGLE_DRIVE") "https://drive.google.com/file/d/gdrive_${System.currentTimeMillis()}/view" else null,
                                storagePlanType = if (selectedStorageProvider == "GOOGLE_DRIVE") "FREE_DRIVE" else StoragePlanManager.getCurrentPlan().id
                            )

                            StoragePlanManager.consumeStorage(524_288_000L) // 500 MB simulated video size
                            onUploadComplete(newVideo)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Rounded.VideoCall, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Publish Course Video", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    }
                }
            }
        }
    }
}

@Composable
fun StorageSubscriptionDialog(
    onDismiss: () -> Unit,
    onPlanSelected: (StoragePlan) -> Unit
) {
    val currentPlan = StoragePlanManager.getCurrentPlan()
    var selectedPlanId by remember { mutableStateOf(currentPlan.id) }
    val plans = StoragePlanManager.availablePlans

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier
            .fillMaxWidth(0.94f)
            .padding(vertical = 16.dp),
        title = {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Rounded.Storage,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Video Storage & Delivery Plans",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Choose between personal Google Drive (Free BYOD) or SkillBuilder AWS Cloud CDN.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                plans.forEach { plan ->
                    val isSelected = plan.id == selectedPlanId
                    val isCurrent = plan.id == currentPlan.id
                    val isDrive = plan.provider == "GOOGLE_DRIVE"
                    val accentColor = if (isDrive) Color(0xFF0F9D58) else MaterialTheme.colorScheme.primary

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { selectedPlanId = plan.id },
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) accentColor.copy(alpha = 0.09f) else MaterialTheme.colorScheme.surface
                        ),
                        border = BorderStroke(
                            if (isSelected) 2.dp else 1.dp,
                            if (isSelected) accentColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (isSelected) Icons.Rounded.RadioButtonChecked else Icons.Rounded.RadioButtonUnchecked,
                                        contentDescription = null,
                                        tint = if (isSelected) accentColor else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = plan.name,
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (plan.isRecommended) {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = Color(0xFFFF9800).copy(alpha = 0.15f)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(Icons.Rounded.Star, contentDescription = null, tint = Color(0xFFFF9800), modifier = Modifier.size(10.dp))
                                                Spacer(modifier = Modifier.width(2.dp))
                                                Text("POPULAR", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold), color = Color(0xFFFF9800))
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(6.dp))
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = accentColor.copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            text = plan.price,
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = accentColor,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Storage Quota: ${plan.storageLimitFormatted}",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            plan.features.forEach { feat ->
                                Row(
                                    modifier = Modifier.padding(vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Rounded.Check,
                                        contentDescription = null,
                                        tint = accentColor,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = feat,
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            if (isCurrent) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                ) {
                                    Text(
                                        text = "✓ Currently Active Plan",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val chosenPlan = StoragePlanManager.getPlan(selectedPlanId)
                    onPlanSelected(chosenPlan)
                },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Select & Confirm Plan", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        shape = RoundedCornerShape(20.dp),
        containerColor = MaterialTheme.colorScheme.surface
    )
}

@Composable
fun ConnectStorageDialog(
    userEmail: String,
    onDismiss: () -> Unit,
    onConnectDrive: () -> Unit,
    onSelectAws: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                modifier = Modifier.size(52.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Rounded.CloudUpload,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        },
        title = {
            Text(
                text = "Connect Video Storage",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Before uploading course lessons, connect where your videos will be stored and streamed from:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                // Option 1: Personal Google Drive (Free BYOD)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { onConnectDrive() },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F9D58).copy(alpha = 0.08f)),
                    border = BorderStroke(1.5.dp, Color(0xFF0F9D58).copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFF0F9D58).copy(alpha = 0.15f),
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            Icons.Rounded.FolderShared,
                                            contentDescription = null,
                                            tint = Color(0xFF0F9D58),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Google Drive Storage",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = userEmail,
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFF0F9D58).copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "Free (₹0)",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = Color(0xFF0F9D58),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "• Uses your free 15 GB Google Drive quota\n• Zero cloud costs for creator\n• Private sharing: automatic permissions for enrolled students only",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = onConnectDrive,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F9D58)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Rounded.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Connect Personal Drive", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Option 2: SkillBuilder AWS S3 Cloud (Paid CDN)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { onSelectAws() },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)),
                    border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            Icons.Rounded.CloudUpload,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "SkillBuilder AWS Cloud",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "High-Performance Streaming",
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "From ₹299/mo",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "• Lightning-fast AWS CloudFront CDN delivery\n• 1080p & 4K adaptive bitrate video streaming\n• Zero Drive quotas; unlimited student concurrent streams",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedButton(
                            onClick = onSelectAws,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth(),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Rounded.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Choose AWS Plan", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        shape = RoundedCornerShape(20.dp),
        containerColor = MaterialTheme.colorScheme.surface
    )
}

