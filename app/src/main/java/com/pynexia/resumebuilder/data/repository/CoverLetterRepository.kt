package com.pynexia.resumebuilder.data.repository

import androidx.lifecycle.LiveData
import com.pynexia.resumebuilder.data.database.CoverLetterDao
import com.pynexia.resumebuilder.data.model.CoverLetterEntity

class CoverLetterRepository(
    private val coverLetterDao: CoverLetterDao
) {
    val allCoverLetters: LiveData<List<CoverLetterEntity>> = coverLetterDao.getAll()
    val totalCount: LiveData<Int> = coverLetterDao.getCount()

    suspend fun getById(id: Long): CoverLetterEntity? = coverLetterDao.getById(id)

    suspend fun insert(coverLetter: CoverLetterEntity): Long {
        return coverLetterDao.insert(coverLetter)
    }

    suspend fun update(coverLetter: CoverLetterEntity) {
        coverLetter.updatedAt = System.currentTimeMillis()
        coverLetterDao.update(coverLetter)
    }

    suspend fun delete(coverLetter: CoverLetterEntity) {
        coverLetterDao.delete(coverLetter)
    }
}
