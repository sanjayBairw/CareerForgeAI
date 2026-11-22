package com.pynexia.resumebuilder

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.pynexia.resumebuilder.data.database.AppDatabase
import com.pynexia.resumebuilder.data.repository.CoverLetterRepository
import com.pynexia.resumebuilder.data.repository.JobApplicationRepository
import com.pynexia.resumebuilder.data.repository.ResumeRepository
import com.pynexia.resumebuilder.data.repository.UserRepository
import com.pynexia.resumebuilder.utils.PreferenceManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class CareerForgeApp : Application() {
    private val applicationScope = CoroutineScope(SupervisorJob())

    val database by lazy { AppDatabase.getDatabase(this, applicationScope) }
    val resumeRepository by lazy { ResumeRepository(database.resumeDao(), database.resumeVersionDao()) }
    val coverLetterRepository by lazy { CoverLetterRepository(database.coverLetterDao()) }
    val jobApplicationRepository by lazy { JobApplicationRepository(database.jobApplicationDao()) }
    val userRepository by lazy { UserRepository(database.userProfileDao()) }
    val preferenceManager by lazy { PreferenceManager(this) }

    override fun onCreate() {
        super.onCreate()
        if (preferenceManager.isDarkModeEnabled) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }

        applicationScope.launch(Dispatchers.IO) {
            if (database.resumeDao().getCountDirect() == 0) {
                AppDatabase.populateInitialData(database)
            }
        }
    }
}
