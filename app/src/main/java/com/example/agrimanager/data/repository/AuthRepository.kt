package com.example.agrimanager.data.repository

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val farmRepository: FarmRepository  // NEW: Inject FarmRepository for sync
) {
    // Sign Up new user
    suspend fun signUp(email: String, password: String): Result<Boolean> = try {
        auth.createUserWithEmailAndPassword(email, password).await()
        Result.success(true)
    } catch (e: Exception) {
        Result.failure(e)
    }

    // Sign In existing user
    suspend fun signIn(email: String, password: String): Result<Boolean> = try {
        auth.signInWithEmailAndPassword(email, password).await()
        
        // NEW: Download data from Firestore if local database is empty
        if (farmRepository.isLocalDatabaseEmpty()) {
            farmRepository.downloadAllDataFromFirestore()
        }
        
        Result.success(true)
    } catch (e: Exception) {
        Result.failure(e)
    }

    // Sign Out
    fun signOut() {
        auth.signOut()
    }

    // Check if user is logged in (returns user or null)
    fun getCurrentUser() = auth.currentUser
}
