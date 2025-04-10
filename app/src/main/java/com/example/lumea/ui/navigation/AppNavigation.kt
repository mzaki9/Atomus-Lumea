package com.example.lumea.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
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
import com.example.lumea.ui.screens.friendlist.FriendListScreen
import com.example.lumea.ui.screens.addfriends.AddFriendScreen

object Routes {
    const val HOME = "home"
    const val CAMERA = "camera"
    const val PROFILE = "profile"
    const val SETTING = "setting"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val FRIEND_LIST = "friend_list"
    const val ADD_FRIEND = "add_friend"
}


sealed class Screen(val route: String) {
    data object Home : Screen(Routes.HOME)
    data object Camera : Screen(Routes.CAMERA)
    data object Profile : Screen(Routes.PROFILE)
    data object Setting : Screen(Routes.SETTING)
    data object Login : Screen(Routes.LOGIN)
    data object Register : Screen(Routes.REGISTER)
    data object FriendList : Screen(Routes.FRIEND_LIST)
    data object AddFriend : Screen(Routes.ADD_FRIEND)
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Home.route

    val authViewModel: AuthViewModel =
        viewModel(factory = AuthViewModel.Factory(LocalContext.current))
    val viewModel: CameraViewModel = viewModel(factory = CameraViewModel.Factory(LocalContext.current))
    val authState by authViewModel.uiState.collectAsState()

    // Handle auth state changes
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

            else -> Unit
        }
    }

    val isExceptionScreen = currentRoute == Screen.Setting.route || currentRoute == Screen.FriendList.route
    val isAuthScreen = currentRoute == Routes.LOGIN || currentRoute == Routes.REGISTER
    val isCameraScreen = currentRoute == Screen.Camera.route
    var screenName = ""
    if (isExceptionScreen){
        if (currentRoute==Screen.Setting.route){
            screenName = "Setting";
        }else if (currentRoute==Screen.FriendList.route){
            screenName = "Friend List";
        }
    }
    Scaffold(
        topBar = {
            if (!isAuthScreen && !isCameraScreen) {
                TopBar(
                    onGroupClick = {
                        navController.navigate(Screen.FriendList.route) {
                            launchSingleTop = true
                        }
                    },
                    onSettingsClick = {
                        navController.navigate(Screen.Setting.route) {
                            launchSingleTop = true
                        }
                    },
                    isExceptionScreen = isExceptionScreen,
                    screenName = screenName,
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }
        },
        bottomBar = {
            if (!isAuthScreen && !isExceptionScreen) {
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
            startDestination = Screen.Login.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            // Auth Screens
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

            composable(Screen.FriendList.route) {
                FriendListScreen(navController) // ✅ navController dipass
            }
            composable(Screen.AddFriend.route) {
                AddFriendScreen() // ✅ navigasi ke halaman tambah teman
            }

            // Main Screens
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
