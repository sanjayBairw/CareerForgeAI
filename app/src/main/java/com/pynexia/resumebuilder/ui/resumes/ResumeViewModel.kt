package com.pynexia.resumebuilder.ui.resumes

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.pynexia.resumebuilder.CareerForgeApp
import com.pynexia.resumebuilder.data.model.ResumeEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ResumeViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = (application as CareerForgeApp).resumeRepository

    val allActiveResumes: LiveData<List<ResumeEntity>> = repository.allActiveResumes
    val recentResumes: LiveData<List<ResumeEntity>> = repository.recentResumes
    val totalResumeCount: LiveData<Int> = repository.totalResumeCount
    val averageAtsScore: LiveData<Float?> = repository.averageAtsScore

    fun searchResumes(query: String): LiveData<List<ResumeEntity>> = repository.searchResumes(query)

    fun getResumeByIdFlow(id: Long): LiveData<ResumeEntity?> = repository.getResumeByIdFlow(id)

    suspend fun getResumeById(id: Long): ResumeEntity? = withContext(Dispatchers.IO) {
        repository.getResumeById(id)
    }

    fun insertResume(resume: ResumeEntity, onComplete: (Long) -> Unit = {}) {
        viewModelScope.launch(Dispatchers.IO) {
            val id = repository.insertResume(resume)
            withContext(Dispatchers.Main) {
                onComplete(id)
            }
        }
    }

    fun updateResume(resume: ResumeEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateResume(resume)
        }
    }

    fun deleteResume(resume: ResumeEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteResume(resume)
        }
    }

    fun duplicateResume(sourceId: Long, onComplete: (Long) -> Unit = {}) {
        viewModelScope.launch(Dispatchers.IO) {
            val newId = repository.duplicateResume(sourceId)
            withContext(Dispatchers.Main) {
                onComplete(newId)
            }
        }
    }
}
