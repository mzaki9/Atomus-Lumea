package com.example.lumea.ui.screens.home

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Air
import androidx.compose.material.icons.rounded.Bloodtype
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lumea.ui.components.GradientCardBackground
import com.example.lumea.ui.screens.camera.CameraViewModel
import com.example.lumea.ui.theme.AppTypography
import com.example.lumea.ui.theme.BluePrimary
import com.example.lumea.ui.theme.BlueSecondary
import com.example.lumea.ui.viewmodel.HomeViewModel

@Composable
fun HomeScreen(
    cameraViewModel: CameraViewModel = viewModel(factory = CameraViewModel.Factory(LocalContext.current))
) {
    // Create HomeViewModel to fetch health data
    val homeViewModel: HomeViewModel = viewModel(
        factory = HomeViewModel.Factory(LocalContext.current, cameraViewModel)
    )
    
    // Add state collection for HomeViewModel
    val isLoading by homeViewModel.isLoading.collectAsState()
    val statusMessage by homeViewModel.statusMessage.collectAsState()
    val latestHealthData by homeViewModel.latestHealthData.collectAsState()
    // Refresh data when the screen is first composed
    LaunchedEffect(key1 = Unit) {
        homeViewModel.clearLatestHealthData()
        homeViewModel.fetchLatestHealthData()
    }
    
    // Collect health metrics from CameraViewModel - keeping the existing UI code as is
    val heartRate by cameraViewModel.heartRate.collectAsState()
    val respiratoryRate by cameraViewModel.respiratoryRate.collectAsState()
    val spo2 by cameraViewModel.spo2.collectAsState()
    val riskClass by cameraViewModel.riskClass.collectAsState()
    
    // Determine health condition based on risk class
    val healthCondition = when (riskClass) {
        1 -> "Tidak Sehat"
        2 -> "Kurang Sehat"
        3 -> "Cukup Sehat"
        4 -> "Sangat Sehat"
        else -> "---" // Default value
    }

    // Scroll state to make content scrollable
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        GradientCardBackground(
            modifier = Modifier.fillMaxSize(),
            height = 400.dp
        )

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
                Text(
                    text = "Sendi sehat",
                    style = AppTypography.headlineSmall,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "semangat gowes ya",
                    style = AppTypography.headlineSmall,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Health Status Card - displays PPG health condition
                HealthStatusCard(
                    healthCondition = healthCondition,
                    heartRate = heartRate
                )
                
                // Add spacing between the top content and the white container
                Spacer(modifier = Modifier.height(32.dp))
            }

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
                        .padding(bottom = 80.dp) // Extra padding at bottom for navigation bar
                ) {
                    Text(
                        text = "Your Health Metrics",
                        style = AppTypography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Heart Rate Card
                        MetricCard(
                            title = "Heart Rate",
                            value = if (heartRate > 0) heartRate.toString() else "--",
                            unit = "BPM",
                            icon = Icons.Rounded.Favorite,
                            gradientColors = listOf(Color(0xFF4CAF50), Color(0xFF8BC34A)),
                            modifier = Modifier.weight(1f)
                        )
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        // SPO2 Card
                        MetricCard(
                            title = "Blood Oxygen",
                            value = if (spo2 > 0) String.format("%.0f", spo2) else "--",
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
                        value = if (respiratoryRate > 0) String.format("%.1f", respiratoryRate) else "--",
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
fun HealthStatusCard(
    healthCondition: String,
    heartRate: Int
) {
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
                text = "Health Status",
                style = AppTypography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Health condition indicator with gradient background
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
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (healthCondition != "---") healthCondition else "No Data",
                            style = AppTypography.titleLarge,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        
                        if (healthCondition == "---") {
                            Text(
                                text = "Take a measurement to see your status",
                                style = AppTypography.bodySmall,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // Heart rate indicator
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Favorite,
                            contentDescription = "Heart Rate",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )

                        Spacer(modifier = Modifier.width(4.dp))

                        Text(
                            text = "${if (heartRate > 0) heartRate else "--"} BPM",
                            style = AppTypography.bodyMedium,
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (heartRate > 0) "Last updated: Just now" else "No measurements yet",
                style = AppTypography.bodySmall,
                color = Color.White.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable @Preview
fun HomeScreenPreview() {
    // For preview only, create a dummy viewModel
    val previewViewModel: CameraViewModel = viewModel(factory = CameraViewModel.Factory(LocalContext.current))
    HomeScreen(cameraViewModel = previewViewModel)
}