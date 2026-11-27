package com.pynexia.resumebuilder.ui.preview

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.chip.Chip
import com.google.gson.Gson
import com.pynexia.resumebuilder.data.model.ResumeData
import com.pynexia.resumebuilder.data.model.ResumeEntity
import com.pynexia.resumebuilder.databinding.ActivityResumePreviewBinding
import com.pynexia.resumebuilder.pdf.PdfGenerator
import com.pynexia.resumebuilder.pdf.PdfPreviewManager
import com.pynexia.resumebuilder.pdf.TemplateRegistry
import com.pynexia.resumebuilder.pdf.TextResumeExporter
import com.pynexia.resumebuilder.ui.editor.ResumeEditorActivity
import com.pynexia.resumebuilder.ui.resumes.ResumeViewModel
import com.pynexia.resumebuilder.utils.applySystemBarsPadding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class ResumePreviewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResumePreviewBinding
    private val viewModel: ResumeViewModel by viewModels()
    private val gson = Gson()

    private var resumeId: Long = 0
    private var currentEntity: ResumeEntity? = null
    private var resumeData: ResumeData = ResumeData()
    private var generatedPdfFile: File? = null
    private lateinit var pageAdapter: PdfPageAdapter
    private var isSettingUpChips = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResumePreviewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.root.applySystemBarsPadding(applyTop = true, applyBottom = true)

        resumeId = intent.getLongExtra(EXTRA_RESUME_ID, 0L)

        setupToolbar()
        setupViewPager()
        setupColorPalette()
        loadAndRenderPdf()
    }

    private fun setupToolbar() {
        binding.toolbarPreview.setNavigationOnClickListener {
            finish()
        }

        binding.btnPreviewShare.setOnClickListener {
            sharePdf()
        }

        binding.btnCopyPlainText.setOnClickListener {
            copyPlainTextResume()
        }

        binding.btnPreviewEdit.setOnClickListener {
            val intent = Intent(this, ResumeEditorActivity::class.java).apply {
                putExtra(ResumeEditorActivity.EXTRA_RESUME_ID, resumeId)
            }
            startActivity(intent)
            finish()
        }

        binding.btnPreviewExport.setOnClickListener {
            exportPdfToDevice()
        }
    }

    private fun setupViewPager() {
        pageAdapter = PdfPageAdapter()
        binding.vpPdfPages.adapter = pageAdapter
        binding.vpPdfPages.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                binding.tvPageIndicator.text = "Page ${position + 1} of ${pageAdapter.itemCount.coerceAtLeast(1)}"
            }
        })
    }

    private fun setupColorPalette() {
        val colorViews = listOf(
            binding.previewDotBlue to "#2563EB",
            binding.previewDotEmerald to "#059669",
            binding.previewDotPurple to "#7C3AED",
            binding.previewDotCrimson to "#DC2626",
            binding.previewDotSlate to "#0F172A",
            binding.previewDotAmber to "#D97706"
        )

        fun updateDotSelection(activeColor: String) {
            colorViews.forEach { (view, color) ->
                view.alpha = if (color.equals(activeColor, ignoreCase = true)) 1.0f else 0.45f
                view.scaleX = if (color.equals(activeColor, ignoreCase = true)) 1.25f else 1.0f
                view.scaleY = if (color.equals(activeColor, ignoreCase = true)) 1.25f else 1.0f
            }
        }

        colorViews.forEach { (view, color) ->
            view.setOnClickListener {
                if (resumeData.colorTheme.equals(color, ignoreCase = true)) return@setOnClickListener
                resumeData.colorTheme = color
                updateDotSelection(color)
                persistAndReRender()
            }
        }
    }

    private fun setupLiveTemplateChips() {
        isSettingUpChips = true
        binding.chipGroupTemplates.removeAllViews()

        for (template in TemplateRegistry.templates) {
            val chip = Chip(this).apply {
                text = template.name
                isCheckable = true
                isChecked = (template.id == resumeData.templateId)
                setOnClickListener {
                    if (resumeData.templateId != template.id) {
                        resumeData.templateId = template.id
                        binding.tvPreviewTemplateName.text = template.name
                        persistAndReRender()
                    }
                }
            }
            binding.chipGroupTemplates.addView(chip)
        }
        isSettingUpChips = false
    }

    private fun persistAndReRender() {
        val entity = currentEntity ?: return
        entity.templateId = resumeData.templateId
        entity.colorTheme = resumeData.colorTheme
        viewModel.updateResume(entity)
        renderPdfOnly()
    }

    private fun loadAndRenderPdf() {
        binding.pbPdfRendering.visibility = View.VISIBLE
        lifecycleScope.launch {
            val entity = viewModel.getResumeById(resumeId)
            if (entity != null) {
                currentEntity = entity
                resumeData = ResumeData.fromEntity(entity, gson)
                val template = TemplateRegistry.getById(resumeData.templateId)
                binding.tvPreviewTemplateName.text = template.name

                setupLiveTemplateChips()
                renderPdfOnly()
            } else {
                binding.pbPdfRendering.visibility = View.GONE
                Toast.makeText(this@ResumePreviewActivity, "Resume not found", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun renderPdfOnly() {
        binding.pbPdfRendering.visibility = View.VISIBLE
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                val cacheDir = File(cacheDir, "resumes").apply { mkdirs() }
                val sanitized = resumeData.title.replace(Regex("[^a-zA-Z0-9_-]"), "_")
                val pdfFile = File(cacheDir, "${sanitized}_${resumeId}.pdf")
                val generator = PdfGenerator(this@ResumePreviewActivity)
                generator.generatePdf(resumeData, pdfFile)
                generatedPdfFile = pdfFile

                val previewManager = PdfPreviewManager(this@ResumePreviewActivity)
                val pages = previewManager.renderPdfPages(pdfFile)

                withContext(Dispatchers.Main) {
                    binding.pbPdfRendering.visibility = View.GONE
                    pageAdapter.setPages(pages)
                    binding.tvPageIndicator.text = "Page 1 of ${pages.size.coerceAtLeast(1)}"
                }
            }
        }
    }

    private fun sharePdf() {
        val file = generatedPdfFile
        if (file == null || !file.exists()) {
            Toast.makeText(this, "PDF not ready yet", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val uri = FileProvider.getUriForFile(this, "${packageName}.fileprovider", file)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "${resumeData.title} - Resume")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(Intent.createChooser(intent, "Share Resume PDF via"))
        } catch (e: Exception) {
            Toast.makeText(this, "Could not share PDF: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun exportPdfToDevice() {
        val srcFile = generatedPdfFile
        if (srcFile == null || !srcFile.exists()) {
            Toast.makeText(this, "PDF not ready", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val docsDir = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS) ?: filesDir
                val exportDir = File(docsDir, "resumes").apply { mkdirs() }
                val targetFile = File(exportDir, "${resumeData.title.replace(" ", "_")}.pdf")

                FileInputStream(srcFile).use { input ->
                    FileOutputStream(targetFile).use { output ->
                        input.copyTo(output)
                    }
                }

                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@ResumePreviewActivity,
                        "PDF Saved Successfully:\n${targetFile.name}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@ResumePreviewActivity,
                        "Export error: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun copyPlainTextResume() {
        val text = TextResumeExporter.export(resumeData)
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Resume Text", text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(this, "Resume copied as plain text! Ready for job portals.", Toast.LENGTH_LONG).show()
    }

    companion object {
        const val EXTRA_RESUME_ID = "extra_resume_id"
    }
}
