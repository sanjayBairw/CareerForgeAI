package com.pynexia.resumebuilder.pdf

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import com.pynexia.resumebuilder.data.model.ResumeData
import java.io.File
import java.io.FileOutputStream

class PdfGenerator(private val context: Context) {

    // Standard A4 dimensions in PostScript points (72 points/inch)
    private val pageWidth = 595
    private val pageHeight = 842

    fun generatePdf(resume: ResumeData, outputFile: File): File {
        val document = PdfDocument()

        val themeColor = try {
            Color.parseColor(resume.colorTheme)
        } catch (e: Exception) {
            Color.parseColor("#2563EB")
        }

        val baseTypeface = when (resume.fontFamily.lowercase()) {
            "serif" -> Typeface.SERIF
            "monospace" -> Typeface.MONOSPACE
            else -> Typeface.SANS_SERIF
        }

        when (resume.templateId) {
            "minimalist" -> renderMinimalist(document, resume, themeColor, baseTypeface)
            "executive" -> renderExecutive(document, resume, themeColor, baseTypeface)
            "tech_developer" -> renderTechDeveloper(document, resume, themeColor, baseTypeface)
            "creative" -> renderCreative(document, resume, themeColor, baseTypeface)
            "academic_fresher" -> renderAcademicFresher(document, resume, themeColor, baseTypeface)
            "nordic_compact" -> renderNordicCompact(document, resume, themeColor, baseTypeface)
            "modern_timeline" -> renderModernTimeline(document, resume, themeColor, baseTypeface)
            else -> renderModernPro(document, resume, themeColor, baseTypeface)
        }

        FileOutputStream(outputFile).use { out ->
            document.writeTo(out)
        }
        document.close()
        return outputFile
    }

    // ==========================================
    // 1. MODERN PRO (2 Columns with Accent Sidebar)
    // ==========================================
    private fun renderModernPro(
        document: PdfDocument,
        resume: ResumeData,
        themeColor: Int,
        typeface: Typeface
    ) {
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        val sidebarWidth = 185
        val margin = 20

        // Sidebar Background
        val sidebarPaint = Paint().apply {
            color = Color.parseColor("#F1F5F9")
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, sidebarWidth.toFloat(), pageHeight.toFloat(), sidebarPaint)

        // Accent divider
        val dividerPaint = Paint().apply {
            color = themeColor
            strokeWidth = 2f
        }
        canvas.drawLine(sidebarWidth.toFloat(), 0f, sidebarWidth.toFloat(), pageHeight.toFloat(), dividerPaint)

        val namePaint = TextPaint().apply {
            color = themeColor
            textSize = 18f
            this.typeface = Typeface.create(typeface, Typeface.BOLD)
            isAntiAlias = true
        }
        val headlinePaint = TextPaint().apply {
            color = Color.parseColor("#334155")
            textSize = 10f
            this.typeface = Typeface.create(typeface, Typeface.NORMAL)
            isAntiAlias = true
        }
        val sectionHeadingPaint = TextPaint().apply {
            color = themeColor
            textSize = 11f
            this.typeface = Typeface.create(typeface, Typeface.BOLD)
            isAntiAlias = true
        }
        val bodyPaint = TextPaint().apply {
            color = Color.parseColor("#0F172A")
            textSize = 9f
            this.typeface = Typeface.create(typeface, Typeface.NORMAL)
            isAntiAlias = true
        }
        val subPaint = TextPaint().apply {
            color = Color.parseColor("#64748B")
            textSize = 8f
            this.typeface = Typeface.create(typeface, Typeface.NORMAL)
            isAntiAlias = true
        }

        // ================= SIDEBAR CONTENT =================
        var sidebarY = 32f

        val qrBitmap = if (resume.qrCodeContent.isNotBlank()) {
            QrCodeGenerator.generateQrBitmap(resume.qrCodeContent, 85)
        } else null

        if (qrBitmap != null) {
            canvas.drawBitmap(qrBitmap, (sidebarWidth - 85) / 2f, sidebarY, null)
            sidebarY += 92f
            val qrLabel = createStaticLayout("Scan for Portfolio / LinkedIn", subPaint, sidebarWidth - 20)
            canvas.save()
            canvas.translate(10f, sidebarY)
            qrLabel.draw(canvas)
            canvas.restore()
            sidebarY += qrLabel.height + 14f
        } else {
            val badgePaint = Paint().apply {
                color = themeColor
                isAntiAlias = true
            }
            val initials = resume.personalInfo.fullName.split(" ")
                .filter { it.isNotBlank() }
                .take(2)
                .map { it.first().uppercase() }
                .joinToString("")
            canvas.drawCircle(sidebarWidth / 2f, sidebarY + 22f, 22f, badgePaint)
            val initialPaint = Paint().apply {
                color = Color.WHITE
                textSize = 15f
                isAntiAlias = true
                textAlign = Paint.Align.CENTER
                this.typeface = Typeface.create(typeface, Typeface.BOLD)
            }
            canvas.drawText(initials.ifBlank { "CF" }, sidebarWidth / 2f, sidebarY + 28f, initialPaint)
            sidebarY += 56f
        }

        // Contact Info in Sidebar
        canvas.drawText("CONTACT", 16f, sidebarY, sectionHeadingPaint)
        sidebarY += 6f
        canvas.drawLine(16f, sidebarY, (sidebarWidth - 16).toFloat(), sidebarY, dividerPaint)
        sidebarY += 12f

        val personal = resume.personalInfo
        val contactLines = listOfNotNull(
            if (personal.email.isNotBlank()) personal.email else null,
            if (personal.phone.isNotBlank()) personal.phone else null,
            if (personal.location.isNotBlank()) personal.location else null,
            if (personal.linkedInUrl.isNotBlank()) "LinkedIn: ${personal.linkedInUrl.removePrefix("https://").removePrefix("www.")}" else null,
            if (personal.githubUrl.isNotBlank()) "GitHub: ${personal.githubUrl.removePrefix("https://").removePrefix("www.")}" else null,
            if (personal.portfolioUrl.isNotBlank()) "Portfolio: ${personal.portfolioUrl.removePrefix("https://").removePrefix("www.")}" else null
        )

        for (line in contactLines) {
            val layout = createStaticLayout(line, bodyPaint, sidebarWidth - 32)
            canvas.save()
            canvas.translate(16f, sidebarY)
            layout.draw(canvas)
            canvas.restore()
            sidebarY += layout.height + 6f
        }

        sidebarY += 12f

        // Skills in Sidebar
        if (resume.skillsList.isNotEmpty()) {
            canvas.drawText("CORE SKILLS", 16f, sidebarY, sectionHeadingPaint)
            sidebarY += 6f
            canvas.drawLine(16f, sidebarY, (sidebarWidth - 16).toFloat(), sidebarY, dividerPaint)
            sidebarY += 12f

            for (skill in resume.skillsList) {
                canvas.drawText("• ${skill.name} (${skill.level})", 16f, sidebarY, bodyPaint)
                sidebarY += 14f
            }
        }

        sidebarY += 12f

        // Education in Sidebar
        if (resume.educationList.isNotEmpty()) {
            canvas.drawText("EDUCATION", 16f, sidebarY, sectionHeadingPaint)
            sidebarY += 6f
            canvas.drawLine(16f, sidebarY, (sidebarWidth - 16).toFloat(), sidebarY, dividerPaint)
            sidebarY += 12f

            for (edu in resume.educationList) {
                val eduTitle = createStaticLayout("${edu.degree} - ${edu.fieldOfStudy}", sectionHeadingPaint, sidebarWidth - 32)
                canvas.save()
                canvas.translate(16f, sidebarY)
                eduTitle.draw(canvas)
                canvas.restore()
                sidebarY += eduTitle.height + 2f

                val school = createStaticLayout("${edu.institution} (${edu.startDate} - ${edu.endDate})", subPaint, sidebarWidth - 32)
                canvas.save()
                canvas.translate(16f, sidebarY)
                school.draw(canvas)
                canvas.restore()
                sidebarY += school.height + 8f
            }
        }

        // ================= MAIN RIGHT COLUMN =================
        val mainX = (sidebarWidth + margin).toFloat()
        val mainWidth = (pageWidth - sidebarWidth - (margin * 2)).coerceAtLeast(100)
        var mainY = 32f

        val name = resume.personalInfo.fullName.ifBlank { "Candidate Name" }
        val nameLayout = createStaticLayout(name, namePaint, mainWidth)
        canvas.save()
        canvas.translate(mainX, mainY)
        nameLayout.draw(canvas)
        canvas.restore()
        mainY += nameLayout.height + 4f

        val headline = resume.personalInfo.headline.ifBlank { resume.targetJobTitle }
        if (headline.isNotBlank()) {
            val headlineLayout = createStaticLayout(headline, headlinePaint, mainWidth)
            canvas.save()
            canvas.translate(mainX, mainY)
            headlineLayout.draw(canvas)
            canvas.restore()
            mainY += headlineLayout.height + 12f
        }

        if (resume.summary.isNotBlank()) {
            canvas.drawText("PROFESSIONAL SUMMARY", mainX, mainY, sectionHeadingPaint)
            mainY += 6f
            canvas.drawLine(mainX, mainY, (pageWidth - margin).toFloat(), mainY, dividerPaint)
            mainY += 10f

            val summaryLayout = createStaticLayout(resume.summary, bodyPaint, mainWidth)
            canvas.save()
            canvas.translate(mainX, mainY)
            summaryLayout.draw(canvas)
            canvas.restore()
            mainY += summaryLayout.height + 14f
        }

        if (resume.experienceList.isNotEmpty()) {
            canvas.drawText("WORK EXPERIENCE", mainX, mainY, sectionHeadingPaint)
            mainY += 6f
            canvas.drawLine(mainX, mainY, (pageWidth - margin).toFloat(), mainY, dividerPaint)
            mainY += 10f

            for (exp in resume.experienceList) {
                val expHeaderPaint = TextPaint().apply {
                    color = Color.parseColor("#0F172A")
                    textSize = 10f
                    this.typeface = Typeface.create(typeface, Typeface.BOLD)
                    isAntiAlias = true
                }
                canvas.drawText("${exp.jobTitle} | ${exp.company}", mainX, mainY, expHeaderPaint)
                val dateText = "${exp.startDate} - ${if (exp.isCurrent) "Present" else exp.endDate} | ${exp.location}"
                canvas.drawText(dateText, mainX, mainY + 11f, subPaint)
                mainY += 24f

                if (exp.responsibilities.isNotBlank()) {
                    val respLayout = createStaticLayout(exp.responsibilities, bodyPaint, mainWidth)
                    canvas.save()
                    canvas.translate(mainX, mainY)
                    respLayout.draw(canvas)
                    canvas.restore()
                    mainY += respLayout.height + 4f
                }
                if (exp.measurableResults.isNotBlank()) {
                    val metricLayout = createStaticLayout("• Key Impact: ${exp.measurableResults}", subPaint, mainWidth)
                    canvas.save()
                    canvas.translate(mainX, mainY)
                    metricLayout.draw(canvas)
                    canvas.restore()
                    mainY += metricLayout.height + 8f
                } else {
                    mainY += 6f
                }
            }
        }

        if (resume.projectsList.isNotEmpty()) {
            canvas.drawText("KEY PROJECTS", mainX, mainY, sectionHeadingPaint)
            mainY += 6f
            canvas.drawLine(mainX, mainY, (pageWidth - margin).toFloat(), mainY, dividerPaint)
            mainY += 10f

            for (proj in resume.projectsList) {
                val projHeaderPaint = TextPaint().apply {
                    color = Color.parseColor("#0F172A")
                    textSize = 9.5f
                    this.typeface = Typeface.create(typeface, Typeface.BOLD)
                    isAntiAlias = true
                }
                canvas.drawText("${proj.title} [${proj.techStack}]", mainX, mainY, projHeaderPaint)
                mainY += 12f

                if (proj.description.isNotBlank()) {
                    val descLayout = createStaticLayout(proj.description, bodyPaint, mainWidth)
                    canvas.save()
                    canvas.translate(mainX, mainY)
                    descLayout.draw(canvas)
                    canvas.restore()
                    mainY += descLayout.height + 6f
                }
            }
        }

        drawFooter(canvas, typeface, "CareerForge AI Modern Professional")
        document.finishPage(page)
    }

    // ==========================================
    // 2. MINIMALIST (Single Column, Ultra ATS)
    // ==========================================
    private fun renderMinimalist(
        document: PdfDocument,
        resume: ResumeData,
        themeColor: Int,
        typeface: Typeface
    ) {
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        val margin = 32
        val contentWidth = pageWidth - (margin * 2)
        var y = 36f

        val namePaint = TextPaint().apply {
            color = Color.parseColor("#0F172A")
            textSize = 20f
            this.typeface = Typeface.create(typeface, Typeface.BOLD)
            isAntiAlias = true
        }
        val headlinePaint = TextPaint().apply {
            color = themeColor
            textSize = 10.5f
            this.typeface = Typeface.create(typeface, Typeface.BOLD)
            isAntiAlias = true
        }
        val bodyPaint = TextPaint().apply {
            color = Color.parseColor("#1E293B")
            textSize = 9f
            this.typeface = Typeface.create(typeface, Typeface.NORMAL)
            isAntiAlias = true
        }
        val subPaint = TextPaint().apply {
            color = Color.parseColor("#475569")
            textSize = 8.5f
            this.typeface = Typeface.create(typeface, Typeface.NORMAL)
            isAntiAlias = true
        }
        val sectionHeadingPaint = TextPaint().apply {
            color = Color.parseColor("#0F172A")
            textSize = 11f
            this.typeface = Typeface.create(typeface, Typeface.BOLD)
            isAntiAlias = true
        }
        val linePaint = Paint().apply {
            color = Color.parseColor("#CBD5E1")
            strokeWidth = 1f
        }

        val name = resume.personalInfo.fullName.ifBlank { "Candidate Name" }
        canvas.drawText(name, margin.toFloat(), y, namePaint)
        y += 16f

        val headline = resume.personalInfo.headline.ifBlank { resume.targetJobTitle }
        canvas.drawText(headline, margin.toFloat(), y, headlinePaint)
        y += 14f

        val p = resume.personalInfo
        val contactLine = listOfNotNull(
            p.email.ifBlank { null },
            p.phone.ifBlank { null },
            p.location.ifBlank { null },
            p.linkedInUrl.ifBlank { null }
        ).joinToString("  •  ")
        canvas.drawText(contactLine, margin.toFloat(), y, subPaint)
        y += 12f
        canvas.drawLine(margin.toFloat(), y, (pageWidth - margin).toFloat(), y, linePaint)
        y += 16f

        if (resume.summary.isNotBlank()) {
            canvas.drawText("PROFESSIONAL SUMMARY", margin.toFloat(), y, sectionHeadingPaint)
            y += 4f
            canvas.drawLine(margin.toFloat(), y, (pageWidth - margin).toFloat(), y, linePaint)
            y += 10f

            val sumLayout = createStaticLayout(resume.summary, bodyPaint, contentWidth)
            canvas.save()
            canvas.translate(margin.toFloat(), y)
            sumLayout.draw(canvas)
            canvas.restore()
            y += sumLayout.height + 14f
        }

        if (resume.experienceList.isNotEmpty()) {
            canvas.drawText("PROFESSIONAL EXPERIENCE", margin.toFloat(), y, sectionHeadingPaint)
            y += 4f
            canvas.drawLine(margin.toFloat(), y, (pageWidth - margin).toFloat(), y, linePaint)
            y += 10f

            for (exp in resume.experienceList) {
                canvas.drawText("${exp.jobTitle} - ${exp.company}", margin.toFloat(), y, sectionHeadingPaint)
                val date = "${exp.startDate} - ${if (exp.isCurrent) "Present" else exp.endDate} | ${exp.location}"
                canvas.drawText(date, margin.toFloat(), y + 10f, subPaint)
                y += 20f

                if (exp.responsibilities.isNotBlank()) {
                    val respLayout = createStaticLayout("• ${exp.responsibilities}", bodyPaint, contentWidth)
                    canvas.save()
                    canvas.translate(margin.toFloat(), y)
                    respLayout.draw(canvas)
                    canvas.restore()
                    y += respLayout.height + 4f
                }
                if (exp.measurableResults.isNotBlank()) {
                    val resLayout = createStaticLayout("• Impact: ${exp.measurableResults}", bodyPaint, contentWidth)
                    canvas.save()
                    canvas.translate(margin.toFloat(), y)
                    resLayout.draw(canvas)
                    canvas.restore()
                    y += resLayout.height + 8f
                }
            }
        }

        if (resume.educationList.isNotEmpty()) {
            canvas.drawText("EDUCATION", margin.toFloat(), y, sectionHeadingPaint)
            y += 4f
            canvas.drawLine(margin.toFloat(), y, (pageWidth - margin).toFloat(), y, linePaint)
            y += 10f

            for (edu in resume.educationList) {
                canvas.drawText("${edu.degree} in ${edu.fieldOfStudy} - ${edu.institution}", margin.toFloat(), y, sectionHeadingPaint)
                canvas.drawText("${edu.startDate} - ${edu.endDate} | ${edu.grade}", margin.toFloat(), y + 10f, subPaint)
                y += 22f
            }
        }

        if (resume.skillsList.isNotEmpty()) {
            canvas.drawText("TECHNICAL & PROFESSIONAL SKILLS", margin.toFloat(), y, sectionHeadingPaint)
            y += 4f
            canvas.drawLine(margin.toFloat(), y, (pageWidth - margin).toFloat(), y, linePaint)
            y += 10f

            val skillsStr = resume.skillsList.joinToString("  •  ") { it.name }
            val skillLayout = createStaticLayout(skillsStr, bodyPaint, contentWidth)
            canvas.save()
            canvas.translate(margin.toFloat(), y)
            skillLayout.draw(canvas)
            canvas.restore()
            y += skillLayout.height + 12f
        }

        drawFooter(canvas, typeface, "CareerForge ATS Clean Template")
        document.finishPage(page)
    }

    // ==========================================
    // 3. EXECUTIVE (Full Header Banner & Callout Box)
    // ==========================================
    private fun renderExecutive(
        document: PdfDocument,
        resume: ResumeData,
        themeColor: Int,
        typeface: Typeface
    ) {
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        val bannerHeight = 84f
        val bannerPaint = Paint().apply {
            color = themeColor
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, pageWidth.toFloat(), bannerHeight, bannerPaint)

        val namePaint = TextPaint().apply {
            color = Color.WHITE
            textSize = 20f
            this.typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }
        val titlePaint = TextPaint().apply {
            color = Color.parseColor("#E2E8F0")
            textSize = 10.5f
            this.typeface = Typeface.create(Typeface.SERIF, Typeface.NORMAL)
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }

        canvas.drawText(resume.personalInfo.fullName.ifBlank { "Executive Candidate" }, pageWidth / 2f, 34f, namePaint)
        canvas.drawText(resume.personalInfo.headline.ifBlank { resume.targetJobTitle }, pageWidth / 2f, 52f, titlePaint)

        val p = resume.personalInfo
        val contactLine = listOfNotNull(p.email.ifBlank { null }, p.phone.ifBlank { null }, p.location.ifBlank { null }).joinToString("   |   ")
        canvas.drawText(contactLine, pageWidth / 2f, 68f, titlePaint)

        val margin = 28
        val contentWidth = pageWidth - (margin * 2)
        var y = bannerHeight + 20f

        val sectionHeadingPaint = TextPaint().apply {
            color = themeColor
            textSize = 12f
            this.typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            isAntiAlias = true
        }
        val bodyPaint = TextPaint().apply {
            color = Color.parseColor("#0F172A")
            textSize = 9f
            this.typeface = Typeface.create(typeface, Typeface.NORMAL)
            isAntiAlias = true
        }
        val subPaint = TextPaint().apply {
            color = Color.parseColor("#475569")
            textSize = 8.5f
            this.typeface = Typeface.create(typeface, Typeface.NORMAL)
            isAntiAlias = true
        }
        val linePaint = Paint().apply {
            color = themeColor
            strokeWidth = 1.5f
        }

        // Executive Summary Callout Box
        if (resume.summary.isNotBlank()) {
            canvas.drawText("EXECUTIVE LEADERSHIP PROFILE", margin.toFloat(), y, sectionHeadingPaint)
            y += 4f
            canvas.drawLine(margin.toFloat(), y, (pageWidth - margin).toFloat(), y, linePaint)
            y += 10f

            val sumLayout = createStaticLayout(resume.summary, bodyPaint, contentWidth - 16)
            val boxRect = RectF(margin.toFloat(), y, (pageWidth - margin).toFloat(), y + sumLayout.height + 14f)
            val boxBgPaint = Paint().apply {
                color = Color.parseColor("#F8FAFC")
                style = Paint.Style.FILL
            }
            val boxBorderPaint = Paint().apply {
                color = Color.parseColor("#E2E8F0")
                style = Paint.Style.STROKE
                strokeWidth = 1f
            }
            canvas.drawRoundRect(boxRect, 6f, 6f, boxBgPaint)
            canvas.drawRoundRect(boxRect, 6f, 6f, boxBorderPaint)

            canvas.save()
            canvas.translate(margin + 8f, y + 7f)
            sumLayout.draw(canvas)
            canvas.restore()
            y += sumLayout.height + 22f
        }

        if (resume.experienceList.isNotEmpty()) {
            canvas.drawText("DIRECTOR & PROFESSIONAL EXPERIENCE", margin.toFloat(), y, sectionHeadingPaint)
            y += 4f
            canvas.drawLine(margin.toFloat(), y, (pageWidth - margin).toFloat(), y, linePaint)
            y += 10f

            for (exp in resume.experienceList) {
                canvas.drawText("${exp.jobTitle}   |   ${exp.company}", margin.toFloat(), y, sectionHeadingPaint)
                canvas.drawText("${exp.startDate} - ${if (exp.isCurrent) "Present" else exp.endDate}  (${exp.location})", margin.toFloat(), y + 10f, subPaint)
                y += 20f

                if (exp.responsibilities.isNotBlank()) {
                    val respLayout = createStaticLayout(exp.responsibilities, bodyPaint, contentWidth)
                    canvas.save()
                    canvas.translate(margin.toFloat(), y)
                    respLayout.draw(canvas)
                    canvas.restore()
                    y += respLayout.height + 4f
                }
                if (exp.measurableResults.isNotBlank()) {
                    val resLayout = createStaticLayout("• Strategic Outcome: ${exp.measurableResults}", bodyPaint, contentWidth)
                    canvas.save()
                    canvas.translate(margin.toFloat(), y)
                    resLayout.draw(canvas)
                    canvas.restore()
                    y += resLayout.height + 8f
                }
            }
        }

        if (resume.educationList.isNotEmpty()) {
            canvas.drawText("ACADEMIC CREDENTIALS & BOARD HONORS", margin.toFloat(), y, sectionHeadingPaint)
            y += 4f
            canvas.drawLine(margin.toFloat(), y, (pageWidth - margin).toFloat(), y, linePaint)
            y += 10f

            for (edu in resume.educationList) {
                canvas.drawText("${edu.degree} in ${edu.fieldOfStudy} - ${edu.institution}", margin.toFloat(), y, bodyPaint)
                canvas.drawText("${edu.startDate} - ${edu.endDate} | ${edu.grade}", margin.toFloat(), y + 10f, subPaint)
                y += 22f
            }
        }

        drawFooter(canvas, typeface, "CareerForge Executive Leadership Suite")
        document.finishPage(page)
    }

    // ==========================================
    // 4. TECH & SOFTWARE DEVELOPER (Skills Matrix & Projects First)
    // ==========================================
    private fun renderTechDeveloper(
        document: PdfDocument,
        resume: ResumeData,
        themeColor: Int,
        typeface: Typeface
    ) {
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        val margin = 24
        val contentWidth = pageWidth - (margin * 2)
        var y = 28f

        // Tech Top Accent Bar
        val topBarPaint = Paint().apply {
            color = themeColor
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, pageWidth.toFloat(), 6f, topBarPaint)

        val namePaint = TextPaint().apply {
            color = Color.parseColor("#0F172A")
            textSize = 20f
            this.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            isAntiAlias = true
        }
        val tagTitlePaint = TextPaint().apply {
            color = themeColor
            textSize = 10f
            this.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            isAntiAlias = true
        }
        val sectionHeadingPaint = TextPaint().apply {
            color = themeColor
            textSize = 10.5f
            this.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            isAntiAlias = true
        }
        val bodyPaint = TextPaint().apply {
            color = Color.parseColor("#1E293B")
            textSize = 8.5f
            this.typeface = Typeface.create(typeface, Typeface.NORMAL)
            isAntiAlias = true
        }
        val subPaint = TextPaint().apply {
            color = Color.parseColor("#64748B")
            textSize = 8f
            this.typeface = Typeface.create(typeface, Typeface.NORMAL)
            isAntiAlias = true
        }
        val linePaint = Paint().apply {
            color = Color.parseColor("#E2E8F0")
            strokeWidth = 1f
        }

        val name = resume.personalInfo.fullName.ifBlank { "Software Engineer" }
        canvas.drawText(name, margin.toFloat(), y, namePaint)
        y += 15f

        val headline = resume.personalInfo.headline.ifBlank { resume.targetJobTitle }
        canvas.drawText("<$headline />", margin.toFloat(), y, tagTitlePaint)
        y += 14f

        val p = resume.personalInfo
        val contactLine = listOfNotNull(
            p.email.ifBlank { null },
            p.phone.ifBlank { null },
            p.githubUrl.ifBlank { null }?.let { "git: ${it.removePrefix("https://").removePrefix("www.")}" },
            p.portfolioUrl.ifBlank { null }?.let { "web: ${it.removePrefix("https://").removePrefix("www.")}" }
        ).joinToString("   •   ")
        canvas.drawText(contactLine, margin.toFloat(), y, subPaint)
        y += 12f
        canvas.drawLine(margin.toFloat(), y, (pageWidth - margin).toFloat(), y, linePaint)
        y += 14f

        // 1. Technical Skills Matrix
        if (resume.skillsList.isNotEmpty()) {
            canvas.drawText("## TECHNICAL SKILLS MATRIX", margin.toFloat(), y, sectionHeadingPaint)
            y += 4f
            canvas.drawLine(margin.toFloat(), y, (pageWidth - margin).toFloat(), y, linePaint)
            y += 10f

            val skillsFormatted = resume.skillsList.joinToString("   |   ") { "[${it.name}: ${it.level}]" }
            val skillLayout = createStaticLayout(skillsFormatted, bodyPaint, contentWidth)
            canvas.save()
            canvas.translate(margin.toFloat(), y)
            skillLayout.draw(canvas)
            canvas.restore()
            y += skillLayout.height + 14f
        }

        // 2. Featured Engineering Projects
        if (resume.projectsList.isNotEmpty()) {
            canvas.drawText("## FEATURED ENGINEERING PROJECTS", margin.toFloat(), y, sectionHeadingPaint)
            y += 4f
            canvas.drawLine(margin.toFloat(), y, (pageWidth - margin).toFloat(), y, linePaint)
            y += 10f

            for (proj in resume.projectsList) {
                val projTitlePaint = TextPaint().apply {
                    color = Color.parseColor("#0F172A")
                    textSize = 9.5f
                    this.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
                    isAntiAlias = true
                }
                canvas.drawText("▶ ${proj.title}  {${proj.techStack}}", margin.toFloat(), y, projTitlePaint)
                y += 12f

                if (proj.description.isNotBlank()) {
                    val descLayout = createStaticLayout(proj.description, bodyPaint, contentWidth)
                    canvas.save()
                    canvas.translate(margin.toFloat(), y)
                    descLayout.draw(canvas)
                    canvas.restore()
                    y += descLayout.height + 6f
                }
            }
            y += 6f
        }

        // 3. Professional Experience
        if (resume.experienceList.isNotEmpty()) {
            canvas.drawText("## PRODUCTION EXPERIENCE", margin.toFloat(), y, sectionHeadingPaint)
            y += 4f
            canvas.drawLine(margin.toFloat(), y, (pageWidth - margin).toFloat(), y, linePaint)
            y += 10f

            for (exp in resume.experienceList) {
                canvas.drawText("${exp.jobTitle} @ ${exp.company}", margin.toFloat(), y, sectionHeadingPaint)
                canvas.drawText("${exp.startDate} - ${if (exp.isCurrent) "Present" else exp.endDate}  (${exp.location})", margin.toFloat(), y + 10f, subPaint)
                y += 20f

                if (exp.responsibilities.isNotBlank()) {
                    val respLayout = createStaticLayout("• ${exp.responsibilities}", bodyPaint, contentWidth)
                    canvas.save()
                    canvas.translate(margin.toFloat(), y)
                    respLayout.draw(canvas)
                    canvas.restore()
                    y += respLayout.height + 4f
                }
                if (exp.measurableResults.isNotBlank()) {
                    val metricLayout = createStaticLayout("  ↳ Metrics: ${exp.measurableResults}", subPaint, contentWidth)
                    canvas.save()
                    canvas.translate(margin.toFloat(), y)
                    metricLayout.draw(canvas)
                    canvas.restore()
                    y += metricLayout.height + 6f
                }
            }
        }

        // 4. Education
        if (resume.educationList.isNotEmpty()) {
            canvas.drawText("## EDUCATION & CREDENTIALS", margin.toFloat(), y, sectionHeadingPaint)
            y += 4f
            canvas.drawLine(margin.toFloat(), y, (pageWidth - margin).toFloat(), y, linePaint)
            y += 10f

            for (edu in resume.educationList) {
                canvas.drawText("${edu.degree} in ${edu.fieldOfStudy} - ${edu.institution} (${edu.startDate} - ${edu.endDate})", margin.toFloat(), y, bodyPaint)
                y += 14f
            }
        }

        drawFooter(canvas, Typeface.MONOSPACE, "CareerForge Tech Matrix Architecture")
        document.finishPage(page)
    }

    // ==========================================
    // 5. CREATIVE PORTFOLIO (Asymmetric Pill Badges & Visual Showcase)
    // ==========================================
    private fun renderCreative(
        document: PdfDocument,
        resume: ResumeData,
        themeColor: Int,
        typeface: Typeface
    ) {
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        val sidebarWidth = 175
        val margin = 20

        // Bold Theme Colored Sidebar
        val sidebarPaint = Paint().apply {
            color = themeColor
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, sidebarWidth.toFloat(), pageHeight.toFloat(), sidebarPaint)

        var sidebarY = 32f

        // Candidate Initials Ring
        val ringPaint = Paint().apply {
            color = Color.WHITE
            style = Paint.Style.STROKE
            strokeWidth = 2f
            isAntiAlias = true
        }
        canvas.drawCircle(sidebarWidth / 2f, sidebarY + 24f, 24f, ringPaint)

        val initialsPaint = Paint().apply {
            color = Color.WHITE
            textSize = 16f
            textAlign = Paint.Align.CENTER
            this.typeface = Typeface.create(typeface, Typeface.BOLD)
            isAntiAlias = true
        }
        val initials = resume.personalInfo.fullName.split(" ")
            .filter { it.isNotBlank() }
            .take(2)
            .map { it.first().uppercase() }
            .joinToString("")
        canvas.drawText(initials.ifBlank { "CR" }, sidebarWidth / 2f, sidebarY + 30f, initialsPaint)
        sidebarY += 66f

        // White Text Paints for Sidebar
        val sideHeadingPaint = TextPaint().apply {
            color = Color.WHITE
            textSize = 10.5f
            this.typeface = Typeface.create(typeface, Typeface.BOLD)
            isAntiAlias = true
        }
        val sideBodyPaint = TextPaint().apply {
            color = Color.parseColor("#F1F5F9")
            textSize = 8f
            this.typeface = Typeface.create(typeface, Typeface.NORMAL)
            isAntiAlias = true
        }

        canvas.drawText("CONTACT", 16f, sidebarY, sideHeadingPaint)
        sidebarY += 12f

        val personal = resume.personalInfo
        val contactLines = listOfNotNull(
            personal.email.ifBlank { null },
            personal.phone.ifBlank { null },
            personal.location.ifBlank { null },
            personal.portfolioUrl.ifBlank { null }
        )
        for (line in contactLines) {
            val layout = createStaticLayout(line, sideBodyPaint, sidebarWidth - 32)
            canvas.save()
            canvas.translate(16f, sidebarY)
            layout.draw(canvas)
            canvas.restore()
            sidebarY += layout.height + 6f
        }

        sidebarY += 14f

        // Skill Pills in Sidebar
        if (resume.skillsList.isNotEmpty()) {
            canvas.drawText("CREATIVE SKILLS", 16f, sidebarY, sideHeadingPaint)
            sidebarY += 12f

            for (skill in resume.skillsList) {
                val pillRect = RectF(16f, sidebarY - 2f, (sidebarWidth - 16).toFloat(), sidebarY + 16f)
                val pillBg = Paint().apply {
                    color = Color.parseColor("#33FFFFFF")
                    style = Paint.Style.FILL
                }
                canvas.drawRoundRect(pillRect, 9f, 9f, pillBg)
                canvas.drawText(skill.name, 22f, sidebarY + 10f, sideBodyPaint)
                sidebarY += 22f
            }
        }

        // ================= RIGHT MAIN CONTENT =================
        val mainX = (sidebarWidth + margin).toFloat()
        val mainWidth = (pageWidth - sidebarWidth - (margin * 2)).coerceAtLeast(100)
        var mainY = 32f

        val namePaint = TextPaint().apply {
            color = Color.parseColor("#0F172A")
            textSize = 21f
            this.typeface = Typeface.create(typeface, Typeface.BOLD)
            isAntiAlias = true
        }
        val headlinePaint = TextPaint().apply {
            color = themeColor
            textSize = 11f
            this.typeface = Typeface.create(typeface, Typeface.BOLD)
            isAntiAlias = true
        }
        val sectionHeadingPaint = TextPaint().apply {
            color = Color.parseColor("#0F172A")
            textSize = 11.5f
            this.typeface = Typeface.create(typeface, Typeface.BOLD)
            isAntiAlias = true
        }
        val bodyPaint = TextPaint().apply {
            color = Color.parseColor("#1E293B")
            textSize = 8.8f
            this.typeface = Typeface.create(typeface, Typeface.NORMAL)
            isAntiAlias = true
        }
        val subPaint = TextPaint().apply {
            color = Color.parseColor("#64748B")
            textSize = 8f
            this.typeface = Typeface.create(typeface, Typeface.NORMAL)
            isAntiAlias = true
        }

        val name = resume.personalInfo.fullName.ifBlank { "Creative Specialist" }
        canvas.drawText(name, mainX, mainY + 14f, namePaint)
        mainY += 24f

        val headline = resume.personalInfo.headline.ifBlank { resume.targetJobTitle }
        canvas.drawText(headline, mainX, mainY + 6f, headlinePaint)
        mainY += 18f

        if (resume.summary.isNotBlank()) {
            val sumLayout = createStaticLayout(resume.summary, bodyPaint, mainWidth)
            canvas.save()
            canvas.translate(mainX, mainY)
            sumLayout.draw(canvas)
            canvas.restore()
            mainY += sumLayout.height + 16f
        }

        if (resume.experienceList.isNotEmpty()) {
            canvas.drawText("CAREER HIGHLIGHTS", mainX, mainY, sectionHeadingPaint)
            mainY += 12f

            for (exp in resume.experienceList) {
                val dotPaint = Paint().apply {
                    color = themeColor
                    isAntiAlias = true
                }
                canvas.drawCircle(mainX + 4f, mainY + 5f, 4f, dotPaint)
                canvas.drawText("  ${exp.jobTitle} - ${exp.company}", mainX + 10f, mainY + 8f, sectionHeadingPaint)
                canvas.drawText("${exp.startDate} - ${if (exp.isCurrent) "Present" else exp.endDate} | ${exp.location}", mainX + 10f, mainY + 19f, subPaint)
                mainY += 28f

                if (exp.responsibilities.isNotBlank()) {
                    val respLayout = createStaticLayout(exp.responsibilities, bodyPaint, mainWidth - 10)
                    canvas.save()
                    canvas.translate(mainX + 10f, mainY)
                    respLayout.draw(canvas)
                    canvas.restore()
                    mainY += respLayout.height + 6f
                }
            }
        }

        if (resume.projectsList.isNotEmpty()) {
            canvas.drawText("PORTFOLIO SHOWCASE", mainX, mainY, sectionHeadingPaint)
            mainY += 12f

            for (proj in resume.projectsList) {
                val cardRect = RectF(mainX, mainY, (pageWidth - margin).toFloat(), mainY + 44f)
                val cardPaint = Paint().apply {
                    color = Color.parseColor("#F8FAFC")
                    style = Paint.Style.FILL
                }
                val borderPaint = Paint().apply {
                    color = Color.parseColor("#E2E8F0")
                    style = Paint.Style.STROKE
                    strokeWidth = 1f
                }
                canvas.drawRoundRect(cardRect, 4f, 4f, cardPaint)
                canvas.drawRoundRect(cardRect, 4f, 4f, borderPaint)

                canvas.drawText("✦ ${proj.title}", mainX + 8f, mainY + 14f, sectionHeadingPaint)
                val desc = createStaticLayout(proj.description, subPaint, mainWidth - 16)
                canvas.save()
                canvas.translate(mainX + 8f, mainY + 18f)
                desc.draw(canvas)
                canvas.restore()
                mainY += 52f
            }
        }

        drawFooter(canvas, typeface, "CareerForge Creative Portfolio")
        document.finishPage(page)
    }

    // ==========================================
    // 6. ACADEMIC & FRESHER (Education First & Capstone Research)
    // ==========================================
    private fun renderAcademicFresher(
        document: PdfDocument,
        resume: ResumeData,
        themeColor: Int,
        typeface: Typeface
    ) {
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        val margin = 30
        val contentWidth = pageWidth - (margin * 2)
        var y = 32f

        val namePaint = TextPaint().apply {
            color = Color.parseColor("#0F172A")
            textSize = 20f
            this.typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        val headlinePaint = TextPaint().apply {
            color = themeColor
            textSize = 10f
            this.typeface = Typeface.create(typeface, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        val subPaint = TextPaint().apply {
            color = Color.parseColor("#475569")
            textSize = 8f
            this.typeface = Typeface.create(typeface, Typeface.NORMAL)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        val sectionHeadingPaint = TextPaint().apply {
            color = themeColor
            textSize = 10.5f
            this.typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            isAntiAlias = true
        }
        val bodyPaint = TextPaint().apply {
            color = Color.parseColor("#1E293B")
            textSize = 8.5f
            this.typeface = Typeface.create(typeface, Typeface.NORMAL)
            isAntiAlias = true
        }
        val linePaint = Paint().apply {
            color = Color.parseColor("#CBD5E1")
            strokeWidth = 1f
        }

        canvas.drawText(resume.personalInfo.fullName.ifBlank { "Graduate Candidate" }, pageWidth / 2f, y, namePaint)
        y += 14f
        canvas.drawText(resume.personalInfo.headline.ifBlank { resume.targetJobTitle }, pageWidth / 2f, y, headlinePaint)
        y += 12f

        val p = resume.personalInfo
        val contactLine = listOfNotNull(p.email.ifBlank { null }, p.phone.ifBlank { null }, p.location.ifBlank { null }).joinToString("   •   ")
        canvas.drawText(contactLine, pageWidth / 2f, y, subPaint)
        y += 12f
        canvas.drawLine(margin.toFloat(), y, (pageWidth - margin).toFloat(), y, linePaint)
        y += 14f

        // 1. EDUCATION (FIRST FOR FRESHERS & ACADEMICS)
        if (resume.educationList.isNotEmpty()) {
            canvas.drawText("I. ACADEMIC CREDENTIALS & EDUCATION", margin.toFloat(), y, sectionHeadingPaint)
            y += 4f
            canvas.drawLine(margin.toFloat(), y, (pageWidth - margin).toFloat(), y, linePaint)
            y += 10f

            for (edu in resume.educationList) {
                canvas.drawText("${edu.degree} in ${edu.fieldOfStudy}", margin.toFloat(), y, sectionHeadingPaint)
                canvas.drawText("${edu.institution} | ${edu.startDate} - ${edu.endDate}", margin.toFloat(), y + 10f, bodyPaint)
                if (edu.grade.isNotBlank()) {
                    canvas.drawText("• Honors / Cumulative GPA: ${edu.grade}", margin.toFloat(), y + 20f, subPaint)
                    y += 28f
                } else {
                    y += 20f
                }
            }
        }

        // 2. ACADEMIC & CAPSTONE PROJECTS
        if (resume.projectsList.isNotEmpty()) {
            canvas.drawText("II. ACADEMIC & CAPSTONE PROJECTS", margin.toFloat(), y, sectionHeadingPaint)
            y += 4f
            canvas.drawLine(margin.toFloat(), y, (pageWidth - margin).toFloat(), y, linePaint)
            y += 10f

            for (proj in resume.projectsList) {
                canvas.drawText("• ${proj.title} [${proj.techStack}]", margin.toFloat(), y, sectionHeadingPaint)
                y += 10f
                if (proj.description.isNotBlank()) {
                    val desc = createStaticLayout(proj.description, bodyPaint, contentWidth - 10)
                    canvas.save()
                    canvas.translate(margin + 10f, y)
                    desc.draw(canvas)
                    canvas.restore()
                    y += desc.height + 6f
                }
            }
        }

        // 3. SKILLS & COURSEWORK
        if (resume.skillsList.isNotEmpty()) {
            canvas.drawText("III. TECHNICAL COMPETENCIES & COURSEWORK", margin.toFloat(), y, sectionHeadingPaint)
            y += 4f
            canvas.drawLine(margin.toFloat(), y, (pageWidth - margin).toFloat(), y, linePaint)
            y += 10f

            val skillsStr = resume.skillsList.joinToString("   •   ") { "${it.name} (${it.level})" }
            val skillLayout = createStaticLayout(skillsStr, bodyPaint, contentWidth)
            canvas.save()
            canvas.translate(margin.toFloat(), y)
            skillLayout.draw(canvas)
            canvas.restore()
            y += skillLayout.height + 12f
        }

        // 4. INTERNSHIPS & EXPERIENCE
        if (resume.experienceList.isNotEmpty()) {
            canvas.drawText("IV. INTERNSHIPS & LEADERSHIP EXPERIENCE", margin.toFloat(), y, sectionHeadingPaint)
            y += 4f
            canvas.drawLine(margin.toFloat(), y, (pageWidth - margin).toFloat(), y, linePaint)
            y += 10f

            for (exp in resume.experienceList) {
                canvas.drawText("${exp.jobTitle} - ${exp.company} (${exp.startDate} - ${exp.endDate})", margin.toFloat(), y, sectionHeadingPaint)
                y += 12f
                if (exp.responsibilities.isNotBlank()) {
                    val resp = createStaticLayout("• ${exp.responsibilities}", bodyPaint, contentWidth)
                    canvas.save()
                    canvas.translate(margin.toFloat(), y)
                    resp.draw(canvas)
                    canvas.restore()
                    y += resp.height + 4f
                }
            }
        }

        drawFooter(canvas, Typeface.SERIF, "CareerForge Academic Curriculum Vitae")
        document.finishPage(page)
    }

    // ==========================================
    // 7. NORDIC COMPACT (Scandinavian High-Density 2-Column Grid)
    // ==========================================
    private fun renderNordicCompact(
        document: PdfDocument,
        resume: ResumeData,
        themeColor: Int,
        typeface: Typeface
    ) {
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        val leftColWidth = 195
        val dividerX = 210f
        val rightColX = 225f
        val rightColWidth = (pageWidth - rightColX - 24).toInt()

        val guidePaint = Paint().apply {
            color = Color.parseColor("#E2E8F0")
            strokeWidth = 1f
        }
        canvas.drawLine(dividerX, 24f, dividerX, pageHeight - 32f, guidePaint)

        val headerPaint = TextPaint().apply {
            color = Color.parseColor("#0F172A")
            textSize = 17f
            this.typeface = Typeface.create(typeface, Typeface.BOLD)
            isAntiAlias = true
        }
        val tagPaint = TextPaint().apply {
            color = themeColor
            textSize = 9f
            this.typeface = Typeface.create(typeface, Typeface.BOLD)
            isAntiAlias = true
        }
        val sectionPaint = TextPaint().apply {
            color = themeColor
            textSize = 9.5f
            this.typeface = Typeface.create(typeface, Typeface.BOLD)
            isAntiAlias = true
        }
        val bodyPaint = TextPaint().apply {
            color = Color.parseColor("#1E293B")
            textSize = 8f
            this.typeface = Typeface.create(typeface, Typeface.NORMAL)
            isAntiAlias = true
        }
        val subPaint = TextPaint().apply {
            color = Color.parseColor("#64748B")
            textSize = 7.5f
            this.typeface = Typeface.create(typeface, Typeface.NORMAL)
            isAntiAlias = true
        }

        // Left Column (Contact, Skills, Education)
        var leftY = 32f
        canvas.drawText("CONTACT & PROFILES", 24f, leftY, sectionPaint)
        leftY += 12f

        val p = resume.personalInfo
        val contacts = listOfNotNull(p.email.ifBlank { null }, p.phone.ifBlank { null }, p.location.ifBlank { null }, p.linkedInUrl.ifBlank { null })
        for (c in contacts) {
            val layout = createStaticLayout(c, bodyPaint, leftColWidth - 24)
            canvas.save()
            canvas.translate(24f, leftY)
            layout.draw(canvas)
            canvas.restore()
            leftY += layout.height + 4f
        }

        leftY += 14f
        if (resume.skillsList.isNotEmpty()) {
            canvas.drawText("SKILLS & TOOLING", 24f, leftY, sectionPaint)
            leftY += 12f
            for (s in resume.skillsList) {
                canvas.drawText("■  ${s.name}", 24f, leftY, bodyPaint)
                leftY += 12f
            }
        }

        leftY += 14f
        if (resume.educationList.isNotEmpty()) {
            canvas.drawText("EDUCATION", 24f, leftY, sectionPaint)
            leftY += 12f
            for (edu in resume.educationList) {
                val eduLayout = createStaticLayout("${edu.degree}\n${edu.institution} (${edu.endDate})", bodyPaint, leftColWidth - 24)
                canvas.save()
                canvas.translate(24f, leftY)
                eduLayout.draw(canvas)
                canvas.restore()
                leftY += eduLayout.height + 6f
            }
        }

        // Right Column (Name, Headline, Summary, Experience, Projects)
        var rightY = 32f
        val name = resume.personalInfo.fullName.ifBlank { "Candidate Name" }
        canvas.drawText(name.uppercase(), rightColX, rightY, headerPaint)
        rightY += 13f

        val headline = resume.personalInfo.headline.ifBlank { resume.targetJobTitle }
        canvas.drawText(headline, rightColX, rightY, tagPaint)
        rightY += 14f

        if (resume.summary.isNotBlank()) {
            val sumLayout = createStaticLayout(resume.summary, bodyPaint, rightColWidth)
            canvas.save()
            canvas.translate(rightColX, rightY)
            sumLayout.draw(canvas)
            canvas.restore()
            rightY += sumLayout.height + 12f
        }

        if (resume.experienceList.isNotEmpty()) {
            canvas.drawText("PROFESSIONAL HISTORY", rightColX, rightY, sectionPaint)
            rightY += 10f

            for (exp in resume.experienceList) {
                canvas.drawText("${exp.jobTitle}  —  ${exp.company}", rightColX, rightY, sectionPaint)
                canvas.drawText("${exp.startDate} - ${if (exp.isCurrent) "Present" else exp.endDate} | ${exp.location}", rightColX, rightY + 9f, subPaint)
                rightY += 18f

                if (exp.responsibilities.isNotBlank()) {
                    val resp = createStaticLayout(exp.responsibilities, bodyPaint, rightColWidth)
                    canvas.save()
                    canvas.translate(rightColX, rightY)
                    resp.draw(canvas)
                    canvas.restore()
                    rightY += resp.height + 4f
                }
                if (exp.measurableResults.isNotBlank()) {
                    val impact = createStaticLayout("• Metric: ${exp.measurableResults}", subPaint, rightColWidth)
                    canvas.save()
                    canvas.translate(rightColX, rightY)
                    impact.draw(canvas)
                    canvas.restore()
                    rightY += impact.height + 6f
                }
            }
        }

        drawFooter(canvas, typeface, "CareerForge Nordic Architecture")
        document.finishPage(page)
    }

    // ==========================================
    // 8. MODERN TIMELINE (Visual Chronological Milestones)
    // ==========================================
    private fun renderModernTimeline(
        document: PdfDocument,
        resume: ResumeData,
        themeColor: Int,
        typeface: Typeface
    ) {
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        val margin = 28
        val contentWidth = pageWidth - (margin * 2)
        var y = 32f

        val namePaint = TextPaint().apply {
            color = Color.parseColor("#0F172A")
            textSize = 20f
            this.typeface = Typeface.create(typeface, Typeface.BOLD)
            isAntiAlias = true
        }
        val headlinePaint = TextPaint().apply {
            color = themeColor
            textSize = 10.5f
            this.typeface = Typeface.create(typeface, Typeface.BOLD)
            isAntiAlias = true
        }
        val sectionPaint = TextPaint().apply {
            color = Color.parseColor("#0F172A")
            textSize = 11f
            this.typeface = Typeface.create(typeface, Typeface.BOLD)
            isAntiAlias = true
        }
        val bodyPaint = TextPaint().apply {
            color = Color.parseColor("#1E293B")
            textSize = 8.8f
            this.typeface = Typeface.create(typeface, Typeface.NORMAL)
            isAntiAlias = true
        }
        val subPaint = TextPaint().apply {
            color = Color.parseColor("#64748B")
            textSize = 8f
            this.typeface = Typeface.create(typeface, Typeface.NORMAL)
            isAntiAlias = true
        }

        // Header
        val name = resume.personalInfo.fullName.ifBlank { "Candidate Name" }
        canvas.drawText(name, margin.toFloat(), y, namePaint)
        y += 15f
        val headline = resume.personalInfo.headline.ifBlank { resume.targetJobTitle }
        canvas.drawText(headline, margin.toFloat(), y, headlinePaint)
        y += 14f

        val p = resume.personalInfo
        val contactLine = listOfNotNull(p.email.ifBlank { null }, p.phone.ifBlank { null }, p.location.ifBlank { null }).joinToString("  •  ")
        canvas.drawText(contactLine, margin.toFloat(), y, subPaint)
        y += 14f

        val dividerPaint = Paint().apply {
            color = Color.parseColor("#E2E8F0")
            strokeWidth = 1f
        }
        canvas.drawLine(margin.toFloat(), y, (pageWidth - margin).toFloat(), y, dividerPaint)
        y += 16f

        // Timeline Track Settings
        val timelineX = margin + 12f
        val entryTextX = timelineX + 20f
        val entryWidth = (pageWidth - margin - entryTextX).toInt()

        if (resume.experienceList.isNotEmpty()) {
            canvas.drawText("CAREER PROGRESSION TIMELINE", margin.toFloat(), y, sectionPaint)
            y += 14f

            val timelineStartY = y
            var currentY = y

            for (exp in resume.experienceList) {
                // Outer circle node
                val nodeOuterPaint = Paint().apply {
                    color = themeColor
                    isAntiAlias = true
                }
                canvas.drawCircle(timelineX, currentY + 5f, 5.5f, nodeOuterPaint)

                // Inner circle node
                val nodeInnerPaint = Paint().apply {
                    color = Color.WHITE
                    isAntiAlias = true
                }
                canvas.drawCircle(timelineX, currentY + 5f, 2.5f, nodeInnerPaint)

                // Role title & company
                val rolePaint = TextPaint().apply {
                    color = Color.parseColor("#0F172A")
                    textSize = 9.5f
                    this.typeface = Typeface.create(typeface, Typeface.BOLD)
                    isAntiAlias = true
                }
                canvas.drawText("${exp.jobTitle} @ ${exp.company}", entryTextX, currentY + 8f, rolePaint)
                canvas.drawText("${exp.startDate} - ${if (exp.isCurrent) "Present" else exp.endDate} | ${exp.location}", entryTextX, currentY + 19f, subPaint)
                currentY += 28f

                if (exp.responsibilities.isNotBlank()) {
                    val resp = createStaticLayout(exp.responsibilities, bodyPaint, entryWidth)
                    canvas.save()
                    canvas.translate(entryTextX, currentY)
                    resp.draw(canvas)
                    canvas.restore()
                    currentY += resp.height + 6f
                }
                if (exp.measurableResults.isNotBlank()) {
                    val impact = createStaticLayout("↳ Impact: ${exp.measurableResults}", subPaint, entryWidth)
                    canvas.save()
                    canvas.translate(entryTextX, currentY)
                    impact.draw(canvas)
                    canvas.restore()
                    currentY += impact.height + 8f
                }
            }

            // Draw vertical track line connecting nodes
            val trackPaint = Paint().apply {
                color = Color.parseColor("#CBD5E1")
                strokeWidth = 2f
            }
            canvas.drawLine(timelineX, timelineStartY + 6f, timelineX, currentY - 14f, trackPaint)
            y = currentY + 10f
        }

        // Education & Skills below timeline
        if (resume.educationList.isNotEmpty()) {
            canvas.drawText("EDUCATION & ACADEMICS", margin.toFloat(), y, sectionPaint)
            y += 12f
            for (edu in resume.educationList) {
                canvas.drawText("${edu.degree} in ${edu.fieldOfStudy} — ${edu.institution} (${edu.startDate} - ${edu.endDate})", margin.toFloat(), y, bodyPaint)
                y += 14f
            }
        }

        drawFooter(canvas, typeface, "CareerForge Chronological Timeline")
        document.finishPage(page)
    }

    private fun drawFooter(canvas: Canvas, typeface: Typeface, templateLabel: String) {
        val footerPaint = TextPaint().apply {
            color = Color.parseColor("#94A3B8")
            textSize = 7f
            textAlign = Paint.Align.CENTER
            this.typeface = Typeface.create(typeface, Typeface.NORMAL)
            isAntiAlias = true
        }
        canvas.drawText("Page 1 of 1 • $templateLabel", (pageWidth / 2f), (pageHeight - 14f), footerPaint)
    }

    private fun createStaticLayout(
        text: String,
        paint: TextPaint,
        width: Int
    ): StaticLayout {
        val safeWidth = width.coerceAtLeast(10)
        return StaticLayout.Builder.obtain(text, 0, text.length, paint, safeWidth)
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setLineSpacing(0f, 1.15f)
            .setIncludePad(false)
            .build()
    }
}
