package com.example.agrimanager.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val uid: String,  // Firebase Auth UID
    val email: String,
    val role: String,  // "owner" or "manager"
    val farmOwnerId: String,  // UID of farm owner (same as uid for owners)
    val addedAt: Long = System.currentTimeMillis(),
    val addedBy: String? = null  // UID of who added this user (null for owners)
)
