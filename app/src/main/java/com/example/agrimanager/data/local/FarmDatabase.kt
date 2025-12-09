package com.example.agrimanager.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.agrimanager.data.local.FuelLogEntity
import com.example.agrimanager.data.local.MachineEntity

@Database(
    entities = [
        MachineEntity::class,
        FuelLogEntity::class,
        LocationEntity::class,
        BillEntity::class,
        EmployeeEntity::class,
        TransactionEntity::class,
        InventoryItemEntity::class,
        StockTransactionEntity::class,
        LaborLogEntity::class,
        MaintenanceLogEntity::class
    ],
    version = 8, exportSchema = false
)
abstract class FarmDatabase : RoomDatabase() {
    abstract fun farmDao(): FarmDao
}
