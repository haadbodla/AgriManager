# User Roles Implementation - Task Checklist

## Overview
Implement Owner and Manager roles. Owner has full access and can add managers. Managers can add/edit/view everything except Analytics and cannot delete anything.

## Checklist

### Phase 1: Database & Repository
- [x] Create `UserEntity` data class
- [x] Add `UserDao` interface with CRUD methods
- [x] Update `FarmDatabase` to include users table
- [x] Create `UserRepository` for user management
- [x] Update `AuthRepository` with role methods
- [x] Add role storage in SharedPreferences

### Phase 2: Permission System
- [x] Create `PermissionHelper` utility class
- [x] Add `canDelete()` method
- [x] Add `canAccessAnalytics()` method
- [x] Add `canManageUsers()` method
- [x] Update login flow to fetch and store user role

### Phase 3: User Management UI
- [x] Create `UserManagementViewModel`
- [x] Create `UserManagementScreen` (owner only)
- [x] Add manager list display
- [x] Add "Add Manager" dialog
- [x] Add "Remove Manager" confirmation
- [x] Wire up navigation

### Phase 4: Dashboard Updates
- [x] Add "Manage Users" button (owner only)
- [x] Hide Analytics button for managers
- [x] Add role badge/indicator (Owner/Manager)
- [x] Update navigation based on role

### Phase 5: List Screens Updates
- [x] Update `MachineListScreen` - hide delete for managers
- [x] Update `LocationListScreen` - hide delete for managers
- [x] Update `EmployeeListScreen` - hide delete for managers
- [x] Update `InventoryListScreen` - hide delete for managers
- [x] Update `LaborListScreen` - hide delete for managers
- [x] Update `MaintenanceListScreen` - hide delete for managers
- [x] Update `LocationBillsScreen` - hide delete for managers

### Phase 6: Firestore Integration
- [x] Update Firestore structure for managers collection
- [x] Create Firestore security rules
- [x] Test rules in Firebase console
- [x] Deploy rules to production
- [ ] Test manager data access

### Phase 7: Testing
- [ ] Test owner can add managers
- [ ] Test owner can remove managers
- [ ] Test manager cannot delete items
- [ ] Test manager cannot access Analytics
- [ ] Test manager cannot access User Management
- [ ] Test manager can add/edit all modules
- [ ] Test cross-device manager access
