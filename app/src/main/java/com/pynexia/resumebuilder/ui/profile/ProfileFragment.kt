package com.pynexia.resumebuilder.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.gson.Gson
import com.pynexia.resumebuilder.CareerForgeApp
import com.pynexia.resumebuilder.databinding.FragmentProfileBinding
import com.pynexia.resumebuilder.ui.jobs.JobApplicationsActivity
import com.pynexia.resumebuilder.ui.resumes.ResumeViewModel
import com.pynexia.resumebuilder.ui.settings.SettingsActivity

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ResumeViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val pref = (requireActivity().application as CareerForgeApp).preferenceManager
        binding.tvProfileName.text = pref.userName
        binding.tvProfileEmail.text = pref.userEmail

        val isDark = pref.isDarkModeEnabled
        binding.switchDarkModeProfile.setOnCheckedChangeListener(null)
        binding.switchDarkModeProfile.isChecked = isDark
        binding.ivThemeIcon.setImageResource(if (isDark) com.pynexia.resumebuilder.R.drawable.ic_light_mode else com.pynexia.resumebuilder.R.drawable.ic_dark_mode)
        binding.switchDarkModeProfile.setOnCheckedChangeListener { buttonView, isChecked ->
            if (!buttonView.isPressed) return@setOnCheckedChangeListener
            pref.isDarkModeEnabled = isChecked
            AppCompatDelegate.setDefaultNightMode(
                if (isChecked) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
            )
        }

        val app = requireActivity().application as CareerForgeApp
        app.jobApplicationRepository.allApplications.observe(viewLifecycleOwner) { list ->
            binding.tvJobsStatusBadge.text = "${list.size} Active"
        }

        binding.cardJobTracker.setOnClickListener {
            startActivity(Intent(requireContext(), JobApplicationsActivity::class.java))
        }

        binding.btnExportBackup.setOnClickListener {
            exportJsonBackup()
        }

        binding.btnOpenSettings.setOnClickListener {
            startActivity(Intent(requireContext(), SettingsActivity::class.java))
        }
    }

    private fun exportJsonBackup() {
        viewModel.allActiveResumes.value?.let { resumes ->
            val json = Gson().toJson(resumes)
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, "CareerForge AI - Resumes Cloud Backup")
                putExtra(Intent.EXTRA_TEXT, json)
            }
            startActivity(Intent.createChooser(shareIntent, "Export Resumes JSON Backup"))
        } ?: Toast.makeText(requireContext(), "No resumes to export", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
