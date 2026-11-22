package com.pynexia.resumebuilder.ui.aitools

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.pynexia.resumebuilder.databinding.FragmentAiToolsBinding
import com.pynexia.resumebuilder.ui.ats.AtsAnalyzerActivity
import com.pynexia.resumebuilder.ui.coverletter.CoverLetterActivity

class AiToolsFragment : Fragment() {

    private var _binding: FragmentAiToolsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAiToolsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.cardAiAtsAnalyzer.setOnClickListener {
            startActivity(Intent(requireContext(), AtsAnalyzerActivity::class.java))
        }

        binding.cardAiCoverLetter.setOnClickListener {
            startActivity(Intent(requireContext(), CoverLetterActivity::class.java))
        }

        binding.cardAiInterviewPrep.setOnClickListener {
            startActivity(Intent(requireContext(), InterviewPrepActivity::class.java))
        }

        binding.cardAiCompareResumes.setOnClickListener {
            startActivity(Intent(requireContext(), ResumeComparisonActivity::class.java))
        }

        binding.cardAiLinkedinOptimizer.setOnClickListener {
            startActivity(Intent(requireContext(), AtsAnalyzerActivity::class.java))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
