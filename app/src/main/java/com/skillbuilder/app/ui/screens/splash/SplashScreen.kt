package com.skillbuilder.app.ui.screens.splash

import android.content.res.Configuration
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.luminance
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.skillbuilder.app.R
import com.skillbuilder.app.ui.theme.ObsidianCanvas
import kotlinx.coroutines.delay

/**
 * Highly compatible, screen-size responsive splash screen.
 * Dynamically adjusts logo sizing, aspect ratio constraints, and typography
 * across compact phones, standard displays, tablets, and landscape orientations.
 */
@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit
) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val isTablet = configuration.smallestScreenWidthDp >= 600

    val backgroundColor = MaterialTheme.colorScheme.background
    val contentColor = MaterialTheme.colorScheme.onBackground
    val subtitleColor = MaterialTheme.colorScheme.onSurfaceVariant
    val crimsonAccent = MaterialTheme.colorScheme.primary
    val isDark = MaterialTheme.colorScheme.background == ObsidianCanvas || 
                 MaterialTheme.colorScheme.background.luminance() < 0.5f

    // Animation controllers
    val logoScale = remember { Animatable(0.85f) }
    val logoAlpha = remember { Animatable(0f) }
    val textAlpha = remember { Animatable(0f) }
    val pulseAlpha = remember { Animatable(0.3f) }

    LaunchedEffect(Unit) {
        // Smooth scale and fade in of the brand logo
        logoAlpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing)
        )
    }

    LaunchedEffect(Unit) {
        logoScale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
    }

    LaunchedEffect(Unit) {
        delay(200)
        // Fade in the tagline and bottom details promptly
        textAlpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 450, easing = FastOutSlowInEasing)
        )
    }

    LaunchedEffect(Unit) {
        pulseAlpha.animateTo(
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 800, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            )
        )
    }

    // Auto-advance to main content after splash completes
    LaunchedEffect(Unit) {
        delay(1500)
        onSplashFinished()
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        // Calculate responsive logo dimensions tailored to the device screen size
        val logoDimensions = remember(screenWidth, screenHeight, isLandscape, isTablet) {
            calculateResponsiveLogoDimensions(
                screenWidth = screenWidth,
                screenHeight = screenHeight,
                isLandscape = isLandscape,
                isTablet = isTablet
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top spacer for optical balance
            Spacer(modifier = Modifier.height(10.dp))

            // Center: Responsive Brand Logo & Tagline
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .scale(logoScale.value)
                    .alpha(logoAlpha.value)
            ) {
                // Main Logo Banner (Banner aspect ratio is ~800x257, 3.11:1)
                val bannerRes = if (isDark) R.drawable.splash_banner_dark else R.drawable.splash_banner_light

                Image(
                    painter = painterResource(id = bannerRes),
                    contentDescription = "Skill Builder",
                    modifier = Modifier
                        .width(logoDimensions.logoWidth)
                        .height(logoDimensions.logoHeight),
                    contentScale = ContentScale.Fit
                )

                Spacer(modifier = Modifier.height(logoDimensions.spacingAfterLogo))

                // Tagline with adaptive font size
                Text(
                    text = "Learn Skill. Master Skill. Build Together.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = logoDimensions.taglineFontSize.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 0.5.sp
                    ),
                    color = subtitleColor,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.alpha(textAlpha.value)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Elegant crimson brand line indicator
                Surface(
                    shape = RoundedCornerShape(2.dp),
                    color = crimsonAccent,
                    modifier = Modifier
                        .width(logoDimensions.indicatorWidth)
                        .height(3.dp)
                        .alpha(textAlpha.value)
                ) {}
            }

            // Bottom: Subdued Brand Info / Version & Loading Pulse
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .alpha(textAlpha.value)
                    .padding(bottom = 12.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(crimsonAccent.copy(alpha = pulseAlpha.value))
                    )
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(crimsonAccent.copy(alpha = 1f - (pulseAlpha.value * 0.5f)))
                    )
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(crimsonAccent.copy(alpha = pulseAlpha.value * 0.8f))
                    )
                }

                Text(
                    text = "Empowering Reciprocal Learning",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = (logoDimensions.taglineFontSize * 0.82f).sp,
                        letterSpacing = 0.8.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = subtitleColor.copy(alpha = 0.7f)
                )
            }
        }
    }
}

/**
 * Holds calculated responsive dimensions for the splash screen logo and typography.
 */
private data class ResponsiveLogoDimensions(
    val logoWidth: Dp,
    val logoHeight: Dp,
    val spacingAfterLogo: Dp,
    val taglineFontSize: Float,
    val indicatorWidth: Dp
)

/**
 * Calculates responsive dimensions based on the screen width, height,
 * tablet qualification, and orientation.
 */
private fun calculateResponsiveLogoDimensions(
    screenWidth: Dp,
    screenHeight: Dp,
    isLandscape: Boolean,
    isTablet: Boolean
): ResponsiveLogoDimensions {
    val bannerAspect = 800f / 257f // ~3.11

    return when {
        // 1. Landscape Orientation: Height constrained to prevent clipping
        isLandscape -> {
            val height = (screenHeight * 0.22f).coerceIn(48.dp, 80.dp)
            val width = height * bannerAspect
            ResponsiveLogoDimensions(
                logoWidth = width,
                logoHeight = height,
                spacingAfterLogo = 8.dp,
                taglineFontSize = 12f,
                indicatorWidth = 32.dp
            )
        }

        // 2. Tablets and Foldable Outer Displays (sw >= 600dp)
        isTablet -> {
            val width = (screenWidth * 0.42f).coerceIn(320.dp, 440.dp)
            val height = width / bannerAspect
            ResponsiveLogoDimensions(
                logoWidth = width,
                logoHeight = height,
                spacingAfterLogo = 22.dp,
                taglineFontSize = 16f,
                indicatorWidth = 52.dp
            )
        }

        // 3. Compact / Small Phones (< 360dp width)
        screenWidth < 360.dp -> {
            val width = (screenWidth * 0.72f).coerceIn(180.dp, 220.dp)
            val height = width / bannerAspect
            ResponsiveLogoDimensions(
                logoWidth = width,
                logoHeight = height,
                spacingAfterLogo = 12.dp,
                taglineFontSize = 11.5f,
                indicatorWidth = 34.dp
            )
        }

        // 4. Large Phones / Phablets (>= 420dp width)
        screenWidth >= 420.dp -> {
            val width = (screenWidth * 0.62f).coerceIn(250.dp, 310.dp)
            val height = width / bannerAspect
            ResponsiveLogoDimensions(
                logoWidth = width,
                logoHeight = height,
                spacingAfterLogo = 18.dp,
                taglineFontSize = 14f,
                indicatorWidth = 44.dp
            )
        }

        // 5. Standard Phones (360dp - 419dp width)
        else -> {
            val width = (screenWidth * 0.65f).coerceIn(220.dp, 260.dp)
            val height = width / bannerAspect
            ResponsiveLogoDimensions(
                logoWidth = width,
                logoHeight = height,
                spacingAfterLogo = 16.dp,
                taglineFontSize = 13f,
                indicatorWidth = 38.dp
            )
        }
    }
}
