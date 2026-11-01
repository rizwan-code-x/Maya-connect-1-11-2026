package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SupportTicketDao {
    @Query("SELECT * FROM support_tickets ORDER BY updatedAt DESC")
    fun getAllTickets(): Flow<List<SupportTicketEntity>>

    @Query("SELECT * FROM support_tickets WHERE id = :id LIMIT 1")
    suspend fun getTicketById(id: String): SupportTicketEntity?

    @Query("SELECT COUNT(*) FROM support_tickets WHERE status = 'OPEN'")
    fun getOpenTicketsCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTicket(ticket: SupportTicketEntity)

    @Update
    suspend fun updateTicket(ticket: SupportTicketEntity)

    @Query("DELETE FROM support_tickets WHERE id = :id")
    suspend fun deleteTicketById(id: String)
}
