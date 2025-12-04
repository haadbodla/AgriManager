package com.example.agrimanager.ui.employee

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.agrimanager.data.local.EmployeeEntity
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalaryScreen(
    employeeId: Int,
    navController: NavController,
    viewModel: SalaryViewModel = hiltViewModel()
) {
    // Load employee and transactions
    val employee by viewModel.employee.collectAsState()
    val balance by viewModel.balance.collectAsState()
    var showAdvanceDialog by remember { mutableStateOf(false) }
    LaunchedEffect(employeeId) {
        viewModel.loadEmployee(employeeId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(employee?.name ?: "Employee") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                shape = RoundedCornerShape(8.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = employee?.name ?: "",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Base Salary: ₹${employee?.baseSalary ?: 0.0}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    val balanceText = if ((balance ?: 0.0) >= 0) {
                        "₹${balance?.toInt()} Payable to Employee"
                    } else {
                        "₹${(-balance!!).toInt()} Recoverable from Employee"
                    }
                    Text(
                        text = balanceText,
                        color = if ((balance ?: 0.0) >= 0) Color(0xFF4CAF50) else Color(0xFFF44336),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = { showAdvanceDialog = true },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Give Advance")
                }
                Button(
                    onClick = { viewModel.addSalary() },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Add Salary")
                }
            }
        }
        if (showAdvanceDialog) {
            AdvanceDialog(
                onDismiss = { showAdvanceDialog = false },
                onConfirm = { amount ->
                    viewModel.giveAdvance(amount)
                    showAdvanceDialog = false
                }
            )
        }
    }
}

@Composable
private fun AdvanceDialog(
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    val isValid = amountText.toDoubleOrNull()?.let { it > 0 } == true
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Give Advance") },
        text = {
            OutlinedTextField(
                value = amountText,
                onValueChange = { amountText = it },
                label = { Text("Amount") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        },
        confirmButton = {
            Button(onClick = { onConfirm(amountText.toDouble()) }, enabled = isValid) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
