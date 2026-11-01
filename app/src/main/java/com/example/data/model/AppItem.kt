package com.example.data.model

enum class AppCategory(val displayName: String) {
    ALL("All"),
    AI("AI"),
    PRODUCTIVITY("Productivity"),
    CREATIVE_STUDIO("Creative Studio"),
    EDUCATION("Education"),
    DOCUMENT_UTILITIES("Document Utilities & Tools"),
    NEXUS_CLOUD("Nexus Cloud"),
    MAYA_DIAGNOSTIC("Maya Diagnostic"),
    ECOMMERCE("E-Commerce")
}

enum class AppStatus(val label: String) {
    AVAILABLE("Available"),
    ACTIVE("Active"),
    COMING_SOON("Coming Soon"),
    MAINTENANCE("Maintenance")
}

data class AppItem(
    val id: String,
    val name: String,
    val tagline: String,
    val fullDescription: String,
    val url: String,
    val category: AppCategory,
    val status: AppStatus = AppStatus.AVAILABLE,
    val isFeatured: Boolean = false,
    val isFavorite: Boolean = false,
    val isPinnedHome: Boolean = true,
    val isEnabled: Boolean = true,
    val iconKey: String = "DEFAULT",
    val primaryColorHex: Long = 0xFF475569,
    val secondaryColorHex: Long = 0xFF334155,
    val lastOpenedTimestamp: Long = 0L,
    val launchCount: Int = 0,
    val isCustom: Boolean = false,
    val customTags: String = ""
)
