# User Roles System Implementation Plan

## Overview

Implement a two-tier user roles system: **Owner** and **Manager**. Owner has full access and can add multiple managers. Managers can add/edit/view all data except Analytics, and cannot delete anything.

---

## Roles & Permissions

| Feature | Owner | Manager |
|---------|-------|---------|
| **Machines** | ✅ Add/Edit/Delete/View | ✅ Add/Edit/View ❌ Delete |
| **Fuel Logs** | ✅ Add/Edit/Delete/View | ✅ Add/Edit/View ❌ Delete |
| **Bills/Locations** | ✅ Add/Edit/Delete/View | ✅ Add/Edit/View ❌ Delete |
| **Employees/Salary** | ✅ Add/Edit/Delete/View | ✅ Add/Edit/View ❌ Delete |
| **Inventory** | ✅ Add/Edit/Delete/View | ✅ Add/Edit/View ❌ Delete |
| **Labor Logs** | ✅ Add/Edit/Delete/View | ✅ Add/Edit/View ❌ Delete |
| **Maintenance** | ✅ Add/Edit/Delete/View | ✅ Add/Edit/View ❌ Delete |
| **Analytics** | ✅ View | ❌ No Access |
| **User Management** | ✅ Add/Remove Managers | ❌ No Access |

---

## Proposed Changes

### 1. Database Schema

#### [NEW] User Entity

```kotlin
@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val uid: String,  // Firebase Auth UID
    val email: String,
    val role: String,  // "owner" or "manager"
    val farmOwnerId: String,  // UID of farm owner
    val addedAt: Long = System.currentTimeMillis()
)
```

#### Firestore Structure

```
users/{ownerId}/
    - email: String
    - role: "owner"
    - managers/
        {managerId}/
            - email: String
            - role: "manager"
            - addedAt: Timestamp
            - addedBy: String (owner UID)
```

---

### 2. Repository Layer

#### [MODIFY] AuthRepository

Add methods:
- `getCurrentUserRole(): String?`
- `isOwner(): Boolean`
- `isManager(): Boolean`
- `getFarmOwnerId(): String?`

#### [NEW] UserRepository

Create new repository for user management:
- `addManager(email: String)`
- `removeManager(managerId: String)`
- `getAllManagers(): Flow<List<UserEntity>>`
- `getManagerById(id: String): UserEntity?`

---

### 3. ViewModel Layer

#### [NEW] UserManagementViewModel

For owner to manage managers:
- `managers: StateFlow<List<UserEntity>>`
- `addManager(email: String)`
- `removeManager(manager: UserEntity)`

#### [MODIFY] All Existing ViewModels

Add permission checks:
- Check role before delete operations
- Emit errors if unauthorized

---

### 4. UI Layer

#### [NEW] User Management Screen

**Location:** `ui/users/UserManagementScreen.kt`

Features:
- List of managers
- Add manager button (email input)
- Remove manager button
- Only accessible by owner

#### [MODIFY] Dashboard Screen

Add:
- "Manage Users" button (owner only)
- Hide Analytics button for managers
- Show role indicator (Owner/Manager badge)

#### [MODIFY] All List Screens

Update delete functionality:
- Hide delete buttons for managers
- Show "No permission" message if manager tries to delete

Affected screens:
- MachineListScreen
- LocationListScreen
- EmployeeListScreen
- InventoryListScreen
- LaborListScreen
- MaintenanceListScreen

---

### 5. Authentication Flow

#### Login Flow Update

```
1. User logs in with email/password
2. Check Firestore for user role:
   - If user is owner → role = "owner"
   - If user is in managers collection → role = "manager"
   - If user not found → create as owner (first time)
3. Store role in local preferences
4. Download data based on role
5. Navigate to dashboard
```

#### Manager Invitation Flow

```
Owner:
1. Opens User Management screen
2. Enters manager email
3. Creates manager account in Firestore

Manager:
1. Receives invitation (email/manual)
2. Creates Firebase account with same email
3. Logs in
4. System detects manager role
5. Gets access to farm data
```

---

### 6. Permission Helper

#### [NEW] PermissionHelper.kt

```kotlin
object PermissionHelper {
    fun canDelete(role: String): Boolean {
        return role == "owner"
    }
    
    fun canAccessAnalytics(role: String): Boolean {
        return role == "owner"
    }
    
    fun canManageUsers(role: String): Boolean {
        return role == "owner"
    }
}
```

---

### 7. Firestore Security Rules

Update rules to enforce permissions:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /users/{ownerId}/{document=**} {
      // Owner has full access
      allow read, write: if request.auth.uid == ownerId;
      
      // Managers have read/write but not delete
      allow read, create, update: if 
        exists(/databases/$(database)/documents/users/$(ownerId)/managers/$(request.auth.uid));
      
      // Prevent managers from deleting
      allow delete: if request.auth.uid == ownerId;
    }
  }
}
```

---

## Implementation Steps

### Phase 1: Database & Repository (1-2 hours)

1. Create `UserEntity`
2. Add to `FarmDao` and `FarmDatabase`
3. Create `UserRepository`
4. Update `AuthRepository` with role methods

### Phase 2: Permission System (1 hour)

1. Create `PermissionHelper`
2. Add role storage in SharedPreferences
3. Update login flow to fetch and store role

### Phase 3: UI Updates (2-3 hours)

1. Create `UserManagementScreen`
2. Create `UserManagementViewModel`
3. Update Dashboard:
   - Add "Manage Users" button
   - Hide Analytics for managers
   - Add role badge
4. Update all list screens:
   - Hide delete buttons for managers
   - Add permission checks

### Phase 4: Firestore Integration (1 hour)

1. Update Firestore structure for managers
2. Implement manager invitation
3. Update security rules

### Phase 5: Testing (1 hour)

1. Test owner can add/remove managers
2. Test manager cannot delete
3. Test manager cannot access Analytics
4. Test manager cannot manage users

---

## File Structure

```
data/
├── local/
│   └── UserEntity.kt (NEW)
├── repository/
│   ├── AuthRepository.kt (MODIFY)
│   └── UserRepository.kt (NEW)

ui/
├── dashboard/
│   └── DashboardScreen.kt (MODIFY)
├── users/
│   ├── UserManagementScreen.kt (NEW)
│   └── UserManagementViewModel.kt (NEW)
└── [all list screens] (MODIFY)

utils/
└── PermissionHelper.kt (NEW)
```

---

## User Experience

### Owner Dashboard
```
┌─────────────────────────┐
│ AgriManager (Owner)     │
├─────────────────────────┤
│ 🚜 Machines             │
│ ⚡ Bills                │
│ 💰 Salary               │
│ 📦 Inventory            │
│ 👷 Labor                │
│ 🔧 Maintenance          │
│ 📊 Analytics            │ ← Visible
│ 👥 Manage Users         │ ← Visible
│ 🚪 Logout               │
└─────────────────────────┘
```

### Manager Dashboard
```
┌─────────────────────────┐
│ AgriManager (Manager)   │
├─────────────────────────┤
│ 🚜 Machines             │
│ ⚡ Bills                │
│ 💰 Salary               │
│ 📦 Inventory            │
│ 👷 Labor                │
│ 🔧 Maintenance          │
│ ❌ Analytics            │ ← Hidden
│ ❌ Manage Users         │ ← Hidden
│ 🚪 Logout               │
└─────────────────────────┘
```

### Delete Button Behavior
```
Owner: [Edit] [Delete] ← Both visible
Manager: [Edit] ← Only edit visible
```

---

## Verification Plan

### Manual Testing

1. **Owner Tests:**
   - Add manager via email
   - Verify manager appears in list
   - Remove manager
   - Access all features including Analytics
   - Delete items from all modules

2. **Manager Tests:**
   - Log in as manager
   - Verify Analytics is hidden
   - Verify "Manage Users" is hidden
   - Try to delete item → should not see delete button
   - Add/Edit items in all modules → should work

3. **Cross-Device:**
   - Owner adds manager on Device A
   - Manager logs in on Device B
   - Verify manager sees correct permissions

---

## Summary

This implementation creates a simple but effective two-tier role system where:
- **Owner** has complete control
- **Managers** can help with daily operations but cannot delete data or access sensitive analytics
- Easy to expand with more roles later if needed

**Estimated Implementation Time:** 5-7 hours
