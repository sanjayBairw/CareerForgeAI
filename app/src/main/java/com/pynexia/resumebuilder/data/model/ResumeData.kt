package com.pynexia.resumebuilder.data.model

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

data class ResumeData(
    var id: Long = 0,
    var title: String = "My Professional Resume",
    var targetJobTitle: String = "Software Engineer",
    var targetIndustry: String = "Technology",
    var templateId: String = "modern_pro",
    var colorTheme: String = "#2563EB",
    var fontFamily: String = "roboto",
    var fontSizeScale: Float = 1.0f,
    var layoutColumns: Int = 2,
    var pageMargin: Int = 24,
    var createdAt: Long = System.currentTimeMillis(),
    var updatedAt: Long = System.currentTimeMillis(),
    var lastAtsScore: Int = 78,
    var isArchived: Boolean = false,
    var versionNumber: Int = 1,
    var personalInfo: PersonalInfo = PersonalInfo(),
    var summary: String = "",
    var educationList: MutableList<EducationItem> = mutableListOf(),
    var experienceList: MutableList<ExperienceItem> = mutableListOf(),
    var skillsList: MutableList<SkillItem> = mutableListOf(),
    var projectsList: MutableList<ProjectItem> = mutableListOf(),
    var certificationsList: MutableList<CertificationItem> = mutableListOf(),
    var additionalList: MutableList<AdditionalItem> = mutableListOf(),
    var sectionConfigs: MutableList<SectionConfig> = defaultSectionConfigs(),
    var qrCodeContent: String = ""
) {
    fun toEntity(gson: Gson = Gson()): ResumeEntity {
        return ResumeEntity(
            id = id,
            title = title,
            targetJobTitle = targetJobTitle,
            targetIndustry = targetIndustry,
            templateId = templateId,
            colorTheme = colorTheme,
            fontFamily = fontFamily,
            fontSizeScale = fontSizeScale,
            layoutColumns = layoutColumns,
            pageMargin = pageMargin,
            createdAt = createdAt,
            updatedAt = updatedAt,
            lastAtsScore = lastAtsScore,
            isArchived = isArchived,
            versionNumber = versionNumber,
            personalInfoJson = gson.toJson(personalInfo),
            summary = summary,
            educationListJson = gson.toJson(educationList),
            experienceListJson = gson.toJson(experienceList),
            skillsListJson = gson.toJson(skillsList),
            projectsListJson = gson.toJson(projectsList),
            certificationsListJson = gson.toJson(certificationsList),
            additionalListJson = gson.toJson(additionalList),
            sectionConfigJson = gson.toJson(sectionConfigs),
            qrCodeContent = qrCodeContent
        )
    }

    fun calculateCompletionPercentage(): Int {
        var score = 0
        val total = 7
        if (personalInfo.fullName.isNotBlank() && personalInfo.email.isNotBlank()) score++
        if (summary.isNotBlank()) score++
        if (experienceList.isNotEmpty()) score++
        if (educationList.isNotEmpty()) score++
        if (skillsList.isNotEmpty()) score++
        if (projectsList.isNotEmpty()) score++
        if (certificationsList.isNotEmpty() || additionalList.isNotEmpty()) score++
        return ((score.toDouble() / total) * 100).toInt().coerceIn(10, 100)
    }

    companion object {
        fun defaultSectionConfigs(): MutableList<SectionConfig> {
            return mutableListOf(
                SectionConfig("personal", "Personal Information", true, 0),
                SectionConfig("summary", "Professional Summary", true, 1),
                SectionConfig("experience", "Work Experience", true, 2),
                SectionConfig("education", "Education", true, 3),
                SectionConfig("skills", "Skills & Proficiencies", true, 4),
                SectionConfig("projects", "Key Projects", true, 5),
                SectionConfig("certifications", "Certifications", true, 6),
                SectionConfig("additional", "Additional Sections", true, 7)
            )
        }

        fun fromEntity(entity: ResumeEntity, gson: Gson = Gson()): ResumeData {
            val personalInfo = try {
                gson.fromJson(entity.personalInfoJson, PersonalInfo::class.java) ?: PersonalInfo()
            } catch (e: Exception) {
                PersonalInfo()
            }

            val educationType = object : TypeToken<MutableList<EducationItem>>() {}.type
            val educationList: MutableList<EducationItem> = try {
                gson.fromJson(entity.educationListJson, educationType) ?: mutableListOf()
            } catch (e: Exception) {
                mutableListOf()
            }

            val expType = object : TypeToken<MutableList<ExperienceItem>>() {}.type
            val experienceList: MutableList<ExperienceItem> = try {
                gson.fromJson(entity.experienceListJson, expType) ?: mutableListOf()
            } catch (e: Exception) {
                mutableListOf()
            }

            val skillType = object : TypeToken<MutableList<SkillItem>>() {}.type
            val skillsList: MutableList<SkillItem> = try {
                gson.fromJson(entity.skillsListJson, skillType) ?: mutableListOf()
            } catch (e: Exception) {
                mutableListOf()
            }

            val projType = object : TypeToken<MutableList<ProjectItem>>() {}.type
            val projectsList: MutableList<ProjectItem> = try {
                gson.fromJson(entity.projectsListJson, projType) ?: mutableListOf()
            } catch (e: Exception) {
                mutableListOf()
            }

            val certType = object : TypeToken<MutableList<CertificationItem>>() {}.type
            val certList: MutableList<CertificationItem> = try {
                gson.fromJson(entity.certificationsListJson, certType) ?: mutableListOf()
            } catch (e: Exception) {
                mutableListOf()
            }

            val addType = object : TypeToken<MutableList<AdditionalItem>>() {}.type
            val addList: MutableList<AdditionalItem> = try {
                gson.fromJson(entity.additionalListJson, addType) ?: mutableListOf()
            } catch (e: Exception) {
                mutableListOf()
            }

            val configType = object : TypeToken<MutableList<SectionConfig>>() {}.type
            val configs: MutableList<SectionConfig> = try {
                gson.fromJson(entity.sectionConfigJson, configType) ?: defaultSectionConfigs()
            } catch (e: Exception) {
                defaultSectionConfigs()
            }

            return ResumeData(
                id = entity.id,
                title = entity.title,
                targetJobTitle = entity.targetJobTitle,
                targetIndustry = entity.targetIndustry,
                templateId = entity.templateId,
                colorTheme = entity.colorTheme,
                fontFamily = entity.fontFamily,
                fontSizeScale = entity.fontSizeScale,
                layoutColumns = entity.layoutColumns,
                pageMargin = entity.pageMargin,
                createdAt = entity.createdAt,
                updatedAt = entity.updatedAt,
                lastAtsScore = entity.lastAtsScore,
                isArchived = entity.isArchived,
                versionNumber = entity.versionNumber,
                personalInfo = personalInfo,
                summary = entity.summary,
                educationList = educationList,
                experienceList = experienceList,
                skillsList = skillsList,
                projectsList = projectsList,
                certificationsList = certList,
                additionalList = addList,
                sectionConfigs = if (configs.isEmpty()) defaultSectionConfigs() else configs,
                qrCodeContent = entity.qrCodeContent
            )
        }
    }
}
