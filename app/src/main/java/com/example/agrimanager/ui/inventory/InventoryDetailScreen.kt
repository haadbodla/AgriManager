package com.example.agrimanager.ui.inventory

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryDetailScreen(
    itemId: Int,
    onNavigateBack: () -> Unit,
    viewModel: InventoryViewModel = hiltViewModel()
) {
    val item by viewModel.selectedItem.collectAsState()
    val history by viewModel.selectedItemHistory.collectAsState()

    // Load data when screen opens
    LaunchedEffect(itemId) {
        viewModel.selectItem(itemId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(item?.name ?: "Stock Ledger") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (item == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(modifier = Modifier.fillMaxSize().padding(padding)) {

                // 1. Summary Card
                Surface(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Current Stock", style = MaterialTheme.typography.labelMedium)
                        Text(
                            "${item!!.currentQuantity} ${item!!.unit}",
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Button(
                                onClick = { viewModel.openPurchaseDialog(isNew = false) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                            ) {
                                Icon(Icons.Default.Add, null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Restock")
                            }
                            Button(
                                onClick = { viewModel.openStockOutDialog(item!!) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828))
                            ) {
                                Icon(Icons.Default.Remove, null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Use")
                            }
                        }
                    }
                }

                Text(
                    "Stock History",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )

                // 2. History List
                if (history.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("No history yet", color = Color.Gray)
                    }
                } else {
                    LazyColumn(contentPadding = PaddingValues(bottom = 16.dp)) {
                        items(history) { record ->
                            HistoryItem(record, item!!.unit)
                        }
                    }
                }
            }
        }

        // Reuse Dialogs from ViewModel
        val showPurchase by viewModel.showPurchaseDialog.collectAsState()
        val showStockOut by viewModel.showStockOutDialog.collectAsState()
        val locations by viewModel.locations.collectAsState()
        val employees by viewModel.employees.collectAsState()
        val inventoryItems by viewModel.inventoryItems.collectAsState()

        if (showPurchase) {
            RecordPurchaseDialog(
                isNewItem = false,
                existingItems = inventoryItems.filter { it.id == itemId },
                onDismiss = { viewModel.closePurchaseDialog() },
                onConfirmNew = { _,_,_,_,_,_ -> },
                onConfirmExisting = { id, qty, cost ->
                    viewModel.recordPurchaseForExistingItem(id, qty, cost)
                    viewModel.closePurchaseDialog()
                }
            )
        }

        showStockOut?.let {
            StockOutDialog(
                item = it,
                locations = locations,
                employees = employees,
                onDismiss = { viewModel.closeStockOutDialog() },
                onConfirm = { qty, loc, emp ->
                    viewModel.recordStockOut(it.id, qty, loc, emp)
                    viewModel.closeStockOutDialog()
                }
            )
        }
    }
}

@Composable
fun HistoryItem(model: StockHistoryUiModel, unit: String) {
    val tx = model.transaction
    val isIn = tx.type == "IN"
    val date = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(tx.date))
    val color = if (isIn) Color(0xFF2E7D32) else Color(0xFFC62828)

    ListItem(
        leadingContent = {
            Box(
                modifier = Modifier.size(40.dp).clip(CircleShape).background(color.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isIn) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                    contentDescription = null,
                    tint = color
                )
            }
        },
        headlineContent = {
            if (isIn) {
                Text("Purchase", fontWeight = FontWeight.SemiBold)
            } else {
                // Here we show the resolved name!
                Text("Used @ ${model.locationName ?: "Unknown Location"}", fontWeight = FontWeight.SemiBold)
            }
        },
        supportingContent = {
            Column {
                Text(date, style = MaterialTheme.typography.bodySmall)
                if (!isIn && model.employeeName != null) {
                    Text("By: ${model.employeeName}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
                if (isIn && tx.totalCost != null) {
                    Text("Cost: ₹${tx.totalCost}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
            }
        },
        trailingContent = {
            Text(
                "${if (isIn) "+" else "-"}${tx.quantity.toInt()} $unit",
                fontWeight = FontWeight.Bold,
                color = color,
                style = MaterialTheme.typography.titleMedium
            )
        }
    )
    Divider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
}
