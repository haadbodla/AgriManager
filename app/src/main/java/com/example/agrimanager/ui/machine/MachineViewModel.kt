package com.example.agrimanager.ui.machine

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.agrimanager.data.local.MachineEntity
import com.example.agrimanager.data.repository.FarmRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MachineViewModel @Inject constructor(
    private val repository: FarmRepository
) : ViewModel() {

    // 1. The List of Machines (Always updated)
    val machineList: StateFlow<List<MachineEntity>> = repository.getAllMachines()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000), // Stop updates if UI is hidden > 5s
            initialValue = emptyList()
        )

    // 2. Function to Add a Machine
    fun addMachine(name: String) {
        if (name.isBlank()) return // Simple validation

        viewModelScope.launch {
            val machine = MachineEntity(
                name = name
            )
            repository.insertMachine(machine)
        }
    }

    // 3. Function to Update a Machine
    fun updateMachine(machine: MachineEntity, newName: String) {
        if (newName.isBlank()) return

        viewModelScope.launch {
            val updatedMachine = machine.copy(
                name = newName,
                dateAdded = System.currentTimeMillis()
            )
            repository.updateMachine(updatedMachine)
        }
    }

    // 4. Function to Delete
    fun deleteMachine(machine: MachineEntity) {
        viewModelScope.launch {
            repository.deleteMachine(machine)
        }
    }
}