package com.example.lumea.ui.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.lumea.data.api.NetworkModule
import com.example.lumea.data.auth.AuthRepository
import com.example.lumea.data.auth.TokenManager
import com.example.lumea.data.model.HealthData
import com.example.lumea.data.model.UserDataComplete
import com.example.lumea.data.repository.HealthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val tokenManager: TokenManager,
    private val authRepository: AuthRepository,
    private val healthRepository: HealthRepository = HealthRepository(NetworkModule.healthApi, tokenManager)
) : ViewModel() {
    private val TAG = "ProfileViewModel"
    
    private val _userData = MutableStateFlow<UserDataComplete?>(null)
    val userData: StateFlow<UserDataComplete?> = _userData
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _healthHistory = MutableStateFlow<List<HealthData>>(emptyList())
    val healthHistory: StateFlow<List<HealthData>> = _healthHistory

    private val _historyLoading = MutableStateFlow(false)
    val historyLoading: StateFlow<Boolean> = _historyLoading

    private val _historyError = MutableStateFlow<String?>(null)
    val historyError: StateFlow<String?> = _historyError
    
    init {
        fetchUserProfile()
    }
    fun fetchHealthHistory() {
        viewModelScope.launch {
            try {
                _historyLoading.value = true
                _historyError.value = null

                val userId = authRepository.currentUserId
                if (userId == null) {
                    _historyError.value = "User ID not available. Please login again."
                    return@launch
                }

                Log.d(TAG, "Fetching health history for user: $userId")
                val result = healthRepository.getHealthHistory(userId)

                if (result.isSuccess) {
                    val history = result.getOrNull() ?: emptyList()
                    _healthHistory.value = history
                    Log.d(TAG, "Successfully retrieved ${history.size} health records")
                } else {
                    val exception = result.exceptionOrNull()
                    _historyError.value = exception?.message ?: "Unknown error"
                    Log.e(TAG, "Failed to fetch health history", exception)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching health history", e)
                _historyError.value = "Error: ${e.message}"
            } finally {
                _historyLoading.value = false
            }
        }
    }

    fun fetchUserProfile() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                
                val token = tokenManager.getAccessToken()
                if (token.isNullOrEmpty()) {
                    _error.value = "Authentication token is missing"
                    return@launch
                }
                
                val userId = authRepository.currentUserId
                if (userId == null) {
                    _error.value = "User ID not available. Please login again."
                    return@launch
                }
                
                Log.d(TAG, "Fetching profile for user: $userId")
                val userApi = NetworkModule.provideUserApi()
                val response = userApi.getUserData("Bearer $token", userId.toString())
                
                if (response.isSuccessful) {
                    response.body()?.let { userResponse ->
                        if (userResponse.success) {
                            _userData.value = userResponse.data
                            Log.d(TAG, "Successfully retrieved user data")
                        } else {
                            _error.value = userResponse.message
                        }
                    } ?: run {
                        _error.value = "Empty response from server"
                    }
                } else {
                    _error.value = "Failed to fetch user data: ${response.code()}"
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching user profile", e)
                _error.value = "Error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
                val tokenManager = TokenManager.getInstance(context)
                val authRepository = AuthRepository.getInstance(context)
                val healthRepository = HealthRepository(NetworkModule.healthApi, tokenManager)
                return ProfileViewModel(tokenManager, authRepository, healthRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}