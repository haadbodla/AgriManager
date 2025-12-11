package com.example.agrimanager.ui.bill

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.navigation.NavController
import com.example.agrimanager.data.local.BillEntity
import com.example.agrimanager.utils.PermissionHelper
import dagger.hilt.android.EntryPointAccessors
import com.example.agrimanager.ui.dashboard.PermissionHelperEntryPoint
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationBillsScreen(
    locationId: Int,
    navController: NavController,
    viewModel: LocationBillsViewModel = hiltViewModel()
) {
    // Get PermissionHelper
    val context = LocalContext.current
    val permissionHelper = remember {
        EntryPointAccessors.fromApplication(
            context.applicationContext,
            PermissionHelperEntryPoint::class.java
        ).permissionHelper()
    }
    
    val location by viewModel.location.collectAsState()
    val bills by viewModel.bills.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var editingBill by remember { mutableStateOf<BillEntity?>(null) }

    LaunchedEffect(locationId) {
        viewModel.loadLocation(locationId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(location?.name ?: "Bills") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                editingBill = null
                showDialog = true
            }) {
                Icon(Icons.Default.Add, "Add Bill")
            }
        }
    ) { paddingValues ->
        if (bills.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("No bills yet for this location")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(bills) { bill ->
                    BillItemCard(
                        bill = bill,
                        onEdit = {
                            editingBill = bill
                            showDialog = true
                        },
                        onDelete = { viewModel.deleteBill(bill.id) },
                        permissionHelper = permissionHelper
                    )
                }
            }
        }

        if (showDialog) {
            BillDialog(
                editingBill = editingBill,
                onDismiss = {
                    showDialog = false
                    editingBill = null
                },
                onConfirm = { month, amount ->
                    if (editingBill != null) {
                        viewModel.updateBill(editingBill!!.id, month, amount)
                    } else {
                        viewModel.addBill(month, amount)
                    }
                    showDialog = false
                    editingBill = null
                }
            )
        }
    }
}

@Composable
private fun BillItemCard(
    bill: BillEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    permissionHelper: PermissionHelper
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Card(elevation = CardDefaults.cardElevation(2.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = bill.billingMonth,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(bill.dateAdded)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = "Rs. ${bill.amount}",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.titleMedium
            )
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, "Edit", tint = MaterialTheme.colorScheme.primary)
            }
            // Only show delete button for owners
            if (permissionHelper.canDelete(PermissionHelper.FEATURE_BILLS)) {
                IconButton(onClick = { showDeleteDialog = true }) {
                    Icon(Icons.Default.Delete, "Delete", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Bill") },
            text = { Text("Are you sure you want to delete this bill?") },
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BillDialog(
    editingBill: BillEntity?,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var month by remember(editingBill) { mutableStateOf(editingBill?.billingMonth ?: "") }
    var amount by remember(editingBill) { mutableStateOf(editingBill?.amount?.toString() ?: "") }
    val isValid = month.isNotBlank() && amount.toDoubleOrNull() != null && amount.toDoubleOrNull()!! > 0
    
    var expanded by remember { mutableStateOf(false) }
    val months = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (editingBill != null) "Edit Bill" else "Add Bill") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                // Month Dropdown
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = month,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Billing Month") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        months.forEach { monthName ->
                            DropdownMenuItem(
                                text = { Text(monthName) },
                                onClick = {
                                    month = monthName
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(month, amount) },
                enabled = isValid
            ) {
                Text(if (editingBill != null) "Update" else "Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
