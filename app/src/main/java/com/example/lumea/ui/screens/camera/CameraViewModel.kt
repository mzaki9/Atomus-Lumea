package com.example.lumea.ui.screens.camera

import android.content.Context
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
    private val cameraManager: CameraManager
) : ViewModel() {

    // Heart rate data
    private val _heartRate = MutableStateFlow(0)
    val heartRate: StateFlow<Int> = _heartRate.asStateFlow()

    private val _confidence = MutableStateFlow(0f)
    val confidence: StateFlow<Float> = _confidence.asStateFlow()

    // Camera state
    val cameraState = cameraManager.cameraState
    val preview = cameraManager.previewUseCase

    init {
        viewModelScope.launch {
            ppgRepository.heartRateResult.collectLatest { result ->
                result?.let {
                    _heartRate.value = it.heartRate
                    _confidence.value = it.confidence
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
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(CameraViewModel::class.java)) {
                val cameraManager = CameraManager(context)
                val heartRateCalculator = HeartRateCalculator()
                val ppgRepository = PpgRepository(cameraManager, heartRateCalculator)
                return CameraViewModel(ppgRepository, cameraManager) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}