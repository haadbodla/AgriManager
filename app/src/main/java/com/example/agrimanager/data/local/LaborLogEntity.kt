package com.example.agrimanager.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "labor_logs",
    foreignKeys = [
        ForeignKey(
            entity = EmployeeEntity::class,
            parentColumns = ["id"],
            childColumns = ["employee_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class LaborLogEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "log_id")
    val id: Int = 0,

    @ColumnInfo(name = "employee_id", index = true)
    val employeeId: Int, // The "Munshi" who managed the labor

    @ColumnInfo(name = "labor_count")
    val laborCount: Int,

    @ColumnInfo(name = "work_type")
    val workType: String, // Harvesting, Watering, Weeding, Sowing, Fertilizing

    @ColumnInfo(name = "total_amount")
    val totalAmount: Double,

    @ColumnInfo(name = "date")
    val date: Long = System.currentTimeMillis()
)
