package com.example.agrimanager.ui.bill

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.agrimanager.data.local.LocationEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationListScreen(
    onNavigateBack: () -> Unit,
    navController: NavController,
    viewModel: LocationViewModel = hiltViewModel()
) {
    val locations by viewModel.locations.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var locationToEdit by remember { mutableStateOf<LocationEntity?>(null) }
    var locationToDelete by remember { mutableStateOf<LocationEntity?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Locations") },
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
                Icon(Icons.Default.Add, contentDescription = "Add Location")
            }
        }
    ) { paddingValues ->
        if (locations.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("No locations yet")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(locations) { location ->
                    LocationCard(
                        location = location,
                        onClick = { navController.navigate("location-bills/${location.id}") },
                        onEdit = {
                            locationToEdit = location
                            showEditDialog = true
                        },
                        onDelete = {
                            locationToDelete = location
                            showDeleteDialog = true
                        }
                    )
                }
            }
        }

        if (showAddDialog) {
            AddLocationDialog(
                existingLocations = locations,
                onDismiss = { showAddDialog = false },
                onSave = { name ->
                    viewModel.addLocation(name)
                    showAddDialog = false
                }
            )
        }

        if (showEditDialog && locationToEdit != null) {
            EditLocationDialog(
                location = locationToEdit!!,
                existingLocations = locations,
                onDismiss = {
                    showEditDialog = false
                    locationToEdit = null
                },
                onSave = { newName ->
                    viewModel.updateLocation(locationToEdit!!, newName)
                    showEditDialog = false
                    locationToEdit = null
                }
            )
        }

        if (showDeleteDialog && locationToDelete != null) {
            DeleteLocationConfirmationDialog(
                locationName = locationToDelete!!.name,
                onConfirm = {
                    viewModel.deleteLocation(locationToDelete!!)
                    showDeleteDialog = false
                    locationToDelete = null
                },
                onDismiss = {
                    showDeleteDialog = false
                    locationToDelete = null
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LocationCard(
    location: LocationEntity,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        onClick = onClick,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = location.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            // Edit button
            IconButton(onClick = onEdit) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit Location",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            // Delete button
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Location",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun AddLocationDialog(
    existingLocations: List<LocationEntity>,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    val trimmedName = name.trim()
    val isDuplicate = existingLocations.any { it.name.equals(trimmedName, ignoreCase = true) }
    val isValid = trimmedName.isNotBlank() && !isDuplicate

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Location") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Location Name") },
                    isError = trimmedName.isNotBlank() && isDuplicate,
                    supportingText = {
                        if (trimmedName.isNotBlank() && isDuplicate) {
                            Text(
                                "Location name already exists",
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(trimmedName) },
                enabled = isValid
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun EditLocationDialog(
    location: LocationEntity,
    existingLocations: List<LocationEntity>,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var name by remember { mutableStateOf(location.name) }
    val trimmedName = name.trim()
    val isDuplicate = existingLocations.any { 
        it.id != location.id && it.name.equals(trimmedName, ignoreCase = true) 
    }
    val isValid = trimmedName.isNotBlank() && trimmedName != location.name && !isDuplicate

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Location") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Location Name") },
                    isError = trimmedName.isNotBlank() && isDuplicate,
                    supportingText = {
                        if (trimmedName.isNotBlank() && isDuplicate) {
                            Text(
                                "Location name already exists",
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(trimmedName) },
                enabled = isValid
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun DeleteLocationConfirmationDialog(
    locationName: String,
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
        title = { Text("Delete Location?") },
        text = {
            Column {
                Text("Are you sure you want to delete \"$locationName\"?")
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "This will also permanently delete:",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
                Text("• All bills for this location")
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
