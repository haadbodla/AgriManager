package com.example.agrimanager.ui.inventory

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.agrimanager.utils.ShareHelper
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryDetailScreen(
    itemId: Int,
    navController: NavController,
    viewModel: InventoryViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val item by viewModel.selectedItem.collectAsState()
    val transactionsWithBalance by viewModel.transactionsWithBalance.collectAsState()
    val totalStockIn by viewModel.totalStockIn.collectAsState()
    val totalStockOut by viewModel.totalStockOut.collectAsState()
    val totalPurchaseCost by viewModel.totalPurchaseCost.collectAsState()

    // Load data when screen opens
    LaunchedEffect(itemId) {
        viewModel.selectItem(itemId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(item?.name ?: "Stock Ledger") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        item?.let { itm ->
                            ShareHelper.shareStockReport(
                                context = context,
                                itemName = itm.name,
                                currentStock = itm.currentQuantity,
                                unit = itm.unit,
                                totalIn = totalStockIn,
                                totalOut = totalStockOut,
                                transactionCount = transactionsWithBalance.size
                            )
                        }
                    }) {
                        Icon(Icons.Default.Share, "Share", tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = {
                        item?.let { itm ->
                            ShareHelper.generateStockReportPDF(
                                context = context,
                                itemName = itm.name,
                                currentStock = itm.currentQuantity,
                                unit = itm.unit,
                                totalIn = totalStockIn,
                                totalOut = totalStockOut,
                                totalCost = totalPurchaseCost,
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
    ) { padding ->
        if (item == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(modifier = Modifier.fillMaxSize().padding(padding)) {

                // 1. Enhanced Summary Card
                Surface(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shadowElevation = 4.dp
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Current Stock", style = MaterialTheme.typography.labelMedium)
                        Text(
                            "${item!!.currentQuantity.toInt()} ${item!!.unit}",
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Mini Stats
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            MiniStatCard("Stock In", "${totalStockIn.toInt()} ${item!!.unit}", Color(0xFF2E7D32))
                            MiniStatCard("Stock Out", "${totalStockOut.toInt()} ${item!!.unit}", Color(0xFFC62828))
                            MiniStatCard("Total Cost", "Rs ${totalPurchaseCost.toInt()}", Color(0xFF1976D2))
                        }
                        
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

                // 2. Two-Column Transaction List
                if (transactionsWithBalance.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.Inventory,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = Color.Gray.copy(alpha = 0.5f)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("No transactions yet", color = Color.Gray)
                        }
                    }
                } else {
                    LazyColumn(contentPadding = PaddingValues(bottom = 16.dp)) {
                        items(transactionsWithBalance) { txWithBalance ->
                            TwoColumnTransactionItem(
                                txWithBalance = txWithBalance,
                                unit = item!!.unit,
                                onClick = {
                                    navController.navigate(
                                        "stock_transaction_detail/${txWithBalance.transaction.id}/${item!!.id}"
                                    )
                                }
                            )
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
private fun MiniStatCard(label: String, value: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.1f),
        modifier = Modifier.width(100.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                label,
                style = MaterialTheme.typography.bodySmall,
                color = color.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun TwoColumnTransactionItem(
    txWithBalance: StockTransactionWithBalance,
    unit: String,
    onClick: () -> Unit
) {
    val tx = txWithBalance.transaction
    val isIn = tx.type == "IN"
    val date = SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date(tx.date))
    val time = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(tx.date))

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Date header
        Text(
            date,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        // Two-column row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Stock OUT column (left)
            if (!isIn) {
                TransactionCard(
                    quantity = tx.quantity,
                    unit = unit,
                    color = Color(0xFFC62828),
                    label = txWithBalance.locationName ?: "Used",
                    time = time,
                    modifier = Modifier.weight(1f)
                )
            } else {
                Spacer(modifier = Modifier.weight(1f))
            }
            
            // Stock IN column (right)
            if (isIn) {
                TransactionCard(
                    quantity = tx.quantity,
                    unit = unit,
                    color = Color(0xFF2E7D32),
                    label = "Purchase",
                    time = time,
                    cost = tx.totalCost,
                    modifier = Modifier.weight(1f)
                )
            } else {
                Spacer(modifier = Modifier.weight(1f))
            }
        }
        
        // Running balance
        Text(
            "Balance: ${txWithBalance.runningStock.toInt()} $unit",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(top = 4.dp)
        )
        
        Divider(
            modifier = Modifier.padding(top = 8.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    }
}

@Composable
private fun TransactionCard(
    quantity: Double,
    unit: String,
    color: Color,
    label: String,
    time: String,
    cost: Double? = null,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.1f)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = if (color == Color(0xFF2E7D32)) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    "${if (color == Color(0xFF2E7D32)) "+" else "-"}${quantity.toInt()} $unit",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            }
            Text(
                label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                time,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
            cost?.let {
                Text(
                    "Rs ${it.toInt()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }
    }
}
