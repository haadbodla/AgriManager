package com.example.agrimanager.ui.fuel

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.agrimanager.data.local.FuelLogEntity
import com.example.agrimanager.utils.PermissionHelper
import com.example.agrimanager.ui.dashboard.PermissionHelperEntryPoint
import dagger.hilt.android.EntryPointAccessors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FuelLogScreen(
    onBackClick: () -> Unit,
    viewModel: FuelLogViewModel = hiltViewModel()
) {
    // Get PermissionHelper
    val context = LocalContext.current
    val permissionHelper = remember {
        EntryPointAccessors.fromApplication(
            context.applicationContext,
            PermissionHelperEntryPoint::class.java
        ).permissionHelper()
    }
    val logs by viewModel.fuelLogs.collectAsState()
    val totalCost by viewModel.totalCost.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var editingLog by remember { mutableStateOf<FuelLogEntity?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Fuel History") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { 
                editingLog = null
                showDialog = true 
            }) {
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
                    FuelLogCard(
                        log = log,
                        onEdit = {
                            editingLog = log
                            showDialog = true
                        },
                        onDelete = { viewModel.deleteFuelLog(log.id) },
                        permissionHelper = permissionHelper
                    )
                }
            }
        }

        if (showDialog) {
            AddFuelDialog(
                editingLog = editingLog,
                onDismiss = { 
                    showDialog = false
                    editingLog = null
                },
                onConfirm = { l, r, h ->
                    if (editingLog != null) {
                        viewModel.updateFuelLog(editingLog!!.id, l, r, h)
                    } else {
                        viewModel.addFuelLog(l, r, h)
                    }
                    showDialog = false
                    editingLog = null
                }
            )
        }
    }
}

@Composable
fun FuelLogCard(
    log: FuelLogEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    permissionHelper: PermissionHelper
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Card(elevation = CardDefaults.cardElevation(2.dp)) {
        Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Date: ${SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(log.date))}", fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("${log.liters} Liters @ ${log.rate}/L")
                        Text("Rs. ${log.totalCost}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                    Text("Meter: ${log.hourMeterReading} hrs", style = MaterialTheme.typography.bodySmall)
                }
                
                Row {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, "Edit", tint = MaterialTheme.colorScheme.primary)
                    }
                    if (permissionHelper.canDelete(PermissionHelper.FEATURE_MACHINES)) {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Default.Delete, "Delete", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Fuel Log") },
            text = { Text("Are you sure you want to delete this fuel log?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteDialog = false
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun AddFuelDialog(
    editingLog: FuelLogEntity? = null,
    onDismiss: () -> Unit, 
    onConfirm: (String, String, String) -> Unit
) {
    var liters by remember(editingLog) { mutableStateOf(editingLog?.liters?.toString() ?: "") }
    var rate by remember(editingLog) { mutableStateOf(editingLog?.rate?.toString() ?: "") }
    var hours by remember(editingLog) { mutableStateOf(editingLog?.hourMeterReading?.toString() ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (editingLog != null) "Edit Fuel Log" else "Add Fuel Log") },
        text = {
            Column {
                OutlinedTextField(
                    value = liters, 
                    onValueChange = { liters = it }, 
                    label = { Text("Liters") }, 
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = rate, 
                    onValueChange = { rate = it }, 
                    label = { Text("Rate per Liter") }, 
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = hours, 
                    onValueChange = { hours = it }, 
                    label = { Text("Hour Meter") }, 
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        },
        confirmButton = { 
            Button(onClick = { onConfirm(liters, rate, hours) }) { 
                Text(if (editingLog != null) "Update" else "Save") 
            } 
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
