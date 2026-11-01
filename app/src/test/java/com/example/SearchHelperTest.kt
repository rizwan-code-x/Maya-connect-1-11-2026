package com.example

import com.example.data.config.CentralAppConfig
import com.example.data.model.AppCategory
import com.example.data.model.AppItem
import com.example.data.model.AppStatus
import com.example.data.model.SearchHelper
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SearchHelperTest {

    private val sampleApp = AppItem(
        id = "neurax",
        name = "NeuraX",
        tagline = "Autonomous Neural AI & Cognitive Intelligence",
        fullDescription = "Next-generation artificial intelligence cognitive suite",
        url = CentralAppConfig.NEURAX_URL,
        category = AppCategory.AI,
        status = AppStatus.AVAILABLE,
        customTags = "neurax ai neural cognitive"
    )

    @Test
    fun testSearchMatches() {
        assertTrue(SearchHelper.matchesQuery(sampleApp, "neur"))
        assertTrue(SearchHelper.matchesQuery(sampleApp, "AI"))
        assertTrue(SearchHelper.matchesQuery(sampleApp, "cognitive"))
        assertTrue(SearchHelper.matchesQuery(sampleApp, ""))
        assertFalse(SearchHelper.matchesQuery(sampleApp, "xyz12345nonexistent"))
    }

    @Test
    fun testHttpsValidation() {
        assertTrue(CentralAppConfig.isValidHttpsUrl("https://rexlify.netlify.app/"))
        assertTrue(CentralAppConfig.isValidHttpsUrl("https://neurax.ai.studio/"))
        assertFalse(CentralAppConfig.isValidHttpsUrl("http://unsecure.com"))
        assertFalse(CentralAppConfig.isValidHttpsUrl(""))
        assertFalse(CentralAppConfig.isValidHttpsUrl("ftp://invalid"))
    }
}
