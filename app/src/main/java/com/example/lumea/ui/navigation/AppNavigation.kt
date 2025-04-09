package com.example.lumea.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.lumea.ui.auth.AuthUiState
import com.example.lumea.ui.auth.AuthViewModel
import com.example.lumea.ui.components.BottomNavigationBar
import com.example.lumea.ui.components.TopBar
import com.example.lumea.ui.screens.camera.CameraScreen
import com.example.lumea.ui.screens.camera.CameraViewModel
import com.example.lumea.ui.screens.home.HomeScreen
import com.example.lumea.ui.screens.login.LoginScreen
import com.example.lumea.ui.screens.profile.ProfileScreen
import com.example.lumea.ui.screens.register.RegisterScreen
import com.example.lumea.ui.screens.setting.SettingScreen

object Routes {
    const val HOME = "home"
    const val CAMERA = "camera"
    const val PROFILE = "profile"
    const val SETTING = "setting"

    // Auth routes
    const val LOGIN = "login"
    const val REGISTER = "register"
}

sealed class Screen(val route: String) {
    data object Home : Screen(Routes.HOME)
    data object Camera : Screen(Routes.CAMERA)
    data object Profile : Screen(Routes.PROFILE)
    data object Setting: Screen(Routes.SETTING)
    data object Login : Screen(Routes.LOGIN)
    data object Register : Screen(Routes.REGISTER)
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Home.route

    val authViewModel: AuthViewModel = viewModel(factory = AuthViewModel.Factory(LocalContext.current))
    val viewModel: CameraViewModel = viewModel(factory = CameraViewModel.Factory(LocalContext.current))
    val authState by authViewModel.uiState.collectAsState()

    // Check authentication state and navigate accordingly
    LaunchedEffect(authState) {
        when (authState) {
            is AuthUiState.Authenticated -> {
                if (currentRoute == Routes.LOGIN || currentRoute == Routes.REGISTER) {
                    navController.navigate(Routes.HOME) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            inclusive = true
                        }
                    }
                }
            }
            is AuthUiState.NotAuthenticated -> {
                if (currentRoute != Routes.LOGIN && currentRoute != Routes.REGISTER) {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            inclusive = true
                        }
                    }
                }
            }
            else -> { /* Loading or Error states handled elsewhere */ }
        }
    }

    // Check if the current screen is the Settings screen
    val isSettingsScreen = currentRoute == Screen.Setting.route

    // Check if we're on an auth screen (login/register)
    val isAuthScreen = currentRoute == Routes.LOGIN || currentRoute == Routes.REGISTER

    Scaffold(
        topBar = {
            if (!isAuthScreen && currentRoute != Screen.Camera.route) {
                TopBar(
                    onGroupClick = { /* Group action */ },
                    onSettingsClick = {
                        navController.navigate(Screen.Setting.route) {
                            launchSingleTop = true
                        }
                    },
                    isSettingsScreen = isSettingsScreen,
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }
        },
        bottomBar = {
            // Only show the bottom navigation bar on main screens
            // Hide it on Settings screen and auth screens
            if (!isSettingsScreen && !isAuthScreen) {
                BottomNavigationBar(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        if (route != currentRoute) {
                            navController.navigate(route) {
                                popUpTo(Screen.Home.route) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Login.route,  // Start with login by default
            modifier = Modifier.padding(innerPadding)
        ) {
            // Auth screens
            composable(Screen.Login.route) {
                LoginScreen(
                    onCreateAccountClick = {
                        navController.navigate(Screen.Register.route)
                    }
                )
            }

            composable(Screen.Register.route) {
                RegisterScreen(
                    onLoginClick = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                )
            }

            // Main app screens
            composable(Screen.Home.route) {
                HomeScreen(viewModel = viewModel)
            }

            composable(Screen.Camera.route) {
                CameraScreen(viewModel = viewModel)
            }

            composable(Screen.Profile.route) {
                ProfileScreen()
            }

            composable(Screen.Setting.route) {
                SettingScreen(
                    onLogoutClick = {
                        // Navigate to login screen after logout
                        navController.navigate(Screen.Login.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                inclusive = true
                            }
                        }
                    }
                )
            }
        }
    }
}