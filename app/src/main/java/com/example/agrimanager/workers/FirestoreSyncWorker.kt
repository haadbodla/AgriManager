package com.example.agrimanager.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirestoreSyncWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val collection = inputData.getString("collection") ?: return Result.failure()
        val docId = inputData.getString("docId") ?: return Result.failure()
        val operation = inputData.getString("operation") ?: return Result.failure() // "set" or "delete"

        // CRITICAL: Use farm owner ID, not current user ID
        // Managers should save data under the owner's UID
        // Access SharedPreferences directly (can't use Hilt in Worker)
        val prefs = applicationContext.getSharedPreferences("agri_manager_prefs", Context.MODE_PRIVATE)
        val farmOwnerId = prefs.getString("farm_owner_id", null)
        
        // Use farm owner ID if available (for both owners and managers)
        // For owners, farmOwnerId == their own UID
        // For managers, farmOwnerId == their owner's UID
        val userId = farmOwnerId ?: FirebaseAuth.getInstance().currentUser?.uid ?: return Result.failure()
        
        val db = FirebaseFirestore.getInstance()

        return try {
            val docRef = db.collection("users").document(userId).collection(collection).document(docId)
            
            // Debug logging
            android.util.Log.d("FirestoreSyncWorker", "Syncing to: /users/$userId/$collection/$docId")
            android.util.Log.d("FirestoreSyncWorker", "Farm Owner ID: $farmOwnerId")
            android.util.Log.d("FirestoreSyncWorker", "Operation: $operation")

            if (operation == "delete") {
                docRef.delete().await()
            } else {
                // Construct the data map from inputs
                // Note: WorkManager inputs are simple (Strings/Ints).
                // We recreate the object map here.
                val dataMap = inputData.keyValueMap.filterKeys {
                    it !in listOf("collection", "docId", "operation")
                }
                android.util.Log.d("FirestoreSyncWorker", "Data: $dataMap")
                docRef.set(dataMap).await()
            }

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            // If it fails (e.g. server error), retry later automatically
            Result.retry()
        }
    }
}
