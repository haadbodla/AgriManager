package com.example.agrimanager.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.agrimanager.data.local.FarmDao
import com.example.agrimanager.data.repository.FarmRepository
import com.example.agrimanager.utils.NewDataTracker
import com.example.agrimanager.utils.SyncStatus
import com.example.agrimanager.utils.SyncStatusManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Data class holding new data counts for each module.
 * Used to show red notification dots on dashboard buttons.
 */
data class NewDataCounts(
    val fuel: Int = 0,
    val labor: Int = 0,
    val inventory: Int = 0,
    val bills: Int = 0,
    val salary: Int = 0,
    val maintenance: Int = 0,
    val dairy: Int = 0
) {
    fun hasAnyNewData(): Boolean = fuel > 0 || labor > 0 || inventory > 0 || 
                                    bills > 0 || salary > 0 || maintenance > 0 || dairy > 0
}

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val farmRepository: FarmRepository,
    private val syncStatusManager: SyncStatusManager,
    private val newDataTracker: NewDataTracker,
    private val farmDao: FarmDao
) : ViewModel() {
    
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing
    
    // Sync status flow
    val syncStatus: StateFlow<SyncStatus> = syncStatusManager.getSyncStatus()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SyncStatus.Synced
        )
    
    // New data counts for notification dots
    // Use MutableStateFlow to manually collect counts (combine() has 5-flow limit)
    private val _newDataCounts = MutableStateFlow(NewDataCounts())
    val newDataCounts: StateFlow<NewDataCounts> = _newDataCounts
    
    init {
        // Collect counts for each module separately
        viewModelScope.launch {
            farmDao.countFuelLogsAfter(newDataTracker.getLastSeenTimestamp(NewDataTracker.MODULE_FUEL))
                .collect { count -> _newDataCounts.value = _newDataCounts.value.copy(fuel = count) }
        }
        viewModelScope.launch {
            farmDao.countLaborLogsAfter(newDataTracker.getLastSeenTimestamp(NewDataTracker.MODULE_LABOR))
                .collect { count -> _newDataCounts.value = _newDataCounts.value.copy(labor = count) }
        }
        viewModelScope.launch {
            farmDao.countInventoryTransactionsAfter(newDataTracker.getLastSeenTimestamp(NewDataTracker.MODULE_INVENTORY))
                .collect { count -> _newDataCounts.value = _newDataCounts.value.copy(inventory = count) }
        }
        viewModelScope.launch {
            farmDao.countBillsAfter(newDataTracker.getLastSeenTimestamp(NewDataTracker.MODULE_BILLS))
                .collect { count -> _newDataCounts.value = _newDataCounts.value.copy(bills = count) }
        }
        viewModelScope.launch {
            farmDao.countTransactionsAfter(newDataTracker.getLastSeenTimestamp(NewDataTracker.MODULE_SALARY))
                .collect { count -> _newDataCounts.value = _newDataCounts.value.copy(salary = count) }
        }
        viewModelScope.launch {
            farmDao.countMaintenanceLogsAfter(newDataTracker.getLastSeenTimestamp(NewDataTracker.MODULE_MAINTENANCE))
                .collect { count -> _newDataCounts.value = _newDataCounts.value.copy(maintenance = count) }
        }
        viewModelScope.launch {
            farmDao.countDairyLogsAfter(newDataTracker.getLastSeenTimestamp(NewDataTracker.MODULE_DAIRY))
                .collect { count -> _newDataCounts.value = _newDataCounts.value.copy(dairy = count) }
        }
    }
    
    /**
     * Mark a module as "seen" to clear the red dot.
     * Call this when user navigates to a module.
     */
    fun markModuleAsSeen(module: String) {
        newDataTracker.markModuleAsSeen(module)
    }
    
    fun refreshData() {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                farmRepository.downloadAllDataFromFirestore()
            } catch (e: Exception) {
                // Handle error silently or show a message
            } finally {
                _isRefreshing.value = false
            }
        }
    }
}

