package com.example.data.model

import com.example.data.config.CentralAppConfig

/**
 * Centralized AppRegistry data class and companion object
 * Manages metadata (name, logo, URL, category, status) for:
 * - REXLIFY
 * - NeuraX
 * - NooraX
 * - SIFRA
 * - RODP
 */
data class AppRegistry(
    val id: String,
    val name: String,
    val logo: String,
    val url: String,
    val category: AppCategory,
    val status: AppStatus = AppStatus.AVAILABLE,
    val tagline: String = "",
    val fullDescription: String = "",
    val primaryColorHex: Long = 0xFF334155,
    val secondaryColorHex: Long = 0xFF1E293B
) {
    fun toAppItem(): AppItem {
        return AppItem(
            id = id,
            name = name,
            tagline = tagline,
            fullDescription = fullDescription,
            url = url,
            category = category,
            status = status,
            isFeatured = true,
            isFavorite = true,
            isPinnedHome = true,
            isEnabled = true,
            iconKey = logo,
            primaryColorHex = primaryColorHex,
            secondaryColorHex = secondaryColorHex
        )
    }

    companion object {
        val REXLIFY = AppRegistry(
            id = "rexlify",
            name = "REXLIFY",
            logo = "REXLIFY",
            url = CentralAppConfig.REXLIFY_URL,
            category = AppCategory.PRODUCTIVITY,
            status = AppStatus.AVAILABLE,
            tagline = "Digital Ecosystem Platform & Command Hub",
            fullDescription = "Flagship portal of the Maya Connect digital ecosystem. Multi-module services, workspace intelligence, unified connectivity, and tools.",
            primaryColorHex = 0xFF334155,
            secondaryColorHex = 0xFF1E293B
        )

        val NEURAX = AppRegistry(
            id = "neurax",
            name = "NeuraX",
            logo = "NEURAX",
            url = CentralAppConfig.NEURAX_URL,
            category = AppCategory.AI,
            status = AppStatus.AVAILABLE,
            tagline = "Neural AI & Cognitive Intelligence",
            fullDescription = "Next-generation artificial intelligence cognitive suite for automated reasoning, deep analysis, and workspace operations.",
            primaryColorHex = 0xFF475569,
            secondaryColorHex = 0xFF334155
        )

        val NOORAX = AppRegistry(
            id = "noorax",
            name = "NooraX",
            logo = "NOORAX",
            url = CentralAppConfig.NOORAX_URL,
            category = AppCategory.CREATIVE_STUDIO,
            status = AppStatus.AVAILABLE,
            tagline = "Creative Generative Studio & Design Intelligence",
            fullDescription = "Creative generative studio powering visionary content, creative workflows, and concept exploration.",
            primaryColorHex = 0xFF64748B,
            secondaryColorHex = 0xFF475569
        )

        val SIFRA = AppRegistry(
            id = "sifra",
            name = "SIFRA",
            logo = "SIFRA",
            url = CentralAppConfig.SIFRA_URL,
            category = AppCategory.AI,
            status = AppStatus.AVAILABLE,
            tagline = "Smart Intelligent Framework & Assistant",
            fullDescription = "Core framework assistant providing algorithmic resource allocation, intelligent automation, and ecosystem assistance.",
            primaryColorHex = 0xFF334155,
            secondaryColorHex = 0xFF1E293B
        )

        val RODP = AppRegistry(
            id = "rodp",
            name = "RODP",
            logo = "RODP",
            url = CentralAppConfig.RODP_URL,
            category = AppCategory.EDUCATION,
            status = AppStatus.AVAILABLE,
            tagline = "Rizwan Online Dreams Platform",
            fullDescription = "Rizwan Online Dreams Platform - Expansion suite for entrepreneurial ventures, ecosystem tools, and collaborative projects.",
            primaryColorHex = 0xFF475569,
            secondaryColorHex = 0xFF334155
        )

        val PH_VEG = AppRegistry(
            id = "ph_veg",
            name = "PH VEG",
            logo = "PH_VEG",
            url = CentralAppConfig.PH_VEG_URL,
            category = AppCategory.ECOMMERCE,
            status = AppStatus.AVAILABLE,
            tagline = "Fresh Vegetables & E-Commerce Produce Platform",
            fullDescription = "Official PH VEG e-commerce platform offering fresh farm vegetables, quality produce, and digital marketplace ordering.",
            primaryColorHex = 0xFF16A34A,
            secondaryColorHex = 0xFF15803D
        )

        val coreEcosystemApps: List<AppRegistry> = listOf(
            REXLIFY,
            NEURAX,
            NOORAX,
            SIFRA,
            RODP,
            PH_VEG
        )

        fun getAll(): List<AppRegistry> = coreEcosystemApps

        fun findById(id: String): AppRegistry? =
            coreEcosystemApps.find { it.id.equals(id, ignoreCase = true) }

        fun getUrl(id: String): String =
            findById(id)?.url ?: ""

        fun toAppItems(): List<AppItem> =
            coreEcosystemApps.map { it.toAppItem() }
    }
}
