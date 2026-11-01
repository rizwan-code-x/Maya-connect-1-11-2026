package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "registered_users")
data class RegisteredUserEntity(
    @PrimaryKey val userId: String,
    val name: String,
    val role: String, // "STANDARD", "RRR_FAMILY", "CEO_PANEL"
    val registeredAt: Long,
    val status: String = "ACTIVE", // "ACTIVE", "WARNING", "SUSPENDED"
    val warningCount: Int = 0,
    val lastActiveAt: Long = System.currentTimeMillis()
)
