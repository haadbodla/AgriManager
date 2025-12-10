package com.example.agrimanager.data.repository

import com.example.agrimanager.utils.PreferencesManager
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val farmRepository: FarmRepository,
    private val userRepository: UserRepository,  // NEW: Inject UserRepository
    private val preferencesManager: PreferencesManager  // NEW: Inject PreferencesManager
) {
    // Sign Up new user
    suspend fun signUp(email: String, password: String): Result<Boolean> {
        return try {
            // Create Firebase Auth user
            auth.createUserWithEmailAndPassword(email, password).await()
            
            // IMPORTANT: Sync user to Firestore immediately after signup
            // This creates the user document with role: "owner"
            val userSyncResult = userRepository.syncUserFromFirestore()
            if (userSyncResult.isFailure) {
                return Result.failure(userSyncResult.exceptionOrNull() ?: Exception("User sync failed"))
            }
            
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Sign In existing user
    suspend fun signIn(email: String, password: String): Result<Boolean> {
        return try {
            auth.signInWithEmailAndPassword(email, password).await()
            
            // NEW: Sync user role from Firestore
            val userSyncResult = userRepository.syncUserFromFirestore()
            if (userSyncResult.isFailure) {
                return Result.failure(userSyncResult.exceptionOrNull() ?: Exception("User sync failed"))
            }
            
            // Download data from Firestore if local database is empty
            val isEmpty = farmRepository.isLocalDatabaseEmpty()
            
            if (isEmpty) {
                val downloadResult = farmRepository.downloadAllDataFromFirestore()
                
                if (downloadResult.isFailure) {
                    return Result.failure(downloadResult.exceptionOrNull() ?: Exception("Download failed"))
                }
            }
            
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Sign Out
    fun signOut() {
        auth.signOut()
        preferencesManager.clearUserData()  // NEW: Clear cached role data
    }

    // Check if user is logged in (returns user or null)
    fun getCurrentUser() = auth.currentUser
    
    // NEW: Get current user role
    suspend fun getCurrentUserRole(): String? {
        return userRepository.getCurrentUserRole()
    }
    
    // NEW: Check if current user is owner
    suspend fun isOwner(): Boolean {
        return userRepository.isOwner()
    }
    
    // NEW: Check if current user is manager
    suspend fun isManager(): Boolean {
        return userRepository.isManager()
    }
    
    // NEW: Get farm owner ID
    suspend fun getFarmOwnerId(): String? {
        return userRepository.getFarmOwnerId()
    }
}
