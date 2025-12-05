package com.example.agrimanager.data.local

data class MaintenanceLogWithMachine(
    val id: Int,
    val machineId: Int,
    val machineName: String,
    val tag: String,
    val cost: Double,
    val mechanicName: String,
    val description: String,
    val date: Long
)
