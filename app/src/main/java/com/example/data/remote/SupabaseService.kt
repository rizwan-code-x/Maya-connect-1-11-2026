package com.example.data.remote

import android.util.Log
import com.example.BuildConfig
import com.example.data.local.SecurityEventEntity
import com.example.data.security.SecurityHardeningEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

object SupabaseService {
    private const val TAG = "SupabaseService"
    private const val CONNECT_TIMEOUT_MS = 8_000
    private const val READ_TIMEOUT_MS = 8_000

    val SUPABASE_URL: String
        get() = try {
            BuildConfig.SUPABASE_URL.ifBlank { "https://cshlocgnefkhglexodmo.supabase.co" }
        } catch (_: Exception) {
            "https://cshlocgnefkhglexodmo.supabase.co"
        }

    private val SUPABASE_ANON_KEY: String
        get() = try {
            BuildConfig.SUPABASE_ANON_KEY
        } catch (_: Exception) {
            ""
        }

    private fun isConfigured(): Boolean {
        val key = SUPABASE_ANON_KEY
        return key.isNotBlank() &&
                key != "SUPABASE_PUBLIC_ANON_KEY_PLACEHOLDER" &&
                !key.endsWith(".placeholder") &&
                SUPABASE_URL.startsWith("https://")
    }

    private fun openSecurePostConnection(endpointPath: String, mergeDuplicates: Boolean = false): HttpURLConnection {
        val fullUrl = "$SUPABASE_URL$endpointPath"
        require(fullUrl.startsWith("https://")) { "SEC_ERR_CLEARTEXT_BLOCKED: Supabase endpoint must use HTTPS" }
        val url = URL(fullUrl)
        return (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = CONNECT_TIMEOUT_MS
            readTimeout = READ_TIMEOUT_MS
            useCaches = false
            setRequestProperty("Content-Type", "application/json; charset=utf-8")
            setRequestProperty("apikey", SUPABASE_ANON_KEY)
            setRequestProperty("Authorization", "Bearer $SUPABASE_ANON_KEY")
            if (mergeDuplicates) {
                setRequestProperty("Prefer", "resolution=merge-duplicates")
            }
            doOutput = true
        }
    }

    suspend fun syncUserToSupabase(
        userId: String,
        name: String,
        role: String,
        status: String,
        warningCount: Int = 0
    ): Boolean = withContext(Dispatchers.IO) {
        if (!isConfigured()) return@withContext false
        return@withContext try {
            val conn = openSecurePostConnection("/rest/v1/registered_users", mergeDuplicates = true)
            val safeName = SecurityHardeningEngine.sanitizeText(name).take(120)
            val safeRole = role.take(64)
            val safeStatus = status.take(32)

            val json = JSONObject().apply {
                put("user_id", userId.take(64))
                put("user_name", safeName)
                put("login_type", safeRole)
                put("role", safeRole)
                put("account_status", safeStatus)
                put("warnings_count", warningCount.coerceIn(0, 100))
            }

            conn.outputStream.use { os ->
                os.write(json.toString().toByteArray(Charsets.UTF_8))
            }

            val code = conn.responseCode
            conn.disconnect()
            code in 200..299
        } catch (e: Exception) {
            Log.w(TAG, "Supabase user sync skipped/failed: ${e.javaClass.simpleName}")
            false
        }
    }

    suspend fun logAuditToSupabase(
        logId: String,
        adminName: String,
        targetUserId: String,
        targetUserName: String,
        action: String,
        reason: String
    ): Boolean = withContext(Dispatchers.IO) {
        if (!isConfigured()) return@withContext false
        return@withContext try {
            val conn = openSecurePostConnection("/rest/v1/account_audit_logs")
            val json = JSONObject().apply {
                put("id", logId.take(64))
                put("admin_name", SecurityHardeningEngine.sanitizeText(adminName).take(120))
                put("admin_role", "CEO_PANEL")
                put("target_user_id", targetUserId.take(64))
                put("target_user_name", SecurityHardeningEngine.sanitizeText(targetUserName).take(120))
                put("action", action.take(64))
                put("reason", SecurityHardeningEngine.sanitizeText(reason).take(1000))
                put("timestamp", System.currentTimeMillis())
            }

            conn.outputStream.use { os ->
                os.write(json.toString().toByteArray(Charsets.UTF_8))
            }

            val code = conn.responseCode
            conn.disconnect()
            code in 200..299
        } catch (e: Exception) {
            Log.w(TAG, "Supabase audit sync skipped/failed: ${e.javaClass.simpleName}")
            false
        }
    }

    suspend fun syncDeletionRequestToSupabase(
        requestId: String,
        userId: String,
        userName: String,
        status: String,
        reason: String
    ): Boolean = withContext(Dispatchers.IO) {
        if (!isConfigured()) return@withContext false
        return@withContext try {
            val conn = openSecurePostConnection("/rest/v1/data_deletion_requests", mergeDuplicates = true)
            val json = JSONObject().apply {
                put("request_id", requestId.take(64))
                put("user_id", userId.take(64))
                put("user_name", SecurityHardeningEngine.sanitizeText(userName).take(120))
                put("status", status.take(32))
                put("reason", SecurityHardeningEngine.sanitizeText(reason).take(1000))
                put("request_date", System.currentTimeMillis())
            }

            conn.outputStream.use { os ->
                os.write(json.toString().toByteArray(Charsets.UTF_8))
            }

            val code = conn.responseCode
            conn.disconnect()
            code in 200..299
        } catch (e: Exception) {
            Log.w(TAG, "Supabase deletion sync skipped/failed: ${e.javaClass.simpleName}")
            false
        }
    }

    suspend fun syncSecurityEventToSupabase(event: SecurityEventEntity): Boolean = withContext(Dispatchers.IO) {
        if (!isConfigured()) return@withContext false
        return@withContext try {
            val conn = openSecurePostConnection("/rest/v1/security_events", mergeDuplicates = true)
            val json = JSONObject().apply {
                put("event_id", event.eventId.take(64))
                put("timestamp_utc", event.timestampUtc)
                put("timestamp_epoch_ms", event.timestampEpochMs)
                put("category", event.category.take(64))
                put("severity", event.severity.take(16))
                put("endpoint_or_module", event.endpointOrModule.take(255))
                put("authenticated_user_id", event.authenticatedUserId.take(120))
                put("user_role", event.userRole.take(64))
                put("session_correlation_id", event.sessionCorrelationId.take(120))
                put("source_ip", event.sourceIp.take(120))
                put("approx_location", event.approxLocation.take(255))
                put("device_info", event.deviceInfo.take(255))
                put("detection_reason", event.detectionReason.take(1000))
                put("error_code", event.errorCode.take(64))
                put("mitigation_action", event.mitigationAction.take(500))
                put("status", event.status.take(32))
                put("investigation_notes", event.investigationNotes.take(2000))
                put("admin_action_history", event.adminActionHistory.take(4000))
                put("is_source_blocked", event.isSourceBlocked)
            }

            conn.outputStream.use { os ->
                os.write(json.toString().toByteArray(Charsets.UTF_8))
            }

            val code = conn.responseCode
            conn.disconnect()
            code in 200..299
        } catch (e: Exception) {
            Log.w(TAG, "Supabase security event sync skipped/failed: ${e.javaClass.simpleName}")
            false
        }
    }
}
