package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "account_audit_logs")
data class AccountAuditLogEntity(
    @PrimaryKey val id: String,
    val adminName: String = "Rizwan RR",
    val targetUserId: String,
    val targetUserName: String,
    val action: String, // "SUSPEND", "HOLD", "DEACTIVATE", "REACTIVATE", "WARN", "DELETE_APPROVED", "DELETE_REJECTED"
    val reason: String,
    val timestamp: Long = System.currentTimeMillis()
)
