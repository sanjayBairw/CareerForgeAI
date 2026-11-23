package com.pynexia.resumebuilder.ui.editor

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayoutMediator
import com.google.android.material.textfield.TextInputEditText
import com.google.gson.Gson
import com.pynexia.resumebuilder.R
import com.pynexia.resumebuilder.ai.AiResumeAssistant
import com.pynexia.resumebuilder.data.model.EducationItem
import com.pynexia.resumebuilder.data.model.ExperienceItem
import com.pynexia.resumebuilder.data.model.ProjectItem
import com.pynexia.resumebuilder.data.model.ResumeData
import com.pynexia.resumebuilder.data.model.SkillItem
import com.pynexia.resumebuilder.databinding.ActivityResumeEditorBinding
import com.pynexia.resumebuilder.databinding.StepDesignBinding
import com.pynexia.resumebuilder.databinding.StepEducationBinding
import com.pynexia.resumebuilder.databinding.StepExperienceBinding
import com.pynexia.resumebuilder.databinding.StepPersonalInfoBinding
import com.pynexia.resumebuilder.databinding.StepProjectsBinding
import com.pynexia.resumebuilder.databinding.StepSkillsBinding
import com.pynexia.resumebuilder.databinding.StepSummaryBinding
import com.pynexia.resumebuilder.pdf.TemplateRegistry
import com.pynexia.resumebuilder.ui.preview.ResumePreviewActivity
import com.pynexia.resumebuilder.ui.resumes.ResumeViewModel
import com.pynexia.resumebuilder.utils.applySystemBarsPadding
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ResumeEditorActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResumeEditorBinding
    private val viewModel: ResumeViewModel by viewModels()
    private val gson = Gson()
    private lateinit var aiAssistant: AiResumeAssistant

    private var resumeId: Long = 0
    private var resumeData: ResumeData = ResumeData()
    private var isBinding: Boolean = false

    // Undo / Redo stacks
    private val undoStack = mutableListOf<String>()
    private val redoStack = mutableListOf<String>()
    private var autosaveJob: Job? = null

    // Step View Bindings
    private lateinit var personalBinding: StepPersonalInfoBinding
    private lateinit var summaryBinding: StepSummaryBinding
    private lateinit var expBinding: StepExperienceBinding
    private lateinit var eduBinding: StepEducationBinding
    private lateinit var skillsBinding: StepSkillsBinding
    private lateinit var projBinding: StepProjectsBinding
    private lateinit var designBinding: StepDesignBinding

    private lateinit var expAdapter: ExperienceAdapter
    private lateinit var eduAdapter: EducationAdapter
    private lateinit var projAdapter: ProjectAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResumeEditorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.root.applySystemBarsPadding(applyTop = true, applyBottom = true)

        aiAssistant = AiResumeAssistant(this)
        resumeId = intent.getLongExtra(EXTRA_RESUME_ID, 0L)

        setupToolbar()
        inflateStepViews()
        setupStepNavigation()
        loadResume()
    }

    private fun setupToolbar() {
        binding.toolbarEditor.setNavigationOnClickListener {
            finish()
        }

        binding.btnUndo.setOnClickListener {
            if (undoStack.size > 1) {
                val current = undoStack.removeAt(undoStack.lastIndex)
                redoStack.add(current)
                val prev = undoStack.last()
                applySnapshot(prev)
            } else {
                Toast.makeText(this, "Nothing to undo", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnRedo.setOnClickListener {
            if (redoStack.isNotEmpty()) {
                val next = redoStack.removeAt(redoStack.lastIndex)
                undoStack.add(next)
                applySnapshot(next)
            } else {
                Toast.makeText(this, "Nothing to redo", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnEditorPreview.setOnClickListener {
            val intent = Intent(this, ResumePreviewActivity::class.java).apply {
                putExtra(ResumePreviewActivity.EXTRA_RESUME_ID, resumeId)
            }
            startActivity(intent)
        }
    }

    private fun inflateStepViews() {
        personalBinding = StepPersonalInfoBinding.inflate(layoutInflater)
        summaryBinding = StepSummaryBinding.inflate(layoutInflater)
        expBinding = StepExperienceBinding.inflate(layoutInflater)
        eduBinding = StepEducationBinding.inflate(layoutInflater)
        skillsBinding = StepSkillsBinding.inflate(layoutInflater)
        projBinding = StepProjectsBinding.inflate(layoutInflater)
        designBinding = StepDesignBinding.inflate(layoutInflater)

        setupPersonalStep()
        setupSummaryStep()
        setupExperienceStep()
        setupEducationStep()
        setupSkillsStep()
        setupProjectsStep()
        setupDesignStep()
    }

    private fun setupStepNavigation() {
        val views = listOf(
            personalBinding.root,
            summaryBinding.root,
            expBinding.root,
            eduBinding.root,
            skillsBinding.root,
            projBinding.root,
            designBinding.root
        )

        val tabTitles = listOf("Personal", "Summary", "Experience", "Education", "Skills", "Projects", "Design")
        for (title in tabTitles) {
            val tab = binding.tabLayoutEditor.newTab().setText(title)
            binding.tabLayoutEditor.addTab(tab)
        }

        binding.containerStepContent.removeAllViews()
        binding.containerStepContent.addView(views[0])

        binding.tabLayoutEditor.addOnTabSelectedListener(object : com.google.android.material.tabs.TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: com.google.android.material.tabs.TabLayout.Tab?) {
                tab?.position?.let { pos ->
                    if (pos in views.indices) {
                        binding.containerStepContent.removeAllViews()
                        val targetView = views[pos]
                        (targetView.parent as? android.view.ViewGroup)?.removeView(targetView)
                        binding.containerStepContent.addView(targetView)
                    }
                }
            }
            override fun onTabUnselected(tab: com.google.android.material.tabs.TabLayout.Tab?) {}
            override fun onTabReselected(tab: com.google.android.material.tabs.TabLayout.Tab?) {}
        })
    }

    private fun loadResume() {
        lifecycleScope.launch {
            val entity = viewModel.getResumeById(resumeId)
            if (entity != null) {
                resumeData = ResumeData.fromEntity(entity, gson)
                bindDataToViews()
                saveSnapshot()
            }
        }
    }

    private fun bindDataToViews() {
        isBinding = true
        try {
            // Personal
            personalBinding.etEditResumeTitle.setText(resumeData.title)
            personalBinding.etEditFullName.setText(resumeData.personalInfo.fullName)
            personalBinding.etEditHeadline.setText(resumeData.personalInfo.headline)
            personalBinding.etEditEmail.setText(resumeData.personalInfo.email)
            personalBinding.etEditPhone.setText(resumeData.personalInfo.phone)
            personalBinding.etEditLocation.setText(resumeData.personalInfo.location)
            personalBinding.etEditLinkedin.setText(resumeData.personalInfo.linkedInUrl)
            personalBinding.etEditGithub.setText(resumeData.personalInfo.githubUrl)
            personalBinding.etEditPortfolio.setText(resumeData.personalInfo.portfolioUrl)

            // Summary
            summaryBinding.etEditSummary.setText(resumeData.summary)
            updateSummaryWordCount(resumeData.summary)

            // Lists
            expAdapter.notifyDataSetChanged()
            eduAdapter.notifyDataSetChanged()
            projAdapter.notifyDataSetChanged()
            populateSkillsChips()

            // Design
            val curIndex = TemplateRegistry.templates.indexOfFirst { it.id == resumeData.templateId }.coerceAtLeast(0)
            designBinding.spinnerDesignTemplate.setSelection(curIndex)
            when (resumeData.fontFamily) {
                "serif" -> designBinding.rbFontSerif.isChecked = true
                "monospace" -> designBinding.rbFontMonospace.isChecked = true
                else -> designBinding.rbFontRoboto.isChecked = true
            }
            designBinding.etDesignQrCode.setText(resumeData.qrCodeContent)
        } finally {
            isBinding = false
        }
    }

    private fun setupPersonalStep() {
        val watcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (isBinding) return
                resumeData.title = personalBinding.etEditResumeTitle.text.toString()
                resumeData.personalInfo.fullName = personalBinding.etEditFullName.text.toString()
                resumeData.personalInfo.headline = personalBinding.etEditHeadline.text.toString()
                resumeData.personalInfo.email = personalBinding.etEditEmail.text.toString()
                resumeData.personalInfo.phone = personalBinding.etEditPhone.text.toString()
                resumeData.personalInfo.location = personalBinding.etEditLocation.text.toString()
                resumeData.personalInfo.linkedInUrl = personalBinding.etEditLinkedin.text.toString()
                resumeData.personalInfo.githubUrl = personalBinding.etEditGithub.text.toString()
                resumeData.personalInfo.portfolioUrl = personalBinding.etEditPortfolio.text.toString()
                triggerAutosave()
            }
        }

        personalBinding.etEditResumeTitle.addTextChangedListener(watcher)
        personalBinding.etEditFullName.addTextChangedListener(watcher)
        personalBinding.etEditHeadline.addTextChangedListener(watcher)
        personalBinding.etEditEmail.addTextChangedListener(watcher)
        personalBinding.etEditPhone.addTextChangedListener(watcher)
        personalBinding.etEditLocation.addTextChangedListener(watcher)
        personalBinding.etEditLinkedin.addTextChangedListener(watcher)
        personalBinding.etEditGithub.addTextChangedListener(watcher)
        personalBinding.etEditPortfolio.addTextChangedListener(watcher)
    }

    private fun setupSummaryStep() {
        summaryBinding.etEditSummary.doAfterTextChanged { text ->
            if (isBinding) return@doAfterTextChanged
            resumeData.summary = text?.toString().orEmpty()
            updateSummaryWordCount(resumeData.summary)
            triggerAutosave()
        }

        summaryBinding.btnAiGenerateSummary.setOnClickListener {
            val selectedTone = when {
                summaryBinding.chipToneExecutive.isChecked -> "Executive"
                summaryBinding.chipToneTechnical.isChecked -> "Technical"
                summaryBinding.chipToneFresher.isChecked -> "Fresh Graduate"
                else -> "Impactful"
            }

            lifecycleScope.launch {
                val summary = aiAssistant.generateSummary(
                    jobTitle = resumeData.personalInfo.headline.ifBlank { resumeData.targetJobTitle },
                    industry = resumeData.targetIndustry,
                    yearsOfExp = "6+ years",
                    keySkills = resumeData.skillsList.map { it.name },
                    tone = selectedTone
                )
                summaryBinding.etEditSummary.setText(summary)
                resumeData.summary = summary
                triggerAutosave()
                Toast.makeText(this@ResumeEditorActivity, "Summary Generated!", Toast.LENGTH_SHORT).show()
            }
        }

        summaryBinding.btnAiPolishSummary.setOnClickListener {
            val current = summaryBinding.etEditSummary.text.toString()
            if (current.isNotBlank()) {
                lifecycleScope.launch {
                    val polished = aiAssistant.polishSentence(current)
                    summaryBinding.etEditSummary.setText(polished)
                    resumeData.summary = polished
                    triggerAutosave()
                    Toast.makeText(this@ResumeEditorActivity, "Tone Polished!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun updateSummaryWordCount(text: String) {
        val count = if (text.isBlank()) 0 else text.trim().split(Regex("\\s+")).size
        summaryBinding.tvSummaryWordCount.text = "$count words (Optimal: 40-90 words)"
    }

    private fun setupExperienceStep() {
        expAdapter = ExperienceAdapter(resumeData.experienceList) { position ->
            resumeData.experienceList.removeAt(position)
            expAdapter.notifyItemRemoved(position)
            triggerAutosave()
        }
        expBinding.rvEditExperience.apply {
            layoutManager = LinearLayoutManager(this@ResumeEditorActivity)
            adapter = expAdapter
        }

        expBinding.btnAddExperience.setOnClickListener {
            showAddExperienceDialog()
        }
    }

    private fun showAddExperienceDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.item_experience, null)
        val etRole = TextInputEditText(this).apply { hint = "Job Title (e.g. Senior Software Engineer)" }
        val etCompany = TextInputEditText(this).apply { hint = "Company Name (e.g. Apex Tech)" }
        val etDates = TextInputEditText(this).apply { hint = "Dates (e.g. Jan 2022 - Present)" }
        val etLocation = TextInputEditText(this).apply { hint = "Location (e.g. San Francisco, CA)" }
        val etResp = TextInputEditText(this).apply {
            hint = "Responsibilities & STAR Accomplishments"
            minLines = 3
        }

        val layout = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setPadding(50, 40, 50, 20)
            addView(etRole)
            addView(etCompany)
            addView(etDates)
            addView(etLocation)
            addView(etResp)
        }

        MaterialAlertDialogBuilder(this)
            .setTitle("Add Work Experience")
            .setView(layout)
            .setPositiveButton("Add") { _, _ ->
                val role = etRole.text.toString().trim()
                val company = etCompany.text.toString().trim()
                if (role.isNotBlank() && company.isNotBlank()) {
                    val item = ExperienceItem(
                        jobTitle = role,
                        company = company,
                        startDate = etDates.text.toString().trim(),
                        location = etLocation.text.toString().trim(),
                        responsibilities = etResp.text.toString().trim()
                    )
                    resumeData.experienceList.add(item)
                    expAdapter.notifyItemInserted(resumeData.experienceList.size - 1)
                    triggerAutosave()
                } else {
                    Toast.makeText(this, "Job title and company required", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun setupEducationStep() {
        eduAdapter = EducationAdapter(resumeData.educationList) { position ->
            resumeData.educationList.removeAt(position)
            eduAdapter.notifyItemRemoved(position)
            triggerAutosave()
        }
        eduBinding.rvEditEducation.apply {
            layoutManager = LinearLayoutManager(this@ResumeEditorActivity)
            adapter = eduAdapter
        }

        eduBinding.btnAddEducation.setOnClickListener {
            showAddEducationDialog()
        }
    }

    private fun showAddEducationDialog() {
        val etDegree = TextInputEditText(this).apply { hint = "Degree (e.g. Bachelor of Science)" }
        val etField = TextInputEditText(this).apply { hint = "Field of Study (e.g. Computer Science)" }
        val etSchool = TextInputEditText(this).apply { hint = "Institution (e.g. UC Berkeley)" }
        val etDates = TextInputEditText(this).apply { hint = "Dates (e.g. 2016 - 2020)" }
        val etGrade = TextInputEditText(this).apply { hint = "Grade / CGPA (e.g. GPA 3.85 / 4.0)" }

        val layout = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setPadding(50, 40, 50, 20)
            addView(etDegree)
            addView(etField)
            addView(etSchool)
            addView(etDates)
            addView(etGrade)
        }

        MaterialAlertDialogBuilder(this)
            .setTitle("Add Education")
            .setView(layout)
            .setPositiveButton("Add") { _, _ ->
                val degree = etDegree.text.toString().trim()
                val school = etSchool.text.toString().trim()
                if (degree.isNotBlank() && school.isNotBlank()) {
                    val item = EducationItem(
                        degree = degree,
                        fieldOfStudy = etField.text.toString().trim(),
                        institution = school,
                        startDate = etDates.text.toString().trim(),
                        grade = etGrade.text.toString().trim()
                    )
                    resumeData.educationList.add(item)
                    eduAdapter.notifyItemInserted(resumeData.educationList.size - 1)
                    triggerAutosave()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun setupSkillsStep() {
        skillsBinding.btnAddSkill.setOnClickListener {
            val name = skillsBinding.etNewSkillName.text?.toString()?.trim().orEmpty()
            if (name.isNotBlank()) {
                resumeData.skillsList.add(SkillItem(name = name))
                skillsBinding.etNewSkillName.setText("")
                populateSkillsChips()
                triggerAutosave()
            }
        }

        skillsBinding.btnAiSuggestSkills.setOnClickListener {
            val suggestions = aiAssistant.recommendSkills(resumeData.personalInfo.headline.ifBlank { resumeData.targetJobTitle })
            for (s in suggestions) {
                if (resumeData.skillsList.none { it.name.equals(s, ignoreCase = true) }) {
                    resumeData.skillsList.add(SkillItem(name = s))
                }
            }
            populateSkillsChips()
            triggerAutosave()
            Toast.makeText(this, "Target skills added!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun populateSkillsChips() {
        skillsBinding.chipGroupEditSkills.removeAllViews()
        for ((index, skill) in resumeData.skillsList.withIndex()) {
            val chip = Chip(this).apply {
                text = skill.name
                isCloseIconVisible = true
                setOnCloseIconClickListener {
                    resumeData.skillsList.removeAt(index)
                    populateSkillsChips()
                    triggerAutosave()
                }
            }
            skillsBinding.chipGroupEditSkills.addView(chip)
        }
    }

    private fun setupProjectsStep() {
        projAdapter = ProjectAdapter(resumeData.projectsList) { position ->
            resumeData.projectsList.removeAt(position)
            projAdapter.notifyItemRemoved(position)
            triggerAutosave()
        }
        projBinding.rvEditProjects.apply {
            layoutManager = LinearLayoutManager(this@ResumeEditorActivity)
            adapter = projAdapter
        }

        projBinding.btnAddProject.setOnClickListener {
            showAddProjectDialog()
        }
    }

    private fun showAddProjectDialog() {
        val etTitle = TextInputEditText(this).apply { hint = "Project Title (e.g. Distributed Engine)" }
        val etStack = TextInputEditText(this).apply { hint = "Tech Stack (e.g. Kotlin, Coroutines, Room)" }
        val etDesc = TextInputEditText(this).apply {
            hint = "Description & Key Accomplishments"
            minLines = 3
        }

        val layout = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setPadding(50, 40, 50, 20)
            addView(etTitle)
            addView(etStack)
            addView(etDesc)
        }

        MaterialAlertDialogBuilder(this)
            .setTitle("Add Project")
            .setView(layout)
            .setPositiveButton("Add") { _, _ ->
                val title = etTitle.text.toString().trim()
                if (title.isNotBlank()) {
                    val proj = ProjectItem(
                        title = title,
                        techStack = etStack.text.toString().trim(),
                        description = etDesc.text.toString().trim()
                    )
                    resumeData.projectsList.add(proj)
                    projAdapter.notifyItemInserted(resumeData.projectsList.size - 1)
                    triggerAutosave()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun setupDesignStep() {
        val templates = TemplateRegistry.templates.map { it.name }
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, templates)
        designBinding.spinnerDesignTemplate.adapter = spinnerAdapter

        val curIndex = TemplateRegistry.templates.indexOfFirst { it.id == resumeData.templateId }.coerceAtLeast(0)
        designBinding.spinnerDesignTemplate.setSelection(curIndex)

        designBinding.spinnerDesignTemplate.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (isBinding) return
                resumeData.templateId = TemplateRegistry.templates[position].id
                triggerAutosave()
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        }

        designBinding.swatchBlue.setOnClickListener { resumeData.colorTheme = "#2563EB"; triggerAutosave() }
        designBinding.swatchEmerald.setOnClickListener { resumeData.colorTheme = "#059669"; triggerAutosave() }
        designBinding.swatchPurple.setOnClickListener { resumeData.colorTheme = "#7C3AED"; triggerAutosave() }
        designBinding.swatchCrimson.setOnClickListener { resumeData.colorTheme = "#DC2626"; triggerAutosave() }
        designBinding.swatchSlate.setOnClickListener { resumeData.colorTheme = "#0F172A"; triggerAutosave() }
        designBinding.swatchAmber.setOnClickListener { resumeData.colorTheme = "#D97706"; triggerAutosave() }

        designBinding.rgFonts.setOnCheckedChangeListener { _, checkedId ->
            if (isBinding) return@setOnCheckedChangeListener
            resumeData.fontFamily = when (checkedId) {
                R.id.rb_font_serif -> "serif"
                R.id.rb_font_monospace -> "monospace"
                else -> "roboto"
            }
            triggerAutosave()
        }

        designBinding.etDesignQrCode.doAfterTextChanged { text ->
            if (isBinding) return@doAfterTextChanged
            resumeData.qrCodeContent = text?.toString().orEmpty()
            triggerAutosave()
        }
    }

    private fun triggerAutosave() {
        if (isBinding) return
        binding.tvAutosaveStatus.text = "Saving..."
        binding.tvAutosaveStatus.setTextColor(getColor(R.color.accent_warning))

        autosaveJob?.cancel()
        autosaveJob = lifecycleScope.launch {
            delay(500)
            viewModel.updateResume(resumeData.toEntity(gson))
            saveSnapshot()
            binding.tvAutosaveStatus.text = "Saved"
            binding.tvAutosaveStatus.setTextColor(getColor(R.color.accent_ats))
        }
    }

    private fun saveSnapshot() {
        val snap = gson.toJson(resumeData)
        if (undoStack.isEmpty() || undoStack.last() != snap) {
            undoStack.add(snap)
            redoStack.clear()
        }
    }

    private fun applySnapshot(json: String) {
        val restored = gson.fromJson(json, ResumeData::class.java)
        resumeData = restored
        bindDataToViews()
        viewModel.updateResume(resumeData.toEntity(gson))
    }

    companion object {
        const val EXTRA_RESUME_ID = "extra_resume_id"
    }
}
