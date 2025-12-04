package com.example.agrimanager.ui.employee

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.agrimanager.data.local.EmployeeEntity
import com.example.agrimanager.data.local.TransactionEntity
import com.example.agrimanager.data.repository.FarmRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SalaryViewModel @Inject constructor(
    private val repository: FarmRepository
) : ViewModel() {

    private val _employeeId = MutableStateFlow(-1)
    private val _employee = MutableStateFlow<EmployeeEntity?>(null)
    val employee: StateFlow<EmployeeEntity?> = _employee.asStateFlow()

    // Load employee data
    fun loadEmployee(id: Int) {
        _employeeId.value = id
        viewModelScope.launch {
            _employee.value = repository.getEmployeeById(id)
        }
    }

    // Total advances (debits)
    private val totalAdvances: Flow<Double> = _employeeId.flatMapLatest { id ->
        if (id < 0) flowOf(0.0) else repository.getTotalAdvances(id).map { it ?: 0.0 }
    }

    // Total salary credits (CREDIT transactions)
    private val totalCredits: Flow<Double> = _employeeId.flatMapLatest { id ->
        if (id < 0) flowOf(0.0) else repository.getTransactionsForEmployee(id)
            .map { list -> list.filter { it.type == "CREDIT" }.sumOf { it.amount } }
    }

    // Balance = credits - advances
    val balance: StateFlow<Double> = combine(totalCredits, totalAdvances) { credits, advances ->
        credits - advances
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    fun giveAdvance(amount: Double) {
        viewModelScope.launch {
            val emp = _employee.value ?: return@launch
            val transaction = TransactionEntity(
                employeeId = emp.id,
                amount = amount,
                type = "DEBIT",
                timestamp = System.currentTimeMillis()
            )
            repository.insertTransaction(transaction)
        }
    }

    fun addSalary() {
        viewModelScope.launch {
            val emp = _employee.value ?: return@launch
            val transaction = TransactionEntity(
                employeeId = emp.id,
                amount = emp.baseSalary,
                type = "CREDIT",
                timestamp = System.currentTimeMillis()
            )
            repository.insertTransaction(transaction)
        }
    }
}
