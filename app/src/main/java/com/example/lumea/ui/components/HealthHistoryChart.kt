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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Air
import androidx.compose.material.icons.rounded.Bloodtype
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Timeline
import androidx.compose.material3.Icon
import androidx.compose.ui.graphics.vector.ImageVector


@Composable
fun HealthHistoryCharts(
    healthData: List<HealthData>,
    modifier: Modifier = Modifier
) {
    if (healthData.isEmpty()) {
        EmptyHistoryPlaceholder(modifier)
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
    val textColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f).toArgb()
    val gridColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f).toArgb()
    val heartRateColor = Color(0xFFFF6B6B).toArgb()
    val bloodOxygenColor = Color(0xFF2196F3).toArgb()
    val respiratoryRateColor = Color(0xFF4CAF50).toArgb()

    Column(modifier = modifier.fillMaxWidth()) {
        // Heart Rate Chart
        ChartCard(
            title = "Heart Rate",
            entries = sortedData.mapIndexed { index, data ->
                Entry(index.toFloat(), data.heartRate.toFloat())
            },
            xAxisLabels = xAxisLabels,
            lineColor = heartRateColor,
            textColor = textColor,
            gridColor = gridColor,
            iconVector = Icons.Rounded.Favorite
        )
        
        // Blood Oxygen Chart
        ChartCard(
            title = "Blood Oxygen",
            entries = sortedData.mapIndexed { index, data ->
                Entry(index.toFloat(), data.bloodOxygen.toFloat())
            },
            xAxisLabels = xAxisLabels,
            lineColor = bloodOxygenColor,
            textColor = textColor,
            gridColor = gridColor,
            yAxisMinimum = 90f,
            yAxisMaximum = 100f,
            iconVector = Icons.Rounded.Bloodtype
        )
        
        // Respiratory Rate Chart
        ChartCard(
            title = "Respiratory Rate",
            entries = sortedData.mapIndexed { index, data ->
                Entry(index.toFloat(), data.respiratoryRate.toFloat())
            },
            xAxisLabels = xAxisLabels,
            lineColor = respiratoryRateColor,
            textColor = textColor,
            gridColor = gridColor,
            iconVector = Icons.Rounded.Air
        )
    }
}

@Composable
private fun ChartCard(
    title: String,
    entries: List<Entry>,
    xAxisLabels: List<String>,
    lineColor: Int,
    textColor: Int,
    gridColor: Int,
    yAxisMinimum: Float? = null,
    yAxisMaximum: Float? = null,
    iconVector: ImageVector
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(top = 16.dp, bottom = 12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Icon(
                    imageVector = iconVector,
                    contentDescription = null,
                    tint = Color(lineColor),
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
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
                        setNoDataText("No data available")
                        setNoDataTextColor(textColor)

                        // Configure X-axis
                        xAxis.apply {
                            position = XAxis.XAxisPosition.BOTTOM
                            granularity = 1f
                            setDrawGridLines(false)
                            valueFormatter = IndexAxisValueFormatter(xAxisLabels)
                            setTextColor(textColor)
                            textSize = 9f
                        }

                        // Configure Y-axis
                        axisLeft.apply {
                            setDrawGridLines(true)
                            this.gridColor = gridColor
                            setTextColor(textColor)
                            textSize = 9f
                            if (yAxisMinimum != null) {
                                this.axisMinimum = yAxisMinimum
                            }
                            if (yAxisMaximum != null) {
                                this.axisMaximum = yAxisMaximum
                            }
                        }

                        axisRight.isEnabled = false
                        extraLeftOffset = 10f
                        extraRightOffset = 10f
                        extraBottomOffset = 5f
                    }
                },
                update = { chart ->
                    // Create and style dataset
                    val dataSet = LineDataSet(entries, title).apply {
                        color = lineColor
                        lineWidth = 2f
                        setDrawCircles(true)
                        setCircleColor(lineColor)
                        circleRadius = 3f
                        setDrawValues(false)
                        mode = LineDataSet.Mode.CUBIC_BEZIER
                        cubicIntensity = 0.15f
                        setDrawFilled(true)
                        fillColor = lineColor
                        fillAlpha = 30
                        highLightColor = lineColor
                        setDrawHorizontalHighlightIndicator(false)
                    }

                    val lineData = LineData(dataSet)
                    chart.data = lineData
                    chart.animateX(500)
                    chart.invalidate()
                }
            )
        }
    }
}

@Composable
private fun EmptyHistoryPlaceholder(modifier: Modifier) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Rounded.Timeline,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "No health history data available yet",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
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