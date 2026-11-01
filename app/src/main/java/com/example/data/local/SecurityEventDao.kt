package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SecurityEventDao {

    @Query("SELECT * FROM security_events ORDER BY timestampEpochMs DESC")
    fun getAllSecurityEvents(): Flow<List<SecurityEventEntity>>

    @Query("SELECT * FROM security_events WHERE eventId = :eventId LIMIT 1")
    suspend fun getEventById(eventId: String): SecurityEventEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: SecurityEventEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvents(events: List<SecurityEventEntity>)

    @Query(
        """
        UPDATE security_events 
        SET status = :newStatus, 
            investigationNotes = :notes, 
            adminActionHistory = :history,
            isSourceBlocked = :isBlocked
        WHERE eventId = :eventId
        """
    )
    suspend fun updateEventIncidentState(
        eventId: String,
        newStatus: String,
        notes: String,
        history: String,
        isBlocked: Boolean
    )

    @Query("UPDATE security_events SET isSourceBlocked = 0, status = 'Resolved', adminActionHistory = adminActionHistory || :recoveryNote WHERE isSourceBlocked = 1")
    suspend fun unblockAllBlockedSources(recoveryNote: String)

    @Query("DELETE FROM security_events WHERE status IN ('Resolved', 'False Positive') AND timestampEpochMs < :olderThanEpochMs")
    suspend fun purgeResolvedEventsOlderThan(olderThanEpochMs: Long)

    @Query("SELECT COUNT(*) FROM security_events")
    suspend fun getEventsCount(): Int
}
