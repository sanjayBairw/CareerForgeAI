package com.pynexia.resumebuilder.data.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.pynexia.resumebuilder.data.model.CoverLetterEntity

@Dao
interface CoverLetterDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(coverLetter: CoverLetterEntity): Long

    @Update
    suspend fun update(coverLetter: CoverLetterEntity)

    @Delete
    suspend fun delete(coverLetter: CoverLetterEntity)

    @Query("SELECT * FROM cover_letters WHERE id = :id")
    suspend fun getById(id: Long): CoverLetterEntity?

    @Query("SELECT * FROM cover_letters ORDER BY updatedAt DESC")
    fun getAll(): LiveData<List<CoverLetterEntity>>

    @Query("SELECT COUNT(*) FROM cover_letters")
    fun getCount(): LiveData<Int>
}
