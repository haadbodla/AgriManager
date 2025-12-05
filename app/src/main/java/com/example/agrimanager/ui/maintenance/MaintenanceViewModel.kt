package com.example.agrimanager.ui.maintenance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.agrimanager.data.local.MachineEntity
import com.example.agrimanager.data.local.MaintenanceLogEntity
import com.example.agrimanager.data.local.MaintenanceLogWithMachine
import com.example.agrimanager.data.repository.FarmRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MaintenanceViewModel @Inject constructor(
    private val repository: FarmRepository
) : ViewModel() {

    // Maintenance logs with machine details
    val maintenanceLogs: StateFlow<List<MaintenanceLogWithMachine>> = repository.getAllMaintenanceLogs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Machines for dropdown
    val machines: StateFlow<List<MachineEntity>> = repository.getAllMachines()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addMaintenanceLog(
        machineId: Int,
        tag: String,
        cost: Double,
        mechanicName: String,
        description: String
    ) {
        viewModelScope.launch {
            val log = MaintenanceLogEntity(
                machineId = machineId,
                tag = tag,
                cost = cost,
                mechanicName = mechanicName,
                description = description
            )
            repository.addMaintenanceLog(log)
        }
    }

    fun deleteMaintenanceLog(logId: Int) {
        viewModelScope.launch {
            // Find the log by ID and delete it
            val log = MaintenanceLogEntity(
                id = logId,
                machineId = 0, // These values don't matter for delete
                tag = "",
                cost = 0.0,
                mechanicName = "",
                description = ""
            )
            repository.deleteMaintenanceLog(log)
        }
    }
}
