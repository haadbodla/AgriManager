// fileName: haadbodla/agrimanager/AgriManager-Antigraviry/app/src/main/java/com/example/agrimanager/ui/employee/SalaryViewModel.kt
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

// Data class to hold transaction with running balance
data class TransactionWithBalance(
    val transaction: TransactionEntity,
    val runningBalance: Double
)

@HiltViewModel
class SalaryViewModel @Inject constructor(
    private val repository: FarmRepository
) : ViewModel() {

    private val _employeeId = MutableStateFlow(-1)

    // Employee Details
    private val _employee = MutableStateFlow<EmployeeEntity?>(null)
    val employee: StateFlow<EmployeeEntity?> = _employee.asStateFlow()

    // Transaction History (The missing piece)
    val transactions: StateFlow<List<TransactionEntity>> = _employeeId.flatMapLatest { id ->
        if (id < 0) flowOf(emptyList())
        else repository.getTransactionsForEmployee(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

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
    
    
    // Transactions with running balance
    val transactionsWithBalance: StateFlow<List<TransactionWithBalance>> = transactions.map { txList ->
        if (txList.isEmpty()) {
            emptyList()
        } else {
            var runningBalance = 0.0
            // Sort by timestamp ascending (oldest first) to calculate running balance chronologically
            val sortedOldestFirst = txList.sortedBy { it.timestamp }
            
            sortedOldestFirst.map { tx ->
                // Add to running balance
                runningBalance += when (tx.type) {
                    "CREDIT" -> tx.amount
                    "DEBIT" -> -tx.amount
                    else -> 0.0
                }
                TransactionWithBalance(tx, runningBalance)
            }.sortedByDescending { it.transaction.timestamp } // Sort back to newest first for display
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    // Total advances (for stats display)
    val totalAdvancesValue: StateFlow<Double> = totalAdvances
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)
    
    // Total credits (for stats display)
    val totalCreditsValue: StateFlow<Double> = totalCredits
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

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
    
    // Error state for operations
    private val _operationError = MutableStateFlow<String?>(null)
    val operationError: StateFlow<String?> = _operationError.asStateFlow()
    
    private val _operationSuccess = MutableStateFlow<String?>(null)
    val operationSuccess: StateFlow<String?> = _operationSuccess.asStateFlow()
    
    fun clearMessages() {
        _operationError.value = null
        _operationSuccess.value = null
    }
    
    fun updateTransaction(transactionId: Int, newAmount: Double, newTimestamp: Long) {
        viewModelScope.launch {
            try {
                repository.updateTransaction(transactionId, newAmount, newTimestamp)
                _operationSuccess.value = "Transaction updated successfully"
            } catch (e: Exception) {
                _operationError.value = "Failed to update transaction: ${e.message}"
            }
        }
    }
    
    fun deleteTransaction(transactionId: Int) {
        viewModelScope.launch {
            try {
                repository.deleteTransaction(transactionId)
                _operationSuccess.value = "Transaction deleted successfully"
            } catch (e: Exception) {
                _operationError.value = "Failed to delete transaction: ${e.message}"
            }
        }
    }
}