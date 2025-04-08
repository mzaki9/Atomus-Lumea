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

object Routes {
    const val HOME = "home"
    const val CAMERA = "camera"
    const val PROFILE = "profile"
}

sealed class Screen(val route: String) {
    data object Home : Screen(Routes.HOME)
    data object Camera : Screen(Routes.CAMERA)
    data object Profile : Screen(Routes.PROFILE)
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Home.route

    Scaffold(
        topBar = {
            if (currentRoute != Screen.Camera.route) {
                TopBar(
                    onGroupClick = { /* Buat Ke koneksi */ },
                    onSettingsClick = { /* buat ke settings k */ }
                )
            }
        },
        bottomBar = {
            BottomNavigationBar(
                currentRoute = currentRoute,
                onNavigate = { route ->
                    navController.navigate(route) {
                        popUpTo(Screen.Home.route) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
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
        }
    }
}