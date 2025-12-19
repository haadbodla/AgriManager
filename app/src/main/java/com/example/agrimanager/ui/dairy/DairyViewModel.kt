package com.example.agrimanager.ui.dairy

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.agrimanager.data.local.DairyCompanyEntity
import com.example.agrimanager.data.local.DairyLogEntity
import com.example.agrimanager.data.repository.FarmRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DairyViewModel @Inject constructor(
    private val repository: FarmRepository
) : ViewModel() {

    val dairyLogs: StateFlow<List<DairyLogEntity>> = repository.getAllDairyLogs()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val companies: StateFlow<List<DairyCompanyEntity>> = repository.getAllDairyCompanies()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addDairyLog(log: DairyLogEntity) {
        viewModelScope.launch {
            repository.insertDairyLog(log)
        }
    }

    fun deleteDairyLog(log: DairyLogEntity) {
        viewModelScope.launch {
            repository.deleteDairyLog(log)
        }
    }

    fun addCompany(name: String, rate: Double) {
        viewModelScope.launch {
            repository.insertDairyCompany(DairyCompanyEntity(name = name, ratePerLiter = rate))
        }
    }

    fun updateCompany(company: DairyCompanyEntity) {
        viewModelScope.launch {
            repository.updateDairyCompany(company)
        }
    }

    fun deleteCompany(company: DairyCompanyEntity) {
        viewModelScope.launch {
            repository.deleteDairyCompany(company)
        }
    }
}
