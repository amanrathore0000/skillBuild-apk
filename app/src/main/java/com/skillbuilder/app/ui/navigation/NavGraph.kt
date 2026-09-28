package com.skillbuilder.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.skillbuilder.app.auth.GoogleAuthClient
import com.skillbuilder.app.data.local.UserSession
import com.skillbuilder.app.ui.screens.auth.AuthScreen
import com.skillbuilder.app.ui.screens.auth.ForgotPasswordScreen
import com.skillbuilder.app.ui.screens.auth.IntroGuidelinesScreen
import com.skillbuilder.app.ui.screens.auth.RegisterScreen
import com.skillbuilder.app.ui.screens.dashboard.MainDashboardScreen
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

sealed interface Screen {
    @Serializable
    data object Splash : Screen

    @Serializable
    data object Auth : Screen

    @Serializable
    data object ForgotPassword : Screen

    @Serializable
    data object IntroGuidelines : Screen

    @Serializable
    data object Register : Screen

    @Serializable
    data object MainDashboard : Screen

    @Serializable
    data class CourseDetail(val courseId: String) : Screen
}

@Composable
fun SkillBuilderNavGraph(
    navController: NavHostController,
    googleAuthClient: GoogleAuthClient
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash
    ) {

        composable<Screen.Splash> {
            com.skillbuilder.app.ui.screens.splash.SplashScreen(
                onSplashFinished = {
                    val target: Screen = if (UserSession.isUserLoggedIn()) {
                        Screen.MainDashboard
                    } else {
                        Screen.Auth
                    }
                    navController.navigate(target) {
                        popUpTo<Screen.Splash> { inclusive = true }
                    }
                }
            )
        }

        composable<Screen.Auth> {
            AuthScreen(
                googleAuthClient = googleAuthClient,
                onNavigateToRegister = {
                    navController.navigate(Screen.IntroGuidelines)
                },
                onNavigateToForgotPassword = {
                    navController.navigate(Screen.ForgotPassword)
                },
                onLoginSuccess = {
                    navController.navigate(Screen.MainDashboard) {
                        popUpTo<Screen.Auth> { inclusive = true }
                    }
                }
            )
        }

        composable<Screen.ForgotPassword> {
            ForgotPasswordScreen(
                onNavigateBackToLogin = {
                    navController.popBackStack()
                },
                onResetSuccess = {
                    navController.navigate(Screen.Auth) {
                        popUpTo<Screen.Auth> { inclusive = false }
                    }
                }
            )
        }

        composable<Screen.IntroGuidelines> {
            IntroGuidelinesScreen(
                onNavigateToLogin = {
                    navController.popBackStack()
                },
                onProceedToRegister = {
                    navController.navigate(Screen.Register)
                }
            )
        }

        composable<Screen.Register> {
            RegisterScreen(
                googleAuthClient = googleAuthClient,
                onNavigateToLogin = {
                    navController.navigate(Screen.Auth) {
                        popUpTo<Screen.Auth> { inclusive = false }
                    }
                },
                onRegisterSuccess = {
                    navController.navigate(Screen.MainDashboard) {
                        popUpTo<Screen.Auth> { inclusive = true }
                    }
                }
            )
        }

        composable<Screen.MainDashboard> {
            val scope = androidx.compose.runtime.rememberCoroutineScope()
            MainDashboardScreen(
                onLogout = {
                    scope.launch {
                        googleAuthClient.signOut()
                    }
                    UserSession.clear()
                    navController.navigate(Screen.Auth) {
                        popUpTo<Screen.MainDashboard> { inclusive = true }
                    }
                }
            )
        }
    }
}
