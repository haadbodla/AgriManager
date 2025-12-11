package com.example.agrimanager.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.agrimanager.data.repository.FarmRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val farmRepository: FarmRepository
) : ViewModel() {
    
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing
    
    fun refreshData() {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                farmRepository.downloadAllDataFromFirestore()
            } catch (e: Exception) {
                // Handle error silently or show a message
            } finally {
                _isRefreshing.value = false
            }
        }
    }
}
