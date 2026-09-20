package com.skillbuilder.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import androidx.navigation.compose.rememberNavController
import com.skillbuilder.app.auth.GoogleAuthClient
import com.skillbuilder.app.data.local.AppSettings
import com.skillbuilder.app.data.local.AppThemeMode
import com.skillbuilder.app.data.local.StoragePlanManager
import com.skillbuilder.app.data.local.UserSession
import com.skillbuilder.app.ui.navigation.SkillBuilderNavGraph
import com.skillbuilder.app.ui.theme.SkillBuilderTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val googleAuthClient by lazy {
        GoogleAuthClient(
            context = this,
            webClientId = getString(R.string.default_web_client_id)
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        UserSession.initialize(this)
        StoragePlanManager.initialize(this)
        AppSettings.initialize(this)
        com.skillbuilder.app.data.local.RealTimeDataManager.initialize(this)

        setContent {
            var showSplash by remember { mutableStateOf(true) }
            val themeMode by AppSettings.themeMode.collectAsState()
            val systemDark = isSystemInDarkTheme()
            val isDarkTheme = when (themeMode) {
                AppThemeMode.SYSTEM -> systemDark
                AppThemeMode.LIGHT -> false
                AppThemeMode.DARK -> true
                else -> systemDark
            }

            SkillBuilderTheme(darkTheme = isDarkTheme) {
                LaunchedEffect(Unit) {
                    delay(900)
                    showSplash = false
                }

                if (showSplash) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(if (isDarkTheme) Color.Black else Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.app_logo),
                            contentDescription = "SkillBuilder",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 32.dp)
                        )
                    }
                } else {
                    val navController = rememberNavController()
                    SkillBuilderNavGraph(
                        navController = navController,
                        googleAuthClient = googleAuthClient
                    )
                }
            }
        }
    }
}
