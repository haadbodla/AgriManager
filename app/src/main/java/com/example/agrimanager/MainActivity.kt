package com.example.agrimanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.agrimanager.data.repository.AuthRepository
import com.example.agrimanager.ui.auth.LoginScreen
import com.example.agrimanager.ui.bill.BillListScreen
import com.example.agrimanager.ui.dashboard.DashboardScreen
import com.example.agrimanager.ui.fuel.FuelLogScreen
import com.example.agrimanager.ui.location.LocationListScreen
import com.example.agrimanager.ui.machine.MachineListScreen
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
                        onLogoutClick = {
                            authRepository.signOut()
                            navController.navigate("login") {
                                popUpTo("dashboard") { inclusive = true }
                            }
                        }
                    )
                }

                // ... (The rest of your routes remain exactly the same) ...

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
            }
        }
    }
}