package com.example.agrimanager.ui.employee

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.agrimanager.data.local.EmployeeEntity
import com.example.agrimanager.data.repository.FarmRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EmployeeViewModel @Inject constructor(
    private val repository: FarmRepository
) : ViewModel() {

    // List of employees
    val employeeList: StateFlow<List<EmployeeEntity>> = repository.getAllEmployees()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addEmployee(name: String, baseSalary: Double) {
        viewModelScope.launch {
            val employee = EmployeeEntity(name = name, baseSalary = baseSalary)
            repository.insertEmployee(employee)
        }
    }

    fun updateEmployee(employee: EmployeeEntity, newName: String, newBaseSalary: Double) {
        if (newName.isBlank() || newBaseSalary <= 0) return

        viewModelScope.launch {
            val updatedEmployee = employee.copy(
                name = newName,
                baseSalary = newBaseSalary
            )
            repository.updateEmployee(updatedEmployee)
        }
    }

    fun deleteEmployee(employee: EmployeeEntity) {
        viewModelScope.launch {
            repository.deleteEmployee(employee)
        }
    }
}
