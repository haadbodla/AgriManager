package com.example.agrimanager.utils

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Tracks "new" vs "seen" data for each module.
 * Uses SharedPreferences to store the last time the user viewed each module.
 * Data added after this timestamp is considered "new" and shows notifications.
 */
@Singleton
class NewDataTracker @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val PREFS_NAME = "new_data_tracker"
        
        // Module keys for tracking
        const val MODULE_FUEL = "fuel"
        const val MODULE_LABOR = "labor"
        const val MODULE_INVENTORY = "inventory"
        const val MODULE_BILLS = "bills"
        const val MODULE_SALARY = "salary"
        const val MODULE_MAINTENANCE = "maintenance"
        const val MODULE_DAIRY = "dairy"
    }
    
    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
    
    /**
     * Get the timestamp when user last viewed a module.
     * Returns 0 if never viewed (all data will be "new" initially, then marked seen).
     */
    fun getLastSeenTimestamp(module: String): Long {
        return prefs.getLong("last_seen_$module", System.currentTimeMillis())
    }
    
    /**
     * Mark a module as "seen" with the current timestamp.
     * Call this when user opens a module screen.
     */
    fun markModuleAsSeen(module: String) {
        prefs.edit().putLong("last_seen_$module", System.currentTimeMillis()).apply()
    }
    
    /**
     * Check if an entry is "new" (created after last seen timestamp).
     */
    fun isNew(module: String, createdAt: Long): Boolean {
        val lastSeen = getLastSeenTimestamp(module)
        return createdAt > lastSeen
    }
    
    /**
     * Count how many entries are "new" for a given module.
     */
    fun countNewEntries(module: String, createdAtList: List<Long>): Int {
        val lastSeen = getLastSeenTimestamp(module)
        return createdAtList.count { it > lastSeen }
    }
    
    /**
     * Check if a module has any new data.
     */
    fun hasNewData(module: String, latestCreatedAt: Long): Boolean {
        return latestCreatedAt > getLastSeenTimestamp(module)
    }
    
    /**
     * Reset all tracking (for logout or testing).
     */
    fun resetAll() {
        prefs.edit().clear().apply()
    }
}
