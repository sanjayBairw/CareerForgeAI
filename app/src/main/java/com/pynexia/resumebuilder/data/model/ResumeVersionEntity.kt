package com.pynexia.resumebuilder.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "resume_versions")
data class ResumeVersionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val resumeId: Long,
    val versionName: String,
    val timestamp: Long = System.currentTimeMillis(),
    val snapshotJson: String,
    val atsScore: Int = 0,
    val changeNote: String = ""
)
