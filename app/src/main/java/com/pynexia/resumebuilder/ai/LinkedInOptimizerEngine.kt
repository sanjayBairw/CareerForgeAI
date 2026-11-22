package com.pynexia.resumebuilder.ai

import com.pynexia.resumebuilder.data.model.ResumeData

data class LinkedInProfileContent(
    val headlines: List<String>,
    val aboutSection: String,
    val featuredSkillsList: List<String>,
    val experienceBlurbs: List<String>
)

class LinkedInOptimizerEngine {

    fun optimize(resume: ResumeData): LinkedInProfileContent {
        val role = resume.targetJobTitle.ifBlank { "Senior Professional" }
        val name = resume.personalInfo.fullName.ifBlank { "Candidate" }
        val topSkills = resume.skillsList.take(5).map { it.name }

        val headlines = listOf(
            "$role | Specializing in ${topSkills.take(3).joinToString(" • ")} | Building High-Impact Solutions",
            "$role @ Driving Scalable Systems, Product Velocity & Digital Transformation",
            "Passionate $role | Architecture • Distributed Systems • Team Leadership"
        )

        val aboutSb = StringBuilder()
        aboutSb.append("👋 Hi! I'm $name, a $role focused on building high-performance, user-centric systems.\n\n")
        if (resume.summary.isNotBlank()) {
            aboutSb.append(resume.summary).append("\n\n")
        }
        aboutSb.append("🚀 Core Competencies:\n")
        for (skill in topSkills) {
            aboutSb.append("• ").append(skill).append("\n")
        }
        aboutSb.append("\n📫 Let's connect or discuss high-scale engineering opportunities!")

        val blurbs = resume.experienceList.map { exp ->
            "At ${exp.company} as ${exp.jobTitle}:\n${exp.responsibilities}\nKey Impact: ${exp.measurableResults}"
        }

        return LinkedInProfileContent(
            headlines = headlines,
            aboutSection = aboutSb.toString(),
            featuredSkillsList = topSkills,
            experienceBlurbs = blurbs
        )
    }
}
