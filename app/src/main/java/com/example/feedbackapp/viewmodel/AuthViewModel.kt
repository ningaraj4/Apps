package com.example.feedbackapp.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.feedbackapp.data.AuthRepository
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    data class Success(val user: FirebaseUser?) : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}

class AuthViewModel(private val repository: AuthRepository = AuthRepository()) : ViewModel() {
    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState

    private val _profilePictureUrl = MutableStateFlow<String?>(null)
    val profilePictureUrl: StateFlow<String?> = _profilePictureUrl

    fun register(name: String, email: String, password: String, role: String, section: String) {
        _uiState.value = AuthUiState.Loading
        viewModelScope.launch {
            val result = repository.registerUser(name, email, password, role, section)
            _uiState.value = result.fold(
                onSuccess = { AuthUiState.Success(it) },
                onFailure = { AuthUiState.Error(it.message ?: "Registration failed") }
            )
        }
    }

    fun login(email: String, password: String) {
        _uiState.value = AuthUiState.Loading
        viewModelScope.launch {
            val result = repository.loginUser(email, password)
            _uiState.value = result.fold(
                onSuccess = { AuthUiState.Success(it) },
                onFailure = { AuthUiState.Error(it.message ?: "Login failed") }
            )
        }
    }

    fun signOut() {
        repository.signOut()
        _uiState.value = AuthUiState.Idle
    }

    fun getCurrentUser(): FirebaseUser? = repository.getCurrentUser()

    fun uploadProfilePicture(userId: String, imageUri: Uri) {
        _uiState.value = AuthUiState.Loading
        viewModelScope.launch {
            val result = repository.uploadProfilePicture(userId, imageUri)
            result.fold(
                onSuccess = { url ->
                    _profilePictureUrl.value = url
                    _uiState.value = AuthUiState.Idle
                },
                onFailure = { e ->
                    _uiState.value = AuthUiState.Error(e.message ?: "Failed to upload profile picture")
                }
            )
        }
    }

    fun firebaseAuthWithGoogle(idToken: String) {
        _uiState.value = AuthUiState.Loading
        viewModelScope.launch {
            val result = repository.firebaseAuthWithGoogle(idToken)
            _uiState.value = result.fold(
                onSuccess = { AuthUiState.Success(it) },
                onFailure = { AuthUiState.Error(it.message ?: "Google sign-in failed") }
            )
        }
    }

    fun sendPasswordResetEmail(email: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            val result = repository.sendPasswordResetEmail(email)
            result.fold(
                onSuccess = { onResult(true, null) },
                onFailure = { onResult(false, it.message) }
            )
        }
    }

    fun checkIfGoogleUser(email: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val isGoogle = repository.isGoogleUser(email)
            onResult(isGoogle)
        }
    }
} 