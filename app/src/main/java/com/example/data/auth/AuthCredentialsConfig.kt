package com.example.data.auth

import com.example.data.security.SecurityHardeningEngine

object AuthCredentialsConfig {
    const val OFFICIAL_SUPPORT_EMAIL = "rexlifyofficialsupportteam@gmail.com"

    /**
     * Verifies RRR Family credentials using salted PBKDF2 constant-time comparison.
     */
    fun isValidFamilyCredentials(id: String, pass: String): Boolean {
        return SecurityHardeningEngine.verifyFamilyCredentials(id, pass)
    }

    /**
     * Verifies CEO / Executive credentials using salted PBKDF2 constant-time comparison.
     */
    fun isValidCeoCredentials(id: String, pass: String): Boolean {
        return SecurityHardeningEngine.verifyCeoCredentials(id, pass)
    }

    /**
     * Verifies Admin Panel gate password using salted PBKDF2 constant-time comparison.
     */
    fun isValidAdminGatePassword(pass: String): Boolean {
        return SecurityHardeningEngine.verifyAdminPanelPassword(pass)
    }

    /**
     * Verifies Admin Panel 2FA Passkey (any 1 of 4 authorized keys: RRR@123, RTECH, Ruma, RRR@007).
     */
    fun isValidAdminPasskey(passkey: String): Boolean {
        return SecurityHardeningEngine.verifyAdminPasskey(passkey)
    }

    /**
     * Applications permitted for Regular / Standard Users:
     * 1. REXLIFY
     * 2. SIFRA AI
     * 3. NeuraX
     * 4. Noorax
     * 5. RODP
     * 6. Maps (Google Maps)
     * 7. PH VEG
     * 8. Tasks (Google Tasks)
     * 9. Rexlify Music
     * 10. YouTube
     * 11. Google Search
     * 12. Microsoft Edge
     */
    val STANDARD_AUTHORIZED_APP_IDS = setOf(
        "rexlify",
        "sifra",
        "neurax",
        "noorax",
        "rodp",
        "google_maps",
        "ph_veg",
        "google_tasks",
        "rexlify_music",
        "youtube",
        "google_search",
        "ms_edge",
        "rizwan_rr",
        "maya_os"
    )

    fun isAppAuthorizedForRole(appId: String, role: UserRole): Boolean {
        return when (role) {
            UserRole.STANDARD -> STANDARD_AUTHORIZED_APP_IDS.contains(appId)
            UserRole.BRANCH_ADMIN,
            UserRole.RRR_FAMILY,
            UserRole.CEO_PANEL,
            UserRole.SECURITY_ADMIN -> true
        }
    }
}
