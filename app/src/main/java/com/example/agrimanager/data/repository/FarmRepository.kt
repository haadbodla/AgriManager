package com.example.agrimanager.data.repository

import android.content.Context
import androidx.work.* // Imports WorkManager, Constraints, NetworkType, etc.
import com.example.agrimanager.data.local.*
import com.example.agrimanager.ui.*
import com.example.agrimanager.workers.FirestoreSyncWorker
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import java.util.concurrent.TimeUnit // <--- Crucial import for time
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FarmRepository @Inject constructor(
    private val dao: FarmDao,
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
            "serviceIntervalHours" to machine.serviceIntervalHours,
            "lastServiceReading" to machine.lastServiceReading
        )
        scheduleSync("machines", id.toString(), data)
    }

    suspend fun deleteMachine(machine: MachineEntity) {
        dao.deleteMachine(machine)
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

    suspend fun deleteLocation(location: LocationEntity) {
        dao.deleteLocation(location)
        scheduleSync("locations", location.id.toString(), emptyMap(), isDelete = true)
    }


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
    // ================== EMPLOYEE OPERATIONS ==================
    suspend fun insertEmployee(employee: EmployeeEntity) {
        dao.insertEmployee(employee)
    }
    suspend fun deleteEmployee(employee: EmployeeEntity) {
        dao.deleteEmployee(employee)
    }
    fun getAllEmployees(): Flow<List<EmployeeEntity>> = dao.getAllEmployees()

    suspend fun getEmployeeById(id: Int): EmployeeEntity? = dao.getEmployeeById(id)

    // ================== TRANSACTION OPERATIONS ==================
    suspend fun insertTransaction(transaction: TransactionEntity) {
        dao.insertTransaction(transaction)
    }

    fun getTransactionsForEmployee(employeeId: Int): Flow<List<TransactionEntity>> = dao.getTransactionsForEmployee(employeeId)

    fun getTotalAdvances(employeeId: Int): Flow<Double?> = dao.getTotalAdvances(employeeId)

}