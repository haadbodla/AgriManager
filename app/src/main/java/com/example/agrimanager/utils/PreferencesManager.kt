package com.example.agrimanager.utils

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferencesManager @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "agri_manager_prefs",
        Context.MODE_PRIVATE
    )
    
    companion object {
        private const val KEY_USER_ROLE = "user_role"
        private const val KEY_FARM_OWNER_ID = "farm_owner_id"
        private const val KEY_USER_EMAIL = "user_email"
    }
    
    // Save user role
    fun saveUserRole(role: String) {
        prefs.edit().putString(KEY_USER_ROLE, role).apply()
    }
    
    // Get user role
    fun getUserRole(): String? {
        return prefs.getString(KEY_USER_ROLE, null)
    }
    
    // Save farm owner ID
    fun saveFarmOwnerId(ownerId: String) {
        prefs.edit().putString(KEY_FARM_OWNER_ID, ownerId).apply()
    }
    
    // Get farm owner ID
    fun getFarmOwnerId(): String? {
        return prefs.getString(KEY_FARM_OWNER_ID, null)
    }
    
    // Save user email
    fun saveUserEmail(email: String) {
        prefs.edit().putString(KEY_USER_EMAIL, email).apply()
    }
    
    // Get user email
    fun getUserEmail(): String? {
        return prefs.getString(KEY_USER_EMAIL, null)
    }
    
    // Clear all user data (on logout)
    fun clearUserData() {
        prefs.edit()
            .remove(KEY_USER_ROLE)
            .remove(KEY_FARM_OWNER_ID)
            .remove(KEY_USER_EMAIL)
            .apply()
    }
    
    // Check if user is owner
    fun isOwner(): Boolean {
        return getUserRole() == "owner"
    }
    
    // Check if user is manager
    fun isManager(): Boolean {
        return getUserRole() == "manager"
    }
}
