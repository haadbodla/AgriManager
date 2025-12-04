package com.example.agrimanager.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.agrimanager.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun signIn(email: String, password: String, onSuccess: () -> Unit) {
        if (email.isBlank() || password.isBlank()) {
            _error.value = "Please fill in all fields"
            return
        }

        _isLoading.value = true
        _error.value = null // Clear previous errors

        viewModelScope.launch {
            val result = repository.signIn(email, password)
            _isLoading.value = false
            result.onSuccess {
                onSuccess()
            }.onFailure {
                _error.value = it.message ?: "Login failed"
            }
        }
    }

    fun signUp(email: String, password: String, onSuccess: () -> Unit) {
        if (email.isBlank() || password.isBlank()) {
            _error.value = "Please fill in all fields"
            return
        }

        _isLoading.value = true
        _error.value = null

        viewModelScope.launch {
            val result = repository.signUp(email, password)
            _isLoading.value = false
            result.onSuccess {
                onSuccess()
            }.onFailure {
                _error.value = it.message ?: "Sign up failed"
            }
        }
    }
}
