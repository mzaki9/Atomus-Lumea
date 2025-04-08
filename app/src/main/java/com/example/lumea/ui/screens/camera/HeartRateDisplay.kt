package com.example.lumea.ui.screens.camera

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale

@Composable
fun HeartRateDisplay(
    heartRate: Int,
    confidence: Float,
    respiratoryRate: Float,
    spo2: Float,
    riskClass: Int?,
    riskPrediction: FloatArray? // Tambahkan parameter riskPrediction
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Heart Icon
        Icon(
            Icons.Rounded.Favorite,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(80.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Heart Rate Value
        Text(
            text = if (heartRate > 0) "$heartRate" else "--",
            fontSize = 72.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Text(
            text = "BPM",
            fontSize = 24.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (respiratoryRate > 0) {
            Text(
                text = "Respiratory Rate: ${String.format("%.1f", respiratoryRate)} breaths/min",
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // SpO2 Display
        if (spo2 > 0) {
            Text(
                text = "SpO2: ${String.format("%.1f", spo2)}%",
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Display Risk Prediction with Percentage and Category
        riskClass?.let { className ->
            riskPrediction?.getOrNull(className)?.let { probability ->
                val percentage = String.format(Locale.US, "%.0f", probability * 100)
                val healthCondition = when (className) {
                    1 -> "Tidak Sehat"
                    2 -> "Kurang Sehat"
                    3 -> "Cukup Sehat"
                    4 -> "Sangat Sehat"
                    else -> "Unknown Condition"
                }
                Text(
                    text = "$percentage% $healthCondition",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        // Confidence Indicator
        if (heartRate > 0) {
            Row(
                modifier = Modifier.fillMaxWidth(0.8f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Accuracy",
                    fontSize = 16.sp,
                    modifier = Modifier.width(80.dp)
                )

                LinearProgressIndicator(
                    progress = { confidence },
                    modifier = Modifier
                        .height(8.dp)
                        .weight(1f),
                    color = when {
                        confidence > 0.7f -> Color(0xFF4CAF50)
                        confidence > 0.4f -> Color(0xFFFFC107)
                        else -> Color(0xFFF44336)
                    },
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = when {
                    confidence > 0.7f -> "Good result"
                    confidence > 0.4f -> "Fair result"
                    else -> "Try again for better accuracy"
                },
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )
        }
    }
}