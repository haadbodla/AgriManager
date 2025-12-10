package com.example.agrimanager.utils

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Centralized permission management for user roles
 * Provides methods to check what actions users can perform based on their role
 */
@Singleton
class PermissionHelper @Inject constructor(
    private val preferencesManager: PreferencesManager
) {
    
    companion object {
        // Role constants
        const val ROLE_OWNER = "owner"
        const val ROLE_MANAGER = "manager"
        
        // Feature constants
        const val FEATURE_MACHINES = "machines"
        const val FEATURE_FUEL_LOGS = "fuel_logs"
        const val FEATURE_BILLS = "bills"
        const val FEATURE_LOCATIONS = "locations"
        const val FEATURE_EMPLOYEES = "employees"
        const val FEATURE_SALARY = "salary"
        const val FEATURE_INVENTORY = "inventory"
        const val FEATURE_LABOR = "labor"
        const val FEATURE_MAINTENANCE = "maintenance"
        const val FEATURE_ANALYTICS = "analytics"
        const val FEATURE_USER_MANAGEMENT = "user_management"
        
        // Permission constants
        const val PERMISSION_VIEW = "view"
        const val PERMISSION_ADD = "add"
        const val PERMISSION_EDIT = "edit"
        const val PERMISSION_DELETE = "delete"
    }
    
    /**
     * Get current user role from preferences
     */
    private fun getCurrentRole(): String? {
        return preferencesManager.getUserRole()
    }
    
    /**
     * Check if user can delete items
     * Only owners can delete
     */
    fun canDelete(): Boolean {
        return getCurrentRole() == ROLE_OWNER
    }
    
    /**
     * Check if user can delete items in a specific feature
     */
    fun canDelete(feature: String): Boolean {
        return when (feature) {
            FEATURE_MACHINES,
            FEATURE_FUEL_LOGS,
            FEATURE_BILLS,
            FEATURE_LOCATIONS,
            FEATURE_EMPLOYEES,
            FEATURE_SALARY,
            FEATURE_INVENTORY,
            FEATURE_LABOR,
            FEATURE_MAINTENANCE -> canDelete()
            else -> false
        }
    }
    
    /**
     * Check if user can access Analytics
     * Only owners can access analytics
     */
    fun canAccessAnalytics(): Boolean {
        return getCurrentRole() == ROLE_OWNER
    }
    
    /**
     * Check if user can manage other users
     * Only owners can manage users
     */
    fun canManageUsers(): Boolean {
        return getCurrentRole() == ROLE_OWNER
    }
    
    /**
     * Check if user can add items
     * Both owners and managers can add
     */
    fun canAdd(): Boolean {
        val role = getCurrentRole()
        return role == ROLE_OWNER || role == ROLE_MANAGER
    }
    
    /**
     * Check if user can add items in a specific feature
     */
    fun canAdd(feature: String): Boolean {
        return when (feature) {
            FEATURE_MACHINES,
            FEATURE_FUEL_LOGS,
            FEATURE_BILLS,
            FEATURE_LOCATIONS,
            FEATURE_EMPLOYEES,
            FEATURE_SALARY,
            FEATURE_INVENTORY,
            FEATURE_LABOR,
            FEATURE_MAINTENANCE -> canAdd()
            FEATURE_ANALYTICS,
            FEATURE_USER_MANAGEMENT -> false  // Cannot "add" to these features
            else -> false
        }
    }
    
    /**
     * Check if user can edit items
     * Both owners and managers can edit
     */
    fun canEdit(): Boolean {
        val role = getCurrentRole()
        return role == ROLE_OWNER || role == ROLE_MANAGER
    }
    
    /**
     * Check if user can edit items in a specific feature
     */
    fun canEdit(feature: String): Boolean {
        return when (feature) {
            FEATURE_MACHINES,
            FEATURE_FUEL_LOGS,
            FEATURE_BILLS,
            FEATURE_LOCATIONS,
            FEATURE_EMPLOYEES,
            FEATURE_SALARY,
            FEATURE_INVENTORY,
            FEATURE_LABOR,
            FEATURE_MAINTENANCE -> canEdit()
            FEATURE_ANALYTICS,
            FEATURE_USER_MANAGEMENT -> false
            else -> false
        }
    }
    
    /**
     * Check if user can view items
     * Both owners and managers can view most features
     */
    fun canView(feature: String): Boolean {
        val role = getCurrentRole()
        
        return when (feature) {
            FEATURE_MACHINES,
            FEATURE_FUEL_LOGS,
            FEATURE_BILLS,
            FEATURE_LOCATIONS,
            FEATURE_EMPLOYEES,
            FEATURE_SALARY,
            FEATURE_INVENTORY,
            FEATURE_LABOR,
            FEATURE_MAINTENANCE -> role == ROLE_OWNER || role == ROLE_MANAGER
            
            FEATURE_ANALYTICS,
            FEATURE_USER_MANAGEMENT -> role == ROLE_OWNER
            
            else -> false
        }
    }
    
    /**
     * Check if user has a specific permission for a feature
     */
    fun hasPermission(feature: String, permission: String): Boolean {
        return when (permission) {
            PERMISSION_VIEW -> canView(feature)
            PERMISSION_ADD -> canAdd(feature)
            PERMISSION_EDIT -> canEdit(feature)
            PERMISSION_DELETE -> canDelete(feature)
            else -> false
        }
    }
    
    /**
     * Get user role display name
     */
    fun getRoleDisplayName(): String {
        return when (getCurrentRole()) {
            ROLE_OWNER -> "Owner"
            ROLE_MANAGER -> "Manager"
            else -> "Unknown"
        }
    }
    
    /**
     * Check if user is owner
     */
    fun isOwner(): Boolean {
        return getCurrentRole() == ROLE_OWNER
    }
    
    /**
     * Check if user is manager
     */
    fun isManager(): Boolean {
        return getCurrentRole() == ROLE_MANAGER
    }
    
    /**
     * Get all permissions for current user
     * Returns a map of feature -> list of permissions
     */
    fun getAllPermissions(): Map<String, List<String>> {
        val features = listOf(
            FEATURE_MACHINES,
            FEATURE_FUEL_LOGS,
            FEATURE_BILLS,
            FEATURE_LOCATIONS,
            FEATURE_EMPLOYEES,
            FEATURE_SALARY,
            FEATURE_INVENTORY,
            FEATURE_LABOR,
            FEATURE_MAINTENANCE,
            FEATURE_ANALYTICS,
            FEATURE_USER_MANAGEMENT
        )
        
        val permissions = listOf(
            PERMISSION_VIEW,
            PERMISSION_ADD,
            PERMISSION_EDIT,
            PERMISSION_DELETE
        )
        
        return features.associateWith { feature ->
            permissions.filter { permission ->
                hasPermission(feature, permission)
            }
        }
    }
    
    /**
     * Validate if action is allowed, throw exception if not
     * Useful for repository/ViewModel validation
     */
    fun requirePermission(feature: String, permission: String) {
        if (!hasPermission(feature, permission)) {
            throw SecurityException(
                "Permission denied: ${getRoleDisplayName()} cannot $permission $feature"
            )
        }
    }
    
    /**
     * Validate delete permission, throw exception if not allowed
     */
    fun requireDeletePermission(feature: String = "items") {
        if (!canDelete()) {
            throw SecurityException(
                "Permission denied: ${getRoleDisplayName()} cannot delete $feature"
            )
        }
    }
}
