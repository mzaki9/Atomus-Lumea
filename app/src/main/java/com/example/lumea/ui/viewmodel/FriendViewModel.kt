package com.example.lumea.ui.viewmodel

import android.content.Context
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import com.example.lumea.data.model.User
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import com.example.lumea.data.api.ConnectionApi
import com.example.lumea.data.api.NetworkModule
import com.example.lumea.data.model.SearchRequest
import com.example.lumea.data.model.SearchResponse
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.lumea.data.auth.TokenManager
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException


class FriendViewModel(
    private val connectionApi: ConnectionApi = NetworkModule.connectionApi,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _searchResults = MutableLiveData<List<User>>()
    var searchResults: LiveData<List<User>> = _searchResults

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    private val _searchQuery = MutableLiveData<String>()
    val searchQuery: LiveData<String> = _searchQuery

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun searchUsers(name: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                val authToken = tokenManager.getAccessToken()
                val response:SearchResponse = connectionApi.searchUsers(
                    authHeader = "Bearer $authToken",
                    request = SearchRequest(name = name)
                )
                _searchResults.value = response.data // Access the data field from response
            } catch (e: HttpException) {
                _error.value = "Network error: ${e.message}"
            } catch (e: IOException) {
                _error.value = "Connection error: ${e.message}"
            } catch (e: Exception) {
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

    var friendRequests by mutableStateOf(
        listOf(
            User("1", "HUGO SABAM AUGUSTO"),
            User("2", "HUGO SABAM AUGUSTO"),
            User("3", "HUGO SABAM AUGUSTO")
        )
    )
        private set
    fun sendFriendRequest(userId: String) {
        // Simulasi kirim permintaan, nanti bisa kirim ke backend
//        searchResults = searchResults.filterNot { it.id == userId }
    }

    fun acceptRequest(userId: String) {
        friendRequests = friendRequests.filterNot { it.id == userId }
    }

    fun rejectRequest(userId: String) {
        friendRequests = friendRequests.filterNot { it.id == userId }
    }




    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(FriendViewModel::class.java)) {
                return FriendViewModel(
                    connectionApi = NetworkModule.connectionApi,
                    tokenManager = TokenManager.getInstance(context)
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
