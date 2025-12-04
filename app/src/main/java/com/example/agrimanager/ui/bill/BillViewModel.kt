package com.example.agrimanager.ui.bill

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.agrimanager.data.local.BillEntity
import com.example.agrimanager.data.local.BillWithLocation
import com.example.agrimanager.data.local.LocationEntity
import com.example.agrimanager.data.repository.FarmRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BillViewModel @Inject constructor(
    private val repository: FarmRepository
) : ViewModel() {

    // 1. Get Unified List of Bills (Bill + Location Name)
    val allBills: StateFlow<List<BillWithLocation>> = repository.getAllBillsWithLocation()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 2. Get List of Locations (For the Dropdown in the Add Dialog)
    val locations: StateFlow<List<LocationEntity>> = repository.getAllLocations()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 3. Get Total (Sum of ALL bills across ALL locations)
    // Note: If you want per-location totals, we would calculate that differently.
    // For now, let's sum everything to show "Total Farm Electricity Cost".
    // We can do this easily by mapping the list in the UI or adding a repo function.

    fun addBill(locationId: Int, month: String, amount: String) {
        val amountVal = amount.toDoubleOrNull() ?: return
        if (month.isBlank()) return

        viewModelScope.launch {
            repository.insertBill(
                BillEntity(
                    locationId = locationId,
                    billingMonth = month,
                    amount = amountVal
                )
            )
        }
    }
}