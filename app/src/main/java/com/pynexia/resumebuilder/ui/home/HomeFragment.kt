package com.pynexia.resumebuilder.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.pynexia.resumebuilder.CareerForgeApp
import com.pynexia.resumebuilder.data.model.PersonalInfo
import com.pynexia.resumebuilder.data.model.ResumeData
import com.pynexia.resumebuilder.data.model.ResumeEntity
import com.pynexia.resumebuilder.databinding.FragmentHomeBinding
import com.pynexia.resumebuilder.ui.ats.AtsAnalyzerActivity
import com.pynexia.resumebuilder.ui.coverletter.CoverLetterActivity
import com.pynexia.resumebuilder.ui.editor.ResumeEditorActivity
import com.pynexia.resumebuilder.ui.aitools.InterviewPrepActivity
import com.pynexia.resumebuilder.ui.jobs.JobApplicationsActivity
import com.pynexia.resumebuilder.ui.preview.ResumePreviewActivity
import com.pynexia.resumebuilder.ui.resumes.ResumeAdapter
import com.pynexia.resumebuilder.ui.resumes.ResumeViewModel
import java.util.Calendar

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val resumeViewModel: ResumeViewModel by activityViewModels()
    private lateinit var resumeAdapter: ResumeAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupGreeting()
        setupThemeToggle()
        setupRecyclerView()
        setupClickListeners()
        observeData()
    }

    private fun setupThemeToggle() {
        val prefManager = (requireActivity().application as CareerForgeApp).preferenceManager
        val isDark = prefManager.isDarkModeEnabled
        binding.ivThemeToggleIcon.setImageResource(if (isDark) com.pynexia.resumebuilder.R.drawable.ic_light_mode else com.pynexia.resumebuilder.R.drawable.ic_dark_mode)
        binding.btnThemeToggle.setOnClickListener {
            val newDark = !prefManager.isDarkModeEnabled
            prefManager.isDarkModeEnabled = newDark
            AppCompatDelegate.setDefaultNightMode(
                if (newDark) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
            )
        }
    }

    private fun setupGreeting() {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val greeting = when {
            hour < 12 -> "Good Morning,"
            hour < 17 -> "Good Afternoon,"
            else -> "Good Evening,"
        }
        binding.tvGreeting.text = greeting

        val prefManager = (requireActivity().application as CareerForgeApp).preferenceManager
        binding.tvUserName.text = prefManager.userName
    }

    private fun setupRecyclerView() {
        resumeAdapter = ResumeAdapter(
            onEditClick = { resume -> openEditor(resume.id) },
            onPreviewClick = { resume -> openPreview(resume.id) },
            onDuplicateClick = { resume -> duplicateResume(resume.id) },
            onShareClick = { resume -> openPreview(resume.id) },
            onDeleteClick = { resume -> confirmDelete(resume) }
        )
        binding.rvRecentResumes.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = resumeAdapter
            isNestedScrollingEnabled = false
        }
    }

    private fun setupClickListeners() {
        binding.btnCreateResume.setOnClickListener {
            createNewResume()
        }

        binding.cardToolAts.setOnClickListener {
            startActivity(Intent(requireContext(), AtsAnalyzerActivity::class.java))
        }

        binding.cardToolAiWriter.setOnClickListener {
            startActivity(Intent(requireContext(), AtsAnalyzerActivity::class.java))
        }

        binding.cardToolCoverLetter.setOnClickListener {
            startActivity(Intent(requireContext(), CoverLetterActivity::class.java))
        }

        binding.cardToolJobs.setOnClickListener {
            startActivity(Intent(requireContext(), JobApplicationsActivity::class.java))
        }

        binding.cardToolInterview.setOnClickListener {
            startActivity(Intent(requireContext(), InterviewPrepActivity::class.java))
        }
    }

    private fun observeData() {
        resumeViewModel.recentResumes.observe(viewLifecycleOwner) { resumes ->
            resumeAdapter.submitList(resumes)
        }

        // Observe total resumes to calculate profile strength
        resumeViewModel.totalResumeCount.observe(viewLifecycleOwner) { count ->
            val strength = if (count > 0) 88 else 40
            binding.tvProfilePercentage.text = "$strength% Complete"
            binding.progressProfileStrength.progress = strength
        }

        val app = requireActivity().application as CareerForgeApp
        app.jobApplicationRepository.allApplications.observe(viewLifecycleOwner) { list ->
            binding.tvActiveJobsCount.text = "${list.size} Active"
        }
    }

    private fun createNewResume() {
        val newResume = ResumeData(
            title = "New Professional Resume",
            targetJobTitle = "Senior Specialist",
            templateId = "modern_pro",
            colorTheme = "#2563EB",
            personalInfo = PersonalInfo(
                fullName = (requireActivity().application as CareerForgeApp).preferenceManager.userName,
                email = (requireActivity().application as CareerForgeApp).preferenceManager.userEmail
            )
        )
        resumeViewModel.insertResume(newResume.toEntity()) { newId ->
            openEditor(newId)
        }
    }

    private fun duplicateResume(id: Long) {
        resumeViewModel.duplicateResume(id) { newId ->
            Toast.makeText(requireContext(), "Resume Duplicated Successfully!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun confirmDelete(resume: ResumeEntity) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Delete Resume")
            .setMessage("Are you sure you want to permanently delete \"${resume.title}\"?")
            .setPositiveButton("Delete") { _, _ ->
                resumeViewModel.deleteResume(resume)
                Toast.makeText(requireContext(), "Resume deleted", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun openEditor(id: Long) {
        val intent = Intent(requireContext(), ResumeEditorActivity::class.java).apply {
            putExtra(ResumeEditorActivity.EXTRA_RESUME_ID, id)
        }
        startActivity(intent)
    }

    private fun openPreview(id: Long) {
        val intent = Intent(requireContext(), ResumePreviewActivity::class.java).apply {
            putExtra(ResumePreviewActivity.EXTRA_RESUME_ID, id)
        }
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
