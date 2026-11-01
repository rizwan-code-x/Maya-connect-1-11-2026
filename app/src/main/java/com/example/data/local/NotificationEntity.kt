package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val message: String,
    val type: String, // "SUPPORT_REPLY", "ANNOUNCEMENT", "ECOSYSTEM_UPDATE", "SYSTEM"
    val relatedTicketId: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)
