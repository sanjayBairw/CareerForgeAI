package com.pynexia.resumebuilder.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.Gson
import com.pynexia.resumebuilder.CareerForgeApp
import com.pynexia.resumebuilder.data.model.UserProfileEntity
import com.pynexia.resumebuilder.databinding.FragmentProfileBinding
import com.pynexia.resumebuilder.ui.jobs.JobApplicationsActivity
import com.pynexia.resumebuilder.ui.resumes.ResumeViewModel
import com.pynexia.resumebuilder.ui.settings.SettingsActivity
import kotlinx.coroutines.launch

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
        binding.tvProfileHeadline.text = pref.userHeadline
        binding.tvProfileEmail.text = pref.userEmail

        binding.cardUserInfo.setOnClickListener { showEditProfileDialog() }
        binding.btnEditProfile.setOnClickListener { showEditProfileDialog() }

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

    private fun showEditProfileDialog() {
        val pref = (requireActivity().application as CareerForgeApp).preferenceManager
        val context = requireContext()

        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            val pad = (20 * resources.displayMetrics.density).toInt()
            setPadding(pad, pad / 2, pad, 0)
        }

        val etName = EditText(context).apply {
            hint = "Full Name"
            setText(pref.userName)
        }
        val etHeadline = EditText(context).apply {
            hint = "Professional Headline (e.g. Lead Software Engineer)"
            setText(pref.userHeadline)
        }
        val etEmail = EditText(context).apply {
            hint = "Email Address"
            setText(pref.userEmail)
        }
        val etPhone = EditText(context).apply {
            hint = "Phone Number"
            setText(pref.userPhone)
        }

        layout.addView(etName)
        layout.addView(etHeadline)
        layout.addView(etEmail)
        layout.addView(etPhone)

        MaterialAlertDialogBuilder(context)
            .setTitle("Edit Master Profile")
            .setMessage("Update your career identity. This information automatically pre-fills newly generated resumes and cover letters.")
            .setView(layout)
            .setPositiveButton("Save Profile") { _, _ ->
                val newName = etName.text.toString().trim()
                val newHeadline = etHeadline.text.toString().trim()
                val newEmail = etEmail.text.toString().trim()
                val newPhone = etPhone.text.toString().trim()

                if (newName.isNotEmpty()) pref.userName = newName
                if (newHeadline.isNotEmpty()) pref.userHeadline = newHeadline
                if (newEmail.isNotEmpty()) pref.userEmail = newEmail
                if (newPhone.isNotEmpty()) pref.userPhone = newPhone

                binding.tvProfileName.text = pref.userName
                binding.tvProfileHeadline.text = pref.userHeadline
                binding.tvProfileEmail.text = pref.userEmail

                val app = requireActivity().application as CareerForgeApp
                lifecycleScope.launch {
                    app.userRepository.saveProfile(
                        UserProfileEntity(
                            id = 1,
                            fullName = pref.userName,
                            headline = pref.userHeadline,
                            email = pref.userEmail,
                            phone = pref.userPhone
                        )
                    )
                }
                Toast.makeText(context, "Profile Updated Successfully!", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
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
