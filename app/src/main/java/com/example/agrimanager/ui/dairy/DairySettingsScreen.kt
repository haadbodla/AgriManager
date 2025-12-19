package com.example.agrimanager.ui.dairy

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.agrimanager.data.local.DairyCompanyEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DairySettingsScreen(
    companies: List<DairyCompanyEntity>,
    onNavigateBack: () -> Unit,
    onAddCompany: (String, Double) -> Unit,
    onUpdateCompany: (DairyCompanyEntity) -> Unit,
    onDeleteCompany: (DairyCompanyEntity) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var editingCompany by remember { mutableStateOf<DairyCompanyEntity?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dairy Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Company")
            }
        }
    ) { padding ->
        if (companies.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No companies added yet. Add one to start logging milk.")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(companies) { company ->
                    CompanyCard(
                        company = company,
                        onEdit = { editingCompany = company },
                        onDelete = { onDeleteCompany(company) }
                    )
                }
            }
        }

        if (showAddDialog) {
            AddCompanyDialog(
                onDismiss = { showAddDialog = false },
                onConfirm = { name, rate ->
                    onAddCompany(name, rate)
                    showAddDialog = false
                }
            )
        }

        editingCompany?.let { company ->
            AddCompanyDialog(
                initialName = company.name,
                initialRate = company.ratePerLiter.toString(),
                onDismiss = { editingCompany = null },
                onConfirm = { name, rate ->
                    onUpdateCompany(company.copy(name = name, ratePerLiter = rate))
                    editingCompany = null
                },
                isEdit = true
            )
        }
    }
}

@Composable
fun CompanyCard(
    company: DairyCompanyEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(company.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("Rate: Rs. ${company.ratePerLiter}/L", style = MaterialTheme.typography.bodyMedium)
            }
            Row {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
fun AddCompanyDialog(
    initialName: String = "",
    initialRate: String = "",
    onDismiss: () -> Unit,
    onConfirm: (String, Double) -> Unit,
    isEdit: Boolean = false
) {
    var name by remember { mutableStateOf(initialName) }
    var rate by remember { mutableStateOf(initialRate) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isEdit) "Edit Company" else "Add Company") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Company Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = rate,
                    onValueChange = { if (it.isEmpty() || it.toDoubleOrNull() != null) rate = it },
                    label = { Text("Rate per Liter") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val r = rate.toDoubleOrNull() ?: 0.0
                    if (name.isNotEmpty() && r > 0) {
                        onConfirm(name, r)
                    }
                },
                enabled = name.isNotEmpty() && rate.isNotEmpty()
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
