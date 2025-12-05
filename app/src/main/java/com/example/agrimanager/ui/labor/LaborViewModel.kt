package com.example.agrimanager.ui.labor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.agrimanager.data.local.EmployeeEntity
import com.example.agrimanager.data.local.LaborLogEntity
import com.example.agrimanager.data.local.LaborLogWithEmployee
import com.example.agrimanager.data.repository.FarmRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LaborViewModel @Inject constructor(
    private val repository: FarmRepository
) : ViewModel() {

    // Labor logs with employee details
    val laborLogs: StateFlow<List<LaborLogWithEmployee>> = repository.getAllLaborLogs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Employees for dropdown
    val employees: StateFlow<List<EmployeeEntity>> = repository.getAllEmployees()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addLaborLog(
        employeeId: Int,
        laborCount: Int,
        workType: String,
        totalAmount: Double
    ) {
        viewModelScope.launch {
            val log = LaborLogEntity(
                employeeId = employeeId,
                laborCount = laborCount,
                workType = workType,
                totalAmount = totalAmount
            )
            repository.addLaborLog(log)
        }
    }

    fun deleteLaborLog(logId: Int) {
        viewModelScope.launch {
            // Find the log by ID and delete it
            val log = LaborLogEntity(
                id = logId,
                employeeId = 0, // These values don't matter for delete
                laborCount = 0,
                workType = "",
                totalAmount = 0.0
            )
            repository.deleteLaborLog(log)
        }
    }
}
