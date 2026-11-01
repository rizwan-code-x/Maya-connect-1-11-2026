package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DataDeletionRequestDao {
    @Query("SELECT * FROM data_deletion_requests ORDER BY requestDate DESC")
    fun getAllRequests(): Flow<List<DataDeletionRequestEntity>>

    @Query("SELECT * FROM data_deletion_requests WHERE userId = :userId")
    suspend fun getRequestsByUserId(userId: String): List<DataDeletionRequestEntity>

    @Query("SELECT * FROM data_deletion_requests WHERE id = :requestId")
    suspend fun getRequestById(requestId: String): DataDeletionRequestEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRequest(request: DataDeletionRequestEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRequests(requests: List<DataDeletionRequestEntity>)

    @Query("UPDATE data_deletion_requests SET status = :status, processedDate = :processedDate, rejectionReason = :rejectionReason WHERE id = :requestId")
    suspend fun updateRequestStatus(requestId: String, status: String, processedDate: Long, rejectionReason: String)

    @Query("DELETE FROM data_deletion_requests WHERE id = :requestId")
    suspend fun deleteRequest(requestId: String)

    @Query("DELETE FROM data_deletion_requests")
    suspend fun clearAllRequests()
}
