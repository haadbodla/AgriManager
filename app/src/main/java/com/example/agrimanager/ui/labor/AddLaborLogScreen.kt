package com.example.agrimanager.ui.labor

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddLaborLogScreen(
    logId: Int? = null,
    onNavigateBack: () -> Unit,
    viewModel: LaborViewModel = hiltViewModel()
) {
    val employees by viewModel.employees.collectAsState()
    
    // Load existing log if editing
    var existingLog by remember { mutableStateOf<com.example.agrimanager.data.local.LaborLogEntity?>(null) }
    
    LaunchedEffect(logId) {
        if (logId != null && logId > 0) {
            existingLog = viewModel.getLaborLogById(logId)
        }
    }
    
    var selectedEmployeeId by remember(existingLog) { mutableStateOf(existingLog?.employeeId) }
    var laborCount by remember(existingLog) { mutableStateOf(existingLog?.laborCount?.toString() ?: "") }
    var selectedWorkType by remember(existingLog) { mutableStateOf(existingLog?.workType ?: "") }
    var totalAmount by remember(existingLog) { mutableStateOf(existingLog?.totalAmount?.toString() ?: "") }
    
    val workTypes = listOf("Harvesting", "Watering", "Weeding", "Sowing", "Fertilizing")
    val isEditMode = logId != null && logId > 0

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditMode) "Edit Labor Log" else "Add Labor Log") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Employee dropdown (Munshi Name)
            var employeeExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = employeeExpanded,
                onExpandedChange = { employeeExpanded = it }
            ) {
                OutlinedTextField(
                    value = employees.find { it.id == selectedEmployeeId }?.name ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Munshi Name") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(employeeExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = employeeExpanded,
                    onDismissRequest = { employeeExpanded = false }
                ) {
                    employees.forEach { employee ->
                        DropdownMenuItem(
                            text = { Text(employee.name) },
                            onClick = {
                                selectedEmployeeId = employee.id
                                employeeExpanded = false
                            }
                        )
                    }
                }
            }

            // Labor Count
            OutlinedTextField(
                value = laborCount,
                onValueChange = { laborCount = it },
                label = { Text("Labor Count") },
                modifier = Modifier.fillMaxWidth()
            )

            // Work Type
            Text(
                text = "Work Type",
                style = MaterialTheme.typography.bodyMedium
            )
            
            // Work type chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                workTypes.take(3).forEach { type ->
                    FilterChip(
                        selected = selectedWorkType == type,
                        onClick = { selectedWorkType = type },
                        label = { Text(type) }
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                workTypes.drop(3).forEach { type ->
                    FilterChip(
                        selected = selectedWorkType == type,
                        onClick = { selectedWorkType = type },
                        label = { Text(type) }
                    )
                }
            }

            // Total Bill Amount
            OutlinedTextField(
                value = totalAmount,
                onValueChange = { totalAmount = it },
                label = { Text("Total Bill Amount") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Save button
            Button(
                onClick = {
                    val empId = selectedEmployeeId
                    val count = laborCount.toIntOrNull()
                    val amount = totalAmount.toDoubleOrNull()
                    
                    if (empId != null && count != null && amount != null && selectedWorkType.isNotEmpty()) {
                        if (isEditMode) {
                            viewModel.updateLaborLog(logId!!, empId, count, selectedWorkType, amount)
                        } else {
                            viewModel.addLaborLog(empId, count, selectedWorkType, amount)
                        }
                        onNavigateBack()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = selectedEmployeeId != null && 
                         laborCount.toIntOrNull() != null && 
                         selectedWorkType.isNotEmpty() && 
                         totalAmount.toDoubleOrNull() != null
            ) {
                Text(if (isEditMode) "Update Log" else "Save Log")
            }
        }
    }
}
