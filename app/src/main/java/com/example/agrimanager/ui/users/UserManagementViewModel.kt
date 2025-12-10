package com.example.agrimanager.ui.users

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.agrimanager.data.local.UserEntity
import com.example.agrimanager.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class UserManagementUiState {
    object Idle : UserManagementUiState()
    object Loading : UserManagementUiState()
    data class Success(val message: String) : UserManagementUiState()
    data class Error(val message: String) : UserManagementUiState()
}

@HiltViewModel
class UserManagementViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    // List of managers
    val managerList: StateFlow<List<UserEntity>> = userRepository.getAllManagers()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // UI state for operations
    private val _uiState = MutableStateFlow<UserManagementUiState>(UserManagementUiState.Idle)
    val uiState: StateFlow<UserManagementUiState> = _uiState

    // Add manager
    fun addManager(email: String) {
        if (email.isBlank()) {
            _uiState.value = UserManagementUiState.Error("Email cannot be empty")
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _uiState.value = UserManagementUiState.Error("Invalid email format")
            return
        }

        viewModelScope.launch {
            _uiState.value = UserManagementUiState.Loading
            
            val result = userRepository.addManager(email)
            
            _uiState.value = if (result.isSuccess) {
                UserManagementUiState.Success("Manager added successfully!\nAsk them to sign up with: $email")
            } else {
                val errorMessage = result.exceptionOrNull()?.message ?: "Failed to add manager"
                UserManagementUiState.Error(errorMessage)
            }
        }
    }

    // Remove manager
    fun removeManager(manager: UserEntity) {
        viewModelScope.launch {
            _uiState.value = UserManagementUiState.Loading
            
            val result = userRepository.removeManager(manager)
            
            _uiState.value = if (result.isSuccess) {
                UserManagementUiState.Success("Manager removed successfully")
            } else {
                val errorMessage = result.exceptionOrNull()?.message ?: "Failed to remove manager"
                UserManagementUiState.Error(errorMessage)
            }
        }
    }

    // Reset UI state
    fun resetUiState() {
        _uiState.value = UserManagementUiState.Idle
    }
}
