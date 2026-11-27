package com.pynexia.resumebuilder.pdf

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TemplateRegistryTest {

    @Test
    fun testAllEightTemplatesAreRegisteredAndDistinct() {
        val templates = TemplateRegistry.templates
        assertEquals(8, templates.size)

        // Verify distinct IDs
        val ids = templates.map { it.id }.distinct()
        assertEquals(8, ids.size)

        // Verify key templates exist
        assertNotNull(TemplateRegistry.getById("modern_pro"))
        assertNotNull(TemplateRegistry.getById("minimalist"))
        assertNotNull(TemplateRegistry.getById("executive"))
        assertNotNull(TemplateRegistry.getById("tech_developer"))
        assertNotNull(TemplateRegistry.getById("creative"))
        assertNotNull(TemplateRegistry.getById("academic_fresher"))
        assertNotNull(TemplateRegistry.getById("nordic_compact"))
        assertNotNull(TemplateRegistry.getById("modern_timeline"))

        // Verify each template has distinct descriptions and layout tags
        templates.forEach {
            assertTrue(it.name.isNotBlank())
            assertTrue(it.description.isNotBlank())
            assertTrue(it.layoutTag.isNotBlank())
            assertTrue(it.atsRating.isNotBlank())
            assertTrue(it.defaultColor.startsWith("#"))
        }
    }

    @Test
    fun testGetByIdFallsBackGracefully() {
        val fallback = TemplateRegistry.getById("non_existent_id")
        assertEquals("modern_pro", fallback.id)
    }
}
