package com.pynexia.resumebuilder.ui.templates

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.pynexia.resumebuilder.CareerForgeApp
import com.pynexia.resumebuilder.data.model.PersonalInfo
import com.pynexia.resumebuilder.data.model.ResumeData
import com.pynexia.resumebuilder.databinding.FragmentTemplatesBinding
import com.pynexia.resumebuilder.pdf.TemplateRegistry
import com.pynexia.resumebuilder.ui.editor.ResumeEditorActivity
import com.pynexia.resumebuilder.ui.resumes.ResumeViewModel

class TemplatesFragment : Fragment() {

    private var _binding: FragmentTemplatesBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ResumeViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTemplatesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = TemplateAdapter(TemplateRegistry.templates) { template, color ->
            val pref = (requireActivity().application as CareerForgeApp).preferenceManager
            val newResume = ResumeData(
                title = "${template.name} Resume",
                targetJobTitle = "Professional",
                templateId = template.id,
                colorTheme = color,
                personalInfo = PersonalInfo(fullName = pref.userName, email = pref.userEmail)
            )
            viewModel.insertResume(newResume.toEntity()) { newId ->
                val intent = Intent(requireContext(), ResumeEditorActivity::class.java).apply {
                    putExtra(ResumeEditorActivity.EXTRA_RESUME_ID, newId)
                }
                startActivity(intent)
            }
        }

        binding.rvTemplates.apply {
            layoutManager = LinearLayoutManager(requireContext())
            this.adapter = adapter
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
