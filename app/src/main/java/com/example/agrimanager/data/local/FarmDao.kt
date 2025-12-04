package com.example.agrimanager.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FarmDao {
    // --- Machines ---
    // MUST RETURN LONG (The new Row ID)
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
    suspend fun insertFuelLog(log: FuelLogEntity): Long // <--- Change to Long

    @Query("SELECT * FROM fuel_logs WHERE machine_id = :machineId ORDER BY date DESC")
    fun getFuelLogsForMachine(machineId: Int): Flow<List<FuelLogEntity>>

    @Query("SELECT SUM(total_cost) FROM fuel_logs WHERE machine_id = :machineId")
    fun getTotalCostForMachine(machineId: Int): Flow<Double?>

    // --- Locations ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocation(location: LocationEntity): Long // <--- Change to Long

    @Delete
    suspend fun deleteLocation(location: LocationEntity)

    @Query("SELECT * FROM locations ORDER BY name ASC")
    fun getAllLocations(): Flow<List<LocationEntity>>

    // --- Bills ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBill(bill: BillEntity): Long // <--- Change to Long

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
}