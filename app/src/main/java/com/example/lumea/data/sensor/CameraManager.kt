package com.example.lumea.data.sensor

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.example.lumea.domain.model.PpgReading
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraManager(
    private val context: Context
) {
    private val TAG = "CameraManager"
    
    private val cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()
    
    private var camera: Camera? = null
    private var preview: Preview? = null
    private var imageAnalysis: ImageAnalysis? = null
    
    private val _cameraState = MutableStateFlow<CameraState>(CameraState.Idle)
    val cameraState: StateFlow<CameraState> = _cameraState.asStateFlow()
    
    private val _previewUseCase = MutableStateFlow<Preview?>(null)
    val previewUseCase: StateFlow<Preview?> = _previewUseCase.asStateFlow()
    
    private val _ppgReadings = MutableStateFlow<List<PpgReading>>(emptyList())
    val ppgReadings: StateFlow<List<PpgReading>> = _ppgReadings.asStateFlow()


    //timer-related properties
    private val handler = Handler(Looper.getMainLooper())
    private var timerStarted = false
    private var timerSeconds = 0
    private val timerRunnable = object : Runnable {
    override fun run() {
        timerSeconds++
        Log.d("HeartRate", "Measurement in progress: $timerSeconds seconds")
        
        if (timerSeconds >= 30) {
            Log.d("HeartRate", "Measurement complete: 30 seconds reached")
            stopPpgMeasurement()
        } else {
            handler.postDelayed(this, 1000)
        }
    }
}
    
    fun startPpgMeasurement(lifecycleOwner: LifecycleOwner) {
        if (_cameraState.value == CameraState.Measuring) {
            return
        }
        
        _cameraState.value = CameraState.Initializing
        _ppgReadings.value = emptyList()
        
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            try {
                val cameraProvider = cameraProviderFuture.get()
                
                // Unbind any existing use cases
                cameraProvider.unbindAll()
                
                // Set up the preview use case
                preview = Preview.Builder().build()
                _previewUseCase.value = preview
                
                // Set up the image analysis use case
                imageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                
                imageAnalysis?.setAnalyzer(cameraExecutor, PpgAnalyzer { ppgReading ->
                    val currentReadings = _ppgReadings.value.toMutableList()
                    currentReadings.add(ppgReading)
                    // Limit size to avoid memory issues
                    if (currentReadings.size > MAX_READINGS) {
                        _ppgReadings.value = currentReadings.takeLast(MAX_READINGS)
                    } else {
                        _ppgReadings.value = currentReadings
                    }
                })
                
                // Select back camera as that's where the flash is
                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                
                // Bind use cases to camera
                camera = cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageAnalysis
                )
                
                // Enable the flash - crucial for PPG
                camera?.cameraControl?.enableTorch(true)

                _cameraState.value = CameraState.Measuring

                if (!timerStarted && _ppgReadings.value.isEmpty()) {
                    timerStarted = true
                    timerSeconds = 0
                    Log.d("HeartRate", "Starting heart rate measurement timer")
                    handler.post(timerRunnable)
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "Use case binding failed", e)
                _cameraState.value = CameraState.Error("Failed to start camera: ${e.message}")
            }
        }, ContextCompat.getMainExecutor(context))
    }
    
    fun stopPpgMeasurement() {
        // Disable the torch
        camera?.cameraControl?.enableTorch(false)
        
        // Get the camera provider and unbind all use cases
        ProcessCameraProvider.getInstance(context).get()?.unbindAll()
        
        camera = null
        preview = null
        imageAnalysis = null
        _previewUseCase.value = null
        
        _cameraState.value = CameraState.Idle

        if (timerStarted) {
            handler.removeCallbacks(timerRunnable)
            timerStarted = false
            Log.d("HeartRate", "Stopped heart rate measurement timer after $timerSeconds seconds")
        }
    }
    
    fun shutdown() {
        stopPpgMeasurement()
        cameraExecutor.shutdown()
    }
    
    companion object {
        private const val MAX_READINGS = 1800 
    }
    
    sealed class CameraState {
        object Idle : CameraState()
        object Initializing : CameraState()
        object Measuring : CameraState()
        data class Error(val message: String) : CameraState()
    }
}