package com.example.lumea.data.repository

import com.example.lumea.data.sensor.CameraManager
import com.example.lumea.domain.HeartRateCalculator
import com.example.lumea.domain.model.HeartRateResult
import com.example.lumea.domain.model.PpgReading
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class PpgRepository(
    private val cameraManager: CameraManager,
    private val heartRateCalculator: HeartRateCalculator
) {
    private val scope = CoroutineScope(Dispatchers.Default)
    
    private val _heartRateResult = MutableStateFlow<HeartRateResult?>(null)
    val heartRateResult: StateFlow<HeartRateResult?> = _heartRateResult.asStateFlow()
    
    val cameraState: Flow<CameraManager.CameraState> = cameraManager.cameraState
    val previewUseCase = cameraManager.previewUseCase
    
    init {
        scope.launch {
            cameraManager.ppgReadings.collect { readings ->
                if (readings.size >= 50) {
                    calculateHeartRate(readings)
                }
            }
        }
    }
    
    private fun calculateHeartRate(readings: List<PpgReading>) {
        scope.launch {
            val result = heartRateCalculator.calculateHeartRate(readings)
            _heartRateResult.value = result
        }
    }
    
    fun shutdownCamera() {
        cameraManager.shutdown()
    }
}