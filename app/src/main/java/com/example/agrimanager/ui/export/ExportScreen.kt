package com.example.agrimanager.ui.export

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.agrimanager.data.models.ExportConfig

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportScreen(
    onNavigateBack: () -> Unit,
    viewModel: ExportViewModel = hiltViewModel()
) {
    val exportState by viewModel.exportState.collectAsState()
    val config by viewModel.config.collectAsState()
    val context = LocalContext.current
    
    var showAllData by remember { mutableStateOf(true) }
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Export Data") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Export type selection
            Card {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Select Data", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = showAllData,
                            onClick = { 
                                showAllData = true
                                viewModel.updateConfig(ExportConfig(includeAll = true))
                            }
                        )
                        Text("All Data (Complete Report)")
                    }
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = !showAllData,
                            onClick = { showAllData = false }
                        )
                        Text("Custom Selection")
                    }
                }
            }
            
            // Date Range Filter
            Card {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Checkbox(
                            checked = config.startDate != null,
                            onCheckedChange = { enabled ->
                                if (enabled) {
                                    // Set to last 30 days by default
                                    val endDate = System.currentTimeMillis()
                                    val startDate = endDate - (30L * 24 * 60 * 60 * 1000)
                                    viewModel.updateConfig(config.copy(startDate = startDate, endDate = endDate))
                                } else {
                                    viewModel.updateConfig(config.copy(startDate = null, endDate = null))
                                }
                            }
                        )
                        Text("Filter by Date Range", style = MaterialTheme.typography.titleMedium)
                    }
                    
                    if (config.startDate != null && config.endDate != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Start Date Button
                        OutlinedButton(
                            onClick = { showStartDatePicker = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.CalendarToday, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Start: ${java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault()).format(java.util.Date(config.startDate!!))}")
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // End Date Button
                        OutlinedButton(
                            onClick = { showEndDatePicker = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.CalendarToday, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("End: ${java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault()).format(java.util.Date(config.endDate!!))}")
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Note: Only fuel logs, bills, transactions, labor, maintenance, and stock transactions will be filtered by date.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            // Custom selection checkboxes
            if (!showAllData) {
                Card {
                    Column(modifier = Modifier.padding(16.dp)) {
                        DataCheckbox("Machines & Fuel", config.includeMachines) {
                            viewModel.updateConfig(config.copy(includeMachines = it, includeFuel = it))
                        }
                        DataCheckbox("Employees & Salary", config.includeEmployees) {
                            viewModel.updateConfig(config.copy(includeEmployees = it, includeSalary = it))
                        }
                        DataCheckbox("Locations & Bills", config.includeLocations) {
                            viewModel.updateConfig(config.copy(includeLocations = it, includeBills = it))
                        }
                        DataCheckbox("Inventory & Stock", config.includeInventory) {
                            viewModel.updateConfig(config.copy(includeInventory = it))
                        }
                        DataCheckbox("Labor Logs", config.includeLabor) {
                            viewModel.updateConfig(config.copy(includeLabor = it))
                        }
                        DataCheckbox("Maintenance Logs", config.includeMaintenance) {
                            viewModel.updateConfig(config.copy(includeMaintenance = it))
                        }
                        DataCheckbox("Analytics Summary", config.includeAnalytics) {
                            viewModel.updateConfig(config.copy(includeAnalytics = it))
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Export button
            Button(
                onClick = { viewModel.exportToPdf() },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = exportState !is ExportState.Loading
            ) {
                if (exportState is ExportState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Generating PDF...")
                } else {
                    Icon(Icons.Default.PictureAsPdf, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Export to PDF")
                }
            }
        }
    }
    
    // Success/Error dialogs
    when (val state = exportState) {
        is ExportState.Success -> {
            AlertDialog(
                onDismissRequest = { viewModel.resetState() },
                icon = { Icon(Icons.Default.CheckCircle, null, tint = MaterialTheme.colorScheme.primary) },
                title = { Text("Export Successful!") },
                text = { 
                    Column {
                        Text("PDF saved to Downloads folder:")
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(state.file.name, style = MaterialTheme.typography.bodySmall)
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        val uri = FileProvider.getUriForFile(
                            context,
                            "${context.packageName}.provider",
                            state.file
                        )
                        val intent = Intent(Intent.ACTION_VIEW).apply {
                            setDataAndType(uri, "application/pdf")
                            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                        }
                        context.startActivity(Intent.createChooser(intent, "Open PDF"))
                        viewModel.resetState()
                        onNavigateBack()
                    }) {
                        Text("Open PDF")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        viewModel.resetState()
                        onNavigateBack()
                    }) {
                        Text("Done")
                    }
                }
            )
        }
        is ExportState.Error -> {
            AlertDialog(
                onDismissRequest = { viewModel.resetState() },
                icon = { Icon(Icons.Default.Error, null, tint = MaterialTheme.colorScheme.error) },
                title = { Text("Export Failed") },
                text = { Text(state.message) },
                confirmButton = {
                    Button(onClick = { viewModel.resetState() }) {
                        Text("OK")
                    }
                }
            )
        }
        else -> {}
    }
    
    // Date Picker Dialogs
    if (showStartDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = config.startDate ?: System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { selectedDate ->
                        viewModel.updateConfig(config.copy(startDate = selectedDate))
                    }
                    showStartDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showStartDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
    
    if (showEndDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = config.endDate ?: System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { selectedDate ->
                        viewModel.updateConfig(config.copy(endDate = selectedDate))
                    }
                    showEndDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEndDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
fun DataCheckbox(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Checkbox(checked = checked, onCheckedChange = onCheckedChange)
        Text(label)
    }
}
