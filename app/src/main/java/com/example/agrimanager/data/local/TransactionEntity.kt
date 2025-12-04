package com.example.agrimanager.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo(name = "employee_id")
    val employeeId: Int,
    val amount: Double, // negative for advances, positive for salary credit
    val type: String, // "DEBIT" or "CREDIT"
    val timestamp: Long
)
