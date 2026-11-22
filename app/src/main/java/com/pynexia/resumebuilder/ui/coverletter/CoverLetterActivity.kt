package com.pynexia.resumebuilder.ui.coverletter

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.pynexia.resumebuilder.CareerForgeApp
import com.pynexia.resumebuilder.data.model.CoverLetterEntity
import com.pynexia.resumebuilder.databinding.ActivityCoverLetterBinding
import kotlinx.coroutines.launch

class CoverLetterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCoverLetterBinding
    private lateinit var adapter: CoverLetterAdapter
    private val repo by lazy { (application as CareerForgeApp).coverLetterRepository }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCoverLetterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbarCoverLetter.setNavigationOnClickListener { finish() }

        setupRecyclerView()
        observeData()

        binding.fabNewCoverLetter.setOnClickListener {
            showCreateDialog()
        }
    }

    private fun setupRecyclerView() {
        adapter = CoverLetterAdapter(
            onEditClick = { cl ->
                val intent = Intent(this, CoverLetterEditorActivity::class.java).apply {
                    putExtra(CoverLetterEditorActivity.EXTRA_CL_ID, cl.id)
                }
                startActivity(intent)
            },
            onShareClick = { cl ->
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_SUBJECT, cl.title)
                    putExtra(Intent.EXTRA_TEXT, "${cl.salutation}\n\n${cl.bodyText}\n\n${cl.signOff}\n${cl.senderName}")
                }
                startActivity(Intent.createChooser(intent, "Share Cover Letter"))
            },
            onDeleteClick = { cl ->
                MaterialAlertDialogBuilder(this)
                    .setTitle("Delete Cover Letter")
                    .setMessage("Are you sure you want to delete \"${cl.title}\"?")
                    .setPositiveButton("Delete") { _, _ ->
                        lifecycleScope.launch {
                            repo.delete(cl)
                            Toast.makeText(this@CoverLetterActivity, "Deleted", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        )
        binding.rvCoverLetters.apply {
            layoutManager = LinearLayoutManager(this@CoverLetterActivity)
            adapter = this@CoverLetterActivity.adapter
        }
    }

    private fun observeData() {
        repo.allCoverLetters.observe(this) { list ->
            adapter.submitList(list)
        }
    }

    private fun showCreateDialog() {
        val etCompany = TextInputEditText(this).apply { hint = "Target Company (e.g. Google, Apex)" }
        val etRole = TextInputEditText(this).apply { hint = "Target Role (e.g. Staff Mobile Engineer)" }

        val layout = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setPadding(50, 40, 50, 20)
            addView(etCompany)
            addView(etRole)
        }

        MaterialAlertDialogBuilder(this)
            .setTitle("Generate AI Cover Letter")
            .setView(layout)
            .setPositiveButton("Generate") { _, _ ->
                val company = etCompany.text.toString().trim()
                val role = etRole.text.toString().trim()
                if (company.isNotBlank() && role.isNotBlank()) {
                    val pref = (application as CareerForgeApp).preferenceManager
                    val newCl = CoverLetterEntity(
                        title = "$company - $role Cover Letter",
                        companyName = company,
                        jobTitle = role,
                        salutation = "Dear $company Hiring Team,",
                        bodyText = "I am writing to express my enthusiastic interest in the $role position at $company. With over 7 years of proven expertise driving high-scale software architectures, performance optimization, and cross-functional team leadership, I am eager to contribute to $company's ongoing innovation.\n\nThroughout my career, I have consistently focused on building scalable, resilient systems that accelerate product delivery while maintaining superior quality standards. I would welcome the opportunity to discuss how my technical leadership and background can help $company achieve its strategic goals.",
                        senderName = pref.userName,
                        senderContact = pref.userEmail
                    )
                    lifecycleScope.launch {
                        val newId = repo.insert(newCl)
                        val intent = Intent(this@CoverLetterActivity, CoverLetterEditorActivity::class.java).apply {
                            putExtra(CoverLetterEditorActivity.EXTRA_CL_ID, newId)
                        }
                        startActivity(intent)
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
