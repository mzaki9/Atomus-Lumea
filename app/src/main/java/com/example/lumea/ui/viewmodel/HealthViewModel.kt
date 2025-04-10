package com.example.lumea.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.lumea.data.api.NetworkModule
import com.example.lumea.data.auth.TokenManager
import com.example.lumea.data.model.HealthCheckInput
import com.example.lumea.data.model.HealthData
import com.example.lumea.data.repository.HealthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HealthViewModel(
    private val healthRepository: HealthRepository
) : ViewModel() {
    
    val healthData: StateFlow<HealthData?> = healthRepository.healthData
    val isLoading: StateFlow<Boolean> = healthRepository.isLoading
    val error: StateFlow<String?> = healthRepository.error
    
    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()
    
    fun fetchHealthData(userId: Int) {
        viewModelScope.launch {
            val result = healthRepository.getHealthData(userId)
            if (result.isFailure) {
                _statusMessage.value = "Failed to load health data: ${result.exceptionOrNull()?.message}"
            }
        }
    }
    
    fun saveHealthData(heartRate: Int, bloodOxygen: Float, respiratoryRate: Float, status: String) {
        viewModelScope.launch {
            val healthData = HealthCheckInput(
                heartRate = heartRate,
                bloodOxygen = bloodOxygen,
                respiratoryRate = respiratoryRate,
                status = status
            )
            
            val result = healthRepository.saveHealthData(healthData)
            _statusMessage.value = if (result.isSuccess) {
                "Health data saved successfully"
            } else {
                "Failed to save health data: ${result.exceptionOrNull()?.message}"
            }
        }
    }
    
    fun updateHealthData(heartRate: Int, bloodOxygen: Float, respiratoryRate: Float, status: String) {
        viewModelScope.launch {
            val healthData = HealthCheckInput(
                heartRate = heartRate,
                bloodOxygen = bloodOxygen,
                respiratoryRate = respiratoryRate,
                status = status
            )
            
            val result = healthRepository.updateHealthData(healthData)
            _statusMessage.value = if (result.isSuccess) {
                "Health data updated successfully"
            } else {
                "Failed to update health data: ${result.exceptionOrNull()?.message}"
            }
        }
    }
    
    fun deleteHealthData() {
        viewModelScope.launch {
            val result = healthRepository.deleteHealthData()
            _statusMessage.value = if (result.isSuccess) {
                "Health data deleted successfully"
            } else {
                "Failed to delete health data: ${result.exceptionOrNull()?.message}"
            }
        }
    }
    
    fun clearStatusMessage() {
        _statusMessage.value = null
    }
    
    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(HealthViewModel::class.java)) {
                val tokenManager = TokenManager.getInstance(context)
                val healthRepository = HealthRepository(NetworkModule.healthApi, tokenManager)
                
                return HealthViewModel(healthRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}