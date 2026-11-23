package com.pynexia.resumebuilder.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.pynexia.resumebuilder.R
import com.pynexia.resumebuilder.databinding.ActivityMainBinding
import com.pynexia.resumebuilder.ui.aitools.AiToolsFragment
import com.pynexia.resumebuilder.ui.home.HomeFragment
import com.pynexia.resumebuilder.ui.profile.ProfileFragment
import com.pynexia.resumebuilder.ui.resumes.MyResumesFragment
import com.pynexia.resumebuilder.ui.templates.TemplatesFragment
import com.pynexia.resumebuilder.utils.applySystemBarsPadding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val homeFragment = HomeFragment()
    private val myResumesFragment = MyResumesFragment()
    private val templatesFragment = TemplatesFragment()
    private val aiToolsFragment = AiToolsFragment()
    private val profileFragment = ProfileFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.navHostFragment.applySystemBarsPadding(applyTop = true, applyBottom = false)

        if (savedInstanceState == null) {
            loadFragment(homeFragment)
        }

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    loadFragment(homeFragment)
                    true
                }
                R.id.navigation_my_resumes -> {
                    loadFragment(myResumesFragment)
                    true
                }
                R.id.navigation_templates -> {
                    loadFragment(templatesFragment)
                    true
                }
                R.id.navigation_ai_tools -> {
                    loadFragment(aiToolsFragment)
                    true
                }
                R.id.navigation_profile -> {
                    loadFragment(profileFragment)
                    true
                }
                else -> false
            }
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.nav_host_fragment, fragment)
            .commit()
    }
}
