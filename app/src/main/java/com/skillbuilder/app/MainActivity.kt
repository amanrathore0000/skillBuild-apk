package com.skillbuilder.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
        com.skillbuilder.app.data.local.SearchHistoryManager.initialize(this)

        setContent {
            val themeMode by AppSettings.themeMode.collectAsState()
            val systemDark = isSystemInDarkTheme()
            val isDarkTheme = when (themeMode) {
                AppThemeMode.SYSTEM -> systemDark
                AppThemeMode.LIGHT -> false
                AppThemeMode.DARK -> true
                else -> systemDark
            }

            SkillBuilderTheme(darkTheme = isDarkTheme) {
                val navController = rememberNavController()
                SkillBuilderNavGraph(
                    navController = navController,
                    googleAuthClient = googleAuthClient
                )
            }
        }
    }
}
