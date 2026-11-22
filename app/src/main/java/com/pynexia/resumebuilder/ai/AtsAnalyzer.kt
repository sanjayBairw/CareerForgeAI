package com.pynexia.resumebuilder.ai

import com.pynexia.resumebuilder.data.model.ResumeData

data class AtsRecommendation(
    val title: String,
    val description: String,
    val severity: String // CRITICAL, HIGH, MEDIUM, LOW
)

data class AtsAnalysisResult(
    val overallScore: Int, // 0 - 100
    val rating: String,    // Elite, Strong Match, Needs Optimization, Critical Issues
    val matchedKeywords: List<String>,
    val missingKeywords: List<String>,
    val sectionScores: Map<String, Int>,
    val recommendations: List<AtsRecommendation>,
    val readabilityScore: Int,
    val quantifiedMetricsCount: Int,
    val disclaimer: String = "ATS evaluation scores are diagnostic indicators based on industry heuristics and do not guarantee employer interview callbacks or hiring decisions."
)

class AtsAnalyzer {

    private val commonStopWords = setOf(
        "the", "and", "or", "in", "at", "for", "with", "a", "an", "to", "of", "on", "by", "as",
        "is", "are", "was", "were", "be", "this", "that", "from", "which", "will", "our", "you",
        "your", "we", "all", "can", "has", "have", "had", "been", "work", "job", "candidate",
        "experience", "years", "role", "looking", "team", "strong", "skills", "ability", "required"
    )

    private val powerActionVerbs = listOf(
        "architected", "engineered", "spearheaded", "orchestrated", "developed",
        "optimized", "deployed", "implemented", "scaled", "streamlined", "accelerated",
        "designed", "reduced", "increased", "transformed", "directed", "formulated"
    )

    fun analyze(resume: ResumeData, jobDescription: String = ""): AtsAnalysisResult {
        var scoreAccumulator = 0
        val sectionScores = mutableMapOf<String, Int>()
        val recommendations = mutableListOf<AtsRecommendation>()

        // 1. Contact Information Audit (Max 15 pts)
        var contactScore = 0
        val personal = resume.personalInfo
        if (personal.fullName.isNotBlank()) contactScore += 3
        if (android.util.Patterns.EMAIL_ADDRESS.matcher(personal.email).matches()) {
            contactScore += 4
        } else {
            recommendations.add(AtsRecommendation("Valid Email Required", "Add a professionally formatted email address for ATS parser verification.", "CRITICAL"))
        }
        if (personal.phone.isNotBlank() && personal.phone.length >= 7) {
            contactScore += 3
        } else {
            recommendations.add(AtsRecommendation("Contact Phone Number Missing", "A valid phone number is essential for recruiter outreach.", "HIGH"))
        }
        if (personal.location.isNotBlank()) contactScore += 2
        if (personal.linkedInUrl.isNotBlank() || personal.githubUrl.isNotBlank()) {
            contactScore += 3
        } else {
            recommendations.add(AtsRecommendation("Add Professional Profiles", "Including your LinkedIn or GitHub profile link increases recruiter verification rates by 40%.", "MEDIUM"))
        }
        sectionScores["Contact Info"] = (contactScore * 100 / 15).coerceIn(0, 100)
        scoreAccumulator += contactScore // out of 15

        // 2. Professional Summary Audit (Max 15 pts)
        var summaryScore = 0
        val summary = resume.summary.trim()
        val summaryWordCount = if (summary.isBlank()) 0 else summary.split(Regex("\\s+")).size
        when {
            summaryWordCount in 40..120 -> {
                summaryScore = 15
            }
            summaryWordCount > 120 -> {
                summaryScore = 10
                recommendations.add(AtsRecommendation("Summary Too Verbose", "Aim for 3-5 concise, impactful sentences (40-90 words) to hold recruiter attention.", "LOW"))
            }
            summaryWordCount in 10..39 -> {
                summaryScore = 8
                recommendations.add(AtsRecommendation("Expand Professional Summary", "Add your core specializations and top accomplishments to give immediate context.", "MEDIUM"))
            }
            else -> {
                summaryScore = 0
                recommendations.add(AtsRecommendation("Missing Professional Summary", "A summary section is parsed by ATS as your primary career thesis.", "HIGH"))
            }
        }
        sectionScores["Summary"] = (summaryScore * 100 / 15).coerceIn(0, 100)
        scoreAccumulator += summaryScore // out of 15

        // 3. Experience & Measurable Metrics Audit (Max 30 pts)
        var expScore = 0
        var quantifiedCount = 0
        var actionVerbCount = 0

        if (resume.experienceList.isNotEmpty()) {
            expScore += 10
            for (exp in resume.experienceList) {
                val fullText = "${exp.responsibilities} ${exp.measurableResults}".lowercase()
                // Check action verbs
                for (verb in powerActionVerbs) {
                    if (fullText.contains(verb)) actionVerbCount++
                }
                // Check numbers, percentages, currency metrics
                val metricRegex = Regex("(\\d+%|\\$\\d+[kKmMbB]?|\\d+\\+?\\s*(users|clients|projects|ms|sec|x|times))")
                if (metricRegex.containsMatchIn(fullText)) {
                    quantifiedCount++
                }
            }

            if (actionVerbCount >= 3) {
                expScore += 10
            } else {
                expScore += 5
                recommendations.add(AtsRecommendation("Use Action Verbs", "Begin experience bullet points with strong verbs (e.g., Engineered, Spearheaded, Accelerated).", "MEDIUM"))
            }

            if (quantifiedCount >= 2) {
                expScore += 10
            } else {
                expScore += 4
                recommendations.add(AtsRecommendation("Add Quantifiable Metrics", "Incorporate metrics like % efficiency gains, $ saved, or user scale to prove measurable impact.", "HIGH"))
            }
        } else {
            recommendations.add(AtsRecommendation("Experience Section Empty", "Add your professional roles or internships to establish relevant career history.", "CRITICAL"))
        }
        sectionScores["Work Experience"] = (expScore * 100 / 30).coerceIn(0, 100)
        scoreAccumulator += expScore // out of 30

        // 4. Skills & Keywords Match (Max 25 pts)
        var skillsScore = 0
        val resumeSkills = resume.skillsList.map { it.name.trim().lowercase() }.filter { it.isNotBlank() }
        val resumeFullText = buildFullResumeText(resume).lowercase()

        val matchedKeywords = mutableListOf<String>()
        val missingKeywords = mutableListOf<String>()

        if (jobDescription.isNotBlank()) {
            val jdKeywords = extractKeywordsFromText(jobDescription)
            for (kw in jdKeywords) {
                if (resumeFullText.contains(kw) || resumeSkills.contains(kw)) {
                    matchedKeywords.add(kw)
                } else {
                    missingKeywords.add(kw)
                }
            }

            val matchRatio = if (jdKeywords.isNotEmpty()) {
                matchedKeywords.size.toFloat() / jdKeywords.size.toFloat()
            } else 0.8f

            skillsScore = (matchRatio * 25).toInt().coerceIn(5, 25)
            if (missingKeywords.isNotEmpty()) {
                recommendations.add(
                    AtsRecommendation(
                        "Target Job Description Keywords Missing",
                        "Consider naturally adding keywords: ${missingKeywords.take(5).joinToString(", ")} to your skills or experience.",
                        "HIGH"
                    )
                )
            }
        } else {
            // General skills evaluation
            when {
                resumeSkills.size >= 8 -> skillsScore = 25
                resumeSkills.size in 4..7 -> {
                    skillsScore = 18
                    recommendations.add(AtsRecommendation("Expand Core Skills", "Include 8-12 relevant technical and soft skills to improve ATS keyword density.", "MEDIUM"))
                }
                resumeSkills.isNotEmpty() -> {
                    skillsScore = 10
                    recommendations.add(AtsRecommendation("Low Skill Count", "Add more technical competencies matching your target job title.", "HIGH"))
                }
                else -> {
                    skillsScore = 0
                    recommendations.add(AtsRecommendation("Skills Section Empty", "Add technical and domain-specific skills for ATS matching.", "CRITICAL"))
                }
            }
        }
        sectionScores["Skills & Keywords"] = (skillsScore * 100 / 25).coerceIn(0, 100)
        scoreAccumulator += skillsScore // out of 25

        // 5. Education & Credentials (Max 15 pts)
        var eduScore = 0
        if (resume.educationList.isNotEmpty()) {
            eduScore += 10
            val firstEdu = resume.educationList[0]
            if (firstEdu.degree.isNotBlank() && firstEdu.institution.isNotBlank()) {
                eduScore += 5
            }
        } else {
            recommendations.add(AtsRecommendation("Education Missing", "List your academic degrees or relevant training programs.", "HIGH"))
        }
        sectionScores["Education"] = (eduScore * 100 / 15).coerceIn(0, 100)
        scoreAccumulator += eduScore // out of 15

        val finalScore = scoreAccumulator.coerceIn(10, 100)
        val rating = when {
            finalScore >= 90 -> "Elite ATS Alignment"
            finalScore >= 75 -> "Strong ATS Match"
            finalScore >= 60 -> "Moderately Aligned"
            else -> "Needs Optimization"
        }

        // Readability heuristic (clean layout, bullet length)
        val readability = if (resume.summary.length > 500) 70 else 92

        return AtsAnalysisResult(
            overallScore = finalScore,
            rating = rating,
            matchedKeywords = matchedKeywords.distinct().take(15),
            missingKeywords = missingKeywords.distinct().take(15),
            sectionScores = sectionScores,
            recommendations = recommendations,
            readabilityScore = readability,
            quantifiedMetricsCount = quantifiedCount
        )
    }

    private fun buildFullResumeText(resume: ResumeData): String {
        val sb = StringBuilder()
        sb.append(resume.personalInfo.fullName).append(" ")
        sb.append(resume.personalInfo.headline).append(" ")
        sb.append(resume.targetJobTitle).append(" ")
        sb.append(resume.summary).append(" ")
        for (exp in resume.experienceList) {
            sb.append(exp.company).append(" ")
            sb.append(exp.jobTitle).append(" ")
            sb.append(exp.responsibilities).append(" ")
            sb.append(exp.measurableResults).append(" ")
        }
        for (edu in resume.educationList) {
            sb.append(edu.degree).append(" ")
            sb.append(edu.fieldOfStudy).append(" ")
            sb.append(edu.institution).append(" ")
        }
        for (skill in resume.skillsList) {
            sb.append(skill.name).append(" ")
        }
        for (proj in resume.projectsList) {
            sb.append(proj.title).append(" ")
            sb.append(proj.techStack).append(" ")
            sb.append(proj.description).append(" ")
        }
        return sb.toString()
    }

    private fun extractKeywordsFromText(text: String): List<String> {
        val words = text.lowercase()
            .replace(Regex("[^a-z0-9+#/\\s-]"), " ")
            .split(Regex("\\s+"))
            .filter { it.length > 2 && !commonStopWords.contains(it) }

        val wordCounts = mutableMapOf<String, Int>()
        for (word in words) {
            wordCounts[word] = (wordCounts[word] ?: 0) + 1
        }
        return wordCounts.entries.sortedByDescending { it.value }.take(20).map { it.key }
    }
}
