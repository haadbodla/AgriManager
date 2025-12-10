package com.example.agrimanager.ui.bill

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.agrimanager.data.local.BillEntity
import com.example.agrimanager.data.local.LocationEntity
import com.example.agrimanager.data.repository.FarmRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LocationBillsViewModel @Inject constructor(
    private val repository: FarmRepository
) : ViewModel() {

    private val _locationId = MutableStateFlow(-1)

    // Location Details
    private val _location = MutableStateFlow<LocationEntity?>(null)
    val location: StateFlow<LocationEntity?> = _location.asStateFlow()

    // Bills for this location
    val bills: StateFlow<List<BillEntity>> = _locationId.flatMapLatest { id ->
        if (id < 0) flowOf(emptyList())
        else repository.getBills(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Load location data
    fun loadLocation(id: Int) {
        _locationId.value = id
        viewModelScope.launch {
            _location.value = repository.getLocationById(id)
        }
    }

    fun addBill(month: String, amount: String) {
        val amountVal = amount.toDoubleOrNull() ?: return
        if (month.isBlank()) return
        val locId = _locationId.value
        if (locId < 0) return

        viewModelScope.launch {
            repository.insertBill(
                BillEntity(
                    locationId = locId,
                    billingMonth = month,
                    amount = amountVal
                )
            )
        }
    }

    fun updateBill(id: Int, month: String, amount: String) {
        val amountVal = amount.toDoubleOrNull() ?: return
        if (month.isBlank()) return
        val locId = _locationId.value
        if (locId < 0) return

        viewModelScope.launch {
            repository.updateBill(
                BillEntity(
                    id = id,
                    locationId = locId,
                    billingMonth = month,
                    amount = amountVal,
                    dateAdded = System.currentTimeMillis()
                )
            )
        }
    }

    fun deleteBill(billId: Int) {
        val locId = _locationId.value
        if (locId < 0) return

        viewModelScope.launch {
            repository.deleteBill(
                BillEntity(
                    id = billId,
                    locationId = locId,
                    billingMonth = "",
                    amount = 0.0
                )
            )
        }
    }
}
