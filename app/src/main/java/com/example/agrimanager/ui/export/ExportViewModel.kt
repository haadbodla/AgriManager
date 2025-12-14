package com.example.agrimanager.ui.export

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.agrimanager.data.models.ExportConfig
import com.example.agrimanager.data.repository.FarmRepository
import com.example.agrimanager.utils.PdfExportManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class ExportViewModel @Inject constructor(
    private val repository: FarmRepository,
    private val pdfExportManager: PdfExportManager
) : ViewModel() {

    private val _exportState = MutableStateFlow<ExportState>(ExportState.Idle)
    val exportState: StateFlow<ExportState> = _exportState
    
    private val _config = MutableStateFlow(ExportConfig())
    val config: StateFlow<ExportConfig> = _config
    
    fun updateConfig(newConfig: ExportConfig) {
        _config.value = newConfig
    }
    
    fun exportToPdf() {
        viewModelScope.launch {
            _exportState.value = ExportState.Loading
            
            try {
                // Get data from repository
                val data = repository.getExportData(_config.value)
                
                // Generate PDF
                val result = pdfExportManager.generatePdf(data)
                
                if (result.isSuccess) {
                    _exportState.value = ExportState.Success(result.getOrNull()!!)
                } else {
                    _exportState.value = ExportState.Error(
                        result.exceptionOrNull()?.message ?: "Unknown error"
                    )
                }
            } catch (e: Exception) {
                _exportState.value = ExportState.Error(e.message ?: "Export failed")
            }
        }
    }
    
    fun resetState() {
        _exportState.value = ExportState.Idle
    }
}

sealed class ExportState {
    object Idle : ExportState()
    object Loading : ExportState()
    data class Success(val file: File) : ExportState()
    data class Error(val message: String) : ExportState()
}
