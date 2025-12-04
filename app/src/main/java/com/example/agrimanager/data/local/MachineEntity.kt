package com.example.agrimanager.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "machines")
data class MachineEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "machine_id")
    val id: Int = 0,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "service_interval_hours")
    val serviceIntervalHours: Int, // e.g., 250 hours

    @ColumnInfo(name = "last_service_reading")
    val lastServiceReading: Int // e.g., 1200 hours
)
