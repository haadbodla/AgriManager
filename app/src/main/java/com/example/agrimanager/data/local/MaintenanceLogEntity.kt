package com.example.agrimanager.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "maintenance_logs"
)
data class MaintenanceLogEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "log_id")
    val id: Int = 0,

    @ColumnInfo(name = "machine_id", index = true)
    val machineId: Int,

    @ColumnInfo(name = "tag")
    val tag: String, // Oil Change, Tyre, Battery, Engine, Other

    @ColumnInfo(name = "cost")
    val cost: Double,

    @ColumnInfo(name = "mechanic_name")
    val mechanicName: String,

    @ColumnInfo(name = "description")
    val description: String,

    @ColumnInfo(name = "date")
    val date: Long = System.currentTimeMillis()
)
