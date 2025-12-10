package com.example.agrimanager.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    // Insert user (for both owner and manager)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)
    
    // Update user
    @Update
    suspend fun updateUser(user: UserEntity)
    
    // Delete user (for removing managers)
    @Delete
    suspend fun deleteUser(user: UserEntity)
    
    // Get user by UID
    @Query("SELECT * FROM users WHERE uid = :uid LIMIT 1")
    suspend fun getUserByUid(uid: String): UserEntity?
    
    // Get user by email
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?
    
    // Get all managers for a specific owner
    @Query("SELECT * FROM users WHERE farmOwnerId = :ownerId AND role = 'manager' ORDER BY addedAt DESC")
    fun getManagersForOwner(ownerId: String): Flow<List<UserEntity>>
    
    // Get all managers for a specific owner (non-Flow for one-time queries)
    @Query("SELECT * FROM users WHERE farmOwnerId = :ownerId AND role = 'manager' ORDER BY addedAt DESC")
    suspend fun getManagersForOwnerList(ownerId: String): List<UserEntity>
    
    // Check if user exists
    @Query("SELECT COUNT(*) FROM users WHERE uid = :uid")
    suspend fun userExists(uid: String): Int
    
    // Delete all managers for an owner (cleanup)
    @Query("DELETE FROM users WHERE farmOwnerId = :ownerId AND role = 'manager'")
    suspend fun deleteAllManagersForOwner(ownerId: String)
    
    // Get owner by farm owner ID
    @Query("SELECT * FROM users WHERE uid = :ownerId AND role = 'owner' LIMIT 1")
    suspend fun getOwnerByUid(ownerId: String): UserEntity?
}
