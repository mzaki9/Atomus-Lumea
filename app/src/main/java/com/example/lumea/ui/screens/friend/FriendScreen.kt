package com.example.lumea.ui.screens.friend

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.rounded.Air
import androidx.compose.material.icons.rounded.Bloodtype
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lumea.ui.components.GradientCardBackground
import com.example.lumea.ui.theme.AppTypography
import com.example.lumea.ui.theme.BluePrimary
import com.example.lumea.ui.theme.BlueSecondary
import com.example.lumea.ui.viewmodel.FriendDetailViewModel
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun FriendScreen(
    friendId: String,
    viewModel: FriendDetailViewModel = viewModel(factory = FriendDetailViewModel.Factory(LocalContext.current))
)  {
    // Fetch friend data when screen is first composed
    LaunchedEffect(friendId) {
        viewModel.fetchFriendData(friendId) // No need to pass token
    }

    // Collect states from ViewModel
    val friendData by viewModel.friendData.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    // Scroll state to make content scrollable
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        GradientCardBackground(
            modifier = Modifier.fillMaxSize(),
            height = 400.dp
        )

        // Loading indicator
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier
                    .size(50.dp)
                    .align(Alignment.Center),
                color = MaterialTheme.colorScheme.primary
            )
        }

        // Error message
        error?.let {
            Text(
                text = it,
                color = Color.Red,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(16.dp)
            )
        }

        // Friend data content
        friendData?.let { friend ->
            // Create a scrollable column for the entire content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
            ) {
                // Header content
                Column(
                    modifier = Modifier
                        .padding(horizontal = 24.dp, vertical = 40.dp)
                ) {
                    Spacer(modifier = Modifier.height(32.dp))

                    if (friend.profile?.name != null) {
                        FriendCard(
                            friendName = friend.profile.name,
                            healthStatus = friend.health?.status ?: "No health data available",
                            heartRate = friend.health?.heartRate ?: 0
                        )
                    }

                    // Add spacing between the top content and the white container
                    Spacer(modifier = Modifier.height(32.dp))
                }

                // White container with metrics
                // White container with metrics
                Surface(
                    modifier = Modifier
                        .fillMaxWidth(),
                    color = Color.White,
                    shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
                ) {
                    // Content inside white container
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .padding(bottom = 80.dp)
                    ) {
                        Text(
                            text = "Health Metrics",
                            style = AppTypography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 16.dp)
                        )

                        if (friend.health == null) {
                            // Display a message when no health data is available
                            Text(
                                text = "No health data available for this friend",
                                style = AppTypography.bodyMedium,
                                color = Color.Gray,
                                modifier = Modifier.padding(vertical = 16.dp)
                            )
                        } else {
                            // Only show metrics if health data is available
                            Row(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                // Heart Rate Card
                                MetricCard(
                                    title = "Heart Rate",
                                    value = (friend.health.heartRate ?: 0).toInt().toString(),
                                    unit = "BPM",
                                    icon = Icons.Rounded.Favorite,
                                    gradientColors = listOf(Color(0xFF4CAF50), Color(0xFF8BC34A)),
                                    modifier = Modifier.weight(1f)
                                )

                                Spacer(modifier = Modifier.width(16.dp))

                                // SPO2 Card
                                MetricCard(
                                    title = "Blood Oxygen",
                                    value = (friend.health.bloodOxygen ?: 0).toInt().toString(),
                                    unit = "%",
                                    icon = Icons.Rounded.Bloodtype,
                                    gradientColors = listOf(Color(0xFF2196F3), Color(0xFF03A9F4)),
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Respiratory Rate Card (full width)
                            MetricCard(
                                title = "Respiratory Rate",
                                value = (friend.health.respiratoryRate ?: 0).toInt().toString(),
                                unit = "breaths/min",
                                icon = Icons.Rounded.Air,
                                gradientColors = listOf(Color(0xFF9C27B0), Color(0xFFE040FB)),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    unit: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    gradientColors: List<Color>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.padding(4.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = gradientColors
                    )
                )
                .padding(16.dp)
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = title,
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = value,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Text(
                    text = unit,
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
fun FriendCard(
    friendName: String,
    healthStatus: String,
    heartRate: Int,
    viewModel: FriendDetailViewModel = viewModel(factory = FriendDetailViewModel.Factory(LocalContext.current))
) {
    val friendData by viewModel.friendData.collectAsState()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.15f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Friend",
                style = AppTypography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Friend info with gradient background
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                BluePrimary.copy(alpha = 0.7f),
                                BlueSecondary.copy(alpha = 0.7f)
                            )
                        )
                    )
                    .padding(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left side: Friend name and icon
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Person,
                            contentDescription = "Person",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )

                        Spacer(modifier = Modifier.width(16.dp))

                        Column {
                            Text(
                                text = friendName,
                                style = AppTypography.titleLarge,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = healthStatus,
                                style = AppTypography.bodyMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            friendData?.location?.let { location ->
                val context = LocalContext.current
                val mapsUrl = "https://www.google.com/maps?q=${location.latitude},${location.longitude}"

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(mapsUrl))
                            context.startActivity(intent)
                        }
                        .padding(vertical = 4.dp)
                ) {
                    Icon(
                        Icons.Filled.LocationOn,
                        contentDescription = "Latest Location",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "Latest Location",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }
        }
    }
}

@Composable @Preview
fun FriendScreenPreview() {
    FriendScreen(friendId = "2")
}