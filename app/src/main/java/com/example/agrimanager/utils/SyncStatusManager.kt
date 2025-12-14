package com.example.agrimanager.utils

import android.content.Context
import androidx.concurrent.futures.await
import androidx.work.WorkInfo
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

sealed class SyncStatus {
    object Synced : SyncStatus()
    data class Pending(val count: Int) : SyncStatus()
    object Syncing : SyncStatus()
    object Offline : SyncStatus()
}

@Singleton
class SyncStatusManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val networkMonitor: NetworkMonitor
) {
    private val workManager = WorkManager.getInstance(context)

    // Poll for sync status every 3 seconds
    private val syncCountFlow: Flow<Pair<Int, Int>> = flow {
        while (true) {
            try {
                val workInfos: List<WorkInfo> = workManager.getWorkInfosByTag("firestore_sync").await()
                val pendingCount = workInfos.count { workInfo: WorkInfo -> 
                    workInfo.state == WorkInfo.State.ENQUEUED 
                }
                val runningCount = workInfos.count { workInfo: WorkInfo -> 
                    workInfo.state == WorkInfo.State.RUNNING 
                }
                emit(Pair(pendingCount, runningCount))
            } catch (e: Exception) {
                emit(Pair(0, 0))
            }
            delay(3000) // Poll every 3 seconds
        }
    }

    // Get sync status as a Flow
    fun getSyncStatus(): Flow<SyncStatus> {
        return combine(syncCountFlow, networkMonitor.isOnline) { counts, isOnline ->
            val pending = counts.first
            val running = counts.second
            when {
                !isOnline -> SyncStatus.Offline
                running > 0 -> SyncStatus.Syncing
                pending > 0 -> SyncStatus.Pending(pending)
                else -> SyncStatus.Synced
            }
        }
    }

    // Get current pending count synchronously (for non-suspend contexts)
    fun getPendingSyncCount(): Int {
        return try {
            val workInfos = workManager.getWorkInfosByTag("firestore_sync").get()
            workInfos.count { workInfo: WorkInfo -> 
                workInfo.state == WorkInfo.State.ENQUEUED || 
                workInfo.state == WorkInfo.State.RUNNING 
            }
        } catch (e: Exception) {
            0
        }
    }
}
