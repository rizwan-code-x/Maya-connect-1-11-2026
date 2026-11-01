package com.example.data.config

import java.net.URI

object CentralAppConfig {
    const val REXLIFY_URL = "https://rexlify.netlify.app/"
    const val NEURAX_URL = "https://neurax.ai.studio/"
    const val NOORAX_URL = "https://noorax.ai.studio/"
    const val SIFRA_URL = "https://sifrar.ai.studio"
    const val PH_VEG_URL = "https://phvegetables.ai.studio/"
    const val RIZWAN_RR_WEBSITE_URL = "https://rizwanr.ai.studio"
    const val EDGE_BROWSER_URL = "https://www.bing.com"
    const val EDGE_BROWSER_PACKAGE = "com.microsoft.emmx"
    const val RODP_URL = "https://rexlify.netlify.app/"
    const val STATUS_URL = "https://rexlify.netlify.app/status"
    const val CLOUD_URL = "https://rexlify.netlify.app/cloud"
    const val MAYA_OS_URL = "maya://os"
    const val GOOGLE_SEARCH_URL = "https://www.google.com"
    const val YOUTUBE_URL = "https://www.youtube.com"
    const val GMAIL_URL = "https://mail.google.com"
    const val INSTAGRAM_URL = "https://www.instagram.com"
    const val FACEBOOK_URL = "https://www.facebook.com"
    const val GOOGLE_TASKS_URL = "https://tasks.google.com"
    const val GEMINI_URL = "https://gemini.google.com"
    const val CHATGPT_URL = "https://chatgpt.com"
    const val CLAUDE_URL = "https://claude.ai"
    const val MAPS_URL = "https://www.google.com/maps?force=web"
    const val OFFICIAL_SUPPORT_EMAIL = "rexlifyofficialsupportteam@gmail.com"

    fun isValidHttpsUrl(url: String?): Boolean {
        if (url.isNullOrBlank()) return false
        val trimmed = url.trim()
        if (trimmed.startsWith("https://", ignoreCase = true)) {
            return trimmed.length > 8
        }
        return try {
            val uri = URI(trimmed)
            uri.scheme?.equals("https", ignoreCase = true) == true && !uri.host.isNullOrBlank()
        } catch (_: Exception) {
            false
        }
    }
}
