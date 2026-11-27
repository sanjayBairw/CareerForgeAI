package com.pynexia.resumebuilder.pdf

import com.pynexia.resumebuilder.data.model.ResumeData

object TextResumeExporter {

    fun export(resume: ResumeData): String {
        val sb = StringBuilder()
        val info = resume.personalInfo

        // Header
        if (info.fullName.isNotBlank()) sb.appendLine(info.fullName.uppercase())
        if (info.headline.isNotBlank()) sb.appendLine(info.headline)

        val contacts = listOfNotNull(
            info.email.takeIf { it.isNotBlank() },
            info.phone.takeIf { it.isNotBlank() },
            info.location.takeIf { it.isNotBlank() },
            info.linkedInUrl.takeIf { it.isNotBlank() },
            info.githubUrl.takeIf { it.isNotBlank() },
            info.portfolioUrl.takeIf { it.isNotBlank() }
        )
        if (contacts.isNotEmpty()) {
            sb.appendLine(contacts.joinToString(" | "))
        }
        sb.appendLine()

        // Summary
        if (resume.summary.isNotBlank()) {
            sb.appendLine("PROFESSIONAL SUMMARY")
            sb.appendLine("-------------------")
            sb.appendLine(resume.summary.trim())
            sb.appendLine()
        }

        // Work Experience
        if (resume.experienceList.isNotEmpty()) {
            sb.appendLine("WORK EXPERIENCE")
            sb.appendLine("---------------")
            resume.experienceList.forEach { exp ->
                val dates = if (exp.startDate.isNotBlank() || exp.endDate.isNotBlank()) " (${exp.startDate} - ${exp.endDate})" else ""
                val loc = if (exp.location.isNotBlank()) " | ${exp.location}" else ""
                sb.appendLine("${exp.jobTitle} - ${exp.company}$loc$dates")
                val textContent = listOfNotNull(
                    exp.responsibilities.takeIf { it.isNotBlank() },
                    exp.measurableResults.takeIf { it.isNotBlank() }
                ).joinToString("\n")

                if (textContent.isNotBlank()) {
                    textContent.lines().forEach { line ->
                        val trimmed = line.trim()
                        if (trimmed.isNotBlank()) {
                            val bullet = if (trimmed.startsWith("•") || trimmed.startsWith("-")) trimmed else "• $trimmed"
                            sb.appendLine("  $bullet")
                        }
                    }
                }
                sb.appendLine()
            }
        }

        // Education
        if (resume.educationList.isNotEmpty()) {
            sb.appendLine("EDUCATION")
            sb.appendLine("---------")
            resume.educationList.forEach { edu ->
                val dates = if (edu.startDate.isNotBlank() || edu.endDate.isNotBlank()) " (${edu.startDate} - ${edu.endDate})" else ""
                val grade = if (edu.grade.isNotBlank()) " | ${edu.grade}" else ""
                sb.appendLine("${edu.degree} in ${edu.fieldOfStudy} - ${edu.institution}$grade$dates")
                if (edu.achievements.isNotBlank()) {
                    sb.appendLine("  • Achievements: ${edu.achievements}")
                }
                sb.appendLine()
            }
        }

        // Skills
        if (resume.skillsList.isNotEmpty()) {
            sb.appendLine("KEY SKILLS")
            sb.appendLine("----------")
            val skillsGrouped = resume.skillsList.groupBy { it.category.ifBlank { "Core Competencies" } }
            skillsGrouped.forEach { (category, skills) ->
                sb.appendLine("$category: ${skills.joinToString(", ") { it.name }}")
            }
            sb.appendLine()
        }

        // Projects
        if (resume.projectsList.isNotEmpty()) {
            sb.appendLine("PROJECTS")
            sb.appendLine("--------")
            resume.projectsList.forEach { proj ->
                val tech = if (proj.techStack.isNotBlank()) " [${proj.techStack}]" else ""
                sb.appendLine("${proj.title}$tech")
                if (proj.description.isNotBlank()) {
                    sb.appendLine("  • ${proj.description.trim()}")
                }
                if (proj.liveUrl.isNotBlank()) sb.appendLine("  • Link: ${proj.liveUrl}")
                sb.appendLine()
            }
        }

        // Certifications
        if (resume.certificationsList.isNotEmpty()) {
            sb.appendLine("CERTIFICATIONS")
            sb.appendLine("--------------")
            resume.certificationsList.forEach { cert ->
                val org = if (cert.issuingOrganization.isNotBlank()) " - ${cert.issuingOrganization}" else ""
                val date = if (cert.issueDate.isNotBlank()) " (${cert.issueDate})" else ""
                sb.appendLine("• ${cert.name}$org$date")
            }
            sb.appendLine()
        }

        return sb.toString().trim()
    }
}
