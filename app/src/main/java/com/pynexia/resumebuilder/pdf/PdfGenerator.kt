package com.pynexia.resumebuilder.pdf

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
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
        var pageNumber = 1

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
            "academic_fresher" -> renderAcademicFresher(document, resume, themeColor, baseTypeface)
            "creative" -> renderCreative(document, resume, themeColor, baseTypeface)
            else -> renderModernPro(document, resume, themeColor, baseTypeface)
        }

        FileOutputStream(outputFile).use { out ->
            document.writeTo(out)
        }
        document.close()
        return outputFile
    }

    // ==========================================
    // 1. MODERN PRO (2 Columns)
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

        val sidebarWidth = 190
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

        // Text Paints
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

        // Candidate Initials Circle or QR Code
        val qrBitmap = if (resume.qrCodeContent.isNotBlank()) {
            QrCodeGenerator.generateQrBitmap(resume.qrCodeContent, 90)
        } else null

        if (qrBitmap != null) {
            canvas.drawBitmap(qrBitmap, (sidebarWidth - 90) / 2f, sidebarY, null)
            sidebarY += 100f
            val qrLabelLayout = createStaticLayout("Scan for Portfolio / LinkedIn", subPaint, sidebarWidth - 20)
            canvas.save()
            canvas.translate(10f, sidebarY)
            qrLabelLayout.draw(canvas)
            canvas.restore()
            sidebarY += qrLabelLayout.height + 14f
        } else {
            // Draw Monogram Badge
            val badgePaint = Paint().apply {
                color = themeColor
                isAntiAlias = true
            }
            val initials = resume.personalInfo.fullName.split(" ")
                .filter { it.isNotBlank() }
                .take(2)
                .map { it.first().uppercase() }
                .joinToString("")
            canvas.drawCircle(sidebarWidth / 2f, sidebarY + 24f, 24f, badgePaint)
            val initialPaint = Paint().apply {
                color = Color.WHITE
                textSize = 16f
                isAntiAlias = true
                textAlign = Paint.Align.CENTER
                this.typeface = Typeface.create(typeface, Typeface.BOLD)
            }
            canvas.drawText(initials.ifBlank { "CF" }, sidebarWidth / 2f, sidebarY + 30f, initialPaint)
            sidebarY += 64f
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

        sidebarY += 14f

        // Skills in Sidebar
        if (resume.skillsList.isNotEmpty()) {
            canvas.drawText("SKILLS", 16f, sidebarY, sectionHeadingPaint)
            sidebarY += 6f
            canvas.drawLine(16f, sidebarY, (sidebarWidth - 16).toFloat(), sidebarY, dividerPaint)
            sidebarY += 12f

            for (skill in resume.skillsList) {
                canvas.drawText("• ${skill.name} (${skill.level})", 16f, sidebarY, bodyPaint)
                sidebarY += 14f
            }
        }

        sidebarY += 14f

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
                sidebarY += school.height + 2f

                if (edu.grade.isNotBlank()) {
                    canvas.drawText(edu.grade, 16f, sidebarY + 8f, subPaint)
                    sidebarY += 14f
                }
                sidebarY += 8f
            }
        }

        // ================= MAIN RIGHT COLUMN =================
        val mainX = (sidebarWidth + margin).toFloat()
        val mainWidth = (pageWidth - sidebarWidth - (margin * 2)).coerceAtLeast(100)
        var mainY = 32f

        // Header: Name & Title
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

        // Professional Summary
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
            mainY += summaryLayout.height + 16f
        }

        // Work Experience
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
                    val bulletPaint = TextPaint(bodyPaint).apply {
                        this.typeface = Typeface.create(typeface, Typeface.ITALIC)
                    }
                    val metricLayout = createStaticLayout("• Key Impact: ${exp.measurableResults}", bulletPaint, mainWidth)
                    canvas.save()
                    canvas.translate(mainX, mainY)
                    metricLayout.draw(canvas)
                    canvas.restore()
                    mainY += metricLayout.height + 10f
                } else {
                    mainY += 8f
                }
            }
        }

        // Key Projects
        if (resume.projectsList.isNotEmpty()) {
            mainY += 6f
            canvas.drawText("KEY PROJECTS", mainX, mainY, sectionHeadingPaint)
            mainY += 6f
            canvas.drawLine(mainX, mainY, (pageWidth - margin).toFloat(), mainY, dividerPaint)
            mainY += 12f

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
                    mainY += descLayout.height + 8f
                } else {
                    mainY += 6f
                }
            }
        }

        // Certifications & Awards
        if (resume.certificationsList.isNotEmpty() || resume.additionalList.isNotEmpty()) {
            mainY += 10f
            canvas.drawText("CERTIFICATIONS & HONORS", mainX, mainY, sectionHeadingPaint)
            mainY += 6f
            canvas.drawLine(mainX, mainY, (pageWidth - margin).toFloat(), mainY, dividerPaint)
            mainY += 12f

            for (cert in resume.certificationsList) {
                canvas.drawText("• ${cert.name} - ${cert.issuingOrganization} (${cert.issueDate})", mainX, mainY, bodyPaint)
                mainY += 14f
            }
            for (add in resume.additionalList) {
                canvas.drawText("• ${add.title} - ${add.subtitle} (${add.date})", mainX, mainY, bodyPaint)
                mainY += 14f
            }
        }

        // Page footer
        val footerPaint = TextPaint().apply {
            color = Color.parseColor("#94A3B8")
            textSize = 7f
            textAlign = Paint.Align.CENTER
            this.typeface = Typeface.create(typeface, Typeface.NORMAL)
        }
        canvas.drawText("Page 1 of 1 • Generated with CareerForge AI Resume Builder", (pageWidth / 2f), (pageHeight - 14f), footerPaint)

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

        // Name
        val name = resume.personalInfo.fullName.ifBlank { "Candidate Name" }
        canvas.drawText(name, margin.toFloat(), y, namePaint)
        y += 16f

        // Headline
        val headline = resume.personalInfo.headline.ifBlank { resume.targetJobTitle }
        canvas.drawText(headline, margin.toFloat(), y, headlinePaint)
        y += 14f

        // Contact info line
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

        // Summary
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
            y += sumLayout.height + 16f
        }

        // Experience
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
            y += 8f
        }

        // Education
        if (resume.educationList.isNotEmpty()) {
            canvas.drawText("EDUCATION", margin.toFloat(), y, sectionHeadingPaint)
            y += 4f
            canvas.drawLine(margin.toFloat(), y, (pageWidth - margin).toFloat(), y, linePaint)
            y += 10f

            for (edu in resume.educationList) {
                canvas.drawText("${edu.degree} in ${edu.fieldOfStudy} - ${edu.institution}", margin.toFloat(), y, sectionHeadingPaint)
                canvas.drawText("${edu.startDate} - ${edu.endDate} | ${edu.grade}", margin.toFloat(), y + 10f, subPaint)
                y += 24f
            }
        }

        // Skills
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
            y += skillLayout.height + 14f
        }

        // Footer
        val footerPaint = TextPaint().apply {
            color = Color.parseColor("#94A3B8")
            textSize = 7f
            textAlign = Paint.Align.CENTER
            this.typeface = Typeface.create(typeface, Typeface.NORMAL)
        }
        canvas.drawText("Page 1 of 1 • CareerForge ATS Clean Template", (pageWidth / 2f), (pageHeight - 14f), footerPaint)

        document.finishPage(page)
    }

    // ==========================================
    // 3. EXECUTIVE (Distinguished Top Banner)
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

        // Header Banner
        val bannerHeight = 85f
        val bannerPaint = Paint().apply {
            color = themeColor
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, pageWidth.toFloat(), bannerHeight, bannerPaint)

        val namePaint = TextPaint().apply {
            color = Color.WHITE
            textSize = 20f
            this.typeface = Typeface.create(typeface, Typeface.BOLD)
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }

        val titlePaint = TextPaint().apply {
            color = Color.parseColor("#E2E8F0")
            textSize = 10f
            this.typeface = Typeface.create(typeface, Typeface.NORMAL)
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }

        canvas.drawText(resume.personalInfo.fullName.ifBlank { "Executive Candidate" }, pageWidth / 2f, 36f, namePaint)
        canvas.drawText(resume.personalInfo.headline.ifBlank { resume.targetJobTitle }, pageWidth / 2f, 54f, titlePaint)

        val p = resume.personalInfo
        val contactLine = listOfNotNull(p.email.ifBlank { null }, p.phone.ifBlank { null }, p.location.ifBlank { null }).joinToString(" | ")
        canvas.drawText(contactLine, pageWidth / 2f, 70f, titlePaint)

        val margin = 28
        val contentWidth = pageWidth - (margin * 2)
        var y = bannerHeight + 24f

        val sectionHeadingPaint = TextPaint().apply {
            color = themeColor
            textSize = 12f
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
            color = Color.parseColor("#475569")
            textSize = 8.5f
            this.typeface = Typeface.create(typeface, Typeface.NORMAL)
            isAntiAlias = true
        }

        val linePaint = Paint().apply {
            color = themeColor
            strokeWidth = 1.5f
        }

        // Summary
        if (resume.summary.isNotBlank()) {
            canvas.drawText("EXECUTIVE PROFILE", margin.toFloat(), y, sectionHeadingPaint)
            y += 4f
            canvas.drawLine(margin.toFloat(), y, (pageWidth - margin).toFloat(), y, linePaint)
            y += 10f

            val sumLayout = createStaticLayout(resume.summary, bodyPaint, contentWidth)
            canvas.save()
            canvas.translate(margin.toFloat(), y)
            sumLayout.draw(canvas)
            canvas.restore()
            y += sumLayout.height + 16f
        }

        // Experience
        if (resume.experienceList.isNotEmpty()) {
            canvas.drawText("LEADERSHIP & WORK EXPERIENCE", margin.toFloat(), y, sectionHeadingPaint)
            y += 4f
            canvas.drawLine(margin.toFloat(), y, (pageWidth - margin).toFloat(), y, linePaint)
            y += 10f

            for (exp in resume.experienceList) {
                canvas.drawText("${exp.jobTitle}  |  ${exp.company}", margin.toFloat(), y, sectionHeadingPaint)
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

        // Education
        if (resume.educationList.isNotEmpty()) {
            canvas.drawText("ACADEMIC CREDENTIALS", margin.toFloat(), y, sectionHeadingPaint)
            y += 4f
            canvas.drawLine(margin.toFloat(), y, (pageWidth - margin).toFloat(), y, linePaint)
            y += 10f

            for (edu in resume.educationList) {
                canvas.drawText("${edu.degree} in ${edu.fieldOfStudy} - ${edu.institution}", margin.toFloat(), y, bodyPaint)
                canvas.drawText("${edu.startDate} - ${edu.endDate} | ${edu.grade}", margin.toFloat(), y + 10f, subPaint)
                y += 22f
            }
        }

        document.finishPage(page)
    }

    // ==========================================
    // 4. TECH DEVELOPER / ENGINEERING
    // ==========================================
    private fun renderTechDeveloper(
        document: PdfDocument,
        resume: ResumeData,
        themeColor: Int,
        typeface: Typeface
    ) {
        renderModernPro(document, resume, themeColor, Typeface.MONOSPACE)
    }

    // ==========================================
    // 5. ACADEMIC & FRESHER
    // ==========================================
    private fun renderAcademicFresher(
        document: PdfDocument,
        resume: ResumeData,
        themeColor: Int,
        typeface: Typeface
    ) {
        renderMinimalist(document, resume, themeColor, typeface)
    }

    // ==========================================
    // 6. CREATIVE PORTFOLIO
    // ==========================================
    private fun renderCreative(
        document: PdfDocument,
        resume: ResumeData,
        themeColor: Int,
        typeface: Typeface
    ) {
        renderModernPro(document, resume, themeColor, typeface)
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
