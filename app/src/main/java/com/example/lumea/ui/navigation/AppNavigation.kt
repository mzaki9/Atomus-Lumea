package com.example.lumea.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.lumea.ui.components.BottomNavigationBar
import com.example.lumea.ui.components.TopBar
import com.example.lumea.ui.screens.camera.CameraScreen
import com.example.lumea.ui.screens.home.HomeScreen
import com.example.lumea.ui.screens.profile.ProfileScreen
import com.example.lumea.ui.screens.setting.SettingScreen

object Routes {
    const val HOME = "home"
    const val CAMERA = "camera"
    const val PROFILE = "profile"
    const val SETTING = "setting"
}

sealed class Screen(val route: String) {
    data object Home : Screen(Routes.HOME)
    data object Camera : Screen(Routes.CAMERA)
    data object Profile : Screen(Routes.PROFILE)
    data object Setting: Screen(Routes.SETTING)
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Home.route

    // Check if the current screen is the Settings screen
    val isSettingsScreen = currentRoute == Screen.Setting.route

    Scaffold(
        topBar = {
            if (currentRoute != Screen.Camera.route) {
                TopBar(
                    onGroupClick = { /* Group action */ },
                    onSettingsClick = { 
                        navController.navigate(Screen.Setting.route) {
                            // Save back stack and state to properly return
                            launchSingleTop = true
                        }
                    },
                    isSettingsScreen = isSettingsScreen,
                    onBackClick = {
                        // Navigate back when on Settings screen
                        navController.popBackStack()
                    }
                )
            }
        },
        bottomBar = {
            // Only show the bottom navigation bar on main screens (Home, Camera, Profile)
            // Hide it on Settings screen
            if (!isSettingsScreen) {
                BottomNavigationBar(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        // Only navigate if we're not already on this route
                        if (route != currentRoute) {
                            navController.navigate(route) {
                                // Pop up to the home route, but save its state
                                popUpTo(Screen.Home.route) {
                                    saveState = true
                                }
                                // Avoid multiple copies of the same destination
                                launchSingleTop = true
                                // Restore state when reselecting a previously selected item
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
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen()
            }
            composable(Screen.Camera.route) {
                CameraScreen()
            }
            composable(Screen.Profile.route) {
                ProfileScreen()
            }
            composable(Screen.Setting.route) {
                SettingScreen(
                    onLogoutClick = { /* Your logout logic */ }
                )
            }
        }
    }
}