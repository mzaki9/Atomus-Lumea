package com.example.lumea.ui.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.lumea.data.api.ConnectionApi
import com.example.lumea.data.api.NetworkModule
import com.example.lumea.data.auth.AuthRepository
import com.example.lumea.data.auth.TokenManager
import com.example.lumea.data.model.ConnectionRequest
import com.example.lumea.data.model.FriendRequest
import com.example.lumea.data.model.RespondRequestBody
import com.example.lumea.data.model.SearchRequest
import com.example.lumea.data.model.User
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class FriendViewModel(
    private val connectionApi: ConnectionApi = NetworkModule.connectionApi,
    private val tokenManager: TokenManager,
    private val authRepository: AuthRepository
) : ViewModel() {

    companion object {
        private const val TAG = "FriendViewModel"
    }

    private val _searchResults = MutableLiveData<List<User>>()
    val searchResults: LiveData<List<User>> = _searchResults

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _searchQuery = MutableLiveData<String>()
    val searchQuery: LiveData<String> = _searchQuery

    private val _friendRequests = MutableLiveData<List<FriendRequest>>(emptyList())
    val friendRequests: LiveData<List<FriendRequest>> = _friendRequests

    private val _requestSuccess = MutableLiveData<String?>()
    val requestSuccess: LiveData<String?> = _requestSuccess

    // Initialize and fetch pending requests
    init {
        fetchFriendRequests()
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun searchUsers(name: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                val authToken = tokenManager.getAccessToken()

                if (authToken.isNullOrEmpty()) {
                    _error.value = "Authentication token is missing"
                    return@launch
                }

                Log.d(TAG, "Searching for users with name: $name")

                val response = connectionApi.searchUsers(
                    authHeader = "Bearer $authToken",
                    request = SearchRequest(name = name)
                )

                _searchResults.value = response.data
                Log.d(TAG, "Found ${response.data.size} users matching the search")

            } catch (e: HttpException) {
                Log.e(TAG, "Network error searching users", e)
                _error.value = "Network error: ${e.message}"
            } catch (e: IOException) {
                Log.e(TAG, "Connection error searching users", e)
                _error.value = "Connection error: ${e.message}"
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error searching users", e)
                _error.value = "An unexpected error occurred: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearSearchResults() {
        _searchResults.value = emptyList()
        _error.value = null
    }

    fun resetError() {
        _error.value = null
    }

    fun resetSuccessMessage() {
        _requestSuccess.value = null
    }

    // Function to fetch pending friend requests
    fun fetchFriendRequests() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                val authToken = tokenManager.getAccessToken()
                if (authToken.isNullOrEmpty()) {
                    _error.value = "Authentication token is missing"
                    return@launch
                }

                Log.d(TAG, "Fetching pending friend requests")
                val response = connectionApi.getPendingRequests("Bearer $authToken")

                if (response.isSuccessful) {
                    response.body()?.let {
                        _friendRequests.value = it.data
                        Log.d(TAG, "Fetched ${it.data.size} pending requests")
                    } ?: run {
                        _friendRequests.value = emptyList()
                        Log.d(TAG, "No pending requests found (empty response)")
                    }
                } else {
                    _error.value = "Failed to fetch friend requests: ${response.code()}"
                    Log.e(TAG, "Failed to fetch friend requests: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching friend requests", e)
                _error.value = "Error fetching friend requests: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Send friend request
    fun sendFriendRequest(userId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                val authToken = tokenManager.getAccessToken()
                if (authToken.isNullOrEmpty()) {
                    _error.value = "Authentication token is missing"
                    return@launch
                }

                val currentUser = authRepository.currentUserId
                if (currentUser == null) {
                    _error.value = "User ID not available"
                    return@launch
                }

                Log.d(TAG, "Sending friend request to user: $userId")

                val request = ConnectionRequest(
                    senderId = currentUser,
                    receiverId = userId.toInt()
                )

                val response = connectionApi.sendConnectionRequest(
                    authHeader = "Bearer $authToken",
                    request = request
                )

                if (response.isSuccessful) {
                    _requestSuccess.value = "Friend request sent successfully"
                    // Remove user from search results to avoid duplicate requests
                    _searchResults.value = _searchResults.value?.filterNot { it.id == userId }
                    Log.d(TAG, "Friend request sent successfully")
                } else {
                    val errorBody = response.errorBody()?.string()
                    _error.value = "Failed to send friend request: ${response.code()} - $errorBody"
                    Log.e(TAG, "Failed to send friend request: ${response.code()} - $errorBody")
                }
            } catch (e: HttpException) {
                Log.e(TAG, "Network error sending friend request", e)
                _error.value = "Network error: ${e.message}"
            } catch (e: IOException) {
                Log.e(TAG, "Connection error sending friend request", e)
                _error.value = "Connection error: ${e.message}"
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error sending friend request", e)
                _error.value = "An unexpected error occurred: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun acceptRequest(requestId: Int) {
        respondToRequest(requestId, true)
    }

    // Reject friend request
    fun rejectRequest(requestId: Int) {
        respondToRequest(requestId, false)
    }

    // Common function for responding to requests
    private fun respondToRequest(requestId: Int, accept: Boolean) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                val authToken = tokenManager.getAccessToken()
                if (authToken.isNullOrEmpty()) {
                    _error.value = "Authentication token is missing"
                    return@launch
                }

                Log.d(TAG, "Responding to friend request $requestId (accept: $accept)")

                val response = connectionApi.respondToRequest(
                    authHeader = "Bearer $authToken",
                    request = RespondRequestBody(
                        requestId = requestId,
                        accept = accept
                    )
                )

                if (response.isSuccessful) {
                    val action = if (accept) "accepted" else "rejected"
                    _requestSuccess.value = "Friend request $action"
                    // Remove request from the list
                    _friendRequests.value = _friendRequests.value?.filterNot { it.requestId == requestId }
                    Log.d(TAG, "Friend request successfully $action")
                } else {
                    val errorBody = response.errorBody()?.string()
                    val action = if (accept) "accept" else "reject"
                    _error.value = "Failed to $action friend request: ${response.code()} - $errorBody"
                    Log.e(TAG, "Failed to $action friend request: ${response.code()} - $errorBody")
                }
            } catch (e: Exception) {
                val action = if (accept) "accepting" else "rejecting"
                Log.e(TAG, "Error $action friend request", e)
                _error.value = "Error $action friend request: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(FriendViewModel::class.java)) {
                val tokenManager = TokenManager.getInstance(context)
                val authRepository = AuthRepository.getInstance(context)
                return FriendViewModel(
                    connectionApi = NetworkModule.connectionApi,
                    tokenManager = tokenManager,
                    authRepository = authRepository
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}