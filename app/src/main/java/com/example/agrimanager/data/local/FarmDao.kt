package com.example.agrimanager.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface FarmDao {

    // --- Employee Operations ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmployee(employee: EmployeeEntity): Long

    @Update
    suspend fun updateEmployee(employee: EmployeeEntity)

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

    @Query("SELECT * FROM transactions WHERE employee_id = :employeeId")
    suspend fun getTransactionsForEmployeeList(employeeId: Int): List<TransactionEntity>

    @Query("DELETE FROM transactions WHERE employee_id = :employeeId")
    suspend fun deleteTransactionsForEmployee(employeeId: Int)

    @Query("SELECT SUM(amount) FROM transactions WHERE employee_id = :employeeId AND type = 'DEBIT'")
    fun getTotalAdvances(employeeId: Int): Flow<Double?>

    // --- Machines ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMachine(machine: MachineEntity): Long

    @Delete
    suspend fun deleteMachine(machine: MachineEntity)

    @Update
    suspend fun updateMachine(machine: MachineEntity)

    @Query("SELECT * FROM machines ORDER BY name ASC")
    fun getAllMachines(): Flow<List<MachineEntity>>

    @Query("SELECT * FROM machines WHERE machine_id = :id")
    suspend fun getMachineById(id: Int): MachineEntity?

    // --- Fuel Logs ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFuelLog(log: FuelLogEntity): Long

    @Update
    suspend fun updateFuelLog(log: FuelLogEntity)

    @Delete
    suspend fun deleteFuelLog(log: FuelLogEntity)

    @Query("SELECT * FROM fuel_logs WHERE log_id = :id")
    suspend fun getFuelLogById(id: Int): FuelLogEntity?

    @Query("SELECT * FROM fuel_logs WHERE machine_id = :machineId ORDER BY date DESC")
    fun getFuelLogsForMachine(machineId: Int): Flow<List<FuelLogEntity>>

    // --- DAIRY MODULE ---
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDairyCompany(company: DairyCompanyEntity): Long
    
    @Update
    suspend fun updateDairyCompany(company: DairyCompanyEntity)
    
    @Delete
    suspend fun deleteDairyCompany(company: DairyCompanyEntity)
    
    @Query("SELECT * FROM dairy_companies ORDER BY name ASC")
    fun getAllDairyCompanies(): Flow<List<DairyCompanyEntity>>

    @Query("SELECT * FROM dairy_companies WHERE company_id = :id")
    suspend fun getDairyCompanyById(id: Int): DairyCompanyEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDairyLog(log: DairyLogEntity): Long
    
    @Delete
    suspend fun deleteDairyLog(log: DairyLogEntity)
    
    @Query("SELECT * FROM dairy_logs ORDER BY date DESC")
    fun getAllDairyLogs(): Flow<List<DairyLogEntity>>
    
    @Query("SELECT COUNT(*) FROM dairy_logs WHERE date > :timestamp")
    fun countDairyLogsAfter(timestamp: Long): Flow<Int>
    
    // Monthly Milk Sales (Current Month)
    @Query("SELECT SUM(total_amount) FROM dairy_logs WHERE date >= :startOfMonth AND date <= :endOfMonth")
    fun getMonthlyMilkSales(startOfMonth: Long, endOfMonth: Long): Flow<Double?>

    @Query("SELECT * FROM dairy_logs WHERE date >= :startDate AND date <= :endDate ORDER BY date DESC")
    suspend fun getDairyLogsByDateRange(startDate: Long, endDate: Long): List<DairyLogEntity>

    @Query("SELECT * FROM fuel_logs WHERE machine_id = :machineId")
    suspend fun getFuelLogsForMachineList(machineId: Int): List<FuelLogEntity>

    @Query("DELETE FROM fuel_logs WHERE machine_id = :machineId")
    suspend fun deleteFuelLogsForMachine(machineId: Int)

    @Query("SELECT SUM(total_cost) FROM fuel_logs WHERE machine_id = :machineId")
    fun getTotalCostForMachine(machineId: Int): Flow<Double?>

    // --- Locations ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocation(location: LocationEntity): Long

    @Update
    suspend fun updateLocation(location: LocationEntity)

    @Delete
    suspend fun deleteLocation(location: LocationEntity)

    @Query("SELECT * FROM locations ORDER BY name ASC")
    fun getAllLocations(): Flow<List<LocationEntity>>

    @Query("SELECT * FROM locations WHERE location_id = :id")
    suspend fun getLocationById(id: Int): LocationEntity?

    // --- Bills ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBill(bill: BillEntity): Long

    @Update
    suspend fun updateBill(bill: BillEntity)

    @Delete
    suspend fun deleteBill(bill: BillEntity)

    @Query("SELECT * FROM bills WHERE bill_id = :id")
    suspend fun getBillById(id: Int): BillEntity?

    @Query("""
        SELECT 
            bills.bill_id,
            bills.location_id,
            bills.billing_month,
            bills.amount,
            bills.date_added,
            COALESCE(locations.name, 'Unknown Location') as location_name 
        FROM bills 
        LEFT JOIN locations ON bills.location_id = locations.location_id 
        ORDER BY bills.date_added DESC
    """)
    fun getAllBillsWithLocation(): Flow<List<BillWithLocation>>

    @Query("SELECT * FROM bills WHERE location_id = :locationId ORDER BY date_added DESC")
    fun getBillsForLocation(locationId: Int): Flow<List<BillEntity>>

    @Query("SELECT * FROM bills WHERE location_id = :locationId")
    suspend fun getBillsForLocationList(locationId: Int): List<BillEntity>

    @Query("DELETE FROM bills WHERE location_id = :locationId")
    suspend fun deleteBillsForLocation(locationId: Int)

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

    @Query("SELECT * FROM stock_transactions WHERE item_id = :itemId")
    suspend fun getStockTransactionsForItemList(itemId: Int): List<StockTransactionEntity>

    @Query("DELETE FROM stock_transactions WHERE item_id = :itemId")
    suspend fun deleteStockTransactionsForItem(itemId: Int)

    // --- Labor Logs ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLaborLog(log: LaborLogEntity): Long

    @Update
    suspend fun updateLaborLog(log: LaborLogEntity)

    @Delete
    suspend fun deleteLaborLog(log: LaborLogEntity)

    @Query("SELECT * FROM labor_logs WHERE log_id = :id")
    suspend fun getLaborLogById(id: Int): LaborLogEntity?

    @Query("""
        SELECT 
            l.log_id as id,
            l.employee_id as employeeId,
            COALESCE(e.name, 'Unknown Employee') as employeeName,
            l.labor_count as laborCount,
            l.work_type as workType,
            l.total_amount as totalAmount,
            l.date as date
        FROM labor_logs l
        LEFT JOIN employees e ON l.employee_id = e.id
        ORDER BY l.date DESC
    """)
    fun getAllLaborLogsWithEmployee(): Flow<List<LaborLogWithEmployee>>

    @Query("SELECT * FROM labor_logs WHERE employee_id = :employeeId")
    suspend fun getLaborLogsForEmployeeList(employeeId: Int): List<LaborLogEntity>

    @Query("DELETE FROM labor_logs WHERE employee_id = :employeeId")
    suspend fun deleteLaborLogsForEmployee(employeeId: Int)

    // --- Maintenance Logs ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMaintenanceLog(log: MaintenanceLogEntity): Long

    @Update
    suspend fun updateMaintenanceLog(log: MaintenanceLogEntity)

    @Delete
    suspend fun deleteMaintenanceLog(log: MaintenanceLogEntity)

    @Query("SELECT * FROM maintenance_logs WHERE log_id = :id")
    suspend fun getMaintenanceLogById(id: Int): MaintenanceLogEntity?

    @Query("""
        SELECT 
            m.log_id as id,
            m.machine_id as machineId,
            COALESCE(ma.name, 'Unknown Machine') as machineName,
            m.tag as tag,
            m.cost as cost,
            m.mechanic_name as mechanicName,
            m.description as description,
            m.date as date
        FROM maintenance_logs m
        LEFT JOIN machines ma ON m.machine_id = ma.machine_id
        ORDER BY m.date DESC
    """)
    fun getAllMaintenanceLogsWithMachine(): Flow<List<MaintenanceLogWithMachine>>

    @Query("SELECT * FROM maintenance_logs WHERE machine_id = :machineId")
    suspend fun getMaintenanceLogsForMachineList(machineId: Int): List<MaintenanceLogEntity>

    @Query("DELETE FROM maintenance_logs WHERE machine_id = :machineId")
    suspend fun deleteMaintenanceLogsForMachine(machineId: Int)

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

    @Query("""
        SELECT SUM(total_cost) 
        FROM stock_transactions 
        WHERE type = 'IN' AND date >= :startOfMonth AND date <= :endOfMonth
    """)
    fun getTotalStockPurchasesThisMonth(startOfMonth: Long, endOfMonth: Long): Flow<Double?>
    
    // ================== OVERALL TOTAL EXPENSES (ALL TIME) ==================
    
    @Query("SELECT SUM(total_cost) FROM fuel_logs")
    fun getTotalFuelCostAllTime(): Flow<Double?>
    
    @Query("SELECT SUM(amount) FROM bills")
    fun getTotalBillsAllTime(): Flow<Double?>
    
    @Query("SELECT SUM(total_amount) FROM labor_logs")
    fun getTotalLaborCostAllTime(): Flow<Double?>
    
    @Query("SELECT SUM(cost) FROM maintenance_logs")
    fun getTotalMaintenanceCostAllTime(): Flow<Double?>
    
    @Query("SELECT SUM(total_cost) FROM stock_transactions WHERE type = 'IN'")
    fun getTotalStockPurchasesAllTime(): Flow<Double?>

    // --- Count Methods for Sync Detection ---
    @Query("SELECT COUNT(*) FROM machines")
    suspend fun getMachineCount(): Int

    @Query("SELECT COUNT(*) FROM employees")
    suspend fun getEmployeeCount(): Int

    @Query("SELECT COUNT(*) FROM locations")
    suspend fun getLocationCount(): Int
    
    // ================== PDF EXPORT QUERIES (DATE RANGE) ==================
    
    @Query("""
        SELECT * FROM fuel_logs 
        WHERE date >= :startDate AND date <= :endDate 
        ORDER BY date DESC
    """)
    suspend fun getFuelLogsByDateRange(startDate: Long, endDate: Long): List<FuelLogEntity>
    
    @Query("""
        SELECT * FROM bills 
        WHERE date_added >= :startDate AND date_added <= :endDate 
        ORDER BY date_added DESC
    """)
    suspend fun getBillsByDateRange(startDate: Long, endDate: Long): List<BillEntity>
    
    @Query("""
        SELECT * FROM transactions 
        WHERE timestamp >= :startDate AND timestamp <= :endDate 
        ORDER BY timestamp DESC
    """)
    suspend fun getTransactionsByDateRange(startDate: Long, endDate: Long): List<TransactionEntity>
    
    @Query("""
        SELECT 
            l.log_id as id,
            l.employee_id as employeeId,
            COALESCE(e.name, 'Unknown Employee') as employeeName,
            l.labor_count as laborCount,
            l.work_type as workType,
            l.total_amount as totalAmount,
            l.date as date
        FROM labor_logs l
        LEFT JOIN employees e ON l.employee_id = e.id
        WHERE l.date >= :startDate AND l.date <= :endDate
        ORDER BY l.date DESC
    """)
    suspend fun getLaborLogsByDateRange(startDate: Long, endDate: Long): List<LaborLogWithEmployee>
    
    @Query("""
        SELECT 
            m.log_id as id,
            m.machine_id as machineId,
            COALESCE(ma.name, 'Unknown Machine') as machineName,
            m.tag as tag,
            m.cost as cost,
            m.mechanic_name as mechanicName,
            m.description as description,
            m.date as date
        FROM maintenance_logs m
        LEFT JOIN machines ma ON m.machine_id = ma.machine_id
        WHERE m.date >= :startDate AND m.date <= :endDate
        ORDER BY m.date DESC
    """)
    suspend fun getMaintenanceLogsByDateRange(startDate: Long, endDate: Long): List<MaintenanceLogWithMachine>
    
    @Query("""
        SELECT * FROM stock_transactions 
        WHERE date >= :startDate AND date <= :endDate 
        ORDER BY date DESC
    """)
    suspend fun getStockTransactionsByDateRange(startDate: Long, endDate: Long): List<StockTransactionEntity>

    // ================== NEW DATA NOTIFICATION QUERIES ==================
    
    // --- Latest Timestamps for Red Dot Detection ---
    
    @Query("SELECT MAX(date) FROM fuel_logs")
    fun getLatestFuelLogTimestamp(): Flow<Long?>
    
    @Query("SELECT MAX(date) FROM labor_logs")
    fun getLatestLaborLogTimestamp(): Flow<Long?>
    
    @Query("SELECT MAX(date_added) FROM bills")
    fun getLatestBillTimestamp(): Flow<Long?>
    
    @Query("SELECT MAX(timestamp) FROM transactions")
    fun getLatestTransactionTimestamp(): Flow<Long?>
    
    @Query("SELECT MAX(date_added) FROM inventory_items")
    fun getLatestInventoryTimestamp(): Flow<Long?>
    
    @Query("SELECT MAX(date) FROM maintenance_logs")
    fun getLatestMaintenanceTimestamp(): Flow<Long?>

    // --- Count Entries After Timestamp (for count badges) ---
    
    @Query("SELECT COUNT(*) FROM fuel_logs WHERE date > :afterTimestamp")
    fun countFuelLogsAfter(afterTimestamp: Long): Flow<Int>
    
    @Query("SELECT COUNT(*) FROM labor_logs WHERE date > :afterTimestamp")
    fun countLaborLogsAfter(afterTimestamp: Long): Flow<Int>
    
    @Query("SELECT COUNT(*) FROM bills WHERE date_added > :afterTimestamp")
    fun countBillsAfter(afterTimestamp: Long): Flow<Int>
    
    @Query("SELECT COUNT(*) FROM transactions WHERE timestamp > :afterTimestamp")
    fun countTransactionsAfter(afterTimestamp: Long): Flow<Int>
    
    @Query("SELECT COUNT(*) FROM stock_transactions WHERE date > :afterTimestamp")
    fun countInventoryTransactionsAfter(afterTimestamp: Long): Flow<Int>
    
    @Query("SELECT COUNT(*) FROM maintenance_logs WHERE date > :afterTimestamp")
    fun countMaintenanceLogsAfter(afterTimestamp: Long): Flow<Int>

    // --- Count Per Parent (for count badges on machine/location/employee cards) ---
    
    @Query("SELECT COUNT(*) FROM fuel_logs WHERE machine_id = :machineId AND date > :afterTimestamp")
    fun countNewFuelLogsForMachine(machineId: Int, afterTimestamp: Long): Flow<Int>
    
    @Query("SELECT COUNT(*) FROM bills WHERE location_id = :locationId AND date_added > :afterTimestamp")
    fun countNewBillsForLocation(locationId: Int, afterTimestamp: Long): Flow<Int>
    
    @Query("SELECT COUNT(*) FROM maintenance_logs WHERE machine_id = :machineId AND date > :afterTimestamp")
    fun countNewMaintenanceLogsForMachine(machineId: Int, afterTimestamp: Long): Flow<Int>
}