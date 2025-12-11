package com.example.agrimanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavController
import androidx.navigation.navArgument
import com.example.agrimanager.data.repository.AuthRepository
import com.example.agrimanager.ui.auth.LoginScreen
import com.example.agrimanager.ui.bill.LocationListScreen as BillLocationListScreen
import com.example.agrimanager.ui.bill.LocationBillsScreen
import com.example.agrimanager.ui.dashboard.DashboardScreen
import com.example.agrimanager.ui.fuel.FuelLogScreen
import com.example.agrimanager.ui.location.LocationListScreen
import com.example.agrimanager.ui.machine.MachineListScreen
import com.example.agrimanager.ui.employee.EmployeeListScreen
import com.example.agrimanager.ui.employee.SalaryScreen
import com.example.agrimanager.ui.inventory.InventoryListScreen
import com.example.agrimanager.ui.labor.AddLaborLogScreen
import com.example.agrimanager.ui.labor.LaborListScreen
import com.example.agrimanager.ui.maintenance.AddMaintenanceLogScreen
import com.example.agrimanager.ui.maintenance.MaintenanceListScreen
import com.example.agrimanager.ui.inventory.InventoryDetailScreen
import com.example.agrimanager.ui.analytics.AnalyticsScreen
import com.example.agrimanager.ui.users.UserManagementScreen  // NEW: Import UserManagementScreen
import com.google.firebase.FirebaseApp
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var authRepository: AuthRepository // Inject Repo to check login status

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        
        // Check if this is first launch after install
        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        val isFirstLaunch = prefs.getBoolean("is_first_launch", true)
        
        setContent {
            val navController = rememberNavController()

            // Determine start screen dynamically
            // If first launch, always show login (even if Firebase has session)
            // Otherwise, check if user is logged in
            val startScreen = if (isFirstLaunch) {
                // Mark as not first launch anymore
                prefs.edit().putBoolean("is_first_launch", false).apply()
                "login"
            } else {
                if (authRepository.getCurrentUser() != null) "dashboard" else "login"
            }

            NavHost(navController = navController, startDestination = startScreen) {

                // 1. Login Screen
                composable("login") {
                    LoginScreen(
                        onLoginSuccess = {
                            // Clear login from backstack so back button exits app
                            navController.navigate("dashboard") {
                                popUpTo("login") { inclusive = true }
                            }
                        }
                    )
                }

                // 2. Dashboard
                composable("dashboard") {
                    DashboardScreen(
                        onFuelClick = { navController.navigate("machine_list") },
                        onBillClick = { navController.navigate("bill_list") },
                        onSalaryClick = { navController.navigate("employee_list") },
                        onInventoryClick = { navController.navigate("inventory_list") },
                        onLaborClick = { navController.navigate("labor_list") },
                        onMaintenanceClick = { navController.navigate("maintenance_list") },
                        onAnalyticsClick = { navController.navigate("analytics") },
                        onManageUsersClick = { navController.navigate("user_management") },  // NEW: Navigate to user management
                        onLogoutClick = {
                            authRepository.signOut()
                            navController.navigate("login") {
                                popUpTo("dashboard") { inclusive = true }
                            }
                        }
                    )
                }

                // Employee List
                composable("employee_list") {
                    EmployeeListScreen(
                        onNavigateBack = { navController.popBackStack() },
                        navController = navController
                    )
                }

                // Salary Screen
                composable(
                    "salary/{employeeId}",
                    arguments = listOf(navArgument("employeeId") { type = NavType.IntType })
                ) { backStackEntry ->
                    val id = backStackEntry.arguments?.getInt("employeeId") ?: -1
                    SalaryScreen(employeeId = id, navController = navController)
                }

                // Existing routes
                composable("machine_list") {
                    MachineListScreen(
                        onNavigateBack = { navController.popBackStack() },
                        onMachineClick = { id -> navController.navigate("fuel_logs/$id") }
                    )
                }

                composable("fuel_logs/{machineId}", arguments = listOf(navArgument("machineId") { type = NavType.IntType })) { backStackEntry ->
                    val machineId = backStackEntry.arguments?.getInt("machineId") ?: 0
                    FuelLogScreen(
                        machineId = machineId,
                        onBackClick = { navController.popBackStack() }
                    )
                }

                composable("bill_list") {
                    BillLocationListScreen(
                        onNavigateBack = { navController.popBackStack() },
                        navController = navController
                    )
                }

                composable(
                    "location-bills/{locationId}",
                    arguments = listOf(navArgument("locationId") { type = NavType.IntType })
                ) { backStackEntry ->
                    val id = backStackEntry.arguments?.getInt("locationId") ?: -1
                    LocationBillsScreen(locationId = id, navController = navController)
                }

                composable("location_list") {
                    LocationListScreen(onBackClick = { navController.popBackStack() })
                }

                composable("inventory_list") {
                    InventoryListScreen(
                        onNavigateBack = { navController.popBackStack() },
                        // ADD THIS: Navigate to Detail Screen
                        onItemClick = { itemId -> navController.navigate("inventory_detail/$itemId") }
                    )
                }
                composable(
                    "inventory_detail/{itemId}",
                    arguments = listOf(navArgument("itemId") { type = NavType.IntType })
                ) { backStackEntry ->
                    val id = backStackEntry.arguments?.getInt("itemId") ?: -1
                    InventoryDetailScreen(
                        itemId = id,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                composable("labor_list") {
                    LaborListScreen(
                        onNavigateBack = { navController.popBackStack() },
                        onAddLaborClick = { navController.navigate("add_labor_log") },
                        onEditLaborClick = { logId -> navController.navigate("add_labor_log/$logId") }
                    )
                }

                composable("add_labor_log") {
                    AddLaborLogScreen(onNavigateBack = { navController.popBackStack() })
                }

                composable(
                    route = "add_labor_log/{logId}",
                    arguments = listOf(navArgument("logId") { type = NavType.IntType })
                ) { backStackEntry ->
                    val logId = backStackEntry.arguments?.getInt("logId")
                    AddLaborLogScreen(
                        logId = logId,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                composable("maintenance_list") {
                    MaintenanceListScreen(
                        onNavigateBack = { navController.popBackStack() },
                        onAddMaintenanceClick = { navController.navigate("add_maintenance_log") },
                        onEditMaintenanceClick = { logId -> navController.navigate("add_maintenance_log/$logId") }
                    )
                }

                composable("add_maintenance_log") {
                    AddMaintenanceLogScreen(onNavigateBack = { navController.popBackStack() })
                }

                composable(
                    route = "add_maintenance_log/{logId}",
                    arguments = listOf(navArgument("logId") { type = NavType.IntType })
                ) { backStackEntry ->
                    val logId = backStackEntry.arguments?.getInt("logId")
                    AddMaintenanceLogScreen(
                        logId = logId,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                composable("analytics") {
                    AnalyticsScreen(onNavigateBack = { navController.popBackStack() })
                }

                // NEW: User Management Screen (Owner only)
                composable("user_management") {
                    UserManagementScreen(onNavigateBack = { navController.popBackStack() })
                }
            }


        }    }
}