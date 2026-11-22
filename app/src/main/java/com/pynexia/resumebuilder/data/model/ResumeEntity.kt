package com.pynexia.resumebuilder.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "resumes")
data class ResumeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    var title: String = "My Professional Resume",
    var targetJobTitle: String = "Software Engineer",
    var targetIndustry: String = "Technology",
    var templateId: String = "modern_pro", // modern_pro, minimalist, executive, creative, tech_developer, academic_fresher
    var colorTheme: String = "#2563EB",
    var fontFamily: String = "roboto",     // roboto, serif, monospace
    var fontSizeScale: Float = 1.0f,
    var layoutColumns: Int = 2,
    var pageMargin: Int = 24,
    var createdAt: Long = System.currentTimeMillis(),
    var updatedAt: Long = System.currentTimeMillis(),
    var lastAtsScore: Int = 78,
    var isArchived: Boolean = false,
    var versionNumber: Int = 1,
    var personalInfoJson: String = "{}",
    var summary: String = "",
    var educationListJson: String = "[]",
    var experienceListJson: String = "[]",
    var skillsListJson: String = "[]",
    var projectsListJson: String = "[]",
    var certificationsListJson: String = "[]",
    var additionalListJson: String = "[]",
    var sectionConfigJson: String = "[]",
    var qrCodeContent: String = ""
)
