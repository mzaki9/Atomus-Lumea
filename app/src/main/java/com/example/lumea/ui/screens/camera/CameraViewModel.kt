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
import com.example.lumea.data.api.UserApi
import com.example.lumea.data.auth.AuthRepository
import com.example.lumea.data.auth.TokenManager
import com.example.lumea.data.model.HealthCheckInput
import com.example.lumea.data.model.Location
import com.example.lumea.data.repository.HealthRepository
import com.example.lumea.data.repository.PpgRepository
import com.example.lumea.data.sensor.CameraManager
import com.example.lumea.data.sensor.CameraManager.CameraState
import com.example.lumea.domain.HeartRateCalculator
import kotlinx.coroutines.delay
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
    private val tokenManager: TokenManager,
    private val healthRepository: HealthRepository,
    private val userApi: UserApi = NetworkModule.provideUserApi()
) : ViewModel() {

    val cameraState: StateFlow<CameraManager.CameraState> = cameraManager.cameraState
    
    private val _preview = MutableStateFlow<Preview?>(null)
    val preview: StateFlow<Preview?> = _preview.asStateFlow()

    // Heart rate data
    private val _heartRate = MutableStateFlow(0)
    val heartRate: StateFlow<Int> = _heartRate.asStateFlow()

    private val _confidence = MutableStateFlow(0f)
    val confidence: StateFlow<Float> = _confidence.asStateFlow()

    private val _respiratoryRate = MutableStateFlow(0f)
    val respiratoryRate: StateFlow<Float> = _respiratoryRate.asStateFlow()

    private val _spo2 = MutableStateFlow(0f)
    val spo2: StateFlow<Float> = _spo2.asStateFlow()

    // Risk prediction results
    private val _riskClass = MutableStateFlow<Int?>(null)
    val riskClass: StateFlow<Int?> = _riskClass.asStateFlow()

    private val _riskPrediction = MutableStateFlow<FloatArray?>(null)
    val riskPrediction: StateFlow<FloatArray?> = _riskPrediction.asStateFlow()

    // Flag to track if we already sent location data for this measurement
    private var locationSent = false
    private var healthDataSent = false

    // Status messages
    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage

    private val _userName = MutableStateFlow("User")
    val userName: StateFlow<String> = _userName.asStateFlow()

    init {
            viewModelScope.launch {
            ppgRepository.heartRateResult.collectLatest { result ->
                result?.let { // Add null check here
                    _heartRate.value = result.heartRate
                    _confidence.value = result.confidence
                    _respiratoryRate.value = result.respiratoryRate
                    _spo2.value = result.spo2

                    // Predict health risk using the model
                    if (result.heartRate > 0) {
                        val prediction = healthRiskPredictor.predict(result)
                        prediction?.let { probabilities ->
                            // Find the class with highest probability
                            var maxIndex = 0
                            var maxProb = probabilities[0]
                            for (i in 1 until probabilities.size) {
                                if (probabilities[i] > maxProb) {
                                    maxProb = probabilities[i]
                                    maxIndex = i
                                }
                            }
                            _riskClass.value = maxIndex + 1 // Classes are 1-based
                            _riskPrediction.value = probabilities
                        }
                    }
                }
            }
        }

        viewModelScope.launch {
            cameraManager.cameraState.collectLatest { state ->
                if (state == CameraState.Idle && _heartRate.value > 0 && !locationSent) {
                    Log.d("Location", "Measurement complete, sending location data")
                    sendLocationAfterScan()
                    locationSent = true
                    
                    // Send health data after successful measurement
                    if (!healthDataSent && _heartRate.value > 0) {
                        sendHealthData()
                        healthDataSent = true
                    }
                } else if (state == CameraState.Measuring) {
                    // Reset the flags when a new measurement starts
                    locationSent = false
                    healthDataSent = false
                }
            }
        }
        Log.d("CameraViewModel", "Initializing CameraViewModel, about to fetch user name")
        fetchUserName()
    }

    fun startMeasurement(lifecycleOwner: LifecycleOwner) {
        locationSent = false
        healthDataSent = false
        cameraManager.startPpgMeasurement(lifecycleOwner)
        viewModelScope.launch {
            cameraManager.previewUseCase.collect { previewUseCase ->
                _preview.value = previewUseCase
            }
        }

    }

    // Add this function to CameraViewModel class
    fun resetHealthData() {
        _heartRate.value = 0
        _spo2.value = 0f
        _respiratoryRate.value = 0f
        _riskClass.value = null
        _riskPrediction.value = null
        _statusMessage.value = null
        locationSent = false
        healthDataSent = false
        Log.d("CameraViewModel", "Health data has been reset")
    }

    fun stopMeasurement() {
        cameraManager.stopPpgMeasurement()

        // If we have valid heart rate data and haven't sent location yet, do it now
        if (_heartRate.value > 0 && !locationSent) {
            Log.d("Location", "Measurement manually stopped, sending location data")
            sendLocationAfterScan()
            locationSent = true
        }
        
        // Send health data if not already sent
        if (_heartRate.value > 0 && !healthDataSent) {
            sendHealthData()
            healthDataSent = true
        }
    }

    // Add this function to your CameraViewModel class - place it anywhere inside the class
    fun updateHealthValues(heartRate: Int, spo2: Float, respiratoryRate: Float, status: String) {
        // Only update if we don't have active readings (avoid overwriting active measurements)
        if (cameraManager.cameraState.value == CameraState.Idle) {
            _heartRate.value = heartRate
            _spo2.value = spo2
            _respiratoryRate.value = respiratoryRate
            
            // Update risk class based on status
            _riskClass.value = when (status.lowercase()) {
                "tidak sehat" -> 1
                "kurang sehat" -> 2
                "cukup sehat" -> 3
                "sangat sehat" -> 4
                else -> null
            }
            
            Log.d("CameraViewModel", "Updated health values from remote data: HR=$heartRate, SPO2=$spo2, RR=$respiratoryRate")
        } else {
            Log.d("CameraViewModel", "Skipped updating health values - measurement in progress")
        }
    }
    
    // Add this function to send health data after measurement
    private fun sendHealthData() {
        viewModelScope.launch {
            try {
                // Only proceed if we have valid heart rate data
                if (_heartRate.value > 0) {
                    // Determine health status based on risk class
                    val healthStatus = when (_riskClass.value) {
                        1 -> "Tidak Sehat"
                        2 -> "Kurang Sehat"
                        3 -> "Cukup Sehat"
                        4 -> "Sangat Sehat"
                        else -> "Unknown"
                    }
                    
                    // Create health data input
                    val healthData = HealthCheckInput(
                        heartRate = _heartRate.value,
                        bloodOxygen = _spo2.value,
                        respiratoryRate = _respiratoryRate.value,
                        status = healthStatus
                    )
                    
                    // Send health data to server
                    val result = healthRepository.saveHealthData(healthData)
                    
                    if (result.isSuccess) {
                        _statusMessage.value = "Health data saved successfully"
                        Log.d("Health", "Successfully sent health data")
                    } else {
                        _statusMessage.value = "Failed to save health data"
                        Log.e("Health", "Failed to send health data: ${result.exceptionOrNull()?.message}")
                    }
                } else {
                    Log.d("Health", "Skipping health data send - invalid heart rate")
                }
            } catch (e: Exception) {
                _statusMessage.value = "Error: ${e.message}"
                Log.e("Health", "Error sending health data: ${e.message}", e)
            }
        }
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

    override fun onCleared() {
        super.onCleared()
        ppgRepository.shutdownCamera()
        healthRiskPredictor.close() // Close HealthRiskPredictor when ViewModel is cleared
    }

    private fun fetchUserName() {
        viewModelScope.launch {
            try {
                val token = tokenManager.getAccessToken()
                if (!token.isNullOrEmpty()) {
                    val authRepository = AuthRepository.getInstance(context)
                    var userId = authRepository.currentUserId

                    if (userId == null) {
                        Log.d("CameraViewModel", "User ID not available yet, will retry...")
                        repeat(3) { attempt ->
                            if (userId == null) {
                                delay(1000)
                                userId = authRepository.currentUserId
                                Log.d("CameraViewModel", "Retry attempt ${attempt+1}, userId: $userId")
                            }
                        }
                    }

                    userId?.let {
                        val response = userApi.getUserData("Bearer $token", it.toString())
                        if (response.isSuccessful) {
                            response.body()?.data?.profile?.name?.let { name ->
                                _userName.value = name
                                Log.d("CameraViewModel", "Fetched user name: $name")
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("CameraViewModel", "Error fetching user name: ${e.message}")
            }
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
                val healthRepository = HealthRepository(NetworkModule.healthApi, tokenManager)

                return CameraViewModel(
                    ppgRepository = ppgRepository,
                    cameraManager = cameraManager,
                    healthRiskPredictor = healthRiskPredictor,
                    context = applicationContext,
                    tokenManager = tokenManager,
                    healthRepository = healthRepository
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}