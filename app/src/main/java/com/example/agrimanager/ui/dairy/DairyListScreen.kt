package com.example.agrimanager.ui.dairy

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.agrimanager.data.local.DairyLogEntity
import com.example.agrimanager.data.local.DairyCompanyEntity
import com.example.agrimanager.utils.PermissionHelper
import com.example.agrimanager.utils.NewDataTracker
import dagger.hilt.android.EntryPointAccessors
import com.example.agrimanager.ui.dashboard.NewDataTrackerEntryPoint
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DairyListScreen(
    logs: List<DairyLogEntity>,
    companies: List<DairyCompanyEntity>,
    permissionHelper: PermissionHelper,
    onNavigateBack: () -> Unit,
    onSettingsClick: () -> Unit,
    onAddLog: (DairyLogEntity) -> Unit,
    onDeleteLog: (DairyLogEntity) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    
    // Mark module as seen after 15 minutes of viewing
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(15 * 60 * 1000L) // 15 minutes
        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            NewDataTrackerEntryPoint::class.java
        )
        entryPoint.newDataTracker().markModuleAsSeen(NewDataTracker.MODULE_DAIRY)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dairy Logs") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (permissionHelper.isOwner()) {
                        IconButton(onClick = onSettingsClick) {
                            Icon(Icons.Default.Settings, contentDescription = "Settings")
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Log")
            }
        }
    ) { padding ->
        if (logs.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No dairy logs yet.")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(logs) { log ->
                    DairyLogCard(
                        log = log,
                        canDelete = permissionHelper.isOwner(),
                        onDelete = { onDeleteLog(log) }
                    )
                }
            }
        }

        if (showAddDialog) {
            AddDairyLogDialog(
                companies = companies,
                onDismiss = { showAddDialog = false },
                onConfirm = { log ->
                    onAddLog(log)
                    showAddDialog = false
                }
            )
        }
    }
}

@Composable
fun DairyLogCard(
    log: DairyLogEntity,
    canDelete: Boolean,
    onDelete: () -> Unit
) {
    val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
    
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
                Text(log.companyName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("${log.liters} Liters", style = MaterialTheme.typography.bodyLarge)
                Text(
                    "Total: Rs. ${String.format("%.2f", log.totalAmount)}",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    dateFormat.format(Date(log.date)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (canDelete) {
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}
