package com.example.agrimanager.ui.machine

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.agrimanager.data.local.FarmDao
import com.example.agrimanager.data.local.MachineEntity
import com.example.agrimanager.data.repository.FarmRepository
import com.example.agrimanager.utils.NewDataTracker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MachineViewModel @Inject constructor(
    private val repository: FarmRepository,
    private val farmDao: FarmDao,
    private val newDataTracker: NewDataTracker
) : ViewModel() {

    // 1. The List of Machines (Always updated)
    val machineList: StateFlow<List<MachineEntity>> = repository.getAllMachines()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    // 2. New fuel log counts per machine (for count badges)
    private val _newFuelLogCounts = MutableStateFlow<Map<Int, Int>>(emptyMap())
    val newFuelLogCounts: StateFlow<Map<Int, Int>> = _newFuelLogCounts
    
    init {
        // Load new counts when ViewModel is created
        loadNewFuelLogCounts()
    }
    
    private fun loadNewFuelLogCounts() {
        viewModelScope.launch {
            val lastSeen = newDataTracker.getLastSeenTimestamp(NewDataTracker.MODULE_FUEL)
            machineList.collect { machines ->
                val countsMap = mutableMapOf<Int, Int>()
                machines.forEach { machine ->
                    farmDao.countNewFuelLogsForMachine(machine.id, lastSeen).collect { count ->
                        countsMap[machine.id] = count
                        _newFuelLogCounts.value = countsMap.toMap()
                    }
                }
            }
        }
    }

    // 3. Function to Add a Machine
    fun addMachine(name: String) {
        if (name.isBlank()) return

        viewModelScope.launch {
            val machine = MachineEntity(
                name = name
            )
            repository.insertMachine(machine)
        }
    }

    // 4. Function to Update a Machine
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

    // 5. Function to Delete
    fun deleteMachine(machine: MachineEntity) {
        viewModelScope.launch {
            repository.deleteMachine(machine)
        }
    }
}
