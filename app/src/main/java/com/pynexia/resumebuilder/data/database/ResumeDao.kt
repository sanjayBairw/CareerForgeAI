package com.pynexia.resumebuilder.data.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.pynexia.resumebuilder.data.model.ResumeEntity

@Dao
interface ResumeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(resume: ResumeEntity): Long

    @Update
    suspend fun update(resume: ResumeEntity)

    @Delete
    suspend fun delete(resume: ResumeEntity)

    @Query("DELETE FROM resumes WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM resumes WHERE id = :id")
    suspend fun getResumeById(id: Long): ResumeEntity?

    @Query("SELECT * FROM resumes WHERE id = :id")
    fun getResumeByIdFlow(id: Long): LiveData<ResumeEntity?>

    @Query("SELECT * FROM resumes WHERE isArchived = 0 ORDER BY updatedAt DESC")
    fun getAllActiveResumes(): LiveData<List<ResumeEntity>>

    @Query("SELECT * FROM resumes WHERE isArchived = 0 ORDER BY updatedAt DESC LIMIT :limit")
    fun getRecentResumes(limit: Int = 5): LiveData<List<ResumeEntity>>

    @Query("SELECT * FROM resumes WHERE isArchived = 1 ORDER BY updatedAt DESC")
    fun getArchivedResumes(): LiveData<List<ResumeEntity>>

    @Query("SELECT * FROM resumes WHERE isArchived = 0 AND (title LIKE '%' || :query || '%' OR targetJobTitle LIKE '%' || :query || '%') ORDER BY updatedAt DESC")
    fun searchResumes(query: String): LiveData<List<ResumeEntity>>

    @Query("SELECT COUNT(*) FROM resumes WHERE isArchived = 0")
    fun getResumeCount(): LiveData<Int>

    @Query("SELECT COUNT(*) FROM resumes")
    suspend fun getCountDirect(): Int

    @Query("SELECT AVG(lastAtsScore) FROM resumes WHERE isArchived = 0")
    fun getAverageAtsScore(): LiveData<Float?>
}
