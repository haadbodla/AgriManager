# User Data Sync on Login - Implementation Plan

## Overview

Implement a complete data synchronization system that loads all user data from Firestore when an existing user logs in, ensuring their data is available offline and synchronized across devices.

---

## Current Architecture Analysis

### ✅ What's Already Working

**Upload (Local → Firestore)**:
- ✅ Data is synced TO Firestore when created/modified locally
- ✅ Uses `WorkManager` for reliable background sync
- ✅ Firestore structure: `users/{userId}/collections/{docId}`
- ✅ Collections: machines, fuel_logs, bills, locations, employees, transactions, inventory_items, stock_transactions, labor_logs, maintenance_logs

**Current Flow**:
```
User Action → Local Room DB → scheduleSync() → WorkManager → FirestoreSyncWorker → Firestore
```

### ❌ What's Missing

**Download (Firestore → Local)**:
- ❌ No data download when user logs in
- ❌ New device = empty local database
- ❌ No sync from Firestore to Room
- ❌ No conflict resolution

---

## Proposed Solution

### Architecture

```
Login Success
    ↓
Check if first login on this device
    ↓
Download all collections from Firestore
    ↓
Insert into local Room database
    ↓
Navigate to Dashboard (data available)
```

---

## Implementation Steps

### Phase 1: Create Data Download Worker

#### 1.1 Create FirestoreDownloadWorker

**New File**: `workers/FirestoreDownloadWorker.kt`

**Purpose**: Download all user data from Firestore and insert into Room

**Key Features**:
- Download all collections for current user
- Convert Firestore documents to Room entities
- Insert into local database
- Handle errors gracefully
- Report progress

**Collections to Download**:
1. `machines` → `MachineEntity`
2. `fuel_logs` → `FuelLogEntity`
3. `bills` → `BillEntity`
4. `locations` → `LocationEntity`
5. `employees` → `EmployeeEntity`
6. `transactions` → `TransactionEntity`
7. `inventory_items` → `InventoryItemEntity`
8. `stock_transactions` → `StockTransactionEntity`
9. `labor_logs` → `LaborLogEntity`
10. `maintenance_logs` → `MaintenanceLogEntity`

---

### Phase 2: Add Download Methods to FarmRepository

#### 2.1 Add Sync Methods

```kotlin
// Check if local DB is empty (first login on device)
suspend fun isLocalDatabaseEmpty(): Boolean

// Download all data from Firestore
suspend fun downloadAllDataFromFirestore(): Result<Boolean>

// Download specific collection
private suspend fun downloadCollection<T>(
    collection: String,
    converter: (Map<String, Any>) -> T,
    inserter: suspend (T) -> Unit
)
```

#### 2.2 Implement Converters

Convert Firestore documents to Room entities:
```kotlin
private fun firestoreToMachine(data: Map<String, Any>): MachineEntity
private fun firestoreToFuelLog(data: Map<String, Any>): FuelLogEntity
// ... for each entity type
```

---

### Phase 3: Update AuthRepository

#### 3.1 Add Post-Login Hook

```kotlin
suspend fun signIn(email: String, password: String): Result<Boolean> = try {
    auth.signInWithEmailAndPassword(email, password).await()
    
    // NEW: Trigger data sync after successful login
    onLoginSuccess()
    
    Result.success(true)
} catch (e: Exception) {
    Result.failure(e)
}

private suspend fun onLoginSuccess() {
    // Check if we need to download data
    if (farmRepository.isLocalDatabaseEmpty()) {
        farmRepository.downloadAllDataFromFirestore()
    }
}
```

---

### Phase 4: Add Loading State to UI

#### 4.1 Update AuthViewModel

```kotlin
private val _syncState = MutableStateFlow<SyncState>(SyncState.Idle)
val syncState: StateFlow<SyncState> = _syncState

sealed class SyncState {
    object Idle : SyncState()
    object Syncing : SyncState()
    data class Progress(val message: String, val percent: Int) : SyncState()
    object Success : SyncState()
    data class Error(val message: String) : SyncState()
}
```

#### 4.2 Update LoginScreen

Show loading indicator during sync:
```kotlin
when (val state = syncState.collectAsState().value) {
    is SyncState.Syncing -> {
        // Show loading dialog
        LoadingDialog("Syncing your data...")
    }
    is SyncState.Progress -> {
        // Show progress
        LoadingDialog("${state.message} (${state.percent}%)")
    }
    // ...
}
```

---

## Detailed Implementation

### Step 1: Create FirestoreDownloadWorker

```kotlin
package com.example.agrimanager.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.agrimanager.data.local.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirestoreDownloadWorker(
    context: Context,
    workerParams: WorkerParameters,
    private val dao: FarmDao
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val userId = FirebaseAuth.getInstance().currentUser?.uid 
            ?: return Result.failure()
        
        val db = FirebaseFirestore.getInstance()
        
        return try {
            // Download each collection
            downloadMachines(db, userId)
            downloadFuelLogs(db, userId)
            downloadBills(db, userId)
            downloadLocations(db, userId)
            downloadEmployees(db, userId)
            downloadTransactions(db, userId)
            downloadInventoryItems(db, userId)
            downloadStockTransactions(db, userId)
            downloadLaborLogs(db, userId)
            downloadMaintenanceLogs(db, userId)
            
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }
    
    private suspend fun downloadMachines(db: FirebaseFirestore, userId: String) {
        val snapshot = db.collection("users")
            .document(userId)
            .collection("machines")
            .get()
            .await()
        
        snapshot.documents.forEach { doc ->
            val data = doc.data ?: return@forEach
            val machine = MachineEntity(
                id = (data["id"] as? Long)?.toInt() ?: 0,
                name = data["name"] as? String ?: "",
                serviceIntervalHours = (data["serviceIntervalHours"] as? Long)?.toInt() ?: 0,
                lastServiceReading = (data["lastServiceReading"] as? Long)?.toInt() ?: 0
            )
            dao.insertMachine(machine)
        }
    }
    
    // Similar methods for other collections...
}
```

---

### Step 2: Add Methods to FarmRepository

```kotlin
// In FarmRepository.kt

suspend fun isLocalDatabaseEmpty(): Boolean {
    return dao.getMachineCount() == 0 &&
           dao.getEmployeeCount() == 0 &&
           dao.getLocationCount() == 0
}

suspend fun downloadAllDataFromFirestore(): Result<Boolean> {
    if (auth.currentUser == null) return Result.failure(Exception("Not logged in"))
    
    return try {
        val userId = auth.currentUser!!.uid
        val db = FirebaseFirestore.getInstance()
        
        // Download all collections
        downloadMachines(db, userId)
        downloadFuelLogs(db, userId)
        downloadBills(db, userId)
        downloadLocations(db, userId)
        downloadEmployees(db, userId)
        downloadTransactions(db, userId)
        downloadInventoryItems(db, userId)
        downloadStockTransactions(db, userId)
        downloadLaborLogs(db, userId)
        downloadMaintenanceLogs(db, userId)
        
        Result.success(true)
    } catch (e: Exception) {
        e.printStackTrace()
        Result.failure(e)
    }
}

private suspend fun downloadMachines(db: FirebaseFirestore, userId: String) {
    val snapshot = db.collection("users")
        .document(userId)
        .collection("machines")
        .get()
        .await()
    
    snapshot.documents.forEach { doc ->
        val data = doc.data ?: return@forEach
        val machine = MachineEntity(
            id = (data["id"] as? Long)?.toInt() ?: 0,
            name = data["name"] as? String ?: "",
            serviceIntervalHours = (data["serviceIntervalHours"] as? Long)?.toInt() ?: 0,
            lastServiceReading = (data["lastServiceReading"] as? Long)?.toInt() ?: 0
        )
        dao.insertMachine(machine)
    }
}

// Repeat for each collection type...
```

---

### Step 3: Add Count Methods to FarmDao

```kotlin
// In FarmDao.kt

@Query("SELECT COUNT(*) FROM machines")
suspend fun getMachineCount(): Int

@Query("SELECT COUNT(*) FROM employees")
suspend fun getEmployeeCount(): Int

@Query("SELECT COUNT(*) FROM locations")
suspend fun getLocationCount(): Int
```

---

### Step 4: Update AuthRepository

```kotlin
// In AuthRepository.kt

@Singleton
class AuthRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val farmRepository: FarmRepository  // NEW: Inject FarmRepository
) {
    suspend fun signIn(email: String, password: String): Result<Boolean> = try {
        auth.signInWithEmailAndPassword(email, password).await()
        
        // NEW: Download data if first login on this device
        if (farmRepository.isLocalDatabaseEmpty()) {
            farmRepository.downloadAllDataFromFirestore()
        }
        
        Result.success(true)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
```

---

### Step 5: Add Loading State to AuthViewModel

```kotlin
// In AuthViewModel.kt

sealed class SyncState {
    object Idle : SyncState()
    object Syncing : SyncState()
    data class Progress(val collection: String, val count: Int) : SyncState()
    object Success : SyncState()
    data class Error(val message: String) : SyncState()
}

private val _syncState = MutableStateFlow<SyncState>(SyncState.Idle)
val syncState: StateFlow<SyncState> = _syncState

fun signIn(email: String, password: String, onSuccess: () -> Unit) {
    viewModelScope.launch {
        _isLoading.value = true
        _error.value = null
        _syncState.value = SyncState.Syncing
        
        when (val result = repository.signIn(email, password)) {
            is Result.Success -> {
                _syncState.value = SyncState.Success
                _isLoading.value = false
                onSuccess()
            }
            is Result.Failure -> {
                _error.value = result.exception.message
                _syncState.value = SyncState.Error(result.exception.message ?: "Unknown error")
                _isLoading.value = false
            }
        }
    }
}
```

---

### Step 6: Update LoginScreen UI

```kotlin
// In LoginScreen.kt

val syncState by viewModel.syncState.collectAsState()

// Show sync dialog
if (syncState is SyncState.Syncing || syncState is SyncState.Progress) {
    AlertDialog(
        onDismissRequest = { },
        title = { Text("Syncing Data") },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
                when (val state = syncState) {
                    is SyncState.Progress -> {
                        Text("Loading ${state.collection}... (${state.count} items)")
                    }
                    else -> {
                        Text("Loading your data...")
                    }
                }
            }
        },
        confirmButton = { }
    )
}
```

---

## Data Flow Diagram

```
┌─────────────────┐
│  User Logs In   │
└────────┬────────┘
         │
         ▼
┌─────────────────────────┐
│ AuthRepository.signIn() │
└────────┬────────────────┘
         │
         ▼
┌──────────────────────────┐
│ Check if DB is empty     │
│ isLocalDatabaseEmpty()   │
└────────┬─────────────────┘
         │
    ┌────┴────┐
    │         │
    ▼         ▼
   YES       NO
    │         │
    │         └──────────────┐
    │                        │
    ▼                        ▼
┌────────────────────┐  ┌──────────────┐
│ Download from      │  │ Use existing │
│ Firestore          │  │ local data   │
└────────┬───────────┘  └──────────────┘
         │
         ▼
┌────────────────────────┐
│ For each collection:   │
│ 1. Fetch from Firestore│
│ 2. Convert to Entity   │
│ 3. Insert into Room    │
└────────┬───────────────┘
         │
         ▼
┌────────────────────┐
│ Navigate to        │
│ Dashboard          │
└────────────────────┘
```

---

## Firestore Structure

```
users/
  {userId}/
    machines/
      {machineId}/
        - id: Int
        - name: String
        - serviceIntervalHours: Int
        - lastServiceReading: Int
    
    fuel_logs/
      {logId}/
        - id: Int
        - machineId: Int
        - date: Long
        - liters: Double
        - rate: Double
        - totalCost: Double
        - hourMeterReading: Int
    
    bills/
      {billId}/
        - id: Int
        - locationId: Int
        - billingMonth: String
        - amount: Double
        - dateAdded: Long
    
    locations/
      {locationId}/
        - id: Int
        - name: String
    
    employees/
      {employeeId}/
        - id: Int
        - name: String
        - baseSalary: Double
    
    transactions/
      {transactionId}/
        - id: Int
        - employeeId: Int
        - amount: Double
        - type: String (DEBIT/CREDIT)
        - timestamp: Long
    
    inventory_items/
      {itemId}/
        - id: Int
        - name: String
        - category: String
        - unit: String
        - currentQuantity: Double
        - reorderLevel: Double
        - dateAdded: Long
    
    stock_transactions/
      {transactionId}/
        - id: Int
        - itemId: Int
        - type: String (IN/OUT)
        - quantity: Double
        - totalCost: Double
        - date: Long
        - locationId: Int (optional)
        - employeeId: Int (optional)
    
    labor_logs/
      {logId}/
        - id: Int
        - employeeId: Int
        - laborCount: Int
        - workType: String
        - totalAmount: Double
        - date: Long
    
    maintenance_logs/
      {logId}/
        - id: Int
        - machineId: Int
        - tag: String
        - cost: Double
        - mechanicName: String
        - description: String
        - date: Long
```

---

## Error Handling

### Scenarios to Handle

1. **No Internet Connection**
   - Show error message
   - Allow offline mode with local data
   - Retry when connection available

2. **Firestore Empty (New User)**
   - Normal flow, start with empty DB
   - No sync needed

3. **Partial Download Failure**
   - Log which collections failed
   - Retry failed collections
   - Don't block user from using app

4. **Data Conflicts**
   - Last write wins (Firestore is source of truth)
   - Clear local DB before download (optional)

---

## Testing Strategy

### Unit Tests
- ✅ Test Firestore to Entity conversion
- ✅ Test empty DB detection
- ✅ Test download methods

### Integration Tests
- ✅ Test full login → download → display flow
- ✅ Test with empty Firestore
- ✅ Test with populated Firestore
- ✅ Test network failures

### Manual Tests
1. Login on new device → verify data appears
2. Login with no internet → verify error handling
3. Login with existing local data → verify no duplicate sync
4. Add data on device A → login on device B → verify data synced

---

## Performance Considerations

### Optimization Strategies

1. **Batch Inserts**
   ```kotlin
   dao.insertMachines(machines) // Insert all at once
   ```

2. **Pagination**
   ```kotlin
   // For large collections, download in batches
   .limit(100)
   .startAfter(lastDoc)
   ```

3. **Background Thread**
   - Already using coroutines
   - WorkManager handles threading

4. **Progress Reporting**
   ```kotlin
   _syncState.value = SyncState.Progress("Machines", machineCount)
   ```

---

## Security Considerations

1. **User Isolation**
   - ✅ Already implemented: `users/{userId}/`
   - Each user can only access their own data

2. **Firestore Rules**
   ```javascript
   rules_version = '2';
   service cloud.firestore {
     match /databases/{database}/documents {
       match /users/{userId}/{document=**} {
         allow read, write: if request.auth != null && request.auth.uid == userId;
       }
     }
   }
   ```

3. **Data Validation**
   - Validate data types before inserting
   - Handle missing fields gracefully

---

## Migration Strategy

### For Existing Users

1. **First Time Setup**
   - Existing users will have empty local DB
   - First login triggers download
   - All their Firestore data loads

2. **Subsequent Logins**
   - Check if DB is empty
   - If not empty, skip download
   - Use existing local data

3. **Force Sync Option**
   - Add "Sync Now" button in settings
   - Clear local DB and re-download

---

## Future Enhancements

### Phase 2 (Optional)

1. **Real-time Sync**
   - Listen to Firestore changes
   - Update local DB in real-time
   - Sync across multiple devices instantly

2. **Conflict Resolution**
   - Detect conflicts (same data modified on 2 devices)
   - Show conflict resolution UI
   - Let user choose which version to keep

3. **Selective Sync**
   - Let user choose which data to sync
   - Save storage space
   - Faster sync

4. **Delta Sync**
   - Only download changed data
   - Use timestamps to detect changes
   - Much faster for large datasets

---

## Summary

### What We're Building

✅ Download all user data from Firestore on login
✅ Insert into local Room database
✅ Show loading progress
✅ Handle errors gracefully
✅ Support offline mode

### Files to Create/Modify

**New Files**:
1. `workers/FirestoreDownloadWorker.kt` (optional, can use repository directly)

**Modified Files**:
1. `data/repository/AuthRepository.kt` - Add post-login sync
2. `data/repository/FarmRepository.kt` - Add download methods
3. `data/local/FarmDao.kt` - Add count queries
4. `ui/auth/AuthViewModel.kt` - Add sync state
5. `ui/auth/LoginScreen.kt` - Add loading UI

### Estimated Effort

- **Implementation**: 4-6 hours
- **Testing**: 2-3 hours
- **Total**: 1 day

### Benefits

✅ Users can access their data on any device
✅ Data persists across app reinstalls
✅ Offline-first architecture
✅ Seamless multi-device experience

---

## Next Steps

1. Review this plan
2. Approve approach
3. Implement Phase 1 (basic download)
4. Test thoroughly
5. Consider Phase 2 enhancements

Ready to proceed with implementation? 🚀
