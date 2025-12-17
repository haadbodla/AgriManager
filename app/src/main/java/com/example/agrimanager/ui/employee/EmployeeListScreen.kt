package com.example.agrimanager.ui.employee

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.agrimanager.data.local.EmployeeEntity
import com.example.agrimanager.ui.fuel.NewDataTrackerEntryPoint
import com.example.agrimanager.utils.NewDataTracker
import com.example.agrimanager.utils.PermissionHelper
import dagger.hilt.android.EntryPointAccessors
import com.example.agrimanager.ui.dashboard.PermissionHelperEntryPoint

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmployeeListScreen(
    onNavigateBack: () -> Unit,
    navController: NavController,
    viewModel: EmployeeViewModel = hiltViewModel()
) {
    // Get PermissionHelper
    val context = LocalContext.current
    val permissionHelper = remember {
        EntryPointAccessors.fromApplication(
            context.applicationContext,
            PermissionHelperEntryPoint::class.java
        ).permissionHelper()
    }
    
    // Get NewDataTracker and mark module as seen when leaving
    val newDataTracker = remember {
        EntryPointAccessors.fromApplication(
            context.applicationContext,
            NewDataTrackerEntryPoint::class.java
        ).newDataTracker()
    }
    
    DisposableEffect(Unit) {
        onDispose {
            newDataTracker.markModuleAsSeen(NewDataTracker.MODULE_SALARY)
        }
    }
    
    val employees by viewModel.employeeList.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var employeeToEdit by remember { mutableStateOf<EmployeeEntity?>(null) }
    var employeeToDelete by remember { mutableStateOf<EmployeeEntity?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Employees") },
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
                Icon(Icons.Default.Add, contentDescription = "Add Employee")
            }
        }
    ) { paddingValues ->
        if (employees.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("No employees yet")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(employees) { employee ->
                    EmployeeCard(
                        employee = employee,
                        onClick = { navController.navigate("salary/${employee.id}") },
                        onEdit = {
                            employeeToEdit = employee
                            showEditDialog = true
                        },
                        onDelete = {
                            employeeToDelete = employee
                            showDeleteDialog = true
                        },
                        permissionHelper = permissionHelper
                    )
                }
            }
        }
        if (showAddDialog) {
            AddEmployeeDialog(
                onDismiss = { showAddDialog = false },
                onSave = { name, salary ->
                    viewModel.addEmployee(name, salary)
                    showAddDialog = false
                }
            )
        }

        if (showEditDialog && employeeToEdit != null) {
            EditEmployeeDialog(
                employee = employeeToEdit!!,
                onDismiss = {
                    showEditDialog = false
                    employeeToEdit = null
                },
                onSave = { newName, newSalary ->
                    viewModel.updateEmployee(employeeToEdit!!, newName, newSalary)
                    showEditDialog = false
                    employeeToEdit = null
                }
            )
        }

        if (showDeleteDialog && employeeToDelete != null) {
            DeleteEmployeeConfirmationDialog(
                employeeName = employeeToDelete!!.name,
                onConfirm = {
                    viewModel.deleteEmployee(employeeToDelete!!)
                    showDeleteDialog = false
                    employeeToDelete = null
                },
                onDismiss = {
                    showDeleteDialog = false
                    employeeToDelete = null
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EmployeeCard(
    employee: EmployeeEntity,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    permissionHelper: PermissionHelper
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
                    text = employee.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Base Salary: Rs ${employee.baseSalary}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            // Edit button
            IconButton(onClick = onEdit) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit Employee",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            // Delete button - Only show for owners
            if (permissionHelper.canDelete(PermissionHelper.FEATURE_EMPLOYEES)) {
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Employee",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
private fun AddEmployeeDialog(
    onDismiss: () -> Unit,
    onSave: (String, Double) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var salaryText by remember { mutableStateOf("") }
    val isValid = name.isNotBlank() && salaryText.toDoubleOrNull() != null && salaryText.toDoubleOrNull()!! > 0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Employee") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Employee Name") }
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = salaryText,
                    onValueChange = { salaryText = it },
                    label = { Text("Base Salary") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(name, salaryText.toDouble()) },
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
private fun EditEmployeeDialog(
    employee: EmployeeEntity,
    onDismiss: () -> Unit,
    onSave: (String, Double) -> Unit
) {
    var name by remember { mutableStateOf(employee.name) }
    var salaryText by remember { mutableStateOf(employee.baseSalary.toString()) }
    val salary = salaryText.toDoubleOrNull()
    val isValid = name.isNotBlank() && 
                  salary != null && 
                  salary > 0 &&
                  (name != employee.name || salary != employee.baseSalary)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Employee") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Employee Name") }
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = salaryText,
                    onValueChange = { salaryText = it },
                    label = { Text("Base Salary") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(name, salary!!) },
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
private fun DeleteEmployeeConfirmationDialog(
    employeeName: String,
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
        title = { Text("Delete Employee?") },
        text = {
            Column {
                Text("Are you sure you want to delete \"$employeeName\"?")
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "This will also permanently delete:",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
                Text("• All salary transactions (advances & payments)")
                Text("• All labor logs")
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