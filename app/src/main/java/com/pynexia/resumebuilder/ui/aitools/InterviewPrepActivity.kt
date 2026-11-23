package com.pynexia.resumebuilder.ui.aitools

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.pynexia.resumebuilder.ai.InterviewPrepEngine
import com.pynexia.resumebuilder.data.model.ResumeData
import com.pynexia.resumebuilder.data.model.ResumeEntity
import com.pynexia.resumebuilder.databinding.ActivityInterviewPrepBinding
import com.pynexia.resumebuilder.ui.resumes.ResumeViewModel
import com.pynexia.resumebuilder.utils.applySystemBarsPadding

class InterviewPrepActivity : AppCompatActivity() {

    private lateinit var binding: ActivityInterviewPrepBinding
    private val viewModel: ResumeViewModel by viewModels()
    private val gson = Gson()
    private val prepEngine = InterviewPrepEngine()
    private lateinit var adapter: InterviewQuestionAdapter

    private var resumeList: List<ResumeEntity> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInterviewPrepBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.root.applySystemBarsPadding(applyTop = true, applyBottom = true)

        binding.toolbarInterview.setNavigationOnClickListener { finish() }

        setupRecyclerView()
        loadResumes()

        binding.btnGenerateInterviewQuestions.setOnClickListener {
            generateQuestions(showToast = true)
        }
    }

    private fun setupRecyclerView() {
        adapter = InterviewQuestionAdapter()
        binding.rvInterviewQuestions.apply {
            layoutManager = LinearLayoutManager(this@InterviewPrepActivity)
            adapter = this@InterviewPrepActivity.adapter
        }
    }

    private fun loadResumes() {
        viewModel.allActiveResumes.observe(this) { list ->
            resumeList = list
            if (list.isNotEmpty()) {
                val titles = list.map { "${it.title} (${it.targetJobTitle})" }
                val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, titles)
                binding.spinnerInterviewResume.adapter = spinnerAdapter
                generateQuestions(showToast = false)
            }
        }
    }

    private fun generateQuestions(showToast: Boolean = true) {
        val selectedIndex = binding.spinnerInterviewResume.selectedItemPosition.coerceAtLeast(0)
        if (selectedIndex in resumeList.indices) {
            val entity = resumeList[selectedIndex]
            val data = ResumeData.fromEntity(entity, gson)
            val questions = prepEngine.generateQuestions(data)
            adapter.submitList(questions)
            if (showToast) {
                Toast.makeText(this, "Generated ${questions.size} tailored questions!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
