package com.example.lumea.domain

import com.example.lumea.domain.model.HeartRateResult
import com.example.lumea.domain.model.PpgReading
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

class HeartRateCalculator {

    companion object {
        private const val SPO2_CALIBRATION_SCALE = 1.1f
        private const val SPO2_CALIBRATION_OFFSET = 5f
        private const val MIN_READINGS = 50 
    }

    fun calculateHeartRate(readings: List<PpgReading>): HeartRateResult? {
        if (readings.size < MIN_READINGS) {
            return null // Not enough data
        }

        // For PPG, the green channel is typically most sensitive to blood volume changes
        val greenValues = readings.map { it.greenMean }

        // Apply a simple smoothing filter
        val smoothedValues = applyMovingAverageFilter(greenValues)

        // Detect peaks 
        val peaks = detectPeaks(smoothedValues)

        if (peaks.size < 2) {
            return null // Not enough peaks detected
        }

        // Calculate time intervals between peaks in milliseconds
        val intervals = mutableListOf<Long>()
        for (i in 1 until peaks.size) {
            val timeDiff = readings[peaks[i]].timestamp - readings[peaks[i-1]].timestamp
            intervals.add(timeDiff)
        }

        // Remove outliers (intervals that are too short or too long)
        val filteredIntervals = filterOutliers(intervals)

        if (filteredIntervals.isEmpty()) {
            return null // No valid intervals after filtering
        }

        // Calculate average interval
        val avgInterval = filteredIntervals.average()

        // Calculate heart rate: 60,000 ms (1 minute) / average interval between beats
        val heartRate = if (avgInterval > 0) (60000 / avgInterval).toInt() else 0

        // Validate heart rate is in a reasonable range
        if (heartRate < 40 || heartRate > 200) {
            return null // Heart rate outside physiological range
        }

        // Calculate confidence based on consistency of intervals
        val stdDev = calculateStandardDeviation(filteredIntervals)
        val coefficientOfVariation = stdDev / avgInterval

        // Convert to confidence score (0-1) - lower variation means higher confidence
        val confidence = maxOf(0f, 1f - (coefficientOfVariation * 5).toFloat())

        val respiratoryRate = calculateRespiratory(filteredIntervals)
        val spo2 = calculateSpO2(readings)

        return HeartRateResult(
            heartRate = heartRate,
            confidence = confidence,
            respiratoryRate = respiratoryRate,
            spo2 = spo2.coerceIn(0f, 100f),
            measurements = readings
        )
    }

    private fun applyMovingAverageFilter(values: List<Float>): List<Float> {
        val windowSize = 5 // Adjust based on your sampling rate
        val result = mutableListOf<Float>()

        for (i in values.indices) {
            var sum = 0f
            var count = 0

            // Calculate bounds for the moving window
            val start = maxOf(0, i - windowSize / 2)
            val end = minOf(values.lastIndex, i + windowSize / 2)

            // Sum values in the window
            for (j in start..end) {
                sum += values[j]
                count++
            }

            // Add average to result
            result.add(sum / count)
        }

        return result
    }

    private fun detectPeaks(values: List<Float>): List<Int> {
        val peaks = mutableListOf<Int>()

        // Need at least 3 points to find a peak
        if (values.size < 3) return peaks

        // Skip first and last points
        for (i in 1 until values.size - 1) {
            // A peak is higher than both its neighbors
            if (values[i] > values[i-1] && values[i] > values[i+1]) {
                peaks.add(i)
            }
        }

        return peaks
    }

    private fun filterOutliers(intervals: List<Long>): List<Long> {
        if (intervals.size <= 2) return intervals

        // Calculate median
        val sortedIntervals = intervals.sorted()
        val median = if (sortedIntervals.size % 2 == 0) {
            (sortedIntervals[sortedIntervals.size / 2] + sortedIntervals[sortedIntervals.size / 2 - 1]) / 2
        } else {
            sortedIntervals[sortedIntervals.size / 2]
        }

        // Filter out intervals that are too different from the median
        // For PPG, we might allow 30-50% deviation from the median
        val maxDeviation = median * 0.4 // 40% deviation allowed

        return intervals.filter { abs(it - median) <= maxDeviation }
    }

    private fun calculateStandardDeviation(values: List<Long>): Double {
        if (values.isEmpty()) return 0.0

        val mean = values.average()
        val variance = values.map { (it - mean).pow(2.0) }.average()
        return sqrt(variance)
    }


      private fun calculateRespiratory(intervals: List<Long>): Float {
        if (intervals.size < 2) return 0f
        
        val avgInterval = intervals.average().toFloat()
        
        return (60000 / (avgInterval * 4)).coerceIn(12f, 20f)
    }

    private fun calculateSpO2(readings: List<PpgReading>): Float {
        // SpO2 calculation using red and infrared light ratio
        // This is a simplified approximation - actual SpO2 needs calibration
        
        val redAC = calculateAC(readings.map { it.redMean })
        val redDC = calculateDC(readings.map { it.redMean })
        val irAC = calculateAC(readings.map { it.intensity })
        val irDC = calculateDC(readings.map { it.intensity })
        
        if (irDC == 0f || redDC == 0f) return 0f
        
        val R = (redAC / redDC) / (irAC / irDC)
        
        // Empirical formula (needs calibration for accuracy)
        val rawSpO2 = 110f - (25f * R)
        
        // Apply calibration formula: (raw * scale) + offset
        return (rawSpO2 * SPO2_CALIBRATION_SCALE + SPO2_CALIBRATION_OFFSET).coerceIn(0f, 100f)
    }

    private fun calculateAC(values: List<Float>): Float {
        if (values.isEmpty()) return 0f
        val mean = values.average().toFloat()
        return sqrt(values.map { (it - mean).pow(2) }.average().toFloat())
    }

    private fun calculateDC(values: List<Float>): Float {
        return if (values.isEmpty()) 0f else values.average().toFloat()
    }

}