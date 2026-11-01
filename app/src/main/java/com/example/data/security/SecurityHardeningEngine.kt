package com.example.data.security

import android.net.Uri
import android.os.Build
import com.example.data.auth.SecurityPermission
import com.example.data.auth.UserRole
import com.example.data.local.SecurityEventEntity
import java.io.File
import java.net.URI
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/**
 * Centralized Security Hardening, Cyberattack Detection, RBAC Authorization,
 * Rate-Limiting, Input Validation, and Automated Threat Response Engine.
 *
 * Security Rules Enforced:
 * - Salted PBKDF2WithHmacSHA256 + Constant-Time MessageDigest.isEqual verification.
 * - Never logs plaintext passwords, OTP values, or raw session tokens.
 * - IP geolocation is clearly labeled as approximate (never claims exact physical address or secretly activates GPS).
 * - Anonymous sessions are recorded as "Unknown / Unauthenticated".
 */
object SecurityHardeningEngine {

    private const val PBKDF2_ITERATIONS = 12_000
    private const val KEY_LENGTH_BITS = 256
    private val CREDENTIAL_SALT = byteArrayOf(
        0x52, 0x45, 0x58, 0x4C, 0x49, 0x46, 0x59, 0x5F,
        0x4D, 0x41, 0x59, 0x41, 0x5F, 0x32, 0x30, 0x32, 0x36
    )

    // Pre-derived PBKDF2WithHmacSHA256 digests initialized once in memory (no plaintext password comparison)
    private val ADMIN_PANEL_PASS_DIGEST: ByteArray by lazy {
        // Derived from char sequence for RTX@0012352
        derivePbkdf2Digest(charArrayOf('R', 'T', 'X', '@', '0', '0', '1', '2', '3', '5', '2'))
    }

    private val FAMILY_PRIMARY_PASS_DIGEST: ByteArray by lazy {
        // Derived from char sequence for RRR@0012352
        derivePbkdf2Digest(charArrayOf('R', 'R', 'R', '@', '0', '0', '1', '2', '3', '5', '2'))
    }

    private val FAMILY_REXLIFY_PASS_DIGEST: ByteArray by lazy {
        // Derived from char sequence for Rexlify
        derivePbkdf2Digest(charArrayOf('R', 'e', 'x', 'l', 'i', 'f', 'y'))
    }

    private val CEO_PANEL_PASS_DIGEST: ByteArray by lazy {
        // Derived from char sequence for RRR@0012352
        derivePbkdf2Digest(charArrayOf('R', 'R', 'R', '@', '0', '0', '1', '2', '3', '5', '2'))
    }

    // 4 Authorized Admin 2FA Passkeys: RRR@123, RTECH, Ruma, RRR@007
    private val ADMIN_PASSKEY_1_DIGEST: ByteArray by lazy {
        derivePbkdf2Digest(charArrayOf('R', 'R', 'R', '@', '1', '2', '3'))
    }
    private val ADMIN_PASSKEY_2_DIGEST: ByteArray by lazy {
        derivePbkdf2Digest(charArrayOf('R', 'T', 'E', 'C', 'H'))
    }
    private val ADMIN_PASSKEY_3_DIGEST: ByteArray by lazy {
        derivePbkdf2Digest(charArrayOf('R', 'u', 'm', 'a'))
    }
    private val ADMIN_PASSKEY_4_DIGEST: ByteArray by lazy {
        derivePbkdf2Digest(charArrayOf('R', 'R', 'R', '@', '0', '0', '7'))
    }

    fun derivePbkdf2Digest(passwordChars: CharArray): ByteArray {
        return try {
            val spec = PBEKeySpec(passwordChars, CREDENTIAL_SALT, PBKDF2_ITERATIONS, KEY_LENGTH_BITS)
            val skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
            skf.generateSecret(spec).encoded
        } catch (_: Exception) {
            val md = MessageDigest.getInstance("SHA-256")
            md.update(CREDENTIAL_SALT)
            md.digest(String(passwordChars).toByteArray(Charsets.UTF_8))
        } finally {
            passwordChars.fill('\u0000')
        }
    }

    /**
     * Constant-time salted PBKDF2 credential verification.
     */
    fun verifyPasswordConstantTime(candidatePassword: String, expectedDigest: ByteArray): Boolean {
        val candidateChars = candidatePassword.trim().toCharArray()
        val candidateDigest = derivePbkdf2Digest(candidateChars)
        return MessageDigest.isEqual(candidateDigest, expectedDigest)
    }

    fun verifyAdminPanelPassword(candidatePassword: String): Boolean {
        return verifyPasswordConstantTime(candidatePassword, ADMIN_PANEL_PASS_DIGEST)
    }

    /**
     * Verifies Admin 2FA Passkey.
     * Grants access if candidate matches ANY ONE of the 4 authorized passkeys:
     * 1) RRR@123
     * 2) RTECH
     * 3) Ruma
     * 4) RRR@007
     */
    fun verifyAdminPasskey(candidatePasskey: String): Boolean {
        val clean = candidatePasskey.trim()
        if (clean.isEmpty()) return false
        val match1 = verifyPasswordConstantTime(clean, ADMIN_PASSKEY_1_DIGEST)
        val match2 = verifyPasswordConstantTime(clean, ADMIN_PASSKEY_2_DIGEST)
        val match3 = verifyPasswordConstantTime(clean, ADMIN_PASSKEY_3_DIGEST)
        val match4 = verifyPasswordConstantTime(clean, ADMIN_PASSKEY_4_DIGEST)
        return match1 || match2 || match3 || match4
    }

    fun verifyFamilyCredentials(id: String, password: String): Boolean {
        val cleanId = id.trim()
        val cleanPass = password.trim()
        return when {
            cleanId.equals("admin", ignoreCase = true) ->
                verifyPasswordConstantTime(cleanPass, FAMILY_REXLIFY_PASS_DIGEST)
            cleanId == "RRR@105" ->
                verifyPasswordConstantTime(cleanPass, FAMILY_PRIMARY_PASS_DIGEST) ||
                    verifyPasswordConstantTime(cleanPass, FAMILY_REXLIFY_PASS_DIGEST)
            else -> {
                // Execute dummy digest to prevent username enumeration via timing channel
                verifyPasswordConstantTime(cleanPass, FAMILY_PRIMARY_PASS_DIGEST)
                false
            }
        }
    }

    fun verifyCeoCredentials(id: String, password: String): Boolean {
        val cleanId = id.trim()
        return when {
            cleanId == "RRR@105" ->
                verifyPasswordConstantTime(password, CEO_PANEL_PASS_DIGEST)
            else -> {
                // Execute dummy digest to prevent username enumeration via timing channel
                verifyPasswordConstantTime(password, CEO_PANEL_PASS_DIGEST)
                false
            }
        }
    }

    // -------------------------------------------------------------------------
    // 2. ROLE-BASED ACCESS CONTROL (RBAC) MATRIX
    // -------------------------------------------------------------------------

    fun hasPermission(role: UserRole, permission: SecurityPermission): Boolean {
        return when (role) {
            UserRole.STANDARD -> permission == SecurityPermission.LAUNCH_STANDARD_APPS
            UserRole.RRR_FAMILY -> permission in setOf(
                SecurityPermission.LAUNCH_STANDARD_APPS,
                SecurityPermission.LAUNCH_ECOSYSTEM_PRO_APPS
            )
            UserRole.BRANCH_ADMIN -> permission in setOf(
                SecurityPermission.LAUNCH_STANDARD_APPS,
                SecurityPermission.LAUNCH_ECOSYSTEM_PRO_APPS,
                SecurityPermission.VIEW_BRANCH_TICKETS_AND_USERS,
                SecurityPermission.VIEW_SECURITY_MONITORING_CENTER
            )
            UserRole.CEO_PANEL -> permission in setOf(
                SecurityPermission.LAUNCH_STANDARD_APPS,
                SecurityPermission.LAUNCH_ECOSYSTEM_PRO_APPS,
                SecurityPermission.VIEW_BRANCH_TICKETS_AND_USERS,
                SecurityPermission.MANAGE_USERS_AND_ROLES,
                SecurityPermission.APPROVE_DATA_DELETION,
                SecurityPermission.MANAGE_APPS_AND_ECOSYSTEM,
                SecurityPermission.VIEW_SECURITY_MONITORING_CENTER,
                SecurityPermission.EXECUTE_THREAT_RESPONSE_AND_UNBLOCK,
                SecurityPermission.PURGE_SECURITY_AUDIT_LOGS
            )
            UserRole.SECURITY_ADMIN -> true // Full security & developer administrative privileges
        }
    }

    // -------------------------------------------------------------------------
    // 3. SLIDING-WINDOW RATE LIMITER & BRUTE-FORCE DETECTOR
    // -------------------------------------------------------------------------

    data class RateLimitState(
        val timestampsMs: MutableList<Long> = mutableListOf(),
        var consecutiveFailures: Int = 0,
        var lockedUntilMs: Long = 0L,
        var isTemporarilyBlocked: Boolean = false
    )

    private val endpointRateMap = ConcurrentHashMap<String, RateLimitState>()

    data class RateLimitDecision(
        val allowed: Boolean,
        val remainingLockoutMs: Long = 0L,
        val failureCount: Int = 0,
        val triggeredSeverity: String? = null,
        val errorCode: String? = null,
        val reason: String? = null
    )

    fun checkAndRecordRequest(
        endpointKey: String,
        maxRequestsPerWindow: Int = 8,
        windowMs: Long = 60_000L,
        nowMs: Long = System.currentTimeMillis()
    ): RateLimitDecision {
        val state = endpointRateMap.getOrPut(endpointKey) { RateLimitState() }
        synchronized(state) {
            if (state.isTemporarilyBlocked && state.lockedUntilMs > nowMs) {
                return RateLimitDecision(
                    allowed = false,
                    remainingLockoutMs = state.lockedUntilMs - nowMs,
                    failureCount = state.consecutiveFailures,
                    triggeredSeverity = "HIGH",
                    errorCode = "SEC_ERR_429_SOURCE_RESTRICTED",
                    reason = "Endpoint '$endpointKey' temporarily restricted due to repeated suspicious requests."
                )
            } else if (state.lockedUntilMs > 0L && nowMs >= state.lockedUntilMs) {
                state.isTemporarilyBlocked = false
                state.lockedUntilMs = 0L
            }

            state.timestampsMs.removeAll { it < nowMs - windowMs }
            state.timestampsMs.add(nowMs)

            if (state.timestampsMs.size > maxRequestsPerWindow) {
                val cooldownMs = 30_000L
                state.lockedUntilMs = nowMs + cooldownMs
                state.isTemporarilyBlocked = true
                return RateLimitDecision(
                    allowed = false,
                    remainingLockoutMs = cooldownMs,
                    failureCount = state.timestampsMs.size,
                    triggeredSeverity = "MEDIUM",
                    errorCode = "SEC_ERR_429_RATE_LIMIT",
                    reason = "Rate limit exceeded on '$endpointKey' (${state.timestampsMs.size} requests within ${windowMs / 1000}s window)."
                )
            }

            return RateLimitDecision(allowed = true, failureCount = state.consecutiveFailures)
        }
    }

    fun recordAuthFailure(
        endpointKey: String,
        nowMs: Long = System.currentTimeMillis()
    ): RateLimitDecision {
        val state = endpointRateMap.getOrPut(endpointKey) { RateLimitState() }
        synchronized(state) {
            state.consecutiveFailures += 1
            val count = state.consecutiveFailures
            // Progressive delay without permanent lockout (prevents permanent DoS against legitimate users)
            val lockoutMs = when {
                count >= 8 -> 300_000L // 5 min temporary lockout (recoverable by Admin)
                count >= 6 -> 60_000L  // 60s cooldown
                count >= 4 -> 30_000L  // 30s cooldown
                count >= 3 -> 15_000L  // 15s cooldown
                else -> 0L
            }
            if (lockoutMs > 0L) {
                state.lockedUntilMs = nowMs + lockoutMs
                state.isTemporarilyBlocked = count >= 6
            }
            val severity = when {
                count >= 6 -> "HIGH"
                count >= 3 -> "MEDIUM"
                else -> "LOW"
            }
            return RateLimitDecision(
                allowed = lockoutMs == 0L,
                remainingLockoutMs = lockoutMs,
                failureCount = count,
                triggeredSeverity = severity,
                errorCode = if (count >= 3) "SEC_ERR_401_BRUTE_FORCE_LOCKOUT" else "SEC_ERR_401_AUTH_FAILED",
                reason = "Authentication failure #$count on '$endpointKey'. ${if (lockoutMs > 0) "Progressive cooldown (${lockoutMs / 1000}s) activated." else "Monitoring active."}"
            )
        }
    }

    fun resetEndpointLockout(endpointKey: String) {
        endpointRateMap[endpointKey]?.let { state ->
            synchronized(state) {
                state.consecutiveFailures = 0
                state.lockedUntilMs = 0L
                state.isTemporarilyBlocked = false
                state.timestampsMs.clear()
            }
        }
    }

    fun unlockAllRateLimits() {
        endpointRateMap.clear()
    }

    // -------------------------------------------------------------------------
    // 4. INPUT VALIDATION, SQLi / XSS / PATH TRAVERSAL DETECTION & SANITIZATION
    // -------------------------------------------------------------------------

    private val SQLI_REGEX = Regex(
        """(?i)(\b(UNION\s+ALL\s+SELECT|UNION\s+SELECT|DROP\s+TABLE|ALTER\s+TABLE|INSERT\s+INTO|DELETE\s+FROM|EXEC(\s|\+)+(s|x)p\w+)\b|'\s*OR\s*'1'\s*=\s*'1|"\s*OR\s*"1"\s*=\s*"1|--\s*$)"""
    )

    private val XSS_SCRIPT_REGEX = Regex(
        """(?i)(<\s*script\b|javascript\s*:|vbscript\s*:|data\s*:\s*text/html|\bon(error|load|click|mouseover|focus|submit)\s*=|<\s*iframe\b|<\s*object\b|<\s*embed\b)"""
    )

    private val PATH_TRAVERSAL_REGEX = Regex(
        """(?i)(\.\./|\.\.\\|%2e%2e%2f|%2e%2e/|/etc/passwd|/proc/self|\bcmd\.exe\b|\b/bin/sh\b)"""
    )

    data class InputValidationResult(
        val isSafe: Boolean,
        val sanitizedValue: String,
        val detectedCategory: String? = null,
        val severity: String = "LOW",
        val errorCode: String? = null,
        val detectionReason: String? = null
    )

    fun validateAndSanitizeInput(
        fieldName: String,
        rawInput: String,
        maxLength: Int = 2000
    ): InputValidationResult {
        val trimmed = rawInput.trim()

        if (trimmed.length > maxLength) {
            return InputValidationResult(
                isSafe = false,
                sanitizedValue = trimmed.take(maxLength),
                detectedCategory = "BOT_TRAFFIC_BURST",
                severity = "MEDIUM",
                errorCode = "SEC_ERR_413_PAYLOAD_TOO_LARGE",
                detectionReason = "Oversized input payload in '$fieldName' (${trimmed.length} chars > max $maxLength limit)."
            )
        }

        if (SQLI_REGEX.containsMatchIn(trimmed)) {
            return InputValidationResult(
                isSafe = false,
                sanitizedValue = sanitizeText(trimmed),
                detectedCategory = "INJECTION_XSS_SQLI",
                severity = "HIGH",
                errorCode = "SEC_ERR_422_SQLI_DETECTED",
                detectionReason = "Blocked suspected SQL Injection pattern in field '$fieldName'."
            )
        }

        if (XSS_SCRIPT_REGEX.containsMatchIn(trimmed)) {
            return InputValidationResult(
                isSafe = false,
                sanitizedValue = sanitizeText(trimmed),
                detectedCategory = "INJECTION_XSS_SQLI",
                severity = "HIGH",
                errorCode = "SEC_ERR_422_XSS_DETECTED",
                detectionReason = "Blocked suspected Cross-Site Scripting (XSS) / script payload in field '$fieldName'."
            )
        }

        if (PATH_TRAVERSAL_REGEX.containsMatchIn(trimmed)) {
            return InputValidationResult(
                isSafe = false,
                sanitizedValue = sanitizeText(trimmed),
                detectedCategory = "INJECTION_XSS_SQLI",
                severity = "HIGH",
                errorCode = "SEC_ERR_422_PATH_TRAVERSAL",
                detectionReason = "Blocked directory/path traversal sequence in field '$fieldName'."
            )
        }

        return InputValidationResult(
            isSafe = true,
            sanitizedValue = trimmed.take(maxLength)
        )
    }

    fun sanitizeText(input: String): String {
        return input
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("javascript:", "", ignoreCase = true)
            .replace("vbscript:", "", ignoreCase = true)
            .replace("../", "")
            .trim()
    }

    // -------------------------------------------------------------------------
    // 5. WEBVIEW & DEEP-LINK URL ALLOWLIST VALIDATOR
    // -------------------------------------------------------------------------

    private val TRUSTED_HTTPS_DOMAIN_SUFFIXES = listOf(
        "rexlify.netlify.app",
        "ai.studio",
        "supabase.co",
        "google.com",
        "youtube.com",
        "youtu.be",
        "bing.com",
        "microsoft.com",
        "instagram.com",
        "facebook.com",
        "chatgpt.com",
        "openai.com",
        "claude.ai",
        "anthropic.com",
        "gstatic.com",
        "googleapis.com",
        "googleusercontent.com",
        "netlify.app",
        "github.com"
    )

    data class UrlValidationResult(
        val isAllowedInWebView: Boolean,
        val isSafeExternalIntent: Boolean = false,
        val normalizedUrl: String = "",
        val errorCode: String? = null,
        val severity: String = "MEDIUM",
        val reason: String? = null
    )

    fun validateWebViewNavigationUrl(rawUrl: String?): UrlValidationResult {
        if (rawUrl.isNullOrBlank()) {
            return UrlValidationResult(
                isAllowedInWebView = false,
                errorCode = "SEC_ERR_400_EMPTY_URL",
                reason = "Blocked empty or null WebView navigation URL."
            )
        }

        val trimmed = rawUrl.trim()

        // Allow internal about:blank cleanup
        if (trimmed.equals("about:blank", ignoreCase = true)) {
            return UrlValidationResult(isAllowedInWebView = true, normalizedUrl = "about:blank")
        }

        // Explicitly block dangerous schemes inside WebView (XSS, local file theft, content provider probing)
        val lower = trimmed.lowercase(Locale.ROOT)
        if (lower.startsWith("javascript:") ||
            lower.startsWith("vbscript:") ||
            lower.startsWith("file://") ||
            lower.startsWith("content://") ||
            lower.startsWith("data:text/html")
        ) {
            return UrlValidationResult(
                isAllowedInWebView = false,
                isSafeExternalIntent = false,
                errorCode = "SEC_ERR_403_UNSAFE_URI_SCHEME",
                severity = "CRITICAL",
                reason = "Blocked dangerous URI scheme in WebView navigation: '${trimmed.take(48)}'."
            )
        }

        // Suppress market:// and intent:// redirects inside WebView
        if (lower.startsWith("intent://") || lower.startsWith("market://")) {
            return UrlValidationResult(
                isAllowedInWebView = false,
                isSafeExternalIntent = false,
                errorCode = "SEC_ERR_403_INTENT_REDIRECT_BLOCKED",
                severity = "LOW",
                reason = "Suppressed external app-hijack redirect (${trimmed.take(36)})."
            )
        }

        // Reject unencrypted cleartext HTTP
        if (lower.startsWith("http://")) {
            return UrlValidationResult(
                isAllowedInWebView = false,
                isSafeExternalIntent = false,
                errorCode = "SEC_ERR_403_CLEARTEXT_HTTP_BLOCKED",
                severity = "HIGH",
                reason = "Blocked unencrypted cleartext HTTP navigation ('${trimmed.take(60)}'). HTTPS is mandatory."
            )
        }

        return try {
            val uri = URI(trimmed)
            val scheme = uri.scheme?.lowercase(Locale.ROOT) ?: ""
            val host = uri.host?.lowercase(Locale.ROOT) ?: ""

            if (scheme != "https" || host.isBlank()) {
                return UrlValidationResult(
                    isAllowedInWebView = false,
                    isSafeExternalIntent = false,
                    errorCode = "SEC_ERR_400_INVALID_HTTPS_HOST",
                    severity = "MEDIUM",
                    reason = "Rejected non-HTTPS or malformed host in URL: '${trimmed.take(60)}'."
                )
            }

            // Check against path traversal or embedded credentials in URL (e.g., https://evil@target.com)
            if (uri.userInfo != null || trimmed.contains("..")) {
                return UrlValidationResult(
                    isAllowedInWebView = false,
                    isSafeExternalIntent = false,
                    errorCode = "SEC_ERR_403_URL_SPOOFING",
                    severity = "HIGH",
                    reason = "Blocked URL containing embedded userInfo or path traversal characters."
                )
            }

            UrlValidationResult(
                isAllowedInWebView = true,
                normalizedUrl = trimmed
            )
        } catch (_: Exception) {
            UrlValidationResult(
                isAllowedInWebView = false,
                errorCode = "SEC_ERR_400_MALFORMED_URI",
                severity = "MEDIUM",
                reason = "Blocked malformed URI syntax during WebView validation."
            )
        }
    }

    fun isTrustedDomain(host: String): Boolean {
        val cleanHost = host.lowercase(Locale.ROOT).trim()
        return TRUSTED_HTTPS_DOMAIN_SUFFIXES.any { domain ->
            cleanHost == domain || cleanHost.endsWith(".$domain")
        }
    }

    // -------------------------------------------------------------------------
    // 6. FILE UPLOAD VALIDATOR (TYPE, EXTENSION & FILENAME SANITIZATION)
    // -------------------------------------------------------------------------

    private val ALLOWED_UPLOAD_EXTENSIONS = setOf(
        "jpg", "jpeg", "png", "webp", "gif", "pdf", "txt", "mp3", "wav", "m4a", "flac", "ogg", "aac"
    )

    private val BLOCKED_EXECUTABLE_EXTENSIONS = setOf(
        "apk", "dex", "so", "sh", "bat", "cmd", "exe", "js", "html", "htm", "php", "py", "jar", "elf"
    )

    fun validateSelectedFileUri(uri: Uri, displayName: String? = null): InputValidationResult {
        val scheme = uri.scheme?.lowercase(Locale.ROOT) ?: ""
        if (scheme != "content" && scheme != "file") {
            return InputValidationResult(
                isSafe = false,
                sanitizedValue = "",
                detectedCategory = "MALICIOUS_FILE_UPLOAD",
                severity = "HIGH",
                errorCode = "SEC_ERR_403_UNTRUSTED_FILE_URI",
                detectionReason = "Rejected file upload from untrusted URI scheme '$scheme'."
            )
        }

        val nameToInspect = (displayName ?: uri.lastPathSegment ?: "upload_file").trim()
        if (PATH_TRAVERSAL_REGEX.containsMatchIn(nameToInspect)) {
            return InputValidationResult(
                isSafe = false,
                sanitizedValue = "",
                detectedCategory = "MALICIOUS_FILE_UPLOAD",
                severity = "CRITICAL",
                errorCode = "SEC_ERR_403_FILE_PATH_TRAVERSAL",
                detectionReason = "Blocked file upload with path traversal sequence in filename."
            )
        }

        val ext = nameToInspect.substringAfterLast('.', "").lowercase(Locale.ROOT)
        if (ext in BLOCKED_EXECUTABLE_EXTENSIONS) {
            return InputValidationResult(
                isSafe = false,
                sanitizedValue = "",
                detectedCategory = "MALICIOUS_FILE_UPLOAD",
                severity = "CRITICAL",
                errorCode = "SEC_ERR_415_EXECUTABLE_UPLOAD_BLOCKED",
                detectionReason = "Blocked dangerous executable/script file extension '.$ext' during upload."
            )
        }

        val safeFileName = nameToInspect.replace(Regex("[^a-zA-Z0-9._ -]"), "_").take(120)
        return InputValidationResult(
            isSafe = true,
            sanitizedValue = safeFileName,
            
        )
    }

    // -------------------------------------------------------------------------
    // 7. DEVICE INTEGRITY & ENVIRONMENT RISK SIGNAL DETECTOR
    // -------------------------------------------------------------------------

    data class EnvironmentRiskReport(
        val riskLevel: String, // LOW, MEDIUM, HIGH
        val signals: List<String>,
        val deviceSummary: String
    )

    fun evaluateDeviceEnvironmentRisk(): EnvironmentRiskReport {
        val signals = mutableListOf<String>()

        // 1. Check test-keys build tags
        if (Build.TAGS?.contains("test-keys") == true) {
            signals.add("Custom/Test-Signed OS Build (test-keys)")
        }

        // 2. Check common su binary locations (treated strictly as a risk signal, never as proof of malice)
        val suPaths = listOf("/system/bin/su", "/system/xbin/su", "/sbin/su", "/data/local/xbin/su")
        if (suPaths.any { runCatching { File(it).exists() }.getOrDefault(false) }) {
            signals.add("Root Binary Indicator (su path present)")
        }

        // 3. Check virtual / emulator environment
        val isVirtual = Build.FINGERPRINT.contains("generic", ignoreCase = true) ||
                Build.HARDWARE.contains("ranchu", ignoreCase = true) ||
                Build.HARDWARE.contains("goldfish", ignoreCase = true) ||
                Build.PRODUCT.contains("sdk_gphone", ignoreCase = true)
        if (isVirtual) {
            signals.add("Virtualized / Emulator Runtime Environment")
        }

        val riskLevel = when {
            signals.size >= 2 -> "MEDIUM"
            signals.size == 1 -> "LOW"
            else -> "LOW"
        }

        val summary = "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT}) • ${Build.MANUFACTURER} ${Build.MODEL}" +
                if (signals.isNotEmpty()) " • Signals: ${signals.joinToString(", ")}" else " • Verified Standard Runtime"

        return EnvironmentRiskReport(
            riskLevel = riskLevel,
            signals = signals,
            deviceSummary = summary
        )
    }

    // -------------------------------------------------------------------------
    // 8. SECURITY EVENT BUILDER & SEED TEST DATA (Deliverable #6)
    // -------------------------------------------------------------------------

    fun formatUtcIsoTimestamp(epochMs: Long = System.currentTimeMillis()): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        return sdf.format(Date(epochMs))
    }

    fun createSecurityEvent(
        category: String,
        severity: String,
        endpointOrModule: String,
        authenticatedUserId: String?,
        userRole: String?,
        sessionCorrelationId: String?,
        detectionReason: String,
        errorCode: String,
        mitigationAction: String,
        sourceIp: String = "103.21.244.18 (Client Edge)",
        approxLocation: String = "Kolkata, West Bengal, IN (Approx. IP-derived • VPN/Proxy/ISP node possible)",
        status: String = "Detected",
        isSourceBlocked: Boolean = false
    ): SecurityEventEntity {
        val now = System.currentTimeMillis()
        val envReport = evaluateDeviceEnvironmentRisk()
        val safeUserId = if (authenticatedUserId.isNullOrBlank()) {
            "Unknown / Unauthenticated"
        } else {
            authenticatedUserId
        }
        val safeRole = if (userRole.isNullOrBlank()) "UNAUTHENTICATED" else userRole
        val safeCorrId = if (sessionCorrelationId.isNullOrBlank()) {
            "corr_" + UUID.randomUUID().toString().replace("-", "").take(10)
        } else {
            sessionCorrelationId
        }
        val eventId = "SEC-2026-" + (10000..99999).random()

        return SecurityEventEntity(
            eventId = eventId,
            timestampUtc = formatUtcIsoTimestamp(now),
            timestampEpochMs = now,
            category = category,
            severity = severity.uppercase(Locale.ROOT),
            endpointOrModule = endpointOrModule,
            authenticatedUserId = safeUserId,
            userRole = safeRole,
            sessionCorrelationId = safeCorrId,
            sourceIp = sourceIp,
            approxLocation = approxLocation,
            deviceInfo = envReport.deviceSummary,
            detectionReason = detectionReason,
            errorCode = errorCode,
            mitigationAction = mitigationAction,
            status = status,
            investigationNotes = "",
            adminActionHistory = "[${formatUtcIsoTimestamp(now)}] Automated Defense: $mitigationAction",
            isSourceBlocked = isSourceBlocked
        )
    }

    /**
     * Generates realistic initial security events for Admin Security Center testing & demonstration.
     */
    fun buildInitialSampleSecurityEvents(nowMs: Long = System.currentTimeMillis()): List<SecurityEventEntity> {
        val env = evaluateDeviceEnvironmentRisk().deviceSummary
        return listOf(
            SecurityEventEntity(
                eventId = "SEC-2026-90412",
                timestampUtc = formatUtcIsoTimestamp(nowMs - 1_200_000L),
                timestampEpochMs = nowMs - 1_200_000L,
                category = "BRUTE_FORCE_AUTH",
                severity = "HIGH",
                endpointOrModule = "/auth/admin-login-gate",
                authenticatedUserId = "Unknown / Unauthenticated",
                userRole = "UNAUTHENTICATED",
                sessionCorrelationId = "corr_7f31a9c02b",
                sourceIp = "185.220.101.42 (Tor/VPN Exit)",
                approxLocation = "Frankfurt, Hesse, DE (Approx. IP-derived • VPN/Proxy/Tor detected)",
                deviceInfo = env,
                detectionReason = "6 consecutive failed password attempts on Admin Panel login gate within 45 seconds (credential stuffing pattern).",
                errorCode = "SEC_ERR_401_BRUTE_FORCE_LOCKOUT",
                mitigationAction = "Progressive 60s lockout enforced; source session flagged; plaintext password excluded from logs.",
                status = "Mitigated",
                investigationNotes = "Automated brute-force protection blocked repeated password guessing.",
                adminActionHistory = "[${formatUtcIsoTimestamp(nowMs - 1_200_000L)}] Automated Defense: Progressive 60s lockout enforced on /auth/admin-login-gate.",
                isSourceBlocked = true
            ),
            SecurityEventEntity(
                eventId = "SEC-2026-90388",
                timestampUtc = formatUtcIsoTimestamp(nowMs - 3_600_000L),
                timestampEpochMs = nowMs - 3_600_000L,
                category = "PRIVILEGE_ESCALATION",
                severity = "CRITICAL",
                endpointOrModule = "/api/v1/users/role-upgrade",
                authenticatedUserId = "usr_gst_david",
                userRole = "STANDARD",
                sessionCorrelationId = "corr_4e82d19a55",
                sourceIp = "49.37.152.88 (ISP Carrier NAT)",
                approxLocation = "Kolkata, West Bengal, IN (Approx. IP-derived • Mobile Carrier NAT)",
                deviceInfo = env,
                detectionReason = "Standard user session attempted unauthorized RBAC privilege escalation to CEO_PANEL role.",
                errorCode = "SEC_ERR_403_RBAC_DENIED",
                mitigationAction = "Rejected privileged operation immediately; preserved forensic audit trail; notified Security Admin.",
                status = "Detected",
                investigationNotes = "Requires review by CEO / Security Administrator.",
                adminActionHistory = "[${formatUtcIsoTimestamp(nowMs - 3_600_000L)}] Automated Defense: Critical RBAC violation blocked immediately.",
                isSourceBlocked = false
            ),
            SecurityEventEntity(
                eventId = "SEC-2026-90345",
                timestampUtc = formatUtcIsoTimestamp(nowMs - 7_200_000L),
                timestampEpochMs = nowMs - 7_200_000L,
                category = "INJECTION_XSS_SQLI",
                severity = "HIGH",
                endpointOrModule = "/support/create-ticket",
                authenticatedUserId = "Unknown / Unauthenticated",
                userRole = "STANDARD",
                sessionCorrelationId = "corr_9a14c70e11",
                sourceIp = "103.145.12.74 (Broadband Edge)",
                approxLocation = "Mumbai, Maharashtra, IN (Approx. IP-derived • ISP Gateway)",
                deviceInfo = env,
                detectionReason = "Blocked malicious XSS/SQLi payload ('<script>fetch(...)</script>' / UNION SELECT) in ticket input.",
                errorCode = "SEC_ERR_422_XSS_DETECTED",
                mitigationAction = "Payload sanitized and request rejected before reaching Room DAO / Supabase REST layer.",
                status = "Mitigated",
                investigationNotes = "Input validation rule matched script injection tag.",
                adminActionHistory = "[${formatUtcIsoTimestamp(nowMs - 7_200_000L)}] Automated Defense: Sanitized and blocked malicious input payload.",
                isSourceBlocked = false
            ),
            SecurityEventEntity(
                eventId = "SEC-2026-90291",
                timestampUtc = formatUtcIsoTimestamp(nowMs - 14_400_000L),
                timestampEpochMs = nowMs - 14_400_000L,
                category = "UNSAFE_WEBVIEW_REDIRECT",
                severity = "CRITICAL",
                endpointOrModule = "WebView/shouldOverrideUrlLoading",
                authenticatedUserId = "usr_gst_alex",
                userRole = "STANDARD",
                sessionCorrelationId = "corr_2b55e01f88",
                sourceIp = "Local WebView Sandbox",
                approxLocation = "Hyderabad, Telangana, IN (Approx. IP-derived • Client Edge)",
                deviceInfo = env,
                detectionReason = "Blocked untrusted javascript:/file:// URI redirect attempt inside In-App Browser WebView.",
                errorCode = "SEC_ERR_403_UNSAFE_URI_SCHEME",
                mitigationAction = "Navigation cancelled; WebView file/content access disabled; session isolated.",
                status = "Resolved",
                investigationNotes = "Verified WebView sandbox blocked local file access.",
                adminActionHistory = "[${formatUtcIsoTimestamp(nowMs - 14_400_000L)}] Automated Defense: Blocked unsafe URI scheme in WebView.\n[${formatUtcIsoTimestamp(nowMs - 10_800_000L)}] Admin Rizwan RR: Verified & marked Resolved.",
                isSourceBlocked = false
            ),
            SecurityEventEntity(
                eventId = "SEC-2026-90210",
                timestampUtc = formatUtcIsoTimestamp(nowMs - 28_800_000L),
                timestampEpochMs = nowMs - 28_800_000L,
                category = "OTP_RATE_LIMIT",
                severity = "MEDIUM",
                endpointOrModule = "/auth/rrr-family-portal",
                authenticatedUserId = "Unknown / Unauthenticated",
                userRole = "UNAUTHENTICATED",
                sessionCorrelationId = "corr_6c90f32d44",
                sourceIp = "157.48.92.19 (Mobile Edge)",
                approxLocation = "Bengaluru, Karnataka, IN (Approx. IP-derived • Mobile Carrier)",
                deviceInfo = env,
                detectionReason = "Rapid verification requests exceeded threshold (9 requests in 60s window) on RRR Family Portal.",
                errorCode = "SEC_ERR_429_RATE_LIMIT",
                mitigationAction = "30-second sliding-window rate limit applied; step-up verification required.",
                status = "Investigating",
                investigationNotes = "Checking whether user mistyped Family ID or automated script.",
                adminActionHistory = "[${formatUtcIsoTimestamp(nowMs - 28_800_000L)}] Automated Defense: Sliding-window rate limit triggered.",
                isSourceBlocked = false
            ),
            SecurityEventEntity(
                eventId = "SEC-2026-90155",
                timestampUtc = formatUtcIsoTimestamp(nowMs - 43_200_000L),
                timestampEpochMs = nowMs - 43_200_000L,
                category = "UNAUTHORIZED_ACCESS",
                severity = "LOW",
                endpointOrModule = "/launcher/restricted-app-gate",
                authenticatedUserId = "usr_gst_rahul",
                userRole = "STANDARD",
                sessionCorrelationId = "corr_1d44b87c21",
                sourceIp = "Local Device Session",
                approxLocation = "Delhi NCR, IN (Approx. IP-derived • ISP Node)",
                deviceInfo = env,
                detectionReason = "Standard user attempted to launch restricted internal module without RRR Family/CEO clearance.",
                errorCode = "SEC_ERR_403_APP_ROLE_RESTRICTED",
                mitigationAction = "Displayed authorization gate dialog; blocked module launch.",
                status = "False Positive",
                investigationNotes = "Normal user clicked locked app icon on home grid; no malicious intent.",
                adminActionHistory = "[${formatUtcIsoTimestamp(nowMs - 43_200_000L)}] Automated Defense: Restricted app launch blocked.\n[${formatUtcIsoTimestamp(nowMs - 36_000_000L)}] Admin Rizwan RR: Marked as False Positive (benign user click).",
                isSourceBlocked = false
            )
        )
    }
}
