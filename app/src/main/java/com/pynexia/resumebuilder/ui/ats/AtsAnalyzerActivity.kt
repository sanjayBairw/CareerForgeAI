package com.pynexia.resumebuilder.ui.ats

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.chip.Chip
import com.google.gson.Gson
import com.pynexia.resumebuilder.R
import com.pynexia.resumebuilder.ai.AtsAnalyzer
import com.pynexia.resumebuilder.data.model.ResumeData
import com.pynexia.resumebuilder.data.model.ResumeEntity
import com.pynexia.resumebuilder.databinding.ActivityAtsAnalyzerBinding
import com.pynexia.resumebuilder.ui.resumes.ResumeViewModel
import com.pynexia.resumebuilder.utils.applySystemBarsPadding
import kotlinx.coroutines.launch

class AtsAnalyzerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAtsAnalyzerBinding
    private val viewModel: ResumeViewModel by viewModels()
    private val gson = Gson()
    private val analyzer = AtsAnalyzer()

    private var resumeList: List<ResumeEntity> = emptyList()
    private var selectedResume: ResumeEntity? = null
    private var isInitialized = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAtsAnalyzerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.root.applySystemBarsPadding(applyTop = true, applyBottom = true)

        binding.toolbarAts.setNavigationOnClickListener { finish() }

        loadResumes()
        setupListeners()
    }

    private fun loadResumes() {
        viewModel.allActiveResumes.observe(this) { list ->
            if (list.isEmpty()) return@observe
            val listChanged = resumeList.map { it.id } != list.map { it.id }
            resumeList = list

            if (!isInitialized || listChanged) {
                isInitialized = true
                val titles = list.map { "${it.title} (${it.targetJobTitle})" }
                val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, titles)
                binding.spinnerResumes.adapter = spinnerAdapter
                selectedResume = list[0]

                // Run initial analysis without triggering DB update recursion
                runAnalysis(saveToDb = false)
            }
        }
    }

    private fun setupListeners() {
        binding.spinnerResumes.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position in resumeList.indices) {
                    val candidate = resumeList[position]
                    if (selectedResume?.id != candidate.id) {
                        selectedResume = candidate
                        runAnalysis(saveToDb = false)
                    }
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        binding.btnRunAtsAnalysis.setOnClickListener {
            runAnalysis(saveToDb = true)
        }
    }

    private fun runAnalysis(saveToDb: Boolean = false) {
        val entity = selectedResume ?: return
        val resumeData = ResumeData.fromEntity(entity, gson)
        val jdText = binding.etJobDescription.text?.toString().orEmpty()

        val result = analyzer.analyze(resumeData, jdText)

        // Update score in DB only when explicitly requested and changed
        if (saveToDb && entity.lastAtsScore != result.overallScore) {
            entity.lastAtsScore = result.overallScore
            viewModel.updateResume(entity)
        }

        // Display results
        binding.cardAtsResults.visibility = View.VISIBLE
        binding.tvAtsScoreNumber.text = "${result.overallScore}/100"
        binding.tvAtsScoreRating.text = result.rating

        if (result.overallScore >= 85) {
            binding.tvAtsScoreNumber.setTextColor(Color.parseColor("#059669"))
            binding.tvAtsScoreNumber.setBackgroundResource(R.drawable.bg_badge_ats)
        } else if (result.overallScore >= 70) {
            binding.tvAtsScoreNumber.setTextColor(Color.parseColor("#D97706"))
            binding.tvAtsScoreNumber.setBackgroundResource(R.drawable.bg_badge_warning)
        } else {
            binding.tvAtsScoreNumber.setTextColor(Color.parseColor("#DC2626"))
            binding.tvAtsScoreNumber.setBackgroundResource(R.drawable.bg_badge_neutral)
        }

        binding.tvReadabilityAndMetrics.text = "Readability: ${result.readabilityScore}% • Quantified Metric Bullets: ${result.quantifiedMetricsCount}"

        // Matched Keywords Chips
        binding.chipGroupMatchedKeywords.removeAllViews()
        for (kw in result.matchedKeywords) {
            val chip = Chip(this).apply {
                text = kw
                setTextColor(Color.parseColor("#059669"))
                setChipBackgroundColorResource(R.color.accent_ats_light)
            }
            binding.chipGroupMatchedKeywords.addView(chip)
        }
        if (result.matchedKeywords.isEmpty()) {
            val chip = Chip(this).apply { text = "Paste a target job description to match keywords" }
            binding.chipGroupMatchedKeywords.addView(chip)
        }

        // Missing Keywords Chips
        binding.chipGroupMissingKeywords.removeAllViews()
        for (kw in result.missingKeywords) {
            val chip = Chip(this).apply {
                text = "+ $kw"
                setTextColor(Color.parseColor("#DC2626"))
                setChipBackgroundColorResource(R.color.accent_danger_light)
            }
            binding.chipGroupMissingKeywords.addView(chip)
        }
        if (result.missingKeywords.isEmpty()) {
            val chip = Chip(this).apply { text = "No critical missing keywords detected" }
            binding.chipGroupMissingKeywords.addView(chip)
        }

        // Section Breakdown
        val sectionSb = StringBuilder()
        for ((section, score) in result.sectionScores) {
            sectionSb.append("• ").append(section).append(": ").append(score).append("%\n")
        }
        binding.tvSectionScores.text = sectionSb.toString().trimEnd()

        // Recommendations
        binding.layoutRecommendationsContainer.removeAllViews()
        for (rec in result.recommendations) {
            val recView = LayoutInflater.from(this).inflate(R.layout.item_ats_recommendation, binding.layoutRecommendationsContainer, false)
            val tvTitle = recView.findViewById<TextView>(R.id.tv_rec_title)
            val tvDesc = recView.findViewById<TextView>(R.id.tv_rec_desc)

            tvTitle.text = "[${rec.severity}] ${rec.title}"
            tvDesc.text = rec.description
            binding.layoutRecommendationsContainer.addView(recView)
        }

        if (saveToDb) {
            Toast.makeText(this, "Analysis updated (Score: ${result.overallScore}%)", Toast.LENGTH_SHORT).show()
        }
    }
}
