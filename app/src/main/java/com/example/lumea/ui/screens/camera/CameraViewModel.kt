package com.example.lumea.ui.screens.camera

import HealthScorePredictor
import android.content.Context
import android.util.Log
import androidx.camera.core.Preview
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.lumea.data.repository.PpgRepository
import com.example.lumea.data.sensor.CameraManager
import com.example.lumea.domain.HeartRateCalculator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class CameraViewModel(
    private val ppgRepository: PpgRepository,
    private val cameraManager: CameraManager,
    private val healthRiskPredictor: HealthScorePredictor
) : ViewModel() {

    // Heart rate data
    private val _heartRate = MutableStateFlow(0)
    val heartRate: StateFlow<Int> = _heartRate.asStateFlow()

    private val _confidence = MutableStateFlow(0f)
    val confidence: StateFlow<Float> = _confidence.asStateFlow()

    private val _respiratoryRate = MutableStateFlow(0f)
    val respiratoryRate: StateFlow<Float> = _respiratoryRate.asStateFlow()

    private val _spo2 = MutableStateFlow(0f)
    val spo2: StateFlow<Float> = _spo2.asStateFlow()


    // Risk prediction data
    private val _riskPrediction = MutableStateFlow<FloatArray?>(null)
    val riskPrediction: StateFlow<FloatArray?> = _riskPrediction.asStateFlow()
    private val _riskClass = MutableStateFlow<Int?>(null)
    val riskClass: StateFlow<Int?> = _riskClass.asStateFlow()

    // Camera state
    val cameraState = cameraManager.cameraState
    val preview = cameraManager.previewUseCase

    init {
        viewModelScope.launch {
            ppgRepository.heartRateResult.collectLatest { result ->
                result?.let {
                    _heartRate.value = it.heartRate
                    _confidence.value = it.confidence
                    _respiratoryRate.value = it.respiratoryRate
                    _spo2.value = it.spo2
                    Log.d("RES: ", "$it")
                    Log.d("HeartRate", "Respiratory Rate: ${it.respiratoryRate}, SPO2: ${it.spo2}")

                    // Lakukan prediksi risiko setiap kali heartRateResult diperbarui
                    val risk = healthRiskPredictor.predict(it)
                    _riskPrediction.value = risk
                    Log.d("Predict: ", "${_riskPrediction.value}")

                    // Calculate argmax for risk prediction
                    risk?.let { predictions ->
                        val maxIndex = predictions.indices.maxByOrNull { i -> predictions[i] }
                        _riskClass.value = maxIndex
                        Log.d("Predict Class: ", "$_riskClass.value")
                    }
                }
            }
        }
    }

    fun startMeasurement(lifecycleOwner: LifecycleOwner) {
        cameraManager.startPpgMeasurement(lifecycleOwner)
    }

    fun stopMeasurement() {
        cameraManager.stopPpgMeasurement()
    }

    override fun onCleared() {
        super.onCleared()
        ppgRepository.shutdownCamera()
        healthRiskPredictor.close() // Tutup HealthRiskPredictor saat ViewModel dibersihkan
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(CameraViewModel::class.java)) {
                val cameraManager = CameraManager(context)
                val heartRateCalculator = HeartRateCalculator()
                val healthRiskPredictor = HealthScorePredictor(context, "health_model.tflite")
                val ppgRepository = PpgRepository(cameraManager, heartRateCalculator)
                return CameraViewModel(ppgRepository, cameraManager, healthRiskPredictor) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}