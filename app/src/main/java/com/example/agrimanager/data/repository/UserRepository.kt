package com.example.agrimanager.data.repository

import com.example.agrimanager.data.local.UserDao
import com.example.agrimanager.data.local.UserEntity
import com.example.agrimanager.utils.PreferencesManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val userDao: UserDao,
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val preferencesManager: PreferencesManager
) {
    
    // Get current user's UID
    private fun getCurrentUid(): String? = auth.currentUser?.uid
    
    // Get current user from local database
    suspend fun getCurrentUser(): UserEntity? {
        val uid = getCurrentUid() ?: return null
        return userDao.getUserByUid(uid)
    }
    
    // Get user role
    suspend fun getCurrentUserRole(): String? {
        return getCurrentUser()?.role
    }
    
    // Check if current user is owner
    suspend fun isOwner(): Boolean {
        return getCurrentUserRole() == "owner"
    }
    
    // Check if current user is manager
    suspend fun isManager(): Boolean {
        return getCurrentUserRole() == "manager"
    }
    
    // Get farm owner ID (for data scoping)
    suspend fun getFarmOwnerId(): String? {
        return getCurrentUser()?.farmOwnerId
    }
    
    // Get all managers for current owner
    fun getAllManagers(): Flow<List<UserEntity>> {
        val ownerId = getCurrentUid() ?: throw IllegalStateException("User not logged in")
        return userDao.getManagersForOwner(ownerId)
    }
    
    // Add a new manager
    suspend fun addManager(email: String): Result<UserEntity> {
        return try {
            val currentUid = getCurrentUid() ?: throw IllegalStateException("User not logged in")
            val currentUser = getCurrentUser() ?: throw IllegalStateException("User not found")
            
            // Only owners can add managers
            if (currentUser.role != "owner") {
                throw IllegalStateException("Only owners can add managers")
            }
            
            // Check if manager already exists in Firestore
            val existingManager = firestore
                .collection("users")
                .document(currentUid)
                .collection("managers")
                .whereEqualTo("email", email)
                .get()
                .await()
            
            if (!existingManager.isEmpty) {
                throw IllegalStateException("Manager with this email already exists")
            }
            
            // Create manager document in Firestore
            val managerData = hashMapOf(
                "email" to email,
                "role" to "manager",
                "addedAt" to System.currentTimeMillis(),
                "addedBy" to currentUid,
                "farmOwnerId" to currentUid
            )
            
            // Add to Firestore (we'll use email as temporary ID until they sign up)
            val managerRef = firestore
                .collection("users")
                .document(currentUid)
                .collection("managers")
                .document()  // Auto-generate ID
            
            managerRef.set(managerData).await()
            
            // Create local entity (temporary UID until manager signs up)
            val managerEntity = UserEntity(
                uid = managerRef.id,  // Use Firestore doc ID as temporary UID
                email = email,
                role = "manager",
                farmOwnerId = currentUid,
                addedAt = System.currentTimeMillis(),
                addedBy = currentUid
            )
            
            // Insert into local database
            userDao.insertUser(managerEntity)
            
            Result.success(managerEntity)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Remove a manager
    suspend fun removeManager(manager: UserEntity): Result<Boolean> {
        return try {
            val currentUid = getCurrentUid() ?: throw IllegalStateException("User not logged in")
            val currentUser = getCurrentUser() ?: throw IllegalStateException("User not found")
            
            // Only owners can remove managers
            if (currentUser.role != "owner") {
                throw IllegalStateException("Only owners can remove managers")
            }
            
            // Delete from Firestore
            firestore
                .collection("users")
                .document(currentUid)
                .collection("managers")
                .document(manager.uid)
                .delete()
                .await()
            
            // Delete from local database
            userDao.deleteUser(manager)
            
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Save or update user in local database
    suspend fun saveUser(user: UserEntity) {
        userDao.insertUser(user)
    }
    
    // Get manager by ID
    suspend fun getManagerById(id: String): UserEntity? {
        return userDao.getUserByUid(id)
    }
    
    // Sync user from Firestore (called during login)
    suspend fun syncUserFromFirestore(): Result<UserEntity> {
        return try {
            val uid = getCurrentUid() ?: throw IllegalStateException("User not logged in")
            val email = auth.currentUser?.email ?: throw IllegalStateException("Email not found")
            
            // Check if user is an owner
            val ownerDoc = firestore
                .collection("users")
                .document(uid)
                .get()
                .await()
            
            val userEntity = if (ownerDoc.exists()) {
                // User is an owner
                UserEntity(
                    uid = uid,
                    email = email,
                    role = "owner",
                    farmOwnerId = uid,
                    addedAt = ownerDoc.getLong("addedAt") ?: System.currentTimeMillis(),
                    addedBy = null
                )
            } else {
                // Check if user is a manager under any owner
                val managersQuery = firestore
                    .collectionGroup("managers")
                    .whereEqualTo("email", email)
                    .get()
                    .await()
                
                if (managersQuery.isEmpty) {
                    // New user - create as owner
                    val newOwnerData = hashMapOf(
                        "email" to email,
                        "role" to "owner",
                        "farmOwnerId" to uid,  // CRITICAL: Required by Firestore rules
                        "addedAt" to System.currentTimeMillis()
                    )
                    
                    firestore
                        .collection("users")
                        .document(uid)
                        .set(newOwnerData)
                        .await()
                    
                    UserEntity(
                        uid = uid,
                        email = email,
                        role = "owner",
                        farmOwnerId = uid,
                        addedAt = System.currentTimeMillis(),
                        addedBy = null
                    )
                } else {
                    // User is a manager
                    val managerDoc = managersQuery.documents.first()
                    val ownerId = managerDoc.reference.parent.parent?.id 
                        ?: throw IllegalStateException("Owner ID not found")
                    
                    UserEntity(
                        uid = uid,
                        email = email,
                        role = "manager",
                        farmOwnerId = ownerId,
                        addedAt = managerDoc.getLong("addedAt") ?: System.currentTimeMillis(),
                        addedBy = managerDoc.getString("addedBy")
                    )
                }
            }
            
            // Save to local database
            userDao.insertUser(userEntity)
            
            // Save to SharedPreferences for quick access
            preferencesManager.saveUserRole(userEntity.role)
            preferencesManager.saveFarmOwnerId(userEntity.farmOwnerId)
            preferencesManager.saveUserEmail(userEntity.email)
            
            Result.success(userEntity)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Quick role check from preferences (no suspend needed)
    fun isOwnerSync(): Boolean {
        return preferencesManager.isOwner()
    }
    
    fun isManagerSync(): Boolean {
        return preferencesManager.isManager()
    }
}
