package com.example.lumea.ui.viewmodel

import android.content.Context
import android.util.Log
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lumea.data.api.NetworkModule
import com.example.lumea.data.auth.AuthRepository
import com.example.lumea.data.auth.TokenManager
import com.example.lumea.data.model.HealthData
import com.example.lumea.data.repository.HealthRepository
import com.example.lumea.ui.screens.camera.CameraViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(
    private val cameraViewModel: CameraViewModel,
    private val healthRepository: HealthRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val TAG = "HomeViewModel"



    // Status message for UI feedback
    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    // Latest health data from API
    private val _latestHealthData = MutableStateFlow<HealthData?>(null)
    val latestHealthData: StateFlow<HealthData?> = _latestHealthData.asStateFlow()

    // Flag to track if data has been fetched
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()


    init {
        // Fetch data when ViewModel is created
        fetchLatestHealthData()

        // Observe health repository data
        viewModelScope.launch {
            healthRepository.healthData.collect { data ->
                _latestHealthData.value = data
                if (data != null) {
                    cameraViewModel.updateHealthValues(
                        heartRate = data.heartRate,
                        spo2 = data.bloodOxygen,
                        respiratoryRate = data.respiratoryRate,
                        status = data.status
                    )
                }
            }
        }
    }

    fun fetchLatestHealthData() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _statusMessage.value = null

                // Get current user ID from AuthRepository
                val userId = authRepository.currentUserId
                if (userId == null) {
                    Log.d(TAG, "No user ID available, user might not be logged in")
                    _statusMessage.value = "Please log in to view your health data"
                    return@launch
                }

                // Fetch health data for the current user
                Log.d(TAG, "Fetching health data for user ID: $userId")
                val result = healthRepository.getHealthData(userId)

                if (result.isSuccess) {
                    val data = result.getOrNull()
                    if (data != null) {
                        Log.d(TAG, "Successfully retrieved health data: ${data.heartRate} BPM, ${data.status}")
                        // Health data will automatically update via the collection
                    } else {
                        Log.d(TAG, "No health data available for this user")
                        _statusMessage.value = "No health data available yet. Try taking a measurement!"
                    }
                } else {
                    val exception = result.exceptionOrNull()
                    Log.e(TAG, "Failed to fetch health data: ${exception?.message}", exception)
                    if (exception?.message?.contains("404") == true) {
                        _statusMessage.value = "No health data recorded yet. Take your first measurement!"
                    } else {
                        _statusMessage.value = "Could not load your health data: ${exception?.message}"
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error in fetchLatestHealthData: ${e.message}", e)
                _statusMessage.value = "An error occurred: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearStatusMessage() {
        _statusMessage.value = null
    }

    fun clearLatestHealthData() {
        _latestHealthData.value = null
        _statusMessage.value = null
    }

    class Factory(
        private val context: Context,
        private val cameraViewModel: CameraViewModel
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
                val tokenManager = TokenManager.getInstance(context)
                val healthRepository = HealthRepository(
                    healthApi = NetworkModule.healthApi,
                    tokenManager = tokenManager
                )
                // Fixed: Passing context as the first parameter instead of tokenManager
                val authRepository = AuthRepository.getInstance(context)

                return HomeViewModel(cameraViewModel, healthRepository, authRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}