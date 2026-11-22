package com.pynexia.resumebuilder.pdf

data class ResumeTemplate(
    val id: String,
    val name: String,
    val category: String,
    val description: String,
    val isTwoColumn: Boolean,
    val atsRating: String,
    val defaultColor: String
)

object TemplateRegistry {
    val templates = listOf(
        ResumeTemplate(
            id = "modern_pro",
            name = "Modern Professional",
            category = "Popular",
            description = "Balanced 2-column layout with clean accent sidebar. Ideal for tech, design, and modern corporate roles.",
            isTwoColumn = true,
            atsRating = "95% ATS Optimized",
            defaultColor = "#2563EB"
        ),
        ResumeTemplate(
            id = "minimalist",
            name = "Minimalist Clean",
            category = "ATS Priority",
            description = "Clean single-column format with crisp typography and subtle dividers. Maximum readability for automated parsers.",
            isTwoColumn = false,
            atsRating = "99% ATS Optimized",
            defaultColor = "#0F172A"
        ),
        ResumeTemplate(
            id = "executive",
            name = "Corporate Executive",
            category = "Leadership",
            description = "Distinguished header banner with serif typography and authoritative structure for senior managers and directors.",
            isTwoColumn = false,
            atsRating = "92% ATS Optimized",
            defaultColor = "#059669"
        ),
        ResumeTemplate(
            id = "creative",
            name = "Creative Portfolio",
            category = "Creative",
            description = "Dynamic split layout with vibrant skill tags and experience timeline bullets for creative and product specialists.",
            isTwoColumn = true,
            atsRating = "90% ATS Optimized",
            defaultColor = "#7C3AED"
        ),
        ResumeTemplate(
            id = "tech_developer",
            name = "Software Developer",
            category = "Engineering",
            description = "Engineered specifically for developers with skills matrix, GitHub repo links, and technical stack chips.",
            isTwoColumn = true,
            atsRating = "96% ATS Optimized",
            defaultColor = "#2563EB"
        ),
        ResumeTemplate(
            id = "academic_fresher",
            name = "Academic & Fresher",
            category = "Students & Freshers",
            description = "Puts Education, Academic Projects, Coursework, and Certifications first. Perfect for students and career switchers.",
            isTwoColumn = false,
            atsRating = "97% ATS Optimized",
            defaultColor = "#D97706"
        )
    )

    fun getById(id: String): ResumeTemplate {
        return templates.firstOrNull { it.id == id } ?: templates[0]
    }
}
