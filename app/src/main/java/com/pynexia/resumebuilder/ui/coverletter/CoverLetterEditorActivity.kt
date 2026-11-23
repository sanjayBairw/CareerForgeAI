package com.pynexia.resumebuilder.ui.coverletter

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.pynexia.resumebuilder.CareerForgeApp
import com.pynexia.resumebuilder.ai.AiResumeAssistant
import com.pynexia.resumebuilder.data.model.CoverLetterEntity
import com.pynexia.resumebuilder.databinding.ActivityCoverLetterEditorBinding
import com.pynexia.resumebuilder.utils.applySystemBarsPadding
import kotlinx.coroutines.launch

class CoverLetterEditorActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCoverLetterEditorBinding
    private val repo by lazy { (application as CareerForgeApp).coverLetterRepository }
    private lateinit var aiAssistant: AiResumeAssistant
    private var coverLetterId: Long = 0
    private var coverLetter: CoverLetterEntity? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCoverLetterEditorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.root.applySystemBarsPadding(applyTop = true, applyBottom = true)

        aiAssistant = AiResumeAssistant(this)
        coverLetterId = intent.getLongExtra(EXTRA_CL_ID, 0L)

        binding.toolbarClEditor.setNavigationOnClickListener { finish() }

        loadData()
        setupListeners()
    }

    private fun loadData() {
        lifecycleScope.launch {
            val entity = repo.getById(coverLetterId)
            if (entity != null) {
                coverLetter = entity
                binding.etClTitle.setText(entity.title)
                binding.etClCompany.setText(entity.companyName)
                binding.etClJobTitle.setText(entity.jobTitle)
                binding.etClSalutation.setText(entity.salutation)
                binding.etClBody.setText(entity.bodyText)
            }
        }
    }

    private fun setupListeners() {
        binding.btnSaveCoverLetter.setOnClickListener {
            saveCoverLetter()
            Toast.makeText(this, "Saved!", Toast.LENGTH_SHORT).show()
            finish()
        }

        binding.btnAiPolishLetter.setOnClickListener {
            val currentBody = binding.etClBody.text.toString()
            if (currentBody.isNotBlank()) {
                lifecycleScope.launch {
                    val polished = aiAssistant.polishSentence(currentBody)
                    binding.etClBody.setText(polished)
                    Toast.makeText(this@CoverLetterEditorActivity, "Tone polished with executive phrasing!", Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.btnExportLetterPdf.setOnClickListener {
            saveCoverLetter()
            val cl = coverLetter ?: return@setOnClickListener
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, cl.title)
                putExtra(Intent.EXTRA_TEXT, "${cl.salutation}\n\n${cl.bodyText}\n\n${cl.signOff}\n${cl.senderName}\n${cl.senderContact}")
            }
            startActivity(Intent.createChooser(intent, "Share Cover Letter"))
        }
    }

    private fun saveCoverLetter() {
        val cl = coverLetter ?: return
        cl.title = binding.etClTitle.text.toString().trim()
        cl.companyName = binding.etClCompany.text.toString().trim()
        cl.jobTitle = binding.etClJobTitle.text.toString().trim()
        cl.salutation = binding.etClSalutation.text.toString().trim()
        cl.bodyText = binding.etClBody.text.toString().trim()
        lifecycleScope.launch {
            repo.update(cl)
        }
    }

    companion object {
        const val EXTRA_CL_ID = "extra_cl_id"
    }
}
