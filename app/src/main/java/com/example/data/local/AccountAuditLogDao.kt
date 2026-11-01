package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountAuditLogDao {
    @Query("SELECT * FROM account_audit_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<AccountAuditLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: AccountAuditLogEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLogs(logs: List<AccountAuditLogEntity>)

    @Query("DELETE FROM account_audit_logs WHERE id = :id")
    suspend fun deleteLogById(id: String)

    @Query("DELETE FROM account_audit_logs")
    suspend fun clearAllLogs()
}
