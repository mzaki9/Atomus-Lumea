package com.example.lumea.ui.components

import android.graphics.Color as AndroidColor
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.lumea.data.model.HealthData
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HealthHistoryCharts(
    healthData: List<HealthData>,
    modifier: Modifier = Modifier
) {
    if (healthData.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(250.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surface),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No health history data available yet",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
        return
    }

    // Sort data by date (newest to oldest) and take last 10 entries
    val sortedData = remember(healthData) {
        healthData.sortedByDescending { it.date }.take(10).reversed()
    }

    // Format dates for X-axis labels
    val dateFormat = SimpleDateFormat("MM/dd", Locale.getDefault())
    val xAxisLabels = remember(sortedData) {
        sortedData.map {
            try {
                val date = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                    .parse(it.date)
                dateFormat.format(date ?: Date())
            } catch (e: Exception) {
                "N/A"
            }
        }
    }

    // Extract colors from the theme in the composable context
    val textColor = MaterialTheme.colorScheme.onSurface.toArgb()
    val primaryColor = MaterialTheme.colorScheme.primary.toArgb()
    val surfaceColor = MaterialTheme.colorScheme.surface.toArgb()

    Column(modifier = modifier.fillMaxWidth()) {
        // Heart Rate Chart
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(top = 12.dp, bottom = 8.dp)) {
                Text(
                    text = "Heart Rate",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
                
                AndroidView(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .padding(8.dp),
                    factory = { context ->
                        LineChart(context).apply {
                            description.isEnabled = false
                            legend.isEnabled = false
                            setTouchEnabled(true)
                            setScaleEnabled(false)
                            isDragEnabled = true
                            setPinchZoom(false)
                            setDrawGridBackground(false)

                            // Configure X-axis
                            xAxis.apply {
                                position = XAxis.XAxisPosition.BOTTOM
                                granularity = 1f
                                setDrawGridLines(false)
                                valueFormatter = IndexAxisValueFormatter(xAxisLabels)
                                setTextColor(textColor)
                            }

                            // Configure Y-axis
                            axisLeft.apply {
                                setDrawGridLines(true)
                                setTextColor(textColor)
                            }

                            axisRight.isEnabled = false

                            // Add empty space to left and right
                            extraLeftOffset = 10f
                            extraRightOffset = 10f
                        }
                    },
                    update = { chart ->
                        // Heart rate data
                        val heartRateEntries = sortedData.mapIndexed { index, data ->
                            Entry(index.toFloat(), data.heartRate.toFloat())
                        }

                        // Create and style dataset
                        val heartRateDataSet = LineDataSet(heartRateEntries, "Heart Rate (BPM)").apply {
                            color = AndroidColor.RED
                            lineWidth = 2.5f
                            setDrawCircles(true)
                            setCircleColor(AndroidColor.RED)
                            circleRadius = 4f
                            setDrawValues(false)
                            mode = LineDataSet.Mode.CUBIC_BEZIER
                            cubicIntensity = 0.2f
                            setDrawFilled(true)
                            fillColor = AndroidColor.RED
                            fillAlpha = 50
                        }

                        val lineData = LineData(heartRateDataSet)
                        chart.data = lineData
                        chart.invalidate()
                    }
                )
            }
        }

        // Blood Oxygen Chart
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(top = 12.dp, bottom = 8.dp)) {
                Text(
                    text = "Blood Oxygen",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
                
                AndroidView(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .padding(8.dp),
                    factory = { context ->
                        LineChart(context).apply {
                            description.isEnabled = false
                            legend.isEnabled = false
                            setTouchEnabled(true)
                            setScaleEnabled(false)
                            isDragEnabled = true
                            setPinchZoom(false)
                            setDrawGridBackground(false)

                            // Configure X-axis
                            xAxis.apply {
                                position = XAxis.XAxisPosition.BOTTOM
                                granularity = 1f
                                setDrawGridLines(false)
                                valueFormatter = IndexAxisValueFormatter(xAxisLabels)
                                setTextColor(textColor)
                            }

                            // Configure Y-axis
                            axisLeft.apply {
                                setDrawGridLines(true)
                                setTextColor(textColor)
                                axisMinimum = 90f // Start from 90% for SPO2
                                axisMaximum = 100f // Max 100%
                            }

                            axisRight.isEnabled = false

                            // Add empty space to left and right
                            extraLeftOffset = 10f
                            extraRightOffset = 10f
                        }
                    },
                    update = { chart ->
                        // Blood oxygen data
                        val spo2Entries = sortedData.mapIndexed { index, data ->
                            Entry(index.toFloat(), data.bloodOxygen.toFloat())
                        }

                        // Create and style dataset
                        val spo2DataSet = LineDataSet(spo2Entries, "Blood Oxygen (%)").apply {
                            color = AndroidColor.BLUE
                            lineWidth = 2.5f
                            setDrawCircles(true)
                            setCircleColor(AndroidColor.BLUE)
                            circleRadius = 4f
                            setDrawValues(false)
                            mode = LineDataSet.Mode.CUBIC_BEZIER
                            cubicIntensity = 0.2f
                            setDrawFilled(true)
                            fillColor = AndroidColor.BLUE
                            fillAlpha = 50
                        }

                        val lineData = LineData(spo2DataSet)
                        chart.data = lineData
                        chart.invalidate()
                    }
                )
            }
        }

        // Respiratory Rate Chart
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(top = 12.dp, bottom = 8.dp)) {
                Text(
                    text = "Respiratory Rate",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
                
                AndroidView(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .padding(8.dp),
                    factory = { context ->
                        LineChart(context).apply {
                            description.isEnabled = false
                            legend.isEnabled = false
                            setTouchEnabled(true)
                            setScaleEnabled(false)
                            isDragEnabled = true
                            setPinchZoom(false)
                            setDrawGridBackground(false)

                            // Configure X-axis
                            xAxis.apply {
                                position = XAxis.XAxisPosition.BOTTOM
                                granularity = 1f
                                setDrawGridLines(false)
                                valueFormatter = IndexAxisValueFormatter(xAxisLabels)
                                setTextColor(textColor)
                            }

                            // Configure Y-axis
                            axisLeft.apply {
                                setDrawGridLines(true)
                                setTextColor(textColor)
                            }

                            axisRight.isEnabled = false

                            // Add empty space to left and right
                            extraLeftOffset = 10f
                            extraRightOffset = 10f
                        }
                    },
                    update = { chart ->
                        // Respiratory rate data
                        val respRateEntries = sortedData.mapIndexed { index, data ->
                            Entry(index.toFloat(), data.respiratoryRate.toFloat())
                        }

                        // Create and style dataset
                        val respRateDataSet = LineDataSet(respRateEntries, "Respiratory Rate").apply {
                            color = AndroidColor.GREEN
                            lineWidth = 2.5f
                            setDrawCircles(true)
                            setCircleColor(AndroidColor.GREEN)
                            circleRadius = 4f
                            setDrawValues(false)
                            mode = LineDataSet.Mode.CUBIC_BEZIER
                            cubicIntensity = 0.2f
                            setDrawFilled(true)
                            fillColor = AndroidColor.GREEN
//                            fillAlpha = the50
                        }

                        val lineData = LineData(respRateDataSet)
                        chart.data = lineData
                        chart.invalidate()
                    }
                )
            }
        }
    }
}

@Composable
fun HealthHistoryChart(
    healthData: List<HealthData>,
    modifier: Modifier = Modifier
) {
    HealthHistoryCharts(healthData, modifier)
}