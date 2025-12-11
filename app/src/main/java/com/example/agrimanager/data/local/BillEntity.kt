package com.example.agrimanager.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "bills"
)
data class BillEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "bill_id")
    val id: Int = 0,

    @ColumnInfo(name = "location_id", index = true)
    val locationId: Int,

    @ColumnInfo(name = "billing_month")
    val billingMonth: String, // e.g., "July 2025"

    @ColumnInfo(name = "amount")
    val amount: Double,

    @ColumnInfo(name = "date_added")
    val dateAdded: Long = System.currentTimeMillis()
)
