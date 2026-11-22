package com.pynexia.resumebuilder.ui.jobs

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.pynexia.resumebuilder.CareerForgeApp
import com.pynexia.resumebuilder.data.model.JobApplicationEntity
import com.pynexia.resumebuilder.databinding.ActivityJobApplicationsBinding
import kotlinx.coroutines.launch

class JobApplicationsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityJobApplicationsBinding
    private lateinit var adapter: JobApplicationAdapter
    private val repo by lazy { (application as CareerForgeApp).jobApplicationRepository }
    private var allJobsList: List<JobApplicationEntity> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityJobApplicationsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbarJobs.setNavigationOnClickListener { finish() }

        setupRecyclerView()
        setupListeners()
        observeData()
    }

    private fun setupRecyclerView() {
        adapter = JobApplicationAdapter(
            onItemClick = { job -> showEditJobDialog(job) }
        )
        binding.rvJobApplications.apply {
            layoutManager = LinearLayoutManager(this@JobApplicationsActivity)
            adapter = this@JobApplicationsActivity.adapter
        }
    }

    private fun setupListeners() {
        binding.fabAddJob.setOnClickListener {
            showAddJobDialog()
        }

        binding.chipJobAll.setOnClickListener { filterJobs("ALL") }
        binding.chipJobApplied.setOnClickListener { filterJobs("APPLIED") }
        binding.chipJobInterview.setOnClickListener { filterJobs("INTERVIEW") }
        binding.chipJobOffer.setOnClickListener { filterJobs("OFFER") }
        binding.chipJobRejected.setOnClickListener { filterJobs("REJECTED") }
    }

    private fun observeData() {
        repo.allApplications.observe(this) { list ->
            allJobsList = list
            adapter.submitList(list)
        }
    }

    private fun filterJobs(status: String) {
        if (status == "ALL") {
            adapter.submitList(allJobsList)
        } else {
            adapter.submitList(allJobsList.filter { it.status.equals(status, ignoreCase = true) })
        }
    }

    private fun showAddJobDialog() {
        val etCompany = TextInputEditText(this).apply { hint = "Company Name (e.g. Apex Tech)" }
        val etTitle = TextInputEditText(this).apply { hint = "Job Title (e.g. Staff Android Engineer)" }
        val etLocation = TextInputEditText(this).apply { hint = "Location (e.g. Remote / New York)" }
        val etSalary = TextInputEditText(this).apply { hint = "Salary Range (e.g. $180k - $210k)" }
        val etNotes = TextInputEditText(this).apply { hint = "Notes & Recruiter Contact" }

        val layout = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setPadding(50, 40, 50, 20)
            addView(etCompany)
            addView(etTitle)
            addView(etLocation)
            addView(etSalary)
            addView(etNotes)
        }

        MaterialAlertDialogBuilder(this)
            .setTitle("Track New Application")
            .setView(layout)
            .setPositiveButton("Add") { _, _ ->
                val company = etCompany.text.toString().trim()
                val title = etTitle.text.toString().trim()
                if (company.isNotBlank() && title.isNotBlank()) {
                    val entity = JobApplicationEntity(
                        companyName = company,
                        jobTitle = title,
                        location = etLocation.text.toString().trim(),
                        salaryRange = etSalary.text.toString().trim(),
                        notes = etNotes.text.toString().trim(),
                        status = "APPLIED"
                    )
                    lifecycleScope.launch {
                        repo.insert(entity)
                        Toast.makeText(this@JobApplicationsActivity, "Application Added!", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showEditJobDialog(job: JobApplicationEntity) {
        val statuses = arrayOf("APPLIED", "ASSESSMENT", "INTERVIEW", "OFFER", "REJECTED")
        MaterialAlertDialogBuilder(this)
            .setTitle("Update Status: ${job.companyName}")
            .setItems(statuses) { _, which ->
                job.status = statuses[which]
                lifecycleScope.launch {
                    repo.update(job)
                    Toast.makeText(this@JobApplicationsActivity, "Status updated to ${statuses[which]}", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
