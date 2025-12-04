package com.example.agrimanager.ui.bill

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.agrimanager.data.local.BillWithLocation
import com.example.agrimanager.data.local.LocationEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillListScreen(
    onBackClick: () -> Unit,
    onManageLocationsClick: () -> Unit, // <--- NEW PARAMETER
    viewModel: BillViewModel = hiltViewModel()
) {
    val bills by viewModel.allBills.collectAsState()
    val locations by viewModel.locations.collectAsState()
    var showDialog by remember { mutableStateOf(false) }

    val totalPaid = bills.sumOf { it.bill.amount }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("All Electricity Bills") },
                navigationIcon = { IconButton(onClick = onBackClick) { Icon(Icons.Default.ArrowBack, "Back") } },
                actions = {
                    // NEW: Button to go to Location Manager
                    IconButton(onClick = onManageLocationsClick) {
                        Icon(Icons.Default.LocationOn, contentDescription = "Manage Locations")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) { Icon(Icons.Default.Add, "Add Bill") }
        },
        bottomBar = {
            BottomAppBar {
                Text(
                    "Total Farm Cost: Rs. $totalPaid",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    ) { paddingValues ->
        if (bills.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Text("No bills yet.")
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(bills) { item -> BillCard(item) }
            }
        }

        if (showDialog) {
            AddBillDialog(
                locations = locations,
                onDismiss = { showDialog = false },
                onConfirm = { locId, m, a ->
                    viewModel.addBill(locId, m, a)
                    showDialog = false
                }
            )
        }
    }
}

// ... BillCard and AddBillDialog remain exactly the same as before ...
@Composable
fun BillCard(item: BillWithLocation) {
    Card(elevation = CardDefaults.cardElevation(2.dp)) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text(item.locationName, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.tertiary)
            Spacer(modifier = Modifier.height(4.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(item.bill.billingMonth, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Text(SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(item.bill.dateAdded)), style = MaterialTheme.typography.bodySmall)
                }
                Text("Rs. ${item.bill.amount}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddBillDialog(locations: List<LocationEntity>, onDismiss: () -> Unit, onConfirm: (Int, String, String) -> Unit) {
    var month by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var selectedLocation by remember { mutableStateOf<LocationEntity?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Bill") },
        text = {
            Column {
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                    OutlinedTextField(
                        value = selectedLocation?.name ?: "Select Location",
                        onValueChange = {}, readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        locations.forEach { location ->
                            DropdownMenuItem(
                                text = { Text(location.name) },
                                onClick = { selectedLocation = location; expanded = false }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(value = month, onValueChange = { month = it }, label = { Text("Month") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = amount, onValueChange = { amount = it }, label = { Text("Amount") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = { Button(onClick = { if (selectedLocation != null) onConfirm(selectedLocation!!.id, month, amount) }, enabled = selectedLocation != null && month.isNotBlank() && amount.isNotBlank()) { Text("Save") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}