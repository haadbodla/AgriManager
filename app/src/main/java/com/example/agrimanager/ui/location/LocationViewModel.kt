package com.example.agrimanager.ui.location

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.agrimanager.data.local.LocationEntity
import com.example.agrimanager.data.repository.FarmRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LocationViewModel @Inject constructor(
    private val repository: FarmRepository
) : ViewModel() {

    // List of locations
    val locationList: StateFlow<List<LocationEntity>> = repository.getAllLocations()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Add a new location
    fun addLocation(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            repository.insertLocation(LocationEntity(name = name))
        }
    }

    // Delete a location
    fun deleteLocation(location: LocationEntity) {
        viewModelScope.launch {
            repository.deleteLocation(location)
        }
    }
}
