package com.pynexia.resumebuilder.data.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.pynexia.resumebuilder.data.model.JobApplicationEntity

@Dao
interface JobApplicationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(application: JobApplicationEntity): Long

    @Update
    suspend fun update(application: JobApplicationEntity)

    @Delete
    suspend fun delete(application: JobApplicationEntity)

    @Query("SELECT * FROM job_applications WHERE id = :id")
    suspend fun getById(id: Long): JobApplicationEntity?

    @Query("SELECT * FROM job_applications ORDER BY appliedDate DESC")
    fun getAll(): LiveData<List<JobApplicationEntity>>

    @Query("SELECT * FROM job_applications WHERE status = :status ORDER BY appliedDate DESC")
    fun getByStatus(status: String): LiveData<List<JobApplicationEntity>>

    @Query("SELECT COUNT(*) FROM job_applications")
    fun getCount(): LiveData<Int>

    @Query("SELECT COUNT(*) FROM job_applications WHERE status = :status")
    fun getCountByStatus(status: String): LiveData<Int>
}
