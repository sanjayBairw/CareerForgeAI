package com.pynexia.resumebuilder.ai

import com.pynexia.resumebuilder.data.model.ResumeData

data class LinkedInOptimizationResult(
    val headlines: List<HeadlineOption>,
    val aboutSummary: String,
    val topKeywords: List<String>
)

data class HeadlineOption(
    val category: String,
    val text: String,
    val description: String
)

class LinkedInOptimizerEngine {

    fun generateOptimization(resume: ResumeData, targetRoleOverride: String = ""): LinkedInOptimizationResult {
        val role = targetRoleOverride.ifBlank {
            resume.personalInfo.headline.ifBlank {
                resume.targetJobTitle.ifBlank { "Software Engineer & Tech Professional" }
            }
        }
        val name = resume.personalInfo.fullName.ifBlank { "Professional" }
        val topSkills = resume.skillsList.take(5).map { it.name }
        val skillsStr = if (topSkills.isNotEmpty()) topSkills.joinToString(" • ") else "Cloud • Architecture • System Design"

        val metricHighlight = resume.experienceList.firstOrNull { it.measurableResults.isNotBlank() }?.measurableResults
            ?: "Delivering scalable high-impact solutions"

        val company = resume.experienceList.firstOrNull()?.company ?: "Top Tech"

        // 1. Generate 4 High-Impact Headlines
        val headlines = listOf(
            HeadlineOption(
                category = "🎯 SEO & Recruiter-Focused",
                text = "$role | $skillsStr | Scaling Robust Solutions",
                description = "Maximum keyword density to rank #1 in recruiter LinkedIn searches."
            ),
            HeadlineOption(
                category = "🏆 Executive & Leadership",
                text = "$role | Ex-$company | Leading High-Performing Teams & Architecture",
                description = "Emphasizes leadership authority, company pedigree, and strategic ownership."
            ),
            HeadlineOption(
                category = "📈 Metric & Impact-Driven",
                text = "$role | $metricHighlight | Passionate about Product Excellence",
                description = "Hooks recruiters with proven quantifiable business and technical results."
            ),
            HeadlineOption(
                category = "💡 Creative & Visionary",
                text = "Building the Next Generation of Tech Products as a $role | $skillsStr",
                description = "Narrative style designed to stand out in feeds and founder searches."
            )
        )

        // 2. Generate Engaging LinkedIn About Summary
        val summaryBuilder = StringBuilder()
        summaryBuilder.append("👋 Hi, I'm $name — a dedicated $role focused on building high-performance, scalable products.\n\n")

        if (resume.summary.isNotBlank()) {
            summaryBuilder.append("${resume.summary}\n\n")
        } else {
            summaryBuilder.append("With extensive background across modern development, I bridge product vision with robust engineering execution to deliver exceptional user experiences.\n\n")
        }

        summaryBuilder.append("🚀 Core Focus & Competencies:\n")
        if (topSkills.isNotEmpty()) {
            topSkills.forEach { skill ->
                summaryBuilder.append("• $skill: Applied expertise across production systems\n")
            }
        } else {
            summaryBuilder.append("• System Architecture & Clean Code\n• Full Lifecycle Product Engineering\n• Cross-Functional Agile Leadership\n")
        }

        if (resume.experienceList.isNotEmpty()) {
            summaryBuilder.append("\n💼 Proven Career Impact:\n")
            resume.experienceList.take(2).forEach { exp ->
                val outcome = if (exp.measurableResults.isNotBlank()) exp.measurableResults else exp.responsibilities.take(80)
                summaryBuilder.append("• At ${exp.company} (${exp.jobTitle}): $outcome\n")
            }
        }

        summaryBuilder.append("\n📫 Let's Connect!\nI'm always open to discussing new opportunities, architecture paradigms, and innovative technology. Feel free to connect or reach me at ${resume.personalInfo.email.ifBlank { "via LinkedIn message" }}.")

        // 3. Generate Searchable Industry Tags
        val keywords = mutableListOf<String>()
        val baseWords = role.split(" ", "/", "&", "-").filter { it.length > 2 }
        baseWords.forEach { word ->
            keywords.add("#${word.capitalize()}")
        }
        topSkills.forEach { s ->
            keywords.add("#${s.replace(" ", "").replace(".", "")}")
        }
        keywords.addAll(listOf("#CareerGrowth", "#Innovation", "#Leadership", "#OpenToWork", "#TechTalent"))

        val distinctTags = keywords.distinct().take(10)

        return LinkedInOptimizationResult(
            headlines = headlines,
            aboutSummary = summaryBuilder.toString(),
            topKeywords = distinctTags
        )
    }

    private fun String.capitalize(): String {
        return this.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    }
}
