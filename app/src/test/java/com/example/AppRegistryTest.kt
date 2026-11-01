package com.example

import com.example.data.model.AppCategory
import com.example.data.model.AppRegistry
import com.example.data.model.AppStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AppRegistryTest {

    @Test
    fun testCoreEcosystemAppsCountAndIds() {
        val apps = AppRegistry.getAll()
        assertEquals(6, apps.size)

        val ids = apps.map { it.id }
        assertTrue(ids.contains("rexlify"))
        assertTrue(ids.contains("neurax"))
        assertTrue(ids.contains("noorax"))
        assertTrue(ids.contains("sifra"))
        assertTrue(ids.contains("rodp"))
        assertTrue(ids.contains("ph_veg"))
    }

    @Test
    fun testAppRegistryMetadataIntegrity() {
        AppRegistry.getAll().forEach { app ->
            assertTrue(app.name.isNotBlank())
            assertTrue(app.logo.isNotBlank())
            assertTrue(app.url.startsWith("https://"))
            assertNotNull(app.category)
            assertEquals(AppStatus.AVAILABLE, app.status)
        }
    }

    @Test
    fun testToAppItemConversion() {
        val rexlify = AppRegistry.REXLIFY.toAppItem()
        assertEquals("rexlify", rexlify.id)
        assertEquals("REXLIFY", rexlify.name)
        assertEquals(AppCategory.PRODUCTIVITY, rexlify.category)
        assertTrue(rexlify.isFeatured)
        assertTrue(rexlify.isEnabled)
    }

    @Test
    fun testFindById() {
        val neurax = AppRegistry.findById("neurax")
        assertNotNull(neurax)
        assertEquals("NeuraX", neurax?.name)
        assertEquals(AppCategory.AI, neurax?.category)

        val noorax = AppRegistry.findById("NOORAX")
        assertNotNull(noorax)
        assertEquals("NooraX", noorax?.name)
    }
}
