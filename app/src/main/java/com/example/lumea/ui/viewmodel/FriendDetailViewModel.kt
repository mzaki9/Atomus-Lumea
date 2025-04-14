package com.example.lumea.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.lumea.data.api.UserApi
import com.example.lumea.data.api.NetworkModule
import com.example.lumea.data.auth.TokenManager
import com.example.lumea.data.model.UserDataComplete
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class FriendDetailViewModel(private val userApi: UserApi, private val tokenManager: TokenManager) : ViewModel() {

    private val _friendData = MutableStateFlow<UserDataComplete?>(null)
    val friendData: StateFlow<UserDataComplete?> = _friendData

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun fetchFriendData(friendId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                val token = tokenManager.getAccessToken()
                if (token.isNullOrEmpty()) {
                    _error.value = "Authentication token is missing"
                    return@launch
                }

                val response = userApi.getUserData("Bearer $token", friendId)
                if (response.isSuccessful) {
                    response.body()?.let {
                        _friendData.value = it.data
                    } ?: run {
                        _error.value = "Empty response from server"
                    }
                } else {
                    _error.value = "Failed to fetch friend data: ${response.code()}"
                }
            } catch (e: Exception) {
                _error.value = "Error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun setError(errorMessage: String) {
        _error.value = errorMessage
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(FriendDetailViewModel::class.java)) {
                // Use public methods/properties from NetworkModule to get the UserApi
                val userApi = NetworkModule.provideUserApi()
                
                // Use public method to get TokenManager instance
                val tokenManager = TokenManager.getInstance(context)
                
                return FriendDetailViewModel(userApi, tokenManager) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}