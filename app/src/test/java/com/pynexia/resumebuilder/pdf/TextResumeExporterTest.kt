package com.pynexia.resumebuilder.pdf

import com.pynexia.resumebuilder.data.model.EducationItem
import com.pynexia.resumebuilder.data.model.ExperienceItem
import com.pynexia.resumebuilder.data.model.PersonalInfo
import com.pynexia.resumebuilder.data.model.ResumeData
import com.pynexia.resumebuilder.data.model.SkillItem
import org.junit.Assert.assertTrue
import org.junit.Test

class TextResumeExporterTest {

    @Test
    fun testExportFormatsAllSectionsCorrectly() {
        val resume = ResumeData(
            personalInfo = PersonalInfo(
                fullName = "John Smith",
                headline = "Lead Android Architect",
                email = "john@example.com",
                phone = "555-987-6543",
                location = "Austin, TX",
                linkedInUrl = "https://linkedin.com/in/johnsmith"
            ),
            summary = "Experienced architect specializing in modern Android platforms.",
            experienceList = mutableListOf(
                ExperienceItem(
                    company = "Tech Dynamics",
                    jobTitle = "Principal Engineer",
                    startDate = "2021",
                    endDate = "2024",
                    responsibilities = "Delivered high-performance apps."
                )
            ),
            educationList = mutableListOf(
                EducationItem(
                    institution = "MIT",
                    degree = "B.S.",
                    fieldOfStudy = "Computer Science"
                )
            ),
            skillsList = mutableListOf(
                SkillItem(name = "Kotlin", category = "Languages"),
                SkillItem(name = "Coroutines", category = "Frameworks")
            )
        )

        val output = TextResumeExporter.export(resume)

        assertTrue("Output should contain user name in uppercase", output.contains("JOHN SMITH"))
        assertTrue("Output should contain headline", output.contains("Lead Android Architect"))
        assertTrue("Output should contain email and phone", output.contains("john@example.com") && output.contains("555-987-6543"))
        assertTrue("Output should contain professional summary section", output.contains("PROFESSIONAL SUMMARY"))
        assertTrue("Output should contain work experience section", output.contains("WORK EXPERIENCE"))
        assertTrue("Output should contain education section", output.contains("EDUCATION"))
        assertTrue("Output should contain key skills section", output.contains("KEY SKILLS"))
    }
}
