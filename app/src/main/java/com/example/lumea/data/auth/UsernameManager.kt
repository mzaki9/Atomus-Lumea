package com.example.lumea.data.auth

import android.content.Context
import android.util.Log
import com.example.lumea.data.api.NetworkModule
import com.example.lumea.data.api.UserApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class UsernameManager private constructor(
    private val userApi: UserApi,
    private val tokenManager: TokenManager,
    private val authRepository: AuthRepository
) {
    private val TAG = "UsernameManager"
    
    private val _username = MutableStateFlow("User")
    val username: StateFlow<String> = _username.asStateFlow()
    
    companion object {
        @Volatile
        private var INSTANCE: UsernameManager? = null
        
        fun getInstance(context: Context): UsernameManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: UsernameManager(
                    NetworkModule.provideUserApi(),
                    TokenManager.getInstance(context),
                    AuthRepository.getInstance(context)
                ).also { INSTANCE = it }
            }
        }
    }

    suspend fun fetchUsername() {
        try {
            val token = tokenManager.getAccessToken() ?: return
            val userId = authRepository.currentUserId ?: return

            // Convert userId to String if it's an Int
            val userIdString = userId.toString()

            Log.d(TAG, "Fetching username for user: $userIdString")
            val response = userApi.getUserData("Bearer $token", userIdString)

            if (response.isSuccessful) {
                response.body()?.data?.profile?.name?.let { name ->
                    _username.value = name
                    Log.d(TAG, "Username updated: $name")
                }
            } else {
                Log.e(TAG, "Failed to fetch username: ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching username: ${e.message}")
        }
    }
}