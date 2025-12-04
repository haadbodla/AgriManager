package com.example.agrimanager.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity): Long

    @Query("SELECT * FROM transactions WHERE employeeId = :employeeId ORDER BY timestamp DESC")
    fun getTransactionsForEmployee(employeeId: Int): Flow<List<TransactionEntity>>

    @Query("SELECT SUM(CASE WHEN type = 'DEBIT' THEN amount ELSE 0 END) FROM transactions WHERE employeeId = :employeeId")
    fun getTotalAdvances(employeeId: Int): Flow<Double?>
}
