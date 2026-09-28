package com.skillbuilder.app.ui.components

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.Uri
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.compose.BackHandler
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.AspectRatio
import androidx.compose.material.icons.rounded.CropFree
import androidx.compose.material.icons.rounded.FitScreen
import androidx.compose.material.icons.rounded.FolderShared
import androidx.compose.material.icons.rounded.Forward10
import androidx.compose.material.icons.rounded.Fullscreen
import androidx.compose.material.icons.rounded.FullscreenExit
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Replay
import androidx.compose.material.icons.rounded.Replay10
import androidx.compose.material.icons.rounded.ScreenRotation
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.skillbuilder.app.util.VideoStorageHelper
import kotlinx.coroutines.delay

/**
 * Screen scaling / aspect ratio modes according to device display dimensions
 */
enum class VideoScreenMode(
    val title: String,
    val shortName: String,
    val resizeMode: Int,
    val description: String
) {
    FIT(
        title = "Fit to Screen",
        shortName = "Fit (16:9)",
        resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT,
        description = "Original aspect ratio with letterboxing"
    ),
    FILL_ZOOM(
        title = "Zoom to Fill Device",
        shortName = "Fill Screen",
        resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM,
        description = "Fills entire device screen, no black bars"
    ),
    STRETCH(
        title = "Stretch to Device",
        shortName = "Stretch",
        resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FILL,
        description = "Stretches to 100% of device screen"
    )
}

private fun Context.findActivity(): Activity? {
    var ctx = this
    while (ctx is ContextWrapper) {
        if (ctx is Activity) return ctx
        ctx = ctx.baseContext
    }
    return null
}

@OptIn(UnstableApi::class)
@Composable
fun VideoPlayer(
    videoUrl: String,
    modifier: Modifier = Modifier,
    title: String? = null,
    isTrialMode: Boolean = false,
    trialLimitMillis: Long = 30_000L,
    onTrialExpired: () -> Unit = {},
    onEnrollClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val isGoogleDrive = remember(videoUrl) { VideoStorageHelper.isGoogleDriveUrl(videoUrl) }
    val resolvedUrl = remember(videoUrl) { VideoStorageHelper.resolvePlayableUrl(context, videoUrl) }
    val driveFileId = remember(videoUrl) { VideoStorageHelper.extractGoogleDriveFileId(videoUrl) }
    val googleDriveViewUrl = remember(driveFileId, videoUrl) {
        driveFileId?.let { VideoStorageHelper.getGoogleDriveViewUrl(it) } ?: videoUrl
    }

    val exoPlayer = remember(context, resolvedUrl) {
        val httpDataSourceFactory = DefaultHttpDataSource.Factory()
            .setAllowCrossProtocolRedirects(true)
            .setUserAgent("Mozilla/5.0 (Linux; Android 14; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Mobile Safari/537.36")
            .setConnectTimeoutMs(20_000)
            .setReadTimeoutMs(20_000)

        val mediaSourceFactory = DefaultMediaSourceFactory(context)
            .setDataSourceFactory(DefaultDataSource.Factory(context, httpDataSourceFactory))

        ExoPlayer.Builder(context)
            .setMediaSourceFactory(mediaSourceFactory)
            .build()
    }

    var isPlaying by remember { mutableStateOf(false) }
    var isBuffering by remember { mutableStateOf(false) }
    var playbackError by remember { mutableStateOf<String?>(null) }
    var currentPosition by remember { mutableLongStateOf(0L) }
    var duration by remember { mutableLongStateOf(0L) }
    var showControls by remember { mutableStateOf(true) }
    var playbackSpeed by remember { mutableFloatStateOf(1.0f) }
    var showSpeedMenu by remember { mutableStateOf(false) }
    var isTrialExpired by remember { mutableStateOf(false) }

    // Full screen and device aspect ratio scaling states
    var isFullscreen by remember { mutableStateOf(false) }
    var isLandscape by remember { mutableStateOf(true) }
    var videoScreenMode by remember { mutableStateOf(VideoScreenMode.FIT) }
    var showScaleNotification by remember { mutableStateOf(false) }

    fun cycleScaleMode() {
        videoScreenMode = when (videoScreenMode) {
            VideoScreenMode.FIT -> VideoScreenMode.FILL_ZOOM
            VideoScreenMode.FILL_ZOOM -> VideoScreenMode.STRETCH
            VideoScreenMode.STRETCH -> VideoScreenMode.FIT
        }
        showScaleNotification = true
    }

    LaunchedEffect(showScaleNotification, videoScreenMode) {
        if (showScaleNotification) {
            delay(1800)
            showScaleNotification = false
        }
    }

    LaunchedEffect(isTrialMode) {
        if (!isTrialMode) {
            isTrialExpired = false
        }
    }

    DisposableEffect(exoPlayer) {
        playbackError = null
        isBuffering = true
        try {
            exoPlayer.setMediaItem(MediaItem.fromUri(Uri.parse(resolvedUrl)))
            exoPlayer.prepare()
            exoPlayer.playWhenReady = true
        } catch (e: Exception) {
            playbackError = e.localizedMessage ?: "Failed to open media source"
            isBuffering = false
        }

        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(playing: Boolean) {
                isPlaying = playing
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    Player.STATE_READY -> {
                        duration = exoPlayer.duration.coerceAtLeast(0L)
                        playbackError = null
                        isBuffering = false
                    }
                    Player.STATE_BUFFERING -> {
                        isBuffering = true
                    }
                    Player.STATE_ENDED -> {
                        isBuffering = false
                    }
                    Player.STATE_IDLE -> {
                        isBuffering = false
                    }
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                playbackError = error.message ?: "Video playback error encountered"
                isPlaying = false
                isBuffering = false
            }
        }
        exoPlayer.addListener(listener)

        onDispose {
            exoPlayer.removeListener(listener)
            exoPlayer.release()
        }
    }

    // Auto-update progress position and enforce 30s trial
    LaunchedEffect(isPlaying, isTrialMode) {
        while (isPlaying) {
            val pos = exoPlayer.currentPosition.coerceAtLeast(0L)
            currentPosition = pos
            duration = exoPlayer.duration.coerceAtLeast(0L)
            if (isTrialMode && pos >= trialLimitMillis) {
                exoPlayer.pause()
                isTrialExpired = true
                onTrialExpired()
                break
            }
            delay(400)
        }
    }

    // Auto-hide controls after 4 seconds of inactivity
    LaunchedEffect(showControls, isPlaying) {
        if (showControls && isPlaying) {
            delay(4000)
            showControls = false
        }
    }

    // System orientation & bars handling when fullscreen toggles
    val activity = context.findActivity()
    val originalOrientation = remember { activity?.requestedOrientation ?: ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED }

    DisposableEffect(isFullscreen, isLandscape) {
        if (isFullscreen) {
            activity?.requestedOrientation = if (isLandscape) {
                ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
            } else {
                ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
            }
            val window = activity?.window
            if (window != null) {
                val insetsController = WindowCompat.getInsetsController(window, window.decorView)
                insetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                insetsController.hide(WindowInsetsCompat.Type.systemBars())
            }
            onDispose {
                activity?.requestedOrientation = originalOrientation
                if (window != null) {
                    val insetsController = WindowCompat.getInsetsController(window, window.decorView)
                    insetsController.show(WindowInsetsCompat.Type.systemBars())
                }
            }
        } else {
            onDispose {}
        }
    }

    // Reusable player content composable
    @Composable
    fun VideoPlayerContent(isFull: Boolean) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { showControls = !showControls },
                        onDoubleTap = { cycleScaleMode() }
                    )
                }
        ) {
            AndroidView(
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        player = exoPlayer
                        useController = false
                        resizeMode = videoScreenMode.resizeMode
                        layoutParams = FrameLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                    }
                },
                update = { playerView ->
                    if (playerView.player != exoPlayer) {
                        playerView.player = exoPlayer
                    }
                    playerView.resizeMode = videoScreenMode.resizeMode
                },
                modifier = Modifier.fillMaxSize()
            )

            // Buffering Indicator
            if (isBuffering && playbackError == null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(42.dp),
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 3.dp
                    )
                }
            }

            // Playback Error Overlay
            if (playbackError != null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF1E1E1E).copy(alpha = 0.94f))
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = if (isGoogleDrive) Color(0xFF0F9D58).copy(alpha = 0.18f) else MaterialTheme.colorScheme.error.copy(alpha = 0.18f),
                            border = BorderStroke(1.5.dp, if (isGoogleDrive) Color(0xFF0F9D58) else MaterialTheme.colorScheme.error)
                        ) {
                            Icon(
                                imageVector = if (isGoogleDrive) Icons.Rounded.FolderShared else Icons.Rounded.WarningAmber,
                                contentDescription = null,
                                tint = if (isGoogleDrive) Color(0xFF0F9D58) else MaterialTheme.colorScheme.error,
                                modifier = Modifier
                                    .padding(12.dp)
                                    .size(28.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = if (isGoogleDrive) "Google Drive Video" else "Video Playback Error",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = if (isGoogleDrive) {
                                "Direct video streaming from Google Drive may be restricted by Google account permissions or daily view quotas. You can launch and watch it directly in Google Drive."
                            } else {
                                "The selected video file or source cannot be loaded. Please ensure the file is accessible."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.82f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (isGoogleDrive) {
                                Button(
                                    onClick = {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(googleDriveViewUrl)).apply {
                                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        }
                                        try {
                                            context.startActivity(intent)
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                        }
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F9D58))
                                ) {
                                    Icon(Icons.AutoMirrored.Rounded.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Open in Google Drive", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                            }

                            Button(
                                onClick = {
                                    playbackError = null
                                    isBuffering = true
                                    try {
                                        exoPlayer.setMediaItem(MediaItem.fromUri(Uri.parse(resolvedUrl)))
                                        exoPlayer.prepare()
                                        exoPlayer.play()
                                    } catch (e: Exception) {
                                        playbackError = e.localizedMessage
                                        isBuffering = false
                                    }
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.White.copy(alpha = 0.16f),
                                    contentColor = Color.White
                                )
                            ) {
                                Icon(Icons.Rounded.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Retry", fontSize = 13.sp)
                            }

                            Button(
                                onClick = {
                                    playbackError = null
                                    isBuffering = true
                                    try {
                                        exoPlayer.setMediaItem(MediaItem.fromUri(Uri.parse(VideoStorageHelper.SAMPLE_FALLBACK_VIDEO_URL)))
                                        exoPlayer.prepare()
                                        exoPlayer.play()
                                    } catch (e: Exception) {
                                        playbackError = e.localizedMessage
                                        isBuffering = false
                                    }
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                )
                            ) {
                                Icon(Icons.Rounded.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Play Backup Stream", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }

            // 30-Second Trial Expired Modal Overlay
            if (isTrialExpired && isTrialMode) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF2B2B2B).copy(alpha = 0.90f))
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                            border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Lock,
                                contentDescription = "Trial Ended",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .padding(12.dp)
                                    .size(28.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "30-Second Free Trial Ended",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Enroll now to unlock the full course, all video lessons & lifetime mentor access.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.82f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 20.dp)
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = onEnrollClick,
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Icon(Icons.Rounded.ShoppingCart, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Enroll to Unlock Full Video", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }

                            Button(
                                onClick = {
                                    isTrialExpired = false
                                    exoPlayer.seekTo(0)
                                    exoPlayer.play()
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.White.copy(alpha = 0.16f),
                                    contentColor = Color.White
                                )
                            ) {
                                Icon(Icons.Rounded.Replay, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Replay", fontSize = 13.sp)
                            }
                        }
                    }
                }
            }

            // On-Screen Feedback Pill for Device Scale Mode
            AnimatedVisibility(
                visible = showScaleNotification,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut(),
                modifier = Modifier.align(Alignment.Center)
            ) {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Color(0xFF1E1E1E).copy(alpha = 0.94f),
                    border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.75f)),
                    shadowElevation = 10.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = when (videoScreenMode) {
                                VideoScreenMode.FIT -> Icons.Rounded.FitScreen
                                VideoScreenMode.FILL_ZOOM -> Icons.Rounded.CropFree
                                VideoScreenMode.STRETCH -> Icons.Rounded.AspectRatio
                            },
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = videoScreenMode.title,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = videoScreenMode.description,
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            // Custom Controls Overlay
            AnimatedVisibility(
                visible = showControls && (!isTrialExpired || !isTrialMode) && playbackError == null,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.fillMaxSize()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF141414).copy(alpha = 0.62f))
                        .padding(if (isFull) 16.dp else 10.dp)
                ) {
                    // Header Row: Title & Action Pills
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.TopStart),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f, fill = false)
                        ) {
                            if (isFull) {
                                IconButton(
                                    onClick = { isFullscreen = false },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                        contentDescription = "Exit Fullscreen",
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(4.dp))
                            }

                            if (title != null) {
                                Text(
                                    text = title,
                                    style = if (isFull) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleSmall,
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.padding(horizontal = 4.dp)
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (isGoogleDrive) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFF0F9D58).copy(alpha = 0.85f),
                                    modifier = Modifier
                                        .padding(end = 8.dp)
                                        .clickable {
                                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(googleDriveViewUrl)).apply {
                                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                            }
                                            try {
                                                context.startActivity(intent)
                                            } catch (e: Exception) {
                                                e.printStackTrace()
                                            }
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.FolderShared,
                                            contentDescription = "Open in Drive",
                                            tint = Color.White,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Drive",
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            // Scale Mode Selector Button (Visible in Fullscreen & Normal)
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color.White.copy(alpha = 0.22f),
                                border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.35f)),
                                modifier = Modifier.clickable { cycleScaleMode() }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = when (videoScreenMode) {
                                            VideoScreenMode.FIT -> Icons.Rounded.FitScreen
                                            VideoScreenMode.FILL_ZOOM -> Icons.Rounded.CropFree
                                            VideoScreenMode.STRETCH -> Icons.Rounded.AspectRatio
                                        },
                                        contentDescription = "Screen Fit Mode",
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = videoScreenMode.shortName,
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            if (isFull) {
                                Spacer(modifier = Modifier.width(6.dp))
                                // Screen Orientation Rotation Button
                                IconButton(
                                    onClick = {
                                        isLandscape = !isLandscape
                                        activity?.requestedOrientation = if (isLandscape) {
                                            ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                                        } else {
                                            ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
                                        }
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.ScreenRotation,
                                        contentDescription = "Rotate Screen",
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            if (isGoogleDrive) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFF0F9D58).copy(alpha = 0.85f),
                                    modifier = Modifier.clickable {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(googleDriveViewUrl)).apply {
                                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        }
                                        try {
                                            context.startActivity(intent)
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                        }
                                    }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.AutoMirrored.Rounded.OpenInNew, contentDescription = null, tint = Color.White, modifier = Modifier.size(11.dp))
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text("Drive", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = Color.White, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            if (isTrialMode) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFFF59E0B).copy(alpha = 0.95f)
                                ) {
                                    Text(
                                        text = "30s FREE TRIAL",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                        color = Color(0xFF2B2B2B),
                                        fontWeight = FontWeight.ExtraBold,
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Center playback controls: -10s, Play/Pause, +10s
                    Row(
                        modifier = Modifier.align(Alignment.Center),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(if (isFull) 36.dp else 24.dp)
                    ) {
                        IconButton(
                            onClick = {
                                exoPlayer.seekTo((exoPlayer.currentPosition - 10000).coerceAtLeast(0))
                            },
                            modifier = Modifier.size(if (isFull) 48.dp else 40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Replay10,
                                contentDescription = "Rewind 10s",
                                tint = Color.White,
                                modifier = Modifier.size(if (isFull) 42.dp else 36.dp)
                            )
                        }

                        IconButton(
                            onClick = {
                                if (exoPlayer.isPlaying) {
                                    exoPlayer.pause()
                                } else {
                                    if (isTrialMode && exoPlayer.currentPosition >= trialLimitMillis) {
                                        exoPlayer.seekTo(0)
                                    }
                                    exoPlayer.play()
                                }
                            },
                            modifier = Modifier
                                .size(if (isFull) 64.dp else 52.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                                contentDescription = if (isPlaying) "Pause" else "Play",
                                tint = Color.White,
                                modifier = Modifier.size(if (isFull) 38.dp else 30.dp)
                            )
                        }

                        IconButton(
                            onClick = {
                                val limit = if (isTrialMode) trialLimitMillis else duration
                                exoPlayer.seekTo((exoPlayer.currentPosition + 10000).coerceAtMost(limit))
                            },
                            modifier = Modifier.size(if (isFull) 48.dp else 40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Forward10,
                                contentDescription = "Forward 10s",
                                tint = Color.White,
                                modifier = Modifier.size(if (isFull) 42.dp else 36.dp)
                            )
                        }
                    }

                    // Bottom progress bar, speed selector, timecodes & fullscreen toggle
                    val effectiveMax = if (isTrialMode) trialLimitMillis else duration
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = formatTime(currentPosition),
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // Speed Selector Dropdown
                                Box {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color.White.copy(alpha = 0.2f))
                                            .clickable { showSpeedMenu = true }
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Icon(
                                            Icons.Rounded.Speed,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "${playbackSpeed}x",
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }

                                    DropdownMenu(
                                        expanded = showSpeedMenu,
                                        onDismissRequest = { showSpeedMenu = false }
                                    ) {
                                        listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f).forEach { speed ->
                                            DropdownMenuItem(
                                                text = { Text("${speed}x") },
                                                onClick = {
                                                    playbackSpeed = speed
                                                    exoPlayer.playbackParameters = PlaybackParameters(speed)
                                                    showSpeedMenu = false
                                                }
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Text(
                                    text = if (isTrialMode) "00:30 (Trial)" else formatTime(duration),
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontSize = 12.sp
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                // Fullscreen Toggle Button
                                IconButton(
                                    onClick = { isFullscreen = !isFullscreen },
                                    modifier = Modifier.size(30.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isFull) Icons.Rounded.FullscreenExit else Icons.Rounded.Fullscreen,
                                        contentDescription = if (isFull) "Exit Full Screen" else "Enter Full Screen",
                                        tint = Color.White,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                        }

                        Slider(
                            value = if (effectiveMax > 0) (currentPosition.toFloat() / effectiveMax.toFloat()).coerceIn(0f, 1f) else 0f,
                            onValueChange = { frac ->
                                val newPos = (frac * effectiveMax).toLong()
                                currentPosition = newPos
                                exoPlayer.seekTo(newPos)
                                if (isTrialMode && newPos < trialLimitMillis) {
                                    isTrialExpired = false
                                }
                            },
                            colors = SliderDefaults.colors(
                                thumbColor = MaterialTheme.colorScheme.primary,
                                activeTrackColor = MaterialTheme.colorScheme.primary,
                                inactiveTrackColor = Color.White.copy(alpha = 0.3f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }

    // Normal Inline Player View
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f)
            .clip(RoundedCornerShape(12.dp)),
        color = Color(0xFF2B2B2B)
    ) {
        if (!isFullscreen) {
            VideoPlayerContent(isFull = false)
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Playing in Full Screen",
                    color = Color.White.copy(alpha = 0.6f),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }

    // Full Screen Dialog View (Takes 100% of display with zero bounds)
    if (isFullscreen) {
        Dialog(
            onDismissRequest = { isFullscreen = false },
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                decorFitsSystemWindows = false,
                dismissOnBackPress = true,
                dismissOnClickOutside = false
            )
        ) {
            val dialogView = LocalView.current
            DisposableEffect(dialogView) {
                val dialogWindow = (dialogView.parent as? DialogWindowProvider)?.window
                if (dialogWindow != null) {
                    WindowCompat.setDecorFitsSystemWindows(dialogWindow, false)
                    val insets = WindowCompat.getInsetsController(dialogWindow, dialogView)
                    insets.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                    insets.hide(WindowInsetsCompat.Type.systemBars())
                    dialogWindow.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                    dialogWindow.setBackgroundDrawableResource(android.R.color.black)
                }
                onDispose {}
            }

            BackHandler(enabled = true) {
                isFullscreen = false
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            ) {
                VideoPlayerContent(isFull = true)
            }
        }
    }
}

private fun formatTime(millis: Long): String {
    val totalSeconds = (millis / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d", minutes, seconds)
}
