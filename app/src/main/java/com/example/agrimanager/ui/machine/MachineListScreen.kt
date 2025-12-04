package com.example.agrimanager.ui.machine

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.agrimanager.data.local.MachineEntity

// 1. THE MAIN SCREEN
@Composable
fun MachineListScreen(
    viewModel: MachineViewModel = hiltViewModel(),
    onMachineClick: (Int) -> Unit // <--- NEW: Callback for navigation
) {
    val machines by viewModel.machineList.collectAsState()
    var showDialog by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Machine")
            }
        }
    ) { paddingValues ->

        if (machines.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No machines yet. Tap + to add one!")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(machines) { machine ->
                    MachineItem(
                        machine = machine,
                        onDelete = { viewModel.deleteMachine(machine) },
                        onClick = { onMachineClick(machine.id) } // <--- NEW: Pass the click event
                    )
                }
            }
        }

        if (showDialog) {
            AddMachineDialog(
                onDismiss = { showDialog = false },
                onConfirm = { name, interval, reading ->
                    viewModel.addMachine(name, interval, reading)
                    showDialog = false
                }
            )
        }
    }
}

// 2. THE LIST ITEM
@OptIn(ExperimentalMaterial3Api::class) // Required for Card onClick
@Composable
fun MachineItem(
    machine: MachineEntity,
    onDelete: () -> Unit,
    onClick: () -> Unit // <--- NEW: Receive the click event
) {
    Card(
        onClick = onClick, // <--- NEW: Enable clicking the card
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = machine.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Service Interval: ${machine.serviceIntervalHours} hrs",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "Current Reading: ${machine.lastServiceReading} hrs",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

// 3. THE ADD DIALOG (Unchanged)
@Composable
fun AddMachineDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var interval by remember { mutableStateOf("") }
    var reading by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Machine") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name (e.g. Tractor)") }
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = interval,
                    onValueChange = { interval = it },
                    label = { Text("Service Interval (Hours)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = reading,
                    onValueChange = { reading = it },
                    label = { Text("Current Reading (Hours)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(name, interval, reading) }) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}