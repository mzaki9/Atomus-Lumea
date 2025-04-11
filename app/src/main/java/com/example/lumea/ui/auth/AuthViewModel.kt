package com.example.lumea.ui.auth

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.lumea.data.auth.AuthRepository
import com.example.lumea.data.auth.AuthState
import com.example.lumea.ui.screens.camera.CameraViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class AuthViewModel(private val authRepository: AuthRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Loading)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.authState.collectLatest { authState ->
                Log.d("AuthViewModel", "Auth state updated: $authState")
                when (authState) {
                    is AuthState.Authenticated -> _uiState.value = AuthUiState.Authenticated
                    is AuthState.NotAuthenticated -> _uiState.value = AuthUiState.NotAuthenticated
                    is AuthState.Error -> _uiState.value = AuthUiState.Error(authState.message)
                    is AuthState.Loading -> _uiState.value = AuthUiState.Loading
                    else -> {}
                }
            }
        }

        checkAuthState()
    }

    fun checkAuthState() {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            val isLoggedIn = authRepository.isLoggedIn()
            Log.d("AuthViewModel", "Is logged in: $isLoggedIn")
            _uiState.value = if (isLoggedIn) {
                AuthUiState.Authenticated
            } else {
                AuthUiState.NotAuthenticated
            }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            authRepository.login(email, password)
                .onSuccess {
                    _loginState.value = LoginState.Success
                }
                .onFailure { error ->
                    _loginState.value = LoginState.Error(error.message ?: "Login failed")
                }
        }
    }

    fun register(email: String, password: String, name: String, age: Int) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading

            authRepository.register(email, password, name, age)
                .onSuccess {
                    // Jika registrasi berhasil, langsung login
                    login(email, password)
                }
                .onFailure { error ->
                    _loginState.value = LoginState.Error(error.message ?: "Registration failed")
                }
        }
    }

    fun logout(cameraViewModel: CameraViewModel) {
        viewModelScope.launch {
            cameraViewModel.resetHealthData()
            authRepository.logout()
        }
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
                val authRepository = AuthRepository.getInstance(context)
                return AuthViewModel(authRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

sealed class AuthUiState {
    data object Loading : AuthUiState()
    data object Authenticated : AuthUiState()
    data object NotAuthenticated : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}

sealed class LoginState {
    data object Idle : LoginState()
    data object Loading : LoginState()
    data object Success : LoginState()
    data class Error(val message: String) : LoginState()
}