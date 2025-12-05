package com.example.agrimanager.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "stock_transactions",
    foreignKeys = [
        ForeignKey(
            entity = InventoryItemEntity::class,
            parentColumns = ["item_id"],
            childColumns = ["item_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = LocationEntity::class,
            parentColumns = ["location_id"],
            childColumns = ["location_id"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = EmployeeEntity::class,
            parentColumns = ["id"],
            childColumns = ["employee_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ]
)
data class StockTransactionEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "transaction_id")
    val id: Int = 0,

    @ColumnInfo(name = "item_id", index = true)
    val itemId: Int,

    @ColumnInfo(name = "type")
    val type: String, // "IN" for purchase, "OUT" for usage

    @ColumnInfo(name = "quantity")
    val quantity: Double,

    @ColumnInfo(name = "total_cost")
    val totalCost: Double? = null, // Only for "IN" transactions

    @ColumnInfo(name = "location_id", index = true)
    val locationId: Int? = null, // Only for "OUT" transactions

    @ColumnInfo(name = "employee_id", index = true)
    val employeeId: Int? = null, // Only for "OUT" transactions

    @ColumnInfo(name = "date")
    val date: Long = System.currentTimeMillis()
)
