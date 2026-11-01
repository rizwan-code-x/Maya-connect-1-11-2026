package com.example.data.auth

import java.util.UUID

enum class UserRole {
    STANDARD,
    BRANCH_ADMIN,
    RRR_FAMILY,
    CEO_PANEL,
    SECURITY_ADMIN;

    val displayName: String
        get() = when (this) {
            STANDARD -> "Regular User"
            BRANCH_ADMIN -> "Branch/Department Admin"
            RRR_FAMILY -> "RRR Family"
            CEO_PANEL -> "CEO / Admin"
            SECURITY_ADMIN -> "Developer / Security Administrator"
        }

    val clearanceLevel: Int
        get() = when (this) {
            STANDARD -> 1
            RRR_FAMILY -> 2
            BRANCH_ADMIN -> 3
            CEO_PANEL -> 4
            SECURITY_ADMIN -> 5
        }

    val isPrivilegedAdmin: Boolean
        get() = this == BRANCH_ADMIN || this == CEO_PANEL || this == SECURITY_ADMIN
}

enum class SecurityPermission {
    LAUNCH_STANDARD_APPS,
    LAUNCH_ECOSYSTEM_PRO_APPS,
    VIEW_BRANCH_TICKETS_AND_USERS,
    MANAGE_USERS_AND_ROLES,
    APPROVE_DATA_DELETION,
    MANAGE_APPS_AND_ECOSYSTEM,
    VIEW_SECURITY_MONITORING_CENTER,
    EXECUTE_THREAT_RESPONSE_AND_UNBLOCK,
    PURGE_SECURITY_AUDIT_LOGS
}

data class UserSession(
    val userId: String,
    val userName: String,
    val role: UserRole,
    val termsAcceptedAt: Long,
    val isLoggedIn: Boolean,
    val sessionCorrelationId: String = "corr_" + UUID.randomUUID().toString().replace("-", "").take(12),
    val expiresAt: Long = System.currentTimeMillis() + 86_400_000L * 7, // 7-day rolling session TTL
    val isRevoked: Boolean = false
) {
    fun isExpired(nowMs: Long = System.currentTimeMillis()): Boolean {
        return isRevoked || nowMs > expiresAt
    }
}
