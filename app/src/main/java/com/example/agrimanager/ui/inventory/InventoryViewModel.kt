package com.example.agrimanager.ui.inventory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.agrimanager.data.local.EmployeeEntity
import com.example.agrimanager.data.local.InventoryItemEntity
import com.example.agrimanager.data.local.LocationEntity
import com.example.agrimanager.data.repository.FarmRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InventoryViewModel @Inject constructor(
    private val repository: FarmRepository
) : ViewModel() {

    // Inventory items
    val inventoryItems: StateFlow<List<InventoryItemEntity>> = repository.getAllInventoryItems()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Locations for "Where used?" dropdown
    val locations: StateFlow<List<LocationEntity>> = repository.getAllLocations()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Employees for "Who took it?" dropdown
    val employees: StateFlow<List<EmployeeEntity>> = repository.getAllEmployees()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // UI State for dialogs
    private val _showPurchaseDialog = MutableStateFlow(false)
    val showPurchaseDialog: StateFlow<Boolean> = _showPurchaseDialog.asStateFlow()

    private val _showStockOutDialog = MutableStateFlow<InventoryItemEntity?>(null)
    val showStockOutDialog: StateFlow<InventoryItemEntity?> = _showStockOutDialog.asStateFlow()

    private val _isNewItem = MutableStateFlow(true)
    val isNewItem: StateFlow<Boolean> = _isNewItem.asStateFlow()

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

    fun addNewItem(name: String, category: String, unit: String, reorderLevel: Double, quantity: Double, totalCost: Double) {
        viewModelScope.launch {
            val item = InventoryItemEntity(
                name = name,
                category = category,
                unit = unit,
                reorderLevel = reorderLevel,
                currentQuantity = quantity
            )
            repository.addInventoryItem(item)
            
            // If there's an initial purchase, record it
            if (quantity > 0 && totalCost > 0) {
                // Get the newly created item to get its ID
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
}
