package com.example.agrimanager.ui.employee

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.agrimanager.utils.ShareHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalaryScreen(
    employeeId: Int,
    navController: NavController,
    viewModel: SalaryViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val employee by viewModel.employee.collectAsState()
    val balance by viewModel.balance.collectAsState()
    val transactionsWithBalance by viewModel.transactionsWithBalance.collectAsState()
    val totalAdvances by viewModel.totalAdvancesValue.collectAsState()
    val totalCredits by viewModel.totalCreditsValue.collectAsState()

    var showAdvanceDialog by remember { mutableStateOf(false) }
    var showSalaryDialog by remember { mutableStateOf(false) }

    LaunchedEffect(employeeId) {
        viewModel.loadEmployee(employeeId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(employee?.name ?: "Employee Ledger") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Quick action buttons
                    IconButton(onClick = {
                        employee?.let { emp ->
                            ShareHelper.shareEmployeeLedger(
                                context = context,
                                employeeName = emp.name,
                                totalSalary = totalCredits,
                                totalAdvances = totalAdvances,
                                balance = balance,
                                transactionCount = transactionsWithBalance.size
                            )
                        }
                    }) {
                        Icon(Icons.Default.Share, "Share", tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = {
                        employee?.let { emp ->
                            ShareHelper.generateEmployeeLedgerPDF(
                                context = context,
                                employeeName = emp.name,
                                baseSalary = emp.baseSalary,
                                totalSalary = totalCredits,
                                totalAdvances = totalAdvances,
                                balance = balance,
                                transactions = transactionsWithBalance
                            )
                        }
                    }) {
                        Icon(Icons.Default.PictureAsPdf, "PDF", tint = MaterialTheme.colorScheme.primary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 1. Enhanced Balance Summary Card
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                tonalElevation = 4.dp
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Current Balance",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    val bal = balance
                    val isPositive = bal >= 0

                    Text(
                        text = if(isPositive) "Rs ${bal.toInt()}" else "- Rs ${kotlin.math.abs(bal.toInt())}",
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isPositive) Color(0xFF2E7D32) else Color(0xFFC62828)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Surface(
                        color = if(isPositive) Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = if (isPositive) "Payable to Employee" else "Recoverable from Employee",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = if (isPositive) Color(0xFF1B5E20) else Color(0xFFB71C1C)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    // Mini stats row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        MiniStatCard(
                            label = "Total",
                            value = "Rs ${totalCredits.toInt()}",
                            sublabel = "Salary Paid"
                        )
                        MiniStatCard(
                            label = "Total",
                            value = "Rs ${totalAdvances.toInt()}",
                            sublabel = "Advances"
                        )
                        employee?.let {
                            MiniStatCard(
                                label = "Base",
                                value = "Rs ${it.baseSalary.toInt()}",
                                sublabel = "/month"
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(20.dp))

                    // Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { showAdvanceDialog = true },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935))
                        ) {
                            Icon(Icons.Default.ArrowUpward, null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Give Advance")
                        }
                        Button(
                            onClick = { showSalaryDialog = true },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF43A047))
                        ) {
                            Icon(Icons.Default.ArrowDownward, null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Salary")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. Transaction History Header with two columns
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = "Date",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "Advances",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.End,
                        color = Color(0xFFC62828),
                        modifier = Modifier.weight(0.7f)
                    )
                    Text(
                        text = "Salary",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.End,
                        color = Color(0xFF2E7D32),
                        modifier = Modifier.weight(0.7f)
                    )
                }
            }

            // 3. Transaction List with two columns
            if (transactionsWithBalance.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.AccountBalance,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "No transactions yet",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(transactionsWithBalance) { txWithBalance ->
                        TwoColumnTransactionItem(
                            txWithBalance = txWithBalance,
                            onClick = {
                                navController.navigate(
                                    "transaction_detail/${txWithBalance.transaction.id}/$employeeId"
                                )
                            }
                        )
                    }
                }
            }
        }

        // Dialogs
        if (showAdvanceDialog) {
            AdvanceDialog(
                onDismiss = { showAdvanceDialog = false },
                onConfirm = { amount ->
                    viewModel.giveAdvance(amount)
                    showAdvanceDialog = false
                }
            )
        }
        
        if (showSalaryDialog) {
            SalaryDialog(
                baseSalary = employee?.baseSalary ?: 0.0,
                onDismiss = { showSalaryDialog = false },
                onConfirm = {
                    viewModel.addSalary()
                    showSalaryDialog = false
                }
            )
        }
    }
}

@Composable
fun MiniStatCard(
    label: String,
    value: String,
    sublabel: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(8.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Text(
            text = sublabel,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
        )
    }
}

@Composable
fun TwoColumnTransactionItem(
    txWithBalance: TransactionWithBalance,
    onClick: () -> Unit = {}
) {
    val tx = txWithBalance.transaction
    val runningBalance = txWithBalance.runningBalance
    val isDebit = tx.type == "DEBIT"
    val date = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(tx.timestamp))

    Column(
        modifier = Modifier.clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Date column
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = date,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Bal: Rs ${runningBalance.toInt()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (runningBalance >= 0) Color(0xFF2E7D32) else Color(0xFFC62828),
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Advances column (DEBIT)
            Text(
                text = if (isDebit) "Rs ${tx.amount.toInt()}" else "",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = if (isDebit) FontWeight.Bold else FontWeight.Normal,
                color = if (isDebit) Color(0xFFC62828) else Color.Transparent,
                textAlign = TextAlign.End,
                modifier = Modifier.weight(0.7f)
            )

            // Salary column (CREDIT)
            Text(
                text = if (!isDebit) "Rs ${tx.amount.toInt()}" else "",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = if (!isDebit) FontWeight.Bold else FontWeight.Normal,
                color = if (!isDebit) Color(0xFF2E7D32) else Color.Transparent,
                textAlign = TextAlign.End,
                modifier = Modifier.weight(0.7f)
            )
        }
        
        Divider(
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}

@Composable
private fun AdvanceDialog(
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    var amountText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                Icons.Default.ArrowUpward,
                contentDescription = null,
                tint = Color(0xFFC62828),
                modifier = Modifier.size(32.dp)
            )
        },
        title = { Text("Give Advance") },
        text = {
            OutlinedTextField(
                value = amountText,
                onValueChange = { 
                    if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) {
                        amountText = it
                    }
                },
                label = { Text("Enter Amount") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                prefix = { Text("Rs ") },
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    amountText.toDoubleOrNull()?.let {
                        if (it > 0) onConfirm(it)
                    }
                },
                enabled = amountText.toDoubleOrNull()?.let { it > 0 } == true,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828))
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun SalaryDialog(
    baseSalary: Double,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                Icons.Default.ArrowDownward,
                contentDescription = null,
                tint = Color(0xFF2E7D32),
                modifier = Modifier.size(32.dp)
            )
        },
        title = { Text("Add Salary") },
        text = {
            Column {
                Text(
                    "Add monthly salary payment?",
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(16.dp))
                Surface(
                    color = Color(0xFFE8F5E9),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Amount:",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            "Rs ${baseSalary.toInt()}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2E7D32)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
