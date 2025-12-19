package com.example.agrimanager.data.models

data class ExportConfig(
    val includeAll: Boolean = true,
    val includeMachines: Boolean = true,
    val includeFuel: Boolean = true,
    val includeEmployees: Boolean = true,
    val includeSalary: Boolean = true,
    val includeLocations: Boolean = true,
    val includeBills: Boolean = true,
    val includeInventory: Boolean = true,
    val includeLabor: Boolean = true,
    val includeMaintenance: Boolean = true,
    val includeAnalytics: Boolean = true,
    val includeDairy: Boolean = true,
    val startDate: Long? = null,
    val endDate: Long? = null
)

data class ExportData(
    val farmName: String,
    val exportDate: Long,
    val generatedBy: String,
    val userRole: String,
    val dateRange: String?,
    
    // Data sections
    val machines: List<com.example.agrimanager.data.local.MachineEntity>?,
    val fuelLogs: List<FuelLogWithMachine>?,
    val employees: List<com.example.agrimanager.data.local.EmployeeEntity>?,
    val transactions: List<TransactionWithEmployee>?,
    val locations: List<com.example.agrimanager.data.local.LocationEntity>?,
    val bills: List<com.example.agrimanager.data.local.BillWithLocation>?,
    val inventoryItems: List<com.example.agrimanager.data.local.InventoryItemEntity>?,
    val stockTransactions: List<StockTransactionWithItem>?,
    val laborLogs: List<com.example.agrimanager.data.local.LaborLogWithEmployee>?,
    val maintenanceLogs: List<com.example.agrimanager.data.local.MaintenanceLogWithMachine>?,
    val dairyLogs: List<com.example.agrimanager.data.local.DairyLogEntity>?,
    val analytics: com.example.agrimanager.data.local.ExpenseBreakdown?
)

// Helper data classes for PDF export (not database entities)
data class FuelLogWithMachine(
    val id: Int,
    val machineId: Int,
    val machineName: String,
    val liters: Double,
    val rate: Double,
    val totalCost: Double,
    val date: Long
)

data class TransactionWithEmployee(
    val id: Int,
    val employeeId: Int,
    val employeeName: String,
    val amount: Double,
    val type: String,
    val timestamp: Long
)

data class StockTransactionWithItem(
    val id: Int,
    val itemId: Int,
    val itemName: String,
    val type: String,  // "IN" or "OUT"
    val quantity: Double,
    val totalCost: Double?,
    val date: Long
)
