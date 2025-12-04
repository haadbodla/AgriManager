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

        // We need the User ID to save to the right path
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return Result.failure()
        val db = FirebaseFirestore.getInstance()

        return try {
            val docRef = db.collection("users").document(userId).collection(collection).document(docId)

            if (operation == "delete") {
                docRef.delete().await()
            } else {
                // Construct the data map from inputs
                // Note: WorkManager inputs are simple (Strings/Ints).
                // We recreate the object map here.
                val dataMap = inputData.keyValueMap.filterKeys {
                    it !in listOf("collection", "docId", "operation")
                }
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
