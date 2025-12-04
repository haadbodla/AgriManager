package com.example.agrimanager.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "fuel_logs",
    foreignKeys = [
        ForeignKey(
            entity = MachineEntity::class,
            parentColumns = ["machine_id"],
            childColumns = ["machine_id"],
            onDelete = ForeignKey.CASCADE // If machine is deleted, delete its logs
        )
    ]
)
data class FuelLogEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "log_id")
    val id: Int = 0,

    @ColumnInfo(name = "machine_id", index = true)
    val machineId: Int,

    @ColumnInfo(name = "date")
    val date: Long, // Store as Timestamp

    @ColumnInfo(name = "liters")
    val liters: Double,

    @ColumnInfo(name = "rate")
    val rate: Double,

    @ColumnInfo(name = "total_cost")
    val totalCost: Double,

    @ColumnInfo(name = "hour_meter_reading")
    val hourMeterReading: Int
)
