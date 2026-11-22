package com.pynexia.resumebuilder.data.repository

import androidx.lifecycle.LiveData
import com.pynexia.resumebuilder.data.database.JobApplicationDao
import com.pynexia.resumebuilder.data.model.JobApplicationEntity

class JobApplicationRepository(
    private val dao: JobApplicationDao
) {
    val allApplications: LiveData<List<JobApplicationEntity>> = dao.getAll()
    val totalCount: LiveData<Int> = dao.getCount()

    fun getByStatus(status: String): LiveData<List<JobApplicationEntity>> = dao.getByStatus(status)
    fun getCountByStatus(status: String): LiveData<Int> = dao.getCountByStatus(status)

    suspend fun getById(id: Long): JobApplicationEntity? = dao.getById(id)

    suspend fun insert(application: JobApplicationEntity): Long {
        return dao.insert(application)
    }

    suspend fun update(application: JobApplicationEntity) {
        dao.update(application)
    }

    suspend fun delete(application: JobApplicationEntity) {
        dao.delete(application)
    }
}
