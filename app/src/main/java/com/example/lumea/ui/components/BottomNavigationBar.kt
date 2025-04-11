package com.example.lumea.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.example.lumea.ui.theme.BlueLight
import com.example.lumea.ui.theme.PinkLight
import com.example.lumea.ui.navigation.Screen

sealed class BottomNavItem(val route: String, val title: String, val icon: ImageVector) {
    data object Home : BottomNavItem(Screen.Home.route, "Beranda", Icons.Default.Home)
    data object Camera : BottomNavItem(Screen.Camera.route, "Kamera", Icons.Default.Fingerprint)
    data object Profile : BottomNavItem(Screen.Profile.route, "Profil", Icons.Default.AccountCircle)
}

@Composable
fun BottomNavigationBar(
    currentRoute: String,
    onNavigate: (String) -> Unit
) {
    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.Camera,
        BottomNavItem.Profile
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(PinkLight)
            .padding(top = 0.dp, bottom = 8.dp, start = 8.dp, end = 8.dp), 
        contentAlignment = Alignment.BottomCenter
    ) {
        // Row for Left and Right Navigation Items
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Navigation Item
            NavigationBarItem(
                icon = { Icon(BottomNavItem.Home.icon, contentDescription = BottomNavItem.Home.title) },
                label = { Text(text = BottomNavItem.Home.title) },
                selected = currentRoute == BottomNavItem.Home.route,
                onClick = { onNavigate(BottomNavItem.Home.route) }
            )

            // Spacer to make room for center button
            Spacer(modifier = Modifier.width(64.dp))

            // Right Navigation Item
            NavigationBarItem(
                icon = { Icon(BottomNavItem.Profile.icon, contentDescription = BottomNavItem.Profile.title) },
                label = { Text(text = BottomNavItem.Profile.title) },
                selected = currentRoute == BottomNavItem.Profile.route,
                onClick = { onNavigate(BottomNavItem.Profile.route) }
            )
        }

        // Center Floating Button (Fingerprint)
        Box(
            modifier = Modifier
                .size(70.dp) 
                .offset(y = (-35).dp) 
                .clip(CircleShape)
                .background(BlueLight)
                .align(Alignment.TopCenter)
                .clickable { onNavigate(Screen.Camera.route) }
                .zIndex(1f),
        contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(65.dp)
                    .clip(CircleShape)
                    .background(BlueLight),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = BottomNavItem.Camera.icon,
                    contentDescription = "Fingerprint",
                    tint = Color.Black,
                    modifier = Modifier.size(40.dp) 
                )
            }

            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.05f))
                    .zIndex(-1f)
            )
        }
    }
}

@Composable @Preview
fun BottomNavigationBarPreview() {
    BottomNavigationBar(currentRoute = Screen.Home.route, onNavigate = {})
}