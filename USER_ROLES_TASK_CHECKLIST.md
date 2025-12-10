# User Roles Implementation - Task Checklist

## Overview
Implement Owner and Manager roles. Owner has full access and can add managers. Managers can add/edit/view everything except Analytics and cannot delete anything.

## Checklist

### Phase 1: Database & Repository
- [ ] Create `UserEntity` data class
- [ ] Add `UserDao` interface with CRUD methods
- [ ] Update `FarmDatabase` to include users table
- [ ] Create `UserRepository` for user management
- [ ] Update `AuthRepository` with role methods
- [ ] Add role storage in SharedPreferences

### Phase 2: Permission System
- [ ] Create `PermissionHelper` utility class
- [ ] Add `canDelete()` method
- [ ] Add `canAccessAnalytics()` method
- [ ] Add `canManageUsers()` method
- [ ] Update login flow to fetch and store user role

### Phase 3: User Management UI
- [ ] Create `UserManagementViewModel`
- [ ] Create `UserManagementScreen` (owner only)
- [ ] Add manager list display
- [ ] Add "Add Manager" dialog
- [ ] Add "Remove Manager" confirmation
- [ ] Wire up navigation

### Phase 4: Dashboard Updates
- [ ] Add "Manage Users" button (owner only)
- [ ] Hide Analytics button for managers
- [ ] Add role badge/indicator (Owner/Manager)
- [ ] Update navigation based on role

### Phase 5: List Screens Updates
- [ ] Update `MachineListScreen` - hide delete for managers
- [ ] Update `LocationListScreen` - hide delete for managers
- [ ] Update `EmployeeListScreen` - hide delete for managers
- [ ] Update `InventoryListScreen` - hide delete for managers
- [ ] Update `LaborListScreen` - hide delete for managers
- [ ] Update `MaintenanceListScreen` - hide delete for managers

### Phase 6: Firestore Integration
- [ ] Update Firestore structure for managers collection
- [ ] Implement manager invitation flow
- [ ] Update Firestore security rules
- [ ] Test manager data access

### Phase 7: Testing
- [ ] Test owner can add managers
- [ ] Test owner can remove managers
- [ ] Test manager cannot delete items
- [ ] Test manager cannot access Analytics
- [ ] Test manager cannot access User Management
- [ ] Test manager can add/edit all modules
- [ ] Test cross-device manager access
