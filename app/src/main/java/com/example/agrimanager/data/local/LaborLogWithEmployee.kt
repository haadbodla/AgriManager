package com.example.agrimanager.data.local

data class LaborLogWithEmployee(
    val id: Int,
    val employeeId: Int,
    val employeeName: String,
    val laborCount: Int,
    val workType: String,
    val totalAmount: Double,
    val date: Long
)
