package com.example.data.auth

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.EmergencyMode
import com.example.data.model.FamilyMemberAccount
import com.example.data.security.SecurityHardeningEngine
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

class UserSessionManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "maya_connect_user_session"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USER_ROLE = "user_role"
        private const val KEY_TERMS_ACCEPTED_AT = "terms_accepted_at"
        private const val KEY_SESSION_CORR_ID = "session_correlation_id"
        private const val KEY_SESSION_EXPIRES_AT = "session_expires_at"
        private const val KEY_SESSION_REVOKED = "session_revoked"

        private const val KEY_FAILED_ATTEMPTS = "auth_failed_attempts"
        private const val KEY_LOCKOUT_UNTIL = "auth_lockout_until"

        // Progressive cooldown tiers (avoids permanent 24h DoS lockout on legitimate users)
        const val LOCKOUT_TIER_1_MS = 15_000L        // > 3 attempts: 15s
        const val LOCKOUT_TIER_2_MS = 30_000L        // > 5 attempts: 30s
        const val LOCKOUT_TIER_3_MS = 300_000L       // > 7 attempts: 5m temporary lockout (Admin recoverable)

        const val DEFAULT_SESSION_TTL_MS = 86_400_000L * 7 // 7 days
        const val ADMIN_SESSION_TTL_MS = 3_600_000L * 12   // 12 hours for high-privilege sessions
    }

    fun isLoggedIn(): Boolean {
        val loggedIn = prefs.getBoolean(KEY_IS_LOGGED_IN, false)
        if (!loggedIn) return false
        val revoked = prefs.getBoolean(KEY_SESSION_REVOKED, false)
        val expiresAt = prefs.getLong(KEY_SESSION_EXPIRES_AT, Long.MAX_VALUE)
        if (revoked || System.currentTimeMillis() > expiresAt) {
            logout()
            return false
        }
        return true
    }

    fun getCurrentSession(): UserSession? {
        if (!isLoggedIn()) return null
        val id = prefs.getString(KEY_USER_ID, null) ?: return null
        val name = prefs.getString(KEY_USER_NAME, "User") ?: "User"
        val roleStr = prefs.getString(KEY_USER_ROLE, UserRole.STANDARD.name) ?: UserRole.STANDARD.name
        val role = try {
            UserRole.valueOf(roleStr)
        } catch (_: Exception) {
            UserRole.STANDARD
        }
        val termsAt = prefs.getLong(KEY_TERMS_ACCEPTED_AT, System.currentTimeMillis())
        val corrId = prefs.getString(KEY_SESSION_CORR_ID, null)
            ?: ("corr_" + UUID.randomUUID().toString().replace("-", "").take(12))
        val expiresAt = prefs.getLong(KEY_SESSION_EXPIRES_AT, System.currentTimeMillis() + DEFAULT_SESSION_TTL_MS)
        val isRevoked = prefs.getBoolean(KEY_SESSION_REVOKED, false)

        return UserSession(
            userId = id,
            userName = name,
            role = role,
            termsAcceptedAt = termsAt,
            isLoggedIn = !isRevoked,
            sessionCorrelationId = corrId,
            expiresAt = expiresAt,
            isRevoked = isRevoked
        )
    }

    fun loginStandard(name: String): UserSession {
        val cleanName = SecurityHardeningEngine.sanitizeText(name).ifBlank { "User" }.take(80)
        val newUserId = "usr_" + UUID.randomUUID().toString().replace("-", "").take(12)
        return createAndSaveSession(newUserId, cleanName, UserRole.STANDARD, DEFAULT_SESSION_TTL_MS)
    }

    fun loginFamily(name: String = "RRR Family Member"): UserSession {
        val cleanName = SecurityHardeningEngine.sanitizeText(name).ifBlank { "RRR Family Member" }.take(80)
        val familyUserId = "rrr_family_" + UUID.randomUUID().toString().replace("-", "").take(8)
        return createAndSaveSession(familyUserId, cleanName, UserRole.RRR_FAMILY, DEFAULT_SESSION_TTL_MS)
    }

    fun loginCeo(name: String = "Rizwan RR"): UserSession {
        val cleanName = SecurityHardeningEngine.sanitizeText(name).ifBlank { "Rizwan RR" }.take(80)
        val ceoUserId = "ceo_panel_" + UUID.randomUUID().toString().replace("-", "").take(8)
        return createAndSaveSession(ceoUserId, cleanName, UserRole.CEO_PANEL, ADMIN_SESSION_TTL_MS)
    }

    fun loginWithRole(name: String, role: UserRole): UserSession {
        val cleanName = SecurityHardeningEngine.sanitizeText(name).ifBlank { role.displayName }.take(80)
        val prefix = when (role) {
            UserRole.STANDARD -> "usr_"
            UserRole.BRANCH_ADMIN -> "branch_adm_"
            UserRole.RRR_FAMILY -> "rrr_family_"
            UserRole.CEO_PANEL -> "ceo_panel_"
            UserRole.SECURITY_ADMIN -> "sec_admin_"
        }
        val uid = prefix + UUID.randomUUID().toString().replace("-", "").take(8)
        val ttl = if (role.isPrivilegedAdmin) ADMIN_SESSION_TTL_MS else DEFAULT_SESSION_TTL_MS
        return createAndSaveSession(uid, cleanName, role, ttl)
    }

    private fun createAndSaveSession(
        userId: String,
        userName: String,
        role: UserRole,
        ttlMs: Long
    ): UserSession {
        val now = System.currentTimeMillis()
        val corrId = "corr_" + UUID.randomUUID().toString().replace("-", "").take(12)
        val expiresAt = now + ttlMs

        prefs.edit()
            .putBoolean(KEY_IS_LOGGED_IN, true)
            .putString(KEY_USER_ID, userId)
            .putString(KEY_USER_NAME, userName)
            .putString(KEY_USER_ROLE, role.name)
            .putLong(KEY_TERMS_ACCEPTED_AT, now)
            .putString(KEY_SESSION_CORR_ID, corrId)
            .putLong(KEY_SESSION_EXPIRES_AT, expiresAt)
            .putBoolean(KEY_SESSION_REVOKED, false)
            .commit()

        resetFailedAttempts()

        return UserSession(
            userId = userId,
            userName = userName,
            role = role,
            termsAcceptedAt = now,
            isLoggedIn = true,
            sessionCorrelationId = corrId,
            expiresAt = expiresAt,
            isRevoked = false
        )
    }

    fun updateUserName(newName: String) {
        val sanitized = SecurityHardeningEngine.sanitizeText(newName).take(80)
        prefs.edit().putString(KEY_USER_NAME, sanitized).commit()
    }

    fun revokeCurrentSession() {
        prefs.edit()
            .putBoolean(KEY_SESSION_REVOKED, true)
            .putBoolean(KEY_IS_LOGGED_IN, false)
            .commit()
    }

    fun logout() {
        prefs.edit()
            .putBoolean(KEY_IS_LOGGED_IN, false)
            .putBoolean(KEY_SESSION_REVOKED, true)
            .remove(KEY_USER_ID)
            .remove(KEY_USER_NAME)
            .remove(KEY_USER_ROLE)
            .remove(KEY_TERMS_ACCEPTED_AT)
            .remove(KEY_SESSION_CORR_ID)
            .remove(KEY_SESSION_EXPIRES_AT)
            .commit()
    }

    // --- Lockout & Failed Attempts Management ---

    fun getFailedAttempts(): Int {
        return prefs.getInt(KEY_FAILED_ATTEMPTS, 0)
    }

    fun getRemainingLockoutMillis(): Long {
        val lockoutUntil = prefs.getLong(KEY_LOCKOUT_UNTIL, 0L)
        val diff = lockoutUntil - System.currentTimeMillis()
        return if (diff > 0) diff else 0L
    }

    fun recordFailedAttempt(): Long {
        val newAttempts = getFailedAttempts() + 1
        var lockoutDuration = 0L

        if (newAttempts > 7) {
            lockoutDuration = LOCKOUT_TIER_3_MS
        } else if (newAttempts > 5) {
            lockoutDuration = LOCKOUT_TIER_2_MS
        } else if (newAttempts > 3) {
            lockoutDuration = LOCKOUT_TIER_1_MS
        }

        val lockoutUntil = if (lockoutDuration > 0) {
            System.currentTimeMillis() + lockoutDuration
        } else {
            0L
        }

        prefs.edit()
            .putInt(KEY_FAILED_ATTEMPTS, newAttempts)
            .putLong(KEY_LOCKOUT_UNTIL, lockoutUntil)
            .apply()

        return lockoutDuration
    }

    fun resetFailedAttempts() {
        prefs.edit()
            .putInt(KEY_FAILED_ATTEMPTS, 0)
            .putLong(KEY_LOCKOUT_UNTIL, 0L)
            .apply()
        SecurityHardeningEngine.unlockAllRateLimits()
    }

    // --- Persistent User Preferences & Real-Time Settings ---

    fun getSavedClockStyle(): String {
        return prefs.getString("user_clock_style", "ANALOGUE") ?: "ANALOGUE"
    }

    fun saveClockStyle(styleName: String) {
        prefs.edit().putString("user_clock_style", styleName).apply()
    }

    fun getSavedThemeMode(): String {
        return prefs.getString("user_theme_mode", "DARK") ?: "DARK"
    }

    fun saveThemeMode(modeName: String) {
        prefs.edit().putString("user_theme_mode", modeName).apply()
    }

    fun getSavedHomeWidgets(): List<String> {
        val str = prefs.getString("user_home_widgets", "WEATHER,CALENDAR,BATTERY_STATUS")
            ?: "WEATHER,CALENDAR,BATTERY_STATUS"
        return str.split(",").filter { it.isNotBlank() }
    }

    fun saveHomeWidgets(widgets: List<String>) {
        prefs.edit().putString("user_home_widgets", widgets.joinToString(",")).apply()
    }

    fun getSavedMayaOsWallpaper(): String {
        return prefs.getString("user_maya_os_wallpaper", "dynamic_abstract") ?: "dynamic_abstract"
    }

    fun saveMayaOsWallpaper(wallpaper: String) {
        prefs.edit().putString("user_maya_os_wallpaper", wallpaper).apply()
    }

    fun getSavedHapticEnabled(): Boolean {
        return prefs.getBoolean("user_haptic_enabled", true)
    }

    fun saveHapticEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("user_haptic_enabled", enabled).apply()
    }

    fun getSavedReduceMotion(): Boolean {
        return prefs.getBoolean("user_reduce_motion", false)
    }

    fun saveReduceMotion(enabled: Boolean) {
        prefs.edit().putBoolean("user_reduce_motion", enabled).apply()
    }

    fun getSavedNotificationsEnabled(): Boolean {
        return prefs.getBoolean("user_notifications_enabled", true)
    }

    fun saveNotificationsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("user_notifications_enabled", enabled).apply()
    }

    // --- Emergency Situation Persistence (Admin Rizwan RR Controls) ---
    fun getEmergencyMode(): EmergencyMode {
        val name = prefs.getString("emergency_active_mode", EmergencyMode.NORMAL.name)
        return try {
            EmergencyMode.valueOf(name ?: EmergencyMode.NORMAL.name)
        } catch (_: Exception) {
            EmergencyMode.NORMAL
        }
    }

    fun saveEmergencyMode(mode: EmergencyMode) {
        prefs.edit().putString("emergency_active_mode", mode.name).apply()
    }

    fun getMaintenanceReason(): String {
        return prefs.getString(
            "emergency_maintenance_reason",
            "Scheduled core infrastructure and security updates in progress."
        ) ?: "Scheduled core infrastructure and security updates in progress."
    }

    fun saveMaintenanceReason(reason: String) {
        prefs.edit().putString("emergency_maintenance_reason", reason).apply()
    }

    fun getCustomDurationHours(): Int {
        return prefs.getInt("emergency_custom_duration_hours", 4).coerceAtLeast(1)
    }

    fun saveCustomDurationHours(hours: Int) {
        val safeHours = hours.coerceIn(1, 720)
        val expiresAt = System.currentTimeMillis() + (safeHours * 3_600_000L)
        prefs.edit()
            .putInt("emergency_custom_duration_hours", safeHours)
            .putLong("emergency_expires_at", expiresAt)
            .apply()
    }

    fun getEmergencyExpiresAt(): Long {
        return prefs.getLong("emergency_expires_at", 0L)
    }

    fun getDisabledFeatureIds(): Set<String> {
        return prefs.getStringSet("emergency_disabled_features", emptySet()) ?: emptySet()
    }

    fun saveDisabledFeatureIds(ids: Set<String>) {
        prefs.edit().putStringSet("emergency_disabled_features", ids).apply()
    }

    // --- Family Member Credentials Persistence (Synced with MaintenanceService Realtime Database) ---
    fun getFamilyMembers(): List<FamilyMemberAccount> {
        val raw = prefs.getString("rrr_family_accounts_json", null)
        if (raw.isNullOrBlank()) {
            val defaults = defaultFamilyMembers()
            saveFamilyMembers(defaults)
            return defaults
        }
        return try {
            val arr = JSONArray(raw)
            val list = mutableListOf<FamilyMemberAccount>()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(
                    FamilyMemberAccount(
                        id = obj.optString("id", ""),
                        password = obj.optString("password", ""),
                        memberName = obj.optString("memberName", "RRR Family Member"),
                        details = obj.optString("details", "Verified RRR Family Member"),
                        createdAt = obj.optLong("createdAt", System.currentTimeMillis()),
                        lastLoginAt = obj.optLong("lastLoginAt", 0L),
                        loginCount = obj.optInt("loginCount", 0),
                        isActive = obj.optBoolean("isActive", true)
                    )
                )
            }
            if (list.none { it.id.equals("admin", ignoreCase = true) }) {
                list.add(0, defaultFamilyMembers().first())
            }
            list
        } catch (_: Exception) {
            defaultFamilyMembers()
        }
    }

    fun saveFamilyMembers(members: List<FamilyMemberAccount>) {
        try {
            val arr = JSONArray()
            members.forEach { member ->
                val obj = JSONObject().apply {
                    put("id", member.id)
                    put("password", member.password)
                    put("memberName", member.memberName)
                    put("details", member.details)
                    put("createdAt", member.createdAt)
                    put("lastLoginAt", member.lastLoginAt)
                    put("loginCount", member.loginCount)
                    put("isActive", member.isActive)
                }
                arr.put(obj)
            }
            prefs.edit().putString("rrr_family_accounts_json", arr.toString()).commit()
        } catch (_: Exception) {}
    }

    private fun defaultFamilyMembers(): List<FamilyMemberAccount> {
        val now = System.currentTimeMillis()
        return listOf(
            FamilyMemberAccount(
                id = "admin",
                password = "Rexlify",
                memberName = "RRR Family Master",
                details = "Primary Official RRR Family Access Account",
                createdAt = now - 86_400_000L * 30,
                lastLoginAt = now - 3_600_000L,
                loginCount = 12,
                isActive = true
            ),
            FamilyMemberAccount(
                id = "arjun_rrr",
                password = "Rexlify",
                memberName = "Arjun RRR",
                details = "Core Family Member • Verified Access",
                createdAt = now - 86_400_000L * 14,
                lastLoginAt = now - 7_200_000L,
                loginCount = 5,
                isActive = true
            ),
            FamilyMemberAccount(
                id = "priya_rrr",
                password = "Rexlify",
                memberName = "Priya Sen",
                details = "Family Member • Ecosystem Access",
                createdAt = now - 86_400_000L * 10,
                lastLoginAt = now - 18_000_000L,
                loginCount = 3,
                isActive = true
            )
        )
    }
}
