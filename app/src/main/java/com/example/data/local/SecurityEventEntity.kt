package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "security_events")
data class SecurityEventEntity(
    @PrimaryKey
    val eventId: String,
    val timestampUtc: String,
    val timestampEpochMs: Long,
    val category: String,
    val severity: String, // LOW, MEDIUM, HIGH, CRITICAL
    val endpointOrModule: String,
    val authenticatedUserId: String, // Or "Unknown / Unauthenticated"
    val userRole: String,
    val sessionCorrelationId: String,
    val sourceIp: String,
    val approxLocation: String, // Approximate IP-derived city/region/country with VPN/Proxy disclaimer
    val deviceInfo: String,
    val detectionReason: String,
    val errorCode: String,
    val mitigationAction: String,
    val status: String, // Detected, Investigating, Mitigated, Resolved, False Positive
    val investigationNotes: String = "",
    val adminActionHistory: String = "",
    val isSourceBlocked: Boolean = false
)
