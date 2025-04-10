package com.example.lumea.data.repository

import android.util.Log
import com.example.lumea.data.api.HealthApi
import com.example.lumea.data.api.NetworkModule
import com.example.lumea.data.auth.TokenManager
import com.example.lumea.data.model.HealthCheckInput
import com.example.lumea.data.model.HealthData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class HealthRepository(
    private val healthApi: HealthApi,
    private val tokenManager: TokenManager
) {
    companion object {
        private const val TAG = "HealthRepository"
    }
    
    private val _healthData = MutableStateFlow<HealthData?>(null)
    val healthData: StateFlow<HealthData?> = _healthData
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error
    
    suspend fun getHealthData(userId: Int): Result<HealthData?> {
        return try {
            _isLoading.value = true
            _error.value = null
            
            val token = tokenManager.getAccessToken() ?: return Result.failure(Exception("No authentication token available"))
            val response = healthApi.getHealthData("Bearer $token", userId)
            
            if (response.isSuccessful) {
                val healthResponse = response.body()
                if (healthResponse?.success == true) {
                    _healthData.value = healthResponse.data
                    Result.success(healthResponse.data)
                } else {
                    val errorMsg = healthResponse?.message ?: "Unknown error occurred"
                    _error.value = errorMsg
                    Result.failure(Exception(errorMsg))
                }
            } else {
                val errorMsg = "Failed to fetch health data: ${response.code()} ${response.message()}"
                _error.value = errorMsg
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error retrieving health data: ${e.message}", e)
            _error.value = e.message ?: "Unknown error occurred"
            Result.failure(e)
        } finally {
            _isLoading.value = false
        }
    }
    
    suspend fun saveHealthData(healthData: HealthCheckInput): Result<HealthData?> {
        return try {
            _isLoading.value = true
            _error.value = null
            
            val token = tokenManager.getAccessToken() ?: return Result.failure(Exception("No authentication token available"))
            val response = healthApi.createHealthData("Bearer $token", healthData)
            
            if (response.isSuccessful) {
                val healthResponse = response.body()
                if (healthResponse?.success == true) {
                    _healthData.value = healthResponse.data
                    Result.success(healthResponse.data)
                } else {
                    val errorMsg = healthResponse?.message ?: "Unknown error occurred"
                    _error.value = errorMsg
                    Result.failure(Exception(errorMsg))
                }
            } else {
                val errorMsg = "Failed to save health data: ${response.code()} ${response.message()}"
                _error.value = errorMsg
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error saving health data: ${e.message}", e)
            _error.value = e.message ?: "Unknown error occurred"
            Result.failure(e)
        } finally {
            _isLoading.value = false
        }
    }
    
    suspend fun updateHealthData(healthData: HealthCheckInput): Result<HealthData?> {
        return try {
            _isLoading.value = true
            _error.value = null
            
            val token = tokenManager.getAccessToken() ?: return Result.failure(Exception("No authentication token available"))
            val response = healthApi.updateHealthData("Bearer $token", healthData)
            
            if (response.isSuccessful) {
                val healthResponse = response.body()
                if (healthResponse?.success == true) {
                    _healthData.value = healthResponse.data
                    Result.success(healthResponse.data)
                } else {
                    val errorMsg = healthResponse?.message ?: "Unknown error occurred"
                    _error.value = errorMsg
                    Result.failure(Exception(errorMsg))
                }
            } else {
                val errorMsg = "Failed to update health data: ${response.code()} ${response.message()}"
                _error.value = errorMsg
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating health data: ${e.message}", e)
            _error.value = e.message ?: "Unknown error occurred"
            Result.failure(e)
        } finally {
            _isLoading.value = false
        }
    }
    
    suspend fun deleteHealthData(): Result<Boolean> {
        return try {
            _isLoading.value = true
            _error.value = null
            
            val token = tokenManager.getAccessToken() ?: return Result.failure(Exception("No authentication token available"))
            val response = healthApi.deleteHealthData("Bearer $token")
            
            if (response.isSuccessful) {
                val healthResponse = response.body()
                if (healthResponse?.success == true) {
                    _healthData.value = null
                    Result.success(true)
                } else {
                    val errorMsg = healthResponse?.message ?: "Unknown error occurred"
                    _error.value = errorMsg
                    Result.failure(Exception(errorMsg))
                }
            } else {
                val errorMsg = "Failed to delete health data: ${response.code()} ${response.message()}"
                _error.value = errorMsg
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting health data: ${e.message}", e)
            _error.value = e.message ?: "Unknown error occurred"
            Result.failure(e)
        } finally {
            _isLoading.value = false
        }
    }
}