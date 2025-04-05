package com.example.lumea.domain.model

data class PpgReading(
    val timestamp: Long,
    val redMean: Float,
    val greenMean: Float,
    val blueMean: Float,
    val intensity: Float
)

data class HeartRateResult(
    val heartRate: Int,
    val confidence: Float,
    val measurements: List<PpgReading>
)