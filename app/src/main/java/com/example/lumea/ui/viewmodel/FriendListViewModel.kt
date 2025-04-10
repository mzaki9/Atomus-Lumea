package com.example.lumea.ui.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.lumea.data.api.ConnectionApi
import com.example.lumea.data.api.NetworkModule
import com.example.lumea.data.auth.AuthRepository
import com.example.lumea.data.auth.TokenManager
import com.example.lumea.data.model.Connection
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class FriendListViewModel(
    private val connectionApi: ConnectionApi,
    private val tokenManager: TokenManager,
    private val authRepository: AuthRepository
) : ViewModel() {
    companion object {
        private const val TAG = "FriendListViewModel"
    }

    data class Friend(val id: String, val name: String)

    private val _friends = MutableStateFlow<List<Friend>>(emptyList())
    val friends: StateFlow<List<Friend>> = _friends

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun fetchFriendList() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                val token = tokenManager.accessTokenFlow.first()
                if (token.isNullOrEmpty()) {
                    _error.value = "Authentication token is missing"
                    return@launch
                }

                val userId = authRepository.currentUserId
                if (userId == null) {
                    _error.value = "User ID not available. Please login again."
                    return@launch
                }

                Log.d(TAG, "Fetching connections for user: $userId")
                val response = connectionApi.getConnections("Bearer $token", userId)

                if (response.isSuccessful) {
                    response.body()?.let { connectionsResponse ->
                        // Based on the API's new response format, data is in the "data" field
                        val connections = connectionsResponse.data ?: emptyList()

                        if (connections.isEmpty()) {
                            Log.d(TAG, "No connections found for user: $userId")
                            _friends.value = emptyList()
                            return@launch
                        }

                        // Convert connections to friends list using the friendName field
                        val friendsList = connections.map { connection ->
                            Friend(
                                id = connection.friendId.toString(),
                                name = connection.friendName ?: "Unknown User"
                            )
                        }

                        _friends.value = friendsList
                        Log.d(TAG, "Successfully loaded ${friendsList.size} connections")
                    } ?: run {
                        _error.value = "Empty response from server"
                    }
                } else {
                    _error.value = "Failed to fetch connections: ${response.code()}"
                    Log.e(TAG, "Error fetching connections: ${response.code()} ${response.message()}")
                }
            } catch (e: Exception) {
                _error.value = "Error: ${e.message}"
                Log.e(TAG, "Exception when fetching connections", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(FriendListViewModel::class.java)) {
                val tokenManager = TokenManager.getInstance(context)
                val authRepository = AuthRepository.getInstance(context)
                return FriendListViewModel(
                    NetworkModule.connectionApi,
                    tokenManager,
                    authRepository
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}