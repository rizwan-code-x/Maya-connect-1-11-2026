package com.example.data.model

enum class EmergencyMode(val code: Int) {
    NORMAL(0),                     // Standard operating state
    MAINTENANCE_FREEZE(1),         // Mode 1: Home page shows application is under maintenance; app launches paused.
    SELECTIVE_FEATURE_CONTROL(2),  // Mode 2: Specific maintenance with custom duration (hours) & selected module list.
    HIGH_SECURITY_LOCKDOWN(3);     // Mode 3: Full home page lockout for Standard Users & Family (CEO Panel exempt).

    companion object {
        fun fromCode(code: Int): EmergencyMode {
            return entries.find { it.code == code } ?: NORMAL
        }
    }
}

data class EmergencyFeatureItem(
    val id: String,
    val name: String,
    val category: String,
    val description: String,
    val isEnabled: Boolean = true
)

data class EmergencySystemState(
    val activeMode: EmergencyMode = EmergencyMode.NORMAL,
    val maintenanceReason: String = "Scheduled core infrastructure and security updates in progress.",
    val customDurationHours: Int = 4,
    val expiresAtTimestamp: Long = 0L,
    val lastUpdatedBy: String = "Rizwan RR",
    val lastUpdatedAt: Long = System.currentTimeMillis(),
    val disabledFeatureIds: Set<String> = emptySet()
) {
    /**
     * Checks if a given feature or app ID is currently disabled under the active emergency mode.
     */
    fun isAppOrFeatureDisabled(featureOrAppId: String): Boolean {
        val clean = featureOrAppId.trim().lowercase()
        return when (activeMode) {
            EmergencyMode.NORMAL -> false
            EmergencyMode.MAINTENANCE_FREEZE -> true
            EmergencyMode.SELECTIVE_FEATURE_CONTROL -> disabledFeatureIds.contains(clean)
            EmergencyMode.HIGH_SECURITY_LOCKDOWN -> true
        }
    }

    /**
     * Returns human-readable names of all currently disabled features/modules for Mode 2 display.
     */
    fun getDisabledFeatureNames(): List<String> {
        if (disabledFeatureIds.isEmpty()) return emptyList()
        return EmergencyFeatureCatalog.ALL_FEATURES
            .filter { disabledFeatureIds.contains(it.id.lowercase()) }
            .map { it.name }
    }
}

/**
 * Represents a Family Member account managed in the Admin Panel and synced via Firebase Realtime Database.
 */
data class FamilyMemberAccount(
    val id: String,
    val password: String,
    val memberName: String,
    val details: String = "Verified RRR Family Member",
    val createdAt: Long = System.currentTimeMillis(),
    val lastLoginAt: Long = 0L,
    val loginCount: Int = 0,
    val isActive: Boolean = true
)

object EmergencyFeatureCatalog {
    val ALL_FEATURES: List<EmergencyFeatureItem> = listOf(
        EmergencyFeatureItem("app_launcher", "App Launchers & Store", "Core Launcher", "Access to system app drawers, installed app shortcuts, and launchers."),
        EmergencyFeatureItem("chat_assistant", "Chat Assistant (Sara Voice)", "AI Intelligence", "Sara Voice Assistant and real-time interactive Gemini chat."),
        EmergencyFeatureItem("customer_support", "Customer Support Desk", "Support", "Live customer support ticket submission, messaging, and inquiry desk."),
        EmergencyFeatureItem("device_diagnostics", "Device Diagnostics Tools", "System Utilities", "Hardware telemetry, battery diagnostics, storage, and network sensors."),
        EmergencyFeatureItem("ecosystem_directory", "Ecosystem Directory", "Navigation", "Official Rexlify ecosystem links, portals, and affiliated partner domains."),
        EmergencyFeatureItem("google_maps", "Google Maps Navigation", "Maps & Navigation", "Integrated mapping, directions, and geographic location services."),
        EmergencyFeatureItem("google_search", "Google Search Portal", "Web Browsing", "Web search engine integration and instant search queries."),
        EmergencyFeatureItem("google_tasks", "Google Tasks & Scheduler", "Productivity", "Task management, daily schedules, checklist reminders, and task sync."),
        EmergencyFeatureItem("maya_os", "Maya OS Desktop Environment", "System Core", "Advanced desktop simulation, multi-windowing, and file ecosystem."),
        EmergencyFeatureItem("ms_edge", "Microsoft Edge Browser", "Web Browsing", "Modern Chromium-based browser gateway and secure web surfing."),
        EmergencyFeatureItem("neurax", "NeuraX Neural Suite", "Productivity", "AI analytical workspace and intelligent automation modules."),
        EmergencyFeatureItem("new_user_login", "New User Login & Registration", "Security & Auth", "Standard user onboarding, registration terms, and profile setup."),
        EmergencyFeatureItem("noorax", "Noorax Suite", "Ecosystem", "Enterprise workflow suite and integrated communications."),
        EmergencyFeatureItem("notepad", "Notepad & Secure Notes", "Productivity", "Personal text editor, markdown notes, auto-save, and scratchpad."),
        EmergencyFeatureItem("ph_veg", "PH VEG Portal", "Commerce", "Agricultural inventory, produce catalog, and commerce hub."),
        EmergencyFeatureItem("rexlify", "Rexlify Core Suite", "Core Ecosystem", "Rexlify main enterprise dashboard, tools, and company services."),
        EmergencyFeatureItem("rexlify_music", "Rexlify Music Player", "Entertainment", "Premium music streaming player, queue management, and audio playback."),
        EmergencyFeatureItem("rizwan_rr", "Rizwan RR Profile", "Executive", "Founder and CEO official profile, biography, and credentials."),
        EmergencyFeatureItem("rodp", "RODP Portal", "Commerce", "Enterprise ordering and partner distribution operations."),
        EmergencyFeatureItem("rrr_family", "RRR Family Access Portal", "Security & Auth", "Verified RRR family member portal, exclusive benefits, and resources."),
        EmergencyFeatureItem("security_center", "Security Center & Event Logs", "Security", "Security incident monitoring, threat response, and audit log analysis."),
        EmergencyFeatureItem("sifra", "SIFRA AI Assistant", "AI Intelligence", "Cognitive query engine and automated assistance."),
        EmergencyFeatureItem("system_quick_actions", "System Quick Actions & Toggles", "System Utilities", "Quick system toggles (Wi-Fi, Bluetooth, Torch, Dark Mode)."),
        EmergencyFeatureItem("universal_search", "Universal Search Engine", "Navigation", "In-app omni-search across all installed tools, notes, and contacts."),
        EmergencyFeatureItem("youtube", "YouTube Media Streamer", "Entertainment", "Video streaming, curated media channels, and media playback.")
    )
}
