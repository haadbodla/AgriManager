package com.example.agrimanager.ui.maintenance

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMaintenanceLogScreen(
    onNavigateBack: () -> Unit,
    viewModel: MaintenanceViewModel = hiltViewModel()
) {
    val machines by viewModel.machines.collectAsState()
    
    var selectedMachineId by remember { mutableStateOf<Int?>(null) }
    var selectedTag by remember { mutableStateOf("") }
    var cost by remember { mutableStateOf("") }
    var mechanicName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    
    val tags = listOf("Oil Change", "Tyre", "Battery", "Engine", "Other")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Maintenance Log") },
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
            // Machine dropdown
            var machineExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = machineExpanded,
                onExpandedChange = { machineExpanded = it }
            ) {
                OutlinedTextField(
                    value = machines.find { it.id == selectedMachineId }?.name ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Machine") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(machineExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = machineExpanded,
                    onDismissRequest = { machineExpanded = false }
                ) {
                    machines.forEach { machine ->
                        DropdownMenuItem(
                            text = { Text(machine.name) },
                            onClick = {
                                selectedMachineId = machine.id
                                machineExpanded = false
                            }
                        )
                    }
                }
            }

            // Tag selection
            Text(
                text = "Tag",
                style = MaterialTheme.typography.bodyMedium
            )
            
            // Tag chips - first row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                tags.take(3).forEach { tag ->
                    FilterChip(
                        selected = selectedTag == tag,
                        onClick = { selectedTag = tag },
                        label = { Text(tag) }
                    )
                }
            }
            // Tag chips - second row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                tags.drop(3).forEach { tag ->
                    FilterChip(
                        selected = selectedTag == tag,
                        onClick = { selectedTag = tag },
                        label = { Text(tag) }
                    )
                }
            }

            // Cost
            OutlinedTextField(
                value = cost,
                onValueChange = { cost = it },
                label = { Text("Cost") },
                modifier = Modifier.fillMaxWidth()
            )

            // Mechanic Name
            OutlinedTextField(
                value = mechanicName,
                onValueChange = { mechanicName = it },
                label = { Text("Mechanic Name") },
                modifier = Modifier.fillMaxWidth()
            )

            // Description
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                maxLines = 5
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Save button
            Button(
                onClick = {
                    val machId = selectedMachineId
                    val costValue = cost.toDoubleOrNull()
                    
                    if (machId != null && costValue != null && selectedTag.isNotEmpty() && 
                        mechanicName.isNotEmpty() && description.isNotEmpty()) {
                        viewModel.addMaintenanceLog(machId, selectedTag, costValue, mechanicName, description)
                        onNavigateBack()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = selectedMachineId != null && 
                         selectedTag.isNotEmpty() && 
                         cost.toDoubleOrNull() != null && 
                         mechanicName.isNotEmpty() && 
                         description.isNotEmpty()
            ) {
                Text("Save Log")
            }
        }
    }
}
