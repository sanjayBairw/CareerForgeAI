package com.pynexia.resumebuilder.ui.aitools

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.gson.Gson
import com.pynexia.resumebuilder.R
import com.pynexia.resumebuilder.ai.HeadlineOption
import com.pynexia.resumebuilder.ai.LinkedInOptimizationResult
import com.pynexia.resumebuilder.ai.LinkedInOptimizerEngine
import com.pynexia.resumebuilder.data.model.ResumeData
import com.pynexia.resumebuilder.data.model.ResumeEntity
import com.pynexia.resumebuilder.databinding.ActivityLinkedinOptimizerBinding
import com.pynexia.resumebuilder.ui.resumes.ResumeViewModel
import com.pynexia.resumebuilder.utils.applySystemBarsPadding

class LinkedInOptimizerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLinkedinOptimizerBinding
    private val viewModel: ResumeViewModel by viewModels()
    private val gson = Gson()
    private val optimizerEngine = LinkedInOptimizerEngine()

    private var resumeList: List<ResumeEntity> = emptyList()
    private var lastResult: LinkedInOptimizationResult? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLinkedinOptimizerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.root.applySystemBarsPadding(applyTop = true, applyBottom = true)

        binding.toolbarLinkedin.setNavigationOnClickListener { finish() }

        loadResumes()
        setupListeners()
    }

    private fun loadResumes() {
        viewModel.allActiveResumes.observe(this) { list ->
            resumeList = list
            if (list.isNotEmpty()) {
                val titles = list.map { "${it.title} (${it.targetJobTitle})" }
                val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, titles)
                binding.spinnerLinkedinResume.adapter = spinnerAdapter
            }
        }
    }

    private fun setupListeners() {
        binding.btnGenerateLinkedinAssets.setOnClickListener {
            runOptimization()
        }

        binding.btnCopyAbout.setOnClickListener {
            val text = binding.tvLinkedinAbout.text.toString()
            if (text.isNotBlank()) {
                copyToClipboard("LinkedIn About Summary", text)
            }
        }

        binding.btnCopyTags.setOnClickListener {
            val tags = lastResult?.topKeywords?.joinToString(" ")
            if (!tags.isNullOrBlank()) {
                copyToClipboard("LinkedIn Hashtags", tags)
            }
        }
    }

    private fun runOptimization() {
        if (resumeList.isEmpty()) {
            Toast.makeText(this, "Please create a resume first to optimize your profile.", Toast.LENGTH_SHORT).show()
            return
        }

        val selectedIndex = binding.spinnerLinkedinResume.selectedItemPosition.coerceAtLeast(0)
        val entity = resumeList.getOrNull(selectedIndex) ?: return
        val resumeData = ResumeData.fromEntity(entity, gson)
        val targetRole = binding.etTargetRole.text?.toString().orEmpty()

        val result = optimizerEngine.generateOptimization(resumeData, targetRole)
        lastResult = result

        displayResults(result)
        Toast.makeText(this, "LinkedIn Profile Assets Generated!", Toast.LENGTH_SHORT).show()
    }

    private fun displayResults(result: LinkedInOptimizationResult) {
        binding.layoutLinkedinResults.visibility = View.VISIBLE

        // 1. Populate Headlines
        binding.containerHeadlines.removeAllViews()
        for (headline in result.headlines) {
            val headlineView = createHeadlineItemView(headline)
            binding.containerHeadlines.addView(headlineView)
        }

        // 2. Populate About
        binding.tvLinkedinAbout.text = result.aboutSummary

        // 3. Populate Keywords
        binding.chipGroupKeywords.removeAllViews()
        for (tag in result.topKeywords) {
            val chip = Chip(this).apply {
                text = tag
                isClickable = true
                setOnClickListener {
                    copyToClipboard("Hashtag", tag)
                }
            }
            binding.chipGroupKeywords.addView(chip)
        }
    }

    private fun createHeadlineItemView(headline: HeadlineOption): View {
        val container = layoutInflater.inflate(R.layout.item_card_action_row, binding.containerHeadlines, false)
        val tvCategory = container.findViewById<TextView>(R.id.tv_item_badge)
        val tvText = container.findViewById<TextView>(R.id.tv_item_text)
        val tvDesc = container.findViewById<TextView>(R.id.tv_item_desc)
        val btnCopy = container.findViewById<MaterialButton>(R.id.btn_item_copy)

        tvCategory.text = headline.category
        tvText.text = headline.text
        tvDesc.text = headline.description

        btnCopy.setOnClickListener {
            copyToClipboard("LinkedIn Headline", headline.text)
        }

        return container
    }

    private fun copyToClipboard(label: String, content: String) {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, content)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(this, "Copied $label to clipboard!", Toast.LENGTH_SHORT).show()
    }
}
