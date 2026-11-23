package com.pynexia.resumebuilder.ui.settings

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.pynexia.resumebuilder.CareerForgeApp
import com.pynexia.resumebuilder.databinding.ActivitySettingsBinding
import com.pynexia.resumebuilder.utils.applySystemBarsPadding

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private val prefManager by lazy { (application as CareerForgeApp).preferenceManager }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.root.applySystemBarsPadding(applyTop = true, applyBottom = true)

        binding.toolbarSettings.setNavigationOnClickListener { finish() }

        loadSettings()
        setupListeners()
    }

    private fun loadSettings() {
        binding.switchDarkMode.setOnCheckedChangeListener(null)
        binding.switchDarkMode.isChecked = prefManager.isDarkModeEnabled
        binding.etAiEndpoint.setText(prefManager.aiEndpointUrl)
        binding.etAiApiKey.setText(prefManager.aiApiKey)
        binding.etAiModel.setText(prefManager.aiModelName)
    }

    private fun setupListeners() {
        binding.switchDarkMode.setOnCheckedChangeListener { buttonView, isChecked ->
            if (!buttonView.isPressed) return@setOnCheckedChangeListener
            prefManager.isDarkModeEnabled = isChecked
            AppCompatDelegate.setDefaultNightMode(
                if (isChecked) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
            )
        }

        binding.btnSaveSettings.setOnClickListener {
            prefManager.aiEndpointUrl = binding.etAiEndpoint.text.toString().trim()
            prefManager.aiApiKey = binding.etAiApiKey.text.toString().trim()
            prefManager.aiModelName = binding.etAiModel.text.toString().trim()
            Toast.makeText(this, "AI Configuration Saved Securely", Toast.LENGTH_SHORT).show()
        }
    }
}
