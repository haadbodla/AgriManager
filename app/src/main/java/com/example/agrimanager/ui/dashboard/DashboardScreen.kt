package com.example.agrimanager.ui.dashboard

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.agrimanager.ui.components.NotificationDot
import com.example.agrimanager.utils.NewDataTracker
import com.example.agrimanager.utils.PermissionHelper
import com.example.agrimanager.utils.SyncStatus
import dagger.hilt.android.EntryPointAccessors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onFuelClick: () -> Unit,
    onBillClick: () -> Unit,
    onSalaryClick: () -> Unit,
    onInventoryClick: () -> Unit,
    onLaborClick: () -> Unit,
    onMaintenanceClick: () -> Unit,
    onDairyClick: () -> Unit,
    onAnalyticsClick: () -> Unit,
    onManageUsersClick: () -> Unit,
    onExportClick: () -> Unit,
    onLogoutClick: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val syncStatus by viewModel.syncStatus.collectAsState()
    val newDataCounts by viewModel.newDataCounts.collectAsState()

    val context = LocalContext.current

    // Permission Helper Setup
    val permissionHelper = remember {
        EntryPointAccessors.fromApplication(
            context.applicationContext,
            PermissionHelperEntryPoint::class.java
        ).permissionHelper()
    }

    // User Data Setup
    val userEmail = remember {
        context.getSharedPreferences("agri_manager_prefs", android.content.Context.MODE_PRIVATE)
            .getString("user_email", "") ?: ""
    }
    val userName = remember(userEmail) {
        if (userEmail.contains("@")) userEmail.substringBefore("@") else "User"
    }

    // Menu Items Definition with new data counts
    val menuItems = listOf(
        DashboardItem("Fuel", Color(0xFFFFC107), Icons.Default.LocalGasStation, newDataCounts.fuel) {
            onFuelClick()
        },
        DashboardItem("Labor", Color(0xFF2196F3), Icons.Default.Person, newDataCounts.labor) {
            onLaborClick()
        },
        DashboardItem("Stock", Color(0xFF4CAF50), Icons.Default.Inventory, newDataCounts.inventory) {
            onInventoryClick()
        },
        DashboardItem("Bill", Color(0xFF9C27B0), Icons.Default.Receipt, newDataCounts.bills) {
            onBillClick()
        },
        DashboardItem("Salary", Color(0xFF009688), Icons.Default.AttachMoney, newDataCounts.salary) {
            onSalaryClick()
        },
        DashboardItem("Repair", Color(0xFFFF5722), Icons.Default.Build, newDataCounts.maintenance) {
            onMaintenanceClick()
        },
        DashboardItem("Dairy", Color(0xFF00BCD4), Icons.Default.LocalDrink, newDataCounts.dairy) {
            onDairyClick()
        }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("AgriManager", fontWeight = FontWeight.Bold)
                },
                actions = {
                    // 1. MOVED: Role Badge is now here in the Top Bar
                    RoleBadge(permissionHelper = permissionHelper)

                    Spacer(modifier = Modifier.width(8.dp))

                    RefreshButton(
                        isRefreshing = isRefreshing,
                        onClick = { viewModel.refreshData() }
                    )
                    IconButton(onClick = onLogoutClick) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Logout")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // --- HEADER SECTION (Welcome + Sync Status) ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left: Welcome Text
                Column {
                    Text(
                        text = "Welcome,",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                    Text(
                        text = userName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Right: Sync Status (Role Badge removed from here)
                SyncStatusIndicator(syncStatus = syncStatus)
            }
            // -------------------------------------------

            Text(
                "Quick Actions",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 16.dp).align(Alignment.Start)
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                verticalArrangement = Arrangement.spacedBy(24.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(menuItems) { item ->
                    DashboardButton(item)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Admin/Owner Buttons
            if (permissionHelper.canAccessAnalytics()) {
                Button(
                    onClick = onAnalyticsClick,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF673AB7))
                ) {
                    Icon(Icons.Default.Analytics, contentDescription = null)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("View Expense Analytics", fontSize = 16.sp)
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            if (permissionHelper.canManageUsers()) {
                Button(
                    onClick = onManageUsersClick,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3F51B5))
                ) {
                    Icon(Icons.Default.People, contentDescription = null)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Manage Users", fontSize = 16.sp)
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            Button(
                onClick = onExportClick,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
            ) {
                Icon(Icons.Default.PictureAsPdf, contentDescription = null)
                Spacer(modifier = Modifier.width(12.dp))
                Text("Export Data to PDF", fontSize = 16.sp)
            }
        }
    }
}

// ---------------- HELPER COMPONENTS & CLASSES ----------------

data class DashboardItem(
    val label: String,
    val color: Color,
    val icon: ImageVector,
    val newCount: Int = 0,  // Number of new entries for this module
    val onClick: () -> Unit
)

/**
 * Dashboard quick action button with optional red notification dot.
 * Shows dot when newCount > 0 (new data exists for this module).
 */
@Composable
fun DashboardButton(item: DashboardItem) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            contentAlignment = Alignment.Center
        ) {
            // Main circular button
            Box(
                modifier = Modifier
                    .size(70.dp)
                    .clip(CircleShape)
                    .background(item.color)
                    .clickable { item.onClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.label,
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
            
            // Red notification dot (top-right corner)
            if (item.newCount > 0) {
                NotificationDot(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 2.dp, y = 2.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = item.label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun RoleBadge(
    permissionHelper: PermissionHelper,
    modifier: Modifier = Modifier
) {
    val role = permissionHelper.getRoleDisplayName()

    if (role != "Unknown") {
        val backgroundColor = if (permissionHelper.isOwner()) {
            Color(0xFF4CAF50)
        } else {
            Color(0xFF2196F3)
        }

        Surface(
            modifier = modifier,
            color = backgroundColor,
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = role,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun RefreshButton(
    isRefreshing: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val rotation by animateFloatAsState(
        targetValue = if (isRefreshing) 360f else 0f,
        animationSpec = if (isRefreshing) {
            infiniteRepeatable(
                animation = tween(1000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            )
        } else {
            tween(0)
        },
        label = "refresh_rotation"
    )

    IconButton(
        onClick = onClick,
        enabled = !isRefreshing,
        modifier = modifier
    ) {
        Icon(
            imageVector = Icons.Default.Refresh,
            contentDescription = if (isRefreshing) "Syncing..." else "Refresh Data",
            modifier = Modifier.rotate(rotation)
        )
    }
}

@Composable
fun SyncStatusIndicator(
    syncStatus: SyncStatus,
    modifier: Modifier = Modifier
) {
    val (icon, color, text) = when (syncStatus) {
        is SyncStatus.Synced -> Triple(
            Icons.Default.CloudDone,
            Color(0xFF4CAF50), // Green
            "Synced"
        )
        is SyncStatus.Pending -> Triple(
            Icons.Default.CloudUpload,
            Color(0xFFFF9800), // Orange
            "Pending (${syncStatus.count})"
        )
        is SyncStatus.Syncing -> Triple(
            Icons.Default.Cloud,
            Color(0xFF2196F3), // Blue
            "Syncing..."
        )
        is SyncStatus.Offline -> Triple(
            Icons.Default.CloudOff,
            Color(0xFF9E9E9E), // Gray
            "Offline"
        )
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .background(
                color = color.copy(alpha = 0.15f),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = text,
            tint = color,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = color
        )
    }
}