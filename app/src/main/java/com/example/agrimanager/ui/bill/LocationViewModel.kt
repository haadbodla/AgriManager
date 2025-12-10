package com.example.agrimanager.ui.bill

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
    val locations: StateFlow<List<LocationEntity>> = repository.getAllLocations()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addLocation(name: String) {
        if (name.isBlank()) return

        viewModelScope.launch {
            repository.insertLocation(LocationEntity(name = name))
        }
    }

    fun updateLocation(location: LocationEntity, newName: String) {
        if (newName.isBlank()) return

        viewModelScope.launch {
            val updatedLocation = location.copy(name = newName)
            repository.updateLocation(updatedLocation)
        }
    }

    fun deleteLocation(location: LocationEntity) {
        viewModelScope.launch {
            repository.deleteLocation(location)
        }
    }
}
