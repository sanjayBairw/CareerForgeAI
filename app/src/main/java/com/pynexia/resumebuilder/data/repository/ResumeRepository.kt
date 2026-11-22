package com.pynexia.resumebuilder.data.repository

import androidx.lifecycle.LiveData
import com.google.gson.Gson
import com.pynexia.resumebuilder.data.database.ResumeDao
import com.pynexia.resumebuilder.data.database.ResumeVersionDao
import com.pynexia.resumebuilder.data.model.ResumeData
import com.pynexia.resumebuilder.data.model.ResumeEntity
import com.pynexia.resumebuilder.data.model.ResumeVersionEntity

class ResumeRepository(
    private val resumeDao: ResumeDao,
    private val versionDao: ResumeVersionDao,
    private val gson: Gson = Gson()
) {
    val allActiveResumes: LiveData<List<ResumeEntity>> = resumeDao.getAllActiveResumes()
    val recentResumes: LiveData<List<ResumeEntity>> = resumeDao.getRecentResumes(5)
    val archivedResumes: LiveData<List<ResumeEntity>> = resumeDao.getArchivedResumes()
    val totalResumeCount: LiveData<Int> = resumeDao.getResumeCount()
    val averageAtsScore: LiveData<Float?> = resumeDao.getAverageAtsScore()

    fun searchResumes(query: String): LiveData<List<ResumeEntity>> = resumeDao.searchResumes(query)

    suspend fun getResumeById(id: Long): ResumeEntity? = resumeDao.getResumeById(id)

    fun getResumeByIdFlow(id: Long): LiveData<ResumeEntity?> = resumeDao.getResumeByIdFlow(id)

    suspend fun insertResume(resume: ResumeEntity): Long {
        return resumeDao.insert(resume)
    }

    suspend fun updateResume(resume: ResumeEntity) {
        resume.updatedAt = System.currentTimeMillis()
        resumeDao.update(resume)
    }

    suspend fun deleteResume(resume: ResumeEntity) {
        versionDao.deleteVersionsForResume(resume.id)
        resumeDao.delete(resume)
    }

    suspend fun duplicateResume(sourceId: Long): Long {
        val source = resumeDao.getResumeById(sourceId) ?: return -1L
        val copy = source.copy(
            id = 0,
            title = "${source.title} (Copy)",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            versionNumber = 1
        )
        return resumeDao.insert(copy)
    }

    suspend fun createVersionSnapshot(resumeId: Long, versionName: String, note: String = "") {
        val entity = resumeDao.getResumeById(resumeId) ?: return
        val snapshot = ResumeVersionEntity(
            resumeId = resumeId,
            versionName = versionName,
            timestamp = System.currentTimeMillis(),
            snapshotJson = gson.toJson(entity),
            atsScore = entity.lastAtsScore,
            changeNote = note
        )
        versionDao.insert(snapshot)
    }

    fun getVersionsForResume(resumeId: Long): LiveData<List<ResumeVersionEntity>> {
        return versionDao.getVersionsForResume(resumeId)
    }

    suspend fun restoreVersion(version: ResumeVersionEntity): Boolean {
        return try {
            val restored = gson.fromJson(version.snapshotJson, ResumeEntity::class.java)
            val updated = restored.copy(
                id = version.resumeId,
                updatedAt = System.currentTimeMillis(),
                versionNumber = restored.versionNumber + 1
            )
            resumeDao.update(updated)
            true
        } catch (e: Exception) {
            false
        }
    }
}
