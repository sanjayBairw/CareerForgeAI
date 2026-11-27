package com.pynexia.resumebuilder.ai

import com.pynexia.resumebuilder.data.model.ExperienceItem
import com.pynexia.resumebuilder.data.model.PersonalInfo
import com.pynexia.resumebuilder.data.model.ResumeData
import com.pynexia.resumebuilder.data.model.SkillItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LinkedInOptimizerEngineTest {

    private val engine = LinkedInOptimizerEngine()

    @Test
    fun testGeneratesFourDistinctHeadlines() {
        val resume = ResumeData(
            targetJobTitle = "Staff Android Engineer",
            personalInfo = PersonalInfo(
                fullName = "Sanjay Kumar",
                headline = "Senior Mobile Architect",
                email = "sanjay@example.com"
            ),
            skillsList = mutableListOf(
                SkillItem(name = "Kotlin"),
                SkillItem(name = "Jetpack Compose"),
                SkillItem(name = "Architecture")
            ),
            experienceList = mutableListOf(
                ExperienceItem(
                    jobTitle = "Lead Mobile Architect",
                    company = "FinTech Global",
                    measurableResults = "Scaled app to 5M+ active users with 99.9% crash-free sessions"
                )
            )
        )

        val result = engine.generateOptimization(resume)

        assertEquals(4, result.headlines.size)
        assertTrue(result.headlines[0].category.contains("SEO"))
        assertTrue(result.headlines[1].category.contains("Executive"))
        assertTrue(result.headlines[2].category.contains("Metric"))
        assertTrue(result.headlines[3].category.contains("Creative"))

        // Assert all headlines are non-empty and unique
        val uniqueHeadlines = result.headlines.map { it.text }.distinct()
        assertEquals(4, uniqueHeadlines.size)

        // Verify About Summary includes candidate name and core competencies
        assertTrue(result.aboutSummary.contains("Sanjay Kumar"))
        assertTrue(result.aboutSummary.contains("Kotlin"))
        assertTrue(result.aboutSummary.contains("FinTech Global"))

        // Verify Keywords
        assertFalse(result.topKeywords.isEmpty())
        assertTrue(result.topKeywords.any { it.contains("Kotlin", ignoreCase = true) })
    }
}
