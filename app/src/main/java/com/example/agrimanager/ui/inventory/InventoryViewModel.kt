// fileName: haadbodla/agrimanager/AgriManager-Antigraviry/app/src/main/java/com/example/agrimanager/ui/inventory/InventoryViewModel.kt
package com.example.agrimanager.ui.inventory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.agrimanager.data.local.EmployeeEntity
import com.example.agrimanager.data.local.InventoryItemEntity
import com.example.agrimanager.data.local.LocationEntity
import com.example.agrimanager.data.local.StockTransactionEntity
import com.example.agrimanager.data.repository.FarmRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

// Simple helper class for the UI (Matches ID to Name)
data class StockHistoryUiModel(
    val transaction: StockTransactionEntity,
    val locationName: String? = null,
    val employeeName: String? = null
)

// NEW: Transaction with running stock
data class StockTransactionWithBalance(
    val transaction: StockTransactionEntity,
    val runningStock: Double,
    val locationName: String? = null,
    val employeeName: String? = null
)

@HiltViewModel
class InventoryViewModel @Inject constructor(
    private val repository: FarmRepository
) : ViewModel() {

    // --- Existing Flows ---
    val inventoryItems: StateFlow<List<InventoryItemEntity>> = repository.getAllInventoryItems()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val locations: StateFlow<List<LocationEntity>> = repository.getAllLocations()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val employees: StateFlow<List<EmployeeEntity>> = repository.getAllEmployees()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- NEW: History Logic ---
    private val _selectedItemId = MutableStateFlow(-1)

    // 1. Get the item details (for the top card)
    val selectedItem: StateFlow<InventoryItemEntity?> = combine(_selectedItemId, inventoryItems) { id, items ->
        items.find { it.id == id }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // 2. Get the history and MATCH NAMES (The logic you needed)
    val selectedItemHistory: StateFlow<List<StockHistoryUiModel>> = _selectedItemId.flatMapLatest { id ->
        if (id == -1) flowOf(emptyList())
        else {
            combine(
                repository.getStockTransactions(id), // Fetch transactions
                locations,                           // Fetch locations
                employees                            // Fetch employees
            ) { transactions, locs, emps ->
                // Map the IDs to Names
                transactions.map { tx ->
                    StockHistoryUiModel(
                        transaction = tx,
                        locationName = locs.find { it.id == tx.locationId }?.name,
                        employeeName = emps.find { it.id == tx.employeeId }?.name
                    )
                }
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    // NEW: Transactions with running stock balance
    val transactionsWithBalance: StateFlow<List<StockTransactionWithBalance>> = _selectedItemId.flatMapLatest { id ->
        if (id == -1) flowOf(emptyList())
        else {
            combine(
                repository.getStockTransactions(id),
                locations,
                employees
            ) { transactions, locs, emps ->
                if (transactions.isEmpty()) {
                    emptyList()
                } else {
                    var runningStock = 0.0
                    val sortedOldestFirst = transactions.sortedBy { it.date }
                    
                    sortedOldestFirst.map { tx ->
                        runningStock += when (tx.type) {
                            "IN" -> tx.quantity
                            "OUT" -> -tx.quantity
                            else -> 0.0
                        }
                        StockTransactionWithBalance(
                            transaction = tx,
                            runningStock = runningStock,
                            locationName = locs.find { it.id == tx.locationId }?.name,
                            employeeName = emps.find { it.id == tx.employeeId }?.name
                        )
                    }.sortedByDescending { it.transaction.date }
                }
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    // NEW: Total stats
    val totalStockIn: StateFlow<Double> = transactionsWithBalance.map { list ->
        list.filter { it.transaction.type == "IN" }.sumOf { it.transaction.quantity }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)
    
    val totalStockOut: StateFlow<Double> = transactionsWithBalance.map { list ->
        list.filter { it.transaction.type == "OUT" }.sumOf { it.transaction.quantity }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)
    
    val totalPurchaseCost: StateFlow<Double> = transactionsWithBalance.map { list ->
        list.filter { it.transaction.type == "IN" }
            .sumOf { it.transaction.totalCost ?: 0.0 }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)
    
    // NEW: Error handling
    private val _operationError = MutableStateFlow<String?>(null)
    val operationError: StateFlow<String?> = _operationError.asStateFlow()
    
    private val _operationSuccess = MutableStateFlow<String?>(null)
    val operationSuccess: StateFlow<String?> = _operationSuccess.asStateFlow()
    
    fun clearMessages() {
        _operationError.value = null
        _operationSuccess.value = null
    }

    fun selectItem(itemId: Int) {
        _selectedItemId.value = itemId
    }

    // --- Existing Dialog Logic ---
    private val _showPurchaseDialog = MutableStateFlow(false)
    val showPurchaseDialog: StateFlow<Boolean> = _showPurchaseDialog.asStateFlow()

    private val _showStockOutDialog = MutableStateFlow<InventoryItemEntity?>(null)
    val showStockOutDialog: StateFlow<InventoryItemEntity?> = _showStockOutDialog.asStateFlow()

    private val _isNewItem = MutableStateFlow(true)
    val isNewItem: StateFlow<Boolean> = _isNewItem.asStateFlow()
    
    // Edit dialog state
    private val _showEditDialog = MutableStateFlow<InventoryItemEntity?>(null)
    val showEditDialog: StateFlow<InventoryItemEntity?> = _showEditDialog.asStateFlow()
    
    // Delete confirmation state
    private val _showDeleteConfirmation = MutableStateFlow<InventoryItemEntity?>(null)
    val showDeleteConfirmation: StateFlow<InventoryItemEntity?> = _showDeleteConfirmation.asStateFlow()

    fun openPurchaseDialog(isNew: Boolean = true) {
        _isNewItem.value = isNew
        _showPurchaseDialog.value = true
    }

    fun closePurchaseDialog() {
        _showPurchaseDialog.value = false
    }

    fun openStockOutDialog(item: InventoryItemEntity) {
        _showStockOutDialog.value = item
    }

    fun closeStockOutDialog() {
        _showStockOutDialog.value = null
    }
    
    fun openEditDialog(item: InventoryItemEntity) {
        _showEditDialog.value = item
    }
    
    fun closeEditDialog() {
        _showEditDialog.value = null
    }
    
    fun openDeleteConfirmation(item: InventoryItemEntity) {
        _showDeleteConfirmation.value = item
    }
    
    fun closeDeleteConfirmation() {
        _showDeleteConfirmation.value = null
    }

    fun addNewItem(name: String, category: String, unit: String, reorderLevel: Double, quantity: Double, totalCost: Double) {
        viewModelScope.launch {
            val item = InventoryItemEntity(
                name = name,
                category = category,
                unit = unit,
                reorderLevel = reorderLevel,
                currentQuantity = 0.0 // Start at 0; recordPurchase() will add the correct quantity
            )
            repository.addInventoryItem(item)

            // Record initial purchase if quantity > 0
            if (quantity > 0 && totalCost > 0) {
                kotlinx.coroutines.delay(100)
                val items = inventoryItems.value
                val newItem = items.find { it.name == name && it.category == category }
                newItem?.let {
                    repository.recordPurchase(it.id, quantity, totalCost)
                }
            }
        }
    }

    fun recordPurchaseForExistingItem(itemId: Int, quantity: Double, totalCost: Double) {
        viewModelScope.launch {
            repository.recordPurchase(itemId, quantity, totalCost)
        }
    }

    fun recordStockOut(itemId: Int, quantity: Double, locationId: Int, employeeId: Int) {
        viewModelScope.launch {
            repository.recordStockOut(itemId, quantity, locationId, employeeId)
        }
    }
    
    fun updateItem(item: InventoryItemEntity) {
        viewModelScope.launch {
            repository.updateInventoryItem(item)
        }
    }
    
    fun deleteItem(item: InventoryItemEntity) {
        viewModelScope.launch {
            repository.deleteInventoryItem(item)
        }
    }
    
    // NEW: Update/Delete stock transactions
    fun updateStockTransaction(transactionId: Int, newQuantity: Double, newDate: Long) {
        viewModelScope.launch {
            try {
                repository.updateStockTransaction(transactionId, newQuantity, newDate)
                _operationSuccess.value = "Stock transaction updated successfully"
            } catch (e: Exception) {
                _operationError.value = "Failed to update: ${e.message}"
            }
        }
    }
    
    fun deleteStockTransaction(transactionId: Int) {
        viewModelScope.launch {
            try {
                repository.deleteStockTransaction(transactionId)
                _operationSuccess.value = "Stock transaction deleted successfully"
            } catch (e: Exception) {
                _operationError.value = "Failed to delete: ${e.message}"
            }
        }
    }
}
