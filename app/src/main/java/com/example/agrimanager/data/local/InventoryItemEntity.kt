package com.example.agrimanager.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "inventory_items")
data class InventoryItemEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "item_id")
    val id: Int = 0,

    @ColumnInfo(name = "name")
    val name: String, // e.g., "Sona urea"

    @ColumnInfo(name = "category")
    val category: String, // e.g., "fertilizer"

    @ColumnInfo(name = "unit")
    val unit: String, // e.g., "Bag", "Kg"

    @ColumnInfo(name = "current_quantity")
    val currentQuantity: Double = 0.0,

    @ColumnInfo(name = "reorder_level")
    val reorderLevel: Double = 10.0,

    @ColumnInfo(name = "date_added")
    val dateAdded: Long = System.currentTimeMillis()
)
