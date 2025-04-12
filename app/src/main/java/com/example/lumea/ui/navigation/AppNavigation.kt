package com.example.lumea.ui.navigation

import ProfileScreen
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.lumea.ui.auth.AuthUiState
import com.example.lumea.ui.auth.AuthViewModel
import com.example.lumea.ui.components.BottomNavigationBar
import com.example.lumea.ui.components.TopBar
import com.example.lumea.ui.screens.addfriends.AddFriendScreen
import com.example.lumea.ui.screens.camera.CameraScreen
import com.example.lumea.ui.screens.camera.CameraViewModel
import com.example.lumea.ui.screens.friend.FriendScreen
import com.example.lumea.ui.screens.friendlist.FriendListScreen
import com.example.lumea.ui.screens.home.HomeScreen
import com.example.lumea.ui.screens.login.LoginScreen
import com.example.lumea.ui.screens.register.RegisterScreen
import com.example.lumea.ui.screens.setting.SettingScreen




sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Home : Screen("home")
    object Camera : Screen("camera")
    object Profile : Screen("profile")
    object Setting : Screen("setting")
    object FriendList : Screen("friend_list")
    object AddFriend : Screen("add_friend")
    object FriendDetail : Screen("detail_teman/{friendId}") {
        fun createRoute(friendId: String) = "detail_teman/$friendId"
    }
}


@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Home.route

    val authViewModel: AuthViewModel =
        viewModel(factory = AuthViewModel.Factory(LocalContext.current))
    val cameraViewModel: CameraViewModel =
        viewModel(factory = CameraViewModel.Factory(LocalContext.current))
    val authState by authViewModel.uiState.collectAsState()

    // Handle auth state changes
    HandleAuthState(authState, currentRoute, navController)

    val isExceptionScreen = remember(currentRoute) {
        currentRoute == Screen.Setting.route ||
                currentRoute == Screen.FriendList.route ||
                currentRoute.startsWith(Screen.FriendDetail.route.substringBefore("{"))
        || currentRoute == Screen.AddFriend.route
    }

    val isAuthScreen = remember(currentRoute) {
        currentRoute == Screen.Login.route || currentRoute == Screen.Register.route
    }
    val isCameraScreen = remember(currentRoute) { currentRoute == Screen.Camera.route }

    val screenName = remember(currentRoute) {
        when {
            currentRoute == Screen.Setting.route -> "Setting"
            currentRoute == Screen.FriendList.route -> "Friend List"
            currentRoute.startsWith(Screen.FriendDetail.route.substringBefore("{")) -> "Friend Detail"
            currentRoute == Screen.AddFriend.route -> "Add Friend"
            else -> ""
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
        AppNavHost(navController, cameraViewModel, Modifier.padding(innerPadding))
    }
}

@Composable
fun AppNavHost(
    navController: NavHostController,
    cameraViewModel: CameraViewModel,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Login.route,
        modifier = modifier
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

        composable(
            route = Screen.FriendDetail.route,
            arguments = listOf(
                navArgument("friendId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val friendId = backStackEntry.arguments?.getString("friendId") ?: ""
            FriendScreen(friendId = friendId)
        }

        composable(Screen.FriendList.route) {
            FriendListScreen(navController)
        }
        composable(Screen.AddFriend.route) {
            AddFriendScreen()
        }

        // Main Screens
        composable(Screen.Home.route) {
            HomeScreen(cameraViewModel = cameraViewModel)
        }

        composable(Screen.Camera.route) {
            CameraScreen(viewModel = cameraViewModel)
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

@Composable
fun HandleAuthState(
    authState: AuthUiState,
    currentRoute: String,
    navController: NavHostController
) {
    LaunchedEffect(authState) {
        when (authState) {
            is AuthUiState.Authenticated -> {
                if (currentRoute == Screen.Login.route || currentRoute == Screen.Register.route) {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            inclusive = true
                        }
                    }
                }
            }

            is AuthUiState.NotAuthenticated -> {
                if (currentRoute != Screen.Login.route && currentRoute != Screen.Register.route) {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            inclusive = true
                        }
                    }
                }
            }

            else -> Unit
        }
    }
}
