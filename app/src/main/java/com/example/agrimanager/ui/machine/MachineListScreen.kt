package com.example.agrimanager.ui.machine

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
import com.example.agrimanager.data.local.MachineEntity
import com.example.agrimanager.utils.PermissionHelper
import dagger.hilt.android.EntryPointAccessors
import com.example.agrimanager.ui.dashboard.PermissionHelperEntryPoint

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MachineListScreen(
    onNavigateBack: () -> Unit,
    viewModel: MachineViewModel = hiltViewModel(),
    onMachineClick: (Int) -> Unit
) {
    // Get PermissionHelper
    val context = LocalContext.current
    val permissionHelper = remember {
        EntryPointAccessors.fromApplication(
            context.applicationContext,
            PermissionHelperEntryPoint::class.java
        ).permissionHelper()
    }
    
    val machines by viewModel.machineList.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var machineToEdit by remember { mutableStateOf<MachineEntity?>(null) }
    var machineToDelete by remember { mutableStateOf<MachineEntity?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Machines") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
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
                        onDelete = {
                            machineToDelete = machine
                            showDeleteDialog = true
                        },
                        onEdit = {
                            machineToEdit = machine
                            showEditDialog = true
                        },
                        onClick = { onMachineClick(machine.id) },
                        permissionHelper = permissionHelper
                    )
                }
            }
        }

        if (showAddDialog) {
            AddMachineDialog(
                onDismiss = { showAddDialog = false },
                onConfirm = { name ->
                    viewModel.addMachine(name)
                    showAddDialog = false
                }
            )
        }

        if (showEditDialog && machineToEdit != null) {
            EditMachineDialog(
                machine = machineToEdit!!,
                onDismiss = {
                    showEditDialog = false
                    machineToEdit = null
                },
                onConfirm = { newName ->
                    viewModel.updateMachine(machineToEdit!!, newName)
                    showEditDialog = false
                    machineToEdit = null
                }
            )
        }

        if (showDeleteDialog && machineToDelete != null) {
            DeleteMachineConfirmationDialog(
                machineName = machineToDelete!!.name,
                onConfirm = {
                    viewModel.deleteMachine(machineToDelete!!)
                    showDeleteDialog = false
                    machineToDelete = null
                },
                onDismiss = {
                    showDeleteDialog = false
                    machineToDelete = null
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
    onEdit: () -> Unit,
    onClick: () -> Unit,
    permissionHelper: PermissionHelper
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
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = machine.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = formatMachineDate(machine.dateAdded),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onEdit) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = "Edit",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            // Only show delete button for owners
            if (permissionHelper.canDelete(PermissionHelper.FEATURE_MACHINES)) {
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

// Helper function to format date
private fun formatMachineDate(timestamp: Long): String {
    val sdf = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault())
    val currentTime = System.currentTimeMillis()
    val daysDiff = (currentTime - timestamp) / (1000 * 60 * 60 * 24)
    
    return if (daysDiff < 1) {
        "Added: ${sdf.format(java.util.Date(timestamp))}"
    } else {
        "Modified: ${sdf.format(java.util.Date(timestamp))}"
    }
}

// 3. THE ADD DIALOG
@Composable
fun AddMachineDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var name by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Machine") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Machine Name") },
                singleLine = true
            )
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(name) },
                enabled = name.isNotBlank()
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

// 4. THE EDIT DIALOG
@Composable
fun EditMachineDialog(
    machine: MachineEntity,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var name by remember { mutableStateOf(machine.name) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Machine") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Machine Name") },
                singleLine = true
            )
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(name) },
                enabled = name.isNotBlank() && name != machine.name
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

// 5. DELETE CONFIRMATION DIALOG
@Composable
fun DeleteMachineConfirmationDialog(
    machineName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                Icons.Default.Delete,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )
        },
        title = { Text("Delete Machine?") },
        text = {
            Column {
                Text("Are you sure you want to delete \"$machineName\"?")
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "This will also permanently delete:",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
                Text("• All fuel logs")
                Text("• All maintenance records")
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "This action cannot be undone.",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Delete")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}