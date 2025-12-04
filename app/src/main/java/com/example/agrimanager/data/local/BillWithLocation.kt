package com.example.agrimanager.data.local

import androidx.room.Embedded
import androidx.room.ColumnInfo

data class BillWithLocation(
    @Embedded val bill: BillEntity,
    @ColumnInfo(name = "location_name") val locationName: String
)