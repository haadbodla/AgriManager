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
    val fuelLogs: List<com.example.agrimanager.data.local.FuelLogEntity>?,
    val employees: List<com.example.agrimanager.data.local.EmployeeEntity>?,
    val transactions: List<com.example.agrimanager.data.local.TransactionEntity>?,
    val locations: List<com.example.agrimanager.data.local.LocationEntity>?,
    val bills: List<com.example.agrimanager.data.local.BillWithLocation>?,
    val inventoryItems: List<com.example.agrimanager.data.local.InventoryItemEntity>?,
    val stockTransactions: List<com.example.agrimanager.data.local.StockTransactionEntity>?,
    val laborLogs: List<com.example.agrimanager.data.local.LaborLogWithEmployee>?,
    val maintenanceLogs: List<com.example.agrimanager.data.local.MaintenanceLogWithMachine>?,
    val analytics: com.example.agrimanager.data.local.ExpenseBreakdown?
)
