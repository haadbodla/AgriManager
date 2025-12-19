package com.example.agrimanager.ui.fuel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.agrimanager.data.local.FuelLogEntity
import com.example.agrimanager.data.repository.FarmRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.round

@HiltViewModel
class FuelLogViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle, // Reads the "machineId" passed from navigation
    private val repository: FarmRepository
) : ViewModel() {

    private val machineId: Int = savedStateHandle.get<Int>("machineId") ?: 0

    val fuelLogs: StateFlow<List<FuelLogEntity>> = repository.getFuelLogs(machineId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalCost: StateFlow<Double?> = repository.getTotalFuelCost(machineId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    fun addFuelLog(liters: String, rate: String, hourMeter: String) {
        val litersVal = liters.toDoubleOrNull() ?: return
        val rateVal = rate.toDoubleOrNull() ?: return
        val hourMeterVal = hourMeter.toIntOrNull() ?: 0

        viewModelScope.launch {
            repository.insertFuelLog(
                FuelLogEntity(
                    machineId = machineId,
                    date = System.currentTimeMillis(),
                    liters = litersVal,
                    rate = rateVal,
                    totalCost = round((litersVal * rateVal) * 100) / 100,
                    hourMeterReading = hourMeterVal
                )
            )
        }
    }

    fun updateFuelLog(id: Int, liters: String, rate: String, hourMeter: String) {
        val litersVal = liters.toDoubleOrNull() ?: return
        val rateVal = rate.toDoubleOrNull() ?: return
        val hourMeterVal = hourMeter.toIntOrNull() ?: 0

        viewModelScope.launch {
            repository.updateFuelLog(
                FuelLogEntity(
                    id = id,
                    machineId = machineId,
                    date = System.currentTimeMillis(),
                    liters = litersVal,
                    rate = rateVal,
                    totalCost = round((litersVal * rateVal) * 100) / 100,
                    hourMeterReading = hourMeterVal
                )
            )
        }
    }

    fun deleteFuelLog(logId: Int) {
        viewModelScope.launch {
            repository.deleteFuelLog(
                FuelLogEntity(
                    id = logId,
                    machineId = machineId,
                    date = 0,
                    liters = 0.0,
                    rate = 0.0,
                    totalCost = 0.0,
                    hourMeterReading = 0
                )
            )
        }
    }
}
