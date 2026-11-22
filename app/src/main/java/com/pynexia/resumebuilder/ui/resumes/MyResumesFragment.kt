package com.pynexia.resumebuilder.ui.resumes

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.pynexia.resumebuilder.CareerForgeApp
import com.pynexia.resumebuilder.data.model.PersonalInfo
import com.pynexia.resumebuilder.data.model.ResumeData
import com.pynexia.resumebuilder.data.model.ResumeEntity
import com.pynexia.resumebuilder.databinding.FragmentMyResumesBinding
import com.pynexia.resumebuilder.ui.editor.ResumeEditorActivity
import com.pynexia.resumebuilder.ui.preview.ResumePreviewActivity

class MyResumesFragment : Fragment() {

    private var _binding: FragmentMyResumesBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ResumeViewModel by activityViewModels()
    private lateinit var adapter: ResumeAdapter
    private var allResumesList: List<ResumeEntity> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyResumesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupListeners()
        observeResumes()
    }

    private fun setupRecyclerView() {
        adapter = ResumeAdapter(
            onEditClick = { resume -> openEditor(resume.id) },
            onPreviewClick = { resume -> openPreview(resume.id) },
            onDuplicateClick = { resume -> duplicateResume(resume.id) },
            onShareClick = { resume -> openPreview(resume.id) },
            onDeleteClick = { resume -> confirmDelete(resume) }
        )
        binding.rvResumes.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@MyResumesFragment.adapter
        }
    }

    private fun setupListeners() {
        binding.fabCreateResume.setOnClickListener {
            createNewResume()
        }

        binding.etSearch.doAfterTextChanged { text ->
            filterList(text?.toString().orEmpty())
        }

        binding.chipFilterAll.setOnClickListener { filterList(binding.etSearch.text?.toString().orEmpty()) }
        binding.chipFilterHighAts.setOnClickListener { filterList(binding.etSearch.text?.toString().orEmpty()) }
        binding.chipFilterRecent.setOnClickListener { filterList(binding.etSearch.text?.toString().orEmpty()) }
    }

    private fun observeResumes() {
        viewModel.allActiveResumes.observe(viewLifecycleOwner) { list ->
            allResumesList = list
            filterList(binding.etSearch.text?.toString().orEmpty())
        }
    }

    private fun filterList(query: String) {
        var filtered = if (query.isBlank()) {
            allResumesList
        } else {
            allResumesList.filter {
                it.title.contains(query, ignoreCase = true) ||
                it.targetJobTitle.contains(query, ignoreCase = true)
            }
        }

        if (binding.chipFilterHighAts.isChecked) {
            filtered = filtered.filter { it.lastAtsScore >= 85 }
        }

        adapter.submitList(filtered)
        if (filtered.isEmpty()) {
            binding.layoutEmptyState.visibility = View.VISIBLE
            binding.rvResumes.visibility = View.GONE
        } else {
            binding.layoutEmptyState.visibility = View.GONE
            binding.rvResumes.visibility = View.VISIBLE
        }
    }

    private fun createNewResume() {
        val pref = (requireActivity().application as CareerForgeApp).preferenceManager
        val newResume = ResumeData(
            title = "New Professional Resume",
            targetJobTitle = "Senior Specialist",
            templateId = "modern_pro",
            colorTheme = "#2563EB",
            personalInfo = PersonalInfo(fullName = pref.userName, email = pref.userEmail)
        )
        viewModel.insertResume(newResume.toEntity()) { newId ->
            openEditor(newId)
        }
    }

    private fun duplicateResume(id: Long) {
        viewModel.duplicateResume(id) {
            Toast.makeText(requireContext(), "Resume Duplicated", Toast.LENGTH_SHORT).show()
        }
    }

    private fun confirmDelete(resume: ResumeEntity) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Delete Resume")
            .setMessage("Are you sure you want to permanently delete \"${resume.title}\"?")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteResume(resume)
                Toast.makeText(requireContext(), "Resume Deleted", Toast.LENGTH_SHORT).show()
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
