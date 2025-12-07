package com.example.agrimanager.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FarmDao {

    // --- Employee Operations ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmployee(employee: EmployeeEntity): Long

    @Query("SELECT * FROM employees ORDER BY name ASC")
    fun getAllEmployees(): Flow<List<EmployeeEntity>>

    @Delete
    suspend fun deleteEmployee(employee: EmployeeEntity)

    @Query("SELECT * FROM employees WHERE id = :id")
    suspend fun getEmployeeById(id: Int): EmployeeEntity?

    // --- Transaction Operations ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity): Long

    @Query("SELECT * FROM transactions WHERE employee_id = :employeeId")
    fun getTransactionsForEmployee(employeeId: Int): Flow<List<TransactionEntity>>

    @Query("SELECT SUM(amount) FROM transactions WHERE employee_id = :employeeId AND type = 'DEBIT'")
    fun getTotalAdvances(employeeId: Int): Flow<Double?>

    // --- Machines ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMachine(machine: MachineEntity): Long

    @Delete
    suspend fun deleteMachine(machine: MachineEntity)

    @Query("SELECT * FROM machines ORDER BY name ASC")
    fun getAllMachines(): Flow<List<MachineEntity>>

    @Query("SELECT * FROM machines WHERE machine_id = :id")
    suspend fun getMachineById(id: Int): MachineEntity?

    // --- Fuel Logs ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFuelLog(log: FuelLogEntity): Long

    @Query("SELECT * FROM fuel_logs WHERE machine_id = :machineId ORDER BY date DESC")
    fun getFuelLogsForMachine(machineId: Int): Flow<List<FuelLogEntity>>

    @Query("SELECT SUM(total_cost) FROM fuel_logs WHERE machine_id = :machineId")
    fun getTotalCostForMachine(machineId: Int): Flow<Double?>

    // --- Locations ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocation(location: LocationEntity): Long

    @Delete
    suspend fun deleteLocation(location: LocationEntity)

    @Query("SELECT * FROM locations ORDER BY name ASC")
    fun getAllLocations(): Flow<List<LocationEntity>>

    // --- Bills ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBill(bill: BillEntity): Long

    @Query("""
        SELECT bills.*, locations.name as location_name 
        FROM bills 
        INNER JOIN locations ON bills.location_id = locations.location_id 
        ORDER BY bills.date_added DESC
    """)
    fun getAllBillsWithLocation(): Flow<List<BillWithLocation>>

    @Query("SELECT * FROM bills WHERE location_id = :locationId ORDER BY date_added DESC")
    fun getBillsForLocation(locationId: Int): Flow<List<BillEntity>>

    @Query("SELECT SUM(amount) FROM bills WHERE location_id = :locationId")
    fun getTotalBillForLocation(locationId: Int): Flow<Double?>

    // --- Inventory Items ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInventoryItem(item: InventoryItemEntity): Long

    @Query("SELECT * FROM inventory_items ORDER BY name ASC")
    fun getAllInventoryItems(): Flow<List<InventoryItemEntity>>

    @Query("SELECT * FROM inventory_items WHERE item_id = :id")
    suspend fun getInventoryItemById(id: Int): InventoryItemEntity?

    @Query("UPDATE inventory_items SET current_quantity = :quantity WHERE item_id = :id")
    suspend fun updateInventoryQuantity(id: Int, quantity: Double)

    @Delete
    suspend fun deleteInventoryItem(item: InventoryItemEntity)

    // --- Stock Transactions ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStockTransaction(transaction: StockTransactionEntity): Long

    @Query("SELECT * FROM stock_transactions WHERE item_id = :itemId ORDER BY date DESC")
    fun getTransactionsForItem(itemId: Int): Flow<List<StockTransactionEntity>>

    // --- Labor Logs ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLaborLog(log: LaborLogEntity): Long

    @Query("""
        SELECT 
            l.log_id as id,
            l.employee_id as employeeId,
            e.name as employeeName,
            l.labor_count as laborCount,
            l.work_type as workType,
            l.total_amount as totalAmount,
            l.date as date
        FROM labor_logs l
        INNER JOIN employees e ON l.employee_id = e.id
        ORDER BY l.date DESC
    """)
    fun getAllLaborLogsWithEmployee(): Flow<List<LaborLogWithEmployee>>

    @Delete
    suspend fun deleteLaborLog(log: LaborLogEntity)

    @Query("SELECT * FROM labor_logs WHERE log_id = :id")
    suspend fun getLaborLogById(id: Int): LaborLogEntity?

    // --- Maintenance Logs ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMaintenanceLog(log: MaintenanceLogEntity): Long

    @Query("""
        SELECT 
            m.log_id as id,
            m.machine_id as machineId,
            ma.name as machineName,
            m.tag as tag,
            m.cost as cost,
            m.mechanic_name as mechanicName,
            m.description as description,
            m.date as date
        FROM maintenance_logs m
        INNER JOIN machines ma ON m.machine_id = ma.machine_id
        ORDER BY m.date DESC
    """)
    fun getAllMaintenanceLogsWithMachine(): Flow<List<MaintenanceLogWithMachine>>

    @Delete
    suspend fun deleteMaintenanceLog(log: MaintenanceLogEntity)

    @Query("SELECT * FROM maintenance_logs WHERE log_id = :id")
    suspend fun getMaintenanceLogById(id: Int): MaintenanceLogEntity?

    // ================== ANALYTICS QUERIES ==================

    // Get total fuel costs for current month
    @Query("""
        SELECT SUM(total_cost) 
        FROM fuel_logs 
        WHERE date >= :startOfMonth AND date <= :endOfMonth
    """)
    fun getTotalFuelCostThisMonth(startOfMonth: Long, endOfMonth: Long): Flow<Double?>

    // Get total bills for current month
    @Query("""
        SELECT SUM(amount) 
        FROM bills 
        WHERE date_added >= :startOfMonth AND date_added <= :endOfMonth
    """)
    fun getTotalBillsThisMonth(startOfMonth: Long, endOfMonth: Long): Flow<Double?>

    // Get total labor costs for current month
    @Query("""
        SELECT SUM(total_amount) 
        FROM labor_logs 
        WHERE date >= :startOfMonth AND date <= :endOfMonth
    """)
    fun getTotalLaborCostThisMonth(startOfMonth: Long, endOfMonth: Long): Flow<Double?>

    // Get total maintenance costs for current month
    @Query("""
        SELECT SUM(cost) 
        FROM maintenance_logs 
        WHERE date >= :startOfMonth AND date <= :endOfMonth
    """)
    fun getTotalMaintenanceCostThisMonth(startOfMonth: Long, endOfMonth: Long): Flow<Double?>

    // Get total stock purchase costs for current month
    @Query("""
        SELECT SUM(total_cost) 
        FROM stock_transactions 
        WHERE type = 'IN' AND date >= :startOfMonth AND date <= :endOfMonth
    """)
    fun getTotalStockPurchasesThisMonth(startOfMonth: Long, endOfMonth: Long): Flow<Double?>
}