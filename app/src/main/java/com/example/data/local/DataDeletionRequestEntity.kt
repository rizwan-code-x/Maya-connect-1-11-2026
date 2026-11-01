package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "data_deletion_requests")
data class DataDeletionRequestEntity(
    @PrimaryKey val id: String, // e.g. "DEL-1029"
    val userId: String,
    val userName: String,
    val requestDate: Long = System.currentTimeMillis(),
    val status: String = "PENDING", // "PENDING", "APPROVED", "REJECTED", "COMPLETED"
    val reason: String = "",
    val processedDate: Long = 0L,
    val rejectionReason: String = ""
)
