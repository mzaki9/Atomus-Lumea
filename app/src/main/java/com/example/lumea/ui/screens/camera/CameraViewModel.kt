package com.example.lumea.ui.screens.camera

import HealthScorePredictor
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.util.Log
import androidx.camera.core.Preview
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.lumea.data.api.LocationApi
import com.example.lumea.data.api.NetworkModule
import com.example.lumea.data.auth.TokenManager
import com.example.lumea.data.model.Location
import com.example.lumea.data.repository.PpgRepository
import com.example.lumea.data.sensor.CameraManager
import com.example.lumea.data.sensor.CameraManager.CameraState
import com.example.lumea.domain.HeartRateCalculator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class CameraViewModel(
    private val ppgRepository: PpgRepository,
    private val cameraManager: CameraManager,
    private val healthRiskPredictor: HealthScorePredictor,
    private val context: Context,
    private val locationApi: LocationApi = NetworkModule.locationApi,
    private val tokenManager: TokenManager
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

    // Flag to track if we already sent location data for this measurement
    private var locationSent = false

    // Risk prediction data
    private val _riskPrediction = MutableStateFlow<FloatArray?>(null)
    val riskPrediction: StateFlow<FloatArray?> = _riskPrediction.asStateFlow()
    private val _riskClass = MutableStateFlow<Int?>(null)
    val riskClass: StateFlow<Int?> = _riskClass.asStateFlow()

    // Camera state
    val cameraState = cameraManager.cameraState
    val preview = cameraManager.previewUseCase

    // Update the init block where you collect heart rate results
    init {
        viewModelScope.launch {
            ppgRepository.heartRateResult.collectLatest {
                it?.let {
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

        // Monitor camera state to detect when measurement is complete
        viewModelScope.launch {
            cameraManager.cameraState.collectLatest { state ->
                // Send location when measurement transitions from Measuring to Idle (completed)
                if (state == CameraState.Idle && !locationSent && _heartRate.value > 0) {
                    Log.d("Location", "Measurement complete, sending location data")
                    sendLocationAfterScan()
                    locationSent = true
                } else if (state == CameraState.Measuring) {
                    // Reset the flag when a new measurement starts
                    locationSent = false
                }
            }
        }
    }

    fun startMeasurement(lifecycleOwner: LifecycleOwner) {
        locationSent = false
        cameraManager.startPpgMeasurement(lifecycleOwner)
    }

    fun stopMeasurement() {
        cameraManager.stopPpgMeasurement()

        // If we have valid heart rate data and haven't sent location yet, do it now
        if (_heartRate.value > 0 && !locationSent) {
            Log.d("Location", "Measurement manually stopped, sending location data")
            sendLocationAfterScan()
            locationSent = true
        }
    }

    override fun onCleared() {
        super.onCleared()
        ppgRepository.shutdownCamera()
        healthRiskPredictor.close() // Tutup HealthRiskPredictor saat ViewModel dibersihkan
    }

    // Add this function to send location after measurement
    private fun sendLocationAfterScan() {
        viewModelScope.launch {
            try {
                // Only proceed if we have valid heart rate data
                if (_heartRate.value > 0) {
                    val currentLocation = getCurrentLocation()
                    if (currentLocation != null) {
                        // Get auth token
                        val token = tokenManager.getAccessToken()
                        if (token != null) {
                            // Send the location data
                            val authHeader = "Bearer $token"
                            val response = locationApi.sendLocation(
                                authHeader,
                                currentLocation
                            )

                            if (response.isSuccessful) {
                                Log.d("Location", "Successfully sent location data")
                            } else {
                                Log.e("Location", "Failed to send location: ${response.code()}")
                            }
                        }
                    }
                } else {
                    Log.d("Location", "Skipping location send - invalid heart rate")
                }
            } catch (e: Exception) {
                Log.e("Location", "Error sending location: ${e.message}", e)
            }
        }
    }

    // Helper function to get current location
    private fun getCurrentLocation(): Location? {
        // Check for permissions
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e("Location", "Location permissions not granted")
            return null
        }

        try {
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                ?: locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)

            return location?.let {
                Location(
                    latitude = it.latitude,
                    longitude = it.longitude
                )
            }
        } catch (e: Exception) {
            Log.e("Location", "Error getting location: ${e.message}", e)
            return null
        }
    }

    class Factory(private val applicationContext: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(CameraViewModel::class.java)) {
                val cameraManager = CameraManager(applicationContext)
                val heartRateCalculator = HeartRateCalculator()
                val healthRiskPredictor = HealthScorePredictor(applicationContext, "health_model.tflite")
                val ppgRepository = PpgRepository(cameraManager, heartRateCalculator)
                val tokenManager = TokenManager.getInstance(applicationContext)

                return CameraViewModel(
                    ppgRepository = ppgRepository,
                    cameraManager = cameraManager,
                    healthRiskPredictor = healthRiskPredictor,
                    context = applicationContext,
                    tokenManager = tokenManager
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}