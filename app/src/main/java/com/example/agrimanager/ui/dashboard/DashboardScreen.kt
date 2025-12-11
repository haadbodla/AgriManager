package com.example.agrimanager.ui.dashboard

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
import androidx.compose.animation.core.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.components.ActivityComponent
import com.example.agrimanager.utils.PermissionHelper
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onFuelClick: () -> Unit,
    onBillClick: () -> Unit,
    onSalaryClick: () -> Unit,
    onInventoryClick: () -> Unit,
    onLaborClick: () -> Unit,
    onMaintenanceClick: () -> Unit,
    onAnalyticsClick: () -> Unit,
    onManageUsersClick: () -> Unit,  // NEW: Navigate to user management
    onLogoutClick: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    // Get PermissionHelper from Hilt
    val context = LocalContext.current
    val permissionHelper = remember {
        EntryPointAccessors.fromApplication(
            context.applicationContext,
            PermissionHelperEntryPoint::class.java
        ).permissionHelper()
    }
    // 1. Define the Menu Data
    val menuItems = listOf(
        DashboardItem("Fuel", Color(0xFFFFC107), Icons.Default.LocalGasStation) { onFuelClick() },
        DashboardItem("Labor", Color(0xFF2196F3), Icons.Default.Person) { onLaborClick() },
        DashboardItem("Stock", Color(0xFF4CAF50), Icons.Default.Inventory) { onInventoryClick() },
        DashboardItem("Bill", Color(0xFF9C27B0), Icons.Default.Receipt) { onBillClick() },
        DashboardItem("Salary", Color(0xFF009688), Icons.Default.AttachMoney) { onSalaryClick() },
        DashboardItem("Maint", Color(0xFFFF5722), Icons.Default.Build) { onMaintenanceClick() }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("AgriManager", fontWeight = FontWeight.Bold)
                },
                actions = {
                    // Role Badge
                    RoleBadge(permissionHelper = permissionHelper)
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    // Refresh Button
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
            Text(
                "Quick Actions",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 16.dp).align(Alignment.Start)
            )

            // 2. The 2x3 Grid
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
            
            // NEW: Analytics Button - Only visible to owners
            if (permissionHelper.canAccessAnalytics()) {
                Button(
                    onClick = onAnalyticsClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF673AB7)
                    )
                ) {
                    Icon(Icons.Default.Analytics, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("View Expense Analytics", fontSize = 16.sp)
                }
                
                Spacer(modifier = Modifier.height(12.dp))
            }
            
            // NEW: Manage Users Button - Only visible to owners
            if (permissionHelper.canManageUsers()) {
                Button(
                    onClick = onManageUsersClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF3F51B5)
                    )
                ) {
                    Icon(Icons.Default.People, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Manage Users", fontSize = 16.sp)
                }
            }
        }
    }
}

// Helper Composable for the Circular Button
@Composable
fun DashboardButton(item: DashboardItem) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(70.dp) // Size of the circle
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
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = item.label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

// NEW: Role Badge Component
@Composable
fun RoleBadge(
    permissionHelper: PermissionHelper,
    modifier: Modifier = Modifier
) {
    val role = permissionHelper.getRoleDisplayName()
    
    // Only show badge if role is valid
    if (role != "Unknown") {
        val backgroundColor = if (permissionHelper.isOwner()) {
            Color(0xFF4CAF50)  // Green for owner
        } else {
            Color(0xFF2196F3)  // Blue for manager
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

// NEW: Refresh Button Component
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
        enabled = !isRefreshing
    ) {
        Icon(
            imageVector = Icons.Default.Refresh,
            contentDescription = if (isRefreshing) "Syncing..." else "Refresh Data",
            modifier = Modifier.rotate(rotation)
        )
    }
}

// Data Class for the items
data class DashboardItem(
    val label: String,
    val color: Color,
    val icon: ImageVector,
    val onClick: () -> Unit
)
