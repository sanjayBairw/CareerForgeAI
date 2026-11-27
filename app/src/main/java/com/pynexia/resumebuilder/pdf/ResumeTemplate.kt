package com.pynexia.resumebuilder.pdf

data class ResumeTemplate(
    val id: String,
    val name: String,
    val category: String,
    val description: String,
    val isTwoColumn: Boolean,
    val atsRating: String,
    val defaultColor: String,
    val layoutTag: String = "2-Col Sidebar"
)

object TemplateRegistry {
    val templates = listOf(
        ResumeTemplate(
            id = "modern_pro",
            name = "Modern Professional",
            category = "Popular",
            description = "Balanced 2-column layout with clean accent sidebar, monogram badge, and structured career timeline. Perfect for corporate and tech roles.",
            isTwoColumn = true,
            atsRating = "95% ATS Optimized",
            defaultColor = "#2563EB",
            layoutTag = "2-Col Sidebar"
        ),
        ResumeTemplate(
            id = "minimalist",
            name = "Minimalist Clean",
            category = "ATS Priority",
            description = "Single-column linear format with crisp hairline dividers and standardized headers. Engineered for 100% compliance with automated ATS parsers.",
            isTwoColumn = false,
            atsRating = "99% ATS Optimized",
            defaultColor = "#0F172A",
            layoutTag = "1-Col ATS Standard"
        ),
        ResumeTemplate(
            id = "executive",
            name = "Corporate Executive",
            category = "Leadership",
            description = "Distinguished full-width executive header banner with authoritative serif typography and strategic outcomes highlight box.",
            isTwoColumn = false,
            atsRating = "93% ATS Optimized",
            defaultColor = "#059669",
            layoutTag = "Executive Banner"
        ),
        ResumeTemplate(
            id = "tech_developer",
            name = "Software Developer",
            category = "Engineering",
            description = "Engineered specifically for developers with a categorized Technical Skills Matrix, tech stack chips [Kotlin, Room], and GitHub repo links.",
            isTwoColumn = false,
            atsRating = "96% ATS Optimized",
            defaultColor = "#2563EB",
            layoutTag = "Technical Matrix"
        ),
        ResumeTemplate(
            id = "creative",
            name = "Creative Portfolio",
            category = "Creative & Product",
            description = "Modern asymmetric split layout with colored pill skill tags, portfolio showcase cards, and contemporary design hierarchy.",
            isTwoColumn = true,
            atsRating = "91% ATS Optimized",
            defaultColor = "#7C3AED",
            layoutTag = "Asymmetric Pill Grid"
        ),
        ResumeTemplate(
            id = "academic_fresher",
            name = "Academic & Fresher",
            category = "Students & Entry-Level",
            description = "Prioritizes Education, GPA, Capstone Projects, and Certifications at the top. Tailored for graduates, interns, and career switchers.",
            isTwoColumn = false,
            atsRating = "97% ATS Optimized",
            defaultColor = "#D97706",
            layoutTag = "Education First"
        ),
        ResumeTemplate(
            id = "nordic_compact",
            name = "Nordic Clean",
            category = "Compact 1-Page",
            description = "Scandinavian-inspired dual column layout with a clean vertical hairline guide and micro-spacing designed to fit extensive data on a single page.",
            isTwoColumn = true,
            atsRating = "94% ATS Optimized",
            defaultColor = "#0284C7",
            layoutTag = "Nordic Dual-Column"
        ),
        ResumeTemplate(
            id = "modern_timeline",
            name = "Chronological Timeline",
            category = "Visual Impact",
            description = "Visual career timeline with connected milestone nodes tracking progression across companies, roles, and education history.",
            isTwoColumn = false,
            atsRating = "92% ATS Optimized",
            defaultColor = "#4F46E5",
            layoutTag = "Milestone Timeline"
        )
    )

    fun getById(id: String): ResumeTemplate {
        return templates.firstOrNull { it.id == id } ?: templates[0]
    }
}
