package com.example.agrimanager.ui.inventory

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.agrimanager.data.local.InventoryItemEntity
import com.example.agrimanager.data.local.LocationEntity
import com.example.agrimanager.data.local.EmployeeEntity


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryListScreen(
    onNavigateBack: () -> Unit,
    onItemClick: (Int) -> Unit,
    viewModel: InventoryViewModel = hiltViewModel()
) {
    val inventoryItems by viewModel.inventoryItems.collectAsState()
    val showPurchaseDialog by viewModel.showPurchaseDialog.collectAsState()
    val showStockOutDialog by viewModel.showStockOutDialog.collectAsState()
    val isNewItem by viewModel.isNewItem.collectAsState()
    val locations by viewModel.locations.collectAsState()
    val employees by viewModel.employees.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Inventory") },
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
            FloatingActionButton(
                onClick = { viewModel.openPurchaseDialog(isNew = true) },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, "Record Purchase")
            }
        }
    ) { padding ->
        if (inventoryItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "No items in inventory",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(inventoryItems) { item ->
                    InventoryItemCard(
                        item = item,
                        onClick = { onItemClick(item.id) }
                    )
                }
            }
        }

        // Dialogs
        if (showPurchaseDialog) {
            RecordPurchaseDialog(
                isNewItem = isNewItem,
                existingItems = inventoryItems,
                onDismiss = { viewModel.closePurchaseDialog() },
                onConfirmNew = { name, category, unit, reorderLevel, quantity, totalCost ->
                    viewModel.addNewItem(name, category, unit, reorderLevel, quantity, totalCost)
                    viewModel.closePurchaseDialog()
                },
                onConfirmExisting = { itemId, quantity, totalCost ->
                    viewModel.recordPurchaseForExistingItem(itemId, quantity, totalCost)
                    viewModel.closePurchaseDialog()
                }
            )
        }

        showStockOutDialog?.let { item ->
            StockOutDialog(
                item = item,
                locations = locations,
                employees = employees,
                onDismiss = { viewModel.closeStockOutDialog() },
                onConfirm = { quantity, locationId, employeeId ->
                    viewModel.recordStockOut(item.id, quantity, locationId, employeeId)
                    viewModel.closeStockOutDialog()
                }
            )
        }
    }
}

@Composable
fun InventoryItemCard(
    item: InventoryItemEntity,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = item.category,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // OUT badge
                if (item.currentQuantity <= 0) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.error
                    ) {
                        Text(
                            text = "OUT",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onError,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Stock info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Current: ${item.currentQuantity} ${item.unit}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Reorder: ${item.reorderLevel.toInt()}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Linear progress indicator with proper calculation
            // Calculate progress based on current quantity vs a reasonable maximum
            // Use reorderLevel * 10 as the "full" amount for better visualization
            val maxQuantity = item.reorderLevel * 10
            val progress = if (maxQuantity > 0) {
                (item.currentQuantity / maxQuantity).toFloat().coerceIn(0f, 1f)
            } else {
                0f  // If no reorder level set, show empty
            }
            
            // Color logic:
            // - Red: At or below reorder level (critical)
            // - Yellow: Between reorder level and middle (50% of max)
            // - Green: Above middle (healthy stock)
            val indicatorColor = when {
                item.currentQuantity <= 0 -> {
                    MaterialTheme.colorScheme.error // Red when out of stock
                }
                item.currentQuantity <= item.reorderLevel -> {
                    MaterialTheme.colorScheme.error // Red when at or below reorder level
                }
                item.currentQuantity <= (maxQuantity / 2) -> {
                    Color(0xFFFFC107) // Yellow/Amber when in middle range
                }
                else -> {
                    Color(0xFF4CAF50) // Green when stock is healthy
                }
            }

            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = indicatorColor,
                trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordPurchaseDialog(
    isNewItem: Boolean,
    existingItems: List<InventoryItemEntity>,
    onDismiss: () -> Unit,
    onConfirmNew: (String, String, String, Double, Double, Double) -> Unit,
    onConfirmExisting: (Int, Double, Double) -> Unit
) {
    var selectedTab by remember { mutableStateOf(if (isNewItem) 1 else 0) }
    var selectedItemId by remember { mutableStateOf<Int?>(null) }
    var itemName by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("Bag") }
    var reorderLevel by remember { mutableStateOf("10") }
    var quantity by remember { mutableStateOf("") }
    var totalCost by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Record Purchase") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Tabs
                TabRow(selectedTabIndex = selectedTab) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Existing") }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("New Item") }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (selectedTab == 0) {
                    // Existing item
                    var expanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = it }
                    ) {
                        OutlinedTextField(
                            value = existingItems.find { it.id == selectedItemId }?.name ?: "",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Select Item") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            existingItems.forEach { item ->
                                DropdownMenuItem(
                                    text = { Text(item.name) },
                                    onClick = {
                                        selectedItemId = item.id
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                } else {
                    // New item
                    OutlinedTextField(
                        value = itemName,
                        onValueChange = { itemName = it },
                        label = { Text("Item Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text("Category (e.g. Fertilizer)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = unit,
                        onValueChange = { unit = it },
                        label = { Text("Unit (e.g. Bag)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = reorderLevel,
                        onValueChange = { reorderLevel = it },
                        label = { Text("Reorder Level") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                OutlinedTextField(
                    value = quantity,
                    onValueChange = { newValue ->
                        // Allow digits and decimal point
                        if (newValue.isEmpty() || newValue.matches(Regex("^\\d*\\.?\\d*$"))) {
                            quantity = newValue
                        }
                    },
                    label = { Text("Quantity Bought") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = quantity.isNotEmpty() && quantity.toDoubleOrNull() == null,
                    supportingText = {
                        if (quantity.isNotEmpty() && quantity.toDoubleOrNull() == null) {
                            Text("Please enter a valid number")
                        }
                    }
                )
                OutlinedTextField(
                    value = totalCost,
                    onValueChange = { newValue ->
                        // Allow digits and decimal point
                        if (newValue.isEmpty() || newValue.matches(Regex("^\\d*\\.?\\d*$"))) {
                            totalCost = newValue
                        }
                    },
                    label = { Text("Total Cost") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = totalCost.isNotEmpty() && totalCost.toDoubleOrNull() == null,
                    supportingText = {
                        if (totalCost.isNotEmpty() && totalCost.toDoubleOrNull() == null) {
                            Text("Please enter a valid amount")
                        }
                    }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (selectedTab == 0 && selectedItemId != null) {
                        onConfirmExisting(
                            selectedItemId!!,
                            quantity.toDoubleOrNull() ?: 0.0,
                            totalCost.toDoubleOrNull() ?: 0.0
                        )
                    } else if (selectedTab == 1) {
                        onConfirmNew(
                            itemName,
                            category,
                            unit,
                            reorderLevel.toDoubleOrNull() ?: 10.0,
                            quantity.toDoubleOrNull() ?: 0.0,
                            totalCost.toDoubleOrNull() ?: 0.0
                        )
                    }
                },
                enabled = if (selectedTab == 0) {
                    selectedItemId != null && 
                    quantity.toDoubleOrNull() != null && 
                    totalCost.toDoubleOrNull() != null
                } else {
                    itemName.isNotBlank() && 
                    category.isNotBlank() && 
                    quantity.toDoubleOrNull() != null && 
                    totalCost.toDoubleOrNull() != null
                }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockOutDialog(
    item: InventoryItemEntity,
    locations: List<LocationEntity>,
    employees: List<EmployeeEntity>,
    onDismiss: () -> Unit,
    onConfirm: (Double, Int, Int) -> Unit
) {
    var quantity by remember { mutableStateOf("") }
    var selectedLocationId by remember { mutableStateOf<Int?>(null) }
    var selectedEmployeeId by remember { mutableStateOf<Int?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Stock Out: ${item.name}") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    label = { Text("Quantity (${item.unit})") },
                    modifier = Modifier.fillMaxWidth()
                )

                // Where used? dropdown
                var locationExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = locationExpanded,
                    onExpandedChange = { locationExpanded = it }
                ) {
                    OutlinedTextField(
                        value = locations.find { it.id == selectedLocationId }?.name ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Where used?") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(locationExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = locationExpanded,
                        onDismissRequest = { locationExpanded = false }
                    ) {
                        locations.forEach { location ->
                            DropdownMenuItem(
                                text = { Text(location.name) },
                                onClick = {
                                    selectedLocationId = location.id
                                    locationExpanded = false
                                }
                            )
                        }
                    }
                }

                // Who took it? dropdown
                var employeeExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = employeeExpanded,
                    onExpandedChange = { employeeExpanded = it }
                ) {
                    OutlinedTextField(
                        value = employees.find { it.id == selectedEmployeeId }?.name ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Who took it?") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(employeeExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = employeeExpanded,
                        onDismissRequest = { employeeExpanded = false }
                    ) {
                        employees.forEach { employee ->
                            DropdownMenuItem(
                                text = { Text(employee.name) },
                                onClick = {
                                    selectedEmployeeId = employee.id
                                    employeeExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val qty = quantity.toDoubleOrNull() ?: 0.0
                    if (qty > 0 && selectedLocationId != null && selectedEmployeeId != null) {
                        onConfirm(qty, selectedLocationId!!, selectedEmployeeId!!)
                    }
                },
                enabled = quantity.toDoubleOrNull() != null && 
                         selectedLocationId != null && 
                         selectedEmployeeId != null
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
