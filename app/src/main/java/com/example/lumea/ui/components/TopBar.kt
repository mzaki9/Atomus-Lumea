package com.example.lumea.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    onGroupClick: () -> Unit,
    onSettingsClick: () -> Unit,
    isExceptionScreen: Boolean = false,
    screenName: String = "",
    onBackClick: () -> Unit = {}
) {
    TopAppBar(
        modifier = Modifier.fillMaxWidth(),
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.White,
            navigationIconContentColor = MaterialTheme.colorScheme.primary,
            actionIconContentColor = MaterialTheme.colorScheme.primary
        ),
        navigationIcon = if (isExceptionScreen) {
            {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.primary // Use primary blue color
                    )
                }
            }
        } else {
            { /* No navigation icon for other screens */ }
        },
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (!isExceptionScreen) {
                    // Show the greeting for non-settings screens
                    Text(
                        text = "Hai, ",
                        fontSize = 18.sp,
                        color = Color.Black
                    )
                    Text(
                        text = "Bowo",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                } else {
                    // Show Settings title for the settings screen
                    Text(
                        text = screenName,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Only show action icons for non-settings screens
                if (!isExceptionScreen) {
                    IconButton(onClick = onGroupClick) {
                        Icon(
                            imageVector = Icons.Default.Group,
                            contentDescription = "Group",
                            tint = MaterialTheme.colorScheme.primary // Use primary blue color
                        )
                    }
                    IconButton(onClick = onSettingsClick) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.primary // Use primary blue color
                        )
                    }
                }
            }
        }
    )
}