package com.example.agrimanager.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "locations")
data class LocationEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "location_id")
    val id: Int = 0,

    @ColumnInfo(name = "name")
    val name: String // e.g., "Main Farm House"
)

