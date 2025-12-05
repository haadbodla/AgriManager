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
import com.example.agrimanager.ui.bill.BillListScreen
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
        setContent {
            val navController = rememberNavController()

            // Determine start screen dynamically
            val startScreen = if (authRepository.getCurrentUser() != null) "dashboard" else "login"

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
                    EmployeeListScreen(navController = navController)
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
                    MachineListScreen(onMachineClick = { id -> navController.navigate("fuel_logs/$id") })
                }

                composable("fuel_logs/{machineId}", arguments = listOf(navArgument("machineId") { type = NavType.IntType })) {
                    FuelLogScreen(onBackClick = { navController.popBackStack() })
                }

                composable("bill_list") {
                    BillListScreen(
                        onBackClick = { navController.popBackStack() },
                        onManageLocationsClick = { navController.navigate("location_list") }
                    )
                }

                composable("location_list") {
                    LocationListScreen(onBackClick = { navController.popBackStack() })
                }

                composable("inventory_list") {
                    InventoryListScreen(onNavigateBack = { navController.popBackStack() })
                }

                composable("labor_list") {
                    LaborListScreen(
                        onNavigateBack = { navController.popBackStack() },
                        onAddLaborClick = { navController.navigate("add_labor_log") }
                    )
                }

                composable("add_labor_log") {
                    AddLaborLogScreen(onNavigateBack = { navController.popBackStack() })
                }

                composable("maintenance_list") {
                    MaintenanceListScreen(
                        onNavigateBack = { navController.popBackStack() },
                        onAddMaintenanceClick = { navController.navigate("add_maintenance_log") }
                    )
                }

                composable("add_maintenance_log") {
                    AddMaintenanceLogScreen(onNavigateBack = { navController.popBackStack() })
                }
            }


        }    }
}