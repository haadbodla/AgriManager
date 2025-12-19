package com.example.agrimanager.data.repository

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.work.* // Imports WorkManager, Constraints, NetworkType, etc.
import com.example.agrimanager.data.local.*
import com.example.agrimanager.ui.*
import com.example.agrimanager.workers.FirestoreSyncWorker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.Calendar
import java.util.concurrent.TimeUnit // <--- Crucial import for time
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FarmRepository @Inject constructor(
    private val dao: FarmDao,
    private val userDao: UserDao,
    private val auth: FirebaseAuth,
    @ApplicationContext private val context: Context
) {

    // Helper to schedule the background sync
    private fun scheduleSync(collection: String, docId: String, data: Map<String, Any>, isDelete: Boolean = false) {
        if (auth.currentUser == null) return

        // 1. Build the Data Bundle
        val builder = Data.Builder()
            .putString("collection", collection)
            .putString("docId", docId)
            .putString("operation", if (isDelete) "delete" else "set")

        if (!isDelete) {
            data.forEach { (key, value) ->
                when (value) {
                    is String -> builder.putString(key, value)
                    is Int -> builder.putInt(key, value)
                    is Long -> builder.putLong(key, value)
                    is Double -> builder.putDouble(key, value)
                    is Boolean -> builder.putBoolean(key, value)
                }
            }
        }

        // 2. Set Constraints (MUST HAVE INTERNET)
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        // 3. Create the Work Request
        val syncRequest = OneTimeWorkRequestBuilder<FirestoreSyncWorker>()
            .setConstraints(constraints)
            .setInputData(builder.build())
            .addTag("firestore_sync")  // Tag for sync status tracking
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                WorkRequest.MIN_BACKOFF_MILLIS, // <--- FIXED: Use WorkRequest class
                TimeUnit.MILLISECONDS
            )
            .build()

        // 4. Enqueue (Run it!)
        WorkManager.getInstance(context).enqueue(syncRequest)
    }

    // ================== MACHINE OPERATIONS ==================

    fun getAllMachines(): Flow<List<MachineEntity>> = dao.getAllMachines()

    suspend fun insertMachine(machine: MachineEntity) {
        val id = dao.insertMachine(machine)
        val data = mapOf(
            "id" to id.toInt(),
            "name" to machine.name,
            "dateAdded" to machine.dateAdded
        )
        scheduleSync("machines", id.toString(), data)
    }

    suspend fun updateMachine(machine: MachineEntity) {
        dao.updateMachine(machine)
        val data = mapOf(
            "id" to machine.id,
            "name" to machine.name,
            "dateAdded" to machine.dateAdded
        )
        scheduleSync("machines", machine.id.toString(), data)
    }

    suspend fun deleteMachine(machine: MachineEntity) {
        // Get all child records before deleting
        val fuelLogs = dao.getFuelLogsForMachineList(machine.id)
        val maintenanceLogs = dao.getMaintenanceLogsForMachineList(machine.id)
        
        // Delete children from Room
        dao.deleteFuelLogsForMachine(machine.id)
        dao.deleteMaintenanceLogsForMachine(machine.id)
        
        // Delete parent from Room
        dao.deleteMachine(machine)
        
        // Delete children from Firestore
        fuelLogs.forEach { log ->
            scheduleSync("fuel_logs", log.id.toString(), emptyMap(), isDelete = true)
        }
        maintenanceLogs.forEach { log ->
            scheduleSync("maintenance_logs", log.id.toString(), emptyMap(), isDelete = true)
        }
        
        // Delete parent from Firestore
        scheduleSync("machines", machine.id.toString(), emptyMap(), isDelete = true)
    }

    suspend fun getMachineById(id: Int): MachineEntity? = dao.getMachineById(id)


    // ================== FUEL LOG OPERATIONS ==================

    fun getFuelLogs(machineId: Int): Flow<List<FuelLogEntity>> = dao.getFuelLogsForMachine(machineId)

    fun getTotalFuelCost(machineId: Int): Flow<Double?> = dao.getTotalCostForMachine(machineId)

    suspend fun insertFuelLog(log: FuelLogEntity) {
        val id = dao.insertFuelLog(log)
        val data = mapOf(
            "id" to id.toInt(),
            "machineId" to log.machineId,
            "date" to log.date,
            "liters" to log.liters,
            "rate" to log.rate,
            "totalCost" to log.totalCost,
            "hourMeterReading" to log.hourMeterReading
        )
        scheduleSync("fuel_logs", id.toString(), data)
    }

    suspend fun updateFuelLog(log: FuelLogEntity) {
        dao.updateFuelLog(log)
        val data = mapOf(
            "id" to log.id,
            "machineId" to log.machineId,
            "date" to log.date,
            "liters" to log.liters,
            "rate" to log.rate,
            "totalCost" to log.totalCost,
            "hourMeterReading" to log.hourMeterReading
        )
        scheduleSync("fuel_logs", log.id.toString(), data)
    }

    suspend fun deleteFuelLog(log: FuelLogEntity) {
        dao.deleteFuelLog(log)
        scheduleSync("fuel_logs", log.id.toString(), emptyMap(), isDelete = true)
    }

    suspend fun getFuelLogById(id: Int): FuelLogEntity? = dao.getFuelLogById(id)


    // ================== LOCATION OPERATIONS ==================

    fun getAllLocations(): Flow<List<LocationEntity>> = dao.getAllLocations()

    suspend fun insertLocation(location: LocationEntity) {
        val id = dao.insertLocation(location)
        val data = mapOf(
            "id" to id.toInt(),
            "name" to location.name
        )
        scheduleSync("locations", id.toString(), data)
    }

    suspend fun updateLocation(location: LocationEntity) {
        dao.updateLocation(location)
        val data = mapOf(
            "id" to location.id,
            "name" to location.name
        )
        scheduleSync("locations", location.id.toString(), data)
    }

    suspend fun deleteLocation(location: LocationEntity) {
        // Get all child records
        val bills = dao.getBillsForLocationList(location.id)
        
        // Delete children from Room
        dao.deleteBillsForLocation(location.id)
        
        // Delete parent from Room
        dao.deleteLocation(location)
        
        // Delete children from Firestore
        bills.forEach { bill ->
            scheduleSync("bills", bill.id.toString(), emptyMap(), isDelete = true)
        }
        
        // Delete parent from Firestore
        scheduleSync("locations", location.id.toString(), emptyMap(), isDelete = true)
    }

    suspend fun getLocationById(id: Int): LocationEntity? = dao.getLocationById(id)


    // ================== BILL OPERATIONS ==================

    fun getAllBillsWithLocation(): Flow<List<BillWithLocation>> = dao.getAllBillsWithLocation()
    fun getBills(locationId: Int): Flow<List<BillEntity>> = dao.getBillsForLocation(locationId)
    fun getTotalBill(locationId: Int): Flow<Double?> = dao.getTotalBillForLocation(locationId)

    suspend fun insertBill(bill: BillEntity) {
        val id = dao.insertBill(bill)
        val data = mapOf(
            "id" to id.toInt(),
            "locationId" to bill.locationId,
            "billingMonth" to bill.billingMonth,
            "amount" to bill.amount,
            "dateAdded" to bill.dateAdded
        )
        scheduleSync("bills", id.toString(), data)
    }

    suspend fun updateBill(bill: BillEntity) {
        dao.updateBill(bill)
        val data = mapOf(
            "id" to bill.id,
            "locationId" to bill.locationId,
            "billingMonth" to bill.billingMonth,
            "amount" to bill.amount,
            "dateAdded" to bill.dateAdded
        )
        scheduleSync("bills", bill.id.toString(), data)
    }

    suspend fun deleteBill(bill: BillEntity) {
        dao.deleteBill(bill)
        scheduleSync("bills", bill.id.toString(), emptyMap(), isDelete = true)
    }

    suspend fun getBillById(id: Int): BillEntity? = dao.getBillById(id)

    // ================== EMPLOYEE OPERATIONS ==================
    suspend fun insertEmployee(employee: EmployeeEntity) {
        val id = dao.insertEmployee(employee)
        val data = mapOf(
            "id" to id.toInt(),
            "name" to employee.name,
            "baseSalary" to employee.baseSalary
        )
        scheduleSync("employees", id.toString(), data)
    }

    suspend fun updateEmployee(employee: EmployeeEntity) {
        dao.updateEmployee(employee)
        val data = mapOf(
            "id" to employee.id,
            "name" to employee.name,
            "baseSalary" to employee.baseSalary
        )
        scheduleSync("employees", employee.id.toString(), data)
    }

    suspend fun deleteEmployee(employee: EmployeeEntity) {
        // Get all child records
        val transactions = dao.getTransactionsForEmployeeList(employee.id)
        val laborLogs = dao.getLaborLogsForEmployeeList(employee.id)
        
        // Delete children from Room
        dao.deleteTransactionsForEmployee(employee.id)
        dao.deleteLaborLogsForEmployee(employee.id)
        
        // Delete parent from Room
        dao.deleteEmployee(employee)
        
        // Delete children from Firestore
        transactions.forEach { transaction ->
            scheduleSync("transactions", transaction.id.toString(), emptyMap(), isDelete = true)
        }
        laborLogs.forEach { log ->
            scheduleSync("labor_logs", log.id.toString(), emptyMap(), isDelete = true)
        }
        
        // Delete parent from Firestore
        scheduleSync("employees", employee.id.toString(), emptyMap(), isDelete = true)
    }
    fun getAllEmployees(): Flow<List<EmployeeEntity>> = dao.getAllEmployees()

    suspend fun getEmployeeById(id: Int): EmployeeEntity? = dao.getEmployeeById(id)

    // ================== TRANSACTION OPERATIONS ==================
    suspend fun insertTransaction(transaction: TransactionEntity) {
        val id = dao.insertTransaction(transaction)
        val data = mapOf(
            "id" to id.toInt(),
            "employeeId" to transaction.employeeId,
            "amount" to transaction.amount,
            "type" to transaction.type,
            "timestamp" to transaction.timestamp
        )
        scheduleSync("transactions", id.toString(), data)
    }

    fun getTransactionsForEmployee(employeeId: Int): Flow<List<TransactionEntity>> = dao.getTransactionsForEmployee(employeeId)

    fun getTotalAdvances(employeeId: Int): Flow<Double?> = dao.getTotalAdvances(employeeId)


    // ================== INVENTORY OPERATIONS ==================
    
    fun getAllInventoryItems(): Flow<List<InventoryItemEntity>> = dao.getAllInventoryItems()
    
    suspend fun addInventoryItem(item: InventoryItemEntity) {
        val id = dao.insertInventoryItem(item)
        val data = mapOf(
            "id" to id.toInt(),
            "name" to item.name,
            "category" to item.category,
            "unit" to item.unit,
            "currentQuantity" to item.currentQuantity,
            "reorderLevel" to item.reorderLevel,
            "dateAdded" to item.dateAdded
        )
        scheduleSync("inventory_items", id.toString(), data)
    }
    
    suspend fun updateInventoryItem(item: InventoryItemEntity) {
        dao.updateInventoryItem(item)
        val data = mapOf(
            "id" to item.id,
            "name" to item.name,
            "category" to item.category,
            "unit" to item.unit,
            "currentQuantity" to item.currentQuantity,
            "reorderLevel" to item.reorderLevel,
            "dateAdded" to item.dateAdded
        )
        scheduleSync("inventory_items", item.id.toString(), data)
    }
    
    suspend fun recordPurchase(itemId: Int, quantity: Double, totalCost: Double) {
        // Get current item
        val item = dao.getInventoryItemById(itemId) ?: return
        
        // Update quantity
        val newQuantity = item.currentQuantity + quantity
        dao.updateInventoryQuantity(itemId, newQuantity)
        
        // Sync updated quantity to Firestore
        val itemData = mapOf(
            "id" to itemId,
            "name" to item.name,
            "category" to item.category,
            "unit" to item.unit,
            "currentQuantity" to newQuantity,
            "reorderLevel" to item.reorderLevel,
            "dateAdded" to item.dateAdded
        )
        scheduleSync("inventory_items", itemId.toString(), itemData)
        
        // Record transaction
        val transaction = StockTransactionEntity(
            itemId = itemId,
            type = "IN",
            quantity = quantity,
            totalCost = totalCost
        )
        val transId = dao.insertStockTransaction(transaction)
        
        // Sync transaction to Firestore
        val transData = mapOf(
            "id" to transId.toInt(),
            "itemId" to itemId,
            "type" to "IN",
            "quantity" to quantity,
            "totalCost" to totalCost,
            "date" to transaction.date
        )
        scheduleSync("stock_transactions", transId.toString(), transData)
    }
    
    suspend fun recordStockOut(itemId: Int, quantity: Double, locationId: Int, employeeId: Int) {
        // Get current item
        val item = dao.getInventoryItemById(itemId) ?: return
        
        // Update quantity
        val newQuantity = (item.currentQuantity - quantity).coerceAtLeast(0.0)
        dao.updateInventoryQuantity(itemId, newQuantity)
        
        // Sync updated quantity to Firestore
        val itemData = mapOf(
            "id" to itemId,
            "name" to item.name,
            "category" to item.category,
            "unit" to item.unit,
            "currentQuantity" to newQuantity,
            "reorderLevel" to item.reorderLevel,
            "dateAdded" to item.dateAdded
        )
        scheduleSync("inventory_items", itemId.toString(), itemData)
        
        // Record transaction
        val transaction = StockTransactionEntity(
            itemId = itemId,
            type = "OUT",
            quantity = quantity,
            locationId = locationId,
            employeeId = employeeId
        )
        val transId = dao.insertStockTransaction(transaction)
        
        // Sync transaction to Firestore
        val transData = mapOf(
            "id" to transId.toInt(),
            "itemId" to itemId,
            "type" to "OUT",
            "quantity" to quantity,
            "locationId" to locationId,
            "employeeId" to employeeId,
            "date" to transaction.date
        )
        scheduleSync("stock_transactions", transId.toString(), transData)
    }
    
    fun getStockTransactions(itemId: Int): Flow<List<StockTransactionEntity>> = 
        dao.getTransactionsForItem(itemId)

    suspend fun deleteInventoryItem(item: InventoryItemEntity) {
        // Get all child records
        val stockTransactions = dao.getStockTransactionsForItemList(item.id)
        
        // Delete children from Room
        dao.deleteStockTransactionsForItem(item.id)
        
        // Delete parent from Room
        dao.deleteInventoryItem(item)
        
        // Delete children from Firestore
        stockTransactions.forEach { transaction ->
            scheduleSync("stock_transactions", transaction.id.toString(), emptyMap(), isDelete = true)
        }
        
        // Delete parent from Firestore
        scheduleSync("inventory_items", item.id.toString(), emptyMap(), isDelete = true)
    }


    // ================== LABOR LOG OPERATIONS ==================
    
    fun getAllLaborLogs(): Flow<List<LaborLogWithEmployee>> = dao.getAllLaborLogsWithEmployee()
    
    suspend fun addLaborLog(log: LaborLogEntity) {
        val id = dao.insertLaborLog(log)
        val data = mapOf(
            "id" to id.toInt(),
            "employeeId" to log.employeeId,
            "laborCount" to log.laborCount,
            "workType" to log.workType,
            "totalAmount" to log.totalAmount,
            "date" to log.date
        )
        scheduleSync("labor_logs", id.toString(), data)
    }
    
    suspend fun updateLaborLog(log: LaborLogEntity) {
        dao.updateLaborLog(log)
        val data = mapOf(
            "id" to log.id,
            "employeeId" to log.employeeId,
            "laborCount" to log.laborCount,
            "workType" to log.workType,
            "totalAmount" to log.totalAmount,
            "date" to log.date
        )
        scheduleSync("labor_logs", log.id.toString(), data)
    }
    
    suspend fun deleteLaborLog(log: LaborLogEntity) {
        dao.deleteLaborLog(log)
        scheduleSync("labor_logs", log.id.toString(), emptyMap(), isDelete = true)
    }

    suspend fun getLaborLogById(id: Int): LaborLogEntity? = dao.getLaborLogById(id)


    // ================== MAINTENANCE LOG OPERATIONS ==================
    
    fun getAllMaintenanceLogs(): Flow<List<MaintenanceLogWithMachine>> = dao.getAllMaintenanceLogsWithMachine()
    
    suspend fun addMaintenanceLog(log: MaintenanceLogEntity) {
        val id = dao.insertMaintenanceLog(log)
        val data = mapOf(
            "id" to id.toInt(),
            "machineId" to log.machineId,
            "tag" to log.tag,
            "cost" to log.cost,
            "mechanicName" to log.mechanicName,
            "description" to log.description,
            "date" to log.date
        )
        scheduleSync("maintenance_logs", id.toString(), data)
    }
    
    suspend fun updateMaintenanceLog(log: MaintenanceLogEntity) {
        dao.updateMaintenanceLog(log)
        val data = mapOf(
            "id" to log.id,
            "machineId" to log.machineId,
            "tag" to log.tag,
            "cost" to log.cost,
            "mechanicName" to log.mechanicName,
            "description" to log.description,
            "date" to log.date
        )
        scheduleSync("maintenance_logs", log.id.toString(), data)
    }
    
    suspend fun deleteMaintenanceLog(log: MaintenanceLogEntity) {
        dao.deleteMaintenanceLog(log)
        scheduleSync("maintenance_logs", log.id.toString(), emptyMap(), isDelete = true)
    }

    suspend fun getMaintenanceLogById(id: Int): MaintenanceLogEntity? = dao.getMaintenanceLogById(id)

    // ================== DAIRY OPERATIONS ==================

    fun getAllDairyCompanies(): Flow<List<DairyCompanyEntity>> = dao.getAllDairyCompanies()

    suspend fun insertDairyCompany(company: DairyCompanyEntity): Long {
        val id = dao.insertDairyCompany(company)
        val data = mapOf(
            "id" to id,
            "name" to company.name,
            "ratePerLiter" to company.ratePerLiter,
            "addedAt" to company.addedAt
        )
        scheduleSync("dairy_companies", id.toString(), data)
        return id
    }

    suspend fun updateDairyCompany(company: DairyCompanyEntity) {
        dao.updateDairyCompany(company)
        val data = mapOf(
            "company_id" to company.id,
            "name" to company.name,
            "ratePerLiter" to company.ratePerLiter,
            "addedAt" to company.addedAt
        )
        scheduleSync("dairy_companies", company.id.toString(), data)
    }

    suspend fun deleteDairyCompany(company: DairyCompanyEntity) {
        dao.deleteDairyCompany(company)
        scheduleSync("dairy_companies", company.id.toString(), emptyMap(), isDelete = true)
    }

    fun getAllDairyLogs(): Flow<List<DairyLogEntity>> = dao.getAllDairyLogs()

    suspend fun insertDairyLog(log: DairyLogEntity) {
        val id = dao.insertDairyLog(log)
        val data = mapOf(
            "id" to id,
            "date" to log.date,
            "companyId" to (log.companyId ?: -1),
            "companyName" to log.companyName,
            "liters" to log.liters,
            "totalAmount" to log.totalAmount,
            "addedBy" to (log.addedBy ?: "")
        )
        scheduleSync("dairy_logs", id.toString(), data)
    }

    suspend fun deleteDairyLog(log: DairyLogEntity) {
        dao.deleteDairyLog(log)
        scheduleSync("dairy_logs", log.id.toString(), emptyMap(), isDelete = true)
    }

    fun getMonthlyMilkSales(): Flow<Double> {
        val (start, end) = getCurrentMonthRange()
        return dao.getMonthlyMilkSales(start, end).map { it ?: 0.0 }
    }
    
    fun countDairyLogsAfter(timestamp: Long): Flow<Int> = dao.countDairyLogsAfter(timestamp)



    // ================== ANALYTICS OPERATIONS ==================

    fun getMonthlyExpenseBreakdown(): Flow<ExpenseBreakdown> {
        val (startOfMonth, endOfMonth) = getCurrentMonthRange()
        
        return combine(
            dao.getTotalFuelCostThisMonth(startOfMonth, endOfMonth),
            dao.getTotalBillsThisMonth(startOfMonth, endOfMonth),
            dao.getTotalLaborCostThisMonth(startOfMonth, endOfMonth),
            dao.getTotalMaintenanceCostThisMonth(startOfMonth, endOfMonth),
            dao.getTotalStockPurchasesThisMonth(startOfMonth, endOfMonth)
        ) { fuel, bills, labor, maintenance, stock ->
            
            val fuelCost = fuel ?: 0.0
            val billsCost = bills ?: 0.0
            val laborCost = labor ?: 0.0
            val maintenanceCost = maintenance ?: 0.0
            val stockCost = stock ?: 0.0
            
            val total = fuelCost + billsCost + laborCost + maintenanceCost + stockCost
            
            val categories = listOf(
                ExpenseCategory("Fuel", fuelCost, Color(0xFFFFC107), if (total > 0) (fuelCost / total * 100).toFloat() else 0f),
                ExpenseCategory("Bills", billsCost, Color(0xFF9C27B0), if (total > 0) (billsCost / total * 100).toFloat() else 0f),
                ExpenseCategory("Labor", laborCost, Color(0xFF2196F3), if (total > 0) (laborCost / total * 100).toFloat() else 0f),
                ExpenseCategory("Maintenance", maintenanceCost, Color(0xFFFF5722), if (total > 0) (maintenanceCost / total * 100).toFloat() else 0f),
                ExpenseCategory("Stock", stockCost, Color(0xFF4CAF50), if (total > 0) (stockCost / total * 100).toFloat() else 0f)
            ).filter { it.amount > 0 } // Only show categories with expenses
            
            ExpenseBreakdown(total, categories)
        }
    }
    
    fun getOverallTotalExpenses(): Flow<Double> {
        return combine(
            dao.getTotalFuelCostAllTime(),
            dao.getTotalBillsAllTime(),
            dao.getTotalLaborCostAllTime(),
            dao.getTotalMaintenanceCostAllTime(),
            dao.getTotalStockPurchasesAllTime()
        ) { fuel, bills, labor, maintenance, stock ->
            val fuelCost = fuel ?: 0.0
            val billsCost = bills ?: 0.0
            val laborCost = labor ?: 0.0
            val maintenanceCost = maintenance ?: 0.0
            val stockCost = stock ?: 0.0
            
            fuelCost + billsCost + laborCost + maintenanceCost + stockCost
        }
    }

    private fun getCurrentMonthRange(): Pair<Long, Long> {
        val calendar = Calendar.getInstance()
        
        // Start of month
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfMonth = calendar.timeInMillis
        
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        val endOfMonth = calendar.timeInMillis
        
        return Pair(startOfMonth, endOfMonth)
    }

    // ================== FIRESTORE SYNC METHODS ==================

    /**
     * Check if local database is empty (first login on this device)
     */
    suspend fun isLocalDatabaseEmpty(): Boolean {
        return dao.getMachineCount() == 0 &&
               dao.getEmployeeCount() == 0 &&
               dao.getLocationCount() == 0
    }

    /**
     * Download all user data from Firestore and insert into local database
     */
    private fun parseInt(value: Any?): Int {
        return when (value) {
            is Number -> value.toInt()
            is String -> value.toIntOrNull() ?: 0
            else -> 0
        }
    }

    private fun parseLong(value: Any?): Long {
        return when (value) {
            is Number -> value.toLong()
            is String -> value.toLongOrNull() ?: 0L
            else -> 0L
        }
    }

    private fun parseDouble(value: Any?): Double {
        return when (value) {
            is Number -> value.toDouble()
            is String -> value.toDoubleOrNull() ?: 0.0
            else -> 0.0
        }
    }

    suspend fun downloadAllDataFromFirestore(): Result<Boolean> {
        // CHANGED: Use Farm Owner ID for all data access
        // This ensures managers download data from the Owner's collection, not their empty one
        val prefs = context.getSharedPreferences("agri_manager_prefs", Context.MODE_PRIVATE) // Quick access context-based
        val farmOwnerId = prefs.getString("farm_owner_id", null)
        val userId = farmOwnerId ?: auth.currentUser?.uid ?: return Result.failure(Exception("Not logged in"))
        
        return try {
            val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
            
            // Download all collections with individual error handling
            // IMPORTANT: Download parent tables FIRST, then child tables to avoid FK constraints
            
            // === PARENT TABLES (no foreign keys) ===
            
            try {
                downloadMachines(db, userId)
            } catch (e: Exception) {
                // Silently continue on error
            }
            
            try {
                downloadLocations(db, userId)
            } catch (e: Exception) {
                // Silently continue on error
            }
            
            try {
                downloadEmployees(db, userId)
            } catch (e: Exception) {
                // Silently continue on error
            }
            
            try {
                downloadManagers(db, userId)
            } catch (e: Exception) {
                // Silently continue on error
            }
            
            try {
                downloadInventoryItems(db, userId)
            } catch (e: Exception) {
                // Silently continue on error
            }

            try {
                downloadDairyCompanies(db, userId)
            } catch (e: Exception) {
                // Silently continue on error
            }
            
            // === CHILD TABLES (have foreign keys) ===
            
            try {
                downloadFuelLogs(db, userId)
            } catch (e: Exception) {
                // Silently continue on error
            }
            
            try {
                downloadBills(db, userId)
            } catch (e: Exception) {
                // Silently continue on error
            }
            
            try {
                downloadTransactions(db, userId)
            } catch (e: Exception) {
                // Silently continue on error
            }
            
            try {
                downloadStockTransactions(db, userId)
            } catch (e: Exception) {
                // Silently continue on error
            }
            
            try {
                downloadLaborLogs(db, userId)
            } catch (e: Exception) {
                // Silently continue on error
            }
            
            try {
                downloadMaintenanceLogs(db, userId)
            } catch (e: Exception) {
                // Silently continue on error
            }

            try {
                downloadDairyLogs(db, userId)
            } catch (e: Exception) {
                // Silently continue on error
            }
            
            Result.success(true)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    private suspend fun downloadMachines(db: com.google.firebase.firestore.FirebaseFirestore, userId: String) {
        val snapshot = db.collection("users")
            .document(userId)
            .collection("machines")
            .get()
            .await()
        
        snapshot.documents.forEach { doc ->
            val data = doc.data ?: return@forEach
            // FIX: Use Number to handle both Int and Long from Firestore
            val storedId = parseInt(data["id"])
            val effectiveId = if (storedId != 0) storedId else doc.id.hashCode()
            
            val machine = MachineEntity(
                id = effectiveId,
                name = data["name"] as? String ?: "",
                dateAdded = parseLong(data["dateAdded"])
            )
            dao.insertMachine(machine)
        }
    }

    private suspend fun downloadFuelLogs(db: com.google.firebase.firestore.FirebaseFirestore, userId: String) {
        val snapshot = db.collection("users")
            .document(userId)
            .collection("fuel_logs")
            .get()
            .await()
        
        snapshot.documents.forEach { doc ->
            val data = doc.data ?: return@forEach
            val storedId = parseInt(data["id"])
            val effectiveId = if (storedId != 0) storedId else doc.id.hashCode()
            
            val log = FuelLogEntity(
                id = effectiveId,
                machineId = parseInt(data["machineId"]),
                date = parseLong(data["date"]),
                liters = parseDouble(data["liters"]),
                rate = parseDouble(data["rate"]),
                totalCost = parseDouble(data["totalCost"]),
                hourMeterReading = parseInt(data["hourMeterReading"])
            )
            dao.insertFuelLog(log)
        }
    }

    private suspend fun downloadBills(db: com.google.firebase.firestore.FirebaseFirestore, userId: String) {
        val snapshot = db.collection("users")
            .document(userId)
            .collection("bills")
            .get()
            .await()
        
        snapshot.documents.forEach { doc ->
            val data = doc.data ?: return@forEach
            val storedId = parseInt(data["id"])
            val effectiveId = if (storedId != 0) storedId else doc.id.hashCode()
            
            val bill = BillEntity(
                id = effectiveId,
                locationId = parseInt(data["locationId"]),
                billingMonth = data["billingMonth"] as? String ?: "",
                amount = parseDouble(data["amount"]),
                dateAdded = parseLong(data["dateAdded"])
            )
            dao.insertBill(bill)
        }
    }

    private suspend fun downloadLocations(db: com.google.firebase.firestore.FirebaseFirestore, userId: String) {
        val snapshot = db.collection("users")
            .document(userId)
            .collection("locations")
            .get()
            .await()
        
        snapshot.documents.forEach { doc ->
            val data = doc.data ?: return@forEach
            val storedId = parseInt(data["id"])
            val effectiveId = if (storedId != 0) storedId else doc.id.hashCode()
            
            val location = LocationEntity(
                id = effectiveId,
                name = data["name"] as? String ?: ""
            )
            dao.insertLocation(location)
        }
    }

    private suspend fun downloadEmployees(db: com.google.firebase.firestore.FirebaseFirestore, userId: String) {
        val snapshot = db.collection("users")
            .document(userId)
            .collection("employees")
            .get()
            .await()
        
        snapshot.documents.forEach { doc ->
            val data = doc.data ?: return@forEach
            val storedId = parseInt(data["id"])
            val effectiveId = if (storedId != 0) storedId else doc.id.hashCode()
            
            val employee = EmployeeEntity(
                id = effectiveId,
                name = data["name"] as? String ?: "",
                baseSalary = parseDouble(data["baseSalary"])
            )
            dao.insertEmployee(employee)
        }
    }
    
    private suspend fun downloadManagers(db: com.google.firebase.firestore.FirebaseFirestore, userId: String) {
        val snapshot = db.collection("users")
            .document(userId)
            .collection("managers")
            .get()
            .await()
        
        snapshot.documents.forEach { doc ->
            val data = doc.data ?: return@forEach
            val manager = com.example.agrimanager.data.local.UserEntity(
                uid = doc.id,
                email = data["email"] as? String ?: "No Email",
                role = "manager",
                farmOwnerId = userId,
                addedAt = parseLong(data["addedAt"]),
                addedBy = data["addedBy"] as? String
            )
            userDao.insertUser(manager)
        }
    }

    private suspend fun downloadTransactions(db: com.google.firebase.firestore.FirebaseFirestore, userId: String) {
        val snapshot = db.collection("users")
            .document(userId)
            .collection("transactions")
            .get()
            .await()
        
        snapshot.documents.forEach { doc ->
            val data = doc.data ?: return@forEach
            val storedId = parseInt(data["id"])
            val effectiveId = if (storedId != 0) storedId else doc.id.hashCode()
            
            val transaction = TransactionEntity(
                id = effectiveId,
                employeeId = parseInt(data["employeeId"]),
                amount = parseDouble(data["amount"]),
                type = data["type"] as? String ?: "DEBIT",
                timestamp = parseLong(data["timestamp"])
            )
            dao.insertTransaction(transaction)
        }
    }

    private suspend fun downloadInventoryItems(db: com.google.firebase.firestore.FirebaseFirestore, userId: String) {
        val snapshot = db.collection("users")
            .document(userId)
            .collection("inventory_items")
            .get()
            .await()
        
        snapshot.documents.forEach { doc ->
            val data = doc.data ?: return@forEach
            val storedId = parseInt(data["id"])
            val effectiveId = if (storedId != 0) storedId else doc.id.hashCode()
            
            val item = InventoryItemEntity(
                id = effectiveId,
                name = data["name"] as? String ?: "",
                category = data["category"] as? String ?: "",
                unit = data["unit"] as? String ?: "",
                currentQuantity = parseDouble(data["currentQuantity"]),
                reorderLevel = parseDouble(data["reorderLevel"]),
                dateAdded = parseLong(data["dateAdded"])
            )
            dao.insertInventoryItem(item)
        }
    }

    private suspend fun downloadStockTransactions(db: com.google.firebase.firestore.FirebaseFirestore, userId: String) {
        val snapshot = db.collection("users")
            .document(userId)
            .collection("stock_transactions")
            .get()
            .await()
        
        snapshot.documents.forEach { doc ->
            val data = doc.data ?: return@forEach
            val storedId = parseInt(data["id"])
            val effectiveId = if (storedId != 0) storedId else doc.id.hashCode()
            
            val transaction = StockTransactionEntity(
                id = effectiveId,
                itemId = parseInt(data["itemId"]),
                type = data["type"] as? String ?: "IN",
                quantity = parseDouble(data["quantity"]),
                totalCost = parseDouble(data["totalCost"]),
                date = parseLong(data["date"]),
                locationId = parseInt(data["locationId"]).takeIf { it != 0 },
                employeeId = parseInt(data["employeeId"]).takeIf { it != 0 }
            )
            dao.insertStockTransaction(transaction)
        }
    }

    private suspend fun downloadLaborLogs(db: com.google.firebase.firestore.FirebaseFirestore, userId: String) {
        val snapshot = db.collection("users")
            .document(userId)
            .collection("labor_logs")
            .get()
            .await()
        
        snapshot.documents.forEach { doc ->
            val data = doc.data ?: return@forEach
            try {
                // FIX: If stored ID is 0, use hashCode of document ID to ensure uniqueness
                val storedId = parseInt(data["id"])
                val effectiveId = if (storedId != 0) storedId else doc.id.hashCode()
            
                val log = LaborLogEntity(
                    id = effectiveId,
                    employeeId = parseInt(data["employeeId"]),
                    laborCount = parseInt(data["laborCount"]),
                    workType = data["workType"] as? String ?: "",
                    totalAmount = parseDouble(data["totalAmount"]),
                    date = parseLong(data["date"])
                )
                dao.insertLaborLog(log)
            } catch (e: Exception) {
                // Silently continue on error
            }
        }
    }

    private suspend fun downloadMaintenanceLogs(db: com.google.firebase.firestore.FirebaseFirestore, userId: String) {
        val snapshot = db.collection("users")
            .document(userId)
            .collection("maintenance_logs")
            .get()
            .await()
        
        snapshot.documents.forEach { doc ->
            val data = doc.data ?: return@forEach
            val storedId = parseInt(data["id"])
            val effectiveId = if (storedId != 0) storedId else doc.id.hashCode()
            
            val log = MaintenanceLogEntity(
                id = effectiveId,
                machineId = parseInt(data["machineId"]),
                tag = data["tag"] as? String ?: "",
                cost = parseDouble(data["cost"]),
                mechanicName = data["mechanicName"] as? String ?: "",
                description = data["description"] as? String ?: "",
                date = parseLong(data["date"])
            )
            dao.insertMaintenanceLog(log)
        }
    }

    private suspend fun downloadDairyCompanies(db: com.google.firebase.firestore.FirebaseFirestore, userId: String) {
        val snapshot = db.collection("users")
            .document(userId)
            .collection("dairy_companies")
            .get()
            .await()
        
        snapshot.documents.forEach { doc ->
            val data = doc.data ?: return@forEach
            val storedId = parseInt(data["id"])
            val effectiveId = if (storedId != 0) storedId else doc.id.hashCode()
            
            val company = DairyCompanyEntity(
                id = effectiveId,
                name = data["name"] as? String ?: "Unknown",
                ratePerLiter = parseDouble(data["ratePerLiter"]),
                addedAt = parseLong(data["addedAt"])
            )
            dao.insertDairyCompany(company)
        }
    }

    private suspend fun downloadDairyLogs(db: com.google.firebase.firestore.FirebaseFirestore, userId: String) {
        val snapshot = db.collection("users")
            .document(userId)
            .collection("dairy_logs")
            .get()
            .await()
        
        snapshot.documents.forEach { doc ->
            val data = doc.data ?: return@forEach
            val storedId = parseInt(data["id"])
            val effectiveId = if (storedId != 0) storedId else doc.id.hashCode()
            
            val log = DairyLogEntity(
                id = effectiveId,
                date = parseLong(data["date"]),
                companyId = parseInt(data["companyId"]),
                companyName = data["companyName"] as? String ?: "Unknown",
                liters = parseDouble(data["liters"]),
                totalAmount = parseDouble(data["totalAmount"]),
                addedBy = data["addedBy"] as? String ?: ""
            )
            dao.insertDairyLog(log)
        }
    }
    
    // ================== PDF EXPORT ==================
    
    suspend fun getExportData(config: com.example.agrimanager.data.models.ExportConfig): com.example.agrimanager.data.models.ExportData {
        val prefs = context.getSharedPreferences("agri_manager_prefs", android.content.Context.MODE_PRIVATE)
        val userEmail = prefs.getString("user_email", "Unknown") ?: "Unknown"
        val userRole = prefs.getString("user_role", "Unknown") ?: "Unknown"
        
        val startDate = config.startDate ?: 0L
        val endDate = config.endDate ?: System.currentTimeMillis()
        
        val dateRange = if (config.startDate != null && config.endDate != null) {
            "${java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault()).format(java.util.Date(startDate))} to ${java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault()).format(java.util.Date(endDate))}"
        } else null
        
        return com.example.agrimanager.data.models.ExportData(
            farmName = "AgriManager Farm",
            exportDate = System.currentTimeMillis(),
            generatedBy = userEmail,
            userRole = userRole.replaceFirstChar { it.uppercase() },
            dateRange = dateRange,
            
            machines = if (config.includeMachines) {
                dao.getAllMachines().first()
            } else null,
            
            fuelLogs = if (config.includeFuel) {
                val logs = if (config.startDate != null) {
                    dao.getFuelLogsByDateRange(startDate, endDate)
                } else {
                    dao.getFuelLogsByDateRange(0L, System.currentTimeMillis())
                }
                // Enrich with machine names
                logs.map { log ->
                    val machine = dao.getMachineById(log.machineId)
                    com.example.agrimanager.data.models.FuelLogWithMachine(
                        id = log.id,
                        machineId = log.machineId,
                        machineName = machine?.name ?: "Unknown Machine",
                        liters = log.liters,
                        rate = log.rate,
                        totalCost = log.totalCost,
                        date = log.date
                    )
                }
            } else null,
            
            employees = if (config.includeEmployees) {
                dao.getAllEmployees().first()
            } else null,
            
            transactions = if (config.includeSalary) {
                val trans = if (config.startDate != null) {
                    dao.getTransactionsByDateRange(startDate, endDate)
                } else {
                    dao.getTransactionsByDateRange(0L, System.currentTimeMillis())
                }
                // Enrich with employee names
                trans.map { transaction ->
                    val employee = dao.getEmployeeById(transaction.employeeId)
                    com.example.agrimanager.data.models.TransactionWithEmployee(
                        id = transaction.id,
                        employeeId = transaction.employeeId,
                        employeeName = employee?.name ?: "Unknown Employee",
                        amount = transaction.amount,
                        type = transaction.type,
                        timestamp = transaction.timestamp
                    )
                }
            } else null,
            
            locations = if (config.includeLocations) {
                dao.getAllLocations().first()
            } else null,
            
            bills = if (config.includeBills) {
                if (config.startDate != null) {
                    // Get bills with location info
                    val billEntities = dao.getBillsByDateRange(startDate, endDate)
                    billEntities.map { bill ->
                        val location = dao.getLocationById(bill.locationId)
                        BillWithLocation(
                            bill = bill,
                            locationName = location?.name ?: "Unknown Location"
                        )
                    }
                } else {
                    dao.getAllBillsWithLocation().first()
                }
            } else null,
            
            inventoryItems = if (config.includeInventory) {
                dao.getAllInventoryItems().first()
            } else null,
            
            stockTransactions = if (config.includeInventory) {
                val trans = if (config.startDate != null) {
                    dao.getStockTransactionsByDateRange(startDate, endDate)
                } else {
                    dao.getStockTransactionsByDateRange(0L, System.currentTimeMillis())
                }
                // Enrich with item names
                trans.map { transaction ->
                    val item = dao.getInventoryItemById(transaction.itemId)
                    com.example.agrimanager.data.models.StockTransactionWithItem(
                        id = transaction.id,
                        itemId = transaction.itemId,
                        itemName = item?.name ?: "Unknown Item",
                        type = transaction.type,
                        quantity = transaction.quantity,
                        totalCost = transaction.totalCost,
                        date = transaction.date
                    )
                }
            } else null,
            
            laborLogs = if (config.includeLabor) {
                if (config.startDate != null) {
                    dao.getLaborLogsByDateRange(startDate, endDate)
                } else {
                    dao.getAllLaborLogsWithEmployee().first()
                }
            } else null,
            
            maintenanceLogs = if (config.includeMaintenance) {
                if (config.startDate != null) {
                    dao.getMaintenanceLogsByDateRange(startDate, endDate)
                } else {
                    dao.getAllMaintenanceLogsWithMachine().first()
                }
            } else null,
            
            analytics = if (config.includeAnalytics) {
                try {
                    getMonthlyExpenseBreakdown().first()
                } catch (e: Exception) {
                    null
                }
            } else null,

            dairyLogs = if (config.includeDairy) {
                if (config.startDate != null) {
                    dao.getDairyLogsByDateRange(startDate, endDate)
                } else {
                    dao.getAllDairyLogs().first()
                }
            } else null
        )
    }

}