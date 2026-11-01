package com.example.data.model

import java.util.UUID

/**
 * AppFolder
 * Represents an organized collection of apps on the Home Screen.
 * Automatically created and named when apps are dragged and dropped onto each other.
 */
data class AppFolder(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val appIds: List<String>,
    val colorHex: Long = 0xFF6366F1,
    val isAutoNamed: Boolean = true
) {
    companion object {
        /**
         * Creates an AppFolder from two apps, generating an intelligent auto-name.
         */
        fun createWithAutoName(app1: AppItem, app2: AppItem): AppFolder {
            val apps = listOf(app1, app2)
            val name = generateFolderName(apps)
            return AppFolder(
                name = name,
                appIds = listOf(app1.id, app2.id),
                isAutoNamed = true
            )
        }

        /**
         * Automatically generates an intelligent folder name based on the apps being grouped.
         */
        fun generateFolderName(apps: List<AppItem>): String {
            if (apps.isEmpty()) return "New Folder"
            if (apps.size == 1) return "${apps[0].name} Hub"

            val ids = apps.map { it.id.lowercase() }.toSet()
            val categories = apps.map { it.category }.distinct()

            // Special intelligent detection for AI tools
            val aiKeywords = setOf("gemini", "chatgpt", "claude", "sifra", "neurax", "noorax", "rodp")
            val isAiHub = ids.any { id -> aiKeywords.any { kw -> id.contains(kw) } }
            if (isAiHub && categories.all { it == AppCategory.AI || it == AppCategory.ALL }) {
                return "AI & Intelligence"
            }

            // Media / Music
            val mediaKeywords = setOf("music", "rexlify_music", "youtube", "media", "spotify", "video")
            if (ids.any { id -> mediaKeywords.any { kw -> id.contains(kw) } }) {
                return "Media & Streaming"
            }

            // Social / Communication
            val socialKeywords = setOf("instagram", "facebook", "gmail", "chat", "dialer", "phone")
            if (ids.any { id -> socialKeywords.any { kw -> id.contains(kw) } }) {
                return "Social & Connect"
            }

            // Single category matching
            if (categories.size == 1) {
                return when (categories[0]) {
                    AppCategory.AI -> "AI Suite"
                    AppCategory.PRODUCTIVITY -> "Productivity"
                    AppCategory.CREATIVE_STUDIO -> "Creative Studio"
                    AppCategory.DOCUMENT_UTILITIES -> "Utilities & Tools"
                    AppCategory.EDUCATION -> "Education Hub"
                    AppCategory.MAYA_DIAGNOSTIC -> "System Diagnostics"
                    AppCategory.NEXUS_CLOUD -> "Cloud Services"
                    AppCategory.ECOMMERCE -> "E-Commerce Market"
                    AppCategory.ALL -> "Workspace Apps"
                    else -> "Workspace Apps"
                }
            }

            // Two distinct categories
            if (categories.size == 2) {
                return "${categories[0].displayName} & ${categories[1].displayName}"
            }

            return "Workspace Hub"
        }
    }
}
