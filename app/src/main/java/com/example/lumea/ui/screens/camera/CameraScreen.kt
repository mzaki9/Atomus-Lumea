package com.example.lumea.ui.screens.camera

import android.Manifest
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lumea.data.sensor.CameraManager
import com.example.lumea.ui.theme.AppTypography
import com.example.lumea.ui.theme.LumeaTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

@OptIn(ExperimentalPermissionsApi::class)
@Composable

fun CameraScreen(
    viewModel: CameraViewModel = viewModel(factory = CameraViewModel.Factory(LocalContext.current))
) {
    val lifecycleOwner = LocalLifecycleOwner.current

    // State
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
    val cameraState by viewModel.cameraState.collectAsState(CameraManager.CameraState.Idle)
    val heartRate by viewModel.heartRate.collectAsState()
    val confidence by viewModel.confidence.collectAsState()
    val respiratoryRate by viewModel.respiratoryRate.collectAsState()
    val spo2 by viewModel.spo2.collectAsState()
    val riskPrediction by viewModel.riskPrediction.collectAsState()
    val riskClass by viewModel.riskClass.collectAsState() // Collect riskClass
    val preview by viewModel.preview.collectAsState()

    // Request camera permission if not granted
    LaunchedEffect(Unit) {
        if (!cameraPermissionState.status.isGranted) {
            cameraPermissionState.launchPermissionRequest()
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {

        val locationPermissionState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)

        if (cameraPermissionState.status.isGranted && locationPermissionState.status.isGranted) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                // Camera Preview or Heart Rate Display
                when (cameraState) {
                    is CameraManager.CameraState.Measuring -> {
                        // Camera Preview with Overlay
                        Box(modifier = Modifier.fillMaxSize()) {
                            // Camera Preview
                            preview?.let { previewUseCase ->
                                AndroidView(
                                    factory = { ctx ->
                                        androidx.camera.view.PreviewView(ctx).apply {
                                            previewUseCase.setSurfaceProvider(surfaceProvider)
                                        }
                                    },
                                    modifier = Modifier.fillMaxSize()
                                )
                            }

                            // Centered Overlay
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Card(
                                    modifier = Modifier
                                        .padding(16.dp)
                                        .width(300.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surface.copy(
                                            alpha = 0.8f
                                        )
                                    ),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(24.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        if (heartRate > 0) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.Center
                                            ) {
                                                Icon(
                                                    Icons.Rounded.Favorite,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(32.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = "$heartRate BPM",
                                                    fontSize = 32.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        } else {
                                            Text(
                                                text = "Place your finger gently over the camera and flash",
                                                textAlign = TextAlign.Center,
                                                style = MaterialTheme.typography.bodyLarge
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    else -> {
                        HeartRateDisplay(
                            heartRate = heartRate,
                            confidence = confidence,
                            respiratoryRate = respiratoryRate,
                            spo2 = spo2,
                            riskClass = riskClass,
                            riskPrediction = riskPrediction
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 32.dp),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    if (cameraState is CameraManager.CameraState.Measuring) {
                    // Show a circular progress indicator when measuring
                    Card(
                        shape = RoundedCornerShape(28.dp),
                        elevation = CardDefaults.cardElevation(4.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                        ) {
                            // Track the elapsed time since measurement started
                            val elapsedTime = remember { mutableStateOf(0f) }
                            val totalDuration = 30f // 60 seconds / 1 minute
                            
                            // Update elapsed time animation
                            LaunchedEffect(cameraState) {
                                elapsedTime.value = 0f
                                while (elapsedTime.value < totalDuration && cameraState is CameraManager.CameraState.Measuring) {
                                    kotlinx.coroutines.delay(100)
                                    elapsedTime.value += 0.1f
                                }
                            }
                            
                            CircularProgressIndicator(
                                progress = { (elapsedTime.value / totalDuration).coerceIn(0f, 1f) },
                                modifier = Modifier.size(32.dp),
                                color = MaterialTheme.colorScheme.primary,
                                strokeWidth = 3.dp
                            )
                            
                            Spacer(modifier = Modifier.width(12.dp))
                            
                            Column {
                                Text(
                                    "Measuring...",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                
                                Text(
                                    "${(totalDuration - elapsedTime.value).toInt()} seconds remaining",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                )
                            }   
                        }
                    }
                
                    } else if (heartRate <= 0 && respiratoryRate <= 0 && spo2 <= 0) {
                        // Only show the start button when there are no measurement results
                        Button(
                            onClick = { viewModel.startMeasurement(lifecycleOwner) },
                            shape = RoundedCornerShape(24.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
                            elevation = ButtonDefaults.buttonElevation(4.dp)
                        ) {
                            Icon(
                                Icons.Rounded.Favorite,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Start Measurement",
                                style = AppTypography.titleMedium,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    } else {
                        // When results are displayed, show a smaller "Measure Again" button
                        Button(
                            onClick = { viewModel.startMeasurement(lifecycleOwner) },
                            shape = RoundedCornerShape(24.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                            elevation = ButtonDefaults.buttonElevation(4.dp)
                        ) {
                            Icon(
                                Icons.Rounded.Favorite,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Measure Again",
                                style = AppTypography.labelLarge,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }
            }
        } else {
            // Permission Request Screen
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Filled.Favorite,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    tint = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Camera Permission Required",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "To measure your heart rate, we need access to your camera,flash, and location",
                    textAlign = TextAlign.Center,
                    style = AppTypography.bodyLarge
                )

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = { cameraPermissionState.launchPermissionRequest()
                    locationPermissionState.launchPermissionRequest() },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Text("Grant Access")
                }
            }
        }
    }
}


@Composable @Preview
//camera screen preview
fun CameraScreenPreview (){
    LumeaTheme {
        CameraScreen()
    }
}