package com.pynexia.resumebuilder.ui.aitools

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.pynexia.resumebuilder.data.model.ResumeData
import com.pynexia.resumebuilder.data.model.ResumeEntity
import com.pynexia.resumebuilder.databinding.ActivityResumeComparisonBinding
import com.pynexia.resumebuilder.ui.resumes.ResumeViewModel
import com.pynexia.resumebuilder.utils.applySystemBarsPadding

class ResumeComparisonActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResumeComparisonBinding
    private val viewModel: ResumeViewModel by viewModels()
    private val gson = Gson()

    private var resumeList: List<ResumeEntity> = emptyList()
    private var resumeA: ResumeEntity? = null
    private var resumeB: ResumeEntity? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResumeComparisonBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.root.applySystemBarsPadding(applyTop = true, applyBottom = true)

        binding.toolbarCompare.setNavigationOnClickListener { finish() }

        loadResumes()
        binding.btnRunComparison.setOnClickListener {
            compareResumes()
        }
    }

    private fun loadResumes() {
        viewModel.allActiveResumes.observe(this) { list ->
            resumeList = list
            if (list.size >= 2) {
                val titles = list.map { it.title }
                val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, titles)
                binding.spinnerResumeA.adapter = adapter
                binding.spinnerResumeB.adapter = adapter

                binding.spinnerResumeA.setSelection(0)
                binding.spinnerResumeB.setSelection(1)

                resumeA = list[0]
                resumeB = list[1]

                binding.spinnerResumeA.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                        resumeA = list[position]
                    }
                    override fun onNothingSelected(parent: AdapterView<*>?) {}
                }

                binding.spinnerResumeB.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                        resumeB = list[position]
                    }
                    override fun onNothingSelected(parent: AdapterView<*>?) {}
                }

                compareResumes()
            }
        }
    }

    private fun compareResumes() {
        val aEntity = resumeA ?: return
        val bEntity = resumeB ?: return

        val dataA = ResumeData.fromEntity(aEntity, gson)
        val dataB = ResumeData.fromEntity(bEntity, gson)

        val sb = StringBuilder()
        sb.append("📊 SIDE-BY-SIDE COMPARISON\n\n")

        sb.append("1. ATS COMPATIBILITY SCORE:\n")
        sb.append("• ${dataA.title}: ${dataA.lastAtsScore}%\n")
        sb.append("• ${dataB.title}: ${dataB.lastAtsScore}%\n")
        val scoreDiff = dataA.lastAtsScore - dataB.lastAtsScore
        if (scoreDiff > 0) {
            sb.append("💡 ${dataA.title} scores $scoreDiff% higher in keyword density.\n\n")
        } else if (scoreDiff < 0) {
            sb.append("💡 ${dataB.title} scores ${-scoreDiff}% higher in keyword density.\n\n")
        } else {
            sb.append("💡 Both resumes have identical ATS baseline scores.\n\n")
        }

        sb.append("2. WORK EXPERIENCE DEPTH:\n")
        sb.append("• ${dataA.title}: ${dataA.experienceList.size} positions listed\n")
        sb.append("• ${dataB.title}: ${dataB.experienceList.size} positions listed\n\n")

        sb.append("3. SKILLS INVENTORY:\n")
        val skillsA = dataA.skillsList.map { it.name }.toSet()
        val skillsB = dataB.skillsList.map { it.name }.toSet()
        val uniqueToA = skillsA - skillsB
        val uniqueToB = skillsB - skillsA

        sb.append("• Skills in ${dataA.title} only: ${if (uniqueToA.isNotEmpty()) uniqueToA.joinToString(", ") else "None"}\n")
        sb.append("• Skills in ${dataB.title} only: ${if (uniqueToB.isNotEmpty()) uniqueToB.joinToString(", ") else "None"}\n\n")

        sb.append("4. SUMMARY WORD COUNT:\n")
        val wordsA = dataA.summary.split(Regex("\\s+")).filter { it.isNotBlank() }.size
        val wordsB = dataB.summary.split(Regex("\\s+")).filter { it.isNotBlank() }.size
        sb.append("• ${dataA.title}: $wordsA words\n")
        sb.append("• ${dataB.title}: $wordsB words\n")

        binding.tvCompareSummary.text = sb.toString()
    }
}
