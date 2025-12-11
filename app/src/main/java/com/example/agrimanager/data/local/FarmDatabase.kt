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
        MaintenanceLogEntity::class,
        UserEntity::class  // NEW: Add UserEntity
    ],
    version = 10,  // UPDATED: Increment version from 9 to 10
    exportSchema = false
)
abstract class FarmDatabase : RoomDatabase() {
    abstract fun farmDao(): FarmDao
    abstract fun userDao(): UserDao  // NEW: Add UserDao
}
