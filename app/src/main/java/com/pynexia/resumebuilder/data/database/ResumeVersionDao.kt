package com.pynexia.resumebuilder.data.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.pynexia.resumebuilder.data.model.ResumeVersionEntity

@Dao
interface ResumeVersionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(version: ResumeVersionEntity): Long

    @Query("SELECT * FROM resume_versions WHERE resumeId = :resumeId ORDER BY timestamp DESC")
    fun getVersionsForResume(resumeId: Long): LiveData<List<ResumeVersionEntity>>

    @Query("DELETE FROM resume_versions WHERE resumeId = :resumeId")
    suspend fun deleteVersionsForResume(resumeId: Long)
}
