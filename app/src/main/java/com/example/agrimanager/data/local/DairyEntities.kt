package com.example.agrimanager.data.local

import androidx.room.*

@Entity(tableName = "dairy_companies")
data class DairyCompanyEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "company_id")
    val id: Int = 0,
    
    @ColumnInfo(name = "name")
    val name: String,
    
    @ColumnInfo(name = "rate_per_liter")
    val ratePerLiter: Double,
    
    @ColumnInfo(name = "added_at")
    val addedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "dairy_logs",
    foreignKeys = [
        ForeignKey(
            entity = DairyCompanyEntity::class,
            parentColumns = ["company_id"],
            childColumns = ["company_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ]
)
data class DairyLogEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "log_id")
    val id: Int = 0,
    
    @ColumnInfo(name = "date")
    val date: Long,
    
    @ColumnInfo(name = "company_id", index = true)
    val companyId: Int?,
    
    @ColumnInfo(name = "company_name")
    val companyName: String, // Cached for history if company is deleted
    
    @ColumnInfo(name = "liters")
    val liters: Double,
    
    @ColumnInfo(name = "total_amount")
    val totalAmount: Double,
    
    @ColumnInfo(name = "added_by")
    val addedBy: String? = null // UID of who added it
)
