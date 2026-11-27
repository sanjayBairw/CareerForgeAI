package com.pynexia.resumebuilder.ai

import com.pynexia.resumebuilder.data.model.EducationItem
import com.pynexia.resumebuilder.data.model.ExperienceItem
import com.pynexia.resumebuilder.data.model.PersonalInfo
import com.pynexia.resumebuilder.data.model.ResumeData
import com.pynexia.resumebuilder.data.model.SkillItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AtsAnalyzerTest {

    private lateinit var analyzer: AtsAnalyzer

    @Before
    fun setUp() {
        analyzer = AtsAnalyzer()
    }

    @Test
    fun testEmptyResumeScoresLow() {
        val emptyResume = ResumeData()
        val result = analyzer.analyze(emptyResume)

        assertTrue("Empty resume should score below 30", result.overallScore < 30)
        assertTrue("Recommendations should contain email warning",
            result.recommendations.any { it.title.contains("Email", ignoreCase = true) })
    }

    @Test
    fun testCompleteResumeYieldsHighScore() {
        val completeResume = ResumeData(
            personalInfo = PersonalInfo(
                fullName = "Jane Doe",
                headline = "Senior Software Architect",
                email = "jane.doe@example.com",
                phone = "+1 555-123-4567",
                location = "Seattle, WA",
                linkedInUrl = "https://linkedin.com/in/janedoe"
            ),
            summary = "Results-driven Senior Software Architect with 8+ years designing cloud microservices and high-throughput backend systems. Spearheaded architectural transformation improving database latency by 35% and scaling platform to 5M daily active users.",
            experienceList = mutableListOf(
                ExperienceItem(
                    company = "Cloud Corp",
                    jobTitle = "Staff Engineer",
                    startDate = "2020",
                    endDate = "Present",
                    responsibilities = "Architected distributed caching engine and orchestrated microservices.",
                    measurableResults = "Reduced p99 latency by 40% and cut cloud costs by 25% for 2M users."
                )
            ),
            educationList = mutableListOf(
                EducationItem(
                    institution = "University of Washington",
                    degree = "B.S.",
                    fieldOfStudy = "Computer Science",
                    startDate = "2014",
                    endDate = "2018",
                    grade = "3.85 GPA"
                )
            ),
            skillsList = mutableListOf(
                SkillItem(name = "Kotlin"),
                SkillItem(name = "Kubernetes"),
                SkillItem(name = "System Design"),
                SkillItem(name = "PostgreSQL")
            )
        )

        val result = analyzer.analyze(completeResume)

        assertTrue("Complete resume should have score >= 80", result.overallScore >= 80)
        assertTrue("Quantified metrics count should be at least 2", result.quantifiedMetricsCount >= 2)
    }

    @Test
    fun testKeywordMatchingAgainstJobDescription() {
        val resume = ResumeData(
            summary = "Experienced in Kotlin, Coroutines, and Room database architecture for scalable mobile apps.",
            skillsList = mutableListOf(
                SkillItem(name = "Kotlin"),
                SkillItem(name = "Room")
            )
        )

        val jobDescription = "Looking for a mobile engineer skilled in Kotlin, Room, GraphQL, and Jetpack Compose."

        val result = analyzer.analyze(resume, jobDescription)

        assertTrue("Should detect Kotlin as matched keyword",
            result.matchedKeywords.any { it.equals("kotlin", ignoreCase = true) })
        assertTrue("Should detect Room as matched keyword",
            result.matchedKeywords.any { it.equals("room", ignoreCase = true) })
        assertTrue("Should detect GraphQL as missing keyword",
            result.missingKeywords.any { it.equals("graphql", ignoreCase = true) })
    }
}
