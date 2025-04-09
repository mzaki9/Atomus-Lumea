package com.example.lumea.data.auth

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import android.content.Context
import android.util.Log
import com.example.lumea.data.api.NetworkModule
import com.example.lumea.data.model.LoginRequest
import com.example.lumea.data.model.RegisterRequest
import com.example.lumea.data.model.RefreshTokenRequest
import com.example.lumea.data.model.VerifyTokenResponse


class AuthRepository private constructor(
    private val tokenManager: TokenManager,
    private val context: Context
) {
    private val authApi = NetworkModule.authApi

    // Auth state untuk tracking status
    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    // Menyimpan ID pengguna yang sedang login
    var currentUserId: Int? = null
        private set

    suspend fun login(email: String, password: String): Result<Boolean> {
        return try {
            _authState.value = AuthState.Loading

            val response = authApi.login(LoginRequest(email, password))
            if (response.isSuccessful) {
                response.body()?.let { authResponse ->
                    tokenManager.saveTokens(authResponse.accessToken, authResponse.refreshToken)
                    _authState.value = AuthState.Authenticated
                    return Result.success(true)
                }
                _authState.value = AuthState.Error("Empty response body")
                Result.failure(Exception("Empty response body"))
            } else {
                _authState.value = AuthState.Error("Login failed: ${response.code()} ${response.message()}")
                Result.failure(Exception("Login failed: ${response.code()} ${response.message()}"))
            }
        } catch (e: Exception) {
            _authState.value = AuthState.Error(e.message ?: "Unknown error")
            Result.failure(e)
        }
    }

    suspend fun register(email: String, password: String, name: String, age: Int): Result<Boolean> {
        Log.d(TAG, "Register started")
        return try {
            _authState.value = AuthState.Loading

            val response = authApi.register(RegisterRequest(name, email, password, age))

            if (response.isSuccessful) {
                Log.d(TAG, "Register successful")
                _authState.value = AuthState.NotAuthenticated // Belum login otomatis
                Result.success(true)
            } else {
                val errorMessage = "Register failed: ${response.code()} ${response.message()}"
                Log.e(TAG, errorMessage)
                _authState.value = AuthState.Error(errorMessage)
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Register error: ${e.message}", e)
            _authState.value = AuthState.Error(e.message ?: "Unknown error during registration")
            Result.failure(e)
        }
    }


    suspend fun logout(): Result<Boolean> {
        Log.d(TAG, "Logout started")
        return try {
            _authState.value = AuthState.Loading

            tokenManager.clearTokens()
            currentUserId = null // Reset ID pengguna saat logout

            delay(300)

            _authState.value = AuthState.NotAuthenticated
            Log.d(TAG, "Logout complete, authState = ${_authState.value}")
            Result.success(true)
        } catch (e: Exception) {
            Log.e(TAG, "Error during logout: ${e.message}")
            try {
                tokenManager.clearTokens()
            } catch (clearError: Exception) {
                Log.e(TAG, "Error clearing tokens: ${clearError.message}")
            }
            _authState.value = AuthState.NotAuthenticated
            Result.success(true)
        }
    }

    suspend fun isLoggedIn(): Boolean {
        val token = tokenManager.getAccessToken()
        val isLoggedIn = token != null

        if (isLoggedIn) {
            val verifyResult = verifyToken()
            if (verifyResult.isSuccess) {
                _authState.value = AuthState.Authenticated
                return true
            } else {
                _authState.value = AuthState.NotAuthenticated
                return false
            }
        } else {
            _authState.value = AuthState.NotAuthenticated
            return false
        }
    }

    suspend fun verifyToken(): Result<VerifyTokenResponse> {
        Log.d(TAG, "Verifying token")
        return try {
            val token = tokenManager.getAccessToken()
                ?: return Result.failure(Exception("No token available"))

            val authHeader = "Bearer $token"
            val response = authApi.verifyToken(authHeader)

            if (response.isSuccessful) {
                Log.d(TAG, "Token verification successful")
                _authState.value = AuthState.Authenticated
                response.body()?.let { verifyTokenResponse ->
                    currentUserId = verifyTokenResponse.user.id
                    return Result.success(verifyTokenResponse)
                } ?: run {
                    return Result.failure(Exception("Empty response body"))
                }
            } else {
                val errorMessage = "Token verification failed: ${response.code()} ${response.message()}"
                Log.d(TAG, errorMessage)

                if (response.code() == 401) {
                    _authState.value = AuthState.Error("Token expired")
                    return Result.failure(Exception("Token expired"))
                } else {
                    _authState.value = AuthState.Error(errorMessage)
                    return Result.failure(Exception(errorMessage))
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error verifying token: ${e.message}", e)
            _authState.value = AuthState.Error(e.message ?: "Unknown error during token verification")
            return Result.failure(e)
        }
    }

    suspend fun refreshToken(): Result<Boolean> {
        Log.d(TAG, "Refreshing token")
        return try {
            val refreshToken = tokenManager.getRefreshToken()
                ?: return Result.failure(Exception("No refresh token available"))

            val request = RefreshTokenRequest(refreshToken)
            val response = authApi.refreshToken(request)

            if (response.isSuccessful) {
                response.body()?.let { refreshResponse ->
                    tokenManager.saveTokens(refreshResponse.accessToken, refreshResponse.refreshToken)
                    Log.d(TAG, "Token refresh successful")
                    _authState.value = AuthState.Authenticated
                    return Result.success(true)
                }

                val errorMessage = "Empty response body during token refresh"
                Log.e(TAG, errorMessage)
                _authState.value = AuthState.Error(errorMessage)
                Result.failure(Exception(errorMessage))
            } else {
                val errorMessage = "Token refresh failed: ${response.code()} ${response.message()}"
                Log.e(TAG, errorMessage)
                _authState.value = AuthState.Error(errorMessage)
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error refreshing token: ${e.message}", e)
            _authState.value = AuthState.Error(e.message ?: "Unknown error during token refresh")
            Result.failure(e)
        }
    }

    companion object {
        private const val TAG = "AuthRepository"

        @Volatile
        private var INSTANCE: AuthRepository? = null

        fun getInstance(context: Context): AuthRepository {
            return INSTANCE ?: synchronized(this) {
                val tokenManager = TokenManager.getInstance(context)
                INSTANCE ?: AuthRepository(tokenManager, context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}

sealed class AuthState {
    data object Idle : AuthState()
    data object Loading : AuthState()
    data object Authenticated : AuthState()
    data object NotAuthenticated : AuthState()
    data class Error(val message: String) : AuthState()
}