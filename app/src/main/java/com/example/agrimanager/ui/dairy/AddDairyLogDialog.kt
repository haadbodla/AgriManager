package com.example.agrimanager.ui.dairy

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.agrimanager.data.local.DairyCompanyEntity
import com.example.agrimanager.data.local.DairyLogEntity
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDairyLogDialog(
    companies: List<DairyCompanyEntity>,
    onDismiss: () -> Unit,
    onConfirm: (DairyLogEntity) -> Unit
) {
    var liters by remember { mutableStateOf("") }
    var selectedCompany by remember { mutableStateOf<DairyCompanyEntity?>(null) }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Milk Log") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Company Selection
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = selectedCompany?.name ?: "Select Company",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Company") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        companies.forEach { company ->
                            DropdownMenuItem(
                                text = { Text("${company.name} (Rs. ${company.ratePerLiter}/L)") },
                                onClick = {
                                    selectedCompany = company
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                // Liters Input
                OutlinedTextField(
                    value = liters,
                    onValueChange = { if (it.isEmpty() || it.toDoubleOrNull() != null) liters = it },
                    label = { Text("Liters") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )

                if (selectedCompany != null && liters.isNotEmpty()) {
                    val rate = selectedCompany!!.ratePerLiter
                    val l = liters.toDoubleOrNull() ?: 0.0
                    val total = l * rate
                    Text(
                        text = "Total Amount: Rs. ${String.format("%.2f", total)}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val l = liters.toDoubleOrNull() ?: 0.0
                    val company = selectedCompany
                    if (l > 0 && company != null) {
                        onConfirm(
                            DairyLogEntity(
                                date = System.currentTimeMillis(),
                                companyId = company.id,
                                companyName = company.name,
                                liters = l,
                                totalAmount = l * company.ratePerLiter
                            )
                        )
                    }
                },
                enabled = liters.isNotEmpty() && selectedCompany != null
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
