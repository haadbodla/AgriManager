package com.example.agrimanager.ui.fuel

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FuelLogScreen(
    onBackClick: () -> Unit,
    viewModel: FuelLogViewModel = hiltViewModel()
) {
    val logs by viewModel.fuelLogs.collectAsState()
    val totalCost by viewModel.totalCost.collectAsState()
    var showDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Fuel History") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Log")
            }
        },
        bottomBar = {
            BottomAppBar {
                Text(
                    text = "Total Spent: Rs. ${totalCost ?: 0.0}",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    ) { paddingValues ->
        if (logs.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Text("No fuel logs yet.")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(logs) { log ->
                    Card(elevation = CardDefaults.cardElevation(2.dp)) {
                        Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                            Text("Date: ${SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(log.date))}", fontWeight = FontWeight.Bold)
                            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                Text("${log.liters} Liters @ ${log.rate}/L")
                                Text("Rs. ${log.totalCost}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                            Text("Meter: ${log.hourMeterReading} hrs", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }

        if (showDialog) {
            AddFuelDialog(onDismiss = { showDialog = false }, onConfirm = { l, r, h ->
                viewModel.addFuelLog(l, r, h)
                showDialog = false
            })
        }
    }
}

@Composable
fun AddFuelDialog(onDismiss: () -> Unit, onConfirm: (String, String, String) -> Unit) {
    var liters by remember { mutableStateOf("") }
    var rate by remember { mutableStateOf("") }
    var hours by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Fuel Log") },
        text = {
            Column {
                OutlinedTextField(value = liters, onValueChange = { liters = it }, label = { Text("Liters") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = rate, onValueChange = { rate = it }, label = { Text("Rate per Liter") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = hours, onValueChange = { hours = it }, label = { Text("Hour Meter") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
            }
        },
        confirmButton = { Button(onClick = { onConfirm(liters, rate, hours) }) { Text("Save") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
